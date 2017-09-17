package com.example.klk45.nfc_sender;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.Locale;

public class MainActivity extends Activity
        implements NfcAdapter.CreateNdefMessageCallback
        , NfcAdapter.OnNdefPushCompleteCallback {
    NfcAdapter mNfcAdapter = null; // NFC 어댑터
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView)findViewById(R.id.textMessage);
        // NFC 어댑터를 구한다
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // NFC 어댑터가 null 이라면 칩이 존재하지 않는 것으로 간주
        if( mNfcAdapter != null )
            mTextView.setText("Tap to another NFC device. And touch screen");
        else
            mTextView.setText("This phone is not NFC enable.");

        // NDEF 메시지 생성 & 전송을 위한 콜백 함수 설정
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        // NDEF 메시지 전송 완료 이벤트 콜백 함수 설정
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    // NDEF 메시지 생성 이벤트 함수
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        // 여러개의 NDEF 레코드를 모아서 하나의 NDEF 메시지를 생성
        NdefMessage message = new NdefMessage( new NdefRecord[] {
                createTextRecord("U+Net51D3", Locale.ENGLISH),
                createTextRecord("**********", Locale.ENGLISH),
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
        data[0] = (byte)langBytes.length;
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
                    mTextView.setText("NDEF message sending completed");
                    break;
            }
        }
    };
}
