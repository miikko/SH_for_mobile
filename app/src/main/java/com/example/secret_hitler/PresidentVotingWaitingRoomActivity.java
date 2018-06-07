package com.example.secret_hitler;

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

public class PresidentVotingWaitingRoomActivity extends AppCompatActivity {
    private TextView votingStatusForPresidentTextView;
    private Button presidentVoteYesBtn;
    private Button presidentVoteNoBtn;
    private Button presidentAdvanceBtn;
    private Player thisPlayer;
    private DatabaseReference thisPlayerRef;
    private DatabaseReference voteCountRef;
    private int jaVotes;
    private int neinVotes;


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

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
            thisPlayerRef = FirebaseDatabase.getInstance().getReference("Players").child("Player_" + thisPlayer.id);
        }

        voteCountRef = FirebaseDatabase.getInstance().getReference("VoteCount");
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
    }
}
