# README #
This project implements the TFTP protocol

### Basic Requirements ###

* JDK 8(The codes was developed and tested on the version, while it might work on a lower version but not guaranteed)

### Terms for README ###

* {BaseDir} stands for the base directory of the project which the the folder where ant build.xml and README.md reside

### Use ant to run the test (the easiest way to do the test) ###

* Requirements

> Ant 1.9.3 or higher version(a build tool for java project) should be installed
> Before running any of the test commands, please go in to the {BaseDir} using a terminal on linux or a command line tool on windows

* Start the server

> `ant run-server`
>
> Note: The server directory is {BaseDir}/files/remote which is defined in the build.xml

* Test sending the file from client to server using the mode of Netascii
ant run-client-send-na
Note: The client path is {BaseDir}/files/local/na-w.txt and the server file name is na-w.txt.
      Since the server directory is {BaseDir}/files/remote, the whole path of the sent file on the server is {BaseDir}/files/remote/na-w.txt
      All of the paths are defined in build.xml as well.
* Test sending the file from client to server using the mode of Octet
ant run-client-send-ot
Note: The client path is {BaseDir}/files/local/ot-w.txt and the server file name is ot-w.txt.
      Since the server directory is {BaseDir}/files/remote, the whole path of the sent file on the server is {BaseDir}/files/remote/ot-w.txt
      All of the paths are defined in build.xml as well.
* Test copying the file from server to client using the mode of Netascii
ant run-client-receive-na
Note: The server file is na-r.txt. As the server directory is {BaseDir}/files/remote, the whole path of the file is {BaseDir}/files/remote/na-r.txt
      The client path is {BaseDir}/files/local/na-r.txt.
      All of the paths are defined in build.xml as well.
* Test copying the file from server to client using the mode of Octet
ant run-client-receive-ot
Note: The server file is ot-r.txt. As the server directory is {BaseDir}/files/remote, the whole path of the file is {BaseDir}/files/remote/ot-r.txt
      The client path is {BaseDir}/files/local/ot-r.txt.
      All of the paths are defined in build.xml as well.
* Want to try the tests again? Please run the following command to delete the copied files
ant clean-copy
Note: This will delete all of the transferred files like {BaseDir}/files/local/na-r.txt which was transferred from the server to the client at on the the tests above.
      The reason of doing it is that if the file already exists, the server/client won't try to overwrite the file
* Want to user you own files?
1. Please put your files used by the client under the folder of {BaseDir}/files/local and put your files used by the server under the folder of {BaseDir}/files/remote.
2. Change both the server file name and client file name according to your file name.
3. Run the steps above to do the tests.

### Use IntelliJ(Jetbrains) to run the test ###

* Requirements
Please download and install the newest version of IntelliJ
* Download the code and put it in your disk.
* Open IntelliJ and choose the menu File->New->Project From Existing Source(the menu might vary a little on different Operation System, but it shouldn't be hard to find it)
* Configure and start the server
1. Open src/com/bgoverseas/TFTPServerText
2. Open Run->Edit Configuration
3. Under the Application add the TFTPServerTest for the java class of 'com.bgoverseas.TFTPServerTest'
4. Enter 'files/remote' as the Program Arguments which is the server directory
5. Run the TFTPServerText class and the server will be started
* Test sending the file from client to server using the mode of Netascii
1. Open src/com/bgoverseas/TFTPClientText
2. Open Run->Edit Configuration
3. Enter "-s -a localhost files/local/na-r.txt na-r.txt" as the Program Arguments value
4. Run the client
* Test sending the file from client to server using the mode of Octet
1. Open src/com/bgoverseas/TFTPClientText
2. Open Run->Edit Configuration
3. Enter "-s -b localhost files/local/ot-r.txt ot-r.txt" as the Program Arguments value
4. Run the client
* Test copying the file from server to client using the mode of Netascii
1. Open src/com/bgoverseas/TFTPClientText
2. Open Run->Edit Configuration
3. Enter "-r -a localhost files/local/na-w.txt na-w.txt" as the Program Arguments value
4. Run the client
* Test copying the file from server to client using the mode of Octet
1. Open src/com/bgoverseas/TFTPClientText
2. Open Run->Edit Configuration
3. Enter "-r -b localhost files/local/ot-w.txt ot-w.txt" as the Program Arguments value
4. Run the client
