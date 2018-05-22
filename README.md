# EEG Workbench

## Description

This project contains a collection of tools to analyze EEG and prototype code for the Android platform. Code currently developed/tested on Java 7,8 and Android 4.4,5.

## EEGCapture

A simple Android application to record raw EEG and export a JSON file. Currently only the Neurosky Mindwave EEG device is supported.

### Installation

After initial Android Studio installation and set up, copy the EEGCapture folder into your Android Studio projects folder, and then File -> Open. Android Studio will probably prompt you to install additional Platform SDK(s) and Gradle version(s).

Download the following dependencies:

- Neurosky libStreamSDK (1.2.0): Available in the "Android Developer Tools" package, http://store.neurosky.com/collections/developer-tools

- MP Android Chart Library (2.2.4): https://github.com/PhilJay/MPAndroidChart 

Go to File -> New -> Module -> Import JAR/AAR Package to make project aware of libraries. Then configure the libraries as project dependencies File -> Project Structure -> Dependencies (tab) -> + (top right) -> Module Dependency.

Enable the Developer Mode feature on Android phone/tablet.

### Use

In Android Studio Run -> App and select desired "Connected Device".

Neurosky device must be paired/active before starting the application. After selected number of trials, press screen with finger to start recording and release to stop recording.

To export JSON EEG files, you can browse the filesystem when connected via USB and drag/drop. Otherwise, use of adb may be required (some devices, like Nexus 7 tablet, emulate external storage):

use "adb shell" to locate files
use "adb pull" to copy files to local directory, e.g. "adb pull storage/emulated/legacy/Music/eeg884409621.json"

Location of "adb" command is where Android Studio installs SDKs, e.g. on Windows Users\user\AppData\Local\Android\sdk1\platform-tools.

## EEG Workbench

A web application to analyze JSON files containing EEG data from EEGCapture.

### Installation

Download the following dependencies:

- Jetty Web Server (9.2.19): http://www.eclipse.org/jetty/download.html
- JWave Wavelet Library: https://github.com/cscheiblich/JWave
- Apache Commons Math Library (3.6.1): http://commons.apache.org/proper/commons-math/download_math.cgi 
- Java Servlet API (3.1.0) 
- JSON Simple Java Library (1.1.1)
- LibSVM Support Vector Machine Library: https://www.csie.ntu.edu.tw/~cjlin/libsvm/
- Neuroph Neural Network Library (2.7): https://sourceforge.net/projects/neuroph/files/neuroph-2.7/ 
- Jquery (3.1.1): http://jquery.com/download/
- Dygraphs charting library: http://dygraphs.com/download.html 
- Pure CSS: https://purecss.io/start/

Adjust htdocs/index.html, make.sh, run.sh if/as necessary. Execute make.sh to compile. To change port or location of eeg_data directory, edit Workbench .java and re-run make.sh. 

### Use

Execute run.sh to start the web application. Access via web browser at http://localhost:9999/workbench/index.html.

## Notes

* On Windows platform, use Git Bash terminal to run git, make.sh, run.sh, adb, etc.

