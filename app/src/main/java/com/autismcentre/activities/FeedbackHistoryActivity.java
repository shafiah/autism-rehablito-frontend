
package com.autismcentre.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ParentChildTherapistResponse;
import com.autismcentre.model.ParentFeedbackResponse;
import com.autismcentre.repository.ParentChildRepository;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackHistoryActivity extends BaseParentActivity {
    private static final String TAG = "FeedbackHistory";
    private LinearLayout layoutFeedbackHistory;
    private TextView tvFeedbackHistoryMessage;

    private ApiService apiService;
    private ParentChildRepository childRepository;

    private final Map<Long, String> childNames = new HashMap<>();
    private final Map<Long, String> therapistNames = new HashMap<>();
    private final Map<Long, String> therapyTypes = new HashMap<>();

    private List<ParentFeedbackResponse> feedbackRecords =
            new ArrayList<>();

    private int pendingTherapistRequests = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_feedback_history);
        // =====================================================
        // API INITIALIZATION
        // =====================================================

        ApiClient.initialize(getApplicationContext());

        apiService = ApiClient.getApiService();
        childRepository = new ParentChildRepository();

        // =====================================================
        // INITIALIZE VIEWS
        // =====================================================

        layoutFeedbackHistory =
                findViewById(R.id.layoutFeedbackHistory);

        tvFeedbackHistoryMessage =
                findViewById(R.id.tvFeedbackHistoryMessage);

        // =====================================================
        // LOAD FEEDBACK HISTORY
        // =====================================================

        loadFeedbackHistory();
    }

    // =====================================================
    // LOAD PARENT FEEDBACK HISTORY
    // =====================================================

    private void loadFeedbackHistory() {

        showMessage("Loading feedback history...");

        apiService.getParentFeedbackHistory()
                .enqueue(new Callback<List<ParentFeedbackResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentFeedbackResponse>> call,
                            Response<List<ParentFeedbackResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            feedbackRecords = response.body();

                            if (feedbackRecords.isEmpty()) {

                                showMessage("No feedback submitted yet.");
                                return;
                            }

                            loadChildAndTherapistDetails();

                        } else {

                            Log.e(TAG,
                                    "Feedback history API failed. HTTP: "
                                            + response.code());

                            showMessage(
                                    "Unable to load feedback history. Please try again."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentFeedbackResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG,
                                "Feedback history request failed",
                                t);

                        showMessage(
                                "Network error while loading feedback history."
                        );
                    }
                });
    }

    // =====================================================
    // LOAD CHILD AND THERAPIST DETAILS
    // =====================================================

    private void loadChildAndTherapistDetails() {

        childRepository.getMyChildren(
                new Callback<List<ParentChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildResponse>> call,
                            Response<List<ParentChildResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Log.e(TAG,
                                    "Children API failed. HTTP: "
                                            + response.code());

                            displayFeedbackHistory();
                            return;
                        }

                        List<ParentChildResponse> children =
                                response.body();

                        List<Long> childIds = new ArrayList<>();

                        for (ParentChildResponse child : children) {

                            if (child == null
                                    || child.getChildId() == null) {
                                continue;
                            }

                            childNames.put(
                                    child.getChildId(),
                                    safeValue(
                                            child.getChildName(),
                                            "Child #" + child.getChildId()
                                    )
                            );

                            if (!childIds.contains(child.getChildId())) {
                                childIds.add(child.getChildId());
                            }
                        }

                        if (childIds.isEmpty()) {

                            displayFeedbackHistory();
                            return;
                        }

                        pendingTherapistRequests = childIds.size();

                        for (Long childId : childIds) {
                            loadTherapistsForChild(childId);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG,
                                "Children details request failed",
                                t);

                        displayFeedbackHistory();
                    }
                });
    }

    // =====================================================
    // LOAD THERAPISTS FOR EACH CHILD
    // =====================================================

    private void loadTherapistsForChild(Long childId) {

        childRepository.getMyChildTherapists(
                childId,
                new Callback<List<ParentChildTherapistResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildTherapistResponse>> call,
                            Response<List<ParentChildTherapistResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            for (ParentChildTherapistResponse therapist
                                    : response.body()) {

                                if (therapist == null
                                        || therapist.getTherapistId() == null) {
                                    continue;
                                }

                                Long therapistId =
                                        therapist.getTherapistId();

                                therapistNames.put(
                                        therapistId,
                                        safeValue(
                                                therapist.getTherapistName(),
                                                "Therapist #" + therapistId
                                        )
                                );

                                therapyTypes.put(
                                        therapistId,
                                        safeValue(
                                                therapist.getTherapyType(),
                                                "Therapy"
                                        )
                                );
                            }

                        } else {

                            Log.e(TAG,
                                    "Therapist details API failed. Child ID: "
                                            + childId);
                        }

                        onTherapistRequestCompleted();
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildTherapistResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG,
                                "Therapist details request failed. Child ID: "
                                        + childId,
                                t);

                        onTherapistRequestCompleted();
                    }
                });
    }

    // =====================================================
    // TRACK THERAPIST REQUESTS
    // =====================================================

    private void onTherapistRequestCompleted() {

        pendingTherapistRequests--;

        if (pendingTherapistRequests <= 0) {

            displayFeedbackHistory();
        }
    }

    // =====================================================
    // DISPLAY FEEDBACK HISTORY
    // =====================================================

    private void displayFeedbackHistory() {

        if (isFinishing() || isDestroyed()) {
            return;
        }

        layoutFeedbackHistory.removeAllViews();

        if (feedbackRecords == null || feedbackRecords.isEmpty()) {

            showMessage("No feedback submitted yet.");
            return;
        }

        tvFeedbackHistoryMessage.setVisibility(View.GONE);

        for (ParentFeedbackResponse feedback : feedbackRecords) {

            if (feedback == null) {
                continue;
            }

            addFeedbackCard(feedback);
        }

        if (layoutFeedbackHistory.getChildCount() == 0) {

            showMessage("No feedback records available.");
        }

        Log.d(TAG,
                "Feedback history displayed: "
                        + layoutFeedbackHistory.getChildCount());
    }

    // =====================================================
    // CREATE DYNAMIC FEEDBACK CARD
    // =====================================================

    private void addFeedbackCard(ParentFeedbackResponse feedback) {

        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, dp(8), 0, dp(12));

        card.setLayoutParams(cardParams);
        card.setRadius(dp(18));
        card.setCardElevation(dp(2));

        card.setCardBackgroundColor(
                ContextCompat.getColor(
                        this,
                        R.color.parent_card_background
                )
        );

        LinearLayout content = new LinearLayout(this);

        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(20));

        // Therapist Name

        Long therapistId = feedback.getTherapistId();

        String therapistName = therapistId != null
                ? therapistNames.get(therapistId)
                : null;

        TextView tvTherapist = createTextView(
                safeValue(
                        therapistName,
                        therapistId != null
                                ? "Therapist #" + therapistId
                                : "Therapist"
                ),
                20,
                android.R.color.black,
                true
        );

        content.addView(tvTherapist);

        // Therapy Type

        String therapyType = therapistId != null
                ? therapyTypes.get(therapistId)
                : null;

        TextView tvTherapy = createTextView(
                safeValue(therapyType, "Therapy"),
                14,
                android.R.color.darker_gray,
                false
        );

        LinearLayout.LayoutParams therapyParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        therapyParams.topMargin = dp(5);
        tvTherapy.setLayoutParams(therapyParams);

        content.addView(tvTherapy);

        // Child Name

        Long childId = feedback.getChildId();

        String childName = childId != null
                ? childNames.get(childId)
                : null;

        TextView tvChild = createTextView(
                "Child: " + safeValue(
                        childName,
                        childId != null ? "Child #" + childId : "Not available"
                ),
                14,
                R.color.parent_navy,
                true
        );

        LinearLayout.LayoutParams childParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        childParams.topMargin = dp(8);
        tvChild.setLayoutParams(childParams);

        content.addView(tvChild);

        // Rating

        RatingBar ratingBar = new RatingBar(
                this,
                null,
                android.R.attr.ratingBarStyleSmall
        );

        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);
        ratingBar.setIsIndicator(true);

        Integer rating = feedback.getRating();

        ratingBar.setRating(
                rating != null ? rating : 0
        );

        LinearLayout.LayoutParams ratingParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        ratingParams.topMargin = dp(10);
        ratingBar.setLayoutParams(ratingParams);

        content.addView(ratingBar);

        // Feedback Message

        TextView tvMessage = createTextView(
                safeValue(feedback.getMessage(), "No message available"),
                15,
                android.R.color.black,
                false
        );

        LinearLayout.LayoutParams messageParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        messageParams.topMargin = dp(10);
        tvMessage.setLayoutParams(messageParams);

        content.addView(tvMessage);

        // Submission Date

        TextView tvDate = createTextView(
                "Submitted: " + formatDate(feedback.getCreatedAt()),
                13,
                android.R.color.darker_gray,
                false
        );

        LinearLayout.LayoutParams dateParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        dateParams.topMargin = dp(12);
        tvDate.setLayoutParams(dateParams);

        content.addView(tvDate);

        // Feedback Status

        TextView tvStatus = createTextView(
                "Status: " + safeValue(feedback.getStatus(), "UNKNOWN"),
                13,
                R.color.parent_navy,
                true
        );

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        statusParams.topMargin = dp(6);
        tvStatus.setLayoutParams(statusParams);

        content.addView(tvStatus);

        card.addView(content);

        layoutFeedbackHistory.addView(card);
    }

    // =====================================================
    // CREATE TEXT VIEW
    // =====================================================

    private TextView createTextView(
            String text,
            float size,
            int colorResource,
            boolean bold) {

        TextView textView = new TextView(this);

        textView.setText(text);
        textView.setTextSize(size);

        textView.setTextColor(
                ContextCompat.getColor(this, colorResource)
        );

        if (bold) {
            textView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        return textView;
    }

    // =====================================================
    // FORMAT BACKEND IST DATE
    // =====================================================


    private String formatDate(String dateValue) {

        if (dateValue == null || dateValue.trim().isEmpty()) {
            return "Not available";
        }

        try {

            // Backend feedback timestamp is already IST.
            // Ignore fractional seconds because history displays minutes only.

            String timestamp = dateValue.trim();

            if (timestamp.length() >= 19) {
                timestamp = timestamp.substring(0, 19);
            }

            SimpleDateFormat inputFormat =
                    new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.US
                    );

            inputFormat.setLenient(false);

            // Treat backend LocalDateTime as IST, NOT UTC.
            inputFormat.setTimeZone(
                    TimeZone.getTimeZone("Asia/Kolkata")
            );

            Date parsedDate = inputFormat.parse(timestamp);

            if (parsedDate == null) {
                return "Not available";
            }

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.US
                    );

            outputFormat.setTimeZone(
                    TimeZone.getTimeZone("Asia/Kolkata")
            );

            return outputFormat.format(parsedDate);

        } catch (ParseException e) {

            Log.e(
                    TAG,
                    "Unable to parse feedback date: " + dateValue,
                    e
            );

            return "Not available";
        }
    }

    // =====================================================
    // SAFE STRING VALUE
    // =====================================================

    private String safeValue(String value, String fallback) {

        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    // =====================================================
    // LOADING / EMPTY / ERROR MESSAGE
    // =====================================================

    private void showMessage(String message) {

        layoutFeedbackHistory.removeAllViews();

        tvFeedbackHistoryMessage.setText(message);
        tvFeedbackHistoryMessage.setVisibility(View.VISIBLE);
    }

    // =====================================================
    // DP CONVERSION
    // =====================================================

    private int dp(int value) {

        return (int) (
                value * getResources()
                        .getDisplayMetrics().density
        );
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_feedback;
    }
}
