package bgu.spl171.net.api.bidi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl171.net.api.bidi.packets.ACK;
import bgu.spl171.net.api.bidi.packets.BCAST;
import bgu.spl171.net.api.bidi.packets.DATA;
import bgu.spl171.net.api.bidi.packets.DELRQ;
import bgu.spl171.net.api.bidi.packets.ERROR;
import bgu.spl171.net.api.bidi.packets.LOGRQ;
import bgu.spl171.net.api.bidi.packets.Packet;
import bgu.spl171.net.api.bidi.packets.RRQ;
import bgu.spl171.net.api.bidi.packets.WRQ;

public class BidiImpl implements BidiMessagingProtocol<Packet> {

	ConcurrentLinkedQueue<DATA> dataQ; // awaiting to be sent to client

	private boolean isDownloading = false; // indicates if the user is downloading
	private boolean isUploading = false; // indicates if the user is uploading
	private File uploadingFile; // name of the file currently uploading
	private File downloadingFile;
	private FileInputStream inputStream;
	private FileOutputStream outputStream; // when writing to a file
	private ConnectionsImpl<Packet> connections;
	private int connectionId; // this protocol instance's ID
	private boolean shouldTerminate = false;
	private boolean loggedIn = false; // indicates if the user did LOGRQ
	private short nextBlockNum;
	private short nextAckNum;

	public void start(int connectionId, Connections<Packet> connections) {
		this.connections = (ConnectionsImpl<Packet>) connections;
		this.connectionId = connectionId;
		this.dataQ = new ConcurrentLinkedQueue<DATA>();
	}

