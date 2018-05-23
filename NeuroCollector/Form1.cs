using System;
using System.IO;
using System.Collections.Generic;
using System.Drawing;
using System.Threading;
using System.Windows.Forms;
using System.Windows.Forms.DataVisualization.Charting;
using WMPLib;

//TODO: Make test button that will allow user to read EEG and check connection
//TODO: Figure out how to add 0 to Y axis

namespace NeuroCollector
{
    public partial class Form1 : Form
    {
        // Delegates
        delegate void SetLabelTextDelegate(string text, Control control); // Used for thread safe altering of label text
        delegate void EnableStatusDelegate(bool enabled, Control control);  // Used for thread safe enabling and disabling of controls
        delegate void AddDataValueDelegate(string value); // Used for inserting new data values into the data list
        delegate void UpdateTimeSeriesDelegate();  // Used for updating the time series chart
        delegate void StartMusicDelegate(); // Used for starting music 
        delegate void SaveChartDelegate(); 

        // Neurosky variables
        NeuroskyConnection connection = new NeuroskyConnection();

        // Time Variables
        private DateTime start;
        private int readTimeMS; // number of milliseconds in the read period

        // EEG Variables
        private List<int> yvalues = new List<int>();
        private EEGCapture currentCapture = new EEGCapture();

        // Music Variables
        private WindowsMediaPlayer wplayer = new WindowsMediaPlayer();
        private bool isPlaying = false;

        // Output variables
        string CHART_DIR = Environment.CurrentDirectory + "\\EEG\\charts\\";
        string JSON_DIR = Environment.CurrentDirectory + "\\EEG\\json\\";

        public Form1()
        {
            InitializeComponent();

            connection.StatusChanged += this.OnConnectionStatusChanged;
            connection.NewInformation += this.OnNewConnectionProgressAvailable;
            connection.NewDataPoint += this.OnNewDataValueReceived;
            connection.ConnectionFinished += this.OnConnectionFinished;

            loadMusicSelection();

            resizeChart();

            updateChart(); // Draw the initial chart with no data

            EEGChart.ChartAreas[0].AxisX.Title = "ms";
            EEGChart.ChartAreas[0].AxisY.Title = "ASIC_EEG_POWER";

            connection.attemptConnection();


            // TODO: remove these, they are no longer necessary
            yAxisUpperNumericalUpDown.ValueChanged += new System.EventHandler(OnChartAreaChanged);
            yAxisLowerNumericalUpDown.ValueChanged += new System.EventHandler(OnChartAreaChanged);
            silentReadTimeNumericalUpDown.ValueChanged += new System.EventHandler(OnChartAreaChanged);
            eventReadTimeNumericalUpDown.ValueChanged += new System.EventHandler(OnChartAreaChanged);




        }

        /* ------------------------- BUTTON CALLBACKS ------------------------- */

        private void reconnectButton_Click(object sender, EventArgs e)
        {
            setControlEnabled(false, reconnectButton);
            setControlEnabled(false, startCaptureButton);
            setControlEnabled(false, abortCaptureButton);

            connection.attemptConnection();
        }

        private void ResetButton_Click(object sender, EventArgs e)
        {
            connection.stopDataFeed();
            setControlEnabled(true, startCaptureButton);
            currentCapture.clearData();
            updateChart();
        }

        private void startCaptureButton_Click(object sender, EventArgs e)
        {
            bool startTrial = true;
            readTimeMS = (int)(silentReadTimeNumericalUpDown.Value + eventReadTimeNumericalUpDown.Value) * 1000; 

            if (String.IsNullOrEmpty(fileNameTextBox.Text))
            {
                const string message = "You cannot start the trial without specifying an export path";
                const string caption = "No export path";
                var result = MessageBox.Show(message, caption,
                                             MessageBoxButtons.OK,
                                             MessageBoxIcon.Error);
                startTrial = false;
            }
            else if (File.Exists(JSON_DIR + fileNameTextBox.Text + "_silent.json"))  // check to see if this is already a set of files
            {
                const string message = "This file already exists \nWould you like to overwrite it?";
                const string caption = "File exists";
                var result = MessageBox.Show(message, caption,
                                             MessageBoxButtons.YesNo,
                                             MessageBoxIcon.Question);

                if (result != DialogResult.Yes)
                    startTrial = false;
            }
            
            if(startTrial)
            {
                start = DateTime.Now;
                enableControls(false);
                connection.startDataFeed();

            }
        }

        private void abortCaptureButton_Click(object sender, EventArgs e)
        {
            stopDataFeed();
            enableControls(true);
            wplayer.controls.stop();
        }

