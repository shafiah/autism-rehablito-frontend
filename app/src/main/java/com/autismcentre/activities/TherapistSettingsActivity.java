package com.autismcentre.activities;

import android.os.Bundle;

import com.autismcentre.R;

public class TherapistSettingsActivity extends BaseTherapistActivity {

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
                R.layout.activity_therapist_settings
        );
    }

    // =====================================================
    // CURRENT BOTTOM NAVIGATION ITEM
    // =====================================================

    @Override
    protected int getCurrentBottomNavItem() {

        return R.id.therapist_nav_home;
    }
}