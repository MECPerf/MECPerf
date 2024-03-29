import matplotlib.pyplot as plt
import matplotlib.dates as md
import matplotlib as mpl
import datetime
import numpy as np
from matplotlib.backends.backend_pdf import PdfPages
import numpy as np
import sys
import errno
import os
from collections import OrderedDict
import math

from readcsv import readbandwidthvalues_self_timeplot, readbandwidthvalues_mim_timeplot, \
                    readbandwidthvalues_mim_timeplot_usingfixbuckets

_TITLE_SIZE = 14
_TICK_SIZE = 10
_XLABEL_SIZE = 14
_YLABEL_SIZE = 14

_LEGENDYPOS_10LINE = 2.05

_BUCKETSIZE_SEC = 0.5
_BUCKETSIZE_SEC_PORTS = 1

_TIMEPLOT_COLORS=['#F8B195', "#355C7D", '#C06C84', '#F67280', '#99B898', '#A8E6CE', '#E84A5F', '#A7226E', 
                  '#F7DB4F', "#FC913A", "#1bdc9d", "#9c5a4c", "#9c4c84", "#4c999c", '#F8B195', "#355C7D", 
                  '#C06C84', '#F67280', '#99B898', '#A8E6CE', '#F8B195', "#355C7D", '#C06C84', '#F67280', 
                  '#99B898', '#A8E6CE', '#F8B195', "#355C7D", '#C06C84', '#F67280', '#99B898', '#A8E6CE', 
                  '#F8B195', "#355C7D", '#C06C84', '#F67280', '#99B898', '#A8E6CE']
_FRAGMENTQUALITY_COLORS={"index":"#e48873", "audio":'#21409a', "low":"#f44546", "mid":'#e68a00', "high":"#039c4b"}

_TIMEPLOT_MARKERS = [".", "o", "v", "^", "<", ">", "1", "2", "3", "4", "8", "s", "p", "P", "*", "h", "H", 
                     "+", "x", "X", "D", "d", "|", "_", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]



def getclientnumberlist(config_parser, section, conntype):
    return config_parser.get(section, "clientnumber_passive" + conntype).split(",")
def getdashfilelist(config_parser, section):
    return config_parser.get(section, "dashfiles").split(",")
def getnoiselist(config_parser, section):
    return config_parser.get(section, "noise").split(",")
def createfolder(directoryname):
    try:
        os.makedirs(directoryname)
    except OSError as error:
        if error.errno != errno.EEXIST:
            print (error)
            sys.exit(0)



def passivetimeseries_createplot(plottitle, nclient):
    fig = plt.figure(figsize=(15,0.6 * nclient))
    ax = plt.gca()
    ax.tick_params(axis="both", labelsize=_TICK_SIZE)
    ax.set_xlabel("Time", fontsize=_XLABEL_SIZE)
    ax.set_ylabel("Bandwidth ()Mbps)", fontsize=_YLABEL_SIZE)

    plt.title(plottitle + "___" + str(datetime.datetime.now()))
    print("plotting " + plottitle + "___" + str(datetime.datetime.now()) + "...")
    plt.gcf().autofmt_xdate()
    formatter = "%H:%M:%S"
    xfmt = md.DateFormatter(formatter)
    ax.xaxis.set_major_formatter(xfmt)

    return plt
def passivetimeseries_plot(plt, results, title, folderpath):
    legendlabels = []
    ytick_labels = []
    ytick_positions = []

    i = 0
    for clientIP, clientresults in results.items():
        times = []
        ypos = []
        #first_timestamp = None
        
        for elem in clientresults:
            '''if first_timestamp == None:
                times.append (elem["timestamp"] + datetime.timedelta(seconds=_BUCKETSIZE_SEC/2))
                ypos.append(i)
                first_timestamp = elem["timestamp"]
                continue

            if (elem["timestamp"] - first_timestamp).total_seconds() < _BUCKETSIZE_SEC:
                continue '''

            times.append(elem["timestamp"] )# + datetime.timedelta(seconds=_BUCKETSIZE_SEC/2))
            ypos.append(i)

            #first_timestamp = elem["timestamp"]
        
        plt.plot(times, ypos, marker =_TIMEPLOT_MARKERS[i], linestyle="None", label=clientIP, color=_TIMEPLOT_COLORS[i])

        ytick_labels.append(clientIP)
        ytick_positions.append(i)
        legendlabels.append(clientIP)
        i += 1


    plt.yticks(ytick_positions, ytick_labels)                
    createfolder(folderpath)

    pdfpage = PdfPages(folderpath + title + ".pdf")
    pdfpage.savefig( bbox_inches="tight")
    pdfpage.close()

    plt.savefig(folderpath + title + ".png", bbox_inches="tight")
    plt.close() 
