import matplotlib.pyplot as plt
import matplotlib
import numpy as np
import os
import sys
import errno

from datetime import datetime

from collections import OrderedDict
from matplotlib.backends.backend_pdf import PdfPages
from readcsv import readvalues_activelatencyboxplot, readvalues_activebandwidthboxplot, \
                    readvalues_activebandwidthlineplot, readbandwidthvalues_self, readbandwidthvalues_mim, \
                    readbandwidthvalues_mim_perclient, readlatencyvalues_noisemim, \
                    readbandwidthvalues_self_perclient, readbandwidthvalues_mim_perclient_usingfixbucket


_BOXPLOT_COLORS=['#F8B195', "#355C7D", '#C06C84', '#F67280', '#99B898', '#A8E6CE', '#E84A5F', '#A7226E', 
                 '#F7DB4F', "#FC913A", "#1bdc9d", "#9c5a4c", "#9c4c84", "#4c999c", '#F8B195', "#355C7D", 
                 '#C06C84', '#F67280', '#99B898', '#A8E6CE', '#F8B195', "#355C7D", '#C06C84', '#F67280', 
                 '#99B898', '#A8E6CE', '#F8B195', "#355C7D", '#C06C84', '#F67280', '#99B898', '#A8E6CE', 
                 '#F8B195', "#355C7D", '#C06C84', '#F67280', '#99B898', '#A8E6CE']

_WARNING = '\033[93m'
_FAIL = '\033[91m'
_RESET = '\033[0m'
    
_LEGENDYPOS_1LINE = 1.15
_LEGENDYPOS_2LINE = 1.25
_LEGENDYPOS_4LINE = 1.40
_LEGENDYPOS_10LINE = 2.05
_LABEL_SIZE = 20
_TICK_SIZE = 20
_XLABEL_SIZE = 25
_YLABEL_SIZE = 25


_PASSIVEBANDWIDTH_CLIENTNOISEGROUPED_BASEFILEPATH = "bandwidthplot/clientnoisegrouped/"
_PASSIVEBANDWIDTH_CLIENTNOISEANDSEGMENTGROUPED_BASEFILEPATH = "bandwidthplot/clientnoiseandsegmentgrouped/"
_PASSIVEMIM_CLIENTFILEANDSEGMENTGROUPED_BASEFILEPATH = "bandwidthplot/fileandsegmentgrouped/mim/"

_SELFMIMBANDWIDTH_CONNTYPEFILESERVERPOS_XNOISE_BASEFILEPATH = "bandwidthplot/conntypefileserverpos/x_noise/"
_SELFMIMBANDWIDTH_CONNTYPEFILESERVERPOS_XCLIENTS_BASEFILEPATH = "bandwidthplot/conntypefileserverpos/x_clients/"
_SELFMIMBANDWIDTH_CONNTYPESERVERPOSNUMCLIENT_BASEFILEPATH = "bandwidthplot/conntypeserverposnumclient/"
_SELFMIMBANDWIDTH_CONNTYPESERVERPOSCROSSTRAFFIC_BASEFILEPATH = "bandwidthplot/conntypeserverposcrosstraffic/"

_ACTIVELATENCY_COMMANDANDNOISEGROUPED_BASEFILEPATH= "latencyplot/commandandnoisegrouped/active/" 
_ACTIVELATENCY_SEGMENTANDNOISEGROUPED_BASEFILEPATH= "latencyplot/segmentandnoisegrouped/active/"
_ACTIVELATENCY_CONNTYPEANDGROUPED_BASEFILEPATH= "latencyplot/contypeandnoisegrouped/active/"



#ACTIVE BANDWIDTH PLOTS
def bandwidthboxplot_active_conntypegrouped(config_parser, command, direction, ncol, legendypos, ylim):
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    dateslist_wifi = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    dateslist_lte = config_parser.get("experiment_conf", "dates_activelte").split(",")
     
    values = OrderedDict()
    title = command + "-" + direction
    folderpath = "bandwidth/active/boxplot_conntype/"
    ylabel = "Bandwidth (Mbps)"
    xlabel = "CrossTraffic (Mbps)"
    legendlabels = []
    if direction == "Upstream":
        legendlabels.append("Wi-Fi: Access -> MEC")
        legendlabels.append("LTE: Access -> MEC")
        legendlabels.append("Wi-Fi: Access -> Cloud")
        legendlabels.append("LTE: Access -> Cloud")
        legendlabels.append("Wi-Fi: MEC -> Cloud")
        legendlabels.append("LTE: MEC -> Cloud")
    elif direction == "Downstream":
        legendlabels.append("Wi-Fi: MEC -> Access")
        legendlabels.append("LTE: MEC -> Access")
        legendlabels.append("Wi-Fi: Cloud -> Access")
        legendlabels.append("LTE: Cloud -> Access")
        legendlabels.append("Wi-Fi: Cloud -> MEC")
        legendlabels.append("LTE: Cloud -> MEC")
    elif direction == "both":
            legendlabels.append("Wi-Fi: Access -> MEC") #upstream
            legendlabels.append("LTE: Access -> MEC") #upstream
       
            legendlabels.append("Wi-Fi: MEC -> Access") #downstream        
            legendlabels.append("LTE: MEC -> Access") #downstream

            legendlabels.append("Wi-Fi: Access -> Cloud") #upstream
            legendlabels.append("LTE: Access -> Cloud") #upstream

            legendlabels.append("Wi-Fi: Cloud -> Access") #downstream
            legendlabels.append("LTE: Cloud -> Access") #downstream

            legendlabels.append("Wi-Fi: MEC -> Cloud") #upstream
            legendlabels.append("LTE: MEC -> Cloud") #upstream

            legendlabels.append("Wi-Fi: Cloud -> MEC") #downstream
            legendlabels.append("LTE: Cloud -> MEC") #downstream
    else:
        print (_WARNING + "unknown direction: " + direction + _RESET)
        sys.exit(0)


    for noise in noiselist:
        values[noise] = []

        if direction == "Upstream" or direction == "Downstream":
            #clientnitos wifi
            filename = "csv/active/" + command + "-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #clientnitos lte
            filename = "csv/active/" + command + "-" + direction + "-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #clientUnipi wifi
            filename = "csv/active/" + command + "-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #clientUnipi lte
            filename = "csv/active/" + command + "-" + direction + "-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #nitosunipi wifi
            filename = "csv/active/" + command + "-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
            #NITOS UNIPI lte
            filename = "csv/active/" + command + "-" + direction + "-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
        elif direction == "both":
            #clientnitos wifi Upstream
            filename = "csv/active/" + command + "-Upstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #clientnitos lte Upstream
            filename = "csv/active/" + command + "-Upstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            
            #clientnitos wifi downstream
            filename = "csv/active/" + command + "-Downstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientNitos"))    
            #clientnitos lte Downstream
            filename = "csv/active/" + command + "-Downstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            
            #clientUnipi wifi upstream
            filename = "csv/active/" + command + "-Upstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #clientUnipi lte Upstream
            filename = "csv/active/" + command + "-Upstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            
            #clientUnipi wifi downstream
            filename = "csv/active/" + command + "-Downstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #clientUnipi lte Downstream
            filename = "csv/active/" + command + "-Downstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            
            #nitosunipi wifi Upstream
            filename = "csv/active/" + command + "-Upstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
            #NITOS UNIPI lte Upstream
            filename = "csv/active/" + command + "-Upstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
            
            #nitosunipi wifi Downstream
            filename = "csv/active/" + command + "-Downstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
            #NITOS UNIPI lte Downstream
            filename = "csv/active/" + command + "-Downstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
        else:
            print ("unknown directioniji")
            print(direction)
            sys.exit(0)

                       
    if len(values) == 0:
        print (_WARNING + "No data for file " + filename + _RESET)
        return

    createfolder(folderpath)

    drawboxplot(folderpath, title+"_showFliers=False", values, legendlabels, ylim, ylabel, xlabel, 
                show_fliers=False, numcolumn = ncol, legendpos=legendypos)

    ylim += 300
    drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, show_fliers=True,  
                numcolumn = ncol, legendpos=legendypos)
