import com.rabbitmq.client.*;
import java.util.ArrayList;
import java.io.IOException;
import java.text.ParseException;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeoutException;


public class Finder {

    private final static String QUEUE_NAME = "Repositories";
    private final static String QUEUE2 = "Alldockerfiles";




    public static void send(String ch)throws IOException{
        ArrayList<String> listdockerfile= new ArrayList<String>();
        listdockerfile = Worker.alldockerfile(ch);
        if(listdockerfile.isEmpty()){
            System.out.println("ce repositorie ne contient pas de dockerfile");
            return;
        }
        try{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE2, false, false, false, null);
        for(String str : listdockerfile) {
            String str2=ch+"+"+str;
            channel.basicPublish("", QUEUE2, null, str2.getBytes("UTF-8"));
            System.out.println(str2);
        }
        channel.close();
        connection.close();
        }catch (Exception e) {
            // Handling other exception. If necessary
        }
    }


    public static void main(String[] argv) throws Exception,ParseException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println("Waiting for Repo");
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException{
                String message = new String(body, "UTF-8");
                send(message);
                //Worker.alldockerfile(message);
                //Worker.getGitdockerfile(message);

            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);

    }

}