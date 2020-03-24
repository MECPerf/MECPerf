import datetime
import requests
import ConfigParser
import json
import colors
import copy
import sys
import time

from Measure import Measure, PassiveMeasure
from bandwidth_plot import simplebandwidth_lineplot, passivebandwidth_lineplot, bandwidth_grouped, bandwidth_raw




def parse_activebandwidthreply(jsonData, clientT_observerT, clientT_observerR, observerT_remoteR, observerR_remoteR):
    bucket = ""

    for line in jsonData:
        if 'local' in line['Keyword']:
            isLocal = True
        else:
            isLocal= False

        direction = line["Direction"]
        receiver_identity = line["ReceiverIdentity"]
        sender_identity = line["SenderIdentity"]
        timestamp = line["Timestamp"]
        timestamp_d = datetime.datetime.strptime(timestamp.replace(".000000", ""), '%Y-%m-%d %H:%M:%S')

        bandwidthbps = float(line["Bandwidth [bit/s]"])
        bandwidthKbps = bandwidthbps / 1024
        bandwidthMbps = bandwidthKbps / 1024

        keyword = line['Keyword']
        measure = Measure(timestamp_d, bandwidthMbps, keyword.split("_")[3].replace("noise", "").strip())

        if ((direction == "Upstream" and sender_identity == "Client") or \
                (direction == "Downstream" and receiver_identity == "Client" )) and bool(isLocal)== True:
            clientT_observerT.append(measure)
            bucket = "clientT_observerT" 
        elif ((direction == "Upstream" and sender_identity == "Client") or \
                (direction == "Downstream" and receiver_identity == "Client" )) and bool(isLocal)== False: 
            clientT_observerR.append(measure)
            bucket = "clientT_observerR"    
        elif ((direction == "Upstream" and sender_identity == "Observer") or \
                (direction == "Downstream" and receiver_identity == "Observer" )) and bool(isLocal)== True: 
            observerT_remoteR.append(measure)
            bucket = "observerT_remoteR_"
        elif ((direction == "Upstream" and sender_identity == "Observer") or \
                (direction == "Downstream" and receiver_identity == "Observer" )) and bool(isLocal)== False: 
            observerR_remoteR.append(measure)
            bucket = "observerR_remoteR_"
        
        assert bucket != ""



def parse_passivebandwidthreply(jsonData, measure, clientT_observerT, clientT_observerR, config_parser, params):

    if "self" in measure:
        clientT_observerT, clientT_observerR = parse_selfmeasurements(jsonData, clientT_observerT, 
                                                                      clientT_observerR, config_parser, params)
    elif "mim"  in measure:
        clientT_observerT, clientT_observerR = parse_mimmeasurements(jsonData, clientT_observerT, 
                                                                     clientT_observerR, config_parser, params)
    else:
        print colors.FAIL + "unknown method " + str(measure)
        sys.exit(1)


    return clientT_observerT, clientT_observerR                                                       
    

        
def parse_selfmeasurements(jsonData, clientT_observerT, clientT_observerR, config_parser, PARAMS):
    filename = PARAMS["dashfilename"]
    numberofclients = PARAMS["clientnumber"]
    nitosclient_subnetaddr = config_parser.get('passive_experiment_params', "nitosclient_subnetaddr")
    nitosserver_subnetaddr = config_parser.get('passive_experiment_params', "nitosserver_subnetaddr")
    unipiserver_subnetaddr = config_parser.get('passive_experiment_params', "unipiserver_subnetaddr")

    for line in jsonData: 
        try:
            assert line['ClientIP'][:len(nitosclient_subnetaddr)] == nitosclient_subnetaddr
            assert line['ServerIP'][:len(nitosserver_subnetaddr)] == nitosserver_subnetaddr or \
                   line['ServerIP'][:len(unipiserver_subnetaddr)] == unipiserver_subnetaddr
            assert str(numberofclients) + "client" in line['Keyword']
        
        except:
            print line
            sys.exit(1)
            
        timestamp = line["Date"]
        timestamp_d = datetime.datetime.strptime(timestamp.replace(".000000", ""), '%Y-%m-%d %H:%M:%S')
        try:
            assert timestamp_d < datetime.datetime.strptime(PARAMS["to"], '%Y-%m-%d-%H:%M:%S')
        except:
            print timestamp_d
            sys.exit(1)

        bandwidthKbps = float(line["Kbps"])
        bandwidthMbps = bandwidthKbps / 1024

        #print line['Keyword'] + line["Date"]

        #extract cross traffic value and the timestamp from the keyword
        keywordlist = line['Keyword'].split('_')
        for element in keywordlist:
            if "noise" == element[:len("noise")]:
                noise = element.replace("noise", "").replace("M", "")
            if "2020-" == element[:len("2020-")]:
                startexperiment_time = element
        #print line["Keyword"] + " - " + noise + ", " + startexperiment_time
        measure = PassiveMeasure(timestamp_d, bandwidthMbps, noise, filename, numberofclients, startexperiment_time)
      

        if line['ServerIP'][:len(nitosserver_subnetaddr)] == nitosserver_subnetaddr:
            #the dash server is inside the testbed
            clientT_observerT.append(measure)
        elif line['ServerIP'][:len(unipiserver_subnetaddr)] == unipiserver_subnetaddr:
            #the unipi dash server
            clientT_observerR.append(measure)
        else:
            print colors.FAIL + "unknown server " + str(line['ServerIP'])
            sys.exit(1)

    return clientT_observerT, clientT_observerR
        


