package jenkins.plugins.ChatRooms;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.SimpleFormatter;

import jenkins.model.Jenkins;
import hudson.ProxyConfiguration;

public class StandardChatService implements ChatService {
static private FileHandler fileTxt;
 static private SimpleFormatter formatterTxt;

  static private FileHandler fileHTML;

      private static final Logger logger = Logger.getLogger(StandardChatService.class.getName());

    private String host = "127.0.0.1:50265";
    private String token;
    private String[] roomIds;
    private String from;

    public StandardChatService(String token, String roomId, String from) {
        super();
     
        this.token = token;
        this.roomIds = roomId.split(",");
        this.from = from;
        try{
        setup();
        } catch (IOException e) {
    //System.err.println("Caught IOException: " + e.getMessage());
}
    }
static public void setup() throws IOException {


    logger.setLevel(Level.INFO);

    fileTxt = new FileHandler("Logging.txt");

  
    // create a TXT formatter

    formatterTxt = new SimpleFormatter();

    fileTxt.setFormatter(formatterTxt);

    logger.addHandler(fileTxt);



    

  }
    public void publish(String message) {
        publish(message, "yellow");
    }

    public void publish(String message, String color) {
        for (String roomId : roomIds) {
            logger.info("Posting: " + from + " to " + roomId + ": " + message + " " + color);
            HttpClient client = getHttpClient();
                   String url = "http://localhost:50265/Default.aspx";
                    PostMethod post = new PostMethod(url);
            try {
                post.addRequestHeader("API-Key", "COHKEN360JD00W43W8BT3X4O23S3T8V4");
                post.addParameter("from", from);
                post.addParameter("room_id", roomId);
                post.addParameter("message", message);
                post.addParameter("color", color);
                post.addParameter("otr", "0");
                post.addParameter("notify", "1");
                post.getParams().setContentCharset("UTF-8");
                int responseCode = client.executeMethod(post);
                String response = post.getResponseBodyAsString();
                logger.info("Posting2:");
                //if(responseCode != HttpStatus.SC_OK || ! response.contains("true")) {
                if(responseCode != HttpStatus.SC_OK ) {
                    logger.log(Level.WARNING, "Chat post may have failed. Response: " + response);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error posting to Chat Room", e);
            } finally {
                post.releaseConnection();
            }
        }
    }
    
    private HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        if (Jenkins.getInstance() != null) {
            ProxyConfiguration proxy = Jenkins.getInstance().proxy;
            if (proxy != null) {
                client.getHostConfiguration().setProxy(proxy.name, proxy.port);
            }
        }
        return client;
    }

    private String shouldNotify(String color) {
        return color.equalsIgnoreCase("green") ? "0" : "1";
    }

    public void setHost(String host) {
        this.host = host;
    }
}
