package Server;

import GamePlay.GameChange;
import GamePlay.Players.Human;
import GamePlay.Players.Player;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Zapuzdrenie komunikácie s klientom. Každý klient je na strane servera reprezentovaný inštanciou tejto triedy.
 * Táto trieda obsahuje ako atribút inštanciu triedy Player ktorá obsahuje ďalšie údaje o danom hráčovi.
 */
public class Connection {

    private Socket clientSocket;
    private ObjectOutputStream outStream; // posielanie objektov
    private ObjectInputStream inStream; // posielanie objektov
    private Player client;
    private boolean connected;

    public Connection(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.connected = true;
        this.client = null;
        try {
            this.outStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.inStream = new ObjectInputStream(clientSocket.getInputStream());
        }
        catch (IOException io) {
            System.out.println();
        }
    }

    public void initConnection(long clientID) throws IOException, ClassNotFoundException{
        client = (Human) receiveMessage(); // cakam kym uzivatel posle referenciu na seba
        client.setId(clientID); // nastavim uzivatelovi unikatne id, kde id sa rovna id vlakna
        sendMessage(client); // naslem spat hraca s nastavenym id
    }

    public Player getPlayer() {
        return client;
    }

    public boolean isConnected() {
        if (connected) {
            try {
                outStream.writeObject(GameChange.CHECK);
                outStream.reset();
                GameChange answer =  (GameChange) inStream.readObject();
                connected = (answer == GameChange.CHECK);
            } catch (Exception e) {
                connected = false;
            }
        }
        return connected;
    }

    public void sendMessage(Object message) throws IOException{
        outStream.reset();
        outStream.writeObject(message);
    }

    public Object receiveMessage() throws IOException, ClassNotFoundException{
        return inStream.readObject();
    }

    public void closeConnection() {
        try {
            clientSocket.close();
            outStream.close();
            inStream.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
