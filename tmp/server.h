#ifndef _SERVER_
#define _SERVER_

#include <sys/socket.h>     // needed for sockets

class Server
{
private:
    // internal variables
    int server_fd ; // The socket descriptor
    struct addrinfo *host_info_list;
    // This needs to be declared volatile because it can be altered by any other
    // thread. Meaning the compiler cannot optimise the code.
    volatile fd_set the_state;
    pthread_mutex_t mutex_state = PTHREAD_MUTEX_INITIALIZER;

    //server constants
    const char PORT[] = "5555";   // port numbers 1-1024 are reserved by the OS
    const char ADDR[] = "127.0.0.1";   // localhost
    const int MAXLEN = 1024 ;   // Max lenhgt of a message
    const int MAXFD = 7 ;   // Maximum file descriptors (= maximum clients)
    const int BACKLOG = 5 ;   // max nbr of connections holded before accepted


    // servers functions
    void interruptHandler(int signum);
    int server_start_listen();
    void mainloop();
    static void *static_entry_point(void *c, void *arg) ;
    void *thread_worker(void *arg) ;

public:
    virtual ~Server();

    int start();
};

#endif