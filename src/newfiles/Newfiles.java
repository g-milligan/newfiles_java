package newfiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author gmilligan
 */
public class Newfiles {
    //fields
    private static String mTemplatesRoot; //the root directory where all templates are stored
    private static String mTargetDir; //the target directory where the new files will be generated (current console directory)
    private static String mBatchFilePath; //the path to the batch file
    private static String mBatchFileName; //the name of the batch file... also the key command to run this app, eg: "nf"
    private static ArrayList<String> mTemplateList; //list of folder paths for each template
    private static BuildTemplate mBuild; //the object class used to build out a template
    //list of commands (the commands can change, but their index position should NOT change)
    private static final String[] mCommands = 
    {
        "ls", //0: show a list of available templates, ie: "nf ls"
        "use", //1: use a template based on its number (starts input process), eg: "nf use 3"
        "help", //2: show help for available commands, eg: "nf" or "nf help" or "nf help ls"
        "end" //3: stop entering commands for newfiles.jar... exit app, eg: "nf end"
    };
    //help text for commands (parallel array for mCommands)
    private static final String[] mCmdHelpText = 
    {
        "show a list of available templates, ie: \"nf ls\"",
        "use a template based on its number (starts input process), eg: \"nf use 3\"",
        "show help for available commands, eg: \"nf\" or \"nf help\" or \"nf help ls\"",
        "exit app, eg: \"nf end\""
    };
    
    private static int mUseTemplateIndex; //the integer number of the current template being used
    private final static int mNumArgsFromBatch=3; //the number of arguments that get passed to this app automatically
    
