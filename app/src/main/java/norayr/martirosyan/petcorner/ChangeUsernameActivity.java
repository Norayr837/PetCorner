package norayr.martirosyan.petcorner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeUsernameActivity extends AppCompatActivity {

    EditText etNewUsername;
    Button btnConfirm;

    FirebaseUser user;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_username);

        etNewUsername = findViewById(R.id.etNewUsername);
        btnConfirm = findViewById(R.id.btnConfirm);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        String uid = user.getUid();

        reference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        btnConfirm.setOnClickListener(v -> updateUsername());
    }

    private void updateUsername() {

        String newUsername = etNewUsername.getText().toString().trim();

        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Enter username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newUsername.length() < 6) {
            Toast.makeText(this,
                    "Username must be at least 6 characters",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        reference.child("username").setValue(newUsername)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show();
                        finish(); // возвращаемся в Profile
                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}