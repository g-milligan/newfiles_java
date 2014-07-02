New Files Generator
===================

NewFiles is a command line tool that generates files based on predefined templates, that you can easily build. 

One useful application of NewFiles is to quickly create 'starter' Magento modules. Simply run NewFiles in your magento root dir, enter your module prefix, module name, extension version and NewFiles will create all the files and folders you need, so you can basically just start with the good stuff 


INSTALLATION (OS X)
-------------------

NewFiles reguires Java 1.7 or higher, to check your version of Java, open Terminal and run

```
java -version
```
_Grab the latest version of Java SDK here_ - 
http://www.oracle.com/technetwork/java/javase/downloads/index.html

_Java Installation Info is here_ - 
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


UNDER THE HOOD
--------------
/runbat/templates/_ExampleTemplate/demofile.txt
```
  <<filename:uppercase:example output filename>>  Hi, <<var:capitalize:your name>> 

  ... so... you say your favorite color is <<var:uppercase:your favorite color>>? 
  Are you trying to be a <<var:lowercase:a random qualitative adjective>> person? 
  Despite this... I guess I will forgive you. 

  Anyway, you may have guessed by now that this template generator has the ability to insert 'madlib' tokens into your files.
  You can insert these tokens into your template file(s) and then define their values when it comes time to generate a template.

  Here are some token examples (you may recognize them):.
  =============================

  \<<var:capitalize:your name\>> 
  \<<var:uppercase:your favorite color\>> 
  \<<var:lowercase:a random adjective\>> 

  Note, 'var' is NOT the only type of token you can put into a template file...  
  You can also define a template file's name or folder path within your project's directory. With...  

  \<<filename:lowercase:file name\>> (you will be asked to enter the file name before the file is created from the template) 
  \<<filename:lowercase:sub/path:file name\>> (your file will be placed in a 'sub/path' directory under your project root directory) 
  \<<filename:lowercase:sub/path:.\>> (your file will be placed in a 'sub/path' directory, but it's template file name will remain the same) 

  I hope this helps. Peace out. 
```
