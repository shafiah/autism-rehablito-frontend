
package com.autismcentre.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.TherapistFeedbackResponse;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistFeedbackActivity extends BaseTherapistActivity {

    private ApiService apiService;

    private LinearLayout layoutFeedbackList;
    private ProgressBar progressFeedback;
    private TextView tvFeedbackEmpty;

    private Call<List<TherapistFeedbackResponse>> feedbackCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_therapist_feedback);
        setPageTitle("Rehalito Autism Centre");

        apiService = ApiClient.getApiService();

        layoutFeedbackList =
                findViewById(R.id.layoutTherapistFeedbackList);

        progressFeedback =
                findViewById(R.id.progressTherapistFeedback);

        tvFeedbackEmpty =
                findViewById(R.id.tvTherapistFeedbackEmpty);

        loadTherapistFeedback();
    }

    private void loadTherapistFeedback() {

        progressFeedback.setVisibility(View.VISIBLE);
        tvFeedbackEmpty.setVisibility(View.GONE);
        layoutFeedbackList.removeAllViews();

        feedbackCall = apiService.getMyTherapistFeedback();

        feedbackCall.enqueue(
                new Callback<List<TherapistFeedbackResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistFeedbackResponse>> call,
                            Response<List<TherapistFeedbackResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        progressFeedback.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<TherapistFeedbackResponse> feedbackList =
                                    response.body();

                            if (feedbackList.isEmpty()) {
                                tvFeedbackEmpty.setVisibility(View.VISIBLE);
                                return;
                            }

                            for (TherapistFeedbackResponse feedback
                                    : feedbackList) {

                                if (feedback != null) {
                                    addFeedbackCard(feedback);
                                }
                            }

                            if (layoutFeedbackList.getChildCount() == 0) {
                                tvFeedbackEmpty.setVisibility(View.VISIBLE);
                            }

                        } else {

                            tvFeedbackEmpty.setText(
                                    "Unable to load feedback. HTTP "
                                            + response.code()
                            );

                            tvFeedbackEmpty.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistFeedbackResponse>> call,
                            Throwable throwable) {

                        if (isFinishing() || isDestroyed()
                                || call.isCanceled()) {
                            return;
                        }

                        progressFeedback.setVisibility(View.GONE);

                        tvFeedbackEmpty.setText(
                                "Unable to load feedback. Please check your connection."
                        );

                        tvFeedbackEmpty.setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    private void addFeedbackCard(
            TherapistFeedbackResponse feedback) {

        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dpToPx(16);

        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(18));
        card.setCardElevation(dpToPx(2));
        card.setCardBackgroundColor(
                ContextCompat.getColor(
                        this,
                        android.R.color.white
                )
        );

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dpToPx(18),
                dpToPx(18),
                dpToPx(18),
                dpToPx(18)
        );

        card.addView(content);

        addText(
                content,
                "Parent: " + safeText(feedback.getParentName()),
                18,
                R.color.parent_navy,
                true,
                0
        );

        addText(
                content,
                "Child: " + safeText(feedback.getChildName()),
                16,
                android.R.color.black,
                false,
                12
        );

        addText(
                content,
                "Rating: " + safeRating(feedback.getRating())
                        + "/5  " + getStars(feedback.getRating()),
                17,
                R.color.parent_navy,
                true,
                12
        );

        addText(
                content,
                safeText(feedback.getMessage()),
                16,
                android.R.color.black,
                false,
                12
        );

        String submittedDate = formatFeedbackDate(
                feedback.getCreatedAt()
        );

        addText(
                content,
                "Submitted: " + submittedDate,
                14,
                android.R.color.darker_gray,
                false,
                14
        );

        TextView statusView = addText(
                content,
                "Status: " + safeText(feedback.getStatus()),
                15,
                R.color.success,
                true,
                12
        );

        if (!"PUBLISHED".equalsIgnoreCase(feedback.getStatus())) {
            statusView.setTextColor(
                    ContextCompat.getColor(
                            this,
                            android.R.color.darker_gray
                    )
            );
        }

        layoutFeedbackList.addView(card);
    }

    private TextView addText(
            LinearLayout parent,
            String value,
            int textSize,
            int textColor,
            boolean bold,
            int topMargin) {

        TextView textView = new TextView(this);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dpToPx(topMargin);

        textView.setLayoutParams(params);
        textView.setText(value);
        textView.setTextSize(textSize);
        textView.setTextColor(
                ContextCompat.getColor(this, textColor)
        );

        if (bold) {
            textView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        parent.addView(textView);

        return textView;
    }

    private String getStars(Integer rating) {

        if (rating == null || rating < 1 || rating > 5) {
            return "Not rated";
        }

        StringBuilder stars = new StringBuilder();

        for (int i = 1; i <= 5; i++) {
            stars.append(i <= rating ? "★" : "☆");
        }

        return stars.toString();
    }

    private String safeRating(Integer rating) {
        return rating == null ? "0" : String.valueOf(rating);
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty()
                ? "Not available"
                : value;
    }

    private String formatFeedbackDate(String dateTime) {

        if (dateTime == null || dateTime.trim().isEmpty()) {
            return "Not available";
        }

        try {
            // Backend feedback timestamp is already in IST.
            // Do not apply another timezone conversion.
            String datePart = dateTime.replace('T', ' ');

            SimpleDateFormat inputFormat =
                    new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                    );

            inputFormat.setLenient(false);

            Date date = inputFormat.parse(
                    datePart.substring(0, 19)
            );

            if (date != null) {
                SimpleDateFormat outputFormat =
                        new SimpleDateFormat(
                                "dd MMM yyyy, hh:mm a",
                                Locale.getDefault()
                        );

                return outputFormat.format(date);
            }

        } catch (ParseException | IndexOutOfBoundsException ignored) {
            // Return original timestamp if formatting fails.
        }

        return dateTime;
    }

    private int dpToPx(int dp) {
        return Math.round(
                dp * getResources().getDisplayMetrics().density
        );
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.therapist_nav_home;
    }

    @Override
    protected void onDestroy() {

        if (feedbackCall != null) {
            feedbackCall.cancel();
        }

        super.onDestroy();
    }
}
