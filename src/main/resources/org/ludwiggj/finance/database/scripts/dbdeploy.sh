#!/bin/sh

USER_HOME=/Users/ludwiggj
JAR_HOME=${USER_HOME}/.ivy2/cache
FINANCE_GADGET_HOME=${USER_HOME}/code/financeGadget
FINANCE_GADGET_DB_HOME=${FINANCE_GADGET_HOME}/src/main/resources/org/ludwiggj/finance/database
APP_CONF_FILE=${FINANCE_GADGET_HOME}/src/main/resources/application.conf

function dbProperty {
  grep $1 ${APP_CONF_FILE} | sed 's/ //g' | sed 's/.*=//' | sed 's/"//g'
}

DB_URL=`dbProperty url`
DB_DRIVER=`dbProperty driver`
DB_USER=`dbProperty user`
DB_PASSWORD=`dbProperty password`

mysql -h 127.0.0.1 -P 3306 -u root < ${FINANCE_GADGET_DB_HOME}/dropAndRecreateFinanceDatabase.sql
mysql -h 127.0.0.1 -P 3306 -u root < ${FINANCE_GADGET_DB_HOME}/createFinanceBaseSchema.sql

cd ${FINANCE_GADGET_DB_HOME}

java -cp ${JAR_HOME}/com.dbdeploy/dbdeploy-cli/jars/dbdeploy-cli-3.0M3.jar:${JAR_HOME}/mysql/mysql-connector-java/jars/mysql-connector-java-5.1.34.jar \
     com.dbdeploy.CommandLineTarget \
     -U ${DB_USER} \
     -P ${DB_PASSWORD} \
     -D ${DB_DRIVER} \
     -u ${DB_URL} \
     -d mysql \
     -s db-scripts