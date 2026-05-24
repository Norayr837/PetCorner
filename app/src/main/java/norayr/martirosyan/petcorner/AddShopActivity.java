package norayr.martirosyan.petcorner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class AddShopActivity extends AppCompatActivity {

    EditText etName, etOwner, etAddress, etPhone, etDescription;
    ImageView imgPhoto;
    Button btnUpload, btnConfirm, btnChooseMap;
    CountryCodePicker ccp;

    Uri imageUri;
    DatabaseReference databaseReference;

    double selectedLat = 0;
    double selectedLng = 0;

    boolean editMode = false;
    String shopId = null;
    String oldImageUrl = null;

    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_PERMISSION = 100;
    private static final int MAP_REQUEST = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shop);

        etName = findViewById(R.id.etShopName);
        etOwner = findViewById(R.id.etOwnerName);
        etAddress = findViewById(R.id.etShopAddress);
        etPhone = findViewById(R.id.etShopPhone);
        etDescription = findViewById(R.id.etShopDescription);

        imgPhoto = findViewById(R.id.imgShopPhoto);
        btnUpload = findViewById(R.id.btnUploadShopPhoto);
        btnConfirm = findViewById(R.id.btnConfirmShop);
        btnChooseMap = findViewById(R.id.btnChooseOnMap);

        ccp = findViewById(R.id.ccpShop);
        ccp.registerCarrierNumberEditText(etPhone);

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("shops");


        Intent intent = getIntent();
        editMode = intent.getBooleanExtra("editMode", false);

        if (editMode) {

            shopId = intent.getStringExtra("id");

            etName.setText(intent.getStringExtra("name"));
            etOwner.setText(intent.getStringExtra("owner"));
            etAddress.setText(intent.getStringExtra("address"));
            etPhone.setText(intent.getStringExtra("phone"));
            etDescription.setText(intent.getStringExtra("description"));

            oldImageUrl = intent.getStringExtra("image");

            selectedLat = intent.getDoubleExtra("lat", 0);
            selectedLng = intent.getDoubleExtra("lng", 0);

            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                Picasso.get().load(oldImageUrl).into(imgPhoto);
            }
        }

        btnUpload.setOnClickListener(v -> checkPermissionAndOpenGallery());

        btnChooseMap.setOnClickListener(v -> {
            Intent i = new Intent(this, MapPickerActivity.class);
            startActivityForResult(i, MAP_REQUEST);
        });

        btnConfirm.setOnClickListener(v -> {
            if (editMode) updateShop();
            else postShop();
        });
    }


    private void postShop() {

        String name = etName.getText().toString().trim();
        String owner = etOwner.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = ccp.getFullNumberWithPlus();
        String userId = FirebaseAuth.getInstance().getUid();

        MediaManager.get().upload(imageUri)
                .unsigned("android_preset")
                .option("folder", "shops")
                .callback(new UploadCallback() {

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        String url = (String) resultData.get("secure_url");
                        String id = databaseReference.push().getKey();

                        if (id == null) return;

                        Shop shop = new Shop(
                                name,
                                owner,
                                address,
                                phone,
                                description,
                                url,
                                userId
                        );

                        shop.id = id;
                        shop.latitude = selectedLat;
                        shop.longitude = selectedLng;
                        shop.timestamp = System.currentTimeMillis();


                        shop.status = "pending";

                        databaseReference.child(id).setValue(shop)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(AddShopActivity.this,
                                            "Your shop will be visible after approval",
                                            Toast.LENGTH_LONG).show();

                                    finish();
                                });
                    }

                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onError(String requestId, ErrorInfo error) {}

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }


    private void updateShop() {

        if (shopId == null) return;

        DatabaseReference ref = databaseReference.child(shopId);

        ref.child("name").setValue(etName.getText().toString().trim());
        ref.child("owner").setValue(etOwner.getText().toString().trim());
        ref.child("address").setValue(etAddress.getText().toString().trim());
        ref.child("phone").setValue(ccp.getFullNumberWithPlus());
        ref.child("description").setValue(etDescription.getText().toString().trim());

        ref.child("latitude").setValue(selectedLat);
        ref.child("longitude").setValue(selectedLng);

        if (imageUri != null) {

            MediaManager.get().upload(imageUri)
                    .unsigned("android_preset")
                    .option("folder", "shops")
                    .callback(new UploadCallback() {

                        @Override
                        public void onSuccess(String requestId, Map resultData) {

                            String url = (String) resultData.get("secure_url");

                            ref.child("imageUrl").setValue(url);

                            finish();
                        }

                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onError(String requestId, ErrorInfo error) {}

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    })
                    .dispatch();

        } else {
            finish();
        }
    }


    private void checkPermissionAndOpenGallery() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_PERMISSION
                );

            } else {
                openGallery();
            }

        } else {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION
                );

            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {

        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );

        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE
                && resultCode == RESULT_OK
                && data != null) {

            imageUri = data.getData();
            imgPhoto.setImageURI(imageUri);
        }

        if (requestCode == MAP_REQUEST
                && resultCode == RESULT_OK
                && data != null) {

            selectedLat = data.getDoubleExtra("lat", 0);
            selectedLng = data.getDoubleExtra("lng", 0);

            String addr = data.getStringExtra("address");

            etAddress.setText(
                    addr != null
                            ? addr
                            : selectedLat + ", " + selectedLng
            );
        }
    }
}