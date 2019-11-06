package it.unipi.dii.observer;




import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unipi.dii.common.Measurements;
import it.unipi.dii.common.Measure;
import it.unipi.dii.common.ControlMessages;




public class Observer {
    private static final String SERVERIP = "131.114.73.3";
    private static final int CMDPORT = 6789;
    private static final int TCPPORT = 6788;
    private static final int UDPPORT = 6787;
    private static final String AGGREGATORIP = "131.114.73.3";
    private static final int AGGRPORT = 6766;
    private static final int OBSCMDPORT = 6792;
    private static final int OBSTCPPORT = 6791;
    private static final int OBSUDPPORT = 6790;



    public static void main(String[] args) throws Exception{
        ServerSocket cmdListener = new ServerSocket(OBSCMDPORT);//socket used to receive commands
        ServerSocket tcpListener = new ServerSocket(OBSTCPPORT);//socket used for tcp operations
        DatagramSocket udpListener = new DatagramSocket(OBSUDPPORT);//socket used for udp operations

        try {


            System.out.println("Observer CMD: inizializzato sulla porta " +
                               cmdListener.getLocalPort());
            System.out.println("Observer TCP: inizializzato sulla porta " +
                               tcpListener.getLocalPort());
            System.out.println("Observer UDP: inizializzato sulla porta " +
                               udpListener.getLocalPort());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }



        ControlMessages controlSocketRemote = new ControlMessages();


        while (true) {
            ControlMessages controlSocketApp = null;
            try {
                controlSocketApp = new ControlMessages(cmdListener.accept());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            String cmd = Objects.requireNonNull(controlSocketApp).receiveCMD();

            //command received is <command ; id>
            String separator ="#";
            String[] cmdSplitted = cmd.split(separator);

            //System.out.println("\nThe cmd sent from the socket was: " + cmdSplitted[0]);
            controlSocketRemote.initializeNewMeasure(SERVERIP, CMDPORT);

            switch(cmdSplitted[0]) {
                case "TCPBandwidthSender": {

                    /*
                        the app sends packets to the observer
                        the observer sends packet to the server
                    */
                    int tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_bandwidth_stream = Integer.parseInt(cmdSplitted[3]) * tcp_bandwidth_pktsize;
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + Integer.parseInt(cmdSplitted[2]));
                    System.out.println("Number of packes : " + Integer.parseInt(cmdSplitted[3]));

                    try {
                        Socket tcpReceiverConnectionSocket = tcpListener.accept();
                        Socket tcpSenderConnectionSocket = new Socket(
                                                          InetAddress.getByName(SERVERIP), TCPPORT);

                        //first measure (client -> observer)
                        controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        Map<Long, Integer> mappaCO = Measurements.TCPBandwidthReceiver(
                                                tcpReceiverConnectionSocket, tcp_bandwidth_pktsize);
                        System.out.println("First measure (receiver) completed");


                        //second measure (observer -> remote)
                        controlSocketRemote.sendCMD(cmd);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                                                                          .START.toString()) != 0) {
                            System.out.println("Start measure with remote FAILED");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            break;
                        }
                        Measurements.TCPBandwidthSender(tcpSenderConnectionSocket,
                                                       tcp_bandwidth_stream, tcp_bandwidth_pktsize);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("Failed remote measure");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            break;
                        }
                        System.out.println("Second measure (sender) completed");


                        //send to aggregator
                        sendAggregator("TCPBandwidth","Client", "Observer",
                                     -1, mappaCO, cmdSplitted[1], tcp_bandwidth_pktsize,
                                             Integer.parseInt(cmdSplitted[3]));
                        System.out.println("results sent to aggregator");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());
                    System.out.println("TCPBandwidthSender: completed");
                    break;
                }
                case "TCPBandwidthReceiver": {
                    Map<Long, Integer> mappaSO = null;
                    int tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_bandwidth_stream = Integer.parseInt(cmdSplitted[3]) * tcp_bandwidth_pktsize;
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + Integer.parseInt(cmdSplitted[2]));
                    System.out.println("Number of packes : " + Integer.parseInt(cmdSplitted[3]));

