package bgu.spl171.net.api.bidi;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.bidi.packets.ACK;
import bgu.spl171.net.api.bidi.packets.BCAST;
import bgu.spl171.net.api.bidi.packets.DATA;
import bgu.spl171.net.api.bidi.packets.DELRQ;
import bgu.spl171.net.api.bidi.packets.DIRQ;
import bgu.spl171.net.api.bidi.packets.DISC;
import bgu.spl171.net.api.bidi.packets.ERROR;
import bgu.spl171.net.api.bidi.packets.LOGRQ;
import bgu.spl171.net.api.bidi.packets.Packet;
import bgu.spl171.net.api.bidi.packets.RRQ;
import bgu.spl171.net.api.bidi.packets.WRQ;

public class BidiEncDecImpl implements MessageEncoderDecoder<Packet> {

	private byte[] bytes = new byte[1 << 10]; // start with 1k
	private int len = 0;
	short opCode;
	short packetSize=0;
	@Override
	public Packet decodeNextByte(byte nextByte) {
		pushByte(nextByte);		//add the next byte		
		if(len<2)
			return null;
		
		else if (len == 2) { 								// we got opCode
			byte[] firstTwo = { bytes[0], bytes[1] };
			this.opCode = bytesToShort(firstTwo);
		}
		switch (this.opCode) {
		// RRQ
		case 1:
			if(nextByte=='\0'){	
				byte[] fileNameAsArray=Arrays.copyOfRange(bytes, 2, len-1);
				String fileName=new String(fileNameAsArray, 0, fileNameAsArray.length, StandardCharsets.UTF_8);		

				Packet toReturn=new RRQ(fileName);
				clear();				//clear fields
				return toReturn;
			}
		// WRQ
		case 2:
			if(nextByte=='\0'){
				
				byte[] fileNameAsArray=Arrays.copyOfRange(bytes, 2, len-1);		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				String fileName=new String(fileNameAsArray, 0, fileNameAsArray.length, StandardCharsets.UTF_8);		
				Packet toReturn=new WRQ(fileName);
				clear();				//clear fields
				return toReturn;
			}
		// DATA
		case 3:
			if(len==4){                    //ready to update the packetSize field
				byte[] packetSizeInBytes = { bytes[2], bytes[3] };
				 this.packetSize = bytesToShort(packetSizeInBytes); 
				 
			}
			else if(len-6==this.packetSize){
				byte[] blockInBytes = { bytes[4], bytes[5] };
				short blockNum = bytesToShort(blockInBytes);
				byte[] data= Arrays.copyOfRange(bytes,6 , len);
				len=0;
				Packet toReturn=new DATA(this.packetSize,blockNum ,data);
				clear();				//clear fields
				return toReturn;
			}
			break;
		// ACK
		case 4:
			if (len == 4) {
				byte[] blockInBytes = { bytes[2], bytes[3] };
				short blockNum = bytesToShort(blockInBytes);

				Packet toReturn=new ACK(blockNum);
				clear();				//clear fields
				return toReturn;
			}
			break;
		// ERROR
		case 5:
			if(nextByte=='\0'){			//if we got a whole error message PACKET
				short errorCode;
				byte[] errorCodeInBytes={bytes[2], bytes[3]};							//the error code bytes
				byte[] errorMsgAsArray=Arrays.copyOfRange(bytes, 4, len-1);	//error msg as Byte array
				errorCode=bytesToShort(errorCodeInBytes);		
				String errorMsg=new String(errorMsgAsArray, 0, errorMsgAsArray.length, StandardCharsets.UTF_8);	//the error Message
				Packet toReturn=new ERROR(errorCode, errorMsg);
				clear();				//clear fields
				return toReturn;
				
			}
			break;
		// DIRQ
		case 6:
				clear();
				return new DIRQ();
		
		// LOGRQ
		case 7:
			if(nextByte=='\0'){		
				byte[] nameAsArray=Arrays.copyOfRange(bytes, 2, len-1);
				String userName=new String(nameAsArray, 0, nameAsArray.length, StandardCharsets.UTF_8);
				Packet toReturn=new LOGRQ(userName);
				clear();				//clear fields
				return toReturn;
			}
			break;
			
		// DELRQ
		case 8:
			if(nextByte=='\0'){
				byte[] fileNameAsArray=Arrays.copyOfRange(bytes, 2, len-1);
				String fileName=new String(fileNameAsArray, 0, fileNameAsArray.length, StandardCharsets.UTF_8);		
				System.out.println("GOT DELRQ-"+fileName);
				Packet toReturn=new DELRQ(fileName);
				clear();				//clear fields
				return toReturn;
			}
			break;
			
		// BCAST
		case 9:
			if(nextByte=='\0' && len!=3){		//if we finished the BCAST message (and '\0' is not the "deleted" flag)
				byte[] fileNameAsArray=Arrays.copyOfRange(bytes, 3,len-1);
				String fileName=new String(fileNameAsArray, 0, fileNameAsArray.length, StandardCharsets.UTF_8);	
				Packet toReturn=new BCAST(fileName,bytes[3]);
				clear();				//clear fields
				return toReturn;
				
			}
			break;
			
			
			
		// DISC
		case 10:
			clear();				//clear fields			
			return new DISC();

		}
		return null;
		

    }