def bandwidthboxplot_active(config_parser, command, direction, connectiontype, ylim, legendypos):
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    if connectiontype == "wifi":
        dateslist = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    elif connectiontype == "lte":
        dateslist = config_parser.get("experiment_conf", "dates_activelte").split(",")

    values = OrderedDict()
    title = command + "-" + direction + "-" + connectiontype
    folderpath = "bandwidth/active/boxplot/" + connectiontype + "/"
    ylabel = "Bandwidth (Mbps)"
    xlabel = "CrossTraffic (Mbps)"


    legendlabels = []
    if direction == "Upstream":
        legendlabels.append("Access -> MEC")
        legendlabels.append("Access -> Cloud")
        legendlabels.append("MEC -> Cloud")
    elif direction == "Downstream":
        legendlabels.append("MEC -> Access")
        legendlabels.append("Cloud -> Access")
        legendlabels.append("Cloud -> MEC")
        
    else:
        print ("unknown direction")
        sys.exti(0)


    for noise in noiselist:
        values[noise] = []

        filename = "csv/active/" + command + "-" + direction + "-" + connectiontype + "-noise" + noise + "_"         
        filename +=  dateslist[0].strip() + "-" + dateslist[-1].strip() + ".csv"
                            
        values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
        values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
        values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))   

                   
    if len(values) == 0:
        print (_WARNING + "No data for file " + filename + _RESET)
        return

    createfolder(folderpath)

    #draw plot without fliers
    show_fliers = False
    ncol = 3
    

    drawboxplot(folderpath, title+"_2", values, legendlabels, ylim, ylabel, xlabel, show_fliers, ncol, legendypos)

    #drawplot with fliers
    ylim += 300
    show_fliers = True

    drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, show_fliers, ncol, legendypos)



#ACTIVE LATENCY PLOTS
def latencyboxplot_active_commandgrouped(config_parser, direction, connectiontype, ylim, legendypos):
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    dateslist_wifi = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    dateslist_lte = config_parser.get("experiment_conf", "dates_activelte").split(",")
    if connectiontype == "wifi":
        dateslist = dateslist_wifi
    elif connectiontype == "lte":
        dateslist = dateslist_lte

     
     
    values = OrderedDict()
    title = direction + "-" + connectiontype
    folderpath = _ACTIVELATENCY_COMMANDANDNOISEGROUPED_BASEFILEPATH 
    ylabel = "Latency (ms)"
    xlabel = "CrossTraffic (Mbps)"
    legendlabels = []
    if direction == "Upstream" and connectiontype != "both":
        legendlabels.append("TCPRTT: Access -> MEC")
        legendlabels.append("UDPRTT: Access -> MEC")
        legendlabels.append("TCPRTT: Access -> Cloud")
        legendlabels.append("UDPRTT: Access -> Cloud")
        legendlabels.append("TCPRTT: MEC -> Cloud")
        legendlabels.append("UDPRTT: MEC -> Cloud")
    elif direction == "Downstream" and connectiontype != "both":
        legendlabels.append("TCPRTT: MEC -> Access")
        legendlabels.append("UDPRTT: MEC -> Access")
        legendlabels.append("TCPRTT: Cloud -> Access")
        legendlabels.append("UDPRTT: Cloud -> Access")
        legendlabels.append("TCPRTT: Cloud -> MEC")
        legendlabels.append("UDPRTT: Cloud -> MEC")
    elif direction == "Upstream" and connectiontype == "both":
        legendlabels.append("TCPRTT Wi-Fi: Access -> MEC")
        legendlabels.append("TCPRTT LTE: Access -> MEC")
        legendlabels.append("UDPRTT Wi-Fi: Access -> MEC")
        legendlabels.append("UDPRTT LTE: Access -> MEC")
        legendlabels.append("TCPRTT Wi-Fi: Access -> Cloud")
        legendlabels.append("TCPRTT LTE: Access -> Cloud")
        legendlabels.append("UDPRTT Wi-Fi: Access -> Cloud")
        legendlabels.append("UDPRTT LTE: Access -> Cloud")
        legendlabels.append("TCPRTT Wi-Fi: MEC -> Cloud")
        legendlabels.append("TCPRTT LTE: MEC -> Cloud")
        legendlabels.append("UDPRTT Wi-Fi: MEC -> Cloud")
        legendlabels.append("UDPRTT LTE: MEC -> Cloud")       
    elif direction == "Downstream" and connectiontype == "both":
        legendlabels.append("TCPRTT Wi-Fi: MEC -> Access")
        legendlabels.append("TCPRTT LTE: MEC -> Access")
        legendlabels.append("UDPRTT Wi-Fi: MEC -> Access")
        legendlabels.append("UDPRTT LTE: MEC -> Access")
        legendlabels.append("TCPRTT Wi-Fi: Cloud -> Access")
        legendlabels.append("TCPRTT LTE: Cloud -> Access")
        legendlabels.append("UDPRTT Wi-Fi: Cloud -> Access")
        legendlabels.append("UDPRTT LTE: Cloud -> Access")
        legendlabels.append("TCPRTT Wi-Fi: Cloud -> MEC")
        legendlabels.append("TCPRTT LTE: Cloud -> MEC")
        legendlabels.append("UDPRTT Wi-Fi: Cloud -> MEC")
        legendlabels.append("UDPRTT LTE: Cloud -> MEC")   
    else:
        print ("unknown direction")
        print (direction)
        sys.exit(0)

    for noise in noiselist:
        values[noise] = []
        if connectiontype != "both":
            #TCPRRT clientnitos
            filename = "csv/active/TCPRTT-" + direction + "-" + connectiontype + "-noise" + noise + "_"         
            filename +=  dateslist[0].strip() + "-" + dateslist[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #UDPRTT clientnitos
            filename = "csv/active/UDPRTT-" + direction + "-" + connectiontype + "-noise" + noise + "_"         
            filename +=  dateslist[0].strip() + "-" + dateslist[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #TCPRTT clientUnipi
            filename = "csv/active/TCPRTT-" + direction + "-" + connectiontype + "-noise" + noise + "_"         
            filename +=  dateslist[0].strip() + "-" + dateslist[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #UDPRTT clientUnipi
            filename = "csv/active/UDPRTT-" + direction + "-" + connectiontype + "-noise" + noise + "_"         
            filename +=  dateslist[0].strip() + "-" + dateslist[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #TCPRTT nitosunipi
            filename = "csv/active/TCPRTT-" + direction + "-" + connectiontype + "-noise" + noise + "_"         
            filename +=  dateslist[0].strip() + "-" + dateslist[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
            #UDPRTT NITOS UNIPI
            filename = "csv/active/UDPRTT-" + direction + "-" + connectiontype + "-noise" + noise + "_"         
            filename +=  dateslist[0].strip() + "-" + dateslist[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
        else:
            #TCPRRT clientnitos wifi
            filename = "csv/active/TCPRTT-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #TCPRRT clientnitos lte
            filename = "csv/active/TCPRTT-" + direction + "-lte-noise" + noise + "_"         
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #UDPRTT clientnitos wifi
            filename = "csv/active/UDPRTT-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #UDPRTT clientnitos lte
            filename = "csv/active/UDPRTT-" + direction + "-lte-noise" + noise + "_"         
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #TCPRTT clientUnipi wifi
            filename = "csv/active/TCPRTT-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #TCPRTT clientUnipi lte
            filename = "csv/active/TCPRTT-" + direction + "-lte-noise" + noise + "_"         
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #UDPRTT clientUnipi wifi
            filename = "csv/active/UDPRTT-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #UDPRTT clientUnipi lte
            filename = "csv/active/UDPRTT-" + direction + "-lte-noise" + noise + "_"         
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #TCPRTT nitosunipi wifi
            filename = "csv/active/TCPRTT-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
            #TCPRTT nitosunipi lte
            filename = "csv/active/TCPRTT-" + direction + "-lte-noise" + noise + "_"         
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
            #UDPRTT NITOS UNIPIwifi
            filename = "csv/active/UDPRTT-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))           
            #UDPRTT NITOS UNIPI lte
            filename = "csv/active/UDPRTT-" + direction + "-lte-noise" + noise + "_"         
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))

                   
    if len(values) == 0:
        print (_WARNING + "No data for file " + filename + _RESET)
        return

    createfolder(folderpath)


    drawboxplot(folderpath, title+"_2", values, legendlabels, ylim, ylabel, xlabel, legendpos=legendypos)

    ylim += 300
    show_fliers = True
    drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, show_fliers, legendpos=legendypos)
def latencyboxplot_active(config_parser, command, direction, connectiontype, ylim, legendypos):
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    if connectiontype == "wifi":
        dateslist = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    elif connectiontype == "lte":
        dateslist = config_parser.get("experiment_conf", "dates_activelte").split(",")
     
     
    values = OrderedDict()

    title = command + "-" + direction + "-" + connectiontype
    ylabel = "Latency (ms)"
    xlabel = "CrossTraffic (Mbps)"


    legendlabels = []
    if direction == "Upstream":
        legendlabels.append("Access -> MEC")
        legendlabels.append("Access -> Cloud")
        legendlabels.append("MEC -> Cloud")
    elif direction == "Downstream":
        legendlabels.append("MEC -> Access")
        legendlabels.append("Cloud -> Access")
        legendlabels.append("Cloud -> MEC")
    else:
        print ("unknown direction")
        sys.exti(0)


    for noise in noiselist:
        values[noise] = []

        filename = "csv/active/" + command + "-" + direction + "-" + connectiontype + "-noise" + noise + "_"         
        filename +=  dateslist[0].strip() + "-" + dateslist[-1].strip() + ".csv"
        folderpath = _ACTIVELATENCY_SEGMENTANDNOISEGROUPED_BASEFILEPATH + connectiontype + "/"
                            
        values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
        values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
        values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))   

                   
    if len(values) == 0:
        print (_WARNING + "No data for file " + filename + _RESET)
        return

    createfolder(folderpath)
    show_fliers = False
    ncolumn = 3
    drawboxplot(folderpath, title+"_2", values, legendlabels, ylim, ylabel, xlabel, show_fliers, ncolumn, 
                legendypos)

    ylim += 300
    show_fliers = True
    drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, show_fliers, ncolumn, 
                legendypos)
