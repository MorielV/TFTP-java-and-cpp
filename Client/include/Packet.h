#ifndef PACKET_H_
#define PACKET_H_
#include <string>

using namespace std;

class Packet{
private:
	short opCode;
public:
	short getOpCode();
	virtual char* getCode()=0;
	Packet(short opCode);
};

class RRQ: public Packet{
public:
	RRQ(string fileName);
	string getFileName();
	char* getCode();
private:
	string fileName;

};
class WRQ: public Packet{
public:
	WRQ(string fileName);
	char* getCode();
private:
	string fileName;
	string getFileName();
};
class ACK: public Packet{
public:
	ACK(short blockNum);
	short getBlockNumber();
	char* getCode();
private:
	short blockNum;
};
class BCAST: public Packet{
public:
	BCAST(char event, string fileName);
	char getEvent();
	string getFileName();
	char* getCode();
private:
	char event;
	string fileName;
};
class DATA: public Packet{
public:
	DATA(short packetSize,short blockNumber, char* data);
	short getPacketSize();
	short getBlockNumber();
	char* getData();
	char* getCode();
private:
	short packetSize;
	short blockNumber;
	char* data;
};
class DELRQ: public Packet{
public:
	DELRQ(string fileName);
	string fileName;
	string getFileName();
	char* getCode();

};
class DIRQ: public Packet{
	public:
		DIRQ();
		char* getCode();
};
class ERROR: public Packet{
public:
	ERROR(short errorCode, string errorMessage);
	short getErrorCode();
	string getErrorMessage();
	char* getCode();
private:
	short errorCode;
	string errorMessage;
};
class DISC: public Packet{
public:
	DISC();
	char* getCode();
};
class LOGRQ: public Packet{
public:
	LOGRQ(string userName);
	char* getCode();
private:
	string userName;
	string getUserName();
};


#endif
