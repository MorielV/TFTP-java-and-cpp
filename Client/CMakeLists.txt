cmake_minimum_required(VERSION 3.6)
project(Client)

set(CMAKE_CXX_STANDARD 11)

include_directories(include)

add_executable(Client
        include/connectionHandler.h
        include/keyThread.h
        include/MessageEncoderDecoder.h
        include/Packet.h
        include/socketListener.h
        include/Task.h
        src/connectionHandler.cpp
        src/keyThread.cpp
        src/MainClass.cpp
        src/MessageEncoderDecoder.cpp
        src/Packet.cpp
        src/socketListener.cpp)
