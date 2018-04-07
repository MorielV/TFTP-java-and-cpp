

#ifndef MESSAGEENCODERDECODER_H_
#define MESSAGEENCODERDECODER_H_

#include <vector>
#include "Packet.h"
#include <string>

using namespace std;

class MessageEncoderDecoder{
public:
	//constructor
	MessageEncoderDecoder();
	Packet* decodeNextByte(char byte);	//returns Packet when finishing decoding
	char* encode(Packet* packet);	//encode the input
	static  short bytesToShort(char* bytesArr);		//STATIC
	static void shortToBytes(short num, char* bytesArr,int i, int j);
    //fields

private:
	unsigned short blockNumber;
    unsigned short packetSize;
	std::vector<char> vector;
	short opCode;




};





#endif