def latencyboxplot_active_conntypegrouped(config_parser, command, direction, ncol, legendypos, ylim):
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    dateslist_wifi = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    dateslist_lte = config_parser.get("experiment_conf", "dates_activelte").split(",")

    values = OrderedDict()
    title = command + "-" + direction

    folderpath = _ACTIVELATENCY_CONNTYPEANDGROUPED_BASEFILEPATH
    ylabel = "Latency (ms)"
    xlabel = "CrossTraffic (Mbps)"
    legendlabels = []

    if direction == "Upstream":
        legendlabels.append("Wi-Fi: Access -> MEC")
        legendlabels.append("LTE: Access -> MEC")
        legendlabels.append("Wi-Fi: Access -> Cloud")
        legendlabels.append("LTE: Access -> Cloud")
        legendlabels.append("Wi-Fi: MEC -> Cloud")
        legendlabels.append("LTE: MEC -> Cloud")
    elif direction == "Downstream":
        legendlabels.append("Wi-Fi: MEC -> Access")
        legendlabels.append("LTE: MEC -> Access")
        legendlabels.append("Wi-Fi: Cloud -> Access")
        legendlabels.append("LTE: Cloud -> Access")
        legendlabels.append("Wi-Fi: Cloud -> MEC")
        legendlabels.append("LTE: Cloud -> MEC")
    elif direction == "both":
        legendlabels.append("Wi-Fi: Access -> MEC") #upstream
        legendlabels.append("LTE: Access -> MEC") #upstream
        legendlabels.append("Wi-Fi: MEC -> Access") #downstream
        legendlabels.append("LTE: MEC -> Access") #downstream

        legendlabels.append("Wi-Fi: Access -> Cloud") #upstream
        legendlabels.append("LTE: Access -> Cloud") #upstream
        legendlabels.append("Wi-Fi: Cloud -> Access") #downstream
        legendlabels.append("LTE: Cloud -> Access") #downstream

        legendlabels.append("Wi-Fi: MEC -> Cloud") #upstream
        legendlabels.append("LTE: MEC -> Cloud") #upstream
        legendlabels.append("Wi-Fi: Cloud -> MEC") #downstream
        legendlabels.append("LTE: Cloud -> MEC") #downstream 
    else:
        print ("unknown direction")
        sys.exit(0)

    for noise in noiselist:
        values[noise] = []
        if direction == "Upstream" or direction == "Downstream":
            #clientnitos wifi
            filename = "csv/active/" + command + "-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #clientnitos lte
            filename = "csv/active/" + command + "-" + direction + "-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #clientUnipi wifi
            filename = "csv/active/" + command + "-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #clientUnipi lte
            filename = "csv/active/" + command + "-" + direction + "-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #nitosunipi wifi
            filename = "csv/active/" + command + "-" + direction + "-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
            #NITOS UNIPI lte
            filename = "csv/active/" + command + "-" + direction + "-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
        if direction == "both":
            #clientnitos wifi Upstream
            filename = "csv/active/" + command + "-Upstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #clientnitos lte Upstream
            filename = "csv/active/" + command + "-Upstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))

            #clientnitos wifi downstream
            filename = "csv/active/" + command + "-Downstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
            #clientnitos lte Downstream
            filename = "csv/active/" + command + "-Downstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))

            #clientUnipi wifi upstream
            filename = "csv/active/" + command + "-Upstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #clientUnipi lte Upstream
            filename = "csv/active/" + command + "-Upstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))

            #clientUnipi wifi downstream
            filename = "csv/active/" + command + "-Downstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
            #clientUnipi lte Downstream
            filename = "csv/active/" + command + "-Downstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))

            #nitosunipi wifi Upstream
            filename = "csv/active/" + command + "-Upstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
            #NITOS UNIPI lte Upstream
            filename = "csv/active/" + command + "-Upstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))

            #nitosunipi wifi Downstream
            filename = "csv/active/" + command + "-Downstream-wifi-noise" + noise + "_"         
            filename +=  dateslist_wifi[0].strip() + "-" + dateslist_wifi[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
            #NITOS UNIPI lte Downstream
            filename = "csv/active/" + command + "-Downstream-lte-noise" + noise + "_" 
            filename +=  dateslist_lte[0].strip() + "-" + dateslist_lte[-1].strip() + ".csv"
            values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))
                       
    if len(values) == 0:
        print (_WARNING + "No data for file " + filename + _RESET)
        return

    createfolder(folderpath)

    drawboxplot(folderpath, title+"_showFliers=False", values, legendlabels, ylim, ylabel, xlabel, 
                show_fliers=False, numcolumn = ncol, legendpos=legendypos)

    ylim += 300
    drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, show_fliers=True,  
                numcolumn = ncol, legendpos=legendypos)


