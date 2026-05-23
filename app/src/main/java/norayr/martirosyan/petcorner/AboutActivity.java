package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        btnOk = findViewById(R.id.btnOk);

        btnOk.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }
}