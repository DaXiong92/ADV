/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amiccom.adv;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.le.AdvertiseCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

//import com.yzq.zxinglibrary.android.CaptureActivity;
//import com.yzq.zxinglibrary.bean.ZxingConfig;
//import com.yzq.zxinglibrary.common.Constant;

//import static android.app.Activity.RESULT_OK;

import com.google.zxing.integration.android.IntentIntegrator;
//import com.google.zxing.integration.android.IntentResult;
//import com.journeyapps.barcodescanner.CaptureActivity;

//import java.io.File;
//import java.io.FileOutputStream;

//import static android.app.Activity.RESULT_OK;
//import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

/**
 * Allows user to start & stop Bluetooth LE Advertising of their device.
 */
public class AdvertiserFragment extends Fragment{

    public final static String TAG = AdvertiserFragment.class.getSimpleName();

    private View view;

    public static RecordsManager recordsManager;
    /**
     * Lets user toggle BLE Advertising.
     */
    public static EditText etAds1;
    public EditText etAds2;
    public static EditText etAds3;

    public static Button btnClearLine1;
    public static Button btnClearLine2;
    public static Button btnClearLine3;
    public static boolean clearLineFlag = false;

    public static TextView tvCurrentADV;
    private TextView tvCurrentADVText;
    private TextView mLanguage;

    public static TextView myVersion;

    private Button btnStartAdv;
    private Button btnStopAdv;

    private RadioGroup radioGroup;
    private RadioButton registerRadioButton;
    public static RadioButton finderRadioButton;

    public static RadioGroup radioGroup1;
    private RadioButton sourchRadioButton;
    private RadioButton updateRadioButton;
    private RadioButton offRadioButton;

    //private Switch cw;

    private boolean findRadioFlag = true;

//    private Button btnScanMail;
//    private Button btnScanMAC;
    private Button btnDelete;

    private static String adsText = "No message.";
    private boolean advStarted = false;
    public static boolean productionMode = false;
    public static boolean shortTime = false;
    private short delNum = -1;
/*
    String[] mailData = new String[50];
    static final String mac01 = "187A93000F12";
    static final String mac02 = "187A93010F12";
    static final String mailNumber01 = "880376835683";
    static final String mailNumber02 = "880376867700";
*/
    public static int myRequestCode = 0;
    public static final int DEVICE_ID_SCAN = 0x12;
    public static final int REFERENCE_SCAN = 0x22;
    private int CAMERA_JAVA_REQUEST_CODE = 12;

    private static int oldVersion = 0;

    String[] single_list = {"English", "中文"};

    /**
     * Listens for notifications that the {@code AdvertiserService} has failed to start advertising.
     * This Receiver deals with Fragment UI elements and only needs to be active when the Fragment
     * is on-screen, so it's defined and registered in code instead of the Manifest.
     */
    private BroadcastReceiver advertisingFailureReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        short i;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_JAVA_REQUEST_CODE);
        }

        advertisingFailureReceiver = new BroadcastReceiver() {

            /**
             * Receives Advertising error codes from {@code AdvertiserService} and displays error messages
             * to the user. Sets the advertising toggle to 'false.'
             */
            @Override
            public void onReceive(Context context, Intent intent) {

                int errorCode = intent.getIntExtra(AdvertiserService.ADVERTISING_FAILED_EXTRA_CODE, -1);

                String errorMessage = getString(R.string.start_error_prefix);
                switch (errorCode) {
                    case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                        errorMessage += " " + getString(R.string.start_error_already_started);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                        errorMessage += " " + getString(R.string.start_error_too_large);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        errorMessage += " " + getString(R.string.start_error_unsupported);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                        errorMessage += " " + getString(R.string.start_error_internal);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        errorMessage += " " + getString(R.string.start_error_too_many);
                        break;
                    case AdvertiserService.ADVERTISING_TIMED_OUT:
                        if(MainActivity.language == MainActivity.Chinese){
                            errorMessage = " " + getString(R.string.advertising_timedout1);
                        }else {
                            errorMessage = " " + getString(R.string.advertising_timedout);
                        }
                        advStarted = false;
                        btnStopAdv.setEnabled(true);
                        btnStartAdv.setEnabled(true);
                        adsText = "";
                        tvCurrentADV.setText("");
                        registerRadioButton.setEnabled(true);
                        finderRadioButton.setEnabled(true);
                        if(MainActivity.language == MainActivity.Chinese) {
                            btnStartAdv.setBackgroundResource(R.drawable.start_cn_1);
                            btnStopAdv.setBackgroundResource(R.drawable.stop_cn_1);
                            registerRadioButton.setBackgroundResource(R.drawable.reg_cn_1);
                            finderRadioButton.setBackgroundResource(R.drawable.sea_cn_1);
                        }else{
                            btnStartAdv.setBackgroundResource(R.drawable.start_en_1);
                            btnStopAdv.setBackgroundResource(R.drawable.stop_en_1);
                            registerRadioButton.setBackgroundResource(R.drawable.reg_en_1);
                            finderRadioButton.setBackgroundResource(R.drawable.sea_en_1);
                        }
                        break;
                    default:
                        errorMessage += " " + getString(R.string.start_error_unknown);
                }

                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            }
        };

        //init_mail_data();
        recordsManager = new RecordsManager();

        if(recordsManager.loadRecordsData() == false) {
            recordsManager.init_mail_data();
            Log.i(TAG, "data file retrieve fail!");
            for(i=0;i<recordsManager.MAX_RECORD-1;i++) {
                Log.i(TAG, recordsManager.mailData[i]);
            }
        } else{
            Log.i(TAG, "data file retrieve success!");
            for(i=0;i<recordsManager.MAX_RECORD;i++) {
                Log.i(TAG, recordsManager.mailData[i]);
            }
        }
        Log.i(TAG, "On Create execute finished!");
    }
