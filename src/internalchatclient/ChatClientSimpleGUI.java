package internalchatclient;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;

/**
 * Created by 44067301 on 8/31/2017.
 */
public class ChatClientSimpleGUI extends Applet implements ActionListener{

    private ChatClient client = null;
    private String serverName = "130.15.23.151";
    private int serverPort = 12345;

    //GUI Elements
    private JTextArea display = new JTextArea();
    private JTextField input = new JTextField();
    private final JButton
            send = new JButton("Send"),
            connect = new JButton("Connect"),
            quit = new JButton("Quit");

    @Override
    public void init(){
        JPanel keys = new JPanel();
        keys.setLayout(new GridLayout(1,2));
        keys.add(quit);
        keys.add(connect);

        JPanel south = new JPanel();
        south.setLayout(new BorderLayout());
        south.add("West", keys);
        south.add("Center", input);
        south.add("East", send);

        JLabel title = new JLabel("InternalChat", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        setLayout(new BorderLayout());

        add("North", title);
        add("Center", display);
        add("South", south);

        quit.setEnabled(false);
        send.setEnabled(false);
        connect.setEnabled(true);

        quit.setActionCommand("quit");
        send.setActionCommand("send");
        connect.setActionCommand("connect");

        quit.addActionListener(e -> quit());

        send.addActionListener(e -> send());

        connect.addActionListener(e -> connect());

        input.addActionListener(e ->{
            if(client.isConnected()){
                send();
            }
        });

    }


    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource().equals(connect)){
            connect();
        }

        /*
        switch(e.getActionCommand()){
            case quit:
                quit();
                return;
            case "send":
                send();
                return;
            case "connect":
                connect();
                return;
            default:
                System.out.println("WHAT");
        }
        */
    }

    private void quit(){
        input.setText("/quit");
        send();
        client = null;
        quit.setEnabled(false);
        send.setEnabled(false);
        connect.setEnabled(true);
    }

    private void send(){
        client.parseCommand(input.getText());
        input.setText("");
        input.requestFocus();
    }

    public void handle(String msg){
        client.handle(msg);
    }

    private void connect(){

        PrintStream ps = new PrintStream(new CustomOutputStream(display));
        System.setOut(ps);
        //System.setErr(ps);

        client = new ChatClient(serverName, serverPort, ps);

        this.serverName = client.getServerName();
        this.serverPort = client.getServerPort();

        if(client.isConnected()){
            send.setEnabled(true);
            quit.setEnabled(true);
            connect.setEnabled(false);
        }
    }

}
