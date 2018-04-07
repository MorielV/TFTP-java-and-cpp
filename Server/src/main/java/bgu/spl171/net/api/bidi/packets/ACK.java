package bgu.spl171.net.api.bidi.packets;

public class ACK extends Packet{
	
	
	short blockNumber;
	
	public ACK(short blockNumber){
		super.opcode=4;
		this.blockNumber=blockNumber;
	}
	public short getBlockNumber(){
		return this.blockNumber;
	}

}
