package it.cnr.iit.mecperf.pcap;

import io.pkts.PacketHandler;
import io.pkts.buffer.Buffer;
import io.pkts.packet.*;
import io.pkts.protocol.Protocol;
import org.javatuples.Pair;
import org.javatuples.Quintet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class MECPerfPacketHandler implements PacketHandler {
    Set<Pair<String, Integer>> servers;
    HashMap<Quintet<Protocol, String, String, Integer, Integer>, Flow> flows;

    public MECPerfPacketHandler(Set<Pair<String, Integer>> servers,
                                HashMap<Quintet<Protocol, String, String, Integer, Integer>, Flow> flows) {
        this.servers = servers;
        this.flows = flows;
    }

    private int getDirection(TransportPacket packet) {
        String srcIp = packet.getSourceIP();
        int srcPort = packet.getSourcePort();
        String dstIp = packet.getDestinationIP();
        int dstPort = packet.getDestinationPort();
        if (servers.contains(new Pair<>(srcIp, srcPort)))
            return Flow.DIR_DOWNLINK;
        else if (servers.contains(new Pair<>(dstIp, dstPort)))
            return Flow.DIR_UPLINK;
        else
            return -1;
    }

    private Quintet<Protocol, String, String, Integer, Integer> getFiveTuple(TransportPacket packet, int dir) {
        String srcIp = packet.getSourceIP();
        int srcPort = packet.getSourcePort();
        String dstIp = packet.getDestinationIP();
        int dstPort = packet.getDestinationPort();
        if (dir == Flow.DIR_DOWNLINK) {
            return new Quintet<>(Protocol.TCP, dstIp, srcIp, dstPort, srcPort);
        } else if (dir == Flow.DIR_UPLINK) {
            return new Quintet<>(Protocol.TCP, srcIp, dstIp, srcPort, dstPort);
        }
        return null;
    }

    private int getPayloadLength(TransportPacket packet) {
        Buffer buf = packet.getPayload();
        return buf != null ? buf.capacity() : 0;
    }

    @Override
    public boolean nextPacket(Packet packet) throws IOException {
        long arrivalTime = packet.getArrivalTime();
        if (packet.hasProtocol(Protocol.TCP)) {
            TCPPacket tcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);
            int dir = getDirection(tcpPacket);
            if (dir == Flow.DIR_UPLINK || dir == Flow.DIR_DOWNLINK) {
                Quintet<Protocol, String, String, Integer, Integer> fiveTuple = getFiveTuple(tcpPacket, dir);
                int payloadLength = getPayloadLength(tcpPacket);
                long seqNum = tcpPacket.getSequenceNumber();
                long ackNum = tcpPacket.getAcknowledgementNumber();
                boolean ackValid = tcpPacket.isACK();
                if (flows.containsKey(fiveTuple)) {
                    flows.get(fiveTuple).insertBytes(arrivalTime, payloadLength, dir);
                    if (payloadLength != 0) {
                        flows.get(fiveTuple).insertExpectedAck((seqNum + payloadLength), arrivalTime, dir);
                    }
                    if (ackValid) {
                        flows.get(fiveTuple).computeRtt(ackNum, arrivalTime, dir);
                    }
                } else {
                    Flow flow = new Flow(fiveTuple);
                    flow.insertBytes(arrivalTime, payloadLength, dir);
                    flow.insertExpectedAck(seqNum, arrivalTime, dir);
                    if (ackValid) {
                        flow.computeRtt(ackNum, arrivalTime, dir);
                    }
                    flows.put(fiveTuple, flow);
                }
            }

        } else if (packet.hasProtocol(Protocol.UDP)) {
            UDPPacket udpPacket = (UDPPacket) packet.getPacket(Protocol.UDP);
            int dir = getDirection(udpPacket);
            if (dir == Flow.DIR_UPLINK || dir == Flow.DIR_DOWNLINK) {
                Quintet<Protocol, String, String, Integer, Integer> fiveTuple = getFiveTuple(udpPacket, dir);
                int payloadLength = getPayloadLength(udpPacket);
                if (flows.containsKey(fiveTuple)) {
                    flows.get(fiveTuple).insertBytes(arrivalTime, payloadLength, dir);
                } else {
                    Flow flow = new Flow(fiveTuple);
                    flow.insertBytes(arrivalTime, payloadLength, dir);
                    flows.put(fiveTuple, flow);
                }
            }
        }
        return true;
    }
}
