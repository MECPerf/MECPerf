package it.unipi.dii.common;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Map;

/**
 * This class is meant to be the packet sent to the aggregator in order to store measurement
 * coming from the three different entities of the system (Client, Observer, Remote Server)
 */
public class Measure implements Serializable {

    private String type;
    private int ID;
    private String sender;
    private String receiver;
    private Map<Long, Integer> bandwidth;
    private Double latency;
    private String extra;

    //personalized settings
    private int len_pack;
    private int num_pack;

    private String senderAddress;
    private String receiverAddress;

    private static final long serialVersionUID = 3919700812200232178L;



    public Measure(){
    }

    /**
     * Constructor
     * @param type TCP or UDP type of used protocol
     * //@param ID  Unique identifier of the test
     * @param sender Name of the component that send the message
     * @param receiver Name of the component that receive the message
     * @param bandwidth Map containing the timestamps and the amount of data sent in the transmission
     * @param latency Latency measured
     * @param "extra" contains Timestamp when we would show Result in App or Keyword in Insert
     */
    public Measure(String type, String sender,String receiver, Map<Long,Integer> bandwidth,
                   double latency, String extra, int len_pack, int num_pack, String senderAddress,
                   String receiverAddress){
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.bandwidth= bandwidth;
        this.latency = latency;
        this.extra = extra;
        this.len_pack = len_pack;
        this.num_pack = num_pack;
        this.senderAddress = senderAddress;
        this.receiverAddress = receiverAddress;
    }

    /**
     * Copy contructor
     * @param m Object that contain the measure
     */
    public Measure(Measure m){
        type = m.type;
        sender = m.sender;
        receiver = m.receiver;
        bandwidth= m.bandwidth;
        latency = m.latency;
        extra = m.extra;

        num_pack = m.num_pack;
        len_pack = m.len_pack;

        senderAddress = m.senderAddress;
        receiverAddress = m.receiverAddress;
    }

    public int getLen_pack() {
        return len_pack;
    }

    public void setLen_pack(int len_pack) {
        this.len_pack = len_pack;
    }

    public int getNum_pack() {
        return num_pack;
    }

    public void setNum_pack(int num_pack) {
        this.num_pack = num_pack;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Map<Long, Integer> getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Map<Long, Integer> bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Double getLatency() {
        return latency;
    }

    public void setLatency(Double latency) {
        this.latency = latency;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }
}

