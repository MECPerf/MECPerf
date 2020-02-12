package it.cnr.iit.mecperf.pcap;

import java.util.LinkedHashMap;

public class MeasurementResult {
    private String client_ip;
    private int client_port;
    private String server_ip;
    private int server_port;
    private String service;
    private String protocol;
    private String mode = "self";
    private LinkedHashMap<Long, Long> uplink;
    private LinkedHashMap<Long, Long> downlink;

    public MeasurementResult(String cip, int cport, String sip, int sport, String service, String protocol,
                             LinkedHashMap<Long, Long> uplink, LinkedHashMap<Long, Long> downlink) {
        this.client_ip = cip;
        this.client_port = cport;
        this.server_ip = sip;
        this.server_port = sport;
        this.service = service;
        this.protocol = protocol;
        this.uplink = uplink;
        this.downlink = downlink;
    }
}
