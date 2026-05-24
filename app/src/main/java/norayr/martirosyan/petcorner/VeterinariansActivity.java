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

public class VeterinariansActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VeterinarianAdapter adapter;

    private ArrayList<Veterinarian> vetList;
    private ArrayList<Veterinarian> filteredList;

    private DatabaseReference databaseReference;

    private ImageButton btnAdd;
    private ImageButton btnSearch;
    private ImageButton btnFilter;
    private SearchView searchView;


    private LinearLayout btnServices, btnProfile, btnForum;


    private LinearLayout dropdownMenu;
    private Button btnServicesOption, btnShops, btnVeterinarians;

    private String currentType = "veterinarians";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veterinarians);


        String type = getIntent().getStringExtra("type");
        if (type != null) currentType = type;


        recyclerView = findViewById(R.id.vetsRecycler);
        btnAdd = findViewById(R.id.btnAddVeterinarian);
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

        vetList = new ArrayList<>();
        filteredList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference(currentType);

        loadVets();



        setActiveTab(btnServices);

        btnServices.setOnClickListener(v -> {
            startActivity(new Intent(this, ServicesActivity.class));
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        btnForum.setOnClickListener(v -> {
            startActivity(new Intent(this, ForumActivity.class));
        });



        btnServices.setOnClickListener(v ->
                dropdownMenu.setVisibility(
                        dropdownMenu.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
                )
        );

        btnServicesOption.setOnClickListener(v -> {
            startActivity(new Intent(this, ServicesActivity.class));
        });

        btnShops.setOnClickListener(v -> {
            startActivity(new Intent(this, ShopActivity.class));
        });

        btnVeterinarians.setOnClickListener(v -> {

        });



        btnSearch.setOnClickListener(v -> {
            searchView.setVisibility(
                    searchView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            );
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String text) {
                filter(text);
                return true;
            }
        });



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



        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddVeterinarianActivity.class))
        );



        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finishAffinity();
                    }
                }
        );
    }



    private void setActiveTab(LinearLayout active) {

        btnServices.setBackgroundResource(R.drawable.circle_button);
        btnProfile.setBackgroundResource(R.drawable.circle_button);
        btnForum.setBackgroundResource(R.drawable.circle_button);

        active.setBackgroundResource(R.drawable.circle_button_active);
    }



    private void loadVets() {

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                vetList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    Veterinarian v = data.getValue(Veterinarian.class);
                    if (v == null) continue;

                    v.id = data.getKey();

                    if ("approved".equalsIgnoreCase(v.status)) {
                        vetList.add(v);
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

                        adapter = new VeterinarianAdapter(filteredList, isAdmin);
                        recyclerView.setAdapter(adapter);

                        resetList();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                        adapter = new VeterinarianAdapter(filteredList, false);
                        recyclerView.setAdapter(adapter);

                        resetList();
                    }
                });
    }



    private void filter(String text) {

        filteredList.clear();

        String q = text.toLowerCase().trim();

        for (Veterinarian v : vetList) {

            if ((v.name != null && v.name.toLowerCase().contains(q)) ||
                    (v.clinic != null && v.clinic.toLowerCase().contains(q)) ||
                    (v.address != null && v.address.toLowerCase().contains(q)) ||
                    (v.phone != null && v.phone.toLowerCase().contains(q))) {

                filteredList.add(v);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void resetList() {
        filteredList.clear();
        filteredList.addAll(vetList);
        adapter.notifyDataSetChanged();
    }

    private void sortByNewest() {
        Collections.sort(filteredList, (a, b) ->
                Long.compare(b.timestamp, a.timestamp));
        adapter.notifyDataSetChanged();
    }

    private void sortByRating() {
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