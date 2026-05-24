package norayr.martirosyan.petcorner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private final List<Note> list;
    private final OnClick listener;

    public interface OnClick {
        void onClick(Note note);
        void onDelete(Note note);
        void onEditText(Note note);
        void onEditTime(Note note);
    }

    public NotesAdapter(List<Note> list, OnClick listener) {
        this.list = list;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, text, time;
        ImageView btnMenu;
        LinearLayout menuContainer;
        TextView btnDelete, btnEditText, btnEditTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tvTitle);
            text = itemView.findViewById(R.id.tvText);
            time = itemView.findViewById(R.id.tvTime);

            btnMenu = itemView.findViewById(R.id.btnMenu);
            menuContainer = itemView.findViewById(R.id.menuContainer);

            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEditText = itemView.findViewById(R.id.btnEditText);
            btnEditTime = itemView.findViewById(R.id.btnEditTime);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        Note note = list.get(position);

        h.title.setText(note.title);
        h.text.setText(note.text);

        String formatted = new SimpleDateFormat(
                "dd.MM.yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date(note.timeMillis));

        h.time.setText(formatted);


        h.itemView.setOnClickListener(v -> listener.onClick(note));

        h.btnMenu.setOnClickListener(v -> {
            if (h.menuContainer.getVisibility() == View.VISIBLE) {
                h.menuContainer.setVisibility(View.GONE);
            } else {
                h.menuContainer.setVisibility(View.VISIBLE);
            }
        });


        h.btnDelete.setOnClickListener(v -> {
            listener.onDelete(note);
            h.menuContainer.setVisibility(View.GONE);
        });


        h.btnEditText.setOnClickListener(v -> {
            listener.onEditText(note);
            h.menuContainer.setVisibility(View.GONE);
        });


        h.btnEditTime.setOnClickListener(v -> {
            listener.onEditTime(note);
            h.menuContainer.setVisibility(View.GONE);
        });

        h.menuContainer.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}