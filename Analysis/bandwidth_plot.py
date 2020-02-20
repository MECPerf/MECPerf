import matplotlib.pyplot as plt
import matplotlib.text as txt
import numpy as np
import random
import colors

from datetime import time, datetime, timedelta, date
from Measure import Measure
from bandwidth_plot_utils import checkplot, plotline_simplebandwidth, computeBandwidth_groupedbyday
from bandwidth_plot_utils import computeyvalues_groupedbyweekdayandintervals



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


    checkplot("bandwidth/simple/")
    plt.savefig("bandwidth/simple/" + title + ".png")

    plt.close()



def bandwidth_grouped(start_intervals, duration_min, clientT_observerT, clientT_observerR, observerT_remoteR, 
                      title, labels, dates):
    start_time = []
    stop_time = []
    xlabels = []

    
    for i in start_intervals:
        x = i.split(":")
        start = time(int(x[0]), int( x[1]), int(x[2]))
        stop = datetime.combine(date.today(), start)  + timedelta(minutes = int(duration_min)) 
        stop=stop.time()
        start_time.append(start)
        stop_time.append(stop)
        xlabels.append(str(start))

    bandwidth_groupedbyday (None, None, observerT_remoteR, title , ["", "", labels[2]], dates, "0M")

    for i in range (0, 51, 10):
    
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
    ax.legend(loc="upper center", bbox_to_anchor=(0.5, -0.08), fancybox=True, shadow=True, ncol = 3, facecolor = "white")   

    checkplot("bandwidth/grouped_barplot/by_day/")
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

    checkplot("bandwidth/grouped_barplot/by_week_day_and_intervals/")
    plt.savefig("bandwidth/grouped_barplot/by_week_day_and_intervals/" + title + ".png", dpi = 500)
    
    plt.close()
