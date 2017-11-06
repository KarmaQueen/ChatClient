package internalchatclient;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by David Lee on 2017-08-31.
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
