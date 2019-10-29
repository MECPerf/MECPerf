#!/bin/bash

REMOTE_PASS=$1
ROOT_USER_PASS=$2
USER_DB="MECPerf"
PW_DB="password"
DB_NAME="MECPerf"

sshpass -p $REMOTE_PASS ssh ubuntu@131.114.73.3 <<EOF
    echo "building DB..."
    mysqldump --user=$USER_DB --password=$PW_DB --no-data $DB_NAME > MECPerf.sql
    echo "Database schema create with no data"
    echo "N.B: AUTO_INCREMENT in Test Table is not resetted"
    sshpass -p $REMOTE_PASS scp MECPerf.sql ubuntu@131.114.73.2:/home/ubuntu
EOF


sshpass -p $REMOTE_PASS ssh ubuntu@131.114.73.2 <<EOF
echo "mysql --user=root --password=$2 "
mysql --user=root --password=$2 <<EOF2
CREATE USER '$USER_DB'@'localhost' IDENTIFIED BY '$PW_DB';
CREATE DATABASE  IF NOT EXISTS MECPerf;
exit
EOF2

echo "importing MECPerf db"
echo "mysql --user=root --password=$2 MECPerf < MECPerf.sql"
mysql --user=root --password=$2  MECPerf < MECPerf.sql

mysql --user=root --password=$2 << EOF3
USE MECPerf;
ALTER TABLE Test AUTO_INCREMENT=1;
GRANT ALL PRIVILEGES ON MECPerf.* to 'MECPerf'@'localhost';
FLUSH PRIVILEGES;

exit
EOF3

EOF
