/*      This file is part of EEGCapture.
*
*       EEGCapture is free software: you can redistribute it and/or modify
*       it under the terms of the GNU General Public License as published by
*       the Free Software Foundation, either version 3 of the License, or
*       (at your option) any later version.
*
*       EEGCapture is distributed in the hope that it will be useful,
*       but WITHOUT ANY WARRANTY; without even the implied warranty of
*       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*       GNU General Public License for more details.
*
*       You should have received a copy of the GNU General Public License
*       along with EEGCapture.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.workbench.eeg.eegcapture;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import java.util.ArrayList;
import java.util.Set;

public class EEGCapture extends Application {
    // bluetooth related
    private BluetoothAdapter        btAdapter;
    private Set<BluetoothDevice>    btDevices;
    private String                  btStatus;

    // neurosky related
    private String                  neuroskyBtAddr;
    private String                  neuroskyAvailability;
    private boolean                 neuroskyConnected;
    private boolean                 neuroskyWorking;
    private TgStreamReader          tgStreamReader;

    // user auth capture related
    private int                         total_capture_phases;
    private boolean                     in_capture_phase;
    private boolean                     touch_down;
    private UserInputAuth               current_auth;
    private ArrayList<UserInputAuth>    user_authentications;

    //public static int TOTAL_CAPTURE_PHASES = 3;
    public int TOTAL_CAPTURE_PHASES = 3;


    // tgStreamHandler used to handle state/data updates from Neurosky
    private TgStreamHandler tgStreamHandler = new TgStreamHandler() {
        @Override
        public void onDataReceived(int i, int i1, Object o) {
            Message msg = linkDetectedHandler.obtainMessage();
            msg.what = i;
            msg.arg1 = i1;
            msg.obj = o;

            linkDetectedHandler.sendMessage(msg);

        }

        @Override
        public void onStatesChanged(int i) {
            //Log.d("TEST", "connectionStates change to: " + String.format("%d", i) );

            switch (i) {
                case ConnectionStates.STATE_CONNECTED:
                    EEGCapture.this.neuroskyConnected = true;
                    EEGCapture.this.tgStreamReader.start();

                    break;
                case ConnectionStates.STATE_WORKING:
                    EEGCapture.this.neuroskyWorking = true;
                    break;
                case ConnectionStates.STATE_FAILED:
                    EEGCapture.this.neuroskyConnected = false;
                    break;

            }

        }

        @Override
        public void onChecksumFail(byte[] bytes, int i, int i1) {}

        @Override
        public void onRecordFail(int i) {}
    };

    private Handler linkDetectedHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch ( msg.what ) {
                case MindDataType.CODE_EEGPOWER:
                    EEGPower power = (EEGPower) msg.obj;

                    if(power.isValidate() && EEGCapture.this.in_capture_phase ) {
                        EEGCapture.this.current_auth.updateData(
                                String.format("%d %d %d", power.theta, power.lowAlpha, power.highAlpha),
                                (float) power.theta,
                                (float) power.lowAlpha,
                                (float) power.highAlpha,
                                (float) power.lowBeta,
                                (float) power.highBeta,
                                (float) power.lowGamma,
                                (float) power.middleGamma );

                    }

                    break;
                case MindDataType.CODE_RAW:
                //case MindDataType.CODE_MEDITATION:
                //case MindDataType.CODE_ATTENTION:
                //case MindDataType.CODE_FILTER_TYPE:
                    //Log.d("handleMessage()", String.format("raw: %d", msg.arg1) );
                    if (in_capture_phase) {
                        EEGCapture.this.current_auth.updateRaw((int) msg.arg1);
                    }
                    break;
                default:
                    break;

            }

            super.handleMessage(msg);

        }

    };

    public void initialize() {
        try {
            // ensure bluetooth functionality is active and neurosky is activated
            neuroskyConnected       = false;
            neuroskyWorking         = false;
            neuroskyBtAddr          = "";
            neuroskyAvailability    = getString(R.string.neurosky_unavailable);
            btAdapter               = BluetoothAdapter.getDefaultAdapter();

            if (btAdapter == null || !btAdapter.isEnabled()) {
                this.btStatus = getString(R.string.bluetooth_required);

            }
            else {
                this.btStatus = getString(R.string.bluetooth_ready);

                // bluetooth discovery is 'heavyweight' process, cancel immediately
                // only open neurosky device that has already been paired/bonded
                if(this.btAdapter.isDiscovering()){
                    this.btAdapter.cancelDiscovery();
                }

                this.btDevices = this.btAdapter.getBondedDevices();
                if ( this.btDevices.isEmpty()) {
                    this.neuroskyAvailability = getString(R.string.neurosky_unavailable);
                }
                else {
                    for (BluetoothDevice btD : this.btDevices) {
                        if ( btD.getName().equals(getString(R.string.neurosky_mobile_bt_name) ) ) {
                            this.neuroskyBtAddr         = btD.getAddress();
                            this.neuroskyAvailability   = getString(R.string.neurosky_available);

                            // ensure neurosky device is powered on/activated by trying to connect
                            this.tgStreamReader = new TgStreamReader(
                                    this.btAdapter.getRemoteDevice(this.neuroskyBtAddr),
                                    tgStreamHandler );

                            this.tgStreamReader.startLog();

                            this.tgStreamReader.connect();

                            break;

                        }
                    }
                }

            }

            // user input auth capture
            this.total_capture_phases   = 3;
            this.touch_down             = false;
            this.in_capture_phase       = false;
            this.user_authentications   = new ArrayList<UserInputAuth>();

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public TgStreamReader getTgStreamReader() {
        return this.tgStreamReader;
    }


    public String getBtStatus() {
        return this.btStatus;
    }

    public String getNeuroskyAvailability() {
        return this.neuroskyAvailability;
    }

    public boolean getNeuroskyConnected() {
        return this.neuroskyConnected;
    }

    public boolean getNeuroskyWorking() { return this.neuroskyWorking; }

    /*public String getNeuroskyBtAddr() {
        return this.neuroskyBtAddr;
    }*/

    public void toggleUserInputCapture() {
        Log.d("toggleUICapture()", String.format("%s", ((this.touch_down)?"true":"false")) );
        if ( !this.in_capture_phase ) {
            //start input capture phase
            if ( !this.touch_down ) {
                this.current_auth       = new UserInputAuth(this.user_authentications.size() + 1);
                this.in_capture_phase   = true;
                this.touch_down         = true;

            }

        }
        else {
            //stop input capture phase
            if (this.touch_down) {
                this.user_authentications.add(this.current_auth);

                this.current_auth       = null;
                this.in_capture_phase   = false;
                this.touch_down         = false;

            }
        }

        Log.d("toggleUICapture()", String.format("%s", ((this.touch_down)?"true":"false")) );

    }

    public void clearUserInputCapture() {
        this.in_capture_phase   = false;
        this.touch_down         = false;
        this.current_auth       = null;
    }

    public ArrayList<UserInputAuth> getUserAuths() {
        return this.user_authentications;
    }

    public int getTotalCapturePhases() {
        return this.total_capture_phases;

    }

    public void setTotalCapturePhases(int p) {
        this.total_capture_phases = p;

    }

    public boolean inCapturePhase() {
        return this.in_capture_phase;
    }

}
