package norayr.martirosyan.petcorner;

public class Service {

    public String id;
    public String name;
    public String company;
    public String address;
    public String phone;
    public String description;
    public String imageUrl;
    public String userId;

    public String status; // 👈 ВОТ ЭТО ДОБАВИЛИ

    public double averageRating;
    public double latitude;
    public double longitude;

    public long timestamp;

    public Service() {
        // Firebase needs empty constructor
    }

    public Service(String name, String company, String address,
                   String phone, String description,
                   String imageUrl, String userId) {

        this.name = name;
        this.company = company;
        this.address = address;
        this.phone = phone;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userId = userId;

        this.status = "pending"; // 👈 ВАЖНО

        this.timestamp = System.currentTimeMillis();
    }
}