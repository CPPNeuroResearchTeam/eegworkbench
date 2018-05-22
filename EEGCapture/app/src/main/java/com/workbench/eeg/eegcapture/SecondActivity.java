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
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {

    private RelativeLayout      top_enrollview;
    private TextView            top_textview;
    private TextView            eeg_textview;
    private Button              to_report_bttn;
    private EditText            et_passthought;

    private EEGCapture secondApp;

    private int                 training_attempts;
    private boolean             training_complete;


    // only captures del/enter keys
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent ev ) {
        switch(keyCode) {
            case (KeyEvent.KEYCODE_NUMPAD_ENTER):
            case (KeyEvent.KEYCODE_ENTER):
                //Log.d("onKeyUp()", String.format("%c\n", (char) keyCode) );

                this.top_textview.setText(R.string.touch_up);

                if (!this.training_complete) {
                    //Log.d("onKeyUp()", "test1");
                    this.secondApp.toggleUserInputCapture();
                    this.training_attempts += 1;

                    this.top_textview.setText(
                            this.top_textview.getText() +
                                    String.format(" %d", this.training_attempts));

                    if (this.training_attempts >= this.secondApp.getTotalCapturePhases() ) {

                    //Log.d("onKeyUp()", "test2");

                        this.to_report_bttn.setEnabled(true);
                        this.training_complete = true;

                    }

                    this.et_passthought.setText("");

                }
                //return true;

        }
        return super.onKeyUp(keyCode, ev);

    }

    /**/
    private class CaptureTouch implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent me) {
            int action = MotionEventCompat.getActionMasked(me);
            SecondActivity.this.top_textview = (TextView) findViewById(R.id.press_status_tv);

            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    //SecondActivity.this.top_textview.setText(R.string.touch_down);

                    if (!SecondActivity.this.training_complete) {
                        SecondActivity.this.secondApp.toggleUserInputCapture();
                    }

                    break;

                case (MotionEvent.ACTION_UP):
                    //SecondActivity.this.top_textview.setText(R.string.touch_up);

                    if (!SecondActivity.this.training_complete) {
                        SecondActivity.this.secondApp.toggleUserInputCapture();
                        SecondActivity.this.training_attempts += 1;

                        SecondActivity.this.top_textview.setText(
                                        String.format(" %d/%d",
                                                SecondActivity.this.training_attempts,
                                                SecondActivity.this.secondApp.getTotalCapturePhases()));

                        if (SecondActivity.this.training_attempts >= SecondActivity.this.secondApp.getTotalCapturePhases()) {

                        SecondActivity.this.to_report_bttn.setEnabled(true);
                            SecondActivity.this.training_complete = true;

                        }

                    }

                    // return true;
                    break;
            }

            return true;

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        this.secondApp = (EEGCapture) getApplicationContext();

        this.top_enrollview = (RelativeLayout) findViewById(R.id.activity_second);
        this.top_enrollview.setOnTouchListener(new CaptureTouch());

        this.eeg_textview = (TextView) findViewById(R.id.eeg_value_tv);
        this.top_textview = (TextView) findViewById(R.id.second_app_training_tv);
        this.to_report_bttn = (Button) findViewById(R.id.next_button2);

        this.training_attempts = 0;
        this.training_complete = false;

        // initialize neurosky device/prepare to record
        this.secondApp.clearUserInputCapture();

        this.secondApp.getTgStreamReader().MWM15_getFilterType();


    }

    public void toReport(View view) {
        if (this.training_complete) {
            Intent intent = new Intent(this, CompleteActivity.class);
            startActivity(intent);
        }

    }

}
