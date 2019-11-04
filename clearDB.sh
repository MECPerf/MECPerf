#!/bin/bash

if [[ ( $# -eq 1  && "$1" != 'localhost' ) ]]; then
    echo "./clearDB remoteAddress [remotePassword] "
    echo "./clearDB localhost "
    
    exit 1
else
    if [[ ( "$1" != 'localhost' && $# -ne 2 ) ]]; then
        echo "./clearDB remoteAddress [remotePassword] "
        echo "./clearDB localhost "
        
        exit 1
    fi
fi


REMOTE_ADDRESS=$1
REMOTE_PASS=$2

USER_DB=$"MECPerf"
PW_DB=$"password"
NAME_DB=$"MECPerf"

  
if [ $# -eq 1 ] && [ "$1" = 'localhost' ]; then
    echo "(localhost)clearing DB..."
  
    mysql --user=$USER_DB --password=$PW_DB $NAME_DB <<SQL
        TRUNCATE RttMeasure;
        TRUNCATE BandwidthMeasure;
        DELETE FROM Test WHERE ID >= 1;
        ALTER TABLE Test AUTO_INCREMENT = 1;
SQL
    
    echo "DB cleared with success!"

else
    sshpass -p $REMOTE_PASS ssh ubuntu@$REMOTE_ADDRESS <<EOF

        echo "($REMOTE_ADDRESS)clearing DB..."

        mysql --user=$USER_DB --password=$PW_DB $NAME_DB <<SQL
            TRUNCATE RttMeasure;
            TRUNCATE BandwidthMeasure;
            DELETE FROM Test WHERE ID >= 1;
            ALTER TABLE Test AUTO_INCREMENT = 1;

SQL
        echo "DB cleared with success!"

EOF
fi