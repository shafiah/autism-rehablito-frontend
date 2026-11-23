package com.autismcentre.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.google.android.material.button.MaterialButton;

public class HelpSupportActivity extends BaseParentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================================================
        // PAGE CONTENT
        // =====================================================

        showPageContent(R.layout.activity_help_support);

        // =====================================================
        // PAGE TITLE
        // =====================================================

        setPageTitle("Rehablito Autism Centre");

        // =====================================================
        // CONTACT US - EMAIL
        // =====================================================

        MaterialButton btnContactUs =
                findViewById(R.id.btnContactUs);

        btnContactUs.setOnClickListener(v -> {

            Intent intent = new Intent(
                    Intent.ACTION_SENDTO
            );

            intent.setData(
                    Uri.parse("mailto:support.rehablitoautismcenter@gmail.com")
            );

            intent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Rehablito Physio & Autism Centre - Parent Support"
            );

            intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Hello Rehablito Physio & Autism Centre Support Team,\n\n"
                            + "I need help regarding my parent account.\n\n"
                            + "Thank you."
            );

            try {

                startActivity(intent);

            } catch (ActivityNotFoundException e) {

                Toast.makeText(
                        this,
                        "No email application found",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // =====================================================
        // WHATSAPP SUPPORT
        // =====================================================

        MaterialButton btnWhatsAppSupport =
                findViewById(R.id.btnWhatsAppSupport);

        btnWhatsAppSupport.setOnClickListener(v -> {

            String phoneNumber = "919204786220";

            String message =
                    "Hello Rehablito Physio & Autism Centre Support Team, "
                            + "I need help regarding my parent account.";

            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                            "https://wa.me/"
                                    + phoneNumber
                                    + "?text="
                                    + Uri.encode(message)
                    )
            );

            try {

                startActivity(intent);

            } catch (ActivityNotFoundException e) {

                Toast.makeText(
                        this,
                        "WhatsApp is not installed",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // =====================================================
    // CURRENT BOTTOM NAVIGATION
    // =====================================================

    @Override
    protected int getCurrentBottomNavItem() {

        return R.id.nav_home;
    }
}