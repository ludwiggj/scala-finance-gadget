financeGadget
=============

# Background #

This is a personal project written in Scala to view my share investment portfolio.

I can log in to my existing financial web site at any time, and view the current value of my portfolio, as well as the
last 3 months' worth of transactions. However, I also want to view additional information, such as:

* how much I have invested
* how the value of my investments has changed over time
* which investment funds are performing badly

Previously I did this manually via a spreadsheet. This project automates most of the process, scraping the information
from the web site, storing the information in a database and presenting it via a Play application.

The portfolio page is an example of the financial information which can be viewed via the Play webapp:

![portfolio page](images/financePortfolioOnDate.png?raw=true "Portfolio Page")

# Architecture #

The system consists of several components:

* A **database persistence layer**, based on Slick, to store and retrieve the investment data.

* A **play webapp** which retrieves the information from the database and displays it to the user. The web application
 can be started in two modes:
    * Demo mode. The database is loaded with user accounts and example financial data for demonstration purposes. See
     details below for how to start the webapp in demo mode.
    * Normal mode. This mode is designed to work with data extracted from the real financial website. The database is
    initially loaded with user accounts only. The real financial data is then loaded into database via the website
    scrapers; see next point.

* **Web site scrapers** which log in to the investment management platform, scrape the investment data from the web site
and persist it into the database. The data is also persisted into local file storage (flat files) as a backup.

* A **database reloader**, which can be used to reload the information from the flat files into the database.

The components are described in more detail below.

# Getting and Building the Code #

