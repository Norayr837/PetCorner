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

public class FavoritesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button btnServices, btnShops, btnVets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        recyclerView = findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnServices = findViewById(R.id.btnServices);
        btnShops = findViewById(R.id.btnShops);
        btnVets = findViewById(R.id.btnVets);

        btnServices.setOnClickListener(v -> loadFavorites("services"));
        btnShops.setOnClickListener(v -> loadFavorites("shops"));
        btnVets.setOnClickListener(v -> loadFavorites("veterinarians"));


        loadFavorites("services");
    }

    private void loadFavorites(String type) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference likesRef = FirebaseDatabase.getInstance()
                .getReference("likes")
                .child(type);

        DatabaseReference dataRef = FirebaseDatabase.getInstance()
                .getReference(type);

        likesRef.get().addOnSuccessListener(snapshot -> {

            List<String> likedIds = new ArrayList<>();

            for (DataSnapshot item : snapshot.getChildren()) {
                if (item.child(userId).exists()) {
                    likedIds.add(item.getKey());
                }
            }

            dataRef.get().addOnSuccessListener(dataSnapshot -> {

                if (type.equals("services")) {

                    List<Service> list = new ArrayList<>();

                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (likedIds.contains(d.getKey())) {
                            Service s = d.getValue(Service.class);
                            if (s != null) {
                                s.id = d.getKey();
                                list.add(s);
                            }
                        }
                    }

                    recyclerView.setAdapter(new ServiceAdapter(list));
                }

                else if (type.equals("shops")) {

                    List<Shop> list = new ArrayList<>();

                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (likedIds.contains(d.getKey())) {
                            Shop s = d.getValue(Shop.class);
                            if (s != null) {
                                s.id = d.getKey();
                                list.add(s);
                            }
                        }
                    }

                    recyclerView.setAdapter(new ShopAdapter(list));
                }

                else if (type.equals("veterinarians")) {

                    List<Veterinarian> list = new ArrayList<>();

                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (likedIds.contains(d.getKey())) {
                            Veterinarian v = d.getValue(Veterinarian.class);
                            if (v != null) {
                                v.id = d.getKey();
                                list.add(v);
                            }
                        }
                    }

                    recyclerView.setAdapter(new VeterinarianAdapter(list));
                }
            });
        });
    }
}