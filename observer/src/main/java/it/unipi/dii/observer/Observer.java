package it.unipi.dii.observer;




import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unipi.dii.common.Measurements;
import it.unipi.dii.common.Measure;
import it.unipi.dii.common.ControlMessages;





public class Observer {
    private static String REMOTEIP = null,
                          AGGREGATORIP = null,
                          OBSERVERIP = null;

    private static int REMOTECMDPORT = -1,
                       REMOTETCPPORT = -1,
                       REMOTEUDPPORT = -1,
                       AGGRPORT = -1,
                       OBSCMDPORT = -1,
                       OBSTCPPORT = -1,
                       OBSUDPPORT = -1;



    public static void main(String[] args) throws Exception{
        OBSERVERIP = getAddress().replace("/", "");
        parseArguments(args);
        if (!checkArguments()){
            System.out.println("checkArguments() failed");
            System.exit(0);
        }

        ServerSocket cmdListener = new ServerSocket(OBSCMDPORT);//socket used to receive commands
        ServerSocket tcpListener = new ServerSocket(OBSTCPPORT);//socket used for tcp operations
        DatagramSocket udpListener = new DatagramSocket(OBSUDPPORT);//socket used for udp operations
        try {
            System.out.println("\n\nObserver address: " + OBSERVERIP);
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
            System.out.println("\nWaiting for  a command...");
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

            controlSocketRemote.initializeNewMeasure(REMOTEIP, REMOTECMDPORT);
            switch(cmdSplitted[0]) {
                case "TCPBandwidthSender": {
                    /*
                        the app sends packets to the observer
                        the observer sends packet to the server
                    */
                    int tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_bandwidth_stream = Integer.parseInt(cmdSplitted[3]) * tcp_bandwidth_pktsize;
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t[Packet size : " + tcp_bandwidth_pktsize);
                    System.out.println(", Number of packes : " + Integer.parseInt(cmdSplitted[3]) + "]");

                    try {
                        Socket tcpReceiverConnectionSocket = tcpListener.accept();
                        Socket tcpSenderConnectionSocket = new Socket(
                                                          InetAddress.getByName(REMOTEIP), REMOTETCPPORT);

                        //first measure (client -> observer)
                        controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        Map<Long, Integer> mappaCO = Measurements.TCPBandwidthReceiver(
                                                tcpReceiverConnectionSocket, tcp_bandwidth_pktsize);


                        //second measure (observer -> remote)
                        controlSocketRemote.sendCMD(cmd + "#" + OBSCMDPORT + "#" + OBSERVERIP);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                                                                          .START.toString()) != 0) {
                            System.out.println("Start measure with remote FAILED");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }
                        Measurements.TCPBandwidthSender(tcpSenderConnectionSocket,
                                                       tcp_bandwidth_stream, tcp_bandwidth_pktsize);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("Failed remote measure");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }

                        String resultRemote = controlSocketRemote.receiveCMD();


                        InputStream isr = controlSocketRemote.getSocket().getInputStream();
                        ObjectInputStream mapInputStream = new ObjectInputStream(isr);
                        Map<Long, Integer> remoteMeasure = (Map<Long, Integer>) mapInputStream.readObject();
                        isr.close();
                        mapInputStream.close();

                        //remote data
                        Measure measureSecondSegment = new Measure("TCPBandwidth",
                                                "Observer","Server", remoteMeasure,
                                                -1, cmdSplitted[1], tcp_bandwidth_pktsize,
                                                 Integer.parseInt(cmdSplitted[3]), OBSERVERIP,
                                                 cutAddress(controlSocketRemote.getSocket()
                                                         .getRemoteSocketAddress().toString()));

                        //observer data
                        Measure measureFirstSegment = new Measure("TCPBandwidth","Client",
                                "Observer", mappaCO, -1, cmdSplitted[1],
                                tcp_bandwidth_pktsize, Integer.parseInt(cmdSplitted[3]),
                                cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                          .toString()), OBSERVERIP);

                        sendAggregator(measureFirstSegment, measureSecondSegment);


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
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t[Packet size : " + tcp_bandwidth_pktsize);
                    System.out.println(", Number of packes : " + Integer.parseInt(cmdSplitted[3]) + "]");

                    try {
                        Socket tcpSenderConnectionSocket = tcpListener.accept();
                        Socket tcpReceiverConnectionSocket = new Socket(InetAddress.getByName(
                                REMOTEIP), REMOTETCPPORT);

                        //first measure (observer -> client)
                        Measurements.TCPBandwidthSender(tcpSenderConnectionSocket,
                                                       tcp_bandwidth_stream, tcp_bandwidth_pktsize);
                        System.out.println("wait for success");
                        if (controlSocketApp.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("Edge measure failed");
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }

                        System.out.println("success");
                        //second measure (remote -> observer)

                        controlSocketRemote.sendCMD(cmd);
                        mappaSO = Measurements.TCPBandwidthReceiver(tcpReceiverConnectionSocket,
                                                                    tcp_bandwidth_pktsize);
                        controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());
                    String resultApp = controlSocketApp.receiveCMD();

                    //Socket connectionSocket = cmdListener.accept();
                    //InputStream isr = connectionSocket.getInputStream();
                    InputStream isr = controlSocketApp.getSocket().getInputStream();
                    ObjectInputStream mapInputStream = new ObjectInputStream(isr);
                    Map<Long, Integer> applicationMeasure = (Map<Long, Integer>) mapInputStream.readObject();
                    isr.close();
                    mapInputStream.close();


                    //application data
                    Measure measureFirstSegment = new Measure("TCPBandwidth", "Observer",
                            "Client", applicationMeasure, -1, cmdSplitted[1],
                            tcp_bandwidth_pktsize, Integer.parseInt(cmdSplitted[3]),
                            OBSERVERIP, cutAddress(controlSocketApp.getSocket()
                            .getRemoteSocketAddress().toString()) );
                    //observer data
                    Measure measureSecondSegment = new Measure("TCPBandwidth", "Server",
                            "Observer", mappaSO, -1, cmdSplitted[1],
                            tcp_bandwidth_pktsize, Integer.parseInt(cmdSplitted[3]),
                            cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                    .toString()), OBSERVERIP);

                    sendAggregator(measureFirstSegment, measureSecondSegment);
                    System.out.println("results sent to aggregator");
                    System.out.println("TCPBandwidthReceiver: completed");
                    break;
                }
                case "UDPCapacityPPSender": {
                    int udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.println("\t[Packet size : " + udp_bandwidth_pktsize + "]");

                    Map<Long, Integer> measureResult = null;
                    try {
                        //first measure (client -> observer)
                        controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        measureResult = Measurements.UDPCapacityPPReceiver(udpListener,
                                                                           udp_bandwidth_pktsize);
                        if (measureResult == null)
                        {
                            System.out.println("Measure filed");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }


                        //second measure (observer -> remote)
                        controlSocketRemote.sendCMD(cmd + "#" + OBSCMDPORT + "#"+ OBSERVERIP);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages.
                                                                             START.toString()) !=0){
                            System.out.println("Start measure with remote FAILED");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }
                        DatagramSocket udpsocket = new DatagramSocket();
                        udpsocket.connect(InetAddress.getByName(REMOTEIP), REMOTEUDPPORT);
                        Measurements.UDPCapacityPPSender(udpsocket, udp_bandwidth_pktsize);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages.
                                                                          SUCCEDED.toString()) !=0){
                            System.out.println("Failed remote measure");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }

                    } catch (SocketException | UnknownHostException ex) {
                        ex.printStackTrace();
                    }


                    String resultRemote = controlSocketRemote.receiveCMD();

                    //Socket connectionSocket = cmdListener.accept();
                    //InputStream isr = connectionSocket.getInputStream();
                    InputStream isr = controlSocketRemote.getSocket().getInputStream();
                    ObjectInputStream mapInputStream = new ObjectInputStream(isr);
                    Map<Long, Integer> remoteMeasure = (Map<Long, Integer>) mapInputStream.readObject();

                    mapInputStream.close();
                    isr.close();

                    //remote data
                    Measure measureSecondSegment = new Measure("UDPBandwidth",
                            "Observer","Server",  remoteMeasure, -1,
                            cmdSplitted[1], udp_bandwidth_pktsize, 2, OBSERVERIP,
                            cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                    .toString()));
                    //observer data
                    Measure measureFirstSegment = new Measure("UDPBandwidth", "Client",
                            "Observer", measureResult, -1, cmdSplitted[1],
                            udp_bandwidth_pktsize,2, cutAddress(controlSocketApp
                            .getSocket().getRemoteSocketAddress().toString()), OBSERVERIP);

                    sendAggregator(measureFirstSegment, measureSecondSegment);

                    controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                    System.out.println("results sent to aggregator");
                    System.out.println("UDPBandwidthSender: completed");
                    break;
                }
                case "UDPCapacityPPReceiver":{
                    int udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.println("\t[Packet size : " + udp_bandwidth_pktsize  + "]");

                    byte[] buf = new byte[1000];
                    DatagramPacket dgp = new DatagramPacket(buf, buf.length);
                    try {
                        udpListener.receive(dgp);
                        udpListener.connect(dgp.getAddress(), dgp.getPort());

                        //first measure (observer -> client)
                        Measurements.UDPCapacityPPSender(udpListener, udp_bandwidth_pktsize);
                        udpListener.disconnect();
                    }
                    catch (IOException ex) {
                        Logger.getLogger(Observer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (controlSocketApp.receiveCMD().compareTo(ControlMessages.Messages.SUCCEDED
                                                                                .toString()) != 0) {
                        System.out.println("Client-observer measure failed");
                        controlSocketRemote.closeConnection();
                        controlSocketApp.closeConnection();
                        break;
                    }


                    //second measure (remote -> observer)
                    controlSocketRemote.sendCMD(cmd);
                    //MO has to send a packet to server to let the server knows Client's IP and Port
                    DatagramSocket udpsocket = null;
                    try {
                        udpsocket = new DatagramSocket();
                        byte[] buff;
                        String outString = "UDPCapacityPPReceiver";
                        buff = outString.getBytes();
                        DatagramPacket out = new DatagramPacket(buff, buff.length, InetAddress.getByName(REMOTEIP), REMOTEUDPPORT);
                        udpsocket.send(out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Map<Long, Integer> measureResult = Measurements.UDPCapacityPPReceiver(udpsocket,
                                                                             udp_bandwidth_pktsize);
                    if (measureResult == null)
                    {
                        System.out.println("Measure filed");
                        controlSocketRemote.sendCMD(ControlMessages.Messages.FAILED.toString());
                        controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                        controlSocketRemote.closeConnection();
                        controlSocketApp.closeConnection();
                        break;
                    }
                    controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                    controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                    String resultApp = controlSocketApp.receiveCMD();

                    //Socket connectionSocket = cmdListener.accept();
                    //InputStream isr = connectionSocket.getInputStream();
                    ObjectInputStream mapInputStream = new ObjectInputStream(controlSocketApp.getSocket().getInputStream());
                    Map<Long, Integer> applicationMeasure = (Map<Long, Integer>) mapInputStream.readObject();
                    //isr.close();
                    mapInputStream.close();

                    //application data
                    Measure measureFirstSegment = new Measure("UDPBandwidth","Observer",
                            "Client", applicationMeasure, -1, cmdSplitted[1],
                            udp_bandwidth_pktsize, 2, OBSERVERIP,
                            cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                    .toString()));
                    //observer data
                    Measure measureSecondSegment = new Measure("UDPBandwidth","Server",
                            "Observer", measureResult, -1, cmdSplitted[1],
                            udp_bandwidth_pktsize, 2,
                            cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                    .toString()), OBSERVERIP);

                    sendAggregator(measureFirstSegment, measureSecondSegment);

                    System.out.println("results sent to aggregator");
                    System.out.println("UDPCapacityPPReceiver: completed");

                    break;
                }
                case "UDPRTTSender": {
                    //MO forwards the command to the server and sends the metrics observer-server
                    int udp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        udp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t[Packet size : " + udp_rtt_pktsize);
                    System.out.println(", Number of packes : " + udp_rtt_num_pack + "]");

                    try {
                        controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        int ret = Measurements.UDPRTTReceiver(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        if (ret < 0 || controlSocketApp.receiveCMD().compareTo(ControlMessages
                                                              .Messages.SUCCEDED.toString()) != 0) {
                            System.out.println("measure with client FAILED");
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            System.out.println("closed");


                            System.out.println("Failed sent");
                            break;
                        }


                        //second measure (observer -> remote -> observer)
                        controlSocketRemote.sendCMD(cmd);
                        if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                                .START.toString()) != 0) {
                            System.out.println("Start measure with remote FAILED");

                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }

                        DatagramSocket udpsocketmo = new DatagramSocket();
                        udpsocketmo.connect(InetAddress.getByName(REMOTEIP), REMOTEUDPPORT);
                        double latency = Measurements.UDPRTTSender(udpsocketmo, udp_rtt_pktsize,
                                                                   udp_rtt_num_pack);
                        if (latency == -1)
                        {
                            controlSocketRemote.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }
                        controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                        String[] resultApp = controlSocketApp.receiveCMD().split(separator);


                        //application data
                        Measure measureFirstSegment = new Measure("UDPRTT", "Client",
                                "Observer", null, Double.parseDouble(resultApp[1]),
                                cmdSplitted[1], udp_rtt_pktsize, udp_rtt_num_pack,
                                cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                        .toString()), OBSERVERIP);
                        //observer data
                        Measure measureSecondSegment = new Measure("UDPRTT", "Observer",
                                "Server",null, latency, cmdSplitted[1],
                                udp_rtt_pktsize, udp_rtt_num_pack, OBSERVERIP,
                                cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                        .toString()));

                        sendAggregator(measureFirstSegment, measureSecondSegment);


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
                    System.out.print("Received command : " + cmdSplitted[0]);
                    System.out.print("\t[Packet size : " + udp_rtt_pktsize);
                    System.out.println(", Number of packes : " + udp_rtt_num_pack + "]");

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
                        if (latency == -1)
                        {
                            System.out.println ("MEasure failed");
                            //controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }

                        System.out.println("before cmd");
                        System.out.println(controlSocketRemote);
                        controlSocketRemote.sendCMD(cmd);
                        System.out.println("after");

                        DatagramSocket udpsocketRTT = new DatagramSocket();
                        byte[] bufRTT;
                        String outString = "UDPCapacityPPReceiver";
                        bufRTT = outString.getBytes();
                        DatagramPacket out = new DatagramPacket(bufRTT, bufRTT.length, InetAddress.getByName(REMOTEIP), REMOTEUDPPORT);
                        //Client has to send a packet to server to let the server knows Client's IP and Port
                        udpsocketRTT.send(out);
                        int ret = Measurements.UDPRTTReceiver(udpsocketRTT, udp_rtt_pktsize, udp_rtt_num_pack);
                        if (ret < 0 || controlSocketRemote.receiveCMD().compareTo(ControlMessages
                                                              .Messages.SUCCEDED.toString()) != 0) {
                            System.out.println("Measure failed");
                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
                            break;
                        }

                        String[] resultRemote = controlSocketRemote.receiveCMD().split(separator);

                        //remote data
                        Measure measureSecondSegment = new Measure("UDPRTT",  "Server",
                                "Observer", null,
                                Double.parseDouble(resultRemote[1]), cmdSplitted[1], udp_rtt_pktsize,
                                udp_rtt_num_pack, cutAddress(controlSocketRemote.getSocket()
                                .getRemoteSocketAddress().toString()), OBSERVERIP);
                        //observer data
                        Measure measureFirstSegment = new Measure("UDPRTT", "Observer",
                                "Client", null, latency, cmdSplitted[1],
                                udp_rtt_pktsize, udp_rtt_num_pack, OBSERVERIP,
                                cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                        .toString()));

                        sendAggregator(measureFirstSegment, measureSecondSegment);

                        controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());
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
                    System.out.print("\nReceived command : " + cmdSplitted[0]);
                    System.out.print("\t[Packet size : " + tcp_rtt_pktsize);
                    System.out.println(", Number of packes : " + tcp_rtt_num_pack + "]");

                    try {
                        Socket tcpRTTClient = tcpListener.accept();
                        //first measure (client -> observer -> client)
                        controlSocketApp.sendCMD(ControlMessages.Messages.START.toString());
                        Measurements.TCPRTTReceiver(tcpRTTClient, tcp_rtt_pktsize, tcp_rtt_num_pack);


                        if (controlSocketApp.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("Measure failed");

                            controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketRemote.closeConnection();
                            controlSocketApp.closeConnection();
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
                        controlSocketRemote.closeConnection();
                        controlSocketApp.closeConnection();
                        break;
                    }
                    latency = Measurements.TCPRTTSender(new Socket(InetAddress.getByName(REMOTEIP),
                            REMOTETCPPORT), tcp_rtt_pktsize, tcp_rtt_num_pack);
                    controlSocketRemote.sendCMD(ControlMessages.Messages.SUCCEDED.toString());
                    controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());

                    String[] resultApp = controlSocketApp.receiveCMD().split(separator);

                    //application data
                    Measure measureFirstSegment = new Measure("TCPRTT","Client",
                            "Observer", null, Double.parseDouble(resultApp[1]),
                            cmdSplitted[1], tcp_rtt_pktsize, tcp_rtt_num_pack,
                            cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                    .toString()), OBSERVERIP);
                    //observer data
                    Measure measureSecondSegment = new Measure("TCPRTT","Observer",
                            "Server", null, latency, cmdSplitted[1],
                            tcp_rtt_pktsize, tcp_rtt_num_pack, OBSERVERIP,
                            cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                    .toString()));

                    sendAggregator(measureFirstSegment, measureSecondSegment);
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
                    System.out.print("\nReceived command : " + cmdSplitted[0]);
                    System.out.print("\t[Packet size : " + tcp_rtt_pktsize);
                    System.out.println(", Number of packes : " + tcp_rtt_num_pack + "]");

                    try {
                        tcpRTT = tcpListener.accept();
                    } catch (IOException ex) {
                        //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        ex.printStackTrace();
                    }
                    latency = Measurements.TCPRTTSender(tcpRTT, tcp_rtt_pktsize, tcp_rtt_num_pack);


                    controlSocketRemote.sendCMD(cmd);
                    Measurements.TCPRTTReceiver(new Socket(InetAddress.getByName(REMOTEIP),
                                                REMOTETCPPORT), tcp_rtt_pktsize, tcp_rtt_num_pack);
                    if (controlSocketRemote.receiveCMD().compareTo(ControlMessages.Messages
                            .SUCCEDED.toString()) != 0) {
                        System.out.println("Measure failed");

                        controlSocketApp.sendCMD(ControlMessages.Messages.FAILED.toString());
                        controlSocketRemote.closeConnection();
                        controlSocketApp.closeConnection();
                        break;
                    }


                    String[] resultRemote = controlSocketRemote.receiveCMD().split(separator);
                    System.out.println("APP RESULT " + resultRemote[1]);

                    //remote data
                    Measure measureSecondSegment = new Measure("TCPRTT", "Server",
                            "Observer", null, Double.parseDouble(resultRemote[1]),
                            cmdSplitted[1], tcp_rtt_pktsize, tcp_rtt_num_pack , "" +
                            cutAddress(controlSocketRemote.getSocket().getRemoteSocketAddress()
                                    .toString()), OBSERVERIP);
                    //observer data
                    Measure measureFirstSegment = new Measure("TCPRTT", "Observer",
                            "Client",null, latency, cmdSplitted[1],
                            tcp_rtt_pktsize, tcp_rtt_num_pack,  OBSERVERIP,
                            cutAddress(controlSocketApp.getSocket().getRemoteSocketAddress()
                                    .toString()));

                    sendAggregator(measureFirstSegment, measureSecondSegment);


                    controlSocketApp.sendCMD(ControlMessages.Messages.COMPLETED.toString());
                    System.out.println("results sent to aggregator");
                    System.out.println("TCPRTTReceiver: completed");

                    break;
                }
            }
            controlSocketRemote.closeConnection();
            controlSocketApp.closeConnection();
        }
    }



    private static boolean checkArguments(){
        try {
            //check OBSERVERIP
            if (OBSERVERIP == null){
                System.out.println("Error: OBSERVERIP cannot be null");
                return false;
            }
            if (!(InetAddress.getByName(OBSERVERIP) instanceof Inet4Address)) {
                System.out.println("Error: OBSERVERIP is not an IPv4Address");
                return false;
            }

            //check AGGREGATORIP
            if (AGGREGATORIP == null){
                System.out.println("Error: AGGREGATORIP cannot be null");
                return false;
            }
            if (!(InetAddress.getByName(AGGREGATORIP) instanceof Inet4Address)) {
                System.out.println("Error: AGGREGATOR is not an IPv4Address");
                return false;
            }

            //check REMOTEIP
            if (REMOTEIP == null){
                System.out.println("Error: REMOTEIP cannot be null");
                return false;
            }
            if (!(InetAddress.getByName(REMOTEIP) instanceof Inet4Address)) {
                System.out.println("Error: REMOTEIP is not an IPv4Address");
                return false;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //check REMOTE ports
        if (REMOTECMDPORT < 0){
            System.out.println("Error: REMOTECMDPORT cannot be negative");
            return false;
        }
        if (REMOTETCPPORT < 0){
            System.out.println("Error: REMOTETCPPORT cannot be negative");
            return false;
        }
        if (REMOTEUDPPORT < 0){
            System.out.println("Error: REMOTEUDPPORT cannot be negative");
            return false;
        }

        //check OBSERVER ports
        if (OBSCMDPORT < 0){
            System.out.println("Error: OBSCMDPORT cannot be negative");
            return false;
        }
        if (OBSTCPPORT < 0){
            System.out.println("Error: OBSTCPPORT cannot be negative");
            return false;
        }
        if (OBSUDPPORT < 0){
            System.out.println("Error: OBSUDPPORT cannot be negative");
            return false;
        }

        //check AGGREGATOR port
        if (AGGRPORT < 0){
            System.out.println("Error: AGGRPORT cannot be negative");
            return false;
        }

        return true;
    }



    private static void parseArguments(String[] args){
        for (int i = 0; i< args.length; i++) {

            if (args[i].equals("-a") || args[i].equals("--aggregator-ip")) {
                AGGREGATORIP = args[++i];
                continue;
            }
            if (args[i].equals("-r") || args[i].equals("--remote-ip")) {
                REMOTEIP = args[++i];
                continue;
            }
            if (args[i].equals("-ap") || args[i].equals("--aggregator-port")) {
                AGGRPORT = Integer.parseInt(args[++i]);
                continue;
            }

            if (args[i].equals("-rtp") || args[i].equals("--remote-tcp-port")) {
                REMOTETCPPORT = Integer.parseInt(args[++i]);
                continue;
            }

            if (args[i].equals("-rup") || args[i].equals("--remote-udp-port")) {
                REMOTEUDPPORT = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-rcp") || args[i].equals("--remote-cmd-port")) {
                REMOTECMDPORT = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-otp") || args[i].equals("--observer-tcp-port")) {
                OBSTCPPORT = Integer.parseInt(args[++i]);
                continue;
            }

            if (args[i].equals("-oup") || args[i].equals("--observer-udp-port")) {
                OBSUDPPORT = Integer.parseInt(args[++i]);
                continue;
            }

            if (args[i].equals("-ocp") || args[i].equals("--observer-cmd-port")) {
                OBSCMDPORT = Integer.parseInt(args[++i]);
                continue;
            }

            System.out.println("Unknown command " + args[i]);
        }
    }



    private static String cutAddress(String address)
    {
        address = address.replace("/", "");
        return address.substring(0, address.indexOf(":"));
    }



    private static String getAddress()
    {
        Enumeration enumerator = null;
        try {
            enumerator = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        while(enumerator.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) enumerator.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                System.out.print(i.getHostAddress());


                if (i instanceof Inet6Address) {
                    System.out.println("\t IPv6 address (next)");

                    continue;
                }
                else
                    if (i instanceof Inet4Address) {
                        try {
                            InetAddress localhostAddress = InetAddress.getByName("127.0.0.1");
                            InetAddress localhostAddress2 = InetAddress.getByName("127.0.1.1");
                            InetAddress nullAddress = InetAddress.getByName("0.0.0.0");


                            if (i.equals(localhostAddress) || i.equals(localhostAddress2)){
                                System.out.println("\t localhost address (next)");

                                continue;
                            }
                            if (i.equals(nullAddress)){
                                System.out.println("\t null address (next)");

                                continue;
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                        System.out.println("\t IPv4 address (take)");
                        return i.toString();
                    }
            }
        }

        return null;
    }



    protected static void sendAggregator(Measure measureFirstSegment, Measure measureSecondSegment)
                                                                                   throws Exception{
        Socket socket = null;
        ObjectOutputStream objOutputStream = null;
        try {
            socket = new Socket(InetAddress.getByName(AGGREGATORIP), AGGRPORT);
            objOutputStream = new ObjectOutputStream(socket.getOutputStream());


            // write the message we want to send
            objOutputStream.writeObject(measureFirstSegment);
            objOutputStream.writeObject(measureSecondSegment);
        }
        finally {
            if(objOutputStream != null)
                objOutputStream.close(); // close the output stream when we're done.
            if (socket != null)
                socket.close();
        }

    }
}





