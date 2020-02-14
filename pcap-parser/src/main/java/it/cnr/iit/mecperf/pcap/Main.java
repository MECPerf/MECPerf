package it.cnr.iit.mecperf.pcap;

import org.apache.commons.cli.*;
import org.javatuples.Pair;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static Set<Pair<String, Integer>> parseServers(String[] serversStrings) {
        Set<Pair<String, Integer>> servers = Collections.synchronizedSet(new HashSet<>());
        for (String s : serversStrings) {
            String[] ipPort = s.split(":");
            if (ipPort.length != 2)
                continue;
            String ip = ipPort[0];
            int port;
            try {
                port = Integer.parseInt(ipPort[1]);
            } catch (NumberFormatException e) {
                Logger.error(e);
                continue;
            }
            servers.add(new Pair<String, Integer>(ip, port));
        }
        return servers;
    }

    public static void main(String[] args) throws IOException {
        Logger.info("Program started");
        Options options = new Options();
        Option watchDir = new Option("d", "directory", true, "Directory to be watched for pcap files");
        watchDir.isRequired();
        options.addOption(watchDir);
        Option server = new Option("s", "server", true, "Server to be monitored. Syntax is <ip>:<port>");
        server.isRequired();
        options.addOption(server);
        Option aggregator = new Option("a", "aggregator", true, "Aggregator address. Syntax is <ip>");
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
        Set<Pair<String, Integer>> servers = parseServers(serverValues);
        Parser parser = new Parser(toParse, toSend, servers);
        Sender sender = new Sender(toSend, aggregatorAddress);
        Watcher watcher = new Watcher(watchDirValue, toParse);
        parser.start();
        sender.start();
        watcher.start();
    }
}
