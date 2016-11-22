package com.jr.flashlight;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ToggleButton btnSwitch;
    private Camera camera;
    private Camera.Parameters params;
    private boolean hasFlash = false;
    private Handler handler;
    private Runnable r;
    private int closeTime = 1000 * 60 * 10;
    private boolean runOnBg = false; //后台运行
    private boolean timingOff = true; //定时关闭

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btnSwitch = (ToggleButton) findViewById(R.id.btn_switch);
        btnSwitch.setChecked(SharedPreferencesUtils.getInt("isOpen") == 1);
        btnSwitch.setOnClickListener(this);
        r = new Runnable() {
            @Override
            public void run() {
                closeFlash();
            }
        };

        getCamera();
        if (SharedPreferencesUtils.getInt("isOpen") != 1) {
            openFlash();
        }
    }

    /**
     * 设置菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //1是选中 0是为选中 -1是未设置
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (SharedPreferencesUtils.getInt("runOnBg") == -1)
            SharedPreferencesUtils.put("runOnBg", 0);

        if (SharedPreferencesUtils.getInt("timingOff") == -1)
            SharedPreferencesUtils.put("timingOff", 1);

        runOnBg = SharedPreferencesUtils.getInt("runOnBg") == 1;
        timingOff = SharedPreferencesUtils.getInt("timingOff") == 1;
        menu.getItem(0).setChecked(runOnBg);
        menu.getItem(1).setChecked(timingOff);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());
        switch (item.getItemId()) {
            case R.id.action_run_on_bg:
                runOnBg = item.isChecked();
                SharedPreferencesUtils.put("runOnBg", item.isChecked() ? 1 : 0);
                break;
            case R.id.action_timing_off:
                timingOff = item.isCheckable();
                SharedPreferencesUtils.put("timingOff", item.isChecked() ? 1 : 0);
                cancelCloseDelay();
                if (timingOff) {
                    setCloseDelay();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                if (btnSwitch.isChecked()) {
                    openFlash();
                } else {
                    closeFlash();
                }
                break;
        }
    }

    /**
     * 关闭闪光灯
     */
    private void closeFlash() {
        cancelCloseDelay(); //取消掉定时关闭
        //关
        if (camera == null || params == null) {
            finish();
            return;
        }
        try {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
        } catch (Exception e) {
            Log.i("warning", "关不掉");
        }
        SharedPreferencesUtils.put("isOpen", 0);
        btnSwitch.setChecked(false);
        finish();
    }

    /**
     * 打开闪光灯
     */
    private void openFlash() {
        Log.i("信息", "openFlash");
        if (!hasFlash) return;
        //开
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        camera.startPreview();
        SharedPreferencesUtils.put("isOpen", 1);
        btnSwitch.setChecked(true);

        if (handler == null) {
            handler = new Handler();
        }
        setCloseDelay();    //定时自动关闭
    }

    /**
     * 设置定时关闭
     */
    public void setCloseDelay() {
        handler.postDelayed(r, closeTime);
    }

    /**
     * 取消定时关闭
     */
    public void cancelCloseDelay() {
        if (handler != null && r != null) {
            handler.removeCallbacks(r);
        }
    }

    public void getCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        //判断有没有闪光灯
        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!hasFlash) return;
        Log.i("信息", "camera:" + camera);
        if (camera == null && SharedPreferencesUtils.getInt("isOpen") != 1) {
            camera = Camera.open();
            params = camera.getParameters();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!runOnBg) {
            closeFlash();
        }
    }
}
