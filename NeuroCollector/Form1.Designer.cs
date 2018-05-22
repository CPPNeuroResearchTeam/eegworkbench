namespace NeuroCollector
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.Windows.Forms.DataVisualization.Charting.ChartArea chartArea1 = new System.Windows.Forms.DataVisualization.Charting.ChartArea();
            System.Windows.Forms.DataVisualization.Charting.Legend legend1 = new System.Windows.Forms.DataVisualization.Charting.Legend();
            System.Windows.Forms.DataVisualization.Charting.Series series1 = new System.Windows.Forms.DataVisualization.Charting.Series();
            this.ConnectionProgressLabel = new System.Windows.Forms.Label();
            this.ConnectionStatusLabel = new System.Windows.Forms.Label();
            this.reconnectButton = new System.Windows.Forms.Button();
            this.startCaptureButton = new System.Windows.Forms.Button();
            this.abortCaptureButton = new System.Windows.Forms.Button();
            this.resetButton = new System.Windows.Forms.Button();
            this.EEGChart = new System.Windows.Forms.DataVisualization.Charting.Chart();
            this.musicComboBox = new System.Windows.Forms.ComboBox();
            this.musicLabel = new System.Windows.Forms.Label();
            this.minutesNumericalUpDown = new System.Windows.Forms.NumericUpDown();
            this.startTimeLabel = new System.Windows.Forms.Label();
            this.minutesLabel = new System.Windows.Forms.Label();
            this.secondsNumericalUpDown = new System.Windows.Forms.NumericUpDown();
            this.secondsLabel = new System.Windows.Forms.Label();
            this.previewMusicButton = new System.Windows.Forms.Button();
            this.stopMusicButton = new System.Windows.Forms.Button();
            this.trialSettingsLabel = new System.Windows.Forms.Label();
            this.silentReadTimeLabel = new System.Windows.Forms.Label();
            this.silentReadTimeNumericalUpDown = new System.Windows.Forms.NumericUpDown();
            this.silentReadTimeUnitLabel = new System.Windows.Forms.Label();
            this.eventReadTimeLabel = new System.Windows.Forms.Label();
            this.eventReadTimeNumericalUpDown = new System.Windows.Forms.NumericUpDown();
            this.eventReadTimeUnitLabel = new System.Windows.Forms.Label();
            this.volumeLabel = new System.Windows.Forms.Label();
            this.volumeNumericalUpDown = new System.Windows.Forms.NumericUpDown();
            this.volumeUnitLabel = new System.Windows.Forms.Label();
            this.yAxisUpperNumericalUpDown = new System.Windows.Forms.NumericUpDown();
            this.upperYValueLabel = new System.Windows.Forms.Label();
            this.yAxisLowerNumericalUpDown = new System.Windows.Forms.NumericUpDown();
            this.lowerYValueLabel = new System.Windows.Forms.Label();
            this.graphSettingsLabel = new System.Windows.Forms.Label();
            this.fileNameTextBox = new System.Windows.Forms.TextBox();
            this.fileNameLabel = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.EEGChart)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.minutesNumericalUpDown)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.secondsNumericalUpDown)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.silentReadTimeNumericalUpDown)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.eventReadTimeNumericalUpDown)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.volumeNumericalUpDown)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.yAxisUpperNumericalUpDown)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.yAxisLowerNumericalUpDown)).BeginInit();
            this.SuspendLayout();
            // 
            // ConnectionProgressLabel
            // 
            this.ConnectionProgressLabel.AutoSize = true;
            this.ConnectionProgressLabel.Location = new System.Drawing.Point(12, 35);
            this.ConnectionProgressLabel.Name = "ConnectionProgressLabel";
            this.ConnectionProgressLabel.Size = new System.Drawing.Size(288, 13);
            this.ConnectionProgressLabel.TabIndex = 0;
            this.ConnectionProgressLabel.Text = "Please turn on your mindwave mobile and ensure it is paired";
            // 
            // ConnectionStatusLabel
            // 
            this.ConnectionStatusLabel.AutoSize = true;
            this.ConnectionStatusLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.ConnectionStatusLabel.Location = new System.Drawing.Point(12, 9);
            this.ConnectionStatusLabel.Name = "ConnectionStatusLabel";
            this.ConnectionStatusLabel.Size = new System.Drawing.Size(188, 20);
            this.ConnectionStatusLabel.TabIndex = 1;
            this.ConnectionStatusLabel.Text = "Awaiting Connection...";
            // 
            // reconnectButton
            // 
            this.reconnectButton.Enabled = false;
            this.reconnectButton.Location = new System.Drawing.Point(12, 51);
            this.reconnectButton.Name = "reconnectButton";
            this.reconnectButton.Size = new System.Drawing.Size(75, 23);
            this.reconnectButton.TabIndex = 6;
            this.reconnectButton.Text = "Reconnect";
            this.reconnectButton.UseVisualStyleBackColor = true;
            this.reconnectButton.Click += new System.EventHandler(this.reconnectButton_Click);
            // 
            // startCaptureButton
            // 
            this.startCaptureButton.Enabled = false;
            this.startCaptureButton.Location = new System.Drawing.Point(541, 435);
            this.startCaptureButton.Name = "startCaptureButton";
            this.startCaptureButton.Size = new System.Drawing.Size(80, 23);
            this.startCaptureButton.TabIndex = 9;
            this.startCaptureButton.Text = "Start Capture";
            this.startCaptureButton.UseVisualStyleBackColor = true;
            this.startCaptureButton.Click += new System.EventHandler(this.startCaptureButton_Click);
            // 
            // abortCaptureButton
            // 
            this.abortCaptureButton.Enabled = false;
            this.abortCaptureButton.Location = new System.Drawing.Point(627, 435);
            this.abortCaptureButton.Name = "abortCaptureButton";
            this.abortCaptureButton.Size = new System.Drawing.Size(79, 23);
            this.abortCaptureButton.TabIndex = 10;
            this.abortCaptureButton.Text = "Stop Capture";
            this.abortCaptureButton.UseVisualStyleBackColor = true;
            this.abortCaptureButton.Click += new System.EventHandler(this.abortCaptureButton_Click);
            // 
            // resetButton
            // 
            this.resetButton.Enabled = false;
            this.resetButton.Location = new System.Drawing.Point(712, 435);
            this.resetButton.Name = "resetButton";
            this.resetButton.Size = new System.Drawing.Size(75, 23);
            this.resetButton.TabIndex = 12;
            this.resetButton.Text = "Reset";
            this.resetButton.UseVisualStyleBackColor = true;
            this.resetButton.Click += new System.EventHandler(this.ResetButton_Click);
            // 
            // EEGChart
            // 
            chartArea1.Name = "ChartArea1";
            this.EEGChart.ChartAreas.Add(chartArea1);
            legend1.Enabled = false;
            legend1.Name = "Legend1";
            this.EEGChart.Legends.Add(legend1);
            this.EEGChart.Location = new System.Drawing.Point(15, 129);
            this.EEGChart.Name = "EEGChart";
            series1.ChartArea = "ChartArea1";
            series1.ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Spline;
            series1.Legend = "Legend1";
            series1.Name = "Series1";
            this.EEGChart.Series.Add(series1);
            this.EEGChart.Size = new System.Drawing.Size(775, 300);
            this.EEGChart.TabIndex = 13;
            // 
            // musicComboBox
            // 
            this.musicComboBox.FormattingEnabled = true;
            this.musicComboBox.Location = new System.Drawing.Point(436, 8);
            this.musicComboBox.Name = "musicComboBox";
            this.musicComboBox.Size = new System.Drawing.Size(351, 21);
            this.musicComboBox.TabIndex = 14;
            // 
            // musicLabel
            // 
            this.musicLabel.AutoSize = true;
            this.musicLabel.Location = new System.Drawing.Point(352, 11);
            this.musicLabel.Name = "musicLabel";
            this.musicLabel.Size = new System.Drawing.Size(82, 13);
            this.musicLabel.TabIndex = 15;
            this.musicLabel.Text = "Music Selection";
            // 
            // minutesNumericalUpDown
            // 
            this.minutesNumericalUpDown.Location = new System.Drawing.Point(436, 51);
            this.minutesNumericalUpDown.Name = "minutesNumericalUpDown";
            this.minutesNumericalUpDown.Size = new System.Drawing.Size(70, 20);
            this.minutesNumericalUpDown.TabIndex = 17;
            // 
            // startTimeLabel
            // 
            this.startTimeLabel.AutoSize = true;
            this.startTimeLabel.Location = new System.Drawing.Point(352, 56);
            this.startTimeLabel.Name = "startTimeLabel";
            this.startTimeLabel.Size = new System.Drawing.Size(55, 13);
            this.startTimeLabel.TabIndex = 18;
            this.startTimeLabel.Text = "Start From";
            // 
            // minutesLabel
            // 
            this.minutesLabel.AutoSize = true;
            this.minutesLabel.Location = new System.Drawing.Point(436, 35);
            this.minutesLabel.Name = "minutesLabel";
            this.minutesLabel.Size = new System.Drawing.Size(24, 13);
            this.minutesLabel.TabIndex = 19;
            this.minutesLabel.Text = "Min";
            // 
            // secondsNumericalUpDown
            // 
            this.secondsNumericalUpDown.Location = new System.Drawing.Point(512, 51);
            this.secondsNumericalUpDown.Name = "secondsNumericalUpDown";
            this.secondsNumericalUpDown.Size = new System.Drawing.Size(67, 20);
            this.secondsNumericalUpDown.TabIndex = 20;
            // 
            // secondsLabel
            // 
            this.secondsLabel.AutoSize = true;
            this.secondsLabel.Location = new System.Drawing.Point(509, 35);
            this.secondsLabel.Name = "secondsLabel";
            this.secondsLabel.Size = new System.Drawing.Size(26, 13);
            this.secondsLabel.TabIndex = 21;
            this.secondsLabel.Text = "Sec";
            // 
            // previewMusicButton
            // 
            this.previewMusicButton.Location = new System.Drawing.Point(595, 35);
            this.previewMusicButton.Name = "previewMusicButton";
            this.previewMusicButton.Size = new System.Drawing.Size(93, 39);
            this.previewMusicButton.TabIndex = 22;
            this.previewMusicButton.Text = "Preview";
            this.previewMusicButton.UseVisualStyleBackColor = true;
            this.previewMusicButton.Click += new System.EventHandler(this.previewMusicButton_Click);
            // 
            // stopMusicButton
            // 
            this.stopMusicButton.Location = new System.Drawing.Point(694, 35);
            this.stopMusicButton.Name = "stopMusicButton";
            this.stopMusicButton.Size = new System.Drawing.Size(93, 39);
            this.stopMusicButton.TabIndex = 23;
            this.stopMusicButton.Text = "Stop";
            this.stopMusicButton.UseVisualStyleBackColor = true;
            this.stopMusicButton.Click += new System.EventHandler(this.stopMusicButton_Click);
            // 
            // trialSettingsLabel
            // 
            this.trialSettingsLabel.AutoSize = true;
            this.trialSettingsLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.trialSettingsLabel.Location = new System.Drawing.Point(796, 7);
            this.trialSettingsLabel.Name = "trialSettingsLabel";
            this.trialSettingsLabel.Size = new System.Drawing.Size(105, 17);
            this.trialSettingsLabel.TabIndex = 24;
            this.trialSettingsLabel.Text = "Trial Settings";
            // 
            // silentReadTimeLabel
            // 
            this.silentReadTimeLabel.AutoSize = true;
            this.silentReadTimeLabel.Location = new System.Drawing.Point(796, 35);
            this.silentReadTimeLabel.Name = "silentReadTimeLabel";
            this.silentReadTimeLabel.Size = new System.Drawing.Size(88, 13);
            this.silentReadTimeLabel.TabIndex = 25;
            this.silentReadTimeLabel.Text = "Silent Read Time";
            // 
            // silentReadTimeNumericalUpDown
            // 
            this.silentReadTimeNumericalUpDown.Location = new System.Drawing.Point(799, 52);
            this.silentReadTimeNumericalUpDown.Maximum = new decimal(new int[] {
            600,
            0,
            0,
            0});
            this.silentReadTimeNumericalUpDown.Name = "silentReadTimeNumericalUpDown";
            this.silentReadTimeNumericalUpDown.Size = new System.Drawing.Size(103, 20);
            this.silentReadTimeNumericalUpDown.TabIndex = 26;
            this.silentReadTimeNumericalUpDown.Value = new decimal(new int[] {
            30,
            0,
            0,
            0});
            // 
            // silentReadTimeUnitLabel
            // 
            this.silentReadTimeUnitLabel.AutoSize = true;
            this.silentReadTimeUnitLabel.Location = new System.Drawing.Point(908, 54);
            this.silentReadTimeUnitLabel.Name = "silentReadTimeUnitLabel";
            this.silentReadTimeUnitLabel.Size = new System.Drawing.Size(24, 13);
            this.silentReadTimeUnitLabel.TabIndex = 28;
            this.silentReadTimeUnitLabel.Text = "sec";
            // 
            // eventReadTimeLabel
            // 
            this.eventReadTimeLabel.AutoSize = true;
            this.eventReadTimeLabel.Location = new System.Drawing.Point(796, 75);
            this.eventReadTimeLabel.Name = "eventReadTimeLabel";
            this.eventReadTimeLabel.Size = new System.Drawing.Size(109, 13);
            this.eventReadTimeLabel.TabIndex = 30;
            this.eventReadTimeLabel.Text = "eventReadTimeLabel";
            // 
            // eventReadTimeNumericalUpDown
            // 
            this.eventReadTimeNumericalUpDown.Location = new System.Drawing.Point(799, 91);
            this.eventReadTimeNumericalUpDown.Maximum = new decimal(new int[] {
            600,
            0,
            0,
            0});
            this.eventReadTimeNumericalUpDown.Name = "eventReadTimeNumericalUpDown";
            this.eventReadTimeNumericalUpDown.Size = new System.Drawing.Size(103, 20);
            this.eventReadTimeNumericalUpDown.TabIndex = 31;
            this.eventReadTimeNumericalUpDown.Value = new decimal(new int[] {
            30,
            0,
            0,
            0});
            // 
            // eventReadTimeUnitLabel
            // 
            this.eventReadTimeUnitLabel.AutoSize = true;
            this.eventReadTimeUnitLabel.Location = new System.Drawing.Point(908, 93);
            this.eventReadTimeUnitLabel.Name = "eventReadTimeUnitLabel";
            this.eventReadTimeUnitLabel.Size = new System.Drawing.Size(24, 13);
            this.eventReadTimeUnitLabel.TabIndex = 32;
            this.eventReadTimeUnitLabel.Text = "sec";
            // 
            // volumeLabel
            // 
            this.volumeLabel.AutoSize = true;
            this.volumeLabel.Location = new System.Drawing.Point(352, 92);
            this.volumeLabel.Name = "volumeLabel";
            this.volumeLabel.Size = new System.Drawing.Size(42, 13);
            this.volumeLabel.TabIndex = 33;
            this.volumeLabel.Text = "Volume";
            // 
            // volumeNumericalUpDown
            // 
            this.volumeNumericalUpDown.Location = new System.Drawing.Point(436, 90);
            this.volumeNumericalUpDown.Name = "volumeNumericalUpDown";
            this.volumeNumericalUpDown.Size = new System.Drawing.Size(143, 20);
            this.volumeNumericalUpDown.TabIndex = 34;
            this.volumeNumericalUpDown.Value = new decimal(new int[] {
            75,
            0,
            0,
            0});
            // 
            // volumeUnitLabel
            // 
            this.volumeUnitLabel.AutoSize = true;
            this.volumeUnitLabel.Location = new System.Drawing.Point(436, 74);
            this.volumeUnitLabel.Name = "volumeUnitLabel";
            this.volumeUnitLabel.Size = new System.Drawing.Size(146, 13);
            this.volumeUnitLabel.TabIndex = 35;
            this.volumeUnitLabel.Text = "Percentage of system volume";
            // 
            // yAxisUpperNumericalUpDown
            // 
            this.yAxisUpperNumericalUpDown.Location = new System.Drawing.Point(799, 171);
            this.yAxisUpperNumericalUpDown.Maximum = new decimal(new int[] {
            3000,
            0,
            0,
            0});
            this.yAxisUpperNumericalUpDown.Name = "yAxisUpperNumericalUpDown";
            this.yAxisUpperNumericalUpDown.Size = new System.Drawing.Size(106, 20);
            this.yAxisUpperNumericalUpDown.TabIndex = 36;
            this.yAxisUpperNumericalUpDown.Value = new decimal(new int[] {
            700,
            0,
            0,
            0});
            // 
            // upperYValueLabel
            // 
            this.upperYValueLabel.AutoSize = true;
            this.upperYValueLabel.Location = new System.Drawing.Point(799, 152);
            this.upperYValueLabel.Name = "upperYValueLabel";
            this.upperYValueLabel.Size = new System.Drawing.Size(76, 13);
            this.upperYValueLabel.TabIndex = 37;
            this.upperYValueLabel.Text = "Upper Y Value";
            // 
            // yAxisLowerNumericalUpDown
            // 
            this.yAxisLowerNumericalUpDown.Location = new System.Drawing.Point(799, 210);
            this.yAxisLowerNumericalUpDown.Maximum = new decimal(new int[] {
            0,
            0,
            0,
            0});
            this.yAxisLowerNumericalUpDown.Minimum = new decimal(new int[] {
            3000,
            0,
            0,
            -2147483648});
            this.yAxisLowerNumericalUpDown.Name = "yAxisLowerNumericalUpDown";
            this.yAxisLowerNumericalUpDown.Size = new System.Drawing.Size(105, 20);
            this.yAxisLowerNumericalUpDown.TabIndex = 38;
            this.yAxisLowerNumericalUpDown.Value = new decimal(new int[] {
            700,
            0,
            0,
            -2147483648});
            // 
            // lowerYValueLabel
            // 
            this.lowerYValueLabel.AutoSize = true;
            this.lowerYValueLabel.Location = new System.Drawing.Point(799, 194);
            this.lowerYValueLabel.Name = "lowerYValueLabel";
            this.lowerYValueLabel.Size = new System.Drawing.Size(76, 13);
            this.lowerYValueLabel.TabIndex = 39;
            this.lowerYValueLabel.Text = "Lower Y Value";
            // 
            // graphSettingsLabel
            // 
            this.graphSettingsLabel.AutoSize = true;
            this.graphSettingsLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.graphSettingsLabel.Location = new System.Drawing.Point(799, 135);
            this.graphSettingsLabel.Name = "graphSettingsLabel";
            this.graphSettingsLabel.Size = new System.Drawing.Size(117, 17);
            this.graphSettingsLabel.TabIndex = 40;
            this.graphSettingsLabel.Text = "Graph Settings";
            // 
            // fileNameTextBox
            // 
            this.fileNameTextBox.Location = new System.Drawing.Point(799, 287);
            this.fileNameTextBox.Name = "fileNameTextBox";
            this.fileNameTextBox.Size = new System.Drawing.Size(106, 20);
            this.fileNameTextBox.TabIndex = 41;
            // 
            // fileNameLabel
            // 
            this.fileNameLabel.AutoSize = true;
            this.fileNameLabel.Location = new System.Drawing.Point(799, 268);
            this.fileNameLabel.Name = "fileNameLabel";
            this.fileNameLabel.Size = new System.Drawing.Size(54, 13);
            this.fileNameLabel.TabIndex = 42;
            this.fileNameLabel.Text = "File Name";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(954, 475);
            this.Controls.Add(this.fileNameLabel);
            this.Controls.Add(this.fileNameTextBox);
            this.Controls.Add(this.graphSettingsLabel);
            this.Controls.Add(this.lowerYValueLabel);
            this.Controls.Add(this.yAxisLowerNumericalUpDown);
            this.Controls.Add(this.upperYValueLabel);
            this.Controls.Add(this.yAxisUpperNumericalUpDown);
            this.Controls.Add(this.volumeUnitLabel);
            this.Controls.Add(this.volumeNumericalUpDown);
            this.Controls.Add(this.volumeLabel);
            this.Controls.Add(this.eventReadTimeUnitLabel);
            this.Controls.Add(this.eventReadTimeNumericalUpDown);
            this.Controls.Add(this.eventReadTimeLabel);
            this.Controls.Add(this.silentReadTimeUnitLabel);
            this.Controls.Add(this.silentReadTimeNumericalUpDown);
            this.Controls.Add(this.silentReadTimeLabel);
            this.Controls.Add(this.trialSettingsLabel);
            this.Controls.Add(this.stopMusicButton);
            this.Controls.Add(this.previewMusicButton);
            this.Controls.Add(this.secondsLabel);
            this.Controls.Add(this.secondsNumericalUpDown);
            this.Controls.Add(this.minutesLabel);
            this.Controls.Add(this.startTimeLabel);
            this.Controls.Add(this.minutesNumericalUpDown);
            this.Controls.Add(this.musicLabel);
            this.Controls.Add(this.musicComboBox);
            this.Controls.Add(this.EEGChart);
            this.Controls.Add(this.resetButton);
            this.Controls.Add(this.abortCaptureButton);
            this.Controls.Add(this.startCaptureButton);
            this.Controls.Add(this.reconnectButton);
            this.Controls.Add(this.ConnectionStatusLabel);
            this.Controls.Add(this.ConnectionProgressLabel);
            this.Name = "Form1";
            this.Text = "EEG Capture Desktop";
            ((System.ComponentModel.ISupportInitialize)(this.EEGChart)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.minutesNumericalUpDown)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.secondsNumericalUpDown)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.silentReadTimeNumericalUpDown)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.eventReadTimeNumericalUpDown)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.volumeNumericalUpDown)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.yAxisUpperNumericalUpDown)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.yAxisLowerNumericalUpDown)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label ConnectionProgressLabel;
        private System.Windows.Forms.Label ConnectionStatusLabel;
        private System.Windows.Forms.Button reconnectButton;
        private System.Windows.Forms.Button startCaptureButton;
        private System.Windows.Forms.Button abortCaptureButton;
        private System.Windows.Forms.Button resetButton;
        private System.Windows.Forms.DataVisualization.Charting.Chart EEGChart;
        private System.Windows.Forms.ComboBox musicComboBox;
        private System.Windows.Forms.Label musicLabel;
        private System.Windows.Forms.NumericUpDown minutesNumericalUpDown;
        private System.Windows.Forms.Label startTimeLabel;
        private System.Windows.Forms.Label minutesLabel;
        private System.Windows.Forms.NumericUpDown secondsNumericalUpDown;
        private System.Windows.Forms.Label secondsLabel;
        private System.Windows.Forms.Button previewMusicButton;
        private System.Windows.Forms.Button stopMusicButton;
        private System.Windows.Forms.Label trialSettingsLabel;
        private System.Windows.Forms.Label silentReadTimeLabel;
        private System.Windows.Forms.NumericUpDown silentReadTimeNumericalUpDown;
        private System.Windows.Forms.Label silentReadTimeUnitLabel;
        private System.Windows.Forms.Label eventReadTimeLabel;
        private System.Windows.Forms.NumericUpDown eventReadTimeNumericalUpDown;
        private System.Windows.Forms.Label eventReadTimeUnitLabel;
        private System.Windows.Forms.Label volumeLabel;
        private System.Windows.Forms.NumericUpDown volumeNumericalUpDown;
        private System.Windows.Forms.Label volumeUnitLabel;
        private System.Windows.Forms.NumericUpDown yAxisUpperNumericalUpDown;
        private System.Windows.Forms.Label upperYValueLabel;
        private System.Windows.Forms.NumericUpDown yAxisLowerNumericalUpDown;
        private System.Windows.Forms.Label lowerYValueLabel;
        private System.Windows.Forms.Label graphSettingsLabel;
        private System.Windows.Forms.TextBox fileNameTextBox;
        private System.Windows.Forms.Label fileNameLabel;
    }
}

