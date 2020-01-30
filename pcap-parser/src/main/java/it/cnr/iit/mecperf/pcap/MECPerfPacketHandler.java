package it.cnr.iit.mecperf.pcap;

import io.pkts.PacketHandler;
import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;
import org.javatuples.Pair;
import org.javatuples.Quintet;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class MECPerfPacketHandler implements PacketHandler {
    HashSet<Pair<String, Integer>> servers;
    HashMap<Quintet<Protocol, String, String, Integer, Integer>, Flow> flows;

    public MECPerfPacketHandler(HashSet<Pair<String, Integer>> servers,
                                HashMap<Quintet<Protocol, String, String, Integer, Integer>, Flow> flows) {
        this.servers = servers;
        this.flows = flows;
    }

    @Override
    public boolean nextPacket(Packet packet) throws IOException {
        if (packet.hasProtocol(Protocol.TCP)) {
            TCPPacket tcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);
            String srcIp = tcpPacket.getSourceIP();
            int srcPort = tcpPacket.getSourcePort();
            String dstIp = tcpPacket.getDestinationIP();
            int dstPort = tcpPacket.getDestinationPort();
            int dir;
            Quintet<Protocol, String, String, Integer, Integer> fiveTuple;
            if (servers.contains(new Pair<>(srcIp, srcPort))) {
                dir = Flow.DIR_DOWNLINK;
                fiveTuple = new Quintet<>(Protocol.TCP, dstIp, srcIp, dstPort, srcPort);
            } else if (servers.contains(new Pair<>(dstIp, dstPort))) {
                dir = Flow.DIR_UPLINK;
                fiveTuple = new Quintet<>(Protocol.TCP, srcIp, dstIp, srcPort, dstPort);
            } else
                return true;
            System.out.println(fiveTuple);

            long arrivalTime = tcpPacket.getArrivalTime();
            int payloadLength;
            Buffer buf = tcpPacket.getPayload();
            if (buf != null) {
                payloadLength = buf.capacity();
            } else {
                payloadLength = 0;
            }
            long seqNum = tcpPacket.getSequenceNumber();
            long ackNum = tcpPacket.getAcknowledgementNumber();
            boolean ackValid = tcpPacket.isACK();
            System.out.printf("Arrival: %d, SN: %d, PL: %d, AN: %d, AV: %b, Dir: %d\n", arrivalTime, seqNum, payloadLength, ackNum, ackValid, dir);
            if (flows.containsKey(fiveTuple)) {
                System.out.println("Aggiungo");
                flows.get(fiveTuple).insertBytes(arrivalTime, payloadLength, dir);
                if (payloadLength != 0) {
                    System.out.printf("Inserisco ExpAck: %d\n ", seqNum + payloadLength);
                    flows.get(fiveTuple).insertExpectedAck((seqNum + payloadLength), arrivalTime, dir);
                }
                if (ackValid) {
                    flows.get(fiveTuple).computeRtt(ackNum, arrivalTime, dir);
                }
            } else {
                System.out.println("Inserisco");
                Flow flow = new Flow(fiveTuple);
                flow.insertBytes(arrivalTime, payloadLength, dir);
                flow.insertExpectedAck(seqNum, arrivalTime, dir);
                if (ackValid) {
                    flows.get(fiveTuple).computeRtt(ackNum, arrivalTime, dir);
                }
                flows.put(fiveTuple, flow);
            }
        } else if (packet.hasProtocol(Protocol.UDP)) {
            UDPPacket udpPacket = (UDPPacket) packet.getPacket(Protocol.UDP);
        }
        return true;
    }
}
