#!/bin/bash

if [[ $# -ne 2 ]]; then
    echo "./buildDB mysqlDestRootPassword DBUserNewPw"
    exit 1
fi


MYSQL_ROOT_PASSWORD=$1
MECPerf_USER_NEWPW=$2

USER_DB="MECPerf"
DB_NAME="MECPerf"

echo "mysql --user=root --password=$MYSQL_ROOT_PASSWORD "
mysql --user=root --password=$MYSQL_ROOT_PASSWORD <<EOF1
CREATE USER IF NOT EXISTS '$USER_DB'@'localhost' IDENTIFIED BY '$MECPerf_USER_NEWPW';
CREATE DATABASE  IF NOT EXISTS MECPerf;
exit
EOF1

mysql --user=root --password=$MYSQL_ROOT_PASSWORD  $DB_NAME < MECPerf.sql

mysql --user=root --password=$MYSQL_ROOT_PASSWORD << EOF3
USE MECPerf;
ALTER TABLE Test AUTO_INCREMENT=1;
GRANT ALL PRIVILEGES ON MECPerf.* to 'MECPerf'@'localhost';
FLUSH PRIVILEGES;

exit
EOF3