package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // 🔔 Permission Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadNotes();

        FloatingActionButton btnAddNote = findViewById(R.id.btnAddNote);
        btnAddNote.setOnClickListener(v ->
                startActivity(new Intent(this, AddNotesActivity.class))
        );
    }

    private void loadNotes() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("notes")
                .child(userId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<Note> list = new ArrayList<>();

                for (DataSnapshot d : snapshot.getChildren()) {

                    Note note = d.getValue(Note.class);

                    if (note != null) {
                        note.id = d.getKey();
                        list.add(note);
                    }
                }

                NotesAdapter adapter = new NotesAdapter(list, new NotesAdapter.OnClick() {

                    @Override
                    public void onClick(Note note) {
                        Intent i = new Intent(NotesActivity.this, NotesDetailActivity.class);
                        i.putExtra("title", note.title);
                        i.putExtra("text", note.text);
                        i.putExtra("time", note.timeMillis);
                        startActivity(i);
                    }

                    @Override
                    public void onDelete(Note note) {

                        new AlertDialog.Builder(NotesActivity.this)
                                .setTitle("Delete Note")
                                .setMessage("Are you sure?")
                                .setPositiveButton("Yes", (dialog, which) -> {

                                    FirebaseDatabase.getInstance()
                                            .getReference("notes")
                                            .child(userId)
                                            .child(note.id)
                                            .removeValue();
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }

                    @Override
                    public void onEditText(Note note) {

                        EditText editText = new EditText(NotesActivity.this);
                        editText.setText(note.text);

                        new AlertDialog.Builder(NotesActivity.this)
                                .setTitle("Edit Text")
                                .setView(editText)
                                .setPositiveButton("Save", (dialog, which) -> {

                                    String newText = editText.getText().toString().trim();

                                    if (!newText.isEmpty()) {

                                        FirebaseDatabase.getInstance()
                                                .getReference("notes")
                                                .child(userId)
                                                .child(note.id)
                                                .child("text")
                                                .setValue(newText);

                                        note.text = newText;

                                        // 🔥 ALARM UPDATE
                                        ReminderWorkerHelper.setReminder(NotesActivity.this, note);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }

                    @Override
                    public void onEditTime(Note note) {

                        TextView textView = new TextView(NotesActivity.this);
                        textView.setText("Tap to select date & time");
                        textView.setPadding(40, 60, 40, 60);

                        final long[] newTime = {note.timeMillis};

                        textView.setOnClickListener(v -> {

                            Calendar calendar = Calendar.getInstance();

                            new android.app.DatePickerDialog(
                                    NotesActivity.this,
                                    (view, year, month, dayOfMonth) -> {

                                        calendar.set(Calendar.YEAR, year);
                                        calendar.set(Calendar.MONTH, month);
                                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                        new android.app.TimePickerDialog(
                                                NotesActivity.this,
                                                (timeView, hour, minute) -> {

                                                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                                                    calendar.set(Calendar.MINUTE, minute);

                                                    newTime[0] = calendar.getTimeInMillis();

                                                    textView.setText(
                                                            android.text.format.DateFormat.format(
                                                                    "dd/MM/yyyy HH:mm",
                                                                    calendar
                                                            )
                                                    );

                                                },
                                                calendar.get(Calendar.HOUR_OF_DAY),
                                                calendar.get(Calendar.MINUTE),
                                                true
                                        ).show();

                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                            ).show();
                        });

                        new AlertDialog.Builder(NotesActivity.this)
                                .setTitle("Edit Time")
                                .setView(textView)
                                .setPositiveButton("Save", (dialog, which) -> {

                                    FirebaseDatabase.getInstance()
                                            .getReference("notes")
                                            .child(userId)
                                            .child(note.id)
                                            .child("timeMillis")
                                            .setValue(newTime[0]);

                                    note.timeMillis = newTime[0];

                                    // 🔥 ALARM UPDATE
                                    ReminderWorkerHelper.setReminder(NotesActivity.this, note);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });

                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}