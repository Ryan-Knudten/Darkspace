# CS180-Project5

# Compile and Run
In order to use Darkspace:
- Compile the files found in the application directory
- Run the Server.java file in the Executables folder
- Run the Client.java file in the Executables folder

# Project Submission
## Ryan Knudten - Submitted Vocareum Workspace
## Ryan Knudten - Submitted Report on Brightspace
## Ryan Knudten - Submitted Presentation on Brightspace

# Class Descriptions
## AppData.java
- saveData and readData functions for server data persistence 

## Controller.java
- Holds the server's Model instance (MVC)
- Handles Requests through the handleRequest method which returns a Callback
- handleRequest is synchronized to prevent race conditions

## Client.java
- Run this class to connect as a user
- Host: localhost
- Port: 4242

## Server.java
- Run this class to start the server
- Port: 4242
- Spawns new threads for each user connection through the ServerThread.java class

## Colors.java
- Contains Color.java constants for the GUIs

## GUI.java
- Interface for all GUI classes (LoginGUI, StudentGUI, TeacherGUI)
- Consumers must implement the requestCallback method which is how the GUIs send/receive data from the server

## LoginGUI.java
- GUI for the login screen

## StudentGUI.java
- GUI for the student dashboard

## TeacherGUI.java
- GUI for the teacher dashboard

## Model.java
- All application data is packaged into this class
- Contains arrays of courses, teacher users, and student users.

## Course.java
- Contains array of quizzes and submissions
- Contains one teacher user

## Quiz.java
- Contains an array of questions and submissions

## Submission.java
- Contains an array of responses and points awarded
- States: isGraded and isTaken

## Callback.java
- Holds a copy of the servers model,
- a boolean representing whether or not the request succeeded, and
- a success/fail message

## Request.java
- Holds a RequestType to determine what function is requested
- and the necessary data to perform the function in an ArrayList of Object type

## RequestType.java
- Enum with all available functions that the server can process

## ServerThread.java
- Instances are spawned when a client connects
- Accepts requests and sends callbacks to the clients