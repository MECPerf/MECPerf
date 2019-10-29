package it.unipi.dii.common;




import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;




public class ControlMessages {
    InetAddress receiverAddress = null;
    int receiverPort;
    Socket controlSocket = null;


    private void setReceiverAddress(String receiverAddress){
        try {
            this.receiverAddress = InetAddress.getByName(receiverAddress);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    private void setReceiverPort(int receiverPort){
        this.receiverPort = receiverPort;

    }


    private void openControlConnection(){
        try {
            this.controlSocket = new Socket(this.receiverAddress, this.receiverPort);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }




    public ControlMessages(String receiverAddress, int receiverPort){
        setReceiverPort(receiverPort);
        setReceiverAddress(receiverAddress);

        openControlConnection();
    }

    public ControlMessages(){
        this.controlSocket = null;
        this.receiverAddress = null;
        this.receiverPort = -1;
    }

    public void closeConnection(){
        try {
            this.controlSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.controlSocket = null;
        this.receiverAddress = null;
        this.receiverPort = -1;
    }


    public void sendCMD(String command) throws IOException {
        // get the output stream from the socket.
        System.out.println("CMD: " + command);
        OutputStream outputStream = this.controlSocket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        // write the message we want to send
        dataOutputStream.writeUTF(command);
        dataOutputStream.flush(); // send the message
        dataOutputStream.close(); // close the output stream when we're done.
    }


    public void startNewMeasure(String receiverAddress, int cmdPort) {
        setReceiverAddress(receiverAddress);
        setReceiverPort(cmdPort);
        openControlConnection();

    }

/*
    public void sendObserverCMD(String command){
        openControlConnection();

        try {
            sendCMD(command);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

 */

}
