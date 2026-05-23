package norayr.martirosyan.petcorner;

public class ChatItem {

    public String chatId;
    public String otherUserId;
    public String lastMessage;
    public long timestamp;

    public ChatItem() {}

    public ChatItem(String chatId, String otherUserId, String lastMessage, long timestamp) {
        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }
}