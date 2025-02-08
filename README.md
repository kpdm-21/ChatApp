# ChatApp 

COMP429 Programming Assignment - A Chat Application for Remote Message Exchange

# Project Group 3

Members:
Kristina 
Natalia 

# Project Overview

ChatApp is a simple command-line-based chat application that allows users to connect to peers over a network. Users can retrieve their IP and port, connect and send messages to other peers, and list active connections. This project demonstrates basic networking concepts in Java, including socket programming, error handling, and multi-threading.

# Team Contributions

Member 1: Kristina

	1.	Configured the Chat class to manage user commands, and initialize the server.
 	2.	Configured TCP sockets, initialized server socket to listen for incoming connections, and continuously accepted incoming connections.
	3.	Implemented commands
 	  •	help: Displays available commands and descriptions.
	  •	myip: Displays the machine’s IP address.
	  •	myport: Shows the server’s listening port.
	  •	connect : Connects to a specified IP and port.
	  •	list: Lists all active connections with IP and port details.
	4.	Added error handling:
	  •	Validate port numbers 
	  •	Ensures connections are only made to valid IPs.
	  •	Prevents self-connections and duplicates, with error messages.

Member 2: Natalia 

  	1. Configured the existing classes to manage user notifications of incoming messages and connection(s) statuses.
	2. Implemented Commands
		• terminate: Closes/terminate the specific connection based on listed ID. Notifies the peer of the termination status.
		• send: Sends a message to a specified connection based on the ID listed with up-to 100 characters long limit (includes whitespace).
		• exit: Closes all connections and terminates this application. Notifies the peer(s) of the termination status to connections.
	3. Added error handling:
		• Prevents sending messages to non-existent connections, with error messages.
		• Ensures messages are within the 100 character limit, with error messages.
		• Ensures exit command reaches and relays termination status to all connections.
	4. Provided editting/voiceover for Demo Video



# Prerequisites

- JAVA JDK 8 or higher installed on your system
- Terminal or command prompt to compile and run the application

# Project Setup and Build Instructions

### 1.	Download the Project Files:
- Class.Java
- ConnectionHandler.Java
- ConnectionManager.Java
- Makefile
       
#### 2.	Compile the Program:
- Open a terminal or command prompt.
- Navigate to the project directory where the `Makefile` and `.java` files are located.
- Run the following command in the terminal to compile all Java files: make
- Alternatively , you can compile all files by first inputting: javac Chat.java and then java Chat.java

### 3. Start the Program:
- In the terminal, start the program with: make run port=<port>
- Replace `<port>` with the desired port number (example: `make run port=4321`).

- If using the alternatively method, you can start the program with: java Chat <port>
- Replace `<port>` with the desired port number (example: `java Chat 4321`).
  
### 4. Using Commands in the Chat application:
- Type `help` in the prompt to see available commands.

### 5. Example Usage

- View help: Type `help` to see all available commands and descriptions.
- Get Local IP Address: Type `myip`.
- Get Listening Port: Type `myport`.
- Connect to a Peer: Type `connect <IP> <port>`.
- List Active Connections: Type `list`.
- Terminate connection: Type `terminate <connection id>`.
- Send Messages: Type `send <connection id> <message>`.
- Exit and close all connections: Type `exit`.
