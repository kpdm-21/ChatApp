# The Makefile for compiling and running the Java chat program

# Target to compile all Java files
all:
	javac Chat.java ConnectionHandler.java ConnectionManager.java

# Target to run the main program
run:
	java Chat $(port)

# Target to clean up .class files
clean:
	rm -f *.class
