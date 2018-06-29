package com.example.secret_hitler;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.ValueEventListener;

public class VotingActivity extends AppCompatActivity {
    private Player thisPlayer;
    private TextView chooseVoteTextView;
    private TextView gameEndedTextView;
    private Button jaBtn;
    private Button neinBtn;
    private Button votingAdvanceBtn;
    private DatabaseReference playersAliveCountRef;
    private DatabaseReference roundsWithoutChancellorRef;
    private DatabaseReference chancellorCandidateNameRef;
    private DatabaseReference hitlerNameRef;
    private DatabaseReference gameBoardRef;
    private DatabaseReference thisPlayerRef;
    private DatabaseReference voteCountRef;
    private DatabaseReference chancellorNeededRef;
    private DatabaseReference playersRef;
    private DatabaseReference playersInThisRoundRef;
    private DatabaseReference gameEndedRef;
    private DatabaseReference winnerFactionRef;
    private ValueEventListener playersAliveCountListener;
    private ValueEventListener roundsWithoutChancellorListener;
    private ValueEventListener chancellorCandidateNameListener;
    private ValueEventListener hitlerNameListener;
    private ValueEventListener activeLawCountListener;
    private ValueEventListener voteCountListener;
    private ValueEventListener chancellorNeededListener;
    private ValueEventListener playersInThisRoundListener;
    private String chancellorCandidateName;
    private String hitlerName;
    private boolean gameEnds;
    private boolean chancellorNeeded;
    private boolean chancellorWasElected;
    private int numOfPlayersAlive;
    private int roundsWithoutChancellor;
    private int activeFascistLaws;
    private int activeLiberalLaws;
    private int jaVotes;
    private int neinVotes;
    private int numOfPlayersInThisRound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        chooseVoteTextView = findViewById(R.id.chooseVoteTextView);
        gameEndedTextView = findViewById(R.id.votingACGameEndedNoteTextView);
        gameEndedTextView.setVisibility(View.INVISIBLE);
        jaBtn = findViewById(R.id.jaBtn);
        neinBtn = findViewById(R.id.neinBtn);
        votingAdvanceBtn = findViewById(R.id.votingAdvanceBtn);
        votingAdvanceBtn.setEnabled(false);
        playersAliveCountRef = FirebaseDatabase.getInstance().getReference("PlayersAliveCount");
        roundsWithoutChancellorRef = FirebaseDatabase.getInstance().getReference("RoundsWithoutChancellor");
        chancellorCandidateNameRef = FirebaseDatabase.getInstance().getReference("ChancellorCandidateName");
        hitlerNameRef = FirebaseDatabase.getInstance().getReference("HitlerName");
        gameBoardRef = FirebaseDatabase.getInstance().getReference("Game_Board");
        thisPlayerRef = FirebaseDatabase.getInstance().getReference("Players").child("Player_" + thisPlayer.id);
        voteCountRef = FirebaseDatabase.getInstance().getReference("VoteCount");
        chancellorNeededRef = FirebaseDatabase.getInstance().getReference("ChancellorNeeded");
        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        playersInThisRoundRef = FirebaseDatabase.getInstance().getReference("PlayersInThisRound");
        gameEndedRef = FirebaseDatabase.getInstance().getReference("Game_Ended");
        winnerFactionRef = FirebaseDatabase.getInstance().getReference("Winner_Faction");
        gameEnds = false;

        playersAliveCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfPlayersAlive = dataSnapshot.getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersAliveCountRef.addListenerForSingleValueEvent(playersAliveCountListener);

        roundsWithoutChancellorListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roundsWithoutChancellor = dataSnapshot.getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        roundsWithoutChancellorRef.addListenerForSingleValueEvent(roundsWithoutChancellorListener);

        chancellorCandidateNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chancellorCandidateName = dataSnapshot.getValue(String.class);
                chooseVoteTextView.setText("Vote on " + chancellorCandidateName + " becoming the next Chancellor");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        chancellorCandidateNameRef.addListenerForSingleValueEvent(chancellorCandidateNameListener);

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

        activeLawCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> bothLawCounts = dataSnapshot.getChildren();
                for (DataSnapshot eachLawCount : bothLawCounts) {
                    if (eachLawCount.getKey().equals("Fascist")) {
                        activeFascistLaws = eachLawCount.getValue(int.class);
                    } else {
                        activeLiberalLaws = eachLawCount.getValue(int.class);
                    }
                }
                if (activeFascistLaws == 5) {
                    gameEndedTextView.setText("The topmost Law was Fascist which means that the Fascists win the game with 6 Active Laws. Press the Advance-button to go to the Game-ending screen.");
                    gameEndedTextView.setVisibility(View.VISIBLE);
                    winnerFactionRef.setValue("Fascists");
                    gameEnds = true;
                } else if (activeLiberalLaws == 6) {
                    gameEndedTextView.setText("The topmost Law was Liberal which means that the Liberals win the game with 5 Active Laws. Press the Advance-button to go to the Game-ending screen.");
                    gameEndedTextView.setVisibility(View.VISIBLE);
                    winnerFactionRef.setValue("Liberals");
                    gameEnds = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        gameBoardRef.child("Active_Laws").addValueEventListener(activeLawCountListener);

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
                        if (chancellorCandidateName.equals(hitlerName) && activeFascistLaws > 2) {
                            chooseVoteTextView.setText("Hitler has become the Chancellor after passing 3 Fascist Laws. Press the Advance-button to go to the Game-ending screen.");
                            winnerFactionRef.setValue("Fascists");
                            gameEnds = true;
                            votingAdvanceBtn.setEnabled(true);
                        } else if (thisPlayer.name.equals(chancellorCandidateName) && !chancellorWasElected) {
                            chooseVoteTextView.setText("You were elected as the Chancellor. Please wait while the President picks 2 Laws that they will pass to you.");
                            playersRef.child("Player_" + thisPlayer.id).child("isChancellor").setValue(true);
                            thisPlayer.SetAsChancellor();
                        } else {
                            chooseVoteTextView.setText(chancellorCandidateName + " was elected Chancellor. Press the Advance-button to move to the Law unveiling screen.");
                            votingAdvanceBtn.setEnabled(true);
                        }
                        chancellorWasElected = true;
                    } else {
                        chancellorWasElected = false;
                        if (roundsWithoutChancellor % 3 == 0) {
                            chooseVoteTextView.setText(chancellorCandidateName + "was not elected Chancellor. Now there have been " + roundsWithoutChancellor + " straight rounds without a Chancellor. This means that the topmost Law in the Draw pile becomes Active.");
                        } else {
                            chooseVoteTextView.setText(chancellorCandidateName + " was not elected Chancellor. Press the Advance-button to go to the next round.");
                        }
                        votingAdvanceBtn.setEnabled(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        voteCountRef.addValueEventListener(voteCountListener);

        chancellorNeededListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chancellorNeeded = dataSnapshot.getValue(boolean.class);
                if (chancellorNeeded && thisPlayer.name.equals(chancellorCandidateName)) {
                    chooseVoteTextView.setText("The president has given you 2 Laws to choose from. Press the Advance-button to pick the Law you want.");
                    votingAdvanceBtn.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        chancellorNeededRef.addValueEventListener(chancellorNeededListener);

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

        jaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisPlayer.DidVote();
                thisPlayer.VotedJa();
                thisPlayerRef.child("hasVoted").setValue(true);
                thisPlayerRef.child("previousVote").setValue("Ja");
                voteCountRef.child("Ja_Votes").setValue(jaVotes + 1);

                chooseVoteTextView.setText("Waiting for everyone to finish voting");
                jaBtn.setEnabled(false);
                neinBtn.setEnabled(false);
            }
        });

        neinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisPlayer.DidVote();
                thisPlayer.VotedNein();
                thisPlayerRef.child("hasVoted").setValue(true);
                thisPlayerRef.child("previousVote").setValue("Nein");
                voteCountRef.child("Nein_Votes").setValue(neinVotes + 1);

                chooseVoteTextView.setText("Waiting for everyone to finish voting");
                jaBtn.setEnabled(false);
                neinBtn.setEnabled(false);
            }
        });

        votingAdvanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameEnds) {
                    gameEndedRef.setValue(true);
                    Intent goToGameEndingActivity = new Intent(getApplicationContext(), GameEndingActivity.class);
                    goToGameEndingActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(goToGameEndingActivity);
                } else if (!chancellorWasElected) {
                    playersInThisRoundRef.setValue(numOfPlayersInThisRound + 1);
                    Intent goBackToSecondActivity = new Intent(getApplicationContext(), SecondActivity.class);
                    goBackToSecondActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(goBackToSecondActivity);

                } else if (thisPlayer.name.equals(chancellorCandidateName)) {
                    Intent chancellorGoPickLawIntent = new Intent(getApplicationContext(), ChancellorPickLawActivity.class);
                    chancellorGoPickLawIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(chancellorGoPickLawIntent);
                } else {
                    Intent goToLawUnveilingActivity = new Intent(getApplicationContext(), LawUnveilingActivity.class);
                    goToLawUnveilingActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(goToLawUnveilingActivity);
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
        playersAliveCountRef.removeEventListener(playersAliveCountListener);
        roundsWithoutChancellorRef.removeEventListener(roundsWithoutChancellorListener);
        chancellorCandidateNameRef.removeEventListener(chancellorCandidateNameListener);
        hitlerNameRef.removeEventListener(hitlerNameListener);
        gameBoardRef.child("Active_Laws").removeEventListener(activeLawCountListener);
        voteCountRef.removeEventListener(voteCountListener);
        chancellorNeededRef.removeEventListener(chancellorNeededListener);
        playersInThisRoundRef.removeEventListener(playersInThisRoundListener);
    }

}
