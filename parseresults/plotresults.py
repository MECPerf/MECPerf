import configparser
import logging
from datetime import datetime
from plot_boxplots import bandwidthboxplot_noisegrouped, bandwidthplot_mimfileandsegment, \
        bandwidthboxplot_active_conntypegrouped, bandwidthboxplot_active, bandwidthplot_fileandsegmentgrouped,\
        latencyboxplot_noiseandsegmentmim, bandwidthboxplot_noisemim, bandwidthboxplot_noiseandsegmentmim,\
        latencyboxplot_active_commandgrouped, latencyboxplot_active, latencyboxplot_active_conntypegrouped, \
        mimselfbandwidthboxplot_conntypefileserverpos_xnoise, \
        mimselfbandwidthboxplot_conntypefileserverpos_xclients,  \
        mimselfbandwidthboxplot_conntypeserverposnumclient, readbandwidthvalues_mim_perclient_usingfixbucket,\
        mimselfbandwidthboxplot_conntypeserverposcrosstraffic, bandwidthplot_perclient, latencyboxplot_noiseandsegmentactivemim
from timeplots import passivetimeseries, passivetimeseries_usingbandwidth, passivetimeseries_usingclientports


WARNING = '\033[93m'
FAIL = '\033[91m'
RESET = '\033[0m'

LEGENDYPOS_1LINE = 1.15
LEGENDYPOS_2LINE = 1.25
LEGENDYPOS_4LINE = 1.40
LEGENDYPOS_8LINE = 1.60

print()
logging.basicConfig(filename="logs/"+str(datetime.now())+".log", filemode='w', format='%(name)s - ' +
                    '%(asctime)s - %(levelname)s_\%(filename)s-%(funcName)s(): %(message)s')
logger = logging.getLogger()
logger.setLevel(logging.DEBUG)


def activebandwidth_lineplot(config_parser, section, command, direction, conn):
    noiselist = config_parser.get(section, "noise").split(",")
    title = command + "-" + direction + "-" + conn

    if "RTT" in command:
        title+="valoreynonsignificativo"

    #plt.figure()
    plt.xlabel("Time")
    plt.ylabel("Bandwidth Mbps")
    plt.title(title)
     
    clientNitos, clientUnipi, NitosUnipi = readvalues_activebandwidthlineplot(config_parser, command, direction, conn)    

    print ("plotting")
    print (len(clientNitos["x"]))
    print (len(clientNitos["y"]))

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
    print ("plotted")
    #print clientNitos["y"]
    
    
    plt.legend()   
    plt.gcf().autofmt_xdate()
    #plt.show()

    createfolder("bandwidth/active/simple/")
    plt.savefig("bandwidth/active/simple/" + title + ".png")   

    plt.close()
def plotactivelatency(config_parser, section):
    ylim_wifi = 50
    ylim_lte = 100

    #active latency wifi boxplots
    latencyboxplot_active(config_parser=config_parser, section=section, command="TCPRTT", direction="Upstream", 
                          connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, section=section, command="TCPRTT", direction="Downstream", 
                          connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, section=section, command="UDPRTT", direction="Upstream", 
                          connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, section=section, command="UDPRTT", direction="Downstream", 
                          connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_1LINE)
    '''
    #active latency lte boxplots
    latencyboxplot_active(config_parser=config_parser, section=section, command="TCPRTT", direction="Upstream", 
                          connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, section=section, command="TCPRTT", direction="Downstream", 
                          connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, section=section, command="UDPRTT", direction="Upstream", 
                          connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, section=section, command="UDPRTT", direction="Downstream", 
                          connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_1LINE)
    '''
    
    #wifi
    latencyboxplot_active_commandgrouped(config_parser=config_parser, section=section, direction="Upstream", 
                                         connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_2LINE)
    latencyboxplot_active_commandgrouped(config_parser=config_parser, section=section, direction="Downstream", 
                                         connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_2LINE)
    '''
    #lte
    latencyboxplot_active_commandgrouped(config_parser=config_parser, section=section, direction="Upstream", 
                                         connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_2LINE)
    latencyboxplot_active_commandgrouped(config_parser=config_parser, section=section, direction="Downstream", 
                                         connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_2LINE)
    #wifi&lte
    latencyboxplot_active_commandgrouped(config_parser=config_parser, section=section, direction="Upstream", 
                                         connectiontype="both", ylim=max(ylim_wifi, ylim_lte), 
                                         legendypos=LEGENDYPOS_4LINE)
    latencyboxplot_active_commandgrouped(config_parser=config_parser, section=section, direction="Downstream", 
                                         connectiontype="both", ylim=max(ylim_wifi, ylim_lte), 
                                         legendypos=LEGENDYPOS_4LINE)
    #wifi&lte
    latencyboxplot_active_conntypegrouped(config_parser, section=section, "TCPRTT", "Upstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_wifi, ylim_lte))
    latencyboxplot_active_conntypegrouped(config_parser, section=section, "TCPRTT", "Downstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_wifi, ylim_lte))
    latencyboxplot_active_conntypegrouped(config_parser, section=section, "TCPRTT", "both", ncol=3, 
                                            legendypos=LEGENDYPOS_4LINE, ylim=max(ylim_wifi, ylim_lte))
    latencyboxplot_active_conntypegrouped(config_parser, section=section, "UDPRTT", "Upstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_wifi, ylim_lte))
    latencyboxplot_active_conntypegrouped(config_parser, section=section, "UDPRTT", "Downstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_wifi, ylim_lte))
    latencyboxplot_active_conntypegrouped(config_parser, section=section, "UDPRTT", "both",  ncol=3, 
                                            legendypos=LEGENDYPOS_4LINE, ylim=max(ylim_wifi, ylim_lte))
    '''
