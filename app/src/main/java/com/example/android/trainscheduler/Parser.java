package com.example.android.trainscheduler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by toshiba pc on 3/28/2018.
 */

public class Parser {
    public ArrayList<Kereta> parseXML() {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream input = LoadingActivity.getInstance().getAssets().open("data.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(input, null);

            return processParsing(parser);
        } catch (XmlPullParserException e) {
        } catch (IOException e) {
        }
        return null;
    }

    public ArrayList<Kereta> processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<Kereta> keretas = new ArrayList<>();
        ArrayList<Jadwal> jadwals = new ArrayList<>();
        int eventType = parser.getEventType();
        Kereta cKereta = null;
        Jadwal cJadwal = null;
        Stasiun cStasiun = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tokenName = null;
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    tokenName = parser.getName();
                    if ("kereta".equals(tokenName)) {
                        cKereta = new Kereta();
                        keretas.add(cKereta);
                    } else if (cKereta != null) {
                        if ("namaK".equals(tokenName)) {
                            cKereta.setNamaKereta(parser.nextText());
                        } else if ("jadwals".equals(tokenName)) {
                            jadwals = new ArrayList<>();
                            cKereta.setJadwals(jadwals);
                        }
                        if ("jadwal".equals(tokenName)) {
                            cJadwal = new Jadwal();
                        }

                        if (cJadwal != null) {
                            if ("datang".equals(tokenName)) {
                                cJadwal.setJamDatang(parser.nextText());
                            } else if ("pergi".equals(tokenName)) {
                                cJadwal.setJamPergi(parser.nextText());
                            }
                        }

                        if ("stasiun".equals(tokenName)) {
                            cStasiun = new Stasiun();
                        }

                        if (cStasiun != null) {
                            if ("namaS".equals(tokenName)) {
                                cStasiun.setNamaStasiun(parser.nextText());
                            } else if ("latitude".equals(tokenName)) {
                                cStasiun.setLatitude(Double.parseDouble(parser.nextText()));
                            } else if ("longitude".equals(tokenName)) {
                                cStasiun.setLongtitude(Double.parseDouble(parser.nextText()));
                                cJadwal.setStasiun(cStasiun);
                                jadwals.add(cJadwal);
                            }
                        }

                    }
                    break;
            }
            eventType = parser.next();
        }
        return keretas;
//        for(Kereta k : keretas) {
//            printKereta(k);
//        }
    }
//    public void printKereta(Kereta kereta){
//        String text = "";
//        text = text + kereta.getNamaKereta();
//        String jadwals = "";
//        for(Jadwal j : kereta.getJadwals()){
//            Stasiun currentStasiun = j.getStasiun();
//            jadwals += currentStasiun.getNamaStasiun()+" "+currentStasiun.getLatitude()+" "
//                    +currentStasiun.getLongtitude()+" "+j.getJamDatang()+" "+j.getJamPergi()+"\n";
//        }
//        text = text +"||"+jadwals;
//        Log.d("PRINTKERETA",text);
//    }
}
