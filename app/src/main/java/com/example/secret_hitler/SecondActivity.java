package com.example.secret_hitler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SecondActivity extends AppCompatActivity {

    public DBHandler dbHandler;
    private String role;
    private int playerID;
    private int playerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        dbHandler = DBHandler.getInstance(getApplicationContext());
        Button votingBtn = findViewById(R.id.votingBtn);
        Button showFactionBtn = findViewById(R.id.showFactionBtn);
        Button statusBtn = findViewById(R.id.statusBtn);

        ImageView roleImageView = findViewById(R.id.roleImageView);
        TextView winConditionsTextView = findViewById(R.id.winConditionsTextView);

        role = "role";

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER_ID")) {
            playerID = (int) getIntent().getExtras().get("com.example.secret_hitler.PLAYER_ID");
            role = dbHandler.GetRole(playerID);

            //Remove this after confirming that the role distribution works properly
            Log.d("PLAYERROLE", role);
            Log.d("PLAYERID", Integer.toString(playerID));
            playerCount = dbHandler.GetPlayerCount();
            for (int i = 0; i < playerCount; i++) {
                Log.d("ALLROLES","Player number " + (i + 1) + " role is " + dbHandler.GetRole(i));
            }
            //
        }

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

        playerCount = dbHandler.GetPlayerCount();

        votingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToVoteIntent = new Intent(getApplicationContext(), VotingActivity.class);
                startActivity(goToVoteIntent);
            }
        });

        showFactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showFaction = new Intent(getApplicationContext(), ShowFactionActivity.class);

                String faction = "Fascist";
                if (role.equals("Liberal")) {
                    faction = "Liberal";
                }
                showFaction.putExtra("com.example.secret_hitler.FACTION", faction);
                startActivity(showFaction);
            }
        });

        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showStatus = new Intent(getApplicationContext(), StatusActivity.class);
                startActivity(showStatus);
            }
        });
    }

    //Here you can choose what happens when you press the back button
    /*
    @Override
    public void onBackPressed() {

    }
    */
}
