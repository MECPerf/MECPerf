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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Measurements {


    /**
     * Number of test made in the latency calculation
     */
//    private static final int NUMLATENCYTEST = 100;  //RTT UDP & TCP

    /**
     * Size of the packet sent through the network
     */
//    private static final int PKTSIZE_FOR_TCP_RTT = 1; //dimensione pacchetto rtt TCP
//    private static final int PKTSIZE_FOR_UDP_LATENCY = 1;//dimensione pacchetto rtt UDP
    private static final long serialVersionUID = 3919700812200232178L;
    //private static final int TCP_BANDWIDTH_BUFFER_LEN = 1024; //dimensione pkt TCP-bandwidth
//    public static final int TCPBANDWIDTH_NUM_OF_BYTES = 1024*TCP_BANDWIDTH_BUFFER_LEN; // 1MB //dimensione stream TCP-bandwidth


    /**
     * This function is used by the receiver to measure the RTT using the UDP protocol
     * The receiver receives a packet and sends another packet to the sender
     *
     * @param serverSocket The Socket use for the communication
     * @throws IOException
     */
    public static void UDPRTTReceiver(DatagramSocket serverSocket, int  udp_rtt_pktsize, int udp_rtt_num_pack) throws IOException {
        byte[] sendData = new byte[udp_rtt_pktsize];
        byte[] receiveData = new byte[udp_rtt_pktsize];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);

        for (int i = 0; i < udp_rtt_num_pack; i++) {
            serverSocket.receive(receivePacket);
            InetAddress IPAddress = receivePacket.getAddress();
            sendPacket.setAddress(IPAddress);
            int port = receivePacket.getPort();
            sendPacket.setPort(port);
            serverSocket.send(sendPacket);
        }
    }

    /**
     * This function is used by the sender to connect with the receiver using the UDP protocol.
     * The sender starts the communication sending a packet and when receive a packet compute the latency.
     * After NUMLATENCYTEST communications, the function computes the mean latency value.
     *
     * @param connectionSocket The Socket use for the comunication
     * @return The mean latency value calculated using the Round Trip Time approach
     * @throws IOException
     */

    //TODO FUNZIONE RTT UGUALE SIA PER TCP CHE UDP (E' DUPLICATA)
    public static double UDPRTTSender(DatagramSocket connectionSocket, int  udp_rtt_pktsize, int udp_rtt_num_pack) throws IOException {
        byte[] sendData = new byte[udp_rtt_pktsize];
        byte[] receiveData = new byte[udp_rtt_pktsize];
        Random rand = new Random();
        rand.nextBytes(sendData);

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        long meanValue = 0;
        for (int i = 0; i < udp_rtt_num_pack; i++) {
            long startTime = System.currentTimeMillis();
            connectionSocket.send(sendPacket);
            connectionSocket.receive(receivePacket);
            long endTime = System.currentTimeMillis();
            meanValue += endTime - startTime;
        }

        double result = ((double) meanValue) / udp_rtt_num_pack;
        return result;
    }

    /**
     * This function is used by the sender to connect with the receiver using the TCP protocol.
     * The sender starts the communication sending a packet and when receive a packet compute the latency.
     * After NUMLATENCYTEST communications, the function computes the mean latency value.
     *
     * @param socket The Socket use for the comunication
     * @return The mean latency calculated using the Round Trip Time
     * @throws IOException
     */
    public static double TCPRTTSender(Socket socket, int tcp_rtt_pktsize, int tcp_rtt_num_pack) throws IOException {
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
     */
    public static void TCPRTTReceiver(Socket sock, int tcp_rtt_pktsize, int tcp_rtt_num_pack) throws IOException {
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
     */
    public static void TCPBandwidthSender(Socket socket, int number_of_bytes, int tcp_bandwidth_pktsize) throws IOException {
        System.out.println("Sending " + number_of_bytes + " bytes<");
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
     * @return The map that contain all the timestamps and the amount of data for each packet received
     */
    public static Map<Long, Integer> TCPBandwidthReceiver(Socket connectionSocket, int tcp_bandwidth_pktsize) throws IOException {

        InputStream isr = null;
        Map<Long, Integer> mappa = new LinkedHashMap<>();
        int totalRead;
        byte[] cbuf = new byte[tcp_bandwidth_pktsize];

        try {
            isr = connectionSocket.getInputStream();

            int i = 0;
            long last = 0;

            while ((totalRead = isr.read(cbuf)) != -1) {

                long actualTime = System.nanoTime();
                long diff =  actualTime - last;

                if (diff < 0)
                    System.exit(1);

                i++;
                // misuro l'istante di tempo ogni volta che faccio la write

                mappa.put(actualTime, totalRead);;
                //mappa.put((long)i, totalRead);

                last = actualTime;
            }
        }
        finally{
            if (isr != null)
                isr.close();
            if (connectionSocket != null)
                connectionSocket.close();
        }

        System.out.println("LUNGHEZZA MAPPA: " + mappa.size());
        return mappa;
    }



    /**
     * This function is used by the sender to connect with the receiver using the UDP protocol.
     * This function send two datagram packets through the network
     *
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

        //wait for first packet on socket
        DatagramPacket receivePacket1 = new DatagramPacket(receiveData1, receiveData1.length);
        DatagramPacket receivePacket2 = new DatagramPacket(receiveData2, receiveData2.length);

        try {
            serverSocket.receive(receivePacket1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        long firstTime = System.nanoTime();

        //wait for second packet
        try {
            serverSocket.receive(receivePacket2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long currentTime = System.nanoTime();
        long timeNs = currentTime - firstTime;

        System.out.println("pktSize " + pktSize);
        System.out.println("timems " + timeNs + "(" + currentTime + "-" + firstTime + ")");

        System.out.println("latency: " + timeNs+ "ns");

        Map<Long, Integer> measureResult = new LinkedHashMap<>();
        measureResult.put(timeNs, pktSize);

        double result = -1;

        for (Map.Entry<Long, Integer> entry : measureResult.entrySet()) {
            result =  entry.getKey()  / 1000000; //ms
            result =  entry.getValue()/result; //B/ms
            result = result / 1024; //KB/ms
            result = result * 1000; //KB/s
        }

        System.out.println("result: " + result+ "KB/s");

        return measureResult;   //deve essere nello stesso formato del TCP per stare nella stessa tabella, per questo non ritorno "result KB/s"
    }
}

