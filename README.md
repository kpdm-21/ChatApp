# ChatApp 

COMP429 Programming Assignment - A Chat Application for Remote Message Exchange

# Project Group 3

Members:
Kristina Dela Merced
Natalia Chavez 

# Project Overview

ChatApp is a simple command-line-based chat application that allows users to connect to peers over a network. Users can retrieve their IP and port, connect and send messages to other peers, and list active connections. This project demonstrates basic networking concepts in Java, including socket programming, error handling, and multi-threading.

# Team Contributions

Member 1: Kristina Dela Merced

	1.	Configured the Chat class to manage user commands, initialize the server, and handle incoming connections.
	2.	Implemented commands:
 	  •	help: Displays available commands and descriptions.
	  •	myip: Displays the machine’s IP address.
	  •	myport: Shows the server’s listening port.
	  •	connect : Connects to a specified IP and port.
	  •	list: Lists all active connections with IP and port details.
	3.	Added error handling:
	  •	Ensures connections are only made to valid IPs.
	  •	Prevents self-connections and duplicates, with error messages.
	4.	Structured the main command loop for clear user input processing.

Member 2: Natalia Chavez

  1. 



# Prerequisites

- JAVA JDK 8 or higher installed on your system
- Terminal or command prompt to compile and run the application

# Project Setup and Build Instructions

1.	Download the Project Files:
     - Class.Java
     - ConnectionHandler.Java
     - ConnectionManager.Java
     - Makefile
     
2.	Compile the Program:
	•	Open a terminal or command prompt.
	•	Navigate to the project directory where the Makefile and .java files are located.
	•	Run the following command to compile all Java files: make 

3. In the terminal, start the program with: make run port=<port>
    Replace <port> with the desired port number (example: make run port=4321)

4. Using Commands in the Chat application:
	•	Type help in the prompt to see available commands.

5. Example Usage

	•	View help: Type help to see all available commands and descriptions.
	•	Get Local IP Address: Type myip.
	•	Get Listening Port: Type myport.
	•	Connect to a Peer: Type connect <IP> <port>.
	•	List Active Connections: Type list.
	•	Terminate connection: Type terminate <connection id>
	•	Send Messages: Type send <connection id> <message>
	•	To exit and close all connections : Type exit
