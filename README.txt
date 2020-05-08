TFTP - Team #12
===============

Files
-----

The directory structure for the project is as follows:

Diagrams
|-Iteration 2
| |-TFTP Team Project - UCM - Read Operation.jpg
| |-TFTP Team Project - UCM - Write Operation.jpg
| |-TFTP UML Class Diagram.pdf
| |-Timing Diagram - Error Code 1.jpg
| |-Timing Diagram - Error Code 3 - Read.jpg
| |-Timing Diagram - Error Code 3 - Write.jpg
| |-Timing diagram error code 2.png
| |-Timing diagram error code 6.png
|-Iteration 3
  |-UML Class Diagram.pdf
  |-Timing Diagram - Packet Loss.pdf
  |-Timing Diagram - Packet Delay.pdf
  |-Timing Diagram - Packet Duplication.pdf
src
|-app
| |-exceptions
| | |-DiskFullException.java - The exception thrown when the disk is full
| |-packets
| | |-AcknowledgementPacket.java - Represents an ACK packet in the application
| | |-DataPacket.java - Represents a DATA packet in the application
| | |-ErrorPacket.java - Represents an ERR packet in the application
| | |-Packet.java - The generic packet superclass that all other packets inherit
| | |-ReadRequestPacket.java - Represents an RRQ packet in the application
| | |-WriteRequestPacket.java - Represents a WRQ packet in the application
| |-Client.java - The model for the client application
| |-ClientController.java - The runnable client application
| |-Connection.java - The representation of a Java Thread for use by the server
| |-Definitions.java - Values used by other files in the project
| |-FileParser.java - The class used to read and write to files
| |-GUI.java - The class that handles drawing the menus to the screen and getting user input
| |-Server.java - The model for the server application
| |-ServerController.java - The runnable server application
| |-Socket.java - The socket wrapper class
|-test
  |-AcknowledgementPacketTest.java - The JUnit test cases for ACK packets
  |-ClientTest.java - The JUnit test cases for the client application
  |-DataPacketTest.java - The JUnit test cases for DATA packets
  |-ErrorPacketTest.java - The JUnit test cases for ERR packets
  |-FileParserReadTest.java - The JUnit test cases for FileParser read operations
  |-FileParserWriteTest.java - The JUnit test cases for FileParser write operations
  |-ReadRequestPacketTest.java - The JUnit test cases for RRQ packets
  |-TFTPSuite.java - The JUnit suite which runs all of the other unit tests
  |-WriteFileAcceptanceTest.java - The acceptance tests for the client and server's write requests
  |-WriteRequestPacketTest.java - The JUnit test cases for WRQ packets
Test-The directory that contains all of our test files
|-0-byte.txt
|-20-byte.txt
|-512-byte.txt
|-513-byte.txt
|-1024-byte.txt
|-33554431-byte.txt
buildJars.xml - The ant build script for producing the .jar files
client.jar - The binary file for the client application
errorsimulator.jar - The binary file for the error simulator application
README.txt - This file
server.jar - The binary file for the server application
TEAM.txt - A description of what each team member did for all iterations

Building
--------

This project compiles to 3 .jar files. To build these jars, simply run the ant script file titled
buildJars.xml.

Executing
---------

The server, client, and errorsimulator components of this program are separate .jar files. 

Server:

To start the server, simply type `java -jar server.jar` from the root of the directory.

Client:

To start the client, simply type `java -jar client.jar` from the root of the directory. The
--verbose switch can be provided if you'd like to be logging excessively. Additionally, you can
supply the --testing switch for the client to start in test mode, which makes use of the
errorsimulator.jar application. The server must be running before the client can be started, and the
errorsimulator must be running for testing mode to work. When asked for IP, "localhost" can be typed for "this" computer

Error Simulator:

To start the errorsimulator, simply type `java -jar errorsimulator.jar` from the root of the
directory.

Testing
-------

This program can be unit tested by running the JUnit test suite that's provided in the src/test
directory, named TFTPSuite.java.

For manual testing, the Test directory contains a number of files of different sizes. These files 
should be sufficient to test all of the cases that could be encountered.
