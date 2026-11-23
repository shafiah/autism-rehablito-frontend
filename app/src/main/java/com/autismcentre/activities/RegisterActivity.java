
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autismcentre.R;
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.model.ParentRegisterRequest;
import com.autismcentre.model.ParentRegisterResponse;
import com.autismcentre.repository.AdminBranchRepository;
import com.autismcentre.repository.AuthRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextView tvLogin;
    private MaterialButton btnRegister;

    private TextInputEditText etFullName;
    private TextInputEditText etMobile;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private AutoCompleteTextView actSelectBranch;

    private AuthRepository authRepository;
    private AdminBranchRepository branchRepository;


    private final List<AdminBranchResponse> branchList =
            new ArrayList<>();

    private Long selectedBranchId;
    private static final String TAG = "RegisterBranchDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        etFullName = findViewById(R.id.etFullName);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        actSelectBranch = findViewById(R.id.actSelectBranch);

        authRepository = new AuthRepository(this);
        branchRepository = new AdminBranchRepository();
        Log.e("RegisterBranchDebug", "REGISTER ACTIVITY OPENED");

        loadBranchesFromApi();

        actSelectBranch.setThreshold(0);

        actSelectBranch.setOnClickListener(v -> {
            if (actSelectBranch.isEnabled()) {
                actSelectBranch.showDropDown();
            }
        });

        actSelectBranch.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < branchList.size()) {
                AdminBranchResponse selected =
                        branchList.get(position);

                selectedBranchId = selected.getId();
            }
        });

        btnRegister.setOnClickListener(v -> {
            if (validateRegister()) {
                registerParent();
            }
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            RegisterActivity.this,
                            LoginActivity.class
                    )
            );
            finish();
        });
    }

    //Log.d("RegisterBranchDebug", "BRANCH API METHOD CALLED");
    private void loadBranchesFromApi() {
        Log.e("RegisterBranchDebug", "BRANCH API METHOD CALLED");

        Log.d(TAG, "loadBranchesFromApi() started");

        actSelectBranch.setEnabled(false);
        actSelectBranch.setHint("Loading branches...");

        branchRepository.getAdminBranches(
                new Callback<List<AdminBranchResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminBranchResponse>> call,
                            Response<List<AdminBranchResponse>> response
                    ) {

                        if (isFinishing() || isDestroyed()) {
                            Log.d(TAG, "Activity destroyed, ignoring response");
                            return;
                        }

                        Log.d(TAG, "API URL: " + call.request().url());
                        Log.d(TAG, "HTTP Code: " + response.code());
                        Log.d(TAG, "Response successful: " + response.isSuccessful());

                        if (response.body() != null) {

                            Log.d(TAG, "Total branches received: "
                                    + response.body().size());

                            for (AdminBranchResponse branch : response.body()) {

                                if (branch == null) {
                                    Log.e(TAG, "Branch object is NULL");
                                    continue;
                                }

                                Log.d(TAG,
                                        "Branch ID: " + branch.getId()
                                                + ", Name: " + branch.getBranchName()
                                                + ", Active: " + branch.getActive()
                                );
                            }

                        } else {
                            Log.e(TAG, "API response body is NULL");
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            branchList.clear();

                            for (AdminBranchResponse branch : response.body()) {

                                if (branch != null
                                        && Boolean.TRUE.equals(branch.getActive())
                                        && branch.getId() != null) {

                                    branchList.add(branch);
                                }
                            }

                            Log.d(TAG, "Active branches added: "
                                    + branchList.size());

                            if (branchList.isEmpty()) {

                                Log.e(TAG, "No active branches found");

                                actSelectBranch.setHint(
                                        "No active branches available"
                                );
                                actSelectBranch.setEnabled(false);
                                return;
                            }

                            List<String> branchNames = new ArrayList<>();

                            for (AdminBranchResponse branch : branchList) {
                                branchNames.add(branch.getBranchName());
                            }

                            Log.d(TAG, "Dropdown names: " + branchNames);

                            ArrayAdapter<String> adapter =
                                    new ArrayAdapter<>(
                                            RegisterActivity.this,
                                            android.R.layout.simple_dropdown_item_1line,
                                            branchNames
                                    );

                            actSelectBranch.setAdapter(adapter);
                            actSelectBranch.setEnabled(true);
                            actSelectBranch.setHint("Select Branch");

                            Log.d(TAG, "Branch dropdown adapter attached successfully");

                        } else {

                            Log.e(TAG, "API failed. HTTP Code: " + response.code());

                            actSelectBranch.setHint("Unable to load branches");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t
                    ) {

                        Log.e(TAG, "Branch API failure", t);

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        actSelectBranch.setEnabled(false);
                        actSelectBranch.setHint("Unable to load branches");
                    }
                }
        );
    }


    private void registerParent() {

        String fullName = getText(etFullName);
        String mobile = getText(etMobile);
        String email = getText(etEmail);
        String password = getText(etPassword);

        ParentRegisterRequest request =
                new ParentRegisterRequest(
                        fullName,
                        email,
                        mobile,
                        password,
                        selectedBranchId
                );

        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        authRepository.registerParent(
                request,
                new Callback<ParentRegisterResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentRegisterResponse> call,
                            Response<ParentRegisterResponse> response
                    ) {

                        btnRegister.setEnabled(true);
                        btnRegister.setText("Register");

                        if (response.isSuccessful()
                                && response.body() != null) {

                            ParentRegisterResponse registerResponse =
                                    response.body();

                            Toast.makeText(
                                    RegisterActivity.this,
                                    registerResponse.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                            Intent intent =
                                    new Intent(
                                            RegisterActivity.this,
                                            LoginActivity.class
                                    );

                            startActivity(intent);
                            finish();

                        } else {
                            handleRegisterError(response);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ParentRegisterResponse> call,
                            Throwable t
                    ) {

                        btnRegister.setEnabled(true);
                        btnRegister.setText("Register");

                        Toast.makeText(
                                RegisterActivity.this,
                                "Unable to connect to server",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    private void handleRegisterError(
            Response<ParentRegisterResponse> response
    ) {

        String message;

        if (response.code() == 400) {
            message = "Invalid registration details";
        } else if (response.code() == 409) {
            message = "Email or mobile number already registered";
        } else if (response.code() == 500) {
            message = "Server error. Please try again later";
        } else {
            message = "Registration failed. Please try again";
        }

        Toast.makeText(
                RegisterActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private boolean validateRegister() {

        String fullName = getText(etFullName);
        String mobile = getText(etMobile);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (fullName.isEmpty()) {
            etFullName.setError("Enter Full Name");
            etFullName.requestFocus();
            return false;
        }

        if (mobile.isEmpty()) {
            etMobile.setError("Enter Mobile Number");
            etMobile.requestFocus();
            return false;
        }

        if (mobile.length() != 10) {
            etMobile.setError("Enter Valid Mobile Number");
            etMobile.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Enter Email");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {
            etEmail.setError("Enter Valid Email");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter Password");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 Characters");
            etPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm Password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(
                    "Password Doesn't Match"
            );
            etConfirmPassword.requestFocus();
            return false;
        }

        if (selectedBranchId == null) {
            actSelectBranch.setError("Please Select Branch");
            actSelectBranch.requestFocus();
            return false;
        }

        return true;
    }
}