#PASSIVE BANDWIDTH
def bandwidthboxplot_noisegrouped(config_parser, mode, direction, connectiontype, ylim, edgeserver, segmentgrouped, ncol, legendypos):
    assert mode == "self"
    
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
     
    values = OrderedDict()

    for dashfile in dashfileslist:
        title = mode + "-" + direction + "-" + connectiontype + "-" + connectiontype + "-bandwidth-" + dashfile 
        legendlabels = []

        if edgeserver and not segmentgrouped:
            title += "-edge"
        elif not edgeserver and not segmentgrouped:
            title += "-cloud"

        for noise in noiselist:
            values[noise] =[]

            if not segmentgrouped:
                for clientnumber in clientnumberlist:
                    filename  = "csv/passive/sorted/" + mode + "-bandwidth-" + direction + "-" + connectiontype
                    filename += "-" + str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                    filename += config_parser.get("experiment_conf", "from_passive") + "-" 
                    filename += config_parser.get("experiment_conf", "to_passive") + "_SORTED_LEGACY.csv"

                    values[noise].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                inputfile=filename, edgeserver=edgeserver, 
                                                conntype=connectiontype)) 
            elif segmentgrouped:
                for clientnumber in clientnumberlist:
                    filename  = "csv/passive/sorted/" + mode + "-bandwidth-" + direction + "-" + connectiontype 
                    filename += "-" + str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                    filename += config_parser.get("experiment_conf", "from_passive") + "-" 
                    filename += config_parser.get("experiment_conf", "to_passive") + "_SORTED_LEGACY.csv"
                
                    values[noise].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                inputfile=filename, edgeserver=True, 
                                                conntype=connectiontype))
                    values[noise].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                inputfile=filename, edgeserver=False, 
                                                conntype=connectiontype))
                

        if not segmentgrouped:
            folderpath =  _PASSIVEBANDWIDTH_CLIENTNOISEGROUPED_BASEFILEPATH + connectiontype + "/"
            for i in range (0, len(clientnumberlist)):
                legendlabels.append("Number of clients = " + str(clientnumberlist[i]))
        if segmentgrouped:
            folderpath = _PASSIVEBANDWIDTH_CLIENTNOISEANDSEGMENTGROUPED_BASEFILEPATH + connectiontype + "/"
            for i in range (0, len(clientnumberlist)):
                if i == 0:
                    legendlabels.append("MEC " + str(clientnumberlist[i]) + " client")
                    legendlabels.append("Cloud " + str(clientnumberlist[i]) + " client")
                else:
                    legendlabels.append("MEC " + str(clientnumberlist[i]) + " clients")
                    legendlabels.append("Cloud " + str(clientnumberlist[i]) + " clients")

                   
        if len(values) == 0:
            print (_WARNING + "No data for file " + dashfile + _RESET)
            continue
        
        #print folderpath
        createfolder(folderpath)
        ylabel = "Bandwidth (Mbps)"
        xlabel = "CrossTraffic (Mbps)"

        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, False, ncol, legendypos)


def mimselfbandwidthboxplot_conntypefileserverpos_xnoise(config_parser, direction,  
                                                         connectiontype, ylim, edgecloudserver, ncol, 
                                                         legendypos, logger):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    fromtimestamp_passive = config_parser.get("experiment_conf", "from_passive")
    totimestamp_passive = config_parser.get("experiment_conf", "to_passive")

    folderpath =  _SELFMIMBANDWIDTH_CONNTYPEFILESERVERPOS_XNOISE_BASEFILEPATH
    legendlabels = []
    for clientnumber in clientnumberlist:
        clientnumber_str = str(clientnumber)
        if clientnumber == 1:
            clientnumber_str += " client"
        else:
            clientnumber_str += "clients"
        
        if edgecloudserver == "both":
            legendlabels.append("self MEC " + clientnumber_str)
            legendlabels.append("mim MEC " + clientnumber_str)
            legendlabels.append("self cloud " + clientnumber_str)
            legendlabels.append("mim cloud " + clientnumber_str)
        elif edgecloudserver == "edge":
            legendlabels.append("self MEC " + clientnumber_str)
            legendlabels.append("mim MEC " + clientnumber_str)
        elif edgecloudserver == "cloud":
            legendlabels.append("self Cloud " + clientnumber_str)
            legendlabels.append("mim Cloud " + clientnumber_str)
        else:
            print ("edgecloudserver: unknown value " + edgecloudserver)
            sys.exit(1)
    
    for dashfile in dashfileslist:
        values = OrderedDict()
        title = connectiontype + "-" + direction + "-bandwidth-" + dashfile + "-" + edgecloudserver
        
        for noise in noiselist:
            values[noise] =[]

            for clientnumber in clientnumberlist:
                filename_self  = "csv/passive/sorted/self-bandwidth-" + direction + "-" + connectiontype + "-" 
                filename_self += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename_self += fromtimestamp_passive + "-" + totimestamp_passive + "_SORTED_LEGACY.csv"

                filename_mim  = "csv/passive/sorted/mim-bandwidth-" + direction + "-" + connectiontype + "-" 
                filename_mim += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename_mim += fromtimestamp_passive + "-" + totimestamp_passive + "_SORTED_LEGACY.csv"

            
                if edgecloudserver != "both":
                    if edgecloudserver == "edge":
                        values[noise].append(readbandwidthvalues_self(config_parser=config_parser, 
                                            inputfile=filename_self, edgeserver=True, conntype=connectiontype))
                        values[noise].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                            inputfile=filename_mim, segment="edge", 
                                            connectiontype=connectiontype, logger=logger))  
                    elif edgecloudserver == "cloud":
                        values[noise].append(readbandwidthvalues_self(config_parser=config_parser, 
                                            inputfile=filename_self, edgeserver=False, conntype=connectiontype))
                        values[noise].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                            inputfile=filename_mim, segment="remote", 
                                            connectiontype=connectiontype, logger=logger))  
                else:
                    values[noise].append(readbandwidthvalues_self(config_parser=config_parser, 
                                        inputfile=filename_self, edgeserver=True, conntype=connectiontype))
                    values[noise].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                        inputfile=filename_mim, segment="edge", connectiontype=connectiontype, 
                                        logger=logger))  
                    values[noise].append(readbandwidthvalues_self(config_parser=config_parser, 
                                        inputfile=filename_self, edgeserver=False, conntype=connectiontype))
                    values[noise].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                        inputfile=filename_mim, segment="remote", 
                                        connectiontype=connectiontype, logger=logger)) 
     
        if len(values) == 0:
            print (_WARNING + "No data for file " + dashfile + _RESET)
            continue
            
        #print folderpath
        createfolder(folderpath)
        ylabel = "Bandwidth (Mbps)"
        xlabel = "CrossTraffic (Mbps)"

        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, False, ncol, legendypos)
