#include <iostream>
#include <cstring>          // needed for memset
#include <sys/socket.h>     // needed for sockets
#include <netdb.h>          // needed for sockets
#include <unistd.h>         // needed for closing socket

int main()
{
    int status;
    struct addrinfo host_info; // the struct filled up by getaddrinfo()
    struct addrinfo *host_info_list; // linked list of host_info

    // make sure all fields are NULL
    memset(&host_info, 0, sizeof host_info);

    /*
        STEP 1: fill up hosts infos
    */
    std::cout << "Setting up the structs...\t" << std::flush;

    host_info.ai_family = AF_UNSPEC; // can be IPv4, IPv6 or both
    host_info.ai_socktype = SOCK_STREAM; // SOCK_STREAM<->TCP, SOCK_DGRAM<->UDP

    // std::string caca = new string("caca");
    char server_addr[] = "127.0.0.1"; // null resolved in "localhost"
    char server_port[] = "5555";
    status = getaddrinfo(server_addr, server_port, &host_info, &host_info_list);

    // getaddrinfo returns 0 on success, some other value when an error occured
    // (translated into human readable text by the gai_strerror function)
    if (status != 0)
        std::cout << "getaddrinfo error" << gai_strerror(status) << std::endl;
    else std::cout << "done" << std::endl;

    // std::cout << "addr : "<< host_info_list->ai_addr << ", socktype : " <<
    //     host_info_list->ai_socktype << ", family : " << host_info_list->ai_family <<
    //     host_info_list->ai_protocol << ", flags : " << host_info_list->ai_flags << std::endl;


    /*
        STEP 2: create socket
    */
    std::cout << "Creating a socket...\t\t" << std::flush;
    int socketfd; // the socket descriptor
    // use the first host_info of host_info_list
    socketfd = socket(host_info_list->ai_family, host_info_list->ai_socktype,
        host_info_list->ai_protocol);

    if (socketfd == -1) std::cout << "socket error" << std::endl;
    else std::cout << "done\tUsing socketfd : " <<  socketfd << std::endl;

    /*
        STEP 3: connect to the server
    */
    std::cout << "Connecting...\t\t\t" << std::flush;
    status = connect(socketfd, host_info_list->ai_addr,
        host_info_list->ai_addrlen);

    if (socketfd == -1) std::cout << "connect error" << std::endl;
    else std::cout << "done" << std::endl;

    /*
        STEP 4: send and receive data
    */
    std::cout << "Sending message...\t\t" << std::flush;
    char msg[] = "GET / HTTP/1.1\nhost: givememore.com\n\n";
    int len;
    ssize_t bytes_sent;
    len = strlen(msg);
    bytes_sent = send(socketfd, msg, len, 0);
    if (bytes_sent == -1) std::cout << "send error" << std::endl;
    else std::cout << "done\t" << bytes_sent << " bytes sent" << std::endl;

    std::cout << "Waiting to receive data...\t"  << std::flush;
    ssize_t bytes_received;
    char incoming_data_buffer[1000];
    bytes_received = recv(socketfd, incoming_data_buffer,1000, 0);
    // If no data arrives, the program will just wait here until some data arrives.
    if (bytes_received == 0) std::cout << "host shut down." << std::endl ;
    if (bytes_received == -1)std::cout << "receive error!" << std::endl ;
    std::cout << "done\t" << bytes_received << " bytes received\x1b[31;1m" << std::endl ;
    incoming_data_buffer[bytes_received] = '\0';
    std::cout << incoming_data_buffer << std::endl;

    /*
        STEP 5: free resources
    */
    std::cout << "\x1b[0mComplete. Closing socket...\t" << std::flush;
    freeaddrinfo(host_info_list);
    close(socketfd);
    std::cout << "done" << std::endl;

    return 0;
}