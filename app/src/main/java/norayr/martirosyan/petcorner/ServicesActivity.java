package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;

public class ServicesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;

    private ArrayList<Service> serviceList;
    private ArrayList<Service> filteredList;

    private DatabaseReference databaseReference;

    private ImageButton btnAddService;
    private ImageButton btnSearch;
    private ImageButton btnFilter;
    private SearchView searchView;

    // bottom nav
    private LinearLayout btnServices, btnProfile, btnForum;

    // dropdown
    private LinearLayout dropdownMenu;
    private Button btnServicesOption, btnShops, btnVeterinarians;

    private String currentType = "services";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        // ===== TYPE =====
        String typeFromIntent = getIntent().getStringExtra("type");
        if (typeFromIntent != null) currentType = typeFromIntent;

        // ===== VIEWS =====
        recyclerView = findViewById(R.id.servicesRecycler);
        btnAddService = findViewById(R.id.btnAddService);
        btnSearch = findViewById(R.id.btnSearch);
        btnFilter = findViewById(R.id.btnFilter);
        searchView = findViewById(R.id.searchView);

        btnServices = findViewById(R.id.btnServices);
        btnProfile = findViewById(R.id.btnProfile);
        btnForum = findViewById(R.id.btnForum);

        dropdownMenu = findViewById(R.id.dropdownMenu);
        btnServicesOption = findViewById(R.id.btnServicesOption);
        btnShops = findViewById(R.id.btnShops);
        btnVeterinarians = findViewById(R.id.btnVeterinarians);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        serviceList = new ArrayList<>();
        filteredList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference(currentType);

        loadServices();

        // ================= BOTTOM NAV =================

        setActiveTab(btnServices);

        btnServices.setOnClickListener(v -> {
            // уже тут → ничего не делаем
        });

        btnProfile.setOnClickListener(v -> {
            if (!isCurrent("ProfileActivity")) {
                startActivity(new Intent(this, ProfileActivity.class));
            }
        });

        btnForum.setOnClickListener(v -> {
            if (!isCurrent("ForumActivity")) {
                startActivity(new Intent(this, ForumActivity.class));
            }
        });

        // ================= DROPDOWN =================

        btnServices.setOnClickListener(v ->
                dropdownMenu.setVisibility(
                        dropdownMenu.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
                )
        );

        btnServicesOption.setOnClickListener(v -> {
            dropdownMenu.setVisibility(View.GONE);
        });

        btnShops.setOnClickListener(v -> {
            startActivity(new Intent(this, ShopActivity.class));
        });

        btnVeterinarians.setOnClickListener(v -> {
            startActivity(new Intent(this, VeterinariansActivity.class));
        });

        // ================= SEARCH =================

        btnSearch.setOnClickListener(v -> {
            searchView.setVisibility(
                    searchView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            );
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
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

        btnAddService.setOnClickListener(v ->
                startActivity(new Intent(this, AddServiceActivity.class))
        );

        // ================= BACK BLOCK =================

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finishAffinity(); // ❌ не возвращает назад в stack
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

    private boolean isCurrent(String activityName) {
        return getClass().getSimpleName().equals(activityName);
    }

    // ================= DATA =================

    private void loadServices() {

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                serviceList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    Service s = data.getValue(Service.class);
                    if (s == null) continue;

                    s.id = data.getKey();

                    if ("approved".equalsIgnoreCase(s.status)) {
                        serviceList.add(s);
                    }
                }

                checkUserRoleAndInitAdapter();
            }

            @Override public void onCancelled(DatabaseError error) {}
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

                        adapter = new ServiceAdapter(filteredList, isAdmin);
                        recyclerView.setAdapter(adapter);

                        resetList();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                        adapter = new ServiceAdapter(filteredList, false);
                        recyclerView.setAdapter(adapter);

                        resetList();
                    }
                });
    }

    // ================= FILTER =================

    private void filter(String text) {

        if (adapter == null) return; // 🔥 FIX

        filteredList.clear();

        String q = text.toLowerCase().trim();

        for (Service s : serviceList) {

            if ((s.name != null && s.name.toLowerCase().contains(q)) ||
                    (s.company != null && s.company.toLowerCase().contains(q)) ||
                    (s.address != null && s.address.toLowerCase().contains(q)) ||
                    (s.phone != null && s.phone.toLowerCase().contains(q))) {

                filteredList.add(s);
            }
        }

        adapter.notifyDataSetChanged();
    }
    private void resetList() {

        if (adapter == null) return;

        filteredList.clear();
        filteredList.addAll(serviceList);
        adapter.notifyDataSetChanged();
    }

    private void sortByNewest() {

        if (adapter == null) return;

        Collections.sort(filteredList, (a, b) ->
                Long.compare(b.timestamp, a.timestamp));

        adapter.notifyDataSetChanged();
    }

    private void sortByRating() {

        if (adapter == null) return;

        Collections.sort(filteredList, (a, b) ->
                Double.compare(b.averageRating, a.averageRating));

        adapter.notifyDataSetChanged();
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