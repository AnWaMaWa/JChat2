import ChatCommands.CommandList;
import config.ConfigHandler;
import couchdb.DBClientWrapper;
import couchdb.MessageFilter;
import couchdb.MessageList;
import couchdb.QuerySender;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbException;
import org.lightcouch.CouchDbProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by awaigand on 09.04.2015.
 */
public class app {

    //Represents the current version and product name
    private static String PRODUCT_DESIGNATION = "Sasagi v1.0a";

    /**
     * This function is used to create a couchDB Client using the ip, port, username and password given as parameters.
     *
     * @param ip       IP of CouchDB Server
     * @param port     Port of CouchDB Server
     * @param username Username
     * @param password Password
     * @return
     */
    public static CouchDbClient getCouchDBClientForIpAndPort(String ip, String port, String username, String password) throws CouchDbException {
        CouchDbProperties properties = new CouchDbProperties()
                .setDbName("jchat")
                .setCreateDbIfNotExist(false)
                .setProtocol("http")
                .setHost(ip)
                .setPort(Integer.parseInt(port))
                .setUsername(password)
                .setPassword(password)
                .setMaxConnections(100)
                .setConnectionTimeout(0);

        return new CouchDbClient(properties);
    }

    /**
     * This function is used to display the login dialog and connect to a database
     * It uses recursion to showLoginDialog if the username or password is wrong.
     * This is mainly done for convenience but could lead to a StackOverflow.
     *
     * @param message This message, if set, will be displayed at the bottom of the login client
     * @param server  This xml Node will be used to get the server IP and Port
     * @return CouchDBClient Null if could not connect
     */

    public static CouchDbClient login(String message, Node server) {
        CouchDbClient dbClient = null;
        String ip = "";
        String port = "";
        try {
            //These two static message get the relevant values from the XML Node "server"
            ip = ConfigHandler.getIPFromServerNode(server);
            port = ConfigHandler.getPortFromServerNode(server);

            Login dialog = new Login();
            if (message != null)
                dialog.toolbarText.setText(message);
            dialog.pack();
            dialog.setVisible(true);

            //tryToGoOn is false when the user presses cancel
            if (!dialog.tryToGoOn)
                System.exit(0);

            dbClient = getCouchDBClientForIpAndPort(ip, port, dialog.getUsername(), dialog.getPassword());

            ConfigHandler.username = dialog.getUsername();
            ConfigHandler.password = dialog.getPassword();

            return dbClient;

          /* A Couchdbexception is thrown by the LightCouch Library when something went wrong in a general sense
             To find out what exactly went wrong, the quickest way is to string-compare the Exception Message
           */
        } catch (CouchDbException ex) {

            //Password wrong, Login wrong or anything like that
            if (ex.getMessage().startsWith("Unauthorized")) {
                if (dbClient != null) {
                    dbClient.shutdown();
                }
                //retry with same IP and Port
                return login(ex.getMessage(), server);
            } //Most likely a network error or the server is down.
            else {
                return null;
            }
        }
    }

    /**
     * Main Method
     *
     * @param args
     * @throws DocumentException Is thrown when something went wrong with reading or writing the Config File
     */

    public static void main(String[] args) throws DocumentException {


        if (!ConfigHandler.checkIfConfigExists())
            try {
                ConfigHandler.writeDefaultConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }


        final ConfigHandler config = new ConfigHandler();


        CouchDbClient dbClient = showLoginDialog(PRODUCT_DESIGNATION, config);


        MessageFilter mf = new MessageFilter(ConfigHandler.username);
        MessageList ml = new MessageList(mf);

        DBClientWrapper dbcw = new DBClientWrapper(dbClient, config, ml, mf);
        ml.setClientWrapper(dbcw);

        QuerySender ms = new QuerySender(dbcw);
        HistoryFrameFactory hff = new HistoryFrameFactory(dbcw);
        CommandList cl = new CommandList(ms, ConfigHandler.username);
        ChatWindow cw = new ChatWindow(ms, hff, cl, "Inner");

        JFrame mainFrame = new JFrame("Chat Window - " + ConfigHandler.username);

        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    config.writeSinceTime();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });

        ml.startListeningToChanges();
        ml.subscribe(cw);
        mainFrame.getContentPane().setPreferredSize(new Dimension(500, 500));
        mainFrame.setContentPane(cw.getMainPane());

        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        mainFrame.pack();
        mainFrame.setVisible(true);


    }

    /**
     * Used for looping login trys and resetting the server iterator once all nodes have been tried.
     *
     * @param message A message which will be displayed at the bottom of the login page
     * @param config  ConfigHandler, used to get the server iterator.
     * @return A working and connected CouchDbClient
     */
    public static CouchDbClient showLoginDialog(String message, ConfigHandler config) {

        CouchDbClient dbClient = null;
        Iterator it = config.getServerIterator();
        while (dbClient == null) {
            if (it.hasNext()) {
                dbClient = login(message, (Node) it.next());
                if (dbClient == null) {
                    message = "Could not connect to Node. Trying next...";
                }
            } else {
                it = config.getServerIterator();
                message = "Failed. No more servers to try. Check your internet connection. Trying them all again.";
            }
        }
        return dbClient;
    }

}
