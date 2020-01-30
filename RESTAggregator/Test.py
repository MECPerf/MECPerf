class Test:

    def __init__(self, request):
        self.test_info_first_segment = request['test_info_first_segment']
        self.test_info_second_segment = request['test_info_second_segment']
        if self.test_info_first_segment['Command'] == 'TCPBandwidth' or self.test_info_first_segment['Command'] == 'UDPBandwidth':
            self.test_values_first_segment = request['bandwidth_values_first_segment']
            self.test_values_second_segment = request['bandwidth_values_second_segment']
        if self.test_info_first_segment['Command'] == 'TCPRTT' or self.test_info_first_segment['Command'] == 'UDPRTT':
            self.test_values_first_segment = request['latency_values_first_segment']
            self.test_values_second_segment = request['latency_values_second_segment']

     