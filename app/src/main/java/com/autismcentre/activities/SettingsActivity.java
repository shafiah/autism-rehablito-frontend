package com.autismcentre.activities;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.autismcentre.R;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends BaseParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load Settings page content
        showPageContent(R.layout.activity_settings);

        // Header title
        setPageTitle("Autism Centre");

        // Delete Account
        MaterialButton btnDeleteAccount =
                findViewById(R.id.btnDeleteAccount);

        btnDeleteAccount.setOnClickListener(v ->
                showDeleteAccountDialog()
        );
    }

    private void showDeleteAccountDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Account?")
                .setMessage(
                        "Are you sure you want to delete your account?\n\n" +
                                "This action cannot be undone."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            // Actual account deletion
                            // will be connected with backend later.
                        }
                )
                .show();
    }

    @Override
    protected int getCurrentBottomNavItem() {

        // Settings is not part of Bottom Navigation.
        return R.id.nav_home;
    }
}