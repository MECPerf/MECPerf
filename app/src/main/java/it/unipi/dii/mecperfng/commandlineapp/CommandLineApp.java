package it.unipi.dii.mecperfng.commandlineapp;



import it.unipi.dii.mecperfng.MainUtils;



public class CommandLineApp {
    private static int CMDPORT = -1,
                       TCPPORT = -1,
                       UDPPORT = -1,
                       pktSizeTCPBandwidth = -1,
                       pktSizeUDPCapacity = -1,
                       pktSizeTCPLatency = -1,
                       pktSizeUDPLatency = -1,
                       numPktTCPBandwidth = -1,
                       numTestsUDPCapacity = -1,
                       numPktTCPLatency = -1,
                       numPktUDPLatency = -1,
                       timerInterval = -1,
                       attemptTimer = 30 * 1000,
                       MAXATTEMPTNUMBER = 3;

    private static String observerAddress = null,
                          measureType = "all",
                          direction = "all",
                          keyword = "DEFAULT",
                          interfaceName = null;



    public static void main(String[] args){
        parseArguments(args);

        System.out.println("Observer address: " + observerAddress);
        System.out.println("Command port: " + CMDPORT);
        System.out.println("TCP port: " + TCPPORT);
        System.out.println("UDP port: " + UDPPORT);

        System.out.println("Type of measure: " + measureType);
        System.out.println("direction: " + direction);
        System.out.println("Keyword: " + keyword);

        System.out.println("pktSizeTCPBandwidth: " + pktSizeTCPBandwidth);
        System.out.println("pktSizeUDPCapacity: " + pktSizeUDPCapacity);
        System.out.println("pktSizeTCPLatency: " + pktSizeTCPLatency);
        System.out.println("pktSizeUDPLatency: " + pktSizeUDPLatency);
        System.out.println("numPktTCPBandwidth: " + numPktTCPBandwidth);
        System.out.println("numTestsUDPCapacity: " + numTestsUDPCapacity);
        System.out.println("numPktTCPLatency: " + numPktTCPLatency);
        System.out.println("numPktUDPLatency: " + numPktUDPLatency);
        System.out.println("MAXATTEMPTNUMBER: " + MAXATTEMPTNUMBER);

        if (interfaceName != null) {
            System.out.println("interface name: " + interfaceName);
        }


        if (timerInterval < 0)
            startMeasure();
        else{
            System.out.println("\n\nTimer interval = " + timerInterval);

            while (true){
                startMeasure();
                System.out.println("\n*************************** SLEEPING ***************************\n");

                try {
                    Thread.sleep(timerInterval * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private static void parseArguments(String[] args){
        for (int i = 0; i< args.length; i++) {

            if (args[i].equals("-o") || args[i].equals("--observer-address")) {
                observerAddress = args[++i];
                continue;
            }
            if (args[i].equals("-c") || args[i].equals("--command-port")){
                 CMDPORT= Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-t") || args[i].equals("--tcp-port")){
                TCPPORT = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-u") || args[i].equals("--udp-port")){
                UDPPORT = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-m") || args[i].equals("--measure-type")){
                 measureType = args[++i];
                continue;
            }
            if (args[i].equals("-d") || args[i].equals("--direction")){
                direction = args[++i];
                continue;
            }
            if (args[i].equals("-k") || args[i].equals("--keyword")){
                 keyword= args[++i];
                continue;
            }

            if (args[i].equals("-stb") || args[i].equals("--packet-size-tcp-bandwidth")){
                pktSizeTCPBandwidth = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-suc") || args[i].equals("--packet-size-udp-capacity")){
                pktSizeUDPCapacity = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-nuc") || args[i].equals("--num-tests-udp-capacity")){
                numTestsUDPCapacity = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-ntb") || args[i].equals("--num-packet-tcp-bandwidth")){
                numPktTCPBandwidth = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-stl") || args[i].equals("--packet-size-tcp-latency")){
                pktSizeTCPLatency = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-sul") || args[i].equals("--packet-size-udp-latency")){
                pktSizeUDPLatency = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-ntl") || args[i].equals("--num-packet-tcp-latency")){
                numPktTCPLatency = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-nul") || args[i].equals("--num-packet-udp-latency")){
                numPktUDPLatency = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-t") || args[i].equals("--timer-interval")){
                timerInterval = Integer.parseInt(args[++i]);
                continue;
            }
            if (args[i].equals("-i") || args[i].equals("--interface-name")){
                interfaceName = args[++i];
                continue;
            }


            System.out.println("Unknown command " + args[i]);
        }
    }



    private static void startMeasure() {
        int ret;

        if (measureType.equalsIgnoreCase("bandwidthTCP") ||
            measureType.equalsIgnoreCase("all")){

            if (numPktTCPBandwidth < 0){
                System.out.println("Error: numPktTCPBandwidth missing");
                System.exit(1);
            }
            if (pktSizeTCPBandwidth < 0){
                System.out.println("Error: pktSizeTCPBandwidth missing");
                System.exit(1);
            }


            if (direction.equalsIgnoreCase("receiver") ||
                direction.equalsIgnoreCase("all")) {
                int remainingAttempt = MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: TCP bandwidth Receiver(" +
                                       numPktTCPBandwidth + "packets of " + pktSizeTCPBandwidth +
                                       "bytes)\t\t\t[Attempt #" + (MAXATTEMPTNUMBER - remainingAttempt) + "]");


                    ret = MainUtils.tcpBandwidthMeasure("Receiver", keyword, CMDPORT,
                                                        observerAddress, TCPPORT,
                                                        pktSizeTCPBandwidth, numPktTCPBandwidth,
                                                        interfaceName);


                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (direction.equalsIgnoreCase("sender") ||
                    direction.equalsIgnoreCase("all")) {
                int remainingAttempt= MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: TCP bandwidth Sender (" +
                                       numPktTCPBandwidth + "packets of " + pktSizeTCPBandwidth +
                                       "bytes)\t\t\t[Attempt #" + (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret = MainUtils.tcpBandwidthMeasure("Sender", keyword, CMDPORT,
                                                        observerAddress, TCPPORT, pktSizeTCPBandwidth,
                                                        numPktTCPBandwidth, interfaceName);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (measureType.equalsIgnoreCase("bandwidthUDP") ||
            measureType.equalsIgnoreCase("all")){

            if (pktSizeUDPCapacity < 0){
                System.out.println("Error: pktSizeUDPCapacity missing");
                System.exit(1);
            }
            if (numTestsUDPCapacity < 0){
                System.out.println("Error: numTestsUDPCapacity missing");
                System.exit(1);
            }


            if (direction.equalsIgnoreCase("receiver") ||
                direction.equalsIgnoreCase("all")) {
                int remainingAttempt= MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: UDP capacity Receiver (" +
                                       numTestsUDPCapacity + " tests of " + pktSizeUDPCapacity +
                                       "bytes)\t\t\t[Attempt #" +
                                       (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret = MainUtils.udpBandwidthMeasure("Receiver", keyword, CMDPORT,
                                                        observerAddress, UDPPORT,
                            pktSizeUDPCapacity, interfaceName, numTestsUDPCapacity);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (direction.equalsIgnoreCase("sender") ||
                    direction.equalsIgnoreCase("all")) {
                int remainingAttempt= MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: UDP capacity Sender (" +
                                       numTestsUDPCapacity+ " tests of " + pktSizeUDPCapacity +
                                       "bytes)\t\t\t\t[Attempt #" +
                                       (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret = MainUtils.udpBandwidthMeasure("Sender", keyword, CMDPORT,
                                                        observerAddress, UDPPORT,
                            pktSizeUDPCapacity, interfaceName, numTestsUDPCapacity);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        if (measureType.equalsIgnoreCase("latencyTCP") ||
            measureType.equalsIgnoreCase("all")){

            if (pktSizeTCPLatency < 0){
                System.out.println("Error: pktSizeTCPLatency missing");
                System.exit(1);
            }
            if (numPktTCPLatency < 0){
                System.out.println("Error: numPktTCPLatency missing");
                System.exit(1);
            }

            if (direction.equalsIgnoreCase("receiver") ||
                    direction.equalsIgnoreCase("all")) {
                int remainingAttempt= MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: TCP RTT Receiver (" + numPktTCPLatency +
                                       "packets of " + pktSizeTCPLatency +
                                       "bytes)\t\t\t\t\t[Attempt #" +
                                       (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret =  MainUtils.tcpRTTMeasure("Receiver", keyword, CMDPORT,
                                                   observerAddress, TCPPORT, pktSizeTCPLatency,
                                                   numPktTCPLatency, interfaceName);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (direction.equalsIgnoreCase("sender") ||
                    direction.equalsIgnoreCase("all")) {
                int remainingAttempt = MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: TCP RTT Sender (" + numPktTCPLatency +
                                       "packets of " + pktSizeTCPLatency +
                                       "bytes)\t\t\t\t\t[Attempt #" +
                                       (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret =  MainUtils.tcpRTTMeasure("Sender", keyword, CMDPORT,
                                                   observerAddress, TCPPORT, pktSizeTCPLatency,
                                                   numPktTCPLatency, interfaceName);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
       if (measureType.equalsIgnoreCase("latencyUDP") ||
            measureType.equalsIgnoreCase("all")){
            if (pktSizeUDPLatency < 0){
                System.out.println("Error: pktSizeUDPLatency missing");
                System.exit(1);
            }
            if (numPktUDPLatency < 0){
                System.out.println("Error: numPktUDPLatency missing");
                System.exit(1);
            }


            if (direction.equalsIgnoreCase("receiver") ||
                    direction.equalsIgnoreCase("all")) {
                int remainingAttempt = MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: UDP RTT Receiver Receiver (" +
                                       numPktUDPLatency + "packets of " + pktSizeUDPLatency +
                            "bytes)\t\t\t[Attempt #" + (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret = MainUtils.udpRTTMeasure("Receiver", keyword, CMDPORT,
                                                  observerAddress, UDPPORT, pktSizeUDPLatency,
                                                  numPktUDPLatency, interfaceName);

                    if (ret == 0)
                        break;
                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (direction.equalsIgnoreCase("sender") ||
                    direction.equalsIgnoreCase("all")) {

                int remainingAttempt= MAXATTEMPTNUMBER;

                while (true) {
                    if (remainingAttempt < 1)
                    {
                        System.out.println("WARNING: too many attempts.");
                        break;
                    }
                    remainingAttempt--;

                    System.out.println("Type of measure: UDP RTT Sender (" + numPktUDPLatency +
                            "packets of " + pktSizeUDPLatency + "bytes)\t\t\t\t\t[Attempt #" +
                            (MAXATTEMPTNUMBER - remainingAttempt) + "]");

                    ret = MainUtils.udpRTTMeasure("Sender", keyword, CMDPORT,
                                                  observerAddress, UDPPORT,  pktSizeUDPLatency,
                                                  numPktUDPLatency, interfaceName);
                    if (ret == 0)
                        break;

                    System.out.println("Error: try again in " + attemptTimer/1000 + "seconds");

                    try {
                        Thread.sleep(attemptTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}


