#!/bin/bash

# place where the directories of this script created
# safety: don't erase a other directory
DIR="$HOME/shared_filesystem"
# N server machine(s)
N=4
# M client machine(s)
M=0
# P number of ports by machine
P=3
# parameters of config file
CONFIG_FILE_SERVER="server/config.txt"
CONFIG_FILE_CLIENT="client/config.txt"
# free port from 1024 to 65535
NP=1024

### FUNCTION ###

# line format in this file
# config file server
# idServer address buddyPort clientPort serverPort
declare -a client_ports
function create_config_file_server { 
    for i in $(seq 0 $((N-1))); do    
        echo -n "$i 127.0.0.1 "
        for j in $(seq 0 $((P-1))); do
            if [ $j -eq 1 ]; then
                client_ports[$i]=$NP;    
            fi 
            echo -n "$NP "
            ((NP++))    
        done
        echo 
    done
}


# line format in this file
# config file server
# idServer address clientPort
function create_config_file_client {
    for i in $(seq 0 $((N-1))); do
        echo "$i 127.0.0.1 ${client_ports[$i]}"
    done     
}

### SCRIPT ###

# create the config files
echo Generating config file...
create_config_file_server > "$CONFIG_FILE_SERVER"
create_config_file_client > "$CONFIG_FILE_CLIENT"
echo Generating config file...DONE

if [ ! -e "$DIR" ]; then
    mkdir "$DIR"
fi


echo "$N server(s) to deploy"
echo Deploying servers...

# deploy the server
for i in $(seq 0 $((N-1))); do
    DEST="$DIR"/server"$i"
    mkdir -p "$DEST"
    cp -r server/dist/* "$DEST"/
    cp server/config.txt "$DEST"/
done

echo Deploying servers...DONE
exit 0
