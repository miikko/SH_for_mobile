package com.example.secret_hitler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class StatusActivity extends AppCompatActivity {

    public DBHandler dbHandler;
    private TextView lawCountTextView;
    private TextView leaderTextView;
    private Button selectLawsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        lawCountTextView = findViewById(R.id.lawCountTextView);
        leaderTextView = findViewById(R.id.leaderTextView);
        selectLawsBtn = findViewById(R.id.selectLawsBtn);
        dbHandler = DBHandler.getInstance(getApplicationContext());



    }
}
