#!/bin/bash

if [[ $# -ne 6 ]]; then
    echo "./buildDB sourceUser@sourceAddress sourcePassword destUsername destAddress destPassword  mysqlDestRootPassword"
    exit 1
fi

SOURCE_USER=$1
SOURCE_PASSWORD=$2
REMOTE_USERNAME=$3
REMOTE_ADDRESS=$4
REMOTE_PASSWORD=$5
MYSQL_ROOT_PASSWORD=$6

USER_DB="MECPerf"
PW_DB="password"
DB_NAME="MECPerf"

echo "sshpass -p $SOURCE_PASSWORD ssh $SOURCE_USER <<EOF"
sshpass -p $SOURCE_PASSWORD ssh $SOURCE_USER <<EOF
    echo "building DB..."
    mysqldump --user=$USER_DB --password=$PW_DB --no-data $DB_NAME > MECPerf.sql
    echo "Database schema create with no data"
    echo "N.B: AUTO_INCREMENT in Test Table is not resetted"

    echo "sshpass -p $REMOTE_PASSWORD scp MECPerf.sql $REMOTE_USERNAME@$REMOTE_ADDRESS:/home/$REMOTE_USERNAME"
    sshpass -p $REMOTE_PASSWORD scp MECPerf.sql $REMOTE_USERNAME@$REMOTE_ADDRESS:/home/$REMOTE_USERNAME
EOF

echo "sshpass -p $REMOTE_PASSWORD ssh $REMOTE_USERNAME@$REMOTE_ADDRESS <<EOF"
sshpass -p $REMOTE_PASSWORD ssh $REMOTE_USERNAME@$REMOTE_ADDRESS <<EOF
echo "mysql --user=root --password=$MYSQL_ROOT_PASSWORD "
mysql --user=root --password=$MYSQL_ROOT_PASSWORD <<EOF2
CREATE USER IF NOT EXISTS '$USER_DB'@'localhost' IDENTIFIED BY '$PW_DB';
CREATE DATABASE  IF NOT EXISTS MECPerf;
exit
EOF2

echo "importing MECPerf db"
echo "mysql --user=root --password=$MYSQL_ROOT_PASSWORD MECPerf < MECPerf.sql"
mysql --user=root --password=$MYSQL_ROOT_PASSWORD  MECPerf < MECPerf.sql

mysql --user=root --password=$MYSQL_ROOT_PASSWORD << EOF3
USE MECPerf;
ALTER TABLE Test AUTO_INCREMENT=1;
GRANT ALL PRIVILEGES ON MECPerf.* to 'MECPerf'@'localhost';
FLUSH PRIVILEGES;

exit
EOF3

EOF
