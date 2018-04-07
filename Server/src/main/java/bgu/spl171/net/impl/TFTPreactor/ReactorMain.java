package bgu.spl171.net.impl.TFTPreactor;

import bgu.spl171.net.api.bidi.BidiEncDecImpl;
import bgu.spl171.net.api.bidi.BidiImpl;
import bgu.spl171.net.srv.Server;

public class ReactorMain {

	public static void main(String[] args){
		int port = Integer.parseInt(args[0]);
		 Server.reactor(
	                Runtime.getRuntime().availableProcessors(),
	                port, //port
	                () ->  new BidiImpl(), //protocol factory
	                BidiEncDecImpl::new //message encoder decoder factory
	        ).serve();
	}
}
