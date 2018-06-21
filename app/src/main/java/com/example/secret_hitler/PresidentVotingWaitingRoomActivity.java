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

public class PresidentVotingWaitingRoomActivity extends AppCompatActivity {
    private TextView votingStatusForPresidentTextView;
    private Button presidentVoteYesBtn;
    private Button presidentVoteNoBtn;
    private Button presidentAdvanceBtn;
    private Player thisPlayer;
    private DatabaseReference hitlerNameRef;
    private DatabaseReference playerCountRef;
    private DatabaseReference playersInThisRoundRef;
    private DatabaseReference gameBoardRef;
    private DatabaseReference voteCountRef;
    private DatabaseReference voteNeededRef;
    private DatabaseReference chancellorCandidateNameRef;
    private DatabaseReference thisPlayerRef;
    private ValueEventListener hitlerNameListener;
    private ValueEventListener playerCountListener;
    private ValueEventListener playersInThisRoundListener;
    private ValueEventListener activeLawCountListener;
    private ValueEventListener voteCountListener;
    private int numOfPlayersInThisRound;
    private int jaVotes;
    private int neinVotes;
    private int playerCount;
    private int liberalLawsActive;
    private int fascistLawsActive;
    private String hitlerName;
    private String chancellorCandidateName;
    private boolean gameEnds;
    private boolean chancellorWasElected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_president_voting_waiting_room);

        votingStatusForPresidentTextView = findViewById(R.id.votingStatusForPresidentTextView);
        votingStatusForPresidentTextView.setText("Vote on your Chancellor election.");
        presidentVoteYesBtn = findViewById(R.id.presidentVoteYesBtn);
        presidentVoteNoBtn = findViewById(R.id.presidentVoteNoBtn);
        presidentAdvanceBtn = findViewById(R.id.presidentAdvanceBtn);
        presidentAdvanceBtn.setVisibility(View.INVISIBLE);
        hitlerNameRef = FirebaseDatabase.getInstance().getReference("HitlerName");
        playerCountRef = FirebaseDatabase.getInstance().getReference("PlayerCount");
        playersInThisRoundRef = FirebaseDatabase.getInstance().getReference("PlayersInThisRound");
        gameBoardRef = FirebaseDatabase.getInstance().getReference("Game_Board");
        voteCountRef = FirebaseDatabase.getInstance().getReference("VoteCount");
        voteNeededRef = FirebaseDatabase.getInstance().getReference("VoteNeeded");
        chancellorCandidateNameRef = FirebaseDatabase.getInstance().getReference("ChancellorCandidateName");
        gameEnds = false;

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
            thisPlayerRef = FirebaseDatabase.getInstance().getReference("Players").child("Player_" + thisPlayer.id);
        }

        if (getIntent().hasExtra("com.example.secret_hitler.CHANCELLORCANDIDATENAME")) {
            chancellorCandidateName = getIntent().getStringExtra("com.example.secret_hitler.CHANCELLORCANDIDATENAME");
        }

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        gameBoardRef.child("Active_Laws").addListenerForSingleValueEvent(activeLawCountListener);

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
                if (jaVotes + neinVotes == 1/*playerCount*/) {
                    if (jaVotes > neinVotes) {
                        if (chancellorCandidateName.equals(hitlerName) && fascistLawsActive > 2) {
                            votingStatusForPresidentTextView.setText("Hitler has become the Chancellor after passing 3 Fascist Laws. Press the Advance-button to go to the Game-ending screen.");
                            gameEnds = true;
                        } else {
                            votingStatusForPresidentTextView.setText(chancellorCandidateName + " was elected Chancellor. Click the Advance-button to pick 2 Laws from 3 that will be passed to the Chancellor.");
                        }
                        chancellorWasElected = true;
                        presidentAdvanceBtn.setText("Advance");
                    } else {
                        chancellorWasElected = false;
                        votingStatusForPresidentTextView.setText(chancellorCandidateName + " was not elected Chancellor. The game will now move on to the next round. Click the Return-button to return.");
                        presidentAdvanceBtn.setText("Return");
                    }
                    presidentAdvanceBtn.setVisibility(View.VISIBLE);
                    playersInThisRoundRef.setValue(0);
                    voteNeededRef.setValue(false);
                    chancellorCandidateNameRef.setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        voteCountRef.addValueEventListener(voteCountListener);

        presidentVoteYesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisPlayer.DidVote();
                thisPlayer.VotedJa();
                thisPlayerRef.child("hasVoted").setValue(true);
                thisPlayerRef.child("lastVote").setValue("Ja");
                voteCountRef.child("Ja_Votes").setValue(jaVotes + 1);

                votingStatusForPresidentTextView.setText("Waiting for everyone to finish voting");
                presidentVoteYesBtn.setEnabled(false);
                presidentVoteNoBtn.setEnabled(false);
            }
        });

        presidentVoteNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisPlayer.DidVote();
                thisPlayer.VotedNein();
                thisPlayerRef.child("hasVoted").setValue(true);
                thisPlayerRef.child("lastVote").setValue("Nein");
                voteCountRef.child("Nein_Votes").setValue(neinVotes + 1);

                votingStatusForPresidentTextView.setText("Waiting for everyone to finish voting");
                presidentVoteYesBtn.setEnabled(false);
                presidentVoteNoBtn.setEnabled(false);
            }
        });

        presidentAdvanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameEnds) {
                    /*Intent goToGameEndingActivity = new Intent(getApplicationContext(), GameEndingActivity.class);
                    goToGameEndingActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(goToGameEndingActivity);*/
                } else if (chancellorWasElected) {
                    Intent presidentSelectLawsIntent = new Intent(getApplicationContext(), PresidentSelectLawsActivity.class);
                    presidentSelectLawsIntent.putExtra("com.example.secret_hitler.ACTIVELIBERALLAWS", liberalLawsActive);
                    presidentSelectLawsIntent.putExtra("com.example.secret_hitler.ACTIVEFASCISTLAWS", fascistLawsActive);
                    presidentSelectLawsIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(presidentSelectLawsIntent);
                } else {
                    playersInThisRoundRef.setValue(numOfPlayersInThisRound + 1);

                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hitlerNameRef.removeEventListener(hitlerNameListener);
        playerCountRef.removeEventListener(playerCountListener);
        playersInThisRoundRef.removeEventListener(playersInThisRoundListener);
        gameBoardRef.child("Active_Laws").removeEventListener(activeLawCountListener);
        voteCountRef.removeEventListener(voteCountListener);
    }

}
