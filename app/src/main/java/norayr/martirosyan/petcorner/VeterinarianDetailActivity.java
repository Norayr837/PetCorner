package norayr.martirosyan.petcorner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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

public class VeterinarianDetailActivity extends AppCompatActivity
        implements ReviewAdapter.ReviewUpdateListener {

    ImageView image;
    TextView name, clinic, address, phone, description;

    RatingBar ratingBar;
    EditText etReview;

    Button btnSendReview, btnDeleteVet, btnEditVet;
    ImageButton btnCopyAddress, btnCopyPhone;

    RecyclerView rvReviews;

    List<Review> reviewList;
    ReviewAdapter reviewAdapter;

    DatabaseReference reviewsRef;

    String vetId;
    String vetOwnerId;
    String vetImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veterinarian_detail);

        init();
        checkIntent();
        initReviews();
        initClicks();

        loadFromFirebase();
        loadReviews();
    }

    private void init() {

        image = findViewById(R.id.detailImage);
        name = findViewById(R.id.detailName);
        clinic = findViewById(R.id.detailClinic);
        address = findViewById(R.id.detailAddress);
        phone = findViewById(R.id.detailPhone);
        description = findViewById(R.id.detailDescription);

        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);

        btnSendReview = findViewById(R.id.btnSendReview);
        btnDeleteVet = findViewById(R.id.btnDeleteVet);
        btnEditVet = findViewById(R.id.btnEditVet);

        btnCopyAddress = findViewById(R.id.btnCopyAddress);
        btnCopyPhone = findViewById(R.id.btnCopyPhone);

        rvReviews = findViewById(R.id.rvReviews);
    }

    private void checkIntent() {

        vetId = getIntent().getStringExtra("id");

        if (vetId == null || vetId.isEmpty()) {
            Toast.makeText(this, "Error: vet not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initReviews() {

        reviewList = new ArrayList<>();

        reviewAdapter = new ReviewAdapter(
                reviewList,
                "vet",
                vetId,
                this
        );

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
    }

    private void initClicks() {

        address.setOnClickListener(v -> {
            String addr = address.getText().toString();
            if (addr.isEmpty()) return;

            Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(addr));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        });

        image.setOnClickListener(v -> {

            if (vetImage == null || vetImage.isEmpty()) return;

            Intent intent = new Intent(this, FullImageActivity.class);
            intent.putExtra("image", vetImage);
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

        btnCopyAddress.setOnClickListener(v -> copy(address.getText().toString()));
        btnCopyPhone.setOnClickListener(v -> copy(phone.getText().toString()));

        btnEditVet.setOnClickListener(v -> {

            Intent intent = new Intent(this, AddVeterinarianActivity.class);

            intent.putExtra("editMode", true);
            intent.putExtra("id", vetId);

            intent.putExtra("name", name.getText().toString());
            intent.putExtra("clinic", clinic.getText().toString());
            intent.putExtra("address", address.getText().toString());
            intent.putExtra("phone", phone.getText().toString());
            intent.putExtra("description", description.getText().toString());
            intent.putExtra("image", vetImage);
            intent.putExtra("userId", vetOwnerId);

            startActivity(intent);
        });

        btnSendReview.setOnClickListener(v -> addReview());

        btnDeleteVet.setOnClickListener(v -> deleteVet());
    }

    private void loadFromFirebase() {

        FirebaseDatabase.getInstance()
                .getReference("veterinarians")
                .child(vetId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Veterinarian v = snapshot.getValue(Veterinarian.class);
                        if (v == null) return;

                        name.setText(v.name);
                        clinic.setText(v.clinic);
                        address.setText(v.address);
                        phone.setText(v.phone);
                        description.setText(v.description);

                        vetOwnerId = v.userId;
                        vetImage = v.imageUrl;

                        if (vetImage != null && !vetImage.isEmpty()) {
                            Picasso.get().load(vetImage).into(image);
                        }

                        updateButtons();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadReviews() {

        reviewsRef = FirebaseDatabase.getInstance()
                .getReference("vet_reviews")
                .child(vetId);

        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                reviewList.clear();

                float sum = 0;
                int count = 0;

                for (DataSnapshot data : snapshot.getChildren()) {

                    Review r = data.getValue(Review.class);
                    if (r == null) continue;

                    r.reviewId = data.getKey();
                    reviewList.add(r);

                    sum += r.rating;
                    count++;
                }

                float avg = count > 0 ? sum / count : 0f;

                reviewAdapter.notifyDataSetChanged();
                ratingBar.setRating(avg);

                FirebaseDatabase.getInstance()
                        .getReference("veterinarians")
                        .child(vetId)
                        .child("averageRating")
                        .setValue(avg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addReview() {

        String text = etReview.getText().toString().trim();
        float rating = ratingBar.getRating();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || text.isEmpty()) return;

        String id = FirebaseDatabase.getInstance()
                .getReference("vet_reviews")
                .child(vetId)
                .push()
                .getKey();

        if (id == null) return;

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {

                        String username = snap.child("username").getValue(String.class);

                        Review r = new Review(
                                id,
                                user.getUid(),
                                username != null ? username : "User",
                                text,
                                rating
                        );

                        reviewsRef.child(id).setValue(r);

                        etReview.setText("");
                        ratingBar.setRating(0);

                        loadReviews();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void deleteVet() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Veterinarian")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (d, w) -> {

                    FirebaseDatabase.getInstance()
                            .getReference("veterinarians")
                            .child(vetId)
                            .removeValue();

                    FirebaseDatabase.getInstance()
                            .getReference("vet_reviews")
                            .child(vetId)
                            .removeValue();

                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateButtons() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        boolean isOwner =
                user != null &&
                        vetOwnerId != null &&
                        user.getUid().equals(vetOwnerId);

        btnDeleteVet.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        btnEditVet.setVisibility(isOwner ? View.VISIBLE : View.GONE);
    }

    private void copy(String text) {

        if (text == null || text.isEmpty()) return;

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