#!/bin/sh

USER_HOME=~
FINANCE_GADGET_HOME=${USER_HOME}/code/personal/financeGadget
PERSISTENCE_DB_HOME=${FINANCE_GADGET_HOME}/conf/db
MYSQL_HOME="C:/Program Files/MySQL/MySQL Server 5.6"

"${MYSQL_HOME}/bin/mysql" -h 127.0.0.1 -P 3306 -u root -v < ${PERSISTENCE_DB_HOME}/dropAndRecreateFinanceDatabase.sql
"${MYSQL_HOME}/bin/mysql" -h 127.0.0.1 -P 3306 -u root -v < ${PERSISTENCE_DB_HOME}/createFinanceBaseSchema.sql
#cd ${PERSISTENCE_DB_HOME}
#exec scala "$0" "$@"
#!#