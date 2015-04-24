package couchdb;

import MessageObserver.IMessagePublisher;
import MessageObserver.ISubscribe;
import MessageObserver.Message;
import com.google.gson.JsonObject;
import config.ConfigHandler;
import org.lightcouch.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses the changeFeed of the CouchDB Server to show recent messages.
 * It is also responsible for ensuring that the current connection is working, and
 * therefore implements a heartbeat function.
 * If connection is lost it tries to restore it.
 * Created by awaigand on 09.04.2015.
 */
public class MessageReceiver implements IMessagePublisher {

    public static final String TYPE = "type";
    public static final String MESSAGE_TYPE = "message";
    public static final String FILTER_TYPE = "filter";
    public static final String EDITED_BY = "edited_by";
    public static final String OWNER = "owner";
    private String since = "";
    private Thread currentMessageThread;
    private Thread currentHeartbeatThread;
    ArrayList<ISubscribe> subscribers = new ArrayList<ISubscribe>();
    DBClientWrapper clientWrapper;
    MessageFilter messageFilter;

    private MessageFilter getMessageFilter() {
        return messageFilter;
    }

    private void setMessageFilter(MessageFilter messageFilter) {
        this.messageFilter = messageFilter;
    }

    private ArrayList<ISubscribe> getSubscribers() {
        return subscribers;
    }

    private CouchDbClient getCouchDbClient() {
        return clientWrapper.getCouchDbClient();
    }

    private void setSubscribers(ArrayList<ISubscribe> subscribers) {
        this.subscribers = subscribers;
    }

    public MessageReceiver(MessageFilter mf) {
        setMessageFilter(mf);
    }

    private void setCurrentMessageThread(Thread messageThread) {
        this.currentMessageThread = messageThread;
    }

    private void setCurrentSince(String since) {
        ConfigHandler.currentSince = since;
    }

    private String getCurrentSince() {
        return ConfigHandler.currentSince;
    }


    public void setClientWrapper(DBClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }

    public DBClientWrapper getClientWrapper() {
        return clientWrapper;
    }


        /**
         * Checks if there are new changes, and if so, returns true. Since changes.hasNext() is blocking and
         * changes is a continuous feed, it would only return false if it throws an exception.
         * If there is a problem it starts the process of reconnecting.
         * @param changes
         * @return true if there are new changes, false if there is a problem with the connection.
         */
    private boolean checkIfGoOn(Changes changes) {
        try {
            return changes.hasNext();
        } catch (CouchDbException ex) {
            currentHeartbeatThread.stop();
            lookForOtherNode();
            return false;
        }
    }

    /**
     * Returns a "heartBeat" thread, which checks against the current couchdb every 15 seconds.
     * This is only relevant if the user lost connection to the entire internet, since otherwise
     * the changeFeed messageThread realize that the connection was lost.
     * It is an idiosyncrasy of lightCouch API that the change feed fails silently if there is no connection at all
     * which makes this heartbeat necessary.
     * @return
     */
    private Thread heartbeatThreadFactory() {
        return new Thread("heartbeat") {
            public void run() {
                try {
                    while (true) {
                        getCouchDbClient().context().info();
                        Thread.sleep(15000);
                    }
                } catch (CouchDbException ex) {
                    publish("Lost connection to current server... trying others...");
                    currentMessageThread.stop();
                    lookForOtherNode();
                } catch (InterruptedException ex) {

                }
            }
        };
    }

    /**
     * Starts the CouchDBClient replacement of the DBClientWrapper
     * Once it was replaced it restarts the messageThread and heartBeat thread.
     */
    private void lookForOtherNode() {
        clientWrapper.replaceCouchDbClient();
        currentMessageThread = messageThreadFactory();
        currentMessageThread.start();
        currentHeartbeatThread = heartbeatThreadFactory();
        currentHeartbeatThread.start();
    }

