package com.example.secret_hitler;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatusActivity extends AppCompatActivity {

    private TextView lawCountTextView;
    private TextView leaderTextView;
    private TextView yourVoteIsNeededTextView;
    private Button selectLawsBtn;
    private Button clearLawsBtn;
    private Button chooseChancellorBtn;
    private Player thisPlayer;
    private String presidentName;
    private DatabaseReference lawCountRef;
    private DatabaseReference playersRef;
    private DatabaseReference voteNeededRef;
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
        selectLawsBtn = findViewById(R.id.selectLawsBtn);
        clearLawsBtn = findViewById(R.id.clearLawsBtn);
        chooseChancellorBtn = findViewById(R.id.chooseChancellorBtn);
        chooseChancellorBtn.setVisibility(View.INVISIBLE);

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        lawCountRef = FirebaseDatabase.getInstance().getReference("LawCount");
        ValueEventListener lawCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> bothLawCounts = dataSnapshot.getChildren();
                for (DataSnapshot eachLawCount : bothLawCounts) {
                    if (eachLawCount.getKey().equals("Liberal")) {
                        liberalLawCount = eachLawCount.getValue(int.class);
                    } else {
                        fascistLawCount = eachLawCount.getValue(int.class);
                    }
                }
                lawCountTextView.setText("There are " + fascistLawCount + " Fascist laws and " + liberalLawCount + " Liberal laws.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        lawCountRef.addValueEventListener(lawCountListener);

        playersRef = FirebaseDatabase.getInstance().getReference("Players");
        ValueEventListener playerParameterListener = new ValueEventListener() {
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
                if (thisPlayer.isPresident) {
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

        voteNeededRef = FirebaseDatabase.getInstance().getReference("VoteNeeded");
        ValueEventListener voteNeededListener = new ValueEventListener() {
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

        selectLawsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lawCountRef.child("Fascist").setValue(fascistLawCount + 1);
            }
        });

        clearLawsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lawCountRef.child("Liberal").setValue(0);
                lawCountRef.child("Fascist").setValue(0);
            }
        });

        chooseChancellorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseChancellorIntent = new Intent(getApplicationContext(), ChooseChancellorActivity.class);
                chooseChancellorIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                startActivity(chooseChancellorIntent);
            }
        });
    }

}
