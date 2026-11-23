
package com.autismcentre.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ParentChildTherapistResponse;
import com.autismcentre.repository.ParentChildRepository;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import okhttp3.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChildProfileActivity extends BaseParentActivity {

    private static final String TAG = "ChildProfile";

    private ParentChildRepository repository;
    private Long childId;

    private TextView tvChildName;
    private TextView tvAge;
    private TextView tvGender;
    private TextView tvDob;
    private TextView tvMedicalNotes;
    private TextView tvActiveTherapies;
    private TextView tvCentreName;
    private TextView tvCentreLocation;

    private ImageView imgChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_child_profile);

        // Initialize API client before creating the repository
        ApiClient.initialize(getApplicationContext());


        repository = new ParentChildRepository();

        tvChildName = findViewById(R.id.tvChildName);
        tvAge = findViewById(R.id.tvAge);
        tvGender = findViewById(R.id.tvGender);
        tvDob = findViewById(R.id.tvDob);
        tvMedicalNotes = findViewById(R.id.tvMedicalNotes);
        tvActiveTherapies = findViewById(R.id.tvActiveTherapies);
        tvCentreName = findViewById(R.id.tvCentreName);
        tvCentreLocation = findViewById(R.id.tvCentreLocation);
        imgChild = findViewById(R.id.imgChild);

        childId = getIntent().getLongExtra("childId", -1L);

        if (childId <= 0) {
            Toast.makeText(
                    this,
                    "Child ID not available. Please return to Home.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        loadChildProfile();
    }

    private void loadChildProfile() {

        repository.getMyChildById(
                childId,
                new Callback<ParentChildResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentChildResponse> call,
                            Response<ParentChildResponse> response
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            ParentChildResponse child = response.body();

                            // Child name
                            tvChildName.setText(
                                    safeValue(
                                            child.getChildName(),
                                            "Child"
                                    )
                            );
                            loadChildImage();

                            // Date of birth and calculated age
                            String dob = child.getDateOfBirth();

                            tvDob.setText(
                                    "Date of Birth: "
                                            + formatDate(dob)
                            );

                            tvAge.setText(
                                    "Age: " + calculateAge(dob)
                            );

                            // Gender
                            tvGender.setText(
                                    "Gender: "
                                            + safeValue(
                                            child.getGender(),
                                            "Not available"
                                    )
                            );

                            // Medical notes
                            tvMedicalNotes.setText(
                                    "Medical Notes: "
                                            + safeValue(
                                            child.getMedicalNotes(),
                                            "No medical notes available"
                                    )
                            );

                            // Centre and branch information
                            tvCentreName.setText(
                                    "Rehablito Autism Centre"
                            );

                            tvCentreLocation.setText(
                                    "Branch: "
                                            + safeValue(
                                            child.getBranchName(),
                                            "Not available"
                                    )
                            );

                            // Load actual assigned therapies
                            loadChildTherapists();

                        } else {
                            Log.e(
                                    TAG,
                                    "Profile API failed: HTTP "
                                            + response.code()
                            );

                            Toast.makeText(
                                    ChildProfileActivity.this,
                                    "Child profile load nahi ho paayi.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ParentChildResponse> call,
                            Throwable t
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG, "Profile request failed", t);

                        Toast.makeText(
                                ChildProfileActivity.this,
                                "Server se child profile nahi mil paayi.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void loadChildImage() {

        imgChild.setImageResource(
                android.R.drawable.ic_menu_gallery
        );

        Log.d(TAG, "Requesting image for childId: " + childId);

        ApiClient.getApiService().getMyChildImage(childId)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response) {

                        Log.d(TAG, "Image API HTTP: " + response.code());

                        if (isFinishing() || isDestroyed()) {
                            if (response.body() != null) {
                                response.body().close();
                            }
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            try {
                                Log.d(TAG, "Image content type: "
                                        + response.body().contentType());

                                byte[] imageBytes = response.body().bytes();

                                Log.d(TAG, "Image bytes received: "
                                        + imageBytes.length);

                                Bitmap bitmap = BitmapFactory.decodeByteArray(
                                        imageBytes,
                                        0,
                                        imageBytes.length
                                );

                                if (bitmap != null) {
                                    imgChild.setImageBitmap(bitmap);
                                    Log.d(TAG, "Image displayed successfully");
                                } else {
                                    Log.e(TAG, "Bitmap decoding returned null");
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Image display exception", e);
                            } finally {
                                response.body().close();
                            }

                        } else {
                            try {
                                if (response.errorBody() != null) {
                                    Log.e(TAG, "Image API error: "
                                            + response.errorBody().string());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading API response", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t) {

                        Log.e(TAG, "Image API network failure", t);
                    }
                });
    }

    private void loadChildTherapists() {

        tvActiveTherapies.setText(
                "Loading assigned therapies..."
        );

        repository.getMyChildTherapists(
                childId,
                new Callback<List<ParentChildTherapistResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildTherapistResponse>> call,
                            Response<List<ParentChildTherapistResponse>> response
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<ParentChildTherapistResponse> therapists =
                                    response.body();

                            StringBuilder therapyNames =
                                    new StringBuilder();

                            for (ParentChildTherapistResponse therapist
                                    : therapists) {

                                if (!Boolean.TRUE.equals(
                                        therapist.getActive())) {
                                    continue;
                                }

                                String therapyType =
                                        therapist.getTherapyType();

                                if (therapyType == null
                                        || therapyType.trim().isEmpty()) {
                                    continue;
                                }

                                if (therapyNames.length() > 0) {
                                    therapyNames.append("\n");
                                }

                                therapyNames
                                        .append("\u2022 ")
                                        .append(therapyType);
                            }

                            if (therapyNames.length() == 0) {
                                tvActiveTherapies.setText(
                                        "No active therapies assigned"
                                );
                            } else {
                                tvActiveTherapies.setText(
                                        therapyNames.toString()
                                );
                            }

                        } else {
                            Log.e(
                                    TAG,
                                    "Therapist API failed: HTTP "
                                            + response.code()
                            );

                            tvActiveTherapies.setText(
                                    "Unable to load assigned therapies"
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildTherapistResponse>> call,
                            Throwable t
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(
                                TAG,
                                "Therapist request failed",
                                t
                        );

                        tvActiveTherapies.setText(
                                "Unable to load assigned therapies"
                        );
                    }
                }
        );
    }

    private String formatDate(String dob) {

        if (dob == null || dob.trim().isEmpty()) {
            return "Not available";
        }

        SimpleDateFormat inputFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                );

        inputFormat.setLenient(false);

        SimpleDateFormat outputFormat =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                );

        try {
            Date date = inputFormat.parse(dob);

            if (date != null) {
                return outputFormat.format(date);
            }

        } catch (ParseException e) {
            Log.e(TAG, "Unable to parse date of birth", e);
        }

        return dob;
    }

    private String calculateAge(String dob) {

        if (dob == null || dob.trim().isEmpty()) {
            return "Not available";
        }

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                );

        dateFormat.setLenient(false);

        try {
            Date birthDate = dateFormat.parse(dob);

            if (birthDate == null) {
                return "Not available";
            }

            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);

            Calendar today = Calendar.getInstance();

            if (birth.after(today)) {
                return "Not available";
            }

            int years = today.get(Calendar.YEAR)
                    - birth.get(Calendar.YEAR);

            int months = today.get(Calendar.MONTH)
                    - birth.get(Calendar.MONTH);

            int days = today.get(Calendar.DAY_OF_MONTH)
                    - birth.get(Calendar.DAY_OF_MONTH);

            if (days < 0) {
                months--;

                Calendar previousMonth =
                        (Calendar) today.clone();

                previousMonth.add(Calendar.MONTH, -1);

                days += previousMonth.getActualMaximum(
                        Calendar.DAY_OF_MONTH
                );
            }

            if (months < 0) {
                years--;
                months += 12;
            }

            if (years < 0) {
                return "Not available";
            }

            if (years == 0 && months == 0) {
                return days + (days == 1 ? " day" : " days");
            }

            if (years == 0) {
                return months
                        + (months == 1 ? " month" : " months");
            }

            if (months == 0) {
                return years
                        + (years == 1 ? " year" : " years");
            }

            return years
                    + (years == 1 ? " year " : " years ")
                    + months
                    + (months == 1 ? " month" : " months");

        } catch (ParseException e) {
            Log.e(TAG, "Unable to calculate age", e);
            return "Not available";
        }
    }

    private String safeValue(String value, String fallback) {
        return value != null && !value.trim().isEmpty()
                ? value
                : fallback;
    }

    @Override
    protected int getCurrentBottomNavItem() {

        return R.id.nav_profile;
    }
}
