package com.realq.mobliesafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.realq.mobliesafe.adapter.MainAdapter;

public class MainActivity extends Activity {
	private GridView gv_main;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		gv_main = (GridView)findViewById(R.id.gv_main);
		gv_main.setAdapter(new MainAdapter(this));

		gv_main.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position){
					case 8:
						Intent settingIntent = new Intent(MainActivity.this,SettingCenterActivity.class);
						startActivity(settingIntent);
						break;
				}
			}
		});
	}

}
