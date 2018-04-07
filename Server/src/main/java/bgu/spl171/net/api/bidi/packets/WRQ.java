package bgu.spl171.net.api.bidi.packets;

import java.io.UnsupportedEncodingException;

public class WRQ extends Packet {
	

	String fileName;
	byte[] fileNameAsByteArray;
	
	public WRQ(String fileName){
		super.opcode=2;
		this.fileName=fileName;
	
		try{this.fileNameAsByteArray=fileName.getBytes("UTF-8");	//convert to sequence of bytes in utf-8
		} catch (UnsupportedEncodingException e) {}
	}

	public String getFileName() {
	
		return this.fileName;
	}
	

}
