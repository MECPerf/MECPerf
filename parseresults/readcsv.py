import csv
import sys



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





def readbandwidthvalues_self(config_parser, inputfile, edgeserver, conntype):
    ret = []

    if conntype == "wifi":
        client_subnetaddr = config_parser.get('experiment_conf', "client_subnetaddr_wifi")
        edgeserver_subnetaddr = config_parser.get('experiment_conf', "edgeserver_subnetaddr_wifi")
        remoteserver_subnetaddr = config_parser.get('experiment_conf', "remoteserver_subnetaddr_wifi")
    elif conntype == "lte":
        client_subnetaddr = config_parser.get('experiment_conf', "client_subnetaddr_lte")
        edgeserver_subnetaddr = config_parser.get('experiment_conf', "edgeserver_subnetaddr_lte")
        remoteserver_subnetaddr = config_parser.get('experiment_conf', "remoteserver_subnetaddr_lte")
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



def readbandwidthvalues_mim(config_parser, inputfile, connectiontype, segment):
    ret = []
    last_testID = ""
    
    if connectiontype == "wifi":
        client_subnetaddr = config_parser.get('experiment_conf', "client_subnetaddr_wifi")
        edgeserver_subnetaddr = config_parser.get('experiment_conf', "edgeserver_subnetaddr_wifi")
        remoteserver_subnetaddr = config_parser.get('experiment_conf', "remoteserver_subnetaddr_wifi")
    elif connectiontype == "lte":
        client_subnetaddr = config_parser.get('experiment_conf', "client_subnetaddr_lte")
        edgeserver_subnetaddr = config_parser.get('experiment_conf', "edgeserver_subnetaddr_lte")
        remoteserver_subnetaddr = config_parser.get('experiment_conf', "remoteserver_subnetaddr_lte")
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
                    assert row[12] == "Timestamp" # in microsec
                    assert row[6] == "Keyword"
                    assert row[2] == "ClientIP"
                    assert row[4] == "ServerIP"
                    assert row[3] == "ClientPort"
                    assert row[5] == "ServerPort"
                except:
                    print (row)
                    sys.exit(1)

                linecount += 1
                #print row
                continue            
            
            linecount += 1

            byte = float(row[13])
            timestamp_micros = float(row[12])  #from microsecons to seconds
            #bps = (byte * 8) / timestamp_s
            #Mbps = bps / 1000000

            clientIP = row[2]
            serverIP = row[4]
            clientPort = row[3]
            serverPort = row[5]
            
            
            if  (segment == "edge" and row[4][:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (segment == "remote" and row[4][:len(remoteserver_subnetaddr)] == remoteserver_subnetaddr):

                currentTestID = clientIP + "-" + clientPort + "-" + serverIP + "-" + serverPort
            

                if last_testID == "":
                    #this is the first row
                    last_testID = currentTestID
                    t0 = timestamp_micros
                    packets_bandwidth = []
                    currentByte = 0.0
                    currentTimestamp_micros = 0.0
                
                elif last_testID == currentTestID:
                    #same test
                    if byte > 0:
                        currentByte += byte
                        currentTimestamp_micros += timestamp_micros - t0

                        if currentTimestamp_micros >= 1000000: #more than one sec
                            currentTimestamp_s = currentTimestamp_micros /1000000
                            bps = (currentByte * 8) / currentTimestamp_s
                            Mbps = bps / 1000000

                            ret.append(Mbps)

                            currentByte = 0.0
                            currentTimestamp_micros = 0.0
                    if timestamp_micros !=  t0:
                        t0 = timestamp_micros
                else:
                    #newtest
                    if currentByte > 0:
                        currentTimestamp_s = currentTimestamp_micros /1000000
                        bps = (currentByte * 8) / currentTimestamp_s
                        Mbps = bps / 1000000

                        ret.append(Mbps)

    

                    last_testID = currentTestID
                    t0 = timestamp_micros
                    packets_bandwidth = []
                    currentByte = 0.0
                    currentTimestamp_micros = 0.0

        #print ret
        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")
    
        
    return ret


 
def readlatencyvalues_noisemim(config_parser, inputfile, connectiontype, segment, noise):
    ret = []
    
    client_subnetaddr = config_parser.get('experiment_conf', "client_subnetaddr_" + connectiontype)
    edgeserver_subnetaddr = config_parser.get('experiment_conf', "edgeserver_subnetaddr_" + connectiontype)
    remoteserver_subnetaddr = config_parser.get('experiment_conf', "remoteserver_subnetaddr_" + connectiontype)


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

            latency = float(row[13])
            serverIP = row[4]

            
            if  (segment == "edge" and serverIP[:len(edgeserver_subnetaddr)] == edgeserver_subnetaddr) or \
                (segment == "remote" and serverIP[:len(remoteserver_subnetaddr)] == remoteserver_subnetaddr):

                #print str(latency/1000)
                if latency != 0:
                    
                    ret.append(latency/1000)
               

        #print ret
        print ("read " + str(linecount) + " from " + inputfile + "(including headers)")
    
        
    return ret


 

def readvalues_activebandwidthlineplot(config_parser, command, direction, conn):
    noiselist = config_parser.get("experiment_conf", "noise").split(",")

    if conn == "wifi":
        dates_list = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    elif conn == "lte":
        dates_list = config_parser.get("experiment_conf", "dates_activelte").split(",")


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
                            print "unknown command"
                            sys.exit(0)

                        assert row[9] == "Keyword"
                        assert row[5] == "SenderIdentity"
                        assert row[6] == "ReceiverIdentity"
                        assert row[3] == "Direction"
                        assert row[2] == "Timestamp"
                        assert row[1] == "ID"
                    except:
                        print row
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
                        print "error"
                        print row
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
                        print "error"
                        print row
                        sys.exit(0)

            print "read " + str(linecount) + " from " + inputfile + "(including headers)"

    
    return clientNitos, clientUnipi, NitosUnipi

      