package couchdb;

import MessageObserver.IMessagePublisher;
import MessageObserver.ISubscribe;
import MessageObserver.Message;
import com.google.gson.JsonObject;
import org.lightcouch.Changes;
import org.lightcouch.ChangesResult;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbInfo;

import java.util.ArrayList;

/**
 * Created by awaigand on 09.04.2015.
 */
public class MessageList implements IMessagePublisher {

    ArrayList<ISubscribe> subscribers = new ArrayList<ISubscribe>();
    CouchDbClient couchDbClient;
    //Changes changes;

    /*public Changes getChanges() {
        return changes;
    }

    public void setChanges(Changes changes) {
        this.changes = changes;
    }*/

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

    public MessageList(CouchDbClient cbd){
        setCouchDbClient(cbd);
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

                        if(checkIfDocIsMessage(doc)){
                            publish(convertJsonObjectToMessage(doc));
                        }
                    }
                }
            }
        }.start();

    }

    private boolean checkIfDocIsMessage(JsonObject doc) {
        return doc.has("type") && doc.get("type").toString().contains("message");
    }

    private Message convertJsonObjectToMessage(JsonObject doc){
        return new Message(doc.get("message").toString(), doc.get("edited_by").toString());
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
}
