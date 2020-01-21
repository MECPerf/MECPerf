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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import it.unipi.dii.common.Measurements;
import it.unipi.dii.common.ControlMessages;

/**
 *
 * @author Bernardi Leonardo
 */


public class Server {
    //command listener, tcp data and udp data ports
    private static int REMOTECMDPORT = -1,
                       REMOTETCPPORT = -1,
                       REMOTEUDPPORT = -1;


    public static void main(String[] args){
        parseArguments(args);
        if (!checkArguments()){
            System.out.println("checkArguments() failed");
            System.exit(0);
        }

        ServerSocket cmdListener = null;    //ServerSocket per la ricezione dei comandi
        ServerSocket tcpListener = null;    //ServerSocket per le misurazioni TCP
        DatagramSocket udpListener = null;  //ServerSocket per le misurazioni UDP
        //socket initialization
        try {
            cmdListener = new ServerSocket(REMOTECMDPORT);
            tcpListener = new ServerSocket(REMOTETCPPORT);
            udpListener = new DatagramSocket(REMOTEUDPPORT);

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
            String cmd;
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
            double latency;

            //Start test based on command received
            switch(cmdSplitted[0]) {
                case "TCPBandwidthSender": {
                    //the observer sends to the remote server
                    Map<Long, Integer> mappa;
                    int tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    System.out.print("Received command: " + cmdSplitted[0]);
                    System.out.print("\t\t[Packet size: " + tcp_bandwidth_pktsize);
                    System.out.println(", Number of packets: " + Integer.parseInt(cmdSplitted[3]) + "]");


                    try {
                        Socket tcpReceiverConnectionSocket = tcpListener.accept();

                        controlSocketObserver.sendCMD(ControlMessages.Messages.START.toString());
                        mappa = Measurements.TCPBandwidthReceiver(tcpReceiverConnectionSocket, tcp_bandwidth_pktsize);
                        controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());


                        controlSocketObserver.sendCMD(ControlMessages.Messages.MEASUREDBANDWIDTH
                                .toString());

                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(controlSocketObserver.getSocket().getOutputStream());
                        objectOutputStream.writeObject(mappa);

                        objectOutputStream.close();


                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    break;
                }
                case "TCPBandwidthReceiver": {
                    int tcp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_bandwidth_stream = Integer.parseInt(cmdSplitted[3]) * tcp_bandwidth_pktsize;
                    System.out.print("Received command: " + cmdSplitted[0]);
                    System.out.print("\t\t[Packet size: " + tcp_bandwidth_pktsize);
                    System.out.println(", Number of packets: " + Integer.parseInt(cmdSplitted[3]) + "]");

                    try {
                        Socket tcpSenderConnectionSocket = tcpListener.accept();
                        Measurements.TCPBandwidthSender(tcpSenderConnectionSocket,
                                tcp_bandwidth_stream, tcp_bandwidth_pktsize);
                        if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages
                                                                       .SUCCEDED.toString()) != 0) {
                            System.out.println("measure failed");
                            controlSocketObserver.closeConnection();
                            break;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    break;
                }
                case "UDPCapacityPPSender": {
                    //UDP latency test using Packet Pair, MRS has to receive
                    int udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    Map<Long, Integer> measureResult = null;
                    System.out.print("Received command: " + cmdSplitted[0]);
                    System.out.println("\t\t[Packet size: " + udp_bandwidth_pktsize + "]");

                    try {
                        controlSocketObserver.sendCMD(ControlMessages.Messages.START.toString());
                        measureResult = Measurements.UDPCapacityPPReceiver(udpListener,
                                                                           udp_bandwidth_pktsize);
                        if (measureResult == null)
                        {
                            System.out.println("Measure failed");
                            controlSocketObserver.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketObserver.closeConnection();
                            break;
                        }

                        controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());

                        controlSocketObserver.sendCMD(ControlMessages.Messages.MEASUREDBANDWIDTH
                                                      .toString());


                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(controlSocketObserver.getSocket().getOutputStream());
                        objectOutputStream.writeObject(measureResult);
                        objectOutputStream.flush();
                        objectOutputStream.close();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }

                    break;
                }
                case "UDPCapacityPPReceiver": {
                    //UDP Latency test using Packet Pair, MRS has to send
                    int udp_bandwidth_pktsize = Integer.parseInt(cmdSplitted[2]);
                    System.out.print("Received command: " + cmdSplitted[0]);
                    System.out.println("\t\t[Packet size: " + udp_bandwidth_pktsize + "]");

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
                            controlSocketObserver.closeConnection();
                            break;
                        }
                    }catch(EOFException e){
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                        controlSocketObserver.closeConnection();

                        continue;
                    }


                    break;
                }
                case "UDPRTTSender":{
                    //UDP RTT test, MRS has to receive
                    int udp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        udp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("Received command: " + cmdSplitted[0]);
                    System.out.print("\t\t\t[Packet size: " + udp_rtt_pktsize);
                    System.out.println(", Number of packets: " + udp_rtt_num_pack + "]");

                    try {
                        controlSocketObserver.sendCMD(ControlMessages.Messages.START.toString());
                        int ret = Measurements.UDPRTTReceiver(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        if (ret < 0 || controlSocketObserver.receiveCMD().compareTo(
                                               ControlMessages.Messages.SUCCEDED.toString()) != 0) {
                            System.out.println("measure failed");
                            controlSocketObserver.closeConnection();
                            break;
                        }

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
                case "UDPRTTReceiver":{
                    //UDP RTT test, MRS has to send
                    int udp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        udp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("Received command: " + cmdSplitted[0]);
                    System.out.print("\t\t[Packet size: " + udp_rtt_pktsize);
                    System.out.println(", Number of packets: " + udp_rtt_num_pack + "]");

                    try {
                        //MRS has to first receive a packet from the client to know Client's Address and Port
                        byte[] bufrtt = new byte[1000];
                        DatagramPacket dgprtt = new DatagramPacket(bufrtt, bufrtt.length);
                        try {
                            udpListener.receive(dgprtt);
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("udpListener.isConnected(): " + udpListener.isConnected());
                            System.out.println("udpListener.isClosed(): " + udpListener.isClosed());
                            System.out.println("udpListener.isBound(): " + udpListener.isBound());
                            udpListener.disconnect();

                            break;
                        }

                        //connecting and actually starting the test
                        udpListener.connect(dgprtt.getAddress(), dgprtt.getPort());
                        latency = Measurements.UDPRTTSender(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        if (latency < 0)
                        {
                            System.out.println("Measure filed");
                            //controlSocketObserver.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketObserver.closeConnection();

                            udpListener.disconnect();
                            break;
                        }
                        udpListener.disconnect();
                        controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());

                        controlSocketObserver.sendCMD(ControlMessages.Messages.MEASUREDLATENCY
                                .toString() + '#' + latency);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }


                    break;
                }
                case "TCPRTTSender": {
                    // the observer starts a RTT measure using the remote server s receiver
                    int tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("Received command: " + cmdSplitted[0]);
                    System.out.print("\t\t\t[Packet size: " + tcp_rtt_pktsize);
                    System.out.println(", Number of packets: " + tcp_rtt_num_pack + "]");

                    try {
                        controlSocketObserver.sendCMD(ControlMessages.Messages.START.toString());
                        Socket tcpRTT = tcpListener.accept();

                        //controlSocketObserver.sendCMD(controlSocketObserver.messages.START.toString());
                        Measurements.TCPRTTReceiver(tcpRTT, tcp_rtt_pktsize, tcp_rtt_num_pack);
                        if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages
                                                                      .SUCCEDED.toString()) != 0) {
                            System.out.println("measure failed");
                            controlSocketObserver.closeConnection();
                            break;
                        }


                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
                case "TCPRTTReceiver": {
                    //the observer starts a TCP RTT using the remote server as sender
                    int tcp_rtt_pktsize = Integer.parseInt(cmdSplitted[2]),
                        tcp_rtt_num_pack = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("Received command: " + cmdSplitted[0]);
                    System.out.print("\t\t[Packet size: " + tcp_rtt_pktsize);
                    System.out.println(", Number of packets: " + tcp_rtt_num_pack + "]");

                    try {
                        Socket tcpRTT = tcpListener.accept();
                        latency = Measurements.TCPRTTSender(tcpRTT, tcp_rtt_pktsize, tcp_rtt_num_pack);

                        controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());

                        //send data to Aggregator
                        controlSocketObserver.sendCMD(ControlMessages.Messages.MEASUREDLATENCY
                                                      .toString() + '#' + latency);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }


                    break;
                }
            }

            controlSocketObserver.closeConnection();
        }
    }



    private static boolean checkArguments(){
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

        return true;
    }



    private static void parseArguments(String[] args){
        for (int i = 0; i< args.length; i++) {
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

            System.out.println("Unknown command " + args[i]);
        }
    }



    protected static String getAddress(String address){
        address = address.replace("/", "");
        return address.substring(0, address.indexOf(":"));
    }
}

