package couchdb;

import MessageObserver.Message;

import java.util.Arrays;
import java.util.HashSet;

/**
 * This class handels filtering out message which are not meant for the current user.
 * It checks against subscribed Hashtags (e.g. #MarketingDepartment) and
 * the username.
 * Created by awaigand on 10.04.2015.
 */

public class MessageFilter implements IExtendReceiverFilter, IFilterMessage, IReplaceFilter {

    HashSet<String> filter = new HashSet<String>();
    String username;

    public MessageFilter(String username) {
        this.username = username;
        addDefaultReceivers();
    }

    private void addDefaultReceivers() {
        addAcceptedReceiver("@" + username);
        addAcceptedReceiver("@global");
    }

    /**
     * Adds the receiverName to the filter, which makes it accept messages which are send to the
     * group designated by the receiverName.
     * @param receiverName
     */

    @Override
    public void addAcceptedReceiver(String receiverName) {
        filter.add(receiverName);
    }

    /**
     * Removes the receiver from the accepted list, thustly messages directet at the group designated by the
     * receiverName are no longer shown
     * @param receiverName
     */

    @Override
    public void removeAcceptedReceiver(String receiverName) {
        filter.remove(receiverName);
    }

    /**
     * Checks whether or not the given Message should be shown to the user.
     * It iterates over all receivers of the message (e.g. user1, user2 and #Marketing)
     * and checks whether or not they are currently in the filter array, and therefore must be
     * shown to the user.
     * It also shows messages with no receiver, which is the default, to the user.
     * Those messages are considered global.
     * @param m Message which receivers need to be checked.
     * @return true if message should be shown to user, false if it should not be shown
     */

    @Override
    public boolean checkIfMessageIsForUser(Message m) {
        if (m.getReceiver().length == 0)
            return true; //global
        for (String receiver : m.getReceiver()) {
            if (filter.contains(receiver))
                return true;
        }
        //If the user himself wrote the message, they should also be shown to the user.
        return username.equals(m.getUser());
    }

    /**
     * Replaces all filters with the given filter array.
     * Used for reloading filters from the config file.
     * @param filters
     */

    @Override
    public void replaceFilter(String[] filters) {
        filter = new HashSet<String>(Arrays.asList(filters));
        addDefaultReceivers();
    }
}
