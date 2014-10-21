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
                userInputForTokens(atLeastOneToken, mData.mUniqueTokenNames, mData.mUniqueListTokenNames, mData.mFileTokensLookup);
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
                System.out.print("\n    Choose option # ");
                System.out.println("\n");
                //for each option
                for(int i=0;i<inputOptions.size();i++){
                    System.out.println("    " + i + "  \"" + inputOptions.get(i) + "\"");
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
                    System.out.println("    --> \"" + line + "\" \n");
                }
            }
        }
        return line;
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
            System.out.print("   ");
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
            userInputForTokens(atLeastOneToken, inFileNameTokens, null, null);
            System.out.println("");
        }
        return inFileNameTokens;
    }
    //ENTRY POINT TO: accept user input for each of the unique tokens
    private void userInputForTokens(boolean atLeastOneToken, ArrayList<String> uniqueTokenNames, ArrayList<String> uniqueListTokenNames, HashMap<String, ArrayList<String>> fileTokensLookup){
        //uniqueTokenNames ... ArrayList<[tokenName]>
        //uniqueListTokenNames ... ArrayList<[tokenName]>
        //fileTokensLookup ... HashMap<[filePath], ArrayList<[tokenItemText]>>
        
        //if there is at least one token
        if(atLeastOneToken){
            if(uniqueListTokenNames==null){uniqueListTokenNames=new ArrayList<String>();}
            //if there is at least one token
            if(uniqueTokenNames.size()+uniqueListTokenNames.size()>0){
                //if the fileTokensLookup was provided
                if(fileTokensLookup!=null){
                    //SHOW THE LIST OF TOKENS TO INPUT IN THIS LEVEL
                    //==============================================
                    showTokensInLevel("", uniqueTokenNames, uniqueListTokenNames, fileTokensLookup);
                }
                //ENTRY POINT: GET ALL OF THE INPUT VALUES FOR TOP LEVEL (CAN CALL THIS RECURSIVELY FOR SUB-LEVELS)
                //=================================================================================================
                inputsForEntireLevel(uniqueTokenNames, uniqueListTokenNames, null);
            }else{
                //no token values to input
                System.out.println(" ZERO unique token values to input: ");
            }
        }
    }
    //get the input for ONE level of values (CAN CALL THIS RECURSIVELY FOR SUB-LEVELS)
    private HashMap<String, String> inputsForEntireLevel(ArrayList<String> uniqueTokenNames, ArrayList<String> uniqueListTokenNames, HashMap<String, String> nestedData){
        if(uniqueListTokenNames==null){uniqueListTokenNames=new ArrayList<String>();}
        //HANDLE NESTED LEVEL VALUES
        //==========================
        //init the nestedLevel and nestedPrefix default values
        int nestedLevel=0;
        int listItemIndex=-1;
        int startFieldIndex=0;
        String nestedPrefix="";
        String parentType="";
        String nestedParentKey="";
        //if this is a nested level
        if(nestedData!=null){
            //get the parentType
            parentType=nestedData.get("parentType");
            //if this parent level is a list token
            if(parentType.equals("list")){
                //get the current list item index
                String listItemIndexStr=nestedData.get("listItemIndex");
                listItemIndex=Integer.parseInt(listItemIndexStr);
                //get the start input-field index (which input field to start at in this level?)
                String startFieldIndexStr=nestedData.get("startFieldIndex");
                startFieldIndex=Integer.parseInt(startFieldIndexStr);
            }
            //get the nestedLevel
            String nestedLevelStr=nestedData.get("nestedLevel");
            nestedLevel=Integer.parseInt(nestedLevelStr);
            //get the nestedParentKey
            nestedParentKey=nestedData.get("nestedParentKey");
            //if this is NOT the first un-nested level
            if(nestedLevel > 0){
                //build the nested prefix
                for(int n=0;n<nestedLevel;n++){
                    if(n<5){
                        nestedPrefix+=" ";
                    }else{
                        break;
                    }
                }
                nestedPrefix+="^"+nestedLevel+".  "; 
            }
        }
        String listItemIndexStr="";
        if(listItemIndex>-1){listItemIndexStr=" #"+(listItemIndex+1);}
        //CONTINUE FOR ANY LEVEL, NESTED OR NOT-NESTED
        //============================================
        //get the total count of input items (in this current level)
        int itemCount=uniqueTokenNames.size();
        int listCount=uniqueListTokenNames.size();
        int totalCount=itemCount+listCount;
        //back and stop key-input
        final String backTxt=mStrMgr.mStartToken;
        final String stopTxt=mStrMgr.mEndToken;
        //ENTER ALL OF THE UNIQUE TOKEN NAMES IN THIS LEVEL
        //=================================================
        //keep a list of input value keys, in this level (if back undo is needed)
        ArrayList<String> saveInputKeys = new ArrayList<String>();
        //init user input value
        String input="";
        //if there are any tokens
        if(totalCount>0){
            //for each unique token name
            for(int t=startFieldIndex;t<uniqueTokenNames.size();t++){
                String tokenName=uniqueTokenNames.get(t);
                //GET POSSIBLE VALUE OPTIONS FOR ONE VALUE (IF LIMITED OPTIONS)
                //=============================================================
                //if this token requires specific value options
                ArrayList<String> inputOptions = new ArrayList<String>();
                if(mData.mUniqueTokenNameOptions.containsKey(tokenName)){
                    //get the allowed options
                    inputOptions = mData.mUniqueTokenNameOptions.get(tokenName);
                }
                //GET THE TALLY LABEL
                //===================
                String tallyLabel="";
                if(totalCount>1){
                    tallyLabel=(t+1)+"/"+totalCount+") ";
                }
                //GET USER INPUT FOR ONE VALUE
                //============================
                //if the user is allowed to enter any value
                if(inputOptions.size()<1){
                    //get user input
                    input=getInput(nestedPrefix+tallyLabel+"Enter --> \""+tokenName+"\""+listItemIndexStr);
                }else{
                    //there are specific option values that are required

                    //get user input
                    input=getInput(nestedPrefix+tallyLabel+"Select --> \""+tokenName+"\""+listItemIndexStr,inputOptions);
                }
                //DECIDE WHAT TO DO BASED ON USER INPUT FOR ONE VALUE
                //===================================================
                //if use wanted to move back
                if(input.equals(backTxt)){
                    String prevInputFieldMsg="\n   BACK (undo) \"";
                    //MOVE BACK
                    //=========
                    switch(parentType){
                        case "list": //moving back inside a list
                            //MOVE BACK INSIDE LIST
                            //=====================
                             //if NOT the first field in this item
                            if(t>0){
                                //PREVIOUS INPUT-FIELD
                                //====================
                                //get the input-key for the last saved value
                                int lastInputKeyIndex=saveInputKeys.size()-1;
                                String lastInputKey=saveInputKeys.get(lastInputKeyIndex);
                                //get the previous entered value
                                String prevValue=mData.mTokenInputValues.get(lastInputKey);
                                //remove the last saved value 
                                mData.mTokenInputValues.remove(lastInputKey);
                                saveInputKeys.remove(lastInputKeyIndex);
                                //print end of the list item
                                System.out.println(prevInputFieldMsg+prevValue+"\" \n");
                                //previous loop iteration
                                t--;t--;
                            }else{
                                //first field in this item...

                                //if NOT also the first item in the list
                                if(listItemIndex>0){
                                    //if this item doesn't contain nested list(s) (can't undo nested list items)
                                    if(uniqueListTokenNames.size()<1){
                                        //PREVIOUS ITEM
                                        //=============
                                        //go back to previous nestedData state (for last input-field in the previous list-item)
                                        int lastTokenNameIndex=uniqueTokenNames.size()-1;
                                        startFieldIndex=lastTokenNameIndex; //next time around, start at the last input-value inside the item
                                        //figure out the input value key to remove
                                        String lastInputKey=nestedParentKey+mStrMgr.mAliasSetter+uniqueTokenNames.get(lastTokenNameIndex)+mStrMgr.mTokenSeparator+(listItemIndex-1);
                                        //previous list item index (-2 for the auto-increment)
                                        listItemIndex--;listItemIndex--; 
                                        //get the previous entered value
                                        String prevValue=mData.mTokenInputValues.get(lastInputKey);
                                        //remove the last saved value 
                                        mData.mTokenInputValues.remove(lastInputKey);
                                        //back message
                                        System.out.println(prevInputFieldMsg+prevValue+"\" \n");
                                        //since this code is currently inside the recursive while loop, the undo will happen on the next while loop iteration...
                                        t=lastTokenNameIndex; //force this token-input, for loop to end
                                    }else{
                                        //CAN'T GO BACK
                                        //=============
                                        //would have to undo a nested list to get back to the previous input value in this level...

                                        System.out.println("\n   CANNOT undo previous \""+uniqueListTokenNames.get(uniqueListTokenNames.size()-1)+"\" nested list... \n");
                                        //redo loop iteration
                                        t--;
                                    }
                                }else{
                                    //CAN'T GO BACK
                                    //=============
                                    //first field in the first list-item...

                                    System.out.println("\n   CANNOT go back; already at first input-field, in first list-item... \n");
                                    //redo loop iteration
                                    t--;
                                }
                            }
                            break;
                        default: //not inside a list
                            //MOVE BACK NON-LIST
                            //==================
                            //if NOT the first field in this level
                            if(t>0){
                                //PREVIOUS INPUT-FIELD
                                //====================
                                //get the input-key for the last saved value
                                int lastInputKeyIndex=saveInputKeys.size()-1;
                                String lastInputKey=saveInputKeys.get(lastInputKeyIndex);
                                //get the previous entered value
                                String prevValue=mData.mTokenInputValues.get(lastInputKey);
                                //remove the last saved value 
                                mData.mTokenInputValues.remove(lastInputKey);
                                saveInputKeys.remove(lastInputKeyIndex);
                                //print end of the list item
                                System.out.println(prevInputFieldMsg+prevValue+"\" \n");
                                //previous loop iteration
                                t--;t--;
                            }else{
                                //CAN'T GO BACK
                                //=============
                                //already at first field in the level, can't go back...

                                //print end of the list item
                                System.out.println("\n   CANNOT go back; already at first input-field at this level... \n");
                                //redo loop iteration
                                t--;
                            }
                            break;
                    }
                }else if(input.equals(stopTxt)){
                    //STOP LIST ENTRY AT THIS LEVEL
                    //=============================
                    switch(parentType){
                        case "list": //stopping list entry
                            //if this item is partially complete with some input-values, but not all of them
                            if(t>0){
                                //for each saved input-value in this item
                                for(int i=0;i<saveInputKeys.size();i++){
                                    //remove the input-value since the user is cancelling this item
                                    String inputKeyToRemove=saveInputKeys.get(i);
                                    mData.mTokenInputValues.remove(inputKeyToRemove);
                                }
                            }
                            //force the list entry to end
                            t=uniqueTokenNames.size()-1;
                            break;
                        default: //not inside a list
                            //print end of the list item
                            System.out.println("\n   CANNOT use \""+stopTxt+"\" here ... \n");
                            //redo loop iteration
                            t--;
                            break;
                    }
                }else{
                    //SAVE ONE INPUT VALUE
                    //====================
                    //if there is a parent above this token
                    String saveInputKey=nestedParentKey;
                    if(saveInputKey.length()>0){
                        //add "=>" separator
                        saveInputKey+=mStrMgr.mAliasSetter;
                        //add this token name to the nestedList
                        saveInputKey+=tokenName;
                        //if this is a list item
                        if(listItemIndex>-1){
                            //add the index to the key
                            saveInputKey+=mStrMgr.mTokenSeparator+listItemIndex;
                        }
                    }else{
                        //NOT a nested token value...

                        //add this token name to the nestedList
                        saveInputKey+=tokenName;
                    }
                    //System.out.println("\n "+nestedPrefix+"SAVED ["+saveInputKey+"] \n"); //---
                    //save this input value with the key
                    mData.mTokenInputValues.put(saveInputKey, input);
                    //add this save input key to the list (for back undo, if needed)
                    saveInputKeys.add(saveInputKey);
                }
            }
            //FOR EACH LIST TO START (AT THIS LEVEL)
            //======================================
            //if NOT stopping list entry
            if(!input.equals(stopTxt)){
                //for each unique LIST token name
                for(int t=0;t<uniqueListTokenNames.size();t++){
                    String listTokenName=uniqueListTokenNames.get(t);
                    //GET THE TALLY LABEL
                    //===================
                    String tallyLabel="";
                    if(totalCount>1){
                        tallyLabel=(itemCount+t+1)+"/"+totalCount+") ";
                    }
                    //SHOW THE START OF THIS LIST
                    //===========================
                    System.out.println(" "+nestedPrefix+tallyLabel+"List --> \""+listTokenName+"\""+listItemIndexStr);
                    //if this is the first level
                    if(nestedLevel==0){
                        //if this is the first list token name, in the first level
                        if(t==0){
                            //show the directions to either quit a list or go back 
                            System.out.println("\n LIST ENTRY: ");
                            System.out.println(" "+nestedPrefix+"\""+backTxt+"\" --> Go back (when conditions allow)");
                            System.out.println(" "+nestedPrefix+"\""+stopTxt+"\" --> Finish complete list / cancel current item \n");
                        }
                    }
                    //START NEW LIST: INIT THE NESTED DATA FOR THIS LIST
                    //==================================================
                    HashMap<String, String> listNestedData = new HashMap<String, String>();
                    listNestedData.put("endList", "false");
                    listNestedData.put("parentType", "list");
                    listNestedData.put("listItemIndex", "0"); //starting a new list of items
                    listNestedData.put("startFieldIndex", "0"); //what input-field (inside the item) index to start? (may not be zero, if moving back to previous item)
                    listNestedData.put("nestedLevel", (nestedLevel+1)+""); //going down to deeper level
                    String listNestedKey=nestedParentKey; //add previous parent key
                    if(listNestedKey.length()>0){listNestedKey+=mStrMgr.mAliasSetter;} //add "=>" separator
                    listNestedKey+=listTokenName; //add this list's name to the listNestedKey
                    //if this is a repeatable list item
                    if(listItemIndex>-1){
                        //add the list item index to this key
                        listNestedKey+=mStrMgr.mTokenSeparator+listItemIndex; //eg: "name:0=>sub-name:1=>another-name:3"
                    }
                    listNestedData.put("nestedParentKey", listNestedKey);
                    //GET listNestedKey WITHOUT INDEXES IN IT 
                    //=======================================
                    String listNestedKeyNoIndexes=mData.getNestedKeyNoIndexes(listNestedKey); //eg: "name=>sub-name=>another-name"
                    //GET THE TOKENS THAT ARE REPEATED FOR EVERY LIST ITEM (IN THIS LIST)
                    //===================================================================
                    //+++HashMap<String, ArrayList<String>> nestedFileTokensLookup=mData.mNestedFileTokensLookup.get(listNestedKeyNoIndexes);
                    //1) try to get the nested non-list tokens, under this parent list token
                    ArrayList<String> listItemUniqueTokenNames=null;
                    //if this list contains any nested tokens
                    if(mData.mNestedUniqueTokenNames!=null){
                        if(mData.mNestedUniqueTokenNames.containsKey(listNestedKeyNoIndexes)){
                            //get the NON-list tokens nested under this list token
                            listItemUniqueTokenNames=mData.mNestedUniqueTokenNames.get(listNestedKeyNoIndexes);
                        }else{listItemUniqueTokenNames=new ArrayList<String>();}
                    }else{listItemUniqueTokenNames=new ArrayList<String>();}
                    //2) try to get the nested token lists under this parent list token
                    ArrayList<String> listItemUniqueListTokenNames=null;
                    //if there are any list tokens nested under a different token parent
                    if(mData.mNestedUniqueListTokenNames!=null){
                        if(mData.mNestedUniqueListTokenNames.containsKey(listNestedKeyNoIndexes)){
                            listItemUniqueListTokenNames=mData.mNestedUniqueListTokenNames.get(listNestedKeyNoIndexes);
                        }else{listItemUniqueListTokenNames=new ArrayList<String>();}
                    }else{listItemUniqueListTokenNames=new ArrayList<String>();}
                    //WHILE THE USER WANTS TO CONTINUE ENTERING LIST ITEMS...
                    //=======================================================
                    while(listNestedData.get("endList").equals("false")){
                        //GET ONE LIST ITEM
                        //=================
                        listNestedData=inputsForEntireLevel(listItemUniqueTokenNames, listItemUniqueListTokenNames, listNestedData);
                    }
                }
            }
        }else{
            //no tokens at this current level...
            
            //if this level is nested under a list token
            if(parentType.equals("list")){
                //ASK THE USER HOW MANY TIMES THIS TEMPLATE CHUNK SOULD BE REPEATED (AS LIST ITEM(S))
                //===================================================================================
                //get a number from the user (for the number of times to repeat this template chunk)
                boolean invalidCount=true;
                while(invalidCount){
                    int inputCount=-1;
                    String itemCountInput=getInput(nestedPrefix+"Enter --> # of \""+nestedParentKey+"\" items");
                    try{
                        //try to parse the input into an integer count value
                        inputCount=Integer.parseInt(itemCountInput);
                        //if the count is zero or greater
                        if(inputCount>=0){
                            //if the count is NOT too high
                            int countLimit=99999;
                            if(inputCount<=countLimit){
                                invalidCount=false;
                                input=inputCount+"";
                            }else{
                                System.out.println(" "+nestedPrefix+"\""+inputCount+"\" is too high; cannot be greater than "+countLimit+". Try again...");
                            }
                        }else{
                            System.out.println(" "+nestedPrefix+"\""+inputCount+"\" is too low; cannot be negative. Try again...");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(" "+nestedPrefix+"\""+itemCountInput+"\" is not a valid integer. Try again...");
                    }
                }
                //SAVE THE ITEM COUNT FOR THIS TEMPLATE CHUNK
                //===========================================
                mData.mTokenInputValues.put(nestedParentKey+mStrMgr.mAliasSetter+mStrMgr.mItemCountName, input);
            }
        }
        //IF THIS IS A NESTED LEVEL (HANDLE FINISHING NESTED LEVEL)
        //=========================================================
        if(nestedData!=null){
            //IF THIS IS A LIST ITEM
            //======================
            switch(nestedData.get("parentType")){
                case "list":
                    //if this list's items contain at least one token
                    if(totalCount>0){
                        //if NOT stopping list-item entry
                        if(!input.equals(stopTxt)){
                            //next list item index
                            nestedData.remove("listItemIndex");
                            nestedData.put("listItemIndex", (listItemIndex+1)+"");
                            //if the user chose to go back
                            if(input.equals(backTxt)){
                                //set the input-value index to go back to (in case going back to previous item)
                                nestedData.remove("startFieldIndex");
                                nestedData.put("startFieldIndex", startFieldIndex+"");
                            }else{
                                //set the input-value index to go back to (in case went back to previous item)
                                nestedData.remove("startFieldIndex");
                                nestedData.put("startFieldIndex", "0");
                            }
                            //print end of the list item
                            System.out.println(" "+nestedPrefix+"... ");
                        }else{
                            //stopping list-item entry...
                            nestedData.remove("endList");
                            nestedData.put("endList", "true");
                            //end message
                            System.out.println("\n  ...end \""+nestedParentKey+"\" \n  with ("+listItemIndex+") item(s) ... \n");
                        }
                    }else{
                        //this list doesn't contain any nested tokens... 
                        //so the number of repeated template chunks is determined by an input number from the user...
                        
                        //stopping list-item entry...
                        nestedData.remove("endList");
                        nestedData.put("endList", "true");
                        //end message
                        System.out.println("\n  ...end \""+nestedParentKey+"\" \n  with ("+input+") item(s) ... \n");
                    }
                    break;
            }
        }else{
            //NOT A NESTED LEVEL (HANDLE FINISHING INPUT ENTRY)
            //=================================================
            //wrap-up message
            String targetBuildMsg = "\n";
            targetBuildMsg+=" Target root path --> " + mTargetDir+File.separator+"...\n";
            String okGotItMsg="Ok, got it. Hit [enter] to build";
            //entered all of the values
            System.out.print(targetBuildMsg);
            input=getInput(okGotItMsg);
        }
        //return data about the nested this level (if this level is nested under a parent)
        return nestedData;
    }
    //show the tokens at this level and provide option to list them
    private void showTokensInLevel(String nestedPrefix, ArrayList<String> uniqueTokenNames, ArrayList<String> uniqueListTokenNames, HashMap<String, ArrayList<String>> fileTokensLookup){
        //uniqueTokenNames ... ArrayList<[tokenName]>
        //uniqueListTokenNames ... ArrayList<[tokenName]>
        //fileTokensLookup ... HashMap<[filePath], ArrayList<[tokenItemText]>>
        
        //SHOW THE LIST OF TOKENS TO INPUT IN THIS LEVEL
        //==============================================
        //if more than one token value to input
        if(uniqueTokenNames.size()+uniqueListTokenNames.size()>1){
            System.out.println(" "+nestedPrefix+(uniqueTokenNames.size()+uniqueListTokenNames.size())+" unique token values to input: ");
        }else{
           //only one token value to input 
           System.out.println(" "+nestedPrefix+"only ONE token value to input: ");
        }
        //for each token value to input
        System.out.print(" "+nestedPrefix+"  ");
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
        //GIVE USER THE OPTION TO VIEW THE TOKEN STRINGS IN THIS LEVEL
        //============================================================
        //if NOT in a nested level
        if(nestedPrefix.length()<1){
            System.out.println("\n");
            //get the list tokens/continue choice 
            String lsOrContinue=getInput(nestedPrefix+"\"ls\"=list files/tokens, [enter]=continue");
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
                    }
                }
            }
            System.out.println("");
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
            //get content in this file
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
            //REPLACE LIST-TOKEN CHUNK PLACEHOLDERS
            //=====================================
            //if this file's content contains a list chunk placeholder
            if(fileContent.contains(mStrMgr.mStartToken+mStrMgr.mPlaceholderChunkName+mStrMgr.mTokenSeparator+"list")){
                //if there are any list token chunks (there should be since the file contains a list placeholder)
                if(mData.mUniqueListTokenNames.size()>0){
                    //for each unique list token (at this first level)
                    for(int lt=0;lt<mData.mUniqueListTokenNames.size();lt++){
                        //if this list token is stored as a template chunk (it should be)
                        String listTokenName=mData.mUniqueListTokenNames.get(lt);
                        if(mData.mTemplateChunks.containsKey(listTokenName)){
                            //if this list token name is used in THIS file
                            if(mData.mTemplateChunks.get(listTokenName).containsKey(filePath)){
                                //get the list of list tokens (inside this file)
                                ArrayList<TemplateChunk> chunkObjs=mData.mTemplateChunks.get(listTokenName).get(filePath);
                                //for each list chunk inside this file
                                for(int c=0;c<chunkObjs.size();c++){
                                    //set the real token chunk that will replace the placeholder in the file content
                                    TemplateChunk chunkObj=chunkObjs.get(c);
                                    fileContent=chunkObj.setTokenValues(chunkObj.getTokenName(),fileContent,mData); 
                                }
                            }
                        }
                    }
                }
            }
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