def passivetimeseries(config_parser, section, mode, direction, connectiontype, ylim, server, ncol, 
                       legendypos, logger): #edgeserver=edge/cloud
    clientnumberlist = getclientnumberlist(config_parser=config_parser, section=section, conntype=connectiontype)
    dashfileslist = getdashfilelist(config_parser=config_parser, section=section)
    noiselist = getnoiselist(config_parser=config_parser, section=section)

    logger.debug("clientnumberlist" + str(clientnumberlist))
    logger.debug ("dashfileslist" + str(dashfileslist))
    logger.debug ("noiselist" +  str(noiselist))
    logger.debug("mode = " + mode)

    for clientnumber in clientnumberlist:
        for noise in noiselist:        
            for dashfile in dashfileslist: 
                title  = str(mode) + "-" + str(direction) + "-" + str(dashfile) + str(clientnumber) + "-" 
                title += str(noise) + "-" + str(server) + "_plot1"

                filename  = "csv/passive/sorted/" + mode + "-bandwidth-" + direction + "-" + connectiontype + "-"
                filename += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename += config_parser.get(section, "from_passive") + "-" 
                filename += config_parser.get(section, "to_passive") + "_SORTED.csv" 

                folderpath = "time/bandwidth/" + str(connectiontype) + "/" 

                plt = passivetimeseries_createplot(plottitle = title, nclient=int(clientnumber))

                if mode == "self":
                    ret = readbandwidthvalues_self_timeplot(config_parser=config_parser, section=section, 
                                                            inputfile=filename, segment=server, 
                                                            conntype=connectiontype, logger=logger)
                elif mode == "mim":
                    ret = readbandwidthvalues_mim_timeplot(config_parser=config_parser, section=section, 
                                                           inputfile=filename, segment=server, 
                                                           conntype=connectiontype, logger=logger)
                else:
                    print("unknown mode " + mode)
                    print("exiting")
                    logger.error("unknown mode " + mode)
                    logger.error("exiting")
                    sys.exit(0)

                passivetimeseries_plot(plt=plt, results=ret, title=title, folderpath=folderpath)    



def passivetimeseries_usingbandwidth_createplot(colsnumber, rowsnumber, plottitle):
    fig, ax = plt.subplots(ncols = colsnumber, nrows = rowsnumber, squeeze=False)
    fig.set_size_inches(11 * colsnumber, 1.6 * rowsnumber + 0.5)
    #fig.tight_layout()
    
    plt.title(plottitle )#+ "___" + str(datetime.datetime.now()), fontsize=_TITLE_SIZE)
    plt.gcf().autofmt_xdate()

    formatter = "%H:%M:%S"
    xfmt = md.DateFormatter(formatter)

    return plt, ax, xfmt
