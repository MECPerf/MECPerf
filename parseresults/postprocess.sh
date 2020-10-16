#!/bin/bash

TEMPFILE="temp.txt"
RESULTS_SORTEDBYKEYWORD="results_sortedbykeyword.txt"
OUTPUT_UNSORTED="out_unsorted.txt"
INPUTFILENAME=""
OUTPUTFILENAME=""

CLOUDSERVER_SUBNETADDR="131.114.73"
EDGESERVER_SUBNETADDR1="192.168.200"
EDGESERVER_SUBNETADDR2="10.0.1"


function search_first_keyword () {
    inputfilesorted=$1
    OLDIFS=$IFS
    IFS=,
    #The first line contains the greatest keyword
    FIRSTLINE=$(head -1 $inputfilesorted)
    #the first keyword    
    set $FIRSTLINE
    FIRSTKEYWORD=$7

    echo $FIRSTKEYWORD  

    IFS=$OLDIFS  
}
function search_second_keyword () {
    inputfilesorted=$1
    firstkey=$2

    OLDIFS=$IFS
    IFS=,
    while read line; do 
        set $line
        actualkey=$7

        if [[ "$actualkey" != "$firstkey" ]]; then
            echo "$actualkey"
            return
        fi
    done < $inputfilesorted

    IFS=$OLDIFS  
}
function search_third_keyword () {
    inputfilesorted=$1
    firstkey=$2
    secondkey=$3

    OLDIFS=$IFS
    IFS=,
    while read line; do 
        set $line
        actualkey=$7

        if [[ "$actualkey" != "$firstkey" ]]; then
            if [[ "$actualkey" != "$secondkey" ]]; then
                echo "$actualkey"
                return
            fi
        fi
    done < $inputfilesorted

    IFS=$OLDIFS  
}

