package MessageObserver;

/**
 * Based on GOF Observer Pattern
 * Created by awaigand on 09.04.2015.
 */
public interface IMessagePublisher {

    public void subscribe(ISubscribe subscriber);

    public void unsubscribe(ISubscribe subscriber);

    public void publish(String s);

    public void publish(Message m);
}
