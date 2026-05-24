package norayr.martirosyan.petcorner;

public class Review {

    public String reviewId;
    public String userId;
    public String username;
    public String text;
    public float rating;
    public long timestamp;

    public Review() {

    }

    public Review(String reviewId, String userId, String username, String text, float rating) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.rating = rating;
        this.timestamp = System.currentTimeMillis();
    }
}