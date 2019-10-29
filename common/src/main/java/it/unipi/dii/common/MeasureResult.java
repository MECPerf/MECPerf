package it.unipi.dii.common;

import java.io.Serializable;

public class MeasureResult implements Serializable {
    private String sender;
    private String receiver;
    private String Command;
    private Double latency;
    private Double bandwidth;
    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Double bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getCommand() {
        return Command;
    }

    public Double getLatency() {
        return latency;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setCommand(String command) {
        Command = command;
    }

    public void setLatency(Double latency) {
        this.latency = latency;
    }

    @Override
    public String toString() {
        return "MeasureResult{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", Command='" + Command + '\'' +
                ", latency=" + latency +
                ", bandwidth=" + bandwidth +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}

