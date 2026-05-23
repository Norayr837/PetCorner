package norayr.martirosyan.petcorner;

public class PrivateMessage {

    public String id;
    public String senderId;
    public String receiverId;
    public String senderName;
    public String text;
    public long timestamp;

    public PrivateMessage() {}

    public PrivateMessage(String id, String senderId, String receiverId,
                          String senderName, String text, long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
    }
}