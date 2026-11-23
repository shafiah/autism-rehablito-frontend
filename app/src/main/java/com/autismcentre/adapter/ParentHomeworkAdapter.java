
package com.autismcentre.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.autismcentre.R;
import com.autismcentre.model.ParentHomeworkResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ParentHomeworkAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_HOMEWORK = 1;

    private final List<Object> items = new ArrayList<>();

    public interface OnHomeworkClickListener {
        void onHomeworkClick(ParentHomeworkResponse homework);
    }

    private final OnHomeworkClickListener listener;

    public ParentHomeworkAdapter(OnHomeworkClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Object> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String
                ? TYPE_HEADER
                : TYPE_HOMEWORK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        LayoutInflater inflater =
                LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(
                    R.layout.item_homework_header,
                    parent,
                    false
            );
            return new HeaderViewHolder(view);
        }

        View view = inflater.inflate(
                R.layout.item_parent_homework,
                parent,
                false
        );

        return new HomeworkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position) {

        Object item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder)
                    .tvHeader.setText((String) item);
            return;
        }

        ParentHomeworkResponse homework =
                (ParentHomeworkResponse) item;

        HomeworkViewHolder homeworkHolder =
                (HomeworkViewHolder) holder;

        homeworkHolder.tvTitle.setText(
                displayValue(homework.getTitle(), "Homework")
        );

        homeworkHolder.tvStatus.setText(
                displayValue(homework.getStatus(), "Status unavailable")
        );

        homeworkHolder.tvInstructions.setText(
                displayValue(
                        homework.getInstructions(),
                        "No instructions provided."
                )
        );

        homeworkHolder.tvTherapist.setText(
                "Therapist: "
                        + displayValue(
                        homework.getTherapistName(),
                        "Not available"
                )
        );

        homeworkHolder.tvDueDate.setText(
                "Due Date: "
                        + displayValue(
                        homework.getDueDate(),
                        "Not specified"
                )
        );

        homeworkHolder.btnView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHomeworkClick(homework);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String displayValue(String value, String fallback) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value.trim();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView tvHeader;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvTherapyHeader);
        }
    }

    static class HomeworkViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvStatus;
        TextView tvInstructions;
        TextView tvTherapist;
        TextView tvDueDate;
        MaterialButton btnView;

        HomeworkViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvHomeworkItemTitle);
            tvStatus = itemView.findViewById(R.id.tvHomeworkItemStatus);
            tvInstructions = itemView.findViewById(R.id.tvHomeworkItemInstructions);
            tvTherapist = itemView.findViewById(R.id.tvHomeworkItemTherapist);
            tvDueDate = itemView.findViewById(R.id.tvHomeworkItemDueDate);
            btnView = itemView.findViewById(R.id.btnViewHomeworkItem);
        }
    }
}
