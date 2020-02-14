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
    private BlockingQueue<Pair<Integer, MeasurementResult>> toSend;
    private MECPerfPacketHandler handler;
    private HashMap<Quintet<Protocol, String, String, Integer, Integer>, Flow> flows;

    public Parser(BlockingQueue<String> toParse, BlockingQueue<Pair<Integer, MeasurementResult>> toSend, Set<Pair<String, Integer>> servers) {
        super();
        this.toParse = toParse;
        this.toSend = toSend;
        this.flows = new HashMap<>();
        this.handler = new MECPerfPacketHandler(servers, flows);
    }

    public void run() {
        Logger.info("Parser started");
        while (true) {
            try {
                Logger.debug("Reading file name from queue");
                String fileName = toParse.take();
                Logger.debug("Read file name: {}", fileName);
                if (fileName.equals(Utils.PARSE_END)) {
                    toSend.put(new Pair<Integer, MeasurementResult>(Utils.SEND_TYPE_END, null));
                    break;
                }
                Pcap pcap = Pcap.openStream(fileName);
                pcap.loop(handler);
                Gson gson = new Gson();
                for (Quintet<Protocol, String, String, Integer, Integer> fiveTuple : flows.keySet()) {
                    MeasurementResult bw = flows.get(fiveTuple).toMeasurementResult(Flow.TYPE_BANDWIDTH);
                    MeasurementResult rtt = flows.get(fiveTuple).toMeasurementResult(Flow.TYPE_RTT);
                    // put MeasurementResults in Senders queue
                    toSend.put(new Pair<>(Utils.SEND_TYPE_BW, bw));
                    if (rtt != null)
                        toSend.put(new Pair<>(Utils.SEND_TYPE_RTT, rtt));
                }
                flows.clear();
            } catch (InterruptedException | IOException e) {
                Logger.error(e);
            }
        }
    }
}
