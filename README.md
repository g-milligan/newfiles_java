New Files Generator
===================

NewFiles is a command line tool that generates files based on predefined templates.

You can use NewFiles to quickly create a 'starter' Magento module. Simply enter your module prefix, module name, extension version and NewFiles will create all the files and folders you need, so you can start with the good stuff


INSTALLATION (OS X)
-------------------

NewFiles reguires Java 1.7 or higher, to check your version of Java, open Terminal and run

```
java -version
```
_Grab the latest version of Java SDK here_
http://www.oracle.com/technetwork/java/javase/downloads/index.html

_Java Installation Info is here_
http://docs.oracle.com/javase/7/docs/webnotes/install/mac/mac-jdk.html

============================================================================================

Clone a copy of this repository
```
git clone git@github.com:g-milligan/newfiles_java.git
```

Create a shell script to run NewFiles. This example will use a myscripts folder within the users home folder
```
cd ~
mkdir myscripts
cd myscripts
```
now we create our shell script
```
nano newfiles.sh
```
next drop in the following text to configure NewFiles to run
```
#!/bin/bash

java -jar "/path/to/newfiles.jar" "/path/to/newfiles_java/runbat/templates" $(pwd) "dirname $0"
```
now make your new script executable
```
chmod +x newfiles.sh
```
we recommend creating a symlink as well
```
ln -s newfiles.sh nf
```

Now all you need to do is add NewFiles to your bash path so you can run it from anywhere.
open up .bash_profile in your home folder (or create it...)
```
nano ~/.bash_profile
```
add the following line to include your myscripts folder
```
export PATH=~/myscripts/:$PATH 
```
now run,
```
. ~/.bash_profile
```
You should now be able to run 'newfiles.sh' or 'nf' from whatever folder you happen to be in, now the fun stuff...

...change directory to your Magento document root, run NewFiles, build modules!