def mimselfbandwidthboxplot_conntypefileserverpos_xclients(config_parser, direction, connectiontype, ylim, 
                                                           edgecloudserver, ncol, legendypos, logger):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    fromtimestamp_passive = config_parser.get("experiment_conf", "from_passive")
    totimestamp_passive = config_parser.get("experiment_conf", "to_passive")

    folderpath =  _SELFMIMBANDWIDTH_CONNTYPEFILESERVERPOS_XCLIENTS_BASEFILEPATH
    legendlabels = []
    for noise in noiselist:       
        if edgecloudserver == "both":
            legendlabels.append("self MEC " + noise + "bps cross traffic")
            legendlabels.append("mim MEC " + noise + "bps cross traffic")
            legendlabels.append("self cloud " + noise + "bps cross traffic")
            legendlabels.append("mim cloud " + noise + "bps cross traffic")
        elif edgecloudserver == "edge":
            legendlabels.append("self MEC " + noise + "bps cross traffic")
            legendlabels.append("mim MEC " + noise + "bps cross traffic")
        elif edgecloudserver == "cloud":
            legendlabels.append("self Cloud " + noise + "bps cross traffic")
            legendlabels.append("mim Cloud " + noise + "bps cross traffic")
        else:
            print ("edgecloudserver: unknown value " + edgecloudserver)
            sys.exit(1)

    for dashfile in dashfileslist:
        values = OrderedDict()
        title = connectiontype + "-" + direction + "-bandwidth-" + dashfile + "-" + edgecloudserver
        
        for clientnumber in clientnumberlist:
            values[clientnumber] =[]

            for noise in noiselist: 
                #input file of self measurements           
                filename_self  = "csv/passive/sorted/self-bandwidth-" + direction + "-" + connectiontype + "-" 
                filename_self += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename_self += fromtimestamp_passive + "-" + totimestamp_passive + "_SORTED_LEGACY.csv"
                #input file for mim measurements
                filename_mim  = "csv/passive/sorted/mim-bandwidth-" + direction + "-" + connectiontype + "-" 
                filename_mim += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename_mim += fromtimestamp_passive + "-" + totimestamp_passive + "_SORTED_LEGACY.csv"

                if edgecloudserver != "both":
                    if edgecloudserver == "edge":
                        values[clientnumber].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=True, 
                                                                conntype=connectiontype))
                        values[clientnumber].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="edge", 
                                                                connectiontype=connectiontype, logger=logger))  
                    elif edgecloudserver == "cloud":
                        values[clientnumber].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=False, 
                                                                conntype=connectiontype))
                        values[clientnumber].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="remote", 
                                                                connectiontype=connectiontype, logger=logger))  
                else:
                    values[clientnumber].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=True, 
                                                                conntype=connectiontype))
                    values[clientnumber].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="edge", 
                                                                connectiontype=connectiontype, logger=logger))  
                    values[clientnumber].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=False, 
                                                                conntype=connectiontype))
                    values[clientnumber].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="remote", 
                                                                connectiontype=connectiontype, logger=logger)) 
     
        if len(values) == 0:
            print (_WARNING + "No data for file " + dashfile + _RESET)
            continue
            
        #print folderpath
        createfolder(folderpath)
        ylabel = "Bandwidth (Mbps)"
        xlabel = "Number of clients"

        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, False, ncol, legendypos)
def mimselfbandwidthboxplot_conntypeserverposnumclient(config_parser, direction, connectiontype, ylim, 
                                                       edgecloudserver, ncol, legendypos, logger):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    fromtimestamp_passive = config_parser.get("experiment_conf", "from_passive")
    totimestamp_passive = config_parser.get("experiment_conf", "to_passive")

    folderpath =  _SELFMIMBANDWIDTH_CONNTYPESERVERPOSNUMCLIENT_BASEFILEPATH
    legendlabels = []
    for filename in dashfileslist:       
        if edgecloudserver == "both":
            legendlabels.append("self MEC " + filename)
            legendlabels.append("mim MEC " + filename)
            legendlabels.append("self cloud " + filename)
            legendlabels.append("mim cloud " + filename)
        elif edgecloudserver == "edge":
            legendlabels.append("self MEC " + filename)
            legendlabels.append("mim MEC " + filename)
        elif edgecloudserver == "cloud":
            legendlabels.append("self Cloud " + filename)
            legendlabels.append("mim Cloud " + filename)
        else:
            print ("edgecloudserver: unknown value " + edgecloudserver)
            sys.exit(1)

    for clientnumber in clientnumberlist:
        values = OrderedDict()
        title = connectiontype + "-" + direction + "-bandwidth-" + clientnumber + "clients-" + edgecloudserver
        
        for noise in noiselist:
            values[noise] =[]

            for dashfile in dashfileslist: 
                #input file of self measurements           
                filename_self  = "csv/passive/sorted/self-bandwidth-" + direction + "-" + connectiontype + "-" 
                filename_self += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename_self += fromtimestamp_passive + "-" + totimestamp_passive + "_SORTED_LEGACY.csv"
                #input file for mim measurements
                filename_mim  = "csv/passive/sorted/mim-bandwidth-" + direction + "-" + connectiontype + "-" 
                filename_mim += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename_mim += fromtimestamp_passive + "-" + totimestamp_passive + "_SORTED_LEGACY.csv"

                if edgecloudserver != "both":
                    if edgecloudserver == "edge":
                        values[noise].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=True, 
                                                                conntype=connectiontype))
                        values[noise].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="edge", 
                                                                connectiontype=connectiontype, logger=logger))  
                    elif edgecloudserver == "cloud":
                        values[noise].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=False, 
                                                                conntype=connectiontype))
                        values[noise].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="remote", 
                                                                connectiontype=connectiontype, logger=logger))  
                else:
                    values[noise].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=True, 
                                                                conntype=connectiontype))
                    values[noise].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="edge", 
                                                                connectiontype=connectiontype, logger=logger))  
                    values[noise].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=False, 
                                                                conntype=connectiontype))
                    values[noise].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="remote", 
                                                                connectiontype=connectiontype, logger=logger)) 
     
        if len(values) == 0:
            print (_WARNING + "No data for " + clientnumber + " clients" + _RESET)
            continue
            
        #print folderpath
        createfolder(folderpath)
        ylabel = "Bandwidth (Mbps)"
        xlabel = "Cross traffic (Mbps)"

        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, False, ncol, legendypos)
