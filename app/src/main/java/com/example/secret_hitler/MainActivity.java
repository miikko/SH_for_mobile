package com.example.secret_hitler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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
                dbHandler = DBHandler.getInstance(getApplicationContext());
                String playerName = nameEditText.getText().toString();
                int playerId = dbHandler.GetPlayerCount();
                Player newPlayer = new Player(playerId, "unknown", playerName, false, false, true);
                dbHandler.addNewPlayer(newPlayer);

                Intent confirmRoleIntent = new Intent(getApplicationContext(), LobbyActivity.class);
                confirmRoleIntent.putExtra("com.example.secret_hitler.PLAYER", newPlayer);
                startActivity(confirmRoleIntent);
            }
        });

    }
}
