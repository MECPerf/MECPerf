
HEADER = '\033[95m'
OKBLUE = '\033[94m'
OKGREEN = '\033[92m'
WARNING = '\033[93m'
FAIL = '\033[91m'
ENDC = '\033[0m'
BOLD = '\033[1m'
UNDERLINE = '\033[4m'


INTERNAL_SERVER_ERROR = 500
NOT_IMPLEMENTED = 501
OK = 200

def build_bandwidth_query(json, keyword, likeKeyword, fromInterval, toInterval, command, direction):
    params = []
    query = "SELECT "
    
    if json == "True" or json == "true":
        query += " JSON_OBJECT ( "
        query += " 'TestNumber', TestNumber, "
        query += " 'ID', Test.ID,"
        query += " 'Timestamp', Timestamp, "
        query += " 'Direction', Direction, "
        query += " 'Command', Command, "
        query += " 'SenderIdentity', SenderIdentity,  "
        query += " 'ReceiverIdentity', ReceiverIdentity, "
        query += " 'SenderIPv4Address', SenderIPv4Address, "
        query += " 'ReceiverIPv4Address', ReceiverIPv4Address,"
        query += " 'Keyword', Keyword, "
        query += " 'PackSize', PackSize, " 
        query += " 'NumPack', NumPack, "
        query += " 'Bandwidth [Mbit/s]',  (1.0 * (SUM(kBytes * 1024 * 8))/(1.0 * SUM(1.0 * nanoTimes / 1000000000))) "
        query += " ) "
    else:
        json = str(False)
        query += "TestNumber, Test.ID, Timestamp, Direction, Command, SenderIdentity, ReceiverIdentity, "
        query += "SenderIPv4Address, ReceiverIPv4Address, Keyword, PackSize, NumPack "

    whereClause = False
    query += "FROM Test INNER JOIN BandwidthMeasure ON Test.ID = BandwidthMeasure.id "

    
    if (keyword != "None"):
        query += " where Keyword = %s"
        params.append(keyword)
        whereClause = True

    if (likeKeyword != "None"):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += " Keyword LIKE %s"

        likeKeyword = "%" + likeKeyword + "%"
        params.append(likeKeyword)

    if (fromInterval != "None"):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += " Timestamp > %s"
        params.append(fromInterval)

    if (toInterval != "None"):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += "Timestamp < %s"
        params.append(toInterval)
    
    if (command != "None"):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += "Command = %s"
        params.append(command)
    
    if (direction != "None"):
        if (whereClause == False):
            query += " where "
            whereClause = True
        else:
            query += " AND "

        query += " Direction = %s"
        params.append(direction)

    query += " GROUP BY Test.ID"



    return json, query, params



def read_last_test_number(mysql):
    query = "SELECT TestNumber FROM MECPerf.Test ORDER BY ID desc limit 1"
    cur = mysql.connection.cursor()
    cur.execute(query, [])
    query_res = cur.fetchone()
    if len(query_res) == 0:
        cur.close()
        return 1

    cur.close()
    return query_res[0] + 1
    
    

def insert_test_table(cur, actual_test_number, test_type, test_info):
    insert_Test_table_query_active = "INSERT INTO MECPerf.Test (TestNumber,Timestamp, Direction, Command, " +\
                                                        " SenderIdentity, ReceiverIdentity, " +\
                                                        " SenderIPv4Address, ReceiverIPv4Address, Keyword, " +\
                                                        " PackSize, NumPack) VALUES (%s, CURRENT_TIMESTAMP," +\
                                                        " %s, %s, %s, %s, %s, %s, %s, %s, %s)"
    insert_Test_table_query_passive = "INSERT INTO MECPerf.PassiveTest (Timestamp, ClientIP, ClientPort, "+\
                                                        "ServerIP, ServerPort, Keyword, Direction, "+\
                                                        "Protocol, Mode) VALUES (CURRENT_TIMESTAMP, "+\
                                                        " %s, %s, %s, %s, %s, %s, %s, %s)"

    if test_type == "active":
        cur.execute(insert_Test_table_query_active, [actual_test_number, test_info['Direction'], 
                    test_info['Command'], test_info['SenderIdentity'], test_info['ReceiverIdentity'], 
                    test_info['SenderIPv4Address'], test_info['ReceiverIPv4Address'], test_info['Keyword'], 
                    test_info['PackSize'], test_info['NumPack']])
    if test_type == "passive":
        cur.execute(insert_Test_table_query_passive, [ test_info['ClientIP'], test_info['ClientPort'], 
                    test_info['ServerIP'], test_info['ServerPort'], test_info['Keyword'], 
                    test_info['Direction'], test_info['Protocol'], test_info['Mode']])




