import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.*;
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
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;





import static org.neo4j.driver.v1.Values.parameters;

public class Worker implements AutoCloseable
{
    // Driver objects are thread-safe and are typically made available application-wide.
    Driver driver;
    public static File folder = new File("../pfedocker/alldockerfiles");
    public static File folder1 = new File("../pfedocker/Config");
    static String temp = "";
    private static  ArrayList<String> listrepos= new ArrayList<String>();



    public Worker(String uri, String user, String password)
    {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }


    public void  addFile( final Fichier dockerfile)
    {
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<Void>() {
                @Override
                public Void execute(Transaction tx) {
                    return createDockerFileNode(tx, dockerfile);
                    //createImageNode(tx,dockerfile.getImage());
                   //return createImageNode(tx,new RandomString().nextString());
                }
            });
;
        }
    }


    public void addFrom(final From link) {
        try (Session session = driver.session()) {
            session.writeTransaction(new TransactionWork<Void>() {
                @Override
                public Void execute(Transaction tx) {
                    createImageNode(tx, link.getImg1());
                    createImageNode(tx, link.getImg2());
                    return createRelationships2(tx, link.getImg1(), link.getImg2());
                }
            });
            ;
        }
    }


    public void addBuild(final Build link)
    {
        try ( Session session = driver.session() )
        {
            session.writeTransaction( new TransactionWork<Void>() {
                @Override
                public Void execute(Transaction tx) {
                    createImageNode(tx,link.getImage());
                    return createRelationships(tx,link.getHash(),link.getImage());
                }
            });
            ;
        }
    }

    private static Void createDockerFileNode( Transaction tx, Fichier fichier )
    {
        StatementResult result = tx.run("MATCH (a:DockerFile) RETURN a.hash AS hash");
        while ( result.hasNext() )
        {
            Record record = result.next();
            if (record.get("hash").asString().equals(fichier.getHash())){
                return  null;
            }
        }
        tx.run( "CREATE (a:DockerFile {name: $name,image: $image,hash: $hash,repo: $repo,path: $path})", parameters( "name", fichier.getName(),"image",fichier.getImage(),"hash",fichier.getHash(),"repo",fichier.getRepo(),"path",fichier.getPath()) );
        createImageNode(tx,fichier.getImage());
        String ch = new RandomString().nextString();
        createImageNode(tx,ch);
        createRelationships(tx,fichier.getHash(),ch);
        createRelationships2(tx,ch,fichier.getImage());
        return null;
    }

    private static Void createImageNode( Transaction tx, String name)
    {
        StatementResult result = tx.run("MATCH (a:Image) RETURN a.name AS name");
        while ( result.hasNext() )
        {
            Record record = result.next();
            if (record.get("name").asString().equals(name)){
            return  null;
        }
        }
        tx.run( "CREATE (a:Image {name: $name})", parameters( "name", name) );
        return null;
    }


    private static Void createRelationships( Transaction tx, String dockerfile,String image )

    {
        //System.out.println("je suis la");
        tx.run( "MATCH (a:DockerFile {hash: $hash }),(b:Image {name: $image}) MERGE (a)-[r:BUILD]->(b)", parameters( "hash", dockerfile,"image",image ));
        return null;
    }





    private static Void createRelationships2( Transaction tx, String img1,String img2 )
    {
        tx.run( "MATCH (a:Image { name:$image1 }),(b:Image {name: $image2}) MERGE (a)-[r:FROM]->(b)", parameters( "image1",img1,"image2",img2 ));
        return null;
    }

    public void close()
    {
        // Closing a driver immediately shuts down all open connections.
        driver.close();
    }



    public static ArrayList<Fichier> listFilesForFolder(final File folder) throws FileNotFoundException, java.io.IOException, NoSuchAlgorithmException {
       ArrayList<Fichier> listfiles = new ArrayList<Fichier>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                 listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.isFile()) {
                    temp = fileEntry.getName();
                    String ch = folder.getAbsolutePath()+ "//" + fileEntry.getName();
                    FileReader fr = new FileReader(fileEntry);
                    BufferedReader br = new BufferedReader(fr);
                    String s = "";
                    while (br.ready()) {
                        s += br.readLine() + "\n";
                    }
                    String delims = "[\n\\ ]+";
                    String[] tokens = s.split(delims);
                    for (int i = 0; i < tokens.length; i++){
                        if (tokens[i].equals("FROM")){
                            listfiles.add(new Fichier("Dockerfile",tokens[i + 1],hashString(s),"local directory","/pfedocker/alldockerfile"));
                        }
                    }

                }

            }
        }
        return  listfiles;
    }


    public static void  Update() throws IOException, NoSuchAlgorithmException {
       Worker x = new Worker("bolt://localhost:7687", "neo4j", "ahmed");
        final ArrayList<Fichier> list1 = listFilesForFolder(folder);
        final ArrayList<Build> list2 = build(folder1);
        final ArrayList<From> list3 = from(folder1);
        //x.matchAllFileNode(list1);

        for(Fichier f : list1){
            x.addFile(f);
            //f.ToString();

        }

        for(Build  f : list2){
            x.addBuild(f);
            //f.ToString();
        }


        for(From f : list3) {
            x.addFrom(f);
            //f.ToString();
        }
        x.close();
    }









    public static ArrayList<From> from(final File folder) throws FileNotFoundException, java.io.IOException, NoSuchAlgorithmException {
        ArrayList<From> FromRelations = new ArrayList<From>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                //System.out.println("je suis la ");
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.isFile()) {
                    temp = fileEntry.getName();
                    //System.out.println(temp);
                    String ch = folder.getAbsolutePath()+ "//" + fileEntry.getName();
                    FileReader fr = new FileReader(fileEntry);
                    BufferedReader br = new BufferedReader(fr);
                    String s = "";
                    while (br.ready()) {
                        s += br.readLine() + "\n";
                    }
                    String delims = "[\n\\ ]+";
                    String[] tokens = s.split(delims);
                    for (int i = 0; i < tokens.length; i++){
                        if (tokens[i].equals("from")){
                            FromRelations.add(new From(tokens[i -1],tokens[i + 1]));
                            //System.out.println(tokens[i + 1]);
                        }
                    }

                }

            }
        }
        return  FromRelations;
    }




    public static ArrayList<Build> build(final File folder) throws FileNotFoundException, java.io.IOException, NoSuchAlgorithmException {
        ArrayList<Build> BuildRelations = new ArrayList<Build>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.isFile()) {
                    temp = fileEntry.getName();
                    String ch = folder.getAbsolutePath()+ "//" + fileEntry.getName();
                    FileReader fr = new FileReader(fileEntry);
                    BufferedReader br = new BufferedReader(fr);
                    String s = "";
                    while (br.ready()) {
                        s += br.readLine() + "\n";
                    }
                    String delims = "[\n\\ ]+";
                    String[] tokens = s.split(delims);
                    for (int i = 0; i < tokens.length; i++){
                        if (tokens[i].equals("build")){
                            BuildRelations.add(new Build(tokens[i -1],tokens[i + 1]));
                        }
                    }

                }

            }
        }
        return  BuildRelations;
    }













