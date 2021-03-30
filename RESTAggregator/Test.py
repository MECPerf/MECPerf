class Test:
    def __init__(self, request, test_type):
        #print (request)
        self.type = test_type

        if self.type == "active":
            self.test_info_first_segment = request['test_info_first_segment']
            self.test_info_second_segment = request['test_info_second_segment']
            if self.test_info_first_segment['Command'] == 'TCPBandwidth' or self.test_info_first_segment['Command'] == 'UDPBandwidth':
                self.test_values_first_segment = request['bandwidth_values_first_segment']
                self.test_values_second_segment = request['bandwidth_values_second_segment']
            if self.test_info_first_segment['Command'] == 'TCPRTT' or self.test_info_first_segment['Command'] == 'UDPRTT':
                self.test_values_first_segment = request['latency_values_first_segment']
                self.test_values_second_segment = request['latency_values_second_segment']
        
        if self.type == "passive":
            self.info = request
            try:
                self.downlink = request['downlink']
                del self.info['downlink']
            except KeyError:
                self.downlink = []

            try:
                self.uplink = request['uplink'] 
                del self.info['uplink']
            except KeyError:
                self.uplink = []


        #print (request)
            
            
           