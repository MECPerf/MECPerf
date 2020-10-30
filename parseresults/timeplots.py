import matplotlib.pyplot as plt
import matplotlib.dates as md
import datetime
import numpy as np
from matplotlib.backends.backend_pdf import PdfPages
import numpy as np
import sys
import errno
import os

from readcsv import readbandwidthvalues_self_timeplot

_LABEL_SIZE = 20
_TICK_SIZE = 20
_XLABEL_SIZE = 25
_YLABEL_SIZE = 25

_LEGENDYPOS_10LINE = 2.05

_BUCKETSIZE_SEC = 0.5
_BUCKETSIZE_SEC_BANDWIDTH = 0.5
_TIMEPLOT_COLORS=['#F8B195', "#355C7D", '#C06C84', '#F67280', '#99B898', '#A8E6CE', '#E84A5F', '#A7226E', 
                  '#F7DB4F', "#FC913A", "#1bdc9d", "#9c5a4c", "#9c4c84", "#4c999c", '#F8B195', "#355C7D", 
                  '#C06C84', '#F67280', '#99B898', '#A8E6CE', '#F8B195', "#355C7D", '#C06C84', '#F67280', 
                  '#99B898', '#A8E6CE', '#F8B195', "#355C7D", '#C06C84', '#F67280', '#99B898', '#A8E6CE', 
                  '#F8B195', "#355C7D", '#C06C84', '#F67280', '#99B898', '#A8E6CE']

_TIMEPLOT_MARKERS = [".", "o", "v", "^", "<", ">", "1", "2", "3", "4", "8", "s", "p", "P", "*", "h", "H", 
                     "+", "x", "X", "D", "d", "|", "_", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]



def _getclientnumberlist(config_parser, conntype):
    return config_parser.get("experiment_conf", "clientnumber_passive" + conntype).split(",")
def _getdashfilelist(config_parser):
    return config_parser.get("experiment_conf", "dashfiles").split(",")
def _getnoiselist(config_parser):
    return config_parser.get("experiment_conf", "noise").split(",")
def _createfolder(directoryname):
    try:
        os.makedirs(directoryname)
    except OSError as error:
        if error.errno != errno.EEXIST:
            print (error)
            sys.exit(0)


# mode=self/mim
#direction = downstream/upstream
#connectiontype=wifi/lte
#ylim
#edgeserver=edge/cloud
def passive_timeseries(config_parser, mode, direction, connectiontype, ylim, server, ncol, 
                       legendypos, logger):
    assert mode == "self"
    clientnumberlist = _getclientnumberlist(config_parser=config_parser, conntype=connectiontype)
    dashfileslist = _getdashfilelist(config_parser=config_parser)
    noiselist = _getnoiselist(config_parser=config_parser)

    logger.debug("clientnumberlist" + str(clientnumberlist))
    logger.debug ("dashfileslist" + str(dashfileslist))
    logger.debug ("noiselist" +  str(noiselist))

    for clientnumber in clientnumberlist:
        for noise in noiselist:        
            for dashfile in dashfileslist: 
                title  = str(mode) + "-" + str(direction) + "-" + str(dashfile) + str(clientnumber) + "-" 
                title += str(noise) + "-" + str(server) + "_plot1"

                filename  = "csv/passive/sorted/" + mode + "-bandwidth-" + direction + "-" + connectiontype + "-"
                filename += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename += config_parser.get("experiment_conf", "from_passive") + "-" 
                filename += config_parser.get("experiment_conf", "to_passive") + "_SORTED.csv" 

                folderpath = "time/bandwidth/" + str(connectiontype) + "/" 

                plt = create_passivetimeseriesplot(plottitle = title, nclient=int(clientnumber))
                ret = readbandwidthvalues_self_timeplot(config_parser=config_parser, inputfile=filename, 
                                                        segment=server, conntype=connectiontype, logger=logger)

                passive_timeseries_plot(plt=plt, results=ret, title=title, folderpath=folderpath)    

def passive_timeseries_plot(plt, results, title, folderpath):
    legendlabels = []
    ytick_labels = []
    ytick_positions = []

    i = 0
    for clientIP, clientresults in results.items():
        times = []
        ypos = []
        first_timestamp = None
        
        for elem in clientresults:
            if first_timestamp == None:
                times.append (elem["timestamp"] + datetime.timedelta(seconds=_BUCKETSIZE_SEC/2))
                ypos.append(i)
                first_timestamp = elem["timestamp"]
                continue

            if (elem["timestamp"] - first_timestamp).total_seconds() < _BUCKETSIZE_SEC:
                continue 

            times.append(elem["timestamp"]  + datetime.timedelta(seconds=_BUCKETSIZE_SEC/2))
            ypos.append(i)

            first_timestamp = elem["timestamp"]
        
        plt.plot(times, ypos, marker =_TIMEPLOT_MARKERS[i], linestyle="None", label=clientIP, color=_TIMEPLOT_COLORS[i])

        ytick_labels.append(clientIP)
        ytick_positions.append(i)
        legendlabels.append(clientIP)
        i += 1


    plt.yticks(ytick_positions, ytick_labels)                
    _createfolder(folderpath)

    pdfpage = PdfPages(folderpath + title + ".pdf")
    pdfpage.savefig( bbox_inches="tight")
    pdfpage.close()

    plt.savefig(folderpath + title + ".png", bbox_inches="tight")
    plt.close() 