public static boolean isDockerfile(String s){
    String[] tokens = s.split("/");
    for (int j = 0; j < tokens.length; j++){
        if(tokens[j].equals("Dockerfile")){
            return  true;
        }
    }
    return false;
}






public static  void getGitdockerfile(String repo) throws IOException{
        System.out.println(repo);
       ArrayList<String>  l = alldockerfile(repo);
       if(l.isEmpty()){
           System.out.println("ce repositorie ne contient pas de dockerfile");
           return;
       }
    final Worker example = new Worker("bolt://localhost:7687", "neo4j", "ahmed");
       for(String str : l){

           //if(getcontents(repo,str) != null) {
               //example.addFile(getcontents(repo, str));
               //System.out.println("DockerFile added");
           //}
           //getcontents(repo,str).ToString();
       }
    example.close();
}


    public static String hashString(String s){

        byte[] hash = null;
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hash = md.digest(s.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                sb.append(0);
                sb.append(hex.charAt(hex.length() - 1));
            } else {
                sb.append(hex.substring(hex.length() - 2));
            }
        }
        return sb.toString();
    }



    public static void getEach(int since,ArrayList<String> l) throws  FileNotFoundException{
        //String url = "https://api.github.com/repositories?since="+since+"ccess_token=ea322f284c983ae4653804ec53c779c2fdc6a233a";
        String url = "https://api.github.com/repositories?since="+since+"&access_token=fb243b822b1608cb42f688d4e0fa074f3540da64";
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            request.addHeader("content-type", "application/json");
            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");

            //System.out.println(json);
            JsonElement jelement = new JsonParser().parse(json);
            //JsonObject jo = jelement.getAsJsonObject();

            JsonArray jarr = jelement.getAsJsonArray();
            for (int i = 0; i < jarr.size(); i++) {
                JsonObject jo = (JsonObject) jarr.get(i);
                String fullName = jo.get("full_name").toString();
                fullName = fullName.substring(1, fullName.length()-1);
                //return fullName;
                l.add(fullName);
                //System.out.println(fullName);
            }

        } catch (IOException ex) {
            System.out.println(ex.getStackTrace());
        }
        //return null;
    }



    // ConvertStreamToString() Utility - we name it as crunchifyGetStringFromStream()
    private static String crunchifyGetStringFromStream(InputStream crunchifyStream) throws IOException {
        if (crunchifyStream != null) {
            Writer crunchifyWriter = new StringWriter();

            char[] crunchifyBuffer = new char[2048];
            try {
                Reader crunchifyReader = new BufferedReader(new InputStreamReader(crunchifyStream, "UTF-8"));
                int counter;
                while ((counter = crunchifyReader.read(crunchifyBuffer)) != -1) {
                    crunchifyWriter.write(crunchifyBuffer, 0, counter);
                }
            } finally {
                crunchifyStream.close();
            }
            return crunchifyWriter.toString();
        } else {
            return "No Contents";
        }
    }


    public static ArrayList<String> alldockerfile(String ch){
        ArrayList<String> dockerfilelist = new ArrayList<String>();
        String url = "https://api.github.com/repos/"+ch+"/git/trees/master?recursive=1&access_token=fb243b822b1608cb42f688d4e0fa074f3540da64";
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            request.addHeader("content-type", "application/json");
            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");

            //System.out.println(json);
            JsonElement jelement = new JsonParser().parse(json);
            JsonObject jo = jelement.getAsJsonObject();
            if(jo.get("tree")!= null) {
                String tree = jo.get("tree").toString();
                //tree = tree.substring(1,tree.length()-1);
                JsonElement element = new JsonParser().parse(tree);
                JsonArray jarr = element.getAsJsonArray();
                for (int i = 0; i < jarr.size(); i++) {
                    JsonObject joo = (JsonObject) jarr.get(i);
                    String fullName = joo.get("path").toString();
                    fullName = fullName.substring(1, fullName.length() - 1);
                    if (isDockerfile(fullName)) {
                        dockerfilelist.add(fullName);
                        //System.out.println(fullName);
                        //return fullName;
                    }


                }
            }

        }  catch (IOException ex) {
            System.out.println(ex.getStackTrace());
        }
        return  dockerfilelist;

    }
    public static  String getcontents(String repo,String f) throws IOException {
        String link = "https://raw.githubusercontent.com/"+repo+"/master/"+f;
        URL crunchifyUrl = new URL(link);
        HttpURLConnection crunchifyHttp = (HttpURLConnection) crunchifyUrl.openConnection();
        Map<String, List<String>> crunchifyHeader = crunchifyHttp.getHeaderFields();

        // If URL is getting 301 and 302 redirection HTTP code then get new URL link.
        // This below for loop is totally optional if you are sure that your URL is not getting redirected to anywhere
        for (String header : crunchifyHeader.get(null)) {
            if (header.contains(" 302 ") || header.contains(" 301 ")) {
                link = crunchifyHeader.get("Location").get(0);
                crunchifyUrl = new URL(link);
                crunchifyHttp = (HttpURLConnection) crunchifyUrl.openConnection();
                crunchifyHeader = crunchifyHttp.getHeaderFields();
            }
        }
        InputStream crunchifyStream = crunchifyHttp.getInputStream();
        String crunchifyResponse = crunchifyGetStringFromStream(crunchifyStream);


        //System.out.println("fichier vide");*/
        return crunchifyResponse;
    }

    public static void main(String... args) throws IOException,NoSuchAlgorithmException,TimeoutException{

        /*Worker x = new Worker("bolt://localhost:7687", "neo4j", "ahmed");
        final ArrayList<Fichier> list1 = listFilesForFolder(folder);
        //final ArrayList<Build> list2 = build(folder1);
        //final ArrayList<From> list3 = from(folder1);


        for(Fichier f : list1){
            x.addFile(f);
            f.ToString();

        }
        x.close();*/






    Producer producteur1 = new Producer(103945200,103945600);
    //Producer producteur2 = new Producer(103945600,103946200);
    producteur1.run();
    //producteur2.run();









        //------------GIT-----------------------------------------------------
        /*for (int i = 103945200; i < 103945200;) {
            getEach(i);
            i = i + 200;
        }

        for(String str : listrepos){
            getGitdockerfile(str);
        }

         /*-------ma clé--------ea322f284c983ae4653804ec53c779c2fdc6a233-------------------------*/


        /*final TimeoutThread thr = new TimeoutThread(new Runa() {

            @Override
            public Object call() throws Exception {
                while(true) {
                    Update();
                    //System.out.println("Yeeee");
                    //Thread.sleep(500L);
                }
            }


        }, 10000L);

        new Thread() {
            @Override
            public void run() {
                thr.run(); //Call it
            }
        }.start(); //Run it*/

    }

}
