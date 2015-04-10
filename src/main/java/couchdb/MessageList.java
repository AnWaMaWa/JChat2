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

    public void startListeningToChanges(){
        CouchDbInfo dbInfo = getCouchDbClient().context().info();
        final String since = dbInfo.getUpdateSeq();

        new Thread("messageListener"){
            public void run(){
                final Changes changes = getCouchDbClient().changes()
                        .includeDocs(true)
                        .heartBeat(1000)
                        .since(since)
                        .continuousChanges();
                while (changes.hasNext()) {
                    ChangesResult.Row feed = changes.next();
                    if(feed != null) {
                        String docId = feed.getId();
                        JsonObject doc = feed.getDoc();

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
        }.start();



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

    private void publish(Message m){
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