    //do something depending on the integer
    private static void doSomething(int doWhatInt, String[] args){
        boolean isEnd=false;
        String cmdLabel=mBatchFileName;
        switch(doWhatInt){
            case 0: //0: show a list of available templates
                ls();
                break; 
            case 1: //1: use a template based on its number (starts input process)
                use(args);
                break;
            case 2: //2: show help for available commands
                help(args);
                break;
            case 3: //4: stop entering commands for newfiles.jar... exit app
                isEnd=true;
                break;
            default:
                //invalid command (int code)
                break;
        }
        if(!isEnd){
            //the user can enter another command
            waitForNextCommand(cmdLabel);  
        }
    }
    //prompt the user for the next command
    private static void waitForNextCommand(){
        waitForNextCommand(mBatchFileName);
    }
    //prompt the user for the next command
    private static void waitForNextCommand(String cmdLabel){
        //prompt for next command
        System.out.print(" " + cmdLabel + " >> ");
        try{
            //accept next input from user
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            String line = bufferRead.readLine();
            //parse the line input
            line=line.trim();
            //if the line starts with "nf "
            if(line.toLowerCase().indexOf(mBatchFileName+" ")==0){
                //strip off the starting "nf "
                line=line.substring((mBatchFileName+" ").length());
            }
            //if the line still has a space
            String doWhat=line;
            if(doWhat.contains(" ")){
                //get just the doWhat command before the space
                doWhat=doWhat.substring(0, doWhat.indexOf(" "));
                //trim off the doWhat command from the start of the remaining args
                line=line.substring(doWhat.length());
                line=line.trim();
            }
            //see what to do next (int code)
            int doWhatNextInt=getDoWhatInt(doWhat);
            //if doWhat code was valid
            if(doWhatNextInt > -1){
                //get the args split by spaces
                String[] args = line.split(" ");
                //execute this next command
                doSomething(doWhatNextInt, args);
            }else{
                //invalid command
                invalidCommandMsg(doWhat);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    //prompt the user for the next command (related to building out a template)
    private static String[] getNextUsingCommand(String cmdLabel, String prompt){
        String[] args = null;
        //prompt for next command
        System.out.print(" " + cmdLabel + "\n\n " + mBatchFileName + "/" + mCommands[1] + "/" + mUseTemplateIndex + " | " + prompt + " >> ");
        try{
            //accept next input from user
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            String line = bufferRead.readLine();
            //parse the line input
            line=line.trim();
            if(line.length()>0){
                //split the line up into an arg String[]
                args = line.split(" ");
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return args;
    }
    private static void invalidCommandMsg(String invalidDoWhat){
        System.out.println("\nInvalid command, '"+invalidDoWhat+"'. \nType '" + mCommands[2] + "' for a list of valid commands.\n");
        //the user can enter another command
        waitForNextCommand();  
    }
    //use one of the available templates
    private static void use(String[] args){
        //clear previous use template index (if any)
        mUseTemplateIndex=-1;
        //load the template list, if not already loaded
        loadTemplateList();
        //if there are any templates
        if(mTemplateList.size()>0){
            if(args.length>0){
                int tIndex=-1;
                //if the first argument is the "use" command
                String templateIndex=args[0].trim();
                if(templateIndex.equals(mCommands[1])){
                    //if there is a second argument
                    if(args.length>1){
                        //get the second argument
                        templateIndex=args[1].trim();
                    }
                }
                try{
                    //try to parse the input into an integer index position
                    tIndex=Integer.parseInt(templateIndex);
                } catch (NumberFormatException e) {
                    System.out.println("\""+templateIndex+"\" invalid integer index.");
                }
                //if a valid integer was chosen
                if(tIndex>-1){
                    //if the integer is within the range of the template indexes
                    if(tIndex<mTemplateList.size()){
                        //set the index of the template being used
                        mUseTemplateIndex=tIndex;
                        //show template header
                        System.out.println("  USING: ");
                        //show the chosen template
                        File[] files=show_template_ls(tIndex, mTemplateList.get(tIndex));
                        //wait for next input (select template files to build)
                        String[] fileArgs=getNextUsingCommand("...ALL FILES: [enter] ... EXCLUDE FILES: - #1 #2 ... INCLUDE FILES: #1 #2...", "which files?");
                        //init variables to hold the file indexes exclude/include
                        String includeOrExclude="";
                        ArrayList<Integer> fileIndexList = new ArrayList<Integer>();
                        boolean isExclude=false;
                        //if the user opted out of building ALL files
                        if(fileArgs!=null){
                            if(fileArgs.length>0){
                                //for each build-file argument
                                for(int f=0;f<fileArgs.length;f++){
                                    //if first argument
                                    if(f==0){
                                        includeOrExclude="";
                                        //get the first argument string
                                        String firstArg=fileArgs[f].trim();
                                        //if first argument starts with minus symbol
                                        if(firstArg.indexOf("-")==0){
                                            isExclude=true;
                                            //if the first argument contains more than just the minus symbol
                                            if(firstArg.length()>1){
                                                //remove minus symbol from first argument
                                                fileArgs[f]=fileArgs[f].substring("-".length());
                                            }else{
                                                //first argument is JUST the minus symbol 
                                                fileArgs[f]="";
                                            }
                                        }else{
                                            if(firstArg.indexOf("\\*")==0){break;}
                                        }
                                    }
                                    //if this argument string (that represents a file index) is not blank
                                    String arg=fileArgs[f].trim();
                                    if(arg.length()>0){
                                        try{
                                            //if arg starts with #
                                            if(arg.indexOf("#")==0){
                                                //remove # from the start of the arg
                                                arg=arg.substring(1);
                                            }
                                            //try to parse the input into an integer file index
                                            int argInt=Integer.parseInt(arg);
                                            //if this index was NOT already added to the list
                                            if(!fileIndexList.contains(argInt)){
                                                //if this argument is NOT below zero (in range)
                                                if(argInt>-1){
                                                    //if this argument is NOT above highest file index (in range)
                                                    if(argInt<files.length){
                                                        fileIndexList.add(argInt);
                                                        //if not the first index
                                                        if(includeOrExclude.length()>0){
                                                            includeOrExclude+=", ";
                                                        }else{
                                                            //first file index
                                                            includeOrExclude+="(";
                                                        }
                                                        //add file index to the list
                                                        includeOrExclude+=""+argInt;
                                                    }
                                                }
                                            }
                                        } catch (NumberFormatException e) {}
                                    }
                                }
                            }
                        }
                        //init a list of files to include into the template build
                        ArrayList<File> includeFiles = new ArrayList<File>();                       
                        //loop through each File[] files and include the appropriate files into includeFiles
                        for(int f=0;f<files.length;f++){
                            //if including all files
                            if(fileIndexList.size()<1){
                                includeFiles.add(files[f]);
                            }else{
                                //NOT including all of the template files...
                                //if this file is NOT one of the excluded files
                                if(isExclude&&!fileIndexList.contains(f)){
                                    //include this file
                                    includeFiles.add(files[f]);
                                }else{
                                    //if this file IS one of the included files
                                    if(!isExclude&&fileIndexList.contains(f)){
                                        //include this file
                                        includeFiles.add(files[f]);
                                    }
                                }
                            }
                        }
                        //if no included or excluded files
                        if(fileIndexList.size()<1){
                            includeOrExclude=" all-files(*)\n";
                        }else{
                            //if numbers were excluded
                            if(isExclude){
                                includeOrExclude=" excluded-files"+includeOrExclude+")\n";
                            }else{
                                //included numbers
                                includeOrExclude=" included-files"+includeOrExclude+")\n";
                            }
                        }
                        //print the include/exclude/all message
                        System.out.println(includeOrExclude);
                        //set the files to use during the template-build
                        mBuild.useFiles(includeFiles);
                        //build the generated output for the includeFiles
                        mBuild.build();
                        //reset the use template index
                        mUseTemplateIndex=-1;
                    }else{
                        //index too high out of range
                        System.out.println("\""+templateIndex+"\" is too high. No template matched (don't confuse template index with file index).");
                    }
                }
            }
        }else{
            //no templates available...
            System.out.println("There are no templates");
            System.out.println("Your setup looks for templates in: ");
            System.out.println(mTemplatesRoot);
            System.out.println("A sub directory under this folder (with at least one file) is considered a template.");
        }
    }
    //show a single template director and listing
    private static File[] show_template_ls(int tIndex, String templateFolder){
        //get the template root file object
        File temRoot = new File(templateFolder);
        //get just the template folder without the root path
        templateFolder=templateFolder.substring(mTemplatesRoot.length()+1);
        //show the template folder
        System.out.println("  " + tIndex + "\t  " + templateFolder +"\n");
        //for each file inside this template
        File[] subFiles = temRoot.listFiles();
        for(int f=0;f<subFiles.length;f++){
            //if not a directory
            if(!subFiles[f].isDirectory()){
                //print the file item
                System.out.println("\t\t  " +f+ "\t  " + subFiles[f].getName());
            }
        }
        //add extra space
        System.out.println();
        //return the file list for this template
        return subFiles;
    }
    //list the available templates
    private static void ls(){
        //load the template list, if not already loaded
        loadTemplateList();
        //if there are any templates
        if(mTemplateList.size()>0){
            System.out.println("\n  TEMPLATE LIST: --> " + mTemplatesRoot + File.separator + "..\n");
            //for each template
            for(int t=0; t<mTemplateList.size();t++){
                System.out.println("-------------------------------");
                //show this listing for this template
                show_template_ls(t, mTemplateList.get(t));
            }
        }else{
            //no templates available...
            System.out.println("There are no templates");
            System.out.println("Your setup looks for templates in: ");
            System.out.println(mTemplatesRoot);
            System.out.println("A sub directory under this folder (with at least one file) is considered a template.");
        }
    }
    //print the help text
    private static void help(String[] args){
        //for each help text item
        System.out.println("\n");
        for (int h=0;h<mCmdHelpText.length;h++){
            //print the command and help text
            System.out.println("  "+mCommands[h]+" \t-->\t "+mCmdHelpText[h]);
            System.out.println("------------------------------------");
        }
        System.out.println("\n");
    }
    //force the template list to update the next time it's called
    private static void update(){mTemplateList=null;}
    //load all of the possible templates 
    //(any directory under the mTemplatesRoot that contains at least one file)
    private static void loadTemplateList(){
        //if the template list is NOT already loaded
        if(mTemplateList==null){
            //initialize
            mTemplateList = new ArrayList<String>();
            //if the root template folder exists
            File temRoot = new File(mTemplatesRoot);
            if(temRoot.exists()){
                //for each direct child under temRoot
                File[] subFiles = temRoot.listFiles();
                for(int f=0;f<subFiles.length;f++){
                    //if this direct child under temRoot is a directory
                    if(subFiles[f].isDirectory()){
                        //add this directory to the template list IF it contains at least one file
                        maybeAddDirToTemplateList(subFiles[f]);
                    }
                }
            }
        }
    }
    //add a directory to the template list IF it contains at least one file
    private static void maybeAddDirToTemplateList(File dir){
        //if the directory does NOT start with _ (you can disable a template sub-directory by adding _ before its name)
        if(dir.getName().indexOf("_")!=0){
            boolean dirHasFile=false;
            ArrayList<String> templateFiles = new ArrayList<String>();
            //for each direct child under dir
            File[] subFiles=dir.listFiles();
            for(int f=0;f<subFiles.length;f++){
                //if this direct child under dir is a directory
                if(subFiles[f].isDirectory()){
                    //add this directory to the template list IF it contains at least one file
                    maybeAddDirToTemplateList(subFiles[f]);
                }else{
                    //this is NOT a directory, it's a file...

                    //since dir contains at least one file, it should be added to mTemplateList
                    dirHasFile=true;
                    //add this file to the list of files under this directory
                    templateFiles.add(subFiles[f].getPath());
                }
            }
            //if dir contained at least one file
            if(dirHasFile){
                //get the directory path
                String dirPath=dir.getPath();
                //if this directory path is NOT already listed in mTemplateList
                if(!mTemplateList.contains(dirPath)){
                    //add the directory path to mTemplateList
                    mTemplateList.add(dirPath);
                }
            }
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //batch file needs to send this app:
        //1) the full path to where the template directories are stored
        //2) the current directory path of the console shell 
        //3) the path to the batch file that calls the java program
        //----------------- from the user ------------------------------
        //4) any additional arguments that are entered by the shell user
        
        //...if calling the JAR file correctly (passing the arguments)
        if(args!=null&&args.length>=mNumArgsFromBatch){
            //init key values
            mTemplatesRoot = args[0];
            mTargetDir = args[1]; 
            mBatchFilePath = args[2];
            File batchFile = new File(mBatchFilePath);
            mBatchFileName = batchFile.getName();
            mUseTemplateIndex=-1;
            mBuild = new BuildTemplate(mTargetDir, mBatchFileName); //object used to build the given template files
            //start the app
            start(args);
        }else{
            //print the troubleshooting setup message
            System.out.println("ERROR -- The correct number of values MUST be passed to the .jar application. ");
            System.out.println("For example, if you are using a windows machine... ");
            System.out.println("A batch file needs to call the .jar file while automatically passing the following arguments: \n");
            System.out.println("1) the full path to the root template directory ");
            System.out.println(" \tExample: C:\\Users\\username\\newfiles\\templates");
            System.out.println("2) the current directory path of the console shell ");
            System.out.println(" \tExample: %cd%");
            System.out.println("3) the path to the batch file that calls the java program ");
            System.out.println(" \tExample: %~n0");
            System.out.println("N) any aditional parameters that you enter into the console ");
            System.out.println(" \tExample: %* \n");
            System.out.println("On windows, you would add your .bat (batch) file to your system's environment variables path. This would allow you to enter commands in any command line directory.");
            System.out.println("For example, IF your batch file path is... ");
            System.out.println("C:\\Users\\username\\newfiles\\runbat\\nf.bat");
            System.out.println("... then you would have to add \"C:\\Users\\username\\newfiles\\runbat\" to your system's PATH variable.");
            System.out.println("\nNote: your .bat (batch) file name determines the special command to evoke the application.");
            System.out.println("For example, IF your .bat (batch) file is called \"nf.bat\", then you can use commands like \"nf ls\".\n");
            System.out.println("Below is an example of what a .bat (batch) file should look like: \n");
            System.out.println("@echo off");
            System.out.println("java -jar \"C:\\Users\\username\\newfiles\\jar\\newfiles.jar\" \"C:\\Users\\username\\newfiles\\templates\" %cd% %~n0 %*\n\n");
        }
    }
    //get the index position of this argument command
    private static int getDoWhatInt(String cmdStr){
        int doWhatInt=-1; cmdStr=cmdStr.toLowerCase();
        //if command is NOT blank
        if(!cmdStr.trim().equals("")){
            //for each command
            for (int i=0;i<mCommands.length;i++){
                //if this is the current command
                if(mCommands[i].toLowerCase().equals(cmdStr)){
                    //set the command's index number
                    doWhatInt=i;
                    break;
                }
            }
        }else{
            //command is blank...
            
            //use the help command by default
            doWhatInt=2;
        }
        return doWhatInt;
    }
    //application main functionality entry point
    private static void start(String[] args){
        //what basic command was given?
        String doWhat=mCommands[2]; //show help command by default
        //if the user entered any arguments at all(not just "nf")
        if(args.length>mNumArgsFromBatch){
            //get the first argument as the doWhat command
            doWhat=args[mNumArgsFromBatch];
        }
        int doWhatInt=getDoWhatInt(doWhat);
        //if the command was found in the list of commands
        if(doWhatInt>-1){
            //additional arguments (after the doWhat command)
            String[] additionalArgs=new String[args.length-mNumArgsFromBatch];
            if(args.length>mNumArgsFromBatch){
                //for each additional arg
                for(int a=mNumArgsFromBatch;a<args.length;a++){
                    //set the additional arg to the array
                    additionalArgs[a-mNumArgsFromBatch]=args[a];
                }
            }
            //load the additional parameters into an array
            doSomething(doWhatInt, additionalArgs);
        }else{
            //doWhat command didn't match any valid commands...
            invalidCommandMsg(doWhat);
        }
    }
}
