#!/bin/sh

USER_HOME=/Users/ludwiggj
#JAR_HOME=${USER_HOME}/.ivy2/cache
FINANCE_GADGET_HOME=${USER_HOME}/code/financeGadget
PERSISTENCE_DB_HOME=${FINANCE_GADGET_HOME}/conf/db
#APP_CONF_FILE=${FINANCE_GADGET_HOME}/conf/application.conf

#function dbProperty {
#  grep $1 ${APP_CONF_FILE} | sed 's/ //g' | sed 's/.*=//' | sed 's/"//g'
#}
#
#DB_URL=`dbProperty url`
#DB_DRIVER=`dbProperty driver`
#DB_USER=`dbProperty username`
#DB_PASSWORD=`dbProperty password`
#
#echo $DB_URL
#echo $DB_DRIVER
#echo $DB_USER
#echo $DB_PASSWORD

mysql -h 127.0.0.1 -P 3306 -u root -v < ${PERSISTENCE_DB_HOME}/dropAndRecreateFinanceDatabase.sql
mysql -h 127.0.0.1 -P 3306 -u root -v < ${PERSISTENCE_DB_HOME}/createFinanceBaseSchema.sql

cd ${PERSISTENCE_DB_HOME}

exec scala "$0" "$@"
!#
// Say hello to the first argument
println("Hi there!")