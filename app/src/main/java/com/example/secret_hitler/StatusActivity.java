package com.example.secret_hitler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StatusActivity extends AppCompatActivity {

    public DBHandler dbHandler;
    private TextView lawCountTextView;
    private TextView leaderTextView;
    private Button selectLawsBtn;
    private Button statusRefreshBtn;
    private Button clearLawsBtn;
    private Button chooseChancellorBtn;
    private Player thisPlayer;
    private String presidentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        lawCountTextView = findViewById(R.id.lawCountTextView);
        leaderTextView = findViewById(R.id.leaderTextView);
        selectLawsBtn = findViewById(R.id.selectLawsBtn);
        statusRefreshBtn = findViewById(R.id.statusRefreshBtn);
        clearLawsBtn = findViewById(R.id.clearLawsBtn);
        chooseChancellorBtn = findViewById(R.id.chooseChancellorBtn);
        chooseChancellorBtn.setVisibility(View.INVISIBLE);
        dbHandler = DBHandler.getInstance(getApplicationContext());
        presidentName = dbHandler.GetPresidentName();

        int fascistLawCount = dbHandler.GetLawCount("Fascist");
        int liberalLawCount = dbHandler.GetLawCount("Liberal");
        lawCountTextView.setText("There are " + fascistLawCount + " Fascist laws and " + liberalLawCount + " Liberal laws.");

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        if (thisPlayer.isPresident) {
            leaderTextView.setText("You are the President.");
            chooseChancellorBtn.setVisibility(View.VISIBLE);
        } else if (thisPlayer.isChancellor) {
            leaderTextView.setText("You are the Chancellor.");
        } else {
            leaderTextView.setText(presidentName + " is the current president.");
        }

        selectLawsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHandler.AddLaw("Fascist");
            }
        });

        statusRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent refresh = getIntent();
                finish();
                startActivity(refresh);
            }
        });

        clearLawsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHandler.ClearTable("gameBoard");
            }
        });

        chooseChancellorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseChancellorIntent = new Intent(getApplicationContext(), ChooseChancellorActivity.class);
                chooseChancellorIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                startActivity(chooseChancellorIntent);
            }
        });
    }
}
