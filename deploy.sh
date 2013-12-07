#!/bin/bash

### CONFIG ###

# place where the directories of this script created
# safety: don't erase a other existing directory
DIR="$HOME/CAR_Application"
# N server machine(s)
N=3
# M client machine(s)
M=0
# P number of ports by machine
P=3
# parameters of config file
CONFIG_FILE="server/config.txt"
# free port from 1024 to 65535
NP=1024

### FUNCTION ###

# line format in this file
# idServer address buddyPort clientPort serverPort
function create_config_file { 
    for i in $(seq 0 $((N-1))); do    
        echo -n "$i 127.0.0.1 "
        for j in $(seq 0 $((P-1))); do
            echo -n "$NP "
            ((NP++))    
        done
        echo 
    done
}

function execute_server {
    cd "$DIR"/server"$i" > /dev/null
    gnome-terminal -t "server $1" -e "java -jar server.jar $1"
    cd - > /dev/null
}

function execute_client {
    cd "$DIR"/client"$i" > /dev/null
    gnome-terminal -t "client $1" -e "java -jar client.jar $1"
    cd - > /dev/null
}

### SCRIPT ###

# create the file config
create_config_file > "$CONFIG_FILE"

# create the repertory contents all executables
if [ ! -e "$DIR" ]; then
    mkdir "$DIR"
else 
    echo "ERREUR: $DIR exists !"
    echo "Delete $DIR and create again (y/n) ?"
    read response
    if [ "$response" = 'y' ]; then
        rm -rf "$DIR"
        mkdir "$DIR"
    else
        exit 1
    fi
fi

# create and launch the server in the previous repertory
for i in $(seq 0 $((N-1))); do
    mkdir -p "$DIR"/server"$i"
    cp -r server/dist/* "$DIR"/server"$i"/
    cp server/config.txt "$DIR"/server"$i"/
    execute_server "$i"
done


# create and launch the client in the 
for i in $(seq 0 $((M-1))); do
    mkdir -p "$DIR"/client"$i"
    cp -r client/dist/* "$DIR"/client"$i"/
    cp server/config.txt "$DIR"/client"$i"
    execute_client "$i"
done

# Track the done actions
echo "$N server(s) deploy."
echo "$M client(s) deploy."
echo Deploying servers...


# kill and recreate server
INFINITY=true
while $INFINITY; do
    echo -n "create server : "
    read number
    execute_server "$number"
done

echo Deploying servers...DONE
exit 0
