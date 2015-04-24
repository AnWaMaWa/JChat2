package MessageObserver;

import org.lightcouch.Document;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of Message Document.
 * Also offers some convenience methods.
 * Created by awaigand on 09.04.2015.
 */
public class Message extends Document implements IMessage {

    private String message;
    private String owner;
    private String[] to;
    private String created;
    private static Pattern receiverPattern = Pattern.compile("(^|\\s)([@#]\\S*)");

    public Message(String message, String user) {
        this.message = message;
        this.owner = user;

    }

    public Message() {
        super();
    }

    /**
     * Takes raw user input data and turns it into a message object.
     * It also parses receiver, which are individuals (starting with @) and groups (starting with #)
     * @param messageAndReceiver Raw Input Data like "Hello! @user1 #friends"
     * @return Message Object made from raw input data.
     */
    public static Message FromMessageAndReceiverFactory(String messageAndReceiver) {
        Message message = new Message();
        Matcher m = receiverPattern.matcher(messageAndReceiver);
        ArrayList<String> temp = new ArrayList<String>();
        while (m.find())
            temp.add(m.group(2));

        message.to = (String[]) temp.toArray(new String[temp.size()]);
        message.message = messageAndReceiver;

        return message;
    }

    @Override
    public String getBody() {
        return message;
    }

    @Override
    public String getUser() {
        return owner;
    }

    @Override
    public String[] getReceiver() {
        return to;
    }

    @Override
    public String getCreated() {
        return created;
    }
}
