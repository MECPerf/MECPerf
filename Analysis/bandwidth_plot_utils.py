from datetime import datetime, time, date
import numpy as np
import scipy.stats
import os
import errno



def checkplot(directory):
    try:
        os.makedirs(directory)
    except OSError as e:
        if e.errno != errno.EEXIST:
            raise



def plotline_simplebandwidth(plt, points, s, label):
    x_values = []
    y_values = []
    for i in range (0, len(points)):
        x_values.append(points[i].x)
        y_values.append(points[i].y)

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

    print ret_x 
    print ret_y
    print error_y

    return ret_x, ret_y, error_y





def mean_confidence_interval(data, confidence=0.95):
    a = 1.0 * np.array(data)
    n = len(a)
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