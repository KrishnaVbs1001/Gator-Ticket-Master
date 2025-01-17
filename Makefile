JC = javac
JFLAGS = -g
SOURCE = GatorTicketMaster.java
MAIN = GatorTicketMaster

.SUFFIXES: 

default: compile

compile:
	$(JC) $(JFLAGS) $(SOURCE)

clean:
		del /Q *.class *_output_file.txt
