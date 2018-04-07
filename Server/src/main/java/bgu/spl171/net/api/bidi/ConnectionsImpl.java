package bgu.spl171.net.api.bidi;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import bgu.spl171.net.srv.ConnectionHandler;
public class ConnectionsImpl<T> implements Connections<T>{
		
	
    private	AtomicInteger nextId;
	private ConcurrentHashMap<Integer, ConnectionHandler<T>> map;	// <id of user, the handler>
	private ConcurrentLinkedQueue<Integer> availableId;
	private ConcurrentHashMap<Integer, String> userNameMap;			// map of <id of user, user name>
	private ConcurrentHashMap<String, Integer> currentlyUploading;
	private ConcurrentHashMap<String, Integer> currentlyDownloading; 
	
	public ConnectionsImpl(){
		this.nextId=new AtomicInteger(0);				//the next ID if a new client connects.
		this.map= new ConcurrentHashMap<Integer, ConnectionHandler<T>>();
		this.availableId= new ConcurrentLinkedQueue<Integer>();
		this.userNameMap=new ConcurrentHashMap<Integer, String>();
		this.currentlyUploading= new ConcurrentHashMap<String,Integer>();
		this.currentlyDownloading= new ConcurrentHashMap<String , Integer>();
	}
	/**
	 * @return the next Id available
	 */
	public Integer getId(){
		
		try{
			return availableId.remove();
		}
		catch (NoSuchElementException e){
			return nextId.getAndIncrement();
		}
		
		
		
	}
	/**
	 * add handler in the map (id as key)
	 * @param id
	 * @param handler
	 */
	public int addToMap(ConnectionHandler<T> handler){
		int id=getId();
		map.put(id, handler);
		return id;
	}
	
	public boolean send(int connectionId, T msg) {	
		if(map.get(connectionId)==null)
				return false;
		else{
				map.get(connectionId).send(msg);
				return true;
		}
		
		
		
	}

	public void broadcast(T msg) {
		
		for (Entry<Integer, ConnectionHandler<T>> entry: map.entrySet()){
			entry.getValue().send(msg);
		}
		
		
		
	}

	public void disconnect(int connectionId) {
		//try {
			//map.get(connectionId).close();	//close connection (?)
			map.remove(connectionId);		//remove from list 
			userNameMap.remove(connectionId);	//remove from userNameMap
		
		} //catch (IOException e) {
		//Caught I/O Exception!
	//	}
		
		
	//}
	
	//adds a username to a map (including the right ID)
	
	public boolean addNameToMap(Integer id, String userName){
		

		if(userNameMap.containsValue(userName))	//if there is already a username with that name
			return false;
		else{
			userNameMap.put(id,userName);
			return true;
		}
	}
	public boolean addUploadingFile(String fileName){
		if(!currentlyUploading.containsKey(fileName)){
			this.currentlyUploading.put(fileName, 1);
			return true;
		}
		else
			return false;
		
	}
	public ConcurrentHashMap<Integer, String> getNameMap(){
		return this.userNameMap;
	}
	public ConcurrentHashMap<String, Integer> getCurrentlyUploadingMap(){
		return this.currentlyUploading;
	}
	public ConcurrentHashMap<String, Integer> getCurrentlyDownloading(){
		return this.currentlyDownloading;
	}
	public void addDownloadingFile(String fileName){
		if(!currentlyDownloading.containsKey(fileName)){
			this.currentlyDownloading.put(fileName, 1);
		}
		else{
			Integer currentValue =this.currentlyDownloading.get(fileName);
			this.currentlyDownloading.replace(fileName,currentValue , currentValue++);
		}
	}
	
	
	public void removeDownloadingFile(String fileName){
		Integer currentValue =this.currentlyDownloading.get(fileName);
		if(currentValue>1){
			this.currentlyDownloading.replace(fileName, currentValue, currentValue--);
		}
		else
			this.currentlyDownloading.remove(fileName);
	}
	
	
}
