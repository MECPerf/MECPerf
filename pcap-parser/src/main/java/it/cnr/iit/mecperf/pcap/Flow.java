package it.cnr.iit.mecperf.pcap;

import io.pkts.protocol.Protocol;
import org.javatuples.Quintet;

import java.util.LinkedHashMap;

public class Flow {
    Quintet<Protocol, String, String, Integer, Integer> fiveTuple;
    LinkedHashMap<Long, Long> uplinkBytes;
    LinkedHashMap<Long, Long> downlinkBytes;
    LinkedHashMap<Long, Long> uplinkExpectedAcks;
    LinkedHashMap<Long, Long> downlinkExpectedAcks;
    LinkedHashMap<Long, Long> uplinkRtts;
    LinkedHashMap<Long, Long> downlinkRtts;
    public static final int DIR_UPLINK = 0;
    public static final int DIR_DOWNLINK = 1;
    public static final String TCP = "tcp";
    public static final String UDP = "udp";
    public static final int TYPE_BANDWIDTH = 0;
    public static final int TYPE_RTT = 1;

    public Flow(Quintet<Protocol, String, String, Integer, Integer> fiveTuple) {
        this.fiveTuple = fiveTuple;
        this.uplinkBytes = new LinkedHashMap<>();
        this.downlinkBytes = new LinkedHashMap<>();
        this.uplinkExpectedAcks = new LinkedHashMap<>();
        this.downlinkExpectedAcks = new LinkedHashMap<>();
        this.uplinkRtts = new LinkedHashMap<>();
        this.downlinkRtts = new LinkedHashMap<>();
    }

    public void insertBytes(long timestamp, long numBytes, int direction) {
        switch (direction) {
            case DIR_UPLINK:
                uplinkBytes.put(timestamp, numBytes);
                break;
            case DIR_DOWNLINK:
                downlinkBytes.put(timestamp, numBytes);
                break;
        }
    }

    public void insertExpectedAck(long expectedAck, long timestamp, int direction) {
        if (fiveTuple.getValue0() == Protocol.TCP) {
            switch (direction) {
                case DIR_UPLINK:
                    if (!uplinkExpectedAcks.containsKey(expectedAck))
                        uplinkExpectedAcks.put(expectedAck, timestamp);
                    break;
                case DIR_DOWNLINK:
                    if (!downlinkExpectedAcks.containsKey(expectedAck))
                        downlinkExpectedAcks.put(expectedAck, timestamp);
                    break;
            }
        }
    }

    public void computeRtt(long ackNum, long timestamp, int direction) {
        if (fiveTuple.getValue0() == Protocol.TCP) {
            switch (direction) {
                case DIR_UPLINK:
                    if (downlinkExpectedAcks.containsKey(ackNum)) {
                        long rtt = timestamp - downlinkExpectedAcks.get(ackNum);
                        downlinkRtts.put(timestamp, rtt);
                        downlinkExpectedAcks.remove(ackNum);
                    }
                    break;
                case DIR_DOWNLINK:
                    if (uplinkExpectedAcks.containsKey(ackNum)) {
                        long rtt = timestamp - uplinkExpectedAcks.get(ackNum);
                        uplinkRtts.put(timestamp, rtt);
                        uplinkExpectedAcks.remove(ackNum);
                    }
                    break;
            }
        }
    }

    public LinkedHashMap<Long, Long> getBytes(int direction) {
        if (direction == DIR_UPLINK)
            return uplinkBytes;
        else if (direction == DIR_DOWNLINK)
            return downlinkBytes;
        else
            return null;
    }

    public LinkedHashMap<Long, Long> getRtts(int direction) {
        if (fiveTuple.getValue0() == Protocol.TCP) {
            if (direction == DIR_UPLINK)
                return uplinkRtts;
            else if (direction == DIR_DOWNLINK)
                return downlinkRtts;
        }
        return null;
    }

    public MeasurementResult toMeasurementResult(int type) {
        String protocol;
        if (fiveTuple.getValue0() == Protocol.TCP)
            protocol = TCP;
        else
            protocol = UDP;
        if (type == TYPE_BANDWIDTH)
            return new MeasurementResult(fiveTuple.getValue1(), fiveTuple.getValue3(), fiveTuple.getValue2(),
                    fiveTuple.getValue4(), "test", protocol, uplinkBytes, downlinkBytes);
        if (type == TYPE_RTT && fiveTuple.getValue0() == Protocol.TCP)
            return new MeasurementResult(fiveTuple.getValue1(), fiveTuple.getValue3(), fiveTuple.getValue2(),
                    fiveTuple.getValue4(), "test", protocol, uplinkRtts, downlinkRtts);
        return null;
    }
}