/*
    class RadioGroupListener implements RadioGroup.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId==registerRadioButton.getId()){
                System.out.println("Register set!");
                findRadioFlag = false;
            }else if (checkedId==finderRadioButton.getId()){
                System.out.println("Finder set!");
                findRadioFlag = true;
            }
        }
    }
*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        if(MainActivity.language == MainActivity.English){
            view = inflater.inflate(R.layout.fragment_advertiser_en, container, false);}
        else{
            view = inflater.inflate(R.layout.fragment_advertiser, container, false);}

        etAds1 = (EditText) view.findViewById(R.id.inputAds1);
        etAds2 = (EditText) view.findViewById(R.id.inputAds2);
        etAds3 = (EditText) view.findViewById(R.id.inputAds3);

        //cw = (Switch) view.findViewById(R.id.switch3);

        btnClearLine1 = (Button) view.findViewById(R.id.clear_line1);
        btnClearLine2 = (Button) view.findViewById(R.id.clear_line2);
        btnClearLine3 = (Button) view.findViewById(R.id.clear_line3);
        btnClearLine1.setBackgroundResource(R.drawable.button1);
        btnClearLine2.setBackgroundResource((R.drawable.button2_1));
        btnClearLine3.setBackgroundResource(R.drawable.button3_1);

        btnStartAdv = (Button) view.findViewById(R.id.start_adv);
        btnStopAdv = (Button) view.findViewById(R.id.stop_adv);

        mLanguage = (TextView) view.findViewById(R.id.languageText);

        if(MainActivity.language == MainActivity.Chinese) {
            btnStartAdv.setBackgroundResource(R.drawable.start_cn_1);
            btnStopAdv.setBackgroundResource(R.drawable.stop_cn);
            //mLanguage.setText("语言");
        }else{
            btnStartAdv.setBackgroundResource(R.drawable.start_en_1);
            btnStopAdv.setBackgroundResource(R.drawable.stop_en);
            //mLanguage.setText("Language");
        }


        tvCurrentADV = (TextView) view.findViewById(R.id.current_adv);

        radioGroup = (RadioGroup) view.findViewById(R.id.myradiogroup);
        registerRadioButton = (RadioButton) view.findViewById(R.id.radioButton1);
        finderRadioButton = (RadioButton) view.findViewById(R.id.radioButton2);

        radioGroup1 = (RadioGroup) view.findViewById(R.id.myRadioGroup1);
        sourchRadioButton = (RadioButton) view.findViewById(R.id.radioButton3);
        updateRadioButton = (RadioButton) view.findViewById(R.id.radioButton4);
        offRadioButton = (RadioButton) view.findViewById(R.id.radioButton5);

        btnDelete = (Button) view.findViewById(R.id.deleteBtn);
        if(MainActivity.language == MainActivity.Chinese) {
            btnDelete.setBackgroundResource(R.drawable.del_cn_1);
            registerRadioButton.setBackgroundResource(R.drawable.reg_cn_1);
            finderRadioButton.setBackgroundResource(R.drawable.sea_cn_1);
        }else{
            btnDelete.setBackgroundResource(R.drawable.del_en_1);
            registerRadioButton.setBackgroundResource(R.drawable.reg_en_1);
            finderRadioButton.setBackgroundResource(R.drawable.sea_en_1);
        }

        tvCurrentADVText=(TextView) view.findViewById(R.id.current_adv_text);
        if(MainActivity.language == MainActivity.Chinese){
            tvCurrentADVText.setBackgroundResource(R.drawable.status_above_cn);
        }else{
            tvCurrentADVText.setBackgroundResource(R.drawable.status_above);
        }

        myVersion=(TextView) view.findViewById(R.id.version_display);

        if(oldVersion == 0){
            myVersion.setText(getResources().getString(R.string.app_version));
        }else{
            myVersion.setText(getResources().getString(R.string.app_version_old));
        }

        btnStopAdv.setEnabled(false);
        radioGroup1.setVisibility(View.INVISIBLE);

        registerAfterMacTextChangedCallback(etAds1, etAds2, 12);
        //registerAfterMacTextChangedCallback(etAds2, etAds3, 16);
        registerAfterMacTextChangedCallback(etAds2, null, 16);
        //registerAfterMacTextChangedCallback(etAds3, null, 16);

        etAds1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearLineFlag = true;
                etAds1.setText("");
                clearLineFlag = false;
            }
        });

        etAds3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearLineFlag = true;
                etAds3.setText("");
                clearLineFlag = false;
            }
        });


        etAds2.setText("01,03,");
        etAds2.setEnabled(false);

        btnClearLine1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLineFlag = true;
                etAds1.setText("");
                clearLineFlag = false;
               // Intent intent1 = new Intent(getActivity(), CaptureActivity.class);
               // ZxingConfig config = new ZxingConfig();
               // config.setShowbottomLayout(true);//底部布局（包括闪光灯和相册）
                //config.setPlayBeep(true);//是否播放提示音
               // config.setShake(true);//是否震动
               // config.setShowAlbum(true);//是否显示相册
               // config.setShowFlashLight(true);//是否显示闪光灯
                //intent1.putExtra(Constant.INTENT_ZXING_CONFIG, config);

               // startActivityForResult(intent1, REQUEST_MAC_CODE_SCAN);

                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setCaptureActivity(myScanActive.class);
                integrator.setPrompt("请扫描ID码"); //底部的提示文字，设为""可以置空
                integrator.setCameraId(0); //前置或者后置摄像头
                integrator.setBeepEnabled(true); //扫描成功的「哔哔」声，默认开启
                integrator.setBarcodeImageEnabled(true);
                myRequestCode = DEVICE_ID_SCAN;
                integrator.initiateScan();
            }
        });

        //search MAC vis mail number
        btnClearLine2.setOnClickListener(new View.OnClickListener() {
            short i;
            short ptr;
            String ss1;
            @Override
            public void onClick(View v) {
                if (finderRadioButton.isChecked())
                {
                    //Finder process
                    //clearLineFlag = false;
                    //etAds2.setText("");
                    if ((etAds3.getText().toString().length() >= recordsManager.MIN_NUMBER_LENGTH)
                            && (etAds3.getText().toString().length() <= recordsManager.MAX_NUMBER_LENGTH)) {
                        ptr = recordsManager.match_number(etAds3.getText().toString());
                        Log.i(TAG, "found location is: "+ ptr);
                        if (ptr != -1) {
                            ss1 = recordsManager.get_mac(ptr);
                            etAds1.setText(ss1);
                        } else {
                            //etAds1.setText("FFFFFFFFFFFF");
                            if(MainActivity.language == MainActivity.Chinese)
                                tvCurrentADV.setText("滞留仓库里没有该快递件！");
                            else
                                tvCurrentADV.setText("There is no mail in the warehouse!");
                        }
                    } else {
                        //etAds1.setText("000000000000");
                        if(MainActivity.language == MainActivity.Chinese)
                            tvCurrentADV.setText("输入的运单号不正确！");
                        else
                            tvCurrentADV.setText("input a incorrect number!");
                    }
                    //clearLineFlag = false;
                }
                else
                {
                    //Register process
                    if ((etAds3.getText().length() != 0) && (etAds1.getText().length() != 0)){
                        Log.i(TAG, "Mail number length is: "+ etAds3.getText().length());
                        Log.i(TAG, "MAC address length is: "+ etAds1.getText().toString().replaceAll(":","").length());
                        ptr = recordsManager.HaveValid();
                        Log.i(TAG, "Valid location is: "+ ptr);
                        if (ptr != -1) {
                            recordsManager.WriteRecord(ptr, etAds3.getText().toString(), etAds1.getText().toString().replaceAll(":",""));
                            Log.i(TAG, "Register successful!");
                            if(MainActivity.language == MainActivity.Chinese)
                                tvCurrentADV.setText("快递件登记成功！");
                            else
                                tvCurrentADV.setText("Register success!");

                            recordsManager.saveRecordsData();
                            for(i=0;i<recordsManager.MAX_RECORD-1;i++) {
                                Log.i(TAG, recordsManager.mailData[i]);
                            }
                        } else {
                            Log.i(TAG, "Register fail!");
                            if(MainActivity.language == MainActivity.Chinese)
                                tvCurrentADV.setText("快递件登记失败！");
                            else
                                tvCurrentADV.setText("Register fail!");
                        }
                    } else {
                        Log.i(TAG, "数据错误，请检查单号或设备编号!");
                    }
                }
            }
        });

        btnClearLine3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLineFlag = true;
                etAds3.setText("");
                //etAds1.setText("");
                clearLineFlag = false;
                //Intent intent1 = new Intent(getActivity(), CaptureActivity.class);
                //ZxingConfig config = new ZxingConfig();
               // config.setShowbottomLayout(true);//底部布局（包括闪光灯和相册）
                //config.setPlayBeep(true);//是否播放提示音
                //config.setShake(true);//是否震动
                //config.setShowAlbum(true);//是否显示相册
                //config.setShowFlashLight(true);//是否显示闪光灯
                //intent1.putExtra(Constant.INTENT_ZXING_CONFIG, config);

                //startActivityForResult(intent1, REQUEST_MAIL_CODE_SCAN);

                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setCaptureActivity(myScanActive.class);
                integrator.setPrompt("请扫描单号"); //底部的提示文字，设为""可以置空
                integrator.setCameraId(0); //前置或者后置摄像头
                integrator.setBeepEnabled(true); //扫描成功的「哔哔」声，默认开启
                integrator.setBarcodeImageEnabled(true);
                myRequestCode = REFERENCE_SCAN;
                integrator.initiateScan();
            }
        });

        btnStartAdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!updateRadioButton.isChecked()) {
                    etAds2.setText("01,03,");
                }
                int et1Len = etAds1.getText().toString().replaceAll(":","").length();
                int et2Len = etAds2.getText().toString().replaceAll(":","").length();
                //int et3Len = etAds3.getText().toString().replaceAll(":","").length();
                int et1ByteLen = et1Len/2;
                int et2ByteLen = et2Len/2;
                //int et3ByteLen = et3Len/2;

                boolean et1Valid = true;
                boolean et2Valid = true;
               // boolean et3Valid = true;


                if(et1Len%2!=0) {
                    Log.i(TAG, "Wrong input format.");
                    //etAds1.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.editsharp));
                    et1Valid = false;
                }
                if(et2Len%2!=0) {
                    Log.i(TAG, "Wrong input format.");
                    //etAds2.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.editsharp));
                    et2Valid = false;
                }
                //if(et3Len%2!=0) {
                //    Log.i(TAG, "Wrong input format.");
                //    etAds3.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.border3));
                 //   et3Valid = false;
               // }
                //if(et1ByteLen+et2ByteLen+et3ByteLen>=6) {
                if(et1ByteLen+et2ByteLen>=6) {
                    if(et1ByteLen<6) {
                        //etAds1.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.border3));
                        et1Valid = false;
                    }
                    /*if(et1ByteLen+et2ByteLen+et3ByteLen>=14) {
                        if(et1ByteLen<6) {
                            etAds1.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.border3));
                            et1Valid = false;
                        }
                        if(et2ByteLen<8) {
                            etAds2.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.border3));
                            et2Valid = false;
                        }
                    }*/
                }

                if(et1Valid)
                    etAds1.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.editsharp));
                if(et2Valid)
                    etAds2.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.editsharp));
                //if(et3Valid)
                //    etAds3.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.border2));
                //if(et1Valid && et2Valid && et3Valid) {
                if(et1Valid && et2Valid) {
                    advStarted = true;
                    btnStartAdv.setEnabled(false);
                    btnStopAdv.setEnabled(false);
                    registerRadioButton.setEnabled(false);
                    finderRadioButton.setEnabled(false);
                    if(MainActivity.language == MainActivity.Chinese) {
                        btnStartAdv.setBackgroundResource(R.drawable.start_cn);
                        btnStopAdv.setBackgroundResource(R.drawable.stop_cn);
                        registerRadioButton.setBackgroundResource(R.drawable.reg_cn);
                        finderRadioButton.setBackgroundResource(R.drawable.sea_cn);
                    }else{
                        btnStartAdv.setBackgroundResource(R.drawable.start_en);
                        btnStopAdv.setBackgroundResource(R.drawable.stop_en);
                        registerRadioButton.setBackgroundResource(R.drawable.reg_en);
                        finderRadioButton.setBackgroundResource(R.drawable.sea_en);
                    }
/*
                    String outSring = etAds1.getText().toString()+etAds2.getText().toString()+etAds3.getText().toString();
                    outSring = outSring.replaceAll(":","");
                    adsText = outSring;
                    String statusMessage = "Len: "+((adsText.length()/2)+3)+"\nAD Type: 0xFF\nPayload: C000 "+adsText;
                    tvCurrentADV.setText(statusMessage); */
                    String ble_mac = "18:7A:93:01:0F:12";   //Fixed Bluetooth MAC address
                    //ADV Data is: Bluetooth MAC & Device ID & CMD

                    if(etAds1.getText().toString().equals("12:12:12:12:12:12")){
                        shortTime = true;
                    }else{
                        shortTime = false;
                    }

                    String outSring;
                    if(oldVersion == 1) {
                        outSring = etAds1.getText().toString() + etAds2.getText().toString();        //Version 0.09
                    }
                    else{
                        outSring = ble_mac + etAds1.getText().toString() + etAds2.getText().toString();    //Version 0.14
                    }
                    outSring = outSring.replaceAll(":","");
                    adsText = outSring;
                    String statusMessage = "Len: "+((adsText.length()/2)+3)+"\nAD Type: 0xFF Payload: C000\n"+adsText;
                    tvCurrentADV.setText(statusMessage);
                    //if(MainActivity.language == MainActivity.Chinese) {
                    //    tvCurrentADV.setText("命令发出，等待标签发声。");
                    //}else{
                    //    tvCurrentADV.setText("Command sending, wait for device beep.");
                    //}
                    startAdvertising();
                }
            }
        });

        btnStopAdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!updateRadioButton.isChecked()) {
                    etAds2.setText("01,02,");
                }
                int et1Len = etAds1.getText().toString().replaceAll(":","").length();
                int et2Len = etAds2.getText().toString().replaceAll(":","").length();
                //int et3Len = etAds3.getText().toString().replaceAll(":","").length();
                int et1ByteLen = et1Len/2;
                int et2ByteLen = et2Len/2;
                //int et3ByteLen = et3Len/2;

                boolean et1Valid = true;
                boolean et2Valid = true;
                // boolean et3Valid = true;


                if(et1Len%2!=0) {
                    Log.i(TAG, "Wrong input format.");
                    //etAds1.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.editsharp));
                    et1Valid = false;
                }
                if(et2Len%2!=0) {
                    Log.i(TAG, "Wrong input format.");
                    //etAds2.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.editsharp));
                    et2Valid = false;
                }
                //if(et3Len%2!=0) {
                //    Log.i(TAG, "Wrong input format.");
                //    etAds3.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.border3));
                //   et3Valid = false;
                // }
                //if(et1ByteLen+et2ByteLen+et3ByteLen>=6) {
                if(et1ByteLen+et2ByteLen>=6) {
                    if(et1ByteLen<6) {
                        //etAds1.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.border3));
                        et1Valid = false;
                    }
                    /*if(et1ByteLen+et2ByteLen+et3ByteLen>=14) {
                        if(et1ByteLen<6) {
                            etAds1.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.border3));
                            et1Valid = false;
                        }
                        if(et2ByteLen<8) {
                            etAds2.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.border3));
                            et2Valid = false;
                        }
                    }*/
                }

                if(et1Valid)
                    etAds1.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.editsharp));
                if(et2Valid)
                    etAds2.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.editsharp));
                //if(et3Valid)
                //    etAds3.setBackground(ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.border2));
                //if(et1Valid && et2Valid && et3Valid) {
                if(et1Valid && et2Valid) {
                    advStarted = true;
                    btnStartAdv.setEnabled(false);
                    btnStopAdv.setEnabled(false);
                    registerRadioButton.setEnabled(false);
                    finderRadioButton.setEnabled(false);
                    if(MainActivity.language == MainActivity.Chinese) {
                        btnStartAdv.setBackgroundResource(R.drawable.start_cn);
                        btnStopAdv.setBackgroundResource(R.drawable.stop_cn);
                        registerRadioButton.setBackgroundResource(R.drawable.reg_cn);
                        finderRadioButton.setBackgroundResource(R.drawable.sea_cn);
                    }else{
                        btnStartAdv.setBackgroundResource(R.drawable.start_en);
                        btnStopAdv.setBackgroundResource(R.drawable.stop_en);
                        registerRadioButton.setBackgroundResource(R.drawable.reg_en);
                        finderRadioButton.setBackgroundResource(R.drawable.sea_en);
                    }
/*
                    String outSring = etAds1.getText().toString()+etAds2.getText().toString()+etAds3.getText().toString();
                    outSring = outSring.replaceAll(":","");
                    adsText = outSring;
                    String statusMessage = "Len: "+((adsText.length()/2)+3)+"\nAD Type: 0xFF\nPayload: C000 "+adsText;
                    tvCurrentADV.setText(statusMessage); */
                    String ble_mac = "18:7A:93:01:0F:12";   //Fixed Bluetooth MAC address
                    //ADV Data is: Bluetooth MAC & Device ID & CMD
                    String outSring = ble_mac + etAds1.getText().toString() + etAds2.getText().toString();
                    outSring = outSring.replaceAll(":","");
                    adsText = outSring;
                    String statusMessage = "Len: "+((adsText.length()/2)+3)+"\nAD Type: 0xFF Payload: C000\n"+adsText;
                    tvCurrentADV.setText(statusMessage);

                    startAdvertising();
                }
