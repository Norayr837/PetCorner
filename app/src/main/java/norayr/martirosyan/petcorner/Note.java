package norayr.martirosyan.petcorner;

public class Note {

    public String id;
    public String title;
    public String text;
    public long timeMillis;
    public String userId;

    public Note() {}

    public Note(String title, String text, long timeMillis, String userId) {
        this.title = title;
        this.text = text;
        this.timeMillis = timeMillis;
        this.userId = userId;
    }
}