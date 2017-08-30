package internalchatclient;

/**
 * Created by 44067301 on 8/24/2017.
 */

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;

public class ChatClient implements Runnable {
    private Socket socket = null;
    private volatile Thread thread = null;
    private BufferedReader console = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread client = null;
    private PrintStream ps = null;

    static Image image = Toolkit.getDefaultToolkit().getImage("images/tray.gif");

    static TrayIcon trayIcon = new TrayIcon(image, "Tester2");

    private String name = "Default";
    private boolean notifications = true;

    public ChatClient(String serverName, int serverPort){
        this(serverName, serverPort, System.out);
    }

    public ChatClient(String serverName, int serverPort, @NotNull PrintStream ps) {

        //Setting up PrintStream
        if(ps == null){
            System.out.println("ERROR: PrintStream is null, reverting to System.out");
            ps = System.out;
        } else this.ps = ps;

        //Setting up SystemTray
        ps.println("SystemTray is" + (SystemTray.isSupported()?"":" not") + " supported!");
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            final PrintStream pstream = ps;
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> pstream.println("SystemTray clicked"));

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }
        }

        //Setting up connection to server
        ps.println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            ps.println("Connected: " + socket);
            ps.println("Welcome!");
            start();
        } catch (UnknownHostException uhe) {
            ps.println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            ps.println("Unexpected exception: " + ioe.getMessage());
        }
    }

    String input;

    public void run() {
        Thread thread = Thread.currentThread();
        while (this.thread == thread) {
            try {
                input = console.readLine();

                if(parseCommand(input)) {
                    streamOut.writeUTF(input);
                    streamOut.flush();
                }
            } catch (IOException ioe) {
                ps.println("Sending error: " + ioe.getMessage());
                stop();
            }
        }
    }

    /**
     * Parses the command and executes it.
     * [] = mandatory
     * {} = optional
     * Currently Supported commands:
     * /name [name] - change your display name
     * /notif {'on':'off'} - set notifications on or off
     * @param input
     * @return boolean whether to send the text to the server
     */
    public boolean parseCommand(String input){
        if(input == null || input.length() < 1) return false;
        if(input.charAt(0) == '/') {
            String[] arr = input.split(" ");
            switch(arr[0]){
                case "/quit":
                case "/exit":
                    ps.println("[Good bye. Press RETURN to exit...]");
                    stop();
                    return false;
                case "/name":
                    if(arr.length >= 2) {
                        this.name = arr[1];
                        ps.println("[ Name changed to " + this.name + " ]");
                    } else {
                        ps.println("[ Wrong Syntax: /name [name] ]");
                    }
                    return false;
                case "/notif":
                    if(arr.length >= 2){
                        if(arr[1] == "on") notifications = true;
                        else if(arr[1] == "off") notifications = false;
                        else
                            ps.println("[ Wrong Syntax: /notif {'on':'off'} ]");
                    } else {
                        notifications = !notifications;
                    }
                    ps.println("[ Notifiations are now " + (notifications?"on ]":"off ]"));
                    return false;
                default:
                    ps.println("[ Unknown Command or Wrong Syntax: " + input + " ]");
                    return false;
            }
        } else {
            this.input = "MSG:" + name + ": " + input;
            return true;
        }
    }

    /**
     * Handle incoming messages from the server.
     * @param msg
     */
    public void handle(String msg) {
        ps.println(msg);
        showNotification("InternalChat", msg, null);
    }

    public void showNotification(String title, String message, @Nullable String location){
        if(SystemTray.isSupported() && notifications){
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
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
            ps.println("Error closing ...");
        }
        client.close();
        client.shutdown();
    }

    public static void main(String args[]) {
        /*
        ChatClient client = null;
        if (args.length != 2)
            ps.println("Usage: java ChatClient host port");
        else
            client = new ChatClient(args[0], Integer.parseInt(args[1]));
        */
        ChatClient client = new ChatClient("130.15.23.151",12345);

    }
}