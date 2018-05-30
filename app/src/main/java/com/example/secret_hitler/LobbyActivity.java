package com.example.secret_hitler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LobbyActivity extends AppCompatActivity {

    public DBHandler dbHandler;
    private RoleHandler roleHandler;
    int playerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        dbHandler = DBHandler.getInstance(getApplicationContext());
        roleHandler = new RoleHandler();
        playerID = 0;

        Button startGameButton = findViewById(R.id.startGameBtn);
        startGameButton.setEnabled(false);
        Button lobbyRefreshButton = findViewById(R.id.lobbyRefreshBtn);
        Button resetButton = findViewById(R.id.resetBtn);
        TextView playerCountTextView = findViewById(R.id.playerCountTextView);
        TextView lobbyStatusTextView = findViewById(R.id.lobbyStatusTextView);

        int playerCount = dbHandler.GetPlayerCount();
        playerCountTextView.setText("Number of players: " + playerCount);
        if (playerCount < 5) {
            lobbyStatusTextView.setText("You don't have enough players to start the game.");
        } else if (playerCount > 10) {
            int numberOfExcessPlayers = playerCount - 10;
            lobbyStatusTextView.setText("You have too many players. You need to remove " + numberOfExcessPlayers + " players from the game to start.");
        } else {
            int fascistCount = 0;
            int liberalCount = 0;
            switch (playerCount) {
                case 5:
                    liberalCount = 3;
                    fascistCount = 1;
                    break;
                case 6:
                    liberalCount = 4;
                    fascistCount = 1;
                    break;
                case 7:
                    liberalCount = 4;
                    fascistCount = 2;
                    break;
                case 8:
                    liberalCount = 5;
                    fascistCount = 2;
                    break;
                case 9:
                    liberalCount = 5;
                    fascistCount = 3;
                    break;
                case 10:
                    liberalCount = 6;
                    fascistCount = 3;
                    break;
                default:
                    break;
            }
            lobbyStatusTextView.setText("There will be " + liberalCount + " liberals, " + fascistCount + " fascists and 1 Hitler in this game.");
            startGameButton.setEnabled(true);
        }

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER_ID")) {
            playerID = (int) getIntent().getExtras().get("com.example.secret_hitler.PLAYER_ID");
        }

        lobbyRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent refresh = getIntent();
                finish();
                startActivity(refresh);
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHandler.ClearTable("playerDetails");
            }
        });

        if (startGameButton.isEnabled()) {
            startGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    roleHandler.AssignRole(getApplicationContext(), playerID);
                    Intent startGameIntent = new Intent(getApplicationContext(), SecondActivity.class);
                    startGameIntent.putExtra("com.example.secret_hitler.PLAYER_ID", playerID);
                    startActivity(startGameIntent);
                }
            });
        }

    }
}
