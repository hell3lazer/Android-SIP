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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.sipdroid.codecs.Codecs;
import org.sipdroid.media.RtpStreamReceiver;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.SipdroidEngine;
import org.zoolu.sip.provider.SipStack;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnClickListener {
	// Current settings handler
	private static SharedPreferences settings;
	// Context definition
	private Settings context = null;

	// Path where is stored the shared preference file - !!!should be replaced by some system variable!!!
	public static String sharedPrefsPath = "/data/data/com.androidsip.app/shared_prefs/";
	// Shared preference file name - !!!should be replaced by some system variable!!!
	private final String sharedPrefsFile = "com.androidsip.app_preferences";
	// List of profile files available on the SD card
	private String[] profileFiles = null;

	// IDs of the menu items
	private static final int MENU_IMPORT = 0;
	private static final int MENU_EXPORT = 1;
	private static final int MENU_ABOUT  = 2;

	private static final int REQUEST_CODE_IMPORT = 1001;
	private static final int REQUEST_CODE_EXPORT = 1002;

	// All possible values of the PREF_PREF preference (see bellow) 
	public static final String VAL_PREF_PSTN = "PSTN";
	public static final String VAL_PREF_SIP = "SIP";
	public static final String VAL_PREF_SIPONLY = "SIPONLY";
	public static final String VAL_PREF_ASK = "ASK";

	/*-
	 * ****************************************
	 * **** HOW TO USE SHARED PREFERENCES *****
	 * ****************************************
	 * 
	 * If you need to check the existence of the preference key
	 *   in this class:		contains(PREF_USERNAME)
	 *   in other classes:	PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).contains(Settings.PREF_USERNAME) 
	 * If you need to check the existence of the key or check the value of the preference
	 *   in this class:		getString(PREF_USERNAME, "").equals("")
	 *   in other classes:	PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(Settings.PREF_USERNAME, "").equals("")
	 * If you need to get the value of the preference
	 *   in this class:		getString(PREF_USERNAME, DEFAULT_USERNAME)
	 *   in other classes:	PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(Settings.PREF_USERNAME, Settings.DEFAULT_USERNAME)
	 */

	// Name of the keys in the Preferences XML file
	public static final String PREF_USERNAME = "username";
	public static final String PREF_PASSWORD = "password";
	public static final String PREF_SERVER = "server";
	public static final String PREF_DOMAIN = "domain";
	public static final String PREF_FROMUSER = "fromuser";
	public static final String PREF_PORT = "port";
	public static final String PREF_PROTOCOL = "protocol";
	public static final String PREF_WLAN = "wlan";
	public static final String PREF_3G = "3g";
	public static final String PREF_4G = "4g";
	public static final String PREF_NOTRAIN = "notrain";
	public static final String PREF_VPN = "vpn";
	public static final String PREF_PREF = "pref";
	public static final String PREF_AUTO_ON = "auto_on";
	public static final String PREF_AUTO_ONDEMAND = "auto_on_demand";
	public static final String PREF_AUTO_HEADSET = "auto_headset";
	public static final String PREF_MWI_ENABLED = "MWI_enabled";
	public static final String PREF_NODATA = "nodata";
	public static final String PREF_SIPRINGTONE = "sipringtone";
	public static final String PREF_SEARCH = "search";
	public static final String PREF_EXCLUDEPAT = "excludepat";
	public static final String PREF_STUN = "stun";
	public static final String PREF_STUN_SERVER = "stun_server";
	public static final String PREF_STUN_SERVER_PORT = "stun_server_port";
	
	// MMTel configurations (added by mandrajg)
	public static final String PREF_MMTEL = "mmtel";
	public static final String PREF_MMTEL_QVALUE = "mmtel_qvalue";
	
	// Call recording preferences.
	public static final String PREF_CALLRECORD = "callrecord";
	
	public static final String PREF_PAR = "par";
	public static final String PREF_IMPROVE = "improve";
	public static final String PREF_POSURL = "posurl";
	public static final String PREF_CALLBACK = "callback";
	public static final String PREF_CALLTHRU = "callthru";
	public static final String PREF_CALLTHRU2 = "callthru2";
	public static final String PREF_CODECS = "codecs_new";
	public static final String PREF_DNS = "dns";
	public static final String PREF_MESSAGE = "vmessage";
	public static final String PREF_BLUETOOTH = "bluetooth";
	public static final String PREF_KEEPON = "keepon";
	public static final String PREF_ACCOUNT = "account";
	
	// Default values of the preferences
	public static final String	DEFAULT_USERNAME = "";
	public static final String	DEFAULT_PASSWORD = "";
	public static final String	DEFAULT_SERVER = "";
	public static final String	DEFAULT_DOMAIN = "";
	public static final String	DEFAULT_FROMUSER = "";
	public static final String	DEFAULT_PORT = "" + SipStack.default_port;
	public static final String	DEFAULT_PROTOCOL = "udp";
	public static final boolean	DEFAULT_WLAN = true;
	public static final boolean	DEFAULT_3G = false;
	public static final boolean	DEFAULT_4G = false;
	public static final boolean DEFAULT_NOTRAIN = false;
	public static final boolean	DEFAULT_VPN = false;
	public static final String	DEFAULT_PREF = VAL_PREF_SIP;
	public static final boolean	DEFAULT_AUTO_ON = false;
	public static final boolean	DEFAULT_AUTO_ONDEMAND = false;
	public static final boolean	DEFAULT_AUTO_HEADSET = false;
	public static final boolean	DEFAULT_MWI_ENABLED = true;
	public static final boolean DEFAULT_REGISTRATION = true;
	public static final boolean	DEFAULT_NOTIFY = false;
	public static final boolean	DEFAULT_NODATA = false;
	public static final String	DEFAULT_SIPRINGTONE = "";
	public static final String	DEFAULT_SEARCH = "";
	public static final String	DEFAULT_EXCLUDEPAT = "";
	public static final boolean	DEFAULT_STUN = false;
	public static final String	DEFAULT_STUN_SERVER = "stun.ekiga.net";
	public static final String	DEFAULT_STUN_SERVER_PORT = "3478";
	
	// MMTel configuration (added by mandrajg)
	public static final boolean	DEFAULT_MMTEL = false;
	public static final String	DEFAULT_MMTEL_QVALUE = "1.00";	

	// Call recording preferences.
	public static final boolean DEFAULT_CALLRECORD = false;
	
	public static final boolean	DEFAULT_PAR = false;
	public static final boolean	DEFAULT_IMPROVE = false;
	public static final String	DEFAULT_POSURL = "";
	public static final boolean	DEFAULT_CALLBACK = false;
	public static final boolean	DEFAULT_CALLTHRU = false;
	public static final String	DEFAULT_CALLTHRU2 = "";
	public static final String	DEFAULT_CODECS = null;
	public static final String	DEFAULT_DNS = "";
	public static final boolean DEFAULT_MESSAGE = false;
	public static final boolean DEFAULT_BLUETOOTH = false;
	public static final boolean DEFAULT_KEEPON = false;
	public static final int     DEFAULT_ACCOUNT = 0;

	// An other preference keys (not in the Preferences XML file)
	public static final String PREF_OLDVALID = "oldvalid";
	public static final String PREF_SETMODE = "setmode";
	public static final String PREF_OLDVIBRATE = "oldvibrate";
	public static final String PREF_OLDVIBRATE2 = "oldvibrate2";
	public static final String PREF_OLDPOLICY = "oldpolicy";
	public static final String PREF_OLDRING = "oldring";
	public static final String PREF_AUTO_DEMAND = "auto_demand";
	public static final String PREF_WIFI_DISABLED = "wifi_disabled";
	public static final String PREF_ON_VPN = "on_vpn";
	public static final String PREF_NODEFAULT = "nodefault";
	public static final String PREF_NOPORT = "noport";
	public static final String PREF_ON = "on";
	public static final String PREF_PREFIX = "prefix";
	public static final String PREF_COMPRESSION = "compression";
	//public static final String PREF_RINGMODEx = "ringmodeX";
	//public static final String PREF_VOLUMEx = "volumeX";

	// Default values of the other preferences
	public static final boolean	DEFAULT_OLDVALID = false;
	public static final boolean	DEFAULT_SETMODE = false;
	public static final int		DEFAULT_OLDVIBRATE = 0;
	public static final int		DEFAULT_OLDVIBRATE2 = 0;
	public static final int		DEFAULT_OLDPOLICY = 0;
	public static final int		DEFAULT_OLDRING = 0;
	public static final boolean	DEFAULT_AUTO_DEMAND = false;
	public static final boolean	DEFAULT_WIFI_DISABLED = false;
	public static final boolean DEFAULT_ON_VPN = false;
	public static final boolean	DEFAULT_NODEFAULT = false;
	public static final boolean DEFAULT_NOPORT = false;
	public static final boolean	DEFAULT_ON = false;
	public static final String	DEFAULT_PREFIX = "";
	public static final String	DEFAULT_COMPRESSION = null;
	//public static final String	DEFAULT_RINGTONEx = "";
	//public static final String	DEFAULT_VOLUMEx = "";

	public static Settings instance;

	public void onCreate(Bundle savedInstanceState) {
		String themePref = PreferenceManager.getDefaultSharedPreferences(this).getString("app_theme", "-1");
        if ("1".equals(themePref)) {
            setTheme(android.R.style.Theme_DeviceDefault_Light);
        } else if ("2".equals(themePref)) {
            setTheme(android.R.style.Theme_DeviceDefault);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setTheme(android.R.style.Theme_DeviceDefault_DayNight);
            } else {
                setTheme(android.R.style.Theme_DeviceDefault);
            }
        }
		super.onCreate(savedInstanceState);
        
    	if (Receiver.mContext == null) Receiver.mContext = this;
		addPreferencesFromResource(R.xml.preferences);
		
		android.widget.ListView listView = getListView();
		android.view.ViewGroup parent = (android.view.ViewGroup) listView.getParent();
		int index = parent.indexOfChild(listView);
		parent.removeView(listView);
		
		final androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeLayout = new androidx.swiperefreshlayout.widget.SwipeRefreshLayout(this);
		swipeLayout.addView(listView, new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		swipeLayout.setOnRefreshListener(new androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				new Thread(() -> {
					try {
						Receiver.engine(Settings.this).halt();
						Receiver.engine(Settings.this).StartEngine();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						runOnUiThread(() -> swipeLayout.setRefreshing(false));
					}
				}).start();
			}
		});
		parent.addView(swipeLayout, index, new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));

		setDefaultValues();
		if (Build.VERSION.SDK_INT >= 24) {
			SettingsNew.ignoreBattery(this);
		}
		if (Build.VERSION.SDK_INT >= 23) {
			requestPermissions(new String[]{
				android.Manifest.permission.RECORD_AUDIO,
				android.Manifest.permission.USE_SIP,
				android.Manifest.permission.CALL_PHONE,
				android.Manifest.permission.READ_PHONE_STATE,
				android.Manifest.permission.READ_CONTACTS,
				"android.permission.POST_NOTIFICATIONS"
			}, 1);
		}
	}
	
	void reload() {
		setPreferenceScreen(null);
		addPreferencesFromResource(R.xml.preferences);		
	}

	private void setDefaultValues() {
		settings = getSharedPreferences(sharedPrefsFile, MODE_MULTI_PROCESS);

		for (int i = 0; i < SipdroidEngine.LINES; i++) {
			String j = (i!=0?""+i:"");
			if (!settings.contains(PREF_SERVER+j)) {
				if (getPreferenceScreen() != null) {
					CheckBoxPreference cb = (CheckBoxPreference) getPreferenceScreen().findPreference(PREF_WLAN+j);
					if (cb != null) cb.setChecked(true);
				}
				Editor edit = settings.edit();

				edit.putString(PREF_PORT+j, DEFAULT_PORT);
				edit.putString(PREF_SERVER+j, DEFAULT_SERVER);
				edit.putString(PREF_PREF+j, DEFAULT_PREF);				
				edit.putString(PREF_PROTOCOL+j, DEFAULT_PROTOCOL);
				edit.commit();
	        	Receiver.engine(this).updateDNS();
	        	reload();
			}
		}
		if (settings.getString(PREF_STUN_SERVER, "").equals("")) {
			Editor edit = settings.edit();

			edit.putString(PREF_STUN_SERVER, DEFAULT_STUN_SERVER);
			edit.putString(PREF_STUN_SERVER_PORT, DEFAULT_STUN_SERVER_PORT);				
			edit.commit();
			reload();
		}

		if (! settings.contains(PREF_MWI_ENABLED)) {
			if (getPreferenceScreen() != null) {
				CheckBoxPreference cb = (CheckBoxPreference) getPreferenceScreen().findPreference(PREF_MWI_ENABLED);
				if (cb != null) cb.setChecked(true);
			}
		}

		settings.registerOnSharedPreferenceChangeListener(this);

		updateSummaries();		
		Codecs.check();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, MENU_IMPORT, 0, getString(R.string.settings_profile_menu_import)).setIcon(R.drawable.ic_upload_24);
	    menu.add(0, MENU_EXPORT, 0, getString(R.string.settings_profile_menu_export)).setIcon(R.drawable.ic_save_24);

	    menu.add(0, MENU_ABOUT, 0, getString(R.string.menu_about)).setIcon(R.drawable.ic_info_24);
	    return true;
    }

    @TargetApi(23)
	public boolean onOptionsItemSelected(MenuItem item) {
    	context = this;

    	switch (item.getItemId()) {
            case MENU_IMPORT:            	
				android.content.Intent intentImport = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
				intentImport.addCategory(android.content.Intent.CATEGORY_OPENABLE);
				intentImport.setType("*/*");
				startActivityForResult(intentImport, REQUEST_CODE_IMPORT);
                return true;
                
            case MENU_EXPORT:
				android.content.Intent intentExport = new android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT);
				intentExport.addCategory(android.content.Intent.CATEGORY_OPENABLE);
				intentExport.setType("application/xml");
				intentExport.putExtra(android.content.Intent.EXTRA_TITLE, "sipdroid_settings_backup.xml");
				startActivityForResult(intentExport, REQUEST_CODE_EXPORT);
            	return true;
                
    		case MENU_ABOUT:
    			new AlertDialog.Builder(this)
    			.setMessage(getString(R.string.about).replace("\\n","\n"))
    			.setTitle(getString(R.string.menu_about))
    			.setIcon(R.drawable.icon)
    			.setCancelable(true)
    			.show();
    			break;
        }

        return false;
    }

    public static String[] getProfileList() {
    	File dir = new File(sharedPrefsPath);
    	return dir.list();
    }

    private String getProfileNameString() {
    	return getProfileNameString(settings);
    }

    public static String getProfileNameString(SharedPreferences s) {
    	String provider = s.getString(PREF_SERVER, DEFAULT_SERVER);

    	if (! s.getString(PREF_DOMAIN, "").equals("")) {
    		provider = s.getString(PREF_DOMAIN, DEFAULT_DOMAIN);
    	}

    	return s.getString(PREF_USERNAME, DEFAULT_USERNAME) + "@" + provider;
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && data != null) {
			android.net.Uri uri = data.getData();
			if (uri == null) return;

			if (requestCode == REQUEST_CODE_EXPORT) {
				try (java.io.OutputStream os = getContentResolver().openOutputStream(uri)) {
					org.xmlpull.v1.XmlSerializer serializer = android.util.Xml.newSerializer();
					serializer.setOutput(os, "UTF-8");
					serializer.startDocument(null, true);
					serializer.startTag(null, "map");
					
					java.util.Map<String, ?> allEntries = settings.getAll();
					for (java.util.Map.Entry<String, ?> entry : allEntries.entrySet()) {
						String key = entry.getKey();
						Object value = entry.getValue();
						if (value instanceof String) {
							serializer.startTag(null, "string");
							serializer.attribute(null, "name", key);
							serializer.text((String) value);
							serializer.endTag(null, "string");
						} else if (value instanceof Boolean) {
							serializer.startTag(null, "boolean");
							serializer.attribute(null, "name", key);
							serializer.attribute(null, "value", value.toString());
							serializer.endTag(null, "boolean");
						} else if (value instanceof Integer) {
							serializer.startTag(null, "int");
							serializer.attribute(null, "name", key);
							serializer.attribute(null, "value", value.toString());
							serializer.endTag(null, "int");
						} else if (value instanceof Long) {
							serializer.startTag(null, "long");
							serializer.attribute(null, "name", key);
							serializer.attribute(null, "value", value.toString());
							serializer.endTag(null, "long");
						} else if (value instanceof Float) {
							serializer.startTag(null, "float");
							serializer.attribute(null, "name", key);
							serializer.attribute(null, "value", value.toString());
							serializer.endTag(null, "float");
						}
					}
					serializer.endTag(null, "map");
					serializer.endDocument();
					serializer.flush();
					Toast.makeText(this, "Settings exported successfully", Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(this, "Error exporting settings", Toast.LENGTH_SHORT).show();
				}
			} else if (requestCode == REQUEST_CODE_IMPORT) {
				try (java.io.InputStream is = getContentResolver().openInputStream(uri)) {
					org.xmlpull.v1.XmlPullParserFactory factory = org.xmlpull.v1.XmlPullParserFactory.newInstance();
					org.xmlpull.v1.XmlPullParser parser = factory.newPullParser();
					parser.setInput(is, null);
					
					android.content.SharedPreferences.Editor editor = settings.edit();
					editor.clear();
					
					int eventType = parser.getEventType();
					while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
						if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG) {
							String name = parser.getAttributeValue(null, "name");
							if (name != null) {
								String tag = parser.getName();
								if (tag.equals("string")) {
									editor.putString(name, parser.nextText());
								} else if (tag.equals("boolean")) {
									editor.putBoolean(name, Boolean.parseBoolean(parser.getAttributeValue(null, "value")));
								} else if (tag.equals("int")) {
									editor.putInt(name, Integer.parseInt(parser.getAttributeValue(null, "value")));
								} else if (tag.equals("long")) {
									editor.putLong(name, Long.parseLong(parser.getAttributeValue(null, "value")));
								} else if (tag.equals("float")) {
									editor.putFloat(name, Float.parseFloat(parser.getAttributeValue(null, "value")));
								}
							}
						}
						eventType = parser.next();
					}
					settings.unregisterOnSharedPreferenceChangeListener(context);
					editor.commit();
					
					Toast.makeText(this, "Settings imported successfully", Toast.LENGTH_SHORT).show();
					setDefaultValues();
					Receiver.engine(context).halt();
					Receiver.engine(context).StartEngine();
					reload();
					settings.registerOnSharedPreferenceChangeListener(context);
					updateSummaries();

				} catch (Exception e) {
					e.printStackTrace();
					java.io.StringWriter sw = new java.io.StringWriter();
					e.printStackTrace(new java.io.PrintWriter(sw));
					final String stackTrace = sw.toString();
					
					new android.app.AlertDialog.Builder(this)
						.setTitle("Import Error")
						.setMessage(stackTrace)
						.setPositiveButton("Copy", new android.content.DialogInterface.OnClickListener() {
							public void onClick(android.content.DialogInterface dialog, int which) {
								android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
								android.content.ClipData clip = android.content.ClipData.newPlainText("Error", stackTrace);
								clipboard.setPrimaryClip(clip);
								Toast.makeText(Settings.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
							}
						})
						.setNegativeButton("Close", null)
						.show();
				}
			}
		}
	}

	private OnClickListener profileOnClick = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichItem) {
			boolean message = settings.getBoolean(PREF_MESSAGE, DEFAULT_MESSAGE);

			try {
				copyFile(new File(sharedPrefsPath + profileFiles[whichItem]), new File(sharedPrefsPath + sharedPrefsFile + ".xml"));
            } catch (Exception e) {
                Toast.makeText(context, getString(R.string.settings_profile_import_error), Toast.LENGTH_SHORT).show();
                return;
            }

   			settings.unregisterOnSharedPreferenceChangeListener(context);
   			setDefaultValues();

           	// Restart the engine
       		Receiver.engine(context).halt();
   			Receiver.engine(context).StartEngine();
   			
   			reload();
   			settings.registerOnSharedPreferenceChangeListener(context);
   			updateSummaries();
   			if (message) {
   	    		Editor edit = settings.edit();
   	    		edit.putBoolean(PREF_MESSAGE, true);
   	    		edit.commit();
   			}
		}
	};



    public void copyFile(File in, File out) throws Exception {
        FileInputStream  fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        try {
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (fis != null) fis.close();
            if (fos != null) fos.close();
        }
    }

	@Override
	public void onDestroy()	{
		super.onDestroy();

		settings.unregisterOnSharedPreferenceChangeListener(this);
	}

	EditText transferText;
	String mKey;

    @TargetApi(23)
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	if (!Thread.currentThread().getName().equals("main") || key == null)
    		return;
        	
        if ("app_theme".equals(key)) {
            recreate();
            return;
        }

		if (key.startsWith(PREF_PORT) && sharedPreferences.getString(key, DEFAULT_PORT).equals("0")) {
	   		Editor edit = sharedPreferences.edit();
    		edit.putString(key, DEFAULT_PORT);
    		edit.commit();

    		transferText = new InstantAutoCompleteTextView(this,null);
			transferText.setInputType(InputType.TYPE_CLASS_NUMBER);
			mKey = key;

			new AlertDialog.Builder(this)
			.setTitle(Receiver.mContext.getString(R.string.settings_port))
			.setView(transferText)
			.setPositiveButton(android.R.string.ok, this)
			.show();
			return;
		} else if (key.startsWith(PREF_SERVER) || key.startsWith(PREF_PROTOCOL)) {
    		Editor edit = sharedPreferences.edit();
    		for (int i = 0; i < SipdroidEngine.LINES; i++) {
    			edit.putString(PREF_DNS+i, DEFAULT_DNS);
    		}
    		edit.commit();
        	new Thread(() -> {
				try {
        	    	Receiver.engine(Settings.this).halt();
    		    	Receiver.engine(Settings.this).StartEngine();
        	    	Receiver.engine(Settings.this).updateDNS();
        	    	Checkin.checkin(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}).start();

	    } else if (key.startsWith(PREF_WLAN) ||
        			key.startsWith(PREF_3G) ||
        			key.startsWith(PREF_4G) ||
        			key.startsWith(PREF_USERNAME) ||
        			key.startsWith(PREF_PASSWORD) ||
        			key.startsWith(PREF_DOMAIN) ||
        			key.startsWith(PREF_SERVER) ||
        			key.startsWith(PREF_PORT) ||
        			key.equals(PREF_STUN) ||
        			key.equals(PREF_STUN_SERVER) ||
        			key.equals(PREF_STUN_SERVER_PORT) ||
        			key.equals(PREF_MMTEL) ||			// (added by mandrajg)
        			key.equals(PREF_MMTEL_QVALUE) ||	// (added by mandrajg)
        			key.startsWith(PREF_PROTOCOL) ||
        			key.startsWith(PREF_VPN) ||
        			key.equals(PREF_POSURL) ||
        			key.startsWith(PREF_FROMUSER) ||
        			key.equals(PREF_AUTO_ONDEMAND) ||
        			key.equals(PREF_MWI_ENABLED) ||
        			key.equals(PREF_KEEPON)) {
        	new Thread(() -> {
				try {
        	    	Receiver.engine(Settings.this).halt();
    		    	Receiver.engine(Settings.this).StartEngine();
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}).start();
		}
		updateSummaries();
    }

	void fill(String pref,String def,int val,int disp) {
		if (getPreferenceScreen() == null) return;
		Preference p = getPreferenceScreen().findPreference(pref);
		if (p == null) return;
    	for (int i = 0; i < getResources().getStringArray(val).length; i++) {
        	if (settings.getString(pref, def).equals(getResources().getStringArray(val)[i])) {
        		p.setSummary(getResources().getStringArray(disp)[i]);
        	}
    	}
    }

	public void updateSummaries() {
		if (getPreferenceScreen() == null) return;
		Preference pStunServer = getPreferenceScreen().findPreference(PREF_STUN_SERVER);
		if (pStunServer != null) pStunServer.setSummary(settings.getString(PREF_STUN_SERVER, DEFAULT_STUN_SERVER));
		
		Preference pStunServerPort = getPreferenceScreen().findPreference(PREF_STUN_SERVER_PORT);
		if (pStunServerPort != null) pStunServerPort.setSummary(settings.getString(PREF_STUN_SERVER_PORT, DEFAULT_STUN_SERVER_PORT));

       	// MMTel settings (added by mandrajg)
		Preference pMmtelQvalue = getPreferenceScreen().findPreference(PREF_MMTEL_QVALUE);
       	if (pMmtelQvalue != null) pMmtelQvalue.setSummary(settings.getString(PREF_MMTEL_QVALUE, DEFAULT_MMTEL_QVALUE));	
    	
       	for (int i = 0; i < SipdroidEngine.LINES; i++) {
       		String j = (i!=0?""+i:"");
       		String username = settings.getString(PREF_USERNAME+j, DEFAULT_USERNAME),
       			server = settings.getString(PREF_SERVER+j, DEFAULT_SERVER);
       		Preference pUsername = getPreferenceScreen().findPreference(PREF_USERNAME+j);
       		if (pUsername != null) pUsername.setSummary(username);
       		Preference pServer = getPreferenceScreen().findPreference(PREF_SERVER+j);
       		if (pServer != null) pServer.setSummary(server);
       		Preference pDomain = getPreferenceScreen().findPreference(PREF_DOMAIN+j);
       		if (pDomain != null) {
		    	if (settings.getString(PREF_DOMAIN+j, DEFAULT_DOMAIN).length() == 0) {
		    		pDomain.setSummary(getString(R.string.settings_domain2));
		    	} else {
		    		pDomain.setSummary(settings.getString(PREF_DOMAIN+j, DEFAULT_DOMAIN));
		    	}
       		}
       		Preference pCallerId = getPreferenceScreen().findPreference(PREF_FROMUSER+j);
       		if (pCallerId != null) {
		    	if (settings.getString(PREF_FROMUSER+j,DEFAULT_FROMUSER).length() == 0) {
		    		pCallerId.setSummary(getString(R.string.settings_callerid2));
		    	} else {
		    		pCallerId.setSummary(settings.getString(PREF_FROMUSER+j, DEFAULT_FROMUSER));
		    	}
       		}
	    	fill(PREF_PORT+j,DEFAULT_PORT,R.array.port_values2,R.array.port_values2);
	    	fill(PREF_PROTOCOL+j,DEFAULT_PROTOCOL,R.array.protocol_values2,R.array.protocol_display_values2);
	    	Preference pAccount = getPreferenceScreen().findPreference(PREF_ACCOUNT+j);
	    	if (pAccount != null) {
				pAccount.setSummary(username.equals("")||server.equals("")?"Not Configured":username+"@"+server);
				boolean isReg = false;
				org.sipdroid.sipua.SipdroidEngine engine = Receiver.engine(this);
				if (engine != null && engine.ras != null && engine.ras[i] != null) {
					isReg = (engine.ras[i].CurrentState == org.sipdroid.sipua.RegisterAgent.REGISTERED);
				}
				pAccount.setIcon(isReg ? R.drawable.ic_connected_modern : R.drawable.ic_disconnected_modern);
			}
       	}

		if (getPreferenceScreen().getRootAdapter() != null && getPreferenceScreen().getRootAdapter() instanceof android.widget.BaseAdapter) {
			((android.widget.BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
		}
       	
		Preference pSearch = getPreferenceScreen().findPreference(PREF_SEARCH);
    	if (pSearch != null) pSearch.setSummary(settings.getString(PREF_SEARCH, DEFAULT_SEARCH)); 
		
		Preference pExcludePat = getPreferenceScreen().findPreference(PREF_EXCLUDEPAT);
    	if (pExcludePat != null) pExcludePat.setSummary(settings.getString(PREF_EXCLUDEPAT, DEFAULT_EXCLUDEPAT)); 
		
    	if (settings.getBoolean(PREF_STUN, DEFAULT_STUN)) {
			if (pStunServer != null) pStunServer.setEnabled(true);
    		if (pStunServerPort != null) pStunServerPort.setEnabled(true);
    	} else {
    		if (pStunServer != null) pStunServer.setEnabled(false);
    		if (pStunServerPort != null) pStunServerPort.setEnabled(false);       	
    	}
    	
    	// MMTel configuration (added by mandrajg)
    	if (settings.getBoolean(PREF_MMTEL, DEFAULT_MMTEL)) {
    		if (pMmtelQvalue != null) pMmtelQvalue.setEnabled(true);
    	} else {
    		if (pMmtelQvalue != null) pMmtelQvalue.setEnabled(false);       	
    	}
    }

    @Override
	public void onClick(DialogInterface arg0, int arg1) {
		Editor edit = settings.edit();
 		edit.putString(mKey, transferText.getText().toString());
		edit.commit();
	}
}


