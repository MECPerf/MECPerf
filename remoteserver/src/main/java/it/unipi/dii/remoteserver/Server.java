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
import java.net.ServerSocket;
import java.net.Socket;
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
                       REMOTEUDPPORT = -1,
                       timeout = 5 * 1000 ;

    private static ServerSocket cmdListener = null; //socket used to receive commands
    private static ServerSocket tcpListener = null; //socket used for tcp operations
    private static DatagramSocket udpListener = null;//socket used for udp operations

    private static void initializeSocket() throws Exception {
        cmdListener = new ServerSocket(REMOTECMDPORT);
        tcpListener = new ServerSocket(REMOTETCPPORT);
        udpListener = new DatagramSocket(REMOTEUDPPORT);


        cmdListener.setSoTimeout(timeout);
        tcpListener.setSoTimeout(timeout);
        udpListener.setSoTimeout(timeout);


        System.out.println("Observer CMD: inizializzato sulla porta " +
                cmdListener.getLocalPort() + "[Timeout = " + timeout/1000 + "s]");
        System.out.println("Observer TCP: inizializzato sulla porta " +
                tcpListener.getLocalPort() + "[Timeout = " + timeout/1000 + "s]");
        System.out.println("Observer UDP: inizializzato sulla porta " +
                udpListener.getLocalPort() + "[Timeout = " + timeout/1000 + "s]");
    }

    private static int restartSockets()  {
        try{

            if (!udpListener.isClosed())
                udpListener.close();
            if (!tcpListener.isClosed())
                tcpListener.close();
            if (!cmdListener.isClosed())
                cmdListener.close();

            initializeSocket();
        }
        catch (Exception ex) {
            ex.printStackTrace();

            System.exit(1);
        }

        return 0;
    }

    public static void main(String[] args){
        parseArguments(args);
        if (!checkArguments()){
            System.out.println("checkArguments() failed");
            System.exit(0);
        }

           //socket initialization
        try {
            initializeSocket();

        } catch (Exception e) {
            e.printStackTrace();
        }


        while (true) {
            ControlMessages controlSocketObserver = null;
            String cmd;
            String separator ="#";

           // System.out.println("Wait for a command;");

            try {
                controlSocketObserver = new ControlMessages(cmdListener.accept());
                cmd = controlSocketObserver.receiveCMD();

                if (cmd == null) {
                    System.out.println("Error: invalid command");
                    continue;
                }
            }
            catch (Exception e)
            {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);

                if (controlSocketObserver != null && !controlSocketObserver.getSocket().isClosed())
                    controlSocketObserver.closeConnection();

                while (true)
                {
                    System.out.println("Restart sockets");
                    int ret= restartSockets();

                    if (ret == 0)
                        break;

                    System.out.println("Failed... restart in 30 seconds");
                    try {
                        Thread.sleep(30 * 1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                }

                continue;
            }

            String[] cmdSplitted = cmd.split(separator);

            //Start test based on command received
            switch(cmdSplitted[0]) {
                case "TCPBandwidthSender": {
                    //the observer sends to the remote server
                    Map<Integer, Long[]> mappa;
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


                    } catch (Exception ex) {
                        ex.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            try {
                                Thread.sleep(30 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
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
                    } catch (Exception ex) {

                        ex.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            try {
                                Thread.sleep(30 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    break;
                }
                case "UDPCapacityPPSender": {
                    //UDP latency test using Packet Pair, MRS has to receive
                    int udp_capacity_pktsize = Integer.parseInt(cmdSplitted[2]);
                    int udp_capacity_num_tests = Integer.parseInt(cmdSplitted[3]);
                    Map<Integer, Long[]> measureResult = null;
                    System.out.print("Received command: " + cmdSplitted[0]);
                    System.out.print("\t\t[ " + udp_capacity_num_tests + " tests of ");
                    System.out.println(udp_capacity_pktsize + " packet size" + "]");

                    try {
                        controlSocketObserver.sendCMD(ControlMessages.Messages.START.toString());
                        measureResult = Measurements.UDPCapacityPPReceiver(udpListener,
                                                                           udp_capacity_pktsize, udp_capacity_num_tests, controlSocketObserver);
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
                    catch(Exception e){
                        e.printStackTrace();


                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            try {
                                Thread.sleep(30 * 1000);
                            } catch (InterruptedException e2) {
                                e2.printStackTrace();
                            }

                        }
                    }

                    break;
                }
                case "UDPCapacityPPReceiver": {
                    //UDP Latency test using Packet Pair, MRS has to send
                    int udp_capacity_pktsize = Integer.parseInt(cmdSplitted[2]);
                    int udp_capacity_num_tests = Integer.parseInt(cmdSplitted[3]);
                    System.out.print("Received command: " + cmdSplitted[0]);
                    System.out.print("\t\t[ " + udp_capacity_num_tests + "tests of ");
                    System.out.println(udp_capacity_pktsize + " packet size" + "]");


                    //MRS first has to receive a packet from the client to know Client's Address and Port
                    byte[] buf = new byte[1000];
                    DatagramPacket dgp = new DatagramPacket(buf, buf.length);
                    try {
                        udpListener.receive(dgp);

                        //remote -> observer bandwidth measure
                        udpListener.connect(dgp.getAddress(), dgp.getPort());
                        int ret = Measurements.UDPCapacityPPSender(udpListener, udp_capacity_pktsize, udp_capacity_num_tests, controlSocketObserver);
                        if (ret < 0) {
                            System.out.println("Start measure with observer FAILED");
                            controlSocketObserver.closeConnection();
                        }

                        udpListener.disconnect();

                            if (controlSocketObserver.receiveCMD().compareTo(ControlMessages.Messages
                                    .SUCCEDED.toString()) != 0) {
                                System.out.println("measure failed");
                                controlSocketObserver.closeConnection();
                                break;
                            }
                        }
                    catch(Exception e){
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);


                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            try {
                                Thread.sleep(30 * 1000);
                            } catch (InterruptedException e2) {
                                e2.printStackTrace();
                            }

                        }

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

                    } catch (Exception ex) {
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
                        Map<Integer, Long[]> latency  = Measurements.UDPRTTSender(udpListener, udp_rtt_pktsize, udp_rtt_num_pack);
                        if (latency == null)
                        {
                            System.out.println("Measure filed");
                            //controlSocketObserver.sendCMD(ControlMessages.Messages.FAILED.toString());
                            controlSocketObserver.closeConnection();

                            udpListener.disconnect();
                            break;
                        }
                        udpListener.disconnect();
                        controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());

                        //controlSocketObserver.sendCMD(ControlMessages.Messages.MEASUREDLATENCY
                         //       .toString() + '#' + latency);

                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(controlSocketObserver.getSocket().getOutputStream());
                        objectOutputStream.writeObject(latency);
                        objectOutputStream.close();

                    }
                    catch (Exception ex) {
                        ex.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            try {
                                Thread.sleep(30 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
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


                    } catch (Exception ex) {
                        ex.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            try {
                                Thread.sleep(30 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
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
                        Map<Integer, Long[]> latency = Measurements.TCPRTTSender(tcpRTT, tcp_rtt_pktsize, tcp_rtt_num_pack);

                        controlSocketObserver.sendCMD(ControlMessages.Messages.SUCCEDED.toString());

                        //send data to Aggregator
                        //controlSocketObserver.sendCMD(ControlMessages.Messages.MEASUREDLATENCY
                        //                              .toString() + '#' + latency);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(controlSocketObserver.getSocket().getOutputStream());
                        objectOutputStream.writeObject(latency);
                        objectOutputStream.close();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();

                        while (true)
                        {
                            System.out.println("Restart sockets");
                            int ret= restartSockets();

                            if (ret == 0)
                                break;

                            System.out.println("Failed... restart in 30 seconds");
                            try {
                                Thread.sleep(30 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }


                    break;
                }
            }

            if (controlSocketObserver != null && controlSocketObserver.getSocket() != null  &&!controlSocketObserver.getSocket().isClosed())
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

