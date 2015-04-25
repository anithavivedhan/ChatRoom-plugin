package jenkins.plugins.chatroom;


import org.junit.Before;
import org.junit.Test;
import jenkins.plugins.ChatRooms.StandardChatService;

public class StandardChatServiceTest {

    /**
     * Publish should generally not rethrow exceptions, or it will cause a build job to fail at end.
     */
    @Test
    public void publishWithBadHostShouldNotRethrowExceptions() {
        StandardChatService service = new StandardChatService("token", "room", "from");
        service.setHost("hostvaluethatwillcausepublishtofail");
        service.publish("message");
    }
}