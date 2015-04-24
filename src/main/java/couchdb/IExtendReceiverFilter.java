package couchdb;

/**
 * Used by classes which can add possible receivers to the filter list.
 * Created by awaigand on 10.04.2015.
 */
public interface IExtendReceiverFilter {
    /**
     * Add a receiver to the filter list.
     * @param receiverName Name of the receiver
     */
    public void addAcceptedReceiver(String receiverName);

    /**
     * Remove a receiver off the filter list.
     * @param receiverName Name of receiver
     */
    public void removeAcceptedReceiver(String receiverName);
}
