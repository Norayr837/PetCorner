package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PersonalInformation extends AppCompatActivity {

    TextView tvUsername, tvEmail, tvPassword;
    Button btnChangeUsername, btnChangePassword, btnLogout, btnDeleteAccount;

    // ✅ НОВОЕ
    Button btnDeleteProfilePhoto;

    FirebaseUser user;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information);

        // =====================
        // INIT VIEWS
        // =====================
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPassword = findViewById(R.id.tvPassword);

        btnChangeUsername = findViewById(R.id.btnChangeUsername);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        // ✅ НОВОЕ
        btnDeleteProfilePhoto = findViewById(R.id.btnDeleteProfilePhoto);

        // =====================
        // FIREBASE USER
        // =====================
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        String uid = user.getUid();

        reference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        loadUserData();

        // =====================
        // CHANGE USERNAME
        // =====================
        btnChangeUsername.setOnClickListener(v ->
                startActivity(new Intent(this, ChangeUsernameActivity.class)));

        // =====================
        // CHANGE PASSWORD
        // =====================
        btnChangePassword.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        // =====================
        // LOGOUT (CONFIRM)
        // =====================
        btnLogout.setOnClickListener(v -> {

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Log out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (d, w) -> {

                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", (d, w) -> d.dismiss())
                    .show();
        });

        // =====================
        // DELETE ACCOUNT
        // =====================
        btnDeleteAccount.setOnClickListener(v -> showPasswordDialog());

        // =====================
        // ✅ DELETE PROFILE PHOTO
        // =====================
        btnDeleteProfilePhoto.setOnClickListener(v -> {

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete photo")
                    .setMessage("Are you sure you want to delete your profile photo?")
                    .setPositiveButton("Yes", (d, w) -> deleteProfilePhoto())
                    .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                    .show();
        });
    }

    // =====================
    // LOAD USER DATA
    // =====================
    private void loadUserData() {

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String username = snapshot.child("username").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                tvUsername.setText(username != null ? username : "N/A");
                tvEmail.setText(email != null ? email : "N/A");
                tvPassword.setText("********");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // =====================
    // DELETE PROFILE PHOTO
    // =====================
    private void deleteProfilePhoto() {

        String uid = user.getUid();

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("imageUrl")
                .removeValue()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // =====================
    // PASSWORD DIALOG
    // =====================
    private void showPasswordDialog() {

        EditText input = new EditText(this);
        input.setHint("Enter password");
        input.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Verify identity")
                .setMessage("Enter your password to continue")
                .setView(input)
                .setPositiveButton("Next", (d, w) -> {

                    String password = input.getText().toString().trim();

                    if (password.isEmpty()) {
                        Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    reauthenticate(password);
                })
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .show();
    }

    private void reauthenticate(String password) {

        AuthCredential credential =
                EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        showFinalConfirmDialog();
                    } else {
                        Toast.makeText(this,
                                "Wrong password",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showFinalConfirmDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete account")
                .setMessage("Are you sure you want to permanently delete your account?")
                .setPositiveButton("Delete", (d, w) -> deleteAccount())
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .show();
    }

    private void deleteAccount() {

        String uid = user.getUid();

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .removeValue();

        user.delete()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {
                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}