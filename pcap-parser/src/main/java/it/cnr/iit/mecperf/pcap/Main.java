package it.cnr.iit.mecperf.pcap;

import org.apache.commons.cli.*;
import org.javatuples.Pair;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static Map<Pair<String, Integer>, String> parseServers(String[] serversStrings) {
        Map<Pair<String, Integer>, String> servers = Collections.synchronizedMap(new HashMap<>());
        for (String s : serversStrings) {
            String[] ipPortService = s.split(":");
            if (ipPortService.length != 3)
                continue;
            String ip = ipPortService[0];
            int port;
            try {
                port = Integer.parseInt(ipPortService[1]);
            } catch (NumberFormatException e) {
                Logger.error(e);
                continue;
            }
            String service = ipPortService[2];
            servers.put(new Pair<>(ip, port), service);
        }
        return servers;
    }

    public static void main(String[] args) throws IOException {
        Logger.info("Program started");
        Options options = new Options();
        Option watchDir = new Option("d", "directory", true, "Directory to be watched for pcap files");
        watchDir.isRequired();
        options.addOption(watchDir);
        Option server = new Option("s", "server", true, "Server to be monitored. Syntax is <ip>:<port>:<service_name>");
        server.isRequired();
        options.addOption(server);
        Option aggregator = new Option("a", "aggregator", true, "Aggregator address. Syntax is http://<ip>:<port>");
        aggregator.isRequired();
        options.addOption(aggregator);

        CommandLineParser clParser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = clParser.parse(options, args);
        } catch (ParseException e) {
            Logger.error(e);
            formatter.printHelp("MECPerf Pcap Parser", options);
            System.exit(1);
        }

        String watchDirValue = cmd.getOptionValue("directory");
        String[] serverValues = cmd.getOptionValues("server");
        String aggregatorAddress = cmd.getOptionValue("aggregator");

        BlockingQueue<String> toParse = new LinkedBlockingQueue<>();
        BlockingQueue<Pair<Integer, MeasurementResult>> toSend = new LinkedBlockingQueue<>();
        Map<Pair<String, Integer>, String> servers = parseServers(serverValues);
        Parser parser = new Parser(toParse, toSend, servers);
        Sender sender = new Sender(toSend, aggregatorAddress);
        Watcher watcher = new Watcher(watchDirValue, toParse);
        parser.start();
        sender.start();
        watcher.start();
    }
}
