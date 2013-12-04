#!/bin/bash

# place where the directories of this script created
# safety: don't erase a other directory
DIR="$HOME/Application_CAR"
# N server machine(s)
N=3
# M client machine(s)
M=0
# P number of ports by machine
P=2

# config file
# free port from 1024 to 65535
CONFIG_FILE="server/config.txt"
NP=1024
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

create_config_file > "$CONFIG_FILE"


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


echo "$N server(s) to deploy"
echo Deploying servers...

# create and launch the server
for i in $(seq 0 $((N-1))); do
    DEST="$DIR"/server"$i"
    mkdir -p "$DEST"
    cp server/dist/server.jar "$DEST"/
    cp server/config.txt "$DEST"/
    cd "$DEST" > /dev/null
    gnome-terminal -e "java -jar server.jar $i"
    cd - > /dev/null
done

echo Deploying servers...DONE
exit 0
