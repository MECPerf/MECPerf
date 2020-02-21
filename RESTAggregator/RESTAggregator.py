from flask import Flask, request, Response
from flask_mysqldb import MySQL

from request_parser import parse_request
from query_handler import build_bandwidth_query, update_bandwidth, update_latencies
from API_utils import print_command_list

import json
import Test



def init_server(app):
    with open('opt.txt') as json_file:
        opt_json = json.load(json_file)

        print "REST server info:"
        for p in opt_json['RestServer']:
            print('address: ' + p['address'] + ":" + p['port'])

            host = p['address'] 
            port = p['port']
            print('\n')

        print "DB info:"
        for q in opt_json['Database']:
            print('address: ' + q['address'] + ":" + q['port'])
            print('DB name: ' + q['db_name'])
            print('user name: ' + q['user'])
            print('pw: ' + q['password'])
            print('charset: ' + q['charset'])
            print('\n')
        

            #configure MySQL connection
            app.config['MYSQL_HOST'] = q['address']
            app.config['MYSQL_PORT'] = int(q['port'])
            app.config['MYSQL_USER'] = q['user']
            app.config['MYSQL_PASSWORD'] = q['password']
            app.config['MYSQL_DB'] = q['db_name']
            app.config['MYSQL_DATABASE_CHARSET'] = q['charset']
        
        print ("\n\n")
        mysql = MySQL(app)

        return mysql



