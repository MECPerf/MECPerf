import ConfigParser
import os
import sys
import errno
import matplotlib.pyplot as plt
import matplotlib
import numpy as np

from matplotlib.backends.backend_pdf import PdfPages
from collections import OrderedDict

from readcsv import readvalues_activelatencyboxplot, readvalues_activebandwidthboxplot
from readcsv import readvalues_activebandwidthlineplot, readvalues_noisegrouped



BOXPLOT_COLORS=['#F8B195', "#355C7D", '#C06C84', '#F67280', '#99B898', '#A8E6CE', '#E84A5F', '#A7226E', 
                '#F7DB4F', "#FC913A", "#1bdc9d", "#9c5a4c", "#9c4c84", "#4c999c"]
WARNING = '\033[93m'
FAIL = '\033[91m'
RESET = '\033[0m'
#matplotlib.interactive(True)



def createfolder(directoryname):
    try:
        os.makedirs(directoryname)
    except OSError as error:
        if error.errno != errno.EEXIST:
            print (error)
            sys.exit(0)



def bandwidthboxplot_noisegrouped(config_parser, mode, direction, connectiontype, ylim, edgeserver, segmentgrouped=False):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber").split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
     
    values = OrderedDict()

    for dashfile in dashfileslist:
        title = mode + "-" + direction + "-" + connectiontype + "-" + connectiontype + "-bandwidth-" + dashfile 
        legendlabels = []

        if edgeserver and not segmentgrouped:
            title += "-edge"
        elif not edgeserver and not segmentgrouped:
            title += "-remote"

        for noise in noiselist:
            values[noise] =[]

            if not segmentgrouped:
                for clientnumber in clientnumberlist:
                    filename = "csv/passive/" + mode + "-bandwidth-" + direction + "-" + connectiontype
                    filename += "-" + str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" \
                             + config_parser.get("experiment_conf", "from_passive") + "-" \
                             + config_parser.get("experiment_conf", "to_passive") + ".csv"

                    values[noise].append(readvalues_noisegrouped(config_parser, int(clientnumber), noise, 
                                                                        filename, edgeserver, connectiontype)) 
            elif segmentgrouped:
                for clientnumber in clientnumberlist:
                    filename = "csv/passive/" + mode + "-bandwidth-" + direction + "-" + connectiontype \
                             + "-" + str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_" \
                             + config_parser.get("experiment_conf", "from_passive") + "-" \
                             + config_parser.get("experiment_conf", "to_passive") + ".csv"
                
                    values[noise].append(readvalues_noisegrouped(config_parser, int(clientnumber), noise, 
                                                                    filename, True, connectiontype))
                    values[noise].append(readvalues_noisegrouped(config_parser, int(clientnumber), noise, 
                                                                    filename, False, connectiontype))
                

        if not segmentgrouped:
            folderpath = "bandwidthplot/noisegrouped/" + connectiontype + "/"
            for i in range (0, len(clientnumberlist)):
                legendlabels.append("Number of clients = " + str(clientnumberlist[i]))
        if segmentgrouped:
            folderpath = "bandwidthplot/noiseandsegmentgrouped/" + connectiontype + "/"
            for i in range (0, len(clientnumberlist)):
                legendlabels.append("Number of clients = " + str(clientnumberlist[i]) + "_edge")
                legendlabels.append("Number of clients = " + str(clientnumberlist[i]) + "_remote")

                   
        if len(values) == 0:
            print WARNING + "No data for file " + dashfile + RESET
            continue
        
        #print folderpath
        createfolder(folderpath)
        ylabel = "Bandwidth (Mbps)"
        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel)



