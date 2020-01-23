package it.unipi.dii.mecperfng;



import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

import it.unipi.dii.common.Measurements;
import it.unipi.dii.common.ControlMessages;



public class MainUtils {
     private static InetAddress getInterfacesInfo(String targetInterface){
        try {
            Enumeration<NetworkInterface> networkInterfaces =  NetworkInterface.getNetworkInterfaces();

            while(networkInterfaces.hasMoreElements())
            {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                    if (networkInterface.isUp()) {
                        if (networkInterface.getName().equals(targetInterface)) {
                            Enumeration ee = networkInterface.getInetAddresses();
                            while (ee.hasMoreElements()) {
                                InetAddress i = (InetAddress) ee.nextElement();


                                if (i instanceof Inet4Address) {
                                    return i;
                                }
                            }
                        }
                    }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

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
                                          int tcp_bandwidth_pktsize, int tcp_bandwidth_num_pkt,
                                          String interfaceName) {
        try {
            int tcp_bandwidth_stream = tcp_bandwidth_num_pkt * tcp_bandwidth_pktsize;

            Socket communicationSocket = new Socket();
            ControlMessages controlSocketObserver;

            if (interfaceName != null) {
                InetAddress sourceIPv4Address =getInterfacesInfo(interfaceName);
                if (sourceIPv4Address == null)
                {
                    System.out.println("Error: Interface not found");
                    return -1;
                }

                communicationSocket.bind(new InetSocketAddress(sourceIPv4Address, 0));
                controlSocketObserver = new ControlMessages(observerAddress, commandPort, sourceIPv4Address, 0);

            } else
                controlSocketObserver = new ControlMessages(observerAddress, commandPort);
            communicationSocket.connect(new InetSocketAddress(InetAddress.getByName(observerAddress), observerPort));


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

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(controlSocketObserver.getSocket().getOutputStream());
                objectOutputStream.writeObject(longIntegerMap);

                objectOutputStream.close();
                controlSocketObserver.closeConnection();
                return 0;
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
            return -1;
        }
    }


    public static int udpBandwidthMeasure(String direction, String keyword, int commandPort,
                                          String observerAddress, int observerPort,
                                          int udp_bandwidth_pktsize, String interfaceName, int num_tests_udp_capacity) {
        try {
            ControlMessages controlSocketObserver;
            DatagramSocket connectionSocket;

            if (interfaceName != null) {
                InetAddress sourceIPv4Address =getInterfacesInfo(interfaceName);
                if (sourceIPv4Address == null)
                {
                    System.out.println("Error: Interface not found");
                    return -1;
                }
                connectionSocket = new DatagramSocket(null);
                connectionSocket.bind(new InetSocketAddress(sourceIPv4Address, 0));
                controlSocketObserver = new ControlMessages(observerAddress, commandPort, sourceIPv4Address, 0);

            } else {
                controlSocketObserver = new ControlMessages(observerAddress, commandPort);
                connectionSocket = new DatagramSocket();
            }
            connectionSocket.connect(new InetSocketAddress(InetAddress.getByName(observerAddress), observerPort));



            if (direction.equals("Sender")) {
                controlSocketObserver.sendCMD("UDPCapacityPPSender#" + keyword + "#" +
                        udp_bandwidth_pktsize + "#" + num_tests_udp_capacity);

                int ret = Measurements.UDPCapacityPPSender(connectionSocket, udp_bandwidth_pktsize, num_tests_udp_capacity, controlSocketObserver);
                if (ret < 0)
                    return ret;

                if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages.COMPLETED
                        .toString()) == 0) {
                    controlSocketObserver.closeConnection();
                    return 0;
                }

                System.out.println("complete signal received");
                controlSocketObserver.closeConnection();
                return -1;
            } else {
                controlSocketObserver.sendCMD("UDPCapacityPPReceiver#" + keyword + "#" +
                        udp_bandwidth_pktsize + "#" + num_tests_udp_capacity);
                //Client has to send a packet to server to let the server knows Client's IP and Port
                String outString = "Dummy message";
                byte[] buf = outString.getBytes();
                connectionSocket.send(new DatagramPacket(buf, buf.length));
                Map<Long, Integer> measureResult = Measurements.UDPCapacityPPReceiver(
                        connectionSocket, udp_bandwidth_pktsize, num_tests_udp_capacity, controlSocketObserver);
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

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(controlSocketObserver.getSocket().getOutputStream());
                objectOutputStream.writeObject(measureResult);

                objectOutputStream.close();
                controlSocketObserver.closeConnection();
                return 0;
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
            return -1;
        }
    }


    public static int tcpRTTMeasure(String direction, String keyword, int commandPort,
                                    String observerAddress, int observerPort,
                                    int tcp_rtt_pktsize, int tcp_rtt_num_pack, String interfaceName) {

        try {
            Socket communicationSocket = new Socket();
            ControlMessages controlSocketObserver;

            if (interfaceName != null) {
                InetAddress sourceIPv4Address =getInterfacesInfo(interfaceName);
                if (sourceIPv4Address == null)
                {
                    System.out.println("Error: Interface not found");
                    return -1;
                }

                communicationSocket.bind(new InetSocketAddress(sourceIPv4Address, 0));
                controlSocketObserver = new ControlMessages(observerAddress, commandPort, sourceIPv4Address, 0);

            } else
                controlSocketObserver = new ControlMessages(observerAddress, commandPort);
            communicationSocket.connect(new InetSocketAddress(InetAddress.getByName(observerAddress), observerPort));



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
        } catch (Exception ioe) {
            ioe.printStackTrace();
            return -1;
        }
    }


    public static int udpRTTMeasure(String direction, String keyword, int commandPort,
                                    String observerAddress, int observerPort,
                                    int udp_rtt_pktsize, int udp_rtt_num_pack, String interfaceName) {

        try {
            ControlMessages controlSocketObserver;
            DatagramSocket udpsocket;

            if (interfaceName != null) {

                InetAddress sourceIPv4Address =getInterfacesInfo(interfaceName);
                if (sourceIPv4Address == null)
                {
                    System.out.println("Error: Interface not found");
                    return -1;
                }

                udpsocket =new DatagramSocket(null);
                udpsocket.bind(new InetSocketAddress(sourceIPv4Address, 0));
                controlSocketObserver = new ControlMessages(observerAddress, commandPort, sourceIPv4Address, 0);

            } else {
                controlSocketObserver = new ControlMessages(observerAddress, commandPort);
                udpsocket = new DatagramSocket();
            }
            udpsocket.connect(new InetSocketAddress(InetAddress.getByName(observerAddress), observerPort));


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
        } catch (Exception ioe) {
            return -1;
        }
    }

}