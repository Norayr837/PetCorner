package norayr.martirosyan.petcorner;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.AlertDialog;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends ArrayAdapter<ChatMessage> {

    private Context context;
    private ArrayList<ChatMessage> list;
    private String category;

    public MessageAdapter(Context context, ArrayList<ChatMessage> list, String category) {
        super(context, 0, list);
        this.context = context;
        this.list = list;
        this.category = category;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_message, parent, false);
        }

        ChatMessage msg = list.get(position);

        ImageView profileImage = convertView.findViewById(R.id.profileImage);
        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        TextView tvTime = convertView.findViewById(R.id.tvTime);
        TextView tvMessage = convertView.findViewById(R.id.tvMessage);

        ImageView btnMenu = convertView.findViewById(R.id.btnMenu);
        LinearLayout actionContainer = convertView.findViewById(R.id.actionContainer);
        Button btnEdit = convertView.findViewById(R.id.btnEdit);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);

        // USERNAME + MESSAGE
        tvUsername.setText(msg.username);
        tvMessage.setText(msg.message);

        // TIME
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(msg.timestamp));
        tvTime.setText(time);

        // PROFILE IMAGE
        if (msg.profileImage != null && !msg.profileImage.isEmpty()) {
            Picasso.get().load(msg.profileImage).into(profileImage);
        } else {
            profileImage.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        // CURRENT USER
        SharedPreferences prefs = context.getSharedPreferences("PetCornerPrefs", Context.MODE_PRIVATE);
        String currentUser = prefs.getString("username", "User");

        // SHOW MENU ONLY OWNER
        if (msg.username.equals(currentUser)) {
            btnMenu.setVisibility(View.VISIBLE);
        } else {
            btnMenu.setVisibility(View.GONE);
            actionContainer.setVisibility(View.GONE);
        }

        // TOGGLE ACTION MENU
        btnMenu.setOnClickListener(v -> {
            if (actionContainer.getVisibility() == View.VISIBLE) {
                actionContainer.setVisibility(View.GONE);
            } else {
                actionContainer.setVisibility(View.VISIBLE);
            }
        });

        // EDIT
        btnEdit.setOnClickListener(v -> {

            EditText editText = new EditText(context);
            editText.setText(msg.message);

            new AlertDialog.Builder(context)
                    .setTitle("Edit message")
                    .setView(editText)
                    .setPositiveButton("Save", (dialog, which) -> {

                        String newText = editText.getText().toString().trim();

                        if (!newText.isEmpty()) {
                            FirebaseDatabase.getInstance()
                                    .getReference("chats")
                                    .child(category)
                                    .child(msg.id)
                                    .child("message")
                                    .setValue(newText);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            actionContainer.setVisibility(View.GONE);
        });

        // DELETE (with confirm)
        btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete message?")
                    .setMessage("Are you sure you want to delete this message?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        FirebaseDatabase.getInstance()
                                .getReference("chats")
                                .child(category)
                                .child(msg.id)
                                .removeValue();
                    })
                    .setNegativeButton("No", null)
                    .show();

            actionContainer.setVisibility(View.GONE);
        });

        return convertView;
    }
}