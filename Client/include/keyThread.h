

#ifndef KEYTHREAD_H_
#define KEYTHREAD_H_
class keyThread{

public:
	bool shouldTerminate;
	keyThread(ConnectionHandler* handler, MessageEncoderDecoder* encDec, socketListener* protocol);
	void run();
	 std::vector<std::string>  tokenize(string line);
	 bool checkLine(std::vector<string>& wordVector);
	ConnectionHandler* handler;
	MessageEncoderDecoder* encDec;
	socketListener * protocol;




};













#endif
