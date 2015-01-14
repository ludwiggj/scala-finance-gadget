# To reload the database from scratch...

## Short version:

Given various alias commands added to .bashrc:

1. dbReload

## Long version:

1. Run script dropAndRecreateFinanceDatabase.sql against mysql database
2. Run script createFinanceBaseSchema.sql against mysql database
3. Run dbdeploy; this assumes that following environment variable exists:
   DB_DEPLOY_CP=/Users/ludwiggj/.ivy2/cache
4. cd ~/code/financeGadget/src/main/resources/org/ludwiggj/finance/database
5. java -cp $DB_DEPLOY_CP/com.dbdeploy/dbdeploy-cli/jars/dbdeploy-cli-3.0M3.jar:$DB_DEPLOY_CP/mysql/mysql-connector-java/jars/mysql-connector-java-5.1.34.jar \
     com.dbdeploy.CommandLineTarget \
  -U **** \
  -P **** \
  -D com.mysql.jdbc.Driver \
  -u jdbc:mysql://localhost:3306/finance \
  -d mysql \
  -s db-scripts