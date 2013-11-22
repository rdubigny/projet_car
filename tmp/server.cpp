#include <iostream>
#include <cstring>          // needed for memset
#include <sys/socket.h>     // needed for sockets
#include <netdb.h>          // needed for sockets
#include <unistd.h>         // needed for closing socket

int main()
{
    // same as the client side. The only differences is that we call bind,
    // listen and accept instead of connect.
    int status;
    struct addrinfo host_info;
    struct addrinfo *host_info_list;
    memset(&host_info, 0, sizeof host_info);

    /*
        STEP 1: setting up host infos
    */
    std::cout << "Setting up the structs...\t" << std::flush;

    host_info.ai_family = AF_UNSPEC;
    host_info.ai_socktype = SOCK_STREAM;
    host_info.ai_flags = AI_PASSIVE; // IP Wildcard

    status = getaddrinfo("127.0.0.1", "5555", &host_info, &host_info_list);
    if (status != 0)  std::cout << "getaddrinfo error" << gai_strerror(status) << std::endl;
    else std::cout << "done" << std::endl;

    // std::cout << "addr : "<< host_info_list->ai_addr << ", socktype : " <<
    //     host_info_list->ai_socktype << ", family : " << host_info_list->ai_family <<
    //     host_info_list->ai_protocol << ", flags : " << host_info_list->ai_flags << std::endl;
    /*
        STEP 2: create socket
    */
    std::cout << "Creating a socket...\t\t" << std::flush;
    int socketfd ; // The socket descripter
    socketfd = socket(host_info_list->ai_family, host_info_list->ai_socktype,
                      host_info_list->ai_protocol);
    if (socketfd == -1)  std::cout << "socket error " << std::endl;
    else std::cout << "done\tUsing socketfd : " <<  socketfd << std::endl;

    /*
        STEP 3: bind socket
    */
    std::cout << "Binding socket...\t\t" << std::flush;
    // use setsockopt() to make sure the port is not in use
    int yes = 1;
    status = setsockopt(socketfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));
    status = bind(socketfd, host_info_list->ai_addr,
        host_info_list->ai_addrlen);
    if (status == -1)  std::cout << "bind error" << std::endl;
    else std::cout << "done" << std::endl;

    /*
        STEP 4: listen for connection
    */
    std::cout << "Listening for connections...\t" << std::flush;
    status = listen(socketfd, 5);
    if (status == -1)  std::cout << "listen error" << std::endl;

    /*
        STEP 5: accept client and create a new socket for him
    */
    int new_sd;
    struct sockaddr_storage their_addr;
    socklen_t addr_size = sizeof(their_addr);
    new_sd = accept(socketfd, (struct sockaddr *)&their_addr, &addr_size);
    if (new_sd == -1) std::cout << "listen error" << std::endl ;
    else
        std::cout << "done\tUsing new socketfd : "  <<  new_sd
        << std::endl;


    /*
        STEP 6: wait for datas
    */
    std::cout << "Waiting to receive data...\t"  << std::flush;
    ssize_t bytes_received;
    char incoming_data_buffer[1000];
    bytes_received = recv(new_sd, incoming_data_buffer,1000, 0);
    // If no data arrives, the program will just wait here until some data arrives.
    if (bytes_received == 0) std::cout << "client shut down." << std::endl ;
    if (bytes_received == -1)std::cout << "receive error!" << std::endl ;
    std::cout << "done\t"<< bytes_received << " bytes received\x1b[31;1m" << std::endl ;
    incoming_data_buffer[bytes_received] = '\0';
    std::cout << incoming_data_buffer << std::endl;

    /*
        STEP 7: sending back a message
    */
    std::cout << "\x1b[0mSending back a message...\t" << std::flush;
    char msg[] = "I don't care. Stop bothering me.\n";
    int len;
    ssize_t bytes_sent;
    len = strlen(msg);
    bytes_sent = send(new_sd, msg, len, 0);
    if (bytes_sent == -1) std::cout << "send error" << std::endl;
    else std::cout << "done" << std::endl;

    /*
        STEP 8: free resources
    */
    std::cout << "Stopping server...\t\t" << std::flush;
    freeaddrinfo(host_info_list);
    close(new_sd);
    close(socketfd);
    std::cout << "done" << std::endl;
    return 0 ;
}