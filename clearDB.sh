#!/bin/bash

PASS=$"ve20ac19"
USER_DB=$"MECPerf"
PW_DB=$"password"
NAME_DB=$"MECPerf"

sshpass -p $PASS ssh ubuntu@131.114.73.3 <<EOF

echo "clearing DB..."

mysql --user=$USER_DB --password=$PW_DB $NAME_DB <<SQL

TRUNCATE RttMeasure;

TRUNCATE BandwidthMeasure;

DELETE
FROM Test
WHERE ID >= 1;

ALTER TABLE Test AUTO_INCREMENT = 1;

SQL

echo "DB cleared with success!"

EOF