def parse_mimmeasurements(jsonData, clientT_observerT, clientT_observerR, config_parser, PARAMS):

    print colors.WARNING + "parse_mimmeasurements: TODO" + colors.RESET

    return clientT_observerT, clientT_observerR
    

    


def send_request(request_params, url):
    print colors.GREEN + "[GET] " + str(url) + " with params " + str(request_params) + colors.RESET
    r = requests.get(url = url, params = request_params) 

    #print r.text
    if  len(r.text) == 1 or "0 rows" in r.text:
        #print r.text
        print "empty reply"
        return None
    
    try:
        jsonData = json.loads(r.text)
        print str(len(jsonData)) + "rows"

        return jsonData
    except ValueError:
        #print r.text
        sys.exit(1)



def create_request (PARAMS, BASE_URL, measure, config_parser, starting_time_intervals_list, duration_m, dates_list):
    clientT_observerT = []
    clientT_observerR = []
    observerT_remoteR = []
    observerR_remoteR = []

    if starting_time_intervals_list != None and duration_m != None:
        for day in dates_list:
            #active measures
            for i in range (0, len(starting_time_intervals_list)):
                start_time = day.strip() + "-" + starting_time_intervals_list[i].strip()
                start_time_d = datetime.datetime.strptime(start_time, '%Y-%m-%d-%H:%M:%S')
                end_time_d = start_time_d + datetime.timedelta(minutes = int(duration_m))
                end_time = end_time_d.strftime('%Y-%m-%d-%H:%M:%S')

                print start_time
                print end_time
                PARAMS["from"] = start_time
                PARAMS["to"] =  end_time
    
                jsonData = send_request(PARAMS, BASE_URL + measure)

                if jsonData != None:
                    parse_activebandwidthreply(jsonData, clientT_observerT, clientT_observerR, observerT_remoteR, observerR_remoteR) 

        return clientT_observerT, clientT_observerR, observerT_remoteR, observerR_remoteR

    elif starting_time_intervals_list == None and duration_m == None:
        #passive measures
        PARAMS["from"] = config_parser.get("passive_experiment_params", "from")
        PARAMS["to"] = config_parser.get("passive_experiment_params", "to")

        PARAMS["limit"] = int(config_parser.get("passive_experiment_params", "sql_limit"))
        PARAMS["offset"] = 0
        while (True):
            jsonData = send_request(PARAMS, BASE_URL + measure)

            if jsonData == None:
                break

            if jsonData != None:
                clientT_observerT, clientT_observerR = parse_passivebandwidthreply(jsonData, measure, 
                                                                    clientT_observerT, clientT_observerR, 
                                                                    config_parser, PARAMS)
                print len(clientT_observerT)
                print len(clientT_observerR)

                print len(clientT_observerT) + len(clientT_observerR)
        
                if len(jsonData) == PARAMS["limit"]:
                    print "len(jsonData) == PARAMS[\"limit\"]"

                    PARAMS["offset"] += PARAMS["limit"]
                if len(jsonData) < PARAMS["limit"]:
                    print "break"
                    break


        return clientT_observerT, clientT_observerR

    else:
        print colors.fail + "WRONG PARAMETERS" + colors.RESET
        sys.exit(1)
        