def plotactivebandwidth(config_parser, section):
    ylim_TCPwifi = 35
    ylim_TCPlte = 35
    ylim_UDPwifi = 3000
    ylim_UDPlte = 3000

    #active bandwidth wifi
    bandwidthboxplot_active(config_parser, section, "TCPBandwidth", "Upstream", "wifi", ylim_TCPwifi, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, section, "TCPBandwidth", "Downstream", "wifi", ylim_TCPwifi, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, section, "UDPBandwidth", "Upstream", "wifi", ylim_UDPwifi, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, section, "UDPBandwidth", "Downstream", "wifi", ylim_UDPwifi, 
                            legendypos=LEGENDYPOS_1LINE)
    '''
    #active bandwidth lte
    bandwidthboxplot_active(config_parser, section, "TCPBandwidth", "Upstream", "lte", ylim_TCPlte, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, section, "TCPBandwidth", "Downstream", "lte", ylim_TCPlte, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, section, "UDPBandwidth", "Upstream", "lte", ylim_UDPlte, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, section, "UDPBandwidth", "Downstream", "lte", ylim_UDPlte, 
                            legendypos=LEGENDYPOS_1LINE)
    

    #wifi
    bandwidthboxplot_active_conntypegrouped(config_parser, section, "TCPBandwidth", "Upstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
    bandwidthboxplot_active_conntypegrouped(config_parser, section, "TCPBandwidth", "Downstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
    bandwidthboxplot_active_conntypegrouped(config_parser, section, "TCPBandwidth", "both", ncol=3, 
                                            legendypos=LEGENDYPOS_4LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
    bandwidthboxplot_active_conntypegrouped(config_parser, section, "UDPBandwidth", "Upstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
    bandwidthboxplot_active_conntypegrouped(config_parser, section, "UDPBandwidth", "Downstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
    bandwidthboxplot_active_conntypegrouped(config_parser, section, "UDPBandwidth", "both",  ncol=3, 
                                            legendypos=LEGENDYPOS_4LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
    '''
