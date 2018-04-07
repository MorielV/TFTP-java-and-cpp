package bgu.spl171.net.api.bidi.packets;
/**
 *Represents a Packet-which can represent a request or Command.
 */
public abstract class Packet {
	
	protected short opcode;
	 public short getOpCode(){
		 return this.opcode;
		 
	 }

}
