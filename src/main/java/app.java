import couchdb.MessageList;
import couchdb.MessageSender;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

/**
 * Created by awaigand on 09.04.2015.
 */
public class app {

    public static void main(String[] args) {

        Login dialog = new Login();
        dialog.pack();
        dialog.setVisible(true);

        CouchDbProperties properties = new CouchDbProperties()
                .setDbName("jchat")
                .setCreateDbIfNotExist(false)
                .setProtocol("http")
                .setHost("193.196.7.76")
                .setPort(8080)
                .setUsername(dialog.getUsername())
                .setPassword(dialog.getPassword())
                .setMaxConnections(100)
                .setConnectionTimeout(0);

        CouchDbClient dbClient = new CouchDbClient(properties);

        MessageSender ms = new MessageSender(dbClient);
        MessageList ml = new MessageList(dbClient);


        ChatWindow cw = new ChatWindow(ms, "Inner");
        JFrame mainFrame = new JFrame("Chat Window");

        ml.startListeningToChanges();
        ml.subscribe(cw);
        mainFrame.getContentPane().setPreferredSize(new Dimension(500, 500));
        mainFrame.setContentPane(cw.getMainPane());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);


    }

}
