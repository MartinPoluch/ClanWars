package Client.Communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Trieda zabuzdruje socket určení pre komunikáciu so serverom.
 */
public class Server {

    private Socket clientSocket;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;
    private boolean stopSending;
    private String error;

    public Server(String ipAdress, int port) throws IOException{
        try {
            this.clientSocket = new Socket(ipAdress, port);
            this.outStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.inStream = new ObjectInputStream(clientSocket.getInputStream());
            this.stopSending = false;
        } catch (Exception e) {
            error = e.getMessage();
        }
    }

    public String getError() {
        return error;
    }

    public void setStopSending(boolean stopSending) {
        this.stopSending = stopSending;
    }

    public boolean canSending() {
        return ! stopSending;
    }

    public void send(Object object) {
        try {
            if (! stopSending) {
                outStream.writeObject(object);
                outStream.reset();
            }
            else {
                System.out.println("message " + object + " was stopped");
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public Object receive() throws SocketException, IOException, ClassNotFoundException{
        return inStream.readObject();
    }

    public boolean connected() {
        return clientSocket.isConnected();
    }

    public void endCommunication() {
        try{
            outStream.writeObject("end");
            stopSending = true;
            clientSocket.close();
            inStream.close();
            outStream.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }


}