def plotselfbandwidth(config_parser, section):
    ylim_wifi = 50
    ylim_lte = 200
    ncol_wifi = 3
    ncol_lte = 2
    bandwidthboxplot_noisegrouped(config_parser, section=section, mode="self", direction="downlink", 
                                  connectiontype="wifi", ylim=ylim_wifi, edgeserver=True, 
                                  segmentgrouped=False, ncol=ncol_wifi, legendypos=LEGENDYPOS_2LINE)   
    bandwidthboxplot_noisegrouped(config_parser, section=section, mode="self", direction="downlink", 
                                  connectiontype="wifi", ylim=ylim_wifi, edgeserver=False, 
                                  segmentgrouped=False, ncol=ncol_wifi, legendypos=LEGENDYPOS_2LINE)
    bandwidthboxplot_noisegrouped(config_parser, section=section, mode="self", direction="downlink", 
                                  connectiontype="wifi", ylim=ylim_wifi, edgeserver=True, segmentgrouped=True, 
                                  ncol=ncol_wifi, legendypos=LEGENDYPOS_4LINE)
    '''
    bandwidthboxplot_noisegrouped(config_parser, section=section, mode="self", direction="downlink", connectiontype="lte", ylim=ylim_lte, edgeserver=True, segmentgrouped=False, ncol=ncol_lte, legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_noisegrouped(config_parser, section=section, mode="self", direction="downlink", connectiontype="lte", ylim=ylim_lte, edgeserver=False, segmentgrouped=False, ncol=ncol_lte, legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_noisegrouped(config_parser, section=section, mode="self", direction="downlink", connectiontype="lte", ylim=ylim_lte, edgeserver=True, segmentgrouped=True, ncol=ncol_lte, legendypos=LEGENDYPOS_2LINE)
    '''

   
    bandwidthplot_fileandsegmentgrouped(config_parser=config_parser, section=section, mode="self", 
                                        direction="downlink", connectiontype="wifi", ncol=ncol_wifi, 
                                        legendypos=LEGENDYPOS_4LINE, ylim=ylim_wifi)
    #bandwidthplot_fileandsegmentgrouped(config_parser=config_parser, section=section, mode="self", direction="downlink", connectiontype="lte", ncol=ncol_lte, legendypos=LEGENDYPOS_2LINE, ylim=ylim_lte)
def plotmimbandwidth(config_parser, section):
    ylim = 50
    bucketsizes = [1.0 * 0.1 * 1000000, 1.0 * 0.5 * 1000000, 1.0 * 1 * 1000000] #[0.1, 0.5, 1]sec

    for elem in bucketsizes:
        bandwidthboxplot_noisemim(config_parser=config_parser, section=section, direction="downlink", connectiontype="wifi", 
                                  ylim=ylim, legendypos=LEGENDYPOS_2LINE, server="edge", ncol=3, logger=logger,
                                  bucketsize_microsec=elem)
        '''bandwidthboxplot_noisemim(config_parser=config_parser, section=section, direction="downlink", connectiontype="lte", 
                                ylim=ylim, legendypos=LEGENDYPOS_1LINE, server="edge", ncol=3, logger=logger, 
                                bucketsize_microsec=elem)
        '''
        bandwidthboxplot_noiseandsegmentmim(config_parser=config_parser, section=section, direction="downlink", logger=logger,
                                            connectiontype="wifi", ylim=ylim, ncol=3, legendypos=LEGENDYPOS_4LINE, 
                                            bucketsize_microsec=elem)
        '''bandwidthboxplot_noiseandsegmentmim(config_parser=config_parser, section=section, direction="downlink", logger=logger,
                                            connectiontype="lte", ylim=ylim, ncol=2, legendypos=LEGENDYPOS_2LINE, 
                                            bucketsize_microsec=elem)
        '''
        bandwidthplot_mimfileandsegment(config_parser=config_parser, section=section, mode="mim", direction="downlink", 
                                        connectiontype="wifi", ncol=3, legendypos=LEGENDYPOS_4LINE, logger=logger, 
                                        bucketsize_microsec=elem)
        '''bandwidthplot_mimfileandsegment(config_parser=config_parser, section=section, mode="mim", direction="downlink", 
                                        connectiontype="lte", ncol=2, legendypos=LEGENDYPOS_2LINE, logger=logger, 
                                        bucketsize_microsec=elem)
        '''
    

def plotmimandselfbandwidth(conf_parser, section):
    ylim = 50

    bucketsizes = [1.0 * 0.1 * 1000000, 1.0 * 0.5 * 1000000, 1.0 * 1 * 1000000] #[0.1, 0.5, 1]sec
    for elem in bucketsizes:
        mimselfbandwidthboxplot_conntypefileserverpos_xnoise(config_parser=conf_parser, section=section, direction="downlink", 
                                                             connectiontype="wifi", ylim=ylim,
                                                             edgecloudserver="edge", ncol=3, 
                                                             legendypos=LEGENDYPOS_4LINE, logger=logger, 
                                                             bucketsize_microsec=elem)

        mimselfbandwidthboxplot_conntypefileserverpos_xclients(config_parser=conf_parser, section=section, direction="downlink", 
                                                               connectiontype="wifi", ylim=ylim,
                                                               edgecloudserver="edge", ncol=3, 
                                                               legendypos=LEGENDYPOS_4LINE, logger=logger, 
                                                               bucketsize_microsec=elem)
        
        mimselfbandwidthboxplot_conntypeserverposnumclient(config_parser=conf_parser, section=section, direction="downlink", 
                                                           connectiontype="wifi", ylim=ylim,
                                                           edgecloudserver="edge", ncol=3,
                                                           legendypos=LEGENDYPOS_4LINE, logger=logger, 
                                                           bucketsize_microsec=elem)
        
        mimselfbandwidthboxplot_conntypeserverposcrosstraffic(config_parser=conf_parser, section=section, direction="downlink", 
                                                              connectiontype="wifi", ylim=ylim,
                                                              edgecloudserver="edge", ncol=3,
                                                              legendypos=LEGENDYPOS_4LINE, logger=logger, 
                                                              bucketsize_microsec=elem)

