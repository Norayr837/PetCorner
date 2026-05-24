package norayr.martirosyan.petcorner;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MyAnnouncementsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button btnServices, btnShops, btnVets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_announcements);

        recyclerView = findViewById(R.id.myRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnServices = findViewById(R.id.btnServices);
        btnShops = findViewById(R.id.btnShops);
        btnVets = findViewById(R.id.btnVets);

        btnServices.setOnClickListener(v -> loadMyAnnouncements("service"));
        btnShops.setOnClickListener(v -> loadMyAnnouncements("shop"));
        btnVets.setOnClickListener(v -> loadMyAnnouncements("veterinarian"));

        loadMyAnnouncements("service");
    }

    private void loadMyAnnouncements(String type) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref;


        if (type.equals("service")) {
            ref = FirebaseDatabase.getInstance().getReference("services");
        } else if (type.equals("shop")) {
            ref = FirebaseDatabase.getInstance().getReference("shops");
        } else {
            ref = FirebaseDatabase.getInstance().getReference("veterinarians");
        }

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (type.equals("service")) {

                    List<Service> list = new ArrayList<>();

                    for (DataSnapshot d : snapshot.getChildren()) {

                        Service s = d.getValue(Service.class);

                        if (s != null && s.userId != null && s.userId.equals(userId)) {
                            s.id = d.getKey();
                            list.add(s);
                        }
                    }

                    recyclerView.setAdapter(new ServiceAdapter(list));
                }

                else if (type.equals("shop")) {

                    List<Shop> list = new ArrayList<>();

                    for (DataSnapshot d : snapshot.getChildren()) {

                        Shop s = d.getValue(Shop.class);

                        if (s != null && s.userId != null && s.userId.equals(userId)) {
                            s.id = d.getKey();
                            list.add(s);
                        }
                    }

                    recyclerView.setAdapter(new ShopAdapter(list));
                }

                else if (type.equals("veterinarian")) {

                    List<Veterinarian> list = new ArrayList<>();

                    for (DataSnapshot d : snapshot.getChildren()) {

                        Veterinarian v = d.getValue(Veterinarian.class);

                        if (v != null && v.userId != null && v.userId.equals(userId)) {
                            v.id = d.getKey();
                            list.add(v);
                        }
                    }

                    recyclerView.setAdapter(new VeterinarianAdapter(list));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}