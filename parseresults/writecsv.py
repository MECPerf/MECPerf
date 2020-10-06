import ConfigParser
import mysql.connector
import os
import errno
import sys
import csv
import datetime

from mysql.connector import Error


 
def connect(config_parser):
    # Connect to MySQL database
    print ("Connect to MySQL database")
    print ("host: " + config_parser.get("sql_conf", "host"))
    print ("user: " + config_parser.get("sql_conf", "user"))
    print ("database: " + config_parser.get("sql_conf", "database"))
    print ("password: " + config_parser.get("sql_conf", "password"))
    print ("\n\n")
        
    try:
        mydb = mysql.connector.connect(host = config_parser.get("sql_conf", "host"), 
                                      database = config_parser.get("sql_conf", "database"), 
                                      user = config_parser.get("sql_conf", "user"),
                                      password = config_parser.get("sql_conf", "password"))
        if mydb.is_connected():
            print('Connected')
    except Error as e:
        print(e)
        sys.exit(0)

    return mydb
 
           

def createfolder(directoryname):
    try:
        os.makedirs(directoryname)
    except OSError as error:
        if error.errno != errno.EEXIST:
            print (error)
            sys.exit(0)


 
def createcsv_passive(config_parser, mydb, typeofmeasure, mode, direction, connectiontype):
    cursor = mydb.cursor()
    
    fromtime = config_parser.get("experiment_conf", "from_passive")
    totime = config_parser.get("experiment_conf", "to_passive")
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")


    basefilename = mode + "-" + typeofmeasure + "-" + direction + "-" + connectiontype
    
    basequery = "SELECT PassiveTest.ID, PassiveTest.Timestamp, ClientIP,  ClientPort, ServerIP, ServerPort,  "
    basequery += "Keyword, Direction, Protocol, Mode, Type, "

    if typeofmeasure == "bandwidth":
        basequery += "PassiveBandwidthMeasure.ID, PassiveBandwidthMeasure.Timestamp, Bytes "
        basequery += "FROM PassiveTest INNER JOIN PassiveBandwidthMeasure ON PassiveTest.ID = PassiveBandwidthMeasure.id "
    elif typeofmeasure == "latency":
        basequery += "PassiveRttMeasure.ID, PassiveRttMeasure.Timestamp, latency "
        basequery += "FROM PassiveTest INNER JOIN PassiveRttMeasure ON PassiveTest.ID = PassiveRttMeasure.id "
    else:
        print ("unknown type of measure " + str(typeofmeasure))
        sys.exit(1)    
        

    basequery += "WHERE Mode = %s AND Keyword LIKE \"%passive%\" AND PassiveTest.Timestamp > %s "
    basequery += "AND PassiveTest.Timestamp < %s AND Direction = %s  AND Keyword LIKE %s  AND Keyword LIKE %s "
    basequery += "AND Type = %s "

    if connectiontype == "wifi":
        basequery += "AND Keyword NOT LIKE \"%lte%\""
    elif connectiontype == "lte":
        basequery += "AND Keyword LIKE \"%lte%\""
    else:
        print ("unknown connection type")
        sys.exit(0)

    query1client = basequery + "AND (Keyword NOT LIKE \"%client%\" OR Keyword LIKE \"%1client%\")"
    querymultipleclients = basequery + "AND Keyword LIKE %s "  


    for dashfile in dashfileslist:
        for clientnumber in clientnumberlist:
            for noise in noiselist:

                filename = basefilename + "-" + clientnumber + "clients-" + dashfile + "-noise" + noise + \
                           "_" + fromtime + "-" + totime + ".csv"
                if clientnumber != "1":
                    query = querymultipleclients
                    params = [mode, fromtime, totime, direction, "%" + dashfile + "%", 
                             "%noise" + noise + "%", typeofmeasure, "%" + clientnumber + "client%"]
                elif clientnumber == "1":
                    query = query1client
                    params = [mode, fromtime, totime, direction, "%" + dashfile + "%", 
                              "%noise" + noise + "%", typeofmeasure]
                            

                with open("csv/passive/" + filename, "w+") as outputfile:
                    writer = csv.writer(outputfile)
                    writer.writerow([query])
                    writer.writerow(params)
                    
                    print (query)
                    cursor.execute(query, params)
                    data = cursor.fetchall()
                    columns = []
                    for column in cursor.description:
                        columns.append(column[0])
                    writer.writerow(columns)
                    print (len(data))
                    for row in data:
                        writer.writerow(row)

                    print(filename + '  \t(Total Rows = ' + str(cursor.rowcount) +")")


    cursor.close()



