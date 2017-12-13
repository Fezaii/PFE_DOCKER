import com.rabbitmq.client.*;
import java.util.ArrayList;
import java.io.IOException;
import java.text.ParseException;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeoutException;
import java.io.*;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.text.ParseException;
import com.google.gson.Gson;
import org.json.simple.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Reader {


    private final static String QUEUE2 = "Alldockerfiles";
    private final static  String QUEUE3 = "dockerfilecontents";



    public static void send(String ch)throws IOException{
        //ArrayList<String> listdockerfile= new ArrayList<String>();
        //listdockerfile = Worker.alldockerfile(ch);
        String delims = "[+]+";
        String[] tokens = ch.split(delims);
        //System.out.println(tokens[0]);
        //System.out.println(tokens[1]);
        //String str = Worker.getcontents(tokens[0],tokens[1]);
        //System.out.println("Sent" + str);
        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            //System.out.println(ch);
            channel.queueDeclare(QUEUE3, false, false, false, null);
            //System.out.println(tokens[0]);
            //System.out.println(tokens[1]);
            String str = Worker.getcontents(tokens[0],tokens[1]);
            str =ch+"+ "+str;
            //System.out.println("Sent" + str);
            if(str != null) {
                channel.basicPublish("", QUEUE3, null, str.getBytes("UTF-8"));
                System.out.println("===================>"+ str);
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

        channel.queueDeclare(QUEUE2, false, false, false, null);
        System.out.println("Waiting for dockerfile");
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException{
                String message = new String(body, "UTF-8");
                //System.out.println(message);
                send(message);
                //Worker.alldockerfile(message);
                //Worker.getGitdockerfile(message);

            }
        };
        channel.basicConsume(QUEUE2, true, consumer);

    }

}