def mimselfbandwidthboxplot_conntypeserverposcrosstraffic(config_parser, direction, connectiontype, ylim, 
                                                          edgecloudserver, ncol, legendypos, logger):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    fromtimestamp_passive = config_parser.get("experiment_conf", "from_passive")
    totimestamp_passive = config_parser.get("experiment_conf", "to_passive")

    folderpath =  _SELFMIMBANDWIDTH_CONNTYPESERVERPOSCROSSTRAFFIC_BASEFILEPATH
    legendlabels = []
    for filename in dashfileslist:       
        if edgecloudserver == "both":
            legendlabels.append("self MEC " + filename)
            legendlabels.append("mim MEC " + filename)
            legendlabels.append("self cloud " + filename)
            legendlabels.append("mim cloud " + filename)
        elif edgecloudserver == "edge":
            legendlabels.append("self MEC " + filename)
            legendlabels.append("mim MEC " + filename)
        elif edgecloudserver == "cloud":
            legendlabels.append("self Cloud " + filename)
            legendlabels.append("mim Cloud " + filename)
        else:
            print ("edgecloudserver: unknown value " + edgecloudserver)
            sys.exit(1)

    for noise in noiselist:
        values = OrderedDict()
        title = connectiontype + "-" + direction + "-bandwidth-" + noise + "-" + edgecloudserver
        
        for clientnumber in clientnumberlist:
            values[clientnumber] =[]

            for dashfile in dashfileslist: 
                #input file of self measurements           
                filename_self  = "csv/passive/sorted/self-bandwidth-" + direction + "-" + connectiontype + "-" 
                filename_self += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename_self += fromtimestamp_passive + "-" + totimestamp_passive + "_SORTED_LEGACY.csv"
                #input file for mim measurements
                filename_mim  = "csv/passive/sorted/mim-bandwidth-" + direction + "-" + connectiontype + "-" 
                filename_mim += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename_mim += fromtimestamp_passive + "-" + totimestamp_passive + "_SORTED_LEGACY.csv"

                if edgecloudserver != "both":
                    if edgecloudserver == "edge":
                        values[clientnumber].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=True, 
                                                                conntype=connectiontype))
                        values[clientnumber].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="edge", 
                                                                connectiontype=connectiontype, logger=logger))  
                    elif edgecloudserver == "cloud":
                        values[clientnumber].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=False, 
                                                                conntype=connectiontype))
                        values[clientnumber].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="remote", 
                                                                connectiontype=connectiontype, logger=logger))  
                else:
                    values[clientnumber].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=True, 
                                                                conntype=connectiontype))
                    values[clientnumber].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="edge", 
                                                                connectiontype=connectiontype, logger=logger))  
                    values[clientnumber].append(readbandwidthvalues_self(config_parser=config_parser, 
                                                                inputfile=filename_self, edgeserver=False, 
                                                                conntype=connectiontype))
                    values[clientnumber].append(readbandwidthvalues_mim(config_parser=config_parser, 
                                                                inputfile=filename_mim, segment="remote", 
                                                                connectiontype=connectiontype, logger=logger)) 
     
        if len(values) == 0:
            print (_WARNING + "No data for " + noise + " of cross traffic" + _RESET)
            continue
            
        #print folderpath
        createfolder(folderpath)
        ylabel = "Bandwidth (Mbps)"
        xlabel = "Number of clients"

        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, False, ncol, legendypos)


def bandwidthplot_fileandsegmentgrouped(config_parser, mode, direction, connectiontype, ncol, legendypos, ylim = 50):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    folderpath = "bandwidthplot/fileandsegmentgrouped/" + connectiontype + "/"
    ylabel = "Bandwidth (Mbps)"
    xlabel = "CrossTraffic (Mbps)"

    createfolder(folderpath)
     
    values = OrderedDict()

    for noise in noiselist:
        title = mode + "-" + direction + "-" + connectiontype + "-bandwidth-" + noise 

        legendlabels = []

        for dashfile in dashfileslist:
            values[dashfile] =[]

            for clientnumber in clientnumberlist:
                filename  = "csv/passive/sorted/" + mode + "-bandwidth-" + direction + "-" + connectiontype
                filename += "-" +  str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_"  
                filename += config_parser.get("experiment_conf", "from_passive") + "-" 
                filename += config_parser.get("experiment_conf", "to_passive") + "_SORTED_LEGACY.csv"
            
                values[dashfile].append(readbandwidthvalues_self(config_parser=config_parser,  
                                                inputfile=filename, edgeserver=True, conntype=connectiontype))
                values[dashfile].append(readbandwidthvalues_self(config_parser=config_parser,  
                                                inputfile=filename, edgeserver=False, conntype=connectiontype))      

        for i in range (0, len(clientnumberlist)):
            if i == 0:
                legendlabels.append("MEC " + str(clientnumberlist[i]) + " client")
                legendlabels.append("Cloud " + str(clientnumberlist[i]) + " client")
            else:
                legendlabels.append("MEC " + str(clientnumberlist[i]) + " clients")
                legendlabels.append("Cloud " + str(clientnumberlist[i]) + " clients")

                   
        if len(values) == 0:
            print (_WARNING + "No data for file " + dashfile + _RESET)
            continue
        
        #ylim = 50
        showfliers = False

        print (ncol)
        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, showfliers, ncol, legendypos)
def bandwidthplot_mimfileandsegment(config_parser, mode, direction, connectiontype, ncol, legendypos, logger):
    assert mode == "mim"
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    folderpath = _PASSIVEMIM_CLIENTFILEANDSEGMENTGROUPED_BASEFILEPATH + connectiontype + "/"
    ylabel = "Bandwidth (Mbps)"
    xlabel = "CrossTraffic (Mbps)"

    createfolder(folderpath)
    values = OrderedDict()

    for noise in noiselist:
        title = mode + "-" + direction + "-" + connectiontype + "-bandwidth-" + noise 

        legendlabels = []

        for dashfile in dashfileslist:
            values[dashfile] =[]

            for clientnumber in clientnumberlist:
                filename = "csv/passive/sorted/" + mode + "-bandwidth-" + direction + "-" + connectiontype 
                filename += "-" + str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_"  
                filename += config_parser.get("experiment_conf", "from_passive") + "-" 
                filename += config_parser.get("experiment_conf", "to_passive") + "_SORTED_LEGACY.csv"
            
                values[dashfile].append(readbandwidthvalues_mim(config_parser, filename, connectiontype,
                                                            "edge", logger=logger))  
                values[dashfile].append(readbandwidthvalues_mim(config_parser, filename, connectiontype,
                                                            "remote", logger=logger))       
        for i in range (0, len(clientnumberlist)):
            if i == 0:
                legendlabels.append("MEC " + str(clientnumberlist[i]) + " client")
                legendlabels.append("Cloud " + str(clientnumberlist[i]) + " client")
            else:
                legendlabels.append("MEC " + str(clientnumberlist[i]) + " clients")
                legendlabels.append("Cloud " + str(clientnumberlist[i]) + " clients")
                   
        if len(values) == 0:
            print (_WARNING + "No data for file " + dashfile + _RESET)
            continue
        
        ylim = 50
        showfliers = False

        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, showfliers, ncol, legendypos)
def bandwidthboxplot_noisemim(config_parser, direction, connectiontype, ylim, legendypos, server, logger,
                              segmentgrouped=False, ncol=5):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    folderpath = "bandwidthplot/noisegrouped/mim" + connectiontype + "/"
    ylabel = "Bandwidth (Mbps)"
    xlabel = "CrossTraffic (Mbps)"
     
    createfolder(folderpath)
    values = OrderedDict()


    for dashfile in dashfileslist:
        title = "mim-" + direction + "-" + connectiontype + "-" + connectiontype + "-bandwidth-" + dashfile+"-"+server
        legendlabels = []


        for noise in noiselist:
            values[noise] =[]

            for clientnumber in clientnumberlist:
                filename  = "csv/passive/sorted/mim-bandwidth-" + direction + "-" + connectiontype
                filename += "-" + str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename += config_parser.get("experiment_conf", "from_passive") + "-" 
                filename += config_parser.get("experiment_conf", "to_passive") + "_SORTED_LEGACY.csv"
            
                values[noise].append(readbandwidthvalues_mim(config_parser, filename, connectiontype,
                                                            server, logger=logger))     
            
        for i in range (0, len(clientnumberlist)):
            legendlabels.append("Number of clients = " + str(clientnumberlist[i]))


        if len(values) == 0:
            print (_WARNING + "No data for file " + dashfile + _RESET)
            continue
        
        #print values
        
        
        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, False, ncol)


