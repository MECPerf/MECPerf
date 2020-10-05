import ConfigParser

from plot_boxplots import bandwidthboxplot_noisegrouped, bandwidthplot_mimfileandsegment
from plot_boxplots import bandwidthboxplot_active_conntypegrouped, bandwidthboxplot_active
from plot_boxplots import bandwidthplot_fileandsegmentgrouped, latencyboxplot_noiseandsegmentmim
from plot_boxplots import bandwidthboxplot_noisemim, bandwidthboxplot_noiseandsegmentmim
from plot_boxplots import latencyboxplot_active_commandgrouped, latencyboxplot_active
from plot_boxplots import latencyboxplot_active_conntypegrouped


WARNING = '\033[93m'
FAIL = '\033[91m'
RESET = '\033[0m'

LEGENDYPOS_1LINE = 1.15
LEGENDYPOS_2LINE = 1.25
LEGENDYPOS_4LINE = 1.40



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
def plotactivelatency(config_parser):
    #active latency wifi
    
    ylim = 50
    latencyboxplot_active(config_parser, "TCPRTT", "Upstream", "wifi", ylim, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser, "TCPRTT", "Downstream", "wifi", ylim, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser, "UDPRTT", "Upstream", "wifi", ylim, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser, "UDPRTT", "Downstream", "wifi", ylim, legendypos=LEGENDYPOS_1LINE)

    #active latency lte
    ylim = 100
    latencyboxplot_active(config_parser, "TCPRTT", "Upstream", "lte", ylim, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser, "TCPRTT", "Downstream", "lte", ylim, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser, "UDPRTT", "Upstream", "lte", ylim, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active(config_parser, "UDPRTT", "Downstream", "lte", ylim, legendypos=LEGENDYPOS_1LINE)
    

    ylim = 50
    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Upstream", 
                                         connectiontype="wifi", ylim=ylim, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Downstream", 
                                         connectiontype="wifi", ylim=ylim, legendypos=LEGENDYPOS_1LINE)
    ylim = 100
    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Upstream", 
                                         connectiontype="lte", ylim=ylim, legendypos=LEGENDYPOS_1LINE)
    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Downstream", 
                                         connectiontype="lte", ylim=ylim, legendypos=LEGENDYPOS_1LINE)

    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Upstream", 
                                         connectiontype="both", ylim=ylim, legendypos=LEGENDYPOS_4LINE)
    latencyboxplot_active_commandgrouped(config_parser=config_parser, direction="Downstream", 
                                         connectiontype="both", ylim=ylim, legendypos=LEGENDYPOS_4LINE)

    ylim = 101
    latencyboxplot_active_conntypegrouped(config_parser, "TCPRTT", "Upstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=ylim)
    latencyboxplot_active_conntypegrouped(config_parser, "TCPRTT", "Downstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=ylim)
    latencyboxplot_active_conntypegrouped(config_parser, "TCPRTT", "both", ncol=3, 
                                            legendypos=LEGENDYPOS_4LINE, ylim=ylim)
    latencyboxplot_active_conntypegrouped(config_parser, "UDPRTT", "Upstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=ylim)
    latencyboxplot_active_conntypegrouped(config_parser, "UDPRTT", "Downstream",  ncol=3, 
                                            legendypos=LEGENDYPOS_2LINE, ylim=ylim)
    latencyboxplot_active_conntypegrouped(config_parser, "UDPRTT", "both",  ncol=3, 
                                            legendypos=LEGENDYPOS_4LINE, ylim=ylim)
def plotactivebandwidth(config_parser):
    #active bandwidth wifi
    ylim = 35
    '''bandwidthboxplot_active(config_parser, "TCPBandwidth", "Upstream", "wifi", ylim, legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, "TCPBandwidth", "Downstream", "wifi", ylim, legendypos=LEGENDYPOS_1LINE)
    ylim = 3000
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Upstream", "wifi", ylim, legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Downstream", "wifi", ylim, legendypos=LEGENDYPOS_1LINE)

    #active bandwidth lte
    ylim = 35
    bandwidthboxplot_active(config_parser, "TCPBandwidth", "Upstream", "lte", ylim, legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, "TCPBandwidth", "Downstream", "lte", ylim, legendypos=LEGENDYPOS_1LINE)
    ylim = 3000
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Upstream", "lte", ylim, legendypos=LEGENDYPOS_1LINE)
    bandwidthboxplot_active(config_parser, "UDPBandwidth", "Downstream", "lte", ylim, legendypos=LEGENDYPOS_1LINE)
    '''
    ylim = 35
    bandwidthboxplot_active_conntypegrouped(config_parser, "TCPBandwidth", "Upstream",  ncol=3, legendypos=LEGENDYPOS_2LINE, ylim=ylim)
    bandwidthboxplot_active_conntypegrouped(config_parser, "TCPBandwidth", "Downstream",  ncol=3, legendypos=LEGENDYPOS_2LINE, ylim=ylim)
    bandwidthboxplot_active_conntypegrouped(config_parser, "TCPBandwidth", "both", ncol=3, legendypos=LEGENDYPOS_4LINE, ylim=ylim)
    ylim = 2800
    bandwidthboxplot_active_conntypegrouped(config_parser, "UDPBandwidth", "Upstream",  ncol=3, legendypos=LEGENDYPOS_2LINE, ylim=ylim)
    bandwidthboxplot_active_conntypegrouped(config_parser, "UDPBandwidth", "Downstream",  ncol=3, legendypos=LEGENDYPOS_2LINE, ylim=ylim)
    bandwidthboxplot_active_conntypegrouped(config_parser, "UDPBandwidth", "both",  ncol=3, legendypos=LEGENDYPOS_4LINE, ylim=ylim)
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
                              ylim=ylim, legendypos=LEGENDYPOS_2LINE, server="edge", ncol=3)
    bandwidthboxplot_noisemim(config_parser=config_parser, direction="downlink", connectiontype="wifi", 
                              ylim=ylim, legendypos=LEGENDYPOS_2LINE, server="remote", ncol=3)
    bandwidthboxplot_noisemim(config_parser=config_parser, direction="downlink", connectiontype="lte", 
                              ylim=ylim, legendypos=LEGENDYPOS_1LINE, server="edge", ncol=3)
    bandwidthboxplot_noisemim(config_parser=config_parser, direction="downlink", connectiontype="lte", 
                              ylim=ylim, legendypos=LEGENDYPOS_1LINE, server="remote", ncol=3)

    bandwidthboxplot_noiseandsegmentmim(config_parser=config_parser, direction="downlink", 
                                        connectiontype="wifi", ylim=ylim, ncol=3, legendypos=LEGENDYPOS_4LINE)
    bandwidthboxplot_noiseandsegmentmim(config_parser=config_parser, direction="downlink", connectiontype="lte", 
                                        ylim=ylim, ncol=2, legendypos=LEGENDYPOS_2LINE)

    bandwidthplot_mimfileandsegment(config_parser=config_parser, mode="mim", direction="downlink", 
                                    connectiontype="wifi", ncol=3, legendypos=LEGENDYPOS_4LINE)
    bandwidthplot_mimfileandsegment(config_parser=config_parser, mode="mim", direction="downlink", 
                                    connectiontype="lte", ncol=2, legendypos=LEGENDYPOS_2LINE)
def plotmimlatency(config_parser):
    ylim = 700
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
    
    plotactivelatency(config_parser)
    plotactivebandwidth(config_parser)
    plotselfbandwidth(config_parser)
    plotmimbandwidth(config_parser)
    plotmimlatency(config_parser)


    
