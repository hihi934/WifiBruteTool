package com.hihi934.wifibrutetool;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText edtSSID;
    private Button btnStart;
    private TextView txtLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtSSID = findViewById(R.id.edtSSID);
        btnStart = findViewById(R.id.btnStart);
        txtLog = findViewById(R.id.txtLog);

        btnStart.setOnClickListener(v -> {
            String ssid = edtSSID.getText().toString();
            txtLog.setText("Đang khởi tạo tấn công SSID: " + ssid + "\nBuild by Vu Vinh An");
        });
    }
}
