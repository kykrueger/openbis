# openBIS - prototypes for new UI

## Setting up the environment

1. Install latest version of VirtualBox (https://www.virtualbox.org)

2. Install latest version of Vagrant (https://www.vagrantup.com/downloads.html)

3. vagrant plugin install vagrant-vbguest vagrant-notify-forwarder vagrant-disksize
 
4. cd env/dev

5. vagrant up

6. Browse to https://localhost:8122/openbis and accept the self-signed certificate

* openBIS is now running at https://localhost:8122/openbis (login: admin/password)
* React proto is now running at http://localhost:8124

## Additional info for Linux users

On Ubuntu (and maybe on other Linux distributions, too), you might get errors like this when running "vagrant up":

  terminate called after throwing an instance of 'std::runtime_error'
    what():  Could not add watch

This situation will prevent the development environment from working properly. The reason for the error is https://github.com/mhallin/vagrant-notify-forwarder/issues/5. It can be fixed by increasing the maximum number of watches on the host system. On Ubuntu, this is done like this:

  echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf && sudo sysctl -p
