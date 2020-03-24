import matplotlib.pyplot as plt
import matplotlib.mlab as mlab
import matplotlib.text as txt
import math
import numpy as np
import random
import colors

from datetime import time, datetime, timedelta, date
from Measure import Measure
from bandwidth_plot_utils import checkdir, plotline_simplebandwidth, computeBandwidth_groupedbyday
from bandwidth_plot_utils import computeyvalues_groupedbyweekdayandintervals, compute_bandwidthhistogram



plt.style.use('seaborn-whitegrid')
np.seterr(over="raise")



def simplebandwidth_lineplot(clientT_observerT, clientT_observerR, observerT_remoteR, title, labels):
    plt.xlabel("Time")
    plt.ylabel("Bandwidth Mbps")
    plt.title(title)

    plotline_simplebandwidth(plt, clientT_observerT, 'o', labels[0])
    plotline_simplebandwidth(plt, clientT_observerR, '+',  labels[1])
    plotline_simplebandwidth(plt, observerT_remoteR, '-', labels[2])

    plt.legend()   
    plt.gcf().autofmt_xdate()


    checkdir("bandwidth/simple/")
    plt.savefig("bandwidth/simple/" + title + ".png")

    plt.close()


def passivebandwidth_lineplot(clientT_observerT, clientT_observerR, title, labels, dashfilelist, noise):
    plt.xlabel("Time")
    plt.ylabel("Bandwidth Mbps")
    plt.title(title)

    for filename in dashfilelist:
        plotline_simplebandwidth(plt, clientT_observerT, 'o', labels[0] + filename, filename, int(noise))
        plotline_simplebandwidth(plt, clientT_observerR, '+',  labels[1] + filename, filename, int(noise))

        print filename.strip() + " plotted (noise = " + str(noise) + ")"
        

    plt.legend()   
    plt.gcf().autofmt_xdate()


    checkdir("passivebandwidth/simple/")
    plt.savefig("passivebandwidth/simple/" + title + ".png")

    plt.close()



def bandwidth_grouped(start_intervals, duration_min, clientT_observerT, clientT_observerR, observerT_remoteR, 
                      title, labels, dates, config_parser):
    start_time = []
    stop_time = []
    xlabels = []

    start_noise = int(config_parser.get("active_experiment_params", "start_noise"))
    stop_noise = int(config_parser.get("active_experiment_params", "stop_noise"))
    step_noise = int(config_parser.get("active_experiment_params", "step_noise"))

    for i in start_intervals:
        x = i.split(":")
        start = time(int(x[0]), int( x[1]), int(x[2]))
        stop = datetime.combine(date.today(), start)  + timedelta(minutes = int(duration_min)) 
        stop=stop.time()
        start_time.append(start)
        stop_time.append(stop)
        xlabels.append(str(start))

    bandwidth_groupedbyday (None, None, observerT_remoteR, title , ["", "", labels[2]], dates, "0M")
    

    for i in range (start_noise, stop_noise + 1, step_noise):    
        bandwidth_groupedbyday (clientT_observerT, clientT_observerR, observerT_remoteR, 
                                title + str(i) + "M Noise" , labels, dates, str(i) + "M")

        
        bandwidth_groupedbyweekdayandintervals(start_time, stop_time, clientT_observerT, 
                                   title + ", client -> observer(Nitos), " + str(i) + "M Noise" ,
                                   str(i) + "M", xlabels)
        bandwidth_groupedbyweekdayandintervals(start_time, stop_time, clientT_observerR, 
                                   title + ", client -> observer(unipi), " + str(i) + "M Noise" , 
                                   str(i) + "M", xlabels)
        bandwidth_groupedbyweekdayandintervals(start_time, stop_time, observerT_remoteR, 
                                   title + ", observer(Nitos) -> Remote, " + str(i) + "M Noise" , 
                                   str(i) + "M", xlabels)
    


