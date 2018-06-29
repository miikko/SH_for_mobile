package com.example.secret_hitler;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PresidentActionActivity extends AppCompatActivity {
    private TextView hintTextView;
    private TextView topThreeLawsTextView;
    private ImageView factionImageView;
    private Spinner playerNamesSpinner;
    private Button confirmBtn;
    private DatabaseReference previousGovernmentRef;
    private DatabaseReference gameBoardRef;
    private DatabaseReference playersRef;
    private DatabaseReference playerCountRef;
    private DatabaseReference nextPresidentIDRef;
    private DatabaseReference playersInThisRoundRef;
    private ValueEventListener activeLawsListener;
    private ValueEventListener drawPileListener;
    private ValueEventListener playerParameterListener;
    private ValueEventListener playerIDListener;
    private ValueEventListener nextPresidentIDListener;
    private ValueEventListener playerCountListener;
    private ValueEventListener playersInThisRoundListener;
    private Helper helper;
    private List<String> drawPile;
    private List<String> playerNames;
    private HashMap<String, String> playerNamesAndRoles;
    private HashMap<String, Integer> playerNamesAndIDs;
    private ArrayAdapter<String> spinnerAdapter;
    private String actionType;
    private String selectedPlayerName;
    private String selectedPlayerRole;
    private Player thisPlayer;
    private List<Integer> alivePlayerIDs;
    private int activeLiberalLaws;
    private int activeFascistLaws;
    private int playerCount;
    private int nextPresidentID;
    private int executedPlayerID;
    private int numOfPlayersInThisRound;
    private boolean revealFaction;
    private boolean normalPresidentRotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_president_action);

        hintTextView = findViewById(R.id.presidentActionHintTextView);
        topThreeLawsTextView = findViewById(R.id.presidentActionShowLaws);
        topThreeLawsTextView.setVisibility(View.INVISIBLE);
        factionImageView = findViewById(R.id.presidentActionFactionImageView);
        factionImageView.setVisibility(View.INVISIBLE);
        playerNamesSpinner = findViewById(R.id.presidentActionPlayerNamesSpinner);
        playerNamesSpinner.setEnabled(false);
        confirmBtn = findViewById(R.id.presidentActionConfirmBtn);
        confirmBtn.setEnabled(false);
        previousGovernmentRef = FirebaseDatabase.getInstance().getReference("PreviousGovernment");
        gameBoardRef = FirebaseDatabase.getInstance().getReference("Game_Board");
        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        playerCountRef = FirebaseDatabase.getInstance().getReference("PlayerCount");
        nextPresidentIDRef = FirebaseDatabase.getInstance().getReference("NextPresidentID");
        playersInThisRoundRef = FirebaseDatabase.getInstance().getReference("PlayersInThisRound");
        helper = new Helper();
        alivePlayerIDs = new ArrayList<>();
        revealFaction = false;
        normalPresidentRotation = true;

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }
        if (getIntent().hasExtra("com.example.secret_hitler.ACTIONTYPE")) {
            actionType = getIntent().getStringExtra("com.example.secret_hitler.ACTIONTYPE");
        } else {
            actionType = "None";
        }

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

        switch (actionType) {

            case "See_Laws":
                hintTextView.setText("The top 3 Laws in the Draw pile are displayed below. Press the Confirm-button to move to the next round.");

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

                drawPileListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        drawPile = (List<String>) dataSnapshot.getValue();
                        if (drawPile.size() < 3) {
                            //Discard pile is shuffled back to the draw pile
                            int numOfLiberalLawsInDrawPile = 0;
                            int numOfFascistLawsInDrawPile = 0;
                            for (int i = 0; i < drawPile.size(); i++) {
                                if (drawPile.get(i).equals("Liberal")) {
                                    numOfLiberalLawsInDrawPile++;
                                } else {
                                    numOfFascistLawsInDrawPile++;
                                }
                            }
                            List<String> shuffledDiscardPile = helper.ShuffleLawsToDrawPile(6 - numOfLiberalLawsInDrawPile - activeLiberalLaws, 11 - numOfFascistLawsInDrawPile - activeFascistLaws);
                            drawPile.addAll(shuffledDiscardPile);
                            gameBoardRef.child("Draw_Pile").setValue(drawPile);
                        } else {
                            topThreeLawsTextView.setText("The top three Laws in the draw pile are: 1. " + drawPile.get(0) + ", 2. " + drawPile.get(1) + ", 3. " + drawPile.get(2));
                            topThreeLawsTextView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                gameBoardRef.child("Draw_Pile").addValueEventListener(drawPileListener);
                break;

            case "Investigate":
                hintTextView.setText("Choose a player whose Faction you want to find out from the List below. Then press the Select-button.");
                confirmBtn.setText("SELECT");

                playerParameterListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        playerNames = new ArrayList<>();
                        playerNamesAndRoles = new HashMap<>();
                        Iterable<DataSnapshot> players = dataSnapshot.getChildren();
                        for (DataSnapshot player : players) {
                            Iterable<DataSnapshot> parameters = player.getChildren();
                            String thisName = "None";
                            for (DataSnapshot parameter : parameters) {
                                if (parameter.getKey().equals("name")) {
                                    thisName = parameter.getValue().toString();
                                    playerNames.add(thisName);
                                } else if (parameter.getValue().equals("role")) {
                                    playerNamesAndRoles.put(thisName, parameter.getValue().toString());
                                }
                            }
                        }
                        playerNames.remove(thisPlayer.name);
                        playerNamesAndRoles.remove(thisPlayer.name);
                        if (spinnerAdapter == null) {
                            spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.my_spinner_text, R.id.textView, playerNames);
                            playerNamesSpinner.setEnabled(true);
                            playerNamesSpinner.setAdapter(spinnerAdapter);
                            revealFaction = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                playersRef.addListenerForSingleValueEvent(playerParameterListener);

                break;

            case "ChoosePresident":
                hintTextView.setText("Choose any other player who you want to be the next President from the List below. Then press the Confirm-button to move to the next round.");
                break;

            case "Execute":
                hintTextView.setText("Choose a player who you want to be Executed from the List below. Then press the Confirm-button to move to the next round.");
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
                break;

            default:
                break;
        }
        if (actionType.equals("ChoosePresident") || actionType.equals("Execute")) {
            playerParameterListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    playerNames = new ArrayList<>();
                    playerNamesAndIDs = new HashMap<>();
                    Iterable<DataSnapshot> players = dataSnapshot.getChildren();
                    for (DataSnapshot player : players) {
                        Iterable<DataSnapshot> parameters = player.getChildren();
                        int thisID = 100;
                        String thisName;
                        for (DataSnapshot parameter : parameters) {
                            if (parameter.getKey().equals("id")) {
                                thisID = parameter.getValue(int.class);
                            } else if (parameter.getKey().equals("name")) {
                                thisName = parameter.getValue().toString();
                                playerNames.add(thisName);
                                playerNamesAndIDs.put(thisName, thisID);
                            }
                        }
                    }
                    playerNames.remove(thisPlayer.name);
                    playerNamesAndIDs.remove(thisPlayer.name);
                    if (spinnerAdapter == null) {
                        spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.my_spinner_text, R.id.textView, playerNames);
                        playerNamesSpinner.setEnabled(true);
                        playerNamesSpinner.setAdapter(spinnerAdapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            playersRef.addListenerForSingleValueEvent(playerParameterListener);
        }

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

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (revealFaction) {
                    selectedPlayerName = playerNamesSpinner.getSelectedItem().toString();
                    selectedPlayerRole = playerNamesAndRoles.get(selectedPlayerName);
                    if (selectedPlayerRole.equals("Liberal")) {
                        factionImageView.setImageResource(R.drawable.liberal_membercard);
                    } else {
                        factionImageView.setImageResource(R.drawable.fascist_membercard);
                    }
                    playerNamesSpinner.setEnabled(false);
                    factionImageView.setVisibility(View.VISIBLE);
                    hintTextView.setText("Below you can see his/her Faction. Press the Confirm-button to move to the next round.");
                    confirmBtn.setText("CONFIRM");
                    revealFaction = false;
                } else {
                    if (actionType.equals("ChoosePresident")) {
                        selectedPlayerName = playerNamesSpinner.getSelectedItem().toString();
                        nextPresidentID = playerNamesAndIDs.get(selectedPlayerName);
                        playersRef.child("Player_" + nextPresidentID).child("isPresident").setValue(true);
                        if (thisPlayer.id + 1 < playerCount) {
                            nextPresidentIDRef.setValue(thisPlayer.id + 1);
                        } else {
                            nextPresidentIDRef.setValue(0);
                        }
                    } else {
                        if (actionType.equals("Execute")) {
                            selectedPlayerName = playerNamesSpinner.getSelectedItem().toString();
                            executedPlayerID = playerNamesAndIDs.get(selectedPlayerName);
                            playersRef.child("Player_" + executedPlayerID).child("isAlive").setValue(false);
                        }
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
                    }
                    playersInThisRoundRef.setValue(numOfPlayersInThisRound + 1);
                    playersRef.child("Player_" + thisPlayer.id).child("isPresident").setValue(false);
                    previousGovernmentRef.child("PreviousPresident").setValue(thisPlayer.name);
                    thisPlayer.RemovePresidency();
                    Intent goBackToSecondActivity = new Intent(getApplicationContext(), SecondActivity.class);
                    goBackToSecondActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(goBackToSecondActivity);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerCountRef.removeEventListener(playerCountListener);
        playersRef.removeEventListener(playerIDListener);
        playersInThisRoundRef.removeEventListener(playersInThisRoundListener);
        if (actionType.equals("See_Laws")) {
            gameBoardRef.child("Active_Laws").removeEventListener(activeLawsListener);
            gameBoardRef.child("Draw_Pile").removeEventListener(drawPileListener);
        } else {
            if (actionType.equals("Execute")) {
                nextPresidentIDRef.removeEventListener(nextPresidentIDListener);
            }
            playersRef.removeEventListener(playerParameterListener);
        }
    }
}