def passivetimeseries_usingbandwidth_plot(results, title, folderpath, ylim, title2="", plotfragmentquality=False):
    legendlabels = []
    ytick_labels = []
    ytick_positions = []
    
    time_min = None
    time_max = None
    k=0
    #xvalues=[]
    #yvalues = []
    #fragmentqualities = []
    #titles =[]
    if not plotfragmentquality:
        plt, ax, xfmt = passivetimeseries_usingbandwidth_createplot(colsnumber=1, plottitle=title, 
                                                                rowsnumber=len(results))
    clientrank=0
    for clientIP, clientresults in results.items():
        clientrank += 1
        if not plotfragmentquality:
            times = []
            bandwidths = []
        else:
            times = {"index":[], "audio":[], "low":[], "mid":[], "high":[]}
            bandwidths = {"index":[], "audio":[], "low":[], "mid":[], "high":[]} 

        for elem in clientresults:
            if not plotfragmentquality:
                times.append (elem["timestamp"])
                bandwidths.append(elem["bandwidthMbps"])
            elif plotfragmentquality:
                fragment_quality=elem["fragmentquality"]

                times[fragment_quality].append (elem["timestamp"])
                bandwidths[fragment_quality].append(elem["bandwidthMbps"])

            if time_min == None:
                time_min = elem["timestamp"]
                time_max = elem["timestamp"]

            if time_min > elem["timestamp"]:
                timemin = elem["timestamp"]
            if time_max < elem["timestamp"]:
                time_max = elem["timestamp"]
        
        #for each sublot
        if len(times) != 0:
            
            #ax[k, 0].set_title("client " + str(k + 1) + "- with IP = " + titles[k] + "___" + \
            #                   str(datetime.datetime.now()) + title2)
            if title2 != "":
                title2 = "_" + title2
            
        
            if not plotfragmentquality:
                ax[k, 0].grid(False)
                ax[k, 0].tick_params(axis="both", labelsize=_TICK_SIZE)
                ax[k, 0].set_xlabel("t", fontsize=_XLABEL_SIZE)
                ax[k, 0].set_ylabel("Mbps", fontsize=_YLABEL_SIZE)
                ax[k, 0].set_ylim(0,ylim)
                ax[k, 0].set_xlim(time_min,time_max)
                ax[k, 0].xaxis.set_major_formatter(xfmt)
                #ax[k, 0].set_title("client " + str(k + 1) + " with IP = " + clientIP + title2, fontsize=_TITLE_SIZE)
                ax[k, 0].set_title("client " + str(k + 1), fontsize=_TITLE_SIZE)
                ax[k, 0].plot(times, bandwidths, marker =_TIMEPLOT_MARKERS[k], markersize=3, 
                              label=clientIP, color=_TIMEPLOT_COLORS[k], linestyle="None")     
                k += 1      
            else:
                k = 0
                plt, ax, xfmt = passivetimeseries_usingbandwidth_createplot(colsnumber=1, plottitle=title+clientIP, 
                                                                rowsnumber=4)
                
    
                for key, value in times.items():
                    if key == "index":
                        continue
                    print ("Plotting")
                    print (key)
                    ax[k, 0].grid(axis="y", color="#dedddc")
                    ax[k, 0].tick_params(axis="both", labelsize=_TICK_SIZE)
                    ax[k, 0].set_xlabel("t", fontsize=_XLABEL_SIZE)
                    ax[k, 0].set_ylabel("Mbps", fontsize=_YLABEL_SIZE)
                    ax[k, 0].set_ylim(0,ylim)
                    ax[k, 0].set_xlim(time_min,time_max)
                    ax[k, 0].xaxis.set_major_formatter(xfmt)
                    #ax[k, 0].set_title("client " + str(clientrank) + " with IP = " + clientIP + " and quality = " + str(key) + title2 , fontsize=_TITLE_SIZE)
                    ax[k, 0].set_title("fragment quality = " + str(key) , fontsize=_TITLE_SIZE)
                    ax[k, 0].plot(times[key], bandwidths[key], marker =_TIMEPLOT_MARKERS[k], markersize=1, 
                              label=clientIP, color=_FRAGMENTQUALITY_COLORS[key], linestyle="None")  

                    k += 1  

                createfolder(folderpath)  
                plt.tight_layout()

                pdfpage = PdfPages(folderpath + title + "-" + str(clientrank) + ".pdf")
                pdfpage.savefig( bbox_inches="tight")
                pdfpage.close()

                plt.savefig(folderpath + title + "-" + str(clientrank) + ".png", bbox_inches="tight")
                plt.close()
    createfolder(folderpath)
    if not plotfragmentquality:
        plt.tight_layout()

        pdfpage = PdfPages(folderpath + title + ".pdf")
        pdfpage.savefig( bbox_inches="tight")
        pdfpage.close()

        plt.savefig(folderpath + title + ".png", bbox_inches="tight") 

    
        plt.close()
    return
