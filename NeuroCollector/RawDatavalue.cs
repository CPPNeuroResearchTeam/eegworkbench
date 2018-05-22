using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace NeuroCollector
{
    class RawDataValue : EventArgs
    {
        private int rawYValue;
        private byte signalQuality; // value between 0 and 255 obtained from EEG Device

        public RawDataValue(int _rawYValue, byte _signalQuality) {
            this.rawYValue = _rawYValue;
            this.signalQuality = _signalQuality;
        }

        public int getRawValue() {
            return rawYValue;
        }

        public byte getSignalQuality() {
            return signalQuality;
        }
    }
}
