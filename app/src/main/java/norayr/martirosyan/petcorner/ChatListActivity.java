package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.SearchView;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private boolean usersLoaded = false;
    private ChatListAdapter adapter;

    private ArrayList<ChatItem> chatList = new ArrayList<>();
    private ArrayList<ChatItem> fullList = new ArrayList<>();

    private HashMap<String, String> usernameMap = new HashMap<>();

    private DatabaseReference ref;
    private DatabaseReference usersRef;

    private String myId;

    private ImageButton btnSearch;

    private SearchView searchView;



    private LinearLayout btnAnnouncements, btnProfile, btnForum;
    private LinearLayout dropdownMenu;
    private Button btnServices, btnShops, btnVeterinarians;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        initViews();
        initSearch();
        initBottomMenu();

        recyclerView = findViewById(R.id.recyclerChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        myId = FirebaseAuth.getInstance().getUid();

        ref = FirebaseDatabase.getInstance().getReference("private_chats");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        adapter = new ChatListAdapter(this, chatList, item -> {
            Intent intent = new Intent(ChatListActivity.this, PrivateChatActivity.class);
            intent.putExtra("chatId", item.chatId);
            intent.putExtra("receiverId", item.otherUserId);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        loadUsers();
        loadChats();
    }

    private void initViews() {

        btnAnnouncements = findViewById(R.id.btnServices);
        btnProfile = findViewById(R.id.btnProfile);
        btnForum = findViewById(R.id.btnForum);

        dropdownMenu = findViewById(R.id.dropdownMenu);

        btnServices = findViewById(R.id.btnServicesOption);
        btnShops = findViewById(R.id.btnShops);
        btnVeterinarians = findViewById(R.id.btnVeterinarians);

        searchView = findViewById(R.id.searchView);
        btnSearch = findViewById(R.id.btnSearch);
    }

    private void initSearch() {

        searchView.setQueryHint("Search chats...");
        searchView.setVisibility(View.GONE);

        btnSearch.setOnClickListener(v -> {

            if (searchView.getVisibility() == View.GONE) {
                searchView.setVisibility(View.VISIBLE);
                searchView.requestFocus();
            } else {
                searchView.setQuery("", false);
                searchView.clearFocus();
                searchView.setVisibility(View.GONE);

                chatList.clear();
                chatList.addAll(fullList);
                adapter.notifyDataSetChanged();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                filterChats(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterChats(newText);
                return true;
            }
        });
    }

    private void initBottomMenu() {

        btnAnnouncements.setOnClickListener(v ->
                dropdownMenu.setVisibility(
                        dropdownMenu.getVisibility() == View.VISIBLE
                                ? View.GONE : View.VISIBLE
                )
        );

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        btnForum.setOnClickListener(v ->
                startActivity(new Intent(this, ForumActivity.class))
        );

        btnServices.setOnClickListener(v ->
                startActivity(new Intent(this, ServicesActivity.class))
        );

        btnShops.setOnClickListener(v ->
                startActivity(new Intent(this, ShopActivity.class))
        );

        btnVeterinarians.setOnClickListener(v ->
                startActivity(new Intent(this, VeterinariansActivity.class))
        );
    }


    private void loadUsers() {

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                usernameMap.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    String uid = ds.getKey();
                    String username = ds.child("username").getValue(String.class);

                    if (uid != null && username != null && !username.isEmpty()) {
                        usernameMap.put(uid, username);
                    }
                }

                usersLoaded = true; // 👈 важно для поиска
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                usersLoaded = false;
            }
        });
    }

    private void filterChats(String query) {

        if (!usersLoaded) return; // защита от пустого usernameMap

        String q = query == null ? "" : query.toLowerCase().trim();

        chatList.clear();

        if (q.isEmpty()) {
            chatList.addAll(fullList);
        } else {

            for (ChatItem item : fullList) {

                String username = usernameMap.get(item.otherUserId);

                if (username == null || username.isEmpty()) continue;

                if (username.toLowerCase().contains(q)) {
                    chatList.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }


    private void loadChats() {

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chatList.clear();
                fullList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    String chatId = ds.getKey();
                    if (chatId == null) continue;

                    if (!ds.child("users").hasChild(myId)) continue;

                    String otherUserId = null;

                    for (DataSnapshot userSnap : ds.child("users").getChildren()) {

                        String uid = userSnap.getKey();

                        if (uid != null && !uid.equals(myId)) {
                            otherUserId = uid;
                            break;
                        }
                    }

                    if (otherUserId == null) continue;

                    String lastMsg = ds.child("lastMessage").getValue(String.class);
                    Long lastTime = ds.child("lastTime").getValue(Long.class);

                    ChatItem item = new ChatItem(
                            chatId,
                            otherUserId,
                            lastMsg != null ? lastMsg : "",
                            lastTime != null ? lastTime : 0
                    );

                    fullList.add(item);   // 👈 ВСЕГДА в fullList
                }

                chatList.addAll(fullList); // 👈 показываем всё

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

}