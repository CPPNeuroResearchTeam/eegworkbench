using System;
using System.Collections.Generic;
using System.Threading;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using libStreamSDK;

//needed for bluetooth connection
using System.Management;
using InTheHand.Net.Sockets;

namespace NeuroCollector
{

    class NeuroskyConnection
    {
        public delegate void OnConnectionStatusChangedEventHandler(object source, EventArgs args);
        public delegate void OnNewConnectionProgressEventHandler(object source, EventArgs args);
        public delegate void OnNewRawDataValueEventHandler(object source, EventArgs args);
        public delegate void OnConnectionFinishEventHandler(object source, EventArgs args);
        public event OnConnectionStatusChangedEventHandler StatusChanged;
        public event OnNewConnectionProgressEventHandler NewInformation;
        public event OnNewRawDataValueEventHandler NewDataPoint;
        public event OnConnectionFinishEventHandler ConnectionFinished;

        private String DEVICE_NAME = "MindWave Mobile";
        private NativeThinkgear nativeThinkgear = new NativeThinkgear();

        // Connection tatus 
        public int AWAITING_CONNECTION = 0;
        public int CONNECTED = 1;
        private int connectionState = 0; // default to awaiting connection

        // Connection Variables
        private int connectionId = 0;
        private String comPort = "";
        private String deviceId = "";
        private int TG_ErrorCode = -2;
        private String connectionInfo = "Starting Connection";

        // Data Feed Variables
        bool isReading = false;

        public NeuroskyConnection() {

            connectionId = NativeThinkgear.TG_GetNewConnectionId();

        }

        public int getConnectionId() {
            return connectionId;
        }

        public int getErrorCode() {
            return TG_ErrorCode;
        }

        public int getConnectionState() {
            return connectionState;
        }

        public String getConnectionProgress() {
            return connectionInfo;
        }


        /*
         * Locate the name of the comport that the mindwave mobile is broadcasting to
         * !! Do not run on UI thread !!
         */
        private String locateComPortOnOperatingSystem() {
            
            String portName = "";

            connectionInfo = "Searching for Mindwave Mobile";
            OnNewConnectionInformationAvailable();

            //locate com port for Neurosky if a deviceId was located
            ManagementObjectCollection instances = new ManagementClass("Win32_SerialPort").GetInstances();
            foreach (ManagementObject port in instances)
            {
                String pnpDeviceId = port.GetPropertyValue("PNPDeviceID").ToString();
                if (deviceId.Length > 0 && pnpDeviceId.Contains(deviceId))
                {
                    connectionInfo = "port located";
                    OnNewConnectionInformationAvailable();

                    portName = port.GetPropertyValue("deviceid").ToString();
                    break;
                }
            }

            

            return portName;

        }

        private String getDeviceId() {
            //get attached bluetooth devices
            BluetoothClient bluetoothClient = new BluetoothClient();
            BluetoothDeviceInfo[] devices = bluetoothClient.DiscoverDevices();

            //locate the Neurosky Mindwave Mobile device
            foreach (BluetoothDeviceInfo device in devices)
            {
                if (String.Compare(device.DeviceName, DEVICE_NAME, true) == 0)
                {
                    Console.WriteLine(bluetoothClient.Connected);
                    return device.DeviceAddress.ToString();
                }
            }

            return "";
        }


        public void attemptConnection() {

            Thread bluetootheConnector = new Thread(new ThreadStart(
                    connect
                ));

            bluetootheConnector.Start();

        }

