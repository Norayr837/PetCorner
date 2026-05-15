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

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<Shop> shopList;
    private boolean isAdmin;

    // ✅ CALLBACKS
    public interface OnApproveClick {
        void onApprove(Shop shop);
    }

    public interface OnDeleteClick {
        void onDelete(Shop shop);
    }

    private OnApproveClick approveClick;
    private OnDeleteClick deleteClick;

    private Map<String, ValueEventListener> likeListeners = new HashMap<>();
    private Map<String, ValueEventListener> ratingListeners = new HashMap<>();

    // ===================== CONSTRUCTORS =====================

    public ShopAdapter(List<Shop> shopList) {
        this.shopList = shopList;
        this.isAdmin = false;
    }

    public ShopAdapter(List<Shop> shopList, boolean isAdmin) {
        this.shopList = shopList;
        this.isAdmin = isAdmin;
    }

    // 🔥 КОНСТРУКТОР С CALLBACKS
    public ShopAdapter(List<Shop> shopList,
                       boolean isAdmin,
                       OnApproveClick approveClick,
                       OnDeleteClick deleteClick) {

        this.shopList = shopList;
        this.isAdmin = isAdmin;
        this.approveClick = approveClick;
        this.deleteClick = deleteClick;
    }

    // ===================== VIEW =====================

    @Override
    public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shop_item, parent, false);

        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShopViewHolder holder, int position) {

        Shop shop = shopList.get(position);

        holder.shopName.setText(shop.name);
        holder.shopDescription.setText(shop.description);

        String info = (shop.address != null ? shop.address : "")
                + " " + (shop.phone != null ? shop.phone : "");

        holder.shopAddressPhone.setText(info.trim());

        if (shop.imageUrl != null && !shop.imageUrl.isEmpty()) {
            Picasso.get().load(shop.imageUrl).into(holder.shopImage);
        } else {
            holder.shopImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // ================= ADMIN UI =================
        if (isAdmin) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        // ================= LIKE SYSTEM =================
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : null;

        DatabaseReference likeRef = FirebaseDatabase.getInstance()
                .getReference("likes")
                .child("shops")
                .child(shop.id);

        if (likeListeners.containsKey(shop.id)) {
            likeRef.removeEventListener(likeListeners.get(shop.id));
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
        likeListeners.put(shop.id, likeListener);

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

        // ================= ADMIN ACTIONS (CALLBACKS) =================
        if (isAdmin) {

            holder.btnApprove.setOnClickListener(v -> {

                if (approveClick != null) {
                    approveClick.onApprove(shop);
                }
            });

            holder.btnDelete.setOnClickListener(v -> {

                if (deleteClick != null) {
                    deleteClick.onDelete(shop);
                }
            });
        }

        // ================= DETAILS =================
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), ShopDetailActivity.class);

            intent.putExtra("id", shop.id);
            intent.putExtra("name", shop.name);
            intent.putExtra("address", shop.address);
            intent.putExtra("phone", shop.phone);
            intent.putExtra("description", shop.description);
            intent.putExtra("image", shop.imageUrl);
            intent.putExtra("userId", shop.userId);

            v.getContext().startActivity(intent);
        });

        // ================= RATING =================
        DatabaseReference ratingRef = FirebaseDatabase.getInstance()
                .getReference("shop_reviews")
                .child(shop.id);

        if (ratingListeners.containsKey(shop.id)) {
            ratingRef.removeEventListener(ratingListeners.get(shop.id));
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
        ratingListeners.put(shop.id, ratingListener);
    }

    @Override
    public int getItemCount() {
        return shopList != null ? shopList.size() : 0;
    }

    // ================= VIEW HOLDER =================

    static class ShopViewHolder extends RecyclerView.ViewHolder {

        ImageView shopImage;

        TextView shopName;
        TextView shopAddressPhone;
        TextView shopDescription;

        ImageView btnLike;
        TextView tvLikes;

        TextView tvRating;
        TextView tvReviews;

        TextView btnApprove;
        TextView btnDelete;

        public ShopViewHolder(View itemView) {
            super(itemView);

            shopImage = itemView.findViewById(R.id.shopImage);

            shopName = itemView.findViewById(R.id.shopName);
            shopAddressPhone = itemView.findViewById(R.id.shopAddressPhone);
            shopDescription = itemView.findViewById(R.id.shopDescription);

            btnLike = itemView.findViewById(R.id.btnLike);
            tvLikes = itemView.findViewById(R.id.tvLikes);

            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviews = itemView.findViewById(R.id.tvReviews);

            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDelete = itemView.findViewById(R.id.btnDeleteAdmin);
        }
    }
}