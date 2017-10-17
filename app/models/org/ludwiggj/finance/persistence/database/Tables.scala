package models.org.ludwiggj.finance.persistence.database

import java.sql.Timestamp

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import models.org.ludwiggj.finance.domain.TransactionType.aTransactionType
import models.org.ludwiggj.finance.domain.{Transaction, _}
import org.joda.time.LocalDate
import slick.lifted.MappedTo
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.ListMap
import play.api.Logger

object PKs {

  final case class PK[A](value: Long) extends AnyVal with MappedTo[Long] with Ordered[PK[A]] {
    def compare(that: PK[A]): Int = value.compare(that.value)
  }

}

// Slick data model trait for extension, choice of backend or usage in the cake pattern.
trait Tables {

  // Self-type indicating that our tables must be mixed in with a Profile
  this: Profile =>

  import PKs.PK

  // Whatever that Profile is, we import it as normal:
  import profile.api._

  // Map joda LocalDate into sql Timestamp
  implicit val jodaDateTimeType = MappedColumnType.base[LocalDate, Timestamp](
    ld => new Timestamp(ld.toDateTimeAtStartOfDay.getMillis),
    ts => new LocalDate(ts.getTime)
  )

  // ---------------
  // FUNDS
  // ---------------

  // ------------------------
  // FUNDS - table definition
  // ------------------------

  implicit val fundNameMapper = MappedColumnType.base[FundName, String](
    fn => fn.name,
    s => FundName(s)
  )

  case class FundRow(
                      id: PK[FundTable],
                      name: FundName
                    )

  class FundTable(tag: Tag) extends Table[FundRow](tag, "FUNDS") {
    val id = column[PK[FundTable]]("ID", O.AutoInc, O.PrimaryKey)
    val name = column[FundName]("NAME", O.Length(254, varying = true))

    def * = (id, name) <> (FundRow.tupled, FundRow.unapply)

    val index1 = index("NAME", name, unique = true)
  }

  // ---------------
  // FUNDS - API
  // ---------------

  object Funds extends TableQuery(new FundTable(_)) {

    def insert(fundName: FundName): DBIO[PK[FundTable]] = {
      this returning this.map(_.id) += FundRow(PK[FundTable](0L), fundName)
    }

    def get(fundName: FundName): DBIO[Option[FundRow]] = {
      this.filter {
        _.name === fundName
      }.result.headOption
    }

    def getOrInsert(fundName: FundName): DBIO[PK[FundTable]] = {
      get(fundName).flatMap {
        _ match {
          case Some(fundRow) => DBIO.successful(fundRow.id)
          case None => insert(fundName)
        }
      }
    }

    // TODO - not tested directly
    def getNameChangeIdTuples(fundChanges: List[FundChange]): DBIO[List[Option[(PK[FundTable], PK[FundTable])]]] = {
      def getNameChangeIdTuple(fundChange: FundChange): DBIO[Option[(PK[FundTable], PK[FundTable])]] = {
        (for {
          oldId <- get(fundChange.oldFundName)
          newId <- get(fundChange.newFundName)
        } yield (oldId.map(_.id), newId.map(_.id))).map {
          // TODO - This last step might be unnecessary!?!
          optionTuple =>
            for (a <- optionTuple._1; b <- optionTuple._2) yield (a, b)
        }
      }

      // TODO - do the flatten here to remove the Option from the result ?
      DBIO.sequence(fundChanges.map(getNameChangeIdTuple))
    }
  }

  // ---------------
  // PRICES
  // ---------------

  // -------------------------
  // PRICES - table definition
  // -------------------------

  case class PriceRow(
                       id: PK[PriceTable],
                       // TODO - Is fundId still needed here?
                       fundId: PK[FundTable],
                       date: LocalDate,
                       price: BigDecimal
                     )