def bandwidthplot_perclient(config_parser, direction, connectiontype, mode, ylim, legendypos, server, ncol, 
                            logger, legacy):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")

    folderpath = "bandwidthplot/perclient/" + mode + connectiontype + "/"
    ylabel = "Bandwidth (Mbps)"
    xlabel = "Number of clients"
     
    createfolder(folderpath)
    
    for dashfile in dashfileslist:
        for noise in noiselist:
            values = OrderedDict()

            title  = mode + "-" + direction + "-" + connectiontype + "-bandwidth-" + dashfile + "-" + server 
            title += "-" + str(noise)
            if mode == "self" or not legacy:
                title += "_2"

            print (title)
            legendlabels = []
            for clientnumber in clientnumberlist:
                values[clientnumber] = []
            
                inputfilename  = "csv/passive/sorted/" + mode + "-bandwidth-" + direction + "-"
                inputfilename += connectiontype + "-" + str(clientnumber) + "clients-" + dashfile + "-noise"  
                inputfilename += noise + "_" +config_parser.get("experiment_conf", "from_passive") + "-"
                inputfilename += config_parser.get("experiment_conf", "to_passive")
                if mode == "mim" and legacy:
                    inputfilename += "_SORTED_LEGACY.csv"
                else:
                    inputfilename += "_SORTED.csv"


                if mode == "mim":    
                    if legacy:        
                        ret = (readbandwidthvalues_mim_perclient(config_parser=config_parser, 
                                            inputfile=inputfilename, connectiontype=connectiontype, 
                                            segment=server, logger=logger)) 
                    else:
                        ret = (readbandwidthvalues_mim_perclient_usingfixbucket(config_parser=config_parser, 
                                            inputfile=inputfilename, connectiontype=connectiontype, 
                                            segment=server, logger=logger))                
                elif mode == "self":
                    ret = (readbandwidthvalues_self_perclient(config_parser=config_parser, 
                                            inputfile=inputfilename, conntype=connectiontype, server=server, 
                                            logger=logger)) 
                else:
                    sys.exit(0)

                #logger.debug (ret)
                boxplotvalues=[]
                i=1
                for key, val in ret.items():
                    logger.debug (val)
                    boxplotvalues.append(val)
                    legendlabels.append(str(clientnumber) + "-" + str(i) + ": " + str(key))
                    i += 1
                
                
                values[clientnumber] = boxplotvalues
                

            
            #for i in range (0, len(clientnumberlist)):
            #    legendlabels.append("Number of clients = " + str(clientnumberlist[i]))


            if len(values) == 0:
                print (_WARNING + "No data for file " + dashfile + "and cross_traffic " + noise + _RESET)
                continue
        
            #logger.debug(values)
        
        
            drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, False, ncol, legendpos=_LEGENDYPOS_10LINE)


def bandwidthboxplot_noiseandsegmentmim(config_parser, direction, connectiontype, ylim, legendypos, 
                                        logger, segmentgrouped = False, ncol = 2):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    folderpath = "bandwidthplot/noiseandsegmentgrouped/mim" + connectiontype + "/"
    ylabel = "Bandwidth (Mbps)"
    xlabel = "CrossTraffic (Mbps)"
     
    createfolder(folderpath)

    for dashfile in dashfileslist:
        title = "mim-" + direction + "-" + connectiontype + "-" + connectiontype + "-bandwidth-" + dashfile+"-"
        values = OrderedDict()
        legendlabels = []


        for noise in noiselist:
            values[noise] =[]

            for clientnumber in clientnumberlist:
                filename = "csv/passive/sorted/mim-bandwidth-" + direction + "-" + connectiontype
                filename += "-" + str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename += config_parser.get("experiment_conf", "from_passive") + "-" 
                filename += config_parser.get("experiment_conf", "to_passive") + "_SORTED_LEGACY.csv"
            
                values[noise].append(readbandwidthvalues_mim(config_parser, filename, connectiontype,
                                                             "edge", logger=logger))      
                values[noise].append(readbandwidthvalues_mim(config_parser, filename, connectiontype,
                                                             "remote", logger=logger))   

            
        for i in range (0, len(clientnumberlist)):
            if i == 0:
                legendlabels.append("MEC " + str(clientnumberlist[i]) + " client")
                legendlabels.append("Cloud " + str(clientnumberlist[i]) + " client")
            else:
                legendlabels.append("MEC " + str(clientnumberlist[i]) + " clients")
                legendlabels.append("Cloud " + str(clientnumberlist[i]) + " clients")


        if len(values) == 0:
            print (_WARNING + "No data for file " + dashfile + _RESET)
            continue
        
        #print values       
        
        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, False, ncol, legendypos)



#MIM LATENCY
def latencyboxplot_noiseandsegmentmim(config_parser, direction, connectiontype, ylim, legendypos, ncol=2):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    folderpath = "latency/mim/noiseandsegmentgrouped/" + connectiontype + "/"
    ylabel = "Latency (ms)"
    xlabel = "CrossTraffic (Mbps)"
     
    createfolder(folderpath)
    values = OrderedDict()

    for dashfile in dashfileslist:
        title = "mim-" + direction + "-" + connectiontype + "-latency-" + dashfile
        legendlabels = []

        for noise in noiselist:
            values[noise] =[]

            for clientnumber in clientnumberlist:
                filename = "csv/passive/sorted/mim-latency-" + direction + "-" + connectiontype
                filename += "-" + str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" \
                            + config_parser.get("experiment_conf", "from_passive") + "-" \
                            + config_parser.get("experiment_conf", "to_passive") + "_SORTED_LEGACY.csv"
            
                values[noise].append(readlatencyvalues_noisemim(config_parser, filename, connectiontype,
                                                         "edge" , noise))      
                values[noise].append(readlatencyvalues_noisemim(config_parser, filename, connectiontype,
                                                         "remote" , noise))   

            
        for i in range (0, len(clientnumberlist)):
            if i == 0:
                legendlabels.append("MEC " + str(clientnumberlist[i]) + " client")
                legendlabels.append("Cloud " + str(clientnumberlist[i]) + " client")
            else:
                legendlabels.append("MEC " + str(clientnumberlist[i]) + " clients")
                legendlabels.append("Cloud " + str(clientnumberlist[i]) + " clients")


        if len(values) == 0:
            print (_WARNING + "No data for file " + dashfile + _RESET)
            continue
        
        #print values       
        
        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, False, ncol, legendypos)



