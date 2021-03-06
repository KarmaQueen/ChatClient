package internalchatserver;

/**
 * Created by David Lee on 2017-08-24.
 */

import java.net.*;
import java.io.*;
import java.security.PublicKey;

public class ChatServerThread extends Thread {

    private ChatServer server = null;
    private Socket socket = null;
    private int ID = -1;
    private DataInputStream streamIn = null;
    private DataOutputStream streamOut = null;
    private volatile boolean done = false;
    private String name = null;
    private PublicKey publicKey = null;

    public ChatServerThread(ChatServer _server, Socket _socket) {
        super();
        server = _server;
        socket = _socket;
        ID = socket.getPort();
        name = "" + ID;
    }

    public void send(String msg) {
        try {
            streamOut.writeUTF(msg);
            streamOut.flush();
        } catch (IOException ioe) {
            System.out.println(ID + " ERROR sending: " + ioe.getMessage());
            server.remove(ID);
            //stop();
            done = true;
        }
    }

    public void sendBytes(byte[] msg){
        try{
            streamOut.write(msg);
            streamOut.flush();
        } catch(Exception e){
            System.out.println(ID + " ERROR sending: " + e.getMessage());
            server.remove(ID);
            done = true;
        }
    }

    public void setPublicKey(PublicKey key){
        publicKey = key;
    }

    public PublicKey getPublicKey(){
        return publicKey;
    }

    public String getChatterName(){
        return name;
    }

    public String setChatterName(String s){
        try {
            return name;
        } finally {
            name = s;
        }
    }

    public int getID() {
        return ID;
    }

    @Override
    public synchronized void run() {
        System.out.println("Server Thread " + ID + " running.");

        while (!done) {
            try {
                server.handle(ID, streamIn.readUTF());
            } catch (IOException ioe) {
                System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                //stop();
                done = true;
            }
        }
    }

    public void open() throws IOException {
        streamIn = new DataInputStream(new
                BufferedInputStream(socket.getInputStream()));
        streamOut = new DataOutputStream(new
                BufferedOutputStream(socket.getOutputStream()));
    }

    public void close() throws IOException {
        shutdown();
        if (socket != null) socket.close();
        if (streamIn != null) streamIn.close();
        if (streamOut != null) streamOut.close();
    }

    public void shutdown(){
        done = true;
    }
}