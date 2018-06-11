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
    public DBHandler dbHandler;
    private TextView chooseChancellorTextView;
    private Spinner chancellorCandidateSpinner;
    private Button lockChancellorCandidateBtn;
    private Player thisPlayer;
    private List<String> playerNames;
    private ArrayAdapter<String> spinnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_chancellor);

        dbHandler = DBHandler.getInstance(getApplicationContext());
        chooseChancellorTextView = findViewById(R.id.chooseChancellorTextView);
        chancellorCandidateSpinner = findViewById(R.id.chancellorCandidateSpinner);
        lockChancellorCandidateBtn = findViewById(R.id.lockChancellorCandidateBtn);

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        DatabaseReference playerParameterRef = FirebaseDatabase.getInstance().getReference("Players");
        ValueEventListener playerNameListener = new ValueEventListener() {
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
                if (spinnerAdapter == null) {
                    spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, playerNames);
                    chancellorCandidateSpinner.setAdapter(spinnerAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playerParameterRef.addListenerForSingleValueEvent(playerNameListener);

        chooseChancellorTextView.setText("Choose the Player that you want to be your Chancellor in this round from the Dropdown List.");

        lockChancellorCandidateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedChancellorCandidate = chancellorCandidateSpinner.getSelectedItem().toString();
                DatabaseReference chancellorCandidateNameRef = FirebaseDatabase.getInstance().getReference("ChancellorCandidateName");
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
}
