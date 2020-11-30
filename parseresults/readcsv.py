import csv
import sys
import logging
import datetime


from collections import OrderedDict
_MINROWNUMBER = 50

def _get_subnetaddresses(config_parser, section, conntype, logger):
    if conntype == "wifi":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_wifi")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_wifi")
        cloudserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_wifi")
    elif conntype == "lte":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_lte")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_lte")
        cloudserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_lte")
    else:
        print ("unknown connection type")
        logger.critical("unknown connection type " + str(conntype))
        logger.critical("EXIT")
        sys.exit(0)

    return client_subnetaddr, edgeserver_subnetaddr, cloudserver_subnetaddr


def readvalues_activelatencyboxplot(inputfile, noise, segment):
    ret = []

    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0
        for row in csvreader:
            if linecount == 0 or linecount == 1:
                linecount += 1
                continue

            if linecount == 2:
                try:
                    assert row[12] == "latency"
                    assert row[9] == "Keyword"
                    assert row[6] == "ReceiverIdentity"
                    assert row[5] == "SenderIdentity"
                    assert row[4] == "Command"
                    assert row[3] == "Direction"
                except Exception as e:
                    print (e)
                    print (row)
                    sys.exit(1)

                linecount += 1
                #print row
                continue

        
            linecount += 1

            command = row[4]
            senderIdentity = row[5]
            receiverIdentity = row[6]
            latency = float(row[12])
            direction = row[3]
            keyword = row[9]

            assert command == "TCPRTT" or command == "UDPRTT"

            if segment == "clientNitos":
                if "local" not in keyword:
                    continue

                if direction == "Upstream":
                    #Upstream: Client -> local observer
                    if senderIdentity != "Client":
                        continue
                    else:
                        ret.append(latency)

                elif direction == "Downstream":
                    #Upstream: Client <- local observer
                    if receiverIdentity != "Client":
                        continue
                    else:
                        ret.append(latency)
                
                else:
                    print ("unknown direction")
                    sys.exit(0)                
                
            elif segment == "clientUnipi":
                if "remote" not in keyword:
                    continue

                if direction == "Upstream":
                    #Upstream: Client -> cloud observer
                    if senderIdentity != "Client":
                        continue
                    else:
                        ret.append(latency)

                elif direction == "Downstream":
                    #Upstream: Client <- cloud observer
                    if receiverIdentity != "Client":
                        continue
                    else:
                        ret.append(latency)
                
                else:
                    print ("unknown direction")
                    sys.exit(0)


            elif segment == "NitosUnipi":
                if "local" not in keyword:
                    continue

                if direction == "Upstream":
                    #Upstream: local observer -> remote server
                    if senderIdentity != "Observer":
                        continue
                    else:
                        ret.append(latency)

                elif direction == "Downstream":
                    #Upstream: local observer <- remote(cloud) server
                    if receiverIdentity != "Observer":
                        continue
                    else:
                        ret.append(latency)
                
                else:
                    print ("unknown direction")
                    sys.exit(0)
    
       
            else:
                print ("unknown segment")
                sys.exit(0)

        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")
    
    return ret
def readvalues_activebandwidthboxplot(inputfile, noise, segment):
    ret = []
    Mb_s = 0
    sec = 0
    lastID = -1

    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0
        for row in csvreader:
            if linecount == 0 or linecount == 1:
                linecount += 1
                continue

            if linecount == 2:
                try:
                    assert row[12] == "Kbit"
                    assert row[13] == "nanoTimes"
                    assert row[9] == "Keyword"
                    assert row[6] == "ReceiverIdentity"
                    assert row[5] == "SenderIdentity"
                    assert row[4] == "Command"
                    assert row[3] == "Direction"
                    assert row[1] == "ID"
                except Exception as e:
                    print (e)
                    print (row)
                    sys.exit(1)

                linecount += 1
                #print row
                continue
          
            linecount += 1

            testID = row[1]
            command = row[4]
            senderIdentity = row[5]
            receiverIdentity = row[6]
            Kbit = float(row[12])
            Mbit = Kbit/1000
            nanoTimes = float(row[13])
            s = nanoTimes/1000000000
            direction = row[3]
            keyword = row[9]

            assert command == "TCPBandwidth" or command == "UDPBandwidth"
    
            if segment == "clientNitos":
                if "local" not in keyword:
                    continue

                if direction == "Upstream":
                    #Upstream: Client -> local observer
                    if senderIdentity != "Client":
                        continue
                    else:
                        if lastID == -1:
                            #first measure
                            lastID = testID
                            Mb_s = Mbit
                            sec =s
                        elif testID == lastID:
                            #new mesure, same test
                            Mb_s += Mbit
                            sec += s
                        else:
                            #new test
                            ret.append(Mb_s/sec)

                            lastID = testID
                            Mb_s = Mbit
                            sec =s
                elif direction == "Downstream":
                    #Upstream: Client <- local observer
                    if receiverIdentity != "Client":
                        continue
                    else:
                        if lastID == -1:
                            #first measure
                            lastID = testID
                            Mb_s = Mbit
                            sec =s
                        elif testID == lastID:
                            #new mesure, same test
                            Mb_s += Mbit
                            sec += s
                        else:
                            #new test
                            ret.append(Mb_s/sec)

                            lastID = testID
                            Mb_s = Mbit
                            sec =s
                else:
                    print ("unknown direction")
                    sys.exit(0)                
                
            elif segment == "clientUnipi":
                if "remote" not in keyword:
                    continue

                if direction == "Upstream":
                    #Upstream: Client -> remote observer (unipi)
                    if senderIdentity != "Client":
                        continue

                    else:
                        if lastID == -1:
                            #first measure
                            lastID = testID
                            Mb_s = Mbit
                            sec =s
                        elif testID == lastID:
                            #new mesure, same test
                            Mb_s += Mbit
                            sec += s
                        else:
                            #new test
                            ret.append(Mb_s/sec)

                            lastID = testID
                            Mb_s = Mbit
                            sec =s
                elif direction == "Downstream":
                    #Upstream: Client <- remote observer (unipi)
                    if receiverIdentity != "Client":
                        continue
                    else:
                        if lastID == -1:
                            #first measure
                            lastID = testID
                            Mb_s = Mbit
                            sec =s
                        elif testID == lastID:
                            #new mesure, same test
                            Mb_s += Mbit
                            sec += s
                        else:
                            #new test
                            ret.append(Mb_s/sec)

                            lastID = testID
                            Mb_s = Mbit
                            sec =s                
                else:
                    print ("unknown direction")
                    sys.exit(0)

            elif segment == "NitosUnipi":
                if "local" not in keyword:
                    continue

                if direction == "Upstream":
                    #Upstream:  MEC observer(local) -> remote (cloud) server
                    if senderIdentity != "Observer":
                        continue
                    else:
                        if lastID == -1:
                            #first measure
                            lastID = testID
                            Mb_s = Mbit
                            sec =s
                        elif testID == lastID:
                            #new mesure, same test
                            Mb_s += Mbit
                            sec += s
                        else:
                            #new test
                            ret.append(Mb_s/sec)

                            lastID = testID
                            Mb_s = Mbit
                            sec =s
                elif direction == "Downstream":
                    #Upstream: MEC observer(local) <- remote (cloud) server
                    if receiverIdentity != "Observer":
                        continue
                    else:
                        if lastID == -1:
                            #first measure
                            lastID = testID
                            Mb_s = Mbit
                            sec =s
                        elif testID == lastID:
                            #new mesure, same test
                            Mb_s += Mbit
                            sec += s
                        else:
                            #new test
                            ret.append(Mb_s/sec)

                            lastID = testID
                            Mb_s = Mbit
                            sec =s                
                else:
                    print ("unknown direction")
                    sys.exit(0)
    
            else:
                print ("unknown segment")
                sys.exit(0)

        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")

    return ret
