package com.example.secret_hitler;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView hintTextView;
    private EditText nameEditText;
    private Button joinGameButton;
    private DatabaseReference playersRef;
    private ValueEventListener playersEventListener;
    private List<String> playerNames;
    private String thisPlayerName;
    private int playerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hintTextView = findViewById(R.id.firstActivityHintTextView);
        hintTextView.setText("Join the game by typing your name and pressing the button.");
        nameEditText = findViewById(R.id.nameEditText);
        nameEditText.setSingleLine(); //Without this line the user input checker may not work
        nameEditText.setCursorVisible(false);
        joinGameButton = findViewById(R.id.joinGameBtn);
        joinGameButton.setEnabled(false);
        playersRef = FirebaseDatabase.getInstance().getReference("Players");

        playersEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                playerCount = (int) dataSnapshot.getChildrenCount();
                DatabaseReference playerCountRef = FirebaseDatabase.getInstance().getReference("PlayerCount");
                playerCountRef.setValue(playerCount);
                Iterable<DataSnapshot> allPlayers = dataSnapshot.getChildren();
                playerNames = new ArrayList<>();
                for (DataSnapshot eachPlayer : allPlayers) {
                    Iterable<DataSnapshot> playerParameters = eachPlayer.getChildren();
                    for (DataSnapshot parameter : playerParameters) {
                        if (parameter.getKey().equals("name")) {
                            playerNames.add(parameter.getValue().toString());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersRef.addValueEventListener(playersEventListener);

        nameEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        // the user is done typing.
                        nameEditText.setCursorVisible(false);
                        if (nameEditText.getText().toString().isEmpty()) {
                            joinGameButton.setEnabled(false);
                            hintTextView.setText("Join the game by typing your name and pressing the button.");
                        } else if (playerNames.contains(nameEditText.getText().toString())) {
                            hintTextView.setText("That name has already been taken by another player. Please choose another name.");
                            joinGameButton.setEnabled(false);
                        } else if (nameEditText.getText().toString().equals("None")) {
                            hintTextView.setText("That name is not acceptable. Please choose another name.");
                            joinGameButton.setEnabled(false);
                        } else {
                            joinGameButton.setEnabled(true);
                            hintTextView.setText("Join the game by typing your name and pressing the button.");
                        }
                    }
                }
                return false;
            }
        });

        nameEditText.setOnClickListener(new EditText.OnClickListener() {

            @Override
            public void onClick(View v) {
                nameEditText.setCursorVisible(true);
            }
        });

        joinGameButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                //ADDING PLAYER TO THE DATABASE
                int playerID = playerCount;
                thisPlayerName = nameEditText.getText().toString();
                Player newPlayer = new Player(playerID, "unknown", thisPlayerName, false, false, true, false, "none");
                playersRef.child("Player_" + playerID).setValue(newPlayer);
                //PLAYER HAS BEEN ADDED TO THE DATABASE

                Intent confirmRoleIntent = new Intent(getApplicationContext(), LobbyActivity.class);
                confirmRoleIntent.putExtra("com.example.secret_hitler.PLAYER", newPlayer);
                confirmRoleIntent.putExtra("com.example.secret_hitler.PLAYERCOUNT", playerCount);
                startActivity(confirmRoleIntent);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playersRef.removeEventListener(playersEventListener);
    }
}
