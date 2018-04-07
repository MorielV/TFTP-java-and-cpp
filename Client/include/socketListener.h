
#ifndef TFTPPROTOCOL_H_
#define TFTPPROTOCOL_H_

#include <connectionHandler.h>
#include "Packet.h"
#include <fstream>
#include "deque"
class socketListener{
public:
	socketListener(ConnectionHandler* handler,MessageEncoderDecoder* encDec);	//Constructor
void process(Packet* packet);
ConnectionHandler* handler;
MessageEncoderDecoder* encDec;
void run();										//listen to the socket
void setDownFile(string fileName);				//set the download file name
void setUpFile(string fileName);				//set the file to be Uploaded
bool shouldTerminate;
bool lastDataUpload=false;
private:
string fileWriteName;		//
string fileWritePath;		//the file we're writing to
std::ofstream fileWrite;	//file stream TO WRITE INTO
std::ifstream fileRead;		//file stream to READ FROM
string fileReadName;       //uploading file name
string fileReadPath;		//uploading file path
string dirqNames;			//collects the fileNames requested by the dirq
deque<char> toUpload;		//contains the file to upload
void fileToVector();		//load file from PC to the char vector


};






#endif
