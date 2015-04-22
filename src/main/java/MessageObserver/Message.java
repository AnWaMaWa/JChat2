package MessageObserver;

import org.lightcouch.Document;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by awaigand on 09.04.2015.
 */
public class Message extends Document implements IMessage {

    private String message;
    private String owner;
    private String[] to;
    public String created;
    private static Pattern receiverPattern = Pattern.compile("(^|\\s)([@#]\\S*)");

    public Message(String message, String user){
        this.message = message;
        this.owner = user;

    }

    public Message(){

    }

    public static Message FromMessageAndReceiverFactory(String messageAndReceiver){
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
}
