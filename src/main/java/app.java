import ChatCommands.CommandList;
import couchdb.MessageFilter;
import couchdb.MessageList;
import couchdb.QuerySender;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbException;
import org.lightcouch.CouchDbProperties;

import javax.swing.*;
import java.awt.*;

/**
 * Created by awaigand on 09.04.2015.
 */
public class app {

    public static String username = "default";

    public static CouchDbClient login(String error) throws Exception{
        CouchDbClient dbClient = null;
        try {
            Login dialog = new Login();
            if(error != null)
                dialog.toolbarText.setText(error);
            dialog.pack();
            dialog.setVisible(true);
            if(!dialog.tryToGoOn)
                throw new Exception();

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

            dbClient = new CouchDbClient(properties);
            username = dialog.getUsername();
            return dbClient;

        }catch(CouchDbException ex){

            if(dbClient!=null){
                dbClient.shutdown();
            }
            return login(ex.getMessage());
        }
    }

    public static void main(String[] args) {


            try {
                CouchDbClient dbClient = login(null);
                QuerySender ms = new QuerySender(dbClient);
                MessageFilter mf = new MessageFilter(username);
                MessageList ml = new MessageList(dbClient, mf);
                CommandList cl = new CommandList(ms, username);
                ChatWindow cw = new ChatWindow(ms, cl, "Inner");
                JFrame mainFrame = new JFrame("Chat Window");

                ml.startListeningToChanges();
                ml.subscribe(cw);
                mainFrame.getContentPane().setPreferredSize(new Dimension(500, 500));
                mainFrame.setContentPane(cw.getMainPane());
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainFrame.pack();
                mainFrame.setVisible(true);
            }catch(Exception ex){

            }






    }

}
