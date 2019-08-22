# fx-calculator
Project is compiled with Eclipse on Windows environment. No gitignore was added since the project is for demo purposes only.

To run the application execute the following command from the console: java -jar {yourprojectsfolder}\fx-calculator\target\fx-calculator-0.0.1-SNAPSHOT.jar

To build the project yourself you can run maven clean -> compile -> test -> install. Maven requires Eclipse to run under the jdk. 
Please download and install the latest version of the jdk, then go to Window -> Preferences -> Java -> Installed JRE's. 
Click Add -> Standard VM -> Next. Specify the path to your jdk folder (C:\Java\jdk-11.0.4) then click finish.

If you are running Maven from command line, make sure you have JAVA_HOME environment variable set to the jdk folder.

To debug you may need to change the way static resouces are loaded.