def createcsv_active(config_parser, mydb, command, direction, conn):
    cursor = mydb.cursor()
    
    if conn == "wifi":
        dates_list = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    elif conn == "lte":
        dates_list = config_parser.get("experiment_conf", "dates_activelte").split(",")

    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    intervalslist_t0 = config_parser.get("experiment_conf", "starting_time_intervals").split(",")
    duration_m = config_parser.get("experiment_conf", "duration_m")
    
    basefilename = command + "-" + direction + "-" + conn
    
    query = "SELECT TestNumber, Test.ID, Timestamp, Direction, Command, SenderIdentity, ReceiverIdentity, "
    query += "SenderIPv4Address, ReceiverIPv4Address, Keyword, PackSize, NumPack,  "
    
    if command == "TCPBandwidth" or command == "UDPBandwidth":
        query += "kBytes * 8 as \"Kbit\", nanoTimes "
        query += "FROM Test INNER JOIN BandwidthMeasure ON Test.ID = BandwidthMeasure.id "

        print (query)
    elif command == "TCPRTT" or command == "UDPRTT":
        query += "latency "
        query += "FROM Test INNER JOIN RttMeasure ON Test.ID = RttMeasure.id "
    else:
        print ("unknown type of measure " + str(command))
        sys.exit(1)    

    if conn== "wifi":
        query += "WHERE Keyword NOT LIKE \"%LTE%\" "
    elif conn == "lte":
        query += "WHERE Keyword LIKE \"%LTE%\" "

    query += "AND Keyword LIKE \"%experiment_active_%\" AND Keyword LIKE %s AND Timestamp > %s "
    query += "AND Timestamp < %s AND command = %s AND Direction = %s order by Test.ID"


    for noise in noiselist:
        filename = basefilename + "-noise" + noise + "_" + dates_list[0].strip() + "-" + dates_list[-1].strip() + ".csv"

        with open("csv/active/" + filename, "w+") as outputfile:
            writer =csv.writer(outputfile)
            writer.writerow([query])
            writer.writerow(["%noise" + noise + "%, " + dates_list[0].strip() + "-" + dates_list[-1].strip() +\
                            "[   " + str(intervalslist_t0) + "], " + command + ", " + direction])

            first = True
            numberofrows = 0

            for day in dates_list:
                #active measures
                for i in range (0, len(intervalslist_t0)):
                    start_time = day.strip() + "-" + intervalslist_t0[i].strip()
                    start_time_d = datetime.datetime.strptime(start_time, '%Y-%m-%d-%H:%M:%S')
                    end_time_d = start_time_d + datetime.timedelta(minutes = int(duration_m))
                    end_time = end_time_d.strftime('%Y-%m-%d-%H:%M:%S')


                    params = ["%noise" + noise + "%", start_time_d, end_time_d, command, direction]                                  
                    cursor.execute(query, params)
                    data = cursor.fetchall()
                    columns = []
                    if first:
                        for column in cursor.description:
                            columns.append(column[0])
                        writer.writerow(columns)
                        first = False
                    for row in data:
                        writer.writerow(row)
                    
                    numberofrows += cursor.rowcount

            print(filename + '  \t(Total Rows = ' + str(numberofrows) +")")

    cursor.close()



if __name__ == '__main__':
    #read configuration file
    config_parser = ConfigParser.RawConfigParser()
    config_parser.read("experiments.conf")
    
    mydb = connect(config_parser)
    createfolder("csv/passive")
    createfolder("csv/active")

    '''
    #active measures (wifi)
    createcsv_active(config_parser, mydb, "TCPRTT", "Upstream", "wifi")
    createcsv_active(config_parser, mydb, "TCPRTT", "Downstream", "wifi")
    createcsv_active(config_parser, mydb, "TCPBandwidth", "Upstream", "wifi")
    createcsv_active(config_parser, mydb, "TCPBandwidth", "Downstream", "wifi")
    createcsv_active(config_parser, mydb, "UDPRTT", "Upstream", "wifi")
    createcsv_active(config_parser, mydb, "UDPRTT", "Downstream", "wifi")
    createcsv_active(config_parser, mydb, "UDPBandwidth", "Upstream", "wifi")
    createcsv_active(config_parser, mydb, "UDPBandwidth", "Downstream", "wifi")
    #active measurements (lte)
    createcsv_active(config_parser, mydb, "TCPRTT", "Upstream", "lte")
    createcsv_active(config_parser, mydb, "TCPRTT", "Downstream", "lte")
    createcsv_active(config_parser, mydb, "TCPBandwidth", "Upstream", "lte")
    createcsv_active(config_parser, mydb, "TCPBandwidth", "Downstream", "lte")
    createcsv_active(config_parser, mydb, "UDPRTT", "Upstream", "lte")
    createcsv_active(config_parser, mydb, "UDPRTT", "Downstream", "lte")
    createcsv_active(config_parser, mydb, "UDPBandwidth", "Upstream", "lte")
    createcsv_active(config_parser, mydb, "UDPBandwidth", "Downstream", "lte")
    

   

    #passive measurements (self)
    createcsv_passive(config_parser, mydb, "bandwidth", "self", "downlink", "wifi")
    createcsv_passive(config_parser, mydb, "bandwidth", "self", "downlink", "lte")

    '''
    
    #passive measurements (mim)
    #createcsv_passive(config_parser, mydb, "bandwidth", "mim", "downlink", "wifi")
    createcsv_passive(config_parser, mydb, "bandwidth", "mim", "uplink", "wifi")
    #createcsv_passive(config_parser, mydb, "bandwidth", "mim", "downlink", "lte")
    createcsv_passive(config_parser, mydb, "bandwidth", "mim", "uplink", "lte")
    '''
    createcsv_passive(config_parser, mydb, "latency", "mim", "downlink", "wifi")
    createcsv_passive(config_parser, mydb, "latency", "mim", "downlink", "lte")
    createcsv_passive(config_parser, mydb, "latency", "mim", "uplink", "wifi")
    createcsv_passive(config_parser, mydb, "latency", "mim", "uplink", "lte")

    '''   

    mydb.close()