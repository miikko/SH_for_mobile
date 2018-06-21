package com.example.secret_hitler;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SecondActivity extends AppCompatActivity {

    private Button votingBtn;
    private Button showFactionBtn;
    private Button statusBtn;
    private ImageView roleImageView;
    private TextView winConditionsTextView;
    private TextView waitForOthersTextView;
    private DatabaseReference playerCountRef;
    private DatabaseReference newActiveLawRef;
    private DatabaseReference voteCountRef;
    private DatabaseReference playersInThisRoundRef;
    private DatabaseReference voteNeededRef;
    private DatabaseReference playersRef;
    private ValueEventListener playerCountListener;
    private ValueEventListener playersInThisRoundListener;
    private ValueEventListener voteNeededListener;
    private ValueEventListener playerParameterListener;
    private Player thisPlayer;
    private String role;
    private int playerCount;
    private int numOfPlayersInThisRound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        votingBtn = findViewById(R.id.votingBtn);
        showFactionBtn = findViewById(R.id.showFactionBtn);
        statusBtn = findViewById(R.id.statusBtn);
        roleImageView = findViewById(R.id.roleImageView);
        winConditionsTextView = findViewById(R.id.winConditionsTextView);
        waitForOthersTextView = findViewById(R.id.waitForOthersTextView);
        waitForOthersTextView.setText("Waiting for other players...");
        playerCountRef = FirebaseDatabase.getInstance().getReference("PlayerCount");
        newActiveLawRef = FirebaseDatabase.getInstance().getReference("NewActiveLaw");
        voteCountRef = FirebaseDatabase.getInstance().getReference("VoteCount");
        playersInThisRoundRef = FirebaseDatabase.getInstance().getReference("PlayersInThisRound");
        voteNeededRef = FirebaseDatabase.getInstance().getReference("VoteNeeded");
        playersRef = FirebaseDatabase.getInstance().getReference("Players");

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
            role = thisPlayer.role;
        }

        playerCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                playerCount = dataSnapshot.getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playerCountRef.addValueEventListener(playerCountListener);

        playersInThisRoundListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfPlayersInThisRound = dataSnapshot.getValue(int.class);
                if (numOfPlayersInThisRound < 1/*playerCount*/) {
                    showFactionBtn.setEnabled(false);
                    statusBtn.setEnabled(false);
                } else {
                    newActiveLawRef.setValue("None");
                    voteCountRef.child("Ja_Votes").setValue(0);
                    voteCountRef.child("Nein_Votes").setValue(0);
                    waitForOthersTextView.setVisibility(View.INVISIBLE);
                    showFactionBtn.setEnabled(true);
                    statusBtn.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersInThisRoundRef.addValueEventListener(playersInThisRoundListener);

        voteNeededListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(boolean.class)) {
                    votingBtn.setEnabled(true);
                } else {
                    votingBtn.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        voteNeededRef.addValueEventListener(voteNeededListener);

        playerParameterListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> players = dataSnapshot.getChildren();
                boolean breakLoop = false;
                for (DataSnapshot player : players) {
                    Iterable<DataSnapshot> parameters = player.getChildren();
                    for (DataSnapshot parameter : parameters) {
                        if (parameter.getKey().equals("id") && parameter.getValue(int.class) == thisPlayer.id) {
                            breakLoop = true;
                        } else if (parameter.getKey().equals("isPresident") && parameter.getValue(boolean.class)) {
                            thisPlayer.SetAsPresident();
                        }
                    }
                    if (breakLoop) {
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersRef.addValueEventListener(playerParameterListener);

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
            winConditions += "- Enact 5 Liberal Laws" + newline + "- Kill Hitler";
        } else {
            winConditions += "- Enact 6 Fascist Laws" + newline + "- Appoint Hitler as Chancellor after passing 3 Fascist Laws";
        }
        winConditionsTextView.setText(winConditions);

        votingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToVoteIntent = new Intent(getApplicationContext(), VotingActivity.class);
                goToVoteIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
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
                Intent showStatusIntent = new Intent(getApplicationContext(), StatusActivity.class);
                showStatusIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                startActivity(showStatusIntent);
            }
        });
    }

    //Here you can choose what happens when you press the back button
    /*
    @Override
    public void onBackPressed() {

    }
    */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerCountRef.removeEventListener(playerCountListener);
        playersInThisRoundRef.removeEventListener(playersInThisRoundListener);
        voteNeededRef.removeEventListener(voteNeededListener);
        playersRef.removeEventListener(playerParameterListener);
    }

}
