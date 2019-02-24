# openBIS - next generation UI

## Setting up the environment

1. Install latest version of VirtualBox (https://www.virtualbox.org)

2. Install latest version of Vagrant (https://www.vagrantup.com/downloads.html)

3. vagrant plugin install vagrant-vbguest vagrant-notify-forwarder vagrant-disksize

4. Relative to the current folder: cd env/dev

5. vagrant up

6. Start openBIS in your Eclipse - it should be running at http://localhost:8888/openbis-test. Make sure you can log in with credentials 'admin / password'.

7. openBIS next generation UI is now running at http://localhost:8124

## Setting up IntelliJ Idea

1. Under "IntelliJ IDEA" -> "Preferences" -> "Languages and Frameworks" -> Javascript, set the language version to ECMAScript 6.

## Login into Vagrant to see the dev server output

1. vagrant ssh

2. screen -r

## Additional info for Linux users

On Ubuntu (and maybe on other Linux distributions, too), you might get errors like this when running "vagrant up":

  terminate called after throwing an instance of 'std::runtime_error'
    what():  Could not add watch

This situation will prevent the development environment from working properly. The reason for the error is https://github.com/mhallin/vagrant-notify-forwarder/issues/5. It can be fixed by increasing the maximum number of watches on the host system. On Ubuntu, this is done like this:

  echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf && sudo sysctl -p

## "Jest" automated tests

Running tests

  npm run test

Debugging tests

  1. execute in command line:

    node --inspect-brk ./node_modules/.bin/jest --runInBand

  2. put the following snippet of code in a line where the debugger should stop:

    /*eslint no-debugger: "info"*/
    debugger

  3. open "chrome://inspect" in Chrome and choose "Open dedicated DevTools for Node" to start debugging
