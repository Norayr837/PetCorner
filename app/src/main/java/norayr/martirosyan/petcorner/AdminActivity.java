package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;

    private ArrayList<Service> list = new ArrayList<>();

    private DatabaseReference servicesRef;
    private DatabaseReference shopsRef;
    private DatabaseReference vetsRef;

    // 🔥 UI
    private ImageButton btnSettings;
    private LinearLayout settingsMenu;
    private TextView btnApproved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerView = findViewById(R.id.recyclerPending);

        btnSettings = findViewById(R.id.btnSettings);
        settingsMenu = findViewById(R.id.settingsMenu);
        btnApproved = findViewById(R.id.btnApproved);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ServiceAdapter(
                list,
                true,
                this::onApprove,
                this::onDelete
        );

        recyclerView.setAdapter(adapter);

        servicesRef = FirebaseDatabase.getInstance().getReference("services");
        shopsRef = FirebaseDatabase.getInstance().getReference("shops");
        vetsRef = FirebaseDatabase.getInstance().getReference("veterinarians");

        loadPending();

        // ===== SETTINGS MENU =====

        settingsMenu.setVisibility(View.GONE);

        btnSettings.setOnClickListener(v -> {
            if (settingsMenu.getVisibility() == View.VISIBLE) {
                settingsMenu.setVisibility(View.GONE);
            } else {
                settingsMenu.setVisibility(View.VISIBLE);
            }
        });

        btnApproved.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, ApprovedActivity.class));
            settingsMenu.setVisibility(View.GONE);
        });

        // 🔥 Закрытие меню при клике вне его
        findViewById(android.R.id.content).setOnTouchListener((v, event) -> {
            if (settingsMenu.getVisibility() == View.VISIBLE) {
                settingsMenu.setVisibility(View.GONE);
            }
            return false;
        });

        // 🔥 Чтобы клики внутри меню НЕ закрывали его
        settingsMenu.setOnTouchListener((v, event) -> true);
    }

    // ================= LOAD =================
    private void loadPending() {

        FirebaseDatabase.getInstance().getReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        list.clear();

                        for (DataSnapshot d : snapshot.child("services").getChildren()) {
                            Service s = d.getValue(Service.class);
                            if (s == null) continue;

                            s.id = d.getKey();

                            if ("pending".equals(s.status)) {
                                list.add(s);
                            }
                        }

                        for (DataSnapshot d : snapshot.child("shops").getChildren()) {
                            Shop shop = d.getValue(Shop.class);
                            if (shop == null) continue;
                            if (!"pending".equals(shop.status)) continue;

                            Service s = new Service();

                            s.id = d.getKey();
                            s.name = shop.name;
                            s.company = shop.owner;
                            s.address = shop.address;
                            s.phone = shop.phone;
                            s.description = shop.description;
                            s.imageUrl = shop.imageUrl;
                            s.userId = shop.userId;
                            s.timestamp = shop.timestamp;
                            s.status = shop.status;

                            list.add(s);
                        }

                        for (DataSnapshot d : snapshot.child("veterinarians").getChildren()) {
                            Veterinarian vet = d.getValue(Veterinarian.class);
                            if (vet == null) continue;
                            if (!"pending".equals(vet.status)) continue;

                            Service s = new Service();

                            s.id = d.getKey();
                            s.name = vet.name;
                            s.company = vet.clinic;
                            s.address = vet.address;
                            s.phone = vet.phone;
                            s.description = vet.description;
                            s.imageUrl = vet.imageUrl;
                            s.userId = vet.userId;
                            s.timestamp = vet.timestamp;
                            s.status = vet.status;

                            list.add(s);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    // ================= APPROVE =================
    private void onApprove(Service item) {

        servicesRef.child(item.id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    servicesRef.child(item.id).child("status").setValue("approved");
                } else {
                    shopsRef.child(item.id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshotShop) {

                            if (snapshotShop.exists()) {
                                shopsRef.child(item.id).child("status").setValue("approved");
                            } else {
                                vetsRef.child(item.id).child("status").setValue("approved");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {}
                    });
                }

                Toast.makeText(AdminActivity.this, "Approved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // ================= DELETE =================
    private void onDelete(Service item) {

        servicesRef.child(item.id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    servicesRef.child(item.id).removeValue();
                } else {
                    shopsRef.child(item.id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshotShop) {

                            if (snapshotShop.exists()) {
                                shopsRef.child(item.id).removeValue();
                            } else {
                                vetsRef.child(item.id).removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {}
                    });
                }

                Toast.makeText(AdminActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}