def latencyboxplot_active_commandgrouped(config_parser, direction, connectiontype, ylim):
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    if connectiontype == "wifi":
        dateslist = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    elif connectiontype == "lte":
        dateslist = config_parser.get("experiment_conf", "dates_activelte").split(",")
     
     
    values = OrderedDict()
    title = direction + "-" + connectiontype
    folderpath = "latency/active/boxplot_command/" + connectiontype + "/"
    ylabel = "Latency (ms)"
    legendlabels = []
    if direction == "Upstream":
        legendlabels.append("TCPRTT: Client -> Observer (Nitos)")
        legendlabels.append("UDPRTT: Client -> Observer (Nitos)")
        legendlabels.append("TCPRTT: Client -> Observer (Unipi)")
        legendlabels.append("UDPRTT: Client -> Observer (Unipi)")
        legendlabels.append("TCPRTT: Observer (Nitos) -> Remote server (Unipi)")
        legendlabels.append("UDPRTT: Observer (Nitos) -> Remote server (Unipi)")
    elif direction == "Downstream":
        legendlabels.append("TCPRTT: Observer (Nitos) -> Client")
        legendlabels.append("UDPRTT: Observer (Nitos) -> Client")
        legendlabels.append("TCPRTT: Observer (Unipi) -> Client")
        legendlabels.append("UDPRTT: Observer (Unipi) -> Client")
        legendlabels.append("TCPRTT: Remote server (Unipi) -> Observer (Nitos)")
        legendlabels.append("UDPRTT: Remote server (Unipi) -> Observer (Nitos)")
    else:
        print "unknown direction"
        sys.exti(0)

    for noise in noiselist:
        values[noise] = []

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

                   
    if len(values) == 0:
        print WARNING + "No data for file " + filename + RESET
        return

    createfolder(folderpath)

    
    drawboxplot(folderpath, title+"_2", values, legendlabels, ylim, ylabel)

    ylim += 300
    show_fliers = True
    drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, show_fliers)
    



def bandwidthboxplot_active_conntypegrouped(config_parser, command, direction, ylim):
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    dateslist_wifi = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    dateslist_lte = config_parser.get("experiment_conf", "dates_activelte").split(",")
     
    values = OrderedDict()
    title = command + "-" + direction
    folderpath = "bandwidth/active/boxplot_conntype/"
    ylabel = "Bandwidth (Mbps)"
    legendlabels = []
    if direction == "Upstream":
        legendlabels.append("wifi: Client -> Observer (Nitos)")
        legendlabels.append("lte: Client -> Observer (Nitos)")
        legendlabels.append("wifi: Client -> Observer (Unipi)")
        legendlabels.append("lte: Client -> Observer (Unipi)")
        legendlabels.append("wifi: Observer (Nitos) -> Remote server (Unipi)")
        legendlabels.append("lte: Observer (Nitos) -> Remote server (Unipi)")
    elif direction == "Downstream":
        legendlabels.append("wifi: Observer (Nitos) -> Client")
        legendlabels.append("lte: Observer (Nitos) -> Client")
        legendlabels.append("wifi: Observer (Unipi) -> Client")
        legendlabels.append("lte: Observer (Unipi) -> Client")
        legendlabels.append("wifi: Remote server (Unipi) -> Observer (Nitos)")
        legendlabels.append("lte: Remote server (Unipi) -> Observer (Nitos)")
    else:
        print "unknown direction"
        sys.exti(0)

    for noise in noiselist:
        values[noise] = []

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

                   
    if len(values) == 0:
        print WARNING + "No data for file " + filename + RESET
        return

    createfolder(folderpath)

    drawboxplot(folderpath, title+"_2", values, legendlabels, ylim, ylabel)

    ylim += 300
    show_fliers = True
    drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, show_fliers)
    

