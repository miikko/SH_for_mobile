package com.example.secret_hitler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class VotingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        Button jaBtn = findViewById(R.id.jaBtn);
        jaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String votingDecision = "Ja";
                Intent showVoteIntent = new Intent(getApplicationContext(), ShowVoteActivity.class);
                showVoteIntent.putExtra("com.example.secret_hitler.VOTE", votingDecision);
                startActivity(showVoteIntent);
            }
        });

        Button neinBtn = findViewById(R.id.neinBtn);
        neinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String votingDecision = "Nein";
                Intent showVoteIntent = new Intent(getApplicationContext(), ShowVoteActivity.class);
                showVoteIntent.putExtra("com.example.secret_hitler.VOTE", votingDecision);
                startActivity(showVoteIntent);
            }
        });

    }
}
