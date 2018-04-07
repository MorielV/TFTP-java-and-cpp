#include <stdlib.h>
#include <socketListener.h>
#include "MessageEncoderDecoder.h"
#include <string>
#include <boost/thread.hpp>
#include "keyThread.h"

std::vector<std::string> keyThread::tokenize(string line){
 	std::vector<string> wordVector;	//vector of words.
 			short index=0;
 			string currentWord="";			//change later
 			char currentChar= line[0];	//take first char
 			while(currentChar!='\0'){	//while its not the end of the input

 				while(currentChar!='\0' && currentChar!=' '){//its still the same word
 					currentWord.append(1,currentChar);	//add another char to the word
 					index++;
 					currentChar=line[index];	//current char
 				}
 				//reaching here-currentWord has a word.
 				wordVector.push_back(currentWord);	//add this word to the vector
 		        currentWord="";
 		        if(currentChar!='\0'){ //if not finished yet
 		            index++;
 		            currentChar=line[index];	//point to next char in the input
 		         }
 			}
 			return wordVector;		//return the word vector.
 }
 bool keyThread::checkLine(std::vector<string>& wordVector){
 	//std::vector<string> wordVector=tokenize(line);	//vector that has the words typed by the user

	 if(wordVector.size()>=2 ){				//length is 2
 		string command=wordVector[0];	//the first word
 		if(command.compare("LOGRQ")==0||command.compare("RRQ")==0||command.compare("WRQ")==0||command.compare("DELRQ")==0){	//if its a LOGRQ request
 			//make sure the username does not have '\0'
 			string fileOrUserName="";
                        
                        for(unsigned int k=1; k<wordVector.size(); k++){
                                    fileOrUserName=fileOrUserName+wordVector[k];
                                    if(k!=wordVector.size()-1)
                                        fileOrUserName=fileOrUserName+" ";
                                    
                        }
 			for(unsigned int i=0; i<fileOrUserName.length(); i++){
 				if(fileOrUserName[i]=='\0'){
 					wordVector[1][i]='X';	//switch with X also in the vector
 				}
 			}
 			return true;				//the right command was entered. (if a bad name was entered, change it.)
 		}
 		else
 			return false;				//this command does not exist
 	}
 	else if(wordVector.size()==1){		//length is 1
 		string command=wordVector[0];	//the first word
 		 if(command.compare("DIRQ")==0||command.compare("DISC")==0)	//if its a good command
 						return true;

 		 else
 			 return false;				//this command does not exist
 	}
 	else								//the input has an illegal size.
 		return false;
 }


void keyThread::run(){
     while(!this->shouldTerminate){
   	    	bool inputIsLegal=false;
   	    	vector<string> wordVector;
   	    	while(!inputIsLegal){
   	    		string input=handler->getLine();		//get line from user
   	    		wordVector=tokenize(input);	//tokenize the input into a string vector
   	    		inputIsLegal=checkLine(wordVector);
   	    	}
   	    	Packet * packetFromUser;
   	    	    int packetLength;
   	    		    if(wordVector[0]=="RRQ"){
   	    		    	string fileName="";
                                for(unsigned int k=1; k<wordVector.size(); k++){
                                    fileName=fileName+wordVector[k];
                                    if(k!=wordVector.size()-1)
                                        fileName=fileName+" ";
                                    
                                }
                                packetFromUser=new RRQ(fileName);
   	    		    	this->protocol->setDownFile(fileName);
   	    		    	packetLength=fileName.length()+3;
  	    		    	 handler->lastSent=1;
   	    		    }
   	    		    else  if(wordVector[0]=="WRQ"){
                                string fileName="";
                                 for(unsigned int k=1; k<wordVector.size(); k++){
                                    fileName=fileName+wordVector[k];
                                    if(k!=wordVector.size()-1)
                                        fileName=fileName+" ";
                                    
                                    }

   	    		    	packetFromUser=new WRQ(fileName);
   	    		    	protocol->setUpFile(fileName);
   	    		    	packetLength=fileName.length()+3;
  	    		    	 handler->lastSent=2;

   	    		    }
   	    		    else  if(wordVector[0]=="DIRQ"){
   	    		    	 packetFromUser=new DIRQ();
   	    		    	 packetLength=2;
   	    		    	 handler->lastSent=6;

   	    		    }
   	    		    else  if(wordVector[0]=="LOGRQ"){
   	    	   	    	 packetFromUser=new LOGRQ(wordVector[1]);
   	    	   	    	packetLength=wordVector[1].length()+3;
  	    		    	 handler->lastSent=7;
   	    	   	    }
   	    		    else  if(wordVector[0]=="DELRQ"){
                                string fileName="";
                                 for(unsigned int k=1; k<wordVector.size(); k++){
                                    fileName=fileName+wordVector[k];
                                    if(k!=wordVector.size()-1)
                                        fileName=fileName+" ";
                                    
                                    }

   	    		       	 packetFromUser=new DELRQ(fileName);
   	    		       	packetLength=fileName.length()+3;
 	    		    	 handler->lastSent=8;
   	    		     }
   	    		    else  if(wordVector[0]=="DISC"){
   	    		          packetFromUser=new DISC();
   	    		          packetLength=2;
   	    		          handler->lastSent=10;
   	    		          this->shouldTerminate=true;
   	    		     }
   	    		    handler->sendBytes(encDec->encode(packetFromUser),packetLength);	//SEND THE PACKET
   	 	}
}
keyThread::keyThread(ConnectionHandler* handler, MessageEncoderDecoder* encDec, socketListener* protocol):shouldTerminate(false),handler(handler), encDec(encDec), protocol(protocol){}
