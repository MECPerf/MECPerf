package it.cnr.iit.mecperf.pcap;

import io.pkts.protocol.Protocol;
import org.javatuples.Quintet;
import org.javatuples.Pair;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.WatchKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Logger.info("Program started");
        BlockingQueue<String> toParse = new LinkedBlockingQueue<>();
        BlockingQueue<String> toSend = new LinkedBlockingQueue<>();
        HashMap<Quintet<Protocol, String, String, Integer, Integer>, Flow> flows = new HashMap<>();
        Set<Pair<String, Integer>> servers = Collections.synchronizedSet(new HashSet<>());
        servers.add(new Pair<>("23.32.11.57", 443));
        Parser parser = new Parser(toParse, toSend, servers);
        Sender sender = new Sender(toSend);
        Watcher watcher = new Watcher("/home/valerio/test/", toParse);
        parser.start();
        sender.start();
        watcher.start();
    }
}
