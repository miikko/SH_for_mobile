package com.example.secret_hitler;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChooseChancellorActivity extends AppCompatActivity {

    private TextView chooseChancellorTextView;
    private Spinner chancellorCandidateSpinner;
    private Button lockChancellorCandidateBtn;
    private DatabaseReference playersAliveCountRef;
    private DatabaseReference previousGovernmentRef;
    private DatabaseReference playerRef;
    private DatabaseReference chancellorCandidateNameRef;
    private ValueEventListener playersAliveCountListener;
    private ValueEventListener restrictedPlayerNamesListener;
    private ValueEventListener playerNameListener;
    private Player thisPlayer;
    private List<String> playerNames;
    private ArrayAdapter<String> spinnerAdapter;
    private String previousPresidentName;
    private String previousChancellorName;
    private int numOfPlayersAlive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_chancellor);

        chooseChancellorTextView = findViewById(R.id.chooseChancellorTextView);
        chooseChancellorTextView.setText("Choose the Player that you want to be your Chancellor in this round from the Dropdown List.");
        chancellorCandidateSpinner = findViewById(R.id.chancellorCandidateSpinner);
        lockChancellorCandidateBtn = findViewById(R.id.lockChancellorCandidateBtn);
        playersAliveCountRef = FirebaseDatabase.getInstance().getReference("PlayersAliveCount");
        previousGovernmentRef = FirebaseDatabase.getInstance().getReference("PreviousGovernment");
        playerRef = FirebaseDatabase.getInstance().getReference("Players");
        chancellorCandidateNameRef = FirebaseDatabase.getInstance().getReference("ChancellorCandidateName");

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        playersAliveCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfPlayersAlive = dataSnapshot.getValue(int.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersAliveCountRef.addListenerForSingleValueEvent(playersAliveCountListener);

        restrictedPlayerNamesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterable<DataSnapshot> bothRestrictedPlayerNames = dataSnapshot.getChildren();
                    for (DataSnapshot eachRestrictedPlayerName : bothRestrictedPlayerNames) {
                        if (eachRestrictedPlayerName.getKey().equals("PreviousPresident")) {
                            previousPresidentName = eachRestrictedPlayerName.getValue().toString();
                        } else {
                            previousChancellorName = eachRestrictedPlayerName.getValue().toString();
                        }
                    }
                } else {
                    previousPresidentName = "None";
                    previousChancellorName = "None";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        previousGovernmentRef.addListenerForSingleValueEvent(restrictedPlayerNamesListener);

        playerNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                playerNames = new ArrayList<>();
                Iterable<DataSnapshot> players = dataSnapshot.getChildren();
                for (DataSnapshot player : players) {
                    Iterable<DataSnapshot> parameters = player.getChildren();
                    for (DataSnapshot parameter : parameters) {
                        if (parameter.getKey().equals("name")) {
                            playerNames.add(parameter.getValue().toString());
                        }
                    }
                }
                playerNames.remove(thisPlayer.name);
                if (!previousChancellorName.equals("None")) {
                    playerNames.remove(previousChancellorName);
                }
                if (numOfPlayersAlive > 4 && !previousPresidentName.equals("None")) {
                    playerNames.remove(previousPresidentName);
                }
                if (spinnerAdapter == null) {
                    spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.my_spinner_text, R.id.textView, playerNames);
                    chancellorCandidateSpinner.setAdapter(spinnerAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playerRef.addListenerForSingleValueEvent(playerNameListener);

        lockChancellorCandidateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedChancellorCandidate = chancellorCandidateSpinner.getSelectedItem().toString();
                chancellorCandidateNameRef.setValue(selectedChancellorCandidate);
                DatabaseReference voteNeeded = FirebaseDatabase.getInstance().getReference("VoteNeeded");
                voteNeeded.setValue(true);
                Intent moveToPresidentWaitingRoomIntent = new Intent(getApplicationContext(), PresidentVotingWaitingRoomActivity.class);
                moveToPresidentWaitingRoomIntent.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                moveToPresidentWaitingRoomIntent.putExtra("com.example.secret_hitler.CHANCELLORCANDIDATENAME", selectedChancellorCandidate);
                startActivity(moveToPresidentWaitingRoomIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playersAliveCountRef.removeEventListener(playersAliveCountListener);
        previousGovernmentRef.removeEventListener(restrictedPlayerNamesListener);
        playerRef.removeEventListener(playerNameListener);
    }

}
