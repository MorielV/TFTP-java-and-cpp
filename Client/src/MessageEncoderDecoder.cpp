#include "MessageEncoderDecoder.h"
#include <iostream>
MessageEncoderDecoder::MessageEncoderDecoder():blockNumber(),packetSize() , vector(),opCode(){

}

//decode next byte (char array)
Packet* MessageEncoderDecoder::decodeNextByte(char byte){
	vector.push_back(byte);		//add the new byte to the vector
	 if(vector.size()==2){
		 char* firstTwo = new char[2];
		 firstTwo[0]=vector[0];
		 firstTwo[1]=vector[1];
		 opCode=bytesToShort(firstTwo);		//the encoderDecoder opCode
		 delete firstTwo;
	 }
	else if(vector.size()>2){	//if we already have an opCode

		//ACK Packet
		//should be 4 Bytes total
		if(opCode==4){
			if(vector.size()==4){	//if we have an entire ACK Packet
				short blockNumber;
				char* secondTwo = new char[2];
				secondTwo[0]=vector[2];
				secondTwo[1]=vector[3];
				blockNumber=bytesToShort(secondTwo);		//the blockNumber
				delete secondTwo;
				ACK* ACKPacket = new ACK(blockNumber);				//create new ACK
				vector.clear();
				return ACKPacket;
			}
		}
		//ERROR Packet
		else if(opCode==5){
			if(byte=='\0'&& vector.size()>=4){
					char* code=new char[2];
					code[0]=vector[2];	//first byte of the Error code
					code[1]=vector[3];	//second byte
					unsigned int messageSize=vector.size()-5;		//the size of the error message (the vector MINUS first 5 chars and last char)
					//copy the relevant chars
					char* errorMessage=new char[messageSize];
					for(unsigned int i=0; i<messageSize; i++){
						errorMessage[i]=vector[i+4];				//CHECK CORRETNESS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
					}
					string errorMessageStr(errorMessage,messageSize);			// = Operator

					//create new ERROR Packet to return
					short codeAsShort=bytesToShort(code);
					ERROR* errorPacket=new ERROR(codeAsShort,errorMessageStr);
					delete [] errorMessage;
					delete [] code;
					vector.clear();
					return errorPacket;
			}
		}
		//DATA Packet
		else if(opCode==3){
			if(vector.size()==6){				//initialize fields- packetSize and blockNumber
				char* packetSizeArray=new char[2];
				char* blockNumberArray=new char[2];
				packetSizeArray[0]=vector[2];	//first byte of the Packet Size
				packetSizeArray[1]=vector[3];	//second byte
				blockNumberArray[0]=vector[4];	//Block Number
				blockNumberArray[1]=vector[5];	//

				this->packetSize=bytesToShort(packetSizeArray);		//the SHORT value -to fields
				this->blockNumber=bytesToShort(blockNumberArray);	//
				delete [] packetSizeArray;
				delete [] blockNumberArray;
			}
			else if(vector.size()>6 && vector.size()-6==packetSize){			//IF ITS A FINISHED DATA Packet
					char* data=new char[packetSize];			// the DATA of the DATA Packet
					//copy the data from the Vector
					for(unsigned int i=0; i<packetSize; i++){
						data[i]=vector[i+6];
					}
					DATA* dataPacket=new DATA(packetSize, blockNumber, data);	//create the DATA Packet
					vector.clear();
					return dataPacket;
				}

		}
		//BCAST Packet
		else if(opCode==9){
			if(byte=='\0'&& vector.size()!=3){	//if its a finished BCAST message
				char event=vector[2];				//file deleted or added
				unsigned int fileNameSize=vector.size()-4;//name of the file
				char* fileNameAsArray=new char[fileNameSize];
				for(unsigned int i=0; i<fileNameSize; i++){
					fileNameAsArray[i]=vector[i+3];
				}
				string fileName(fileNameAsArray,fileNameSize);		//string operator=.
				delete [] fileNameAsArray;
				BCAST* BCASTPacket=new BCAST(event, fileName);
				vector.clear();
				return BCASTPacket;
			}
		}

	}

	 return 0;
}
char* MessageEncoderDecoder::encode(Packet* packet){
		return packet->getCode();
}
 short MessageEncoderDecoder::bytesToShort(char* bytesArr){
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}
 void MessageEncoderDecoder:: shortToBytes(short num, char* bytesArr,int i, int j){
    bytesArr[i] = ((num >> 8) & 0xFF);
    bytesArr[j] = (num & 0xFF);
}