def passivetimeseries_usingbandwidthseparatedflows_plot(results, title, folderpath, ylim, title2=""):
    legendlabels = []
    ytick_labels = []
    ytick_positions = []
    
    time_min = None
    time_max = None
    xvalues=[]
    yvalues = []
    titles =[]
    clientnum = 1
    for clientIP, clientresults in results.items():
        times = OrderedDict()
        bandwidths = OrderedDict()

        for elem in clientresults:
            if elem["clientPort"] not in times:
                times[elem["clientPort"]] = []
                bandwidths[elem["clientPort"]] = []

            times[elem["clientPort"]].append (elem["timestamp"])
            bandwidths[elem["clientPort"]].append(elem["bandwidthMbps"])

            if time_min == None:
                time_min = elem["timestamp"]
                time_max = elem["timestamp"]

            if time_min > elem["timestamp"]:
                timemin = elem["timestamp"]
            if time_max < elem["timestamp"]:
                time_max = elem["timestamp"]

        #xvalues.append(times)
        #yvalues.append(bandwidths)  
        #titles.append(clientIP) 
        colsnum = 2
        rowsnumber = int(math.ceil(1.0 * len(times)/colsnum))
        plt, ax, xfmt = passivetimeseries_usingbandwidth_createplot(colsnumber=colsnum, plottitle=title, 
                                                                    rowsnumber=rowsnumber)

        #k = 0
        x = 0
        y = 0
        counter = 1
        print("columns: " + str(colsnum)  + ", rows: " + str(int(math.ceil(1.0 * len(times)/colsnum))))
        for key in times: #for each sublot
            print ("plot[" + str(x) + ", " + str(y) + "]")
          
            if len(times[key]) != 0:
                ax[x, y].tick_params(axis="both", labelsize=_TICK_SIZE)
                ax[x, y].set_xlabel("t", fontsize=_XLABEL_SIZE)
                ax[x, y].set_ylabel("Mbps", fontsize=_YLABEL_SIZE)
                ax[x, y].set_ylim(0,ylim)
                ax[x, y].set_xlim(time_min,time_max)
                #ax[x, y].set_title(str(counter) + " of " + str(len(times)) + " with IP = " + clientIP + " and port = " + \
                #                   str(key) + "___" + str(datetime.datetime.now()) + title2)
                if title2 != "":
                    title2 = "_" + title2
                ax[x, y].set_title(str(counter) + " of " + str(len(times)) + " with IP = " + clientIP + " and port = " + \
                                   str(key)  + title2, fontsize=_TITLE_SIZE)
                ax[x, y].xaxis.set_major_formatter(xfmt)
                ax[x, y].plot(times[key], bandwidths[key], marker =_TIMEPLOT_MARKERS[x+y], markersize=3, 
                              color=_TIMEPLOT_COLORS[x+y], linestyle="None")   
            x += 1
            counter += 1    
            if x == rowsnumber:
                x = 0
                y +=1  

        print (counter)
        plotsnum = rowsnumber * colsnum
        if counter <= plotsnum:
            #remove empty subplots
            while True:
                print ("delete plot[" + str(x) + ", " + str(y) + "]")
                plt.delaxes(ax[x, y])

                x += 1
                counter += 1    
                if x == rowsnumber:
                    x = 0
                    y +=1 
                
                if counter > plotsnum:
                    break

        createfolder(folderpath)

        plt.tight_layout()

        pdfpage = PdfPages(folderpath + title + "-" + str(clientnum) + ".pdf")
        pdfpage.savefig( bbox_inches="tight")
        pdfpage.close()

        plt.savefig(folderpath + title + "-" + str(clientnum) + ".png", bbox_inches="tight") 
        clientnum += 1

        plt.close()
    return

