package bgu.spl171.net.api.bidi.packets;

public class LOGRQ extends Packet {
	
	String username;
	public LOGRQ(String username){
		super.opcode=7;
		this.username=username;
	}
	public String getUserName() {
		return this.username;
	}
	
	

}
