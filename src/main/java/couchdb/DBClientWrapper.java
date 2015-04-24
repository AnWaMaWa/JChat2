package couchdb;

import CustomException.NoMoreServerException;
import MessageObserver.IMessagePublisher;
import MessageObserver.ISubscribe;
import MessageObserver.Message;
import config.ConfigHandler;
import org.dom4j.Node;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbException;
import org.lightcouch.CouchDbProperties;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps the CouchDBClient provided by lightcouch API, so it can be
 * globally replaced by just replacing the reference provided here.
 * It also provides access to the message history by using said client.
 * All database connection, except MessageReceiver, is in this class.
 * Created by awaigand on 21.04.2015.
 */
public class DBClientWrapper implements IClientHandler {

    public static final String JCHAT_BY_DATE_VIEW = "jchat/byDate"; //CouchDB View which shows messages by date
    Lock lookForServersLock = new ReentrantLock(true); //This lock is active when connection was lost.
    CouchDbClient client;
    ConfigHandler config;
    IMessagePublisher publisher;
    IFilterMessage mf;

    public DBClientWrapper(CouchDbClient cbd, ConfigHandler config, IMessagePublisher publisher, IFilterMessage mf) {
        this.client = cbd;
        this.config = config;
        this.publisher = publisher;
        this.mf = mf;
    }

    /**
     * Prints all messages received since jsonDateTime to all subscribers of the MessagePublisher, usually only the main chat window
     * @param jsonDateTime ISO8061 formatted DateTime
     */
    public void printHistorySince(String jsonDateTime) {
        List<Message> list = client.view(JCHAT_BY_DATE_VIEW)
                .includeDocs(true).startKey(jsonDateTime)
                .query(Message.class); //calls a view with datetime as start key, meaning it will return all messages since (and including) jsonDateTime
        for (Message m : list) {
            if (mf.checkIfMessageIsForUser(m)) { //checks if the message is meant for the user using the same filter as the MessageReceiver
                publisher.publish(m);
                ConfigHandler.currentSince = m.created;
            }//updates the currentSince Timestamp, since users most recent message is shown at the end.
        }
    }

    /**
     * Sends all messages since jsonDateTime to only one subscriber, which is given as a parameters.
     * @param jsonDateTime ISO8061 formatted DateTime
     * @param sub Subscriber to send to
     */
    public void printHistorySince(String jsonDateTime, ISubscribe sub) {
        List<Message> list = client.view(JCHAT_BY_DATE_VIEW)
                .includeDocs(true).startKey(jsonDateTime)
                .query(Message.class);
        for (Message m : list) {
            if (mf.checkIfMessageIsForUser(m)) {
                sub.notify(m);
                ConfigHandler.currentSince = m.created;
            }
        }
    }

    /**
     * Creates client using the ip and port given as parameters and the currently configured username and password.
     * @param ip CouchDB Server IP
     * @param port CouchDB Server PORT
     * @return CouchDbClient
     * @throws CouchDbException thrown when connection could not be established
     */
    private CouchDbClient createClient(String ip, String port) throws CouchDbException {
        CouchDbProperties properties = new CouchDbProperties()
                .setDbName("jchat")
                .setCreateDbIfNotExist(false)
                .setProtocol("http")
                .setHost(ip)
                .setPort(Integer.parseInt(port))
                .setUsername(ConfigHandler.username)
                .setPassword(ConfigHandler.password)
                .setMaxConnections(100)
                .setConnectionTimeout(0);

        return new CouchDbClient(properties);
    }

    /**
     * Trys to create a connection with the server represented by the server node
     * @param server Server to try
     * @return working couchDbClient
     * @throws CouchDbException Thrown if connection can not be established.
     */
    private CouchDbClient tryConnectingToNode(Node server) throws CouchDbException {
        CouchDbClient testclient = createClient(ConfigHandler.getIPFromServerNode(server), ConfigHandler.getPortFromServerNode(server));
        return testclient;
    }

    /**
     * Tries to connect to all nodes provided by the iterator.
     * This is done by using recursion, which can lead to stack overflow if there are many, many nodes.
     * If there are no more servers to try it throughs a NoMoreServerException, wihch is catched by the
     * calling function to restart with all servers again.
     * It handles the CouchDbExcpetion thrown by tryConnectoinToNode, which represents that no
     * connection could be established to the node.
     * @param it Iterator over all server nodes provided by config file
     * @return Working CouchDbClient
     * @throws NoMoreServerException When iterator has no more nodes.
     */
    private CouchDbClient recursiveTest(Iterator it) throws NoMoreServerException {
        try {
            if (it.hasNext()) {
                return tryConnectingToNode((Node) it.next());
            } else {
                throw new NoMoreServerException();
            }
        } catch (CouchDbException ex) {
            publisher.publish("Could not connect to node. Trying next");
            return recursiveTest(it);
        }
    }

    /**
     * Starts the process of checking through all nodes repeately, until connection can be reestablished.
     * Does so by using recursion, which could lead to a StackOverflow if it goes on for a couple of hours.
     * This specific function does a recursion if all servers provided by the config file have been tried.
     */
    private void startReplacing() {
        Iterator it = config.getServerIterator();
        try {
            client = recursiveTest(it);
            publisher.publish("Reconnected! Carry on!");
        } catch (NoMoreServerException ex) {
            publisher.publish("Tried all servers, Check your internet connection! Retrying in a bit...");
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startReplacing();
        }
    }

    /**
     * Starts going through all server nodes until one can be reached or the user quits the program.
     * Locks all possible couchDB activity by locking the lookForServersLock, which is also used by
     * the getCouchDbClient() function.
     * Unlocks the lock after a working server connection was established.
     * If this function is called while the lock is already set, meaning a different thread already noticed
     * that there are connectoin issues, the calling thread will wait until the lock is unlocked (else block).
     */
    public void replaceCouchDbClient() {
        if (lookForServersLock.tryLock()) {
            try {
                publisher.publish("You seem to have connection problems, trying other servers...");
                startReplacing();
            } finally {
                lookForServersLock.unlock();
            }
        } else {
            //lock is already active. Wait for Lock to be unlocked. The only method to achieve this we found was
            //To lock and unlock the lock directly.
            lookForServersLock.lock();
            lookForServersLock.unlock();
            return;
        }
    }

    public CouchDbClient getCouchDbClient() {
        //When the lookforServersLock is currently active, i.e. something is wrong with the connection
        //wait until the lock is unlocked, meaning that the connection error was fixed, before you return
        //the couchDBclient. Locking and unlocking was the only method to achieve this we found.
        lookForServersLock.lock();
        lookForServersLock.unlock();
        return client;
    }

}