1. Install the following pre-requisites:

   * [sbt 0.13.15](http://www.scala-sbt.org/0.13/docs/Setup.html)
   * [MySql Community Server 5.6.x](https://dev.mysql.com/downloads/mysql/5.6.html#downloads)

2. Start the mysql server (see [this page](http://osxdaily.com/2014/11/26/start-stop-mysql-commands-mac-os-x/)
for further details):

   ```
   sudo /usr/local/mysql/support-files/mysql.server start
   ```

3. Check that you can log in:

   ```
   /usr/local/mysql/bin/mysql
   ```

   You should be able to log in and get the mysql> prompt. Log out via 'exit'.

4. Clone this repo:

   ```
   git clone https://github.com/ludwiggj/financeGadget.git

   cd financeGadget
   ```

5. Initialise the ssoup submodule:

   ```
   git submodule init

   > Submodule 'ssoup' (git@github.com:ludwiggj/ssoup.git) registered for path 'ssoup'

   git submodule update

   > Cloning into 'ssoup'...
     ...
   ```

6. Build the ssoup submodule and publish the jar locally:

   ```
   cd ssoup

   sbt publishLocal
   ```

7. Create the database schemas:

   ```
   cd ..

   . ./conf/db/dbdeploy.sh
   ```

8. Create a user environment variable USER_HOME, and point it to your home directory. For example, on a Mac:

   ```
   export USER_HOME=~ (Mac)
   ```

9. Build the code and run the tests:

   ```
   sbt clean test
   ```

10. To stop the mysql server:

    ```
    sudo /usr/local/mysql/support-files/mysql.server stop
    ```

# The Demo #

## Starting the Demo ##

1. Start the mysql server:

   ```
   sudo /usr/local/mysql/support-files/mysql.server start
   ```

2. Start the web application:

   ```
   . ./startDemo.sh
   ```

   This runs the command:
   ```
   sbt -Dconfig.file=conf/demo/demo.conf -Dlogger.file=conf/demo/logback-demo.xml run
   ```

3. Open the [finance gadget home page](http://localhost:9000).

4. Once finished, stop the web application and the mysql database:
   ```
   sudo /usr/local/mysql/support-files/mysql.server stop
   ```

## Logging In ##

The application requires the user to log in. There are three available accounts:

| Username | Password | Notes                                          |
|    ---   |    ---   |  ---                                           |
| MissA    | A        | Can view her own data only.                    |
| MisterB  | B        | Can view his own data only.                    |
| Admin    | Admin    | Can view investments of both MissA and MisterB |

## Investment Summary Page ##

Following a successful login, the user is shown an investment summary page:

![investment summary page](images/financePortfolioDateList.png?raw=true "Investment Summary page")

This shows a list of **investment dates**, and the total value of the user's portfolio on each date. The investment date
is the regular monthly date (the 1<sup>st</sup> in this example) on which the user's money is used to buy new shares in one or more
funds. Thus the summary page shows how each user's portfolio has built up over time.

If any additional transactions have occurred since the most recent investment date, then for each such transaction the
date is displayed together with the value of the user's portfolio on that date.

The dates are shown in reverse order, from the most recent date backwards. If there are too many rows to show on
one page additional links are provided to support navigation between each page of results.

## Portfolio Page ##

The user can click on a specific date to view a portfolio page, which shows a detailed breakdown of the user's
investments on that specific date:

![portfolio page](images/financePortfolioOnDate.png?raw=true "Portfolio Page")

# Components #

## WebSite Holding Scraper ##

The [WebSiteHoldingScraper](app/utils/WebSiteHoldingScraper.scala) retrieves details of the real web site user account
from a typesafe config file. The real config file is not stored in github for obvious reasons; the format of the file
is based on [this template](conf/site.conf).

The scraper logs in to the investment management web site as each user, and retrieves details of the user's current
holdings. If the config file lists multiple accounts then each one is processed in parallel via Futures. The format of
the holdings is:

| Holding              | Units/Shares | Price date | Price(p) | Value(GBP)|
| :------------------- |-------------:| ----------:| --------:| ---------:|
| H Bear Beer Emporium | 1,912.0785   | 09/10/2015 | 132.1200 | 2,526.24  |
| Quantum Inc          | 2,468.3505   | 09/10/2015 | 199.5000 | 4,924.36  |

The scraper parses the holding information and persists it into the database, and also local flat files in the
**holdings** directory. The file name format is:

**holdings_YY_MM_DD_\<userName\>.txt**

where **userName** is the name of the user. The file contains the holdings for the named user on the specified date.

Currently the WebSite Holding Scraper is triggered manually.

## WebSite Transaction Scraper ##

The [WebSiteTransactionScraper](app/utils/WebSiteTransactionScraper.scala) is similar to the **WebSiteHoldingScraper**,
except that it scrapes and persists the latest transactions for each user. Again, the config file can list details of
multiple accounts; the scraper will log in to the web site separately using each account. This is done in
parallel via Futures.

The format of the scraped transaction data is shown below:

| Holding                  | Date       | Description              | In (GBP) | Out (GBP) | Price date | Price(p) | Units/Shares | Int charge (GBP) |
| :----------------------- | :--------- | :----------------------- | :------- | :-------- | ---------- | --------:| ------------:| ---------------- |
| H Bear Beer Emporium     | 25/09/2015 | Investment Regular       | 200.00   |           | 25/09/2015 | 1,344.54 | 14.8750      |                  |
| Quantum Inc              | 11/09/2015 | Sale for Regular Payment |          | 25.67     | 11/09/2015 | 136.40   | 18.8187      |                  |

The transaction flat files are stored in the **data** directory. The file name format is:

**txs_YY_MM_DD_\<userName\>.txt**

where **userName** is the name of the user. The file contains the recent transactions for the named user up to and
including the specified date.

Currently the WebSite Transaction Scraper is triggered manually.

## MySql ##

MySql is used to store the data scraped by the previously described components. The schema is normalised; the key
representations (users, prices, transactions and funds) are stored in separate tables:

![schema](images/financeERD.png?raw=true "Schema")

The **play_evolutions** table is used by the play evolutions component, which manages the deployment and versioning of
database schema changes.

## Database Persistence Layer ##

This is implemented via slick. See the
[Tables definition](app/models/org/ludwiggj/finance/persistence/database/Tables.scala) and the corresponding
[domain classes](app/models/org/ludwiggj/finance/domain).

## Play Web Application ##

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

# Key Technologies #

* Scala 2.11.6.

* MySql 5.6.22.

* [SSoup](https://github.com/filosganga/ssoup "SSoup"), a Scala wrapper around the [JSoup](http://jsoup.org "JSoup")
Java library for parsing HTML. This is used to parse the HTML scraped from the investment management platform.

* Slick 2.1.0, to create the database persistence layer.

* Typesafe config, to store details of the accounts used to access the investment management platform.

* Play 2.4.0, to provide the web front end.

* ScalaTest 2.2.1.

* sbt 0.13.15

Graeme Ludwig, 17/10/17.