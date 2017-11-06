package internalchatclient;

/**
 * Created by David Lee on 2017-08-24.
 */

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.net.*;
import java.io.*;
import java.security.PublicKey;

public class ChatClient implements Runnable {
    private Socket socket = null;
    private volatile Thread thread = null;
    private BufferedReader console = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread client = null;
    private PrintStream ps = null;
    private String serverName = null;
    private int serverPort = -1;
    private boolean debug = false;

    static Image image = Toolkit.getDefaultToolkit().getImage("images/tray.gif");
    static TrayIcon trayIcon = new TrayIcon(image, "Tester2");

    private String name = "Default";
    private boolean notifications = true;
    private EncryptionManager encryptionManager = null;

    public ChatClient(String serverName, int serverPort){
        this(serverName, serverPort, System.out);
    }
    public ChatClient(String serverName, int serverPort, @NotNull PrintStream ps) {

        //Copy Elements
        this.serverName = serverName;
        this.serverPort = serverPort;

        //Setting up PrintStream
        if(ps == null){
            System.out.println("ERROR: PrintStream is null, reverting to System.out");
            ps = System.out;
        } else this.ps = ps;

        //Init EncryptionManager
        encryptionManager = EncryptionManager.getInstance();

        //Setting up SystemTray
        ps.println("SystemTray is" + (SystemTray.isSupported()?"":" NOT") + " supported!");
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
            ps.println("This chat is encrypted using RSA-2048.");
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

        // Distribute Public Key
        try{
            streamOut.writeUTF(encryptionManager.getDistributeKeyProtocol());
            streamOut.flush();
        } catch(IOException ioe){
            ps.println("Sending error: " + ioe.getMessage());
            stop();
        }

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

    public boolean isConnected(){
        return this.thread != null;
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
    //TODO: parseCommand
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
                        this.input = "msg [" + this.name + " changed their name to " + arr[1] + "]";
                        this.name = arr[1];
                        ps.println(this.input.substring(4));
                        sendEncryptedMessage(this.input);
                        return true;
                    } else {
                        ps.println("[Wrong Syntax: /name [name]]");
                        return false;
                    }
                case "/notif":
                    if(arr.length >= 2){
                        if("on".equals(arr[1])) notifications = true;
                        else if("off".equals(arr[1])) notifications = false;
                        else {
                            ps.println("[Wrong Syntax: /notif {'on':'off'}]");
                            return false;
                        }
                    } else {
                        notifications = !notifications;
                    }
                    ps.println("[Notifiations are now " + (notifications?"on]":"off]"));
                    return false;
                default:
                    ps.println("[Unknown Command or Wrong Syntax: " + input + "]");
                    return false;
            }
        } else {
            this.input = "msg " + name + ": " + input;
            ps.println(this.input.substring(4));
            sendEncryptedMessage(this.input);
            return false;
        }
    }

    public void sendEncryptedMessage(String str){
        for(PublicKey key : encryptionManager.getKeyChain()){
            try {
                streamOut.writeUTF(encryptionManager.encrypt(str, key));
                streamOut.flush();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    //TODO: parseProtocol
    public void parseProtocol(String str){
        if(str.length() <= 5)
            if(debug)
                ps.println("Protocol Received is Invalid: " + str);
        String protocol = str.substring(0, 3);
        switch(protocol) {
            case "dst":
                String key = str.substring(4);
                if (!key.equals(encryptionManager.getEncodedPublicKey())) {
                    if(encryptionManager.addToChain(key))
                        ps.println("[Added Public Key to keychain: " + key + "]");

                    //Send Key
                    try {
                        streamOut.writeUTF(encryptionManager.getAddKeyProtocol());
                        streamOut.flush();
                    } catch (IOException ioe) {
                        ps.println("Sending Error: " + ioe.getMessage());
                    }

                } else {
                    ps.println("This is your Public Key: " + str.substring(4));
                }
                return;
            case "add":
                String key2 = str.substring(4);
                ps.println("[Received Public Key: " + key2 + "]");
                if(encryptionManager.addToChain(key2)){
                    ps.println("[Added key to keychain]");
                } else {
                    ps.println("[Key already exists, so it was not added]");
                }
                return;
            default:
        }

        //decrypt and attempt to resolve encrypted message
        String decrypted = encryptionManager.decrypt(str);
        if(decrypted == null) {
            //ps.println("[Decryption Failed!]");
        }
        else {
            switch(decrypted.substring(0, 3)) {
                case "msg":
                    ps.println(decrypted.substring(4));
                    showNotification("InternalChat", decrypted.substring(4), null);
                default:
                    if(debug) ps.println("Unknown Protocol: " + str);
                    return;
            }
        }
    }

    /**
     * Handle incoming messages from the server.
     * @param msg
     */
    public void handle(String msg) {
//        if(debug)
//            ps.println("raw input: " + msg);
        parseProtocol(msg);
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

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
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