package org.sipdroid.sipua.ui;

import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.util.Log;

public class SipConnection extends Connection {
    private static final String TAG = "SipConnection";

    public SipConnection() {
        super();
        setAudioModeIsVoip(true);
    }

    @Override
    public void onAnswer() {
        Log.d(TAG, "onAnswer() called");
        super.onAnswer();
        if (Receiver.mContext != null) {
            Receiver.engine(Receiver.mContext).answercall();
        }
        setActive();
    }

    @Override
    public void onReject() {
        Log.d(TAG, "onReject() called");
        super.onReject();
        if (Receiver.mContext != null) {
            Receiver.engine(Receiver.mContext).rejectcall();
        }
        setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
        destroy();
    }

    @Override
    public void onDisconnect() {
        Log.d(TAG, "onDisconnect() called");
        super.onDisconnect();
        if (Receiver.mContext != null) {
            Receiver.engine(Receiver.mContext).rejectcall(); // rejectcall() is also used to hang up active calls in Sipdroid
        }
        setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        destroy();
    }

    @Override
    public void onAbort() {
        Log.d(TAG, "onAbort() called");
        super.onAbort();
        if (Receiver.mContext != null) {
            Receiver.engine(Receiver.mContext).rejectcall();
        }
        setDisconnected(new DisconnectCause(DisconnectCause.CANCELED));
        destroy();
    }

    @Override
    public void onHold() {
        Log.d(TAG, "onHold() called");
        super.onHold();
        if (Receiver.mContext != null) {
            Receiver.engine(Receiver.mContext).togglehold();
        }
        setOnHold();
    }

    @Override
    public void onUnhold() {
        Log.d(TAG, "onUnhold() called");
        super.onUnhold();
        if (Receiver.mContext != null) {
            Receiver.engine(Receiver.mContext).togglehold();
        }
        setActive();
    }

    @Override
    public void onPlayDtmfTone(char c) {
        Log.d(TAG, "onPlayDtmfTone() called with: " + c);
        super.onPlayDtmfTone(c);
        if (Receiver.mContext != null) {
            Receiver.engine(Receiver.mContext).info(c, 250);
        }
    }
}
