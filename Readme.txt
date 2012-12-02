App-name: CO2EmissionAlert

Version: V1.2.0

Author: Junlong Xiang, Xiang Gao, Feihua Qu

For Developers:

Our repository is on GitHub: https://github.com/mrr7rhq/CO2Alert

You can also check our version updates in changelog.txt.

This is an Android project developed in Eclipse (ADT), and tested with Samsung Galaxy S3 (Android 4.0.3) and Motorola ME525 (Android 2.3.6).

To test the project in Eclipse, you need a Google API key additionally. Some log files (debugger output) and database files are included in the project zip.

For Users: 1. Launch CO2EmissionAlert app.
           2. Select a transport mode and click the start button.
           3. Wait for initialization of GPS tracking process until the readings at the bottom appears.
           4. The bottom of screen displays some real-time parameters: your location(latitude and longitude), your current distance, duration and corresponding CO2 emission.
           5. There is an icon indicating your transport mode at the starting point of your trip. And when you start to move, there will be a tracking route
           6. Routes of different colors the icons represents different transport mode.
           7. You can shake your phone when you want to know how much CO2 has emitted currently, and there will be a speech prompt to inform you the information.
           8. The app will give a speech & vibration alert to you once you exceeded the predefined threshold for CO2 emission.
           9. By clicking "Transfer" in the option menu, you can change your transport mode.
           10. When you decide to finish your trip, you can click "Stop" in the option menu to stop tracking, and by clicking "Commit", you will see the statistics view of this trip: transport modes, elapsed time, total distance, total CO2 emission, average speed and average rate of CO2 emission. There is also a plot of CO2 emission rate.
           11. By clicking "Exit" in the option menu, you can quit the application.