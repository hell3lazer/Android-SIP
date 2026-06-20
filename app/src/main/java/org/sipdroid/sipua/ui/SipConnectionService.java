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
        connection.setAddress(request.getAddress(), android.telecom.TelecomManager.PRESENTATION_ALLOWED);
        connection.setCallerDisplayName("Sipdroid Call", android.telecom.TelecomManager.PRESENTATION_ALLOWED);
        connection.setRinging();
        Receiver.activeConnection = connection;
        return connection;
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "onCreateOutgoingConnection");
        SipConnection connection = new SipConnection();
        connection.setConnectionCapabilities(Connection.CAPABILITY_MUTE | Connection.CAPABILITY_SUPPORT_HOLD | Connection.CAPABILITY_HOLD);
        connection.setAddress(request.getAddress(), android.telecom.TelecomManager.PRESENTATION_ALLOWED);
        connection.setDialing();
        Receiver.activeConnection = connection;

        if (request.getAddress() != null) {
            if (Receiver.call_state != org.sipdroid.sipua.UserAgent.UA_STATE_OUTGOING_CALL) {
                if (Receiver.engine(this) == null || !Receiver.engine(this).isRegistered()) {
                    return Connection.createFailedConnection(new android.telecom.DisconnectCause(android.telecom.DisconnectCause.ERROR, "SIP Offline"));
                }
                String number = request.getAddress().getSchemeSpecificPart();
                boolean success = Receiver.engine(this).call(number, false);
                if (!success) {
                    return Connection.createCanceledConnection();
                }
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
