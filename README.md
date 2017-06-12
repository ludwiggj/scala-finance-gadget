financeGadget
=============

# Background #

This is a personal project written in Scala to view my share investment portfolio.
 
I can log in to my existing financial web site at any time, and view the current value of my portfolio, as well as the
last 3 months' worth of transactions.

However, I also want to view additional information, such as:

* how much I have invested
* how my investment has grown (or decreased) over time
* which investment funds are performing badly

Previously I did this manually via a spreadsheet. This project automates the process, scraping the information from the
web site, storing the information in a database and presenting it via a Play application.

The [portfolio page](financePortfolioOnDate.png) is an example of the financial information which can be viewed via the
Play webapp.

# Architecture #

The system consists of several components:

* **Web site scrapers** which log in to the investment management platform, scrape the investment data from the web site
and persist it into a MySql database. The data is also persisted into local file storage (flat files) as a backup.

* A **database persistence layer** based on Slick.
 
* A **play webapp** which retrieves the information from the database and displays it to the user.
 
* A **database reloader**, which can be used to reload the information from the flat files into the Mysql database.

The components are described in more detail below.

# Key Technologies #

* Scala 2.11.6.

* MySql 5.6.22.

* [SSoup](https://github.com/filosganga/ssoup "SSoup"), a Scala wrapper around the [JSoup](http://jsoup.org "JSoup")
Java library for parsing HTML. This is used to parse the HTML scraped from the investment management platform.

* Slick 2.1.0, to create the database persistence layer.

* Typesafe config, to store details of the accounts used to access the investment management platform.

* Play 2.4.0, to provide the web front end.

* ScalaTest 2.2.1.

# Getting the Code #

Pre-requisites:

* [Play 2.4](https://www.playframework.com/documentation/2.4.x/Installing)

* [MySql 5.6.x](https://dev.mysql.com/downloads/mysql/)

The code for is available from the [financeGadget git repo](https://github.com/ludwiggj/financeGadget.git).

Once downloaded, you can run the tests from the command line:

**activator clean test**

# Components #

## WebSite Holding Scraper ##

The **WebSiteHoldingScraper** retrieves details of the web site user accounts from a typesafe config file. (The config
file is not stored in github for obvious reasons).

The scraper logs in to the investment management web site as each user, and retrieves the current holdings of the user.
The format of the holdings is:

| Holding                                  | Units/Shares | Price date | Price(p) | Value(GBP)|
| :--------------------------------------- |-------------:| ----------:| --------:| ---------:|
| Aberdeen Ethical World Equity A Fund Inc | 1,912.0785   | 09/10/2015 | 132.1200 | 2,526.24  |
| EdenTree Amity European A Fund Inc       | 2,468.3505   | 09/10/2015 | 199.5000 | 4,924.36  |

The scraper parses the holding information and persists it to the the MySql database, and also local flat files in the 
**holdings** directory. The format of a holdings flat file name is:
 
**holdings_YY_MM_DD_\<userName\>.txt**

where **userName** is a user-defined name to represent the user. Thus the file represents the holdings for a particular
user on a particular date. 

If the config file lists multiple accounts then each one is processed in parallel via Futures.

Currently the WebSite Holding Scraper is triggered manually.

## WebSite Transaction Scraper ##

The **WebSiteTransactionScraper** is similar to the **WebSiteHoldingScraper**, except that it scrapes and persists the
latest transactions for each user.

The format of the scraped transaction data is shown below:

| Holding                                       | Date       | Description              | In (GBP) | Out (GBP) | Price date | Price(p) | Units/Shares | Int charge (GBP) |
| :-------------------------------------------- | ----------:| :------------------------| --------:| ---------:| ----------:| --------:| ------------:| ----------------:|
| ^ M&G Feeder of Property Portfolio I Fund Acc | 25/09/2015 | Investment Regular       | 200.00   |           | 25/09/2015 | 1,344.54 | 14.8750      |                  |
| F&C Responsible UK Income 1 Fund Inc          | 11/09/2015 | Sale for Regular Payment |          | 25.67     | 11/09/2015 | 136.40   | 18.8187      |                  |	

The transaction flat files are stored in the **data** directory. The format of a transactions flat file name is:
 
**txs_YY_MM_DD_\<userName\>.txt**

where **userName** is a user-defined name to represent the user. Thus the file represents the recent transactions for a
particular user up to and including the specified date. 

Again, the config file can list details of multiple accounts; in this case the scraper will log in to the web site
separately using each account. This is done in parallel via Futures.

Currently the WebSite Transaction Scraper is triggered manually.

## MySql ##

MySql is used to store the data scraped by the previously described components. The [schema](financeERD.png) shows that
the database has been normalised, with common occurring entities (users, prices, transactions and funds) stored in
separate tables. 

The **play_evolutions** table is used by the play evolutions component, which manages the deployment and versioning of
database schema changes.

## Database Persistence Layer ##

This is implemented via slick. See the
[Tables definition](app/models/org/ludwiggj/finance/persistence/database/Tables.scala) and the corresponding
[domain classes](app/models/org/ludwiggj/finance/domain).

## Play Web Application ##

The application requires the user to log in, either as an administrator or him/herself. Following a successful login,
the user is shown an [investment summary page](financePortfolioDateList.png). This shows a list of **investment dates**,
and the total value of the user's portfolio on each date. The investment date is the regular monthly date on which the
user's money is used to buy new shares in one or more funds. Thus the summary shows how each user's portfolio has built
up over time. 

The user can click on a specific date to view a [portfolio page](financePortfolioOnDate.png) which provides a detailed
breakdown of the user's investments on that specific date.

Note that a user can only view his/her own information, whereas an administrator can view the information across all
users.

The application retrieves the investment data via the persistence layer. It's converted into "higher value" domain
representations, such as [HoldingSummary](app/models/org/ludwiggj/finance/domain/HoldingSummary.scala) (the current
value of shares held in a single fund by a user, including gain/loss information) and
[Portfolio](app/models/org/ludwiggj/finance/domain/Portfolio.scala) (all of the funds held by a user on a particular
date, including gain/loss information across the whole portfolio).

## Scala tests ##

There are a number of unit and integration tests. Note that the integration tests are currently run directly against
the MySql database, though in a separate database instance called **test**.

## Database Reloader ##

The **Database Reloader** executable cleans out the MySql database, parses the flat files, and saves the resulting data
into the Mysql database via the persistence layer.

This component is not as important as it once was, as the integration tests are now run against a separate database
instance, though it's still nice to know that the data can be reloaded into the database at any time.

Graeme Ludwig, 12/06/17.