financeGadget
=============

# Background #

This is a personal project written in Scala, and is intended to help manage share investments.
 
I can log in to my existing investment management web site at any time, and view the current value of my portfolio, as
well as the last 3 months' worth of transactions.

However, I also want to view the history of my portfolio; for example:

* how much I have invested
* how my investment has grown (or decreased) over time
* which investment funds are performing badly

Previously I have achieved this by copying data from the investment management website into a spreadsheet and then
manipulating the data. This project is intended to automate that process, and help me to develop my Scala skills at the
same time.

# Architecture #

The system consists of several components:

* **Web site scrapers** to log in to the investment management platform, retrieve (scrape) the investment data from the 
web site and persist it into local file storage (flat files) and a MySql database.

* A **database facade**.
 
* A **reports component**, which runs queries against the MySql database and displays the results as a number of different
 reports.
 
* A **database reloader**, which parses scraped investment data from flat files and reloads it into the Mysql database.

The components are described in more detail below.

# Key Technologies #

* Scala 2.11.6.

* MySql 5.6.22.

* [SSoup](https://github.com/filosganga/ssoup "SSoup"), a Scala wrapper around the [JSoup](http://jsoup.org "JSoup")
Java library for parsing HTML. This is used to parse the HTML scraped from the investment management platform.

* Slick 2.1.0, to create the database facade.

* Typesafe config, to store details of the accounts used to access the investment management platform.

* Play 2.4.0, to provide a web front end (work in progress).

* ScalaTest 2.2.1.

# Getting the Code #

Pre-requisites:

** [Play 2.4](https://www.playframework.com/documentation/2.4.x/Installing)

** [MySql 5.6.x](https://dev.mysql.com/downloads/mysql/)

The code for is available from the [financeGadget git repo](https://github.com/ludwiggj/financeGadget.git).

Once downloaded, you can run the tests from the command line via the command:

**activator clean test**

# Components #

## WebSite Holding Scraper ##

The **WebSiteHoldingScraper** executable logs first retrieves details of the web site user accounts from a configuration
file, which uses the typesafe config format. (The config file is not stored in github for obvious reasons).

The config file can contain details of multiple user accounts. The scraper logs in to the investment management web site
as each user, and retrieves the current holdings of the user. The format of the holdings is:

| Holding                                  | Units/Shares | Price date | Price(p) | Value(GBP)|
| :--------------------------------------- |-------------:| ----------:| --------:| ---------:|
| Aberdeen Ethical World Equity A Fund Inc | 1,912.0785   | 09/10/2015 | 132.1200 | 2,526.24  |
| EdenTree Amity European A Fund Inc       | 2,468.3505   | 09/10/2015 | 199.5000 | 4,924.36  |

The scraper obtains the above data from the web site, parses it and then persists it to the local flat files (in the 
**reports** directory), as well as the MySql database.

The format of a holdings flat file name is:
 
**holdings_YY_MM_DD_\<userName\>.txt**

where **userName** is a user-defined name to represent the user. Thus the file represents the holdings for a particular
user on a particular date. 

If the config file lists multiple accounts then each one is processed in parallel via Futures.

## WebSite Transaction Scraper ##

The **WebSiteTransactionScraper** executable is similar to the **WebSiteHoldingScraper**, except that it scrapes and
persists the latest transactions for each user.

The format of the scraped transaction data is shown below:

| Holding                                       | Date       | Description              | In (GBP) | Out (GBP) | Price date | Price(p) | Units/Shares | Int charge (GBP) |
| :-------------------------------------------- | ----------:| :------------------------| --------:| ---------:| ----------:| --------:| ------------:| ----------------:|
| ^ M&G Feeder of Property Portfolio I Fund Acc | 25/09/2015 | Investment Regular       | 200.00   |           | 25/09/2015 | 1,344.54 | 14.8750      |                  |
| F&C Responsible UK Income 1 Fund Inc          | 11/09/2015 | Sale for Regular Payment |          | 25.67     | 11/09/2015 | 136.40   | 18.8187      |                  |

The transactions for a particular user on a particular date are stored as a single file in the reports directory, with
the name **txs_YY_MM_DD_\<userName\>.txt**, where **userName** is a user-defined name to represent the user.		

The transaction flat files are again stored in the **reports** directory. The format of a transactions flat file name is:
 
**txs_YY_MM_DD_\<userName\>.txt**

where **userName** is a user-defined name to represent the user. Thus the file represents the recent transactions for a
particular user up to and including the specified date. 

Again, the config file can list details of multiple accounts; in this case the scraper will log in to the web site
separately using each account, processing each one separately, and in parallel, via Futures.

## MySql ##

MySql is used to store the data scraped by the previously described components. The [schema](financeERD.png) shows that
the database has been normalised, with common occurring entities (users, prices and funds) being given their own tables. 

The **play_evolutions** table is used by the play evolutions component, which manages the deployment and versions of
database schema changes.

## Database Facade ##

This is implemented via slick, see the classes in the **models.org.ludwiggj.finance.persistence.database** package.

## Reports ##

This makes use of slick to retrieve data from the MySql database. The data is initially retrieved as row case classes, 
which are generated automatically by slick. The data is then converted into "higher value" domain representations, such 
as **HoldingSummary** (the current value of shares held in a single fund by a user, including gain/loss information) and 
**Portfolio** (all of the fund holdings for a user on a particular date, including gain/loss information across the whole
portfolio).

There are a number of report executables which retrieve and display this information. For example,
**ShowPortfoliosFromTransactions** displays the value of each user's portfolio on each **investment date** (the regular
monthly date on which the investor's money is used to buy new shares in one or more funds). This shows how each user's
portfolio has built up over time. 

## Scala tests ##

There are a number of unit and integration tests. Note that the integration tests are currently run directly against
the MySql database, though in a separate database instance called **test**.

## Database Reloader ##

The **Database Reloader** executable cleans out the MySql database, parses the report files and saves the resulting data 
into the Mysql database via the database facade.

This component is not needed so much now that the integration tests are run against a separate database instance, though
it's still nice to know that the data can be reloaded into the database at any time.

## Future Directions ##

* Fully integrate the code with play, so that the existing reports can be displayed via a web front end. This should
just be a case of returning the existing domain objects via the database facade, and displaying the resultant data via
play templates. 

* Automatically retrieve the financial data from the investment management web site, ideally just after the regular
monthly **investment date**, perhaps via Akka.

* Experimenting with the web front end to show additional reports e.g. price history of single fund, perhaps via a
 graphing component.
 
* Simplifying the use of futures in the scraper code via Scala Async.
 
* More coding improvements.

Graeme Ludwig, 11/10/15.