package norayr.martirosyan.petcorner;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class ApprovedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;

    private ArrayList<Service> list = new ArrayList<>();

    private DatabaseReference rootRef;
    private DatabaseReference servicesRef;
    private DatabaseReference shopsRef;
    private DatabaseReference vetsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approved);

        recyclerView = findViewById(R.id.recyclerApproved);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ServiceAdapter(
                list,
                true,              // admin mode ON
                null,              // approve OFF
                this::onDelete     // delete ON
        );

        recyclerView.setAdapter(adapter);

        rootRef = FirebaseDatabase.getInstance().getReference();
        servicesRef = FirebaseDatabase.getInstance().getReference("services");
        shopsRef = FirebaseDatabase.getInstance().getReference("shops");
        vetsRef = FirebaseDatabase.getInstance().getReference("veterinarians");

        loadApproved();
    }

    // ================= LOAD APPROVED =================
    private void loadApproved() {

        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                list.clear();

                // ================= SERVICES =================
                for (DataSnapshot d : snapshot.child("services").getChildren()) {

                    Service s = d.getValue(Service.class);
                    if (s == null) continue;

                    if (!"approved".equals(s.status)) continue;

                    s.id = d.getKey();
                    list.add(s);
                }

                // ================= SHOPS =================
                for (DataSnapshot d : snapshot.child("shops").getChildren()) {

                    Shop shop = d.getValue(Shop.class);
                    if (shop == null) continue;

                    if (!"approved".equals(shop.status)) continue;

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

                // ================= VETERINARIANS =================
                for (DataSnapshot d : snapshot.child("veterinarians").getChildren()) {

                    Veterinarian vet = d.getValue(Veterinarian.class);
                    if (vet == null) continue;

                    if (!"approved".equals(vet.status)) continue;

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

                Toast.makeText(ApprovedActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}