        private void previewMusicButton_Click(object sender, EventArgs e)
        {
            openMusicThread();
        }

        private void stopMusicButton_Click(object sender, EventArgs e)
        {
            wplayer.controls.stop();
        }

        /* ------------------------- NEUROSKY CONNECTION ------------------------- */
        /*   These methods are communicating with the neurosky connection object   */


        /*
         * Called when the connection status of the NeuroskyConnection object changes
         * Updates the user on the status of the Neurosky Mobile
         */
        private void OnConnectionStatusChanged(object source, EventArgs e)
        {
            int connectionState = connection.getConnectionState();
            String status = "";

            switch (connectionState)
            {
                case 0:
                    status = "Awaiting Connection...";
                    break;
                case 1:
                    status = "Connected to Mindwave Mobile";
                    break;
            }

            setControlText(status, ConnectionStatusLabel);
        }

        /*
         * Call when the progress of a Neurosky Connection object changes
         * Updates the user to the status of the current status of a Neurosky Connection connect attempt
         */
        public void OnNewConnectionProgressAvailable(object source, EventArgs e)
        {
            String progress = connection.getConnectionProgress();
            setControlText(progress, ConnectionProgressLabel);
        }

        public void SaveChart() {
            if (EEGChart.InvokeRequired)
            {
                SaveChartDelegate d = new SaveChartDelegate(SaveChart);
                this.Invoke(d, new object[] { });
            }
            else
            {
                string imagePath = CHART_DIR + fileNameTextBox.Text + ".png";
                EEGChart.SaveImage(imagePath, ChartImageFormat.Png);
            }
        }

        /*
         * Unlocks the buttons that are locked by default when attempting to connect to an EEG device
         * If no connection is established, only the reconnect button will be unlocked
         */
        public void OnConnectionFinished(object source, EventArgs e)
        {
            setControlEnabled(true, this.reconnectButton);

            if (connection.getConnectionState() == connection.CONNECTED)
            {
                setControlEnabled(true, this.startCaptureButton);
                setControlEnabled(true, this.abortCaptureButton);
                setControlEnabled(true, this.resetButton);
            }
        }

        /* ------------------------- CONTROL FUNCTIONS ------------------------- */
        /* Functions are primarily modified control functions for a cross threaded application */

        /* 
         * General purpose function for setting the text of a control in a threadsafe manner
         * @param text: The desired text for the control
         * @param control: The control being modified
         */
        private void setControlText(String text, Control control)
        {

            if (control.InvokeRequired)
            {
                SetLabelTextDelegate d = new SetLabelTextDelegate(setControlText);
                this.Invoke(d, new object[] { text, control });
            }
            else
            {
                control.Text = text;
            }
        }

        /* 
         * Gneral purpose function for enabling and disabling a control in a threadsafe manner
         * @param enabled: True if device is being enabled, false otherwise
         * @param control: The control being modified
         */
        private void setControlEnabled(bool enabled, Control control)
        {
            if (control.InvokeRequired)
            {
                EnableStatusDelegate d = new EnableStatusDelegate(setControlEnabled);
                this.Invoke(d, new object[] { enabled, control });
            }
            else
            {
                control.Enabled = enabled;
            }
        }

        /* 
         * Redraws the EEG chart in a threadsafe manner
         */
        private void updateChart()
        {

            if (this.EEGChart.InvokeRequired)
            {
                UpdateTimeSeriesDelegate d = new UpdateTimeSeriesDelegate(updateChart);
                this.Invoke(d, new object[] { });
            }
            else
            {
                EEGChart.Series["Series1"].Points.Clear();
                EEGChart.ChartAreas[0].AxisX.StripLines.Clear();


                // Force Zero line to appear on graph
                StripLine zeroLine = new StripLine();
                zeroLine.Interval = 0; // Causes strip to be drawn only once
                zeroLine.IntervalOffset = 0.0; // Convert seconds into milliseconds
                zeroLine.BorderColor = Color.Black;
                EEGChart.ChartAreas[0].AxisY.StripLines.Add(zeroLine);

                //TODO: Force zero to appear on y axis labeling

                // Shade event region of chart
                StripLine eventStart = new StripLine();
                eventStart.Interval = 0; // Causes strip to be drawn only once
                eventStart.IntervalOffset = currentCapture.getSilentYVals().Count;
                eventStart.StripWidth = currentCapture.getEventYVals().Count; // Amount of time event is being recorded converted to milliseconds
                eventStart.BackColor = Color.FromArgb(100, Color.OrangeRed);
                EEGChart.ChartAreas[0].AxisX.StripLines.Add(eventStart);

                // Adding point offscreen forces chart to draw
                EEGChart.Series["Series1"].Points.AddXY(-1, 0);

                resizeChart();

                int x = 0;
                foreach(double y in currentCapture.getSilentYVals())
                {
                    EEGChart.Series["Series1"].Points.AddXY(x, y);
                    x++;
                }
                foreach (double y in currentCapture.getEventYVals())
                {
                    EEGChart.Series["Series1"].Points.AddXY(x, y);
                    x++;
                }

            }
        }

