package norayr.martirosyan.petcorner;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText etOldPassword, etNewPassword, etConfirmPassword;
    Button btnChangePassword;

    ImageButton btnEyeOld, btnEyeNew, btnEyeConfirm;

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnEyeOld = findViewById(R.id.btnEyeOld);
        btnEyeNew = findViewById(R.id.btnEyeNew);
        btnEyeConfirm = findViewById(R.id.btnEyeConfirm);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
            return;
        }


        btnEyeOld.setOnClickListener(v ->
                togglePassword(etOldPassword, btnEyeOld));

        btnEyeNew.setOnClickListener(v ->
                togglePassword(etNewPassword, btnEyeNew));

        btnEyeConfirm.setOnClickListener(v ->
                togglePassword(etConfirmPassword, btnEyeConfirm));

        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void togglePassword(EditText editText, ImageButton button) {
        if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {

            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            button.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {

            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            button.setImageResource(android.R.drawable.ic_menu_view);
        }


        editText.setSelection(editText.getText().length());
    }

    private void changePassword() {

        String oldPass = etOldPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), oldPass))
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        user.updatePassword(newPass)
                                .addOnCompleteListener(task2 -> {

                                    if (task2.isSuccessful()) {
                                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(this,
                                                "Error: " + task2.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}