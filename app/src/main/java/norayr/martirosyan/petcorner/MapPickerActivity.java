package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.gms.common.api.Status;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LatLng selectedLatLng;
    private String selectedAddress = "";

    private TextView searchInput;

    private ActivityResultLauncher<Intent> autocompleteLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBwyi4yxocMtZAgiOnd1jB5LUpTMoDcGLs");
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        searchInput = findViewById(R.id.searchInput);


        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        Place place = Autocomplete.getPlaceFromIntent(result.getData());

                        selectedLatLng = place.getLatLng();
                        selectedAddress = place.getAddress();

                        if (mMap != null && selectedLatLng != null) {
                            setMarker(selectedLatLng);
                            mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f)
                            );
                        }

                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {

                        Status status = Autocomplete.getStatusFromIntent(result.getData());
                        Toast.makeText(this,
                                "Error: " + status.getStatusMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );


        searchInput.setOnClickListener(v -> {

            List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
            );

            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY,
                    fields
            ).build(this);

            autocompleteLauncher.launch(intent);
        });


        Button btnConfirm = findViewById(R.id.btnConfirmLocation);

        btnConfirm.setOnClickListener(v -> {

            if (selectedLatLng == null) {
                Toast.makeText(this, "Select location first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("lat", selectedLatLng.latitude);
            intent.putExtra("lng", selectedLatLng.longitude);
            intent.putExtra("address", selectedAddress);

            setResult(RESULT_OK, intent);
            finish();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng yerevan = new LatLng(40.1792, 44.4991);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yerevan, 12));

        mMap.setOnMapClickListener(latLng -> {

            setMarker(latLng);

            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                List<Address> addresses =
                        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    selectedAddress = addresses.get(0).getAddressLine(0);
                } else {
                    selectedAddress = "Unknown location";
                }

                Toast.makeText(this, selectedAddress, Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                selectedAddress = "Error getting address";
            }
        });
    }

    private void setMarker(LatLng latLng) {
        selectedLatLng = latLng;

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
    }
}