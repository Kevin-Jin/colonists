Checking out the workspace
==========================
1. Create a GitHub account and message me your account name so I can add you as a collaborator.
2. Download the GitHub client from http://windows.github.com/ and install it.
3. Open the GitHub client and sign into the account you created on GitHub. In the left column under the github header, click your name. Then hover over the Kevin-Jin/celdroids repository and click the Clone link to the right.

Setting up the workspace
========================
1. You need an earlier version of Java to be compatible with Android. Install Java Development Kit 6 Update 45 from [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk6downloads-1902814.html). Choose Windows x86 if you have a 32-bit operating system and Windows x64 if you have a 64-bit operating system.
2. Install the latest version of Eclipse if you haven't already.
3. Download Android SDK Tools from [here](http://developer.android.com/sdk/index.html). Expand **USE AN EXISTING IDE** and click the **Download the SDK Tools for Windows** button. Install.
4. Open Eclipse.
5. Go to **Help** > **Install New Software**. Click **Add** in the top-right corner. Enter *ADT Plugin* next to **Name:** and *https://dl-ssl.google.com/android/eclipse/* next to **Location:**. Click **OK**.
6. Click the checkbox next to **Developer Tools**. Click **Next >**. Click **Next >**. Click radio button next to **I accept the terms of the license agreement** and click **Finish**.
7. Restart Eclipse. In the "Welcome to Android Development" window, choose **Use existing SDKs** and browse to where you installed the SDK Tools.
8. Go to **Window** > **Android SDK Manager**. Check the box next to **Android 4.0 (API 14)** and click **Install # packages...**.
9. Go to **Window** > **Preferences**. Expand **Java** and click **Installed JREs**. Click **Add...**, select **Standard VM**, click **Next >**. Next to **JRE home:**, click **Directory...** and navigate to the JDK 6 folder (usually *C:\Program Files\Java\jdk1.6.0_45*). Then click **Finish** and **OK**.
10. Go to **File** > **Import...**. Expand **General** and double click **Existing Projects into Workspace**. Next to **Select root directory:**, click **Browse...** and select the *Celdroids Common* folder from the workspace you cloned in the GitHub Client.
11. Repeat step 10 for *Celdroids Client Common*, *Celdroids Client Desktop*, *Celdroids Client Android*, and *Celdroids Server*.
12. Try the game out by opening the *net.pjtb.celdroids.client.desktop.DesktopGame* class from the *Celdroids Client Desktop* project and clicking the Run button on the Eclipse toolbar.