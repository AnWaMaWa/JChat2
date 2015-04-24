package MessageObserver;

/**
 * All getter a message object must implement
 * Created by awaigand on 09.04.2015.
 */
public interface IMessage {
    public String getBody();

    public String getUser();

    public String[] getReceiver();

    public String getCreated();
}
