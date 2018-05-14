package com.example.secret_hitler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    public DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button joinGameButton = findViewById(R.id.joinGameBtn);
        joinGameButton.setOnClickListener(new View.OnClickListener() {

            public void onClick (View view) {

                Player newPlayer = new Player();
                dbHandler = DBHandler.getInstance(getApplicationContext());
                dbHandler.addNewPlayer(newPlayer);
                int playerId = dbHandler.GetPlayerCount() - 1;

                Intent confirmRoleIntent = new Intent(getApplicationContext(), LobbyActivity.class);
                confirmRoleIntent.putExtra("com.example.secret_hitler.PLAYER_ID", playerId);
                startActivity(confirmRoleIntent);
            }
        });

    }
}
