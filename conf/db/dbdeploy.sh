#!/bin/sh

USER_HOME=/Users/ludwiggj
FINANCE_GADGET_HOME=${USER_HOME}/code/financeGadget
PERSISTENCE_DB_HOME=${FINANCE_GADGET_HOME}/conf/db

mysql -h 127.0.0.1 -P 3306 -u root -v < ${PERSISTENCE_DB_HOME}/dropAndRecreateFinanceDatabase.sql
mysql -h 127.0.0.1 -P 3306 -u root -v < ${PERSISTENCE_DB_HOME}/createFinanceBaseSchema.sql

cd ${PERSISTENCE_DB_HOME}

exec scala "$0" "$@"
!#
// Say hello to the first argument
println("Hi there!")