package com.realq.mobliesafe;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


public class SettingCenterActivity extends Activity {
    private SharedPreferences sp;
    private TextView tv_setting_autoupdae_status;
    private CheckBox cb_setting_autoupdae;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_center);

        sp = getSharedPreferences("config",MODE_PRIVATE);
        cb_setting_autoupdae = (CheckBox)findViewById(R.id.cb_setting_autoupate);
        tv_setting_autoupdae_status = (TextView)findViewById(R.id.tv_setting_autoupdate_status);
        boolean autoupdate = sp.getBoolean("autoupdate",true);
        if(autoupdate){
            tv_setting_autoupdae_status.setText("自动更新已开启");
            cb_setting_autoupdae.setChecked(true);
        }else{
            tv_setting_autoupdae_status.setText("自动更新已关闭");
            cb_setting_autoupdae.setChecked(false);
        }

        cb_setting_autoupdae.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("autoupdate", isChecked);
                editor.commit();
                if(isChecked){
                    tv_setting_autoupdae_status.setText("自动更新已开启");
                    tv_setting_autoupdae_status.setTextColor(Color.WHITE);
                }else{
                    tv_setting_autoupdae_status.setText("自动更新已关闭");
                    tv_setting_autoupdae_status.setTextColor(Color.RED);
                }
            }
        });
    }
}
