package it.unipi.dii.remoteserver;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */




import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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



    public static void main(String[] args){
        ServerSocket cmdListener = null;//ServerSocket per la ricezione dei comandi
        ServerSocket tcpListener = null;//ServerSocket per le misurazioni TCP
        DatagramSocket udpListener = null;//ServerSocket per le misurazioni UDP
        //socket initialization
        try {
            cmdListener = new ServerSocket(CMDPORT);
            tcpListener = new ServerSocket(TCPPORT);
            udpListener = new DatagramSocket(UDPPORT);

            System.out.println("Server CMD: inizializzato sulla porta " +
                               cmdListener.getLocalPort());
            System.out.println("Server TCP: inizializzato sulla porta " +
                               tcpListener.getLocalPort());
            System.out.println("Server UDP: inizializzato sulla porta " +
                               udpListener.getLocalPort());
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }



        while (true) {
            ControlMessages controlSocketObserver = null;
            String cmd = "";
            String separator ="#";

            try {
                controlSocketObserver = new ControlMessages(cmdListener.accept());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                cmd = controlSocketObserver.receiveCMD();
            }
            catch (EOFException | NullPointerException e)
            {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                controlSocketObserver.closeConnection();

                continue;
            }

            String[] cmdSplitted = cmd.split(separator);
            double latency = 0.0;

            //Start test based on command received
            switch(cmdSplitted[0]) {
                case "TCPBandwidthSender": {
                    //the observer sends to the remote server
                    Map<Long, Integer> mappa;
                    int tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    System.out.print("\nReceived command : " + cmdSplitted[0]);
                    System.out.print("\t[Packet size : " + tcp_bandwidth_pktsize);
                    System.out.println(", Number of packes : " + Integer.parseInt(cmdSplitted[3]) + "]");

                    try {
                        Socket tcpReceiverConnectionSocket = tcpListener.accept();

                        controlSocketObserver.sendCMD(ControlMessages.Messages.START.toString());
                        mappa = Measurements.TCPBandwidthReceiver(tcpReceiverConnectionSocket, tcp_bandwidth_pktsize);
                        controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());

                        sendDataToAggregator("TCPBandwidth","Observer","Server",
                                            -1, mappa, cmdSplitted[1],
                                tcp_bandwidth_pktsize, Integer.parseInt(cmdSplitted[3]));
                        System.out.println("Sent result to aggregator");

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.out.println("TCPBandwidthSender: completed");
                    break;
                }
                case "TCPBandwidthReceiver": {
                    int tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_bandwidth_stream = Integer.parseInt(cmdSplitted[3]) * tcp_bandwidth_pktsize;
                    System.out.print("\nReceived command : " + cmdSplitted[0]);
                    System.out.print("\t[Packet size : " + tcp_bandwidth_pktsize);
                    System.out.println(", Number of packes : " + tcp_bandwidth_stream + "]");

                    try {
                        Socket tcpSenderConnectionSocket = tcpListener.accept();
                        Measurements.TCPBandwidthSender(tcpSenderConnectionSocket,
                                tcp_bandwidth_stream, tcp_bandwidth_pktsize);
                        if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("measure failed");
                            break;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    System.out.println("TCPBandwidthReceiver: completed");
                    break;
                }
                case "UDPCapacityPPSender": {
                    //UDP latency test using Packet Pair, MRS has to receive
                    int udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    Map<Long, Integer> measureResult = null;
                    System.out.print("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("\t[Packet size : " + udp_bandwidth_pktsize + "]");

                    try {
                        controlSocketObserver.sendCMD(ControlMessages.Messages.START.toString());
                        System.out.print("1");
                        measureResult = Measurements.UDPCapacityPPReceiver(
                                udpListener, udp_bandwidth_pktsize);
                        System.out.print("2");
                        if (measureResult == null)
                        {
                            System.out.println("Measure failed");
                            controlSocketObserver.sendCMD(ControlMessages.Messages.FAILED.toString());
                            break;
                        }
                        controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    System.out.print("3");
                    //send data to Aggregator
                    sendDataToAggregator("UDPBandwidth", "Observer","Server",
                                        -1, measureResult,
                            cmdSplitted[1], udp_bandwidth_pktsize, 2);
                    System.out.println("Sent results to aggregator");

                    System.out.println("UDPBandwidthSender: completed");
                    break;
                }
                case "UDPCapacityPPReceiver": {
                    //UDP Latency test using Packet Pair, MRS has to send
                    int udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    System.out.print("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("\t[Packet size : " + udp_bandwidth_pktsize + "]");

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
                    try {
                        if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages
                                .SUCCEDED.toString()) != 0) {
                            System.out.println("measure failed");
                            break;
                        }
                    }catch(EOFException e){
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                        controlSocketObserver.closeConnection();

                        continue;
                    }

                    System.out.println("UDPBandwidthReceiver: completed");

                    break;
                }
                case "UDPRTTSender":{
                    //UDP RTT test, MRS has to receive
                    int udp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        udp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("\nReceived command: " + cmdSplitted[0]);
                    System.out.print("\t[Packet size: " + udp_rtt_pktsize);
                    System.out.println(", Number of packes: " + udp_rtt_num_pack + "]");

                    try {
                        controlSocketObserver.sendCMD(ControlMessages.Messages.START.toString());
                        Measurements.UDPRTTReceiver(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("measure failed");
                            break;
                        }
                        System.out.println("UDPRTTSender: completed");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
                case "UDPRTTReceiver":{
                    //UDP RTT test, MRS has to send
                    int udp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        udp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("\nReceived command: " + cmdSplitted[0]);
                    System.out.print("\t[Packet size: " + udp_rtt_pktsize);
                    System.out.println(", Number of packes: " + udp_rtt_num_pack + "]");

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
                        if (latency == -1)
                        {
                            System.out.println("Measure filed");
                            controlSocketObserver.sendCMD(ControlMessages.Messages.FAILED.toString());
                            break;
                        }

                        controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    //send data to Aggregator
                    sendDataToAggregator("UDPRTT","Server","Observer", latency,
                                     null, cmdSplitted[1], udp_rtt_pktsize,
                                               udp_rtt_num_pack);
                    System.out.println("results sent to aggregator");
                    System.out.println("UDPRTTReceiver: completed");

                    break;
                }
                case "TCPRTTSender": {
                    // the observer starts a RTT measure using the remote server s receiver
                    int tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("\nReceived command: " + cmdSplitted[0]);
                    System.out.print("\t[Packet size: " + tcp_rtt_pktsize);
                    System.out.println("Number of packes: " + tcp_rtt_num_pack + "]");

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
                case "TCPRTTReceiver": {
                    //the observer starts a TCP RTT using the remote server as sender
                    int tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("\nReceived command: " + cmdSplitted[0]);
                    System.out.print("\t[Packet size: " + tcp_rtt_pktsize);
                    System.out.println(", Number of packes: " + tcp_rtt_num_pack + "]");

                    try {
                        Socket tcpRTT = tcpListener.accept();
                        latency = Measurements.TCPRTTSender(tcpRTT, tcp_rtt_pktsize, tcp_rtt_num_pack);

                        controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    //send data to Aggregator
                    sendDataToAggregator("TCPRTT","Server", "Observer", latency,
                                null, cmdSplitted[1], tcp_rtt_pktsize, tcp_rtt_num_pack);
                    System.out.println("results sent to aggregator");
                    System.out.println("TCPRTTReceiver: completed");

                    break;
                }
            }

            controlSocketObserver.closeConnection();
        }
    }



    protected static void sendDataToAggregator(String type, String sender, String receiver,
                                               double latency, Map<Long, Integer> bandwidth,
                                               String keyword, int len_pack, int num_pack){
        Socket socket = null;
        ObjectOutputStream objOutputStream = null;
        try {
            socket = new Socket(InetAddress.getByName(AGGREGATORIP), AGGRPORT);
            objOutputStream = new ObjectOutputStream(socket.getOutputStream());
            Measure measure = new Measure();
            measure.setType(type);
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
                if (objOutputStream != null)
                    objOutputStream.close(); // close the output stream when we're done.
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

