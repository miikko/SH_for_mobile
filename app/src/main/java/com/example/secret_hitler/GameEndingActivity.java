package com.example.secret_hitler;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameEndingActivity extends AppCompatActivity {

    private TextView hintTextView;
    private TextView playerStatsTextView;
    private DatabaseReference playersRef;
    private DatabaseReference deadPlayersRef;
    private DatabaseReference winnerFactionRef;
    private DatabaseReference gameEndedRef;
    private ValueEventListener playerParameterListener;
    private ValueEventListener deadPlayerParameterListener;
    private ValueEventListener winnerFactionListener;
    private ValueEventListener gameEndedListener;
    private Player thisPlayer;
    private List<String> playerNames;
    private List<String> playerRoles;
    private String newLine;
    private String winnerFaction;
    private boolean gameEnded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_ending);

        hintTextView = findViewById(R.id.gameEndingHintTextView);
        playerStatsTextView = findViewById(R.id.gameEndingPlayerStatsTextView);
        playerStatsTextView.setVisibility(View.INVISIBLE);
        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        deadPlayersRef = FirebaseDatabase.getInstance().getReference("Dead_Players");
        winnerFactionRef = FirebaseDatabase.getInstance().getReference("Winner_Faction");
        gameEndedRef = FirebaseDatabase.getInstance().getReference("Game_Ended");
        playerNames = new ArrayList<>();
        playerRoles = new ArrayList<>();
        newLine = System.getProperty("line.separator");
        gameEnded = false;

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        playerParameterListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> alivePlayers = dataSnapshot.getChildren();
                for (DataSnapshot eachPlayer : alivePlayers) {
                    Iterable<DataSnapshot> playerParameters = eachPlayer.getChildren();
                    for (DataSnapshot parameter : playerParameters) {
                        if (parameter.getKey().equals("name") && !playerNames.contains(parameter.getValue().toString())) {
                            playerNames.add(parameter.getValue().toString());
                        } else if (parameter.getKey().equals("role") && !playerRoles.contains(parameter.getValue().toString())) {
                            playerRoles.add(parameter.getValue().toString());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersRef.addListenerForSingleValueEvent(playerParameterListener);

        deadPlayerParameterListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterable<DataSnapshot> deadPlayers = dataSnapshot.getChildren();
                    for (DataSnapshot eachPlayer : deadPlayers) {
                        Iterable<DataSnapshot> playerParameters = eachPlayer.getChildren();
                        for (DataSnapshot parameter : playerParameters) {
                            if (parameter.getKey().equals("name") && !playerNames.contains(parameter.getValue().toString())) {
                                playerNames.add(parameter.getValue().toString());
                            } else if (parameter.getKey().equals("role") && !playerRoles.contains(parameter.getValue().toString())) {
                                playerRoles.add(parameter.getValue().toString());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        deadPlayersRef.addListenerForSingleValueEvent(deadPlayerParameterListener);

        winnerFactionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.getValue().toString().equals("Liberals")) {
                        winnerFaction = "Liberals";
                    } else {
                        winnerFaction = "Fascists";
                    }
                } else {
                    winnerFaction = "None";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        winnerFactionRef.addValueEventListener(winnerFactionListener);

        gameEndedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gameEnded = dataSnapshot.getValue(boolean.class);
                if (gameEnded) {
                    if (winnerFaction.equals("Liberals")) {
                        hintTextView.setText("The Liberals have won the Game!");
                    } else if (winnerFaction.equals("Fascists")) {
                        hintTextView.setText("The Fascists have won the Game!");
                    }
                    playerStatsTextView.setText("");
                    for (int i = 0; i < playerNames.size(); i++) {
                        playerStatsTextView.append(playerNames.get(i) + " role was " + playerRoles.get(i) + "." + newLine);
                    }
                    playerStatsTextView.setVisibility(View.VISIBLE);
                } else {
                    hintTextView.setText("Please wait for the other players to finish the game.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        gameEndedRef.addValueEventListener(gameEndedListener);

    }
}
