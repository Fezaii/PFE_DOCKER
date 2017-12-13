import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.ArrayList;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.TimeoutException;
public class Producer {

    private final static String QUEUE_NAME = "Repositories";
    private static  ArrayList<String> listrepos= new ArrayList<String>();
    private  static  int  a, b;
    public Producer(int a , int b ){
        this.a=a;
        this.b=b;
    }
    public static void run()throws IOException,TimeoutException{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        for (int i = a; i < b;) {
            Worker.getEach(i, listrepos);
            i = i + 200;
            }
            for(String ch : listrepos) {
                //String message = "Hello World!";
                channel.basicPublish("", QUEUE_NAME, null, ch.getBytes("UTF-8"));
                System.out.println("Sent" + ch);
            }

        channel.close();
        connection.close();
    }
}