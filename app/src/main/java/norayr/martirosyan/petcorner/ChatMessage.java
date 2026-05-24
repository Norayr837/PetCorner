package norayr.martirosyan.petcorner;

public class ChatMessage {

    public String id;
    public String username;
    public String message;
    public long timestamp;
    public String profileImage;


    public ChatMessage() {
    }

    public ChatMessage(String id, String username, String message, long timestamp, String profileImage) {
        this.id = id;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
        this.profileImage = profileImage;
    }
}