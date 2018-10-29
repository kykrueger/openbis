build="http://stage-jenkins.ethz.ch:8090/job/installation-18.06/lastSuccessfulBuild"
path=$(curl -s "$build/api/xml?xpath=//relativePath"|sed -e "s/<relativePath>//"|sed -e "s/<\/relativePath>//")
wget -q $build/artifact/$path
archive=$(basename $path)
tar xvfz $archive
directory=$(echo "$archive" | cut -f 1 -d '.')
cp /files/console.properties $directory
export ADMIN_PASSWORD='password'
$directory/run-console.sh

sed -i "/jetty.ssl.port=/ s/=.*/=8122/" /home/vagrant/openbis/servers/openBIS-server/jetty/start.d/ssl.ini
sed -i "/host-address =/ s/=.*/= https:\/\/localhost/" /home/vagrant/openbis/servers/datastore_server/etc/service.properties
sed -i "/port =/ s/=.*/= 8123/" /home/vagrant/openbis/servers/datastore_server/etc/service.properties
sed -i "/server-url =/ s/=.*/= \${host-address}:8122/" /home/vagrant/openbis/servers/datastore_server/etc/service.properties

cd  /home/vagrant/openbis-ui-proto/react
ln -s /node/node_modules
