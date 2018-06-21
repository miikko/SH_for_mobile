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

public class MainActivity extends AppCompatActivity {

    private EditText nameEditText;
    private Button joinGameButton;
    private DatabaseReference playersRef;
    private ValueEventListener playersEventListener;
    private int playerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameEditText = findViewById(R.id.nameEditText);
        nameEditText.setSingleLine(); //Without this line the user input checker may not work
        nameEditText.setCursorVisible(false);
        joinGameButton = findViewById(R.id.joinGameBtn);
        joinGameButton.setEnabled(false);
        playersRef = FirebaseDatabase.getInstance().getReference("Players");

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
                        } else {
                            joinGameButton.setEnabled(true);
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

        playersEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                playerCount = (int) dataSnapshot.getChildrenCount();
                DatabaseReference playerCountRef = FirebaseDatabase.getInstance().getReference("PlayerCount");
                playerCountRef.setValue(playerCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersRef.addValueEventListener(playersEventListener);

        joinGameButton.setOnClickListener(new View.OnClickListener() {

            public void onClick (View view) {

                //ADDING PLAYER TO THE DATABASE
                int playerID = playerCount;
                String playerName = nameEditText.getText().toString();
                Player newPlayer = new Player(playerID, "unknown", playerName, false, false, true, false, "none");
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
