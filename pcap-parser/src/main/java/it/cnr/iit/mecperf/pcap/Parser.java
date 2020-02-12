package it.cnr.iit.mecperf.pcap;

import com.google.gson.Gson;
import io.pkts.Pcap;
import io.pkts.protocol.Protocol;
import org.javatuples.Pair;
import org.javatuples.Quintet;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class Parser extends Thread {
    private BlockingQueue<String> toParse;
    private BlockingQueue<String> toSend;
    private MECPerfPacketHandler handler;
    private HashMap<Quintet<Protocol, String, String, Integer, Integer>, Flow> flows;

    public Parser(BlockingQueue<String> toParse, BlockingQueue<String> toSend, Set<Pair<String, Integer>> servers) {
        super();
        this.toParse = toParse;
        this.toSend = toSend;
        this.flows = new HashMap<>();
        this.handler = new MECPerfPacketHandler(servers, flows);
    }

    public void run() {
        while (true) {
            try {
                Logger.info("Reading file name from queue");
                String fileName = toParse.take();
                Pcap pcap = Pcap.openStream(fileName);
                pcap.loop(handler);
                Gson gson = new Gson();
                for (Quintet<Protocol, String, String, Integer, Integer> fiveTuple : flows.keySet()) {
                    // convert flow object to JSONs with METADATA
                    String bandwidthJson = gson.toJson(flows.get(fiveTuple).toMeasurementResult(Flow.TYPE_BANDWIDTH));
                    String rttJson = gson.toJson(flows.get(fiveTuple).toMeasurementResult(Flow.TYPE_RTT));
                    // put JSONs in Senders queue
                    toSend.put(bandwidthJson);
                    if (rttJson != null)
                        toSend.put(rttJson);
                }
                flows.clear();
            } catch (InterruptedException | IOException e) {
                Logger.error(e);
            }
        }
    }
}
