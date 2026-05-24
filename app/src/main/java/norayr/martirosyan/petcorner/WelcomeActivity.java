package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {


            user.reload().addOnCompleteListener(task -> {

                if (auth.getCurrentUser() != null) {
                    startActivity(new Intent(this, ProfileActivity.class));
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                }

                finish();
            });

            return;
        }

        setContentView(R.layout.activity_welcome);

        findViewById(R.id.btnContinue).setOnClickListener(v ->
                startActivity(new Intent(this, InfoActivity.class))
        );
    }
}