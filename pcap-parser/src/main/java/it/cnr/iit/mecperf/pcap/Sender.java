package it.cnr.iit.mecperf.pcap;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.javatuples.Pair;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class Sender extends Thread {
    private BlockingQueue<Pair<Integer, MeasurementResult>> toSend;
    private String aggregatorUrl;
    private static final String BW = "/post_passive_measures/insert_bandwidth_measure";
    private static final String RTT = "/post_passive_measures/insert_latency_measure";

    public Sender(BlockingQueue<Pair<Integer, MeasurementResult>> toSend, String aggregatorUrl) {
        super();
        this.toSend = toSend;
        this.aggregatorUrl = aggregatorUrl;
    }

    public void run() {
        Logger.info("Sender started");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        while (true) {
            try {
                Logger.info("Reading string to send from queue");
                Pair<Integer, MeasurementResult> toSendMeasurement = toSend.take();
                String url = aggregatorUrl;
                if (toSendMeasurement.getValue0() == Utils.SEND_TYPE_BW) {
                    url += BW;
                    Logger.info("Received measurement type bw: {}", toSendMeasurement.getValue1());
                } else if (toSendMeasurement.getValue0() == Utils.SEND_TYPE_RTT) {
                    url += RTT;
                    Logger.info("Received measurement type rtt: {}", toSendMeasurement.getValue1());
                } else if (toSendMeasurement.getValue0() == Utils.SEND_TYPE_END) {
                    Logger.info("Received end command");
                    break;
                } else {
                    Logger.info("Unknown command, ignoring");
                    continue;
                }
                Gson gson = new Gson();
                String reqBody = gson.toJson(toSendMeasurement.getValue1());
                Logger.info("Sending to {} body {}", url, reqBody);
                HttpPost httpPost = new HttpPost(url);
                StringEntity requestEntity = new StringEntity(reqBody, ContentType.APPLICATION_JSON);
                httpPost.setEntity(requestEntity);
                CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    Logger.info("Measurement sent with success");
                } else {
                    Logger.info("Error in sending measurements: {}", httpResponse.getStatusLine().getStatusCode());
                }
            } catch (InterruptedException | IOException e) {
                Logger.error(e);
            }
        }
    }
}
