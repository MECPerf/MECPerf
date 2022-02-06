#!/bin/bash
#java -cp commons-cli-1.4.jar:CommandLineApp.jar it.unipi.dii.mecperfng.commandlineapp.CommandLineApp --observer-address "" --measure-type all --direction all --keyword "" --command-port <port-number> --tcp-port <port-number> --udp-port <port-number>  --packet-size-tcp-bandwidth 1420 --num-packet-tcp-bandwidth 1024 --packet-size-udp-capacity 1420 --num-tests-udp-capacity 25 --packet-size-tcp-latency 1 --num-packet-tcp-latency 100 --packet-size-udp-latency 1 --num-packet-udp-latency 100 --timer-interval 30 --node-id "" --interface-name ""

#maesure-type can be one of all, bandwidthTCP, bandwidthUDP, latencyTCP, latencyUDP
java -cp commons-cli-1.4.jar:CommandLineApp.jar it.unipi.dii.mecperfng.commandlineapp.CommandLineApp --observer-address 131.114.73.2 --measure-type all --direction all --keyword "prova insermento REST" --command-port 6792 --tcp-port 6791 --udp-port 6790  --packet-size-tcp-bandwidth 1420 --num-packet-tcp-bandwidth 30  --num-tests-udp-capacity 3 --packet-size-udp-capacity 1420 --packet-size-tcp-latency 1 --num-packet-tcp-latency 4 --packet-size-udp-latency 1 --num-packet-udp-latency 5 --timer-interval 100 --node-id "TESTNODE" --interface-name "enp0s3" --crosstraffic "OMbps" --observer-position "edge" --access-technology "wifi"  --number-of-clients 1 




SELECT Timestamp AS "time",        (1.0 *(SUM(kBytes) * 1024 *1024 )/(1.0 * SUM(nanoTimes) / 1000000000)) AS "MEC -> Cloud - MBps" FROM Test INNER JOIN BandwidthMeasure ON Test.ID = BandwidthMeasure.id WHERE (SenderIdentity = 'Observer' AND ReceiverIdentity = 'Server' AND Command = 'TCPBandwidth') GROUP BY Test.ID ORDER BY time desc limit 10;


for j in 0 1 2 3 4 5 6 7 8 9 
    do
java -cp commons-cli-1.4.jar:CommandLineApp.jar it.unipi.dii.mecperfng.commandlineapp.CommandLineApp --observer-address 131.114.73.2 --measure-type bandwidthTCP --direction sender --keyword "Validazione REST" --command-port 6792 --tcp-port 6791 --udp-port 6790  --packet-size-tcp-bandwidth 1420 --num-packet-tcp-bandwidth 1024  --num-tests-udp-capacity 1000 --packet-size-udp-capacity 1420 --packet-size-tcp-latency 1 --num-packet-tcp-latency 100 --packet-size-udp-latency 1 --num-packet-udp-latency 100 --node-id "TESTNODE" --interface-name "eth0" --crosstraffic "OMbps" --observer-position "edge" --access-technology "wifi"  --number-of-clients 1 
done; for j in 0 1 2 3 4 5 6 7 8 9 
    do
java -cp commons-cli-1.4.jar:CommandLineApp.jar it.unipi.dii.mecperfng.commandlineapp.CommandLineApp --observer-address 131.114.73.2 --measure-type bandwidthUDP --direction sender --keyword "Validazione REST" --command-port 6792 --tcp-port 6791 --udp-port 6790  --packet-size-tcp-bandwidth 1420 --num-packet-tcp-bandwidth 1024  --num-tests-udp-capacity 1000 --packet-size-udp-capacity 1420 --packet-size-tcp-latency 1 --num-packet-tcp-latency 100 --packet-size-udp-latency 1 --num-packet-udp-latency 100 --node-id "TESTNODE" --interface-name "eth0" --crosstraffic "OMbps" --observer-position "edge" --access-technology "wifi"  --number-of-clients 1 
done




