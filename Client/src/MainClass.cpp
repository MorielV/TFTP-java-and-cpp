//#include <stdlib.h>
#include <iostream>
#include <socketListener.h>
#include "MessageEncoderDecoder.h"
#include <string>
#include <boost/thread.hpp>
#include "keyThread.h"

using namespace std;


 int main (int argc, char *argv[]) {


	std::string host = argv[1];	//Server IP
	    short port = atoi(argv[2]);//Server port
	    ConnectionHandler* handler=new ConnectionHandler(host, port);		//HANDLER
	    if (!handler->connect()) {
	        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
	        return 1;
	    }
	    //now handler is connected
	    MessageEncoderDecoder* encDec=new MessageEncoderDecoder();				//ENCODER DECODER
   	    socketListener* protocol=new socketListener(handler, encDec);			// "Protocol"
   	    keyThread* key=new keyThread(handler, encDec, protocol);
   	    boost::thread thread1(&socketListener::run, protocol); //CREATE THREAD
   	    boost::thread thread2(&keyThread::run, key); //CREATE THREAD

   	    thread2.join();
   	    thread1.join();

    return 0;
 }