                    try {
                        Socket tcpSenderConnectionSocket = tcpListener.accept();
                        Socket tcpReceiverConnectionSocket = new Socket(InetAddress.getByName(
                                                                                SERVERIP), TCPPORT);

                        //first measure (observer -> client)
                        Measurements.TCPBandwidthSender(tcpSenderConnectionSocket,
                                                       tcp_bandwidth_stream, tcp_bandwidth_pktsize);
                        if (controlSocketApp.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("Edge measure failed");
                            break;
                        }
                        System.out.println("First measure (sender) completed");


                        //second measure (remote -> observer)
                        controlSocketRemote.sendCMD(cmd);
                        mappaSO = Measurements.TCPBandwidthReceiver(tcpReceiverConnectionSocket,
                                                                    tcp_bandwidth_pktsize);
                        controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                        System.out.println("Second measure (sender) completed");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                    sendAggregator("TCPBandwidth", "Server","Observer",
                                 -1, mappaSO, cmdSplitted[1], tcp_bandwidth_pktsize,
                                         Integer.parseInt(cmdSplitted[3]));
                    System.out.println("results sent to aggregator");

                    System.out.println("TCPBandwidthReceiver: completed");
                    break;
                }
                case "UDPCapacityPPSender": {
                    int udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + Integer.parseInt(cmdSplitted[2]));

