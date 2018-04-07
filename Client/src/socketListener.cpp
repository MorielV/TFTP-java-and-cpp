#include "socketListener.h"

socketListener::socketListener(ConnectionHandler* handler, MessageEncoderDecoder* encDec):handler(handler),encDec(encDec),shouldTerminate(false),lastDataUpload(lastDataUpload),fileWriteName(),fileWritePath("Files/"),fileWrite(),fileRead(),dirqNames(), toUpload(){
}

void socketListener::process(Packet* packet){

	short opCode=packet->getOpCode();	//the opcode of the received packet

	//GOT DATA
	if(opCode==3 && handler->lastSent==1){//the last command was a read request
		DATA* data=static_cast<DATA*>(packet);
		short packetSize=data->getPacketSize();	//the data packetSize
		short blockNum=data->getBlockNumber();	//the data blockNumber
		if(blockNum==1)
			this->fileWrite.open(fileWriteName);

		if(packetSize==0){
			handler->print("READING EMPTY FILE");
			char toWrite[1];
			toWrite[0]='\n';
			packetSize=1;
		}
		char* toWrite=data->getData();

		this->fileWrite.write(toWrite,packetSize);					//write into the file
		ACK* ackData=new ACK(blockNum);								//send this ACK
		handler->sendBytes(encDec->encode(ackData), 4);				//send this ACK (4 Bytes)
		if(packetSize<512){

			handler->lastSent=0;	//"delete" the last command that we sent ------------------?????????????????????????
			handler->print("RRQ "+this->fileWriteName+" complete");
			this->fileWrite.close();
		}

		delete ackData;
		delete toWrite;
	}
	//GOT DATA
	else if(opCode==3 && handler->lastSent==6){	//if awaiting DIRQ file names+
		DATA* dirqFile=static_cast<DATA*>(packet);
		short packetSize=dirqFile->getPacketSize();	//the data packetSize
		if(packetSize>0){
			short blockNum=dirqFile->getBlockNumber();	//the data blockNumber
			char* names=dirqFile->getData();
			for(int i=0; i<packetSize; i++){
				if(names[i]=='\0')
					this->dirqNames.push_back('\n');
				else
					this->dirqNames.push_back(names[i]);
			}

			ACK* ackData=new ACK(blockNum);					//send this ACK
			handler->sendBytes(encDec->encode(ackData), 4);	//
			if(packetSize<512){
				handler->lastSent=0;	//"delete" the last command that we sent ------------------?????????????????????????
				handler->print(this->dirqNames);
				dirqNames.clear();
			}
			delete ackData;
			}
		else
			handler->lastSent=0;
				delete dirqFile;
	}
	else if(opCode==9){				//if we received a BCAST
		BCAST* cast=static_cast<BCAST*>(packet);
		char event=cast->getEvent();		//get the event
		string fileName=cast->getFileName();//the file name

		string eventString;
		if(event==0)		//if a file was deleted
			eventString="BCAST del ";
		else
			eventString="BCAST add ";
		handler->print(eventString+fileName);

	}
	//ERROR Packet
	else if(opCode==5){
		ERROR* ERRORPacket=static_cast<ERROR*>(packet);
		short errorCode=ERRORPacket->getErrorCode();
		handler->print("Error "+std::to_string(errorCode));

	}
	//ACK Packet
	else if(opCode==4 && handler->lastSent==2){ //the last command was a write request
		ACK* ack=static_cast<ACK*>(packet); //cast to ACK packet
		short blockNum = ack->getBlockNumber();

		if(blockNum==0){ 						//if its ACK 0
			fileToVector();						//Load the file into a vector

		}
		handler->print("ACK "+std::to_string(blockNum)+" ");
		short numOfBytes;
		if(toUpload.size()>=512)
			numOfBytes=512;
		else{
			numOfBytes=toUpload.size();
			handler->lastSent=0;
			this->lastDataUpload=true;	//means the next ACK symbols the end of an upload

		}

		char data[numOfBytes];

		for(int i=0; i<numOfBytes; i++){
			data[i]=toUpload.front();				//get char from vector
			toUpload.pop_front();
		}
		DATA* dataPacket=new DATA(numOfBytes,blockNum+1,data );	//the DATA Packet to send									/// IF NOT DATA SENTTTTTT THIS IS THE PROBLEM!!!! (MORIEL_
		handler->sendBytes(encDec->encode(dataPacket), numOfBytes+6);		//send to server
		delete dataPacket;
		delete packet;

	}
	else if(opCode==4 && handler->lastSent==10){	//RECEIVED AN ACK
				ACK* ack=static_cast<ACK*>(packet); //cast to ACK packet
				short blockNum = ack->getBlockNumber();
				handler->print("ACK "+std::to_string(blockNum));		//print ACK #

				this->shouldTerminate=true;					//MAKE THIS THREAD TERMINATE
			}
	else if(opCode==4 && this->lastDataUpload){	//RECEIVED AN ACK

		lastDataUpload=false;
		ACK* ack=static_cast<ACK*>(packet); //cast to ACK packet
		short blockNum = ack->getBlockNumber();
		handler->print("ACK "+std::to_string(blockNum));
		handler->print("WRQ "+this->fileReadName+" complete");


	}
	else if(opCode==4 ){	//RECEIVED AN ACK

			ACK* ack=static_cast<ACK*>(packet); //cast to ACK packet
			short blockNum = ack->getBlockNumber();
			handler->print("ACK "+std::to_string(blockNum));


		}


}
void socketListener::setDownFile(string fileName){
this->fileWriteName=fileName;
}
void socketListener::setUpFile(string fileName){
	this->fileReadName=fileName;

}
void socketListener::run(){


	while(!shouldTerminate){

		char charArray[1];
		bool listen=handler->getBytes(charArray,1);	//read 1 byte at a time
		if(listen==false){
			shouldTerminate=true;
		}


		Packet * gotPacket=this->encDec->decodeNextByte(charArray[0]); //try to make a Packet
		if(gotPacket){	//if we have constructed a new Packet!

			process(gotPacket);
		}
	}

}
void:: socketListener::fileToVector(){			//load file into a char vector



	ifstream infile(fileReadName, ifstream::binary);
	infile.seekg(0,infile.end);
	long size=infile.tellg();
	infile.seekg(0);
	char* buffer=new char[size];
	infile.read(buffer,size);
	for(int i=0; i<size; i++){
		this->toUpload.push_back(buffer[i]);

	}




}
