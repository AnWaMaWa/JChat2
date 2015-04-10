package MessageObserver;

import org.lightcouch.Document;

/**
 * Created by awaigand on 09.04.2015.
 */
public class Message extends Document implements IMessage {

    private String message;
    private String owner;
    private String to;

    public Message(String message, String user){
        this.message = message;
        this.owner = user;
    }

    public Message(){

    }

    public static Message FromMessageAndReceiverFactory(String messageAndReceiver){
        Message message = new Message();
        if(messageAndReceiver.startsWith("@")){
            message.to=messageAndReceiver.substring(1,messageAndReceiver.indexOf(' '));
            message.message = messageAndReceiver.substring(messageAndReceiver.indexOf(' '));
        }else {
            message.message = messageAndReceiver;
        }
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
    public String getReceiver() {
        return to;
    }
}
