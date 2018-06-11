package com.example.secret_hitler;

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
    private DatabaseReference remainingLawsRef;
    private DatabaseReference discardedLawsRef;
    private Helper helper;
    private int liberalLawImg;
    private int fascistLawImg;
    private int remainingLiberalLaws;
    private int remainingFascistLaws;
    private int discardedLiberalLaws;
    private int discardedFascistLaws;
    private int liberalLawsActive;
    private int fascistLawsActive;
    private boolean firstBtnImgSet;
    private boolean secondBtnImgSet;
    private boolean thirdBtnImgSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_president_select_laws);

        presidentChooseLawsTextView = findViewById(R.id.presidentChooseLawsTextView);
        presidentChooseLawsTextView.setText("Press the law that you want to discard. The other 2 will be sent to the Chancellor.");
        presidentFirstImgBtn = findViewById(R.id.presidentFirstLawImgBtn);
        presidentSecondImgBtn = findViewById(R.id.presidentSecondLawImgBtn);
        presidentThirdImgBtn = findViewById(R.id.presidentThirdLawImgBtn);
        liberalLawImg = R.drawable.liberal_law;
        fascistLawImg = R.drawable.fascist_law;
        remainingLawsRef = FirebaseDatabase.getInstance().getReference("RemainingLaws");
        discardedLawsRef = FirebaseDatabase.getInstance().getReference("DiscardedLaws");
        helper = new Helper();
        firstBtnImgSet = false;
        secondBtnImgSet = false;
        thirdBtnImgSet = false;

        if (getIntent().hasExtra("com.example.secret_hitler.ACTIVELIBERALLAWS")) {
            liberalLawsActive = getIntent().getIntExtra("com.example.secret_hitler.ACTIVELIBERALLAWS", liberalLawsActive);
        }
        if (getIntent().hasExtra("com.example.secret_hitler.ACTIVEFASCISTLAWS")) {
            fascistLawsActive = getIntent().getIntExtra("com.example.secret_hitler.ACTIVEFASCISTLAWS", fascistLawsActive);
        }

        ValueEventListener discardedLawCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> bothDiscardedLawCounts = dataSnapshot.getChildren();
                for (DataSnapshot eachDiscardedLawCount : bothDiscardedLawCounts) {
                    if (eachDiscardedLawCount.getKey().equals("Liberal")) {
                        discardedLiberalLaws = eachDiscardedLawCount.getValue(int.class);
                    } else {
                        discardedFascistLaws = eachDiscardedLawCount.getValue(int.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        discardedLawsRef.addValueEventListener(discardedLawCountListener);

        ValueEventListener remainingLawCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> bothLawCounts = dataSnapshot.getChildren();
                for (DataSnapshot eachLawCount : bothLawCounts) {
                    if (eachLawCount.getKey().equals("Liberal")) {
                        remainingLiberalLaws = eachLawCount.getValue(int.class);
                    } else {
                        remainingFascistLaws = eachLawCount.getValue(int.class);
                    }
                }
                if (!thirdBtnImgSet) {
                    if (remainingLiberalLaws + remainingFascistLaws == 0) {
                        //Shuffles the discard pile back to the remaining laws pile
                        //Then calls this method again.
                        remainingLawsRef.child("Liberal").setValue(discardedLiberalLaws - liberalLawsActive);
                        discardedLawsRef.child("Liberal").setValue(0);
                        remainingLawsRef.child("Fascist").setValue(discardedFascistLaws - fascistLawsActive);
                        discardedLawsRef.child("Fascist").setValue(0);
                    } else if (remainingLiberalLaws + remainingFascistLaws == 1) {
                        int thisLawImg;
                        if (remainingLiberalLaws == 1) {
                            thisLawImg = liberalLawImg;
                        } else {
                            thisLawImg = fascistLawImg;
                        }
                        if (!firstBtnImgSet) {
                            presidentFirstImgBtn.setImageResource(thisLawImg);
                            firstBtnImgSet = true;
                        } else if (!secondBtnImgSet) {
                            presidentSecondImgBtn.setImageResource(thisLawImg);
                            secondBtnImgSet = true;
                        } else {
                            presidentThirdImgBtn.setImageResource(thisLawImg);
                            thirdBtnImgSet = true;
                        }
                        remainingLawsRef.child("Liberal").setValue(discardedLiberalLaws - liberalLawsActive);
                        discardedLawsRef.child("Liberal").setValue(0);
                        remainingLawsRef.child("Fascist").setValue(discardedFascistLaws - fascistLawsActive);
                        discardedLawsRef.child("Fascist").setValue(0);
                        firstBtnImgSet = true;
                    } else if (remainingLiberalLaws + remainingFascistLaws == 2) {
                        int firstLawImg;
                        int secondLawImg;
                        boolean liberalLawFirst = helper.rand.nextBoolean();
                        if (remainingLiberalLaws == 2) {
                            firstLawImg = liberalLawImg;
                            secondLawImg = liberalLawImg;
                        } else if (remainingFascistLaws == 2) {
                            firstLawImg = fascistLawImg;
                            secondLawImg = fascistLawImg;
                        } else {
                            if (liberalLawFirst) {
                                firstLawImg = liberalLawImg;
                                secondLawImg = fascistLawImg;
                            } else {
                                firstLawImg = fascistLawImg;
                                secondLawImg = liberalLawImg;
                            }
                        }
                        if (!firstBtnImgSet) {
                            presidentFirstImgBtn.setImageResource(firstLawImg);
                            presidentSecondImgBtn.setImageResource(secondLawImg);
                            firstBtnImgSet = true;
                            secondBtnImgSet = true;
                            remainingLawsRef.child("Liberal").setValue(discardedLiberalLaws - liberalLawsActive);
                            discardedLawsRef.child("Liberal").setValue(0);
                            remainingLawsRef.child("Fascist").setValue(discardedFascistLaws - fascistLawsActive);
                            discardedLawsRef.child("Fascist").setValue(0);
                        } else if (!secondBtnImgSet) {
                            presidentSecondImgBtn.setImageResource(firstLawImg);
                            presidentThirdImgBtn.setImageResource(secondLawImg);
                            secondBtnImgSet = true;
                            thirdBtnImgSet = true;
                            remainingLawsRef.child("Liberal").setValue(discardedLiberalLaws - liberalLawsActive);
                            discardedLawsRef.child("Liberal").setValue(0);
                            remainingLawsRef.child("Fascist").setValue(discardedFascistLaws - fascistLawsActive);
                            discardedLawsRef.child("Fascist").setValue(0);
                        } else {
                            presidentThirdImgBtn.setImageResource(firstLawImg);
                            thirdBtnImgSet = true;
                            if (liberalLawFirst) {
                                remainingLawsRef.child("Liberal").setValue(remainingLiberalLaws - 1);
                            } else {
                                remainingLawsRef.child("Fascist").setValue(remainingFascistLaws - 1);
                            }
                        }
                    } else {
                        List<String> drawnLaws = helper.DrawLaws(1, remainingLiberalLaws, remainingFascistLaws);

                        int thisLawImg;
                        if (drawnLaws.get(0).equals("Liberal")) {
                            thisLawImg = liberalLawImg;
                        } else {
                            thisLawImg = fascistLawImg;
                        }
                        if (!firstBtnImgSet) {
                            presidentFirstImgBtn.setImageResource(thisLawImg);
                            firstBtnImgSet = true;
                        } else if (!secondBtnImgSet) {
                            presidentSecondImgBtn.setImageResource(thisLawImg);
                            secondBtnImgSet = true;
                        } else {
                            presidentThirdImgBtn.setImageResource(thisLawImg);
                            thirdBtnImgSet = true;
                        }
                        if (thisLawImg == liberalLawImg) {
                            remainingLawsRef.child("Liberal").setValue(remainingLiberalLaws - 1);
                        } else {
                            remainingLawsRef.child("Fascist").setValue(remainingFascistLaws - 1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        remainingLawsRef.addValueEventListener(remainingLawCountListener);

        presidentFirstImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presidentSecondImgBtn.setImageResource(fascistLawImg);
            }
        });
    }
}
