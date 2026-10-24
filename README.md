# Android SIP (Modernized Sipdroid Fork)

Android SIP is an open-source SIP client for Android. This fork modernizes the historic Sipdroid application with updated user interfaces, dynamic theme support, and quality-of-life improvements while retaining its robust SIP core.

## ✨ Features & Changes in this Fork

- **Native Telecom Integration:** The outdated custom dialer and call screens have been fully removed. Android SIP now seamlessly integrates with the native Android Telecom framework. Your SIP account acts as a native calling account in the system dialer, providing a 100% native call experience.
- **Material Design 3 Upgrade:** The application UI has been fully revamped using Material Design 3 principles, including updated padding, sizing, and iconography.
- **Smart Background Services:** The SIP engine intelligently detects if battery optimizations are ignored, and if so, runs completely invisibly in the background without any persistent notification cluttering your status bar.
- **Intelligent Account Management:** When the SIP engine loses connection to the server, the native calling account capabilities are dynamically toggled to gracefully hide it from your system dialer options without losing your permanent enabled permissions.
- **Modern Cloud Backups:** Replaced the obsolete SD-card text backup system with standard Android Storage Access Framework (SAF) `XML` backups, allowing seamless import/export of configuration to any cloud drive or local folder.
- **Cleaner Notifications:** Ongoing call notifications now display a clean, readable Caller ID rather than leaking the raw SIP connection URIs, and the SIP account settings have been refined for better visibility.

## 🚀 Release Notes - Version 1.1

- **Bidirectional RTP Media Fix:** Resolved socket address locking in `RtpSocket` that previously threw `IllegalArgumentException: connected address and packet address differ` and prevented outbound audio from reaching the carrier SBC.
- **Smooth Audio Playback:** Removed synchronous disk writes (`edit.commit()`) and volume polling inside the high-priority RTP audio receiver loop, eliminating `AudioTrack` buffer underruns and audio stutter.
- **Native Call Audio Routing:** Streamlined audio route transitions between earpiece, speakerphone, and Bluetooth headset during active calls without destroying or recreating `AudioTrack`.
- **Modern Android Telecom Audio Mode:** Configured Telecom Connection to operate in native cellular mode (`setAudioModeIsVoip(false)`), allowing Samsung and Android audio policy managers to manage voice call routing seamlessly.
- **Automatic E.164 Phone Formatting:** Integrated automatic country-code formatting via `PhoneNumberUtils.formatNumberToE164` for dialing local contact numbers.
- **Mid-Call SIP Stability:** Suppressed redundant SIP re-registrations caused by radio capability/signal fluctuations during active calls.
- **Private Release Key:** Signed with a newly generated private release keystore. *Note: users upgrading from earlier development or debug builds must perform a one-time uninstall before installing.*

### ⚠️ Known Issues
- **INVITE Retransmissions During Extended Ringing:** When an outbound call is challenged with `407 Proxy Authentication`, retransmitted authenticated INVITEs may repeat at 4-second intervals during ringing until answered or terminated.

## 🙌 Credits & Acknowledgments

This fork is built upon the phenomenal work of the original Sipdroid developers and contributors. All original rights and credits belong to:

- **The Sipdroid Open Source Project** (http://sipdroid.org) - Copyright (C) 2009
- **Hughes Systique Corporation, USA** (http://hsc.com) - Copyright (C) 2008 *(Initial port and development of a Test SIP Client to Google's Android Platform)*
- **The Android Open Source Project** (http://android.com) - Copyright (C) 2006
- **Luca Veltri, University of Parma, Italy** (http://mjsip.org) - Copyright (C) 2005 *(Mjsip original stack)*

*Welcome to the growing worldwide community of Sipdroid users and developers!*

## 📜 License

This project is licensed under the GNU General Public License v3 (or later). 
Please see the source files for complete license details and copyright headers.
