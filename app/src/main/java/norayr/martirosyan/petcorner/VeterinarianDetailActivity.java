package norayr.martirosyan.petcorner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
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
    Button btnSendMessageVet;
    ImageButton btnCopyAddress, btnCopyPhone;

    LinearLayout reviewContainer;

    RecyclerView rvReviews;

    ScrollView scrollView;

    LinearLayout bottomMenu;

    List<Review> reviewList;
    ReviewAdapter reviewAdapter;

    DatabaseReference reviewsRef;

    private LinearLayout btnAnnouncements, btnProfile, btnForum;

    private LinearLayout dropdownMenu;

    private Button btnServices, btnShops, btnVeterinarians;

    ImageButton btnMoreOptions;

    LinearLayout optionsMenu;

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
        initBottomMenu();

        loadFromFirebase();
        loadReviews();


        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            int lastScrollY = 0;
            boolean isVisible = true;

            @Override
            public void onScrollChanged() {

                int scrollY = scrollView.getScrollY();

                if (scrollY > lastScrollY + 10 && isVisible) {

                    bottomMenu.animate()
                            .translationY(bottomMenu.getHeight())
                            .setDuration(200);

                    isVisible = false;

                } else if (scrollY < lastScrollY - 10 && !isVisible) {

                    bottomMenu.animate()
                            .translationY(0)
                            .setDuration(200);

                    isVisible = true;
                }

                lastScrollY = scrollY;
            }
        });
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
        btnSendMessageVet = findViewById(R.id.btnSendMessageVet);

        btnAnnouncements = findViewById(R.id.btnServices);
        btnProfile = findViewById(R.id.btnProfile);
        btnForum = findViewById(R.id.btnForum);

        dropdownMenu = findViewById(R.id.dropdownMenu);

        btnServices = findViewById(R.id.btnServicesOption);
        btnShops = findViewById(R.id.btnShops);
        btnVeterinarians = findViewById(R.id.btnVeterinarians);
        reviewContainer = findViewById(R.id.reviewContainer);

        rvReviews = findViewById(R.id.rvReviews);

        scrollView = findViewById(R.id.scrollView);
        bottomMenu = findViewById(R.id.bottomMenu);

        btnMoreOptions = findViewById(R.id.btnMoreOptions);
        optionsMenu = findViewById(R.id.optionsMenu);

        btnMoreOptions.setOnClickListener(v -> {

            if (optionsMenu.getVisibility() == View.VISIBLE) {
                optionsMenu.setVisibility(View.GONE);
            } else {
                optionsMenu.setVisibility(View.VISIBLE);
            }
        });
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

        btnSendMessageVet.setOnClickListener(v -> {

            String currentUserId = FirebaseAuth.getInstance().getUid();
            String receiverId = vetOwnerId;

            if (currentUserId == null || receiverId == null) return;

            String chatId = getChatId(currentUserId, receiverId);

            DatabaseReference chatRef = FirebaseDatabase.getInstance()
                    .getReference("private_chats")
                    .child(chatId);

            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (!snapshot.exists()) {

                        chatRef.child("users").child(currentUserId).setValue(true);
                        chatRef.child("users").child(receiverId).setValue(true);

                        chatRef.child("createdAt").setValue(System.currentTimeMillis());
                        chatRef.child("lastMessage").setValue("");
                        chatRef.child("lastTime").setValue(System.currentTimeMillis());
                    }

                    Intent intent = new Intent(VeterinarianDetailActivity.this, PrivateChatActivity.class);
                    intent.putExtra("chatId", chatId);
                    intent.putExtra("receiverId", receiverId);
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        phone.setOnClickListener(v -> {

            String raw = phone.getText().toString().trim();
            String number = raw.replaceAll("[^0-9+]", "");
            String cleanNumber = number.replace("+", "");


            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:" + number));


            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
            whatsappIntent.setData(Uri.parse("https://wa.me/" + cleanNumber));


            Intent viberIntent = new Intent(Intent.ACTION_VIEW);
            viberIntent.setData(Uri.parse("viber://chat?number=" + cleanNumber));


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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String text = etReview.getText().toString().trim();
        float rating = ratingBar.getRating();


        if (user == null) {
            Toast.makeText(this, "You need to login", Toast.LENGTH_SHORT).show();
            return;
        }

        if (text.isEmpty()) {
            Toast.makeText(this, "Write a review first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reviewsRef == null || vetId == null) {
            return;
        }


        if (vetOwnerId != null && vetOwnerId.equals(user.getUid())) {
            Toast.makeText(this, "You cannot review your own profile", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewId = reviewsRef.push().getKey();
        if (reviewId == null) return;

        String userId = user.getUid();

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {

                        String username = snap.child("username").getValue(String.class);
                        if (username == null || username.isEmpty()) {
                            username = "User";
                        }

                        Review review = new Review(
                                reviewId,
                                userId,
                                username,
                                text,
                                rating
                        );

                        reviewsRef.child(reviewId).setValue(review)
                                .addOnSuccessListener(unused -> {
                                    etReview.setText("");
                                    ratingBar.setRating(0f);
                                    loadReviews();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(VeterinarianDetailActivity.this,
                                                "Failed to send review",
                                                Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VeterinarianDetailActivity.this,
                                "Error loading user data",
                                Toast.LENGTH_SHORT).show();
                    }
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


    private void initBottomMenu() {

        btnAnnouncements.setOnClickListener(v ->
                dropdownMenu.setVisibility(
                        dropdownMenu.getVisibility() == View.VISIBLE
                                ? View.GONE : View.VISIBLE
                )
        );

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        btnForum.setOnClickListener(v ->
                startActivity(new Intent(this, ForumActivity.class))
        );

        btnServices.setOnClickListener(v ->
                startActivity(new Intent(this, ServicesActivity.class))
        );

        btnShops.setOnClickListener(v ->
                startActivity(new Intent(this, ShopActivity.class))
        );

        btnVeterinarians.setOnClickListener(v ->
                startActivity(new Intent(this, VeterinariansActivity.class))
        );
    }

    private void updateButtons() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        boolean isOwner =
                user != null &&
                        vetOwnerId != null &&
                        user.getUid().equals(vetOwnerId);

        btnDeleteVet.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        btnEditVet.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        btnSendMessageVet.setVisibility(isOwner ? View.GONE : View.VISIBLE);


        reviewContainer.setVisibility(isOwner ? View.GONE : View.VISIBLE);
        btnMoreOptions.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        if (!isOwner) {
            optionsMenu.setVisibility(View.GONE);
        }

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

    private String getChatId(String user1, String user2) {
        if (user1.compareTo(user2) < 0) {
            return user1 + "_" + user2;
        } else {
            return user2 + "_" + user1;
        }
    }
}