def passivetimeseries_usingbandwidth(config_parser, section, mode, direction, connectiontype, ylim, server, ncol, 
                                      legendypos, logger, bucketsize_microsec=None): #edgeserver=edge/cloud
    assert mode == "self" or mode == "mim"
    assert direction == "downlink"
    assert server == "edge" or server == "cloud"

    clientnumberlist = getclientnumberlist(config_parser=config_parser, section=section, conntype=connectiontype)
    dashfileslist = getdashfilelist(config_parser=config_parser, section=section)
    noiselist = getnoiselist(config_parser=config_parser, section=section)
    evaluate_fragmentquality=config_parser.getboolean(section, "evaluate_fragmentquality")

    logger.debug("clientnumberlist " + str(clientnumberlist))
    logger.debug("dashfileslist " + str(dashfileslist))
    logger.debug("noiselist " +  str(noiselist))
    logger.debug("mode " + mode)

    print("clientnumberlist " + str(clientnumberlist))
    print("dashfileslist " + str(dashfileslist))
    print("noiselist " +  str(noiselist))
    print("mode " + mode)
    print("bucket size: " + str(bucketsize_microsec))
    for clientnumber in clientnumberlist:
        for noise in noiselist:
            for dashfile in dashfileslist:        
                title  = "timeseries_" + str(mode) + "-" + str(direction) + "-" + str(dashfile) + str(clientnumber) + "-" 
                title += str(noise) + "-" + str(server) + "_plot2"

                filename  = "csv/passive/sorted/" + mode + "-bandwidth-" + direction + "-" + connectiontype + "-"
                filename += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename += config_parser.get(section, "from_passive") + "-" 
                filename += config_parser.get(section, "to_passive") + "_SORTED.csv"

                folderpath = "time/bandwidth/" + str(connectiontype) + "/"
                
                if mode == "self":
                    ret = readbandwidthvalues_self_timeplot(config_parser=config_parser, section=section,
                                                            inputfile=filename,  segment=server, 
                                                            conntype=connectiontype, logger=logger)
                    
                    
                    passivetimeseries_usingbandwidth_plot(results=ret, title=title, folderpath=folderpath, ylim=ylim)
                    
                    if evaluate_fragmentquality:
                        passivetimeseries_usingbandwidth_plot(results=ret, title=title + "_fragmentquality", folderpath=folderpath, 
                                                          ylim=ylim, plotfragmentquality=True)

                    passivetimeseries_usingbandwidthseparatedflows_plot(results=ret, title=title, 
                                                          folderpath=folderpath, ylim=ylim)
                else: 
                    title2 = "_" + str(1.0 * bucketsize_microsec / 1000000) + "s"
                    ret = readbandwidthvalues_mim_timeplot_usingfixbuckets(config_parser=config_parser, 
                                                           section=section, inputfile=filename, segment=server, 
                                                           conntype=connectiontype, logger=logger,
                                                           bucketsize_microsec=bucketsize_microsec)
                    #plt, ax, xfmt = passivetimeseries_usingbandwidth_createplot(colsnumber=1, 
                    #                    plottitle=title + "_" + str(1.0 * bucketsize_microsec / 1000000) + "s", 
                    #                    rowsnumber=len(ret))
                    passivetimeseries_usingbandwidth_plot(results=ret, title=title, 
                                                        folderpath=folderpath, ylim=ylim, title2=title2)

                    plt.close()
                    
                    
                    

