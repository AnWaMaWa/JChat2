import ChatCommands.CommandList;
import CustomException.NoMoreServerException;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * Created by awaigand on 09.04.2015.
 */
public class app {

    public static String username = "default";
    public static String password = "default";

    public static CouchDbClient login(String error, Iterator serverIterator) throws IllegalComponentStateException, NoMoreServerException {
        CouchDbClient dbClient = null;
        String ip = "";
        String port = "";
        try {

            if(serverIterator.hasNext()){
                Node server = (Node) serverIterator.next();
                ip = ConfigHandler.getIPFromServerNode(server);
                port = ConfigHandler.getPortFromServerNode(server);
            }
            else{
                throw new NoMoreServerException();
            }
            Login dialog = new Login();
            if (error != null)
                dialog.toolbarText.setText(error);
            dialog.pack();
            dialog.setVisible(true);
            if (!dialog.tryToGoOn)
                throw new IllegalComponentStateException();

            CouchDbProperties properties = new CouchDbProperties()
                    .setDbName("jchat")
                    .setCreateDbIfNotExist(false)
                    .setProtocol("http")
                    .setHost(ip)
                    .setPort(Integer.parseInt(port))
                    .setUsername(dialog.getUsername())
                    .setPassword(dialog.getPassword())
                    .setMaxConnections(100)
                    .setConnectionTimeout(0);

            dbClient = new CouchDbClient(properties);
            username = dialog.getUsername();
            password = dialog.getPassword();
            return dbClient;

        } catch (CouchDbException ex) {

            if(ex.getMessage().startsWith("Unauthorized")) {
                if (dbClient != null) {
                    dbClient.shutdown();
                }
                return login(ex.getMessage(), serverIterator);
            }
            else{
                return login("Could not connect to " + ip + ":" + port + " Try next node.",serverIterator);
            }
        }
    }


    public static void main(String[] args) throws DocumentException, NoMoreServerException {

        if(!ConfigHandler.checkIfConfigExists())
            try {
                ConfigHandler.writeDefaultConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }

        ConfigHandler config = new ConfigHandler();

        try {



            CouchDbClient dbClient = retry(null, config);
            ConfigHandler.username = username;
            ConfigHandler.password = password;

            MessageFilter mf = new MessageFilter(username);
            MessageList ml = new MessageList(mf);

            DBClientWrapper dbcw = new DBClientWrapper(dbClient,config,ml);
            ml.setClientWrapper(dbcw);

            QuerySender ms = new QuerySender(dbcw);
            HistoryFrameFactory hff = new HistoryFrameFactory(dbcw);
            CommandList cl = new CommandList(ms, username);
            ChatWindow cw = new ChatWindow(ms, hff,cl, "Inner");

            JFrame mainFrame = new JFrame("Chat Window - " + username);

            ml.startListeningToChanges();
            ml.subscribe(cw);
            mainFrame.getContentPane().setPreferredSize(new Dimension(500, 500));
            mainFrame.setContentPane(cw.getMainPane());

            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            mainFrame.pack();
            mainFrame.setVisible(true);
        } catch (IllegalComponentStateException ex) {

        }


    }
    public static CouchDbClient retry(String initialError, ConfigHandler config) throws IllegalComponentStateException{
        try {
            Iterator it = config.getServerIterator();
            return login(initialError, it);
        }catch(NoMoreServerException ex){
            return retry("Failed. No More Servers to try. Check your internet connection. Trying them all again from now", config);
        }
    }

}
