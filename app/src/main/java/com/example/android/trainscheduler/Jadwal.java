package com.example.android.trainscheduler;

/**
 * Created by toshiba pc on 2/9/2018.
 */

public class Jadwal {
    private Stasiun stasiun;
    private String jamDatang;
    private String jamPergi;

    public Jadwal(Stasiun stasiun, String jamDatang, String jamPergi) {
        this.stasiun = stasiun;
        this.jamDatang = jamDatang;
        this.jamPergi = jamPergi;
    }

    public Stasiun getStasiun() {
        return stasiun;
    }

    public void setStasiun(Stasiun stasiun) {
        this.stasiun = stasiun;
    }

    public String getJamDatang() {
        return jamDatang;
    }

    public void setJamDatang(String jamDatang) {
        this.jamDatang = jamDatang;
    }

    public String getJamPergi() {
        return jamPergi;
    }

    public void setJamPergi(String jamPergi) {
        this.jamPergi = jamPergi;
    }
}