def create_app():
    app = Flask(__name__)
    mysql = init_server( app)


    @app.route('/')
    def index():
        return print_command_list()
        

   
    @app.route('/get_measures/bandwidth', methods=['GET'])
    def getBandwidth():
        result = ""

        compact, keyword, likeKeyword, json, fromInterval, toInterval, command, direction, group_by = parse_request(request)
        json, query, params = build_bandwidth_query(json, keyword, likeKeyword, fromInterval, toInterval, command, direction, group_by)

        cur = mysql.connection.cursor()
        cur.execute(query, params)
        queryRes = cur.fetchall()
        cur.close()

        
        print "asked compact " + str(compact)
        print "json output = " + str(json)
        print "keyword " + str(keyword)
        print "keyword LIKE " + str(likeKeyword)
        print "fromInterval " + str(fromInterval)
        print "toInterval " + str(toInterval)
        print "group_by " + str(group_by)

        if (compact != "True"):
            result += "<b>" + str(query) + "</b><br><br>"
            result += "json " + json + "<br>"
            result += "keyword \"" + keyword + "\"<br>"
            result += "likeKeyword " + likeKeyword + "<br>"
            result += "fromInterval \"" + fromInterval + "\"<br>"
            result += "toInterval \"" + toInterval + "\"<br>"
            result += "group_by \"" + str(group_by) + "\"<br><br>"
        if len(queryRes) != 0:
            result += "["
            for row in queryRes:
                print str(row)
                print json
                
                if json== 'False':
                    result += str(row) + " <br>"
                else:
                    result += str(row[0]).replace("}", "},")

            result = result[:-1] + "]"
        else:
            result += "<b>0 rows returned </b>"
            
        return result




    ############################################ POST QUERIES #########################################
    @app.route('/post_<test_type>_measures/insert_<measure_type>', methods=['POST'])
    def post_measures(test_type, measure_type):
        body = str(request.data)
        #print "req"
        #print request
        #print "req data"
        #print len( request.data)
        #print request.data

        request_body_list = json.loads(body)
        test = Test.Test(request_body_list, test_type)

        if measure_type == "bandwidth_measure":
            return_code = update_bandwidth(test, mysql)
        elif measure_type == "latency_measure":
            return_code = update_latencies(test, mysql)   

        print return_code
        return "", return_code
    


  
    ##################################################### MOBILE QUERIES ################################
    @app.route('/<type>/get_data_list', methods=['GET'])
    def get_data_list(type):
        if type != "get_measures" and type != "mobile":
            return "unrecognized request"
            
        result = ""

        query = "SELECT JSON_OBJECT('Timestamp', DATE_FORMAT(Timestamp, '%Y-%m-%d %T'), "
        query += "'ID', ID, "
        query += "'Command', Command, "
        query += "'Keyword', Keyword, "
        query += "'SenderIdentity', SenderIdentity, "
        query += "'ReceiverIdentity', ReceiverIdentity "
        query += " ) FROM MECPerf.Test "
        query += " ORDER BY (Timestamp) DESC "

        cur = mysql.connection.cursor()
        cur.execute(query)
        queryRes = cur.fetchall()
        cur.close()
    

        i = 0
        for row in queryRes:
            print row
            if i == 0:
                result += "["
                i += 1
            else:
                result += ", "

            result += str(row[0].encode('utf8'))
        result += "]"
        return result



    @app.route('/get_RTT_data', methods=['GET'])
    def get_RTT_data():
        result = ""

        try:
            testid = str(request.args.get('id'))
            sender = str(request.args.get('sender'))
        except KeyError:
            return ""

        query = "SELECT JSON_OBJECT('Test.ID', Test.ID, "
        query += "'Timestamp', Timestamp, "
        query += "'SenderIdentity', SenderIdentity, " 
        query += "'ReceiverIdentity', ReceiverIdentity, "
        query += "'Command', Command, "
        query += "'latency', latency, "
        query += "'Keyword', Keyword) "
        query += " FROM MECPerf.RttMeasure INNER JOIN MECPerf.Test ON(Test.ID=RttMeasure.id)"
        query += " WHERE Test.ID = %s AND SenderIdentity = %s "


        cur = mysql.connection.cursor()
        cur.execute(query, ([int(testid), sender]))


        
        queryRes = cur.fetchall()
        cur.close()

        i = 0
        for row in queryRes:
            if i == 0:
                result += "["
                i += 1
            else:
                result += ", "
            result += str(row[0].encode('utf-8'))
        result += "]"
        
        return result



    @app.route('/get_bandwidth_data', methods=['GET'])
    def get_bandwidth_data():
        result = ""

        try:
            testid = str(request.args.get('id'))
            sender = str(request.args.get('sender'))
        except KeyError:
            return ""

        query = "SELECT JSON_OBJECT( "
        query += "'SenderIdentity', SenderIdentity, " 
        query += "'ReceiverIdentity', ReceiverIdentity, "
        query += "'Command', Command, "
        query += "'nanoTimes', nanoTimes, "
        query += "'kBytes', kBytes, "
        query += "'Keyword', Keyword) "
        query += " FROM MECPerf.BandwidthMeasure INNER JOIN MECPerf.Test ON(Test.ID=BandwidthMeasure.id) "
        query += " WHERE Test.ID = %s AND SenderIdentity = %s "


        cur = mysql.connection.cursor()
        cur.execute(query, ([int(testid), sender]))


        queryRes = cur.fetchall()
        cur.close()

        i = 0
        for row in queryRes:
            if i == 0:
                result += "["
                i += 1
            else:
                result += ", "
            result += str(row[0].encode('utf-8'))
        result += "]"
        
        return result



    @app.route('/get_AVGbandwidth_data', methods=['GET'])
    def get_AVGbandwidth_data():
        result = ""

        try:
            testid = str(request.args.get('id'))
            sender = str(request.args.get('sender'))
        except KeyError:
            return ""

        query = "SELECT JSON_OBJECT( "
        query += "'SenderIdentity', SenderIdentity, " 
        query += "'ReceiverIdentity', ReceiverIdentity, "
        query += "'Command', Command, "
        query += "'Bandwidth', (1.0 * (SUM(kBytes) / (1.0 * SUM(nanoTimes)))*1000000000) , "
        query += "'Keyword', Keyword) "
        query += " FROM MECPerf.BandwidthMeasure INNER JOIN MECPerf.Test ON(Test.ID=BandwidthMeasure.id) "
        query += " WHERE Test.ID = %s AND SenderIdentity = %s "
        query += " GROUP BY Test.ID "


        cur = mysql.connection.cursor()
        cur.execute(query, ([int(testid), sender]))


        queryRes = cur.fetchall()
        cur.close()

        i = 0
        for row in queryRes:
            if i == 0:
                result += "["
                i += 1
            else:
                result += ", "
            result += str(row[0].encode('utf-8'))
        result += "]"
        
        return result


    return app



if __name__ == "__main__":
    app = create_app()
    with open('opt.txt') as json_file:
        opt_json = json.load(json_file)
    
    for i in opt_json["RestServer"]:
        app.run(host=i['address'], port = int(i['port']), debug=True)
