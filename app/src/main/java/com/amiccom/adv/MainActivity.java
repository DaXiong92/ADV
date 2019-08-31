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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.ChineseCalendar;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.amiccom.adv.AdvertiserFragment;

import static com.amiccom.adv.AdvertiserFragment.DEVICE_ID_SCAN;
import static com.amiccom.adv.AdvertiserFragment.REFERENCE_SCAN;
import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

/**
 * Setup display fragments and ensure the device supports Bluetooth.
 */
public class MainActivity extends FragmentActivity {


    public static int language;
    public final static int English = 0;
    public final static int Chinese = 1;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //同样，在读取SharedPreferences数据前要实例化出一个SharedPreferences对象
        SharedPreferences mySharedPreferences = getSharedPreferences("Language", Activity.MODE_PRIVATE);
        // 使用getString方法获得value，注意第2个参数是value的默认值
        String s = mySharedPreferences.getString("Language", "1");
        if(s.equals("0")) {
            language = English;   //
        }
        else {
            language = Chinese;
        }

        //language = Chinese;

        super.onCreate(savedInstanceState);

        if(language == English){
            setContentView(R.layout.activity_main_en);}
        else{
            setContentView(R.layout.activity_main);}

        if (savedInstanceState == null) {

            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                    .getAdapter();

            // Is Bluetooth supported on this device?
            if (mBluetoothAdapter != null) {

                // Is Bluetooth turned on?
                if (mBluetoothAdapter.isEnabled()) {

                    // Are Bluetooth Advertisements supported on this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

                        // Everything is supported and enabled, load the fragments.
                        setupFragments();

                    } else {

                        // Bluetooth Advertisements are not supported.
                        showErrorText(R.string.bt_ads_not_supported);
                    }
//                    setupFragments();
                } else {

                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
                }
            } else {

                // Bluetooth is not supported.
                showErrorText(R.string.bt_not_supported);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:

                if (resultCode == RESULT_OK) {

                    // Bluetooth is now Enabled, are Bluetooth Advertisements supported on
                    // this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

                        // Everything is supported and enabled, load the fragments.
                        setupFragments();

                    } else {

                        // Bluetooth Advertisements are not supported.
                        showErrorText(R.string.bt_ads_not_supported);
                    }
//                    setupFragments();
                } else {

                    // User declined to enable Bluetooth, exit the app.
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (scanResult != null) {
                String result = scanResult.getContents();
                if (AdvertiserFragment.myRequestCode == DEVICE_ID_SCAN) {
                    //Toast.makeText(MainActivity.this, "1-" + result, Toast.LENGTH_LONG).show();
                    AdvertiserFragment.etAds1.setText(result);
                } else if (AdvertiserFragment.myRequestCode == REFERENCE_SCAN) {
                    //Toast.makeText(MainActivity.this, "2-" + result, Toast.LENGTH_LONG).show();
                    AdvertiserFragment.etAds3.setText(result);
                    AdvertiserFragment.decodeReferenceCode();
                } else {
                    Toast.makeText(MainActivity.this, "0-" + result, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void setupFragments() {
        Timer timer=new Timer();
        TimerTask task=new TimerTask(){
            public void run(){
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                AdvertiserFragment advertiserFragment = new AdvertiserFragment();
                if(language == English)
                    transaction.replace(R.id.advertiser_fragment_container_en, advertiserFragment);
                else
                    transaction.replace(R.id.advertiser_fragment_container, advertiserFragment);
                transaction.commitAllowingStateLoss();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        };
        timer.schedule(task, 2500);
    }

    private void showErrorText(int messageId) {

        ErrorDialog.newInstance(new ErrorDialog.onOneClickListener() {
            @Override
            public void onDismiss() {
                finish();
                System.exit(0);
            }
        }, messageId, false).show(getFragmentManager(), ErrorDialog.TAG);
//        TextView view = (TextView) findViewById(R.id.error_textview);
//        view.setText(getString(messageId));
    }

    // 0 = English, 1 = Chinese, others = reserved
    public void setLanguage(int lang){
        if(lang == English || lang == Chinese)
            language = lang;
    }

    public int getLanguage(){
        return language;
    }
}