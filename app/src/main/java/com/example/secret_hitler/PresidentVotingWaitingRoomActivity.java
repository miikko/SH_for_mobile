package com.example.secret_hitler;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PresidentVotingWaitingRoomActivity extends AppCompatActivity {
    private TextView votingStatusForPresidentTextView;
    private TextView gameEndedTextView;
    private Button presidentVoteYesBtn;
    private Button presidentVoteNoBtn;
    private Button presidentAdvanceBtn;
    private DatabaseReference governmentRef;
    private DatabaseReference hitlerNameRef;
    private DatabaseReference gameBoardRef;
    private DatabaseReference countersRef;
    private DatabaseReference triggersRef;
    private DatabaseReference thisPlayerRef;
    private DatabaseReference playersRef;
    private DatabaseReference winnerFactionRef;
    private ValueEventListener roundsWithoutChancellorListener;
    private ValueEventListener nextPresidentIDListener;
    private ValueEventListener hitlerNameListener;
    private ValueEventListener playersAliveCountListener;
    private ValueEventListener playersInThisRoundListener;
    private ValueEventListener activeLawCountListener;
    private ValueEventListener drawPileListener;
    private ValueEventListener voteCountListener;
    private ValueEventListener playerIDListener;
    private Player thisPlayer;
    private Helper helper;
    private List<Integer> alivePlayerIDs;
    private int roundsWithoutChancellor;
    private int nextPresidentID;
    private int numOfPlayersInThisRound;
    private int jaVotes;
    private int neinVotes;
    private int numOfPlayersAlive;
    private int liberalLawsActive;
    private int fascistLawsActive;
    private List<String> drawPile;
    private String hitlerName;
    private String chancellorCandidateName;
    private boolean normalPresidentRotation;
    private boolean gameEnds;
    private boolean chancellorWasElected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_president_voting_waiting_room);

        votingStatusForPresidentTextView = findViewById(R.id.votingStatusForPresidentTextView);
        votingStatusForPresidentTextView.setText("Vote on your Chancellor election.");
        gameEndedTextView = findViewById(R.id.presWaitRoomGameEndsTextView);
        gameEndedTextView.setVisibility(View.INVISIBLE);
        presidentVoteYesBtn = findViewById(R.id.presidentVoteYesBtn);
        presidentVoteNoBtn = findViewById(R.id.presidentVoteNoBtn);
        presidentAdvanceBtn = findViewById(R.id.presidentAdvanceBtn);
        presidentAdvanceBtn.setVisibility(View.INVISIBLE);
        presidentAdvanceBtn.setEnabled(false);
        governmentRef = FirebaseDatabase.getInstance().getReference("Government");
        hitlerNameRef = FirebaseDatabase.getInstance().getReference("HitlerName");
        gameBoardRef = FirebaseDatabase.getInstance().getReference("Game_Board");
        countersRef = FirebaseDatabase.getInstance().getReference("Counters");
        triggersRef = FirebaseDatabase.getInstance().getReference("Triggers");
        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        winnerFactionRef = FirebaseDatabase.getInstance().getReference("Winner_Faction");
        helper = new Helper();
        alivePlayerIDs = new ArrayList<>();
        normalPresidentRotation = true;
        gameEnds = false;

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
            thisPlayerRef = FirebaseDatabase.getInstance().getReference("Players").child("Player_" + thisPlayer.id);
        }

        if (getIntent().hasExtra("com.example.secret_hitler.CHANCELLORCANDIDATENAME")) {
            chancellorCandidateName = getIntent().getStringExtra("com.example.secret_hitler.CHANCELLORCANDIDATENAME");
        }

        roundsWithoutChancellorListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roundsWithoutChancellor = dataSnapshot.getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        countersRef.child("Rounds_Without_Chancellor").addListenerForSingleValueEvent(roundsWithoutChancellorListener);

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
        governmentRef.child("Next_President_ID").addListenerForSingleValueEvent(nextPresidentIDListener);

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

        playersAliveCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfPlayersAlive = dataSnapshot.getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        countersRef.child("Players_Alive_Count").addListenerForSingleValueEvent(playersAliveCountListener);

        playersInThisRoundListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfPlayersInThisRound = dataSnapshot.getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        countersRef.child("Players_In_This_Round").addValueEventListener(playersInThisRoundListener);

        activeLawCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> bothActiveLawCounts = dataSnapshot.getChildren();
                for (DataSnapshot eachActiveLawCount : bothActiveLawCounts) {
                    if (eachActiveLawCount.getKey().equals("Liberal")) {
                        liberalLawsActive = eachActiveLawCount.getValue(int.class);
                    } else {
                        fascistLawsActive = eachActiveLawCount.getValue(int.class);
                    }
                }
                if (liberalLawsActive == 5) {
                    gameEndedTextView.setText("The topmost Law was Liberal which means that the Liberals win the game with 5 Active Laws. Press the Advance-button to go to the Game-ending screen.");
                    gameEndedTextView.setVisibility(View.VISIBLE);
                    winnerFactionRef.setValue("Liberals");
                    gameEnds = true;
                } else if (fascistLawsActive == 6) {
                    gameEndedTextView.setText("The topmost Law was Fascist which means that the Fascists win the game with 6 Active Laws. Press the Advance-button to go to the Game-ending screen.");
                    gameEndedTextView.setVisibility(View.VISIBLE);
                    winnerFactionRef.setValue("Fascists");
                    gameEnds = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        gameBoardRef.child("Active_Laws").addValueEventListener(activeLawCountListener);

        drawPileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                drawPile = (List<String>) dataSnapshot.getValue();
                if (drawPile.isEmpty()) {
                    drawPile = helper.ShuffleLawsToDrawPile(6 - liberalLawsActive, 11 - fascistLawsActive);
                    gameBoardRef.child("Draw_Pile").setValue(drawPile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        gameBoardRef.child("Draw_Pile").addValueEventListener(drawPileListener);

        voteCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> bothVoteCounts = dataSnapshot.getChildren();
                for (DataSnapshot eachVoteCount : bothVoteCounts) {
                    if (eachVoteCount.getKey().equals("Ja_Votes")) {
                        jaVotes = eachVoteCount.getValue(int.class);
                    } else {
                        neinVotes = eachVoteCount.getValue(int.class);
                    }
                }
                if (jaVotes + neinVotes == numOfPlayersAlive) {
                    if (jaVotes > neinVotes) {
                        if (chancellorCandidateName.equals(hitlerName) && fascistLawsActive > 2) {
                            votingStatusForPresidentTextView.setText("Hitler has become the Chancellor after passing 3 Fascist Laws. Press the Advance-button to go to the Game-ending screen.");
                            winnerFactionRef.setValue("Fascists");
                            gameEnds = true;
                        } else {
                            votingStatusForPresidentTextView.setText(chancellorCandidateName + " was elected Chancellor. Click the Advance-button to pick 2 Laws from 3 that will be passed to the Chancellor.");
                        }
                        chancellorWasElected = true;
                    } else {
                        chancellorWasElected = false;
                        if (roundsWithoutChancellor > 0 && roundsWithoutChancellor % 3 == 0) {
                            votingStatusForPresidentTextView.setText(chancellorCandidateName + " was not elected Chancellor. Now there have been " + roundsWithoutChancellor + " straight rounds without a Chancellor. This means that the topmost Law in the Draw pile becomes Active.");
                            if (drawPile.get(0).equals("Liberal")) {
                                gameBoardRef.child("Active_Laws").child("Liberal").setValue(liberalLawsActive + 1);
                            } else {
                                gameBoardRef.child("Active_Laws").child("Fascist").setValue(fascistLawsActive + 1);
                            }
                            drawPile.remove(0);
                            gameBoardRef.child("Draw_Pile").setValue(drawPile);
                        } else {
                            votingStatusForPresidentTextView.setText(chancellorCandidateName + " was not elected Chancellor. Click the Advance-button to go to the next round.");
                        }
                        countersRef.child("Rounds_Without_Chancellor").setValue(roundsWithoutChancellor + 1);
                    }
                    presidentAdvanceBtn.setEnabled(true);
                    presidentAdvanceBtn.setVisibility(View.VISIBLE);
                    if (numOfPlayersInThisRound == numOfPlayersAlive) {
                        countersRef.child("Players_In_This_Round").setValue(0);
                    } else {
                        countersRef.child("Players_In_This_Round").setValue(numOfPlayersInThisRound - numOfPlayersAlive); //This line will hopefully prevent any problems with the "Second Activity" check
                    }

                    triggersRef.child("Vote_Needed").setValue(false);
                    governmentRef.child("Chancellor_Candidate_Name").setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        countersRef.child("Vote_Count").addValueEventListener(voteCountListener);

        playerIDListener = new ValueEventListener() {
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
        playersRef.addListenerForSingleValueEvent(playerIDListener);

        presidentVoteYesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisPlayer.DidVote();
                thisPlayer.VotedJa();
                thisPlayerRef.child("hasVoted").setValue(true);
                thisPlayerRef.child("previousVote").setValue("Ja");
                countersRef.child("Vote_Count").child("Ja_Votes").runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(currentValue++);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                    }
                });

                votingStatusForPresidentTextView.setText("Waiting for everyone to finish voting");
                presidentVoteYesBtn.setEnabled(false);
                presidentVoteNoBtn.setEnabled(false);
                presidentVoteYesBtn.setVisibility(View.INVISIBLE);
                presidentVoteNoBtn.setVisibility(View.INVISIBLE);
            }
        });

        presidentVoteNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisPlayer.DidVote();
                thisPlayer.VotedNein();
                thisPlayerRef.child("hasVoted").setValue(true);
                thisPlayerRef.child("previousVote").setValue("Nein");
                countersRef.child("Vote_Count").child("Nein_Votes").runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(currentValue++);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                    }
                });

                votingStatusForPresidentTextView.setText("Waiting for everyone to finish voting");
                presidentVoteYesBtn.setEnabled(false);
                presidentVoteNoBtn.setEnabled(false);
                presidentVoteYesBtn.setVisibility(View.INVISIBLE);
                presidentVoteNoBtn.setVisibility(View.INVISIBLE);
            }
        });

        presidentAdvanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameEnds) {
                    triggersRef.child("Game_Ended").setValue(true);
                    Intent goToGameEndingActivity = new Intent(getApplicationContext(), GameEndingActivity.class);
                    goToGameEndingActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(goToGameEndingActivity);
                } else if (chancellorWasElected) {
                    Intent presidentSelectLawsIntent = new Intent(getApplicationContext(), PresidentSelectLawsActivity.class);
                    presidentSelectLawsIntent.putExtra("com.example.secret_hitler.ACTIVELIBERALLAWS", liberalLawsActive);
                    presidentSelectLawsIntent.putExtra("com.example.secret_hitler.ACTIVEFASCISTLAWS", fascistLawsActive);
                    presidentSelectLawsIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(presidentSelectLawsIntent);
                } else {
                    countersRef.child("Players_In_This_Round").runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Integer currentValue = mutableData.getValue(Integer.class);
                            if (currentValue == null) {
                                mutableData.setValue(1);
                            } else {
                                mutableData.setValue(currentValue++);
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                        }
                    });
                    if (normalPresidentRotation) {
                        Collections.sort(alivePlayerIDs);
                        int i = alivePlayerIDs.indexOf(thisPlayer.id);
                        if (i + 1 == alivePlayerIDs.size()) {
                            i = 0;
                        } else {
                            i++;
                        }
                        playersRef.child("Player_" + alivePlayerIDs.get(i)).child("isPresident").setValue(true);
                    } else {
                        playersRef.child("Player_" + nextPresidentID).child("isPresident").setValue(true);
                        if (thisPlayer.id != nextPresidentID) {
                            playersRef.child("Player_" + thisPlayer.id).child("isPresident").setValue(false);
                        }
                        governmentRef.child("Next_President_ID").setValue(null);
                    }
                    thisPlayer.RemovePresidency();
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
        countersRef.child("Rounds_Without_Chancellor").removeEventListener(roundsWithoutChancellorListener);
        governmentRef.child("Next_President_ID").removeEventListener(nextPresidentIDListener);
        hitlerNameRef.removeEventListener(hitlerNameListener);
        countersRef.child("Players_Alive_Count").removeEventListener(playersAliveCountListener);
        countersRef.child("Players_In_This_Round").removeEventListener(playersInThisRoundListener);
        gameBoardRef.child("Active_Laws").removeEventListener(activeLawCountListener);
        gameBoardRef.child("Draw_Pile").removeEventListener(drawPileListener);
        countersRef.child("Vote_Count").removeEventListener(voteCountListener);
        playersRef.removeEventListener(playerIDListener);
    }

}
