package com.example.secret_hitler;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowVoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_vote);

        ImageView voteImageView = findViewById(R.id.showVoteImageView);

        if (getIntent().hasExtra("com.example.secret_hitler.VOTE")) {
            String vote = getIntent().getExtras().get("com.example.secret_hitler.VOTE").toString();

            if (vote.equals("Nein")) {
                voteImageView.setImageResource(R.drawable.nein_vote);
            } else {
                voteImageView.setImageResource(R.drawable.ja_vote);
            }
        }
    }
}
