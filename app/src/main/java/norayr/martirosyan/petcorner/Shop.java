package norayr.martirosyan.petcorner;

public class Shop {

    public String id;
    public String name;
    public String owner;
    public String address;
    public String phone;
    public String description;
    public String imageUrl;
    public String userId;

    public float averageRating;

    public double latitude;
    public double longitude;

    public long timestamp;


    public String status;


    public Shop() {}

    public Shop(String name,
                String owner,
                String address,
                String phone,
                String description,
                String imageUrl,
                String userId) {

        this.name = name;
        this.owner = owner;
        this.address = address;
        this.phone = phone;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userId = userId;

        this.timestamp = System.currentTimeMillis();
        this.averageRating = 0f;


        this.status = "pending";
    }
}