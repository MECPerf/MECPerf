package it.cnr.iit.mecperf.pcap;

import io.pkts.PacketHandler;
import io.pkts.buffer.Buffer;
import io.pkts.packet.*;
import io.pkts.protocol.Protocol;
import org.javatuples.Pair;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MECPerfPacketHandler implements PacketHandler {
    Map<Pair<String, Integer>, String> servers;
    HashMap<Quintet<Protocol, String, String, Integer, Integer>, Flow> flows;

    public MECPerfPacketHandler(Map<Pair<String, Integer>, String> servers,
                                HashMap<Quintet<Protocol, String, String, Integer, Integer>, Flow> flows) {
        this.servers = servers;
        this.flows = flows;
        for (Pair<String, Integer> server : this.servers.keySet()) {
            Logger.debug("Looking for server {}:{}:{}", server.getValue0(), server.getValue1(), this.servers.get(server));
        }
    }

    private Triplet<Integer, Quintet<Protocol, String, String, Integer, Integer>, String> getInfo(TransportPacket packet) {
        String srcIp = packet.getSourceIP();
        int srcPort = packet.getSourcePort();
        String dstIp = packet.getDestinationIP();
        int dstPort = packet.getDestinationPort();
        Protocol protocol = packet.getProtocol();
        int dir;
        Quintet<Protocol, String, String, Integer, Integer> fiveTuple;
        String service;
        if (servers.containsKey(new Pair<>(srcIp, srcPort)))
            return new Triplet<>(Flow.DIR_DOWNLINK, new Quintet<>(protocol, dstIp, srcIp, dstPort, srcPort),
                    servers.get(new Pair<>(srcIp, srcPort)));
        if (servers.containsKey(new Pair<>(dstIp, dstPort)))
            return new Triplet<>(Flow.DIR_UPLINK, new Quintet<>(protocol, srcIp, dstIp, srcPort, dstPort),
                    servers.get(new Pair<>(dstIp, dstPort)));
        return null;
    }

    private Quintet<Protocol, String, String, Integer, Integer> getFiveTuple(TransportPacket packet, int dir) {
        String srcIp = packet.getSourceIP();
        int srcPort = packet.getSourcePort();
        String dstIp = packet.getDestinationIP();
        int dstPort = packet.getDestinationPort();
        Protocol protocol = packet.getProtocol();
        if (dir == Flow.DIR_DOWNLINK) {
            return new Quintet<>(protocol, dstIp, srcIp, dstPort, srcPort);
        } else if (dir == Flow.DIR_UPLINK) {
            return new Quintet<>(protocol, srcIp, dstIp, srcPort, dstPort);
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
            Triplet<Integer, Quintet<Protocol, String, String, Integer, Integer>, String> info = getInfo(tcpPacket);
            if (info != null) {
                int dir = info.getValue0();
                Quintet<Protocol, String, String, Integer, Integer> fiveTuple = info.getValue1();
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
                    Flow flow = new Flow(fiveTuple, info.getValue2());
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
            Triplet<Integer, Quintet<Protocol, String, String, Integer, Integer>, String> info = getInfo(udpPacket);
            if (info != null) {
                int dir = info.getValue0();
                Quintet<Protocol, String, String, Integer, Integer> fiveTuple = info.getValue1();
                int payloadLength = getPayloadLength(udpPacket);
                if (flows.containsKey(fiveTuple)) {
                    flows.get(fiveTuple).insertBytes(arrivalTime, payloadLength, dir);
                } else {
                    Flow flow = new Flow(fiveTuple, info.getValue2());
                    flow.insertBytes(arrivalTime, payloadLength, dir);
                    flows.put(fiveTuple, flow);
                }
            }
        }
        return true;
    }
}
