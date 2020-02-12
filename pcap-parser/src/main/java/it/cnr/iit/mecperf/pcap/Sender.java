package it.cnr.iit.mecperf.pcap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class Sender extends Thread {
    private BlockingQueue<String> toSend;
    private String aggregatorUrl = "http://localhost/";

    public Sender(BlockingQueue<String> toSend) {
        super();
        this.toSend = toSend;
    }

    public void run() {
        Logger.info("Sender started");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        while (true) {
            try {
                Logger.info("Reading string to send from queue");
                String reqBody = toSend.take();
                Logger.info(reqBody);
                if (reqBody.equals("END"))
                    break;
                // TODO complete with REST API
//                HttpPost httpPost = new HttpPost(aggregatorUrl);
//                StringEntity requestEntity = new StringEntity(reqBody, ContentType.APPLICATION_JSON);
//                httpPost.setEntity(requestEntity);
//                CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }
    }
}
