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

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CompleteActivity extends AppCompatActivity {
    EEGCapture secondApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        this.secondApp              = (EEGCapture) getApplicationContext();

        //export data to json file
        String external_storage_state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(external_storage_state)) {

            try {
                File json_file = File.createTempFile("eeg",
                        ".json",
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));


                JSONObject uia_set          = new JSONObject();
                String uia_set_desc         = "set description";
                JSONArray uias  = new JSONArray();

                for(UserInputAuth uia : this.secondApp.getUserAuths() ) {
                    uias.put(uia.exportJSON());
                }

                uia_set.put("description", uia_set_desc);
                uia_set.put("data", uias);
                FileOutputStream outs = new FileOutputStream(json_file); //openFileOutput( json_file.toString(), Context.MODE_PRIVATE);
                outs.write(uia_set.toString().getBytes() );


            } catch( IOException ioe) {
                ioe.printStackTrace();
            } catch ( JSONException je) {
                je.printStackTrace();
            }

        }

    }

}
