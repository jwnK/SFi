package com.example.klk45.nfc_scanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by klk45 on 2017-09-09.
 */
public class SOSPopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
        setContentView(R.layout.activity_sos_popup);
    }

    public void onButtonYes(View v) {
        Intent intent = getIntent();
        String Wifi_name = intent.getExtras().getString("id");
        String Wifi_password = intent.getExtras().getString("pw");

        ConnectivityManager manager;
        WifiManager wifiManager=(WifiManager)getSystemService(WIFI_SERVICE);
        manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean status = wifiManager.isWifiEnabled();
        if (status == false){
            // 와이파이가 활성화되지 않았다면
            Toast.makeText(getApplicationContext(),"WIFI 활성화 시 이용하실 수 있습니다.", Toast.LENGTH_LONG).show();
            //와이파이 활성화
            wifiManager.setWifiEnabled(true);
        }
        Intent intentConfirm = new Intent();
        intentConfirm.setAction("android.settings.WIFI_SETTINGS");
        startActivity(intentConfirm);
        WifiConfiguration wifiConfig = new WifiConfiguration();

//        String Wifi_name = "U+Net51D3";
//        String Wifi_password = "4000021671";

        wifiConfig.SSID = String.format("\"%s\"", Wifi_name);
        wifiConfig.preSharedKey = String.format("\"%s\"", Wifi_password);
        wifiConfig.priority = 1;
        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        finish();
    }

    public void onButtonNo(View v) {
        Toast.makeText(getApplicationContext(), "아 개짱나네", Toast.LENGTH_SHORT).show();
        finish();
    }
}
