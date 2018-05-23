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
    struct JsonObject {
        public string device;
        public string version;
        public string sample_rate;
        public string record_datetime;
        public List<double> FP1;
    }

    class EEGCapture
    {
        // silent read data
        private List<double> silentYVals = new List<double>();
        private List<double> eventYVals = new List<double>();

        // event read data
        private List<byte> silentSignalQualities = new List<byte>();
        private List<byte> eventSignalQualities = new List<byte>();

        public EEGCapture() {
        }

        // Getters and Setters
        public List<double> getSilentYVals() { return silentYVals; }
        public List<double> getEventYVals() { return eventYVals;  }
        public List<byte> getSilentSignalQualities() { return silentSignalQualities; }
        public List<byte> getEventSignalQualities() { return eventSignalQualities; }


        public void clearData() {
            silentYVals.Clear();
            silentSignalQualities.Clear();
            eventYVals.Clear();
            eventSignalQualities.Clear();
        }

        // voltage reading from the device
        public void addSilentPoint(double y, byte signalQuality) {
            silentYVals.Add(y);
            silentSignalQualities.Add(signalQuality);
        }

        public void addEventPoint(double y, byte signalQuality) {
            eventYVals.Add(y);
            eventSignalQualities.Add(signalQuality);
        }

        /*
         * exports all collected data to the json_dir
         * each read will produce three files in a set from the same subject
         * @param json_dir: directory of the json files
         * @param set: each of three files will have this associated set name
         */
        public void exportJson(string json_dir, string set) {
            List<JsonObject> data = new List<JsonObject>();

            JsonObject data1 = new JsonObject();

            string silentPath = json_dir + set + "_silent.json";
            string eventPath = json_dir + set + "_event.json";

            // set tags 
            data1.device = "NSKYMW";
            data1.sample_rate = "512";
            data1.version = "1";
            data1.record_datetime = DateTime.Now.ToString();

            // pring silent read
            data1.FP1 = silentYVals;
            string json = JsonConvert.SerializeObject(data);
            System.IO.File.WriteAllText(silentPath, json);

            // print event read
            data1.FP1 = eventYVals;
            json = JsonConvert.SerializeObject(data);
            System.IO.File.WriteAllText(eventPath, json);
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
