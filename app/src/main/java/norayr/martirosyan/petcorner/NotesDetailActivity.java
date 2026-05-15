package norayr.martirosyan.petcorner;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotesDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_detail);

        TextView title = findViewById(R.id.tvTitle);
        TextView text = findViewById(R.id.tvText);
        TextView time = findViewById(R.id.tvTime);

        String t = getIntent().getStringExtra("title");
        String tx = getIntent().getStringExtra("text");

        long tm = getIntent().getLongExtra("time", -1);

        // TITLE
        title.setText(t != null && !t.isEmpty() ? t : "No title");

        // TEXT
        text.setText(tx != null && !tx.isEmpty() ? tx : "No text");

        // TIME
        if (tm > 0) {

            String formatted = new SimpleDateFormat(
                    "dd MMM yyyy • HH:mm",
                    Locale.getDefault()
            ).format(new Date(tm));

            time.setText("⏰ " + formatted);

        } else {
            time.setText("");
        }
    }
}