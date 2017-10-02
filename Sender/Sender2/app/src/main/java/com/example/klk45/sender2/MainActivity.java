package com.example.klk45.sender2;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.Locale;

public class MainActivity extends Activity
        implements NfcAdapter.CreateNdefMessageCallback
        , NfcAdapter.OnNdefPushCompleteCallback {
    NfcAdapter mNfcAdapter = null; // NFC 어댑터
    TextView mTextView;

    protected void NFC_func() {
        // NFC 어댑터를 구한다
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // NFC 어댑터가 null 이라면 칩이 존재하지 않는 것으로 간주
//        if (mNfcAdapter != null)
//            mTextView.setText("Tap to another NFC device. And touch screen");
//        else
//            mTextView.setText("This phone is not NFC enable.");

        // NDEF 메시지 생성 & 전송을 위한 콜백 함수 설정
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        // NDEF 메시지 전송 완료 이벤트 콜백 함수 설정
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    // NDEF 메시지 생성 이벤트 함수
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        // 여러개의 NDEF 레코드를 모아서 하나의 NDEF 메시지를 생성
        NdefMessage message = new NdefMessage(new NdefRecord[]{
                createTextRecord("TOMNTOMS_Guest2", Locale.ENGLISH),
                createTextRecord("TOM@1234", Locale.ENGLISH),
        });
        return message;
    }

    // 텍스트 형식의 레코드를 생성
    public NdefRecord createTextRecord(String text, Locale locale) {
        // 텍스트 데이터를 인코딩해서 byte 배열로 변환
        byte[] data = byteEncoding(text, locale);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    // 텍스트 데이터를 인코딩해서 byte 배열로 변환
    public byte[] byteEncoding(String text, Locale locale) {
        // 언어 지정 코드 생성
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        // 인코딩 형식 생성
        Charset utfEncoding = Charset.forName("UTF-8");
        // 텍스트를 byte 배열로 변환
        byte[] textBytes = text.getBytes(utfEncoding);

        // 전송할 버퍼 생성
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) langBytes.length;
        // 버퍼에 언어 코드 저장
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        // 버퍼에 데이터 저장
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        return data;
    }

    // URI 형식의 레코드를 생성
    public NdefRecord createUriRecord(String url) {
        // URI 경로를 byte 배열로 변환할 때 US-ACSII 형식으로 지정
        byte[] uriField = url.getBytes(Charset.forName("US-ASCII"));
        // URL 경로를 의미하는 1 을 첫번째 byte 에 추가
        byte[] payload = new byte[uriField.length + 1];
        payload[0] = 0x01;
        System.arraycopy(uriField, 0, payload, 1, uriField.length);
        // NDEF 레코드를 생성해서 반환
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], payload);
    }

    // NDEF 메시지 전송 완료 이벤트 함수
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        // 핸들러에 메시지를 전달한다
        mHandler.obtainMessage(1).sendToTarget();
    }

    // NDEF 메시지 전송이 완료되면 TextView 에 결과를 표시한다
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //mTextView.setText("NDEF message sending completed");
                    break;
            }
        }
    };

    ////////////////////////////////////////
    //private ImageView mImg;
    private static final String IMAGEVIEW_TAG = "드래그 이미지";
    private int wifi_state = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.textMessage);
        // setting
        findViewById(R.id.bot).setVisibility(View.INVISIBLE);
        findViewById(R.id.top).setVisibility(View.VISIBLE);
        Button s_top = (Button) findViewById(R.id.top);
        Button s_bot = (Button) findViewById(R.id.bot);

        s_top.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                ConnectivityManager manager;
                WifiManager wifiManager=(WifiManager)getSystemService(WIFI_SERVICE);
                manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                boolean status = wifiManager.isWifiEnabled();
                if (status == false)wifi_state = 0;
                else wifi_state = 1;
                RelativeLayout layout = (RelativeLayout)findViewById(R.id.layout);
                if(wifi_state == 0)layout.setBackgroundResource(R.drawable.wifioff);
                else layout.setBackgroundResource(R.drawable.wifion);
                findViewById(R.id.top).setVisibility(View.INVISIBLE);
                findViewById(R.id.bot).setVisibility(View.VISIBLE);
                findViewById(R.id.Wifi).setVisibility(View.VISIBLE);
                return true;
            }
        });
        s_bot.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                RelativeLayout layout = (RelativeLayout)findViewById(R.id.layout);
                layout.setBackgroundResource(R.drawable.normal);
                findViewById(R.id.bot).setVisibility(View.INVISIBLE);
                findViewById(R.id.top).setVisibility(View.VISIBLE);
                findViewById(R.id.Wifi).setVisibility(View.INVISIBLE);
                return true;
            }
        });

        RelativeLayout layout = (RelativeLayout)findViewById(R.id.layout);
        layout.setBackgroundResource(R.drawable.normal);
        Button toss_b =(Button) findViewById(R.id.toss);
        Button setting_b = (Button) findViewById(R.id.setting);
        toss_b.setVisibility(View.INVISIBLE);
        setting_b.setVisibility(View.INVISIBLE);
        Button b = (Button) findViewById(R.id.Wifi);
        //b.setVisibility(View.INVISIBLE);
        b.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((Button) findViewById(R.id.toss)).setVisibility(View.VISIBLE);
                ((Button) findViewById(R.id.setting)).setVisibility(View.VISIBLE);
                ClipData clip = ClipData.newPlainText("dragtext", "dragtext");
                v.startDrag(clip, new View.DragShadowBuilder(v), null, 0);
                return true;
            }
        });
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ConnectivityManager manager;
                WifiManager wifiManager=(WifiManager)getSystemService(WIFI_SERVICE);
                manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                boolean status = wifiManager.isWifiEnabled();
                if (status == false){
                    // 와이파이가 활성화되지 않았다면
                    //와이파이 활성화
                    wifiManager.setWifiEnabled(true);
                }
                else wifiManager.setWifiEnabled(false);
                RelativeLayout layout = (RelativeLayout)findViewById(R.id.layout);
                if(wifi_state == 0) {
                    Toast.makeText(getApplicationContext(), "Wifi on", Toast.LENGTH_SHORT).show();
                    layout.setBackgroundResource(R.drawable.wifion);
                    wifi_state = 1;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Wifi off", Toast.LENGTH_SHORT).show();
                    layout.setBackgroundResource(R.drawable.wifioff);
                    wifi_state = 0;
                }
            }
        });
        toss_b.setOnDragListener(mDragListener_for_toss);
        setting_b.setOnDragListener(mDragListener_for_setting);
    }
    View.OnDragListener mDragListener_for_toss = new View.OnDragListener(){
        @Override
        public boolean onDrag(View v, DragEvent event){
            Button btn;
            if(v instanceof Button){
                btn = (Button) v;
            }
            else return false;
            switch(event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        return true;
                    } else return false;
                case DragEvent.ACTION_DRAG_ENTERED:
                    //btn.setText("Enter");
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    //btn.setText("Exit");
                    return true;
                case DragEvent.ACTION_DROP:
                    Toast.makeText(getApplicationContext(), "Toss wifi", Toast.LENGTH_SHORT).show();
                    NFC_func();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    ((Button) findViewById(R.id.toss)).setVisibility(View.INVISIBLE);
                    ((Button) findViewById(R.id.setting)).setVisibility(View.INVISIBLE);
                    return true;
            };
            return true;
        }
    };

    View.OnDragListener mDragListener_for_setting = new View.OnDragListener(){
        @Override
        public boolean onDrag(View v, DragEvent event){
            Button btn;
            if(v instanceof Button){
                btn = (Button) v;
            }
            else return false;
            switch(event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        return true;
                    } else return false;
                case DragEvent.ACTION_DRAG_ENTERED:
                    //btn.setText("Enter");
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    //btn.setText("Exit");
                    return true;
                case DragEvent.ACTION_DROP:
                    //Toast.makeText(getApplicationContext(), "go settings", Toast.LENGTH_SHORT).show();
                    Intent intentConfirm = new Intent();
                    intentConfirm.setAction("android.settings.WIFI_SETTINGS");
                    startActivity(intentConfirm);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    ((Button) findViewById(R.id.toss)).setVisibility(View.INVISIBLE);
                    ((Button) findViewById(R.id.setting)).setVisibility(View.INVISIBLE);
                    return true;
            };
            return true;
        }
    };
}