        private void connect() {

            // Disconnect any existing connection
            if (connectionState == CONNECTED) {
                NativeThinkgear.TG_Disconnect(connectionId);
                this.connectionState = AWAITING_CONNECTION;
            }

            //// Obtain new connection ID
            //this.connectionId = NativeThinkgear.TG_GetNewConnectionId();

            // Com port may change if device is reconnected to windows
            this.comPort = "";
            //this.deviceId = "";

            // Search for the device ID of the mindwave mobile
            // If device is not found, continue to search in the background
            updateConnectionInfo("Locating device ID...");
            while (string.IsNullOrEmpty(deviceId))
            {
                this.deviceId = getDeviceId();

                // pause thread inbetween to save processing
                if(String.IsNullOrEmpty(deviceId))
                {
                    Thread.Sleep(500);
                }
            }

            // Search available com ports for device
            // if device is not found, continue to search in the background
            updateConnectionInfo("Locating serial port...");
            while (string.IsNullOrEmpty(comPort))
            {
                comPort = locateComPortOnOperatingSystem();

                // pause thread inbetween attempts to save processing
                if (string.IsNullOrEmpty(comPort))
                {
                    Thread.Sleep(500);
                }
            }

            // Error code 0 indicates a successful connection
            updateConnectionInfo("Attempting connection...");
            TG_ErrorCode = NativeThinkgear.TG_Connect(connectionId,
                            comPort,
                            NativeThinkgear.Baudrate.TG_BAUD_57600,
                            NativeThinkgear.SerialDataFormat.TG_STREAM_PACKETS);
            if (TG_ErrorCode < 0)
            {
                connectionState = AWAITING_CONNECTION;
                updateConnectionInfo("Connection failed");
            }
            else
            {
                connectionState = CONNECTED;
                updateConnectionInfo("Connected");
            }

            OnConnectionStatusChanged();
            OnConnectionFinished();
        }

        private void updateConnectionInfo(String progress) {
            connectionInfo = progress;
            OnNewConnectionInformationAvailable();
        }

        public void startDataFeed() {
            Thread dataFeed = new Thread(new ThreadStart(
                readEEGData
            ));

            isReading = true;
            dataFeed.Start();
        }

        public void stopDataFeed() {
            isReading = false;
        }

        public void readEEGData() {
            while (isReading == true)
            {
                /* Attempt to read a Packet of data from the connection */
                TG_ErrorCode = NativeThinkgear.TG_ReadPackets(connectionId, 1);
                /* If TG_ReadPackets() was able to read a complete Packet of data... */
                if (TG_ErrorCode == 1)
                {
                    /* If raw eeg value has been updated by TG_ReadPackets()... */
                    if (NativeThinkgear.TG_GetValueStatus(connectionId, NativeThinkgear.DataType.TG_DATA_RAW) != 0)
                    {

                        byte signalQuality = (byte)NativeThinkgear.TG_GetValue(connectionId, NativeThinkgear.DataType.TG_DATA_POOR_SIGNAL);

                        /* If the headset has a good signal send data to UI thread */
                        if (signalQuality == 0)
                        {
                            /* Get and print out the updated eeg value */
                            updateConnectionInfo("Strong Signal");
                        }
                        else /* Headset needs adjusting */
                        {
                            updateConnectionInfo("Poor Signal Received: " + signalQuality.ToString());
                        }

                        /* Send new data to the UI thread and update EEG array */
                        OnRawDataReceived((int)NativeThinkgear.TG_GetValue(connectionId, NativeThinkgear.DataType.TG_DATA_RAW), signalQuality);
                    } 

                } 

            } 

        }

        //Delegate Methods
        protected virtual void OnConnectionStatusChanged() {
            if (StatusChanged != null)
                StatusChanged(this, EventArgs.Empty);
        }

        protected virtual void OnNewConnectionInformationAvailable() {
            if (NewInformation != null)
                NewInformation(this, EventArgs.Empty);
        }

        protected virtual void OnRawDataReceived(int value, byte signalQuality) {
            if (NewDataPoint != null)
            {
                RawDataValue newValue = new RawDataValue(value, signalQuality);
                NewDataPoint(this, newValue);
            }
        }

        protected virtual void OnConnectionFinished() {
            if (ConnectionFinished != null)
            {
                ConnectionFinished(this, EventArgs.Empty);
            }
        }

    }
}
