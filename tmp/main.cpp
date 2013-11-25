#include <csignal>          // needed to catch interruption signal
#include <iostream>

#include "server.h"

/* LAST STEP : free resources */
void intHandler(int signum);

Server* server;

int main(int argc, char** argv)
{
    server = new Server();
    server->start();

    // register interruption signal handler
    signal(SIGINT, intHandler);
}

char** worker(char* inMsg)
// worker get a in message as parameter and return the resulting message
{
    return NULL;
}

void intHandler(int signum)
{
    std::cout << "\tInterrupt signal (" << signum << ") received!" << std::endl;
    delete server;
}