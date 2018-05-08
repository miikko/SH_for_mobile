package com.example.secret_hitler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SecondActivity extends AppCompatActivity {
    public DBHandler dbHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        dbHandler = DBHandler.getInstance(getApplicationContext());
        String testRole = dbHandler.GetRole(0);
        Toast toast = Toast.makeText(this, testRole, Toast.LENGTH_SHORT);
        toast.show();
        ImageView roleImageView = findViewById(R.id.roleImageView);
        TextView winConditionsTextView = findViewById(R.id.winConditionsTextView);
        
        if (getIntent().hasExtra("com.example.secret_hitler.ROLE")) {
            String role = getIntent().getExtras().get("com.example.secret_hitler.ROLE").toString();

            switch (role) {
                case "Fascist":
                    roleImageView.setImageResource(R.drawable.fascist_role);
                    break;
                case "Liberal":
                    roleImageView.setImageResource(R.drawable.liberal_role);
                    break;
                case "Hitler":
                    roleImageView.setImageResource(R.drawable.hitler_role);
                    break;
                default:
                    break;
            }

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
