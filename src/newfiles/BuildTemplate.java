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
                userInputForTokens(atLeastOneToken, 0, mData.mUniqueTokenNames, mData.mUniqueListTokenNames, mData.mFileTokensLookup);
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
        return line;
    }
    //get the input from the user for all of the template tokens
    private void getAllTokenInput(int nestedLevel, ArrayList<String> uniqueTokenNames, int startIndex, int count, boolean isBack){
        /*ASSIGN REAL USER VALUES TO EACH UNIQUE TOKEN NAME
            1) mTokenInputValues
            load a HashMap: HashMap<[tokenName], [userInput]>

            HashMap values come from any file inside the template, including the special _filenames.xml file
                
                your name
                    gmilligan
                your test
                    I am testing
                your something
                    in the way she moves...
                your hi
                    howdy
         */
        String backTxt="<<";
        //if NOT moved back
        if(!isBack){
            //display direction on how to move back
            System.out.println("\n --------------------");
            System.out.println(" DEFINE VARIABLE VALUES");
            System.out.println(" Type \""+backTxt+"\" to back-track... \n");
        }
        //for each input value to enter
        String lastTokenName=null;
        for(int i=startIndex;i<=uniqueTokenNames.size();i++){
            String input="";
            //if NOT entered all of the values
            if(i<count){
                //if this is one of the NON-list-token values
                if(i<uniqueTokenNames.size()){
                    //if this token requires specific value options
                    ArrayList<String> inputOptions = new ArrayList<String>();
                    if(mData.mUniqueTokenNameOptions.containsKey(uniqueTokenNames.get(i))){
                        //get the allowed options
                        inputOptions = mData.mUniqueTokenNameOptions.get(uniqueTokenNames.get(i));
                    }
                    //if the user is allowed to enter any value
                    if(inputOptions.size()<1){
                        //get user input
                        input=getInput("" + (i+1) + "/" + count + ") Enter --> \"" +uniqueTokenNames.get(i) + "\"");
                    }else{
                        //there are specific option values that are required

                        //get user input
                        input=getInput("" + (i+1) + "/" + count + ") Select --> \"" +uniqueTokenNames.get(i) + "\"", inputOptions);
                    }
                }else{
                    //this is a list token value...
                    
                    //*** recursive call to:
                    //*** userInputForTokens(boolean atLeastOneToken, int nestedLevel, ArrayList<String> uniqueTokenNames, ArrayList<String> uniqueListTokenNames, HashMap<String, ArrayList<String>> fileTokensLookup)
                }
            }else{
                //entered all of the values
                System.out.println("");
                System.out.println(" Target root path --> " + mTargetDir+File.separator+"...");
                input=getInput("Ok, got it. Hit [enter] to build");
            }
            //if the input is the back text
            if(input.equals(backTxt)){
                //print the back message
                System.out.println("\n \tback... \n");
                //make sure the index won't go below zero when backtracking
                if(i==0){i++;}
                //if there was a previous item
                if(lastTokenName!=null){
                    //if the previous value was saved
                    if(mData.mTokenInputValues.containsKey(lastTokenName)){
                       //remove the saved value for the previous item 
                       mData.mTokenInputValues.remove(lastTokenName);
                    }
                }
                //recursive move back
                getAllTokenInput(nestedLevel,uniqueTokenNames,i-1,count,true);
                break;
            }else{
                //if NO already saved all input values
                if(i<uniqueTokenNames.size()){
                    //add this input value to the list
                    mData.mTokenInputValues.put(uniqueTokenNames.get(i), input);
                    //record the last token name to be assigned a value
                    lastTokenName=uniqueTokenNames.get(i);
                }
            }
        }
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
            getAllTokenInput(0,inFileNameTokens,0,inFileNameTokens.size(),false);System.out.println("");
        }
        return inFileNameTokens;
    }
    //accept user input for each of the unique tokens (inside one nested level)
    private void userInputForTokens(boolean atLeastOneToken, int nestedLevel, ArrayList<String> uniqueTokenNames, ArrayList<String> uniqueListTokenNames, HashMap<String, ArrayList<String>> fileTokensLookup){
        //if there is at least one token
        if(uniqueTokenNames.size()+uniqueListTokenNames.size()>0){
            //DISPLAY THE LIST OF NON-NESTED, UNIQUE, TOKEN NAMES
            //===================================================
            //if more than one token value to input
            if(uniqueTokenNames.size()+uniqueListTokenNames.size()>1){
                System.out.println(" "+(uniqueTokenNames.size()+uniqueListTokenNames.size())+" unique token values to input: ");
            }else{
               //only one token value to input 
               System.out.println(" only ONE token value to input: ");
            }
            //for each token value to input
            System.out.print(" ");
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
            System.out.println("\n");
            //DISPLAY LIST OF TOKENS? (FOR TEMPLATE-CODE DEBUGGING)
            //====================================================
            //get the list tokens/continue choice 
            String lsOrContinue=getInput("\"ls\"=list files/tokens, [enter]=continue");
            //if the user chose to view the file / token listing
            if(lsOrContinue.toLowerCase().equals("ls")){
                //for each file (that contains at least one token)
                for (String path : fileTokensLookup.keySet()) {
                    File file=new File(path);
                    //print the file name and number of tokens
                    ArrayList<String> fileTokens = fileTokensLookup.get(path);
                    System.out.println("\n "+file.getName()+" >> "+fileTokens.size()+" token(s) \n");
                    //for each token in this file
                    for(int t=0;t<fileTokens.size();t++){
                        //print the token text
                        String tokenStr=fileTokens.get(t).trim();
                        System.out.println("   "+tokenStr);
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
                                    System.out.println("      --> contains ("+numNestedTokens+") nested token(s)");
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
            getAllTokenInput(nestedLevel,uniqueTokenNames,0,count,false);System.out.println("");
            //HANDLE THE LIST TOKENS INPUT
            //============================
            //***************** do I need to do this here?
            //if there are any list tokens 
            if(uniqueListTokenNames.size()>0){
                int inputNum=uniqueTokenNames.size();
                //for each unique list name
                for(int l=0;l<uniqueListTokenNames.size();l++){
                    //if this list token is in the chunks data
                    String tName=uniqueListTokenNames.get(l);
                    System.out.println("" + inputNum + "/" + count + ") List --> \"" + tName + "\"");
                    /*if(mData.mTemplateChunks.containsKey(tName)){
                        //HashMap<[uniqueTokenName], ArrayList<TemplateChunk>>
                        HashMap<String, ArrayList<TemplateChunk>> tokenNameChunks = new HashMap<String, ArrayList<TemplateChunk>>();
                        //for each file where this same list appears ***
                        for (String filePath : mData.mTemplateChunks.get(tName).keySet()) {
                            //get all of the template chunks, with this name, in this file
                            ArrayList<TemplateChunk> templateChunks = mData.mTemplateChunks.get(tName).get(filePath);
                            //***
                            //get all of the <<list>> token type values
                            //+++getAllNestedTokenChunksInput(templateChunks);
                        }
                    }*/
                }
            }
        }else{
            //no token values to input
            System.out.println(" ZERO unique token values to input: ");
        }
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
