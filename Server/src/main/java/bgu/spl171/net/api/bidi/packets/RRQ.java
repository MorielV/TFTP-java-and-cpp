package bgu.spl171.net.api.bidi.packets;

import java.io.UnsupportedEncodingException;

public class RRQ extends Packet {

	private String fileName;
	private byte[] fileNameAsByteArray;
	
	public RRQ(String fileName){
		super.opcode=1;
		this.fileName=fileName;
		try{this.fileNameAsByteArray=fileName.getBytes("UTF-8");	//convert to sequence of bytes in utf-8
		} catch (UnsupportedEncodingException e) {}
		
	}

	public String getFileName() {
		return this.fileName;
	}
	
	
	
	
	
}
