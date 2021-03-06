from datetime import datetime, time, date
from collections import OrderedDict
import numpy as np
import scipy.stats
import os
import errno



def checkdir(directory):
    try:
        os.makedirs(directory)
    except OSError as e:
        if e.errno != errno.EEXIST:
            raise



def plotline_simplebandwidth(plt, points, s, label, filename = None, noise=None):
    x_values = []
    y_values = []

    
    for i in range (0, len(points)):     
        if filename == None or (points[i].dashfilename == filename.strip() and int(noise) == int(points[i].noise)):
            x_values.append(points[i].x)
            y_values.append(points[i].y)
            #print "point: " + str(noise) + str(points[i].noise)
        
    plt.plot(x_values, y_values, s, label = label)





def computeBandwidth_groupedbyday(points, dates, noise):
    values = {}
    ret_x = []
    for i in range(0, len(dates)):
        dates[i] = dates[i].strip()
        values[dates[i]] = []
        ret_x.append(dates[i].split('-')[1] + "-" + dates[i].split('-')[2])

    for i in range (0, len(points)):
        if points[i].noise != noise:
            continue

        index = -1
        for j in range (0, len(dates)):
            start = datetime.combine(datetime.strptime(dates[j], "%Y-%m-%d"), time(0, 0, 0))
            stop = datetime.combine(datetime.strptime(dates[j], "%Y-%m-%d"), time(23, 59, 59))

            if points[i].x >= start and points[i].x <= stop:
                index = j
                break

        assert index != -1
        values[dates[index]].append(points[i].y)

        #print str (points[i].y) + "-" +  str (points[i].x) +"     -> " + str(dates[index]) 
        #print values

    #print values
    ret_y = []
    error_y = []
    
    for i in range(0, len(dates)):
        if len (values[dates[i]]) == 0:
            ret_y.append(0)
            error_y.append(0)
            continue
            
        mean, error = mean_confidence_interval(values[dates[i]])
        ret_y.append(mean)
        error_y.append(error)

    return ret_x, ret_y, error_y



def mean_confidence_interval(data, confidence=0.95):
    a = 1.0 * np.array(data)
    n = len(a)
    
    if n <= 1:
        return np.mean(a), 0
    m, se = np.mean(a), scipy.stats.sem(a)

    h = se * scipy.stats.t.ppf((1 + confidence) / 2., n-1)

    return m, h



def computeyvalues_groupedbyweekdayandintervals(points, day, start_intervals, stop_intervals, noise):
    y_values = {}
    for i in range(0, len(start_intervals)):
        y_values[str(start_intervals[i]) + "-" + str(stop_intervals[i])] = []

    for i in range (0, len(points)):

        if  points[i].x.weekday() != day:
            continue
        if points[i].noise != noise:
            continue


        time = points[i].x.time()
        index = -1
        for j in range (0, len(start_intervals)):
            if time >= start_intervals[j] and time <= stop_intervals[j]:
                index = j

        #print str (points[i].y) + "     -> " + str(time) + "  [" + str(start_intervals[index]) + "," + str(stop_intervals[index]) + "]"
        y_values[str(start_intervals[index]) + "-" + str(stop_intervals[index])]. append(points[i].y)


    # print y_values
    ret = []
    for i in range(0, len(start_intervals)):
        if len (y_values[str(start_intervals[i]) + "-" + str(stop_intervals[i])]) == 0:
            ret.append(0)
            continue
            
        ret.append(np.mean(y_values[str(start_intervals[i]) + "-" + str(stop_intervals[i])]))

    #print ret
    return ret



def compute_bandwidthhistogram(segment, noise):
    ret = []
    for value in segment:
        if value.noise != noise:
            #print str(value.y) + " - " + str(value.y) + " CONTINUE"
            continue

        #print str(value.noise) + "==" + str(value.noise)
        ret.append(value.y)

    #print ret
    return ret


def processvalues_noiseandfilegroupedboxplot(client_server, dashfilename, clientnumberlist, noise):
    assert len(client_server) != 0
    assert len(client_server) != 0
    
    values = OrderedDict()
    
    for measure in client_server:        
        

        if int(measure.noise) != int(noise):
            continue
        if str(measure.dashfilename.strip()) != str(dashfilename.strip()):
            continue

        #print str(measure.dashfilename.strip()) + "-" + str(dashfilename.strip())
        #print str (measure.noise) + "-" + str(noise)

        if values.get(measure.startexperiment_timestamp, None) == None:
            values[measure.startexperiment_timestamp] = [[] for k in range(0, len(clientnumberlist))]
            print measure.startexperiment_timestamp
            print len(values[measure.startexperiment_timestamp])

        values[measure.startexperiment_timestamp][clientnumberlist.index(int(measure.numberofclients))].append(measure.y)
        #values[measure.startexperiment_timestamp][int(measure.numberofclients)-1].append(measure.y)
        
 


    
    return values