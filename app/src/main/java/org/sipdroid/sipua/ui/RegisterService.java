/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.sipdroid.sipua.ui;

import org.sipdroid.media.RtpStreamReceiver;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;

public class RegisterService extends Service {
	Receiver m_receiver;

	
    public void onDestroy() {
		super.onDestroy();
		if (m_receiver != null) {
			unregisterReceiver(m_receiver);
		}
		Receiver.alarm(0, OneShotAlarm2.class);
	}
    
    @Override
    public void onCreate() {
    	super.onCreate();
    	Receiver.sContext = this;
    	if (Receiver.mContext == null)
				Receiver.mContext = this;
        if (m_receiver == null) {
			 IntentFilter intentfilter = new IntentFilter();
			 intentfilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			 intentfilter.addAction(Receiver.ACTION_DATA_STATE_CHANGED);
			 intentfilter.addAction(Receiver.ACTION_PHONE_STATE_CHANGED);
			 intentfilter.addAction(Receiver.ACTION_DOCK_EVENT);
			 intentfilter.addAction(Intent.ACTION_HEADSET_PLUG);
			 intentfilter.addAction(Intent.ACTION_USER_PRESENT);
			 intentfilter.addAction(Intent.ACTION_SCREEN_OFF);
			 intentfilter.addAction(Intent.ACTION_SCREEN_ON);
			 intentfilter.addAction(Receiver.ACTION_VPN_CONNECTIVITY);
			 intentfilter.addAction(Receiver.ACTION_SCO_AUDIO_STATE_CHANGED);
			 intentfilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				androidx.core.content.ContextCompat.registerReceiver(this, m_receiver = new Receiver(), intentfilter, androidx.core.content.ContextCompat.RECEIVER_EXPORTED);
			} else
				registerReceiver(m_receiver = new Receiver(), intentfilter);
			intentfilter = new IntentFilter();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean isIgnoringBatteryOptimizations = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                android.os.PowerManager pm = (android.os.PowerManager) getSystemService(android.content.Context.POWER_SERVICE);
                if (pm != null) {
                    isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());
                }
            }
            if (!isIgnoringBatteryOptimizations) {
                android.app.NotificationManager mNotificationMgr = (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
                mNotificationMgr.createNotificationChannel(new android.app.NotificationChannel("sipdroid_bg", "SIP Background Service", android.app.NotificationManager.IMPORTANCE_MIN));
                android.app.Notification bgNotif = new androidx.core.app.NotificationCompat.Builder(this, "sipdroid_bg")
                    .setSmallIcon(org.sipdroid.sipua.R.drawable.icon)
                    .setContentTitle("Android SIP")
                    .setContentText("Running in background")
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MIN)
                    .build();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        startForeground(Receiver.REGISTER_NOTIFICATION, bgNotif, 128); // FOREGROUND_SERVICE_TYPE_MICROPHONE
                    } catch (Exception e) {}
                } else {
                    try {
                        startForeground(Receiver.REGISTER_NOTIFICATION, bgNotif);
                    } catch (Exception e) {}
                }
            }
        }
        Receiver.engine(this).isRegistered();
    }
    
    @Override
    public void onStart(Intent intent, int id) {
         super.onStart(intent,id);
         RtpStreamReceiver.restoreSettings();
         Receiver.alarm(10*60, OneShotAlarm2.class);
    }

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
}
