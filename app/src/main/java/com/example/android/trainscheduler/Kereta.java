package com.example.android.trainscheduler;

import java.util.ArrayList;

/**
 * Created by toshiba pc on 2/9/2018.
 */

public class Kereta {
    private String namaKereta;
    private ArrayList<Jadwal> jadwals;

    public Kereta(String namaKereta, ArrayList<Jadwal> jadwals) {
        this.namaKereta = namaKereta;
        this.jadwals = jadwals;
    }

    public String getNamaKereta() {
        return namaKereta;
    }

    public void setNamaKereta(String namaKereta) {
        this.namaKereta = namaKereta;
    }

    public ArrayList<Jadwal> getJadwals() {
        return jadwals;
    }

    public void setJadwals(ArrayList<Jadwal> jadwals) {
        this.jadwals = jadwals;
    }

    public void addJadwal(Jadwal jadwal){
        this.jadwals.add(jadwal);
    }
}
