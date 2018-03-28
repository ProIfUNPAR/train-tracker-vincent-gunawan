/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csvparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author toshiba pc
 */
public class CSVParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here
        BufferedReader reader = new BufferedReader(new FileReader("semua2.csv"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"));
        String text = "";
        String hasil = "";
        String nk = "", ns, lat, longi, jDatang, jPergi;
        while ((text = reader.readLine()) != null) {
            String[] token = text.split(",");
            if (nk.equals("") || !nk.equalsIgnoreCase(token[0])) {
                nk = token[0].trim();
                if (!hasil.equals("")) {
                    hasil += "</jadwals>\n</kereta>";
                }
                hasil += "<kereta>\n<namaK>" + nk + "</namaK>\n<jadwals>\n";
            } else {
            }
            ns = token[1].trim().toLowerCase();
            lat = token[2].trim();
            longi = token[3].trim();
            jDatang = token[4].trim();
            if (jDatang.equals("–")) {
                jDatang = "X";
            }
            jPergi = token[5].trim();
            if (jPergi.equals("–")) {
                jPergi = "X";
            }
            if (!jDatang.equalsIgnoreCase("X")) {
                if (jDatang.length() > 2) {
                    String[] jdSplit = jDatang.split("\\.");
                    if (jdSplit[0].length() == 1) {
                        jdSplit[0] = "0" + jdSplit[0];
                    }
                    if (jdSplit[1].length() == 1) {
                        jdSplit[1] = jdSplit[1] + "0";
                    }
                    jDatang = jdSplit[0] + ":" + jdSplit[1];
                }
            }
            if (!jPergi.equalsIgnoreCase("X")) {
                if (jPergi.length() > 2) {
                    String[] jpSplit = jPergi.split("\\.");
                    if (jpSplit[0].length() == 1) {
                        jpSplit[0] = "0" + jpSplit[0];
                    }
                    if (jpSplit[1].length() == 1) {
                        jpSplit[1] = jpSplit[1] + "0";
                    }
                    jPergi = jpSplit[0] + ":" + jpSplit[1];
                } else {
                    if (jPergi.length() == 1) {
                        if (jPergi.equalsIgnoreCase("-")) {
                            jPergi = jPergi;
                        } else {
                            jPergi = "0" + jPergi + ":00";
                        }
                    } else {
                        jPergi += ":00";
                    }
                }
            }
            hasil += "<jadwal>\n"
                    + "<datang>" + jDatang + "</datang>\n"
                    + "<pergi>" + jPergi + "</pergi>\n"
                    + "<stasiun>\n"
                    + "<namaS>" + ns + "</namaS>\n"
                    + "<latitude>" + lat + "</latitude>\n"
                    + "<longitude>" + longi + "</longitude>\n"
                    + "</stasiun>\n"
                    + "</jadwal>\n";
        }
        hasil += "</jadwals>\n</kereta>";
        System.out.print(hasil);
        writer.write(hasil);
    }

}
