package com.example.android.trainscheduler;


public class MainPresenter {
    public static final double R= 6371;//in KM

    public MainPresenter(){}

    public double getJarak(double latAwal, double latAkhir, double longAwal, double longAkhir){
        double dLong = Math.toRadians(longAkhir - longAwal);
        double dLat = Math.toRadians(latAkhir - latAwal);
        double a = Math.sin(dLat/2)*Math.sin(dLat /2) + (Math.cos(latAwal)*Math.cos(latAkhir))*(Math.sin(dLong /2)*Math.sin(dLong /2));
        double c = 2 * (Math.atan2(Math.sqrt(a),Math.sqrt(1-a)));
        return MainPresenter.R * c;

    }

    public String formatWaktu(int jam, int menit,int detik){
        String sJam = (jam < 10)? "0"+jam : jam+"";
        String sMenit = (menit < 10)? "0"+menit : (""+menit).substring(0,2);
        String sDetik = (detik < 10)? "0"+detik : (""+detik).substring(0,2);
        return sJam+":"+sMenit+":"+sDetik;
    }

    public int[] hitungWaktu(double jarak,double kecepatan){
        int[] array = new int[3];
        if (kecepatan == 0){
            kecepatan = MenuActivity.KECEPATAN_DEFAULT;
        }
        double temp = (jarak/kecepatan);
        int jam = 0;
        jam = (int) Math.floor(temp);
        int menit = (int) ((temp % 1) * 60);
        int detik = (int)((((temp % 1) * 60)%1)*60);

        array[0] = jam;
        array[1] = menit;
        array[2] = detik;
        return array;
    }
}
