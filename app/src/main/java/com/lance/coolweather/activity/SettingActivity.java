package com.lance.coolweather.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lance.common.widget.OptionView;
import com.lance.common.widget.TopBar;
import com.lance.coolweather.R;
import com.lance.coolweather.config.AppUtil;
import com.lance.coolweather.service.AutoUpdateService;

/**
 * 设置
 */
public class SettingActivity extends BaseActivity implements TopBar.OnClickListener, View.OnClickListener {
    private static final String TAG = "SettingActivity";
    private boolean autoUpdate, showNotification;
    private int autoUpdateInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initData();
        initView();
    }

    private void initData() {
        autoUpdate = AppUtil.isAutoUpdateEnabled(this);
        showNotification = AppUtil.isNotificationDisplayEnabled(this);
        autoUpdateInterval = AppUtil.getAutoUpdateInterval(this);
    }

    private void initView() {
        TopBar tbTitleBar = (TopBar) findViewById(R.id.tb_title_bar);
        tbTitleBar.setOnClickTopBarListener(this);
        OptionView ovAutoUpdate = (OptionView) findViewById(R.id.ov_auto_update);
        OptionView ovAutoUpdateInterval = (OptionView) findViewById(R.id.ov_auto_update_interval);
        OptionView ovShowNotification = (OptionView) findViewById(R.id.ov_show_notification);

        ovAutoUpdate.setOnClickListener(this);
        ovAutoUpdateInterval.setOnClickListener(this);
        ovShowNotification.setOnClickListener(this);
    }

    @Override
    public void onClickLeft() {
        finish();
    }

    @Override
    public void onClickTitle() {

    }

    @Override
    public void onClickRight() {

    }

    @Override
    public void onClick(View v) {
        showOptionDialog(v.getId());
    }

    private void showOptionDialog(int viewId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] options = null;
        switch (viewId) {
            case R.id.ov_auto_update:
                options = getResources().getStringArray(R.array.app_setting_auto_update_items);
                break;
            case R.id.ov_auto_update_interval:
                options = getResources().getStringArray(R.array.app_setting_auto_update_interval_items);
                break;
            case R.id.ov_show_notification:
                options = getResources().getStringArray(R.array.app_setting_show_notification_items);
                break;
        }
        if (options != null) {
            switch (viewId) {
                case R.id.ov_auto_update:
                    builder.setSingleChoiceItems(options, autoUpdate ? 0 : 1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            boolean temp = autoUpdate;
                            if (which == 0) {
                                temp = true;
                            } else if (which == 1) {
                                temp = false;
                            }
                            if (temp != autoUpdate) {
                                autoUpdate = temp;
                                AppUtil.setAutoUpdateEnabled(SettingActivity.this, autoUpdate);
                                Intent intent = new Intent(SettingActivity.this, AutoUpdateService.class);
                                stopService(intent);
                                startService(intent);
                            }
                        }
                    }).create().show();
                    break;
                case R.id.ov_auto_update_interval:
                    if (autoUpdate) {
                        final String[] finalOptions = options;
                        String prefix = String.valueOf(autoUpdateInterval);
                        int selectedIndex = 3;
                        for (int i = 0; i < finalOptions.length; i++) {
                            if (finalOptions[i].startsWith(prefix)) {
                                selectedIndex = i;
                                break;
                            }
                        }
                        builder.setSingleChoiceItems(options, selectedIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                String intervalString = finalOptions[which];
                                try {
                                    int temp = Integer.parseInt(intervalString.substring(0, 1));
                                    if (autoUpdateInterval != temp) {
                                        autoUpdateInterval = temp;
                                        AppUtil.setAutoUpdateInterval(SettingActivity.this, autoUpdateInterval);
                                        Intent intent = new Intent(SettingActivity.this, AutoUpdateService.class);
                                        stopService(intent);
                                        startService(intent);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "onClick: " + e.toString());
                                }
                            }
                        }).create().show();
                    }
                    break;
                case R.id.ov_show_notification:
                    if (autoUpdate) {
                        builder.setSingleChoiceItems(options, showNotification ? 0 : 1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                boolean result = which == 0;
                                if (result != showNotification) {
                                    showNotification = result;
                                    AppUtil.setNotificationDisplayEnabled(SettingActivity.this, showNotification);
                                    Intent intent = new Intent(SettingActivity.this, AutoUpdateService.class);
                                    stopService(intent);
                                    startService(intent);
                                }
                            }
                        }).create().show();
                    }
                    break;
            }
        }
    }
}
