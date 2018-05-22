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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EEGCapture secondApp;
    private Button              next_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.secondApp = (EEGCapture) getApplicationContext();

        TextView top_textview       = (TextView) findViewById(R.id.second_app_init_tv);
        TextView btstatus_textview  = (TextView) findViewById(R.id.bt_status_tv);
        TextView nsmstatus_textview = (TextView) findViewById(R.id.nsm_status_tv);

        try {
            top_textview.setText(R.string.second_app_init_activity);
            btstatus_textview.setText(R.string.bluetooth_required);
            nsmstatus_textview.setText(R.string.neurosky_required);

            this.next_button = (Button) findViewById(R.id.next_button);

            secondApp.initialize();

            btstatus_textview.setText( secondApp.getBtStatus() );
            nsmstatus_textview.setText( secondApp.getNeuroskyAvailability() );

            this.next_button.setEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void toTraining(View view) {

        if (this.secondApp.getNeuroskyConnected() && this.secondApp.getNeuroskyWorking()) {
            this.secondApp.setTotalCapturePhases( Integer.valueOf( ((EditText) findViewById(R.id.trial_count_te)).getText().toString()) );
            Intent intent = new Intent( this, SecondActivity.class );
            startActivity(intent);
        }
        else {
            TextView nsmactive_textview = (TextView) findViewById(R.id.nsm_active_tv);

            nsmactive_textview.setText(getString(R.string.neurosky_required));

        }

    }
}
