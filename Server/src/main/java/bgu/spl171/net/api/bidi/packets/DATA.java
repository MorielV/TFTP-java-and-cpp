package bgu.spl171.net.api.bidi.packets;

public class DATA extends Packet {
	short packetSize;
	short blockNumber;
	byte[] data;
	
	public DATA(short packetSize, short blockNumber, byte[] data){
		super.opcode=3;
		this.packetSize=packetSize;
		this.blockNumber=blockNumber;
		this.data=data;					//the data (byte array)
		
	}
	
	public short getPacketSize(){
		return this.packetSize;
	}
	public short getBlockNumber(){
		return this.blockNumber;
	}
	
	public byte[] getData(){
		return this.data;
	}

}
