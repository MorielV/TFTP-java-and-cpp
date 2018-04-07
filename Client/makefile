CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_locale -lboost_thread

all: TFTPclient
	g++ -o bin/TFTPclient  bin/MainClass.o bin/connectionHandler.o bin/socketListener.o bin/MessageEncoderDecoder.o bin/Packet.o bin/keyThread.o $(LDFLAGS) 

TFTPclient:  bin/MainClass.o bin/connectionHandler.o bin/socketListener.o bin/MessageEncoderDecoder.o bin/Packet.o bin/keyThread.o


bin/keyThread.o: src/keyThread.cpp
		g++ $(CFLAGS) -o bin/keyThread.o src/keyThread.cpp

bin/MainClass.o: src/MainClass.cpp
	g++ $(CFLAGS) -o bin/MainClass.o src/MainClass.cpp
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/socketListener.o: src/socketListener.cpp
	g++ $(CFLAGS) -o bin/socketListener.o src/socketListener.cpp
	
bin/MessageEncoderDecoder.o: src/MessageEncoderDecoder.cpp

	g++ $(CFLAGS) -o bin/MessageEncoderDecoder.o src/MessageEncoderDecoder.cpp

bin/Packet.o: src/Packet.cpp

	g++ $(CFLAGS) -o bin/Packet.o src/Packet.cpp
	
	
.PHONY: clean
clean:
	rm -f bin/*
