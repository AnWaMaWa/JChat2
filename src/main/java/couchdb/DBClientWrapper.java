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
 * Created by awaigand on 21.04.2015.
 */
public class DBClientWrapper implements IClientHandler{

    public static final String JCHAT_BY_DATE_VIEW = "jchat/byDate";
    Lock lookForServersLock = new ReentrantLock(true);
    CouchDbClient client;
    ConfigHandler config;
    IMessagePublisher publisher;

    public DBClientWrapper(CouchDbClient cbd, ConfigHandler config, IMessagePublisher publisher){
        this.client = cbd;
        this.config = config;
        this.publisher = publisher;
    }

    public void printHistorySince(String jsonDateTime){
        List<Message> list = client.view(JCHAT_BY_DATE_VIEW)
                .includeDocs(true).startKey(jsonDateTime)
                .query(Message.class);
        for(Message m : list){
            publisher.publish(m);
        }
    }

    public void printHistorySince(String jsonDateTime, ISubscribe sub){
        List<Message> list = client.view(JCHAT_BY_DATE_VIEW)
                .includeDocs(true).startKey(jsonDateTime)
                .query(Message.class);
        for(Message m : list){
            sub.notify(m);
        }
    }

    private CouchDbClient createClient(String ip, String port) throws CouchDbException{
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

    private CouchDbClient testNextNode(Node server) throws CouchDbException{
        CouchDbClient testclient = createClient(ConfigHandler.getIPFromServerNode(server), ConfigHandler.getPortFromServerNode(server));
        return testclient;
    }

    private CouchDbClient recursiveTest(Iterator it) throws NoMoreServerException {
        try{
            if(it.hasNext()){
                return testNextNode((Node)it.next());
            }else{
                throw new NoMoreServerException();
            }
        }catch(CouchDbException ex){
            publisher.publish("Could not connect to node. Trying next");
            return recursiveTest(it);
        }
    }

    private void startReplacing(){
        Iterator it = config.getServerIterator();
        try {
            client = recursiveTest(it);
            publisher.publish("Reconnected! Carry on!");
        }catch(NoMoreServerException ex){
            publisher.publish("Tried all servers, Check your internet connection! Retrying in a bit...");
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startReplacing();
        }
    }

    public void replaceCouchDbClient(){
        if(lookForServersLock.tryLock()){
            try{
                publisher.publish("You seem to have connection problems, trying other servers...");
                startReplacing();
            }finally {
                lookForServersLock.unlock();
            }
        }else{
            //lock is already active. Wait for Lock to be unlocked
            lookForServersLock.lock();
            lookForServersLock.unlock();
            return;
        }
    }

    public CouchDbClient getCouchDbClient(){
        //lock is already active. Wait for Lock to be unlocked
        lookForServersLock.lock();
        lookForServersLock.unlock();
        return client;
    }

}
