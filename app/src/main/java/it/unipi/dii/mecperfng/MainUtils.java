package it.unipi.dii.mecperfng;



import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;


import it.unipi.dii.common.Measurements;
import it.unipi.dii.common.ControlMessages;



public class MainUtils {
    /*****
     *
     * @param direction
     * @param keyword
     * @param commandPort
     * @param observerAddress
     * @param observerPort
     * @param tcp_bandwidth_pktsize
     * @param tcp_bandwidth_num_pkt
     * @return: 0 = successfull
     *          -1 = IOException
     */
    public static int tcpBandwidthMeasure(String direction, String keyword, int commandPort,
                                          String observerAddress, int observerPort,
                                          int tcp_bandwidth_pktsize, int tcp_bandwidth_num_pkt) {
        try {
            int tcp_bandwidth_stream = tcp_bandwidth_num_pkt * tcp_bandwidth_pktsize;

            Socket communicationSocket = new Socket(InetAddress.getByName(observerAddress), observerPort);
            ControlMessages controlSocketObserver = new ControlMessages(observerAddress, commandPort);

            if (direction.equals("Sender")) {
                controlSocketObserver.sendCMD("TCPBandwidthSender#" + keyword + "#" +
                        tcp_bandwidth_pktsize + "#" + tcp_bandwidth_num_pkt);
                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages.START
                        .toString()) != 0) {
                    System.out.println("Start measure with Observer FAILED");

                    controlSocketObserver.closeConnection();
                    return -1;
                }
                Measurements.TCPBandwidthSender(communicationSocket, tcp_bandwidth_stream, tcp_bandwidth_pktsize);

                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages
                        .COMPLETED.toString()) == 0) {
                    controlSocketObserver.closeConnection();
                    return 0;
                }

                controlSocketObserver.closeConnection();
                return -1;
            } else {
                controlSocketObserver.sendCMD("TCPBandwidthReceiver#" + keyword +
                        "#" + tcp_bandwidth_pktsize + "#" + tcp_bandwidth_num_pkt);
                Map<Long, Integer> longIntegerMap = Measurements.TCPBandwidthReceiver(
                        communicationSocket, tcp_bandwidth_pktsize);
                controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());

                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages
                        .COMPLETED.toString()) != 0) {
                    System.out.println("measure with Observer FAILED");

                    controlSocketObserver.closeConnection();
                    return -1;
                }

                controlSocketObserver.sendCMD(ControlMessages.Messages.MEASUREDBANDWIDTH
                                              .toString());
                Socket tmpsocket = new Socket(InetAddress.getByName(observerAddress), commandPort);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(tmpsocket.getOutputStream());
                objectOutputStream.writeObject(longIntegerMap);

                objectOutputStream.close();
                tmpsocket.close();
                controlSocketObserver.closeConnection();
                return 0;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return -1;
        }
    }


    public static int udpBandwidthMeasure(String direction, String keyword, int commandPort,
                                          String observerAddress, int observerPort,
                                          int udp_bandwidth_pktsize) {
        try {
            ControlMessages controlSocketObserver = new ControlMessages(observerAddress, commandPort);
            DatagramSocket connectionSocket = new DatagramSocket();
            connectionSocket.connect(InetAddress.getByName(observerAddress), observerPort);


            if (direction.equals("Sender")) {
                controlSocketObserver.sendCMD("UDPCapacityPPSender#" + keyword + "#" +
                        udp_bandwidth_pktsize);
                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages.START
                        .toString()) != 0) {
                    System.out.println("Start measure with Observer FAILED");

                    controlSocketObserver.closeConnection();
                    return -1;
                }
                Measurements.UDPCapacityPPSender(connectionSocket, udp_bandwidth_pktsize);

                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages.COMPLETED
                        .toString()) == 0) {
                    controlSocketObserver.closeConnection();
                    return 0;
                }

                controlSocketObserver.closeConnection();
                return -1;
            } else {
                controlSocketObserver.sendCMD("UDPCapacityPPReceiver#" + keyword + "#" +
                        udp_bandwidth_pktsize);
                //Client has to send a packet to server to let the server knows Client's IP and Port
                String outString = "Dummy message";
                byte[] buf = outString.getBytes();
                connectionSocket.send(new DatagramPacket(buf, buf.length));
                Map<Long, Integer> measureResult = Measurements.UDPCapacityPPReceiver(
                        connectionSocket, udp_bandwidth_pktsize);
                if (measureResult == null) {
                    System.out.println("Measure filed");
                    controlSocketObserver.sendCMD(ControlMessages.Messages.FAILED.toString());
                    controlSocketObserver.closeConnection();
                    return -1;
                }
                controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());

                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages.COMPLETED
                        .toString()) != 0) {
                    System.out.println("measure with Observer FAILED");

                    controlSocketObserver.closeConnection();
                    return -1;
                }

                controlSocketObserver.sendCMD(ControlMessages.Messages.MEASUREDBANDWIDTH
                                              .toString());
                Socket tmpsocket = new Socket(InetAddress.getByName(observerAddress), commandPort);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(tmpsocket.getOutputStream());
                objectOutputStream.writeObject(measureResult);

                objectOutputStream.close();
                tmpsocket.close();
                controlSocketObserver.closeConnection();
                return 0;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return -1;
        }
    }


    public static int tcpRTTMeasure(String direction, String keyword, int commandPort,
                                    String observerAddress, int observerPort,
                                    int tcp_rtt_pktsize, int tcp_rtt_num_pack) {

        try {
            Socket communicationSocket = new Socket(InetAddress.getByName(observerAddress), observerPort);
            ControlMessages controlSocketObserver = new ControlMessages(observerAddress, commandPort);

            if (direction.equals("Sender")) {
                // the client application starts a TCP RTT measure (as sender) with the observer
                controlSocketObserver.sendCMD("TCPRTTSender#" + keyword + "#" + tcp_rtt_pktsize
                        + "#" + tcp_rtt_num_pack);
                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages.START
                        .toString()) != 0) {
                    System.out.println("Start measure with Observer FAILED");

                    controlSocketObserver.closeConnection();
                    return -1;
                }

                //perform measure
                double latency = Measurements.TCPRTTSender(communicationSocket, tcp_rtt_pktsize,
                        tcp_rtt_num_pack);
                controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());


                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages
                        .COMPLETED.toString()) != 0) {
                    controlSocketObserver.closeConnection();
                    return -1;
                }

                controlSocketObserver.sendCMD(ControlMessages.Messages.MEASUREDLATENCY
                                              .toString() + '#' + latency );

                controlSocketObserver.closeConnection();
                return 0;
            } else {
                //the observer starts a TCP RTT measure using the mobile application
                // as receiver
                controlSocketObserver.sendCMD("TCPRTTReceiver#" + keyword + "#" +
                        tcp_rtt_pktsize + "#" + tcp_rtt_num_pack);

                Measurements.TCPRTTReceiver(communicationSocket, tcp_rtt_pktsize, tcp_rtt_num_pack);

                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages.COMPLETED
                        .toString()) != 0) {
                    System.out.println("measure with Observer FAILED");

                    controlSocketObserver.closeConnection();
                    return -1;
                }

                controlSocketObserver.closeConnection();
                return 0;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return -1;
        }
    }


    public static int udpRTTMeasure(String direction, String keyword, int commandPort,
                                    String observerAddress, int observerPort,
                                    int udp_rtt_pktsize, int udp_rtt_num_pack) {

        try {
            ControlMessages controlSocketObserver = new ControlMessages(observerAddress, commandPort);
            DatagramSocket udpsocket = new DatagramSocket();
            udpsocket.connect(InetAddress.getByName(observerAddress), observerPort);

            if (direction.equals("Sender")) {
                // the client application starts a TCP RTT measure as sender. The observer is the
                // receiver
                controlSocketObserver.sendCMD("UDPRTTSender#" + keyword + "#" +
                        udp_rtt_pktsize + "#" + udp_rtt_num_pack);
                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages.START
                        .toString()) != 0) {
                    System.out.println("Start measure with Observer FAILED");

                    controlSocketObserver.closeConnection();
                    return -1;
                }

                //perform measure
                double latency = Measurements.UDPRTTSender(udpsocket, udp_rtt_pktsize,
                        udp_rtt_num_pack);
                if (latency == -1) {
                    System.out.println("Measure filed");
                    controlSocketObserver.sendCMD(ControlMessages.Messages.FAILED.toString());
                    controlSocketObserver.closeConnection();
                    return -1;
                }
                controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());

                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages
                        .COMPLETED.toString()) != 0) {
                    controlSocketObserver.closeConnection();
                    return -1;
                }
                controlSocketObserver.sendCMD(ControlMessages.Messages.MEASUREDLATENCY
                                              .toString() + '#' + latency);

                controlSocketObserver.closeConnection();
                return 0;
            } else {
                // the client application starts a TCP RTT measure with the observer as
                //sender

                controlSocketObserver.sendCMD("UDPRTTReceiver#" + keyword + "#" + udp_rtt_pktsize +
                        "#" + udp_rtt_num_pack);
                String outString = "DummyPacket";
                byte[] buf = outString.getBytes();
                udpsocket.send(new DatagramPacket(buf, buf.length));
                int ret = Measurements.UDPRTTReceiver(udpsocket, udp_rtt_pktsize, udp_rtt_num_pack);
                if (ret < 0) {
                    controlSocketObserver.closeConnection();
                    return -1;
                }

                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages.COMPLETED
                        .toString()) != 0) {
                    System.out.println("measure with Observer FAILED");

                    controlSocketObserver.closeConnection();
                    return -1;
                }

                controlSocketObserver.closeConnection();
                return 0;
            }
        } catch (IOException ioe) {
            return -1;
        }
    }

}