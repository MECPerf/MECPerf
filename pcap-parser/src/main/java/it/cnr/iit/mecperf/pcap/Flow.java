package it.cnr.iit.mecperf.pcap;

import io.pkts.protocol.Protocol;
import org.javatuples.Quintet;

import java.util.LinkedHashMap;

public class Flow {
    Quintet<Protocol, String, String, Integer, Integer> fiveTuple;
    LinkedHashMap<Long, Integer> uplinkBytes;
    LinkedHashMap<Long, Integer> downlinkBytes;
    LinkedHashMap<Long, Long> uplinkExpectedAcks;
    LinkedHashMap<Long, Long> downlinkExpectedAcks;
    LinkedHashMap<Long, Long> uplinkRtts;
    LinkedHashMap<Long, Long> downlinkRtts;
    public static final int DIR_UPLINK = 0;
    public static final int DIR_DOWNLINK = 1;

    public Flow(Quintet<Protocol, String, String, Integer, Integer> fiveTuple) {
        this.fiveTuple = fiveTuple;
        this.uplinkBytes = new LinkedHashMap<>();
        this.downlinkBytes = new LinkedHashMap<>();
        this.uplinkExpectedAcks = new LinkedHashMap<>();
        this.downlinkExpectedAcks = new LinkedHashMap<>();
        this.uplinkRtts = new LinkedHashMap<>();
        this.downlinkRtts = new LinkedHashMap<>();
    }

    public void insertBytes(long timestamp, int numBytes, int direction) {
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

    public LinkedHashMap<Long, Integer> getBytes(int direction) {
        if (fiveTuple.getValue0() == Protocol.TCP) {
            if (direction == DIR_UPLINK)
                return uplinkBytes;
            else if (direction == DIR_DOWNLINK)
                return downlinkBytes;
        }
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
}
