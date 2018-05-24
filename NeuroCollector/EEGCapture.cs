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

    //TODO: fix how this prints json
    struct JsonObject {
        public List<Diode> data; 
    }
    
    struct Diode {
        public string device;
        public string version;
        public string sample_rate;
        public string record_datetime;
        public List<long> FP1;
    }

    class EEGCapture
    {
        // silent read data
        private List<long> silentYVals = new List<long>();
        private List<long> eventYVals = new List<long>();

        // event read data
        private List<byte> silentSignalQualities = new List<byte>();
        private List<byte> eventSignalQualities = new List<byte>();

        public EEGCapture() {
        }

        // Getters and Setters
        public List<long> getSilentYVals() { return silentYVals; }
        public List<long> getEventYVals() { return eventYVals;  }
        public List<byte> getSilentSignalQualities() { return silentSignalQualities; }
        public List<byte> getEventSignalQualities() { return eventSignalQualities; }


        public void clearData() {
            silentYVals.Clear();
            silentSignalQualities.Clear();
            eventYVals.Clear();
            eventSignalQualities.Clear();
        }

        // voltage reading from the device
        public void addSilentPoint(long y, byte signalQuality) {
            silentYVals.Add(y);
            silentSignalQualities.Add(signalQuality);
        }

        public void addEventPoint(long y, byte signalQuality) {
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
            JsonObject obj = new JsonObject();

            obj.data = new List<Diode>();

            Diode d = new Diode();

            string silentPath = json_dir + set + "_silent.json";
            string eventPath = json_dir + set + "_event.json";

            // set tags 
            d.device = "NSKYMW";
            d.sample_rate = "512";
            d.version = "1";
            d.record_datetime = DateTime.Now.ToString();

            // pring silent read
            d.FP1 = silentYVals;
            obj.data.Add(d);
            string json = JsonConvert.SerializeObject(obj);
            System.IO.File.WriteAllText(silentPath, json);

            obj.data.Clear();

            // print event read
            d.FP1 = eventYVals;
            obj.data.Add(d);
            json = JsonConvert.SerializeObject(obj);
            System.IO.File.WriteAllText(eventPath, json);
        }
    }
}