                    try {
                        //first measure (client -> observer)
                        controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        Map<Long, Integer> measureResult = Measurements.UDPCapacityPPReceiver(
                                                                udpListener, udp_bandwidth_pktsize);
                        System.out.println("First measure (receiver) completed");


                        //second measure (observer -> remote)
                        controlSocketRemote.sendCMD(cmd);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages.
                                                                             START.toString()) !=0){
                            System.out.println("Start measure with remote FAILED");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            break;
                        }
                        DatagramSocket udpsocket = new DatagramSocket();
                        udpsocket.connect(InetAddress.getByName(SERVERIP), UDPPORT);
                        Measurements.UDPCapacityPPSender(udpsocket, udp_bandwidth_pktsize);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages.
                                                                          SUCCEDED.toString()) !=0){
                            System.out.println("Failed remote measure");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            break;
                        }
                        System.out.println("Second measure (sender) completed");


                        //send data to aggregator
                        sendAggregator("UDPBandwidth", "Client","Observer",
                                       -1, measureResult, cmdSplitted[1],
                                        udp_bandwidth_pktsize, 2);
                        System.out.println("results sent to aggregator");
                    } catch (SocketException | UnknownHostException ex) {
                        ex.printStackTrace();
                    }

                    controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());
                    System.out.println("UDPBandwidthSender: completed");
                    break;
                }
                case "UDPCapacityPPReceiver":{
                    int udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + Integer.parseInt(cmdSplitted[2]));

                    byte[] buf = new byte[1000];
                    DatagramPacket dgp = new DatagramPacket(buf, buf.length);
                    try {
                        udpListener.receive(dgp);
                    } catch (IOException ex) {
                        Logger.getLogger(Observer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    udpListener.connect(dgp.getAddress(), dgp.getPort());

                    //first measure (observer -> client)
                    Measurements.UDPCapacityPPSender(udpListener, udp_bandwidth_pktsize);
                    udpListener.disconnect();
                    if (controlSocketApp.receiveCMD().compareTo(ControlMessages.Messages.SUCCEDED
                                                                                .toString()) != 0) {
                        System.out.println("Client-observer measure failed");
                        break;
                    }
                    System.out.println("First measure (sender) completed");

                    //second measure (remote -> observer)
                    controlSocketRemote.sendCMD(cmd);
                    //MO has to send a packet to server to let the server knows Client's IP and Port
                    DatagramSocket udpsocket = null;
                    try {
                        udpsocket = new DatagramSocket();
                        byte[] buff;
                        String outString = "UDPCapacityPPReceiver";
                        buff = outString.getBytes();
                        DatagramPacket out = new DatagramPacket(buff, buff.length, InetAddress.getByName(SERVERIP), UDPPORT);
                        udpsocket.send(out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Map<Long, Integer> measureResult = Measurements.UDPCapacityPPReceiver(udpsocket,
                                                                             udp_bandwidth_pktsize);
                    controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                    System.out.println("Second measure (sender) completed");

                    controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                    sendAggregator("UDPBandwidth","Server", "Observer",
                                -1, measureResult, cmdSplitted[1], udp_bandwidth_pktsize, 2);

                    System.out.println("results sent to aggregator");

                    break;
                }
                case "UDPRTTSender": {
                    //MO forwards the command to the server and sends the metrics observer-server
                    int udp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        udp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + udp_rtt_pktsize);
                    System.out.println("Number of packes : " + udp_rtt_num_pack);

                    try {
                        controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        Measurements.UDPRTTReceiver(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        if (controlSocketApp.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("Start measure with remote FAILED");

                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            break;
                        }
                        System.out.println("First measure (receiver) completed");

                        //second measure (observer -> remote -> observer)
                        controlSocketRemote.sendCMD(cmd);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                                .START.toString()) != 0) {
                            System.out.println("Start measure with remote FAILED");

                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            break;
                        }

                        DatagramSocket udpsocketmo = new DatagramSocket();
                        udpsocketmo.connect(InetAddress.getByName(SERVERIP), UDPPORT);
                        double latency = Measurements.UDPRTTSender(udpsocketmo, udp_rtt_pktsize, udp_rtt_num_pack);
                        controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());
                        System.out.println("Second measure (sender) completed");

                        sendAggregator("UDPRTT", "Observer", "Server",latency,
                                null, cmdSplitted[1], udp_rtt_pktsize, udp_rtt_num_pack);

                        System.out.println("results sent to aggregator");

                        System.out.println("UDPRTTSender: completed");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
                case "UDPRTTReceiver": {
                    double latency;
                    int udp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        udp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + udp_rtt_pktsize);
                    System.out.println("Number of packes : " + udp_rtt_num_pack);

                    try {
                        //MO has to receive a packet from the client to know Client's Address and Port
                        byte[] bufrtt = new byte[1000];
                        DatagramPacket dgprtt = new DatagramPacket(bufrtt, bufrtt.length);
                        try {
                            udpListener.receive(dgprtt);
                        } catch (IOException ex) {
                            Logger.getLogger(Observer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        udpListener.connect(dgprtt.getAddress(), dgprtt.getPort());
                        latency = Measurements.UDPRTTSender(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        udpListener.disconnect();

                        System.out.println("First measure (sender) completed");

                        controlSocketRemote.sendCMD(cmd);

                        DatagramSocket udpsocketRTT = new DatagramSocket();
                        byte[] bufRTT;
                        String outString = "UDPCapacityPPReceiver";
                        bufRTT = outString.getBytes();
                        DatagramPacket out = new DatagramPacket(bufRTT, bufRTT.length, InetAddress.getByName(SERVERIP), UDPPORT);
                        //Client has to send a packet to server to let the server knows Client's IP and Port
                        udpsocketRTT.send(out);
                        Measurements.UDPRTTReceiver(udpsocketRTT, udp_rtt_pktsize, udp_rtt_num_pack);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                                                                   .SUCCEDED.toString()) != 0) {
                            System.out.println("Measure failed");

                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            break;
                        }
                        System.out.println("Second measure (receiver) completed");
                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                        sendAggregator("UDPRTT", "Observer", "Client", latency,
                                null, cmdSplitted[1], udp_rtt_pktsize, udp_rtt_num_pack);
                        System.out.println("results sent to aggregator");

                        System.out.println("UDPRTTReceiver: completed");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    break;
                    }
                case "TCPRTTSender": {
                    double latency;
                    //the client start a TCP RTT measure using the observer as receiver
                    //the observer starts a TCP RTT measure using the remote server as receiver
                    int tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + tcp_rtt_pktsize);
                    System.out.println("Number of packes : " + tcp_rtt_num_pack);

                    try {
                        Socket tcpRTTClient = tcpListener.accept();
                        //first measure (client -> observer -> client)
                        controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        Measurements.TCPRTTReceiver(tcpRTTClient, tcp_rtt_pktsize, tcp_rtt_num_pack);
                        System.out.println("First measure (receiver) completed");

                        if (controlSocketApp.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("Measure failed");

                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            break;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    //second measure (observer -> remote -> observer)
                    controlSocketRemote.sendCMD(cmd);
                    if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages.START
                                                                            .toString()) != 0) {
                        System.out.println("Start measure with remote FAILED");

                        controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                        break;
                    }
                    latency = Measurements.TCPRTTSender(new Socket(InetAddress.getByName(SERVERIP),
                            TCPPORT), tcp_rtt_pktsize, tcp_rtt_num_pack);
                    System.out.println("Second measure (sender) completed");
                    controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                    controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                    sendAggregator("TCPRTT","Observer", "Server", latency,
                                null, cmdSplitted[1], tcp_rtt_pktsize,
                            tcp_rtt_num_pack);
                    System.out.println("results sent to aggregator");

                    System.out.println("TCPRTTSender: completed");
                    break;
                }
                case "TCPRTTReceiver": {
                    double latency;
                    Socket tcpRTT = null;
                    //MO sends metrics client-observer and forwards the command to the server
                    int tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.println("\nReceived command : " + cmdSplitted[0]);
                    System.out.println("Packet size : " + tcp_rtt_pktsize);
                    System.out.println("Number of packes : " + tcp_rtt_num_pack);

                    try {
                        tcpRTT = tcpListener.accept();
                    } catch (IOException ex) {
                        //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        ex.printStackTrace();
                    }
                    latency = Measurements.TCPRTTSender(tcpRTT, tcp_rtt_pktsize, tcp_rtt_num_pack);
                    System.out.println("First measure (sender) completed");

                    controlSocketRemote.sendCMD(cmd);
                    Measurements.TCPRTTReceiver(new Socket(InetAddress.getByName(SERVERIP), TCPPORT), tcp_rtt_pktsize, tcp_rtt_num_pack);
                    if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                            .SUCCEDED.toString()) != 0) {
                        System.out.println("Measure failed");

                        controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                        break;
                    }
                    System.out.println("Second measure (receiver) completed");

                    controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                    sendAggregator("TCPRTT", "Observer", "Client", latency,
                               null, cmdSplitted[1], tcp_rtt_pktsize,
                            tcp_rtt_num_pack);
                    System.out.println("results sent to aggregator");

                    System.out.println("TCPRTTReceiver: completed");
                    break;
                }
            }
            controlSocketRemote.closeConnection();
            controlSocketApp.closeConnection();
        }
    }



    protected static void sendAggregator(String type, String sender, String receiver,
                                         double latency, Map<Long, Integer> bandwidth,
                                         String keyword, int len_pack, int num_pack)throws Exception{
        Socket socket = null;
        ObjectOutputStream objOutputStream = null;
        try {
            socket = new Socket(InetAddress.getByName(AGGREGATORIP), AGGRPORT);
            objOutputStream = new ObjectOutputStream(socket.getOutputStream());

            Measure measure = new Measure(type, sender, receiver, bandwidth, latency, keyword, len_pack, num_pack);

            // write the message we want to send
            objOutputStream.writeObject(measure);
        }
        finally {
            if(objOutputStream != null)
                objOutputStream.close(); // close the output stream when we're done.
            if (socket != null)
                socket.close();
        }

    }
}


