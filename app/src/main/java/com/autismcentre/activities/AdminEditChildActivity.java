
package com.autismcentre.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.model.AdminChildResponse;
import com.autismcentre.model.AdminChildUpdateRequest;
import com.autismcentre.model.AdminParentOptionResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminEditChildActivity extends BaseAdminActivity {

    private TextInputEditText etEditChildName;
    private TextInputEditText etEditChildDob;
    private TextInputEditText etEditMedicalNotes;

    private MaterialAutoCompleteTextView actEditGender;
    private MaterialAutoCompleteTextView actEditParent;
    private MaterialAutoCompleteTextView actEditBranch;

    private MaterialButton btnUpdateChild;

    private ApiService apiService;

    private Long childId;
    private Long selectedParentId;
    private Long selectedBranchId;

    private String selectedGender = "";

    private AdminChildResponse childResponse;

    private final List<AdminParentOptionResponse> parentList =
            new ArrayList<>();

    private final List<AdminBranchResponse> branchList =
            new ArrayList<>();

    private boolean parentsLoaded = false;
    private boolean branchesLoaded = false;
    private boolean childLoaded = false;
    private boolean isUpdating = false;

    private ImageView imgEditChild;

    private final ExecutorService imageExecutor =
            Executors.newSingleThreadExecutor();

    private MaterialButton btnChangeChildPhoto;
    private Uri selectedImageUri = null;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");
        showAdminPageContent(R.layout.activity_admin_edit_child);

        apiService = ApiClient.getApiService();

        initializeEditChildViews();
        setupImagePicker();
        setupGenderDropdown();
        setupParentDropdown();
        setupBranchDropdown();
        setupUpdateChild();

        childId = getIntent().getLongExtra("child_id", -1L);

        if (childId <= 0) {
            Toast.makeText(
                    this,
                    "Child ID not found. Please open Edit Child again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        loadChildDetails();
       // loadParents();
        loadBranches();
    }

    private void initializeEditChildViews() {
        etEditChildName = findViewById(R.id.etEditChildName);
        etEditChildDob = findViewById(R.id.etEditChildDob);
        etEditMedicalNotes = findViewById(R.id.etEditMedicalNotes);

        actEditGender = findViewById(R.id.actEditGender);
        actEditParent = findViewById(R.id.actEditParent);
        actEditBranch = findViewById(R.id.actEditBranch);

        btnUpdateChild = findViewById(R.id.btnUpdateChild);
        imgEditChild = findViewById(R.id.imgEditChild);
        btnChangeChildPhoto = findViewById(R.id.btnChangeChildPhoto);
    }

    private void setupGenderDropdown() {
        String[] genders = {"Male", "Female", "Other"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genders
        );

        actEditGender.setAdapter(adapter);

        actEditGender.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectedGender = genders[position];
                }
        );
    }

    private void setupImagePicker() {

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {

                    if (uri != null) {

                        selectedImageUri = uri;

                        imgEditChild.setImageURI(selectedImageUri);

                        Toast.makeText(
                                this,
                                "New photo selected",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        btnChangeChildPhoto.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );
    }

    private void setupParentDropdown() {
        actEditParent.setOnItemClickListener(
                (parent, view, position, id) -> {
                    if (position >= 0 && position < parentList.size()) {
                        selectedParentId =
                                parentList.get(position).getParentId();
                    }
                }
        );
    }

    private void setupBranchDropdown() {

        actEditBranch.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position >= 0 && position < branchList.size()) {

                        Long newBranchId = branchList.get(position).getId();

                        if (!newBranchId.equals(selectedBranchId)) {

                            selectedBranchId = newBranchId;

                            // Load only parents from newly selected branch.
                            loadParentsForBranch(selectedBranchId);
                        }
                    }
                }
        );
    }
    private void loadChildDetails() {
        apiService.getAdminChildById(childId)
                .enqueue(new Callback<AdminChildResponse>() {
                    @Override
                    public void onResponse(
                            Call<AdminChildResponse> call,
                            Response<AdminChildResponse> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showApiError(
                                    "Unable to load child details",
                                    response.code()
                            );
                            return;
                        }

                        childResponse = response.body();
                        childLoaded = true;

                        selectedBranchId = childResponse.getBranchId();

                        loadParentsForBranch(selectedBranchId);

                        populateFormIfReady();
                    }

                    @Override
                    public void onFailure(
                            Call<AdminChildResponse> call,
                            Throwable t) {

                        showNetworkError("load child details", t);
                    }
                });
    }

    private void loadParentsForBranch(Long branchId) {

        if (branchId == null) {
            return;
        }

        parentsLoaded = false;
        parentList.clear();
        selectedParentId = null;

        actEditParent.setText("", false);
        actEditParent.setAdapter(null);

        apiService.getAdminParentsByBranch(branchId)
                .enqueue(new Callback<List<AdminParentOptionResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminParentOptionResponse>> call,
                            Response<List<AdminParentOptionResponse>> response) {

                        // Ignore an outdated response if branch was changed.
                        if (!branchId.equals(selectedBranchId)) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            showApiError(
                                    "Unable to load parents for selected branch",
                                    response.code()
                            );
                            return;
                        }

                        parentList.clear();
                        parentList.addAll(response.body());

                        List<String> parentNames = new ArrayList<>();

                        for (AdminParentOptionResponse parent : parentList) {

                            String displayName = parent.getName();

                            if (TextUtils.isEmpty(displayName)) {
                                displayName = "Parent";
                            }

                            if (!TextUtils.isEmpty(parent.getEmail())) {
                                displayName += " (" + parent.getEmail() + ")";
                            }

                            parentNames.add(displayName);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                AdminEditChildActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                parentNames
                        );

                        actEditParent.setAdapter(adapter);

                        parentsLoaded = true;

                        populateFormIfReady();
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminParentOptionResponse>> call,
                            Throwable t) {

                        showNetworkError("load branch parents", t);
                    }
                });
    }

    private void loadBranches() {
        apiService.getAdminBranches()
                .enqueue(new Callback<List<AdminBranchResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<AdminBranchResponse>> call,
                            Response<List<AdminBranchResponse>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            showApiError(
                                    "Unable to load branch list",
                                    response.code()
                            );
                            return;
                        }

                        branchList.clear();
                        branchList.addAll(response.body());

                        List<String> branchNames = new ArrayList<>();

                        for (AdminBranchResponse branch : branchList) {
                            branchNames.add(branch.getBranchName());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                AdminEditChildActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                branchNames
                        );

                        actEditBranch.setAdapter(adapter);

                        branchesLoaded = true;
                        populateFormIfReady();
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t) {

                        showNetworkError("load branches", t);
                    }
                });
    }

    private void populateFormIfReady() {
        if (!childLoaded || !parentsLoaded || !branchesLoaded
                || childResponse == null) {
            return;
        }

        etEditChildName.setText(childResponse.getChildName());

        String dateOfBirth = childResponse.getDateOfBirth();

        if (dateOfBirth != null) {
            dateOfBirth = dateOfBirth.trim();

            // Handle an ISO date-time if the backend returns one.
            if (dateOfBirth.length() >= 10
                    && dateOfBirth.charAt(4) == '-'
                    && dateOfBirth.charAt(7) == '-') {
                dateOfBirth = dateOfBirth.substring(0, 10);
            }

            etEditChildDob.setText(dateOfBirth);
        }

        selectedGender = childResponse.getGender() == null
                ? ""
                : childResponse.getGender();

        if (!selectedGender.isEmpty()) {
            actEditGender.setText(
                    formatGender(selectedGender),
                    false
            );
        }

        etEditMedicalNotes.setText(childResponse.getMedicalNotes());

        selectedParentId = childResponse.getParentId();
        selectedBranchId = childResponse.getBranchId();

        selectParentById(selectedParentId);
        selectBranchById(selectedBranchId);
        loadExistingChildImage();

    }
    private void loadExistingChildImage() {

        if (childResponse == null) {
            return;
        }

        apiService.getAdminChildImage(childId)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            // Keep the default placeholder if image is missing.
                            return;
                        }

                        ResponseBody responseBody = response.body();

                        imageExecutor.execute(() -> {

                            Bitmap bitmap = null;

                            try (InputStream inputStream =
                                         responseBody.byteStream()) {

                                bitmap = BitmapFactory.decodeStream(inputStream);

                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }

                            Bitmap finalBitmap = bitmap;

                            runOnUiThread(() -> {

                                if (finalBitmap != null
                                        && !isFinishing()
                                        && !isDestroyed()) {

                                    imgEditChild.setImageBitmap(finalBitmap);
                                }
                            });
                        });
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t) {

                        // Keep placeholder if image cannot be loaded.
                    }
                });
    }

    private String formatGender(String gender) {
        if ("MALE".equalsIgnoreCase(gender)) {
            return "Male";
        }

        if ("FEMALE".equalsIgnoreCase(gender)) {
            return "Female";
        }

        if ("OTHER".equalsIgnoreCase(gender)) {
            return "Other";
        }

        return gender;
    }

    private void selectParentById(Long parentId) {
        if (parentId == null) {
            return;
        }

        for (int i = 0; i < parentList.size(); i++) {
            if (parentId.equals(parentList.get(i).getParentId())) {
                String displayName = parentList.get(i).getName();

                if (TextUtils.isEmpty(displayName)) {
                    displayName = "Parent";
                }

                if (!TextUtils.isEmpty(parentList.get(i).getEmail())) {
                    displayName += " (" + parentList.get(i).getEmail() + ")";
                }

                actEditParent.setText(displayName, false);
                return;
            }
        }

        actEditParent.setText("", false);
        selectedParentId = null;
    }

    private void selectBranchById(Long branchId) {
        if (branchId == null) {
            return;
        }

        for (int i = 0; i < branchList.size(); i++) {
            AdminBranchResponse branch = branchList.get(i);

            if (branchId.equals(branch.getId())) {
                actEditBranch.setText(branch.getBranchName(), false);
                return;
            }
        }

        actEditBranch.setText("", false);
        selectedBranchId = null;
    }

    private void setupUpdateChild() {
        btnUpdateChild.setOnClickListener(v -> updateChild());
    }

    private void updateChild() {
        if (isUpdating) {
            return;
        }

        if (!childLoaded || !parentsLoaded || !branchesLoaded) {
            Toast.makeText(
                    this,
                    "Please wait until child, parent and branch data load",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String childName = getText(etEditChildName);
        String dateOfBirth = getText(etEditChildDob);
        String medicalNotes = getText(etEditMedicalNotes);

        String genderText = getText(actEditGender);

        if (TextUtils.isEmpty(childName)) {
            etEditChildName.setError("Enter child name");
            etEditChildName.requestFocus();
            return;
        }

        if (!isValidDate(dateOfBirth)) {
            etEditChildDob.setError("Use date format yyyy-MM-dd");
            etEditChildDob.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(genderText)) {
            actEditGender.setError("Select gender");
            actEditGender.requestFocus();
            return;
        }

        if (!Arrays.asList("Male", "Female", "Other")
                .contains(genderText)) {
            actEditGender.setError("Select a gender from the list");
            actEditGender.requestFocus();
            return;
        }

        if (selectedParentId == null) {
            actEditParent.setError("Select a parent");
            actEditParent.requestFocus();
            return;
        }

        if (selectedBranchId == null) {
            actEditBranch.setError("Select a branch");
            actEditBranch.requestFocus();
            return;
        }

        /*
         * Preserve the backend's existing gender representation where
         * possible. Confirm the accepted values with the backend contract.
         */
        String genderForRequest = genderText;

        if (childResponse.getGender() != null
                && childResponse.getGender().equalsIgnoreCase(genderText)) {
            genderForRequest = childResponse.getGender();
        }

        AdminChildUpdateRequest request = new AdminChildUpdateRequest(
                childName,
                dateOfBirth,
                genderForRequest,
                selectedParentId,
                selectedBranchId,
                medicalNotes
        );

        isUpdating = true;
        btnUpdateChild.setEnabled(false);
        btnUpdateChild.setText("Saving...");

        apiService.updateAdminChild(childId, request)
                .enqueue(new Callback<AdminChildResponse>() {
                    @Override
                    public void onResponse(
                            Call<AdminChildResponse> call,
                            Response<AdminChildResponse> response) {

                        isUpdating = false;
                        btnUpdateChild.setEnabled(true);
                        btnUpdateChild.setText("Save Changes");

                        if (response.isSuccessful()) {

                            if (selectedImageUri != null) {

                                // Upload selected photo after details are updated.
                                uploadSelectedChildImage();

                            } else {

                                // No new photo selected. Keep existing photo unchanged.
                                Toast.makeText(
                                        AdminEditChildActivity.this,
                                        "Child details updated successfully",
                                        Toast.LENGTH_SHORT
                                ).show();

                                setResult(RESULT_OK);
                                finish();
                            }

                        } else {
                            showApiError(
                                    "Failed to update child",
                                    response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminChildResponse> call,
                            Throwable t) {

                        isUpdating = false;
                        btnUpdateChild.setEnabled(true);
                        btnUpdateChild.setText("Save Changes");

                        showNetworkError("update child", t);
                    }
                });
    }

    private void uploadSelectedChildImage() {

        if (selectedImageUri == null) {
            return;
        }

        btnUpdateChild.setEnabled(false);
        btnUpdateChild.setText("Uploading Photo...");

        imageExecutor.execute(() -> {

            try {

                String contentType =
                        getContentResolver().getType(selectedImageUri);

                if (contentType == null) {
                    contentType = "image/jpeg";
                }

                if (!contentType.equalsIgnoreCase("image/jpeg")
                        && !contentType.equalsIgnoreCase("image/png")
                        && !contentType.equalsIgnoreCase("image/webp")) {

                    runOnUiThread(() -> {

                        btnUpdateChild.setEnabled(true);
                        btnUpdateChild.setText("Save Changes");

                        Toast.makeText(
                                AdminEditChildActivity.this,
                                "Please select JPG, PNG or WEBP image",
                                Toast.LENGTH_LONG
                        ).show();
                    });

                    return;
                }

                byte[] imageBytes;

                try (InputStream inputStream =
                             getContentResolver().openInputStream(
                                     selectedImageUri
                             );
                     ByteArrayOutputStream outputStream =
                             new ByteArrayOutputStream()) {

                    if (inputStream == null) {
                        throw new IOException("Unable to read selected image");
                    }

                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {

                        outputStream.write(buffer, 0, bytesRead);

                        if (outputStream.size() > 5 * 1024 * 1024) {
                            throw new IOException(
                                    "Image size must not exceed 5 MB"
                            );
                        }
                    }

                    imageBytes = outputStream.toByteArray();
                }

                String extension;

                if (contentType.equalsIgnoreCase("image/png")) {
                    extension = ".png";
                } else if (contentType.equalsIgnoreCase("image/webp")) {
                    extension = ".webp";
                } else {
                    extension = ".jpg";
                }

                RequestBody requestBody = RequestBody.create(
                        MediaType.parse(contentType),
                        imageBytes
                );

                MultipartBody.Part imagePart =
                        MultipartBody.Part.createFormData(
                                "image",
                                "child_image" + extension,
                                requestBody
                        );

                runOnUiThread(() -> {

                    apiService.uploadAdminChildImage(
                            childId,
                            imagePart
                    ).enqueue(new Callback<AdminChildResponse>() {

                        @Override
                        public void onResponse(
                                Call<AdminChildResponse> call,
                                Response<AdminChildResponse> response) {

                            btnUpdateChild.setEnabled(true);
                            btnUpdateChild.setText("Save Changes");

                            if (response.isSuccessful()
                                    && response.body() != null) {

                                Toast.makeText(
                                        AdminEditChildActivity.this,
                                        "Child details and photo updated successfully",
                                        Toast.LENGTH_LONG
                                ).show();

                                setResult(RESULT_OK);
                                finish();

                            } else {

                                Toast.makeText(
                                        AdminEditChildActivity.this,
                                        "Details updated, but photo upload failed. HTTP: "
                                                + response.code()
                                                + ". Please try Save Changes again.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<AdminChildResponse> call,
                                Throwable t) {

                            btnUpdateChild.setEnabled(true);
                            btnUpdateChild.setText("Save Changes");

                            Toast.makeText(
                                    AdminEditChildActivity.this,
                                    "Details updated, but photo upload failed: "
                                            + t.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
                });

            } catch (Exception exception) {

                runOnUiThread(() -> {

                    btnUpdateChild.setEnabled(true);
                    btnUpdateChild.setText("Save Changes");

                    Toast.makeText(
                            AdminEditChildActivity.this,
                            "Unable to prepare photo: "
                                    + exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private String getText(TextInputEditText field) {
        return field.getText() == null
                ? ""
                : field.getText().toString().trim();
    }

    private String getText(MaterialAutoCompleteTextView field) {
        return field.getText() == null
                ? ""
                : field.getText().toString().trim();
    }


    private boolean isValidDate(String value) {
        if (TextUtils.isEmpty(value)
                || !value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }

        java.text.SimpleDateFormat dateFormat =
                new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);

        dateFormat.setLenient(false);

        try {
            java.util.Date parsedDate = dateFormat.parse(value);

            return parsedDate != null
                    && dateFormat.format(parsedDate).equals(value);

        } catch (java.text.ParseException e) {
            return false;
        }
    }

    private void showApiError(String message, int code) {
        Toast.makeText(
                this,
                message + " (HTTP " + code + ")",
                Toast.LENGTH_LONG
        ).show();
    }

    private void showNetworkError(String action, Throwable t) {
        String details = t.getMessage();

        if (TextUtils.isEmpty(details)) {
            details = "Please check your network connection";
        }

        Toast.makeText(
                this,
                "Unable to " + action + ": " + details,
                Toast.LENGTH_LONG
        ).show();
    }
}
