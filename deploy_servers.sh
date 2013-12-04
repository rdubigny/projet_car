#!/bin/bash
# Deploy servers

echo Deploying servers...

QTE=($(wc server/config.txt))
QTE=${QTE[0]}
echo "$QTE server(s) to deploy"
FOLDERNAME=car_test_
#rm -R "$FOLDERNAME"*
for i in $(seq 0 $((QTE-1)))
do
    mkdir -p ~/$FOLDERNAME$i
    cp server/dist/server.jar ~/$FOLDERNAME$i
    cp server/config.txt ~/$FOLDERNAME$i
done
echo Deploying servers...DONE

exit 0