// library for printf, free and malloc, errno ...
#include <stdio.h> 
#include <stdlib.h> 
#include <errno.h> 

// library for sockets
#include <sys/types.h> 
#include <sys/socket.h> 
#include <netinet/in.h> 
#include <arpa/inet.h> 
#include <unistd.h> 
#include <netdb.h>


#define INVALID_SOCKET -1
#define SOCKET_ERROR -1

// TCP/IP socket
int main(void) {
    // creation of one socket from client side
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if(sock == INVALID_SOCKET) {
        perror("socket()");
        exit(errno);
    }
    //connexion to the server
    struct sockaddr_in sin = {0};
    inet_aton("127.0.0.1", &sin.sin_addr);
    sin.sin_port = htons(1234);
    sin.sin_family = AF_INET;

    if(connect(sock, (struct sockaddr *)&sin, sizeof(struct sockaddr)) == SOCKET_ERROR) {
        perror("connect()");
        exit(errno);
    }
    // sending and reception of data
    // 1.sending
    char sending_buffer[1024];
    sending_buffer[0]='a';
    if(send(sock, sending_buffer, sizeof(char), 0) < 0) {
        perror("send()");
        exit(errno);
    }
    // 2.reception
    char reception_buffer[1024];
    int n = 0;
    if((n = recv(sock, reception_buffer, sizeof(reception_buffer)-1, 0)) < 0) {
        perror("recv()");
        exit(errno);
    }
    reception_buffer[n] = '\0';
    // close the created socket
    close(sock);
    // trace
    printf("\n-- END --\n");
    return 0;
}
