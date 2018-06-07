package com.example.secret_hitler;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private String chancellorCandidateName;
    private DatabaseReference thisPlayerRef;
    private DatabaseReference voteCountRef;
    private int jaVotes;
    private int neinVotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        chooseVoteTextView = findViewById(R.id.chooseVoteTextView);
        thisPlayerRef = FirebaseDatabase.getInstance().getReference("Players").child("Player_" + thisPlayer.id);
        voteCountRef = FirebaseDatabase.getInstance().getReference("VoteCount");

        DatabaseReference chancellorCandidateNameRef = FirebaseDatabase.getInstance().getReference("ChancellorCandidateName");
        ValueEventListener chancellorCandidateNameListener = new ValueEventListener() {
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

        ValueEventListener voteCountListener = new ValueEventListener() {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        voteCountRef.addValueEventListener(voteCountListener);

        Button jaBtn = findViewById(R.id.jaBtn);
        jaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisPlayer.DidVote();
                thisPlayer.VotedJa();
                thisPlayerRef.child("hasVoted").setValue(true);
                thisPlayerRef.child("lastVote").setValue("Ja");
                voteCountRef.child("Ja_Votes").setValue(jaVotes + 1);

                chooseVoteTextView.setText("Waiting for everyone to finish voting");
            }
        });

        Button neinBtn = findViewById(R.id.neinBtn);
        neinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisPlayer.DidVote();
                thisPlayer.VotedNein();
                thisPlayerRef.child("hasVoted").setValue(true);
                thisPlayerRef.child("lastVote").setValue("Nein");
                voteCountRef.child("Nein_Votes").setValue(neinVotes + 1);

                chooseVoteTextView.setText("Waiting for everyone to finish voting");
            }
        });

    }
}
