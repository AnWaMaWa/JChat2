import ChatCommands.CommandList;
import MessageObserver.ISubscribe;
import MessageObserver.Message;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import config.ConfigHandler;
import couchdb.ISendMessage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
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

        //When user press ENTER or RETURN in input field
        messageInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processInput(messageInput.getText());
                messageInput.setText("");
            }
        });

        //When user clicks on history since yesterday button.
        historyYesterday.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHistorySinceDate(ConfigHandler.getCurrentUTCTime().minusDays(1));
            }
        });

        //When user clicks on "All history" button
        historyEver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Ever means since the Unix Epoch began in this case.
                //This date is used for possible backwards compatibility with other chat systems
                //So that, potentially, someone could write a MSN Messenger History converter and messages which
                //were sind by MSN Messenger before the creation of this chat even began would still be displayed
                showHistorySinceDate(new DateTime(1970, 1, 1, 0, 0, DateTimeZone.UTC));
            }
        });

        //When user clicks on "Month History" Button
        historyMonth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHistorySinceDate(ConfigHandler.getCurrentUTCTime().minusMonths(1));
            }
        });

        //When user clicks on "Hour History" Button
        history1Hour.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHistorySinceDate(ConfigHandler.getCurrentUTCTime().minusHours(1));
            }
        });

        //When user clicks on "Week History" Button
        historyWeek.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHistorySinceDate(ConfigHandler.getCurrentUTCTime().minusDays(7));
            }
        });
    }

    /**
     * Creates and shows a history window for all messages since datetime time
     * @param datetime Show all messages since this time.
     */
    private void showHistorySinceDate(DateTime datetime) {
        HistoryFrame hist = hf.buildHistoryFrame(datetime);
        hist.setVisible(true);
    }

    /**
     * This creates a thread which sends the message.
     * It is a thread so it gets blocked when the messageSender is currently busy, but still does not block
     * any other UI Interaction (i.e. it does not block the user to type another message).
     * @param message
     * @return
     */
    private Thread messageSenderThreadFactory(final String message) {
        return new Thread() {
            public void run() {
                messageSender.sendMessage(message);
            }
        };
    }

    /**
     * Checks if the input starts with the command prefix "\" by default.
     * If so it is handed to the commandList.runCommand to check if it is a actual command.
     * Whatever message the command returns is shown to the user via append.
     * If the input is not a command, it is considered a message, and is therefore given to the messageSenderThreadFactory
     * @param input Raw User Input
     */

    private void processInput(String input) {
        if (input.startsWith(COMMAND_PREFIX)) {
            append(commandList.runCommand(input));
        } else {

            messageSenderThreadFactory(input).start();
        }
    }

    /**
     * Prepends the String to the messagePane, i.e. showing it before all other messages currently seen on screen.
     * @param s
     */
    private void prepend(String s) {
        try {
            Document doc = messagePane.getDocument();
            doc.insertString(0, s + "\n", null);
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Appends the string to the messagePane, i.e. showing it after all other messages currently seen on screen.
     * @param s
     */
    private void append(String s) {
        try {
            Document doc = messagePane.getDocument();
            doc.insertString(doc.getLength(), s + "\n", null);
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Shows the message on screen. Used by the MessageList to show new messages.
     * @param m
     */
    @Override
    public void notify(Message m) {
        append(m.getUser() + ": " + m.getBody());
    }

    /**
     * Shows string on screen.
     * @param m
     */
    @Override
    public void notify(String m) {
        append(m);
    }

    {

// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPane = new JPanel();
        rootPane.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        messageInput = new JTextField();
        messageInput.setText("");
        rootPane.add(messageInput, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(800, 30), null, null, 0, false));
        scrollPane1 = new JScrollPane();
        rootPane.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(800, 520), null, null, 0, false));
        messagePane = new JTextPane();
        messagePane.setEditable(false);
        scrollPane1.setViewportView(messagePane);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPane.add(panel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        historyYesterday = new JButton();
        historyYesterday.setText("Show History since yesterday");
        panel1.add(historyYesterday, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        history1Hour = new JButton();
        history1Hour.setText("Show History since 1 Hour");
        panel1.add(history1Hour, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        historyWeek = new JButton();
        historyWeek.setText("Show History Week");
        panel1.add(historyWeek, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        historyMonth = new JButton();
        historyMonth.setText("Show History Month");
        panel1.add(historyMonth, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        historyEver = new JButton();
        historyEver.setText("Show History Ever");
        panel1.add(historyEver, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPane;
    }
}
