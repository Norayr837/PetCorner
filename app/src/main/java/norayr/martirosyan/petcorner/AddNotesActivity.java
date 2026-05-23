package norayr.martirosyan.petcorner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddNotesActivity extends AppCompatActivity {

    EditText etTitle, etText;
    Button btnDate, btnTime, btnConfirm;

    Calendar calendar = Calendar.getInstance(); // 🔥 ОДИН календарь

    long selectedTimeMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);

        etTitle = findViewById(R.id.etTitle);
        etText = findViewById(R.id.etText);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnDate.setOnClickListener(v -> pickDate());
        btnTime.setOnClickListener(v -> pickTime());
        btnConfirm.setOnClickListener(v -> saveNote());
    }

    private void pickDate() {

        new android.app.DatePickerDialog(this,
                (view, year, month, day) -> {

                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);

                    selectedTimeMillis = calendar.getTimeInMillis();

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void pickTime() {

        new android.app.TimePickerDialog(this,
                (view, hour, minute) -> {

                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);

                    selectedTimeMillis = calendar.getTimeInMillis();

                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void saveNote() {

        String title = etTitle.getText().toString().trim();
        String text = etText.getText().toString().trim();

        if (title.isEmpty() || text.isEmpty() || selectedTimeMillis == 0) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        if (selectedTimeMillis <= System.currentTimeMillis()) {
            Toast.makeText(this, "Choose future time", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("notes")
                .child(userId);

        String id = ref.push().getKey();

        Note note = new Note(title, text, selectedTimeMillis, userId);
        note.id = id;

        ref.child(id).setValue(note);

        // 🔥 ВОТ ЭТО ТЕПЕРЬ БУДЕТ РАБОТАТЬ
        ReminderWorkerHelper.setReminder(this, note);

        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();

        finish();
    }
}