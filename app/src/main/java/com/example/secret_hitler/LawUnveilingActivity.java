package com.example.secret_hitler;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LawUnveilingActivity extends AppCompatActivity {
    private TextView lawUnveilingAnnouncerTextView;
    private TextView waitForLawToAppearTextView;
    private ImageView unveiledLawImageView;
    private Button lawUnveilingReturnButton;
    private DatabaseReference previousGovernmentRef;
    private DatabaseReference nextPresidentIDRef;
    private DatabaseReference playerCountRef;
    private DatabaseReference gameBoardRef;
    private DatabaseReference newActiveLawRef;
    private DatabaseReference winnerFactionRef;
    private DatabaseReference playersInThisRoundRef;
    private DatabaseReference playersRef;
    private DatabaseReference roundsWithoutChancellorRef;
    private DatabaseReference gameEndedRef;
    private ValueEventListener nextPresidentIDListener;
    private ValueEventListener activeLawsListener;
    private ValueEventListener playerCountListener;
    private ValueEventListener newActiveLawListener;
    private ValueEventListener playerParameterListener;
    private ValueEventListener playersInThisRoundListener;
    private Player thisPlayer;
    private boolean normalPresidentRotation;
    private boolean presidentActionNeeded;
    private String presidentActionType;
    private List<Integer> alivePlayerIDs;
    private int liberalLawImg;
    private int fascistLawImg;
    private int nextPresidentID;
    private int activeLiberalLaws;
    private int activeFascistLaws;
    private int playerCount;
    private int numOfPlayersInThisRound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_law_unveiling);

        lawUnveilingAnnouncerTextView = findViewById(R.id.lawUnveilingAnnouncerTextView);
        lawUnveilingAnnouncerTextView.setText("The selected Law is...");
        waitForLawToAppearTextView = findViewById(R.id.waitForLawToAppearTextView);
        waitForLawToAppearTextView.setText("Please wait for the President and their Chancellor to pick the Law.");
        unveiledLawImageView = findViewById(R.id.unveiledLawImageView);
        unveiledLawImageView.setVisibility(View.INVISIBLE);
        lawUnveilingReturnButton = findViewById(R.id.lawUnveilingReturnBtn);
        lawUnveilingReturnButton.setEnabled(false);
        previousGovernmentRef = FirebaseDatabase.getInstance().getReference("PreviousGovernment");
        nextPresidentIDRef = FirebaseDatabase.getInstance().getReference("NextPresidentID");
        playerCountRef = FirebaseDatabase.getInstance().getReference("PlayerCount");
        gameBoardRef = FirebaseDatabase.getInstance().getReference("Game_Board");
        newActiveLawRef = FirebaseDatabase.getInstance().getReference("NewActiveLaw");
        winnerFactionRef = FirebaseDatabase.getInstance().getReference("WinnerFaction");
        playersInThisRoundRef = FirebaseDatabase.getInstance().getReference("PlayersInThisRound");
        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        roundsWithoutChancellorRef = FirebaseDatabase.getInstance().getReference("RoundsWithoutChancellor");
        gameEndedRef = FirebaseDatabase.getInstance().getReference("Game_Ended");
        normalPresidentRotation = true;
        presidentActionNeeded = false;
        presidentActionType = "None";
        alivePlayerIDs = new ArrayList<>();
        liberalLawImg = R.drawable.liberal_law;
        fascistLawImg = R.drawable.fascist_law;

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        nextPresidentIDListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    nextPresidentID = dataSnapshot.getValue(int.class);
                    normalPresidentRotation = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        nextPresidentIDRef.addListenerForSingleValueEvent(nextPresidentIDListener);

        activeLawsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> bothActiveLawCounts = dataSnapshot.getChildren();
                for (DataSnapshot eachActiveLawCount : bothActiveLawCounts) {
                    if (eachActiveLawCount.getKey().equals("Liberal")) {
                        activeLiberalLaws = eachActiveLawCount.getValue(int.class);
                    } else {
                        activeFascistLaws = eachActiveLawCount.getValue(int.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        gameBoardRef.child("Active_Laws").addValueEventListener(activeLawsListener);

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

        newActiveLawListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().equals("Liberal")) {
                    unveiledLawImageView.setImageResource(liberalLawImg);
                    unveiledLawImageView.setVisibility(View.VISIBLE);
                    lawUnveilingReturnButton.setEnabled(true);
                    if (activeLiberalLaws == 5) {
                        lawUnveilingReturnButton.setText("ADVANCE");
                        waitForLawToAppearTextView.setText("The Liberals have won the game. Press the Advance-button to go to the Game-ending screen.");
                        winnerFactionRef.setValue("Liberal");
                    } else {
                        lawUnveilingReturnButton.setText("RETURN");
                        waitForLawToAppearTextView.setText("Press the Return-button to move to the next round.");
                    }
                } else if (dataSnapshot.getValue().equals("Fascist")) {
                    unveiledLawImageView.setImageResource(fascistLawImg);
                    unveiledLawImageView.setVisibility(View.VISIBLE);
                    lawUnveilingReturnButton.setEnabled(true);
                    if (activeFascistLaws == 6) {
                        lawUnveilingReturnButton.setText("ADVANCE");
                        waitForLawToAppearTextView.setText("The Fascists have won the game. Press the Advance-button to go to the Game-ending screen.");
                        winnerFactionRef.setValue("Fascist");
                    } else if (playerCount < 7 && activeFascistLaws == 3) {
                        //President sees the top 3 remaining laws.
                        //Because of an earlier operation, there should always be more than 2 laws in the draw pile.
                        presidentActionNeeded = true;
                        presidentActionType = "See_Laws";
                    } else if ((playerCount > 6 && activeFascistLaws == 2) || (playerCount > 8 && activeFascistLaws == 1)) {
                        //President investigates a player
                        presidentActionNeeded = true;
                        presidentActionType = "Investigate";
                    } else if ((playerCount > 6 && activeFascistLaws == 3)) {
                        //President selects the next president.
                        //Presidency rotation is unaffected which means a player can become President 2 times in a row.
                        presidentActionNeeded = true;
                        presidentActionType = "ChoosePresident";
                    } else if (activeFascistLaws > 3) {
                        //President must execute a player.
                        presidentActionNeeded = true;
                        presidentActionType = "Execute";
                    } else {
                        lawUnveilingReturnButton.setText("RETURN");
                        waitForLawToAppearTextView.setText("Press the Return-button to move to the next round.");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        newActiveLawRef.addValueEventListener(newActiveLawListener);

        playersInThisRoundListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfPlayersInThisRound = dataSnapshot.getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersInThisRoundRef.addValueEventListener(playersInThisRoundListener);

        if (thisPlayer.isPresident) {
            playerParameterListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> allPlayers = dataSnapshot.getChildren();
                    for (DataSnapshot eachPlayer : allPlayers) {
                        Iterable<DataSnapshot> playerParameters = eachPlayer.getChildren();
                        for (DataSnapshot parameter : playerParameters) {
                            if (parameter.getKey().equals("id") && !alivePlayerIDs.contains(parameter.getValue(int.class))) {
                                alivePlayerIDs.add(parameter.getValue(int.class));
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            playersRef.addListenerForSingleValueEvent(playerParameterListener);
        }

        lawUnveilingReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeLiberalLaws == 5 || activeFascistLaws == 6) {
                    gameEndedRef.setValue(true);
                    Intent goToGameEndingActivity = new Intent(getApplicationContext(), GameEndingActivity.class);
                    goToGameEndingActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(goToGameEndingActivity);
                } else {
                    if (thisPlayer.isPresident) {
                        roundsWithoutChancellorRef.setValue(0);
                        if (presidentActionNeeded) {
                            Intent presidentActionIntent = new Intent(getApplicationContext(), PresidentActionActivity.class);
                            presidentActionIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                            presidentActionIntent.putExtra("com.example.secret_hitler.ACTIONTYPE", presidentActionType);
                            startActivity(presidentActionIntent);
                        } else {
                            if (normalPresidentRotation) {
                                Collections.sort(alivePlayerIDs);
                                int i = alivePlayerIDs.indexOf(thisPlayer.id);
                                if (i + 1 == alivePlayerIDs.size()) {
                                    i = 0;
                                } else {
                                    i++;
                                }
                                playersRef.child("Player_" + alivePlayerIDs.get(i)).child("isPresident").setValue(true);
                                playersRef.child("Player_" + thisPlayer.id).child("isPresident").setValue(false);
                            } else {
                                playersRef.child("Player_" + nextPresidentID).child("isPresident").setValue(true);
                                if (thisPlayer.id != nextPresidentID) {
                                    playersRef.child("Player_" + thisPlayer.id).child("isPresident").setValue(false);
                                }
                                nextPresidentIDRef.setValue(null);
                            }
                            previousGovernmentRef.child("PreviousPresident").setValue(thisPlayer.name);
                            thisPlayer.RemovePresidency();
                        }
                    } else if (thisPlayer.isChancellor) {
                        playersRef.child("Player_" + thisPlayer.id).child("isChancellor").setValue(false);
                        previousGovernmentRef.child("PreviousChancellor").setValue(thisPlayer.name);
                        thisPlayer.RemoveChancellorStatus();
                    }
                    playersInThisRoundRef.setValue(numOfPlayersInThisRound + 1);
                    Intent goBackToSecondActivity = new Intent(getApplicationContext(), SecondActivity.class);
                    goBackToSecondActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(goBackToSecondActivity);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nextPresidentIDRef.removeEventListener(nextPresidentIDListener);
        gameBoardRef.child("Active_Laws").removeEventListener(activeLawsListener);
        playerCountRef.removeEventListener(playerCountListener);
        newActiveLawRef.removeEventListener(newActiveLawListener);
        playersInThisRoundRef.removeEventListener(playersInThisRoundListener);
    }

}

