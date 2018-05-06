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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner roleListSpin = findViewById(R.id.roleListSpin);
        String[] roles = new String[] {"Liberal", "Fascist", "Hitler"};
        ArrayAdapter <String>  adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        roleListSpin.setAdapter(adapter);

        Button roleLockBtn = findViewById(R.id.roleLockBtn);
        roleLockBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick (View view) {
                String selectedRole = roleListSpin.getSelectedItem().toString();
                switch (selectedRole) {
                    case "Liberal":

                        break;
                    case "Fascist":

                        break;
                    case "Hitler":

                        break;
                    default:
                        break;
                }
                Intent confirmRoleIntent = new Intent(getApplicationContext(), SecondActivity.class);
                confirmRoleIntent.putExtra("com.example.secret_hitler.ROLE", selectedRole);
                startActivity(confirmRoleIntent);
            }
        });

    }
}
