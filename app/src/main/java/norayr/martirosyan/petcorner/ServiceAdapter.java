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

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> serviceList;
    private boolean isAdmin;


    public interface OnApproveClick {
        void onApprove(Service service);
    }

    public interface OnDeleteClick {
        void onDelete(Service service);
    }

    private OnApproveClick approveClick;
    private OnDeleteClick deleteClick;

    private Map<String, ValueEventListener> likeListeners = new HashMap<>();
    private Map<String, ValueEventListener> ratingListeners = new HashMap<>();



    public ServiceAdapter(List<Service> serviceList) {
        this.serviceList = serviceList;
        this.isAdmin = false;
    }

    public ServiceAdapter(List<Service> serviceList, boolean isAdmin) {
        this.serviceList = serviceList;
        this.isAdmin = isAdmin;
    }


    public ServiceAdapter(List<Service> serviceList,
                          boolean isAdmin,
                          OnApproveClick approveClick,
                          OnDeleteClick deleteClick) {

        this.serviceList = serviceList;
        this.isAdmin = isAdmin;
        this.approveClick = approveClick;
        this.deleteClick = deleteClick;
    }



    @Override
    public ServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.service_item, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ServiceViewHolder holder, int position) {

        Service service = serviceList.get(position);

        holder.serviceName.setText(service.name);
        holder.serviceDescription.setText(service.description);

        String info = (service.address != null ? service.address : "")
                + " " + (service.phone != null ? service.phone : "");

        holder.serviceAddressPhone.setText(info.trim());

        if (service.imageUrl != null && !service.imageUrl.isEmpty()) {
            Picasso.get().load(service.imageUrl).into(holder.serviceImage);
        } else {
            holder.serviceImage.setImageResource(R.drawable.ic_launcher_background);
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
                .child("services")
                .child(service.id);

        if (likeListeners.containsKey(service.id)) {
            likeRef.removeEventListener(likeListeners.get(service.id));
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
        likeListeners.put(service.id, likeListener);

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
                    approveClick.onApprove(service);
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (deleteClick != null) {
                    deleteClick.onDelete(service);
                }
            });
        }


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ServiceDetailActivity.class);

            intent.putExtra("id", service.id);
            intent.putExtra("name", service.name);
            intent.putExtra("address", service.address);
            intent.putExtra("phone", service.phone);
            intent.putExtra("description", service.description);
            intent.putExtra("image", service.imageUrl);
            intent.putExtra("userId", service.userId);

            v.getContext().startActivity(intent);
        });


        DatabaseReference ratingRef = FirebaseDatabase.getInstance()
                .getReference("service_reviews")
                .child(service.id);

        if (ratingListeners.containsKey(service.id)) {
            ratingRef.removeEventListener(ratingListeners.get(service.id));
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
        ratingListeners.put(service.id, ratingListener);
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }


    static class ServiceViewHolder extends RecyclerView.ViewHolder {

        ImageView serviceImage;
        TextView serviceName, serviceAddressPhone, serviceDescription;

        ImageView btnLike;
        TextView tvLikes;

        TextView tvRating;
        TextView tvReviews;

        TextView btnApprove;
        TextView btnDelete;

        public ServiceViewHolder(View itemView) {
            super(itemView);

            serviceImage = itemView.findViewById(R.id.serviceImage);
            serviceName = itemView.findViewById(R.id.serviceName);
            serviceAddressPhone = itemView.findViewById(R.id.serviceAddressPhone);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);

            btnLike = itemView.findViewById(R.id.btnLike);
            tvLikes = itemView.findViewById(R.id.tvLikes);

            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviews = itemView.findViewById(R.id.tvReviews);

            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDelete = itemView.findViewById(R.id.btnDeleteAdmin);
        }
    }
}