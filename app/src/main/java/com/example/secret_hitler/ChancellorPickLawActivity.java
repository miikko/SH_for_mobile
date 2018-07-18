package com.example.secret_hitler;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChancellorPickLawActivity extends AppCompatActivity {
    private TextView chancellorLawHintTextView;
    private ImageButton chancellorFirstLawImgBtn;
    private ImageButton chancellorSecondLawImgBtn;
    private DatabaseReference chancellorsOptionsRef;
    private DatabaseReference gameBoardRef;
    private DatabaseReference triggersRef;
    private ValueEventListener chancellorsOptionsListener;
    private ValueEventListener activeLawsListener;
    private Player thisPlayer;
    private int liberalLawOptions;
    private int fascistLawOptions;
    private int activeLiberalLaws;
    private int activeFascistLaws;
    private int liberalLawImg;
    private int fascistLawImg;
    private boolean firstLawIsLiberal;
    private boolean secondLawIsLiberal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chancellor_pick_law);

        chancellorLawHintTextView = findViewById(R.id.chancellorLawHintTextView);
        chancellorLawHintTextView.setText("These 2 Laws were picked by the President. Choose the one you want to activate by pressing it.");
        chancellorFirstLawImgBtn = findViewById(R.id.chancellorFirstLawImgBtn);
        chancellorSecondLawImgBtn = findViewById(R.id.chancellorSecondLawImgBtn);
        chancellorsOptionsRef = FirebaseDatabase.getInstance().getReference("ChancellorsOptions");
        gameBoardRef = FirebaseDatabase.getInstance().getReference("Game_Board");
        triggersRef = FirebaseDatabase.getInstance().getReference("Triggers");
        triggersRef.child("Chancellor_Needed").setValue(false);
        liberalLawImg = R.drawable.liberal_law;
        fascistLawImg = R.drawable.fascist_law;
        firstLawIsLiberal = true;
        secondLawIsLiberal = true;

        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        chancellorsOptionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> bothFactionOptions = dataSnapshot.getChildren();
                for (DataSnapshot eachFactionOption : bothFactionOptions) {
                    if (eachFactionOption.getKey().equals("Liberal")) {
                        liberalLawOptions = eachFactionOption.getValue(int.class);
                    } else {
                        fascistLawOptions = eachFactionOption.getValue(int.class);
                    }
                }
                if (liberalLawOptions == 2) {
                    chancellorFirstLawImgBtn.setImageResource(liberalLawImg);
                    chancellorSecondLawImgBtn.setImageResource(liberalLawImg);
                } else if (fascistLawOptions == 2) {
                    chancellorFirstLawImgBtn.setImageResource(fascistLawImg);
                    chancellorSecondLawImgBtn.setImageResource(fascistLawImg);
                    firstLawIsLiberal = false;
                    secondLawIsLiberal = false;
                } else {
                    chancellorFirstLawImgBtn.setImageResource(liberalLawImg);
                    chancellorSecondLawImgBtn.setImageResource(fascistLawImg);
                    secondLawIsLiberal = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        chancellorsOptionsRef.addListenerForSingleValueEvent(chancellorsOptionsListener);

        activeLawsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> bothActiveLaws = dataSnapshot.getChildren();
                for (DataSnapshot eachActiveLaw : bothActiveLaws) {
                    if (eachActiveLaw.getKey().equals("Liberal")) {
                        activeLiberalLaws = eachActiveLaw.getValue(int.class);
                    } else {
                        activeFascistLaws = eachActiveLaw.getValue(int.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        gameBoardRef.child("Active_Laws").addListenerForSingleValueEvent(activeLawsListener);

        chancellorFirstLawImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstLawIsLiberal) {
                    gameBoardRef.child("Active_Laws").child("Liberal").setValue(activeLiberalLaws + 1);
                    gameBoardRef.child("Active_Laws").child("New_Active_Law").setValue("Liberal");
                } else {
                    gameBoardRef.child("Active_Laws").child("Fascist").setValue(activeFascistLaws + 1);
                    gameBoardRef.child("Active_Laws").child("New_Active_Law").setValue("Fascist");
                }
                Intent goToLawUnveilingActivity = new Intent(getApplicationContext(), LawUnveilingActivity.class);
                goToLawUnveilingActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                startActivity(goToLawUnveilingActivity);
            }
        });

        chancellorSecondLawImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (secondLawIsLiberal) {
                    gameBoardRef.child("Active_Laws").child("Liberal").setValue(activeLiberalLaws + 1);
                    gameBoardRef.child("Active_Laws").child("New_Active_Law").setValue("Liberal");
                } else {
                    gameBoardRef.child("Active_Laws").child("Fascist").setValue(activeFascistLaws + 1);
                    gameBoardRef.child("Active_Laws").child("New_Active_Law").setValue("Fascist");
                }
                Intent goToLawUnveilingActivity = new Intent(getApplicationContext(), LawUnveilingActivity.class);
                goToLawUnveilingActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                startActivity(goToLawUnveilingActivity);
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chancellorsOptionsRef.removeEventListener(chancellorsOptionsListener);
        gameBoardRef.child("Active_Laws").removeEventListener(activeLawsListener);
    }
}
