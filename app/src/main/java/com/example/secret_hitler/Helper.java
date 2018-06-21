package com.example.secret_hitler;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Helper {
    Random rand = new Random();


    public String AssignRole(int playerCount, ArrayList<String> playerRoles) {
        String role;

        int hitlerCount = 0;
        int fascistCount = 0;
        int liberalCount = 0;
        for (int i = 0; i < playerRoles.size(); i++) {

            if (playerRoles.get(i).equals("Hitler")) {
                hitlerCount++;
            } else if (playerRoles.get(i).equals("Fascist")) {
                fascistCount++;
            } else if (playerRoles.get(i).equals("Liberal")) {
                liberalCount++;
            }
        }

        int hitlerMax = 1;
        int fascistMax = 0;
        int liberalMax = 0;

        switch (playerCount) {
            case 5:
                fascistMax = 1;
                liberalMax = 3;
                break;
            case 6:
                fascistMax = 1;
                liberalMax = 4;
                break;
            case 7:
                fascistMax = 2;
                liberalMax = 4;
                break;
            case 8:
                fascistMax = 2;
                liberalMax = 5;
                break;
            case 9:
                fascistMax = 3;
                liberalMax = 5;
                break;
            case 10:
                fascistMax = 3;
                liberalMax = 6;
                break;
            default:
                break;
        }

        boolean canBeHitler = false;
        boolean canBeFascist = false;
        boolean canBeLiberal = false;

        if (hitlerCount < hitlerMax) {
            canBeHitler = true;
        }
        if (fascistCount < fascistMax) {
            canBeFascist = true;
        }
        if (liberalCount < liberalMax) {
            canBeLiberal = true;
        }

        double roleLottery = Math.random() * 100;

        if (canBeHitler && canBeFascist && canBeLiberal) {
            if (roleLottery < 33) {
                role = "Hitler";
            } else if (roleLottery < 66) {
                role = "Fascist";
            } else {
                role = "Liberal";
            }
        } else if (canBeHitler && canBeFascist) {
            if (roleLottery < 50) {
                role = "Hitler";
            } else {
                role = "Fascist";
            }
        } else if (canBeHitler && canBeLiberal) {
            if (roleLottery < 50) {
                role = "Hitler";
            } else {
                role = "Liberal";
            }
        } else if (canBeFascist && canBeLiberal) {
            if (roleLottery < 50) {
                role = "Fascist";
            } else {
                role = "Liberal";
            }
        } else if (canBeHitler) {
            role = "Hitler";
        } else if (canBeFascist) {
            role = "Fascist";
        } else {
            role = "Liberal";
        }

        List<String> actualPlayerRoles = new ArrayList<>();
        for (String item : playerRoles) {
            if (!item.equals("unknown")) {
                actualPlayerRoles.add(item);
            }
        }
        if (actualPlayerRoles.size() == 4 && canBeHitler) {
            role = "Hitler";
        }

        return role;
    }

    public int AssignFirstPresidency(int playerCount) {
        int firstPresidentID = 4;/*rand.nextInt(playerCount);*/
        return firstPresidentID;
    }

    //This method should not be called if there are less than 3 total laws remaining.
    //Database needs to be updated accordingly after calling this method.
    public List<String> DrawLaws(int howMany, int liberalLawsRemaining, int fascistLawsRemaining) {
        int tempLiberalLawsRemaining = liberalLawsRemaining;
        int tempFascistLawsRemaining = fascistLawsRemaining;
        List<String> drawnLaws = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            int totalNumberOfLawsRemaining = tempLiberalLawsRemaining + tempFascistLawsRemaining;
            int selectedLawNumber = rand.nextInt(totalNumberOfLawsRemaining);
            if (selectedLawNumber < tempLiberalLawsRemaining) {
                drawnLaws.add("Liberal");
                tempLiberalLawsRemaining--;
            } else {
                drawnLaws.add("Fascist");
                tempFascistLawsRemaining--;
            }
        }
        return drawnLaws;
    }

    public List<String> ShuffleLawsToDrawPile(int liberalLawsToBeShuffled, int fascistLawsToBeShuffled) {
        List<String> lawDeck = new ArrayList<>();
        int tempLiberalLaws = liberalLawsToBeShuffled;
        int tempFascistLaws = fascistLawsToBeShuffled;
        int totalNumberOfLawsToBeShuffled = tempLiberalLaws + tempFascistLaws;
        for (int i = 0; i < totalNumberOfLawsToBeShuffled; i++) {
            int thisLawOrderNumber = rand.nextInt((tempLiberalLaws + tempFascistLaws));
            if (thisLawOrderNumber < tempLiberalLaws) {
                lawDeck.add("Liberal");
                tempLiberalLaws--;
            } else {
                lawDeck.add("Fascist");
                tempFascistLaws--;
            }
        }
        return lawDeck;
    }
}
