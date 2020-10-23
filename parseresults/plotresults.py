import ConfigParser
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
from timeplots import passive_timeseries


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
def plotactivelatency(config_parser):
    ylim_wifi = 50
    ylim_lte = 100

    #active latency wifi boxplots
    latencyboxplot_active(config_parser=config_parser, command="TCPRTT", direction="Upstream", 
                          connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, command="TCPRTT", direction="Downstream", 
                          connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, command="UDPRTT", direction="Upstream", 
                          connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, command="UDPRTT", direction="Downstream", 
                          connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_1LINE)

    #active latency lte boxplots
    latencyboxplot_active(config_parser=config_parser, command="TCPRTT", direction="Upstream", 
                          connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, command="TCPRTT", direction="Downstream", 
                          connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, command="UDPRTT", direction="Upstream", 
                          connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser=config_parser, command="UDPRTT", direction="Downstream", 
                          connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_1LINE)
    
    #wifi
    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Upstream", 
                                         connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_2LINE)
    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Downstream", 
                                         connectiontype="wifi", ylim=ylim_wifi, legendypos=LEGENDYPOS_2LINE)
    #lte
    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Upstream", 
                                         connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_2LINE)
    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Downstream", 
                                         connectiontype="lte", ylim=ylim_lte, legendypos=LEGENDYPOS_2LINE)
    #wifi&lte
    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Upstream", 
                                         connectiontype="both", ylim=max(ylim_wifi, ylim_lte), 
                                         legendypos=LEGENDYPOS_4LINE)
    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Downstream", 
                                         connectiontype="both", ylim=max(ylim_wifi, ylim_lte), 
                                         legendypos=LEGENDYPOS_4LINE)
    #wifi&lte
    latencyboxplot_active_conntypegrouped(config_parser, "TCPRTT", "Upstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_wifi, ylim_lte))
    latencyboxplot_active_conntypegrouped(config_parser, "TCPRTT", "Downstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_wifi, ylim_lte))
    latencyboxplot_active_conntypegrouped(config_parser, "TCPRTT", "both", ncol=3, 
                                            legendypos=LEGENDYPOS_4LINE, ylim=max(ylim_wifi, ylim_lte))
    latencyboxplot_active_conntypegrouped(config_parser, "UDPRTT", "Upstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_wifi, ylim_lte))
    latencyboxplot_active_conntypegrouped(config_parser, "UDPRTT", "Downstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_wifi, ylim_lte))
    latencyboxplot_active_conntypegrouped(config_parser, "UDPRTT", "both",  ncol=3, 
                                            legendypos=LEGENDYPOS_4LINE, ylim=max(ylim_wifi, ylim_lte))
def plotactivebandwidth(config_parser):
    ylim_TCPwifi = 35
    ylim_TCPlte = 35
    ylim_UDPwifi = 3000
    ylim_UDPlte = 3000

    #active bandwidth wifi
    bandwidthboxplot_active(config_parser, "TCPBandwidth", "Upstream", "wifi", ylim_TCPwifi, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, "TCPBandwidth", "Downstream", "wifi", ylim_TCPwifi, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Upstream", "wifi", ylim_UDPwifi, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Downstream", "wifi", ylim_UDPwifi, 
                            legendypos=LEGENDYPOS_1LINE)

    #active bandwidth lte
    bandwidthboxplot_active(config_parser, "TCPBandwidth", "Upstream", "lte", ylim_TCPlte, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, "TCPBandwidth", "Downstream", "lte", ylim_TCPlte, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Upstream", "lte", ylim_UDPlte, 
                            legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Downstream", "lte", ylim_UDPlte, 
                            legendypos=LEGENDYPOS_1LINE)

    #wifi
    bandwidthboxplot_active_conntypegrouped(config_parser, "TCPBandwidth", "Upstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
    bandwidthboxplot_active_conntypegrouped(config_parser, "TCPBandwidth", "Downstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
    bandwidthboxplot_active_conntypegrouped(config_parser, "TCPBandwidth", "both", ncol=3, 
                                            legendypos=LEGENDYPOS_4LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
    bandwidthboxplot_active_conntypegrouped(config_parser, "UDPBandwidth", "Upstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
    bandwidthboxplot_active_conntypegrouped(config_parser, "UDPBandwidth", "Downstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
    bandwidthboxplot_active_conntypegrouped(config_parser, "UDPBandwidth", "both",  ncol=3, 
                                            legendypos=LEGENDYPOS_4LINE, ylim=max(ylim_TCPwifi, ylim_TCPlte))
def plotselfbandwidth(config_parser):
    ylim = 50
    ncol = 3
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "wifi", ylim, True, False, ncol, LEGENDYPOS_2LINE)
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "wifi", ylim, False, False, ncol, LEGENDYPOS_2LINE)
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "wifi", ylim, True, True, ncol, LEGENDYPOS_4LINE)
    ylim = 200
    ncol = 2
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "lte", ylim, True, False, ncol, LEGENDYPOS_1LINE)
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "lte", ylim, False, False, ncol, LEGENDYPOS_1LINE)
    bandwidthboxplot_noisegrouped(config_parser, "self", "downlink", "lte", ylim, True, True, ncol, LEGENDYPOS_2LINE)
    
    ylim = 50
    ncol = 3
    bandwidthplot_fileandsegmentgrouped(config_parser, "self", "downlink", "wifi", ncol, LEGENDYPOS_4LINE)
    ylim = 200
    ncol = 2
    bandwidthplot_fileandsegmentgrouped(config_parser, "self", "downlink", "lte", ncol, LEGENDYPOS_2LINE, ylim)
def plotmimbandwidth(config_parser):
    ylim = 50

    bandwidthboxplot_noisemim(config_parser=config_parser, direction="downlink", connectiontype="wifi", 
                              ylim=ylim, legendypos=LEGENDYPOS_2LINE, server="edge", ncol=3, logger=logger)
    bandwidthboxplot_noisemim(config_parser=config_parser, direction="downlink", connectiontype="wifi", 
                              ylim=ylim, legendypos=LEGENDYPOS_2LINE, server="remote", ncol=3, logger=logger)
    bandwidthboxplot_noisemim(config_parser=config_parser, direction="downlink", connectiontype="lte", 
                              ylim=ylim, legendypos=LEGENDYPOS_1LINE, server="edge", ncol=3, logger=logger)
    bandwidthboxplot_noisemim(config_parser=config_parser, direction="downlink", connectiontype="lte", 
                              ylim=ylim, legendypos=LEGENDYPOS_1LINE, server="remote", ncol=3, logger=logger)
    
    bandwidthboxplot_noiseandsegmentmim(config_parser=config_parser, direction="downlink", logger=logger,
                                        connectiontype="wifi", ylim=ylim, ncol=3, legendypos=LEGENDYPOS_4LINE)
    bandwidthboxplot_noiseandsegmentmim(config_parser=config_parser, direction="downlink", logger=logger,
                                        connectiontype="lte", ylim=ylim, ncol=2, legendypos=LEGENDYPOS_2LINE)
    
    bandwidthplot_mimfileandsegment(config_parser=config_parser, mode="mim", direction="downlink", 
                                    connectiontype="wifi", ncol=3, legendypos=LEGENDYPOS_4LINE, logger=logger)
    bandwidthplot_mimfileandsegment(config_parser=config_parser, mode="mim", direction="downlink", 
                                    connectiontype="lte", ncol=2, legendypos=LEGENDYPOS_2LINE, logger=logger)

    
    
def plotmimandselfbandwidth(conf_parser):
    ylim = 50
    
    mimselfbandwidthboxplot_conntypefileserverpos_xnoise(config_parser=conf_parser, direction="downlink", 
                                                         connectiontype="wifi", ylim=ylim,
                                                         edgecloudserver="edge", ncol=3, 
                                                         legendypos=LEGENDYPOS_4LINE, logger=logger)
    mimselfbandwidthboxplot_conntypefileserverpos_xnoise(config_parser=conf_parser, direction="downlink",
                                                         connectiontype="wifi", ylim=ylim, 
                                                         edgecloudserver="cloud", ncol=3, 
                                                         legendypos=LEGENDYPOS_4LINE, logger=logger)
    mimselfbandwidthboxplot_conntypefileserverpos_xnoise(config_parser=conf_parser, direction="downlink",
                                                         connectiontype="wifi", ylim=ylim, 
                                                         edgecloudserver="both", ncol=3, 
                                                         legendypos=LEGENDYPOS_8LINE, logger=logger)

    mimselfbandwidthboxplot_conntypefileserverpos_xclients(config_parser=conf_parser, direction="downlink", 
                                                           connectiontype="wifi", ylim=ylim,
                                                           edgecloudserver="edge", ncol=3, 
                                                           legendypos=LEGENDYPOS_4LINE, logger=logger)
    mimselfbandwidthboxplot_conntypefileserverpos_xclients(config_parser=conf_parser, direction="downlink",
                                                           connectiontype="wifi", ylim=ylim, 
                                                           edgecloudserver="cloud", ncol=3, 
                                                           legendypos=LEGENDYPOS_4LINE, logger=logger)
    mimselfbandwidthboxplot_conntypefileserverpos_xclients(config_parser=conf_parser, direction="downlink",
                                                           connectiontype="wifi", ylim=ylim, 
                                                           edgecloudserver="both", ncol=3, 
                                                           legendypos=LEGENDYPOS_8LINE, logger=logger)
    
    mimselfbandwidthboxplot_conntypeserverposnumclient(config_parser=conf_parser, direction="downlink", 
                                                       connectiontype="wifi", ylim=ylim,
                                                       edgecloudserver="edge", ncol=3,
                                                       legendypos=LEGENDYPOS_4LINE, logger=logger)
    mimselfbandwidthboxplot_conntypeserverposnumclient(config_parser=conf_parser, direction="downlink",
                                                       connectiontype="wifi", ylim=ylim, 
                                                       edgecloudserver="cloud", ncol=3, 
                                                       legendypos=LEGENDYPOS_4LINE, logger=logger)
    mimselfbandwidthboxplot_conntypeserverposnumclient(config_parser=conf_parser, direction="downlink",
                                                       connectiontype="wifi", ylim=ylim, 
                                                       edgecloudserver="both", ncol=3, 
                                                       legendypos=LEGENDYPOS_8LINE, logger=logger)
    
    mimselfbandwidthboxplot_conntypeserverposcrosstraffic(config_parser=conf_parser, direction="downlink", 
                                                          connectiontype="wifi", ylim=ylim,
                                                          edgecloudserver="edge", ncol=3,
                                                          legendypos=LEGENDYPOS_4LINE, logger=logger)
    mimselfbandwidthboxplot_conntypeserverposcrosstraffic(config_parser=conf_parser, direction="downlink",
                                                          connectiontype="wifi", ylim=ylim, 
                                                          edgecloudserver="cloud", ncol=3, 
                                                          legendypos=LEGENDYPOS_4LINE, logger=logger)
    mimselfbandwidthboxplot_conntypeserverposcrosstraffic(config_parser=conf_parser, direction="downlink",
                                                          connectiontype="wifi", ylim=ylim, 
                                                          edgecloudserver="both", ncol=3, 
                                                          legendypos=LEGENDYPOS_8LINE, logger=logger)

def plotpassiveperclient(conf_parser):
    ylim = 20
    
    bandwidthplot_perclient(config_parser=config_parser, direction="downlink", connectiontype="wifi", 
                            mode="mim", ylim=ylim, legendypos=LEGENDYPOS_2LINE, server="edge", ncol=3, 
                            logger=logger, legacy=True)     
    bandwidthplot_perclient(config_parser=config_parser, direction="downlink", connectiontype="wifi", 
                            mode="mim", ylim=ylim, legendypos=LEGENDYPOS_2LINE, server="cloud", ncol=3, 
                            logger=logger, legacy=True)   
    
    
    bandwidthplot_perclient(config_parser=config_parser, direction="downlink", connectiontype="wifi", 
                            mode="mim", ylim=ylim, legendypos=LEGENDYPOS_2LINE, server="edge", ncol=3, 
                            logger=logger, legacy=False)     
    bandwidthplot_perclient(config_parser=config_parser, direction="downlink", connectiontype="wifi", 
                            mode="mim",ylim=ylim, legendypos=LEGENDYPOS_2LINE, server="cloud", ncol=3, 
                            logger=logger, legacy=False)   
    

    ylim = 50
    bandwidthplot_perclient(config_parser=config_parser, direction="downlink", connectiontype="wifi", mode="self", 
                            ylim=ylim, legendypos=LEGENDYPOS_2LINE, server="edge", ncol=3, logger=logger, legacy=True)     
    bandwidthplot_perclient(config_parser=config_parser, direction="downlink", connectiontype="wifi", mode="self", 
                            ylim=ylim, legendypos=LEGENDYPOS_2LINE, server="cloud", ncol=3, logger=logger, legacy=True)                                               
    

def plotmimlatency(config_parser):
    ylim = 405
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, direction="downlink", connectiontype="wifi", 
                                      ylim=ylim, ncol=3, legendypos=LEGENDYPOS_4LINE)
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, direction="downlink", connectiontype="wifi", 
                                      ylim=ylim, ncol=3, legendypos=LEGENDYPOS_4LINE)
    ylim = 1800
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, direction="uplink", connectiontype="wifi", 
                                      ylim=ylim, ncol=3, legendypos=LEGENDYPOS_4LINE)
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, direction="uplink", connectiontype="wifi", 
                                      ylim=ylim, ncol=3, legendypos=LEGENDYPOS_4LINE)
    ylim = 200
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, direction="downlink", connectiontype="lte", 
                                      ylim=ylim, ncol=2, legendypos=LEGENDYPOS_2LINE)
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, direction="downlink", connectiontype="lte", 
                                      ylim=ylim, ncol=2, legendypos=LEGENDYPOS_2LINE)
    ylim = 20
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, direction="uplink", connectiontype="lte", 
                                      ylim=ylim, ncol=2, legendypos=LEGENDYPOS_2LINE)
    latencyboxplot_noiseandsegmentmim(config_parser=config_parser, direction="uplink", connectiontype="lte", 
                                      ylim=ylim, ncol=2, legendypos=LEGENDYPOS_2LINE)



def plotactiveandpassivelatency(config_parser):
    ylim = 405
    latencyboxplot_noiseandsegmentactivemim(config_parser=config_parser, direction="downlink", ylim=ylim, 
                                            connectiontype="wifi", ncol=3, legendypos=LEGENDYPOS_4LINE, logger=logger)




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
    '''

    #passive_timeseries(config_parser, mode="self", direction="downlink", connectiontype="wifi", ylim=50, 
    #                   server="edge", clientnumber=10, noise="0M", ncol=3, legendypos=LEGENDYPOS_4LINE, logger=logger)
    
    #plotactivelatency(config_parser)
    #plotmimlatency(config_parser)
    #plotactiveandpassivelatency(config_parser)

    #plotactivebandwidth(config_parser)
    
    plotpassiveperclient(config_parser)
    plotselfbandwidth(config_parser)
    plotmimbandwidth(config_parser)
    plotmimandselfbandwidth(config_parser)
    

    