def plotpassiveperclient(conf_parser, section):
    ylim = 20
    bucketsize = 1.0 * 0.1 * 1000000 #0.1 sec
    
    bandwidthplot_perclient(config_parser=config_parser, section=section, direction="downlink", 
                            connectiontype="wifi", mode="mim", ylim=ylim, legendypos=LEGENDYPOS_2LINE, 
                            server="edge", ncol=3, logger=logger, legacy=False, bucketsize_microsec=bucketsize)   
    

    bucketsize = 1.0 * 0.5 * 1000000 #0.5 sec
    bandwidthplot_perclient(config_parser=config_parser, section=section, direction="downlink", 
                            connectiontype="wifi", mode="mim", ylim=ylim, legendypos=LEGENDYPOS_2LINE, 
                            server="edge", ncol=3, logger=logger, legacy=False, bucketsize_microsec=bucketsize)    
    
    bucketsize = 1.0 * 1000000 #1 sec
    bandwidthplot_perclient(config_parser=config_parser, section=section, direction="downlink", 
                            connectiontype="wifi", mode="mim", ylim=ylim, legendypos=LEGENDYPOS_2LINE, 
                            server="edge", ncol=3, logger=logger, legacy=False, bucketsize_microsec=bucketsize)   
    bandwidthplot_perclient(config_parser=config_parser, section=section, direction="downlink", 
                            connectiontype="wifi", mode="mim", ylim=ylim, legendypos=LEGENDYPOS_2LINE, 
                            server="cloud", ncol=3, logger=logger, legacy=False, bucketsize_microsec=bucketsize)   
    
    
    ylim = 50
    
    bandwidthplot_perclient(config_parser=config_parser, section=section, direction="downlink", 
                            connectiontype="wifi", mode="self", ylim=ylim, legendypos=LEGENDYPOS_2LINE, 
                            server="edge", ncol=3, logger=logger, legacy=False)     
    
    bandwidthplot_perclient(config_parser=config_parser, section=section, direction="downlink", 
                            connectiontype="wifi", mode="self", ylim=ylim, legendypos=LEGENDYPOS_2LINE, 
                            server="cloud", ncol=3, logger=logger, legacy=False)                                               
    

def plotmimlatency(config_parser, section):
    ylim = 405
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, section=section, direction="downlink", connectiontype="wifi", 
                                      ylim=ylim, ncol=3, legendypos=LEGENDYPOS_4LINE)
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, direction="downlink", connectiontype="wifi", 
                                      ylim=ylim, ncol=3, legendypos=LEGENDYPOS_4LINE)
    ylim = 1800
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, section=section, direction="uplink", connectiontype="wifi", 
                                      ylim=ylim, ncol=3, legendypos=LEGENDYPOS_4LINE)
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, section=section, direction="uplink", connectiontype="wifi", 
                                      ylim=ylim, ncol=3, legendypos=LEGENDYPOS_4LINE)
    ylim = 200
    '''
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, section=section, direction="downlink", connectiontype="lte", 
                                      ylim=ylim, ncol=2, legendypos=LEGENDYPOS_2LINE)
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, section=section, direction="downlink", connectiontype="lte", 
                                      ylim=ylim, ncol=2, legendypos=LEGENDYPOS_2LINE)
    ylim = 20
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, section=section, direction="uplink", connectiontype="lte", 
                                      ylim=ylim, ncol=2, legendypos=LEGENDYPOS_2LINE)
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, section=section, direction="uplink", connectiontype="lte", 
                                      ylim=ylim, ncol=2, legendypos=LEGENDYPOS_2LINE)

    '''

