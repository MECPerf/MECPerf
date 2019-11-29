from flask import Flask, request
from flask_mysqldb import MySQL

import json


def create_app():
    app = Flask(__name__)


    #configure MySQL connection
    app.config['MYSQL_HOST'] = '131.114.73.3'
    app.config['MYSQL_PORT'] = 3306
    app.config['MYSQL_USER'] = 'MECPerf'
    app.config['MYSQL_PASSWORD'] = 'password'
    app.config['MYSQL_DB'] = 'MECPerf'
    app.config['MYSQL_DATABASE_CHARSET'] = 'latin1'
    mysql = MySQL(app)



    @app.route('/')
    def index():
        string = '<List of commands:<br>'
    
        string += '<a href="/bandwidth">bandwidth</a> <br>'

        #mobile commands
        string += '<a href="/get_data_list">get_data_list</a> optional: keyword(default "", json(default False) <br>'
        string += '<a href="/get_RTT_data">get_RTT_data</a> required: id, sender<br>'
        return string
        


    @app.route('/bandwidth', methods=['GET'])
    def getBandwidth():
        result = ""

        try:
            keyword = str(request.args.get('keyword'))
        except KeyError:
            keyword = ""
        try:
            json = str(request.args.get('json'))
        except KeyError:
            json = "False"


        json, query = build_bandwidth_query(json, keyword)
        cur = mysql.connection.cursor()
        cur.execute(query, [keyword])
        queryRes = cur.fetchall()
        cur.close()
        
        result += str(query) + "<br>"
        result += "json output = " + json + "<br>"
        result += "keyword \"" + keyword + "\"<br><br>"
        for row in queryRes:
            result += str(row).replace("(u'", "").replace("',)", "")
        return result
    


    def build_bandwidth_query(json, keyword):
        query = "SELECT "
        
        if json == "True" or json == "true":
            query += "JSON_ARRAYAGG(JSON_OBJECT ('TestNumber', TestNumber, 'ID', ID, 'Timestamp', Timestamp, "
            query += "'Direction', Direction, 'Commnand', Command, 'SenderIdentity', SenderIdentity, "
            query += "'ReceiverIdentity', ReceiverIdentity, 'SenderIPv4Address', SenderIPv4Address, "
            query += "'ReceiverIPv4Address', ReceiverIPv4Address, 'Keyword', Keyword, 'PackSize', PackSize, " 
            query += "'NumPack', NumPack)) "
        else:
            json = str(False)
            query += "TestNumber, ID, Timestamp, Direction, Command, SenderIdentity, ReceiverIdentity, "
            query += "SenderIPv4Address, ReceiverIPv4Address, Keyword, PackSize, NumPack "

        query += "FROM Test where Keyword = %s"

        return json, query





    ##################################################### MOBILE QUERIES ################################
    @app.route('/get_data_list', methods=['GET'])
    def get_data_list():
        result = ""

        query = "SELECT JSON_OBJECT('Timestamp', DATE_FORMAT(Timestamp, '%Y-%m-%d %T'), "
        query += "'ID', ID, "
        query += "'Command', Command, "
        query += "'Keyword', Keyword, "
        query += "'SenderIdentity', SenderIdentity, "
        query += "'ReceiverIdentity', ReceiverIdentity "
        query += " ) FROM MECPerf.Test "
        query += " GROUP BY ID, Timestamp, Command, Keyword "
        query += " ORDER BY (Timestamp) DESC "


        cur = mysql.connection.cursor()
        cur.execute(query)
        queryRes = cur.fetchall()
        cur.close()

        i = 0
        for row in queryRes:
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


    return app




if __name__ == "__main__":
    app = create_app()
    app.run(host="131.114.73.3", port = 5001, debug=True)
