#!/bin/bash
java -jar Observer.jar --aggregator-ip "192.168.122.3" --aggregator-port 6766 --remote-ip  "192.168.122.4"  --remote-cmd-port 6789 --remote-tcp-port 6788 --remote-udp-port 6787 --observer-cmd-port 6792 --observer-tcp-port 6791 --observer-udp-port 6790
