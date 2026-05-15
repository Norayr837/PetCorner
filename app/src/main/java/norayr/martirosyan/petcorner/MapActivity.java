package norayr.martirosyan.petcorner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Button btnMenu, btnAll, btnServices, btnShops, btnVets;
    private LinearLayout filterPanel;

    private static final int LOCATION_REQUEST = 1001;

    private final List<Marker> serviceMarkers = new ArrayList<>();
    private final List<Marker> shopMarkers = new ArrayList<>();
    private final List<Marker> vetMarkers = new ArrayList<>();

    // 🔥 ВАЖНО: чтобы Picasso Target не удалялся
    private final List<com.squareup.picasso.Target> targets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btnMenu = findViewById(R.id.btnMenu);
        btnAll = findViewById(R.id.btnAll);
        btnServices = findViewById(R.id.btnServices);
        btnShops = findViewById(R.id.btnShops);
        btnVets = findViewById(R.id.btnVets);
        filterPanel = findViewById(R.id.filterPanel);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnMenu.setOnClickListener(v ->
                filterPanel.setVisibility(
                        filterPanel.getVisibility() == View.GONE
                                ? View.VISIBLE
                                : View.GONE
                )
        );

        btnAll.setOnClickListener(v -> showAll());
        btnServices.setOnClickListener(v -> showServices());
        btnShops.setOnClickListener(v -> showShops());
        btnVets.setOnClickListener(v -> showVets());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        enableLocation();

        loadServices();
        loadShops();
        loadVets();

        mMap.setOnMarkerClickListener(marker -> {

            Object obj = marker.getTag();
            if (obj == null) return false;

            String[] parts = obj.toString().split(":");
            if (parts.length != 2) return false;

            String type = parts[0];
            String id = parts[1];

            Intent intent;

            if (type.equals("service")) {
                intent = new Intent(this, ServiceDetailActivity.class);
            } else if (type.equals("shop")) {
                intent = new Intent(this, ShopDetailActivity.class);
            } else {
                intent = new Intent(this, VeterinarianDetailActivity.class);
            }

            intent.putExtra("id", id);
            startActivity(intent);

            return true;
        });

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(40.1772, 44.5035),
                10f
        ));
    }

    // ================= LOCATION =================
    private void enableLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST
            );
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    // ================= MARKER =================
    private void buildMarker(String type, String id,
                             String name, String imageUrl,
                             LatLng pos, List<Marker> list) {

        com.squareup.picasso.Target target = new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                View view = getLayoutInflater().inflate(R.layout.map_marker, null);

                ImageView img = view.findViewById(R.id.imgMarker);
                img.setImageBitmap(bitmap);


                view.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );

                view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

                Bitmap finalBitmap = Bitmap.createBitmap(
                        view.getMeasuredWidth(),
                        view.getMeasuredHeight(),
                        Bitmap.Config.ARGB_8888
                );

                Canvas canvas = new Canvas(finalBitmap);
                view.draw(canvas);

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(name)
                        .icon(BitmapDescriptorFactory.fromBitmap(finalBitmap)));

                marker.setTag(type + ":" + id);
                list.add(marker);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                Bitmap fallback = BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.marker_bg
                );

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(name)
                        .icon(BitmapDescriptorFactory.fromBitmap(fallback)));

                marker.setTag(type + ":" + id);
                list.add(marker);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };

        targets.add(target);

        Picasso.get()
                .load(imageUrl)
                .resize(120, 120)
                .centerCrop()
                .into(target);
    }

    // ================= LOAD DATA =================
    private void loadServices() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("services");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {

                    Service s = d.getValue(Service.class);
                    if (s == null) continue;

                    buildMarker("service", d.getKey(),
                            s.name, s.imageUrl,
                            new LatLng(s.latitude, s.longitude),
                            serviceMarkers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadShops() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("shops");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {

                    Shop s = d.getValue(Shop.class);
                    if (s == null) continue;

                    buildMarker("shop", d.getKey(),
                            s.name, s.imageUrl,
                            new LatLng(s.latitude, s.longitude),
                            shopMarkers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadVets() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("veterinarians");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {

                    Veterinarian v = d.getValue(Veterinarian.class);
                    if (v == null) continue;

                    buildMarker("veterinarian", d.getKey(),
                            v.name, v.imageUrl,
                            new LatLng(v.latitude, v.longitude),
                            vetMarkers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ================= FILTERS =================
    private void showAll() {
        setVisible(serviceMarkers, true);
        setVisible(shopMarkers, true);
        setVisible(vetMarkers, true);
    }

    private void showServices() {
        setVisible(serviceMarkers, true);
        setVisible(shopMarkers, false);
        setVisible(vetMarkers, false);
    }

    private void showShops() {
        setVisible(serviceMarkers, false);
        setVisible(shopMarkers, true);
        setVisible(vetMarkers, false);
    }

    private void showVets() {
        setVisible(serviceMarkers, false);
        setVisible(shopMarkers, false);
        setVisible(vetMarkers, true);
    }

    private void setVisible(List<Marker> list, boolean visible) {
        for (Marker m : list) {
            m.setVisible(visible);
        }
    }
}