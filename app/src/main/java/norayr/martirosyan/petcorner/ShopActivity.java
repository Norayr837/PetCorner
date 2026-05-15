package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SearchView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;

public class ShopActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShopAdapter adapter;

    private ArrayList<Shop> shopList;
    private ArrayList<Shop> filteredList;

    private DatabaseReference databaseReference;

    private ImageButton btnAdd;
    private ImageButton btnSearch;
    private ImageButton btnFilter;
    private SearchView searchView;

    private LinearLayout btnServices, btnProfile, btnForum;
    private LinearLayout dropdownMenu;

    private String currentType = "shops";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        recyclerView = findViewById(R.id.shopsRecycler);
        btnAdd = findViewById(R.id.btnAddShop);
        btnSearch = findViewById(R.id.btnSearch);
        btnFilter = findViewById(R.id.btnFilter);
        searchView = findViewById(R.id.searchView);

        btnServices = findViewById(R.id.btnServices);
        btnProfile = findViewById(R.id.btnProfile);
        btnForum = findViewById(R.id.btnForum);

        dropdownMenu = findViewById(R.id.dropdownMenu);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        shopList = new ArrayList<>();
        filteredList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference(currentType);

        loadShops();

        // 🔥 ACTIVE TAB
        setActiveTab(btnServices);

        // ================= ANNOUNCEMENTS MENU =================
        btnServices.setOnClickListener(v -> {
            dropdownMenu.setVisibility(
                    dropdownMenu.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            );
        });

        findViewById(R.id.btnServicesOption).setOnClickListener(v -> {
            startActivity(new Intent(this, ServicesActivity.class));
        });

        findViewById(R.id.btnShops).setOnClickListener(v -> {
            // уже в Shops → ничего не делаем
        });

        findViewById(R.id.btnVeterinarians).setOnClickListener(v -> {
            startActivity(new Intent(this, VeterinariansActivity.class));
        });

        // ================= NAV =================
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        btnForum.setOnClickListener(v ->
                startActivity(new Intent(this, ForumActivity.class))
        );

        // ================= SEARCH =================
        btnSearch.setOnClickListener(v ->
                searchView.setVisibility(
                        searchView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
                )
        );

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String text) {
                filter(text);
                return true;
            }
        });

        // ================= FILTER =================
        btnFilter.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(this, btnFilter);

            popup.getMenu().add("Default");
            popup.getMenu().add("Newest");
            popup.getMenu().add("Top rated");

            popup.setOnMenuItemClickListener(item -> {

                String t = item.getTitle().toString();

                if (t.equals("Default")) resetList();
                if (t.equals("Newest")) sortByNewest();
                if (t.equals("Top rated")) sortByRating();

                return true;
            });

            popup.show();
        });

        // ================= ADD =================
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddShopActivity.class))
        );

        // ================= BACK BLOCK =================
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // блок назад
                    }
                }
        );
    }

    // ================= ACTIVE TAB =================
    private void setActiveTab(LinearLayout active) {

        btnServices.setBackgroundResource(R.drawable.circle_button);
        btnProfile.setBackgroundResource(R.drawable.circle_button);
        btnForum.setBackgroundResource(R.drawable.circle_button);

        active.setBackgroundResource(R.drawable.circle_button_active);
    }

    // ================= LOAD =================
    private void loadShops() {

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                shopList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    Shop shop = data.getValue(Shop.class);
                    if (shop == null) continue;

                    shop.id = data.getKey();

                    if ("approved".equalsIgnoreCase(shop.status)) {
                        shopList.add(shop);
                    }
                }

                checkUserRoleAndInitAdapter();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void checkUserRoleAndInitAdapter() {

        String uid = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        boolean isAdmin = "admin".equals(snapshot.getValue(String.class));

                        adapter = new ShopAdapter(filteredList, isAdmin);
                        recyclerView.setAdapter(adapter);

                        resetList();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    // ================= FILTER =================
    private void filter(String text) {

        filteredList.clear();

        String q = text.toLowerCase().trim();

        for (Shop s : shopList) {

            if ((s.name != null && s.name.toLowerCase().contains(q)) ||
                    (s.owner != null && s.owner.toLowerCase().contains(q))) {

                filteredList.add(s);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void resetList() {
        filteredList.clear();
        filteredList.addAll(shopList);
        adapter.notifyDataSetChanged();
    }

    private void sortByNewest() {
        Collections.sort(filteredList, (a, b) ->
                Long.compare(b.timestamp, a.timestamp));
        adapter.notifyDataSetChanged();
    }

    private void sortByRating() {
        Collections.sort(filteredList, (a, b) ->
                Float.compare(b.averageRating, a.averageRating));
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onBackPressed() {

        // если меню открыто — просто закрываем
        if (dropdownMenu.getVisibility() == View.VISIBLE) {
            dropdownMenu.setVisibility(View.GONE);
            return;
        }

        // переход в профиль
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }
}