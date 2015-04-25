package jenkins.plugins.ChatRooms;

public interface ChatService {
    void publish(String message);

    void publish(String message, String color);
}
