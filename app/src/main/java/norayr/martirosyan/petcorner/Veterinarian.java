package norayr.martirosyan.petcorner;

public class Veterinarian {

    public String id;
    public String name;
    public String clinic;
    public String address;
    public String phone;
    public String description;
    public String imageUrl;
    public String userId;

    public String status; // 👈 добавлено как в Service

    public double averageRating;
    public double latitude;
    public double longitude;

    public long timestamp;

    // Firebase needs empty constructor
    public Veterinarian() {}

    public Veterinarian(String name,
                        String clinic,
                        String address,
                        String phone,
                        String description,
                        String imageUrl,
                        String userId) {

        this.name = name;
        this.clinic = clinic;
        this.address = address;
        this.phone = phone;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userId = userId;

        this.status = "pending"; // 👈 по аналогии с Service
        this.averageRating = 0f;
        this.timestamp = System.currentTimeMillis();
    }
}