def plotactiveandpassivelatency(config_parser, section):
    ylim = 405
    latencyboxplot_noiseandsegmentactivemim(config_parser=config_parser, section=section, direction="downlink", ylim=ylim, 
                                            connectiontype="wifi", ncol=3, legendypos=LEGENDYPOS_4LINE, logger=logger)




def plottimeseries(config_parser, section):
    ylim_selfbandwidth = 100
    ylim_mimbandwidth = 20
    bucketsize_micros = 1.0 * 1000000 * 0.5 #0.5 sec

    #plottimeseries_bandwidth(config_parser, section=section, mode="self", ylim=ylim_selfbandwidth)
    plottimeseries_bandwidth(config_parser, section=section, mode="mim", ylim=ylim_mimbandwidth, bucketsize_micros=bucketsize_micros)
    plottimeseries_ports(config_parser, section=section, mode="self")
def plottimeseries_bandwidth(config_parser, section, mode, ylim, bucketsize_micros=None):
    passivetimeseries_usingbandwidth(config_parser, section=section, mode=mode, direction="downlink", connectiontype="wifi", 
                                     ylim=ylim, server="edge",bucketsize_microsec=bucketsize_micros, ncol=3, 
                                     legendypos=LEGENDYPOS_4LINE, logger=logger)
    
    passivetimeseries_usingbandwidth(config_parser, section=section, mode=mode, direction="downlink", connectiontype="wifi", 
                                     ylim=ylim, server="cloud", bucketsize_microsec=bucketsize_micros, ncol=3, 
                                     legendypos=LEGENDYPOS_4LINE, logger=logger)

    passivetimeseries(config_parser, section=section, mode=mode, direction="downlink", connectiontype="wifi", ylim=ylim, 
                      server="edge", ncol=3, legendypos=LEGENDYPOS_4LINE, logger=logger)
    
    passivetimeseries(config_parser, section=section, mode=mode, direction="downlink", connectiontype="wifi", ylim=ylim, 
                       server="cloud", ncol=3, legendypos=LEGENDYPOS_4LINE, logger=logger)
def plottimeseries_ports(config_parser, section, mode):
    passivetimeseries_usingclientports(config_parser, section=section, mode=mode, direction="downlink", connectiontype="wifi", 
                                       ylim=5, server="edge", ncol=3, legendypos=LEGENDYPOS_4LINE, logger=logger)    
    
    passivetimeseries_usingclientports(config_parser, section=section, mode=mode, direction="downlink", connectiontype="wifi", 
                                       ylim=5, server="cloud", ncol=3, legendypos=LEGENDYPOS_4LINE, logger=logger)

def getconfiguration(conffile):
    config_parser = configparser.RawConfigParser()
    config_parser.read(conffile)

    print ("Available configurations:")
    confsections = config_parser.sections()
    for section in confsections:
        if "plot_" in section:
            print("\t" + section)

    while True:
        inputsection = input("Choose a configuration:")
        if inputsection in confsections:
            break
        
        print(WARNING + "Invalid configuration" + RESET)

    return config_parser, inputsection

if __name__ == '__main__':
    #read configuration file
    config_parser, inputsection = getconfiguration("experiments.conf")

    '''
    #activebandwidth_lineplot(config_parser, "TCPRTT", "Upstream", "wifi")
    #activebandwidth_lineplot(config_parser, "TCPRTT", "Downstream", "wifi")
    #activebandwidth_lineplot(config_parser, "UDPRTT", "Upstream", "wifi")
    #activebandwidth_lineplot(config_parser, "UDPRTT", "Downstream", "wifi")
    #activebandwidth_lineplot(config_parser, "TCPBandwidth", "Upstream", "wifi")
    '''
    #plotpassiveperclient(config_parser, section=inputsection)
    
    
    plottimeseries(config_parser, section=inputsection)
    #plotselfbandwidth(config_parser, section=inputsection)
    #plotmimbandwidth(config_parser, section=inputsection)
    #plotmimlatency(config_parser, section=inputsection)
    
    
    #plotmimandselfbandwidth(config_parser, section=inputsection)
    
    #plotactivelatency(config_parser, section=inputsection)
    
    #plotactiveandpassivelatency(config_parser, section=inputsection)
    plotactivebandwidth(config_parser, section=inputsection)

    

    