    /**
     * Returns the message Thread.
     * The MessageThread checks for changes in the couchDB. This means it checks if documents where changed there,
     * e.g. if a new message has been sent.
     * When it starts up it first prints all messages which were sent since the "currentSince" value,
     * which is set to the last time the user was online on startuo, and is then regularly changed by this thread to
     * reflect the latest message created time received.
     * Since all those message created times are set on the CouchDB Server, which are synced by NTP, they are within a small error margin
     * correct.
     * So, if it the client received "message1" which was "timestamped" by the server with time x, then currentSince is set to time x.
     * If the client then loses it's connection to the CouchDB for example and needs to retry for one minute, it then asks the server for all messages which
     * were created since time x, and gets this minutes worth of messages.
     * Since it is not the client's pc time, but the most recent message created time, this works without flaw.
     * @return
     */
    private Thread messageThreadFactory() {
        return new Thread() {
            public void run() {
                final Changes changes = getCouchDbClient().changes()
                        .includeDocs(true)
                        .heartBeat(1000)
                        .timeout(3000)
                        .since("now")
                        .continuousChanges();
                clientWrapper.printHistorySince(ConfigHandler.currentSince); //showing missed messages
                while (checkIfGoOn(changes)) {
                    ChangesResult.Row feed = changes.next();
                    if (feed != null) { //This needs to be done due to stupidy of lightcouch API (i.e. changes.next() can return "true" even though there is no next.
                        JsonObject doc = feed.getDoc();
                        if (checkIfDocIsOfType(doc, MESSAGE_TYPE)) { //If received document is a message
                            Message m = getCouchDbClient().getGson().fromJson(doc, Message.class);
                            if (messageFilter.checkIfMessageIsForUser(m)) { //if message needs to be seen by user
                                setCurrentSince(m.created); //update currentSince timestamp
                                publish(m); //show message to user
                            }
                        } else if (checkIfUserIsOwner(doc)) { //If received document is owned by the current user
                            if (checkIfDocIsOfType(doc, FILTER_TYPE)) { // If the received document is a "filter_type", i.e. the document which contains information about user groups
                                Filter f = getCouchDbClient().getGson().fromJson(doc, Filter.class);
                                messageFilter.replaceFilter(f.getFilter()); // replace all filters by the new filters.
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * Starts the message and heartbeat threads at startup
     */
    public void startListeningToChanges() {
        setCurrentMessageThread(messageThreadFactory());
        currentMessageThread.start();
        heartbeatThreadFactory().start();
    }

    /**
     * Checks whether the json document contains a property owner, and if so, if it is set tu the current username.
     * @param doc JsonDocument to be checked
     * @return true if owner is current user
     */
    private boolean checkIfUserIsOwner(JsonObject doc) {
        return doc.has(OWNER) && doc.get(OWNER).getAsString().equals(ConfigHandler.username);
    }

    /**
     * Checks whether the json document contains a property type, and if so, if it is set to the given type
     * @param doc JsonDocument to be checked
     * @param type Type to check against
     * @return true if type is type
     */
    private boolean checkIfDocIsOfType(JsonObject doc, String type) {
        return doc.has(TYPE) && doc.get(TYPE).getAsString().equals(type);
    }


    /**
     * Observer Pattern GOF
     * Sends message to all subscribes.
     * Normally, only the main message pane is a subscriber
     * @param m Message to be send
     */
    public synchronized void publish(Message m) {
        for (ISubscribe sub : subscribers) {
            sub.notify(m);
        }
    }

    /**
     * Observer Pattern GOF
     * Sends strings to all subscribes.
     * Normally, only the main message pane is a subscriber
     * @param m String to be send
     */

    public synchronized void publish(String m) {
        for (ISubscribe sub : subscribers) {
            sub.notify(m);
        }
    }

    /**
     * Observer Pattern GOF
     * @param subscriber
     */

    @Override
    public void subscribe(ISubscribe subscriber) {
        getSubscribers().add(subscriber);
    }

    /**
     * Observer Pattern GOF
     * @param subscriber
     */
    @Override
    public void unsubscribe(ISubscribe subscriber) {
        getSubscribers().remove(subscriber);
    }
}
