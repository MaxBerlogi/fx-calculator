# fx-calculator

*Warning: if you use M2 Maven plugin for eclipse, M2 Maven test option may fail. Instead use M2 Maven build... goal -> test

Project is compiled with Eclipse on Windows environment.

Project was build with JDK 8.

To build the project yourself you can run maven clean -> compile -> test -> install. Maven requires Eclipse to run under the jdk. 
Please download and install the latest version of the jdk, then go to Window -> Preferences -> Java -> Installed JRE's. 
Click Add -> Standard VM -> Next. Specify the path to your jdk folder then click finish.
If you are running Maven from command line, make sure you have JAVA_HOME environment variable set to the jdk folder.

To run the application after install execute the following command from the console: 
java -jar {yourprojectsfolder}\fx-calculator\target\fx-calculator-0.0.1-SNAPSHOT.jar

To debug you may need to change the way static resouces are loaded.
