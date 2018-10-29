sudo /usr/sbin/VBoxService --timesync-set-start

/home/vagrant/openbis/bin/allup.sh

#cd openbis-ui-proto/react
#../gradlew npmInstall
#cd 

#export PATH=$PATH:/home/vagrant/openbis-ui-proto/react/nodejs/node-v10.1.0-linux-x64/bin

#screen -S dev -t webpack -Adm bash -c "cd openbis-ui-proto/react; npm run dev; bash"

#echo "Waiting Webpack to launch on 8124..."

#while ! nc -z localhost 8124; do
#  sleep 3
#done