def latencyboxplot_noiseandsegmentactivemim(config_parser, direction, connectiontype, ylim, legendypos, logger, activecommand="TCPRTT", ncol=2):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber_passive" + connectiontype).split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    folderpath = "latency/activemim/" + connectiontype + "/"
    ylabel = "Latency (ms)"
    xlabel = "CrossTraffic (Mbps)"
    if connectiontype == "wifi":
        dateslist_active = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    elif connectiontype == "lte":
        dateslist_active = config_parser.get("experiment_conf", "dates_activelte").split(",")

    if direction == "downlink":
        direction_active = "Downstream"
    elif direction == "uplink":
        direction_active = "Upstream"
     
    createfolder(folderpath)
    values = OrderedDict()

    for dashfile in dashfileslist:
        title = "activemim-" + direction + "-" + connectiontype + "-latency-" + dashfile
        legendlabels = []

        for noise in noiselist:
            values[noise] =[]

            filenameactive  = "csv/active/" + activecommand + "-" + direction_active + "-" + connectiontype 
            filenameactive += "-noise" + noise + "_"  + dateslist_active[0].strip() + "-"
            filenameactive += dateslist_active[-1].strip() + ".csv"
    
            
            #active edge
            values[noise].append(readvalues_activelatencyboxplot(filenameactive, int(noise.replace("M", "")), 
                                                                 "clientNitos"))
            #mim edge                                           
            for clientnumber in clientnumberlist:
                filenamepassive  = "csv/passive/sorted/mim-latency-" + direction + "-" + connectiontype
                filenamepassive += "-" + str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" \
                                 + config_parser.get("experiment_conf", "from_passive") + "-" \
                                 + config_parser.get("experiment_conf", "to_passive") + "_SORTED_LEGACY.csv"
            
                values[noise].append(readlatencyvalues_noisemim(config_parser, filenamepassive, connectiontype,
                                                                "edge" , noise))   

            #active cloud
            values[noise].append(readvalues_activelatencyboxplot(filenameactive, 
                                                            int(noise.replace("M", "")), "clientUnipi"))
            #mim cloud
            for clientnumber in clientnumberlist:
                filenamepassive  = "csv/passive/sorted/mim-latency-" + direction + "-" + connectiontype
                filenamepassive += "-" + str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" \
                                 + config_parser.get("experiment_conf", "from_passive") + "-" \
                                 + config_parser.get("experiment_conf", "to_passive") + "_SORTED_LEGACY.csv"
                values[noise].append(readlatencyvalues_noisemim(config_parser, filenamepassive, connectiontype,
                                                                "remote" , noise))   
        #active edge
        legendlabels.append("active MEC (1 client)")
        #passive - edge
        for i in range (0, len(clientnumberlist)):
            if i == 0:
                legendlabels.append("mim - MEC " + str(clientnumberlist[i]) + " client")
            else:
                legendlabels.append("mim -MEC " + str(clientnumberlist[i]) + " clients")
        #active cloud
        legendlabels.append("active Cloud (1 client)")
        #passive cloud
        for i in range (0, len(clientnumberlist)):
            if i == 0:
                legendlabels.append("mim - Cloud " + str(clientnumberlist[i]) + " client")
            else:
                legendlabels.append("mim - Cloud " + str(clientnumberlist[i]) + " clients")


        if len(values) == 0:
            print (_WARNING + "No data for file " + dashfile + _RESET)
            continue
        
        #print values       
        
        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, False, ncol, legendypos)





def createfolder(directoryname):
    try:
        os.makedirs(directoryname)
    except OSError as error:
        if error.errno != errno.EEXIST:
            print (error)
            sys.exit(0)
def writelogfile_boxplot(noise, logfile,  bp, clients, values):
    logfile.write("\n\n\n" + noise )

    logfile.write("\nmedians: \n")
    for i in range(0, len(bp["medians"])):
        medianvalue = bp["medians"][i].get_ydata()[0]
        logfile.write("\t" + clients[i] + ": median = " + str(medianvalue) + "\n")

    logfile.write("\nwhiskers: \n")
    for i in range(0, len(bp["whiskers"])/2):
        logfile.write("\t" + clients[i] + ": Q1=" + str(bp["whiskers"][i * 2].get_ydata()[0]) + ", " \
                                            "Q1-1.5IQR=" + str(bp["whiskers"][i * 2].get_ydata()[1]) + ", " \
                                            "Q3=" + str(bp["whiskers"][i * 2 + 1].get_ydata()[0]) + ", " \
                                            "Q3 + 1.5IQR=" + str(bp["whiskers"][i * 2 + 1].get_ydata()[1]) +\
                                            "\n")
    

    logfile.write("\nValues:\n")
    for i in range(0, len(values)):
        logfile.write("\tValues len[" + str(i) + "]: " + str(len(values[i])) + "\n")
        #logfile.write("\tvalues[" + str(i) + "]:" + str(values[i]) + "\n")
        logfile.write("\n")
def drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, xlabel, show_fliers=False, 
                numcolumn = 3, legendpos = 1.12):
    fig = plt.figure(figsize=(15,6))
    
    ax = plt.axes()
    ax.tick_params(axis="both", labelsize=_TICK_SIZE)
    plt.grid(True, axis="y", color="#dedddc")

    xlabels = []
    xlabelspos = []

    i = 1
    with open(folderpath + title + ". txt", "w") as logfile:
        #values = [  
        #           (group #1, [[bar 1 measures], [bar #2 measures], ..., [bar #n measures]]),
        #           (group #2, [[bar 1 measures], [bar #2 measures], ..., [bar #n measures]]),
        #                                      ....
        #           (group #k, [[bar 1 measures], [bar #2 measures], ..., [bar #n measures]])
        #        ]
        for key, value in values.items():
            #   key = group #i
            #   value = [[bar 1 of group i measures], [bar #2 of group i measures], ..., [bar #n of group i measures]]
            
            #for eac group of boxplots
            plt.axvline(1.0 * i + len(value) + 0.1, 0, 10, color="#b0b0b0")

            boxplotpos = []

            

            for k in range (0, len(value)):
                boxplotpos.append(i + k)
            
            #print ("len: " + str(len(value)))
            #print (boxplotpos)

            if len(value) == 0:
                i += 1
                continue
            

            #print (value)
            
            bp = ax.boxplot(value, positions = boxplotpos, widths = 0.6, patch_artist=True, showfliers=show_fliers,
                            medianprops={"color":"black"}, showmeans=False)

            j=0
            for box in bp["boxes"]:
                #set the color
                plt.setp(box, facecolor=_BOXPLOT_COLORS[j], edgecolor='black')
                j += 1
            

            xlabels.append(key)
            xlabelspos.append(1.0* i + (1.0 * len(value)/2) - 0.5)
            i += k + 2

            #writelogfile_boxplot(key, logfile, bp, legendlabels, value)
            
    # set axes limits and labels
    plt.xlim(0,i - 1)
    plt.ylim(0, ylim)
    if ylim < 100:
        plt.yticks(np.arange(0, ylim, step = 5))
    elif ylim < 200:
        plt.yticks(np.arange(0, ylim, step = 10))
    elif ylim < 1000:
        plt.yticks(np.arange(0, ylim, step = 50))
    elif ylim < 3000:
        plt.yticks(np.arange(0, ylim, step = 200))
    ax.set_xticklabels(xlabels)
    ax.set_xticks(xlabelspos)

    ax.set_xlabel(xlabel, fontsize=_XLABEL_SIZE)
    ax.set_ylabel(ylabel, fontsize=_YLABEL_SIZE)
    #plt.xlabel(xlabel)
    #plt.ylabel(ylabel)

    plt.title(title + "___" + str(datetime.now()))
    #print (legendpos)
    leg = ax.legend(legendlabels, loc="upper center", bbox_to_anchor=(0.5, legendpos), ncol = 3, 
                    facecolor = "white", frameon=False, fontsize=_LABEL_SIZE) 

    i=0
    for item in leg.legendHandles:
        item.set_color(_BOXPLOT_COLORS[i])
        item.set_linewidth(2.0)
        i += 1
    
    pdfpage = PdfPages(folderpath + title + ".pdf")
    pdfpage.savefig( bbox_inches="tight")
    pdfpage.close()

    #plt.show()
    plt.savefig(folderpath + title + ".png", bbox_inches="tight")
    plt.close()


