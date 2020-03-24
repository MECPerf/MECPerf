import json
import ConfigParser
import colors
import sys
import distutils.util

from AnalyzeMeasures import analyze_activebandwidthmeasures, analyze_passivebandwidthmeasures



BASE_URL = ""
        
 

if __name__ ==  "__main__":
      config_file_path = sys.argv[1]

      #read configuration file
      config_parser = ConfigParser.RawConfigParser()
      config_parser.read(config_file_path)

      BASE_URL = "http://" + str(config_parser.get("REST_server_params", "address")) + ":"
      BASE_URL += str(config_parser.get("REST_server_params", "port"))

      if distutils.util.strtobool(config_parser.get('experiment_params', "analyze_active_data")) == True:
            print "\n\n"
            print "###########################################################################################"
            print colors.BLUE + "ACTIVE EXPERIMENT CONF:"
            print "BASE_URL: " + BASE_URL
            print "dates: " + config_parser.get("active_experiment_params", "dates") 
            print "starting hours " + config_parser.get("active_experiment_params", "starting_time_intervals") + " [ " +\
                  config_parser.get("active_experiment_params", "duration_m") + " min.]" + colors.RESET
            print "###########################################################################################"
            print "\n\n"
                  
            #active uplink/downlink bandwidth measures
            analyze_activebandwidthmeasures(BASE_URL, 'Upstream', "TCPBandwidth", config_parser)
            analyze_activebandwidthmeasures(BASE_URL, 'Downstream', "TCPBandwidth", config_parser)

            analyze_activebandwidthmeasures(BASE_URL, 'Upstream', "UDPBandwidth", config_parser)
            analyze_activebandwidthmeasures(BASE_URL, 'Downstream', "UDPBandwidth", config_parser)

      if distutils.util.strtobool(config_parser.get('experiment_params', "analyze_passive_data")) == True:
            #passive measurements

            print "\n\n"
            print "###########################################################################################"
            print colors.BLUE + "PASSIVE EXPERIMENT CONF:"
            print "BASE_URL: " + BASE_URL
            print "from " + config_parser.get("passive_experiment_params", "from") + " to " + \
                  config_parser.get("passive_experiment_params", "to") + colors.RESET
            
            print "###########################################################################################"
            print "\n\n"

            analyze_passivebandwidthmeasures(BASE_URL, 'downlink', "bandwidth", "tcp", config_parser)

                  

     

        
