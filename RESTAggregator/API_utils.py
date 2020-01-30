def print_command_list():
    string = '<List of commands:<br>'
    string += '<H4>GET Requests</H4>'
    string += '<a href="/get_measures/bandwidth">get_measures/bandwidth</a> <br>'
    string += '<H4>POST Requests</H4>'
    string += '<a href="/post_measures/insert_bandwidth_measure">/post_measures/insert_bandwidth_measure</a> <br>'
    string += '<a href="/post_measures/insert_rtt_measure">/post_measures/insert_rtt_measure</a> <br>'
    string += '<H4>Mobile GET Requests</H4>'
    string += '<a href="/get_data_list">get_data_list</a> optional: keyword(default ""), json(default False) <br>'
    string += '<a href="/get_RTT_data">get_RTT_data</a> required: id, sender<br>'
    string += '<a href="/get_bandwidth_data">get_bandwidth_data</a> required: id, sender<br>'
    string += '<a href="/get_AVGBandwidth_data">get_AVGBandwidth_data</a> required: id, sender<br>'


    return string


