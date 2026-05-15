package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends AppCompatActivity {

    Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        btnOk = findViewById(R.id.btnOk);

        btnOk.setOnClickListener(v -> {
            Intent intent = new Intent(PrivacyPolicyActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish(); // закрывает PrivacyPolicyActivity
        });
    }
}