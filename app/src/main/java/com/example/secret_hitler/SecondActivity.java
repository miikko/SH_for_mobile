package com.example.secret_hitler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        TextView roleTextView = findViewById(R.id.roleTextView);
        TextView winConditionsTextView = findViewById(R.id.winConditionsTextView);
        
        if (getIntent().hasExtra("com.example.secret_hitler.ROLE")) {
            String role = getIntent().getExtras().get("com.example.secret_hitler.ROLE").toString();
            roleTextView.setText(role);

            String newline = System.getProperty("line.separator");
            String winConditions = "Your win conditions are:" + newline;
            if (role.equals("Liberal")) {
                winConditions += "- Enact 5 Liberal policies" + newline + "- Kill Hitler";
            } else {
                winConditions += "- Enact 6 Fascist policies" + newline + "- Appoint Hitler as Chancellor after passing 3 Fascist policies";
            }
            winConditionsTextView.setText(winConditions);
        }

        Button votingBtn = findViewById(R.id.votingBtn);
        votingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToVoteIntent = new Intent(getApplicationContext(), VotingActivity.class);
                startActivity(goToVoteIntent);
            }
        });

        Button showFactionBtn = findViewById(R.id.showFactionBtn);
        showFactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showFaction = new Intent(getApplicationContext(), ShowFactionActivity.class);

                if (getIntent().hasExtra("com.example.secret_hitler.ROLE")) {
                    String role = getIntent().getExtras().get("com.example.secret_hitler.ROLE").toString();
                    String faction = "Fascist";
                    if (role.equals("Liberal")) {
                        faction = "Liberal";
                    }
                    showFaction.putExtra("com.example.secret_hitler.FACTION", faction);
                }
                startActivity(showFaction);
            }
        });
    }
}
