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
    private Button jaBtn;
    private Button neinBtn;
    private Button votingAdvanceBtn;
    private String chancellorCandidateName;
    private DatabaseReference chancellorCandidateNameRef;
    private DatabaseReference hitlerNameRef;
    private DatabaseReference gameBoardRef;
    private DatabaseReference playerCountRef;
    private DatabaseReference thisPlayerRef;
    private DatabaseReference voteCountRef;
    private DatabaseReference chancellorNeededRef;
    private DatabaseReference playersRef;
    private DatabaseReference playersInThisRoundRef;
    private ValueEventListener chancellorCandidateNameListener;
    private ValueEventListener hitlerNameListener;
    private ValueEventListener activeLawCountListener;
    private ValueEventListener playerCountListener;
    private ValueEventListener voteCountListener;
    private ValueEventListener chancellorNeededListener;
    private ValueEventListener playersInThisRoundListener;
    private String hitlerName;
    private boolean gameEnds;
    private boolean chancellorNeeded;
    private boolean chancellorWasElected;
    private int activeFascistLaws;
    private int jaVotes;
    private int neinVotes;
    private int playerCount;
    private int numOfPlayersInThisRound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        chooseVoteTextView = findViewById(R.id.chooseVoteTextView);
        jaBtn = findViewById(R.id.jaBtn);
        neinBtn = findViewById(R.id.neinBtn);
        votingAdvanceBtn = findViewById(R.id.votingAdvanceBtn);
        votingAdvanceBtn.setEnabled(false);
        chancellorCandidateNameRef = FirebaseDatabase.getInstance().getReference("ChancellorCandidateName");
        hitlerNameRef = FirebaseDatabase.getInstance().getReference("HitlerName");
        gameBoardRef = FirebaseDatabase.getInstance().getReference("Game_Board");
        playerCountRef = FirebaseDatabase.getInstance().getReference("PlayerCount");
        thisPlayerRef = FirebaseDatabase.getInstance().getReference("Players").child("Player_" + thisPlayer.id);
        voteCountRef = FirebaseDatabase.getInstance().getReference("VoteCount");
        chancellorNeededRef = FirebaseDatabase.getInstance().getReference("ChancellorNeeded");
        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        playersInThisRoundRef = FirebaseDatabase.getInstance().getReference("PlayersInThisRound");
        gameEnds = false;

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
                hitlerName = "mike";/*dataSnapshot.getValue().toString();*/
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
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        gameBoardRef.child("Active_Laws").addListenerForSingleValueEvent(activeLawCountListener);

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
                if (jaVotes + neinVotes == 2/*playerCount*/) {
                    if (jaVotes > neinVotes) {
                        if (chancellorCandidateName.equals(hitlerName) && activeFascistLaws > 2) {
                            chooseVoteTextView.setText("Hitler has become the Chancellor after passing 3 Fascist Laws. Press the Advance-button to go to the Game-ending screen.");
                            gameEnds = true;
                            votingAdvanceBtn.setEnabled(true);
                        } else if (thisPlayer.name.equals(chancellorCandidateName)) {
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
                        chooseVoteTextView.setText(chancellorCandidateName + " was not elected Chancellor. The game will now move on to the next round.");
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
                thisPlayerRef.child("lastVote").setValue("Ja");
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
                thisPlayerRef.child("lastVote").setValue("Nein");
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
                    /*Intent goToGameEndingActivity = new Intent(getApplicationContext(), GameEndingActivity.class);
                    goToGameEndingActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(goToGameEndingActivity);*/
                } else if (!chancellorWasElected) {
                    playersInThisRoundRef.setValue(numOfPlayersInThisRound + 1);

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
    protected void onDestroy() {
        super.onDestroy();
        chancellorCandidateNameRef.removeEventListener(chancellorCandidateNameListener);
        hitlerNameRef.removeEventListener(hitlerNameListener);
        gameBoardRef.child("Active_Laws").removeEventListener(activeLawCountListener);
        playerCountRef.removeEventListener(playerCountListener);
        voteCountRef.removeEventListener(voteCountListener);
        chancellorNeededRef.removeEventListener(chancellorNeededListener);
        playersInThisRoundRef.removeEventListener(playersInThisRoundListener);
    }

}
