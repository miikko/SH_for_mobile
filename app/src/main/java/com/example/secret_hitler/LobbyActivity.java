package com.example.secret_hitler;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.List;


public class LobbyActivity extends AppCompatActivity {
    private Button startGameButton;
    private Button resetButton;
    private TextView playerCountTextView;
    private TextView lobbyStatusTextView;
    private DatabaseReference countersRef;
    private DatabaseReference playersRef;
    private DatabaseReference gameBoardRef;
    private ValueEventListener playerParameterListener;
    private Helper helper;
    private Player thisPlayer;
    private ArrayList<String> playerRoles;
    private int playerCount;
    private boolean presidentChosen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        helper = new Helper();
        playerRoles = new ArrayList<>();

        startGameButton = findViewById(R.id.startGameBtn);
        resetButton = findViewById(R.id.resetBtn);
        playerCountTextView = findViewById(R.id.playerCountTextView);
        lobbyStatusTextView = findViewById(R.id.lobbyStatusTextView);
        countersRef = FirebaseDatabase.getInstance().getReference("Counters");
        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        gameBoardRef = FirebaseDatabase.getInstance().getReference("Game_Board");

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        playerParameterListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                playerCount = (int) dataSnapshot.getChildrenCount();
                countersRef.child("Player_Count").setValue(playerCount);
                playerCountTextView.setText("Number of players: " + playerCount);
                if (playerCount < 5) {
                    lobbyStatusTextView.setText("You don't have enough players to start playing.");
                    startGameButton.setEnabled(false);
                } else if (playerCount > 10) {
                    int numberOfExcessPlayers = playerCount - 10;
                    lobbyStatusTextView.setText("You have too many players. You need to remove " + numberOfExcessPlayers + " player(s) from the game to start.");
                    startGameButton.setEnabled(false);
                } else {
                    int fascistCount = 0;
                    int liberalCount = 0;
                    switch (playerCount) {
                        case 5:
                            liberalCount = 3;
                            fascistCount = 1;
                            break;
                        case 6:
                            liberalCount = 4;
                            fascistCount = 1;
                            break;
                        case 7:
                            liberalCount = 4;
                            fascistCount = 2;
                            break;
                        case 8:
                            liberalCount = 5;
                            fascistCount = 2;
                            break;
                        case 9:
                            liberalCount = 5;
                            fascistCount = 3;
                            break;
                        case 10:
                            liberalCount = 6;
                            fascistCount = 3;
                            break;
                        default:
                            break;
                    }
                    lobbyStatusTextView.setText("There will be " + liberalCount + " Liberals, " + fascistCount + " Fascist(s) and 1 Hitler in this game.");
                    startGameButton.setEnabled(true);
                }

                Iterable<DataSnapshot> players = dataSnapshot.getChildren();
                playerRoles.clear();
                for (DataSnapshot player : players) {
                    Iterable<DataSnapshot> playerParameters = player.getChildren();
                    for (DataSnapshot parameter : playerParameters) {
                        if (parameter.getKey().equals("role")) {
                            playerRoles.add(parameter.getValue().toString());
                        } else if (parameter.getKey().equals("isPresident") && parameter.getValue(boolean.class)) {
                            presidentChosen = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersRef.addValueEventListener(playerParameterListener);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                rootRef.child("Players").setValue(null);
                rootRef.child("Game_Board").setValue(null);
                rootRef.child("ChancellorsOptions").setValue(null);
                rootRef.child("WinnerFaction").setValue(null);
                rootRef.child("HitlerName").setValue(null);
                rootRef.child("Dead_Players").setValue(null);
                rootRef.child("Government").setValue(null);
                rootRef.child("Triggers").setValue(null);
                rootRef.child("Counters").setValue(null);
            }
        });

        if (startGameButton.isEnabled()) {
            startGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String thisPlayerRole = helper.AssignRole(playerCount, playerRoles);
                    thisPlayer.SetRole(thisPlayerRole);
                    DatabaseReference thisPlayerRoleRef = playersRef.child("Player_" + thisPlayer.id).child("role");
                    thisPlayerRoleRef.setValue(thisPlayerRole);
                    if (thisPlayer.role.equals("Hitler")) {
                        FirebaseDatabase.getInstance().getReference("HitlerName").setValue(thisPlayer.name);
                    }

                    if (!presidentChosen) {
                        //Picks and sets the first president by random
                        int firstPresidentID = helper.AssignFirstPresidency(playerCount);
                        DatabaseReference presidentStatusRef = playersRef.child("Player_" + firstPresidentID).child("isPresident");
                        presidentStatusRef.setValue(true);

                        //Resets both of the active law trackers
                        gameBoardRef.child("Active_Laws").child("Liberal").setValue(0);
                        gameBoardRef.child("Active_Laws").child("Fascist").setValue(0);

                        //Shuffles the draw pile locally and posts the laws in order to the database
                        List<String> drawPile = helper.ShuffleLawsToDrawPile(6, 11);
                        gameBoardRef.child("Draw_Pile").setValue(drawPile);
                    }

                    DatabaseReference triggersRef = FirebaseDatabase.getInstance().getReference("Triggers");
                    triggersRef.child("Vote_Needed").setValue(false);
                    triggersRef.child("Game_Ended").setValue(false);
                    triggersRef.child("Chancellor_Needed").setValue(false);
                    countersRef.child("Players_Alive_Count").setValue(playerCount);
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
                    countersRef.child("Vote_Count").child("Ja_Votes").setValue(0);
                    countersRef.child("Vote_Count").child("Nein_Votes").setValue(0);
                    countersRef.child("Rounds_Without_Chancellor").setValue(0);
                    DatabaseReference chancellorsOptionsRef = FirebaseDatabase.getInstance().getReference("ChancellorsOptions");
                    chancellorsOptionsRef.child("Liberal").setValue(0);
                    chancellorsOptionsRef.child("Fascist").setValue(0);
                    gameBoardRef.child("Active_Laws").child("New_Active_Law").setValue("None");

                    Intent startGameIntent = new Intent(getApplicationContext(), SecondActivity.class);
                    startGameIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(startGameIntent);
                }
            });
        }

    }

    /*
    @Override
    public void onBackPressed() {

    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playersRef.removeEventListener(playerParameterListener);
    }

}