def latencyboxplot_active(config_parser, command, direction, connectiontype, ylim):
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    if connectiontype == "wifi":
        dateslist = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    elif connectiontype == "lte":
        dateslist = config_parser.get("experiment_conf", "dates_activelte").split(",")
     
     
    values = OrderedDict()

    title = command + "-" + direction + "-" + connectiontype
    ylabel = "Latency (ms)"


    legendlabels = []
    if direction == "Upstream":
        legendlabels.append("Client -> Observer (Nitos)")
        legendlabels.append("Client -> Observer (Unipi)")
        legendlabels.append("Observer (Nitos) -> Remote server (Unipi)")
    elif direction == "Downstream":
        legendlabels.append("Observer (Nitos) -> Client")
        legendlabels.append("Observer (Unipi) -> Client")
        legendlabels.append("Remote server (Unipi) -> Observer (Nitos)")
    else:
        print "unknown direction"
        sys.exti(0)


    for noise in noiselist:
        values[noise] = []

        filename = "csv/active/" + command + "-" + direction + "-" + connectiontype + "-noise" + noise + "_"         
        filename +=  dateslist[0].strip() + "-" + dateslist[-1].strip() + ".csv"
        folderpath = "latency/active/boxplot/" + connectiontype + "/"
                            
        values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
        values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
        values[noise].append(readvalues_activelatencyboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))   

                   
    if len(values) == 0:
        print WARNING + "No data for file " + filename + RESET
        return

    createfolder(folderpath)

    drawboxplot(folderpath, title+"_2", values, legendlabels, ylim, ylabel)

    ylim += 300
    show_fliers = True
    drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, show_fliers)
    



def bandwidthboxplot_active(config_parser, command, direction, connectiontype, ylim):
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    if connectiontype == "wifi":
        dateslist = config_parser.get("experiment_conf", "dates_activewifi").split(",")
    elif connectiontype == "lte":
        dateslist = config_parser.get("experiment_conf", "dates_activelte").split(",")

    values = OrderedDict()
    title = command + "-" + direction + "-" + connectiontype
    folderpath = "bandwidth/active/boxplot/" + connectiontype + "/"
    ylabel = "Bandwidth (Mbps)"


    legendlabels = []
    if direction == "Upstream":
        legendlabels.append("Client -> Observer (Nitos)")
        legendlabels.append("Client -> Observer (Unipi)")
        legendlabels.append("Observer (Nitos) -> Remote server (Unipi)")
    elif direction == "Downstream":
        legendlabels.append("Observer (Nitos) -> Client")
        legendlabels.append("Observer (Unipi) -> Client")
        legendlabels.append("Remote server (Unipi) -> Observer (Nitos)")
    else:
        print "unknown direction"
        sys.exti(0)


    for noise in noiselist:
        values[noise] = []

        filename = "csv/active/" + command + "-" + direction + "-" + connectiontype + "-noise" + noise + "_"         
        filename +=  dateslist[0].strip() + "-" + dateslist[-1].strip() + ".csv"
                            
        values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientNitos"))
        values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "clientUnipi"))
        values[noise].append(readvalues_activebandwidthboxplot(filename, int(noise.replace("M", "")), "NitosUnipi"))   

                   
    if len(values) == 0:
        print WARNING + "No data for file " + filename + RESET
        return

    createfolder(folderpath)

    #draw plot without fliers
    drawboxplot(folderpath, title+"_2", values, legendlabels, ylim, ylabel)

    #drawplot with fliers
    ylim += 300
    show_fliers = True
    drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, show_fliers)
    





def bandwidthplot_fileandsegmentgrouped(config_parser, mode, direction, connectiontype):
    clientnumberlist = config_parser.get("experiment_conf", "clientnumber").split(",")
    dashfileslist = config_parser.get("experiment_conf", "dashfiles").split(",")
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    folderpath = "bandwidthplot/fileandsegmentgrouped/" + connectiontype + "/"
    ylabel = "Bandwidth (Mbps)"

    createfolder(folderpath)
     
    values = OrderedDict()

    for noise in noiselist:
        title = mode + "-" + direction + "-" + connectiontype + "-bandwidth-" + noise 

        legendlabels = []

        for dashfile in dashfileslist:
            values[dashfile] =[]

            for clientnumber in clientnumberlist:
                filename = "csv/passive/" + mode + "-bandwidth-" + direction + "-" + connectiontype + "-" \
                         + str(clientnumber) + "clients-" + dashfile + "-noise" + noise + "_"  \
                         + config_parser.get("experiment_conf", "from_passive") + "-" \
                         + config_parser.get("experiment_conf", "to_passive") + ".csv"
            
                values[dashfile].append(readvalues_noisegrouped(config_parser, int(clientnumber), noise, 
                                                                filename, True))
                values[dashfile].append(readvalues_noisegrouped(config_parser, int(clientnumber), noise, 
                                                                filename, False))        
        for i in range (0, len(clientnumberlist)):
            legendlabels.append("Number of clients = " + str(clientnumberlist[i]) + "_edge")
            legendlabels.append("Number of clients = " + str(clientnumberlist[i]) + "_remote")

                   
        if len(values) == 0:
            print WARNING + "No data for file " + dashfile + RESET
            continue
        
        ylim = 50
        
        showfliers = False
        drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, showfliers)

    

