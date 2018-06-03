package com.example.secret_hitler;

import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Parcelable{

    int id;
    String role;
    String name;
    boolean isPresident;
    boolean isChancellor;
    boolean isAlive;
    int parameters = 6;

    public Player (int id, String role, String name, boolean isPresident, boolean isChancellor, boolean isAlive){
        this.id = id;
        this.role = role;
        this.name = name;
        this.isPresident = isPresident;
        this.isChancellor = isChancellor;
        this.isAlive = isAlive;
    }

    public void SetID(int id) {
        this.id = id;
    }

    public String GetRole() {
        return this.role;
    }

    public void SetRole(String newRole) {
        this.role = newRole;
    }

    public void SetAsPresident () {
        this.isPresident = true;
    }

    public Player (Parcel in) {
        //!!Change size when adding new parameters to Player class!!
        String[] data = new String[parameters];

        in.readStringArray(data);
        this.id = Integer.parseInt(data[0]);
        this.role = data[1];
        this.name = data[2];
        this.isPresident = Boolean.valueOf(data[3]);
        this.isChancellor = Boolean.valueOf(data[4]);
        this.isAlive = Boolean.valueOf(data[5]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeStringArray(new String[]{String.valueOf(this.id), this.role, this.name, String.valueOf(this.isPresident), String.valueOf(this.isChancellor), String.valueOf(this.isAlive)});
    }

    public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel source) {
            return new Player(source); //using parcelable constructor
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };
}
