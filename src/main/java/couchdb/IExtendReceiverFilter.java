package couchdb;

/**
 * Created by awaigand on 10.04.2015.
 */
public interface IExtendReceiverFilter {
    public void addAcceptedReceiver(String receiverName);
    public void removeAcceptedReceiver(String receiverName);
}
