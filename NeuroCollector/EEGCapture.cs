using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace NeuroCollector
{

    /*
     * Custom data point containing a standard x, y pair
     * An extra variable for signal quality has been added
     * This value comes from the EEG device
     * Any value but 0 indicates a poor signal quality
     * 
     * :::::From the Neurosky Developer Documentation:::::
     * This unsigned one-byte integer value describes how poor the signal measured by the ThinkGear is. 
     * It ranges in value from 0 to 255. Any non-zero value indicates that some sort of noise contamination is detected. 
     * The higher the number, the more noise is detected. 
     * A value of 200 has a special meaning, specifically that the ThinkGear electrodes aren't contacting a person's skin.
     */
    struct Point {
        public double x;
        public double y;
        public int signalQuality;
    }

    class EEGCapture
    {
        //Raw EEG
        private List<Point> rawEEGPoints = new List<Point>();

        public EEGCapture() {
        }

        // Getters and Setters
        public List<Point> getRawEEGPoints() { return rawEEGPoints; }

        public void clearData() {
            rawEEGPoints.Clear();
        }

        // x will be a time value and y will be voltage reading from the device
        public void addRawPoint(double x, double y, byte signalQuality) {
            Point p;
            p.x = x;
            p.y = y;
            p.signalQuality = signalQuality;
            rawEEGPoints.Add(p);           
        }

        public void exportJson(string filePath) {

            //string sensor = "FP1"; //neurosky has a single sensor
            //string device = "NSKYMW";
            //string sample_rate = "512";
            //string eegwb_ver = "1";
            
            string json = JsonConvert.SerializeObject(rawEEGPoints.ToArray());

            System.IO.File.WriteAllText(filePath, json);
        }

        /*
             // only raw eeg and sensor info
    public JSONObject exportJSON() {
        JSONObject export = new JSONObject();
        

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

         */
    }
}