def bandwidth_groupedbyday(clientT_observerT, clientT_observerR, observerT_remoteR, title, labels,
                                   dates, noise):
    barwidth = 0.3
    totalbarwidth = 0.0
    lastx = []

    fig = plt.figure()
    ax = fig.add_subplot(111)

    plt.xlabel("Days")
    xtickslabels = None
    plt.ylabel("Bandwidth Mbps")
    plt.title(title)
   

    if clientT_observerT != None:
        client_observer_testbed_labels, client_observer_testbed_y, client_observer_testbed_error  = computeBandwidth_groupedbyday(clientT_observerT, dates, noise)
        client_observer_testbed_x = np.arange(len(client_observer_testbed_y))
        totalbarwidth += barwidth
        lastx = client_observer_testbed_x
        xtickslabels = client_observer_testbed_labels
    if clientT_observerR != None:
        client_observer_unipi_labels, client_observer_unipi_y, client_observer_unipi_error = computeBandwidth_groupedbyday(clientT_observerR, dates, noise)  
        if len(lastx) == 0:
            client_observer_unipi_x = np.arange(len(client_observer_unipi_y))
        else:
            client_observer_unipi_x = [x + barwidth for x in lastx]
        lastx = client_observer_unipi_x

        totalbarwidth += barwidth

        if xtickslabels == None:
            xtickslabels = client_observer_unipi_labels
    if observerT_remoteR != None:
        observer_remote_labels, observer_remote_y, observer_remote_error =  computeBandwidth_groupedbyday(observerT_remoteR, dates, noise)
        if len(lastx) == 0:
            observer_remote_x = np.arange(len(observer_remote_y))
        else:
            observer_remote_x = [x + barwidth for x in lastx]
        
        totalbarwidth += barwidth

        if xtickslabels == None:
            xtickslabels = observer_remote_labels

    if clientT_observerT != None and clientT_observerR != None and observerT_remoteR != None:
        assert len(client_observer_testbed_y) == len(client_observer_unipi_y)
        assert len(client_observer_unipi_y) == len(observer_remote_y)

        assert len(client_observer_testbed_y) == len(client_observer_testbed_error)
        assert len(client_observer_unipi_y) == len(client_observer_unipi_error)
        assert len(observer_remote_y) == len(observer_remote_error)
        for i in range (0, len(client_observer_testbed_y)):
            assert client_observer_testbed_labels == client_observer_unipi_labels
            assert client_observer_unipi_labels == observer_remote_labels

    if clientT_observerT != None:
        ax.bar(client_observer_testbed_x, client_observer_testbed_y, yerr = client_observer_testbed_error, 
                width= barwidth, label = labels[0], color = colors.OBSERVER_CLIENT_TESTBED)
    if clientT_observerR != None:    
        ax.bar(client_observer_unipi_x, client_observer_unipi_y, yerr = client_observer_unipi_error, 
                width= barwidth, label = labels[1], color = colors.OBSERVER_CLIENT_UNIPI) 
    if observerT_remoteR != None:
        ax.bar(observer_remote_x, observer_remote_y, yerr=observer_remote_error, width= barwidth, 
                label = labels[2], color = colors.NITOS_REMOTE)


    xtickposition = 1.0 * np.arange(len(xtickslabels))
    totalbarwidth /= 3
    for i in range(0, len(xtickposition)):
        xtickposition[i] = xtickposition[i] + totalbarwidth
    ax.set_xticks(xtickposition)
    ax.set_xticklabels(xtickslabels)

    box = ax.get_position()
    # Shrink current axis's height by 10% on the bottom
    ax.set_position([box.x0, box.y0 + box.height * 0.1,  box.width, box.height * 0.9])
    ax.legend(loc="upper center", bbox_to_anchor=(0.5, -0.08), fancybox=True, shadow=True, ncol = 1, facecolor = "white")   

    checkdir("bandwidth/grouped_barplot/by_day/")
    plt.savefig("bandwidth/grouped_barplot/by_day/" + title + ".png", dpi = 500)
    plt.close()



def bandwidth_groupedbyweekdayandintervals(start_time, stop_time, segment, title, noise, xlabels):
    plt.xlabel("Time")
    plt.ylabel("Bandwidth Mbps")
    plt.title(title)
    barwidth = 0.1

    plt.xticks(np.arange(len(xlabels)), xlabels)

    
    monday_y = computeyvalues_groupedbyweekdayandintervals(segment, 0, start_time, stop_time, noise)
    tuesday_y = computeyvalues_groupedbyweekdayandintervals(segment, 1, start_time, stop_time, noise)
    wednesday_y = computeyvalues_groupedbyweekdayandintervals(segment, 2, start_time, stop_time, noise)
    thursday_y = computeyvalues_groupedbyweekdayandintervals(segment, 3, start_time, stop_time, noise) 
    friday_y = computeyvalues_groupedbyweekdayandintervals(segment, 4, start_time, stop_time, noise)
    saturday_y = computeyvalues_groupedbyweekdayandintervals(segment, 5, start_time, stop_time, noise)  
    sunday_y = computeyvalues_groupedbyweekdayandintervals(segment, 6, start_time, stop_time, noise)

    monday_x = np.arange(len(monday_y))
    tuesday_x = [x + barwidth for x in monday_x]
    wednesday_x = [x + barwidth for x in tuesday_x]
    thursday_x = [x + barwidth for x in wednesday_x]
    friday_x = [x + barwidth for x in thursday_x]
    saturday_x = [x + barwidth for x in friday_x] 
    sunday_x = [x + barwidth for x in saturday_x]    

    plt.bar(monday_x, monday_y, width= barwidth, label = "monday")
    plt.bar(tuesday_x, tuesday_y, width= barwidth, label = "tuesday")
    plt.bar(wednesday_x, wednesday_y, width= barwidth, label = "wednesday")
    plt.bar(thursday_x, thursday_y, width= barwidth, label = "thursday")
    plt.bar(friday_x, friday_y, width= barwidth, label = "friday")
    plt.bar(saturday_x, saturday_y, width= barwidth, label = "saturday")
    plt.bar(sunday_x, sunday_y, width= barwidth, label = "sunday")


    plt.legend()

    checkdir("bandwidth/grouped_barplot/by_week_day_and_intervals/")
    plt.savefig("bandwidth/grouped_barplot/by_week_day_and_intervals/" + title + ".png", dpi = 500)
    
    plt.close()



