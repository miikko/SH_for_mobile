package com.example.secret_hitler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ShowVoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_vote);

        TextView voteTextView = findViewById(R.id.voteTextView);

        if (getIntent().hasExtra("com.example.secret_hitler.VOTE")) {
            String vote = getIntent().getExtras().get("com.example.secret_hitler.VOTE").toString();
            voteTextView.setText(vote);
        }
    }
}
