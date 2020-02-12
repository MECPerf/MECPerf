package it.cnr.iit.mecperf.pcap;

import org.tinylog.Logger;

import java.util.concurrent.BlockingQueue;

public class Sender extends Thread {
    private BlockingQueue<String> toSend;

    public Sender(BlockingQueue<String> toSend) {
        super();
        this.toSend = toSend;
    }

    public void run() {
        while (true) {
            try {
                Logger.info("Reading string to send from queue");
                String reqBody = toSend.take();
                Logger.info(reqBody);
                // TODO complete with REST API
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }
    }
}
