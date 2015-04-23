import ChatCommands.CommandList;
import MessageObserver.ISubscribe;
import MessageObserver.Message;
import couchdb.ISendMessage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by awaigand on 09.04.2015.
 */
public class ChatWindow extends JPanel implements ISubscribe {
    private static final String COMMAND_PREFIX = "\\";
    private JTextField messageInput;
    private JScrollPane scrollPane1;
    private JTextPane messagePane;
    private JPanel rootPane;
    private JButton historyYesterday;
    private JButton history1Hour;
    private JButton historyWeek;
    private JButton historyMonth;
    private JButton historyEver;
    final private ISendMessage messageSender;
    final private CommandList commandList;
    private HistoryFrameFactory hf;

    public JPanel getMainPane() {
        return rootPane;
    }

    public ChatWindow(final ISendMessage messageSender, final HistoryFrameFactory hf, CommandList cl, String title) {
        super();
        this.commandList = cl;
        this.hf = hf;
        this.messageSender = messageSender;


        messageInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processInput(messageInput.getText());
                messageInput.setText("");
            }
        });
        historyYesterday.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HistoryFrame hist = hf.buildHistoryFrame(new DateTime(DateTimeZone.UTC).minusDays(1));
                hist.setVisible(true);
            }
        });
        historyEver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HistoryFrame hist = hf.buildHistoryFrame(new DateTime(2015, 3, 1, 0, 0, DateTimeZone.UTC));
                hist.setVisible(true);
            }
        });
        historyMonth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HistoryFrame hist = hf.buildHistoryFrame(new DateTime(DateTimeZone.UTC).minusMonths(1));
                hist.setVisible(true);
            }
        });
        history1Hour.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HistoryFrame hist = hf.buildHistoryFrame(new DateTime(DateTimeZone.UTC).minusHours(1));
                hist.setVisible(true);
            }
        });
        historyWeek.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HistoryFrame hist = hf.buildHistoryFrame(new DateTime(DateTimeZone.UTC).minusDays(7));
                hist.setVisible(true);
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
