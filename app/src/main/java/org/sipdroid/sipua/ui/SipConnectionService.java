package org.sipdroid.sipua.ui;

import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.util.Log;

public class SipConnectionService extends ConnectionService {
    private static final String TAG = "SipConnectionService";

    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "onCreateIncomingConnection");
        SipConnection connection = new SipConnection();
        connection.setConnectionCapabilities(Connection.CAPABILITY_MUTE | Connection.CAPABILITY_SUPPORT_HOLD | Connection.CAPABILITY_HOLD);
        android.net.Uri address = request.getAddress();
        if (address == null && request.getExtras() != null) {
            address = request.getExtras().getParcelable(android.telecom.TelecomManager.EXTRA_INCOMING_CALL_ADDRESS);
        }
        if (address != null) {
            connection.setAddress(address, android.telecom.TelecomManager.PRESENTATION_ALLOWED);
            String number = address.getSchemeSpecificPart();
            if (number != null && !number.isEmpty()) {
                connection.setCallerDisplayName(number, android.telecom.TelecomManager.PRESENTATION_ALLOWED);
            }
        }
        connection.setRinging();
        Receiver.activeConnection = connection;
        Receiver.startCallForeground();
        return connection;
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "onCreateOutgoingConnection");
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "RECORD_AUDIO permission not granted");
            return Connection.createFailedConnection(new android.telecom.DisconnectCause(android.telecom.DisconnectCause.ERROR, "Microphone permission required"));
        }
        SipConnection connection = new SipConnection();
        connection.setConnectionCapabilities(Connection.CAPABILITY_MUTE | Connection.CAPABILITY_SUPPORT_HOLD | Connection.CAPABILITY_HOLD);
        connection.setAddress(request.getAddress(), android.telecom.TelecomManager.PRESENTATION_ALLOWED);
        connection.setDialing();
        Receiver.activeConnection = connection;
        Receiver.startCallForeground();

        connection.setAudioModeIsVoip(true);
        if (request.getAddress() != null) {
            if (Receiver.call_state != org.sipdroid.sipua.UserAgent.UA_STATE_OUTGOING_CALL) {
                String number = request.getAddress().getSchemeSpecificPart();
                if ("tel".equals(request.getAddress().getScheme())) {
                    android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) getSystemService(android.content.Context.TELEPHONY_SERVICE);
                    String countryIso = tm != null ? tm.getNetworkCountryIso() : null;
                    if (countryIso == null || countryIso.isEmpty()) {
                        countryIso = tm != null ? tm.getSimCountryIso() : null;
                    }
                    if (countryIso == null || countryIso.isEmpty()) {
                        countryIso = java.util.Locale.getDefault().getCountry();
                    }
                    String formatted = null;
                    if (countryIso != null && !countryIso.isEmpty()) {
                        formatted = android.telephony.PhoneNumberUtils.formatNumberToE164(number, countryIso.toUpperCase(java.util.Locale.US));
                    }
                    if (formatted != null) {
                        number = formatted;
                    } else {
                        number = android.telephony.PhoneNumberUtils.stripSeparators(number);
                    }
                }
                final String numberToCall = number;
                new Thread(() -> {
                    org.sipdroid.sipua.SipdroidEngine engine = Receiver.engine(SipConnectionService.this);
                    if (engine == null) {
                        connection.setDisconnected(new android.telecom.DisconnectCause(android.telecom.DisconnectCause.ERROR, "SIP Offline"));
                        connection.destroy();
                        return;
                    }
                    long start = System.currentTimeMillis();
                    if (!engine.isRegistered()) {
                        engine.register();
                        while (!engine.isRegistered() && (System.currentTimeMillis() - start) < 4000) {
                            try { Thread.sleep(100); } catch (Exception ignored) {}
                        }
                    }
                    if (!engine.isRegistered()) {
                        connection.setDisconnected(new android.telecom.DisconnectCause(android.telecom.DisconnectCause.ERROR, "SIP Offline"));
                        connection.destroy();
                        return;
                    }
                    boolean success = engine.call(numberToCall, false);
                    if (!success) {
                        connection.setDisconnected(new android.telecom.DisconnectCause(android.telecom.DisconnectCause.CANCELED));
                        connection.destroy();
                    }
                }).start();
            }
        } else {
            return Connection.createFailedConnection(new android.telecom.DisconnectCause(android.telecom.DisconnectCause.ERROR, "No address provided"));
        }

        return connection;
    }

    @Override
    public void onCreateIncomingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.e(TAG, "onCreateIncomingConnectionFailed");
    }

    @Override
    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.e(TAG, "onCreateOutgoingConnectionFailed");
    }
}
