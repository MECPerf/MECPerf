package it.cnr.iit.mecperf.pcap;

import io.pkts.Pcap;
import io.pkts.protocol.Protocol;
import org.javatuples.Pair;
import org.javatuples.Quintet;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class Parser extends Thread {
    private BlockingQueue<String> toParse;
    private MECPerfPacketHandler handler;
    private HashMap<Quintet<Protocol, String, String, Integer, Integer>, Flow> flows;

    public Parser(BlockingQueue<String> toParse, Set<Pair<String, Integer>> servers) {
        super();
        this.toParse = toParse;
        this.flows = new HashMap<>();
        this.handler = new MECPerfPacketHandler(servers, flows);
    }

    public void run() {
        while (true) {
            try {
                String fileName = toParse.take();
                Pcap pcap = Pcap.openStream(fileName);
                pcap.loop(handler);
                for (Quintet<Protocol, String, String, Integer, Integer> fiveTuple : flows.keySet()) {
                    LinkedHashMap<Long, Integer> uplinkBytes = flows.get(fiveTuple).getBytes(Flow.DIR_UPLINK);
                    LinkedHashMap<Long, Integer> downlinkBytes = flows.get(fiveTuple).getBytes(Flow.DIR_DOWNLINK);
                    LinkedHashMap<Long, Long> uplinkRtts = flows.get(fiveTuple).getRtts(Flow.DIR_UPLINK);
                    LinkedHashMap<Long, Long> downlinkRtts = flows.get(fiveTuple).getRtts(Flow.DIR_DOWNLINK);
                    // TODO convert maps to JSONs with METADATA
                    // TODO put JSONs in Senders queue
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
