package newfiles;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

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
        "end", //3: stop entering commands for newfiles.jar... exit app, eg: "nf end"
        "templates", //4: open the root templates directory file-system window
        "export" //5: export a project's template files, eg: "nf export 3"
    };
    //help text for commands (parallel array for mCommands)
    private static final String[] mCmdHelpText = 
    {
        "show a list of available templates, ie: \"{nf} "+mCommands[0]+"\"",
        "use a template based on its number (starts input process), eg: \"{nf} "+mCommands[1]+" 3\"",
        "show help for available commands, eg: \"{nf} "+mCommands[2]+"\"",
        "exit app, eg: \"{nf} "+mCommands[3]+"\"",
        "open the root templates directory file-system window, eg: \"{nf} "+mCommands[4]+"\"",
        "export a project's template files, eg: \"{nf} "+mCommands[5]+" 3\""
    };
    
    private static int mUseTemplateIndex; //the integer number of the current template being used
    private final static int mNumArgsFromBatch=3; //the number of arguments that get passed to this app automatically
    //open up a direcctory window
    private static void openDirWindow(String dirPath){
        Desktop desktop = Desktop.getDesktop();
        try {
            File dirToOpen = new File(dirPath);
            System.out.print(" opening... ");
            desktop.open(dirToOpen);
            System.out.println("");
        } catch (IOException e) {
            System.out.println("\nUh oh... failed to open directory --> " + dirPath);
            System.out.println(e.getMessage()+"\n");
        }
    }
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
            case 3: //3: stop entering commands for newfiles.jar... exit app
                isEnd=true;
                break;
            case 4: //4: open the root templates directory file-system window
                openDirWindow(mTemplatesRoot);
                break;
            case 5: //5: export a project's template files, eg: "nf export 3"
                export(args);
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
    //export/package a set of generated template files
    private static void export(String[] args){
        //clear previous use template index (if any)
        mUseTemplateIndex=-1;
        //load the template list, if not already loaded
        loadTemplateList();
        //if there are any templates
        if(mTemplateList.size()>0){
            if(args.length>0){
                int tIndex=-1;
                //if the first argument is the "export" command
                String templateIndex=args[0].trim();
                if(templateIndex.equals(mCommands[5])){
                    //if there is a second argument
                    if(args.length>1){
                        //get the second argument
                        templateIndex=args[1].trim();
                    }
                }
                boolean invalidInt=false;
                try{
                    //try to parse the input into an integer index position
                    tIndex=Integer.parseInt(templateIndex);
                } catch (NumberFormatException e) {
                    System.out.println("\""+templateIndex+"\" invalid integer index.");
                    invalidInt=true;
                }
                //if a valid integer was chosen
                if(tIndex>-1){
                    //if the integer is within the range of the template indexes
                    if(tIndex<mTemplateList.size()){
                        //set the index of the template being used
                        mUseTemplateIndex=tIndex;
                        //show template header
                        System.out.println("  EXPORTING: ");
                        //show the chosen template
                        File[] files=show_template_ls(tIndex, mTemplateList.get(tIndex));
                        //init a list of files to include into the template build
                        ArrayList<File> includeFiles = new ArrayList<File>();                       
                        //loop through each File[] files and include the appropriate files into includeFiles
                        for(int f=0;f<files.length;f++){
                            //if the file does NOT start with "_"
                            if(files[f].getName().indexOf("_")!=0){
                                includeFiles.add(files[f]);
                            }
                        }
                        //set the files to use during the template-export
                        mBuild.useFiles(includeFiles);
                        //export the package for the includeFiles
                        mBuild.export();
                        //reset the use template index
                        mUseTemplateIndex=-1;
                    }else{
                        //index too high out of range
                        templateIndexTooHighMsg(templateIndex);
                    }
                }else{
                    if(!invalidInt){
                        //index is below zero
                        templateIndexTooLowMsg(templateIndex);
                    }
                }
            }
        }else{
            //no templates available...
            showNoTemplatesMsg();
        }
    }
    //show a message indicating that there are no templates
    private static void showNoTemplatesMsg(){
        System.out.println("There are no templates");
        System.out.println("Your setup looks for templates in: ");
        System.out.println(mTemplatesRoot);
        System.out.println("A sub directory under this folder (with at least one file) is considered a template.");
    }
    //show message for template index too high
    private static void templateIndexTooHighMsg(String index){
        System.out.println("\""+index+"\" is too high. No template matched (don't confuse template index with file index).");
    }
    //show message for template index too low
    private static void templateIndexTooLowMsg(String index){
        System.out.println("\""+index+"\" is too low (below zero).");
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
                boolean invalidInt=false;
                try{
                    //try to parse the input into an integer index position
                    tIndex=Integer.parseInt(templateIndex);
                } catch (NumberFormatException e) {
                    System.out.println("\""+templateIndex+"\" invalid integer index.");
                    invalidInt=true;
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
                            //if the file does NOT start with "_"
                            if(files[f].getName().indexOf("_")!=0){
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
                        templateIndexTooHighMsg(templateIndex);
                    }
                }else{
                    if(!invalidInt){
                        //index is below zero
                        templateIndexTooLowMsg(templateIndex);
                    }
                }
            }
        }else{
            //no templates available...
            showNoTemplatesMsg();
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
        File[] subFiles = temRoot.listFiles(); int fileIndex=0;
        for(int f=0;f<subFiles.length;f++){
            //if not a directory
            if(!subFiles[f].isDirectory()){
                //if the file does NOT start with _ (you can disable a template file by adding _ before its name)
                if(subFiles[f].getName().indexOf("_")!=0){
                    String configStr="";
                    //print the file item
                    System.out.println("\t\t  " +fileIndex+ "\t  " + subFiles[f].getName() + configStr);
                    //next file index
                    fileIndex++;
                }
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
            showNoTemplatesMsg();
        }
    }
    //print the help text
    private static void help(String[] args){
        //for each help text item
        System.out.println("\n");
        for (int h=0;h<mCmdHelpText.length;h++){
            //print the command and help text
            System.out.println("  "+mCommands[h]+" -->\t\t "+mCmdHelpText[h].replace("{nf}", mBatchFileName));
            System.out.println("---------------------------------------------------------------------------------------------------");
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

                    //if the file does NOT start with _ (you can disable a template file by adding _ before its name)
                    //but a file called _template.xml is a special config file for the template
                    if(subFiles[f].getName().indexOf("_")!=0){
                        //since dir contains at least one file, it should be added to mTemplateList
                        dirHasFile=true;
                        //add this file to the list of files under this directory
                        templateFiles.add(subFiles[f].getPath());
                    }
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
    private static String getSetupValues(String str, String platform){
        HashMap<String, String> vals = new HashMap<String, String>();
        String batchExample="";String pathSetupExample="";
        //use different setup-values depending on the platform
        switch(platform){
            case "windows":
                //batch file example
                batchExample+="\t@echo off\n";
                batchExample+="\tjava -jar \"{jar_path}\" \"{templates_root}\" {current_dir} {bat_path_var} {attrs}\n\n";
                //how to set up the path
                pathSetupExample+="You will need to edit your system's environment variables to include your {bat_ext} file's parent directory. \n";
                pathSetupExample+="These are example steps you can take to edit your environment variables: \n\n";
                pathSetupExample+="\t1) Right click \"Computer\" in the start menu. Click \"Properties\" \n";
                pathSetupExample+="\t2) \"Advanced systems settings\" \n";
                pathSetupExample+="\t3) \"Environment Variables\" \n";
                pathSetupExample+="\t4) Add the {bat_ext} file's parent folder, such as \"{bat_root}\" to the list in your PATH system variable \n\n";
                pathSetupExample+="\t\"{bat_root}\"\n";
                pathSetupExample+="\tThis is the folder path, for example, if your {bat_ext} file is at \"{bat_path}\" \n";
                pathSetupExample+="\tHowever, it's your choice where to put the {bat_ext} file in your system. \n\n";
                pathSetupExample+="\tIf you are still unsure how to edit your PATH, you should research other examples online. ";
                //values
                vals.put("{templates_root}", "C:\\Users\\username\\newfiles\\templates");
                vals.put("{current_dir}", "%cd%");
                vals.put("{bat_ext}", ".bat");
                vals.put("{bat_name}", "batch");
                vals.put("{bat_path_var}", "%~n0");
                vals.put("{bat_path}", "C:\\Users\\username\\newfiles\\runbat\\nf.bat");
                vals.put("{bat_root}", "C:\\Users\\username\\newfiles\\runbat");
                vals.put("{attrs}", "%*");
                vals.put("{platform}", "Windows");
                vals.put("{jar_path}", "C:\\Users\\username\\newfiles\\jar\\newfiles.jar");
                break;
            case "mac":
                //batch file example
                batchExample+="\t#!/bin/bash\n";
                batchExample+="\tjava -jar \"{jar_path}\" \"{templates_root}\" $(pwd) {bat_path_var} {attrs}\n\n";
                //how to set up the path
                pathSetupExample+="You will need to make your {bat_name} file executable AND add it to your bash path. \n";
                pathSetupExample+="These are example commands that will accomplish this: \n\n";
                pathSetupExample+="\t1) make your {bat_ext} file executable (where \"nf{bat_ext}\" is the file name): \n\n";
                pathSetupExample+="\t\tchmod +x nf{bat_ext}\n\n";
                pathSetupExample+="\t2) create a symlink, to shorten the name of your file to just \"nf\": \n\n";
                pathSetupExample+="\t\tln -s nf{bat_ext} nf\n\n";
                pathSetupExample+="\t3) start editing your bash profile (you will need to add the nf symlink to your path)\n\n";
                pathSetupExample+="\t\tnano ~/.bash_profile\n\n";
                pathSetupExample+="\t4) insert the parent directory of your \"nf\" symlink into your bash profile\n\n";
                pathSetupExample+="\t\texport PATH=/my/directory/path/:$PATH \n\n";
                pathSetupExample+="\t5) run the bash file to save your changes\n\n";
                pathSetupExample+="\t\t. ~/.bash_profile ";
                //values
                vals.put("{templates_root}", "/path/to/newfiles_java/runbat/templates");
                vals.put("{current_dir}", "$(pwd)");
                vals.put("{bat_ext}", ".sh");
                vals.put("{bat_name}", "shell-script");
                vals.put("{bat_path_var}", "\"dirname $0\"");
                vals.put("{bat_path}", "/path/to/newfiles_java/runbat/newfiles.sh");
                vals.put("{bat_root}", "/path/to/newfiles_java/runbat/");
                vals.put("{attrs}", "\"$@\"");
                vals.put("{platform}", "Mac OSX");
                vals.put("{jar_path}", "/path/to/newfiles.jar");
                break;
            default:
                break;
        }
        //for each value to replace
        str=str.replace("{batch_script_example}", batchExample);
        str=str.replace("{path_setup_example}", pathSetupExample);
        for (String token : vals.keySet()) {
            //replace the token with the platform-specific value
            str=str.replace(token, vals.get(token));
        }
        return str;
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
            System.out.println("Your java -jar file is working! ... BUT you MUST pass the correct number of values to the .jar application. ");
            //build out the generic directions (any platform) with tokens to replaced for specific platforms
            String platformSetup = "";
            platformSetup+="A {bat_ext} {bat_name} file needs to call the .jar file while automatically passing the following arguments: \n\n";
            platformSetup+="1) the full path to the root template directory \n";
            platformSetup+=" \tExample: {templates_root} \n";
            platformSetup+="2) the current directory path of the console shell \n";
            platformSetup+=" \tExample: {current_dir} \n";
            platformSetup+="3) the path to the {bat_ext} {bat_name} file that calls the .jar java program \n";
            platformSetup+=" \tExample: {bat_path_var} \n";
            platformSetup+="N) any aditional parameters that you enter into the console \n";
            platformSetup+=" \tExample: {attrs} \n\n";
            platformSetup+="Below is an example of what a {platform} {bat_ext} {bat_name} file should look like. \n";
            platformSetup+="You can choose where to save this file, but call it name the file \"nf{bat_ext}\" for simplicity sake: \n\n";
            platformSetup+="{batch_script_example}";
            platformSetup+="----------------------------------------\n";
            platformSetup+="How will your console reference \"nf{bat_ext}\" when you type \"nf [some command]\" ? \n\n";
            platformSetup+="{path_setup_example}";
            platformSetup+="\n\n----------------------------------------\nFinally, if you type \"nf help\", you should see a list of help-commands. \nIf you do, your setup is complete. \n\n";
            //print windows setup directions
            System.out.println("*========================================* ");
            System.out.println("\tWINDOWS SETUP: ");
            System.out.println("*========================================* ");
            System.out.println(getSetupValues(platformSetup, "windows"));
            //print mac setup directions
            System.out.println("*========================================* ");
            System.out.println("\tMAC OSX SETUP: ");
            System.out.println("*========================================* ");
            System.out.println(getSetupValues(platformSetup, "mac"));
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
