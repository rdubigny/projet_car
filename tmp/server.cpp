/*
    compile with :
        g++ -o server server.cpp -fpermissive -pthread -w && ./server

    request server with telnet
*/

#include <iostream>
#include <cstring>          // needed for memset
#include <sys/socket.h>     // needed for sockets
#include <netdb.h>          // needed for sockets
#include <unistd.h>         // needed for closing socket
#include <csignal>          // needed to catch interruption signal
#include <cstdlib>

//server constants
const char PORT[] = "5555";     // port numbers 1-1024 are reserved by the OS
const char ADDR[] = "127.0.0.1";// localhost
const int MAXLEN = 1024 ;       // Max lenhgt of a message
const int MAXFD = 7 ;           // Maximum file descriptors (= maximum clients)
const int BACKLOG = 5 ;         // max nbr of connections holded before accepted

int server_fd ; // The socket descriptor
struct addrinfo *host_info_list;

// This needs to be declared volatile because it can be altered by any other
// thread. Meaning the compiler cannot optimise the code
volatile fd_set the_state;

pthread_mutex_t mutex_state = PTHREAD_MUTEX_INITIALIZER;

// servers functions

void interruptHandler(int signum);
int server_start_listen();
void mainloop();
void *tcp_server_read(void *arg) ;

int main()
{
    int status;
    std::cout << "Server started on port " << PORT << std::endl;
    // make the server listen on port PORT
    status = server_start_listen();
    if (status != 0){
        std::cout << "An error occured. Closing program." << std::endl;
        return 1 ;
    }
    // register interruption signal handler
    signal(SIGINT, interruptHandler);

    mainloop();

    return 0;
}

int server_start_listen()
{
    // same as the client side. The only differences is that we call bind,
    // listen and accept instead of connect.
    int status;
    struct addrinfo host_info;
    memset(&host_info, 0, sizeof host_info);

    /*
        STEP 1: setting up host infos
    */
    std::cout << "Initializing...\t\t" << std::flush;

    host_info.ai_family = AF_UNSPEC;
    host_info.ai_socktype = SOCK_STREAM;
    host_info.ai_flags = AI_PASSIVE; // IP Wildcard

    status = getaddrinfo(ADDR, PORT, &host_info, &host_info_list);
    if (status != 0){
        std::cout << "getaddrinfo error" << gai_strerror(status) << std::endl;
        return status;
    }

    /*
        STEP 2: create socket
    */
    server_fd = socket(host_info_list->ai_family, host_info_list->ai_socktype,
                      host_info_list->ai_protocol);
    if (server_fd == -1){
        std::cout << "socket error " << std::endl;
        return 1;
    }

    /*
        STEP 3: bind socket
    */
    // use setsockopt() to make sure the port is not in use
    int yes = 1;
    status = setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));
    status = bind(server_fd, host_info_list->ai_addr,
        host_info_list->ai_addrlen);
    if (status == -1){
       std::cout << "bind error" << std::endl;
       return 1;
    }

    std::cout << "done" << std::endl;
    /*
        STEP 4: listen for connection
    */
    std::cout << "Listening for connections...\t" << std::flush;
    status = listen(server_fd, BACKLOG);
    if (status == -1){
        std::cout << "listen error" << std::endl;
        return 1;
    }
    std::cout << "\n\n" << std::endl;
    return 0;
}

void mainloop()
// This loop will wait for a client to connect. When the client connects, it
// creates a new thread for the client and starts waiting again for a new client
{

    char welcome_msg[] = "You're not welcome. Leave me alone. \
        Valid commands are: all you want to write. I don't care.";
    pthread_t threads[MAXFD]; //create MAXFD handles for threads.

    FD_ZERO(&the_state); // clears all the filedescriptors

    while(1){

        /*
            STEP 5: accept client and create a new socket for him
        */
        int new_sd;
        void *arg;

        struct sockaddr_storage their_addr;
        socklen_t addr_size = sizeof(their_addr);
        new_sd = accept(server_fd, (struct sockaddr *)&their_addr, &addr_size);
        // If no connection arrives, the program will just wait here.
        if (new_sd < 0){
            std::cout << "listen error" << std::endl ;
            continue;
        }

        if (new_sd > MAXFD){
            std::cout << "Connection refused!\t\tTo many clients trying to connect."
                << std::endl;
            close(new_sd);
            continue;
        }

        std::cout << "Connection accepted!\t\tUsing new file descriptor : "
            << new_sd << std::endl;

        // make sure 2 threads can't create fd simultaneously
        pthread_mutex_lock(&mutex_state);

        FD_SET(new_sd, &the_state);  // Add a file descriptor to the FD-set.

        pthread_mutex_unlock(&mutex_state);

        arg = (void *) new_sd;

        // now create a thread for this client.
        pthread_create(&threads[new_sd], NULL, tcp_server_read, arg);
    }
}


void *tcp_server_read(void *arg)
// This function runs in a thread for every client, and reads incomming data.
// It also writes the incomming data to all other clients.
{
    int sd = (int)arg;

    while(1){
        /*
            STEP 6: wait for datas
        */
        std::cout << "fd." << sd << ": Waiting to receive data...\t"
            << std::endl;
        ssize_t bytes_received;
        char in_buf[MAXLEN];
        bytes_received = recv(sd, in_buf, sizeof(in_buf), 0);
        // If no data arrives, the program will just wait here.
        if (bytes_received <= 0){
            if (bytes_received == 0)
                std::cout << "fd." << sd << ": Client disconnected! "<< std::flush;
            if (bytes_received == -1)
                std::cout << "fd." << sd << ": Receive error! "<< std::flush;
            std::cout << "Clearing fd. " << sd << std::endl;
            pthread_mutex_lock(&mutex_state);
            FD_CLR(sd, &the_state);      // free fd's from  clients
            pthread_mutex_unlock(&mutex_state);
            close(sd);
            pthread_exit(NULL);
        }
        std::cout << "fd." << sd << ": " << bytes_received
            << " bytes received => " << std::endl;
        in_buf[bytes_received] = '\0';
        std::cout << "\x1b[31;1m" << in_buf << "\x1b[0m" << std::endl;

        if (in_buf[0] == 'a'){
            // std::string str1 ("green apple");
            // std::string str2 (in_buf);
            // str1.compare(6,5,str2,4,5) == 0
            std::cout << "Clearing fd. " << sd << "...\t\t" << std::flush;
            pthread_mutex_lock(&mutex_state);
            FD_CLR(sd, &the_state);      // free fd's from  clients
            pthread_mutex_unlock(&mutex_state);
            close(sd);
            std::cout << "done\n\n" << std::endl;
            pthread_exit(NULL);
        }
        /*
            STEP 7: sending back a message
        */
        std::cout << "fd." << sd << ": Sending back a message...\t" << std::flush;
        char msg[] = "I don't care. Stop bothering me.\n";
        int len;
        ssize_t bytes_sent;
        len = strlen(msg);
        bytes_sent = send(sd, msg, len, 0);
        if (bytes_sent == -1) std::cout << "send error" << std::endl;
        else std::cout << "done" << std::endl;
    }
}

void interruptHandler(int signum)
/* LAST STEP : free resources */
{
    std::cout << "\tInterrupt signal (" << signum << ") received!" << std::endl;
    std::cout << "Stopping server...\t\t" << std::flush;
    freeaddrinfo(host_info_list);
    close(server_fd);
    std::cout << "done" << std::endl;
    exit(0);
}