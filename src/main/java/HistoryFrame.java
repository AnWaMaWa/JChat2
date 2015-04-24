import MessageObserver.ISubscribe;
import MessageObserver.Message;
import couchdb.DBClientWrapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;

/**
 * Used for displaying message history.
 * Created by awaigand on 22.04.2015.
 */
public class HistoryFrame extends JFrame implements ISubscribe {
    JTextArea jTextArea = new JTextArea();
    GridLayout gridLayout = new GridLayout(1, 1);
    DateTimeFormatter isoDateTimeFormatter = ISODateTimeFormat.dateTime();

    /**
     * Creates the necessary scrollPane, adds the textarea to it and uses
     * the printHistorySince function provided by the DBClientWrapper to get the desired histor.
     * Its only purpose is to display the desired history, it will not be reused to display other history for example.
     * @param windowTitle
     * @param dbc
     * @param jsonTime ISO8601 Time String in UTC signalling from what point in time onwards messages should be shown
     */
    public HistoryFrame(String windowTitle, DBClientWrapper dbc, String jsonTime) {
        super(windowTitle);
        jTextArea.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        jScrollPane.setLayout(new ScrollPaneLayout());
        this.setLayout(gridLayout);
        //Makes DbClientWrapper write all history since jsonTime into the jTextArea via the ISubscribe Interface.
        //Skips no messages, since all history beginning with jsonTime should be shown
        dbc.printHistorySince(jsonTime, this,0);
        this.add(jScrollPane);
        this.setPreferredSize(new Dimension(800, 800));
    }


    /**
     * Appends the given string to the textarea
     * @param s String to append
     */
    private void append(String s) {
        try {
            Document doc = jTextArea.getDocument();
            doc.insertString(doc.getLength(), s + "\n", null);
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
    }


    /**
     * Appends the message to the textarea.
     * Unlike the normal chat window, the history also adds the time the message was created to the output.
     * It converts the rather unusual ISO 8601 Format to mediumDateTime Format for easy normal human readability
     * @param m Message to be displayed
     */
    @Override
    public void notify(Message m) {
        DateTime dt = new DateTime(isoDateTimeFormatter.parseDateTime(m.getCreated())); //CouchDB uses ISO8601, so it can be parsed with joda-time
        append(m.getUser() + "(" + dt.toString(DateTimeFormat.mediumDateTime()) + "): " + m.getBody());
    }

    @Override
    public void notify(String m) {
        append(m);
    }
}
