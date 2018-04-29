package com.example.secret_hitler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ShowFactionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_faction);

        TextView factionTextView = findViewById(R.id.factionTextView);

        if (getIntent().hasExtra("com.example.secret_hitler.FACTION")) {
            String faction = getIntent().getExtras().get("com.example.secret_hitler.FACTION").toString();
            factionTextView.setText(faction);
        }
    }
}
