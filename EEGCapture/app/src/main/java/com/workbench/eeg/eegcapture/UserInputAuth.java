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

import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class UserInputAuth {
    private int id;
    //private HashMap<String, ArrayList<Integer> > neighbor_uia_distance_diff;
    private HashMap<String, HashMap<Integer,Integer> > neighbor_uia_distance_diff;
    private HashMap<String, HashMap<Integer,Integer> > neighbor_uia_distance_diff_mod;

    private int closest_neighbor_uid;
    private HashMap<String, List<Entry> >               closest_neighbor_avg_entries;

    private HashMap<String, HashMap<String,String>>     transition_direction; //i.e. relative_calibration_vector's direction
    private HashMap<String, HashMap<String,Integer>>    transition_range; // same as relative_calibration_vector but computed in real-time

    private HashMap<String, ArrayList<Integer> >        calibration_vectors; // based on absolute value of eeg power
    private HashMap<String, HashMap<String, Integer> >  relative_calibration_vector; // based on diff between points (can apply to any absolute eeg power value)
    private HashMap<String, ArrayList<Integer> >        noise_threshold;
    private HashMap<String, Integer>                    freq_msv; //Mean Sample Value for each frequency


    // graph/chart related data
    private List<Entry>     theta_yvals;
    private List<Entry>     lalpha_yvals;
    private List<Entry>     halpha_yvals;
    private List<Entry>     lbeta_yvals;
    private List<Entry>     hbeta_yvals;
    private List<Entry>     mgamma_yvals;
    private List<Entry>     lgamma_yvals;

    private List<Entry>     theta_yvals_mod;
    private List<Entry>     lalpha_yvals_mod;
    private List<Entry>     halpha_yvals_mod;
    private List<Entry>     lbeta_yvals_mod;
    private List<Entry>     hbeta_yvals_mod;
    private List<Entry>     mgamma_yvals_mod;
    private List<Entry>     lgamma_yvals_mod;

    // raw eeg
    private List<Entry>     raw_yvals;
    private List<String>    chart_raw_xvals;
    private int             current_raw_xval;

    private Date            record_datetime;

    private List<String>    chart_xvals;
    private int             current_xval;

    private String          activity_description;

    // column vector representation (median magnitude)
    private ArrayList<Entry>        column_vector;
    private ArrayList<Integer>      rounded_column_vector;

    private ArrayList<Integer>        sum_vector;

    // EEG pre-processing - entries to disregard from start/end of theta/lalpha/halpha
    //private static final int TEMPORAL_CHOMP = 1;

    public static final String THETA   = "theta";
    public static final String LALPHA  = "lalpha";
    public static final String HALPHA  = "halpha";
    public static final String LBETA   = "lbeta";
    public static final String HBETA   = "hbeta";
    public static final String MGAMMA  = "mgamma";
    public static final String LGAMMA  = "lgamma";

    public UserInputAuth(int i) {
        this.id                         = i;
        //this.neighbor_uia_distance_diff = new HashMap<String, ArrayList<Integer> >();
        this.neighbor_uia_distance_diff = new HashMap<String, HashMap<Integer,Integer> >();
        this.neighbor_uia_distance_diff_mod = new HashMap<String, HashMap<Integer,Integer>>();

        this.closest_neighbor_uid           = -1;
        this.closest_neighbor_avg_entries   = new HashMap<String, List<Entry> >();

        this.transition_range               = new HashMap<String, HashMap<String,Integer>>();
        this.transition_range.put(UserInputAuth.THETA, new HashMap<String, Integer>());
        this.transition_range.put(UserInputAuth.LALPHA, new HashMap<String, Integer>());
        this.transition_range.put(UserInputAuth.HALPHA, new HashMap<String, Integer>());
        this.transition_range.put(UserInputAuth.LBETA, new HashMap<String, Integer>());
        this.transition_range.put(UserInputAuth.HBETA, new HashMap<String, Integer>());
        this.transition_range.put(UserInputAuth.LGAMMA, new HashMap<String, Integer>());
        this.transition_range.put(UserInputAuth.MGAMMA, new HashMap<String, Integer>());

        this.transition_direction           = new HashMap<String, HashMap<String,String>>();
        this.transition_direction.put(UserInputAuth.THETA, new HashMap<String, String>());
        this.transition_direction.put(UserInputAuth.LALPHA, new HashMap<String, String>());
        this.transition_direction.put(UserInputAuth.HALPHA, new HashMap<String, String>());
        this.transition_direction.put(UserInputAuth.LBETA, new HashMap<String, String>());
        this.transition_direction.put(UserInputAuth.HBETA, new HashMap<String, String>());
        this.transition_direction.put(UserInputAuth.LGAMMA, new HashMap<String, String>());
        this.transition_direction.put(UserInputAuth.MGAMMA, new HashMap<String, String>());

        this.calibration_vectors            = new HashMap<String, ArrayList<Integer> >();
        this.relative_calibration_vector    = new HashMap<String, HashMap<String,Integer> >();
        this.noise_threshold                = new HashMap<String, ArrayList<Integer> >();
        this.freq_msv                       = new HashMap<String, Integer >();

        // initialize graph/charts related
        this.theta_yvals    = new ArrayList<Entry>();
        this.lalpha_yvals   = new ArrayList<Entry>();
        this.halpha_yvals   = new ArrayList<Entry>();
        this.lbeta_yvals    = new ArrayList<Entry>();
        this.hbeta_yvals    = new ArrayList<Entry>();
        this.lgamma_yvals   = new ArrayList<Entry>();
        this.mgamma_yvals   = new ArrayList<Entry>();

        this.theta_yvals_mod    = new ArrayList<Entry>();
        this.lalpha_yvals_mod   = new ArrayList<Entry>();
        this.halpha_yvals_mod   = new ArrayList<Entry>();
        this.lbeta_yvals_mod    = new ArrayList<Entry>();
        this.hbeta_yvals_mod    = new ArrayList<Entry>();
        this.lgamma_yvals_mod   = new ArrayList<Entry>();
        this.mgamma_yvals_mod   = new ArrayList<Entry>();

        this.chart_xvals    = new ArrayList<String>();
        this.current_xval   = 0;

        //raw eeg wave data
        this.record_datetime    = new Date();
        this.raw_yvals          = new ArrayList<Entry>();
        this.chart_raw_xvals    = new ArrayList<String>();
        this.current_raw_xval   = 0;

        // initialize EEG data pre-processing
        this.column_vector          = new ArrayList<Entry>();
        this.rounded_column_vector  = new ArrayList<Integer>();

        this.sum_vector             = new ArrayList<Integer>();

    }

    public int getId() {
        return this.id;
    }

    public void addNeighborDistDiff(String freq, int uia_id, int dist) {
        //Log.d("addNeighborDistDiff()", String.format("for UIA %d: %s, %d, %d", this.getId(), freq, uia_id, dist) );
        /*
        if (!this.neighbor_uia_distance_diff.containsKey(freq) || this.neighbor_uia_distance_diff.get(freq).size() == 0 ) {

            Log.d("addNeighborDistDiff()", String.format("init neighbor set") );
            this.neighbor_uia_distance_diff.put( freq, new ArrayList<Integer>());

            for( int idx = 0; idx < ( EEGCapture.TOTAL_CAPTURE_PHASES + 1); idx += 1) {
                this.neighbor_uia_distance_diff.get(freq).add(null);
            }
        }


        this.neighbor_uia_distance_diff.get(freq).add(uia_id, dist);
        */
        if (!this.neighbor_uia_distance_diff.containsKey(freq)) {
            this.neighbor_uia_distance_diff.put(freq, new HashMap<Integer, Integer>());
            this.neighbor_uia_distance_diff_mod.put(freq, new HashMap<Integer, Integer>());
        }

        this.neighbor_uia_distance_diff.get(freq).put(uia_id, dist);
        this.neighbor_uia_distance_diff_mod.get(freq).put(uia_id, dist / 100 );

    }

    public HashMap<String, HashMap<Integer,Integer> > getNeighborDistDiff() {
        return this.neighbor_uia_distance_diff;
        //return this.neighbor_uia_distance_diff_mod;

    }

    // closest neighbor has lowest distance across all frequencies
    public Integer getClosestNeighbor() {
        int uid_id = this.closest_neighbor_uid;

        if ( uid_id < 0 && this.getNeighborDistDiff().size() > 0 ) {
            int min_dist                                    = -1;
            HashMap<String, HashMap<Integer, Integer>> n    = this.getNeighborDistDiff();
            Set<Integer> neighbor_uid_ids                   = n.get(UserInputAuth.THETA).keySet();

            for (Integer cuid_id: neighbor_uid_ids) {
                int current_diff_total = 0;

                for ( String freq : n.keySet() ){
                    current_diff_total += n.get(freq).get(cuid_id);
                }

                if ( min_dist < 0 ) {
                    min_dist = current_diff_total;
                    uid_id = cuid_id;
                }
                else {
                    if ( current_diff_total < min_dist ) {
                        min_dist = current_diff_total;
                        uid_id = cuid_id;
                    }
                }

            }

            this.closest_neighbor_uid = uid_id;

        }

        return uid_id;

    }

    public HashMap<String, List<Entry>> getClosestNeighborAvg(UserInputAuth uia) {
        if ( this.closest_neighbor_uid > 0 && this.closest_neighbor_avg_entries.isEmpty() ) {

            // "trim" user input due to bt transmission lag (among other possible influences)
            int start_idx = 1;

            int x_ceiling = this.getX().size();
            if ( uia.getX().size() < x_ceiling ) {
                x_ceiling = uia.getX().size();
            }

            if ( x_ceiling > 3 ) {
                x_ceiling -= 1;

            }

            ArrayList<Entry> c_uia_theta    = (ArrayList<Entry>) this.getEEGEntries(UserInputAuth.THETA, false);
            ArrayList<Entry> uia_theta      = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.THETA, false);
            ArrayList<Entry> theta_avg      = new ArrayList<Entry>();

            ArrayList<Entry> c_uia_lalpha   = (ArrayList<Entry>) this.getEEGEntries(UserInputAuth.LALPHA, false);
            ArrayList<Entry> uia_lalpha      = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.LALPHA, false);
            ArrayList<Entry> lalpha_avg      = new ArrayList<Entry>();

            ArrayList<Entry> c_uia_halpha   = (ArrayList<Entry>) this.getEEGEntries(UserInputAuth.HALPHA, false);
            ArrayList<Entry> uia_halpha     = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.HALPHA, false);
            ArrayList<Entry> halpha_avg     = new ArrayList<Entry>();

            ArrayList<Entry> c_uia_lbeta  = (ArrayList<Entry>) this.getEEGEntries(UserInputAuth.LBETA, false);
            ArrayList<Entry> uia_lbeta    = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.LBETA, false);
            ArrayList<Entry> lbeta_avg    = new ArrayList<Entry>();

            ArrayList<Entry> c_uia_hbeta  = (ArrayList<Entry>) this.getEEGEntries(UserInputAuth.HBETA, false);
            ArrayList<Entry> uia_hbeta    = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.HBETA, false);
            ArrayList<Entry> hbeta_avg    = new ArrayList<Entry>();

            ArrayList<Entry> c_uia_lgamma  = (ArrayList<Entry>) this.getEEGEntries(UserInputAuth.LGAMMA, false);
            ArrayList<Entry> uia_lgamma    = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.LGAMMA, false);
            ArrayList<Entry> lgamma_avg    = new ArrayList<Entry>();

            ArrayList<Entry> c_uia_mgamma  = (ArrayList<Entry>) this.getEEGEntries(UserInputAuth.MGAMMA, false);
            ArrayList<Entry> uia_mgamma    = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.MGAMMA, false);
            ArrayList<Entry> mgamma_avg    = new ArrayList<Entry>();

            for(int idx = start_idx; idx < x_ceiling; idx += 1) {
                theta_avg.add( new Entry( ( c_uia_theta.get(idx).getVal() + uia_theta.get(idx).getVal() ) / 2, idx ) );
                lalpha_avg.add( new Entry( ( c_uia_lalpha.get(idx).getVal() + uia_lalpha.get(idx).getVal() ) / 2, idx )  );
                halpha_avg.add( new Entry( ( c_uia_halpha.get(idx).getVal() + uia_halpha.get(idx).getVal() ) / 2, idx ) );
                lbeta_avg.add( new Entry( ( c_uia_lbeta.get(idx).getVal() + uia_lbeta.get(idx).getVal() ) / 2, idx ) );
                hbeta_avg.add( new Entry( ( c_uia_hbeta.get(idx).getVal() + uia_hbeta.get(idx).getVal() ) / 2, idx ) );
                lgamma_avg.add( new Entry( ( c_uia_lgamma.get(idx).getVal() + uia_lgamma.get(idx).getVal() ) / 2, idx ) );
                mgamma_avg.add( new Entry( ( c_uia_mgamma.get(idx).getVal() + uia_mgamma.get(idx).getVal() ) / 2, idx ) );

            }

            this.closest_neighbor_avg_entries.put(UserInputAuth.THETA, theta_avg);
            this.closest_neighbor_avg_entries.put(UserInputAuth.LALPHA, lalpha_avg);
            this.closest_neighbor_avg_entries.put(UserInputAuth.HALPHA, halpha_avg);
            this.closest_neighbor_avg_entries.put(UserInputAuth.LBETA, lbeta_avg);
            this.closest_neighbor_avg_entries.put(UserInputAuth.HBETA, hbeta_avg);
            this.closest_neighbor_avg_entries.put(UserInputAuth.LGAMMA, lgamma_avg);
            this.closest_neighbor_avg_entries.put(UserInputAuth.MGAMMA, mgamma_avg);


        }

        return this.closest_neighbor_avg_entries;

    }

    // only raw eeg and sensor info
    public JSONObject exportJSON() {
        JSONObject export = new JSONObject();
        String sensor = "FP1"; //neurosky has a single sensor
        String device = "NSKYMW";
        String sample_rate = "512";
        String eegwb_ver = "1";

        try {
            JSONArray raw_eeg_ints = new JSONArray();
            for (Entry e : this.raw_yvals) {
                raw_eeg_ints.put( (int) e.getVal() );
            }

            export.put(sensor, (JSONArray) raw_eeg_ints);
            export.put("device", device);
            export.put("sample_rate", sample_rate);
            export.put("ver", eegwb_ver);

            DateFormat df           = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            export.put("record_datetime", df.format(this.record_datetime));

        } catch ( JSONException je) {
            je.printStackTrace();

        }

        return export;

    }

    public void updateRaw(int raw_wave) {
        //Log.d("updateRaw()", String.format("raw: %d", raw_wave) );

        //if (this.current_raw_xval % 10 == 0) {
            // Java has no unsigned data types, which has to be handled carefully when combining the two bytes
            int[]   highlow = new int[2];
            highlow[0] = (int)(raw_wave & 0xFF);
            highlow[1] = (int)(raw_wave & 0xFF);
            int raw = (highlow[0] * 256) + highlow[1];
            if( raw > 32768 ) raw -= 65536;


            this.raw_yvals.add(new Entry(raw_wave, this.current_raw_xval));
            this.chart_raw_xvals.add(String.format("%d", this.current_raw_xval));
        //}

        this.current_raw_xval += 1;

    }

    public void updateData( String desc,
                            float theta,
                            float lowAlpha,
                            float highAlpha,
                            float lowBeta,
                            float highBeta,
                            float lowGamma,
                            float midGamma ) {
        Log.d("updateData()", String.format("%d", this.id));
        //int modifier = 1000;
        int modifier = 100;

        this.activity_description = desc;

        this.theta_yvals.add( new Entry( theta, this.current_xval ) );
        this.lalpha_yvals.add( new Entry( lowAlpha, this.current_xval) );
        this.halpha_yvals.add( new Entry( highAlpha, this.current_xval) );
        this.lbeta_yvals.add( new Entry( lowBeta, this.current_xval) );
        this.hbeta_yvals.add( new Entry( highBeta, this.current_xval) );
        this.mgamma_yvals.add( new Entry( midGamma, this.current_xval) );
        this.lgamma_yvals.add( new Entry( lowGamma, this.current_xval) );

        // modded data
        this.theta_yvals_mod.add( new Entry( theta / modifier, this.current_xval ) );
        this.lalpha_yvals_mod.add( new Entry( lowAlpha / modifier, this.current_xval) );
        this.halpha_yvals_mod.add( new Entry( highAlpha / modifier, this.current_xval) );
        this.lbeta_yvals_mod.add( new Entry( lowBeta / modifier, this.current_xval) );
        this.hbeta_yvals_mod.add( new Entry( highBeta / modifier, this.current_xval) );
        this.mgamma_yvals_mod.add( new Entry( midGamma / modifier, this.current_xval) );
        this.lgamma_yvals_mod.add( new Entry( lowGamma / modifier, this.current_xval) );

        this.chart_xvals.add( String.format("%d", this.current_xval) );

        if ( this.current_xval > 0) {
            // keep track of "range" from point to point, i.e. diff of y vals
            /*int t_range = (int) ( theta_yvals.get(this.current_xval).getVal() - theta_yvals.get(this.current_xval - 1).getVal() );
            int la_range = (int) ( lalpha_yvals.get(this.current_xval).getVal() - lalpha_yvals.get(this.current_xval - 1).getVal() );
            int ha_range = (int) ( halpha_yvals.get(this.current_xval).getVal() - halpha_yvals.get(this.current_xval - 1).getVal() );
            int lb_range = (int) ( lbeta_yvals.get(this.current_xval).getVal() - lbeta_yvals.get(this.current_xval - 1).getVal() );
            int hb_range = (int) ( hbeta_yvals.get(this.current_xval).getVal() - hbeta_yvals.get(this.current_xval - 1).getVal() );
            int lg_range = (int) ( lgamma_yvals.get(this.current_xval).getVal() - lgamma_yvals.get(this.current_xval - 1).getVal() );
            int mg_range = (int) ( mgamma_yvals.get(this.current_xval).getVal() - mgamma_yvals.get(this.current_xval - 1).getVal() );
            */
            int t_range  = (int) Math.ceil( theta_yvals.get(this.current_xval).getVal() / theta_yvals.get(this.current_xval - 1).getVal() );
            int la_range = (int) Math.ceil( lalpha_yvals.get(this.current_xval).getVal() / lalpha_yvals.get(this.current_xval - 1).getVal() );
            int ha_range = (int) Math.ceil( halpha_yvals.get(this.current_xval).getVal() / halpha_yvals.get(this.current_xval - 1).getVal() );
            int lb_range = (int) Math.ceil( lbeta_yvals.get(this.current_xval).getVal() / lbeta_yvals.get(this.current_xval - 1).getVal() );
            int hb_range = (int) Math.ceil( hbeta_yvals.get(this.current_xval).getVal() / hbeta_yvals.get(this.current_xval - 1).getVal() );
            int lg_range = (int) Math.ceil( lgamma_yvals.get(this.current_xval).getVal() / lgamma_yvals.get(this.current_xval - 1).getVal() );
            int mg_range = (int) Math.ceil( mgamma_yvals.get(this.current_xval).getVal() / mgamma_yvals.get(this.current_xval - 1).getVal() );


            this.transition_range.get(UserInputAuth.THETA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), Math.abs(t_range));
            this.transition_range.get(UserInputAuth.LALPHA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), Math.abs(la_range));
            this.transition_range.get(UserInputAuth.HALPHA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), Math.abs(ha_range));
            this.transition_range.get(UserInputAuth.LBETA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), Math.abs(lb_range));
            this.transition_range.get(UserInputAuth.HBETA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), Math.abs(hb_range));
            this.transition_range.get(UserInputAuth.LGAMMA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), Math.abs(lg_range));
            this.transition_range.get(UserInputAuth.MGAMMA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), Math.abs(mg_range));

            // keep track of "direction" from point to point, i.e. moving up/down?
            String t_dir = (t_range < 0)?"-":"+";
            String la_dir = (la_range < 0)?"-":"+";
            String ha_dir = (ha_range < 0)?"-":"+";
            String lb_dir = (lb_range < 0)?"-":"+";
            String hb_dir = (hb_range < 0)?"-":"+";
            String lg_dir = (lg_range < 0)?"-":"+";
            String mg_dir = (mg_range < 0)?"-":"+";

            this.transition_direction.get(UserInputAuth.THETA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), t_dir);
            this.transition_direction.get(UserInputAuth.LALPHA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), la_dir);
            this.transition_direction.get(UserInputAuth.HALPHA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), ha_dir);
            this.transition_direction.get(UserInputAuth.LBETA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), lb_dir);
            this.transition_direction.get(UserInputAuth.HBETA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), hb_dir);
            this.transition_direction.get(UserInputAuth.LGAMMA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), lg_dir);
            this.transition_direction.get(UserInputAuth.MGAMMA).put(String.format("%d-%d", this.current_xval - 1, this.current_xval), mg_dir);

        }

        this.current_xval += 1;
    }

    public List<Entry> getEEGEntries(String eeg_type, boolean modded) {
        List<Entry> eeg_entries = null;

        switch ( eeg_type ) {
            case UserInputAuth.THETA:
                eeg_entries = (modded)?this.theta_yvals_mod:this.theta_yvals;
                break;
            case UserInputAuth.LALPHA:
                eeg_entries = (modded)?this.lalpha_yvals_mod:this.lalpha_yvals;
                break;
            case UserInputAuth.HALPHA:
                eeg_entries = (modded)?this.halpha_yvals_mod:this.halpha_yvals;
                break;
            case UserInputAuth.LBETA:
                eeg_entries = (modded)?this.lbeta_yvals_mod:this.lbeta_yvals;
                break;
            case UserInputAuth.HBETA:
                eeg_entries = (modded)?this.hbeta_yvals_mod:this.hbeta_yvals;
                break;
            case UserInputAuth.MGAMMA:
                eeg_entries = (modded)?this.mgamma_yvals_mod:this.mgamma_yvals;
                break;
            case UserInputAuth.LGAMMA:
                eeg_entries = (modded)?this.lgamma_yvals_mod:this.lgamma_yvals;
                break;
            case "raw":
                eeg_entries = this.raw_yvals;
                break;

        }

        return eeg_entries;
    }

    public List<String> getX() {
        return this.chart_xvals;
    }

    public List<String> getRawX() {
        return this.chart_raw_xvals;
    }


    /*public String getDesc() {
        return this.activity_description;
    }*/


    public ArrayList<Integer> getSumVector(int ceiling, ArrayList<String> freqs) {
        // "trim" user input due to bt transmission lag (among other possible influences)
        int start_idx = 1;

        if ( ceiling > 3 ) {
            ceiling -= 1;

        }

        // for each frequency within temporal dimension ( after 1 second to "ceiling" second)
        // i.e. disregard first second and up to minimum length of other training sessions (ceiling)
        if (    this.sum_vector.isEmpty() &&
                !this.theta_yvals.isEmpty() && this.theta_yvals.size() > start_idx &&
                !this.lalpha_yvals.isEmpty() && this.lalpha_yvals.size() > start_idx &&
                !this.halpha_yvals.isEmpty() && this.halpha_yvals.size() > start_idx &&
                !this.lbeta_yvals.isEmpty() && this.lbeta_yvals.size() > start_idx &&
                !this.hbeta_yvals.isEmpty() && this.hbeta_yvals.size() > start_idx &&
                !this.mgamma_yvals.isEmpty() && this.mgamma_yvals.size() > start_idx &&
                !this.lgamma_yvals.isEmpty() && this.lgamma_yvals.size() > start_idx &&
                this.chart_xvals.size() > 0 )
        {

            float sum_theta     = this.theta_yvals.get(start_idx).getVal();
            float sum_lalpha    = this.lalpha_yvals.get(start_idx).getVal();
            float sum_halpha    = this.halpha_yvals.get(start_idx).getVal();
            float sum_lbeta     = this.lbeta_yvals.get(start_idx).getVal();
            float sum_hbeta     = this.hbeta_yvals.get(start_idx).getVal();
            float sum_mgamma    = this.mgamma_yvals.get(start_idx).getVal();
            float sum_lgamma    = this.lgamma_yvals.get(start_idx).getVal();

            for ( int idx = start_idx; idx < ceiling; idx += 1 ) {

                    sum_theta += this.theta_yvals.get(idx).getVal();

                    sum_lalpha += this.lalpha_yvals.get(idx).getVal();

                    sum_halpha += this.halpha_yvals.get(idx).getVal();

                    sum_lbeta += this.lbeta_yvals.get(idx).getVal();

                    sum_hbeta += this.hbeta_yvals.get(idx).getVal();

                    sum_lgamma += this.lgamma_yvals.get(idx).getVal();

                    sum_mgamma += this.mgamma_yvals.get(idx).getVal();

            }

            if (freqs.indexOf(UserInputAuth.THETA) >= 0) {
                this.sum_vector.add( (int) sum_theta);
            }
            if (freqs.indexOf(UserInputAuth.LALPHA) >= 0) {
                this.sum_vector.add( (int) sum_lalpha);
            }
            if (freqs.indexOf(UserInputAuth.HALPHA) >= 0) {
                this.sum_vector.add( (int) sum_halpha);
            }
            if (freqs.indexOf(UserInputAuth.LBETA) >= 0) {
                this.sum_vector.add( (int) sum_lbeta);
            }
            if (freqs.indexOf(UserInputAuth.HBETA) >= 0) {
                this.sum_vector.add( (int) sum_hbeta);
            }
            if (freqs.indexOf(UserInputAuth.LGAMMA) >= 0) {
                this.sum_vector.add( (int) sum_lgamma);
            }
            if (freqs.indexOf(UserInputAuth.MGAMMA) >= 0) {
                this.sum_vector.add( (int) sum_mgamma);
            }
        }

        return this.sum_vector;

    }

    public ArrayList<Entry> getColVector(int ceiling, ArrayList<String> freqs) {
        // "trim" user input due to bt transmission lag (among other possible influences)
        int start_idx = 1;

        if ( ceiling > 3 ) {
            ceiling -= 1;

        }

        // compute and cache column vector of median magnitudes
        // for each frequency within temporal dimension ( after 1 second to "ceiling" second)
        // i.e. disregard first second and up to minimum length of other training sessions (ceiling)
        if (    this.column_vector.isEmpty() &&
                !this.theta_yvals.isEmpty() && this.theta_yvals.size() > start_idx &&
                !this.lalpha_yvals.isEmpty() && this.lalpha_yvals.size() > start_idx &&
                !this.halpha_yvals.isEmpty() && this.halpha_yvals.size() > start_idx &&
                !this.lbeta_yvals.isEmpty() && this.lbeta_yvals.size() > start_idx &&
                !this.hbeta_yvals.isEmpty() && this.hbeta_yvals.size() > start_idx &&
                !this.mgamma_yvals.isEmpty() && this.mgamma_yvals.size() > start_idx &&
                !this.lgamma_yvals.isEmpty() && this.lgamma_yvals.size() > start_idx &&
                this.chart_xvals.size() > 0 )
        {

            Entry max_theta     = this.theta_yvals.get(start_idx);
            Entry max_lalpha    = this.lalpha_yvals.get(start_idx);
            Entry max_halpha    = this.halpha_yvals.get(start_idx);
            Entry max_lbeta     = this.lbeta_yvals.get(start_idx);
            Entry max_hbeta     = this.hbeta_yvals.get(start_idx);
            Entry max_mgamma    = this.mgamma_yvals.get(start_idx);
            Entry max_lgamma    = this.lgamma_yvals.get(start_idx);

            for ( int idx = start_idx; idx < ceiling; idx += 1 ) {

                if ( max_theta.getVal() < this.theta_yvals.get(idx).getVal() ) {
                    max_theta = this.theta_yvals.get(idx);
                }

                if ( max_lalpha.getVal() < this.lalpha_yvals.get(idx).getVal() ) {
                    max_lalpha = this.lalpha_yvals.get(idx);
                }

                if ( max_halpha.getVal() < this.halpha_yvals.get(idx).getVal() ) {
                    max_halpha = this.halpha_yvals.get(idx);
                }

                if ( max_lbeta.getVal() < this.lbeta_yvals.get(idx).getVal() ) {
                    max_lbeta = this.lbeta_yvals.get(idx);
                }

                if ( max_hbeta.getVal() < this.hbeta_yvals.get(idx).getVal() ) {
                    max_hbeta = this.hbeta_yvals.get(idx);
                }

                if ( max_lgamma.getVal() < this.lgamma_yvals.get(idx).getVal() ) {
                    max_lgamma = this.lgamma_yvals.get(idx);
                }

                if ( max_mgamma.getVal() < this.mgamma_yvals.get(idx).getVal() ) {
                    max_mgamma = this.mgamma_yvals.get(idx);
                }

            }

            /*this.column_vector.add(max_theta);
            this.column_vector.add(max_lalpha);
            this.column_vector.add(max_halpha);
            this.column_vector.add(max_lbeta);
            this.column_vector.add(max_hbeta);
            this.column_vector.add(max_lgamma);
            this.column_vector.add(max_mgamma);
            */

            if (freqs.indexOf(UserInputAuth.THETA) >= 0) {
                this.column_vector.add(max_theta);
            }
            if (freqs.indexOf(UserInputAuth.LALPHA) >= 0) {
                this.column_vector.add(max_lalpha);
            }
            if (freqs.indexOf(UserInputAuth.HALPHA) >= 0) {
                this.column_vector.add(max_halpha);
            }
            if (freqs.indexOf(UserInputAuth.LBETA) >= 0) {
                this.column_vector.add(max_lbeta);
            }
            if (freqs.indexOf(UserInputAuth.HBETA) >= 0) {
                this.column_vector.add(max_hbeta);
            }
            if (freqs.indexOf(UserInputAuth.LGAMMA) >= 0) {
                this.column_vector.add(max_lgamma);
            }
            if (freqs.indexOf(UserInputAuth.MGAMMA) >= 0) {
                this.column_vector.add(max_mgamma);
            }
        }

        return this.column_vector;

    }

    public ArrayList<Integer> getRoundedColVector() {
        if (this.rounded_column_vector.isEmpty()) {
            this.rounded_column_vector = new ArrayList<Integer>();

            for ( Entry e : this.column_vector ) {
                this.rounded_column_vector.add( this.rounded_column_vector.size(),
                        ( (Integer.valueOf((int) e.getVal()) + 99) / 100) * 100 );
            }

        }

        return this.rounded_column_vector;

    }

    public double cosineSim( ArrayList<Entry> col_vect ) {
        double cosine_sim       = 0;

        if ( col_vect.size() == this.column_vector.size() ) {

            // calculate dot product
            double dot_product       = 0;

            for (int idx = 0; idx < this.column_vector.size(); idx += 1 ) {
                dot_product += ( col_vect.get(idx).getVal() * this.column_vector.get(idx).getVal() );
            }

            // calculate euclidean norms
            double this_cv_en    = 0;
            double cv_en         = 0;

            for ( Entry u : this.column_vector ) {
                this_cv_en += Math.pow(u.getVal(), 2);
            }
            this_cv_en = Math.sqrt(this_cv_en);

            for ( Entry v : col_vect ) {
                cv_en += Math.pow(v.getVal(), 2);

            }
            cv_en = Math.sqrt(cv_en);

            // calculate cosine similarity
            cosine_sim = dot_product / ( this_cv_en * cv_en );
        }

        return cosine_sim;
    }

    public double magnitudeDiff( ArrayList<Entry> col_vect ) {
        // avg of differences between each frequency in column vector
        double sum_magnitude_diff = 0;
        double avg_magnitude_diff = 0;

        if (col_vect.size() == this.column_vector.size()) {

            for (int idx = 0; idx < this.column_vector.size(); idx += 1) {
                sum_magnitude_diff += Math.abs(col_vect.get(idx).getVal() - this.column_vector.get(idx).getVal());
            }

            avg_magnitude_diff = sum_magnitude_diff / col_vect.size();
        }

        return avg_magnitude_diff;

    }

    private void calcFreqSim(UserInputAuth uia, int x_ceiling) {
        // determine top 3 freq avg minimum graph distance
        float theta_dist    = 0;
        float lalpha_dist   = 0;
        float halpha_dist   = 0;
        float lbeta_dist    = 0;
        float hbeta_dist    = 0;
        float mgamma_dist   = 0;
        float lgamma_dist   = 0;

        ArrayList<Entry> uia_theta  = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.THETA, false);
        ArrayList<Entry> uia_lalpha = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.LALPHA, false);
        ArrayList<Entry> uia_halpha = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.HALPHA, false);
        ArrayList<Entry> uia_lbeta  = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.LBETA, false);
        ArrayList<Entry> uia_hbeta  = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.HBETA, false);
        ArrayList<Entry> uia_mgamma = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.MGAMMA, false);
        ArrayList<Entry> uia_lgamma = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.LGAMMA, false);

        ArrayList<Integer> freq_dist  = new ArrayList<Integer>();
        for ( int idx = 1; idx < x_ceiling; idx += 1) {
            theta_dist += Math.abs( uia_theta.get(idx).getVal() - this.theta_yvals.get(idx).getVal() );
            lalpha_dist += Math.abs( uia_lalpha.get(idx).getVal() - this.lalpha_yvals.get(idx).getVal() );
            halpha_dist += Math.abs( uia_halpha.get(idx).getVal() - this.halpha_yvals.get(idx).getVal() );
            lbeta_dist += Math.abs( uia_lbeta.get(idx).getVal() - this.lbeta_yvals.get(idx).getVal() );
            hbeta_dist += Math.abs( uia_hbeta.get(idx).getVal() - this.hbeta_yvals.get(idx).getVal() );
            lgamma_dist += Math.abs( uia_lgamma.get(idx).getVal() - this.lgamma_yvals.get(idx).getVal() );
            mgamma_dist += Math.abs( uia_mgamma.get(idx).getVal() - this.mgamma_yvals.get(idx).getVal() );

        }

        addNeighborDistDiff(UserInputAuth.THETA, uia.getId(), (int) theta_dist);
        addNeighborDistDiff(UserInputAuth.LALPHA, uia.getId(), (int) lalpha_dist);
        addNeighborDistDiff(UserInputAuth.HALPHA, uia.getId(), (int) halpha_dist);
        addNeighborDistDiff(UserInputAuth.LBETA, uia.getId(), (int) lbeta_dist);
        addNeighborDistDiff(UserInputAuth.HBETA, uia.getId(), (int) hbeta_dist);
        addNeighborDistDiff(UserInputAuth.LGAMMA, uia.getId(), (int) lgamma_dist);
        addNeighborDistDiff(UserInputAuth.MGAMMA, uia.getId(), (int) mgamma_dist);

    }

    public HashMap<String, HashMap<Integer,Integer> > freqSim2(UserInputAuth uia) {
        Log.d("UIA.freqSim2()", String.format( " %d %d\n", this.getId(), uia.getId() ) );
        //if (this.neighbor_uia_distance_diff.isEmpty()) {

            // determine ceiling
            int x_ceiling = uia.getX().size();
            if ( x_ceiling > this.getX().size() ) {
                x_ceiling = this.getX().size();

            }

            this.calcFreqSim(uia, x_ceiling);

        //}

        return this.getNeighborDistDiff();

    }

    public ArrayList<String> freqSim(UserInputAuth uia) {
        // determine ceiling
        int x_ceiling = uia.getX().size();
        if ( x_ceiling > this.getX().size() ) {
            x_ceiling = this.getX().size();

        }

        // determine top 3 freq avg minimum graph distance
        float theta_dist    = 0;
        float lalpha_dist   = 0;
        float halpha_dist   = 0;
        float lbeta_dist    = 0;
        float hbeta_dist    = 0;
        float mgamma_dist   = 0;
        float lgamma_dist   = 0;

        ArrayList<Entry> uia_theta  = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.THETA, false);
        ArrayList<Entry> uia_lalpha = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.LALPHA, false);
        ArrayList<Entry> uia_halpha = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.HALPHA, false);
        ArrayList<Entry> uia_lbeta  = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.LBETA, false);
        ArrayList<Entry> uia_hbeta  = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.HBETA, false);
        ArrayList<Entry> uia_mgamma = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.MGAMMA, false);
        ArrayList<Entry> uia_lgamma = (ArrayList<Entry>) uia.getEEGEntries(UserInputAuth.LGAMMA, false);

        ArrayList<Integer> freq_dist  = new ArrayList<Integer>();
        for ( int idx = 0; idx < x_ceiling; idx += 1) {
            theta_dist += Math.abs( uia_theta.get(idx).getVal() - this.theta_yvals.get(idx).getVal() );
            lalpha_dist += Math.abs( uia_lalpha.get(idx).getVal() - this.lalpha_yvals.get(idx).getVal() );
            halpha_dist += Math.abs( uia_halpha.get(idx).getVal() - this.halpha_yvals.get(idx).getVal() );
            lbeta_dist += Math.abs( uia_lbeta.get(idx).getVal() - this.lbeta_yvals.get(idx).getVal() );
            hbeta_dist += Math.abs( uia_hbeta.get(idx).getVal() - this.hbeta_yvals.get(idx).getVal() );
            lgamma_dist += Math.abs( uia_lgamma.get(idx).getVal() - this.lgamma_yvals.get(idx).getVal() );
            mgamma_dist += Math.abs( uia_mgamma.get(idx).getVal() - this.mgamma_yvals.get(idx).getVal() );

        }

        freq_dist.add((int) theta_dist);
        freq_dist.add((int) lalpha_dist);
        freq_dist.add((int) halpha_dist);
        freq_dist.add((int) lbeta_dist);
        freq_dist.add((int) hbeta_dist);
        freq_dist.add((int) lgamma_dist);
        freq_dist.add((int) mgamma_dist);

        Collections.sort(freq_dist);

        ArrayList<String> freq_dist_str = new ArrayList<String>();
        for ( int idx = 0; idx < 3; idx += 1 ) {
            if ( freq_dist.get(idx) == theta_dist && freq_dist_str.indexOf(UserInputAuth.THETA) < 0 ) {
                freq_dist_str.add(UserInputAuth.THETA);
            }
            else if (freq_dist.get(idx) == lalpha_dist && freq_dist_str.indexOf(UserInputAuth.LALPHA) < 0 ) {
                freq_dist_str.add(UserInputAuth.LALPHA);
            }
            else if (freq_dist.get(idx) == halpha_dist && freq_dist_str.indexOf(UserInputAuth.HALPHA) < 0 ) {
                freq_dist_str.add(UserInputAuth.HALPHA);
            }
            else if (freq_dist.get(idx) == lbeta_dist && freq_dist_str.indexOf(UserInputAuth.LBETA) < 0 ) {
                freq_dist_str.add(UserInputAuth.LBETA);
            }
            else if (freq_dist.get(idx) == hbeta_dist && freq_dist_str.indexOf(UserInputAuth.HBETA) < 0 ) {
                freq_dist_str.add(UserInputAuth.HBETA);
            }
            else if (freq_dist.get(idx) == lgamma_dist && freq_dist_str.indexOf(UserInputAuth.LGAMMA) < 0 ) {
                freq_dist_str.add(UserInputAuth.LGAMMA);
            }
            else if (freq_dist.get(idx) == mgamma_dist && freq_dist_str.indexOf(UserInputAuth.MGAMMA) < 0 ) {
                freq_dist_str.add(UserInputAuth.MGAMMA);
            }

        }

        return freq_dist_str;

    }

    // each point for each frequency gets it's own "calibration window"
    public HashMap<String, ArrayList<Integer> > getCalibrationVector() {
        Log.d("getCalibrationVector()", "start");
        if (this.calibration_vectors.isEmpty() && !this.closest_neighbor_avg_entries.isEmpty() ) {
            Log.d("getCalibrationVector()", "init calibrations");

            // absolute value calibration vector
            ArrayList<Integer> theta_cal_yvals = new ArrayList<Integer>();
            ArrayList<Integer> lalpha_cal_yvals = new ArrayList<Integer>();

            // relative calibration vector, i.e. "windows"
            HashMap<String, Integer> lalpha_rel_cal_yvals = new HashMap<String,Integer>();

            for( int idx = 0; idx < this.getX().size(); idx += 1) {
                    int c_theta_val = -1;
                    int c_lalpha_val = -1;
                    if ( (this.getEEGEntries(UserInputAuth.THETA, false).size() - 1) >= idx &&
                            this.getEEGEntries(UserInputAuth.THETA, false).get(idx).getXIndex() == idx ) {
                        c_theta_val = (int) this.getEEGEntries(UserInputAuth.THETA, false).get(idx).getVal();
                        c_lalpha_val = (int) this.getEEGEntries(UserInputAuth.LALPHA, false).get(idx).getVal();
                    }

                    int n_theta_val = -1;
                    for ( Entry e : this.closest_neighbor_avg_entries.get(UserInputAuth.THETA) ) {
                        if (e.getXIndex() == idx) {
                            n_theta_val = (int) e.getVal();
                            break;
                        }
                    }

                    int n_lalpha_val = -1;
                    for ( Entry e : this.closest_neighbor_avg_entries.get(UserInputAuth.LALPHA) ) {
                        if (e.getXIndex() == idx) {
                            n_lalpha_val = (int) e.getVal();

                            //closest neighbor avg entries only has "desired" data, i.e. trimmed entries
                            //Log.d("getCalibrationVector()", String.format("neighbor_avg lalpha size: %d", this.closest_neighbor_avg_entries.get(UserInputAuth.LALPHA).size()) );
                            for ( Entry f : this.closest_neighbor_avg_entries.get(UserInputAuth.LALPHA) ) {
                                if (f.getXIndex() == (idx + 1)){
                                    int nn_lalpha_val = (int) f.getVal();

                                    lalpha_rel_cal_yvals.put(String.format("%d-%d", idx, (idx + 1)), Math.abs(n_lalpha_val - nn_lalpha_val));
                                }
                            }

                            break;
                        }
                    }

                    Log.d("getCalibrationVector()", String.format("%d: %d - %d", idx, c_lalpha_val, n_lalpha_val));
                    theta_cal_yvals.add((int) Math.abs( c_theta_val - n_theta_val ) );
                    lalpha_cal_yvals.add((int) Math.abs( c_lalpha_val - n_lalpha_val ) );

                    // set noise threshold - the greater value from neighbor average vs current uia
                    /*if ( c_theta_val < n_theta_val ) {
                        this.noise_threshold.get(UserInputAuth.THETA).add(n_theta_val + Math.abs( c_theta_val - n_theta_val ) );
                    }
                    else {
                        this.noise_threshold.get(UserInputAuth.THETA).add(c_theta_val);
                    }

                    if ( c_lalpha_val < n_lalpha_val ) {
                        this.noise_threshold.get(UserInputAuth.LALPHA).add(n_lalpha_val + Math.abs(c_theta_val - n_lalpha_val) );
                    }
                    else {
                        this.noise_threshold.get(UserInputAuth.LALPHA).add(c_lalpha_val);

                    }*/

            }

            this.calibration_vectors.put(UserInputAuth.THETA, theta_cal_yvals);
            this.calibration_vectors.put(UserInputAuth.LALPHA, lalpha_cal_yvals);

            this.relative_calibration_vector.put(UserInputAuth.LALPHA, lalpha_rel_cal_yvals);

        }

        return this.calibration_vectors;
    }

    public Integer getCalibratedValue(String eeg_type, int x, int y) {
        int cal_val = -1;
        HashMap<String, ArrayList<Integer> > cal_vect = this.getCalibrationVector();

        for ( Integer xx : cal_vect.get(eeg_type) ) {
            Log.d("getCalibratedValue()", String.format("%s: %d, %d", eeg_type, x, xx));

            // trim off the noise
            //int y_trimmed = 0;
            //Log.d("getCalibratedValue()", String.format("trim noise: %d %d", y, ) );

        }

        if ( (cal_vect.get(eeg_type).size() - 1) >= x) {

            int cal_window_interval = this.getCalibrationVector().get(eeg_type).get(x);
            int cal_val_incrementer = 1;
            cal_val = cal_val_incrementer * cal_window_interval;

            while (cal_val < y) {
                cal_val_incrementer += 1;
                cal_val = cal_val_incrementer * cal_window_interval;
            }

            cal_val = cal_val - cal_window_interval;

        }

        Log.d("getCalibratedValue()", String.format("size of rel cal: %d",
                this.relative_calibration_vector.get(UserInputAuth.LALPHA).size() ) );

        for ( String pts : this.relative_calibration_vector.get(UserInputAuth.LALPHA).keySet() ) {
            Log.d("getCalibratedValue()", String.format("relative calibration: %s, %d",
                    pts,
                    this.relative_calibration_vector.get(UserInputAuth.LALPHA).get(pts)) );
        }

        return cal_val;

    }

    public void calcFreqMSV() {
        // "trim" user input due to bt transmission lag (among other possible influences)
        int start_idx = 1;

        int x_ceiling = this.getX().size();
        if ( this.getX().size() < x_ceiling ) {
            x_ceiling = this.getX().size();
        }

        if ( x_ceiling > 3 ) {
            x_ceiling -= 1;

        }

        // N, e.g. "length" of y_val vector (in this case a subset, i.e. "slice"
        int x_slice = x_ceiling - start_idx;

        // sum of all y_vals^2 in slice
        int theta_y_sq_sum = 0;
        int lalpha_y_sq_sum = 0;
        int halpha_y_sq_sum = 0;
        int lbeta_y_sq_sum = 0;
        int hbeta_y_sq_sum = 0;
        int lgamma_y_sq_sum = 0;
        int mgamma_y_sq_sum = 0;
        for( int idx = start_idx; idx < x_ceiling; idx += 1) {
            theta_y_sq_sum += Math.pow( this.theta_yvals.get(idx).getVal() , 2);
            lalpha_y_sq_sum += Math.pow( this.lalpha_yvals.get(idx).getVal() , 2);
            halpha_y_sq_sum += Math.pow( this.halpha_yvals.get(idx).getVal() , 2);
            lbeta_y_sq_sum += Math.pow( this.lbeta_yvals.get(idx).getVal() , 2);
            hbeta_y_sq_sum += Math.pow( this.hbeta_yvals.get(idx).getVal() , 2);
            lgamma_y_sq_sum += Math.pow( this.lgamma_yvals.get(idx).getVal() , 2);
            mgamma_y_sq_sum += Math.pow( this.mgamma_yvals.get(idx).getVal() , 2);

        }

        // msv
        this.freq_msv.put( UserInputAuth.THETA, (int) Math.sqrt(theta_y_sq_sum / x_slice) );
        this.freq_msv.put( UserInputAuth.LALPHA, (int) Math.sqrt(lalpha_y_sq_sum / x_slice) );
        this.freq_msv.put( UserInputAuth.HALPHA, (int) Math.sqrt(halpha_y_sq_sum / x_slice) );
        this.freq_msv.put( UserInputAuth.LBETA, (int) Math.sqrt(lbeta_y_sq_sum / x_slice) );
        this.freq_msv.put( UserInputAuth.HBETA, (int) Math.sqrt(hbeta_y_sq_sum / x_slice) );
        this.freq_msv.put( UserInputAuth.LGAMMA, (int) Math.sqrt(lgamma_y_sq_sum / x_slice) );
        this.freq_msv.put( UserInputAuth.MGAMMA, (int) Math.sqrt(mgamma_y_sq_sum / x_slice) );

    }

    public HashMap<String,Integer> getFreqMSV() {
        return this.freq_msv;
    }

    public HashMap<String, HashMap<String,String> > getTransitionDirection() {
        return this.transition_direction;
    }

    public HashMap<String, HashMap<String,Integer> > getTransitionRange() {
        return this.transition_range;
    }

}
