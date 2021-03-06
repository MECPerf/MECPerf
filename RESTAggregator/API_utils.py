def print_command_list():
    string = '<List of commands:<br>'
    string += '<H4>GET Requests</H4>'
    string += '<a href="/get_measures/get_data_list">get_measures/get_data_list</a> [json]<br>'
    string += '<a href="/get_active_measures/bandwidth">get_active_measures/bandwidth</a> <br>'
    string += '<a href="/get_passive_measures/self_bandwidth">get_passive_measures/self_bandwidth</a> <br>'
    string += '<a href="/get_passive_measures/mim_bandwidth">get_passive_measures/mim_bandwidth</a> <br>'

    string += '<a href="/get_measures/get_data_list">get_measures/get_data_list</a> <br>'
    string += '<H4>POST Requests</H4>'
    string += '<a href="/post_active_measures/insert_bandwidth_measure">post_active_measures/insert_bandwidth_measure</a> <br>'
    string += '<a href="/post_passive_measures/insert_bandwidth_measure">post_passive_measures/insert_bandwidth_measure</a> <br>'
    string += '<a href="/post_active_measures/insert_latency_measure">post_active_measures/insert_latency_measure</a> <br>'
    string += '<a href="/post_passive_measures/insert_latency_measure">post_passive_measures/insert_latency_measure</a> <br>'

    string += '<H4>Mobile GET Requests</H4>'
    string += '<a href="/mobile/get_data_list">mobile/get_data_list</a>  <br>'
    string += '<a href="/mobile/get_RTT_data">get_RTT_data</a> required: id, sender<br>'
    string += '<a href="/mobile/get_bandwidth_data">get_bandwidth_data</a> required: id, sender<br>'
    string += '<a href="/mobile/get_AVGBandwidth_data">get_AVGBandwidth_data</a> required: id, sender<br>'


    return string