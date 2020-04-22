import datetime
import requests
import ConfigParser
import json
import colors
import copy
import sys
import time

from Measure import Measure
from bandwidth_plot import simplebandwidth_lineplot, bandwidth_grouped, bandwidth_raw




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
        print r.text
        sys.exit(1)



def create_request (PARAMS, BASE_URL, measure, config_parser, starting_time_intervals_list, duration_m, dates_list, clientT_observerT = None, clientT_observerR = None):
    

    if starting_time_intervals_list != None and duration_m != None:
        clientT_observerT = []
        clientT_observerR = []
        observerT_remoteR = []
        observerR_remoteR = []

        for day in dates_list:
            #active measures
            for i in range (0, len(starting_time_intervals_list)):
                start_time = day.strip() + "-" + starting_time_intervals_list[i].strip()
                start_time_d = datetime.datetime.strptime(start_time, '%Y-%m-%d-%H:%M:%S')
                end_time_d = start_time_d + datetime.timedelta(minutes = int(duration_m))
                end_time = end_time_d.strftime('%Y-%m-%d-%H:%M:%S')

                #print start_time
                #print end_time
                PARAMS["from"] = start_time
                PARAMS["to"] =  end_time
    
                jsonData = send_request(PARAMS, BASE_URL + measure)

                if jsonData != None:
                    parse_activebandwidthreply(jsonData, clientT_observerT, clientT_observerR, observerT_remoteR, observerR_remoteR) 

        
        return clientT_observerT, clientT_observerR, observerT_remoteR, observerR_remoteR
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
        labels.append("Access -> MEC")
        labels.append("Access -> Cloud")
        labels.append("MEC -> Cloud")
        labels.append("Observer (unipi) -> Remote(unipi)")
    elif direction == "Downstream":
        labels.append("MEC -> Access")
        labels.append("Cloud -> Access")
        labels.append("Cloud -> Edge")
        labels.append("Remote(unipi) -> Observer (unipi)")

    #print PARAMS

    starting_time_intervals = config_parser.get("active_experiment_params", "starting_time_intervals")
    duration_m = config_parser.get("active_experiment_params", "duration_m")
    dates = config_parser.get("active_experiment_params", "dates")
    dates_list = dates.split(",")
    starting_time_intervals_list = starting_time_intervals.split(",")

    #PARAMS["group_by"] = "false"
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
            