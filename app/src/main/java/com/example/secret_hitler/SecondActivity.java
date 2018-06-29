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
    private Button seeOtherVotesBtn;
    private Button statusBtn;
    private Button goToGameEndingBtn;
    private ImageView roleImageView;
    private TextView winConditionsTextView;
    private TextView waitForOthersTextView;
    private TextView playerExecutedTextView;
    private DatabaseReference playersAliveCountRef;
    private DatabaseReference playerCountRef;
    private DatabaseReference hitlerNameRef;
    private DatabaseReference newActiveLawRef;
    private DatabaseReference voteCountRef;
    private DatabaseReference playersInThisRoundRef;
    private DatabaseReference voteNeededRef;
    private DatabaseReference playersRef;
    private DatabaseReference gameEndedRef;
    private DatabaseReference winnerFactionRef;
    private DatabaseReference deadPlayersRef;
    private ValueEventListener playersAliveCountListener;
    private ValueEventListener playerCountListener;
    private ValueEventListener hitlerNameListener;
    private ValueEventListener playersInThisRoundListener;
    private ValueEventListener voteNeededListener;
    private ValueEventListener playerParameterListener;
    private Player thisPlayer;
    private String role;
    private String hitlerName;
    private int numOfPlayersAlive;
    private int playerCount;
    private int numOfPlayersInThisRound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        votingBtn = findViewById(R.id.votingBtn);
        seeOtherVotesBtn = findViewById(R.id.seeOtherVotesBtn);
        statusBtn = findViewById(R.id.statusBtn);
        goToGameEndingBtn = findViewById(R.id.secondActivityGameEndsBtn);
        goToGameEndingBtn.setEnabled(false);
        goToGameEndingBtn.setVisibility(View.INVISIBLE);
        roleImageView = findViewById(R.id.roleImageView);
        winConditionsTextView = findViewById(R.id.winConditionsTextView);
        waitForOthersTextView = findViewById(R.id.waitForOthersTextView);
        waitForOthersTextView.setText("Waiting for other players...");
        playerExecutedTextView = findViewById(R.id.playerExecutedTextView);
        playerExecutedTextView.setVisibility(View.INVISIBLE);
        playersAliveCountRef = FirebaseDatabase.getInstance().getReference("PlayersAliveCount");
        playerCountRef = FirebaseDatabase.getInstance().getReference("PlayerCount");
        hitlerNameRef = FirebaseDatabase.getInstance().getReference("HitlerName");
        newActiveLawRef = FirebaseDatabase.getInstance().getReference("NewActiveLaw");
        voteCountRef = FirebaseDatabase.getInstance().getReference("VoteCount");
        playersInThisRoundRef = FirebaseDatabase.getInstance().getReference("PlayersInThisRound");
        voteNeededRef = FirebaseDatabase.getInstance().getReference("VoteNeeded");
        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        gameEndedRef = FirebaseDatabase.getInstance().getReference("Game_Ended");
        winnerFactionRef = FirebaseDatabase.getInstance().getReference("Winner_Faction");
        deadPlayersRef = FirebaseDatabase.getInstance().getReference("Dead_Players");

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
            role = thisPlayer.role;
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
            winConditions += "- Enact 5 Liberal Laws" + newline + "- Kill Hitler";
        } else {
            winConditions += "- Enact 6 Fascist Laws" + newline + "- Appoint Hitler as Chancellor after passing 3 Fascist Laws";
        }
        winConditionsTextView.setText(winConditions);

        playersAliveCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfPlayersAlive = dataSnapshot.getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersAliveCountRef.addValueEventListener(playersAliveCountListener);

        playerCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                playerCount = dataSnapshot.getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playerCountRef.addListenerForSingleValueEvent(playerCountListener);

        hitlerNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hitlerName = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        hitlerNameRef.addListenerForSingleValueEvent(hitlerNameListener);

        playerParameterListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> players = dataSnapshot.getChildren();
                for (DataSnapshot player : players) {
                    Iterable<DataSnapshot> parameters = player.getChildren();
                    int thisID = -1;
                    String thisName = "";
                    boolean youAreDead = false;
                    boolean someoneIsDead = false;
                    for (DataSnapshot parameter : parameters) {
                        if (parameter.getKey().equals("id")) {
                            thisID = parameter.getValue(int.class);
                        } else if (parameter.getKey().equals("isPresident") && parameter.getValue(boolean.class) && thisID == thisPlayer.id) {
                            thisPlayer.SetAsPresident();
                        } else if (parameter.getKey().equals("isAlive") && !parameter.getValue(boolean.class)) {
                            if (thisID == thisPlayer.id) {
                                youAreDead = true;
                            } else {
                                someoneIsDead = true;
                            }
                        } else if (parameter.getKey().equals("name")) {
                            thisName = parameter.getValue().toString();
                            //Someone was executed
                            //1. Find out who was executed and check if his role was Hitler.
                            //2. Display message. Message content depends on the user
                            //3. Check if game ends
                            //4. if not then assign new president if the executed player was the president
                            //5. remove the executed player
                            //6. update player count
                            if (youAreDead) {
                                if (thisPlayer.name.equals(hitlerName)) {
                                    playerExecutedTextView.setText("You have been executed and so the Liberals have won. Press the Advance-button to go to the Game-ending screen.");
                                    winnerFactionRef.setValue("Liberals");
                                } else {
                                    if (thisPlayer.isPresident) {
                                        int i;
                                        if (thisPlayer.id + 1 < playerCount) {
                                            i = thisPlayer.id + 1;
                                        } else {
                                            i = 0;
                                        }
                                        for (; i < playerCount; i++) {
                                            if (dataSnapshot.hasChild("Player_" + i)) {
                                                playersRef.child("Player_" + i).child("isPresident").setValue(true);
                                                break;
                                            }
                                        }
                                        thisPlayer.RemovePresidency();
                                    }
                                    playerExecutedTextView.setText("You have been executed. Press the Advance-button to go to the Game-ending screen, where you will wait for the others to finish the game.");
                                    playersRef.child("Player_" + thisPlayer.id).setValue(null);
                                    deadPlayersRef.child("Player_" + thisPlayer.id).setValue(thisPlayer);
                                    playersAliveCountRef.setValue(numOfPlayersAlive - 1);
                                }
                                thisPlayer.Died();
                                goToGameEndingBtn.setEnabled(true);
                                goToGameEndingBtn.setVisibility(View.VISIBLE);
                                playerExecutedTextView.setVisibility(View.VISIBLE);
                            } else if (someoneIsDead) {
                                if (thisName.equals(hitlerName)) {
                                    playerExecutedTextView.setText("Hitler has been executed and so the Liberals have won. Press the Advance-button to go to the Game-ending screen.");
                                    goToGameEndingBtn.setEnabled(true);
                                    goToGameEndingBtn.setVisibility(View.VISIBLE);
                                } else {
                                    playerExecutedTextView.setText(thisName + " has been executed. His role was not Hitler.");
                                }
                                playerExecutedTextView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersRef.addValueEventListener(playerParameterListener);

        playersInThisRoundListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfPlayersInThisRound = dataSnapshot.getValue(int.class);
                if (numOfPlayersInThisRound < numOfPlayersAlive) {
                    seeOtherVotesBtn.setEnabled(false);
                    statusBtn.setEnabled(false);
                } else {
                    newActiveLawRef.setValue("None");
                    voteCountRef.child("Ja_Votes").setValue(0);
                    voteCountRef.child("Nein_Votes").setValue(0);
                    waitForOthersTextView.setVisibility(View.INVISIBLE);
                    seeOtherVotesBtn.setEnabled(true);
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

        votingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToVoteIntent = new Intent(getApplicationContext(), VotingActivity.class);
                goToVoteIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                startActivity(goToVoteIntent);
            }
        });


        seeOtherVotesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent seeLastRoundVotes = new Intent(getApplicationContext(), LastRoundVotesActivity.class);
                startActivity(seeLastRoundVotes);
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

        goToGameEndingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameEndedRef.setValue(true);
                Intent goToGameEndingActivity = new Intent(getApplicationContext(), GameEndingActivity.class);
                goToGameEndingActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                startActivity(goToGameEndingActivity);
            }
        });
    }

    //Here you can choose what happens when you press the back button

    @Override
    public void onBackPressed() {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        playersAliveCountRef.removeEventListener(playersAliveCountListener);
        playerCountRef.removeEventListener(playerCountListener);
        hitlerNameRef.removeEventListener(hitlerNameListener);
        playersInThisRoundRef.removeEventListener(playersInThisRoundListener);
        voteNeededRef.removeEventListener(voteNeededListener);
        playersRef.removeEventListener(playerParameterListener);
    }

}
