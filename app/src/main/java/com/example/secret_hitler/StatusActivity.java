package com.example.secret_hitler;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatusActivity extends AppCompatActivity {

    private TextView lawCountTextView;
    private TextView leaderTextView;
    private TextView yourVoteIsNeededTextView;
    private Button chooseChancellorBtn;
    private Player thisPlayer;
    private String presidentName;
    private DatabaseReference gameBoardRef;
    private DatabaseReference playersRef;
    private DatabaseReference voteNeededRef;
    private ValueEventListener activeLawCountListener;
    private ValueEventListener playerParameterListener;
    private ValueEventListener voteNeededListener;
    private int fascistLawCount;
    private int liberalLawCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        lawCountTextView = findViewById(R.id.lawCountTextView);
        leaderTextView = findViewById(R.id.leaderTextView);
        yourVoteIsNeededTextView = findViewById(R.id.yourVoteIsNeededTextView);
        yourVoteIsNeededTextView.setVisibility(View.INVISIBLE);
        chooseChancellorBtn = findViewById(R.id.chooseChancellorBtn);
        chooseChancellorBtn.setVisibility(View.INVISIBLE);
        gameBoardRef = FirebaseDatabase.getInstance().getReference("Game_Board");
        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        voteNeededRef = FirebaseDatabase.getInstance().getReference("VoteNeeded");

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        activeLawCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("children", dataSnapshot.getChildrenCount() + "");
                if (dataSnapshot.exists()) {
                    Iterable<DataSnapshot> bothActiveLawCounts = dataSnapshot.getChildren();
                    for (DataSnapshot eachActiveLawCount : bothActiveLawCounts) {
                        if (eachActiveLawCount.getKey().equals("Liberal")) {
                            liberalLawCount = eachActiveLawCount.getValue(int.class);
                        } else {
                            fascistLawCount = eachActiveLawCount.getValue(int.class);
                        }
                    }
                    lawCountTextView.setText("There are " + fascistLawCount + " active Fascist laws and " + liberalLawCount + " active Liberal laws.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        gameBoardRef.child("Active_Laws").addListenerForSingleValueEvent(activeLawCountListener);

        playerParameterListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean stopLoop = false;
                Iterable<DataSnapshot> players = dataSnapshot.getChildren();
                for (DataSnapshot player : players) {
                    Iterable<DataSnapshot> parameters = player.getChildren();
                    for (DataSnapshot parameter : parameters) {
                        if (parameter.getKey().equals("name")) {
                            presidentName = parameter.getValue().toString();
                        } else if (parameter.getKey().equals("isPresident") && parameter.getValue(boolean.class)) {
                            stopLoop = true;
                        }
                    }
                    if (stopLoop) {
                        break;
                    }
                }
                if (thisPlayer.name.equals(presidentName)) {
                    thisPlayer.SetAsPresident();
                    leaderTextView.setText("You are the President.");
                    chooseChancellorBtn.setVisibility(View.VISIBLE);
                } else if (thisPlayer.isChancellor) {
                    leaderTextView.setText("You are the Chancellor.");
                } else {
                    leaderTextView.setText(presidentName + " is the current president.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersRef.addValueEventListener(playerParameterListener);

        voteNeededListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(boolean.class)) {
                    yourVoteIsNeededTextView.setVisibility(View.VISIBLE);
                    yourVoteIsNeededTextView.setText("The president has picked his Chancellor for this round. Move to the Voting screen to vote on the matter.");
                } else {
                    yourVoteIsNeededTextView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        voteNeededRef.addValueEventListener(voteNeededListener);

        chooseChancellorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseChancellorIntent = new Intent(getApplicationContext(), ChooseChancellorActivity.class);
                chooseChancellorIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                startActivity(chooseChancellorIntent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameBoardRef.child("Active_Laws").removeEventListener(activeLawCountListener);
        playersRef.removeEventListener(playerParameterListener);
        voteNeededRef.removeEventListener(voteNeededListener);
    }

}
