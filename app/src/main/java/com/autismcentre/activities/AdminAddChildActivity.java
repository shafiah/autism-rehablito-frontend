
package com.autismcentre.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.model.AdminChildCreateRequest;
import com.autismcentre.model.AdminChildResponse;
import com.autismcentre.model.AdminParentOptionResponse;
import com.autismcentre.repository.AdminBranchRepository;
import com.autismcentre.repository.AdminChildRepository;
import com.autismcentre.repository.AdminParentRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddChildActivity extends BaseAdminActivity {

    private static final String TAG = "AdminChildAPI";

    private TextInputEditText etChildName;
    private TextInputEditText etChildAge;
    private TextInputEditText etMedicalNotes;

    private MaterialAutoCompleteTextView actGender;
    private AutoCompleteTextView actTherapyType;
    private MaterialAutoCompleteTextView actSelectBranch;
    private MaterialAutoCompleteTextView actSelectParent;

    private MaterialButton btnSaveChild;

    private AdminParentRepository adminParentRepository;
    private AdminBranchRepository adminBranchRepository;
    private AdminChildRepository adminChildRepository;

    private final List<AdminParentOptionResponse> parentList =
            new ArrayList<>();

    private final List<AdminBranchResponse> branchList =
            new ArrayList<>();

    //private ArrayAdapter<String> parentAdapter;
    private ParentOptionAdapter parentAdapter;
    private ArrayAdapter<String> branchAdapter;

    private Long selectedParentId = null;
    private Long selectedBranchId = null;

    private String selectedBranch = "";

    private static final int PICK_IMAGE_REQUEST = 1001;

    private ImageView imgChildPhoto;
    private MaterialButton btnSelectChildPhoto;

    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(R.layout.activity_admin_add_child);

        initializeAddChildViews();

        setupDateOfBirthPicker();
        setupGenderDropdown();
        setupTherapyDropdown();

        etChildAge.setText("");

        adminBranchRepository = new AdminBranchRepository();
        adminParentRepository = new AdminParentRepository();
        adminChildRepository = new AdminChildRepository();

        setupSaveChild();

        // First load branches.
        // Parents will load only after branch selection.
        loadBranchesFromApi();
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeAddChildViews() {

        etChildName = findViewById(R.id.etChildName);
        etChildAge = findViewById(R.id.etChildAge);
        etMedicalNotes = findViewById(R.id.etMedicalNotes);

        actGender = findViewById(R.id.actGender);
        actTherapyType = findViewById(R.id.actTherapyType);
        actSelectBranch = findViewById(R.id.actSelectBranch);
        actSelectParent = findViewById(R.id.actSelectParent);

        btnSaveChild = findViewById(R.id.btnSaveChild);
        imgChildPhoto = findViewById(R.id.imgChildPhoto);
        btnSelectChildPhoto = findViewById(R.id.btnSelectChildPhoto);

        btnSelectChildPhoto.setOnClickListener(v -> openImagePicker());

        // Parent dropdown initially disabled.
        actSelectParent.setEnabled(false);
        actSelectParent.setHint("Select Branch First");
    }

    // =====================================================
    // DATE OF BIRTH PICKER
    // =====================================================

    private void setupDateOfBirthPicker() {

        etChildAge.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    AdminAddChildActivity.this,
                    (view, year, month, dayOfMonth) -> {

                        Calendar selectedDate = Calendar.getInstance();

                        selectedDate.set(
                                year,
                                month,
                                dayOfMonth
                        );

                        SimpleDateFormat dateFormat =
                                new SimpleDateFormat(
                                        "yyyy-MM-dd",
                                        Locale.US
                                );

                        String formattedDate =
                                dateFormat.format(selectedDate.getTime());

                        etChildAge.setText(formattedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            dialog.getDatePicker().setMaxDate(
                    System.currentTimeMillis()
            );

            dialog.show();
        });
    }

    // =====================================================
    // GENDER DROPDOWN
    // =====================================================

    private void setupGenderDropdown() {

        String[] genders = {
                "Male",
                "Female",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genders
        );

        actGender.setAdapter(adapter);

        actGender.setOnClickListener(v ->
                actGender.showDropDown()
        );
    }

    // =====================================================
    // THERAPY DROPDOWN
    // =====================================================

    private void setupTherapyDropdown() {

        String[] therapyTypes = {
                "Occupational Therapy",
                "Speech Therapy",
                "Occupational+Speech therapy"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                therapyTypes
        );

        actTherapyType.setAdapter(adapter);

        actTherapyType.setOnClickListener(v ->
                actTherapyType.showDropDown()
        );
    }

    // =====================================================
    // LOAD BRANCHES FROM API
    // =====================================================

    private void loadBranchesFromApi() {

        actSelectBranch.setEnabled(false);
        actSelectBranch.setText("Loading branches...", false);

        adminBranchRepository.getAdminBranches(
                new Callback<List<AdminBranchResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminBranchResponse>> call,
                            Response<List<AdminBranchResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            branchList.clear();

                            List<String> branchNames = new ArrayList<>();

                            for (AdminBranchResponse branch
                                    : response.body()) {

                                if (branch.getId() != null
                                        && branch.getBranchName() != null
                                        && Boolean.TRUE.equals(
                                        branch.getActive())) {

                                    branchList.add(branch);
                                    branchNames.add(
                                            branch.getBranchName()
                                    );
                                }
                            }

                            branchAdapter = new ArrayAdapter<>(
                                    AdminAddChildActivity.this,
                                    android.R.layout
                                            .simple_dropdown_item_1line,
                                    branchNames
                            );

                            actSelectBranch.setAdapter(branchAdapter);
                            actSelectBranch.setText("", false);
                            actSelectBranch.setHint("Select Branch");

                            actSelectBranch.setEnabled(
                                    !branchList.isEmpty()
                            );

                            actSelectBranch.setOnClickListener(v ->
                                    actSelectBranch.showDropDown()
                            );

                            actSelectBranch.setOnItemClickListener(
                                    (parent, view, position, id) -> {

                                        AdminBranchResponse selected =
                                                branchList.get(position);

                                        selectedBranch =
                                                selected.getBranchName();

                                        selectedBranchId =
                                                selected.getId();

                                        // Clear previous parent selection
                                        clearParentSelection();

                                        // Load parents for selected branch
                                        loadParentsByBranch(selectedBranchId);
                                    }
                            );

                            if (branchList.isEmpty()) {
                                Toast.makeText(
                                        AdminAddChildActivity.this,
                                        "No active branches found.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                        } else {

                            Log.e(
                                    TAG,
                                    "Branch API failed. HTTP "
                                            + response.code()
                            );

                            actSelectBranch.setText("", false);
                            actSelectBranch.setHint(
                                    "Unable to load branches"
                            );

                            Toast.makeText(
                                    AdminAddChildActivity.this,
                                    "Branch list load nahi hui. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG, "Branch API request failed", t);

                        actSelectBranch.setText("", false);
                        actSelectBranch.setHint(
                                "Unable to load branches"
                        );

                        Toast.makeText(
                                AdminAddChildActivity.this,
                                "Server se branch list nahi mili.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =====================================================
    // CLEAR PARENT SELECTION
    // =====================================================

    private void clearParentSelection() {

        selectedParentId = null;

        parentList.clear();

        actSelectParent.setText("", false);
        actSelectParent.setAdapter(null);
        actSelectParent.setEnabled(false);
        actSelectParent.setHint("Loading parents...");
    }

    // =====================================================
    // LOAD PARENTS BY SELECTED BRANCH
    // =====================================================

    private void loadParentsByBranch(Long branchId) {

        if (branchId == null) {
            clearParentSelection();
            actSelectParent.setHint("Select Branch First");
            return;
        }

        clearParentSelection();

        adminParentRepository.getAdminParentsByBranch(
                branchId,
                new Callback<List<AdminParentOptionResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminParentOptionResponse>> call,
                            Response<List<AdminParentOptionResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        // Ignore response if admin changed branch
                        if (!branchId.equals(selectedBranchId)) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            parentList.clear();

                            for (AdminParentOptionResponse parent
                                    : response.body()) {

                                if (parent.getParentId() != null
                                        && parent.getName() != null
                                        && Boolean.TRUE.equals(parent.getActive())
                                        && branchId.equals(parent.getBranchId())) {

                                    parentList.add(parent);
                                }
                            }

                            parentAdapter = new ParentOptionAdapter(parentList);
                            actSelectParent.setAdapter(parentAdapter);
                            actSelectParent.setText("", false);
                            actSelectParent.setHint(
                                    "Select Registered Parent"
                            );

                            actSelectParent.setEnabled(
                                    !parentList.isEmpty()
                            );

                            actSelectParent.setOnClickListener(v ->
                                    actSelectParent.showDropDown()
                            );

                            actSelectParent.setOnItemClickListener(
                                    (parentView, view, position, id) -> {

                                        if (position < 0
                                                || position >= parentList.size()) {
                                            return;
                                        }

                                        AdminParentOptionResponse selected =
                                                parentList.get(position);

                                        selectedParentId =
                                                selected.getParentId();

                                        Toast.makeText(
                                                AdminAddChildActivity.this,
                                                "Parent selected: "
                                                        + selected.getName(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                            );

                            if (parentList.isEmpty()) {

                                actSelectParent.setHint(
                                        "No registered parents in this branch"
                                );

                                Toast.makeText(
                                        AdminAddChildActivity.this,
                                        "Is branch mein koi active registered parent nahi mila.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                        } else {

                            actSelectParent.setEnabled(false);
                            actSelectParent.setHint(
                                    "Unable to load parents"
                            );

                            Log.e(
                                    TAG,
                                    "Parent API failed. HTTP "
                                            + response.code()
                            );

                            Toast.makeText(
                                    AdminAddChildActivity.this,
                                    "Parent list load nahi hui. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminParentOptionResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!branchId.equals(selectedBranchId)) {
                            return;
                        }

                        Log.e(
                                "AdminParentAPI",
                                "Branch-wise Parent API failed",
                                t
                        );

                        actSelectParent.setEnabled(false);
                        actSelectParent.setHint(
                                "Unable to connect to server"
                        );

                        Toast.makeText(
                                AdminAddChildActivity.this,
                                "Server se parent list nahi mili.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void openImagePicker() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(
                Intent.createChooser(intent, "Select Child Photo"),
                PICK_IMAGE_REQUEST
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            selectedImageUri = data.getData();

            imgChildPhoto.setImageURI(selectedImageUri);
        }
    }

    // =====================================================
    // SAVE CHILD
    // =====================================================


    private void setupSaveChild() {

        btnSaveChild.setOnClickListener(v -> {

            if (!validateChildForm()) {
                return;
            }

            String childName = etChildName.getText()
                    .toString()
                    .trim();

            String dateOfBirth = etChildAge.getText()
                    .toString()
                    .trim();

            String gender = actGender.getText()
                    .toString()
                    .trim();

            String therapy = actTherapyType.getText()
                    .toString()
                    .trim();

            String medicalNotes = etMedicalNotes.getText()
                    .toString()
                    .trim();

            if (medicalNotes.isEmpty()) {
                medicalNotes = null;
            }

            if (selectedParentId == null
                    || selectedBranchId == null) {

                Toast.makeText(
                        this,
                        "Please select a parent and branch",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            List<String> selectedTherapies = new ArrayList<>();

            switch (therapy) {

                case "Occupational Therapy":
                    selectedTherapies.add("Occupational Therapy");
                    break;

                case "Speech Therapy":
                    selectedTherapies.add("Speech Therapy");
                    break;

                case "Occupational+Speech therapy":
                    selectedTherapies.add("Occupational Therapy");
                    selectedTherapies.add("Speech Therapy");
                    break;

                default:
                    Toast.makeText(
                            this,
                            "Please select a valid therapy type",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
            }

            AdminChildCreateRequest request =
                    new AdminChildCreateRequest(
                            childName,
                            dateOfBirth,
                            gender,
                            selectedParentId,
                            selectedBranchId,
                            medicalNotes,
                            selectedTherapies
                    );

            btnSaveChild.setEnabled(false);
            btnSaveChild.setText("Saving...");

            adminChildRepository.createAdminChild(
                    request,
                    new Callback<AdminChildResponse>() {

                        @Override
                        public void onResponse(
                                Call<AdminChildResponse> call,
                                Response<AdminChildResponse> response) {

                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            if (response.isSuccessful()
                                    && response.body() != null) {

                                Long childId =
                                        response.body().getChildId();

                                if (selectedImageUri != null
                                        && childId != null) {

                                    btnSaveChild.setText("Uploading Photo...");

                                    uploadSelectedChildImage(childId);

                                } else {

                                    Toast.makeText(
                                            AdminAddChildActivity.this,
                                            "Child added successfully",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    finish();
                                }

                            } else {

                                btnSaveChild.setEnabled(true);
                                btnSaveChild.setText("Add Child");

                                String message;

                                if (response.code() == 401
                                        || response.code() == 403) {

                                    message = "Session expired ya permission denied. Admin login check karo.";

                                } else if (response.code() == 400) {

                                    message = "Invalid child details. DOB, gender, therapy, parent aur branch check karo.";

                                } else if (response.isSuccessful()) {

                                    message = "Child created, but response mein child ID nahi mili.";

                                } else {

                                    message = "Child save nahi hua. HTTP "
                                            + response.code();
                                }

                                Log.e(
                                        TAG,
                                        "Create child failed. HTTP "
                                                + response.code()
                                );

                                Toast.makeText(
                                        AdminAddChildActivity.this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<AdminChildResponse> call,
                                Throwable t) {

                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            btnSaveChild.setEnabled(true);
                            btnSaveChild.setText("Add Child");

                            Log.e(
                                    TAG,
                                    "Create child request failed",
                                    t
                            );

                            Toast.makeText(
                                    AdminAddChildActivity.this,
                                    "Server se connect nahi hua. Internet aur backend check karo.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        });
    }

    private void uploadSelectedChildImage(Long childId) {

        try {

            InputStream inputStream =
                    getContentResolver().openInputStream(selectedImageUri);

            if (inputStream == null) {
                showPhotoUploadResult(false);
                return;
            }

            ByteArrayOutputStream byteArrayOutputStream =
                    new ByteArrayOutputStream();

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();

            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            if (imageBytes.length == 0
                    || imageBytes.length > 5 * 1024 * 1024) {

                Toast.makeText(
                        this,
                        "Photo empty hai ya 5 MB se badi hai.",
                        Toast.LENGTH_LONG
                ).show();

                showPhotoUploadResult(false);
                return;
            }

            String mimeType =
                    getContentResolver().getType(selectedImageUri);

            if (mimeType == null
                    || (!mimeType.equals("image/jpeg")
                    && !mimeType.equals("image/png"))) {

                Toast.makeText(
                        this,
                        "Only JPG and PNG images are supported.",
                        Toast.LENGTH_LONG
                ).show();

                showPhotoUploadResult(false);
                return;
            }

            RequestBody requestBody =
                    RequestBody.create(
                            MediaType.parse(mimeType),
                            imageBytes
                    );

            MultipartBody.Part imagePart =
                    MultipartBody.Part.createFormData(
                            "image",
                            "child_photo",
                            requestBody
                    );

            adminChildRepository.uploadAdminChildImage(
                    childId,
                    imagePart,
                    new Callback<AdminChildResponse>() {

                        @Override
                        public void onResponse(
                                Call<AdminChildResponse> call,
                                Response<AdminChildResponse> response) {

                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            showPhotoUploadResult(response.isSuccessful());
                        }

                        @Override
                        public void onFailure(
                                Call<AdminChildResponse> call,
                                Throwable t) {

                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            Log.e(TAG, "Child photo upload failed", t);

                            showPhotoUploadResult(false);
                        }
                    }
            );

        } catch (Exception e) {

            Log.e(TAG, "Unable to read selected child photo", e);

            showPhotoUploadResult(false);
        }
    }

    private void showPhotoUploadResult(boolean uploaded) {

        if (uploaded) {

            Toast.makeText(
                    this,
                    "Child and photo added successfully.",
                    Toast.LENGTH_LONG
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Child added successfully, but photo upload failed. You can upload it later.",
                    Toast.LENGTH_LONG
            ).show();
        }

        finish();
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private boolean validateChildForm() {

        String childName = etChildName.getText()
                .toString()
                .trim();

        String dateOfBirth = etChildAge.getText()
                .toString()
                .trim();

        String gender = actGender.getText()
                .toString()
                .trim();

        String therapy = actTherapyType.getText()
                .toString()
                .trim();

        if (childName.isEmpty()) {
            etChildName.setError("Enter Child Name");
            etChildName.requestFocus();
            return false;
        }

        if (dateOfBirth.isEmpty()) {
            etChildAge.setError("Select Date of Birth");
            etChildAge.requestFocus();
            return false;
        }

        if (gender.isEmpty()) {
            actGender.setError("Select Gender");
            actGender.requestFocus();
            return false;
        }

        if (!gender.equals("Male")
                && !gender.equals("Female")
                && !gender.equals("Other")) {

            actGender.setError("Select a valid gender");
            actGender.requestFocus();
            return false;
        }

        if (therapy.isEmpty()) {
            actTherapyType.setError("Select Therapy Type");
            actTherapyType.requestFocus();
            return false;
        }

        if (selectedBranchId == null) {

            Toast.makeText(
                    this,
                    "Please select a branch",
                    Toast.LENGTH_SHORT
            ).show();

            actSelectBranch.requestFocus();
            return false;
        }

        if (selectedParentId == null) {

            Toast.makeText(
                    this,
                    "Please select a registered parent",
                    Toast.LENGTH_SHORT
            ).show();

            actSelectParent.requestFocus();
            return false;
        }

        return true;
    }

    // =====================================================
// CUSTOM PARENT DROPDOWN ADAPTER
// =====================================================

    private class ParentOptionAdapter
            extends ArrayAdapter<AdminParentOptionResponse> {

        private final List<AdminParentOptionResponse> items;

        ParentOptionAdapter(
                List<AdminParentOptionResponse> parentItems) {

            super(
                    AdminAddChildActivity.this,
                    0,
                    parentItems
            );

            this.items = parentItems;
        }

        // Selected item display
        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent) {

            View view = convertView;

            if (view == null) {
                view = LayoutInflater.from(getContext())
                        .inflate(
                                R.layout.item_admin_parent_option,
                                parent,
                                false
                        );
            }

            TextView tvName =
                    view.findViewById(R.id.tvParentName);

            TextView tvEmail =
                    view.findViewById(R.id.tvParentEmail);

            TextView tvChildLinked =
                    view.findViewById(R.id.tvChildLinked);

            AdminParentOptionResponse item = getItem(position);

            if (item != null) {

                tvName.setText(item.getName());

                tvEmail.setText(
                        item.getEmail() == null
                                ? ""
                                : item.getEmail()
                );

                if (Boolean.TRUE.equals(item.getChildLinked())) {
                    tvChildLinked.setVisibility(View.VISIBLE);
                } else {
                    tvChildLinked.setVisibility(View.GONE);
                }
            }

            return view;
        }
        // Dropdown list display
        @Override
        public View getDropDownView(
                int position,
                View convertView,
                ViewGroup parent) {

            return getView(position, convertView, parent);
        }
    }
}
