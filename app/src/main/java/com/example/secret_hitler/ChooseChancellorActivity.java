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
    private DatabaseReference playerRef;
    private DatabaseReference chancellorCandidateNameRef;
    private ValueEventListener playerNameListener;
    private Player thisPlayer;
    private List<String> playerNames;
    private ArrayAdapter<String> spinnerAdapter;
    private String previousChancellorName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_chancellor);

        chooseChancellorTextView = findViewById(R.id.chooseChancellorTextView);
        chooseChancellorTextView.setText("Choose the Player that you want to be your Chancellor in this round from the Dropdown List.");
        chancellorCandidateSpinner = findViewById(R.id.chancellorCandidateSpinner);
        lockChancellorCandidateBtn = findViewById(R.id.lockChancellorCandidateBtn);
        playerRef = FirebaseDatabase.getInstance().getReference("Players");
        chancellorCandidateNameRef = FirebaseDatabase.getInstance().getReference("ChancellorCandidateName");

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }
        if (getIntent().hasExtra("com.example.secret_hitler.PREVIOUSCHANCELLORNAME")) {
            previousChancellorName = getIntent().getStringExtra("com.example.secret_hitler.PREVIOUSCHANCELLORNAME");
        } else {
            previousChancellorName = "none";
        }

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
                if (!previousChancellorName.equals("none")) {
                    playerNames.remove(previousChancellorName);
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
    protected void onDestroy() {
        super.onDestroy();
        playerRef.removeEventListener(playerNameListener);
    }

}
