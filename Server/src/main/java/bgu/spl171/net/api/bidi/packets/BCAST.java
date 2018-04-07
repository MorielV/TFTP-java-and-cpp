package bgu.spl171.net.api.bidi.packets;

import java.io.UnsupportedEncodingException;

public class BCAST extends Packet {
	
	byte event;
	String fileName;
	byte[] fileNameAsByteArray;
	public BCAST(String fileName, byte event){
		super.opcode=9;
		this.event=event;
		this.fileName=fileName;
		
		try{this.fileNameAsByteArray=fileName.getBytes("UTF-8");	//convert to sequence of bytes in utf-8
		} catch (UnsupportedEncodingException e) {}
	}
	public String getFileName(){
		return this.fileName;
	}
	public byte getEvent() {
		return this.event;
	}
	public byte[] getFileNameAsByteArray(){
		return this.fileNameAsByteArray;
	}

}
