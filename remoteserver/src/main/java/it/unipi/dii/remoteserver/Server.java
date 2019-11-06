package it.unipi.dii.remoteserver;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */




import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import it.unipi.dii.common.Measurements;
import it.unipi.dii.common.Measure;
import it.unipi.dii.common.ControlMessages;

/**
 *
 * @author Bernardi Leonardo
 */


public class Server {
    //command listener, tcp data and udp data ports
    private static final int CMDPORT = 6789;
    private static final int TCPPORT = 6788;
    private static final int UDPPORT = 6787;
    private static final String AGGREGATORIP = "131.114.73.3";
    private static final int AGGRPORT = 6766;

    //used in udp measurements
    private static int udp_bandwidth_pktsize = 1024;
    private static int tcp_bandwidth_pktsize = 1024;
    private static int tcp_bandwidth_stream = 1024*1024;
    private static int tcp_rtt_pktsize = 1;
    private static int tcp_rtt_num_pack = 100;
    private static int udp_rtt_pktsize = 1;
    private static int udp_rtt_num_pack = 100;


    public static void main(String[] args){
        ServerSocket cmdListener = null;//ServerSocket per la ricezione dei comandi
        ServerSocket tcpListener = null;//ServerSocket per le misurazioni TCP
        DatagramSocket udpListener = null;//ServerSocket per le misurazioni UDP

        //socket initialization
        try {
            cmdListener = new ServerSocket(CMDPORT);
            tcpListener = new ServerSocket(TCPPORT);
            udpListener = new DatagramSocket(UDPPORT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Log
        System.out.println("Server CMD: inizializzato sulla porta " + cmdListener.getLocalPort());
        System.out.println("Server TCP: inizializzato sulla porta " + tcpListener.getLocalPort());
        System.out.println("Server UDP: inizializzato sulla porta " + udpListener.getLocalPort());

        while (true) {
            ControlMessages controlSocketObserver = null;
            try {
                controlSocketObserver = new ControlMessages(cmdListener.accept());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            String cmd = controlSocketObserver.receiveCMD();
            String separator ="#";
            String[] cmdSplitted = cmd.split(separator);

            double latency = 0.0;

            //Start test based on command received
            switch(cmdSplitted[0]) {
                case "TCPBandwidthSender": {
                    //the observer sends to the remote server
                    Map<Long, Integer> mappa;
                    tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + Integer.parseInt(cmdSplitted[2]));
                    System.out.println("Number of packes : " + Integer.parseInt(cmdSplitted[3]));

                    try {
                        Socket tcpReceiverConnectionSocket = tcpListener.accept();

                        controlSocketObserver.sendCMD(controlSocketObserver.messages.START.toString());
                        mappa = Measurements.TCPBandwidthReceiver(tcpReceiverConnectionSocket, tcp_bandwidth_pktsize);
                        controlSocketObserver.sendCMD(controlSocketObserver.messages.SUCCEDED.toString());
                        System.out.println("measure (receiver) completed");

                        sendDataToAggregator("TCPBandwidth", 0, "Observer",
                                "Server", -1, mappa, cmdSplitted[1],
                                tcp_bandwidth_pktsize, Integer.parseInt(cmdSplitted[3]));
                        System.out.println("Sent result to aggregator");

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.out.println("TCPBandwidthSender: completed");
                    break;
                }
                case "TCPBandwidthReceiver": {
                    tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    tcp_bandwidth_stream = Integer.parseInt(cmdSplitted[3]) * tcp_bandwidth_pktsize;
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + Integer.parseInt(cmdSplitted[2]));
                    System.out.println("Number of packes : " + Integer.parseInt(cmdSplitted[3]));

                    try {
                        Socket tcpSenderConnectionSocket = tcpListener.accept();
                        Measurements.TCPBandwidthSender(tcpSenderConnectionSocket,
                                tcp_bandwidth_stream, tcp_bandwidth_pktsize);
                        if (controlSocketObserver.receiveCMD().compareTo(controlSocketObserver
                                .messages.SUCCEDED.toString()) != 0) {
                            System.out.println("measure failed");
                            break;
                        }
                        System.out.println("measure (receiver) completed");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.out.println("TCPBandwidthReceiver: completed");
                    break;
                }
                case "UDPCapacityPPSender": {
                    //UDP latency test using Packet Pair, MRS has to receive
                    udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    Map<Long, Integer> measureResult = null;
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + udp_bandwidth_pktsize);

                    try {
                        controlSocketObserver.sendCMD(controlSocketObserver.messages.START
                                .toString());
                        measureResult = Measurements.UDPCapacityPPReceiver(
                                udpListener, udp_bandwidth_pktsize);
                        controlSocketObserver.sendCMD(controlSocketObserver.messages.SUCCEDED
                                .toString());
                        System.out.println("measure (receiver DONE");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    //send data to Aggregator
                    sendDataToAggregator("UDPBandwidth", 0, "Observer",
                            "Server", -1, measureResult,
                            cmdSplitted[1], udp_bandwidth_pktsize, 2);
                    System.out.println("Sent results to aggregator");

                    System.out.println("UDPBandwidthSender: completed");
                    break;
                }
                case "UDPCapacityPPReceiver": {
                    //UDP Latency test using Packet Pair, MRS has to send
                    udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + Integer.parseInt(cmdSplitted[2]));

                    //MRS first has to receive a packet from the client to know Client's Address and Port
                    byte[] buf = new byte[1000];
                    DatagramPacket dgp = new DatagramPacket(buf, buf.length);
                    try {
                        udpListener.receive(dgp);
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    //remote -> observer bandwidth measure
                    udpListener.connect(dgp.getAddress(), dgp.getPort());
                    Measurements.UDPCapacityPPSender(udpListener, udp_bandwidth_pktsize);
                    udpListener.disconnect();
                    if (controlSocketObserver.receiveCMD().compareTo(controlSocketObserver
                            .messages.SUCCEDED.toString()) != 0) {
                        System.out.println("measure failed");
                        break;
                    }
                    System.out.println("UDPBandwidthReceiver: completed");

                    break;
                }

                //UDP RTT test, MRS has to receive
                case "UDPRTTSender":{
                    udp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]);
                    udp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.println("\nReceived command: " + cmdSplitted[0]);
                    System.out.println("Packet size: " + udp_rtt_pktsize);
                    System.out.println("Number of packes: " + udp_rtt_num_pack);

                    try {
                        controlSocketObserver.sendCMD(controlSocketObserver.messages.START.toString());
                        Measurements.UDPRTTReceiver(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        if (controlSocketObserver.receiveCMD().compareTo(controlSocketObserver
                                .messages.SUCCEDED.toString()) != 0) {
                            System.out.println("measure failed");
                            break;
                        }
                        System.out.println("UDPRTTSender: completed");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }

                //UDP RTT test, MRS has to send
                case "UDPRTTMRS":
                    udp_rtt_pktsize = Integer.parseInt(cmdSplitted[3]);
                    udp_rtt_num_pack = Integer.parseInt(cmdSplitted[4]);

                    System.out.println("udp_rtt_pktsize: " +udp_rtt_pktsize);
                    System.out.println("udp_rtt_num_pack: " +udp_rtt_num_pack);
                    try {
                        //MRS has to first receive a packet from the client to know Client's Address and Port
                        byte[] bufrtt = new byte[1000];
                        DatagramPacket dgprtt = new DatagramPacket(bufrtt, bufrtt.length);
                        try {
                            udpListener.receive(dgprtt);
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        //connecting and actually starting the test
                        udpListener.connect(dgprtt.getAddress(), dgprtt.getPort());
                        latency = Measurements.UDPRTTSender(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        udpListener.disconnect();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    //send data to Aggregator
                    sendDataToAggregator("UDPRTT", Integer.parseInt(cmdSplitted[1]), "Server", "Observer", latency , null, cmdSplitted[2], udp_rtt_pktsize, udp_rtt_num_pack);
                    //Log
                    System.out.println("Server UDP RTT : " + latency + " Ms");
                    break;


                case "TCPRTTSender": {
                    // the observer starts a RTT measure using the remote server s receiver
                    tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]);
                    tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.println("\nReceived command: " + cmdSplitted[0]);
                    System.out.println("Packet size: " + tcp_rtt_pktsize);
                    System.out.println("Number of packes: " + tcp_rtt_num_pack);



                    try {
                        controlSocketObserver.sendCMD(controlSocketObserver.messages.START.toString());
                        Socket tcpRTT = tcpListener.accept();

                        //controlSocketObserver.sendCMD(controlSocketObserver.messages.START.toString());
                        Measurements.TCPRTTReceiver(tcpRTT, tcp_rtt_pktsize, tcp_rtt_num_pack);
                        if (controlSocketObserver.receiveCMD().compareTo(controlSocketObserver
                                                              .messages.SUCCEDED.toString()) != 0) {
                            System.out.println("measure failed");
                            break;
                        }
                        System.out.println("TCPRTTSender: completed");

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }

                case "TCPRTTMRS":
                    //the observer starts a TCP RTT using the remote server as sender
                    tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[3]);
                    tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[4]);
                    System.out.println("tcp_rtt_pktsize: " +tcp_rtt_pktsize);
                    System.out.println("tcp_rtt_num_pack: " +tcp_rtt_num_pack);
                    try {
                        Socket tcpRTT = tcpListener.accept();
                        latency = Measurements.TCPRTTSender(tcpRTT, tcp_rtt_pktsize, tcp_rtt_num_pack);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    //send data to Aggregator
                    sendDataToAggregator("TCPRTT", Integer.parseInt(cmdSplitted[1]), "Server", "Observer", latency , null, cmdSplitted[2], tcp_rtt_pktsize, tcp_rtt_num_pack);

                    //Log
                    System.out.println("Server TCP RTT : " + latency + " Ms");
                    break;
            }

            controlSocketObserver.closeConnection();
        }
    }

    protected static void sendDataToAggregator(String type, int id, String sender, String receiver, double latency, Map<Long, Integer> bandwidth, String keyword, int len_pack, int num_pack){
        Socket socket = null;
        ObjectOutputStream objOutputStream = null;
        try {
            socket = new Socket(InetAddress.getByName(AGGREGATORIP), AGGRPORT);
            objOutputStream = new ObjectOutputStream(socket.getOutputStream());
            Measure measure = new Measure();
            measure.setType(type);
            //measure.setID(id);
            measure.setSender(sender);
            measure.setReceiver(receiver);
            measure.setLatency(latency);
            measure.setBandwidth(bandwidth);
            measure.setExtra(keyword);
            measure.setLen_pack(len_pack);
            measure.setNum_pack(num_pack);

            // write the message we want to send
            objOutputStream.writeObject(measure);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                objOutputStream.close(); // close the output stream when we're done.
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

