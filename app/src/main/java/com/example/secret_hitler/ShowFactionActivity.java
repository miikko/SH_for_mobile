package com.example.secret_hitler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowFactionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_faction);

        ImageView factionImageView = findViewById(R.id.factionImageView);

        if (getIntent().hasExtra("com.example.secret_hitler.FACTION")) {
            String faction = getIntent().getExtras().get("com.example.secret_hitler.FACTION").toString();

            if (faction.equals("Fascist")) {
                factionImageView.setImageResource(R.drawable.fascist_membercard);
            } else {
                factionImageView.setImageResource(R.drawable.liberal_membercard);
            }
        }
    }
}