def readvalues_activebandwidthlineplot(config_parser, section, command, direction, conn):
    noiselist = config_parser.get(section, "noise").split(",")

    if conn == "wifi":
        dates_list = config_parser.get(section, "dates_activewifi").split(",")
    elif conn == "lte":
        dates_list = config_parser.get(section, "dates_activelte").split(",")


    legend = []
    if direction == "Upstream":
        legend.append("Client -> Observer (Nitos)")
        legend.append("Client -> Observer (unipi)")
        legend.append("Observer (Nitos) -> Remote(unipi)")
        legend.append("Observer (unipi) -> Remote(unipi)")
    elif direction == "Downstream":
        legend.append("Observer (Nitos) -> Client")
        legend.append("Observer (unipi) -> Client")
        legend.append("Remote(unipi) -> Observer (Nitos)")
        legend.append("Remote(unipi) -> Observer (unipi)")

    clientNitos = {"x":[], "y":[], "legend": legend[0]}
    clientUnipi = {"x":[], "y":[], "legend": legend[1]}
    NitosUnipi = {"x":[], "y":[], "legend": legend[2]}

    lastID = -1
    Mbitlist = []
    seclist = []
    for noise in noiselist:
        inputfile = "csv/active/" + command + "-" + direction + "-" + conn + "-noise" + noise + "_" 
        inputfile += dates_list[0].strip() + "-" + dates_list[-1].strip() + ".csv"

        with open (inputfile, "r") as csvinput:
            csvreader = csv.reader(csvinput, delimiter=",")
            linecount = 0

            for row in csvreader:
                if linecount == 0 or linecount == 1:
                    linecount += 1
                    continue

                if linecount == 2:
                    try:
                        if command == "TCPBandwidth" or command == "UDPBandwidth":
                            assert row[12] == "Kbit"
                            assert row[13] == "nanoTimes"
                        elif command == "TCPRTT" or command == "UDPRTT":
                            assert row[12] == "latency"
                        else:
                            print ("unknown command")
                            sys.exit(0)

                        assert row[9] == "Keyword"
                        assert row[5] == "SenderIdentity"
                        assert row[6] == "ReceiverIdentity"
                        assert row[3] == "Direction"
                        assert row[2] == "Timestamp"
                        assert row[1] == "ID"
                    except:
                        print (row)
                        sys.exit(1)

                    linecount += 1
                    #print row
                    continue

                linecount += 1

        
                if direction != row[3]:
                    sys.exit(0)
                    continue

                if command == "TCPBandwidth" or command == "UDPBandwidth":
                    Kbit = float(row[12])
                    Mbit = Kbit/1024
                    nanosec = float(row[13])
                    sec = 1.0 * nanosec/1000000000

                elif command == "TCPRTT" or command == "UDPRTT":
                    # row[12] == "latency"
                    measure = 100
                

                if "local" in row[9]:
                    if row[5] == "Client" or row[6] == "Client":
                        if command == "TCPBandwidth" or command == "UDPBandwidth":
                            if lastID == -1 or lastID == row[1]:
                                Mbitlist.append(Mbit)
                                seclist.append(sec)
                                if lastID == -1:
                                    lastID = row[1]
                            else:
                                lastID = row[1]
                                clientNitos["y"].append(1.0 * sum(Mbitlist)/sum(seclist))
                                clientNitos["x"].append(row[2])
                                Mbitlist = []
                                seclist = []
                        else:
                            clientNitos["y"].append(100)

                    elif row[5] == "Server" or row[6] == "Server":
                        if command == "TCPBandwidth" or command == "UDPBandwidth":
                            if lastID == -1 or lastID == row[1]:
                                Mbitlist.append(Mbit)
                                seclist.append(sec)
                                if lastID == -1:
                                    lastID = row[1]
                            else:
                                lastID = row[1]
                                NitosUnipi["y"].append(1.0 * sum(Mbitlist)/sum(seclist))
                                NitosUnipi["x"].append(row[2])
                                Mbitlist = []
                                seclist = []
                        else:
                            NitosUnipi["y"].append(200)
                    else:
                        print ("error")
                        print (row)
                        sys.exit(0)


                if "remote" in row[9]:
                    if row[5] == "Client" or row[6] == "Client":                        
                        if command == "TCPBandwidth" or command == "UDPBandwidth":
                            if lastID == -1 or lastID == row[1]:
                                Mbitlist.append(Mbit)
                                seclist.append(sec)
                                if lastID == -1:
                                    lastID = row[1]
                            else:
                                lastID = row[1]
                                clientUnipi["y"].append(1.0 * sum(Mbitlist)/sum(seclist))
                                clientUnipi["x"].append(row[2])
                                Mbitlist = []
                                seclist = []
                        else:
                            clientUnipi["y"].append(300)

                    elif row[5] == "Server" or row[6] == "Server":
                        continue
                    else:
                        print ("error")
                        print (row)
                        sys.exit(0)

            print ("read " + str(linecount) + " from " + inputfile + "(including headers)")

    
    return clientNitos, clientUnipi, NitosUnipi

      
