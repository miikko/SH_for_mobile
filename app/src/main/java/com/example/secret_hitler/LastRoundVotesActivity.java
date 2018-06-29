package com.example.secret_hitler;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class LastRoundVotesActivity extends AppCompatActivity {

    private TextView lastRoundVotesTextView;
    private DatabaseReference playersRef;
    private ValueEventListener playerParameterListener;
    private List<String> playerNames;
    private List<String> playerVotes;
    private String newLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_round_votes);

        lastRoundVotesTextView = findViewById(R.id.displayLastRoundVotesTextView);
        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        newLine = System.getProperty("line.separator");

        playerParameterListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> allPlayers = dataSnapshot.getChildren();
                playerNames = new ArrayList<>();
                playerVotes = new ArrayList<>();
                for (DataSnapshot eachPlayer : allPlayers) {
                    Iterable<DataSnapshot> playerParameters = eachPlayer.getChildren();
                    for (DataSnapshot parameter : playerParameters) {
                        if (parameter.getKey().equals("previousVote")) {
                            playerVotes.add(parameter.getValue().toString());
                        } else if (parameter.getKey().equals("name")) {
                            playerNames.add(parameter.getValue().toString());
                        }
                    }
                }
                if (playerVotes.contains("none")) {
                    lastRoundVotesTextView.setText("No one has voted yet.");
                } else {
                    lastRoundVotesTextView.setText("");
                    for (int i = 0; i < playerNames.size(); i++) {
                        lastRoundVotesTextView.append(playerNames.get(i) + " voted: " + playerVotes.get(i) + "." + newLine);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersRef.addListenerForSingleValueEvent(playerParameterListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playersRef.removeEventListener(playerParameterListener);
    }
}
