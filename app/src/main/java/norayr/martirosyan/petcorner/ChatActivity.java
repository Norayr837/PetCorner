package norayr.martirosyan.petcorner;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;

public class ChatActivity extends AppCompatActivity {

    private EditText etMessage;
    private Button btnSend;
    private ListView listView;
    private TextView tvCategory;

    private ArrayList<ChatMessage> messages;
    private MessageAdapter adapter;

    private DatabaseReference chatRef;

    private String category;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        listView = findViewById(R.id.listView);
        tvCategory = findViewById(R.id.tvCategory);


        category = getIntent().getStringExtra("category");

        if (category == null) category = "";
        category = category.toLowerCase();

        switch (category) {
            case "services":
                title = "Services";
                category = "services";
                break;

            case "shops":
                title = "Shops";
                category = "shops";
                break;

            case "veterinarians":
                title = "Veterinarians";
                category = "veterinarians";
                break;

            case "main":
            default:
                title = "Main";
                category = "main";
                break;
        }


        tvCategory.setText(title);


        chatRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(category);


        messages = new ArrayList<>();
        adapter = new MessageAdapter(this, messages, category);
        listView.setAdapter(adapter);


        loadMessages();


        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                messages.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatMessage msg = ds.getValue(ChatMessage.class);
                    if (msg != null) {
                        messages.add(msg);
                    }
                }

                Collections.sort(messages, (a, b) ->
                        Long.compare(a.timestamp, b.timestamp)
                );

                adapter.notifyDataSetChanged();

                listView.post(() ->
                        listView.setSelection(messages.size() - 1)
                );
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ChatActivity.this,
                        "Error loading messages",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendMessage() {

        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String id = chatRef.push().getKey();
        if (id == null) return;

        String username = getSharedPreferences("PetCornerPrefs", MODE_PRIVATE)
                .getString("username", "User");

        String image = getSharedPreferences("PetCornerPrefs", MODE_PRIVATE)
                .getString("profileImage", "");

        ChatMessage msg = new ChatMessage(
                id,
                username,
                text,
                System.currentTimeMillis(),
                image
        );

        chatRef.child(id).setValue(msg);

        etMessage.setText("");
    }
}