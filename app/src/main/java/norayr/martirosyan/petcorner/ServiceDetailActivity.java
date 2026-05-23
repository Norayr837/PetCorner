package norayr.martirosyan.petcorner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

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

    LinearLayout reviewContainer;

    Button btnSendReview, btnDeleteService, btnEditService, btnSendMessage;

    private LinearLayout btnAnnouncements, btnProfile, btnForum;

    private LinearLayout dropdownMenu;

    private Button btnServices, btnShops, btnVeterinarians;

    ImageButton btnMoreOptions;

    LinearLayout optionsMenu;

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

        initViews();
        initBottomMenu();

        ScrollView scrollView = findViewById(R.id.scrollView);
        LinearLayout bottomMenu = findViewById(R.id.bottomMenu);

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
        btnSendMessage = findViewById(R.id.btnSendMessage);

        btnCopyAddress = findViewById(R.id.btnCopyAddress);
        btnCopyPhone = findViewById(R.id.btnCopyPhone);

        rvReviews = findViewById(R.id.rvReviews);

        serviceId = getIntent().getStringExtra("id");
        serviceOwnerId = getIntent().getStringExtra("userId");

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

        // MAP
        address.setOnClickListener(v -> {
            Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(address.getText().toString()));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
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

        // PHONE ACTIONS
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
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extra.toArray(new Intent[0]));

            startActivity(chooser);
        });

        // COPY
        btnCopyAddress.setOnClickListener(v -> copy(address.getText().toString()));
        btnCopyPhone.setOnClickListener(v -> copy(phone.getText().toString()));

        // EDIT
        btnEditService.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddServiceActivity.class);

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

        // SEND MESSAGE 💬
        btnSendMessage.setOnClickListener(v -> {

            String currentUserId = FirebaseAuth.getInstance().getUid();
            String receiverId = serviceOwnerId;

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


                    Intent intent = new Intent(ServiceDetailActivity.this, PrivateChatActivity.class);
                    intent.putExtra("chatId", chatId);
                    intent.putExtra("receiverId", receiverId); // 🔥 ВОТ ЭТО
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
        // LOAD DATA
        if (serviceId == null) {

            name.setText(n);
            company.setText(comp);
            address.setText(addr != null ? addr : "");
            phone.setText(ph != null ? ph : "");
            description.setText(desc != null ? desc : "");

            serviceOwnerId = getIntent().getStringExtra("userId");

            if (img != null && !img.isEmpty()) {
                Picasso.get().load(img).into(image);
            }

            updateButtons();
        } else {
            loadFromFirebase(serviceId);
            loadReviews();
        }


        btnSendReview.setOnClickListener(v -> {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;

            String currentUserId = user.getUid();


            if (serviceOwnerId != null && serviceOwnerId.equals(currentUserId)) {
                Toast.makeText(this, "You cannot review your own service", Toast.LENGTH_SHORT).show();
                return;
            }

            String text = etReview.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (text.isEmpty() || reviewsRef == null) return;

            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String username = snapshot.child("username").getValue(String.class);
                            if (username == null) username = "User";

                            String id = reviewsRef.push().getKey();
                            if (id == null) return;

                            Review review = new Review(
                                    id,
                                    currentUserId,
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

    private void initViews() {

        btnAnnouncements = findViewById(R.id.btnServices);
        btnProfile = findViewById(R.id.btnProfile);
        btnForum = findViewById(R.id.btnForum);

        dropdownMenu = findViewById(R.id.dropdownMenu);

        btnServices = findViewById(R.id.btnServicesOption);
        btnShops = findViewById(R.id.btnShops);
        btnVeterinarians = findViewById(R.id.btnVeterinarians);
        reviewContainer = findViewById(R.id.reviewContainer);


        // ❗ ВАЖНО: используем поля класса, а не новые переменные
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


    private String getChatId(String user1, String user2) {
        if (user1.compareTo(user2) < 0) {
            return user1 + "_" + user2;
        } else {
            return user2 + "_" + user1;
        }
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

        btnDeleteService.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        btnEditService.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        btnSendMessage.setVisibility(!isOwner ? View.VISIBLE : View.GONE);
        reviewContainer.setVisibility(isOwner ? View.GONE : View.VISIBLE);

        btnMoreOptions.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        if (!isOwner) {
            optionsMenu.setVisibility(View.GONE);
        }
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