def readbandwidthvalues_timeseries_self(config_parser, section, inputfile, edgeserver, conntype):
    assert "SORTED_LEGACY" in inputfile
    assert "self" in inputfile
    ret = []

    if conntype == "wifi":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_wifi")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_wifi")
        remoteserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_wifi")
    elif conntype == "lte":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_lte")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_lte")
        remoteserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_lte")
    else:
        print ("unknown connection type")
        sys.exit(0)
    
    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0
        for row in csvreader:
            if linecount == 0 or linecount == 1:
                linecount += 1
                continue
            if linecount == 2:
                try:
                    #columns: ID,Timestamp,ClientIP,ClientPort,ServerIP,ServerPort,Keyword,Direction,Protocol,Mode,Type,ID,Timestamp,Bytes
                    assert row[13] == "Bytes"
                    assert row[12] == "Timestamp"
                    assert row[6] == "Keyword"
                    assert row[2] == "ClientIP"
                    assert row[4] == "ServerIP"
                except Exception as e:
                    print (e)
                    print (row)
                    sys.exit(1)

                linecount += 1
                #print row
                continue

            

            measuredbytes = row[13]
            current_timenstamp = row[12]
            keyword = row[6]
            clientIP = row[2]
            serverIP = row[4] 
            try:
                assert row[2][:len(client_subnetaddr)].strip() == client_subnetaddr.strip()
                assert conntype.strip() in inputfile
            except Exception as e:
                print (conntype)
                print (inputfile)
                print (row[2] [:len(client_subnetaddr)] + "!=" + client_subnetaddr)
                linecount += 1
                continue
            
            linecount += 1

            if  (edgeserver == True and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (edgeserver == False and serverIP[:len(remoteserver_subnetaddr)] == remoteserver_subnetaddr):

                bandwidthkbps = float(row[13])
                bandwidthMbps = bandwidthkbps / 1000
                ret.append(bandwidthMbps)

        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")


    return ret


def readbandwidthvalues_mim(config_parser, section, inputfile, connectiontype, segment, logger):
    assert "SORTED_LEGACY" in inputfile
    assert "mim" in inputfile
    logger.debug("\n")
    
    ret = []
    last_testID = ""
    
    if connectiontype == "wifi":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_wifi")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_wifi")
        cloudserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_wifi")
    elif connectiontype == "lte":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_lte")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_lte")
        cloudserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_lte")
    else:
        print ("unknown connection type")
        logger.critical("unknown connection type")
        logger.critical("EXIT")
        sys.exit(0)
    
    logger.debug("inputfile = " + str(inputfile))
    logger.debug("connectiontype = " + str(connectiontype))
    logger.debug("segment = " + segment)
    logger.debug("client_subnetaddr = " + str(client_subnetaddr))
    logger.debug("edgeserver_subnetaddr = " + str(edgeserver_subnetaddr))
    logger.debug("cloudserver_subnetaddr = " + str(cloudserver_subnetaddr))

    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0

        for row in csvreader:
            #line #0 contains the query
            #line #1 contains query's arguments 
            if linecount == 0 or linecount == 1:
                linecount += 1
                logger.debug("line " + (str(linecount) + ": " + str(row)))
                continue
            #line #3 contains the name of each column
            #       mim-bandwidth columns: ID,Timestamp,ClientIP,ClientPort,ServerIP,ServerPort,Keyword,
            #                             Direction,Protocol,Mode,Type,ID,Timestamp,Bytes
            if linecount == 2:
                try:
                    assert row[13] == "Bytes"
                    assert row[12] == "Timestamp" # in microsec
                    assert row[6] == "Keyword"
                    assert row[2] == "ClientIP"
                    assert row[4] == "ServerIP"
                    assert row[3] == "ClientPort"
                    assert row[5] == "ServerPort"
                except:
                    print (row)
                    logger.critical("linecount = 2 " + str(row) + "unexpected columns")
                    logger.critical ("EXIT")
                    sys.exit(1)

                linecount += 1
                #print row
                continue            
            
            linecount += 1

            byte = float(row[13])
            currenttimestamp_micros = float(row[12])  #timestamp microsecons

            clientIP = row[2]
            serverIP = row[4]
            clientPort = row[3]
            serverPort = row[5]
            
            
            if  (segment == "edge" and row[4][:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (segment == "remote" and row[4][:len(cloudserver_subnetaddr)] == cloudserver_subnetaddr):

                currentTestID = clientIP + "-" + clientPort + "-" + serverIP + "-" + serverPort

                if last_testID == "":
                    #this is the first row
                    last_testID = currentTestID
                    lastclientIP = clientIP

                    ####################  FOR DEBUGGING ONLY ####################
                    lastClientPort = clientPort
                    lastServerIP = serverIP
                    lastServerPort = serverPort
                    ############################################################

                    previoustimestamp_micros = currenttimestamp_micros
                    packets_bandwidth = []
                    currentByte = 0.0
                    rowcounter = 1
                    current_micros = 0.0
                elif last_testID == currentTestID:
                    #same test
                    ####################  FOR DEBUGGING ONLY ####################
                    assert clientIP == lastclientIP
                    assert serverIP == lastServerIP
                    assert clientPort == lastClientPort
                    assert serverPort == lastServerPort
                    try:
                        assert previoustimestamp_micros <= currenttimestamp_micros
                    except:
                        print (previoustimestamp_micros)
                        print (currenttimestamp_micros)

                        logger.critical("previoustimestamp_micros = " + str(previoustimestamp_micros))
                        logger.critical("currenttimestamp_micros = " + str(currenttimestamp_micros))
                        sys.exit(0)
                    ##############################################################

                    rowcounter += 1

                    if byte > 0:
                        currentByte += byte
                        current_micros += currenttimestamp_micros - previoustimestamp_micros

                        if current_micros >= 1000000: #more than one sec
                            current_s = current_micros /1000000 #from microseconds to seconds
                            bps = (currentByte * 8) / current_s
                            Mbps = bps / 1000000
                            
                            packets_bandwidth.append(Mbps)

                            currentByte = 0.0
                            current_micros = 0.0

                    previoustimestamp_micros = currenttimestamp_micros
                else:
                    #newtest
                    if currentByte > 0:
                        current_s = current_micros /1000000
                        bps = (currentByte * 8) / current_s
                        Mbps = bps / 1000000

                        packets_bandwidth.append(Mbps)
                    
                    if rowcounter >= _MINROWNUMBER:
                        for elem in packets_bandwidth:
                            #print (elem)
                            ret.append(elem)
                        #logger.debug("accepted " + last_testID + " with rowcounter " + str(rowcounter))
                    else:
                        #print ("skipped " + last_testID + " with rowcounter " + str(rowcounter))
                        logger.debug("skipped " + last_testID + " with rowcounter " + str(rowcounter))
                    

                    last_testID = currentTestID
                    lastclientIP = clientIP
                    ############################FOR DEBUGGING ONLY##############################
                    lastClientPort = clientPort
                    lastServerIP = serverIP
                    lastServerPort = serverPort    
                    ###########################################################################
                    
                    previoustimestamp_micros = currenttimestamp_micros
                    packets_bandwidth = []
                    currentByte = 0.0
                    rowcounter = 1
                    current_micros = 0.0

        #print ret
        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")
    
        
    return ret
def readbandwidthvalues_mim_usingfixbucket(config_parser, section, inputfile, connectiontype, segment, logger, 
                                           bucketsize_microsec):
    assert "SORTED" in inputfile
    assert "LEGACY" not in inputfile
    assert "mim" in inputfile
    logger.debug("\n")
    
    ret = []
    lastclientIP = ""
    pastclientIP = []
    
    client_subnetaddr, edgeserver_subnetaddr, cloudserver_subnetaddr = _get_subnetaddresses(
                                        config_parser=config_parser, section=section, conntype=connectiontype, 
                                        logger=logger)
    
    logger.debug("inputfile = " + str(inputfile))
    logger.debug("bucketsize_microsec = " + str(bucketsize_microsec))
    logger.debug("connectiontype = " + str(connectiontype))
    logger.debug("segment = " + segment)
    logger.debug("client_subnetaddr = " + str(client_subnetaddr))
    logger.debug("edgeserver_subnetaddr = " + str(edgeserver_subnetaddr))
    logger.debug("cloudserver_subnetaddr = " + str(cloudserver_subnetaddr))

    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0

        for row in csvreader:
            #line #0 contains the query
            #line #1 contains query's arguments 
            if linecount == 0 or linecount == 1:
                linecount += 1
                logger.debug("line " + (str(linecount) + ": " + str(row)))
                continue
            #line #3 contains the name of each column
            #       mim-bandwidth columns: ID,Timestamp,ClientIP,ClientPort,ServerIP,ServerPort,Keyword,
            #                             Direction,Protocol,Mode,Type,ID,Timestamp,Bytes
            if linecount == 2:
                try:
                    assert row[13] == "Bytes"
                    assert row[12] == "Timestamp" # in microsec
                    assert row[6] == "Keyword"
                    assert row[2] == "ClientIP"
                    assert row[4] == "ServerIP"
                    assert row[3] == "ClientPort"
                    assert row[5] == "ServerPort"
                except:
                    print (row)
                    logger.critical("linecount = 2 " + str(row) + "unexpected columns")
                    logger.critical ("EXIT")
                    sys.exit(1)

                linecount += 1
                #print row
                continue            
            
            linecount += 1

            byte = float(row[13])
            currenttimestamp_micros = float(row[12])  #timestamp microsecons
            clientIP = row[2]
            serverIP = row[4]
            
            if  (segment == "edge" and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (segment == "remote" and serverIP[:len(cloudserver_subnetaddr)] == cloudserver_subnetaddr):

                if lastclientIP == "":
                    #this is the first row containing results for the target server
                    lastclientIP = clientIP
                    ####################  FOR DEBUGGING ONLY ####################
                    pastclientIP.append(clientIP)
                    lastServerIP = serverIP
                    ############################################################

                    currentBytes = 0.0
                    previoustimestamp_micros = currenttimestamp_micros
                    bucket_starttime_microsec = currenttimestamp_micros
                    bucket_endtime_microsec = bucket_starttime_microsec + bucketsize_microsec                     
                elif lastclientIP == clientIP:
                    #same testID
                    ####################  FOR DEBUGGING ONLY ####################
                    assert serverIP == lastServerIP
                    try:
                        assert clientIP in pastclientIP
                        assert previoustimestamp_micros <= currenttimestamp_micros
                    except Exception as e:
                        print (clientIP)
                        print(pastclientIP)
                        print (linecount)
                        print (previoustimestamp_micros)
                        print (currenttimestamp_micros)

                        logger.critical("assertion failed: assert previoustimestamp_micros <= currenttimestamp_micros")
                        logger.critical("line number = " + str(linecount))
                        logger.critical("previoustimestamp_micros=" + str(previoustimestamp_micros))
                        logger.critical("currenttimestamp_micros=" + str(currenttimestamp_micros))
                        logger.critical ("EXIT")
                        sys.exit(-1)
                    ##############################################################
                    
                    if currenttimestamp_micros < bucket_endtime_microsec:
                        #packet received within the bucketsize_microsec interval
                        currentBytes += byte
                    else:
                        #packet received within the next bucketsize_microsec interval
                        if currentBytes == 0:
                            Mbps = 0
                        else:
                            bucketsize_sec = 1.0 * bucketsize_microsec / 1000000
                            bps = (1.0 * currentBytes * 8) / bucketsize_sec
                            Mbps = bps/1000000

                        ret.append(Mbps)
                        currentBytes = byte
                        bucket_starttime_microsec = bucket_endtime_microsec
                        bucket_endtime_microsec = bucket_starttime_microsec + bucketsize_microsec                               
                else:
                    #switch to a new client
                    pastclientIP.append(clientIP)
                    #add the last results
                    if currentBytes == 0:
                        Mbps = 0
                    else:
                        bucketsize_sec = 1.0 * bucketsize_microsec / 1000000
                        bps = (1.0 * currentBytes * 8) / bucketsize_sec
                        Mbps = bps/1000000   
                    ret.append(Mbps)

                    lastclientIP = clientIP
                    lastServerIP = serverIP
                
                    currentByte = 0.0
                    bucket_starttime_microsec = currenttimestamp_micros
                    bucket_endtime_microsec = bucket_starttime_microsec + bucketsize_microsec             
        
                previoustimestamp_micros = currenttimestamp_micros

        #print ret
        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")
       
    return ret
def readbandwidthvalues_self(config_parser, section, inputfile, edgeserver, conntype):
    assert "LEGACY" not in inputfile
    assert "SORTED" in inputfile
    assert "self" in inputfile

    ret = []

    if conntype == "wifi":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_wifi")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_wifi")
        remoteserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_wifi")
    elif conntype == "lte":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_lte")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_lte")
        remoteserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_lte")
    else:
        print ("unknown connection type")
        sys.exit(0)
    
    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0
        for row in csvreader:
            if linecount == 0 or linecount == 1:
                linecount += 1
                continue
            if linecount == 2:
                try:
                    assert row[13] == "Bytes"
                    assert row[6] == "Keyword"
                    assert row[2] == "ClientIP"
                    assert row[4] == "ServerIP"
                except Exception as e:
                    
                    print (e)
                    print (row)
                    sys.exit(1)

                linecount += 1
                #print row
                continue

            measuredbytes = row[13]
            keyword = row[6]
            clientIP = row[2]
            serverIP = row[4] 
            try:
                assert row[2][:len(client_subnetaddr)].strip() == client_subnetaddr.strip()
                assert conntype.strip() in inputfile
            except Exception as e:
                print (conntype)
                print (inputfile)
                print (row[2] [:len(client_subnetaddr)] + "!=" + client_subnetaddr)
                linecount += 1
                continue
            
            linecount += 1

            if  (edgeserver == True and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (edgeserver == False and serverIP[:len(remoteserver_subnetaddr)] == remoteserver_subnetaddr):

                bandwidthkbps = float(row[13])
                bandwidthMbps = bandwidthkbps / 1000
                ret.append(bandwidthMbps)

        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")

    return ret

#returns a dict
def readbandwidthvalues_mim_perclient(config_parser, section, inputfile, connectiontype, segment, logger):
    assert "SORTED_LEGACY" in inputfile
    assert "mim" in inputfile
    logger.debug("\n")
    
    #ret = {}
    ret= OrderedDict()
    last_testID = ""
    
    if connectiontype == "wifi":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_wifi")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_wifi")
        cloudserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_wifi")
    elif connectiontype == "lte":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_lte")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_lte")
        cloudserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_lte")
    else:
        print ("unknown connection type")
        logger.critical("unknown connection type.")
        logger.critical("EXIT")
        sys.exit(0)

    logger.debug("inputfile = " + str(inputfile))
    logger.debug("connectiontype = " + str(connectiontype))
    logger.debug("segment = " + segment)
    logger.debug("client_subnetaddr = " + str(client_subnetaddr))
    logger.debug("edgeserver_subnetaddr = " + str(edgeserver_subnetaddr))
    logger.debug("cloudserver_subnetaddr = " + str(cloudserver_subnetaddr))
    
    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0
        for row in csvreader:
            #line #0 contains the query
            #line #1 contains query's arguments 
            if linecount == 0 or linecount == 1:
                linecount += 1
                logger.debug("line " + (str(linecount) + ": " + str(row)))
                continue
            #line #3 contains the name of each column
            #       mim-bandwidth columns: ID,Timestamp,ClientIP,ClientPort,ServerIP,ServerPort,Keyword,
            #                             Direction,Protocol,Mode,Type,ID,Timestamp,Bytes
            if linecount == 2:
                try:
                    assert row[13] == "Bytes"
                    assert row[12] == "Timestamp" # in microsec
                    assert row[6] == "Keyword"
                    assert row[2] == "ClientIP"
                    assert row[4] == "ServerIP"
                    assert row[3] == "ClientPort"
                    assert row[5] == "ServerPort"
                except:
                    print (row)
                    logger.critical("linecount = 2 " + str(row) + "unexpercted columns")
                    logger.critical ("EXIT")
                    sys.exit(1)

                linecount += 1
                continue            
            
            linecount += 1

            byte = float(row[13])
            currenttimestamp_micros = float(row[12])  #timestamp microseconds
            clientIP = row[2]
            serverIP = row[4]
            clientPort = row[3]
            serverPort = row[5]
            
            if  (segment == "edge" and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (segment == "cloud" and serverIP[:len(cloudserver_subnetaddr)] == cloudserver_subnetaddr):

                currentTestID = clientIP + "-" + clientPort + "-" + serverIP + "-" + serverPort
                if last_testID == "":
                    #this is the first row that contains results values
                    last_testID = currentTestID
                    lastclientIP = clientIP

                    ####################  FOR DEBUGGING ONLY ####################
                    lastClientPort = clientPort
                    lastServerIP = serverIP
                    lastServerPort = serverPort
                    ############################################################

                    previoustimestamp_micros = currenttimestamp_micros
                    packets_bandwidth = []
                    currentByte = 0.0
                    totalbytes = byte
                    rowcounter = 1
                    current_micros = 0.0 
                elif last_testID == currentTestID:
                    #same testID
                    ####################  FOR DEBUGGING ONLY ####################
                    assert clientIP == lastclientIP
                    assert serverIP == lastServerIP
                    assert clientPort == lastClientPort
                    assert serverPort == lastServerPort
                    try:
                        assert previoustimestamp_micros <= currenttimestamp_micros
                    except Exception as e:
                        exception_type, exception_obj, exception_traceback = sys.exc_info()
                        print (linecount)
                        print ("error on " + str(exception_traceback.tb_frame.f_code.co_filename) + "," + \
                               str(exception_traceback.tb_lineno))
                        print (previoustimestamp_micros)
                        print (currenttimestamp_micros)

                        logger.critical("assertion failed: assert previoustimestamp_micros <= currenttimestamp_micros")
                        logger.critical("line number = " + str(linecount))
                        logger.critical("previoustimestamp_micros=" + str(previoustimestamp_micros))
                        logger.critical("currenttimestamp_micros=" + str(currenttimestamp_micros))
                        logger.critical ("EXIT")
                        sys.exit(-1)
                    ##############################################################

                    rowcounter += 1

                    
                    if byte > 0:
                        #if currenttimestamp_micros - previoustimestamp_micros > 1000000:
                        #    print (currenttimestamp_micros - previoustimestamp_micros)/1000000 * 3
                        #    print(str(currenttimestamp_micros) + "-" + str(previoustimestamp_micros) + ": " + str(currenttimestamp_micros - previoustimestamp_micros))
                        currentByte += byte
                        #totalbyte += byte
                        current_micros += currenttimestamp_micros - previoustimestamp_micros

                        if current_micros >= 1000000: #more than one sec
                            current_s = current_micros /1000000 #from microseconds to seconds
                            bps = (currentByte * 8) / current_s
                            Mbps = bps / 1000000

                            packets_bandwidth.append(Mbps)
                            currentByte = 0.0
                            current_micros = 0.0

                    previoustimestamp_micros = currenttimestamp_micros
                else:
                    #new testID                    
                    if currentByte > 0:
                        current_s = current_micros /1000000
                        bps = (currentByte * 8) / current_s
                        Mbps = bps / 1000000

                        packets_bandwidth.append(Mbps)
                        
                    if rowcounter >= _MINROWNUMBER:
                        #Kb = 1.0 * totalbyte /1024
                        #Mb = Kb / 1024
                        #index = clientIP + str(Mb)
                        #ret[index]=packets_bandwidth
                        ret[clientIP]=packets_bandwidth
                        logger.debug("accepted " + last_testID + " with rowcounter " + str(rowcounter))
                    else:
                        logger.debug("skipped " + last_testID + " with rowcounter " + str(rowcounter))
                    
                    last_testID = currentTestID
                    lastclientIP = clientIP
                    ############################FOR DEBUGGING ONLY##############################
                    lastClientPort = clientPort
                    lastServerIP = serverIP
                    lastServerPort = serverPort    
                    #################################################################
                    
                    previoustimestamp_micros = currenttimestamp_micros
                    packets_bandwidth = []
                    currentByte = 0.0
                    totalbyte = byte
                    rowcounter = 1
                    current_micros = 0.0      
        
        
        if  (segment == "edge" and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
            (segment == "cloud" and serverIP[:len(cloudserver_subnetaddr)] == cloudserver_subnetaddr):
            #add the last flow results
                        
            assert clientIP == lastclientIP
            if rowcounter >= _MINROWNUMBER:
                ret[clientIP]=packets_bandwidth
                logger.debug("accepted " + last_testID + " with rowcounter " + str(rowcounter))
            else:
                logger.debug("skipped " + last_testID + " with rowcounter " + str(rowcounter))
        

        #logger.debug("dict = " + str(ret))
        logger.debug(str(len(ret)) + " clients in ret")
        print ("read  kn" + str(linecount) + " from " + inputfile + "(including headers)")

    return ret
def readbandwidthvalues_mim_perclient_usingfixbucket(config_parser, section, inputfile, connectiontype, 
                                                     segment, logger, bucketsize_microsec):
    assert bucketsize_microsec != None
    assert "SORTED" in inputfile
    assert "LEGACY" not in inputfile
    assert "mim" in inputfile

    logger.debug("\n")
    ret= OrderedDict()
    totalbytes= OrderedDict()
    lastclientIP = ""
    pastclientIP = []
    currentBytes = 0.0
    
    client_subnetaddr, edgeserver_subnetaddr, cloudserver_subnetaddr = _get_subnetaddresses(
                                        config_parser=config_parser, section=section, conntype=connectiontype, 
                                        logger=logger)
    logger.debug("inputfile = " + str(inputfile))
    logger.debug("connectiontype = " + str(connectiontype))
    logger.debug("segment = " + segment)
    logger.debug("client_subnetaddr = " + str(client_subnetaddr))
    logger.debug("edgeserver_subnetaddr = " + str(edgeserver_subnetaddr))
    logger.debug("cloudserver_subnetaddr = " + str(cloudserver_subnetaddr))
    logger.debug("bucketsize_microsec = " + str(bucketsize_microsec))
    
    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0
        for row in csvreader:
            #line #0 contains the query
            #line #1 contains query's arguments 
            if linecount == 0 or linecount == 1:
                linecount += 1
                logger.debug("line " + (str(linecount) + ": " + str(row)))
                continue
            #line #3 contains the name of each column
            #       mim-bandwidth columns: ID,Timestamp,ClientIP,ClientPort,ServerIP,ServerPort,Keyword,
            #                             Direction,Protocol,Mode,Type,ID,Timestamp,Bytes
            if linecount == 2:
                try:
                    assert row[13] == "Bytes"
                    assert row[12] == "Timestamp" # in microsec
                    assert row[6]  == "Keyword"
                    assert row[2]  == "ClientIP"
                    assert row[4]  == "ServerIP"
                    assert row[3]  == "ClientPort"
                    assert row[5]  == "ServerPort"
                except:
                    print (row)
                    logger.critical("linecount = 2 " + str(row) + "unexpected columns")
                    logger.critical ("EXIT")
                    sys.exit(1)

                linecount += 1
                continue            
            
            linecount += 1

            byte = float(row[13])
            currenttimestamp_micros = float(row[12])  #timestamp microseconds
            clientIP = row[2]
            serverIP = row[4]
            
            if  (segment == "edge" and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (segment == "cloud" and serverIP[:len(cloudserver_subnetaddr)] == cloudserver_subnetaddr):
                
                if lastclientIP == "":
                    #this is the first row containing results for the target server
                    lastclientIP = clientIP
                    ####################  FOR DEBUGGING ONLY ####################
                    pastclientIP.append(clientIP)
                    lastServerIP = serverIP
                    ############################################################

                    totalbytes[clientIP] = byte
                    ret[clientIP] = []

                    previoustimestamp_micros = currenttimestamp_micros
                    bucket_starttime_microsec = currenttimestamp_micros
                    bucket_endtime_microsec = bucket_starttime_microsec + bucketsize_microsec                     
                elif lastclientIP == clientIP:
                    #same testID
                    ####################  FOR DEBUGGING ONLY ####################
                    assert serverIP == lastServerIP
                    try:
                        assert clientIP in pastclientIP
                        assert previoustimestamp_micros <= currenttimestamp_micros
                    except Exception as e:
                        print (clientIP)
                        print(pastclientIP)
                        print (linecount)
                        print (previoustimestamp_micros)
                        print (currenttimestamp_micros)

                        logger.critical("assertion failed: assert previoustimestamp_micros <= currenttimestamp_micros")
                        logger.critical("line number = " + str(linecount))
                        logger.critical("previoustimestamp_micros=" + str(previoustimestamp_micros))
                        logger.critical("currenttimestamp_micros=" + str(currenttimestamp_micros))
                        logger.critical ("EXIT")
                        sys.exit(-1)
                    ##############################################################
                    
                    totalbytes[clientIP] += byte
                    if currenttimestamp_micros < bucket_endtime_microsec:
                        #packet received within the bucketsize_microsec interval
                        currentBytes += byte
                    else:
                        #packet received within the next bucketsize_microsec interval
                        if currentBytes == 0:
                            Mbps = 0
                        else:
                            bucketsize_sec = 1.0 * bucketsize_microsec / 1000000
                            bps = (1.0 * currentBytes * 8) / bucketsize_sec
                            Mbps = bps/1000000

                        ret[lastclientIP].append(Mbps)
                        #currentBytes = 0
                        currentBytes = byte
                        bucket_starttime_microsec = bucket_endtime_microsec
                        bucket_endtime_microsec = bucket_starttime_microsec + bucketsize_microsec
                else:

                    #switch to a new client
                    pastclientIP.append(clientIP)
                    #add the last results
                    if currentBytes == 0:
                            Mbps = 0
                    else:
                        #bucketsize_sec = 1.0 * bucketsize_microsec / 1000000
                        bucketsize_sec = 1.0 * (previoustimestamp_micros-bucket_starttime_microsec) / 1000000
                        print (bucketsize_sec)
                        bps = (1.0 * currentBytes * 8) / bucketsize_sec
                        Mbps = bps/1000000   
                    ret[lastclientIP].append(Mbps)
                    

                    lastclientIP = clientIP
                    lastServerIP = serverIP
                
                    currentByte = 0.0
                    ret[clientIP] = []
                    totalbytes[clientIP] = byte
                    bucket_starttime_microsec = currenttimestamp_micros
                    bucket_endtime_microsec = bucket_starttime_microsec + bucketsize_microsec         
        
                previoustimestamp_micros = currenttimestamp_micros
        logger.debug(str(len(ret)) + " clients in ret")
        print ("read  kn" + str(linecount) + " from " + inputfile + "(including headers)")

    return ret, totalbytes

def readbandwidthvalues_self_perclient(config_parser, section, inputfile, server, conntype, logger):
    assert "LEGACY" not in inputfile
    assert "SORTED" in inputfile
    assert "self" in inputfile

    #ret = {}
    ret = OrderedDict()

    if conntype == "wifi":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_wifi")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_wifi")
        remoteserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_wifi")
    elif conntype == "lte":
        client_subnetaddr = config_parser.get(section, "client_subnetaddr_lte")
        edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_lte")
        remoteserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_lte")
    else:
        print ("unknown connection type" + str(conntype))
        sys.exit(0)
    
    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0
        for row in csvreader:
            if linecount == 0 or linecount == 1:
                linecount += 1
                continue
            if linecount == 2:
                try:
                    #columns: ID,Timestamp,ClientIP,ClientPort,ServerIP,ServerPort,Keyword,Direction,Protocol,
                    #         Mode,Type,ID,Timestamp,Bytes
                    assert row[13] == "Bytes"
                    assert row[6] == "Keyword"
                    assert row[2] == "ClientIP"
                    assert row[3] == "ClientPort"
                    assert row[4] == "ServerIP"
                    assert row[5] == "ServerPort"
                except Exception as e:
                    print (e)
                    print (row)
                    sys.exit(1)

                linecount += 1
                #print row
                continue

            measuredbytes = row[13]
            keyword = row[6]
            clientIP = row[2]
            clientPort = row[3]
            serverIP = row[4] 
            ServerPort = row[5]
            currentTestID = ""
            try:
                assert row[2][:len(client_subnetaddr)].strip() == client_subnetaddr.strip()
                assert conntype.strip() in inputfile
            except Exception as e:
                print (conntype)
                print (inputfile)
                print (row[2] [:len(client_subnetaddr)] + "!=" + client_subnetaddr)
                linecount += 1
                continue
            
            linecount += 1
            
            if  (server == "edge" and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (server == "cloud" and serverIP[:len(remoteserver_subnetaddr)] == remoteserver_subnetaddr):

                if not ret.has_key(clientIP):
                    print ("first " + str(clientIP))
                    ret[clientIP] = []

                bandwidthkbps = float(row[13])
                bandwidthMbps = bandwidthkbps / 1000
                #print (bandwidthMbps)
                #print (row)
                ret[clientIP].append(bandwidthMbps)
            #else: 
            #    print("discarded" + str(row))                    
                
        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")


    return ret


def readbandwidthvalues_self_timeplot(config_parser, section, inputfile, segment, conntype, logger):
    assert "LEGACY" not in inputfile
    assert "SORTED" in inputfile
    assert "self" in inputfile
    client_subnetaddr, edgeserver_subnetaddr, cloudserver_subnetaddr = _get_subnetaddresses(
                                        config_parser=config_parser, section=section, conntype=conntype, 
                                        logger=logger)
    evaluate_fragmentquality=config_parser.getboolean(section, "evaluate_fragmentquality")
    logger.debug("inputfile = " + str(inputfile))
    logger.debug("connectiontype = " + str(conntype))
    logger.debug("segment = " + segment)
    logger.debug("client_subnetaddr = " + str(client_subnetaddr))
    logger.debug("edgeserver_subnetaddr = " + str(edgeserver_subnetaddr))
    logger.debug("cloudserver_subnetaddr = " + str(cloudserver_subnetaddr))
    logger.debug("evaluate_fragmentquality = " + str(evaluate_fragmentquality))
    
    ret = OrderedDict()
    
    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0
        for row in csvreader:
            #line 0 contains the query
            #line #1 contains its arguments
            if linecount == 0 or linecount == 1:
                linecount += 1
                continue
            #line #2 contains the name of each column
            #       ID,Timestamp,ClientIP,ClientPort,ServerIP,ServerPort,Keyword,Direction,Protocol,Mode,Type,
            #       ID,Timestamp,Bytes
            if linecount == 2:
                try:
                    assert row[13] == "Bytes"
                    assert row[6]  == "Keyword"
                    assert row[2]  == "ClientIP"
                    assert row[3]  == "ClientPort"
                    assert row[4]  == "ServerIP"
                except Exception as e:
                    logger.critical("unknown columns: " + str(row))
                    logger.critical("EXIT")
                    sys.exit(1)

                linecount += 1
                #print row
                continue

            measuredbytes = row[13]
            timestamp_micros = row[12]
            keyword = row[6]
            clientIP = row[2]
            clientPort = row[3]
            serverIP = row[4]
            try:
                assert clientIP[:len(client_subnetaddr)].strip() == client_subnetaddr.strip()
                assert conntype.strip() in inputfile
            except:
                logger.critical("conntype: " + str(conntype))
                logger.critical("inputfile" + str(inputfile))
                logger.critical(str(clientIP[:len(client_subnetaddr)]) + "!=" + str(client_subnetaddr))
                logger.critical("Exit")
                sys.exit(0)  

            linecount += 1

            if  (segment == "edge" and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (segment == "cloud" and serverIP[:len(cloudserver_subnetaddr)] == cloudserver_subnetaddr):
                if clientIP not in ret:
                    ret[clientIP] = []

                bandwidthkbps = float(measuredbytes)
                bandwidthMbps = bandwidthkbps / 1000
                date = datetime.datetime.fromtimestamp(float(timestamp_micros) / 1000000.0)

                if evaluate_fragmentquality:
                    ret[clientIP].append({"bandwidthMbps": bandwidthMbps, "clientPort": clientPort, "timestamp": date, "fragmentquality":row[14]})
                else:
                    ret[clientIP].append({"bandwidthMbps": bandwidthMbps, "clientPort": clientPort, "timestamp": date})

        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")
    return ret
def readbandwidthvalues_mim_timeplot(config_parser, section, inputfile, segment, conntype, logger):
    assert "LEGACY" not in inputfile
    assert "SORTED" in inputfile
    assert "mim" in inputfile
    client_subnetaddr, edgeserver_subnetaddr, cloudserver_subnetaddr = _get_subnetaddresses(
                                        config_parser=config_parser, section=section, conntype=conntype, 
                                        logger=logger)
    logger.debug("inputfile = " + str(inputfile))
    logger.debug("connectiontype = " + str(conntype))
    logger.debug("segment = " + segment)
    logger.debug("client_subnetaddr = " + str(client_subnetaddr))
    logger.debug("edgeserver_subnetaddr = " + str(edgeserver_subnetaddr))
    logger.debug("cloudserver_subnetaddr = " + str(cloudserver_subnetaddr))
    
    ret = OrderedDict()
    
    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0
        for row in csvreader:
            #line 0 contains the query
            #line #1 contains its arguments
            if linecount == 0 or linecount == 1:
                linecount += 1
                continue
            #line #2 contains the name of each column
            #       ID,Timestamp,ClientIP,ClientPort,ServerIP,ServerPort,Keyword,Direction,Protocol,Mode,Type,
            #       ID,Timestamp,Bytes
            if linecount == 2:
                try:
                    assert row[13] == "Bytes"
                    assert row[6] == "Keyword"
                    assert row[2] == "ClientIP"
                    assert row[3] == "ClientPort"
                    assert row[4] == "ServerIP"
                except Exception as e:
                    logger.critical("unknown columns: " + str(row))
                    logger.critical("EXIT")
                    sys.exit(1)

                linecount += 1
                #print row
                continue

            #measuredbytes = row[13]
            timestamp_micros = row[12]
            #keyword = row[6]
            clientIP = row[2]
            #clientPort = row[3]
            serverIP = row[4]
            try:
                assert clientIP[:len(client_subnetaddr)].strip() == client_subnetaddr.strip()
                assert conntype.strip() in inputfile
            except:
                logger.critical("conntype: " + str(conntype))
                logger.critical("inputfile" + str(inputfile))
                logger.critical(str(clientIP[:len(client_subnetaddr)]) + "!=" + str(client_subnetaddr))
                logger.critical("Exit")
                sys.exit(0)  

            linecount += 1

            if  (segment == "edge" and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (segment == "cloud" and serverIP[:len(cloudserver_subnetaddr)] == cloudserver_subnetaddr):
                if clientIP not in ret:
                    ret[clientIP] = []

                date = datetime.datetime.fromtimestamp(float(timestamp_micros) / 1000000.0)

                ret[clientIP].append({"timestamp": date})
        
        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")
    return ret
def readbandwidthvalues_mim_timeplot_usingfixbuckets(config_parser, section, inputfile, segment, conntype, 
                                                     logger, bucketsize_microsec):
    assert "LEGACY" not in inputfile
    assert "SORTED" in inputfile
    assert "mim" in inputfile

    client_subnetaddr, edgeserver_subnetaddr, cloudserver_subnetaddr = _get_subnetaddresses(
                                        config_parser=config_parser, section=section, conntype=conntype, 
                                        logger=logger)
    logger.debug("inputfile = " + str(inputfile))
    logger.debug("connectiontype = " + str(conntype))
    logger.debug("segment = " + segment)
    logger.debug("client_subnetaddr = " + str(client_subnetaddr))
    logger.debug("edgeserver_subnetaddr = " + str(edgeserver_subnetaddr))
    logger.debug("cloudserver_subnetaddr = " + str(cloudserver_subnetaddr))

    print (segment)
    print ("bucketsize_microsec " + str(bucketsize_microsec))
    
    ret = OrderedDict()
    lastclientIP = ""
    
    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")
        linecount = 0
        for row in csvreader:
            #line 0 contains the query
            #line #1 contains its arguments
            if linecount == 0 or linecount == 1:
                linecount += 1
                continue
            #line #2 contains the name of each column
            #       ID,Timestamp,ClientIP,ClientPort,ServerIP,ServerPort,Keyword,Direction,Protocol,Mode,Type,
            #       ID,Timestamp,Bytes
            if linecount == 2:
                try:
                    assert row[13] == "Bytes"
                    assert row[6] == "Keyword"
                    assert row[2] == "ClientIP"
                    assert row[3] == "ClientPort"
                    assert row[4] == "ServerIP"
                except Exception as e:
                    logger.critical("unknown columns: " + str(row))
                    logger.critical("EXIT")
                    sys.exit(1)

                linecount += 1
                #print row
                continue

            clientIP = row[2]
            serverIP = row[4]
            byte = float(row[13])
            currenttimestamp_micros = float(row[12]) 
            try:
                assert clientIP[:len(client_subnetaddr)].strip() == client_subnetaddr.strip()
                assert conntype.strip() in inputfile
            except:
                logger.critical("conntype: " + str(conntype))
                logger.critical("inputfile" + str(inputfile))
                logger.critical(str(clientIP[:len(client_subnetaddr)]) + "!=" + str(client_subnetaddr))
                logger.critical("Exit")
                sys.exit(0)  

            linecount += 1

            if  (segment == "edge" and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (segment == "cloud" and serverIP[:len(cloudserver_subnetaddr)] == cloudserver_subnetaddr):
                if clientIP not in ret:
                    #new clientIP
                    ret[clientIP] = []
                    if len(ret) == 1:
                        #this is the first row containing results for the first target server
                        lastclientIP = clientIP

                        currentBytes = byte
                        bucket_starttime_microsec = currenttimestamp_micros
                        bucket_endtime_microsec = bucket_starttime_microsec + bucketsize_microsec         
                    else:
                        #switch to a new client with a different IP     
                        
                        #add the results of the previous client
                        if currentBytes == 0:
                            Mbps = 0
                        else:
                            bucketsize_sec = 1.0 * bucketsize_microsec / 1000000
                            bps = (1.0 * currentBytes * 8) / bucketsize_sec
                            Mbps = bps/1000000 
                          
                          
                        time_datetime = datetime.datetime.fromtimestamp(float(bucket_starttime_microsec + (bucketsize_microsec/2)) / 1000000.0)
                        ret[lastclientIP].append({"bandwidthMbps": Mbps, "timestamp": time_datetime})

                        lastclientIP = clientIP
                        currentByte = 0.0
                        bucket_starttime_microsec = currenttimestamp_micros
                        bucket_endtime_microsec = bucket_starttime_microsec + bucketsize_microsec 

                    continue
                elif lastclientIP == clientIP:
                    if currenttimestamp_micros < bucket_endtime_microsec:
                        #packet received within the bucketsize_microsec interval
                        currentBytes += byte
                    else:
                        #packet received within the next bucketsize_microsec interval
                        if currentBytes == 0:
                            Mbps = 0
                        else:
                            bucketsize_sec = 1.0 * bucketsize_microsec / 1000000
                            bps = (1.0 * currentBytes * 8) / bucketsize_sec
                            Mbps = bps/1000000

                        
                        time_datetime = datetime.datetime.fromtimestamp(float(bucket_starttime_microsec + (bucketsize_microsec/2)) / 1000000.0)
                        ret[clientIP].append({"bandwidthMbps": Mbps, "timestamp": time_datetime})

                        currentBytes = byte
                        bucket_starttime_microsec = bucket_endtime_microsec
                        bucket_endtime_microsec = bucket_starttime_microsec + bucketsize_microsec
                    
                    continue
                else:
                    print("NON DOVREMMO MAI ARRIVARCI")
                    assert False

        #add the last results
                
        if currentBytes == 0:
            Mbps = 0
        else:
            bucketsize_sec = 1.0 * bucketsize_microsec / 1000000
            bps = (1.0 * currentBytes * 8) / bucketsize_sec
            Mbps = bps/1000000 
            
            
        time_datetime = datetime.datetime.fromtimestamp(float(bucket_starttime_microsec + (bucketsize_microsec/2)) / 1000000.0)
        ret[lastclientIP].append({"bandwidthMbps": Mbps, "timestamp": time_datetime})

        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")
    return ret




def readlatencyvalues_noisemim(config_parser, section, inputfile, connectiontype, segment, noise):
    assert "SORTED_LEGACY" in inputfile
    assert "mim" in inputfile
    
    ret = []
    
    client_subnetaddr = config_parser.get(section, "client_subnetaddr_" + connectiontype)
    edgeserver_subnetaddr = config_parser.get(section, "edgeserver_subnetaddr_" + connectiontype)
    remoteserver_subnetaddr = config_parser.get(section, "remoteserver_subnetaddr_" + connectiontype)


    with open (inputfile, "r") as csvinput:
        csvreader = csv.reader(csvinput, delimiter=",")

        linecount = 0
        for row in csvreader:
            if linecount == 0 or linecount == 1:
                linecount += 1
                continue

            if linecount == 2:
                try:
                    assert row[13] == "latency"
                    assert row[6] == "Keyword"
                    assert row[4] == "ServerIP"
                except:
                    print (row)
                    sys.exit(1)

                linecount += 1
                #print row
                continue            
            
            linecount += 1

            try:
                latency = float(row[13])
            except:
                print (inputfile)
                for iii in range(0, len(row)):
                    print(str(iii) + ": " + str(row[iii]) )
                

                sys.exit(0)
            serverIP = row[4]

            
            if  (segment == "edge" and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (segment == "remote" and serverIP[:len(remoteserver_subnetaddr)] == remoteserver_subnetaddr):

                #print str(latency/1000)
                if latency != 0:
                    
                    ret.append(latency/1000)
               

        #print ret
        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")
    
        
    return ret