	public void process(Packet message) {
		if(message.getOpCode()>10){			//if this is a BAD packet
			ERROR badOpCode= new ERROR((short)4,"Illegal TFTP operation");
			this.connections.send(connectionId, badOpCode);
		}
		else{
		switch (message.getOpCode()) {
		// RRQ
		case 1:
			if (this.loggedIn) {
				String fileName = ((RRQ) message).getFileName();
				System.out.println("Looking for"+fileName);
				this.downloadingFile = findFile(fileName);
				
				if (downloadingFile == null) {
					ERROR error = new ERROR((short) 1, "File not found");
					this.connections.send(connectionId, error);
				} else { // file found
					this.nextAckNum=1;
					this.isDownloading = true;
					this.connections.addDownloadingFile(fileName);// add file to currently downloading map
					byte[] fileAsBytes;
					if(downloadingFile.exists()&&downloadingFile.length()==0){
						
						fileAsBytes=new byte[1];
						fileAsBytes[0]='\0';
						
					}
					else
							fileAsBytes = new byte[(int) downloadingFile.length()];
					try {
						
						this.inputStream = new FileInputStream(downloadingFile);

						this.inputStream.read(fileAsBytes);
					} catch (IOException e) {
					}

					addDATA(fileAsBytes);
					this.connections.send(connectionId, this.dataQ.poll());

				}

			} else
				notLogged();

			break;
		// WRQ
		case 2: {
			if (this.loggedIn) { // if the user is logged in

				String fileName = ((WRQ) message).getFileName();
				// make sure the file does not exist yet
				File dir = findFile(fileName);
				if (dir != null) {
					this.connections.send(connectionId,
							new ERROR((short) 5, "File already exists–File name exists on WRQ."));
				} else {
					// write to file
					this.nextBlockNum=1;
					this.isUploading = true;// mark this client as "uploading"
					connections.send(connectionId, new ACK((short) 0)); // SEND ACK TO CLIENT- ready for data packets
					this.connections.addUploadingFile(fileName);
					try {
						this.uploadingFile = new File("Files"+File.separator+fileName);
						this.outputStream = new FileOutputStream(this.uploadingFile); // make the outputStream point to this file.
					} catch (FileNotFoundException e) {
						// ERROR??
					}
				}
			}

			else
				this.notLogged();
		}
			break;
		// DATA
		case 3:
			if (this.isUploading&&this.nextBlockNum==((DATA)message).getBlockNumber()) { // if the server is waiting for Data packets to write to a file
				this.nextBlockNum++;
				byte[] data = ((DATA) message).getData();
				short packetSize = ((DATA) message).getPacketSize(); // size of DATA part in the DATA PACKET
				short blockNumber = ((DATA) message).getBlockNumber();
				try {
					File dir= new File("Files");
					if(dir.getUsableSpace()<packetSize){
						ERROR noSpace= new ERROR((short)3,"Disk full or allocation exceeded – No room in disk.");
						this.connections.send(connectionId, noSpace);
					}
						
						
					this.outputStream.write(data); // write the byte array to  the stream
						//outputStream.flush();
					if (packetSize == (short) 512) { // if its not the end of the file
						this.connections.send(connectionId, new ACK(blockNumber)); // send ACK to the client -ready for next packet
					} else if (packetSize < (short) 512) { // if its the last packet  of the file
						this.isUploading = false;
						try {
							outputStream.flush();
							outputStream.close(); // close the stream
						} catch (IOException e) {
						}
						this.connections.getCurrentlyUploadingMap().remove(this.uploadingFile.getName());
						this.connections.send(connectionId, new ACK(blockNumber));
						// Broadcast to all clients
						BCAST cast = new BCAST(this.uploadingFile.getName(), (byte) 1);
						this.outputStream.close();
						this.uploadingFile = null;
						process(cast); // process this cast Packet.

					}

				} catch (IOException e) {

					ERROR error = new ERROR((short) 3, "Disk full or allocation exceeded – No room in disk.");
					this.connections.send(connectionId, error);
				}

			}
			else{
				ERROR error = new ERROR((short)0,"Not defined, see error message (if any).");
				this.connections.send(connectionId, error);
			}

			break;
		// ACK
		case 4:
			if (this.isDownloading&& this.nextAckNum==((ACK)message).getBlockNumber()){ 
				this.nextAckNum++;
				if (!dataQ.isEmpty()) {
					this.connections.send(connectionId, dataQ.poll()); // send the next data msg in queue
				} else if(this.downloadingFile!=null){ // if Null, we manage DIRQ
					this.isDownloading = false;
					this.connections.removeDownloadingFile(this.downloadingFile.getName());
					this.downloadingFile = null;
				}
			}
			else{
				ERROR error = new ERROR((short)0,"Not defined, see error message (if any).");
				this.connections.send(connectionId, error);
			}
			break;
		// ERROR
		case 5:

			break;
		// DIRQ
		case 6: {
			if (this.loggedIn) {
				this.nextAckNum=1;
				File dir = new File("Files");
				File[] directoryListing = dir.listFiles();
				if (directoryListing.length!=0) { // if there are any files in the server

					this.isDownloading = true; // client is downloading information & is awaiting DATA Packets
					String dirqString = ""; // string that will be returned to client as DATA Packets
					for (File currentFile : directoryListing) { // for each file in the folder
						if (!this.connections.getCurrentlyUploadingMap().containsKey(currentFile.getName())) {
							dirqString = dirqString + currentFile.getName() + '\0';
						}
					}
					byte[] toSend = dirqString.getBytes();
					addDATA(toSend);
					
				}

				else {
					byte[] bArray = { '\0' };
					this.nextAckNum=1;
					this.isDownloading = true;
					addDATA(bArray);
				}
				this.connections.send(connectionId, this.dataQ.poll()); // send the first data msg in the queue
			} else
				this.notLogged();
		}

			break;
		// LOGRQ
		case 7:
			boolean loginWorked;
			String userName = ((LOGRQ) message).getUserName(); // get the USERNAME of the user
			loginWorked = connections.addNameToMap(this.connectionId, userName); // add to the map in "connectionsImpl"

			if (loginWorked) {

				this.loggedIn = true; // mark this protocol (and user) as
										// "Logged in"
				connections.send(connectionId, new ACK((short) 0)); // send ACKNOLEDGEMENT - LOGIN WORKED

			} else { // login didn't work
				ERROR error = new ERROR((short) 7, "User already logged in–Login usernamealready connected.");
				connections.send(connectionId, error);
			}
			break;
		// DELRQ
		case 8:
			if (this.loggedIn) {
				String fileName = ((DELRQ) message).getFileName();
				File toDelete = findFile(fileName);
				if (toDelete == null) {
					ERROR error = new ERROR((short) 1, "File not found");
					this.connections.send(connectionId, error);
				} else if (this.connections.getCurrentlyDownloading().containsKey(fileName)) {
					ERROR error = new ERROR((short) 2, "Access violation – File cannot be written, read or deleted.");
					this.connections.send(connectionId, error);
				} else {

					boolean deleted=toDelete.delete(); // delete file from directory
					if(deleted){
						BCAST cast = new BCAST(fileName, (byte) 0);
						process(cast);
						ACK ackDelete = new ACK((short) 0);
						this.connections.send(connectionId, ackDelete);
					}
					else{
						ERROR cannotDelete=new ERROR((short)2, "Access violation");
						this.connections.send(connectionId, cannotDelete);
					}
					
				}

			}

			else {
				this.notLogged();
			}

			break;
		// BCAST
		case 9: {
			ConcurrentHashMap<Integer, String> nameMap = this.connections.getNameMap(); // map that has ALL LOGGED IN users
			for (Map.Entry<Integer, String> entry : nameMap.entrySet()) { // SEND to every logged in user
				this.connections.send(entry.getKey(), message);
			}

		}
			break;
		// DISC
		case 10:
			if (this.loggedIn) { // if the user is logged in
				connections.send(connectionId, new ACK((short) 0)); // send ACK to the userthat'sloggingout
				this.loggedIn = false;
				this.shouldTerminate = true;
				this.connections.disconnect(connectionId); // disconnect the user (removes it's userName from map and Handler from map 
			} else
				notLogged(); // send error
			break;
		}
		}
	}

	public boolean shouldTerminate() {

		return this.shouldTerminate;
	}

	// sends an error when a user is unauthorized to do something
	public void notLogged() {
		ERROR error = new ERROR((short) 6, "User not logged in");
		this.connections.send(connectionId, error);

	}

	public byte[][] splitByteArray(byte[] dataToSplit) {

		return null;
	}

	public File findFile(String fileName) {
		File dir = new File("Files");
		String [] namesArray= dir.list();
		for(int i=0;i<namesArray.length;i++){
			if(namesArray[i].compareTo(fileName)==0){
				File toReturn= new File(dir.getPath()+File.separator+fileName);
				return toReturn;
			}
		}
		return null;
	}
	public void addDATA(byte[] dataArray) {
		int dataArrayLength = dataArray.length;
		short blockNum = 1;
		int current = 0;
		while (dataArrayLength >= 512) {
			byte[] bArray = Arrays.copyOfRange(dataArray, current, current + 512); // copy
																					// array
			current = current + 512;
			DATA data = new DATA(((short) bArray.length), blockNum, bArray);
			this.dataQ.add(data);
			blockNum++;
			dataArrayLength = dataArrayLength - 512;
		} // send last data Packet
		byte[] bArray = Arrays.copyOfRange(dataArray, current, dataArray.length); // copy
		// array
		DATA data = new DATA(((short) bArray.length), blockNum, bArray);
		this.dataQ.add(data);
	} 
}
