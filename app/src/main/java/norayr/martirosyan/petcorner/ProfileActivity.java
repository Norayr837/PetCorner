package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.ErrorInfo;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    TextView tvUsername;
    ImageView profileImage, btnGear;

    LinearLayout settingsContainer;

    TextView btnLanguages, btnPrivacy, btnAbout;

    Button btnAdminPanel;

    FirebaseUser currentUser;
    String userId;

    private static final int PICK_IMAGE = 1;
    private long lastBackPressedTime = 0;

    // NAV BUTTONS
    LinearLayout btnServices, btnProfile, btnForum;

    LinearLayout dropdownMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        userId = currentUser.getUid();

        // INIT VIEWS
        tvUsername = findViewById(R.id.tvUsername);
        profileImage = findViewById(R.id.profileImage);
        btnGear = findViewById(R.id.btnGear);
        settingsContainer = findViewById(R.id.settingsContainer);

        btnLanguages = findViewById(R.id.btnLanguages);
        btnPrivacy = findViewById(R.id.btnPrivacy);
        btnAbout = findViewById(R.id.btnAbout);

        btnAdminPanel = findViewById(R.id.btnAdminPanel);

        // NAV
        btnServices = findViewById(R.id.btnServices);
        btnProfile = findViewById(R.id.btnProfile);
        btnForum = findViewById(R.id.btnForum);

        dropdownMenu = findViewById(R.id.dropdownMenu);

        // ================= SETTINGS =================
        btnGear.setOnClickListener(v -> {
            settingsContainer.setVisibility(
                    settingsContainer.getVisibility() == View.GONE
                            ? View.VISIBLE
                            : View.GONE
            );
        });

        btnLanguages.setOnClickListener(v -> {
            startActivity(new Intent(this, LanguagesActivity.class));
            settingsContainer.setVisibility(View.GONE);
        });

        btnPrivacy.setOnClickListener(v -> {
            startActivity(new Intent(this, PrivacyPolicyActivity.class));
            settingsContainer.setVisibility(View.GONE);
        });

        btnAbout.setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
            settingsContainer.setVisibility(View.GONE);
        });

        // ================= DROPDOWN =================
        btnServices.setOnClickListener(v -> {
            dropdownMenu.setVisibility(
                    dropdownMenu.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            );
        });

        findViewById(R.id.btnServicesOption).setOnClickListener(v -> {
            startActivity(new Intent(this, ServicesActivity.class));
        });

        findViewById(R.id.btnShops).setOnClickListener(v -> {
            startActivity(new Intent(this, ShopActivity.class));
        });

        findViewById(R.id.btnVeterinarians).setOnClickListener(v -> {
            startActivity(new Intent(this, VeterinariansActivity.class));
        });

        // ================= OTHER NAV =================

        btnProfile.setOnClickListener(v -> {
            // уже тут → ничего не делаем
            setActiveTab(btnProfile);
        });

        btnForum.setOnClickListener(v -> {
            startActivity(new Intent(this, ForumActivity.class));
        });

        // ================= OTHER BUTTONS =================
        findViewById(R.id.btnPersonalInfo).setOnClickListener(v ->
                startActivity(new Intent(this, PersonalInformation.class)));

        findViewById(R.id.btnFavoriteAnnouncements).setOnClickListener(v ->
                startActivity(new Intent(this, FavoritesActivity.class)));

        findViewById(R.id.btnMyAnnouncements).setOnClickListener(v ->
                startActivity(new Intent(this, MyAnnouncementsActivity.class)));

        findViewById(R.id.btnAnnouncementsMap).setOnClickListener(v ->
                startActivity(new Intent(this, MapActivity.class)));

        findViewById(R.id.btnMyNotes).setOnClickListener(v ->
                startActivity(new Intent(this, NotesActivity.class)));

        // ================= PROFILE IMAGE =================
        profileImage.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, PICK_IMAGE);
        });

        // ================= LOAD USERNAME =================
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("username")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        tvUsername.setText(name != null ? name : "User");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });

        // ================= LOAD IMAGE =================
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("imageUrl")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String url = snapshot.getValue(String.class);

                        if (url != null && !url.isEmpty()) {
                            Picasso.get().load(url).into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.ic_launcher_foreground);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });

        // ================= ADMIN CHECK =================
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        String role = snapshot.getValue(String.class);

                        btnAdminPanel.setVisibility("admin".equals(role)
                                ? View.VISIBLE
                                : View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });

        btnAdminPanel.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, AdminActivity.class)));

        // 🔥 ВАЖНО: всегда Profile активный тут
        setActiveTab(btnProfile);
    }

    // ================= NAV ACTIVE =================
    private void setActiveTab(LinearLayout active) {

        btnServices.setBackgroundResource(R.drawable.circle_button);
        btnProfile.setBackgroundResource(R.drawable.circle_button);
        btnForum.setBackgroundResource(R.drawable.circle_button);

        active.setBackgroundResource(R.drawable.circle_button_active);
    }

    // ================= IMAGE UPLOAD =================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            Uri imageUri = data.getData();
            if (imageUri == null) return;

            MediaManager.get().upload(imageUri)
                    .unsigned("android_preset")
                    .callback(new UploadCallback() {

                        @Override
                        public void onSuccess(String requestId, Map resultData) {

                            String imageUrl = resultData.get("secure_url").toString();

                            FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(userId)
                                    .child("imageUrl")
                                    .setValue(imageUrl);

                            runOnUiThread(() ->
                                    Picasso.get().load(imageUrl).into(profileImage)
                            );
                        }

                        @Override public void onStart(String requestId) {}
                        @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Toast.makeText(ProfileActivity.this,
                                    error.getDescription(),
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override public void onReschedule(String requestId, ErrorInfo error) {}
                    })
                    .dispatch();
        }
    }
    @Override
    public void onBackPressed() {

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastBackPressedTime < 2000) {
            // second press within 2 seconds → exit app
            finishAffinity();
            return;
        }

        lastBackPressedTime = currentTime;

        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
    }
}