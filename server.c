// library for printf, free and malloc, errno, strlen, strcopy ...
#include <stdio.h> 
#include <stdlib.h> 
#include <errno.h> 
#include <string.h>

// library for sockets
#include <sys/types.h> 
#include <sys/socket.h> 
#include <netinet/in.h> 
#include <arpa/inet.h> 
#include <unistd.h> 
#include <netdb.h>

#define INVALID_SOCKET -1
#define SOCKET_ERROR -1

#define PORT 1234

int main(void) {
    // creation of one socket
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if(sock == INVALID_SOCKET) {
        perror("socket()");
        exit(errno);
    }
    // creation of the interface
    struct sockaddr_in sin = {0};
    // server accepts any addresses
    sin.sin_addr.s_addr = htonl(INADDR_ANY);
    sin.sin_family = AF_INET;
    sin.sin_port = htons(PORT);
    if(bind(sock, (struct sockaddr *)&sin, sizeof(sin)) == SOCKET_ERROR) {
        perror("bind()");
        exit(errno);
    }
    // listen and connexion with clients
    // 5 max connexions in the queue at the same time
    if(listen(sock, 5) == SOCKET_ERROR) {
        perror("lsiten()");
        exit(errno);
    }
    struct sockaddr_in csin = {0};
    int sinsize = sizeof(csin);
    int csock = accept(sock, (struct sockaddr *)&csin, &sinsize);
    if(csock == INVALID_SOCKET) {
        perror("accept()");
        exit(errno);
    }
    // reception from a message
    char reception_buffer[1024];
    int n = 0;
    if((n = recv(csock, reception_buffer, sizeof(reception_buffer)-1, 0)) < 0) {
        perror("recv()");
        exit(errno);
    }
    reception_buffer[n] = '\0';
    int i;
    for(i=0; i<n; i++) {
        printf("%c", reception_buffer[i]);
    }
    // close the sockets
    close(sock);
    close(csock);
    // trace
    printf("\n-- END --\n");
    return 0;
}
