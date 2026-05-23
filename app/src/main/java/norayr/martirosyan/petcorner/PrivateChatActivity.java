package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

public class PrivateChatActivity extends AppCompatActivity {

    private EditText etMessage;
    private Button btnSend;
    private RecyclerView recyclerView;

    private ImageView imgChatProfile, btnBack;
    private TextView tvChatUsername;

    private ArrayList<PrivateMessage> messages;
    private PrivateMessageAdapter adapter;

    private DatabaseReference chatRef;

    private String currentUserId;
    private String chatId;
    private String receiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerView = findViewById(R.id.recyclerView);

        imgChatProfile = findViewById(R.id.imgChatProfile);
        tvChatUsername = findViewById(R.id.tvChatUsername);
        btnBack = findViewById(R.id.btnBack);

        currentUserId = FirebaseAuth.getInstance().getUid();
        chatId = getIntent().getStringExtra("chatId");
        receiverId = getIntent().getStringExtra("receiverId");

        if (currentUserId == null || chatId == null) {
            finish();
            return;
        }

        chatRef = FirebaseDatabase.getInstance()
                .getReference("private_chats")
                .child(chatId);

        messages = new ArrayList<>();
        adapter = new PrivateMessageAdapter(this, messages, chatId);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadMessages();
        loadUserInfo();

        btnSend.setOnClickListener(v -> sendMessage());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    // ===================== USER INFO =====================
    private void loadUserInfo() {

        chatRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {

                    String uid = ds.getKey();

                    if (uid != null && !uid.equals(currentUserId)) {

                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(uid);

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                String username = snapshot.child("username").getValue(String.class);
                                String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                                tvChatUsername.setText(username != null ? username : "User");

                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    Picasso.get()
                                            .load(imageUrl)
                                            .placeholder(R.drawable.ic_launcher_foreground)
                                            .into(imgChatProfile);
                                } else {
                                    imgChatProfile.setImageResource(R.drawable.ic_launcher_foreground);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ===================== LOAD MESSAGES =====================
    private void loadMessages() {

        chatRef.child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        messages.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PrivateMessage msg = ds.getValue(PrivateMessage.class);
                            if (msg != null) messages.add(msg);
                        }

                        Collections.sort(messages, (a, b) ->
                                Long.compare(a.timestamp, b.timestamp)
                        );

                        adapter.notifyDataSetChanged();

                        recyclerView.post(() ->
                                recyclerView.scrollToPosition(messages.size() - 1)
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    private void sendMessage() {

        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String msgId = chatRef.child("messages").push().getKey();
        if (msgId == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId);

        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String senderName = snapshot.getValue(String.class);

                PrivateMessage msg = new PrivateMessage(
                        msgId,
                        currentUserId,
                        receiverId,
                        senderName != null ? senderName : "User",
                        text,
                        System.currentTimeMillis()
                );

                chatRef.child("messages").child(msgId).setValue(msg);

                chatRef.child("lastMessage").setValue(text);
                chatRef.child("lastTime").setValue(System.currentTimeMillis());

                etMessage.setText("");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}