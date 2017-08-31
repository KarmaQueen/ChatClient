package internalchatclient;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by 44067301 on 8/31/2017.
 */
public class CustomOutputStream extends OutputStream {

    private JTextArea textArea;

    public CustomOutputStream(JTextArea textArea){
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            textArea.getDocument().insertString(
                    textArea.getDocument().getLength(),
                    String.valueOf((char)b),
                    null
            );
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
