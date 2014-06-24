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
    private static ArrayList<String> mTemplateList; //list of folder paths for each template
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
        "stop entering commands... exit app, eg: \"nf end\""
    };
    //do something depending on the integer
    private static void doSomething(int doWhatInt, String[] args){
        boolean isEnd=false;
        switch(doWhatInt){
            case 0: //0: show a list of available templates
                ls();
                break; 
            case 1: //1: use a template based on its number (starts input process)
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
            waitForNextCommand();  
        }
    }
    //prompt the user for the next command
    private static void waitForNextCommand(){
        //prompt for next command
        System.out.print("end/next command>> ");
        try{
            //accept next input from user
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            String line = bufferRead.readLine();
            //parse the line input
            line=line.trim();
            File batchFile = new File(mBatchFilePath);
            String batchFileName = batchFile.getName();
            //if the line starts with "nf "
            if(line.toLowerCase().indexOf(batchFileName+" ")==0){
                //strip off the starting "nf "
                line=line.substring((batchFileName+" ").length());
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
    private static void invalidCommandMsg(String invalidDoWhat){
        System.out.println("\nInvalid command, '"+invalidDoWhat+"'. \nType '" + mCommands[2] + "' for a list of valid commands.\n");
        //the user can enter another command
        waitForNextCommand();  
    }
    //list the available templates
    private static void ls(){
        //load the template list, if not already loaded
        loadTemplateList();
        //if there are any templates
        if(mTemplateList.size()>0){
            System.out.println("\n  TEMPLATE LIST: --> " + mTemplatesRoot + "\n");
            //for each template
            for(int t=0; t<mTemplateList.size();t++){
                System.out.println("-------------------------------");
                System.out.println("  " + t + "\t  " + mTemplateList.get(t)+"\n");
                //for each file inside this template
                File temRoot = new File(mTemplateList.get(t));
                File[] subFiles = temRoot.listFiles();
                for(int f=0;f<subFiles.length;f++){
                    //if not a directory
                    if(!subFiles[f].isDirectory()){
                        System.out.println("\t\t  " +f+ "\t  " + subFiles[f].getName());
                    }
                }
                System.out.println();
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
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //init key values
        mTemplatesRoot = args[0];
        mTargetDir = args[1]; 
        mBatchFilePath = args[2];
        start(args);
    }
    //get the index position of this argument command
    private static int getDoWhatInt(String cmdStr){
        int doWhatInt=-1; cmdStr=cmdStr.toLowerCase();
        //for each command
        for (int i=0;i<mCommands.length;i++){
            //if this is the current command
            if(mCommands[i].toLowerCase().equals(cmdStr)){
                //set the command's index number
                doWhatInt=i;
                break;
            }
        }
        return doWhatInt;
    }
    //application main functionality entry point
    private static void start(String[] args){
        int numArgsFromBatch=3; //the number of arguments that the batch file is responsible for sending to the jar application
        //if there was at least a doWhat command (3rd argument, 1st argument that doesn't come from batch file)
        if(args.length>=numArgsFromBatch){
            //what basic command was given?
            String doWhat=mCommands[2]; //show help command by default
            //if the user entered any arguments at all(not just "nf")
            if(args.length>numArgsFromBatch){
                //get the first argument as the doWhat command
                doWhat=args[numArgsFromBatch];
            }
            int doWhatInt=getDoWhatInt(doWhat);
            //if the command was found in the list of commands
            if(doWhatInt>-1){
                //additional arguments (after the doWhat command)
                String[] additionalArgs=new String[args.length-numArgsFromBatch];
                if(args.length>numArgsFromBatch){
                    //for each additional arg
                    for(int a=numArgsFromBatch;a<args.length;a++){
                        //set the additional arg to the array
                        additionalArgs[a-numArgsFromBatch]=args[a];
                    }
                }
                //load the additional parameters into an array
                doSomething(doWhatInt, additionalArgs);
            }else{
                //doWhat command didn't match any valid commands...
                invalidCommandMsg(doWhat);
            }
        }else{
            //the batch file did NOT provide enough arguments...
            //batch file needs to send this app:
            //1) the full path to where the template directories are stored
            //2) the current directory path of the console shell 
            //3) the path to the batch file that calls the java program
            //----------------- from the user ------------------------------
            //4) any additional arguments that are entered by the shell user
            
            //*** show help
        }
    }
}