def insert_bandwidth_table(cur, rowID, test_type, test_values):
    insert_BandwidthMeasure_table_active = "INSERT INTO MECPerf.BandwidthMeasure VALUES (%s, %s, %s, %s)"
    insert_BandwidthMeasure_table_passive = "INSERT INTO MECPerf.PassiveBandwidthMeasure VALUES (%s, %s, %s, %s)"

    if test_type == "active":
        for i in range(0, len(test_values)):
            args = [rowID, i + 1,  long(test_values[i]['nanoTimes']), long(test_values[i]['kBytes'])]
                    
            cur.execute(insert_BandwidthMeasure_table_active, args)

    if test_type == "passive":
        for i in range(0, len(test_values)):
            args = [rowID, test_values[i]['Timestamp'], long(test_values[i]['nanoTimes']), long(test_values[i]['Bytes'])]
           
            cur.execute(insert_BandwidthMeasure_table_passive, args)



def insert_latency_table(cur, actual_test_ID, test_values):
    insert_latency_query = "INSERT INTO MECPerf.RttMeasure (id, sub_id, latency) VALUES (%s, %s, %s)"

    for i in range(0, len(test_values)):
            args = [actual_test_ID, i + 1,  long(test_values[i]['latency'])]
            #print args
            cur.execute(insert_latency_query, args)



def update_bandwidth(test, mysql):
    if test.type == "active":
        return update_active_bandwidth(test, mysql)
    elif test.type == "passive":
        return update_passive_bandwidth(test, mysql)
    else:
        print "\n\n" + FAIL + "ERROR unknown test.type" + ENDC
        return 501

                



def update_latencies(test, mysql):
    if test.test.type == "active":
        return update_active_latencies(test, mysql)
    elif test.test.type == "passive":
        return update_passive_latencies(test, mysql) 
    else:
        print "\n\n" + FAIL + "ERROR unknown test.type" + ENDC    
        return NOT_IMPLEMENTED




def update_active_bandwidth(test, mysql):
    actual_test_number = read_last_test_number(mysql)
    
    mysql.connection.autocommit = False
    try:
        cur = mysql.connection.cursor()

        #store first segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_first_segment)
        insert_bandwidth_table(cur, cur.lastrowid, test.type, test.test_values_first_segment)

        #update second segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_second_segment)
        insert_bandwidth_table(cur, cur.lastrowid, test.type, test.test_values_second_segment)


        mysql.connection.commit()
        mysql.connection.autocommit=True
        cur.close()
    
    except mysql.connection.Error as e:
        print FAIL + "Failed to update the database."
        print e
        print "Execute a roll back" + ENDC
        mysql.connection.rollback()
        cur.close()

        return INTERNAL_SERVER_ERROR
    
    return OK



def update_active_latencies(test, mysql):
    
    actual_test_number = read_last_test_number(mysql)

    
    mysql.connection.autocommit = False
    try:
        cur = mysql.connection.cursor()

        #store first segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_first_segment)
        insert_latency_table(cur, cur.lastrowid, test.test_values_first_segment)

        #update second segment measures
        insert_test_table(cur, actual_test_number, test.type, test.test_info_second_segment)
        insert_latency_table(cur, cur.lastrowid, test.test_values_second_segment)


        mysql.connection.commit()
        mysql.connection.autocommit=True
        cur.close()
    
    except mysql.connection.Error as e:
        print "Failed to update the database."
        print e
        print "Execute a roll back"
        mysql.connection.rollback()
        cur.close()
        return INTERNAL_SERVER_ERROR

    return OK



def update_passive_bandwidth(test, mysql):
    print test.info
    print test.values

    mysql.connection.autocommit = False
    try:
        cur = mysql.connection.cursor()

        insert_test_table(cur, -1, test.type, test.info)
        insert_bandwidth_table(cur, cur.lastrowid, test.type, test.values)


        mysql.connection.commit()
        mysql.connection.autocommit=True
        cur.close()
    
    except mysql.connection.Error as e:
        print FAIL + "Failed to update the database."
        print e
        print "Execute a roll back" + ENDC
        mysql.connection.rollback()
        cur.close()

        return INTERNAL_SERVER_ERROR
    
    return OK


def update_passive_latencies(test, mysql):
    return NOT_IMPLEMENTED







     