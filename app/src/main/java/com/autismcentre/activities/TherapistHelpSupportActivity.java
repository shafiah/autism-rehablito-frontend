package com.autismcentre.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.autismcentre.R;
import com.google.android.material.button.MaterialButton;

public class TherapistHelpSupportActivity extends BaseTherapistActivity {

    private MaterialButton btnEmailSupport;
    private MaterialButton btnWhatsappSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================================================
        // PAGE TITLE
        // =====================================================

        setPageTitle("Rehablito Autism Center");

        // =====================================================
        // PAGE CONTENT
        // =====================================================

        showPageContent(
                R.layout.activity_therapist_help_support
        );

        // =====================================================
        // FIND VIEWS
        // =====================================================

        btnEmailSupport =
                findViewById(R.id.btnTherapistEmailSupport);

        btnWhatsappSupport =
                findViewById(R.id.btnTherapistWhatsappSupport);

        // =====================================================
        // EMAIL SUPPORT
        // =====================================================

        btnEmailSupport.setOnClickListener(v -> {

            Intent intent = new Intent(
                    Intent.ACTION_SENDTO
            );

            intent.setData(
                    Uri.parse("mailto:support.rehablitoautismcenter@gmail.com")
            );

            startActivity(intent);
        });

        // =====================================================
        // WHATSAPP SUPPORT
        // =====================================================

        btnWhatsappSupport.setOnClickListener(v -> {

            Intent intent = new Intent(
                    Intent.ACTION_VIEW
            );

            intent.setData(
                    Uri.parse("https://wa.me/919204786220")
            );

            startActivity(intent);
        });
    }

    // =====================================================
    // CURRENT BOTTOM NAVIGATION ITEM
    // =====================================================

    @Override
    protected int getCurrentBottomNavItem() {

        return R.id.therapist_nav_home;
    }
}