def passivetimeseries_usingclientports_plot(plt, ax, xfmt, results, title, folderpath, ylim):
    legendlabels = []
    ytick_labels = []
    ytick_positions = []
    
    time_min = None
    time_max = None
    xvalues=[]
    yvalues = []
    titles =[]
    for clientIP, clientresults in results.items():
        times = []
        clientportsnumber = []

        clientportslist =[]
        first_timestamp = None
        
        for elem in clientresults:
            if time_min == None:
                time_min = elem["timestamp"]
                time_max = elem["timestamp"]
                
            if time_min > elem["timestamp"]:
                timemin = elem["timestamp"]
            if time_max < elem["timestamp"]:
                time_max = elem["timestamp"]


            if first_timestamp == None:
                first_timestamp = elem["timestamp"]
                clientportslist.append(elem["clientPort"])
                continue
                
            if (elem["timestamp"] - first_timestamp).total_seconds() < _BUCKETSIZE_SEC_PORTS:
                if elem["clientPort"] not in clientportslist:
                    clientportslist.append(elem["clientPort"])
                continue 
            
            times.append(elem["timestamp"])
            clientportsnumber.append(len(clientportslist))

            first_timestamp = elem["timestamp"]
            clientportslist = [elem["clientPort"]]
        
        xvalues.append(times)
        yvalues.append(clientportsnumber)
        titles.append(clientIP)
        

    for k in range(0, len(xvalues)): #for each sublot
        if len(xvalues[k]) != 0:
            ax[k, 0].tick_params(axis="both", labelsize=_TICK_SIZE)
            ax[k, 0].set_xlabel("t", fontsize=_XLABEL_SIZE)
            ax[k, 0].set_ylabel("Mbps", fontsize=_YLABEL_SIZE)
            ax[k, 0].set_ylim(0,ylim)
            ax[k, 0].set_xlim(time_min,time_max)
            #ax[k, 0].set_title("client " + str(k + 1) + "- with IP = " + titles[k] + "___" + \
            #                   str(datetime.datetime.now()))
            ax[k, 0].set_title("client " + str(k + 1) + "- with IP = " + titles[k], fontsize=_TITLE_SIZE)
            ax[k, 0].xaxis.set_major_formatter(xfmt)
            ax[k, 0].plot(xvalues[k], yvalues[k], marker =_TIMEPLOT_MARKERS[k], markersize=3, 
                        label=titles[k], color=_TIMEPLOT_COLORS[k], linestyle="None")           

    createfolder(folderpath)

    plt.tight_layout()

    pdfpage = PdfPages(folderpath + title + ".pdf")
    pdfpage.savefig( bbox_inches="tight")
    pdfpage.close()

    plt.savefig(folderpath + title + ".png", bbox_inches="tight") 
    return

def passivetimeseries_usingclientports(config_parser, section, mode, direction, connectiontype, ylim, server, ncol, 
                                       legendypos, logger): #edgeserver=edge/cloud
    assert mode == "self"
    clientnumberlist = getclientnumberlist(config_parser=config_parser, section=section, conntype=connectiontype)
    dashfileslist = getdashfilelist(config_parser=config_parser, section=section)
    noiselist = getnoiselist(config_parser=config_parser, section=section)

    logger.debug("clientnumberlist" + str(clientnumberlist))
    logger.debug ("dashfileslist" + str(dashfileslist))
    logger.debug ("noiselist" +  str(noiselist))

    for clientnumber in clientnumberlist:
        for noise in noiselist:
            for dashfile in dashfileslist:        
                title  = str(mode) + "-" + str(direction) + "-" + str(dashfile) + str(clientnumber) + "-" 
                title += str(noise) + "-" + str(server) + "_plotclientports"

                filename  = "csv/passive/sorted/" + mode + "-bandwidth-" + direction + "-" + connectiontype + "-"
                filename += str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" 
                filename += config_parser.get(section, "from_passive") + "-" 
                filename += config_parser.get(section, "to_passive") + "_SORTED.csv"

                folderpath = "time/bandwidth/" + str(connectiontype) + "/"
                
                ret = readbandwidthvalues_self_timeplot(config_parser=config_parser, section=section, inputfile=filename, 
                                                        segment=server, conntype=connectiontype, logger=logger)
                plt, ax, xfmt = passivetimeseries_usingbandwidth_createplot(colsnumber=1, plottitle=title, 
                                                                             rowsnumber=len(ret))
                
                #passivetimeseries_usingbandwidth_plot(plt=plt, ax=ax, xfmt=xfmt, results=ret, title=title, 
                #                                       folderpath=folderpath, ylim=ylim)
                passivetimeseries_usingclientports_plot(plt=plt, ax=ax, xfmt=xfmt, results=ret, title=title, 
                                                        folderpath=folderpath, ylim=ylim)
                plt.close()