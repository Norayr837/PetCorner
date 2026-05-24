package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etPasswordConfirm;
    private Button btnSubmit;


    private ImageButton btnEyePassword, btnEyeConfirm;

    private static final int ADD_PET_REQUEST = 100;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordconf);
        btnSubmit = findViewById(R.id.btnSubmit);


        btnEyePassword = findViewById(R.id.btnEyePassword);
        btnEyeConfirm = findViewById(R.id.btnEyeConfirm);

        mAuth = FirebaseAuth.getInstance();


        btnEyePassword.setOnClickListener(v -> {
            if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnEyePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnEyePassword.setImageResource(android.R.drawable.ic_menu_view);
            }
            etPassword.setSelection(etPassword.getText().length());
        });


        btnEyeConfirm.setOnClickListener(v -> {
            if (etPasswordConfirm.getTransformationMethod() instanceof PasswordTransformationMethod) {
                etPasswordConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnEyeConfirm.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            } else {
                etPasswordConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnEyeConfirm.setImageResource(android.R.drawable.ic_menu_view);
            }
            etPasswordConfirm.setSelection(etPasswordConfirm.getText().length());
        });

        btnSubmit.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etPasswordConfirm.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 6) {
            Toast.makeText(this, "Username must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = task.getResult().getUser();

                        if (user == null) {
                            Toast.makeText(this, "Auth error", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String uid = user.getUid();

                        SharedPreferences prefs = getSharedPreferences("PetCornerPrefs", MODE_PRIVATE);
                        prefs.edit()
                                .putString("username", username)
                                .putString("animal_info", "")
                                .apply();

                        FirebaseDatabase database = FirebaseDatabase.getInstance(
                                "https://petcorner-2f7b7-default-rtdb.firebaseio.com/"
                        );

                        DatabaseReference usersRef = database.getReference("users");

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("username", username);
                        userData.put("email", email);

                        user.sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {

                                    if (emailTask.isSuccessful()) {

                                        usersRef.child(uid).setValue(userData)
                                                .addOnCompleteListener(dbTask -> {

                                                    if (dbTask.isSuccessful()) {
                                                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(this,
                                                                "Database error: " + dbTask.getException().getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                        Intent intent = new Intent(RegistrationActivity.this, ProfileActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);

                                    } else {
                                        Toast.makeText(this,
                                                "Failed to send verification email: " +
                                                        emailTask.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });

                    } else {
                        Toast.makeText(this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}