/*                advStarted = false;
                btnStopAdv.setEnabled(false);
                btnStartAdv.setEnabled(true);
                adsText = "";
                tvCurrentADV.setText("");
                registerRadioButton.setEnabled(true);
                finderRadioButton.setEnabled(true);
                if(MainActivity.language == MainActivity.Chinese) {
                    btnStartAdv.setBackgroundResource(R.drawable.start_cn_1);
                    btnStopAdv.setBackgroundResource(R.drawable.stop_cn);
                    registerRadioButton.setBackgroundResource(R.drawable.reg_cn_1);
                    finderRadioButton.setBackgroundResource(R.drawable.sea_cn_1);
                }else{
                    btnStartAdv.setBackgroundResource(R.drawable.start_en_1);
                    btnStopAdv.setBackgroundResource(R.drawable.stop_en);
                    registerRadioButton.setBackgroundResource(R.drawable.reg_en_1);
                    finderRadioButton.setBackgroundResource(R.drawable.sea_en_1);
                }
                stopAdvertising(); */
            }
        });
/*
        btnScanMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearLineFlag = true;
                etAds3.setText("");
                clearLineFlag = false;
                Intent intent1 = new Intent(getActivity(), CaptureActivity.class);
                ZxingConfig config = new ZxingConfig();
                config.setShowbottomLayout(true);//底部布局（包括闪光灯和相册）
                config.setPlayBeep(true);//是否播放提示音
                config.setShake(true);//是否震动
                config.setShowAlbum(true);//是否显示相册
                config.setShowFlashLight(true);//是否显示闪光灯
                intent1.putExtra(Constant.INTENT_ZXING_CONFIG, config);

                startActivityForResult(intent1, REQUEST_MAIL_CODE_SCAN);
                // etAds3.setText("123456789012");
            }
        });

        btnScanMAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearLineFlag = true;
                etAds1.setText("");
                clearLineFlag = false;
                Intent intent1 = new Intent(getActivity(), CaptureActivity.class);
                ZxingConfig config = new ZxingConfig();
                config.setShowbottomLayout(true);//底部布局（包括闪光灯和相册）
                config.setPlayBeep(true);//是否播放提示音
                config.setShake(true);//是否震动
                config.setShowAlbum(true);//是否显示相册
                config.setShowFlashLight(true);//是否显示闪光灯
                intent1.putExtra(Constant.INTENT_ZXING_CONFIG, config);

                startActivityForResult(intent1, REQUEST_MAC_CODE_SCAN);
                //etAds1.setText("123456789012");
            }
        });
*/
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((etAds3.getText().toString().length() >= recordsManager.MIN_NUMBER_LENGTH)
                        && (etAds3.getText().toString().length() <= recordsManager.MAX_NUMBER_LENGTH)){
                    delNum = recordsManager.match_number(etAds3.getText().toString());

                    if (delNum != -1) {
                        dialog1Delete();
                        if(MainActivity.language == MainActivity.Chinese)
                            tvCurrentADV.setText("该快递件已经删除！");
                        else
                            tvCurrentADV.setText("The mail has been deleted!");
                    } else {
                        Log.i(TAG, "No such record in database!: "+ delNum);
                        if(MainActivity.language == MainActivity.Chinese)
                            tvCurrentADV.setText("输入的运单号不正确！");
                        else
                            tvCurrentADV.setText("Input a incorrect number!");
                    }
                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==registerRadioButton.getId()){
                    System.out.println("Register set!");
                    findRadioFlag = false;
                    //btnClearLine2.setText("登记");
                    btnClearLine1.setEnabled(true);
                    btnStartAdv.setEnabled(false);
                    btnStopAdv.setEnabled(false);
                    btnClearLine1.setBackgroundResource(R.drawable.button1_1);
                    if(MainActivity.language == MainActivity.Chinese) {
                        btnStartAdv.setBackgroundResource(R.drawable.start_cn);
                        btnStopAdv.setBackgroundResource(R.drawable.stop_cn);
                    }else{
                        btnStartAdv.setBackgroundResource(R.drawable.start_en);
                        btnStopAdv.setBackgroundResource(R.drawable.stop_en);
                    }
                }else if(i==finderRadioButton.getId()){
                    System.out.println("Finder set!");
                    findRadioFlag = true;
                    //btnClearLine2.setText("搜索");
                    if(productionMode == true){
                        btnClearLine1.setEnabled(true);
                        btnClearLine1.setBackgroundResource(R.drawable.button1_1);
                    }else{
                        btnClearLine1.setEnabled(false);
                        btnClearLine1.setBackgroundResource(R.drawable.button1);
                    }
                    btnClearLine1.setEnabled(false);
                    btnStartAdv.setEnabled(true);
                    btnStopAdv.setEnabled(true);
                    if(MainActivity.language == MainActivity.Chinese) {
                        btnStartAdv.setBackgroundResource(R.drawable.start_cn_1);
                        btnStopAdv.setBackgroundResource(R.drawable.stop_cn_1);
                    }else{
                        btnStartAdv.setBackgroundResource(R.drawable.start_en_1);
                        btnStopAdv.setBackgroundResource(R.drawable.stop_en_1);
                    }
                }
            }
        });

        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == sourchRadioButton.getId()){
                    etAds2.setText("01,03,");
                }else if(i == updateRadioButton.getId()){
                    etAds2.setText("02,01,");
                }else if(i == offRadioButton.getId()) {
                    etAds2.setText("01,02,");
                }
            }
        });

        mLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //alert dialog pop up
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                int lan = MainActivity.language;

                builder.setSingleChoiceItems(single_list, lan, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateLanguage(which);
                        //String str = single_list[which];
                        //Toast.makeText(getContext(), str + "被点击了", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        Log.i(TAG, "On Create View execute finished!");
        return view;
    }


    private void registerAfterMacTextChangedCallback(final EditText editText, final EditText nextEditText, final int MaxLength) {
//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.i(TAG, "s: "+s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if(nextEditText!=null) {
//                    Log.i(TAG, "editText.getText().length(): "+editText.getText().length());
//                    if (editText.getText().length() == MaxLength+(MaxLength/2)-1) {
//                        nextEditText.requestFocus();
//                    }
//                }
//            }
//        });

        editText.addTextChangedListener(new TextWatcher() {
            String mPreviousMac = null;


            /* (non-Javadoc)
             * Does nothing.
             * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
             */
            @Override
            public void afterTextChanged(Editable arg0) {
                if(nextEditText!=null) {
                    Log.i(TAG, "editText.getText().length(): "+editText.getText().length());
                    if (editText.getText().length() == MaxLength+(MaxLength/2)-1) {
                        nextEditText.requestFocus();
                    }
                }
            }

            /* (non-Javadoc)
             * Does nothing.
             * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
             */
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            /* (non-Javadoc)
             * Formats the MAC address and handles the cursor position.
             * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
             */
            @SuppressLint("DefaultLocale")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!clearLineFlag) {
                    String enteredMac = editText.getText().toString().toUpperCase();
                    String cleanMac = clearNonMacCharacters(enteredMac);
                    String formattedMac = formatMacAddress(cleanMac);

                    int selectionStart = editText.getSelectionStart();
                    formattedMac = handleColonDeletion(enteredMac, formattedMac, selectionStart);
                    int lengthDiff = formattedMac.length() - enteredMac.length();

                    setMacEdit(cleanMac, formattedMac, selectionStart, lengthDiff);
                }
            }

            /**
             * Strips all characters from a string except A-F and 0-9.
             * @param mac       User input string.
             * @return          String containing MAC-allowed characters.
             */
            private String clearNonMacCharacters(String mac) {
                return mac.toString().replaceAll("[^A-Fa-f0-9]", "");
            }

            /**
             * Adds a colon character to an unformatted MAC address after
             * every second character (strips full MAC trailing colon)
             * @param cleanMac      Unformatted MAC address.
             * @return              Properly formatted MAC address.
             */
            private String formatMacAddress(String cleanMac) {
                int grouppedCharacters = 0;
                String formattedMac = "";

                for (int i = 0; i < cleanMac.length(); ++i) {
                    formattedMac += cleanMac.charAt(i);
                    ++grouppedCharacters;

                    if (grouppedCharacters == 2) {
                        formattedMac += ":";
                        grouppedCharacters = 0;
                    }
                }

                // Removes trailing colon for complete MAC address
                if (cleanMac.length() == MaxLength)
                    formattedMac = formattedMac.substring(0, formattedMac.length() - 1);

                return formattedMac;
            }

            /**
             * Upon users colon deletion, deletes MAC character preceding deleted colon as well.
             * @param enteredMac            User input MAC.
             * @param formattedMac          Formatted MAC address.
             * @param selectionStart        MAC EditText field cursor position.
             * @return                      Formatted MAC address.
             */
            private String handleColonDeletion(String enteredMac, String formattedMac, int selectionStart) {
                if (mPreviousMac != null && mPreviousMac.length() > 1) {
                    int previousColonCount = colonCount(mPreviousMac);
                    int currentColonCount = colonCount(enteredMac);

                    if (currentColonCount < previousColonCount) {
                        formattedMac = formattedMac.substring(0, selectionStart - 1) + formattedMac.substring(selectionStart);
                        String cleanMac = clearNonMacCharacters(formattedMac);
                        formattedMac = formatMacAddress(cleanMac);
                    }
                }
                return formattedMac;
            }

            /**
             * Gets MAC address current colon count.
             * @param formattedMac      Formatted MAC address.
             * @return                  Current number of colons in MAC address.
             */
            private int colonCount(String formattedMac) {
                return formattedMac.replaceAll("[^,]", "").length();
            }

            /**
             * Removes TextChange listener, sets MAC EditText field value,
             * sets new cursor position and re-initiates the listener.
             * @param cleanMac          Clean MAC address.
             * @param formattedMac      Formatted MAC address.
             * @param selectionStart    MAC EditText field cursor position.
             * @param lengthDiff        Formatted/Entered MAC number of characters difference.
             */
            private void setMacEdit(String cleanMac, String formattedMac, int selectionStart, int lengthDiff) {
                editText.removeTextChangedListener(this);
                if (cleanMac.length() <= MaxLength) {
                    editText.setText(formattedMac);
                    editText.setSelection(selectionStart + lengthDiff);
                    mPreviousMac = formattedMac;
                } else {
                    editText.setText(mPreviousMac);
                    editText.setSelection(mPreviousMac.length());
                }
                editText.addTextChangedListener(this);
            }
        });
    }
    /**
     * When app comes on screen, check if BLE Advertisements are running, set switch accordingly,
     * and register the Receiver to be notified if Advertising fails.
     */
    @Override
    public void onResume() {
        super.onResume();

        IntentFilter failureFilter = new IntentFilter(AdvertiserService.ADVERTISING_FAILED);
        getActivity().registerReceiver(advertisingFailureReceiver, failureFilter);

    }

    /**
     * When app goes off screen, unregister the Advertising failure Receiver to stop memory leaks.
     * (and because the app doesn't care if Advertising fails while the UI isn't active)
     */
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(advertisingFailureReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAdvertising();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.app_version:
                ErrorDialog.newInstance(new ErrorDialog.onOneClickListener() {
                    @Override
                    public void onDismiss() {

                    }
                }, R.string.app_version, true).show(getActivity().getFragmentManager(), ErrorDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
/*
    //Call back from BarCode scan module. And return result.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        short ptr;
        String ss1;
        // 扫描二维码/条码回传 */
/*        if (requestCode == REQUEST_MAIL_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                etAds3.setText(content);
                if(content.equals("Hunter Clear.")){
                    RecordsManager.init_mail_data();
                    tvCurrentADV.setText("全部登记件已清除！");
                    etAds3.setText("");
                } else if(content.equals("Hunter Production.")) {
                    productionMode = true;
                    radioGroup1.setVisibility(View.VISIBLE);
                    tvCurrentADV.setText("生产线模式已开启！");
                    etAds3.setText("");
                    btnClearLine1.setEnabled(true);
                    btnClearLine1.setBackgroundResource(R.drawable.button1_1);
                } else if (finderRadioButton.isChecked()) {
                    //Finder process
                    clearLineFlag = true;
                    etAds1.setText("");
                    clearLineFlag = false;
                    if ((etAds3.getText().toString().length() >= recordsManager.MIN_NUMBER_LENGTH)
                            && (etAds3.getText().toString().length() <= recordsManager.MAX_NUMBER_LENGTH)) {
                        ptr = recordsManager.match_number(etAds3.getText().toString());
                        Log.i(TAG, "found location is: "+ ptr);
                        if (ptr != -1) {
                            ss1 = recordsManager.get_mac(ptr);
                            etAds1.setText(ss1);
                            if(MainActivity.language == MainActivity.Chinese)
                                tvCurrentADV.setText("仓库里存放着该物件！");
                            else
                                tvCurrentADV.setText("The thing is in the warehouse!");
                        } else {
                            //etAds1.setText("FFFFFFFFFFFF");
                            if(MainActivity.language == MainActivity.Chinese)
                                tvCurrentADV.setText("仓库里没有该物件！");
                            else
                                tvCurrentADV.setText("There is no such thing in the warehouse!");
                        }
                    } else {
                        //etAds1.setText("000000000000");
                        if(MainActivity.language == MainActivity.Chinese)
                            tvCurrentADV.setText("输入的运单号不正确！");
                        else
                            tvCurrentADV.setText("Input a incorrect number!");
                    }
                    //clearLineFlag = false;
                }
            }
        }
        else if (requestCode == REQUEST_MAC_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {

                String content = data.getStringExtra(Constant.CODED_CONTENT);
                etAds1.setText(content);
            }
        }
*/
 /*       if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (scanResult != null) {
                String result = scanResult.getContents();
                if (myRequestCode == DEVICE_ID_SCAN) {
                    Toast.makeText(getActivity(), "1-" + result, Toast.LENGTH_LONG).show();
                    etAds1.setText(result);
                } else if (myRequestCode == REFERENCE_SCAN) {
                    Toast.makeText(getActivity(), "2-" + result, Toast.LENGTH_LONG).show();
                    etAds3.setText(result);
                } else {
                    Toast.makeText(getActivity(), "0-" + result, Toast.LENGTH_LONG).show();
                }
            }
        }
    } */

    public static String getAdsText() {
        return adsText;
    }

    public static void decodeReferenceCode(){
        short ptr;
        String ss1;

        String content = etAds3.getText().toString();

                if (content.equals("Hunter Clear.")) {
                    RecordsManager.init_mail_data();
                    tvCurrentADV.setText("全部登记件已清除！");
                    etAds3.setText("");
                } else if (content.equals("Hunter Production.")) {
                    productionMode = true;
                    radioGroup1.setVisibility(View.VISIBLE);
                    tvCurrentADV.setText("生产线模式已开启！");
                    etAds3.setText("");
                    btnClearLine1.setEnabled(true);
                    btnClearLine1.setBackgroundResource(R.drawable.button1_1);
                } else if (content.equals("Hunter Version 009")) {
                    oldVersion = 1;
                    myVersion.setText(R.string.app_version_old);
                    etAds3.setText("");
                } else if (content.equals("Hunter Version Back")){
                    oldVersion = 0;
                    myVersion.setText(R.string.app_version);
                    etAds3.setText("");
                } else if (finderRadioButton.isChecked()) {
                    //Finder process
                    clearLineFlag = true;
                    etAds1.setText("");
                    clearLineFlag = false;
                    if ((etAds3.getText().toString().length() >= recordsManager.MIN_NUMBER_LENGTH)
                            && (etAds3.getText().toString().length() <= recordsManager.MAX_NUMBER_LENGTH)) {
                        ptr = recordsManager.match_number(etAds3.getText().toString());
                        Log.i(TAG, "found location is: " + ptr);
                        if (ptr != -1) {
                            ss1 = recordsManager.get_mac(ptr);
                            etAds1.setText(ss1);
                            if (MainActivity.language == MainActivity.Chinese)
                                tvCurrentADV.setText("仓库里存放着该物件！");
                            else
                                tvCurrentADV.setText("The thing is in the warehouse!");
                        } else {
                            //etAds1.setText("FFFFFFFFFFFF");
                            if (MainActivity.language == MainActivity.Chinese)
                                tvCurrentADV.setText("仓库里没有该物件！");
                            else
                                tvCurrentADV.setText("There is no such thing in the warehouse!");
                        }
                    } else {
                        //etAds1.setText("000000000000");
                        if (MainActivity.language == MainActivity.Chinese)
                            tvCurrentADV.setText("输入的运单号不正确！");
                        else
                            tvCurrentADV.setText("Input a incorrect number!");
                    }
                    //clearLineFlag = false;
                }

    }

    /**
     * Returns Intent addressed to the {@code AdvertiserService} class.
     */
    private static Intent getServiceIntent(Context c) {
        return new Intent(c, AdvertiserService.class);
    }


    /**
     * Starts BLE Advertising by starting {@code AdvertiserService}.
     */
    private void startAdvertising() {
        Context c = getActivity();
        c.startService(getServiceIntent(c));
    }

    /**
     * Stops BLE Advertising by stopping {@code AdvertiserService}.
     */
    private void stopAdvertising() {
        Context c = getActivity();
        c.stopService(getServiceIntent(c));
    }

    private void dialog1Delete(){
        //先new出一个监听器，设置好监听
        DialogInterface.OnClickListener dialogOnclicListener=new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case Dialog.BUTTON_POSITIVE:
                        //Toast.makeText(MainActivity.this, "确认" + which, Toast.LENGTH_SHORT).show();
                        recordsManager.DeleteRecord(delNum);
                        recordsManager.saveRecordsData();
                        Log.i(TAG, "OK is selected!");
                        break;
                    case Dialog.BUTTON_NEGATIVE:
                        //Toast.makeText(MainActivity.this, "取消" + which, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Cancel is selected!");
                        break;
//                    case Dialog.BUTTON_NEUTRAL:
//                        Toast.makeText(MainActivity.this, "忽略" + which, Toast.LENGTH_SHORT).show();
//                        break;
                }
            }
        };
        //dialog参数设置
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());  //先得到构造器

        if(MainActivity.language == MainActivity.Chinese)
            builder.setTitle("请确认"); //设置标题
        else
            builder.setTitle("Confirmation");

        if(MainActivity.language == MainActivity.Chinese)
            builder.setMessage("真的要删除垓项登记吗?"); //设置内容
        else
            builder.setMessage("Are you sure to delete the record?");

        //builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可

        if(MainActivity.language == MainActivity.Chinese)
            builder.setPositiveButton("确定",dialogOnclicListener);
        else
            builder.setPositiveButton("OK",dialogOnclicListener);

        if(MainActivity.language == MainActivity.Chinese)
            builder.setNegativeButton("取消", dialogOnclicListener);
        else
            builder.setNegativeButton("Cancel", dialogOnclicListener);
/*
        if(MainActivity.language == MainActivity.Chinese)
            builder.setNeutralButton("忽略", dialogOnclicListener);
        else
            builder.setNeutralButton("Ignore", dialogOnclicListener);
*/
        builder.create().show();
    }

    void updateLanguage(int lang){
        MainActivity.language = lang;
        String sL;
        if(lang == 0){
            sL = "0";
        }else{
            sL = "1";
        }

        //实例化SharedPreferences对象（第一步）
        SharedPreferences mySharedPreferences = getContext().getSharedPreferences("Language", Activity.MODE_PRIVATE);
        //实例化SharedPreferences.Editor对象（第二步）
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        //用putString的方法保存数据
        editor.putString("Language", sL);
        //提交当前数据
        editor.commit();
    }

}