  class PriceTable(tag: Tag) extends Table[PriceRow](tag, "PRICES") {
    val id = column[PK[PriceTable]]("ID", O.AutoInc, O.PrimaryKey)
    val fundId = column[PK[FundTable]]("FUND_ID")
    val date = column[LocalDate]("PRICE_DATE")
    val price = column[BigDecimal]("PRICE")

    def * = (id, fundId, date, price) <> (PriceRow.tupled, PriceRow.unapply)

    val index1 = index("FUND_ID_DATE", (fundId, date), unique = true)

    // Foreign key referencing Funds
    lazy val fundsFk = foreignKey(
      "PRICES_FUNDS_FK", fundId, Funds
    )(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  // ---------------
  // PRICES - API
  // ---------------

  object Prices extends TableQuery(new PriceTable(_)) {

    type LatestPriceForEachFundMap = Map[PK[FundTable], Price]
    type FundChangeMap = Map[PK[FundTable], PK[FundTable]]

    import models.org.ludwiggj.finance.LocalDateOrdering._

    def insert(fundId: PK[FundTable], date: LocalDate, price: BigDecimal): DBIO[PK[PriceTable]] = {
      this returning this.map(_.id) += PriceRow(PK[PriceTable](0L), fundId, date, price)
    }

    def insert(price: Price): DBIO[PK[PriceTable]] = {
      Funds.getOrInsert(price.fundName).flatMap { fundId =>
        get(price.fundName, price.date).flatMap {
          _ match {
            case Some(priceRow) => DBIO.successful(priceRow.id)
            case None => {
              try {
                insert(fundId, price.date, price.inPounds)
              } catch {
                case ex: MySQLIntegrityConstraintViolationException =>
                  // TODO - Exception type?
                  DBIO.failed(new Exception(s"Price: ${ex.getMessage} New price [$price]"))
              }
            }
          }
        }
      }
    }

    def insert(prices: List[Price]): DBIO[List[PK[PriceTable]]] = {
      DBIO.sequence(prices.map(insert))
    }

    def get(fundName: FundName, date: LocalDate): DBIO[Option[PriceRow]] = {
      Prices.filter {
        _.date === date
      }.join(Funds).on((p, f) => (p.fundId === f.id) && (f.name === fundName))
        .result.map {
        _.map {
          case (priceRow, _) => priceRow
        }.headOption
      }
    }

    def latestPrices(dateOfInterest: LocalDate): DBIO[LatestPriceForEachFundMap] = {
      def latestPrices() = {
        val dateDifference = SimpleFunction.binary[LocalDate, LocalDate, Int]("DATEDIFF")

        Prices
          .filter { p => ((dateDifference(p.date, dateOfInterest)) <= 1) && (p.price =!= BigDecimal(0.0)) }
          .groupBy(p => p.fundId)
          .map { case (fundId, group) => {
            (fundId, group.map(_.date).max)
          }
          }
      }

      def latestPriceForEachFund(): DBIO[LatestPriceForEachFundMap] = {
        val pricesList =
          (for {
            (fundId, lastPriceDate) <- latestPrices()
            f <- Funds if f.id === fundId
            p <- Prices if p.fundId === fundId && p.date === lastPriceDate
          } yield (p.fundId, f.name, lastPriceDate, p.price)
            ).result

        pricesList.map { prices =>
          prices.foldLeft(Map[PK[FundTable], Price]()) {
            (m, priceInfo) => {
              val (fundId, fundName, lastPriceDate, price) = priceInfo
              m + (fundId -> Price(fundName, lastPriceDate.get, price))
            }
          }
        }
      }

      def fundChanges(): DBIO[FundChangeMap] = {
        val changes = FundChange.getFundChangesUpUntil(dateOfInterest.plusDays(1))
        Funds.getNameChangeIdTuples(changes).map {
          _.flatten.toMap
        }
      }

      def pricesAdjustedByFundChanges(fundChangesMap: FundChangeMap,
                                      latestPricesMap: LatestPriceForEachFundMap): LatestPriceForEachFundMap = {
        val adjustedPrices = for {
          (fundId, latestPrice) <- latestPricesMap
          newFundId <- fundChangesMap.get(fundId)
          newFundPrice <- latestPricesMap.get(newFundId)
        } yield if (newFundPrice.date > latestPrice.date) {
          (fundId, newFundPrice.copy(fundName = latestPrice.fundName))
        } else (fundId, latestPrice)
        adjustedPrices
      }

      fundChanges().flatMap { fundChangesMap =>
        latestPriceForEachFund().map { latestPricesMap =>
          val adjustedPrices = pricesAdjustedByFundChanges(fundChangesMap, latestPricesMap)
          val unadjustedPrices = latestPricesMap.filterKeys(!adjustedPrices.contains(_))
          adjustedPrices ++ unadjustedPrices
        }
      }
    }
  }

  // ---------------
  // TRANSACTIONS
  // ---------------

  // -------------------------------
  // TRANSACTIONS - table definition
  // -------------------------------

  implicit val transactionTypeMapper = MappedColumnType.base[TransactionType, String](
    tt => tt.name,
    s => aTransactionType(s)
  )

  case class TransactionRow(
                             id: PK[TransactionTable],
                             fundId: PK[FundTable],
                             userId: PK[UserTable],
                             date: LocalDate,
                             description: TransactionType,
                             amountIn: Option[BigDecimal] = None,
                             amountOut: Option[BigDecimal] = None,
                             priceDate: LocalDate,
                             units: BigDecimal
                           )

  class TransactionTable(tag: Tag) extends Table[TransactionRow](tag, "TRANSACTIONS") {
    val id = column[PK[TransactionTable]]("ID", O.AutoInc, O.PrimaryKey)
    val fundId = column[PK[FundTable]]("FUND_ID")
    val userId = column[PK[UserTable]]("USER_ID")
    val date = column[LocalDate]("TRANSACTION_DATE")
    val description = column[TransactionType]("DESCRIPTION", O.Length(254, varying = true))
    val amountIn = column[Option[BigDecimal]]("AMOUNT_IN")
    val amountOut = column[Option[BigDecimal]]("AMOUNT_OUT")
    val priceDate = column[LocalDate]("PRICE_DATE")
    val units = column[BigDecimal]("UNITS")

    def * = (
      id, fundId, userId, date, description, amountIn, amountOut, priceDate, units
    ) <> (TransactionRow.tupled, TransactionRow.unapply)

    // Foreign keys
    lazy val fundsFk = foreignKey(
      "TRANSACTIONS_FUNDS_FK", fundId, Funds
    )(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

    lazy val pricesFk = foreignKey(
      "TRANSACTIONS_PRICES_FK", (fundId, priceDate), Prices
    )(r => (r.fundId, r.date), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

    lazy val usersFk = foreignKey(
      "TRANSACTIONS_USERS_FK", userId, Users
    )(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }

  // ------------------
  // TRANSACTIONS - API
  // ------------------

  object Transactions extends TableQuery(new TransactionTable(_)) {

    implicit class TransactionQueryFilterableOnAmount(txQuery: Query[TransactionTable, TransactionRow, Seq]) {
      def withAmountIn(amountIn: Option[BigDecimal]) = {
        amountIn match {
          case Some(amount) => txQuery.filter(_.amountIn === amount)
          case None => txQuery.filter(_.amountIn.isEmpty)
        }
      }

      def withAmountOut(amountOut: Option[BigDecimal]) = {
        amountOut match {
          case Some(amount) => txQuery.filter(_.amountOut === amount)
          case None => txQuery.filter(_.amountOut.isEmpty)
        }
      }
    }

    def insert(fundId: PK[FundTable], userId: PK[UserTable], date: LocalDate, description: TransactionType,
               in: Option[BigDecimal], out: Option[BigDecimal], priceDate: LocalDate,
               units: BigDecimal): DBIO[PK[TransactionTable]] = {
      this returning this.map(_.id) +=
        TransactionRow(PK[TransactionTable](0L), fundId, userId, date, description, in, out, priceDate, units)
    }

    def insert(tx: Transaction): DBIO[PK[TransactionTable]] = {
      get(tx.fundName, tx.userName, tx.date, tx.description, tx.in, tx.out, tx.priceDate, tx.units).flatMap {
        _ match {
          case Some(txRow) => DBIO.successful(txRow.id)
          case None =>
            // NOTE - Flipped the order of these DB actions around i.e. now Price followed by User. This should be OK.
            Prices.insert(tx.price) andThen Users.getOrInsert(tx.userName).flatMap { userId =>
              // NOTE - Could use fundId returned by Prices.insert rather than fetching it explicitly, but
              //        Prices.insert should really return a priceId
              Funds.get(tx.fundName).flatMap {
                _ match {
                  case Some(fundsRow) => insert(
                    fundsRow.id, userId, tx.date, tx.description, tx.in,
                    tx.out, tx.priceDate, tx.units
                  )
                  // Exception type?
                  case None => DBIO.failed(
                    new Exception(s"Could not insert Transaction: fund ${tx.fundName} not found")
                  )
                }
              }
            }
        }
      }
    }

    def insert(transactions: List[Transaction]): DBIO[List[PK[TransactionTable]]] = {
      DBIO.sequence(transactions.map(insert))
    }

    def get(fundName: FundName, userName: String, date: LocalDate, description: TransactionType, in: Option[BigDecimal],
            out: Option[BigDecimal], priceDate: LocalDate, units: BigDecimal): DBIO[Option[TransactionRow]] = {

      val txQuery = Transactions
        .join(Funds).on { case (tx, fund) => tx.fundId === fund.id }
        .join(Users).on { case ((tx, _), user) => tx.userId === user.id }
        .filter { case ((tx, fund), user) =>
          fund.name === fundName && user.name === userName && tx.date === date && tx.description === description &&
            tx.priceDate === priceDate && tx.units === units
        }
        .map {
          case ((tx, _), _) => tx
        }
        .withAmountIn(in)
        .withAmountOut(out)

      txQuery.result.headOption
    }

    private def getDates(transactionFilter: (TransactionTable) => Rep[Boolean]): DBIO[List[LocalDate]] = {
      Transactions.filter {
        transactionFilter(_)
      }.map {
        _.date
      }.sorted.result.map {
        _.distinct.reverse.toList
      }
    }

    def getRegularInvestmentDates(): DBIO[List[LocalDate]] = {
      def transactionFilter(t: TransactionTable) = {
        t.description === (InvestmentRegular: TransactionType)
      }

      getDates(transactionFilter _)
    }

    def getDatesSince(dateOfInterest: LocalDate): DBIO[List[LocalDate]] = {
      def transactionFilter(t: TransactionTable) = {
        t.date > dateOfInterest
      }

      getDates(transactionFilter _)
    }

    def getDatesSince(dateOfInterest: LocalDate, userName: String): DBIO[List[LocalDate]] = {
      Transactions.join(Users).on(_.userId === _.id)
        .filter { case (_, user) => user.name === userName }
        .map { case (tx, _) => tx.date }
        .filter {
          _ > dateOfInterest
        }.sorted.result.map {
        _.distinct.reverse.toList
      }
    }

    private def getTransactionsUntil(dateOfInterest: LocalDate,
                                     userFilter: (TransactionTable, UserTable) => Rep[Boolean]): DBIO[TransactionsPerUserAndFund] = {

      // Following import used to avoid 'diverging implicit expansion for type scala.math.Ordering' error
      // as per https://issues.scala-lang.org/browse/SI-8541
      import Ordering.Tuple2

      val txsInScope = (for {
        t <- Transactions.filter {
          _.date <= dateOfInterest
        }
        f <- Funds if f.id === t.fundId
        u <- Users if userFilter(t, u)
        p <- Prices if t.fundId === p.fundId && t.priceDate === p.date
      } yield (u, f, p, t)).result.map {
        _.map { case (u, f, p, t) =>
          (
            f.id,
            Transaction(
              u.name, t.date, t.description, t.amountIn, t.amountOut,
              Price(f.name, p.date, p.price), t.units
            )
          )
        }
      }.map { txs =>
        val txCandidates = txs.groupBy { case (_, tx) => (tx.userName, tx.fundName) }.toSeq.sortBy {
          case (userNameAndFundName, _) => userNameAndFundName
        }
        ListMap(txCandidates: _*)
      }

      Prices.latestPrices(dateOfInterest).flatMap {
        latestPrice =>
          txsInScope.map {
            _.mapValues(fundIdAndTxs => {
              val transactions = fundIdAndTxs.map { case (_, tx) => tx }
              val price = latestPrice(fundIdAndTxs.map { case (fundId, _) => fundId }.head)

              (transactions, price)
            })
          }
      }
    }

    def getTransactionsUntil(dateOfInterest: LocalDate): DBIO[TransactionsPerUserAndFund] = {
      def userFilter(t: TransactionTable, u: UserTable) = {
        u.id === t.userId
      }

      getTransactionsUntil(dateOfInterest, userFilter _)
    }


    def getTransactionsUntil(dateOfInterest: LocalDate, userName: String): DBIO[TransactionsPerUserAndFund] = {
      def userFilter(t: TransactionTable, u: UserTable) = {
        u.id === t.userId && u.name === userName
      }

      getTransactionsUntil(dateOfInterest, userFilter _)
    }
  }

  // ---------------
  // USERS
  // ---------------

  // ------------------------
  // USERS - table definition
  // ------------------------

  case class UserRow(
                      id: PK[UserTable],
                      name: String,
                      password: Option[String] = None
                    )

  class UserTable(tag: Tag) extends Table[UserRow](tag, "USERS") {
    val id = column[PK[UserTable]]("ID", O.AutoInc, O.PrimaryKey)
    val name = column[String]("NAME", O.Length(254, varying = true))
    val password = column[Option[String]]("PASSWORD", O.Length(254, varying = true), O.Default(None))

    def * = (id, name, password) <> (UserRow.tupled, UserRow.unapply)

    val index1 = index("NAME", name, unique = true)
  }

  // ---------------
  // USERS - API
  // ---------------

  object Users extends TableQuery(new UserTable(_)) {

    def get(username: String): DBIO[Option[UserRow]] = {
      Users.filter {
        _.name === username
      }.result.headOption
    }

    def insert(username: String, password: Option[String] = None): DBIO[PK[UserTable]] = {
      (this returning this.map(_.id)) += UserRow(PK[UserTable](0L), username, password)
    }

    def getOrInsert(username: String): DBIO[PK[UserTable]] = {
      get(username).flatMap {
        _ match {
          case Some(aUser: UserRow) => DBIO.successful(aUser.id)
          case _ => insert(username)
        }
      }
    }

    def authenticate(username: String, password: String): DBIO[Int] = {
      val q1 = for (u <- Users if u.name === username && u.password === password) yield u
      Query(q1.length).result.head
    }
  }

  // --------------------
  // PORTFOLIO LIST - API
  // --------------------

  object PortfolioLists {

    def get(dateOfInterest: LocalDate): DBIO[PortfolioList] = {

      Transactions.getTransactionsUntil(dateOfInterest).map { transactions =>
        transactions.keys.map {
          _._1
        }.toList.distinct.sorted.map { userName =>
          Portfolio(userName, dateOfInterest, HoldingSummaryList(transactions, userName, dateOfInterest))
        }
      }.map { portfolios =>
        PortfolioList(portfolios)
      }
    }

    def get(dateOfInterest: LocalDate, userName: String): DBIO[PortfolioList] = {

      Transactions.getTransactionsUntil(dateOfInterest, userName) map { transactions =>
        PortfolioList(
          List(Portfolio(userName, dateOfInterest, HoldingSummaryList(transactions, userName, dateOfInterest)))
        )
      }
    }
  }

}