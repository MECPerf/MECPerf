package it.cnr.iit.mecperf.pcap;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;
import org.javatuples.Quintet;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class Main {
    public static void main(String[] args) throws IOException {
        final Pcap pcap = Pcap.openStream("/home/valerio/tcpdump.pcap");
        HashMap<Quintet<Protocol, String, String, Integer, Integer>, Flow> flows = new HashMap<>();
        HashSet<Pair<String, Integer>> servers = new HashSet<>();
        servers.add(new Pair<>("23.32.11.57", 443));

        pcap.loop(new MECPerfPacketHandler(servers, flows));
        for (Quintet<Protocol, String, String, Integer, Integer> fiveTuple : flows.keySet()) {
            System.out.println(fiveTuple);
            LinkedHashMap<Long, Integer> uplinkBytes = flows.get(fiveTuple).getBytes(Flow.DIR_UPLINK);
            System.out.println("Uplink Bytes");
            for (Long time : uplinkBytes.keySet()) {
                System.out.printf("%d: %d\n", time, uplinkBytes.get(time));
            }
            LinkedHashMap<Long, Long> uplinkRtts = flows.get(fiveTuple).getRtts(Flow.DIR_UPLINK);
            System.out.println("Uplink RTTs");
            for (Long time : uplinkRtts.keySet()) {
                System.out.printf("%d: %d\n", time, uplinkRtts.get(time));
            }
            System.out.println("Downlink Bytes");
            LinkedHashMap<Long, Integer> downlinkBytes = flows.get(fiveTuple).getBytes(Flow.DIR_DOWNLINK);
            boolean first = true;
            long prevTime = 0;
            for (Long time : downlinkBytes.keySet()) {
                double bw;
                if (first) {
                    bw = 0;
                    first = false;
                } else {
                    bw = ((double) downlinkBytes.get(time)) * 8 * 1000000 / (time - prevTime);
                }
                prevTime = time;
                System.out.printf("%d: %d %f\n", time, downlinkBytes.get(time), bw);
            }
            LinkedHashMap<Long, Long> downlinkRtts = flows.get(fiveTuple).getRtts(Flow.DIR_DOWNLINK);
            System.out.println("Dowlink RTTs");
            for (Long time : downlinkRtts.keySet()) {
                System.out.printf("%d: %d\n", time, downlinkRtts.get(time));
            }
        }
    }
}
