package norayr.martirosyan.petcorner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ServiceDetailActivity extends AppCompatActivity
        implements ReviewAdapter.ReviewUpdateListener {

    ImageView image;
    TextView name, company, address, phone, description;

    RatingBar ratingBar;
    EditText etReview;
    Button btnSendReview, btnDeleteService, btnEditService;

    ImageButton btnCopyAddress, btnCopyPhone;

    RecyclerView rvReviews;

    List<Review> reviewList;
    ReviewAdapter reviewAdapter;

    DatabaseReference reviewsRef;

    String serviceId;
    String serviceOwnerId;
    String serviceImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        image = findViewById(R.id.detailImage);
        name = findViewById(R.id.detailName);
        company = findViewById(R.id.detailCompany);
        address = findViewById(R.id.detailAddress);
        phone = findViewById(R.id.detailPhone);
        description = findViewById(R.id.detailDescription);

        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);

        btnSendReview = findViewById(R.id.btnSendReview);
        btnDeleteService = findViewById(R.id.btnDeleteService);
        btnEditService = findViewById(R.id.btnEditService);

        btnCopyAddress = findViewById(R.id.btnCopyAddress);
        btnCopyPhone = findViewById(R.id.btnCopyPhone);

        rvReviews = findViewById(R.id.rvReviews);

        serviceId = getIntent().getStringExtra("id");

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(
                reviewList,
                "service",
                serviceId,
                this
        );

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        String img = getIntent().getStringExtra("image");
        String n = getIntent().getStringExtra("name");
        String comp = getIntent().getStringExtra("company");
        String addr = getIntent().getStringExtra("address");
        String ph = getIntent().getStringExtra("phone");
        String desc = getIntent().getStringExtra("description");

        serviceOwnerId = getIntent().getStringExtra("userId");

        // MAP
        address.setOnClickListener(v -> {
            Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(address.getText().toString()));
            startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, uri));
        });

        image.setOnClickListener(v -> {

            String finalImage = (serviceImage != null && !serviceImage.isEmpty())
                    ? serviceImage
                    : img;

            if (finalImage == null || finalImage.isEmpty()) return;

            Intent intent = new Intent(this, FullImageActivity.class);
            intent.putExtra("image", finalImage);
            startActivity(intent);
        });


        phone.setOnClickListener(v -> {

            String raw = phone.getText().toString().trim();
            String number = raw.replaceAll("[^0-9+]", "");
            String cleanNumber = number.replace("+", "");

            // 📞 звонок
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:" + number));

            // 💬 WhatsApp
            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
            whatsappIntent.setData(Uri.parse("https://wa.me/" + cleanNumber));

            // 🟣 Viber
            Intent viberIntent = new Intent(Intent.ACTION_VIEW);
            viberIntent.setData(Uri.parse("viber://chat?number=" + cleanNumber));

            // ✈️ Telegram
            Intent telegramIntent = new Intent(Intent.ACTION_VIEW);

            if (number.startsWith("+")) {
                telegramIntent.setData(Uri.parse("tg://resolve?phone=" + cleanNumber));
            } else {
                telegramIntent.setData(Uri.parse("https://t.me/" + raw.replace("@", "")));
            }

            List<Intent> extra = new ArrayList<>();
            extra.add(whatsappIntent);
            extra.add(viberIntent);
            extra.add(telegramIntent);

            Intent chooser = Intent.createChooser(phoneIntent, "Choose action");

            chooser.putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    extra.toArray(new Intent[0])
            );

            startActivity(chooser);
        });

        // COPY
        btnCopyAddress.setOnClickListener(v -> copy(address.getText().toString()));
        btnCopyPhone.setOnClickListener(v -> copy(phone.getText().toString()));

        // EDIT
        btnEditService.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AddServiceActivity.class);

            intent.putExtra("editMode", true);
            intent.putExtra("id", serviceId);

            intent.putExtra("name", name.getText().toString());
            intent.putExtra("company", company.getText().toString());
            intent.putExtra("address", address.getText().toString());
            intent.putExtra("phone", phone.getText().toString());
            intent.putExtra("description", description.getText().toString());
            intent.putExtra("image", serviceImage);
            intent.putExtra("userId", serviceOwnerId);

            startActivity(intent);
        });

        if (serviceId == null) {
            name.setText(n);
            company.setText(comp);
            address.setText(addr != null ? addr : "");
            phone.setText(ph != null ? ph : "");
            description.setText(desc != null ? desc : "");

            if (img != null && !img.isEmpty()) {
                Picasso.get().load(img).into(image);
            }

            updateButtons();
        } else {
            loadFromFirebase(serviceId);
            loadReviews();
        }

        // ADD REVIEW
        btnSendReview.setOnClickListener(v -> {

            String text = etReview.getText().toString().trim();
            float rating = ratingBar.getRating();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null || text.isEmpty() || reviewsRef == null) return;

            String userId = user.getUid();

            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String username = snapshot.child("username").getValue(String.class);
                            if (username == null) username = "User";

                            String id = reviewsRef.push().getKey();
                            if (id == null) return;

                            Review review = new Review(
                                    id,
                                    userId,
                                    username,
                                    text,
                                    rating
                            );

                            reviewsRef.child(id).setValue(review);

                            etReview.setText("");
                            ratingBar.setRating(0);

                            loadReviews();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        });

        // DELETE
        btnDeleteService.setOnClickListener(v -> {

            if (serviceId == null) return;

            new AlertDialog.Builder(this)
                    .setTitle("Delete Service")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", (d, w) -> {

                        FirebaseDatabase.getInstance()
                                .getReference("services")
                                .child(serviceId)
                                .removeValue();

                        FirebaseDatabase.getInstance()
                                .getReference("service_reviews")
                                .child(serviceId)
                                .removeValue();

                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadFromFirebase(String id) {

        FirebaseDatabase.getInstance()
                .getReference("services")
                .child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Service s = snapshot.getValue(Service.class);
                        if (s == null) return;

                        name.setText(s.name);
                        company.setText(s.company != null ? s.company : "");
                        address.setText(s.address != null ? s.address : "");
                        phone.setText(s.phone != null ? s.phone : "");
                        description.setText(s.description != null ? s.description : "");

                        serviceOwnerId = s.userId;
                        serviceImage = s.imageUrl;

                        if (serviceImage != null && !serviceImage.isEmpty()) {
                            Picasso.get().load(serviceImage).into(image);
                        }

                        updateButtons();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadReviews() {

        reviewsRef = FirebaseDatabase.getInstance()
                .getReference("service_reviews")
                .child(serviceId);

        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                reviewList.clear();

                float sum = 0;
                int count = 0;

                for (DataSnapshot data : snapshot.getChildren()) {

                    Review r = data.getValue(Review.class);

                    if (r != null) {
                        r.reviewId = data.getKey();
                        reviewList.add(r);

                        sum += r.rating;
                        count++;
                    }
                }

                float avg = count > 0 ? sum / count : 0f;

                reviewAdapter.notifyDataSetChanged();
                ratingBar.setRating(avg);

                FirebaseDatabase.getInstance()
                        .getReference("services")
                        .child(serviceId)
                        .child("averageRating")
                        .setValue(avg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateButtons() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        boolean isOwner =
                user != null &&
                        serviceOwnerId != null &&
                        user.getUid().equals(serviceOwnerId);

        btnDeleteService.setVisibility(isOwner ? android.view.View.VISIBLE : android.view.View.GONE);
        btnEditService.setVisibility(isOwner ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void copy(String text) {

        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        clipboard.setPrimaryClip(ClipData.newPlainText("text", text));

        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReviewsChanged() {
        loadReviews();
    }
}