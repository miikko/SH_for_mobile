package com.example.secret_hitler;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RoleHandler {

    public DBHandler dbHandler;
    Random rand;


    public void AssignRole(Context appContext, Player thisPlayer) {
        dbHandler = DBHandler.getInstance(appContext);
        int playerCount = dbHandler.GetPlayerCount();
        String role;

        List<String> playerRoles = new ArrayList<>();

        for (int i = 1; i < playerCount; i++) {
            String thisPlayerRole = dbHandler.GetRole(i);
            if (thisPlayerRole != null) {
                playerRoles.add(thisPlayerRole);
            }
        }

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

        //If the game is ready to start and no Hitler has been chosen, this statement makes it so the last player is Hitler.
        //Otherwise the game could start without a Hitler
        if (canBeHitler && playerCount == 5) {
            role = "Hitler";
        }

        dbHandler.SetRole(thisPlayer.id, role);
        thisPlayer.SetRole(role);
    }

    public void AssignFirstPresidency(Context appContext) {
        dbHandler = DBHandler.getInstance(appContext);
        int playerCount = dbHandler.GetPlayerCount();
        int firstPresidentID = 4;/*rand.nextInt(playerCount);*/
        dbHandler.SetAsPresident(firstPresidentID);
    }
}