def analyze_activebandwidthmeasures(BASE_URL, direction, command, config_parser):  
    labels= []  

    PARAMS = {}
    PARAMS["compact"] = True
    PARAMS["json"] = True
    PARAMS["likeKeyword"] ='experiment_active_'
    PARAMS["command"] = command
    PARAMS["direction"] = direction
    title = str(PARAMS["command"]) + " - " + PARAMS["direction"]

    if direction == "Upstream":
        labels.append("Client -> Observer (Nitos)")
        labels.append("Client -> Observer (unipi)")
        labels.append("Observer (Nitos) -> Remote(unipi)")
        labels.append("Observer (unipi) -> Remote(unipi)")
    elif direction == "Downstream":
        labels.append("Observer (Nitos) -> Client")
        labels.append("Observer (unipi) -> Client")
        labels.append("Remote(unipi) -> Observer (Nitos)")
        labels.append("Remote(unipi) -> Observer (unipi)")

    print PARAMS

    starting_time_intervals = config_parser.get("active_experiment_params", "starting_time_intervals")
    duration_m = config_parser.get("active_experiment_params", "duration_m")
    dates = config_parser.get("active_experiment_params", "dates")
    dates_list = dates.split(",")
    starting_time_intervals_list = starting_time_intervals.split(",")

    
    clientT_observerT, clientT_observerR, observerT_remoteR, observerR_remoteR = create_request (PARAMS, BASE_URL, "/get_active_measures/bandwidth",
                                                                                config_parser, 
                                                                                starting_time_intervals_list, 
                                                                                duration_m, dates_list)
    
    simplebandwidth_lineplot(clientT_observerT, clientT_observerR, observerT_remoteR, title, labels)
    bandwidth_grouped(starting_time_intervals_list, duration_m, clientT_observerT, clientT_observerR, 
                      observerT_remoteR, title, labels, dates_list, config_parser)
    
    if PARAMS["command"] == "UDPBandwidth":
        print "UDPBandwidth"
        PARAMS["group_by"] = "false"

        clientT_observerT_rawdata, clientT_observerR_rawdata, observerT_remoteR_rawdata, observerR_remoteR_rawdata = create_request (PARAMS, 
                                            BASE_URL, "/get_active_measures/bandwidth", config_parser, 
                                            starting_time_intervals_list, duration_m, dates_list)

        bandwidth_raw(clientT_observerT_rawdata, clientT_observerR_rawdata, observerT_remoteR_rawdata, 
                      observerR_remoteR_rawdata, title, config_parser)
            
            

def analyze_passivebandwidthmeasures(BASE_URL, direction, measuretype, protocol, config_parser):  
    labels= []  

    PARAMS = {}
    PARAMS["json"] = True
    PARAMS["compact"] = True
    PARAMS["likeKeyword"] ='_passive_'
    PARAMS["type"] = measuretype
    PARAMS["direction"] = direction
    PARAMS["protocol"] = protocol
    title = str(PARAMS["protocol"]) + str(PARAMS["type"]) + " - " + PARAMS["direction"]
    
    if direction == "uplink":
        labels.append("Client -> Observer (Nitos)")
        labels.append("Client -> Observer (unipi)")
    elif direction == "downlink":
        labels.append("Observer (Nitos) -> Client")
        labels.append("Observer (unipi) -> Client")

    dashfileslist = config_parser.get('passive_experiment_params', "dashfiles").split(",")
    clientnumberlist =config_parser.get('passive_experiment_params', "clientnumber").split(",")
    print dashfileslist
    print dashfileslist[0]
    print clientnumberlist


    for filename in dashfileslist:
        for clientnumber in clientnumberlist:

            PARAMS["dashfilename"] = filename.strip()
            PARAMS["clientnumber"] = clientnumber.strip()
            clientT_observerT_mim, clientT_observerR_mim = create_request (PARAMS, BASE_URL, 
                                                                        "/get_passive_mim_measures/bandwidth", 
                                                                        config_parser, None, None, None)
            clientT_observerT_self, clientT_observerR_self = create_request (PARAMS, BASE_URL, 
                                                                            "/get_passive_self_measures/bandwidth",
                                                                            config_parser, None, None, None)

    noiselist = config_parser.get("passive_experiment_params", "noise").split(",")

    for noise in noiselist:
        passivebandwidth_lineplot(clientT_observerT_mim, clientT_observerR_mim, title + "-mim_" + str(noise) + "M", labels, 
                                    dashfileslist, int(noise))
        passivebandwidth_lineplot(clientT_observerT_self, clientT_observerR_self, title + "-self_" + str(noise) + "M", labels, 
                                    dashfileslist, int(noise))







    '''
    
    bandwidth_grouped(starting_time_intervals_list, duration_m, clientT_observerT, clientT_observerR, 
                      observerT_remoteR, title, labels, dates_list, config_parser)
    
    if PARAMS["command"] == "UDPBandwidth":
        print "UDPBandwidth"
        PARAMS["group_by"] = "false"

        clientT_observerT_rawdata, clientT_observerR_rawdata, observerT_remoteR_rawdata, observerR_remoteR_rawdata = create_request (PARAMS, BASE_URL, 
                                                                            config_parser, starting_time_intervals_list, duration_m,
                                                                            dates_list)

        bandwidth_raw(clientT_observerT_rawdata, clientT_observerR_rawdata, observerT_remoteR_rawdata, observerR_remoteR_rawdata,
                        title, config_parser)
    '''   