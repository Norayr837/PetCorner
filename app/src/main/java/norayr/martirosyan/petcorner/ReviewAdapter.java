package norayr.martirosyan.petcorner;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    private final List<Review> list;
    private final String type;
    private final String itemId;
    private final ReviewUpdateListener listener;

    public ReviewAdapter(List<Review> list,
                         String type,
                         String itemId,
                         ReviewUpdateListener listener) {

        this.list = list;
        this.type = type;
        this.itemId = itemId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_item, parent, false);

        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        if (position < 0 || position >= list.size()) return;

        Review r = list.get(position);
        if (r == null) return;

        holder.tvUsername.setText(r.username != null ? r.username : "User");
        holder.tvReviewText.setText(r.text != null ? r.text : "");
        holder.ratingBar.setRating(r.rating);

        if (r.timestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(r.timestamp)));
        } else {
            holder.tvDate.setText("");
        }

        holder.imgProfile.setImageResource(R.drawable.ic_launcher_foreground);

        if (r.userId != null) {
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(r.userId)
                    .child("imageUrl")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String url = snapshot.getValue(String.class);
                        if (url != null && !url.isEmpty()) {
                            Picasso.get().load(url).into(holder.imgProfile);
                        }
                    });
        }

        String currentUserId = FirebaseAuth.getInstance().getUid();
        boolean isOwner = currentUserId != null && currentUserId.equals(r.userId);

        holder.btnMenu.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        holder.btnMenu.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Review review = list.get(pos);
            if (review == null || review.reviewId == null) return;

            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference(type + "_reviews")
                    .child(itemId)
                    .child(review.reviewId);

            PopupMenu menu = new PopupMenu(v.getContext(), holder.btnMenu);
            menu.getMenu().add("Edit review");
            menu.getMenu().add("Delete review");

            menu.setOnMenuItemClickListener(item -> {

                String title = item.getTitle().toString();


                if (title.equals("Delete review")) {

                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete review")
                            .setMessage("Are you sure?")
                            .setPositiveButton("Yes", (d, w) -> {

                                ref.removeValue();

                                if (listener != null)
                                    listener.onReviewsChanged();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }


                if (title.equals("Edit review")) {

                    Context context = v.getContext();

                    LinearLayout layout = new LinearLayout(context);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(40, 20, 40, 20);

                    EditText editText = new EditText(context);
                    editText.setText(review.text);

                    RatingBar rating = new RatingBar(context);
                    rating.setNumStars(5);
                    rating.setStepSize(1f);
                    rating.setRating((float) review.rating);

                    LinearLayout.LayoutParams params =
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);

                    rating.setLayoutParams(params);


                    layout.addView(editText);
                    layout.addView(rating);

                    new AlertDialog.Builder(context)
                            .setTitle("Edit review")
                            .setView(layout)
                            .setPositiveButton("Save", (d, w) -> {

                                String newText = editText.getText().toString().trim();
                                float newRating = rating.getRating();

                                if (newText.isEmpty()) return;

                                ref.child("text").setValue(newText);
                                ref.child("rating").setValue(newRating);

                                if (listener != null)
                                    listener.onReviewsChanged();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }

                return true;
            });

            menu.show();
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvUsername, tvReviewText, tvDate;
        RatingBar ratingBar;
        ImageView imgProfile;
        ImageButton btnMenu;

        VH(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvDate = itemView.findViewById(R.id.tvDate);
            ratingBar = itemView.findViewById(R.id.ratingBarReview);
            imgProfile = itemView.findViewById(R.id.profileImage);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }

    public interface ReviewUpdateListener {
        void onReviewsChanged();
    }
}