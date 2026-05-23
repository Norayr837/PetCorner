package norayr.martirosyan.petcorner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.Holder> {

    public interface OnChatClick {
        void onClick(ChatItem item);
    }

    Context context;
    ArrayList<ChatItem> list;
    OnChatClick listener;

    public ChatListAdapter(Context context, ArrayList<ChatItem> list, OnChatClick listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_chat_user, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        ChatItem item = list.get(position);


        holder.profileImage.setImageResource(R.drawable.ic_launcher_foreground);
        holder.tvUsername.setText("");
        holder.tvLastMessage.setText("");
        holder.tvTime.setText("");


        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(item.otherUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) return;

                        String username = snapshot.child("username").getValue(String.class);
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                        holder.tvUsername.setText(
                                username != null ? username : item.otherUserId
                        );

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get()
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(holder.profileImage);
                        } else {
                            holder.profileImage.setImageResource(R.drawable.ic_launcher_foreground);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });


        holder.tvLastMessage.setText(
                item.lastMessage != null ? item.lastMessage : ""
        );


        if (item.timestamp > 0) {
            String time = new SimpleDateFormat("HH:mm",
                    Locale.getDefault())
                    .format(new Date(item.timestamp));

            holder.tvTime.setText(time);
        }


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView tvUsername;
        TextView tvLastMessage;
        TextView tvTime;

        public Holder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}