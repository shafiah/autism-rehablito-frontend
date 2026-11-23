package com.autismcentre.activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.autismcentre.R;

public class ReceiptActivity extends BaseParentActivity {

  //  private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_receipt);
        showPageContent(R.layout.activity_receipt);

       // btnBack = findViewById(R.id.btnBack);

       // btnBack.setOnClickListener(v -> finish());
    }
    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_payments;
    }
}