package norayr.martirosyan.petcorner;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrivateMessageAdapter extends RecyclerView.Adapter<PrivateMessageAdapter.ViewHolder> {

    private Context context;
    private List<PrivateMessage> list;
    private String currentUserId;
    private String chatId;

    public PrivateMessageAdapter(Context context, List<PrivateMessage> list, String chatId) {
        this.context = context;
        this.list = list;
        this.chatId = chatId;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_private_message, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PrivateMessage msg = list.get(position);

        // RESET STATE (важно для RecyclerView)
        holder.actionContainer.setVisibility(View.GONE);
        holder.btnMenu.setVisibility(View.GONE);

        holder.tvMessage.setText(msg.text != null ? msg.text : "");

        // RESET AVATAR (важно!)
        holder.profileImage.setImageResource(R.drawable.ic_launcher_foreground);
        holder.tvUsername.setText("");

        // TIME
        String time = "";
        try {
            time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(msg.timestamp));
        } catch (Exception ignored) {}

        holder.tvTime.setText(time);

        boolean isMe = msg.senderId != null && msg.senderId.equals(currentUserId);

        if (isMe) {
            holder.messageContainer.setBackgroundResource(R.drawable.bg_my_message);
            holder.btnMenu.setVisibility(View.VISIBLE);
        } else {
            holder.messageContainer.setBackgroundResource(R.drawable.bg_other_message);
            holder.btnMenu.setVisibility(View.GONE);
        }

        // 🔥 LOAD USER INFO (USERNAME + PROFILE IMAGE)
        String senderId = msg.senderId;

        if (senderId != null) {

            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(senderId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String username = snapshot.child("username").getValue(String.class);
                            String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                            holder.tvUsername.setText(username != null ? username : "User");

                            holder.profileImage.setImageResource(R.drawable.ic_launcher_foreground);

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get()
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .error(R.drawable.ic_launcher_foreground)
                                        .fit()
                                        .centerCrop()
                                        .into(holder.profileImage);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }

        // MENU TOGGLE
        holder.btnMenu.setOnClickListener(v -> {
            holder.actionContainer.setVisibility(
                    holder.actionContainer.getVisibility() == View.VISIBLE
                            ? View.GONE : View.VISIBLE
            );
        });

        // DELETE
        holder.btnDelete.setOnClickListener(v -> {
            FirebaseDatabase.getInstance()
                    .getReference("private_chats")
                    .child(chatId)
                    .child("messages")
                    .child(msg.id)
                    .removeValue();
        });

        // EDIT
        holder.btnEdit.setOnClickListener(v -> {

            EditText input = new EditText(context);
            input.setText(msg.text);

            new AlertDialog.Builder(context)
                    .setTitle("Edit message")
                    .setView(input)
                    .setPositiveButton("Save", (d, w) -> {

                        String newText = input.getText().toString().trim();

                        FirebaseDatabase.getInstance()
                                .getReference("private_chats")
                                .child(chatId)
                                .child("messages")
                                .child(msg.id)
                                .child("text")
                                .setValue(newText);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImage, btnMenu;
        TextView tvUsername, tvTime, tvMessage;
        RelativeLayout messageContainer;
        LinearLayout actionContainer;
        TextView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMessage = itemView.findViewById(R.id.tvMessage);

            messageContainer = itemView.findViewById(R.id.messageBubble);

            btnMenu = itemView.findViewById(R.id.btnMenu);
            actionContainer = itemView.findViewById(R.id.actionContainer);

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}