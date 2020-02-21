import datetime
import requests
import ConfigParser
import json
import colors
import copy

from Measure import Measure
from bandwidth_plot import simplebandwidth_lineplot, bandwidth_grouped, bandwidth_raw



def parse_reply(jsonData, clientT_observerT, clientT_observerR, observerT_remoteR):
    bucket = ""
    labels= []  

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
            bucket = "None"
        
        assert bucket != ""


    if direction == "Upstream":
        labels.append("Client -> Observer (Nitos)")
        labels.append("Client -> Observer (unipi)")
        labels.append("Observer (Nitos) -> Remote(unipi)")
    elif direction == "Downstream":
        labels.append("Observer (Nitos) -> Client")
        labels.append("Observer (unipi) -> Client")
        labels.append("Remote(unipi) -> Observer (Nitos)")

    return (labels)
        


def send_request(request_params, url):
    r = requests.get(url = url, params = request_params) 
    print colors.GREEN + "[GET] " + str(url) + " with params " + str(request_params) +\
          colors.RESET

    print colors.FAIL + str (request_params) + colors.RESET

    #print r.text
    if  len(r.text) == 1 or "0 rows" in r.text:
        print "empty reply"
        return None
    
    jsonData = json.loads(r.text)
    print str(len(jsonData)) + "rows"

    return jsonData



def create_request (PARAMS, BASE_URL, config_parser, starting_time_intervals_list,
                    duration_m, dates_list):
    clientT_observerT = []
    clientT_observerR = []
    observerT_remoteR = []

    starting_time_intervals = config_parser.get("experiment_params", "starting_time_intervals")
    duration_m = config_parser.get("experiment_params", "duration_m")
    dates = config_parser.get("experiment_params", "dates")
    dates_list = dates.split(",")
    starting_time_intervals_list = starting_time_intervals.split(",")


    for day in dates_list:
        for i in range (0, len(starting_time_intervals_list)):
            start_time = day.strip() + "-" + starting_time_intervals_list[i].strip()
            start_time_d = datetime.datetime.strptime(start_time, '%Y-%m-%d-%H:%M:%S')
            end_time_d = start_time_d + datetime.timedelta(minutes = int(duration_m))
            end_time = end_time_d.strftime('%Y-%m-%d-%H:%M:%S')

            PARAMS["from"] = start_time
            PARAMS["to"] =  end_time
 
            jsonData = send_request(PARAMS, BASE_URL + "/get_measures/bandwidth")

            if jsonData != None:
                labels = parse_reply(jsonData, clientT_observerT, clientT_observerR, observerT_remoteR)

    return clientT_observerT, clientT_observerR, observerT_remoteR, labels
    

def analyze_activebandwidthmeasures(BASE_PARAMS, BASE_URL, direction, command, config_parser):  
    PARAMS = copy.deepcopy(BASE_PARAMS)
    PARAMS["command"] = command
    PARAMS["direction"] = direction

    print BASE_PARAMS

    starting_time_intervals = config_parser.get("experiment_params", "starting_time_intervals")
    duration_m = config_parser.get("experiment_params", "duration_m")
    dates = config_parser.get("experiment_params", "dates")
    dates_list = dates.split(",")
    starting_time_intervals_list = starting_time_intervals.split(",")

    '''
    clientT_observerT, clientT_observerR, observerT_remoteR, labels = create_request (PARAMS, BASE_URL, 
                                                                                config_parser, 
                                                                                starting_time_intervals_list, 
                                                                                duration_m, dates_list)
    '''
    title = str(PARAMS["command"]) + " - " + PARAMS["direction"]
    '''

    simplebandwidth_lineplot(clientT_observerT, clientT_observerR, observerT_remoteR, title, labels)
    bandwidth_grouped(starting_time_intervals_list, duration_m, clientT_observerT, clientT_observerR, 
                      observerT_remoteR, title, labels, dates_list)


    '''
    if PARAMS["command"] == "UDPBandwidth":
        PARAMS["group_by"] = "false"

        clientT_observerT_rawdata, clientT_observerR_rawdata, observerT_remoteR_rawdata, labels = create_request (PARAMS, BASE_URL, 
                                                                            config_parser, starting_time_intervals_list, duration_m,
                                                                            dates_list)

        bandwidth_raw(clientT_observerT_rawdata, clientT_observerR_rawdata, observerT_remoteR_rawdata, 
                        title)
            