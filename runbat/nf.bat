@echo off
REM run the newfiles.jar (on windows, must set the .bat parent directory in PATH system variable AND write the full path to the .jar file below): 
REM the second directory path defines where the newfile template roots are stored
REM %cd% passes the current console directory to the jar program
REM %~n0 passes the file path of this batch file to the console program
REM %* passes any additional arguments (typed into the console) to the jar program
java -jar "C:\Users\gmilligan\JAVA_APPLICATIONS_NETBEANS\newfiles\dist\newfiles.jar" "C:\Users\gmilligan\JAVA_APPLICATIONS_NETBEANS\newfiles\runbat\templates" %cd% %~n0 %*