        public void OnChartAreaChanged(object source, EventArgs e) {
            resizeChart();
            updateChart();
        }


        public void OnNewDataValueReceived(object source, EventArgs e)
        {
            RawDataValue newData = (RawDataValue)e;
            double y = newData.getRawValue();
            double time = (DateTime.Now.Ticks - start.Ticks) / TimeSpan.TicksPerMillisecond;
            byte signalQuality = newData.getSignalQuality();

            if (time > (int)this.silentReadTimeNumericalUpDown.Value * 1000)
            {
                if (!isPlaying)
                {
                    openMusicThread();
                    isPlaying = true;
                }
            }
            if (time > readTimeMS)// Time has ended
            {
                updateChart();

                stopDataFeed();
                enableControls(true);
                if (isPlaying)
                {
                    wplayer.controls.stop();
                    isPlaying = false;
                }

                //Save Json file
                currentCapture.exportJson(JSON_DIR, fileNameTextBox.Text);

                //Save chart image
                SaveChart();
                
            }

            if (!isPlaying)
            {
                currentCapture.addSilentPoint(y, signalQuality);
            }
            else
            {
                currentCapture.addEventPoint(y, signalQuality);
            }
        }


        private void stopDataFeed()
        {
            setControlEnabled(true, startCaptureButton);
            connection.stopDataFeed();
        }

        

        private void loadMusicSelection()
        {

            String[] mp3s = Directory.GetFiles("mp3");
            musicComboBox.DataSource = mp3s;

        }

        private void openMusicThread() {
            Thread musicThread = new Thread(new ThreadStart(
                playMusic
                ));

            musicThread.Start();
        }

        private void playMusic() {
            if (this.musicComboBox.InvokeRequired)
            {
                StartMusicDelegate d = new StartMusicDelegate(playMusic);
                this.Invoke(d, new object[] { });
            }
            else
            {
                // get path to mp3 file
                wplayer.URL = (string)musicComboBox.SelectedItem;

                // decide starting time in song
                int min = (int)minutesNumericalUpDown.Value;
                int sec = (int)secondsNumericalUpDown.Value;

                // set playback time to start here
                wplayer.controls.currentPosition = min * 60 + sec; // currentPosition is in seconds

                // get volume value
                int volume = (int)volumeNumericalUpDown.Value;

                // set playback volume
                wplayer.settings.volume = volume;

                wplayer.controls.play();
            }
        }

        private void resizeChart() {
            int xMax = currentCapture.getSilentYVals().Count + currentCapture.getEventYVals().Count;

            this.EEGChart.ChartAreas[0].AxisX.Minimum = 0; // Time always starts at 0           
            this.EEGChart.ChartAreas[0].AxisX.Maximum = xMax;

            this.EEGChart.ChartAreas[0].AxisY.Minimum = (int)yAxisLowerNumericalUpDown.Value; // Ensure chart is symmetrical
            this.EEGChart.ChartAreas[0].AxisY.Maximum = (int)yAxisUpperNumericalUpDown.Value;
        }

        private void enableControls(bool enabled) {
            setControlEnabled(enabled, reconnectButton);
            setControlEnabled(enabled, previewMusicButton);
            setControlEnabled(enabled, stopMusicButton);
            setControlEnabled(enabled, musicComboBox);
            setControlEnabled(enabled, minutesNumericalUpDown);
            setControlEnabled(enabled, secondsNumericalUpDown);
            setControlEnabled(enabled, volumeNumericalUpDown);
            setControlEnabled(enabled, silentReadTimeNumericalUpDown);
            setControlEnabled(enabled, eventReadTimeNumericalUpDown);
            setControlEnabled(enabled, yAxisLowerNumericalUpDown);
            setControlEnabled(enabled, yAxisUpperNumericalUpDown);
            setControlEnabled(enabled, resetButton);
            setControlEnabled(enabled, startCaptureButton);
            setControlEnabled(enabled, fileNameTextBox);
        }


    }
}
