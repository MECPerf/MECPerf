package it.unipi.dii.aggregator;

/*
This code was implemented by Enrico Alberti.
The use of this code is permitted by BSD licenses
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unipi.dii.common.Measure;
import it.unipi.dii.common.MeasureResult;

public class Aggregator {
    private static final int AGGREGATOR_PORT = 6766;

    private static final String INSERT_TEST_TABLE = "INSERT INTO MECPerf.Test (TestNumber,Timestamp,"
                                                    + " Direction, Command, SenderIdentity, "
                                                    + "ReceiverIdentity, SenderIPv4Address, "
                                                    + "ReceiverIPv4Address,  Keyword, PackSize, "
                                                    + "NumPack)  VALUES (?, CURRENT_TIMESTAMP, ?, ?,"
                                                    +                   "?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_BANDWIDTH_TABLE = "INSERT INTO MECPerf.BandwidthMeasure " +
                                                         " VALUES (?, ?, ?, ?)";

    private static final String INSERT_LATENCY_TABLE = "INSERT INTO MECPerf.RttMeasure "
            + " VALUES (?, ?)";


   /* private static final String SELECT_ALL_TEST_TABLE = "SELECT DATE_FORMAT(Timestamp, '%Y-%m-%d %T') As Timestamp, Command, Keyword, Sender, Receiver "
            + " FROM MECPerf.Test "
            + " GROUP BY ID, Timestamp, Command, Keyword "
            + " ORDER BY (Timestamp) DESC ";*/

    /*private static final String SELECT_MEASURE_RTT_TABLE = "SELECT Sender, Receiver, Command, latency, Keyword "
            + " FROM MECPerf.RttMeasure INNER JOIN MECPerf.Test ON(Test.ID=RttMeasure.id) "
            + " WHERE DATE_FORMAT(Timestamp, '%Y-%m-%d %T') = ? "
            + " AND Sender = ? ";*/

    /*private static final String SELECT_MEASURE_BANDWIDTH_TABLE = "SELECT Sender, Receiver, Command, nanoTimes, kBytes, Keyword "
            + " FROM MECPerf.BandwidthMeasure INNER JOIN MECPerf.Test ON(Test.ID=BandwidthMeasure.id) "
            + " WHERE DATE_FORMAT(Timestamp, '%Y-%m-%d %T') = ? "
            + " AND Sender = ? ";*/

    private static final String SELECT_AVG_MEASURE_BANDWIDTH_TABLE= "SELECT Test.Sender, Test.Receiver, Test.Command, ((SUM(kBytes) / SUM(nanoTimes))*1000000000) as Bandwidth, Keyword "
            + " FROM MECPerf.BandwidthMeasure INNER JOIN MECPerf.Test ON(Test.ID=BandwidthMeasure.id) "
            + " WHERE DATE_FORMAT(Timestamp, '%Y-%m-%d %T') = ? "
            + " AND Sender = ? "
            + " GROUP BY Test.ID, Test.Sender, Test.Receiver, Test.Command ";

    private static final String SELECT_TEST_NUMBER= "SELECT ID, TestNumber FROM MECPerf.Test  "
                                                    + "ORDER BY ID desc " ;



    public static void main (String[] args){
        ServerSocket welcomeSoket = null;

        try {
            welcomeSoket = new ServerSocket(AGGREGATOR_PORT);
        }catch (IOException | NullPointerException e ){
            e.printStackTrace();
        }

        while (true) {
            try {
                System.out.println("Aggregator in attesa di connessione... " );
                Socket connectionSocket = welcomeSoket.accept();

                InputStream isr = connectionSocket.getInputStream();
                ObjectInputStream mapInputStream = new ObjectInputStream(isr);
                Measure measure = (Measure) mapInputStream.readObject();


                switch(measure.getType()){
                    case "TCPBandwidth":
                    case "UDPBandwidth":
                    case "TCPRTT":
                    case "UDPRTT": {
                        System.out.println("Comando: " + measure.getType());

                        Measure measureSecondSegment = (Measure) mapInputStream.readObject();

                        try(
                            Connection dbConnection = DriverManager.getConnection(
                                        "jdbc:mysql://131.114.73.3:3306/MECPerf?useSSL=false",
                                        "MECPerf","password")
                            ){
                            dbConnection.setAutoCommit(false);

                            long id = writeToDB(measure, measureSecondSegment, dbConnection);
                            if(id == -1){
                                System.out.println("Inserimento in Tabella Test Fallito");
                            }

                            dbConnection.setAutoCommit(true);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                   /* case "GET_DATA_LIST":{
                        System.out.println("Comando: GET_DATA_LIST");
                        ObjectOutputStream objOutputStream = null;
                        objOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                        List<String> list = loadDataFromDb();

                        System.out.println("LISTA: " + list);
                        objOutputStream.writeObject(list);
                        break;
                    }*/
                    /*case "GET_RTT_DATA":{
                        System.out.println("Comando: GET_RTT_DATA");
                        ObjectOutputStream objOutputStream = null;
                        objOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                        System.out.println("DATA QUERY: " + measure.getExtra());
                        List<MeasureResult> obj = loadRttDataFromDb(measure.getExtra(), measure.getSender());
                        System.out.println("OGGETTO_RTT: " + obj);
                        objOutputStream.writeObject(obj);
                        break;
                    }*/
                   /* case "GET_BANDWIDTH_DATA":{
                        System.out.println("Comando: GET_BANDWIDTH_DATA");
                        ObjectOutputStream objOutputStream = null;
                        objOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                        System.out.println("DATA QUERY: " + measure.getExtra());
                        List<MeasureResult> obj = loadBandwidthDataFromDb(measure.getExtra(), measure.getSender());
                        System.out.println("OGGETTO_RTT: " + obj);
                        objOutputStream.writeObject(obj);
                        break;
                    }*/
                    case "GET_AVG_BANDWIDTH_DATA":{
                        System.out.println("Comando: GET_AVG_BANDWIDTH_DATA");
                        ObjectOutputStream objOutputStream = null;
                        objOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                        System.out.println("DATA QUERY: " + measure.getExtra());
                        List<MeasureResult> obj = loadAVGBandwidthDataFromDb(measure.getExtra(), measure.getSender());
                        System.out.println("OGGETTO_RTT: " + obj);
                        objOutputStream.writeObject(obj);
                        break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
        try {
            if (welcomeSoket != null)
                welcomeSoket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static void writeToDB_Latency(Double latency, long id, Connection co) throws SQLException {

        try (PreparedStatement ps = co.prepareStatement(INSERT_LATENCY_TABLE);
        ){
            ps.setInt(1, (int)id);
            ps.setDouble(2, latency);
            System.out.println("rows affected: " + ps.executeUpdate());
            co.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (co != null) {
                try {
                    System.out.print("Transaction is being rolled back");
                    co.rollback();
                } catch(SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }



    private static void writeToDB_Bandwidth(Map<Long, Integer> map, long id, Connection co, String protocol) throws SQLException {
        try (PreparedStatement ps = co.prepareStatement(INSERT_BANDWIDTH_TABLE);
        ){
            ps.setInt(1, (int)id);

            int iteration = 0;
            long previous = 0;
            System.out.println("PROTOCOL: " + protocol+" MAP_SIZE: " + map.size());

            for (Map.Entry<Long, Integer> entry : map.entrySet()) { //per UDP ha un solo elemento
                long actualTime = entry.getKey();
                long diff = actualTime - previous;

                if (Long.MAX_VALUE < actualTime)
                    System.exit(1);


                previous = actualTime;
                iteration++;
                if ((iteration == 1) &&(protocol.equals("TCP")))
                    continue;


                ps.setInt(2, iteration);
                ps.setLong(3, diff);
                ps.setDouble(4, (double)entry.getValue()/1024);

                if((iteration != 1) || (protocol.equals("UDP")))
                    ps.executeUpdate();
            }
            System.out.println("rows affected: " + iteration);

            co.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (co != null) {
                try {
                    System.out.print("Transaction is being rolled back");
                    co.rollback();
                } catch(SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }



    private static long writeToDB(Measure measureFirstSegment, Measure measureSecondSegment,
                                  Connection co) throws SQLException{
        int testNumber = readLastTestNumber() + 1;

        long id = writeSegment(measureFirstSegment, co, testNumber);
        if(id == -1){
            System.out.println("Inserimento in Tabella Test Fallito");

            return -1;
        }

        id = writeSegment(measureSecondSegment, co, testNumber);
        if(id == -1){
            System.out.println("Inserimento in Tabella Test Fallito");

            return -1;
        }


        return id;
    }

    private static long writeSegment(Measure measure, Connection co, int testNumber) throws SQLException{
        long id = -1;

        try (PreparedStatement ps = co.prepareStatement(INSERT_TEST_TABLE,
                Statement.RETURN_GENERATED_KEYS)){
            // 1: TestNumber
            ps.setInt(1, testNumber);
            // 2: Direction

            if (measure.getSender().equals("Client") && measure.getReceiver().equals("Observer") ||
                    measure.getSender().equals("Observer") && measure.getReceiver().equals("Server") )
                ps.setString(2, "Upstream");
            if (measure.getSender().equals("Server") && measure.getReceiver().equals("Observer") ||
                    measure.getSender().equals("Observer") && measure.getReceiver().equals("Client") )
                ps.setString(2, "Downstream");
            // 3: Command
            ps.setString(3, measure.getType());
            // 4: SenderIdentity
            ps.setString(4, measure.getSender());
            // 5: ReceiverIdentity
            ps.setString(5, measure.getReceiver());
            // 6: SenderIPv4Address
            ps.setString(6, measure.getSenderAddress());
            // 7: ReceiverIPv4Address
            ps.setString(7, measure.getReceiverAddress());
            // 8: Keyword
            ps.setString(8, measure.getExtra());//keyword
            // 9: PackSize
            ps.setInt(9, measure.getLen_pack());
            // 10: NumPack
            ps.setInt(10, measure.getNum_pack());

            System.out.println("rows affected: " + ps.executeUpdate());
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getLong(1);


            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(id == -1){
            System.out.println("Inserimento in Tabella Test Fallito");

            co.rollback();
            return -1;
        }

        switch(measure.getType()) {
            case "TCPBandwidth":
            case "UDPBandwidth": {
                writeToDB_Bandwidth(measure.getBandwidth(), id, co, measure.getType().substring(0, 3));
                System.out.println("Inserimento in Tabella Test, Bandwidth effettuato con successo!");
                break;
            }
            case "TCPRTT":
            case "UDPRTT": {
                writeToDB_Latency(measure.getLatency(), id, co);
                System.out.println("Inserimento in Tabella Test, Latency effettuato con successo!");

                break;
            }
        }


        return id;
    }

    private static int readLastTestNumber(){
        int testNumber = -1;

        try (Connection co = DriverManager.getConnection("jdbc:mysql://131.114.73.3:3306/MECPerf?useSSL=false", "MECPerf","password");
             PreparedStatement ps = co.prepareStatement(SELECT_TEST_NUMBER);
        ){
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                testNumber = Integer.parseInt(rs.getString("TestNumber"));
                System.out.println(testNumber);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return testNumber;

    }

    /*private static List<String> loadDataFromDb(){
        List<String> results = new ArrayList<>();

        try (Connection co = DriverManager.getConnection("jdbc:mysql://131.114.73.3/MECPerf", "MECPerf","password");
             PreparedStatement ps = co.prepareStatement(SELECT_ALL_TEST_TABLE);
        ){
            ResultSet rs = ps.executeQuery();
            // the '/' character is the SEPARATOR which the application use to SPLIT
            while (rs.next()) {
                String sender = rs.getString("Sender");
                String receiver = rs.getString("Receiver");
                String direction = sender +"->"+receiver;
                String total = rs.getString("Timestamp") +"/"+rs.getString("Command") +"/"+rs.getString("Keyword") +"/"+direction;
                results.add(total);
            }
            System.out.println("RESULTS: " + results);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }*/


    /*private static List<MeasureResult> loadRttDataFromDb(String date, String sender) {
        List<MeasureResult> results =  new ArrayList<>();

        try (Connection co = DriverManager.getConnection("jdbc:mysql://131.114.73.3:3306/MECPerf", "MECPerf","password");
             PreparedStatement ps = co.prepareStatement(SELECT_MEASURE_RTT_TABLE);
        ){

            ps.setString(1, date);
            ps.setString(2, sender);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MeasureResult tmp = new MeasureResult();
                tmp.setSender(rs.getString("Sender"));
                tmp.setReceiver(rs.getString("Receiver"));
                tmp.setCommand(rs.getString("Command"));
                tmp.setLatency(rs.getDouble("latency"));
                tmp.setKeyword(rs.getString("Keyword"));
                results.add(tmp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }
*/
/*

    private static List<MeasureResult> loadBandwidthDataFromDb(String date, String sender) {
        List<MeasureResult> results =  new ArrayList<>();

        try (Connection co = DriverManager.getConnection("jdbc:mysql://131.114.73.3:3306/MECPerf", "MECPerf","password");
             PreparedStatement ps = co.prepareStatement(SELECT_MEASURE_BANDWIDTH_TABLE);
        ){

            ps.setString(1, date);
            ps.setString(2, sender);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MeasureResult tmp = new MeasureResult();
                tmp.setSender(rs.getString("Sender"));
                tmp.setReceiver(rs.getString("Receiver"));
                tmp.setCommand(rs.getString("Command"));

                Double time = rs.getDouble("nanoTimes");
                Double bytes = rs.getDouble("kBytes");

                time = time / 1000000; //ms
                Double bandwidth =  bytes/time; //KB/ms
                //bandwidth = bandwidth / 1024; //KB/ms Già in KB sul DB
                bandwidth = bandwidth * 1000; //KB/s

                tmp.setBandwidth(bandwidth);

                tmp.setKeyword(rs.getString("Keyword"));
                results.add(tmp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

*/

    private static List<MeasureResult> loadAVGBandwidthDataFromDb(String date, String sender) {
        List<MeasureResult> results =  new ArrayList<>();

        try (Connection co = DriverManager.getConnection("jdbc:mysql://131.114.73.3:3306/MECPerf", "MECPerf","password");
             PreparedStatement ps = co.prepareStatement(SELECT_AVG_MEASURE_BANDWIDTH_TABLE);
        ){

            ps.setString(1, date);
            ps.setString(2, sender);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MeasureResult tmp = new MeasureResult();
                tmp.setSender(rs.getString("Sender"));
                tmp.setReceiver(rs.getString("Receiver"));
                tmp.setCommand(rs.getString("Command"));
                tmp.setBandwidth(rs.getDouble("Bandwidth"));
                tmp.setKeyword(rs.getString("Keyword"));

                results.add(tmp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

}

