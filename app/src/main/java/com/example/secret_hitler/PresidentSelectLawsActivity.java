package com.example.secret_hitler;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PresidentSelectLawsActivity extends AppCompatActivity {
    private TextView presidentChooseLawsTextView;
    private ImageButton presidentFirstImgBtn;
    private ImageButton presidentSecondImgBtn;
    private ImageButton presidentThirdImgBtn;
    private DatabaseReference gameBoardRef;
    private DatabaseReference triggersRef;
    private DatabaseReference chancellorsOptionsRef;
    private ValueEventListener drawPileListener;
    private Player thisPlayer;
    private Helper helper;
    private int liberalLawImg;
    private int fascistLawImg;
    private int liberalLawsActive;
    private int fascistLawsActive;
    private boolean firstBtnImgSet;
    private boolean secondBtnImgSet;
    private boolean thirdBtnImgSet;
    private boolean firstBtnIsLiberal;
    private boolean secondBtnIsLiberal;
    private boolean thirdBtnIsLiberal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_president_select_laws);

        presidentChooseLawsTextView = findViewById(R.id.presidentChooseLawsTextView);
        presidentChooseLawsTextView.setText("Press the Law that you want to discard. The other 2 will be sent to the Chancellor.");
        presidentFirstImgBtn = findViewById(R.id.presidentFirstLawImgBtn);
        presidentSecondImgBtn = findViewById(R.id.presidentSecondLawImgBtn);
        presidentThirdImgBtn = findViewById(R.id.presidentThirdLawImgBtn);
        liberalLawImg = R.drawable.liberal_law;
        fascistLawImg = R.drawable.fascist_law;
        gameBoardRef = FirebaseDatabase.getInstance().getReference("Game_Board");
        triggersRef = FirebaseDatabase.getInstance().getReference("Triggers");
        chancellorsOptionsRef = FirebaseDatabase.getInstance().getReference("ChancellorsOptions");
        helper = new Helper();
        firstBtnImgSet = false;
        secondBtnImgSet = false;
        thirdBtnImgSet = false;
        firstBtnIsLiberal = true;
        secondBtnIsLiberal = true;
        thirdBtnIsLiberal = true;

        if (getIntent().hasExtra("com.example.secret_hitler.ACTIVELIBERALLAWS")) {
            liberalLawsActive = getIntent().getIntExtra("com.example.secret_hitler.ACTIVELIBERALLAWS", liberalLawsActive);
        }
        if (getIntent().hasExtra("com.example.secret_hitler.ACTIVEFASCISTLAWS")) {
            fascistLawsActive = getIntent().getIntExtra("com.example.secret_hitler.ACTIVEFASCISTLAWS", fascistLawsActive);
        }
        if (getIntent().hasExtra("com.example.secret_hitler.PLAYER")) {
            thisPlayer = getIntent().getParcelableExtra("com.example.secret_hitler.PLAYER");
        }

        drawPileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> drawPile = (List<String>) dataSnapshot.getValue();

                if (!thirdBtnImgSet) {
                    //If there are no laws in the draw pile, the discard pile will be shuffled to the draw pile
                    if (drawPile.isEmpty()) {
                        drawPile = helper.ShuffleLawsToDrawPile(6 - liberalLawsActive, 11 - fascistLawsActive);
                    } else {
                        String thisLawString = drawPile.get(0);
                        if (!firstBtnImgSet) {
                            if (thisLawString.equals("Liberal")) {
                                presidentFirstImgBtn.setImageResource(liberalLawImg);
                            } else {
                                presidentFirstImgBtn.setImageResource(fascistLawImg);
                                firstBtnIsLiberal = false;
                            }
                            firstBtnImgSet = true;
                        } else if (!secondBtnImgSet) {
                            if (thisLawString.equals("Liberal")) {
                                presidentSecondImgBtn.setImageResource(liberalLawImg);
                            } else {
                                presidentSecondImgBtn.setImageResource(fascistLawImg);
                                secondBtnIsLiberal = false;
                            }
                            secondBtnImgSet = true;
                        } else {
                            if (thisLawString.equals("Liberal")) {
                                presidentThirdImgBtn.setImageResource(liberalLawImg);
                            } else {
                                presidentThirdImgBtn.setImageResource(fascistLawImg);
                                thirdBtnIsLiberal = false;
                            }
                            thirdBtnImgSet = true;
                        }
                        drawPile.remove(0);
                    }
                    gameBoardRef.child("Draw_Pile").setValue(drawPile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        gameBoardRef.child("Draw_Pile").addValueEventListener(drawPileListener);

        presidentFirstImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (secondBtnIsLiberal && thirdBtnIsLiberal) {
                    chancellorsOptionsRef.child("Liberal").setValue(2);
                    chancellorsOptionsRef.child("Fascist").setValue(0);
                } else if (!secondBtnIsLiberal && !thirdBtnIsLiberal) {
                    chancellorsOptionsRef.child("Liberal").setValue(0);
                    chancellorsOptionsRef.child("Fascist").setValue(2);
                } else {
                    chancellorsOptionsRef.child("Liberal").setValue(1);
                    chancellorsOptionsRef.child("Fascist").setValue(1);
                }
                triggersRef.child("Chancellor_Needed").setValue(true);
                Intent goToLawUnveilingActivity = new Intent(getApplicationContext(), LawUnveilingActivity.class);
                goToLawUnveilingActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                startActivity(goToLawUnveilingActivity);
            }
        });

        presidentSecondImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstBtnIsLiberal && thirdBtnIsLiberal) {
                    chancellorsOptionsRef.child("Liberal").setValue(2);
                    chancellorsOptionsRef.child("Fascist").setValue(0);
                } else if (!firstBtnIsLiberal && !thirdBtnIsLiberal) {
                    chancellorsOptionsRef.child("Liberal").setValue(0);
                    chancellorsOptionsRef.child("Fascist").setValue(2);
                } else {
                    chancellorsOptionsRef.child("Liberal").setValue(1);
                    chancellorsOptionsRef.child("Fascist").setValue(1);
                }
                triggersRef.child("Chancellor_Needed").setValue(true);
                Intent goToLawUnveilingActivity = new Intent(getApplicationContext(), LawUnveilingActivity.class);
                goToLawUnveilingActivity.putExtra("com.example.secret_hitler.PLAYER", thisPlayer);
                startActivity(goToLawUnveilingActivity);
            }
        });

        presidentThirdImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstBtnIsLiberal && secondBtnIsLiberal) {
                    chancellorsOptionsRef.child("Liberal").setValue(2);
                    chancellorsOptionsRef.child("Fascist").setValue(0);
                } else if (!firstBtnIsLiberal && !secondBtnIsLiberal) {
                    chancellorsOptionsRef.child("Liberal").setValue(0);
                    chancellorsOptionsRef.child("Fascist").setValue(2);
                } else {
                    chancellorsOptionsRef.child("Liberal").setValue(1);
                    chancellorsOptionsRef.child("Fascist").setValue(1);
                }
                triggersRef.child("Chancellor_Needed").setValue(true);
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
        gameBoardRef.child("Draw_Pile").removeEventListener(drawPileListener);
    }

}
