package it.unipi.dii.mecperfng;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;


import it.unipi.dii.common.Measure;
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
     * @param aggregatorAddress
     * @param aggregatorPort
     * @param tcp_bandwidth_pktsize
     * @param tcp_bandwidth_num_pkt
     * @return: 0 = successfull
     *          -1 = IOException
     */
    public static int tcpBandwidthMeasure(String direction,  String keyword, int commandPort,
                                             String observerAddress, int observerPort,
                                             String aggregatorAddress, int aggregatorPort,
                                             int tcp_bandwidth_pktsize, int tcp_bandwidth_num_pkt){
        try{
            int tcp_bandwidth_stream = tcp_bandwidth_num_pkt * tcp_bandwidth_pktsize;

            Socket communicationSocket = new Socket(InetAddress.getByName(observerAddress), observerPort);
            ControlMessages controlSocketObserver= new ControlMessages(observerAddress, commandPort);

            if (direction.equals("Sender")) {
                controlSocketObserver.sendCMD( "TCPBandwidthSender" + "#" + keyword + "#" +
                                       tcp_bandwidth_pktsize + "#" + tcp_bandwidth_num_pkt);
                if (controlSocketObserver.receiveCMD().compareTo(controlSocketObserver.messages.START.toString()) != 0) {
                    System.out.println("Start measure with Observer FAILED");
                    return -1;
                }
                Measurements.TCPBandwidthSender(communicationSocket, tcp_bandwidth_stream, tcp_bandwidth_pktsize);
            }
            else {
                controlSocketObserver.sendCMD("TCPBandwidthReceiver" + "#" + '0' + "#" + keyword +
                                       "#"+tcp_bandwidth_pktsize+"#"+tcp_bandwidth_num_pkt);

                Map<Long, Integer> longIntegerMap = Measurements.
                                   TCPBandwidthReceiver(communicationSocket, tcp_bandwidth_pktsize);

                sendDataToAggregator(aggregatorAddress, aggregatorPort,"TCPBandwidth",
                        "Observer","Client", -1, longIntegerMap,
                        keyword, tcp_bandwidth_pktsize, tcp_bandwidth_num_pkt);
            }

            String measureOutcome = controlSocketObserver.receiveCMD();
            controlSocketObserver.closeConnection();
            if (measureOutcome.compareTo(controlSocketObserver.messages.COMPLETED.toString()) == 0) {
                return 0;
            }

            return -1;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return -1;
        }
    }



    public static int udpBandwidthMeasure(String direction,  String keyword,int commandPort,
                                             String observerAddress, int observerPort,
                                             String aggregatorAddress, int aggregatorPort,
                                             int udp_bandwidth_pktsize){
        try{
            ControlMessages controlSocketObserver= new ControlMessages(observerAddress, commandPort);
            DatagramSocket connectionSocket = new DatagramSocket();
            connectionSocket.connect(InetAddress.getByName(observerAddress), observerPort);


            if (direction.equals("Sender")) {
                controlSocketObserver.sendCMD("UDPCapacityPPSender" + "#0#" + keyword+ "#"+
                                       udp_bandwidth_pktsize);
                try{
                    //TODO sostituire la sleep con una ACK da parte del Receiver => "Sono pronto a ricevere"
                    Thread.sleep(10000);
                }catch( InterruptedException e) {
                    e.printStackTrace();
                }

                Measurements.UDPCapacityPPSender(connectionSocket, udp_bandwidth_pktsize);
            } else {
                controlSocketObserver.sendCMD("UDPCapacityPPReceiver" + "#0#" + keyword+ "#"+ udp_bandwidth_pktsize);

                //Client has to send a packet to server to let the server knows Client's
                // IP and Port
                String outString = "Dummy message";
                byte[] buf = outString.getBytes();
                connectionSocket.send( new DatagramPacket(buf, buf.length));
                Map<Long, Integer> measureResult = Measurements.UDPCapacityPPReceiver(connectionSocket,
                        udp_bandwidth_pktsize);

                sendDataToAggregator(aggregatorAddress, aggregatorPort, "UDPBandwidth",
                              "Observer", "Client",0, measureResult,
                                     keyword, udp_bandwidth_pktsize, 2);
            }

            String measureOutcome = controlSocketObserver.receiveCMD();
            controlSocketObserver.closeConnection();

            if (measureOutcome.compareTo("DONE") == 0) {
                return 0;
            }

            return -1;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return -1;
        }
    }



    public static int tcpRTTMeasure(String direction, String keyword, int commandPort,
                                       String observerAddress, int observerPort,
                                       String aggregatorAddress, int aggregatorPort,
                                       int tcp_rtt_pktsize, int tcp_rtt_num_pack){

        try {
            Socket communicationSocket = new Socket(InetAddress.getByName(observerAddress), observerPort);
            ControlMessages controlSocketObserver= new ControlMessages(observerAddress, commandPort);

            if (direction.equals("Sender")) {
                // the client application starts a TCP RTT measure (as sender) with
                // the observer
                controlSocketObserver.sendCMD("TCPRTTC" + "#0#" + keyword + "#" +
                                       tcp_rtt_pktsize + "#" + tcp_rtt_num_pack);

                double latency = Measurements.TCPRTTSender(communicationSocket, tcp_rtt_pktsize,
                                                           tcp_rtt_num_pack);
                sendDataToAggregator(aggregatorAddress, aggregatorPort, "TCPRTT",
                              "Client","Observer", latency, null, keyword,
                                     tcp_rtt_pktsize, tcp_rtt_num_pack);
            } else {
                //the observer starts a TCP RTT measure using the mobile application
                // as receiver
                controlSocketObserver.sendCMD("TCPRTTMO" + "#0#" + keyword + "#" +
                                       tcp_rtt_pktsize + "#" + tcp_rtt_num_pack);

                Measurements.TCPRTTReceiver(communicationSocket, tcp_rtt_pktsize, tcp_rtt_num_pack);
            }

            String measureOutcome = controlSocketObserver.receiveCMD();
            controlSocketObserver.closeConnection();

            if (measureOutcome.compareTo("DONE") == 0) {
                return 0;
            }

            return -1;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return -1;
        }
    }



    public static int udpRTTMeasure(String direction, String keyword, int commandPort,
                                       String observerAddress, int observerPort,
                                       String aggregatorAddress, int aggregatorPort,
                                       int udp_rtt_pktsize, int udp_rtt_num_pack){

        try {
            ControlMessages controlSocketObserver= new ControlMessages(observerAddress, commandPort);
            DatagramSocket udpsocket = new DatagramSocket();
            udpsocket.connect(InetAddress.getByName(observerAddress), observerPort);

            if (direction.equals("Sender")) {
                // the client application starts a TCP RTT measure as sender. The
                // observer is the receiver

                controlSocketObserver.sendCMD("UDPRTTC" + "#0#" + keyword +"#" + udp_rtt_pktsize +
                                       "#" + udp_rtt_num_pack);
                double latency = Measurements.UDPRTTSender(udpsocket, udp_rtt_pktsize,
                                       udp_rtt_num_pack);
                sendDataToAggregator(aggregatorAddress, aggregatorPort, "UDPRTT",
                              "Client","Observer", latency, null, keyword,
                                     udp_rtt_pktsize, udp_rtt_num_pack);
            } else {
                // the client application starts a TCP RTT measure with the observer as
                //sender

                controlSocketObserver.sendCMD("UDPRTTMO" + "#0#" + keyword+"#" + udp_rtt_pktsize +
                                       "#" + udp_rtt_num_pack);
                String outString = "DummyPacket";
                byte[] buf = outString.getBytes();
                udpsocket.send(new DatagramPacket(buf, buf.length));
                Measurements.UDPRTTReceiver(udpsocket, udp_rtt_pktsize, udp_rtt_num_pack);
            }

            String measureOutcome = controlSocketObserver.receiveCMD();
            controlSocketObserver.closeConnection();

            if (measureOutcome.compareTo("DONE") == 0) {
                return 0;
            }

            return -1;
        } catch (IOException ioe) {
            return -1;
        }
    }



    protected static  void sendDataToAggregator(String aggregatorAddress, int aggregatorPort,
                                                String type, String sender, String receiver,
                                                double latency, Map<Long, Integer> bandwidth,
                                                String keyword, int len_pack, int num_pack) {
        Socket socket = null;
        ObjectOutputStream objOutputStream = null;


        try {
            socket = new Socket(InetAddress.getByName(aggregatorAddress), aggregatorPort);
            objOutputStream = new ObjectOutputStream(socket.getOutputStream());
            Measure measure = new Measure(type, sender, receiver, bandwidth, latency, keyword,
                                          len_pack, num_pack);

            objOutputStream.writeObject(measure);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } finally {
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