	@Override
    public byte[] encode(Packet message) {
		byte[] toReturn=null;	//the byteArray to return
		byte[] opCodeBytes= shortToBytes(message.getOpCode());			//opCode as byte array

		switch(message.getOpCode()){
		
		//DATA	
		case 3:{
			toReturn=new byte[((DATA) message).getPacketSize()+6];
			byte[] packetSizeBytes=shortToBytes(((DATA) message).getPacketSize());//packet size as byte array
			byte[] blockNumBytes=shortToBytes(((DATA) message).getBlockNumber());//block number as byte array
			toReturn[0]=opCodeBytes[0];		//opCode 
			toReturn[1]=opCodeBytes[1];		//
			toReturn[2]=packetSizeBytes[0];	//packetSize
			toReturn[3]=packetSizeBytes[1];	//
			toReturn[4]=blockNumBytes[0];	//block #
			toReturn[5]=blockNumBytes[1];	//	
			byte[] data=((DATA) message).getData();	//the data array of the DATA packet
			//copy the data ITSELF to the return DATA Packet
			for(int i=0; i<data.length; i++){
				toReturn[i+6]=data[i];
			}
			break;
		}		
		//ACK	
		case 4:{
			toReturn=new byte[4];
			byte[] blockNumBytes=shortToBytes(((ACK) message).getBlockNumber());
			toReturn[0]=opCodeBytes[0];		//opCode
			toReturn[1]=opCodeBytes[1];		//
			toReturn[2]=blockNumBytes[0];	//block #
			toReturn[3]=blockNumBytes[1];	//
			break;
		}

		//ERROR	
		case 5:{
			byte[] errorCode=shortToBytes(((ERROR) message).getErrorCode());// get the error Code as byte array
			byte[] errorMsg=((ERROR) message).getMsgArray();				//get the error message- as byte array
			toReturn=new byte[5+errorMsg.length];
			toReturn[0]=opCodeBytes[0];	//opCode
			toReturn[1]=opCodeBytes[1];	//
			toReturn[2]=errorCode[0];	//error Code
			toReturn[3]=errorCode[1];	//
			
			//copy the error message ITSELF to the return ERROR Packet
			for(int i=0; i<errorMsg.length; i++){
				toReturn[i+4]=errorMsg[i];
			}
			toReturn[toReturn.length-1]='\0';			//the 0 byte that marks the end of the packet!!
			
			break;
		}
		//BCAST
				case 9 :{
					byte event=((BCAST) message).getEvent();// get the error Code as byte array
					byte[] bcastMsg=((BCAST) message).getFileNameAsByteArray();
					toReturn=new byte[4+bcastMsg.length];
					toReturn[0]=opCodeBytes[0];	//opCode
					toReturn[1]=opCodeBytes[1];	//
					toReturn[2]=event;	//error Code
					

					for(int i=0; i<bcastMsg.length; i++){
						toReturn[i+3]=bcastMsg[i];
					}
					
					toReturn[toReturn.length-1]='\0';			//the 0 byte that marks the end of the packet!!
					
					break;
				}
		}
		return toReturn;
	}

	private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

	public short bytesToShort(byte[] byteArr) {
		short result = (short) ((byteArr[0] & 0xff) << 8);
		result += (short) (byteArr[1] & 0xff);
		return result;
	}

	public byte[] shortToBytes(short num) {
		byte[] bytesArr = new byte[2];
		bytesArr[0] = (byte) ((num >> 8) & 0xFF);
		bytesArr[1] = (byte) (num & 0xFF);
		return bytesArr;
	}

	public void clear(){
		this.len=0;
		this.opCode=0;
		this.packetSize=0;
	}
}