def drawboxplot(folderpath, title, values, legendlabels, ylim, ylabel, show_fliers=False):
    fig = plt.figure(figsize=(15,6))
    ax = plt.axes()
    plt.grid(True, axis="y", color="#dedddc")

    xlabels = []
    xlabelspos = []

    i = 1
    with open(folderpath + title + ". txt", "w") as logfile:
        for key, value in values.items():
            plt.axvline(1.0 * i + len(value), 0, 10, color="#b0b0b0")

            #value = [group # 1: [bar 1 measures], [bar #2 measures], ..., [bar #n measures], ...., group #k:[bar 1 measures], [bar #2 measures], ..., [bar #n measures]]
            boxplotpos = []
            for k in range (0, len(value)):
                boxplotpos.append(i + k)
            
            bp = ax.boxplot(value, positions = boxplotpos, widths = 0.6, patch_artist=True, showfliers=show_fliers,
                            medianprops={"color":"black"}, showmeans=False)

            j=0
            for box in bp["boxes"]:
                plt.setp(box, facecolor=BOXPLOT_COLORS[j], edgecolor='black')
                j += 1
            

            xlabels.append(key)
            xlabelspos.append(1.0* i + (1.0 * len(value)/2) - 0.5)
            i += k + 2

            writelogfile_boxplot(key, logfile, bp, legendlabels, value)
            
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

    plt.xlabel("Cross traffic (Mbit/sec)")
    plt.ylabel(ylabel)

    plt.title(title)

     ax.legend(loc="upper center", bbox_to_anchor=(0.5, -0.08), fancybox=True, shadow=True, ncol = 1, facecolor = "white") 

    #leg=ax.legend(legendlabels, loc="best", frameon=True)
    i=0
    for item in leg.legendHandles:
        item.set_color(BOXPLOT_COLORS[i])
        item.set_linewidth(2.0)
        i += 1
    
    pdfpage = PdfPages(folderpath + title + ".pdf")
    pdfpage.savefig( bbox_inches="tight")
    pdfpage.close()

    #plt.show()
    plt.savefig(folderpath + title + ".png")
    plt.close()



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



def activebandwidth_lineplot(config_parser, command, direction, conn):
    noiselist = config_parser.get("experiment_conf", "noise").split(",")
    title = command + "-" + direction + "-" + conn

    if "RTT" in command:
        title+="valoreynonsignificativo"

    #plt.figure()
    plt.xlabel("Time")
    plt.ylabel("Bandwidth Mbps")
    plt.title(title)
     
    clientNitos, clientUnipi, NitosUnipi = readvalues_activebandwidthlineplot(config_parser, command, direction, conn)    

    print "plotting"
    print len(clientNitos["x"])
    print len(clientNitos["y"])

    #plt.plot(clientNitos["x"], clientNitos["y"], 'o', label=clientNitos["legend"])
    #plt.plot(clientUnipi["x"], clientUnipi["y"], '+', label=clientUnipi["legend"])
    #plt.plot(NitosUnipi["x"], NitosUnipi["y"], '-', label=NitosUnipi["legend"])

    #plt.plot(["2020-04-06", "2020-04-07", "2020-04-08"], [1,4,9], marker ='o',  label='line 2')

    x = []
    y = []
    for i in range (0, 2000):
        x.append(clientNitos["x"][i])
        y.append(clientNitos["y"][i])

    plt.plot(x, y, 'o', label=clientNitos["legend"] )
    print "plotted"
    #print clientNitos["y"]
    
    
    plt.legend()   
    plt.gcf().autofmt_xdate()
    #plt.show()

    createfolder("bandwidth/active/simple/")
    plt.savefig("bandwidth/active/simple/" + title + ".png")   

    plt.close()




