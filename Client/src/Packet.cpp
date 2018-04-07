#include "Packet.h"
#include "MessageEncoderDecoder.h"
Packet::Packet(short opCode):opCode(opCode){}
short Packet::getOpCode(){
	return this->opCode;

}

//ACK Constructor
ACK::ACK(short blockNumber):Packet(4),blockNum(blockNumber){}
short ACK::getBlockNumber(){
	return this->blockNum;
}
char* ACK::getCode(){
	char* code=new char[4];
MessageEncoderDecoder::shortToBytes(4, code,0,1);			//opcode
MessageEncoderDecoder::shortToBytes(this->blockNum, code,2,3);//blockNum
	return code;
}
//ERROR constructor
ERROR::ERROR(short errorCode, string errorMessage):Packet(5),errorCode(errorCode), errorMessage(errorMessage){}
short ERROR::getErrorCode(){
	return this->errorCode;
}
string ERROR::getErrorMessage(){
	return this->errorMessage;
}
char* ERROR::getCode(){
	char* code=new char[this->getErrorMessage().length()+5];	//the code form of this packet
	MessageEncoderDecoder::shortToBytes(5,code,0,1);	//opCode
	MessageEncoderDecoder::shortToBytes(this->errorCode,code,2,3);	//opCode
	string message=this->errorMessage;	//the error message
	for(unsigned int i=0; i<errorMessage.length(); i++){	//copy each char
		code[i+4]=errorMessage[i];
	}
	code[this->getErrorMessage().length()+4]='\0';
	return code;
}
//LOGRQ Constructor
LOGRQ::LOGRQ(string userName):Packet(7),userName(userName){}
string LOGRQ::getUserName(){
	return this->userName;
}
char* LOGRQ:: getCode(){
	char* code=new char[this->userName.length()+3];

	MessageEncoderDecoder::shortToBytes(7,code,0,1);
	for(unsigned int i=0; i<this->userName.length(); i++){	//copy each char
			code[i+2]=userName[i];
		}
		code[userName.length()+2]='\0';	//add the 0 byte at the end
	return code;
}
//DELRQ Constructor
DELRQ::DELRQ(string fileName):Packet(8),fileName(fileName){}
string DELRQ::getFileName(){
	return this->fileName;
}
char* DELRQ::getCode(){
	char* code=new char[this->fileName.length()+3];
            MessageEncoderDecoder::shortToBytes(8,code,0,1);
		for(unsigned int i=0; i<this->fileName.length(); i++){	//copy each char
				code[i+2]=fileName[i];
			}
			code[fileName.length()+2]='\0';	//add the 0 byte at the end
		return code;

}
//RRQ Constructor
RRQ::RRQ(string fileName): Packet(1), fileName(fileName){}
string RRQ::getFileName(){
	return this->fileName;
}
char* RRQ::getCode(){
	char* code=new char[this->fileName.length()+3];

		MessageEncoderDecoder::shortToBytes(1,code,0,1);
		for(unsigned int i=0; i<this->fileName.length(); i++){	//copy each char
				code[i+2]=fileName[i];
			}
			code[fileName.length()+2]='\0';	//add the 0 byte at the end
		return code;
}
//WRQ Constructor
WRQ::WRQ(string fileName):Packet(2), fileName(fileName){}
string WRQ::getFileName(){
	return this->fileName;
}
char* WRQ::getCode(){
	char* code=new char[this->fileName.length()+3];

		MessageEncoderDecoder::shortToBytes(2,code,0,1);
		for(unsigned int i=0; i<this->fileName.length(); i++){	//copy each char
				code[i+2]=fileName[i];
			}
			code[fileName.length()+2]='\0';	//add the 0 byte at the end
		return code;
}
//DIRQ Constructor
DIRQ::DIRQ():Packet(6){}
char* DIRQ::getCode(){
	char* code=new char[2];
	MessageEncoderDecoder::shortToBytes(6,code,0,1);
	return code;
}
//DATA Constructor
DATA::DATA(short packetSize, short blockNumber,char* data):Packet(3), data(data), packetSize(packetSize),blockNumber(blockNumber){
}
char* DATA::getCode(){

	short codeSize=this->packetSize+6;
	char* code=new char[codeSize];
	MessageEncoderDecoder::shortToBytes(3,code,0,1);
	MessageEncoderDecoder::shortToBytes(this->packetSize,code,2,3);
	MessageEncoderDecoder::shortToBytes(this->blockNumber,code,4,5);
	for(int i=0; i<packetSize; i++){	//copy the data
		code[i+6]=this->data[i];
	}
	return code;




}

short DATA::getBlockNumber(){
	return this->blockNumber;
}
short DATA::getPacketSize(){
	return this->packetSize;
}
char* DATA::getData(){
	return this->data;
}
//BCAST Constructor
BCAST::BCAST(char event, string fileName):Packet(9),event(event),fileName(fileName){}
char BCAST::getEvent(){
	return this->event;
}
string BCAST::getFileName(){
	return this->fileName;
}
char* BCAST::getCode(){
	char* code=new char[fileName.length()+4];
	MessageEncoderDecoder::shortToBytes(9,code,0,1);
	code[2]=event;		//the event
	for(unsigned int i=0; i<fileName.length(); i++){
		code[i+3]=fileName[i];
	}
	code[fileName.length()+3]='\0';	//add the 0 byte at the end
	return code;
}
//DISC Constructor
DISC::DISC():Packet(10){}
char* DISC::getCode(){
	char* code=new char[2];
	MessageEncoderDecoder::shortToBytes(10,code,0,1);
	return code;
}

