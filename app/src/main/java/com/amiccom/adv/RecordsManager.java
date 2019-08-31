/*
 Register mail number.
 search mail
 delete mail
 */

package com.amiccom.adv;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;


import static android.content.ContentValues.TAG;
//import static com.amiccom.adv.AdvertiserFragment.TAG;


public class RecordsManager {

    static final int MAX_RECORD = 500;
    static final int MAC_LENGTH = 12;
    static final int MAX_NUMBER_LENGTH = 16;
    static final int MIN_NUMBER_LENGTH = 8;
    static final int HEADER_LENGTH = 2;
    static final int RECORD_LENGTH = 30;

    static String[] mailData = new String[MAX_RECORD];
    static final String mac01 = "187A93000F12";
    static final String mac02 = "187A93010F12";
    static final String mailNumber01 = "880376835683";
    static final String mailNumber02 = "880376867700";

    public void RecordsManager(){
        init_mail_data();
    }


    public static void init_mail_data() {
        short i;

        for(i=0;i<MAX_RECORD;i++) {
            //mailData[i] = "0000000000" + "0000000000" + "0000000000";
            DeleteRecord(i);
        }

        //Assume two records was registered
//        mailData[0] = "0C" + mac01 + mailNumber01 + "0000";  //record valid, number length 12
//        mailData[1] = "0C" + mac02 + mailNumber02 + "0000";
    }

    public static short match_number(String s) {
        short i;
        String ss1;
        String ss2;
        int temp_length;
        for(i=0; i<MAX_RECORD; i++){
            ss1 = mailData[i].substring(0,HEADER_LENGTH);
//            Log.i(TAG, "message header length: "+ ss1.length());
            if(!ss1.equals("00")){
                temp_length = ((Character.digit(ss1.charAt(0), 16) << 4) + Character.digit(ss1.charAt(1), 16));
//                Log.i(TAG, "message header string is: "+ ss1);
//                Log.i(TAG, "message header byte[0] is: "+ temp_byte);
                int length1 = s.length();
                if(temp_length == length1) {
                    ss2 = mailData[i].substring(14, length1 + 14);
//                  Log.i(TAG, "storeed string is: "+ ss2);
//                  Log.i(TAG, "input string is: "+ s);
                    if (ss2.equals(s)) {
//                    Log.i(TAG, "record location is: "+ i);
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public static String get_mac(short pte){
        String ss;
        ss = mailData[pte].substring(HEADER_LENGTH, 14);

        return ss;
    }

    public static short HaveValid(){
        short i;
        String ss1;

        for(i=0;i<MAX_RECORD;i++){
            ss1 = mailData[i].substring(0,HEADER_LENGTH);
            if(ss1.equals("00")){
                return i;
            }
        }
        return -1;
    }

    public static void WriteRecord(short location, String s, String m){
        short i;
        int length = s.length();
        int length1 = m.length();
        if((length >= MIN_NUMBER_LENGTH) && (length <= MAX_NUMBER_LENGTH) && (length1 == MAC_LENGTH)) {

            String header = Integer.toHexString(length);

            if((header.length() >= 1) && (header.length() <= 2)){

                if (header.length() == 1) {
                    header = "0" + header;
                }

                Log.i(TAG, "header is: " + header);

                String tempRecord = header + m + s;
                length = tempRecord.length();

                if (length < RECORD_LENGTH) {
                    for (i = 0; i < (RECORD_LENGTH - length); i++)
                        tempRecord = tempRecord + "0";
                }

                Log.i(TAG, "record is: " + tempRecord);
                mailData[location] = tempRecord;
            } else {
                Log.i(TAG, "record header error!");
            }
        }
    }

    public static void DeleteRecord(short location){
        mailData[location] = "0000000000" + "0000000000" + "0000000000";
    }

    private static void writeSDcard(String str) {
        try {
            // check for SD card
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                // get SD card path
                File sdDire = Environment.getExternalStorageDirectory();
                FileOutputStream outFileStream = new FileOutputStream(
                        sdDire.getCanonicalPath() + "/test.txt");
                outFileStream.write(str.getBytes(), 0, RECORD_LENGTH);
                outFileStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeSDcardA(String[] str, int length) {
        short i;
        try {
            // check for SD card
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                // get SD card path
                File sdDire = Environment.getExternalStorageDirectory();
                // File sdDire = getFilesDir();
                FileOutputStream outFileStream = new FileOutputStream(
                        sdDire.getCanonicalPath() + "/test.txt", true);
//                FileOutputStream outFileStream = new FileOutputStream("/test.txt", true);
                for(i=1;i<length;i++) {
                    outFileStream.write(str[i].getBytes(), 0, RECORD_LENGTH);
                }
                outFileStream.close();
//                Toast.makeText(this, "data saved to text.txt", Toast.LENGTH_LONG)
//                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readSDcard() {
        StringBuffer strsBuffer = new StringBuffer();

        try {
            // 判断是否存在SD
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {

                File sdDire = Environment.getExternalStorageDirectory();
                File file = new File(sdDire.getCanonicalPath() + "/test.txt");

                if (file.exists()) {
                    //open file input stream
                    FileInputStream fileR = new FileInputStream(file);
                    BufferedReader reads = new BufferedReader(
                            new InputStreamReader(fileR));
                    String st = null;
                    while ((st = reads.readLine()) != null) {
                        strsBuffer.append(st);
                    }
                    fileR.close();

                    return strsBuffer.toString();
                } else {
                    return "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    public static boolean loadRecordsData(){
        short i;

        String allData = readSDcard();

        if(allData.length() == MAX_RECORD*RECORD_LENGTH) {
            for (i = 0; i < MAX_RECORD ; i++) {
                mailData[i] = allData.substring(i * RECORD_LENGTH, (i + 1) * RECORD_LENGTH);
            }
            return true;
        }
        return false;
    }

    public static boolean saveRecordsData(){
        short i;

        writeSDcard(mailData[0]);
        writeSDcardA(mailData, MAX_RECORD);

        return true;
    }
}

