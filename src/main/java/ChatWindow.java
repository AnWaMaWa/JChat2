import ChatCommands.CommandList;
import MessageObserver.ISubscribe;
import MessageObserver.Message;
import couchdb.ISendMessage;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by awaigand on 09.04.2015.
 */
public class ChatWindow extends JPanel implements ISubscribe {
    private static final String COMMAND_PREFIX = "\\";
    private JTextField messageInput;
    private JScrollPane scrollPane1;
    private JTextPane messagePane;
    private JPanel rootPane;
    private JButton loadHistory;
    final private ISendMessage messageSender;
    final private CommandList commandList;

    public JPanel getMainPane() {
        return rootPane;
    }

    public ChatWindow(final ISendMessage messageSender, CommandList cl, String title) {
        super();
        this.commandList = cl;
        this.messageSender = messageSender;


        messageInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processInput(messageInput.getText());
                messageInput.setText("");
            }
        });
        loadHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    private Thread messageSenderThreadFactory(final String message){
        return new Thread(){
            public void run(){
                messageSender.sendMessage(message);
            }
        };
    }

    private void processInput(String input){
        if(input.startsWith(COMMAND_PREFIX)){
            append(commandList.runCommand(input));
        }else{

            messageSenderThreadFactory(input).start();
        }
    }

    private void prepend(String s) {
        try {
            Document doc = messagePane.getDocument();
            doc.insertString(0, s + "\n", null);
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
    }

    private void append(String s) {
        try {
            Document doc = messagePane.getDocument();
            doc.insertString(doc.getLength(), s + "\n", null);
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public void notify(Message m) {
        append(m.getUser() + ": " + m.getBody());
    }

    @Override
    public void notify(String m) {
        append(m);
    }

}