def bandwidth_raw(clientT_observerT, clientT_observerR, observerT_remoteR,  observerR_remoteR,
                      title, config_parser):
    start_noise = int(config_parser.get("active_experiment_params", "start_noise"))
    stop_noise = int(config_parser.get("active_experiment_params", "stop_noise"))
    step_noise = int(config_parser.get("active_experiment_params", "step_noise"))

    for i in range (start_noise, stop_noise + 1, step_noise):
        if len(clientT_observerT) != 0:
            bandwidth_histogram(clientT_observerT, str(i) + "M",  
                            "client-observer(nitos)" + title + str(i) + "M Noise", 
                            "client -> Observer(Nitos)", colors.OBSERVER_CLIENT_TESTBED)
        else:
            colors.WARNING + "len(clientT_observerT) is 0"
        if len(clientT_observerR) != 0:
            bandwidth_histogram(clientT_observerR, str(i) + "M",  
                            "client-observer(unipi)" + title + str(i) + "M Noise", 
                            "client -> Observer(Unipi))", colors.OBSERVER_CLIENT_UNIPI)
        else:
            colors.WARNING + "len(clientT_observerR) is 0"
        if len(observerT_remoteR) != 0:
            bandwidth_histogram(observerT_remoteR, str(i) + "M",  
                            "observer-remote" + title + str(i) + "M Noise", 
                            "Observer -> Remote", colors.NITOS_REMOTE)
        else:
            colors.WARNING + "len(observerT_remoteR) is 0"
        if len(observerR_remoteR) != 0:
            bandwidth_histogram(observerR_remoteR, str(i) + "M",  
                            "observer-remote" + title + str(i) + "M Noise", 
                            "Observer -> Remote", colors.NITOS_REMOTE)
        else:
            colors.WARNING + "len(observerR_remoteR) is 0"




def bandwidth_histogram(segment, noise, title, legendlabels, histcolor, minbin_number = 1000, maxbin_number = 1001, step = 10):
    assert minbin_number <= maxbin_number
    assert step > 0

    print "bandwidth histogram: noise = " + str(noise) 

    basedir = "bandwidth/bandwidth_histogram/"
    if "TCPBandwidth" in title:
        basedir = basedir + "TCPBandwidth"
    elif "UDPBandwidth" in title:
        basedir = basedir + "UDPBandwidth"
    else:
        exit(-1)

    if "Upstream" in title:
        basedir = basedir + "Upstream/"
    elif "Downstream" in title:
        basedir = basedir + "Downstream/"
    else:
        exit(-1)

    x = compute_bandwidthhistogram(segment, noise)
    if len(x) == 0:
        print colors.WARNING + "\t\tempty set. returning" + colors.RESET
        return

    
    x.sort(reverse = True)
    print "max x" +  str(x[0])
    maxx = x[3] + 1
    print "maxx" +  str(maxx)

    plt.xlim(0, maxx)

    for i in range (minbin_number, maxbin_number, step):
        plt.ylabel("Number of occurrences")
        plt.xlabel("Mbps \nNumber of bins = " + str(i))
        plt.title(title)


        binsize = 1.0 * maxx/i
        print binsize
        binslist = []
        p = binsize/2


        while (p < maxx):
            binslist.append(p)
            p = p + binsize
            #print str(p) +" of " + str(maxx)
    
        print "plotting"  + basedir + title + "(" + str(i) + "bins).png"
        print "\t\tlen(x)"  + str(len(x))
        
        plt.hist(x, bins=binslist, color = histcolor)
        checkdir(basedir)
        plt.savefig(basedir + title + "(" + str(i) + "bins).png", dpi = 500)
        
        plt.close()
    