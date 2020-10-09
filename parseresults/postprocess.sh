#!/bin/bash

INPUTFILENAME="mim-bandwidth-downlink-wifi-1clients-filetest_1-noise0M_2019-03-23-00:00:00-2021-03-27-23:59:50.csv"
OUTPUTFILENAME="mim-bandwidth-downlink-wifi-1clients-filetest_1-noise0M_2019-03-23-00:00:00-2021-03-27-23:59:50_SORTED.csv"
TEMPFILE="temp.txt"
TEMPFILE2="temp2.txt"


for INPUTFILENAME in *.csv; do
    OLDIFS=$IFS
    echo $OLDIFS
    IFS=.
    set $INPUTFILENAME

    echo $1
    OUTPUTFILENAME=$1"_SORTED.csv"

    IFS=$OLDIFS
    echo $INPUTFILENAME
    echo $OUTPUTFILENAME

    #copy the headers
    head -3 $INPUTFILENAME > $OUTPUTFILENAME
    #copy the remaining lines in a temporary file
    tail -n +4 $INPUTFILENAME >$TEMPFILE


    #-t, --field-separator=SEP: use SEP instead of non-blank to blank transition
    #-r, --reverse: reverse the result of comparisons
    #-k, --key=KEYDEF: sort via a key; KEYDEF gives location and type
    #K7 is the keyword -> sorted by keyword in descending order. 
    sort $TEMPFILE -t "," -r -k7 > $TEMPFILE2
    #The first line contains the greatest keyword
    FIRSTLINE=$(head -1 $TEMPFILE2)


    IFS=,
    set $FIRSTLINE
    SEARCHEDKEYWORD=$7
    #echo $SEARCHEDKEYWORD

    grep $SEARCHEDKEYWORD $TEMPFILE2 > $TEMPFILE

    sort $TEMPFILE -t "," -k3 -k4 -k5 -k6 >>$OUTPUTFILENAME

done






