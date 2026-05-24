package norayr.martirosyan.petcorner;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VeterinarianAdapter extends RecyclerView.Adapter<VeterinarianAdapter.VetViewHolder> {

    private List<Veterinarian> vetList;
    private boolean isAdmin;


    public interface OnApproveClick {
        void onApprove(Veterinarian vet);
    }

    public interface OnDeleteClick {
        void onDelete(Veterinarian vet);
    }

    private OnApproveClick approveClick;
    private OnDeleteClick deleteClick;

    private Map<String, ValueEventListener> likeListeners = new HashMap<>();
    private Map<String, ValueEventListener> ratingListeners = new HashMap<>();



    public VeterinarianAdapter(List<Veterinarian> vetList) {
        this.vetList = vetList;
        this.isAdmin = false;
    }

    public VeterinarianAdapter(List<Veterinarian> vetList, boolean isAdmin) {
        this.vetList = vetList;
        this.isAdmin = isAdmin;
    }

    public VeterinarianAdapter(List<Veterinarian> vetList,
                               boolean isAdmin,
                               OnApproveClick approveClick,
                               OnDeleteClick deleteClick) {
        this.vetList = vetList;
        this.isAdmin = isAdmin;
        this.approveClick = approveClick;
        this.deleteClick = deleteClick;
    }



    @Override
    public VetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.veterinarian_item, parent, false);
        return new VetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VetViewHolder holder, int position) {

        Veterinarian vet = vetList.get(position);

        holder.vetName.setText(vet.name);
        holder.vetDescription.setText(vet.description);

        String info = (vet.address != null ? vet.address : "")
                + " " + (vet.phone != null ? vet.phone : "");
        holder.vetAddressPhone.setText(info.trim());

        if (vet.imageUrl != null && !vet.imageUrl.isEmpty()) {
            Picasso.get().load(vet.imageUrl).into(holder.vetImage);
        } else {
            holder.vetImage.setImageResource(R.drawable.ic_launcher_background);
        }


        if (isAdmin) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        DatabaseReference likeRef = FirebaseDatabase.getInstance()
                .getReference("likes")
                .child("veterinarians")
                .child(vet.id);

        if (likeListeners.containsKey(vet.id)) {
            likeRef.removeEventListener(likeListeners.get(vet.id));
        }

        ValueEventListener likeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                holder.tvLikes.setText(String.valueOf(snapshot.getChildrenCount()));

                if (userId != null && snapshot.hasChild(userId)) {
                    holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
                } else {
                    holder.btnLike.setImageResource(R.drawable.ic_heart_outline);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        };

        likeRef.addValueEventListener(likeListener);
        likeListeners.put(vet.id, likeListener);

        holder.btnLike.setOnClickListener(v -> {
            if (userId == null) return;

            likeRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        likeRef.child(userId).removeValue();
                    } else {
                        likeRef.child(userId).setValue(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {}
            });
        });


        if (isAdmin) {
            holder.btnApprove.setOnClickListener(v -> {
                if (approveClick != null) {
                    approveClick.onApprove(vet);
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (deleteClick != null) {
                    deleteClick.onDelete(vet);
                }
            });
        }


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), VeterinarianDetailActivity.class);

            intent.putExtra("id", vet.id);
            intent.putExtra("name", vet.name);
            intent.putExtra("address", vet.address);
            intent.putExtra("phone", vet.phone);
            intent.putExtra("description", vet.description);
            intent.putExtra("image", vet.imageUrl);
            intent.putExtra("userId", vet.userId);

            v.getContext().startActivity(intent);
        });


        DatabaseReference ratingRef = FirebaseDatabase.getInstance()
                .getReference("vet_reviews")
                .child(vet.id);

        if (ratingListeners.containsKey(vet.id)) {
            ratingRef.removeEventListener(ratingListeners.get(vet.id));
        }

        ValueEventListener ratingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double sum = 0;
                int count = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Float rating = snap.child("rating").getValue(Float.class);
                    if (rating != null) {
                        sum += rating;
                        count++;
                    }
                }

                double avg = count > 0 ? sum / count : 0;

                holder.tvRating.setText(String.format("%.1f ★", avg));
                holder.tvReviews.setText(count + " reviews");
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        };

        ratingRef.addValueEventListener(ratingListener);
        ratingListeners.put(vet.id, ratingListener);
    }

    @Override
    public int getItemCount() {
        return vetList != null ? vetList.size() : 0;
    }

    @Override
    public void onViewRecycled(VetViewHolder holder) {
        super.onViewRecycled(holder);

        int position = holder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return;

        Veterinarian vet = vetList.get(position);

        DatabaseReference likeRef = FirebaseDatabase.getInstance()
                .getReference("likes")
                .child("veterinarians")
                .child(vet.id);

        if (likeListeners.containsKey(vet.id)) {
            likeRef.removeEventListener(likeListeners.get(vet.id));
            likeListeners.remove(vet.id);
        }

        DatabaseReference ratingRef = FirebaseDatabase.getInstance()
                .getReference("vet_reviews")
                .child(vet.id);

        if (ratingListeners.containsKey(vet.id)) {
            ratingRef.removeEventListener(ratingListeners.get(vet.id));
            ratingListeners.remove(vet.id);
        }
    }


    static class VetViewHolder extends RecyclerView.ViewHolder {

        ImageView vetImage;
        TextView vetName, vetAddressPhone, vetDescription;

        ImageView btnLike;
        TextView tvLikes;

        TextView tvRating;
        TextView tvReviews;

        TextView btnApprove;
        TextView btnDelete;

        public VetViewHolder(View itemView) {
            super(itemView);

            vetImage = itemView.findViewById(R.id.vetImage);
            vetName = itemView.findViewById(R.id.vetName);
            vetAddressPhone = itemView.findViewById(R.id.vetAddressPhone);
            vetDescription = itemView.findViewById(R.id.vetDescription);

            btnLike = itemView.findViewById(R.id.btnLike);
            tvLikes = itemView.findViewById(R.id.tvLikes);

            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviews = itemView.findViewById(R.id.tvReviews);

            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDelete = itemView.findViewById(R.id.btnDeleteAdmin);
        }
    }
}