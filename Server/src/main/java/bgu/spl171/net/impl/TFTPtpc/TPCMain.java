package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.api.bidi.BidiEncDecImpl;
import bgu.spl171.net.api.bidi.BidiImpl;
import bgu.spl171.net.srv.Server;

public class TPCMain {
	public static void main(String[] args) {
		int port = Integer.parseInt(args[0]);
		Server.threadPerClient(port, // port
				() -> new BidiImpl(), // protocol factory
							()->new BidiEncDecImpl() // message encoder decoder factory
		).serve();
	}
}