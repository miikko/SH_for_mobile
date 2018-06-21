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

import java.util.ArrayList;
import java.util.List;


public class LobbyActivity extends AppCompatActivity {
    private Button startGameButton;
    private TextView playerCountTextView;
    private TextView lobbyStatusTextView;
    private DatabaseReference playerCountRef;
    private DatabaseReference playersRef;
    private DatabaseReference playersInThisRoundRef;
    private ValueEventListener playerCountListener;
    private ValueEventListener playerParameterListener;
    private ValueEventListener playersInThisRoundListener;
    private Helper helper;
    private Player thisPlayer;
    private ArrayList<String> playerRoles;
    private int playerCount;
    private boolean presidentChosen;
    private int numOfPlayersInThisRound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        helper = new Helper();
        playerRoles = new ArrayList<>();

        startGameButton = findViewById(R.id.startGameBtn);
        Button resetButton = findViewById(R.id.resetBtn);
        playerCountTextView = findViewById(R.id.playerCountTextView);
        lobbyStatusTextView = findViewById(R.id.lobbyStatusTextView);
        playerCountRef = FirebaseDatabase.getInstance().getReference("PlayerCount");
        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        playersInThisRoundRef = FirebaseDatabase.getInstance().getReference("PlayersInThisRound");

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYERCOUNT")) {
            playerCount = getIntent().getIntExtra("com.example.secret_hitler.PLAYERCOUNT", playerCount);
            playerCount++;
        }

        playerCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    playerCount = dataSnapshot.getValue(int.class);
                } else {
                    playerCount = 0;
                }
                playerCountTextView.setText("Number of players: " + playerCount);
                if (playerCount < 5) {
                    lobbyStatusTextView.setText("You don't have enough player to start playing.");
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playerCountRef.addValueEventListener(playerCountListener);

        playerParameterListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> players = dataSnapshot.getChildren();
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

        playersInThisRoundListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    numOfPlayersInThisRound = dataSnapshot.getValue(int.class);
                } else {
                    playersInThisRoundRef.setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersInThisRoundRef.addValueEventListener(playersInThisRoundListener);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                rootRef.child("Players").setValue(null);
                rootRef.child("PlayerCount").setValue(null);
                rootRef.child("PresidentID").setValue(null);
                rootRef.child("Game_Board").setValue(null);
                rootRef.child("LawCount").setValue(null);
                rootRef.child("VoteNeeded").setValue(null);
                rootRef.child("ChancellorCandidateName").setValue(null);
                rootRef.child("RemainingLaws").setValue(null);
                rootRef.child("DiscardedLaws").setValue(null);
                rootRef.child("ActiveLaws").setValue(null);
                rootRef.child("ChancellorNeeded").setValue(null);
                rootRef.child("ChancellorsOptions").setValue(null);
                rootRef.child("PreviousChancellorName").setValue(null);
                rootRef.child("VoteCount").child("Ja_Votes").setValue(0);
                rootRef.child("VoteCount").child("Nein_Votes").setValue(0);
                rootRef.child("NewActiveLaw").setValue("None");
                rootRef.child("WinnerFaction").setValue(null);
                rootRef.child("HitlerName").setValue(null);
                rootRef.child("PlayersInThisRound").setValue(null);
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
                        FirebaseDatabase.getInstance().getReference("PresidentID").setValue(firstPresidentID);

                        //Creates the Game board in the database.
                        //The Game board contains the draw pile and the active law trackers
                        DatabaseReference gameBoardRef = FirebaseDatabase.getInstance().getReference("Game_Board");

                        //Resets both of the active law trackers
                        gameBoardRef.child("Active_Laws").child("Liberal").setValue(0);
                        gameBoardRef.child("Active_Laws").child("Fascist").setValue(0);

                        //Shuffles the draw pile locally and posts the laws in order to the database
                        DatabaseReference drawPileRef = gameBoardRef.child("Draw_Pile");
                        List<String> drawPile = helper.ShuffleLawsToDrawPile(6, 11);
                        drawPileRef.setValue(drawPile);
                    }

                    DatabaseReference voteNeededRef = FirebaseDatabase.getInstance().getReference("VoteNeeded");
                    voteNeededRef.setValue(false);
                    DatabaseReference voteCountRef = FirebaseDatabase.getInstance().getReference("VoteCount");
                    voteCountRef.child("Ja_Votes").setValue(0);
                    voteCountRef.child("Nein_Votes").setValue(0);
                    DatabaseReference chancellorNeededRef = FirebaseDatabase.getInstance().getReference("ChancellorNeeded");
                    chancellorNeededRef.setValue(false);
                    DatabaseReference chancellorsOptionsRef = FirebaseDatabase.getInstance().getReference("ChancellorsOptions");
                    chancellorsOptionsRef.child("Liberal").setValue(0);
                    chancellorsOptionsRef.child("Fascist").setValue(0);
                    DatabaseReference newActiveLawRef = FirebaseDatabase.getInstance().getReference("NewActiveLaw");
                    newActiveLawRef.setValue("None");
                    playersInThisRoundRef.setValue(numOfPlayersInThisRound + 1);

                    Intent startGameIntent = new Intent(getApplicationContext(), SecondActivity.class);
                    startGameIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                    startActivity(startGameIntent);
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerCountRef.removeEventListener(playerCountListener);
        playersRef.removeEventListener(playerParameterListener);
        playersInThisRoundRef.removeEventListener(playersInThisRoundListener);
    }

}
