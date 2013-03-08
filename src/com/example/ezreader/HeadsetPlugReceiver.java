package com.example.ezreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HeadsetPlugReceiver extends BroadcastReceiver {

	private static final String TAG = "HeadsetPlugReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		  if (intent.hasExtra("state")){
			   if (intent.getIntExtra("state", 0) == 0){	
			   }
			   else if (intent.getIntExtra("state", 0) == 1){
			   }
		  }
		
	}

}
