package internalchatclient;

/**
 * Created by 44067301 on 8/24/2017.
 */

import java.net.*;
import java.io.*;

public class ChatClient implements Runnable {
    private Socket socket = null;
    private volatile Thread thread = null;
    private BufferedReader console = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread client = null;

    public ChatClient(String serverName, int serverPort) {
        System.out.println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            start();
        } catch (UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected exception: " + ioe.getMessage());
        }
    }

    public void run() {
        Thread thread = Thread.currentThread();
        while (this.thread == thread) {
            try {
                streamOut.writeUTF(console.readLine());
                streamOut.flush();
            } catch (IOException ioe) {
                System.out.println("Sending error: " + ioe.getMessage());
                stop();
            }
        }
    }

    public void handle(String msg) {
        if (msg.equals("/bye")) {
            System.out.println("Good bye. Press RETURN to exit ...");
            stop();
        } else
            System.out.println(msg);
    }

    public void start() throws IOException {
        console = new BufferedReader(new InputStreamReader(System.in));
        streamOut = new DataOutputStream(socket.getOutputStream());
        if (thread == null) {
            client = new ChatClientThread(this, socket);
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        thread = null;

        try {
            if (console != null) console.close();
            if (streamOut != null) streamOut.close();
            if (socket != null) socket.close();
        } catch (IOException ioe) {
            System.out.println("Error closing ...");
        }
        client.close();
        client.shutdown();
    }

    public static void main(String args[]) {
        /*
        ChatClient client = null;
        if (args.length != 2)
            System.out.println("Usage: java ChatClient host port");
        else
            client = new ChatClient(args[0], Integer.parseInt(args[1]));
        */

        ChatClient client = new ChatClient("130.15.23.151",12345);

    }
}