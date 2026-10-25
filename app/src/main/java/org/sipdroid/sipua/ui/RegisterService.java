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
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;

public class RegisterService extends Service {
	Receiver m_receiver;
	private ConnectivityManager.NetworkCallback mNetworkCallback;

    public void onDestroy() {
		super.onDestroy();
		if (m_receiver != null) {
			unregisterReceiver(m_receiver);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && mNetworkCallback != null) {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			if (cm != null) {
				try {
					cm.unregisterNetworkCallback(mNetworkCallback);
				} catch (Exception e) {}
			}
			mNetworkCallback = null;
		}
		Receiver.alarm(0, OneShotAlarm2.class);
		if (Receiver.sContext == this) {
			Receiver.sContext = null;
		}
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
        if (Receiver.call_state != org.sipdroid.sipua.UserAgent.UA_STATE_IDLE) {
            Receiver.startCallForeground();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && mNetworkCallback == null) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (cm != null) {
                mNetworkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        if (!Sipdroid.release) android.util.Log.i("SipUA", "NetworkCallback onAvailable");
                        if (Receiver.call_state == 0) {
                            Receiver.engine(RegisterService.this).register();
                        }
                    }

                    @Override
                    public void onLost(Network network) {
                        if (!Sipdroid.release) android.util.Log.i("SipUA", "NetworkCallback onLost");
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                        boolean hasNetwork = false;
                        if (cm != null) {
                            Network active = cm.getActiveNetwork();
                            if (active != null) {
                                NetworkCapabilities caps = cm.getNetworkCapabilities(active);
                                if (caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                                     caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                                     caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) {
                                    hasNetwork = true;
                                }
                            }
                        }
                        if (!hasNetwork) {
                            org.sipdroid.sipua.SipdroidEngine engine = Receiver.engine(RegisterService.this);
                            if (engine != null && engine.ras != null) {
                                for (org.sipdroid.sipua.RegisterAgent ra : engine.ras) {
                                    if (ra != null) {
                                        ra.setState(org.sipdroid.sipua.RegisterAgent.UNREGISTERED);
                                    }
                                }
                            }
                            Receiver.registered();
                        } else if (Receiver.call_state == 0) {
                            Receiver.engine(RegisterService.this).register();
                        }
                    }

                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                        // Do not re-register on radio capability fluctuations mid-call
                    }
                };
                try {
                    cm.registerDefaultNetworkCallback(mNetworkCallback);
                } catch (Exception e) {
                    if (!Sipdroid.release) android.util.Log.e("SipUA", "registerDefaultNetworkCallback failed", e);
                }
            }
        }
        Receiver.engine(this).isRegistered();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         RtpStreamReceiver.restoreSettings();
         Receiver.alarm(10*60, OneShotAlarm2.class);
         return START_STICKY;
    }

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
}
