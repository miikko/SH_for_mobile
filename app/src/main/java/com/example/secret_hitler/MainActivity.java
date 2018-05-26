package com.example.secret_hitler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public DBHandler dbHandler;
    private EditText nameEditText;
    private Button joinGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameEditText = findViewById(R.id.nameEditText);
        nameEditText.setSingleLine(); //Without this line the user input checker may not work
        nameEditText.setCursorVisible(false);
        joinGameButton = findViewById(R.id.joinGameBtn);
        joinGameButton.setEnabled(false);

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

        joinGameButton.setOnClickListener(new View.OnClickListener() {

            public void onClick (View view) {

                Player newPlayer = new Player();
                dbHandler = DBHandler.getInstance(getApplicationContext());
                dbHandler.addNewPlayer(newPlayer);
                int playerId = dbHandler.GetPlayerCount() - 1;
                newPlayer.SetID(playerId);

                Intent confirmRoleIntent = new Intent(getApplicationContext(), LobbyActivity.class);
                confirmRoleIntent.putExtra("com.example.secret_hitler.PLAYER_ID", playerId);
                confirmRoleIntent.putExtra("bää", newPlayer);
                startActivity(confirmRoleIntent);
            }
        });

    }
}
