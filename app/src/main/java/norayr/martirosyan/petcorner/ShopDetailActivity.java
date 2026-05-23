package norayr.martirosyan.petcorner;

import android.content.*;
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

public class ShopDetailActivity extends AppCompatActivity
        implements ReviewAdapter.ReviewUpdateListener {

    ImageView image;
    TextView name, address, phone, description, ownerName;

    LinearLayout reviewContainer;

    RatingBar ratingBar;
    EditText etReview;

    Button btnSendReview, btnDeleteShop, btnEditShop;
    ImageButton btnCopyAddress, btnCopyPhone;

    Button btnSendMessageShop;

    RecyclerView rvReviews;

    ScrollView scrollView;

    LinearLayout bottomMenu;

    List<Review> reviewList;
    ReviewAdapter reviewAdapter;

    DatabaseReference reviewsRef;

    String shopId;
    String shopOwnerId;
    String shopImage;

    private LinearLayout btnAnnouncements, btnProfile, btnForum;

    private LinearLayout dropdownMenu;

    private Button btnServices, btnShops, btnVeterinarians;

    ImageButton btnMoreOptions;

    LinearLayout optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        init();
        initBottomMenu();
        checkIntent();
        initReviews();
        initClicks();

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
        address = findViewById(R.id.detailAddress);
        phone = findViewById(R.id.detailPhone);
        description = findViewById(R.id.detailDescription);
        ownerName = findViewById(R.id.detailOwnerName);

        reviewContainer = findViewById(R.id.reviewContainer);

        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);

        btnSendReview = findViewById(R.id.btnSendReview);
        btnDeleteShop = findViewById(R.id.btnDeleteShop);
        btnEditShop = findViewById(R.id.btnEditShop);

        btnCopyAddress = findViewById(R.id.btnCopyAddress);
        btnCopyPhone = findViewById(R.id.btnCopyPhone);

        rvReviews = findViewById(R.id.rvReviews);
        btnSendMessageShop = findViewById(R.id.btnSendMessageShop);

        scrollView = findViewById(R.id.scrollView);
        bottomMenu = findViewById(R.id.bottomMenu);

        btnAnnouncements = findViewById(R.id.btnServices);
        btnProfile = findViewById(R.id.btnProfile);
        btnForum = findViewById(R.id.btnForum);

        dropdownMenu = findViewById(R.id.dropdownMenu);

        btnServices = findViewById(R.id.btnServicesOption);
        btnShops = findViewById(R.id.btnShops);
        btnVeterinarians = findViewById(R.id.btnVeterinarians);

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

        shopId = getIntent().getStringExtra("id");

        if (shopId == null || shopId.isEmpty()) {
            Toast.makeText(this, "Error: shop not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void initReviews() {

        reviewList = new ArrayList<>();

        reviewAdapter = new ReviewAdapter(
                reviewList,
                "shop",
                shopId,
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

            String finalImage = shopImage;

            if (finalImage == null || finalImage.isEmpty()) {
                Toast.makeText(this, "Image not loaded", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, FullImageActivity.class);
            intent.putExtra("image", finalImage);
            startActivity(intent);
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

        btnSendMessageShop.setOnClickListener(v -> {

            String currentUserId = FirebaseAuth.getInstance().getUid();
            String receiverId = shopOwnerId;

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

                    Intent intent = new Intent(ShopDetailActivity.this, PrivateChatActivity.class);
                    intent.putExtra("chatId", chatId);
                    intent.putExtra("receiverId", receiverId);
                    startActivity(intent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        btnCopyAddress.setOnClickListener(v -> copy(address.getText().toString()));
        btnCopyPhone.setOnClickListener(v -> copy(phone.getText().toString()));

        btnEditShop.setOnClickListener(v -> {

            Intent intent = new Intent(this, AddShopActivity.class);

            intent.putExtra("editMode", true);
            intent.putExtra("id", shopId);

            intent.putExtra("name", name.getText().toString());
            intent.putExtra("address", address.getText().toString());
            intent.putExtra("phone", phone.getText().toString());
            intent.putExtra("description", description.getText().toString());

            intent.putExtra("image", shopImage);
            intent.putExtra("userId", shopOwnerId);

            intent.putExtra("owner", ownerName.getText().toString());

            startActivity(intent);
        });

        btnSendReview.setOnClickListener(v -> addReview());
        btnDeleteShop.setOnClickListener(v -> deleteShop());
    }


    private void loadFromFirebase() {

        FirebaseDatabase.getInstance()
                .getReference("shops")
                .child(shopId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            finish();
                            return;
                        }

                        Shop s = snapshot.getValue(Shop.class);
                        if (s == null) return;

                        name.setText(s.name);
                        address.setText(s.address);
                        phone.setText(s.phone);
                        description.setText(s.description);

                        shopOwnerId = s.userId;
                        shopImage = s.imageUrl;

                        ownerName.setText(s.owner != null ? s.owner : "");

                        if (shopImage != null && !shopImage.isEmpty()) {
                            Picasso.get().load(shopImage).into(image);
                        }

                        updateButtons();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    private void loadReviews() {

        reviewsRef = FirebaseDatabase.getInstance()
                .getReference("shop_reviews")
                .child(shopId);

        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                        .getReference("shops")
                        .child(shopId)
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

        String id = reviewsRef.push().getKey();
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


    private void deleteShop() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Shop")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (d, w) -> {

                    FirebaseDatabase.getInstance().getReference("shops")
                            .child(shopId).removeValue();

                    FirebaseDatabase.getInstance().getReference("shop_reviews")
                            .child(shopId).removeValue();

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
                        shopOwnerId != null &&
                        user.getUid().equals(shopOwnerId);

        btnDeleteShop.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        btnEditShop.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        btnSendMessageShop.setVisibility(isOwner ? View.GONE : View.VISIBLE);


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