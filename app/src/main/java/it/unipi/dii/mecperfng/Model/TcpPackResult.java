package it.unipi.dii.mecperfng.Model;

import java.io.Serializable;
import java.util.List;

public class TcpPackResult implements Serializable {

    List<String> listMeasurements, listBandwidth, listBandwidthForCdf;
    String avg;
    String direction;
    String protocol;

    public TcpPackResult(List<String> listMeasurements, List<String> listBandwidth, List<String> listBandwidthForCdf,
                         String avg, String direction, String protocol){
        super();
        this.listMeasurements = listMeasurements;
        this.listBandwidth = listBandwidth;
        this.listBandwidthForCdf = listBandwidthForCdf;
        this.avg = avg;
        this.direction = direction;
        this.protocol = protocol;
    }

    public List<String> getListMeasurements() {
        return listMeasurements;
    }

    public List<String> getListBandwidth() {
        return listBandwidth;
    }

    public List<String> getListBandwidthForCdf() {
        return listBandwidthForCdf;
    }

    public String getAvg() {
        return avg;
    }

    public String getDirection() {
        return direction;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setListMeasurements(List<String> listMeasurements) {
        this.listMeasurements = listMeasurements;
    }

    public void setListBandwidth(List<String> listBandwidth) {
        this.listBandwidth = listBandwidth;
    }

    public void setListBandwidthForCdf(List<String> listBandwidthForCdf) {
        this.listBandwidthForCdf = listBandwidthForCdf;
    }

    public void setAvg(String avg) {
        this.avg = avg;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
