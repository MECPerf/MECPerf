package it.unipi.dii.mecperfng.commandlineapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import it.unipi.dii.mecperfng.MainUtils;

public class CommandLineApp {
    private static final int CMDPORT = 6792,
            TCPPORT = 6791,
            UDPPORT = 6790,
            AGGRPORT = 6766;
    private static int command,
            pktSize = 0,
            pktNumber = 0;


    private static final String observerAddress = "131.114.73.2",
            aggregatorAddress = "131.114.73.3",
            measureType = "";
    private static String direction = "",
            keyword = "DEFAULT";


    public static void main(String[] args){
        retrieveMeasureParameters();

        startMeasure();
    }


    private static void retrieveMeasureParameters() {
        try {
            int readInt;
            String readString;
            command = -1;
            direction = "";
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

            while(command == -1) {
                System.out.println("Inserire tipo misura");
                System.out.println("\t1) TCP Bandwidth");
                System.out.println("\t2) TCP RTT");
                System.out.println("\t3) UDP Bandwidth");
                System.out.println("\t4) UDP RTT");
                try {
                    readInt = Integer.parseInt(inputReader.readLine());
                }catch (NumberFormatException ne){
                    continue;
                }

                if (readInt < 1 || readInt > 4)
                    continue;

                command = readInt;
            }

            while(direction.compareTo("") == 0) {

                System.out.println("Inserire la direzione");
                System.out.println("\t1) Sender");
                System.out.println("\t2) Receiver");
                try{
                    readInt = Integer.parseInt(inputReader.readLine());
                }catch (NumberFormatException ne){
                    continue;
                }

                switch (readInt) {
                    case 1:
                        direction = "Sender";
                        break;
                    case 2:
                        direction = "Receiver";
                        break;
                }
            }

            System.out.println("Camobiare la keyword? (keyword = " + keyword + ")\n y/n");
            readString = inputReader.readLine();

            System.out.println("readString: " + readString);
            if (readString.equals("y"))
                keyword = inputReader.readLine();
            else
                System.out.println("DIFFERENT");


            while(true) {
                System.out.println("Inserire la dimensione del pacchetto");
                try {
                    readInt = Integer.parseInt(inputReader.readLine());
                }catch (NumberFormatException ne){
                    continue;
                }

                if (readInt < 1)
                    continue;

                pktSize = readInt;
                break;
            }

            if (command != 3)
                while(true) {
                    System.out.println("Inserire il numero di pacchetti");
                    try {
                        readInt = Integer.parseInt(inputReader.readLine());
                    }catch (NumberFormatException ne){
                        continue;
                    }

                    if (readInt < 1)
                        continue;

                    pktNumber = readInt;
                    break;
                }


        }catch(IOException ioe){
            ioe.printStackTrace();
            return;
        }
    }


    private static void startMeasure() {
        System.out.println("\n\nDirection: " + direction);
        System.out.println("Keyword: " + keyword);
        System.out.println("pktSize: " + pktSize);
        System.out.println("pktNumber: " + pktNumber);

        int ret = 0;

        switch (command) {
            case 1: //1) TCP Bandwidth
                System.out.println("Type of measure: TCP bandwidth");

                ret = MainUtils.tcpBandwidthMeasure(direction, keyword, CMDPORT,
                                    observerAddress, TCPPORT, aggregatorAddress, AGGRPORT, pktSize,
                                    pktNumber);

                break;

            case 3: //3)UDP Bandwidth"
                System.out.println("Type of measure: UDP bandwidth");

                    ret = MainUtils.udpBandwidthMeasure(direction, keyword, CMDPORT,
                            observerAddress, UDPPORT, aggregatorAddress, AGGRPORT, pktSize);
                break;

            case 2: //2) TCP RTT
                System.out.println("Type of measure: TCP RTT");

                    ret =  MainUtils.tcpRTTMeasure(direction, keyword, CMDPORT, observerAddress,
                                     TCPPORT, aggregatorAddress, AGGRPORT, pktSize, pktNumber);

                break;
            case 4: //4) UDP RTT
                System.out.println("Type of measure: UDP RTT");
                ret = MainUtils.udpRTTMeasure(direction, keyword, CMDPORT, observerAddress, UDPPORT,
                            aggregatorAddress, AGGRPORT,pktSize, pktNumber);
                break;
        }

        System.out.println(ret);
    }

}