rm *_SORTED.csv
for INPUTFILENAME in *.csv; do
    rm $TEMPFILE
    rm $RESULTS_SORTEDBYKEYWORD
    rm $OUTPUT_UNSORTED

    touch $TEMPFILE
    touch $RESULTS_SORTEDBYKEYWORD
    touch $OUTPUT_UNSORTED

    #generate the ouput file
    OLDIFS=$IFS
    IFS=.
    set $INPUTFILENAME
    #echo $1
    OUTPUTFILENAME=$1"_SORTED.csv"
    IFS=$OLDIFS
    echo -e "\033[0;34m$OUTPUTFILENAME\033[0m"   

    #copy the headers into the output file (they should not be sorted)
    head -3 $INPUTFILENAME > $OUTPUTFILENAME
    #copy the remaining lines in a temporary file
    tail -n +4 $INPUTFILENAME > $TEMPFILE
    #-t, --field-separator=SEP: use SEP instead of non-blank to blank transition
    #-r, --reverse: reverse the result of comparisons
    #-k, --key=KEYDEF: sort via a key; KEYDEF gives location and type
    #K7 = keyword -> sorted by keyword in descending order. 
    sort -t, -r -k7,7 $TEMPFILE  > $RESULTS_SORTEDBYKEYWORD

    #search keywords
    FIRSTKEYWORD=$(search_first_keyword $RESULTS_SORTEDBYKEYWORD)
    echo -e "\tFirst keyword = "$FIRSTKEYWORD
    SECONDKEYWORD=$(search_second_keyword $RESULTS_SORTEDBYKEYWORD $FIRSTKEYWORD)
    echo -e "\tSecond keyword ="$SECONDKEYWORD
    if [[ $(echo $SECONDKEYWORD | wc -c) -gt 1 ]]; then
        THIRDKEYWORD=$(search_third_keyword  $RESULTS_SORTEDBYKEYWORD $FIRSTKEYWORD $SECONDKEYWORD)
    else
        THIRDKEYWORD=""
    fi
    echo -e "\tThird keyword ="$THIRDKEYWORD


    #search for the target result lines
    grep $FIRSTKEYWORD $RESULTS_SORTEDBYKEYWORD > $OUTPUT_UNSORTED

    if [[ $(grep $CLOUDSERVER_SUBNETADDR $OUTPUT_UNSORTED| wc -l ) -ne 0 ]]; then 
        echo -e "\tcloud results found using sever_subnet=$CLOUDSERVER_SUBNETADDR"
        CLOUDSERVER=1
    else
        CLOUDSERVER=0
    fi
    #v, --invert-match:Invert the sense of matching, to select non-matching lines.
    if [[ $(grep -v $CLOUDSERVER_SUBNETADDR $OUTPUT_UNSORTED | wc -l ) -ne 0 ]]; then 
        echo -e "\tedge results found using sever_subnet=$EDGESERVER_SUBNETADDR1/$EDGESERVER_SUBNETADDR2"
        EDGESERVER=1
    else
        EDGESERVER=0
    fi
    
    if [[ $CLOUDSERVER -eq 0 ]];then
            echo -e "\tcloud measurements not found using sever_subnet=$CLOUDSERVER_SUBNETADDR and Keyword=$FIRSTKEYWORD"
            if [[ $(echo $SECONDKEYWORD | wc -c) -gt 1 ]]; then
                echo -e "\t\tSearch results using Keyword=$SECONDKEYWORD"

                grep $SECONDKEYWORD $RESULTS_SORTEDBYKEYWORD > $TEMPFILE
                grep $CLOUDSERVER_SUBNETADDR $TEMPFILE >> $OUTPUT_UNSORTED
            else
                echo -e "\033[91mSECONDKEYWORD is null. Aborted\033[0m"
            fi

            
            if [[ $(grep $CLOUDSERVER_SUBNETADDR $OUTPUT_UNSORTED| wc -l ) -ne 0 ]]; then 
                echo -e "\tcloud results found using server_subnet=$CLOUDSERVER_SUBNETADDR"
            else
                echo -e "\tcloud results not found using server_subnet=$CLOUDSERVER_SUBNETADDR and Keyword = $SECONDKEYWORD"
                if [[ $(echo $THIRDKEYWORD | wc -c) -gt 1 ]]; then
                    echo -e "\t\tSearch with keyword $THIRDKEYWORD"
                    grep $THIRDKEYWORD $INPUTFILENAME > $TEMPFILE
                    grep $CLOUDSERVER_SUBNETADDR $TEMPFILE >> $OUTPUT_UNSORTED
                else
                    echo -e "\033[91mTHIRDKEYWORD is null. Aborted\033[0m"
            fi
            
        fi
    fi

    if [[ $EDGESERVER -eq 0 ]];then
        echo -e "\tedge measurements not found using server_subnet=$EDGESERVER_SUBNETADDR1/$EDGESERVER_SUBNETADDR2 and Keyword=$FIRSTKEYWORD"
        if [[ $(echo $SECONDKEYWORD | wc -c) -gt 1 ]]; then
            echo -e "\n\tSearch with keyword $SECONDKEYWORD"
            
            grep $SECONDKEYWORD $RESULTS_SORTEDBYKEYWORD > $TEMPFILE
            #-v, --invert-match: Invert the sense of matching, to select non-matching lines.
            grep -v $CLOUDSERVER_SUBNETADDR $TEMPFILE >> $OUTPUT_UNSORTED

            #v, --invert-match:Invert the sense of matching, to select non-matching lines.
            if [[ $(grep -v $CLOUDSERVER_SUBNETADDR $OUTPUT_UNSORTED | wc -l ) -ne 0 ]]; then 
                echo -e "\tedge results found using sever_subnet=$EDGESERVER_SUBNETADDR1/$EDGESERVER_SUBNETADDR2"
            else
                echo -e "\tEdgeserver results not found using sever_subnet==$EDGESERVER_SUBNETADDR1/$EDGESERVER_SUBNETADDR2 and Keyword=$SECONDKEYWORD"
                    
                if [[ $(echo $THIRDKEYWORD | wc -c) -gt 1 ]]; then
                    echo -e "\n\tSearch with keyword $THIRDKEYWORD"

                    grep $THIRDKEYWORD $RESULTS_SORTEDBYKEYWORD > $TEMPFILE
                    #-v, --invert-match: Invert the sense of matching, to select non-matching lines.
                    grep -v $CLOUDSERVER_SUBNETADDR $TEMPFILE >> $OUTPUT_UNSORTED
                else
                    echo -e "\033[91mTHIRDKEYWORD is null. Aborted\033[0m"
                fi       
            fi
        else
            echo -e "\t\t\033[91mSECONDKEYWORD is null. Aborted\033[0m"
        fi
    fi

    #k5 = ServerIP
    #k3 = ClientIP
    #k4 = ClientPort
    #k6 = ServerPort
    #k13 = Timestamp
    sort -t, -k5,5 -k6,6 -k3,3 -k4,4 -k13,13  $OUTPUT_UNSORTED >>$OUTPUTFILENAME
done






