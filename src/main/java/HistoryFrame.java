import MessageObserver.IMessagePublisher;
import MessageObserver.ISubscribe;
import MessageObserver.Message;
import couchdb.DBClientWrapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by awaigand on 22.04.2015.
 */
public class HistoryFrame extends JFrame implements ISubscribe {
    JTextArea jTextPane = new JTextArea();

    GridLayout experimentLayout = new GridLayout(1,1);

    public HistoryFrame(String name, DBClientWrapper dbc, String jsonTime) {
        super(name);
        jTextPane.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(jTextPane);
        jScrollPane.setLayout(new ScrollPaneLayout());
        this.setLayout(experimentLayout);
        dbc.printHistorySince(jsonTime,this);
        this.add(jScrollPane);
        this.setPreferredSize(new Dimension(800,800));
    }

    private void append(String s) {
        try {
            Document doc = jTextPane.getDocument();
            doc.insertString(doc.getLength(), s + "\n", null);
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
    }



    @Override
    public void notify(Message m) {
        DateTime dt = new DateTime(ISODateTimeFormat.dateTime().parseDateTime(m.created));
        append(m.getUser() + "("+dt.toString(DateTimeFormat.mediumDateTime())+"): " + m.getBody());
    }

    @Override
    public void notify(String m) {
        append(m);
    }
}
