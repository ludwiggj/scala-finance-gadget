package models.org.ludwiggj.finance.domain

import models.org.ludwiggj.finance.persistence.database.Tables.{Funds, FundsRow, Holdings, HoldingsRow, Prices, PricesRow, Users, UsersRow, _}
import models.org.ludwiggj.finance.persistence.file.PersistableToFile
import models.org.ludwiggj.finance.domain.User.stringToUsersRow
import play.api.Play.current
import play.api.db.DB
import scala.language.implicitConversions
import scala.slick.driver.MySQLDriver.simple._

case class Holding(val userName: String, val price: Price, val units: BigDecimal) extends PersistableToFile {
  def value = (units * price.inPounds).setScale(2, BigDecimal.RoundingMode.HALF_UP)

  def priceInPounds = price.inPounds

  def priceDate = price.date

  def name = price.fundName

  override def toString =
    s"Financial Holding [userName: ${userName}, name: ${price.fundName}, units: $units, date: ${price.date}, " +
      s"price: £${price.inPounds}, value: £$value]"

  def toFileFormat = s"${price.fundName}$separator$units$separator${price.date}$separator${price.inPounds}" +
    s"$separator$value"

  def canEqual(h: Holding) = (userName == h.userName) && (name == h.name) && (priceDate == h.priceDate)

  override def equals(that: Any): Boolean =
    that match {
      case that: Holding => that.canEqual(this) && (this.hashCode == that.hashCode)
      case _ => false
    }

  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result + units.intValue();
    result = prime * result + (if (userName == null) 0 else userName.hashCode)
    result = prime * result + (if (name == null) 0 else name.hashCode)
    result = prime * result + (if (priceDate == null) 0 else priceDate.hashCode)
    return result
  }
}

object Holding {

  def apply(userName: String, row: String): Holding = {
    val holdingPattern = (
      """.*?<span.*?>([^<]+)</span>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?<td[^>]*>(.*?)</td>""" +
        """.*?"""
      ).r

    val holdingPattern(fundName, units, date, priceInPence, _) = stripAllWhitespaceExceptSpace(row)
    val priceInPounds: BigDecimal = priceInPence / 100;
    Holding(userName, Price(fundName, date, priceInPounds), units)
  }

  import FinanceDate.stringToFinanceDate

  def apply(userName: String, row: Array[String]): Holding = {
    Holding(userName, Price(row(0), row(2), row(3)), row(1))
  }

  lazy val db = Database.forDataSource(DB.getDataSource("finance"))

  def insert(holding: Holding) = {
    db.withSession {
      implicit session =>

        if (!get().contains(holding)) {

          val userId = User.getOrInsert(holding.userName)

          def insert(fundId: Long) {
            Holdings += HoldingsRow(fundId, userId, holding.units, holding.priceDate)
          }

          Price.insert(holding.price)

          Fund.get(holding.name) match {
            case Some(fundsRow) => insert(fundsRow.id)
            case _ => println(s"Could not insert Holding: fund ${holding.name} not found")
          }
        }
    }
  }

  def insert(holdings: List[Holding]): Unit = {
    for (holding <- holdings) {
      insert(holding)
    }
  }

  implicit class HoldingExtension(q: Query[Holdings, HoldingsRow, Seq]) {
    def withFundsAndPricesAndUser = {
      q.join(Prices).on((h, p) => h.fundId === p.fundId && h.date === p.date)
        .join(Funds).on((h_p, f) => h_p._1.fundId === f.id)
        .join(Users).on((h_p_f, u) => h_p_f._1._1.userId === u.id)
    }
  }

  implicit def asListOfHoldings(q: Query[(((Holdings, Prices), Funds), Users),
    (((HoldingsRow, PricesRow), FundsRow), UsersRow), Seq]): List[Holding] = {
    db.withSession {
      implicit session =>
        q.list map {
          case (((HoldingsRow(_, _, units, _), PricesRow(_, priceDate, price)),
          FundsRow(_, fundName)), UsersRow(_, userName)) =>

            Holding(userName, Price(fundName, priceDate, price), units)
        }
    }
  }

  def get(): List[Holding] = {
    db.withSession {
      implicit session =>
        Holdings.withFundsAndPricesAndUser
    }
  }
}