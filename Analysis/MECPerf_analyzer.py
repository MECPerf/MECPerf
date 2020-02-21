import json
import ConfigParser
import colors

from AnalyzeMeasures import analyze_activebandwidthmeasures



BASE_URL = ""
BASE_PARAMS = {'compact':True, 'json':True, "likeKeyword":'experiment_active_'}
        
 

if __name__ ==  "__main__":
    #read configuration file
    config_parser = ConfigParser.RawConfigParser()
    config_file_path = r'test1.conf'
    config_parser.read(config_file_path)

    BASE_URL = "http://" + str(config_parser.get("REST_server_params", "address")) + ":"
    BASE_URL += str(config_parser.get("REST_server_params", "port"))

    print "\n\n"
    print "###########################################################################################"
    print colors.BLUE + "EXPERIMENT CONF:"
    print "BASE_URL: " + BASE_URL
    print "dates: " + config_parser.get("experiment_params", "dates") 
    print "from " + config_parser.get("experiment_params", "starting_time_intervals") + " [ " +\
          config_parser.get("experiment_params", "duration_m") + " min.]" + colors.RESET
    print "###########################################################################################"
    print "\n\n"
        
    #active uplink/downlink bandwidth measures
    print BASE_PARAMS
    analyze_activebandwidthmeasures(BASE_PARAMS, BASE_URL, 'Upstream', "TCPBandwidth", config_parser)
    analyze_activebandwidthmeasures(BASE_PARAMS, BASE_URL, 'Downstream', "TCPBandwidth", config_parser)

    analyze_activebandwidthmeasures(BASE_PARAMS, BASE_URL, 'Upstream', "UDPBandwidth", config_parser)
    analyze_activebandwidthmeasures(BASE_PARAMS, BASE_URL, 'Downstream', "UDPBandwidth", config_parser)
        
