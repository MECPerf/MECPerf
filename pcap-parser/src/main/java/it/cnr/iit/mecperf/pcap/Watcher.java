package it.cnr.iit.mecperf.pcap;

import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;

public class Watcher extends Thread {
    private WatchService watcher;
    private WatchKey key;
    private Path dir;
    private String next;
    private BlockingQueue<String> toParse;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    public Watcher(String path, BlockingQueue<String> toParse) throws IOException {
        this.dir = Paths.get(path);
        this.watcher = FileSystems.getDefault().newWatchService();
        this.key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        this.next = "";
        this.toParse = toParse;
    }

    public void run() {
        Logger.info("Watcher started");
        while (true) {
            WatchKey nextKey;
            try {
                nextKey = watcher.take();
            } catch (InterruptedException e) {
                Logger.error(e);
                continue;
            }

            for (WatchEvent<?> event: nextKey.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                // The filename is the
                // context of the event.
                WatchEvent<Path> ev = cast(event);
                Path filename = ev.context();
                Path child = dir.resolve(filename);
                Logger.info("Filename is {}", child);
                if (!next.equals("")) {
                    try {
                        Logger.debug("Inserting {} in toParse queue", next);
                        toParse.put(next);
                    } catch (InterruptedException e) {
                        Logger.error(e);
                    }
                }
                next = child.toString();

                boolean valid = nextKey.reset();
                if (!valid) {
                    break;
                }
            }
        }
    }
}
