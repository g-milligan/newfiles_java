package newfiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
All rights reserved
Copyright (C) 2014 by Gregg Tyler Milligan II

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
------------------------------------------------------------------------------

Newfiles is a command-line tool to help build new projects from existing 
templates, instead of from scratch. You can create templates for 
virtually any type of project. Newfiles is completely platform agnostic; 
it's not geared towards any one type of code Nor any one type of platform. 
You are only limited by your imagination, when it comes to creating templates. 

For more information / documentation, please visit <http://pandowerx.com>
Or email <pandowerx@gmail.com>
------------------------------------------------------------------------------
 */
public class BuildTemplate {
    
    private static ArrayList<File> mIncludeFiles; //Files objects to include into the build 
    private static String mTargetDir;
    private static String mBatchFileName;
    private static String mTemplatesRoot;
    private static String mUseTemplatePath; //the path to the current using template
    
    //objects
    private static TemplateData mData; //the primary template data (eg: tokens, aliases, etc...)
    private static StrMgr mStrMgr;
    private static FileMgr mFileMgr;
    
    //constructor
    public BuildTemplate(String targetDir, String batchFileName, String templatesRoot){
        //objects
        mData = new TemplateData();
        mStrMgr = new StrMgr();
        mFileMgr = new FileMgr();
        //fields
        mIncludeFiles=null;
        mTargetDir=targetDir;
        mBatchFileName=batchFileName;
        mTemplatesRoot=templatesRoot;
    }
    //method to use a given list of files
    public void useFiles(String useTemplatePath, ArrayList<File> includeFiles){
        mIncludeFiles=includeFiles;
        mUseTemplatePath=useTemplatePath;
        mData.useFiles(mUseTemplatePath, mIncludeFiles);
    }
    //build the template
    public void build(){
        if(mIncludeFiles!=null){
            if(mIncludeFiles.size()>0){
                //1) look through all of the mIncludeFiles and load the content/token/alias hash lookups
                boolean atLeastOneToken = mData.loadFilesData();
                //2) accept user input for tokens
                userInputForTokens(atLeastOneToken, "", 0, 0, mData.mUniqueTokenNames, mData.mUniqueListTokenNames, mData.mFileTokensLookup);
                //3) get the template files' contents with replaced tokens 
                setTokenValues();
                //4) write the template files
                writeOutputFiles();
                //reset the include files
                mIncludeFiles=null;
            }
        }
    }
    //export a package based on a template
    public String export(){
        String exportDir="";
        if(mIncludeFiles!=null){
            if(mIncludeFiles.size()>0){
                //1) look through all of the mIncludeFiles and load the content/token/alias hash lookups
                boolean atLeastOneToken = mData.loadFilesData();
                //2) accept user input for tokens (only if the token is used to build out the file path)
                ArrayList<String> inFileNameTokens=userInputForFilenameTokens(atLeastOneToken);
                //3) set the template files' contents with replaced filename-tokens
                //only replace token value in the inFileNameTokens list
                setTokenValues(inFileNameTokens);
                //4) write the export files
                exportDir=writeOutputFiles("export");
                //reset the include files
                mIncludeFiles=null;
            }
        }
        return exportDir;
    }
    //prompt the user for the next input
    public String getInput(String inputLabel){return getInput(inputLabel, null);}
    public String getInput(String inputLabel, ArrayList<String> inputOptions){
         //if the options aren't null
        boolean hasOptions = false;
        if(inputOptions!=null){
            //if there are any option items
            if(inputOptions.size()>0){
                hasOptions=true;
                System.out.print("\n \tChoose option # ");
                System.out.println("\n");
                //for each option
                for(int i=0;i<inputOptions.size();i++){
                    System.out.println(" \t" + i + "  \"" + inputOptions.get(i) + "\"");
                }
                System.out.print("\n " + inputLabel + " >> ");
            }
        }
        //no options (any value)
        if(!hasOptions){
            //prompt for next command
            System.out.print(" " + inputLabel + " >> ");
        }
        String line = "";
        try{
            //accept next input from user
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            line = bufferRead.readLine();
            line=line.trim();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        //if input is based on options...
        if(hasOptions){
            //if not trying to go back
            if(!line.equals(mStrMgr.mStartToken)){
                //if no input was given
                if(line.length()<1){line="0";}
                int lineInt=-1;
                String errMsg = "";
                try{
                    lineInt=lineInt=Integer.parseInt(line);
                }catch(NumberFormatException e){
                    errMsg="\""+line+"\" is not a number. Try again...";
                }
                //if valid number
                if(errMsg.length()<1){
                    //if number greater than -1
                    if(lineInt>-1){
                        //if number less than the number of items
                        if(lineInt<inputOptions.size()){
                            //set the chosen option
                            line=inputOptions.get(lineInt);
                        }else{
                            //number too high
                            errMsg="\""+line+"\" is too high. Try again...";
                        }
                    }else{
                        //number less than 0
                        errMsg="\""+line+"\" is too low. Try again...";
                    }
                }
                //if number too high or low
                if(errMsg.length()>0){
                    //print error message
                    System.out.print("\n " + errMsg + " \n");
                    //recursive try again
                    line = getInput(inputLabel, inputOptions);
                }else{
                    //print the valid selected value
                    System.out.println(" \t--> \"" + line + "\" \n");
                }
            }
        }
        return line;
    }
    //get the input from the user for all of the template tokens
    private int getAllTokenInput(String nestedParentKey, int nestedLevel, int listItemIndex, ArrayList<String> uniqueTokenNames, ArrayList<String> uniqueListTokenNames, int startIndex, int count, boolean isBack){
        /*ASSIGN REAL USER VALUES TO EACH UNIQUE TOKEN NAME
            1) mTokenInputValues
            load a HashMap: HashMap<[tokenName], [userInput]>
         */
        String nestedPrefix="";
        if(nestedLevel > 0){
           nestedPrefix+="  ^"+nestedLevel+"/  "; 
        }
        String backTxt=mStrMgr.mStartToken; //<<
        String stopTxt=mStrMgr.mEndToken; //>>
        //message to display at the start of repeated list items
        String startOneListItem = "\n "+nestedPrefix+"--------------------\n";
        startOneListItem+=" "+nestedPrefix+"DEFINE ONE ITEM: "+nestedParentKey+" (" + (listItemIndex + 1) + ")\n";
        String startListItemDirections=" "+nestedPrefix+"Type \""+backTxt+"\" to back-track... \n";
        startListItemDirections+=" "+nestedPrefix+"Type \""+stopTxt+"\" to cancel current item / end list... \n\n";
        //if NOT moved back
        if(!isBack){
            //START DIRECTIONS
            //================
            //display direction on how to move back
            
            if(nestedLevel > 0){
                //start repeated list item
                System.out.print(startOneListItem + startListItemDirections);
            }else{
                //start non-repeated list items
                System.out.println("\n "+nestedPrefix+"--------------------");
                System.out.println(" "+nestedPrefix+"DEFINE VALUES: ");
                System.out.println(" "+nestedPrefix+"Type \""+backTxt+"\" to back-track... \n");
            }
        }else{
            //moving back... 
            
            if(nestedLevel > 0){
                //re-start repeated list item
                System.out.print(startOneListItem + "\n");
            }
        }
        //LOOP THROUGH TOKENS INSIDE ONE LEVEL
        //====================================
        //for each input value to enter
        String lastTokenName = null;
        for(int i=startIndex;i<=uniqueTokenNames.size();i++){
            String input="";
            //if NOT entered all of the values
            if(i<count){
                //if this is one of the NON-list-token values
                if(i<uniqueTokenNames.size()){
                    //ACCEPT ONE USER INPUT VALUE
                    //=======================
                    //if this token requires specific value options
                    ArrayList<String> inputOptions = new ArrayList<String>();
                    if(mData.mUniqueTokenNameOptions.containsKey(uniqueTokenNames.get(i))){
                        //get the allowed options
                        inputOptions = mData.mUniqueTokenNameOptions.get(uniqueTokenNames.get(i));
                    }
                    //if the user is allowed to enter any value
                    if(inputOptions.size()<1){
                        //get user input
                        input=getInput("" + nestedPrefix + (i+1) + "/" + count + ") Enter --> \"" +uniqueTokenNames.get(i) + "\"");
                    }else{
                        //there are specific option values that are required

                        //get user input
                        input=getInput("" + nestedPrefix + (i+1) + "/" + count + ") Select --> \"" +uniqueTokenNames.get(i) + "\"", inputOptions);
                    }
                }else{
                    //this is a list token value...
                    
                    //if there are any list token names
                    if(uniqueListTokenNames!=null){
                        if(uniqueListTokenNames.size()>0){
                            //HANDLE THE LIST TOKENS
                            //======================
                            //for each list token
                            int inputNum = i + 1;
                            for(int ls=0;ls<uniqueListTokenNames.size();ls++){
                                //if any tokens were put into the nested list
                                if(mData.mNestedUniqueTokenNames!=null){
                                    //get the nested key
                                    String nestedKey=nestedParentKey;
                                    if(nestedKey.length()>0){nestedKey+=mStrMgr.mAliasSetter;}
                                    nestedKey+=uniqueListTokenNames.get(ls);
                                    //if there is data for this nested key (there should be or else something is wrong)
                                    if(mData.mNestedUniqueTokenNames.containsKey(nestedKey)){
                                        //get the nested token lists, under this parent list token
                                        HashMap<String, ArrayList<String>> nestedFileTokensLookup=mData.mNestedFileTokensLookup.get(nestedKey);
                                        ArrayList<String> nestedUniqueTokenNames=mData.mNestedUniqueTokenNames.get(nestedKey);
                                        ArrayList<String> nestedUniqueListTokenNames=null;
                                        //if there are any list tokens nested under a different token parent
                                        if(mData.mNestedUniqueListTokenNames!=null){
                                            if(mData.mNestedUniqueListTokenNames.containsKey(nestedKey)){
                                                nestedUniqueListTokenNames=mData.mNestedUniqueListTokenNames.get(nestedKey);
                                            }
                                        }else{nestedUniqueListTokenNames=new ArrayList<String>();}
                                        //introduce this list token's input
                                        System.out.println(" " + nestedPrefix + (inputNum+ls) + "/" + count + ") List --> \"" +uniqueListTokenNames.get(ls) + "\"");
                                        //if the user did not elect to quit this list
                                        int levelIsComplete = 0;
                                        while(levelIsComplete==0){
                                            //recursive call to ask for this nested template section's token values
                                            int[] inputResult = userInputForTokens(true, nestedKey, nestedLevel+1, listItemIndex, nestedUniqueTokenNames, nestedUniqueListTokenNames, nestedFileTokensLookup);
                                            listItemIndex=inputResult[1]+1; //get the listItemIndex and move to the next one
                                            levelIsComplete=inputResult[0]; //is level complete?
                                        }
                                        //the list has ended
                                        System.out.println(" \t--> End \"" +uniqueListTokenNames.get(ls) + "\" list");
                                        break;
                                    }
                                }else{
                                    //the template contains a list that doesn't have any nested tokens...
                                    
                                    System.out.println(" " + nestedPrefix + (inputNum+ls) + "/" + count + ") List --> \"" +uniqueListTokenNames.get(ls) + "\"");
                                    input=getInput(nestedPrefix + "Repeat this sub-section, how many times?");
                                    //***
                                }
                            }
                        }
                    }
                }
            }else{
                //if not nested in a list 
                if(nestedLevel < 1){
                    //ALL SET; BUILD PROJECT OR GO BACK?
                    //==================================
                    //entered all of the values
                    System.out.println("");
                    System.out.println(" Target root path --> " + mTargetDir+File.separator+"...");
                    input=getInput("Ok, got it. Hit [enter] to build");
                }
            }
            //if the input is the back text
            if(input.equals(backTxt)){
                //ROLL BACK TO THE PREVIOUS INPUT VALUE (LIKE UNDO)
                //=================================================
                //if not at first input field (there can be multiple input fields per item)
                if(i!=0){
                    //print the back message
                    System.out.println("\n \tback (input-value)... \n");
                    //if there was a previous item
                    if(lastTokenName!=null){
                        //if the previous value was saved
                        if(mData.mTokenInputValues.containsKey(lastTokenName)){
                           //remove the saved value for the previous item 
                           mData.mTokenInputValues.remove(lastTokenName);
                        }
                    }
                    //recursive move back (previous input field)
                    getAllTokenInput(nestedParentKey,nestedLevel,listItemIndex,uniqueTokenNames,uniqueListTokenNames,i-1,count,true);
                }else{
                    //already at first input field...
                    
                    //if at first input-value (inside list-item), but not the first list-item
                    if(listItemIndex!=0){
                        //print the back message
                        System.out.println("\n \tback (list-item)... \n");
                        //if there was Not previous input-value
                        if(lastTokenName==null){
                            //try to get the lastTokenName for the previous input-value
                            lastTokenName = nestedParentKey + mStrMgr.mAliasSetter + uniqueTokenNames.get(uniqueTokenNames.size()-1) + mStrMgr.mTokenSeparator + (listItemIndex-1);
                        }
                        //if the previous value was saved
                        if(mData.mTokenInputValues.containsKey(lastTokenName)){
                           //remove the saved value for the previous item 
                           mData.mTokenInputValues.remove(lastTokenName); 
                        }
                        //recursive move back (previous input field)
                        listItemIndex--; //decrement list item index
                        getAllTokenInput(nestedParentKey,nestedLevel,listItemIndex,uniqueTokenNames,uniqueListTokenNames,count-1,count,true);
                    }else{
                        //already at the first input field of the first item (in this level)...

                        //print the back message
                        System.out.println("\n \tCANNOT go back; already at first input field... \n");
                        //recursive repeat ask for input
                        getAllTokenInput(nestedParentKey,nestedLevel,listItemIndex,uniqueTokenNames,uniqueListTokenNames,i,count,true);
                    }
                }
                break;
            }else{
                //if NOT already saved all input values
                if(i<count){
                    //if NOT already saved all input values (for NON-list tokens)
                    if(i<uniqueTokenNames.size()){
                        //STORE THE INPUT VALUE FOR THIS TOKEN
                        //====================================
                        //if this token key is nested under a parent token
                        String itemIndex="";
                        String tokenInputKey=uniqueTokenNames.get(i);
                        if(nestedParentKey.length()>0){
                            //nested tokens can have multiple values, so the input value key should end in an index number
                            itemIndex=mStrMgr.mTokenSeparator + listItemIndex;
                            //this token input key should reflect the nestedKey hierarchy
                            tokenInputKey=nestedParentKey+mStrMgr.mAliasSetter+tokenInputKey;
                        }
                        //add the index (if any) to the end of the tokenInputKey
                        tokenInputKey+=itemIndex;
                        //add this input value to the list
                        mData.mTokenInputValues.put(tokenInputKey, input);
                        //record the last token name to be assigned a value
                        lastTokenName=tokenInputKey;
                    }
                }
            }
        }
        return listItemIndex;
    }
    //get a list of tokens that have an influence on filename/paths
    //note: the only way for a token to influence a file name or file path is for it to have its alias inside either the name or path
    private ArrayList<String> getTokensInFilenames(){
        /*RETURN A LIST OF ALL OF THE TOKENS (WITHIN THE TEMPLATE) THAT HAVE INFLUENCE OVER ANY FILENAME
        Load an ArrayList<[uniqe token name]>
        
        This is an import list of tokens to have if you want to ask the user 
        ONLY for tokens that distinguish an individual project's files from those belonging to a different project
        
        If a file's name is defined inside _filenames.xml, then ONLY token aliases within _filenames.xml (if any) can have influence for THAT file...
        But a token can control a template file's name if it's NOT defined in _filenames.xml; _filenames.xml definitions overwrite token definitions.
            
            your name
            your test
         */
        ArrayList<String> inFileNameTokens = new ArrayList<String>();
        //for each file that contains tokens
        for (String filePath : mData.mFileTokensLookup.keySet()) {
            String fileAliasPath=filePath;
            String name="";String dir="";
            //GET THE FILENAME TOKEN FROM EITHER _filenames.xml OR WITHIN A TOKEN INSIDE THE FILE
            //if this file's name gets defined in _filenames.xml
            if(mData.mFilenameXmlOverwriteLookup.containsKey(filePath)){
                //the file aliases (used inside the filename) are assigned to _filenames.xml instead of the actual template file
                fileAliasPath=mUseTemplatePath+File.separator+mStrMgr.mFilenamesXml;
                //get the token string
                String tokenStr=mData.mFilenameXmlOverwriteLookup.get(filePath);
                String[] tokenParts = tokenStr.split(mStrMgr.mTokenSeparator);
                //get the file name
                name=mData.getTokenPart("name", tokenParts);
                //if the name is NOT a string literal (non-string literals CANNOT contain aliases)
                if (name.indexOf("\"") != 0 && name.indexOf("'") != 0){
                    name=""; //no need to check a NON-string literal. It will NOT contain any aliases
                }
                //get the file folder
                dir=mData.getTokenPart("dir", tokenParts);
            }else{
                //TRY TO FIND A FILENAME TOKEN WITHIN THE TEMPLATE FILE... _filenames.xml DOESN'T INFLUENCE THIS TEMPLATE FILE'S NAME...
                //for each token belonging to this file
                ArrayList<String> tokens = mData.mFileTokensLookup.get(filePath);
                for(int t=0;t<tokens.size();t++){
                    //get the token text, eg: <<var:l:something>>
                    String tokenStr=tokens.get(t);
                    String[] tokenParts = tokenStr.split(mStrMgr.mTokenSeparator);
                    //get the token alias
                    String tokenType=mData.getTokenPart("type", tokenParts);
                    //if the token type is filename
                    if(tokenType.equals("filename")){
                        //get the file name
                        name=mData.getTokenPart("name", tokenParts);
                        //if the name is NOT a string literal (non-string literals CANNOT contain aliases)
                        if (name.indexOf("\"") != 0 && name.indexOf("'") != 0){
                            name=""; //no need to check a NON-string literal. It will NOT contain any aliases
                        }
                        //get the file folder
                        dir=mData.getTokenPart("dir", tokenParts);
                        //end the looped search for the filename token inside this file
                        break;
                    }
                }
            }
            //CHECK ALL ALIASES TO SEE IF THEY ARE BEING USED IN EITHER THE FILE dir OR FILE name
            //if EITHER the filename directory OR the name is a candidate for possibly containing token aliases
            if(name.length()>0||dir.length()>0){
                //if the file (that defines the filename token) contains any aliases
                if(mData.mFileAliasesLookup.containsKey(fileAliasPath)){
                    //for each alias inside this file (one or more aliases MAY appear inside either the filename "name" or "dir")
                    for (String aliasStr : mData.mFileAliasesLookup.get(fileAliasPath).keySet()) {
                        //get the token name, associated with this alias
                        String nameForAlias=mData.getTokenPart("name", mData.mFileAliasesLookup.get(fileAliasPath).get(aliasStr));
                        //if this token name is NOT ALREADY listed as influencing one or more filename/paths
                        if(!inFileNameTokens.contains(nameForAlias)){
                            boolean aliasInflencesFilename=false;
                            //if the "name" might contain one or more aliases
                            if(name.length()>0){
                                //if this alias string is inside the file "name"
                                if(name.contains(aliasStr)){
                                    aliasInflencesFilename=true;
                                }
                            }
                            //if this alias was NOT found as part of the file "name"
                            if(!aliasInflencesFilename){
                                //if the "dir" might contain one or more aliases
                                if(dir.length()>0){
                                    //if this alias string is inside the file "directory"
                                    if(dir.contains(aliasStr)){
                                        aliasInflencesFilename=true;
                                    }
                                }
                            }
                            //if this alias influences this filename
                            if(aliasInflencesFilename){
                                //add the token name (associated with this alias) to the list
                                //the user will have to input a value for this token in order to determine the file path
                                inFileNameTokens.add(nameForAlias);
                            }
                        }
                    }
                }
            }
        }
        return inFileNameTokens;
    }
    //accept user input ONLY for tokens that effect the file-name/paths
    private ArrayList<String> userInputForFilenameTokens(boolean atLeastOneToken){ 
        //get ONLY the token names that have influence over one or more filenames or paths
        ArrayList<String> inFileNameTokens=getTokensInFilenames();
        //if there is at least one token
        if(inFileNameTokens.size()>0){
            System.out.println(" Which project would you like to export? \n");
            //if more than one token value to input
            if(inFileNameTokens.size()>1){
                System.out.println(" "+inFileNameTokens.size()+" unique token values needed to identify your project: ");
            }else{
               //only one token value to input 
               System.out.println(" only ONE token value needed to identify your project: ");
            }
            //for each token value to input
            System.out.print(" ");
            for(int v=0;v<inFileNameTokens.size();v++){
                //if NOT the first unique token name
                if(v!=0){
                    System.out.print(", ");
                }
                //print the token name
                System.out.print("\""+inFileNameTokens.get(v)+"\"");
            }
            System.out.println("");
            //get all of the token input values from the user
            getAllTokenInput("",0,0,inFileNameTokens,null,0,inFileNameTokens.size(),false);System.out.println("");
        }
        return inFileNameTokens;
    }
    //accept user input for each of the unique tokens (inside one nested level)
    private int[] userInputForTokens(boolean atLeastOneToken, String nestedParentKey, int nestedLevel, int listItemIndex, ArrayList<String> uniqueTokenNames, ArrayList<String> uniqueListTokenNames, HashMap<String, ArrayList<String>> fileTokensLookup){
        int levelIsComplete = 1;
        //if there is at least one token
        if(uniqueTokenNames.size()+uniqueListTokenNames.size()>0){
            String nestedPrefix=""; String perItem="";
            if(nestedLevel > 0){
               nestedPrefix+="  ^"+nestedLevel+"/  "; 
               perItem = " (per item)";
               levelIsComplete = 0;
            }
            //DISPLAY THE LIST OF NON-NESTED, UNIQUE, TOKEN NAMES
            //===================================================
            //if first item in a list
            if(listItemIndex==0){
                //if more than one token value to input
                if(uniqueTokenNames.size()+uniqueListTokenNames.size()>1){
                    System.out.println(" "+nestedPrefix+(uniqueTokenNames.size()+uniqueListTokenNames.size())+" unique token values to input"+perItem+": ");
                }else{
                   //only one token value to input 
                   System.out.println(" "+nestedPrefix+"only ONE token value to input"+perItem+": ");
                }
                //for each token value to input
                System.out.print(" "+nestedPrefix);
                for(int v=0;v<uniqueTokenNames.size();v++){
                    //if NOT the first unique token name
                    if(v!=0){
                        System.out.print(", ");
                    }
                    //print the token name
                    System.out.print("\""+uniqueTokenNames.get(v)+"\"");
                }
                //for each <<list>> token value to input
                for(int v=0;v<uniqueListTokenNames.size();v++){
                    //if NOT the first unique token name
                    if(v!=0||uniqueTokenNames.size()>0){
                        System.out.print(", ");
                    }
                    //print the token name
                    System.out.print("LIST(\""+uniqueListTokenNames.get(v)+"\")");
                }
            }
            //DISPLAY LIST OF TOKENS? (FOR TEMPLATE-CODE DEBUGGING)
            //====================================================
            String endListEntry = "";String lsOrContinue="";
            if(nestedLevel>0){
                endListEntry="\">>\"=cancel item/list-entry, ";
            }else{
                System.out.println("\n");
                //get the list tokens/continue choice 
                lsOrContinue=getInput(nestedPrefix+"\"ls\"=list files/tokens, "+endListEntry+"[enter]=continue");
            }
            //if the user chose to view the file / token listing
            if(lsOrContinue.toLowerCase().equals("ls")){
                //for each file (that contains at least one token)
                for (String path : fileTokensLookup.keySet()) {
                    File file=new File(path);
                    //print the file name and number of tokens
                    ArrayList<String> fileTokens = fileTokensLookup.get(path);
                    System.out.println("\n "+nestedPrefix+file.getName()+" >> "+fileTokens.size()+" token(s) \n");
                    //for each token in this file
                    for(int t=0;t<fileTokens.size();t++){
                        //print the token text
                        String tokenStr=fileTokens.get(t).trim();
                        System.out.println("   "+nestedPrefix+tokenStr);
                        //if this is a list token
                        String tokenType=mData.getTokenPart("type", tokenStr);
                        if(tokenType.equals("list")){
                            //if this list token is in mTemplateChunks
                            String tokenName=mData.getTokenPart("name", tokenStr);
                            if(mData.mTemplateChunks.containsKey(tokenName)){
                                //if this list token name is in this file
                                if(mData.mTemplateChunks.get(tokenName).containsKey(path)){
                                    //for each token (with this name, in this file)
                                    int numNestedTokens = 0;
                                    for(int c=0;c<mData.mTemplateChunks.get(tokenName).get(path).size();c++){
                                        TemplateChunk chunkObj=mData.mTemplateChunks.get(tokenName).get(path).get(c);
                                        numNestedTokens+=chunkObj.mTokens.size();
                                    }
                                    //print the number of nested token 
                                    System.out.println("      "+nestedPrefix+"--> contains ("+numNestedTokens+") nested token(s)");
                                }
                            }
                        }
                    }
                }
            }
            //HANDLE TOKEN INPUT (THAT CANNOT CONTAIN NESTED TOKEN INPUT)
            //===========================================================
            //get all of the token input values from the user
            int count=uniqueTokenNames.size()+uniqueListTokenNames.size();
            listItemIndex=getAllTokenInput(nestedParentKey,nestedLevel,listItemIndex,uniqueTokenNames,uniqueListTokenNames,0,count,false);System.out.println("");
        }else{
            //no token values to input
            System.out.println(" ZERO unique token values to input: ");
        }
        return new int[]{levelIsComplete, listItemIndex};
    }
    //replace the aliases inside fileContent with their associated value (if the alias is inside fileContent)
    private String getReplacedAliases(String fileContent, HashMap<String, String> aliasValueLookup)
    {
        //if there are any aliases
        if (aliasValueLookup.size()>0){
            //if there is any file content
            if (fileContent.trim().length() > 0){
                //for each alias
                for (String aliasKey : aliasValueLookup.keySet()) {
                    //if the file content contains this alias
                    if (fileContent.contains(aliasKey)){
                        //replace this alias with the value inside file content
                        fileContent = fileContent.replace(aliasKey, aliasValueLookup.get(aliasKey));
                    }
                }
            }
        }
        return fileContent;
    }
    //determine if the default file name / path should be changed based on the given token
    private void setChangedFilenameForFile(String filePath, String[] tokenParts, String tokenValue, HashMap<String, String> aliasValueLookup){
        //if this file doesn't already have a designated changed name
        if (!mData.mChangedFileNames.containsKey(filePath)){
            //if there is a specified file name (other than the existing template file's name)
            if (!tokenValue.equals("") && !tokenValue.equals(".")){
                //replace any aliases with real values that may be inside the filename
                tokenValue = getReplacedAliases(tokenValue, aliasValueLookup);
                //set the new name of the file
                mData.mChangedFileNames.put(filePath, tokenValue);
            }else{
                //no specified file name...

                String tokenName = mData.getTokenPart("name", tokenParts);
                //if the file name was literally hard-coded (surrounded by "quotes")
                if (tokenName.indexOf("\"") == 0 || tokenName.indexOf("'") == 0){
                    //set the static filename value surrounded by "quotes"
                    tokenValue = tokenName;
                    //strip off starting quote
                    tokenValue = tokenValue.substring(1);
                    //strip off ending quote
                    tokenValue = tokenValue.substring(0, tokenValue.length() - 1);
                    //trim
                    tokenValue = tokenValue.trim();
                    //if the literal file name value is not blank
                    if (tokenValue.length() > 0){
                        //replace any aliases with real values that may be inside the filename
                        tokenValue = getReplacedAliases(tokenValue, aliasValueLookup);
                        //set the static name of the file
                        mData.mChangedFileNames.put(filePath, tokenValue);
                    }
                }
            }
        }
        //since this token specifies a filename, it may also specify the root directory for the file (if not, changedFileDir = "")...
        //if this file doesn't already have a designated changed sub directory
        if (!mData.mChangedFileDirs.containsKey(filePath)){
            //if there is a specified directory in the tokenParts
            String changedDir = mData.getTokenPart("dir", tokenParts);
            if (!changedDir.equals("") && !changedDir.equals(".")){
                //replace any aliases with real values that may be inside the directory
                changedDir = getReplacedAliases(changedDir, aliasValueLookup);
                //set the new sub directory of the file
                mData.mChangedFileDirs.put(filePath, changedDir);
            }
        }
    }
    //build out the template files
    private void setTokenValues(){setTokenValues(null);} 
    private void setTokenValues(ArrayList<String> inFileNameTokens){
        /*LOOP EACH FILE A SECOND TIME
        1) remove alias definitions from file content and determine what value each alias should be replaced with
            Replace substring tokens in mFileContentLookup
            with the actual values in mTokenInputValues
        2) replace non-aliased tokens with their actual values
        3) determine the file-path and file name that each file will have
        ============================
        IF inFileNameTokens is NOT null, then only replace the tokens that are named in this inFileNameTokens list
        */        
        
        //for each template file
        for (String filePath : mData.mFileTokensLookup.keySet()) {
            //get key values related to this file
            String fileContent=mData.mFileContentLookup.get(filePath);
            //if this file is NOT a non-text file, eg: an image
            ArrayList<String> tokens=mData.mFileTokensLookup.get(filePath);
            /*tokens:
                ../config.xml
                    <<var:l:your hi>> --> _filenames.xml
                    <<var:u:your something => [something]>>
                ../otherfile.txt
                    <<filename:n:my/path:"newname">>
                    <<filename:n:my/path:"overwrite">> --> _filenames.xml
                    <<var:u:another thing => [yep]>>
             */
            //GET FORMATTED VALUES FOR ALIASES AND REMOVE ALIAS DECLARATIONS FROM FILE CONTENTS
            //=================================================================================
            //if there are any aliased tokens in this file,
            //side-note: ONLY text-based files might have associated aliases. Non-text files, like images, will never have any items in mFileAliasesLookup ...
            HashMap<String, String> aliasValueLookup=new HashMap<String, String>();
            if(mData.mFileAliasesLookup.containsKey(filePath)){
                //for each alias declaration inside the file
                for (String aliasKey : mData.mFileAliasesLookup.get(filePath).keySet()) {
                    //if the alias doesn't already have an associated formatted value
                    if(!aliasValueLookup.containsKey(aliasKey)){
                        //REMOVE ALIAS DEFINITION FROM THE CONTENT
                        //get the token string for this alias
                        String tokenStr = mData.mFileAliasesLookup.get(filePath).get(aliasKey);
                        //remove the tokenStr (alias variable declaration) from fileContent
                        fileContent = fileContent.replace(tokenStr, "");
                        //STORE THE VALUE ASSIGNED TO THE ALIAS (THAT WILL REPLACE INSTANCES OF THE ALIAS THROUGHOUT THE FILE)
                        //split the token key parts up
                        String[] tokenParts = tokenStr.split(mStrMgr.mTokenSeparator);
                        //get the formatted token value
                        String tokenValue = mData.getFormattedTokenValue(tokenParts,inFileNameTokens);
                        //associate this value with this alias (only for this file)
                        aliasValueLookup.put(aliasKey, tokenValue);
                    }
                }
            }
            //SET THE OVERRIDING FILENAME FROM _filenames.xml, IF THIS FILE HAS SUCH AN OVERRIDE
            //==================================================================================
            //if this file has a filename defined in _filenames.xml
            boolean filenameInXml=false;
            if(mData.mFilenameXmlOverwriteLookup.containsKey(filePath)){
                String tokenStr=mData.mFilenameXmlOverwriteLookup.get(filePath);
                //split the token key parts up
                String[] tokenParts=tokenStr.split(mStrMgr.mTokenSeparator);
                //get the formatted token value
                String tokenValue=mData.getFormattedTokenValue(tokenParts,inFileNameTokens);
                //save the changed file name or directory, if either dir or name should be changed
                setChangedFilenameForFile(filePath, tokenParts, tokenValue, aliasValueLookup);
                filenameInXml=true;
            }
            //LOOP THROUGH ALL OF THE NON-ALIASED TOKENS IN THIS FILE
            //=======================================================
            //for each token in the file
            for(int t=0;t<tokens.size();t++){
                //FORMAT THE TOKEN VALUE DEPENDING ON IT'S PARAMETERS
                //get the token key, eg: <<type:casing:name>>
                String tokenStr=tokens.get(t);
                //split the token key parts up
                String[] tokenParts=tokenStr.split(mStrMgr.mTokenSeparator);
                //get the formatted token value
                String tokenValue=mData.getFormattedTokenValue(tokenParts,inFileNameTokens);
                //INSERT THE FORMATTED TOKEN VALUE INTO THE CONTENT (OR SET IT AS A SPECIAL VALUE, LIKE FILENAME, ETC...)
                //=======================================================================================================
                //get the token name
                String tokenName=mData.getTokenPart("name", tokenParts);
                //get the token type
                String type=mData.getTokenPart("type", tokenParts);
                switch (type){
                    case "var":
                        //replace the tokens with the actual values
                        fileContent=fileContent.replace(tokenStr, tokenValue);
                        break;
                    case "filename": 
                        //remove these tokens from the file content
                        fileContent=fileContent.replace(tokenStr, "");
                        //if the filename is NOT already defined in _filenames.xml
                        if(!filenameInXml){
                            //save the changed file name or directory, if either dir or name should be changed
                            setChangedFilenameForFile(filePath, tokenParts, tokenValue, aliasValueLookup);
                        }
                        break;
                    default:
                        break;
                }
            }
            //replace any aliases with real values that may be inside the fileContent
            fileContent=getReplacedAliases(fileContent, aliasValueLookup);
            //set the modified content into the lookup
            mData.mFileContentLookup.remove(filePath);
            mData.mFileContentLookup.put(filePath, fileContent);
        }
    }
    //write the output files
    private String writeOutputFiles(){return writeOutputFiles("build");} 
    private String writeOutputFiles(String writeType){
        //if exporting a completed project file-set
        String exportDir=""; String failedExportDir="";
        if(writeType.equals("export")){
            //CREATE A UNIQUE FAILED EXPORT FOLDER NAME, JUST IN CASE
            //get a root directory path to export to
            failedExportDir=mTargetDir + File.separator + "DELETE_THIS_FAILED_EXPORT";
            //if this export directory already exists
            if(new File(failedExportDir).exists()){
                int dirIndex = 2;
                //while this directory PLUS the index exists
                while(new File(failedExportDir + dirIndex).exists()){
                    //increase the index
                    dirIndex++;
                }
                //modify the export directory name so that it's unique
                failedExportDir+=dirIndex;
            }
            //REAL EXPORT DIRECTORY
            //get a root directory path to export to
            exportDir=mTargetDir + File.separator + "export";
            //if this export directory already exists
            if(new File(exportDir).exists()){
                int dirIndex = 2;
                //while this directory PLUS the index exists
                while(new File(exportDir + dirIndex).exists()){
                    //increase the index
                    dirIndex++;
                }
                //modify the export directory name so that it's unique
                exportDir+=dirIndex;
            }
            //create the export directory
            File exportDirFile=new File(exportDir);
            try{
                //try to create this directory
                exportDirFile.mkdir();
            }catch(SecurityException se){
                System.out.println("\n Uh oh... failed to create directory --> "+exportDir);
                se.printStackTrace();
            }
        }
        //LOOP EACH FILE A FINAL THIRD TIME
        //1) create a new file based on the corresponding template file (the new file will have real input values instead of tokens)
        //=================================
        //for each file to create
        int fileCount = 0; int skippedFileCount = 0; int errFileCount = 0;
        for (String filePath : mData.mFileContentLookup.keySet()) {
            String fileName = "";File templateFile=new File(filePath);
            //if this file doesn't start with _, eg: _filenames.xml
            if(!mFileMgr.isIgnoredFileOrFolder(templateFile)){
                //if changing the file name
                if (mData.mChangedFileNames.containsKey(filePath)){
                    //get just the file extension
                    String fileExt = templateFile.getName();
                    if (fileExt.indexOf(".") != -1){
                        //remove file name from the extension
                        fileExt = fileExt.substring(fileExt.lastIndexOf("."));
                        //add file extension to file name
                        fileName = mData.mChangedFileNames.get(filePath) + fileExt;
                    }else{fileExt="";}
                }
                else //use same filename as the original template file
                {
                    //get just the filename with no path
                    fileName = templateFile.getName();
                }
                //if changing the file directory (under the current project directory)
                String changedFileDir = "";
                if (mData.mChangedFileDirs.containsKey(filePath)){
                    changedFileDir = mData.mChangedFileDirs.get(filePath);
                    //make sure each directory exists... create them if they don't
                    changedFileDir=changedFileDir.replace(File.separator, "/");
                    String[] dirs = changedFileDir.split("/");
                    String currentDir = "";
                    //depending on the write type...
                    switch(writeType){
                        case "build": //if building a boiler-plate template files...
                            currentDir = mTargetDir + File.separator;
                        break;
                        case "export": //if exporting a completed project files...
                            currentDir = exportDir + File.separator;
                        break;
                    }
                    //MAKE SURE EACH OUTPUT DIRECTORY IS CREATED IF IT DOESN'T ALREADY EXIST
                    //for each sub directory (that may need to be created)
                    for (int d=0;d<dirs.length;d++){
                        //if this directory doesn't exist
                        currentDir += dirs[d].trim() + File.separator;
                        File dirFile=new File(currentDir);
                        if (!dirFile.exists()){
                            try{
                                //try to create this directory
                                dirFile.mkdir();
                            }catch(SecurityException se){
                                System.out.println("\n Uh oh... failed to create directory --> "+currentDir);
                                se.printStackTrace();
                                break;
                            }
                        }
                    }
                    //append the final \\ at the end of the directory path
                    changedFileDir += File.separator;
                }
                //set the ouput file path (relative to the current directory)
                String outputFilePath = File.separator + changedFileDir + fileName;
                //depending on the write type...
                switch(writeType){
                    case "build": //if building a boiler-plate template file...
                        //set the target directory as the root of the output file path
                        outputFilePath = mTargetDir + outputFilePath;
                        //get the new file content
                        String fileContent = mData.mFileContentLookup.get(filePath);
                        File newFile=new File(outputFilePath);
                        //if the new file doesn't already exist
                        if (!newFile.exists()){
                            //if the file content is NOT blank
                            fileContent = fileContent.trim();
                            if (fileContent.length() > 0){
                                //create the file with its content (maybe changed or maybe not changed and just copied over)
                                boolean success=false;
                                //if this is a NON-text file, eg and image
                                if(fileContent.equals(mStrMgr.mNonTextFileContent)){
                                    //copy the NON text file to the build location
                                    success=mFileMgr.copyFileTo(new File(mUseTemplatePath + File.separator + new File(filePath).getName()), newFile);
                                }else{
                                    //this IS a text-based file...
                                    //restore certain string contents
                                    fileContent = fileContent.replace(mStrMgr.mStartEscToken, mStrMgr.mStartToken);
                                    fileContent = fileContent.replace(mStrMgr.mEndEscToken, mStrMgr.mEndToken);
                                    //write the token-replaced file content
                                    success=mFileMgr.writeFile(newFile.getPath(), fileContent);
                                }
                                if(success){
                                    System.out.println(" FILE CREATED: \t" + "..."+newFile.getPath().substring(mTargetDir.length()+1));
                                    fileCount++;
                                }else{
                                    errFileCount++;
                                }
                            }
                            else
                            {
                                System.out.println(" FILE SKIP (BLANK): \t" + "..."+newFile.getPath().substring(mTargetDir.length()+1));
                                skippedFileCount++;
                            }
                        }
                        else
                        {
                            System.out.println(" FILE SKIP (ALREADY EXISTS): \t" + "..."+newFile.getPath().substring(mTargetDir.length()+1));
                            skippedFileCount++;
                        }
                    break;
                    case "export": //if exporting a completed project file...
                        //normalize file path separators
                        outputFilePath=outputFilePath.replace("\\", "/");
                        outputFilePath=outputFilePath.replace("///", "/");
                        outputFilePath=outputFilePath.replace("//", "/");
                        outputFilePath=outputFilePath.replace("/", File.separator);
                        //get the file to export
                        File exportFile=new File(mTargetDir + outputFilePath);
                        //if the new file already exists
                        if (exportFile.exists()){
                            //try to get the file content
                            String exportContent = ""; boolean errorReading=false;
                            exportContent = mFileMgr.readFile(exportFile.getPath());
                            if(exportContent==null){
                                errorReading=true;
                                errFileCount++;
                            }
                            //if the file could be read
                            if(!errorReading){
                                //if the file content is NOT blank
                                if (exportContent.length() > 0){
                                    //make a copy of the export file under the exportDir root
                                    boolean success=mFileMgr.copyFileTo(exportFile, new File(exportDir + outputFilePath));
                                    if(success){
                                        System.out.println(" FILE EXPORTED: \t" + "..."+(exportDir + outputFilePath).substring(mTargetDir.length()+1));
                                        fileCount++;
                                    }else{
                                        errFileCount++;
                                    }
                                }
                                else
                                {
                                    System.out.println(" FILE SKIP (BLANK): \t" + "..."+exportFile.getPath().substring(mTargetDir.length()+1));
                                    skippedFileCount++;
                                }
                            }
                        }
                        else
                        {
                            System.out.println(" FILE SKIP (DOES NOT EXIST): \t" + "..."+exportFile.getPath().substring(mTargetDir.length()+1));
                            skippedFileCount++;
                        }
                    break;
                }
            }
        }
        //FILE WRITING COMPLETE... PRINT STATUS MESSAGE
        System.out.println("\n -------------------------------------------------------");
        System.out.println(" Done.\n Created files: (" + fileCount + ") \n Skipped files: (" + skippedFileCount + ") \n Error files: (" + errFileCount + ") \n ");
        //ADDITIONAL INPUT FOR EXPORT OPERATIONS
        //if exported project files
        if(writeType.equals("export")){
            //if any files were successfully exported
            if(fileCount > 0){
                //get just the export folder name (without the full path)
                String exportFolder=exportDir;
                if(exportFolder.contains(File.separator)){
                    exportFolder=exportFolder.substring(exportFolder.lastIndexOf(File.separator)+File.separator.length());
                }
                //get the new export directory file name
                String newName = getInput("Rename the \"" + exportFolder + "\" root folder by typing a new name... \n OR hit [enter] to keep this name");
                //if a new folder name was given
                newName=newName.trim();
                if(newName.length()>0){
                    System.out.println(" Renaming: \"" + exportFolder + "\" --> \"" + newName + "\" \n");
                    //rename the export directory
                    File existingDir = new File(exportDir);
                    File renamedDir = new File(existingDir.getParent() + File.separator + newName);
                    //if this folder doesn't already exist
                    if(!renamedDir.exists()){
                        //rename
                        existingDir.renameTo(renamedDir);
                        exportDir=renamedDir.getPath();
                    }else{
                        //already exists message
                        System.out.println(" No. \"" + newName + "\" already exists. \n");
                    }
                }
            }else{
                //no files were successfully exported... 
                
                //remove the exportDir
                File emptyDir = new File(exportDir);
                //make sure the failed export folder path is still unique
                String indexStr="";int index=2;
                while(new File(failedExportDir+indexStr).exists()){
                    indexStr=index+"";
                    index++;
                }
                failedExportDir+=indexStr;
                //rename the export directory so that it's obvious that it's a failed export
                emptyDir.renameTo(new File(failedExportDir));
                //display failer message
                System.out.println(" No files were successfully exported.");
                System.out.println(" Note: This app renamed the failed export folder. You can manually delete this folder:");
                System.out.println(" " + failedExportDir + "\n\n");
                //clear the export directory path since the export failed
                exportDir="";
            }
        }
        return exportDir;
    }
}
