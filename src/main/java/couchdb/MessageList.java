package couchdb;

import MessageObserver.IMessagePublisher;
import MessageObserver.ISubscribe;
import MessageObserver.Message;
import com.google.gson.JsonObject;
import org.lightcouch.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by awaigand on 09.04.2015.
 */
public class MessageList implements IMessagePublisher, IMessageHistory {

    public static final String TYPE = "type";
    public static final String MESSAGE_TYPE = "message";
    public static final String FILTER_TYPE = "filter";
    public static final String EDITED_BY = "edited_by";
    public static final String OWNER = "owner";
    private String since = "";
    private Thread currentMessageThread;
    ArrayList<ISubscribe> subscribers = new ArrayList<ISubscribe>();
    CouchDbClient couchDbClient;
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
        return couchDbClient;
    }

    private void setCouchDbClient(CouchDbClient couchDbClient) {
        this.couchDbClient = couchDbClient;
    }

    private void setSubscribers(ArrayList<ISubscribe> subscribers) {
        this.subscribers = subscribers;
    }

    public MessageList(CouchDbClient cbd, MessageFilter mf){
        setCouchDbClient(cbd);
        setMessageFilter(mf);
    }

    private void setCurrentMessageThread(Thread messageThread){
        this.currentMessageThread = messageThread;
    }

    private synchronized void setCurrentSince(String since){
        this.since = since;
    }

    private synchronized String getCurrentSince(){
        return this.since;
    }

    private boolean checkIfGoOn(Changes changes){
        try {
            return changes.hasNext();
        }catch(CouchDbException ex){
            publish(ex.getMessage());
            return true;
        }
    }

    private Thread heartbeatThreadFactory(){
        return new Thread("heartbeat"){
            public void run(){
                try {
                    while (true) {
                        getCouchDbClient().context().info();
                        Thread.sleep(5000);
                    }
                }catch(CouchDbException ex){
                    publish("Cannot connect to current server, while try others.");
                    currentMessageThread.stop();
                }catch(InterruptedException ex){

                }
            }
        };
    }

    private Thread messageThreadFactory(){
        return new Thread("messageListener"){
            public void run(){
                final Changes changes = getCouchDbClient().changes()
                        .includeDocs(true)
                        .heartBeat(1000)
                        .timeout(3000)
                        .since(getCurrentSince())
                        .continuousChanges();
                while (checkIfGoOn(changes)) {
                    ChangesResult.Row feed = changes.next();
                    if(feed != null) {
                        String docId = feed.getId();
                        JsonObject doc = feed.getDoc();
                        setCurrentSince(feed.getSeq());
                        if(checkIfDocIsOfType(doc,MESSAGE_TYPE)){
                            Message m = getCouchDbClient().getGson().fromJson(doc,Message.class);
                            if(messageFilter.checkIfMessageIsForUser(m))
                                publish(m);
                        }else if(checkIfUserIsOwner(doc)){
                            if(checkIfDocIsOfType(doc,FILTER_TYPE)){
                                Filter f = getCouchDbClient().getGson().fromJson(doc,Filter.class);
                                messageFilter.replaceFilter(f.getFilter());
                            }
                        }
                    }
                }
            }
        };
    }

    public void startListeningToChanges(){
        CouchDbInfo dbInfo = getCouchDbClient().context().info();
        setCurrentSince(dbInfo.getUpdateSeq());
        setCurrentMessageThread(messageThreadFactory());
        currentMessageThread.start();
        heartbeatThreadFactory().start();
    }

    private boolean checkIfUserIsOwner(JsonObject doc){
        return doc.has(OWNER) && doc.get(OWNER).getAsString().equals(messageFilter.username);
    }

    private boolean checkIfDocIsOfType(JsonObject doc, String type) {
        return doc.has(TYPE) && doc.get(TYPE).getAsString().equals(type);
    }

    private Message convertJsonObjectToMessage(JsonObject doc){
        return new Message(doc.get(MESSAGE_TYPE).toString(), doc.get(EDITED_BY).toString());
    }

    private synchronized void publish(Message m){
        for(ISubscribe sub : subscribers){
            sub.notify(m);
        }
    }

    private synchronized void publish(String m){
        for(ISubscribe sub : subscribers){
            sub.notify(m);
        }
    }

    @Override
    public void subscribe(ISubscribe subscriber) {
        getSubscribers().add(subscriber);
    }

    @Override
    public void unsubscribe(ISubscribe subscriber) {
        getSubscribers().remove(subscriber);
    }

    @Override
    public List<Message> getMessageHistory(int offset, int limit) {
        return null;
    }
}
