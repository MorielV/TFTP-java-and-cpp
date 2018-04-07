package bgu.spl171.net.api.bidi.packets;

import java.io.UnsupportedEncodingException;

public class ERROR extends Packet {
	
	short errorCode;
	String errMsg;
	byte[] msgAsByteArray;
	public ERROR(short errorCode, String errMsg){
		super.opcode=5;
		this.errorCode=errorCode;
		this.errMsg=errMsg;
		
		try{this.msgAsByteArray=errMsg.getBytes("UTF-8");	//convert to sequence of bytes in utf-8
		} catch (UnsupportedEncodingException e) {}
		
		
	}
	
	public byte[] getMsgArray(){
		return this.msgAsByteArray;
		
	}

	public short getErrorCode(){
		return this.errorCode;
	}

	public String getErrMsg() {
		
		return this.errMsg;
	}

}
