package com.example.secret_hitler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChooseChancellorActivity extends AppCompatActivity {
    public DBHandler dbHandler;
    private TextView chooseChancellorTextView;
    private Spinner chancellorCandidateSpinner;
    private Button lockChancellorCandidateBtn;
    private Player thisPlayer;


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

        chooseChancellorTextView.setText("Choose the Player that you want to be your Chancellor in this round from the Dropdown List.");

        int playerCount = dbHandler.GetPlayerCount();
        List<String> playerNames = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            playerNames.add(dbHandler.GetName(i));
        }
        playerNames.remove(thisPlayer.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, playerNames);
        chancellorCandidateSpinner.setAdapter(adapter);

        lockChancellorCandidateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedChancellorCandidate = chancellorCandidateSpinner.getSelectedItem().toString();
            }
        });
    }
}
