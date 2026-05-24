package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class ForumActivity extends AppCompatActivity {


    LinearLayout btnMain, btnServicesCard, btnVetCard, btnShopsCard;


    LinearLayout btnAnnouncements, btnProfile, btnForum;


    LinearLayout dropdownMenu;
    Button btnServices, btnShops, btnVeterinarians;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);


        btnMain = findViewById(R.id.btnMain);
        btnServicesCard = findViewById(R.id.btnServicesForum);
        btnVetCard = findViewById(R.id.btnVeterinariansForum);
        btnShopsCard = findViewById(R.id.btnShopsForum);

        btnMain.setOnClickListener(v -> openChat("main"));
        btnServicesCard.setOnClickListener(v -> openChat("services"));
        btnVetCard.setOnClickListener(v -> openChat("veterinarians"));
        btnShopsCard.setOnClickListener(v -> openChat("shops"));


        dropdownMenu = findViewById(R.id.dropdownMenu);

        btnServices = findViewById(R.id.btnServicesOption);
        btnShops = findViewById(R.id.btnShops);
        btnVeterinarians = findViewById(R.id.btnVeterinarians);

        btnServices.setOnClickListener(v -> openServices());
        btnShops.setOnClickListener(v -> openShops());
        btnVeterinarians.setOnClickListener(v -> openVets());


        btnAnnouncements = findViewById(R.id.btnAnnouncements);
        btnProfile = findViewById(R.id.btnProfile);
        btnForum = findViewById(R.id.btnForum);

        setActiveBottomNav();



        btnAnnouncements.setOnClickListener(v -> toggleDropdown(v));

        btnProfile.setOnClickListener(v -> {
            if (!isCurrent(ProfileActivity.class)) {
                startActivity(new Intent(this, ProfileActivity.class));
            }
        });

        btnForum.setOnClickListener(v -> {
            setActiveBottomNav();
        });
    }


    private void openChat(String category) {
        Intent intent = new Intent(ForumActivity.this, ChatActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    private void openServices() {
        dropdownMenu.setVisibility(View.GONE);
        startActivity(new Intent(this, ServicesActivity.class));
    }

    private void openShops() {
        dropdownMenu.setVisibility(View.GONE);
        startActivity(new Intent(this, ShopActivity.class));
    }

    private void openVets() {
        dropdownMenu.setVisibility(View.GONE);
        startActivity(new Intent(this, VeterinariansActivity.class));
    }


    private void toggleDropdown(View anchor) {

        if (dropdownMenu.getVisibility() == View.GONE) {

            dropdownMenu.setVisibility(View.INVISIBLE);

            dropdownMenu.post(() -> {

                dropdownMenu.measure(
                        View.MeasureSpec.UNSPECIFIED,
                        View.MeasureSpec.UNSPECIFIED
                );

                int[] location = new int[2];
                anchor.getLocationInWindow(location);

                dropdownMenu.setX(location[0]);

                float y = location[1] - dropdownMenu.getMeasuredHeight();


                if (y < 0) {
                    y = location[1] + anchor.getHeight();
                }

                dropdownMenu.setY(y);

                dropdownMenu.setVisibility(View.VISIBLE);
            });

        } else {
            dropdownMenu.setVisibility(View.GONE);
        }
    }

    private void setActiveBottomNav() {

        btnAnnouncements.setAlpha(1f);
        btnProfile.setAlpha(1f);
        btnForum.setAlpha(1f);

        btnAnnouncements.setBackgroundResource(R.drawable.circle_button);
        btnProfile.setBackgroundResource(R.drawable.circle_button);
        btnForum.setBackgroundResource(R.drawable.circle_button_active);

        btnForum.setAlpha(0.6f);
    }

    private boolean isCurrent(Class<?> cls) {
        return getClass().equals(cls);
    }
    @Override
    public void onBackPressed() {


        if (dropdownMenu.getVisibility() == View.VISIBLE) {
            dropdownMenu.setVisibility(View.GONE);
            return;
        }


        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }
}