if __name__ == '__main__':
    #read configuration file
    config_parser = ConfigParser.RawConfigParser()
    config_parser.read("experiments.conf")
 
    '''

    #activebandwidth_lineplot(config_parser, "TCPRTT", "Upstream", "wifi")
    #activebandwidth_lineplot(config_parser, "TCPRTT", "Downstream", "wifi")
    #activebandwidth_lineplot(config_parser, "UDPRTT", "Upstream", "wifi")
    #activebandwidth_lineplot(config_parser, "UDPRTT", "Downstream", "wifi")
    #activebandwidth_lineplot(config_parser, "TCPBandwidth", "Upstream", "wifi")

    #active latency wifi
    ylim = 50
    latencyboxplot_active(config_parser, "TCPRTT", "Upstream", "wifi", ylim)
    latencyboxplot_active(config_parser, "TCPRTT", "Downstream", "wifi", ylim)
    latencyboxplot_active(config_parser, "UDPRTT", "Upstream", "wifi", ylim)
    latencyboxplot_active(config_parser, "UDPRTT", "Downstream", "wifi", ylim)

    #active latency lte
    ylim = 100
    latencyboxplot_active(config_parser, "TCPRTT", "Upstream", "lte", ylim)
    latencyboxplot_active(config_parser, "TCPRTT", "Downstream", "lte", ylim)
    latencyboxplot_active(config_parser, "UDPRTT", "Upstream", "lte", ylim)
    latencyboxplot_active(config_parser, "UDPRTT", "Downstream", "lte", ylim)

    #
    ylim = 50
    latencyboxplot_active_commandgrouped(config_parser, "Upstream", "wifi", ylim)
    latencyboxplot_active_commandgrouped(config_parser, "Downstream", "wifi", ylim)
    ylim = 100
    latencyboxplot_active_commandgrouped(config_parser, "Upstream", "lte", ylim)
    latencyboxplot_active_commandgrouped(config_parser, "Downstream", "lte", ylim)



    #active bandwidth wifi
    ylim = 35
    bandwidthboxplot_active(config_parser, "TCPBandwidth", "Upstream", "wifi", ylim)
    bandwidthboxplot_active(config_parser, "TCPBandwidth", "Downstream", "wifi", ylim)
    ylim = 3000
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Upstream", "wifi", ylim)
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Downstream", "wifi", ylim)

    #active bandwidth lte
    ylim = 35
    bandwidthboxplot_active(config_parser, "TCPBandwidth", "Upstream", "lte", ylim)
    bandwidthboxplot_active(config_parser, "TCPBandwidth", "Downstream", "lte", ylim)
    ylim = 3000
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Upstream", "lte", ylim)
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Downstream", "lte", ylim)



    ylim = 35
    bandwidthboxplot_active_conntypegrouped(config_parser, "TCPBandwidth", "Upstream", ylim)
    bandwidthboxplot_active_conntypegrouped(config_parser, "TCPBandwidth", "Downstream", ylim)
    ylim = 2800
    bandwidthboxplot_active_conntypegrouped(config_parser, "UDPBandwidth", "Upstream", ylim)
    bandwidthboxplot_active_conntypegrouped(config_parser, "UDPBandwidth", "Downstream", ylim)

    '''


    #passive plot

    ylim = 50
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "wifi", ylim, True, False)
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "wifi", ylim, False, False)
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "wifi", ylim, True, True)
    ylim = 100
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "lte", ylim, True, False)
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "lte", ylim, False, False)
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "lte", ylim, True, True)

    ylim = 50
    bandwidthplot_fileandsegmentgrouped(config_parser, "self", "downlink", "wifi")
    ylim = 100
    bandwidthplot_fileandsegmentgrouped(config_parser, "self", "downlink", "lte")
  

    #mim 
    #createcsv(config_parser, mydb, "bandwidth", "mim", "downlink")
    #createcsv(config_parser, mydb, "bandwidth", "mim", "uplink")
    #createcsv(config_parser, mydb, "latency", "mim", "downlink")
    #createcsv(config_parser, mydb, "latency", "mim", "uplink")
