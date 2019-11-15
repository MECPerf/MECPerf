package it.unipi.dii.common;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Measurements {
    private static final long serialVersionUID = 3919700812200232178L;



    /**
     * This function is used by the receiver to measure the RTT using the UDP protocol
     * The receiver receives a packet and sends another packet to the sender
     *
     * @param serverSocket The Socket used for the communication
     * @param udp_rtt_pktsize The size of each UDP packet used
     * @param udp_rtt_num_pack  The number of UDP packets used
     * @throws IOException
     */
    public static int UDPRTTReceiver(DatagramSocket serverSocket, int  udp_rtt_pktsize,
                                      int udp_rtt_num_pack) throws IOException {
        byte[] sendData = new byte[udp_rtt_pktsize];
        byte[] receiveData = new byte[udp_rtt_pktsize];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
        serverSocket.setSoTimeout(10000); //10s timeout
        try {
            for (int i = 0; i < udp_rtt_num_pack; i++) {

                serverSocket.receive(receivePacket);
                InetAddress IPAddress = receivePacket.getAddress();
                sendPacket.setAddress(IPAddress);
                int port = receivePacket.getPort();
                sendPacket.setPort(port);
                serverSocket.send(sendPacket);
            }
        }
        catch (SocketTimeoutException e){
            System.out.println("UDPRTT: SOCKET TIMEOUT.\nReturning.");
            return -1;
        }

        return 0;
    }



    /**
     * This function is used by the sender to connect with the receiver using the UDP protocol.
     * The sender starts the communication sending a packet and when receive a packet compute the latency.
     * After NUMLATENCYTEST communications, the function computes the mean latency value.
     *
     * @param connectionSocket The Socket use for the comunication
     * @param udp_rtt_pktsize The size of each UDP packet used
     * @param udp_rtt_num_pack The number of UDP packets used
     * @return -1 if the timeout expired. Otherwise the mean latency value calculated for a RTT
     *         measure is returned
     * @throws IOException
     */
    public static double UDPRTTSender(DatagramSocket connectionSocket, int  udp_rtt_pktsize,
                                      int udp_rtt_num_pack) throws IOException {
        byte[] sendData = new byte[udp_rtt_pktsize];
        byte[] receiveData = new byte[udp_rtt_pktsize];
        Random rand = new Random();
        rand.nextBytes(sendData);

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        connectionSocket.setSoTimeout(10000);// 10s timeout

        long meanValue = 0;
        try {
            for (int i = 0; i < udp_rtt_num_pack; i++) {
                long startTime = System.currentTimeMillis();
                connectionSocket.send(sendPacket);
                connectionSocket.receive(receivePacket);
                long endTime = System.currentTimeMillis();
                meanValue += endTime - startTime;
            }
        }
        catch (SocketTimeoutException e){
            System.out.println("UDPRTT: SOCKET TIMEOUT.\nReturning.");
            return -1;
        }

        return ((double) meanValue) / udp_rtt_num_pack;
    }



    /**
     * This function is used by the sender to connect with the receiver using the TCP protocol.
     * The sender starts the communication sending a packet and when receive a packet compute the latency.
     * After NUMLATENCYTEST communications, the function computes the mean latency value.
     *
     * @param socket The Socket use for the comunication
     * @param tcp_rtt_pktsize The size of each TCP packet used
     * @param tcp_rtt_num_pack The number of TCP packets used
     * @return The mean latency calculated using the Round Trip Time
     * @throws IOException
     */
    public static double TCPRTTSender(Socket socket, int tcp_rtt_pktsize, int tcp_rtt_num_pack)
                                                                                throws IOException {
        OutputStream outputStream = null;
        InputStream inputStream = null;

        byte[] sendData = new byte[tcp_rtt_pktsize];
        Random rand = new Random();
        rand.nextBytes(sendData);

        long total = 0;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            long lBegin,
                 lEnd;

            for (int i = 0; i < tcp_rtt_num_pack; i++) {
                outputStream.write(sendData, 0, tcp_rtt_pktsize);
                lBegin = System.currentTimeMillis();
                int ret = 0;
                while (ret < tcp_rtt_pktsize){
                    ret += inputStream.read(sendData);
                }
                lEnd = System.currentTimeMillis();
                total += lEnd - lBegin;
            }
        } finally {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
        }
        return ((double) (total)) / tcp_rtt_num_pack;
    }



    /**
     * This function is used by the receiver to measure the RTT using the UDP protocol
     * The receiver receives a packet and sends another packet to the sender
     *
     * @param sock The Socket use for the comunication
     * @param tcp_rtt_pktsize The size of each TCP packet used
     * @param tcp_rtt_num_pack The number of TCP packets used
     * @throws IOException
     */
    public static void TCPRTTReceiver(Socket sock, int tcp_rtt_pktsize, int tcp_rtt_num_pack)
                                                                                throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        byte[] sendData = new byte[tcp_rtt_pktsize]; //TODO BUG: se diverso da 1 Causa Eccezione
        Random rand = new Random();
        rand.nextBytes(sendData);

        try {
            inputStream = sock.getInputStream();
            outputStream = sock.getOutputStream();
            for (int i = 0; i < tcp_rtt_num_pack; i++) {
                int ret = 0;
                while (ret < tcp_rtt_pktsize){
                    ret += inputStream.read(sendData);
                }

                outputStream.write(sendData, 0, tcp_rtt_pktsize);
            }
        }finally {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (sock != null)
                sock.close();
        }
    }



    /**
     * This function is used by the sender to connect with the receiver using the TCP protocol.
     * The sender send a file through the network
     *
     * @param socket The Socket use for the comunication
     * @param number_of_bytes the number of bytes to be transferred
     * @param tcp_bandwidth_pktsize The size of each TCP packet used
     * @throws IOException
     */
    public static void TCPBandwidthSender(Socket socket, int number_of_bytes,
                                          int tcp_bandwidth_pktsize) throws IOException {
        OutputStream outputStream = null;
        byte[] buffer = new byte[tcp_bandwidth_pktsize];
        Random random = new Random();
        random.nextBytes(buffer);//fill the buffer with random bytes

        try {
            outputStream = socket.getOutputStream();

            while (number_of_bytes > 0) {
                if (number_of_bytes < tcp_bandwidth_pktsize) {
                    outputStream.write(buffer, 0, number_of_bytes);
                    number_of_bytes = 0;
                }
                else {
                    outputStream.write(buffer, 0, tcp_bandwidth_pktsize);
                    number_of_bytes -= tcp_bandwidth_pktsize;
                }
            }
        } finally {
            if(outputStream != null)
                outputStream.close();
            if(socket != null)
                socket.close();
        }
    }




    /**
     * This function is used by the receiver to connect with the sender using the TCP protocol.
     * The receiver receive the file and for each packet save in the map the timestamp and the amount of
     * data received.
     *
     * @param connectionSocket The Socket use for the comunication
     * @param tcp_bandwidth_pktsize The size of each TCP packet used
     * @return The map that contain all the timestamps and the amount of data for each packet received
     */
    public static Map<Long, Integer> TCPBandwidthReceiver(Socket connectionSocket,
                                                          int tcp_bandwidth_pktsize) throws IOException {

        InputStream isr = null;
        Map<Long, Integer> mappa = new LinkedHashMap<>();
        int totalRead;
        byte[] cbuf = new byte[tcp_bandwidth_pktsize];

        try {
            isr = connectionSocket.getInputStream();

            long last = 0;

            while ((totalRead = isr.read(cbuf)) != -1) {

                long actualTime = System.nanoTime();
                long diff =  actualTime - last;

                if (diff < 0)
                    System.exit(1);

                mappa.put(actualTime, totalRead);

                last = actualTime;
            }
        }
        finally{
            if (isr != null)
                isr.close();
            if (connectionSocket != null)
                connectionSocket.close();
        }


        return mappa;
    }



    /**
     * This function is used by the sender to connect with the receiver using the UDP protocol.
     * This function send two datagram packets through the network
     *
     *
     * @param connectionSocket The Socket used for the comunication
     * @param packet_size The size of each packet used
     */
    public static void UDPCapacityPPSender(DatagramSocket connectionSocket, int packet_size) {
        byte[] buf = new byte[packet_size];
        Random rand = new Random();
        rand.nextBytes(buf);

        DatagramPacket packet1 = new DatagramPacket(buf, buf.length);
        DatagramPacket packet2 = new DatagramPacket(buf, buf.length);

        //send 2 packets
        try{
            connectionSocket.send(packet1);
            connectionSocket.send(packet2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * This function is used by the receiver to connect with the sender using the UDP protocol.
     * This function receive two datagram packets and compute the difference between the two timestamps
     *
     * @param serverSocket The Socket use for the comunication
     * @param pktSize    Amount of data to receive for each comunication
     * @return The latency calculated using the packet pair approach
     */
    public static Map<Long, Integer> UDPCapacityPPReceiver(DatagramSocket serverSocket, int pktSize) {
        byte[] receiveData1 = new byte[pktSize];
        byte[] receiveData2 = new byte[pktSize];

        long firstTime = 0,
             currentTime,
             timeNs;

        //wait for first packet on socket
        DatagramPacket receivePacket1 = new DatagramPacket(receiveData1, receiveData1.length);
        DatagramPacket receivePacket2 = new DatagramPacket(receiveData2, receiveData2.length);

        try {
            serverSocket.setSoTimeout(10000);//timeout = 10s
            serverSocket.receive(receivePacket1);
            firstTime = System.nanoTime();
            //receive the second packet
            serverSocket.receive(receivePacket2);
        }
        catch (SocketTimeoutException e){
            System.out.println("UDPRTT: SOCKET TIMEOUT.\nReturning.");
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    currentTime = System.nanoTime();
        timeNs = currentTime - firstTime;


        Map<Long, Integer> measureResult = new LinkedHashMap<>();
        measureResult.put(timeNs, pktSize);
        return measureResult;   //deve essere nello stesso formato del TCP per stare nella stessa tabella, per questo non ritorno "result KB/s"
    }
}