# mode=self/mim
#direction = downstream/upstream
#connectiontype=wifi/lte
#ylim
#edgeserver=edge/cloud
def passive_timeseries_usingbandwidth(config_parser, mode, direction, connectiontype, ylim, server, ncol, 
                                      legendypos, logger):
    assert mode == "self"
    clientnumberlist = _getclientnumberlist(config_parser=config_parser, conntype=connectiontype)
    dashfileslist = _getdashfilelist(config_parser=config_parser)
    noiselist = _getnoiselist(config_parser=config_parser)

    logger.debug("clientnumberlist" + str(clientnumberlist))
    logger.debug ("dashfileslist" + str(dashfileslist))
    logger.debug ("noiselist" +  str(noiselist))



    for clientnumber in clientnumberlist:
        for noise in noiselist:
            for dashfile in dashfileslist:        
                title  = str(mode) + "-" + str(direction) + "-" + str(dashfile) + str(clientnumber) + "-" 
                title += str(noise) + "-" + str(server) + "_plot2"

                filename  = "csv/passive/sorted/" + mode + "-bandwidth-" + direction + "-" + connectiontype + "-"
                filename += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename += config_parser.get("experiment_conf", "from_passive") + "-" 
                filename += config_parser.get("experiment_conf", "to_passive") + "_SORTED.csv"
                

                    
                ret = readbandwidthvalues_self_timeplot(config_parser=config_parser, inputfile=filename, 
                                                        segment=server, conntype=connectiontype, logger=logger)
                
                print (len(ret))
                
                fig, ax = plt.subplots(ncols = 1, nrows = len(ret), squeeze=False)
                plt.title(title + "___" + str(datetime.datetime.now()))
                fig.set_size_inches(8.3, 1.6 * len(ret))
                fig.tight_layout()
                plt.grid(True, axis="y", color="#dedddc")
                plt.title(title)
                plt.gcf().autofmt_xdate()
                formatter = "%H:%M:%S"
                xfmt = md.DateFormatter(formatter)
                
                legendlabels = []
                ytick_labels = []
                ytick_positions = []
                
                
                time_min = None
                time_max = None
                xvalues=[]
                yvalues = []
                titles =[]
                for clientIP, clientresults in ret.items():
                    times = []
                    bandwidths = []
                    first_timestamp = None
                
                    for elem in clientresults:
                        '''if first_timestamp == None:
                            first_timestamp = elem["timestamp"]
                            bandwidthsMbps = elem["bandwidthMbps"]
                            counter = 1
                            if time_min == None:
                                time_max = first_timestamp
                                time_min = first_timestamp
                            continue

                        if (elem["timestamp"] - first_timestamp).total_seconds() < _BUCKETSIZE_SEC_BANDWIDTH:
                            bandwidthsMbps += elem["bandwidthMbps"]
                            counter += 1
                            continue 

                        if time_min > elem["timestamp"]:
                            timemin = elem["timestamp"]
                        if time_max < elem["timestamp"]:
                            time_max = elem["timestamp"]

                        times.append (first_timestamp + datetime.timedelta(seconds=_BUCKETSIZE_SEC_BANDWIDTH/2))
                        bandwidths.append(1.0 * bandwidthsMbps / counter)

                        first_timestamp = elem["timestamp"]
                        bandwidthsMbps = elem["bandwidthMbps"]
                        counter = 1'''

                        times.append (elem["timestamp"])
                        if time_min == None:
                            time_min = elem["timestamp"]
                            time_max = elem["timestamp"]

                        if time_min > elem["timestamp"]:
                            timemin = elem["timestamp"]
                        if time_max < elem["timestamp"]:
                            time_max = elem["timestamp"]

                        bandwidths.append(elem["bandwidthMbps"])

                    xvalues.append(times)
                    yvalues.append(bandwidths)  
                    titles.append(clientIP) 

               
                    
                #time_min -= datetime.timedelta(seconds=_BUCKETSIZE_SEC * 2)
                #time_max += datetime.timedelta(seconds=_BUCKETSIZE_SEC * 2)
                #print (xvalues)
                #print (yvalues)
                for k in range(0, len(xvalues)):
                    #print times
                    #print bandwidth

                    if len(xvalues[k]) != 0:
                        ax[k, 0].tick_params(axis="both", labelsize=_TICK_SIZE)
                        ax[k, 0].set_xlabel("t")
                        ax[k, 0].set_ylabel("Mbps)")
                        ax[k, 0].set_ylim(0,ylim)
                        ax[k, 0].set_xlim(time_min,time_max)
                        ax[k, 0].set_title("client " + str(k + 1) + "- with IP = " + titles[k])
                        ax[k, 0].xaxis.set_major_formatter(xfmt)
                        ax[k, 0].plot(xvalues[k], yvalues[k], marker =_TIMEPLOT_MARKERS[k], markersize=3, 
                                    label=titles[k], color=_TIMEPLOT_COLORS[k], linestyle="None")           

                folderpath = "time/bandwidth/" + str(connectiontype) + "/"
                _createfolder(folderpath)
            
                '''pdfpage = PdfPages(folderpath + title + ".pdf")
                pdfpage.savefig( bbox_inches="tight")
                pdfpage.close()'''


                plt.savefig(folderpath + title + ".png", bbox_inches="tight") 
                plt.close()

            

def create_passivetimeseriesplot(plottitle, nclient):
    fig = plt.figure(figsize=(15,0.6 * nclient))
    plt.grid(True, axis="y", color="#dedddc")
    ax = plt.gca()
    ax.tick_params(axis="both", labelsize=_TICK_SIZE)
    ax.set_xlabel("Time")
    ax.set_ylabel("Bandwidth ()Mbps)")


    plt.title(plottitle + "___" + str(datetime.datetime.now()))
    print("plotting " + plottitle + "___" + str(datetime.datetime.now()) + "...")
    plt.gcf().autofmt_xdate()
    formatter = "%H:%M:%S"
    xfmt = md.DateFormatter(formatter)
    ax.xaxis.set_major_formatter(xfmt)

    return plt