/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package newfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
public class TemplateData {
    //list fields 
    public static HashMap<String, String> mFileContentLookup; //HashMap<[filePath], [fileContent]>
    public static HashMap<String, ArrayList<String>> mFileTokensLookup; //HashMap<[filePath], ArrayList<[tokenItemText]>>
    public static HashMap<String, String> mFilenameXmlOverwriteLookup; //HashMap<[filePath], [filenameTokenTxt from _filename.xml]>>
    public static HashMap<String, HashMap<String, String>> mFileAliasesLookup; //HashMap<[filePath], HashMap<[tokenAlias], [tokenStr]>>
    public static ArrayList<String> mUniqueTokenNames; //ArrayList<[tokenName]> each token name only appears once
    //chunk list fields
    public static ArrayList<String> mUniqueListTokenNames; //ArrayList<[tokenName]> each <<list>> token name only appears once
    public static HashMap<String, HashMap<String, ArrayList<TemplateChunk>>> mTemplateChunks; //HashMap<[tokenName], HashMap<[filePath], ArrayList<TemplateChunk>>>
    //nested list fields... not [nestedKey] values are in the form: "token name=>nested token name=>another nested name"
    public static HashMap<String, ArrayList<String>> mNestedUniqueTokenNames; //HashMap<[nestedKey], ArrayList<[tokenName]>> each token name only appears once
    public static HashMap<String, ArrayList<String>> mNestedUniqueListTokenNames; //HashMap<[nestedKey], ArrayList<[tokenName]>> each <<list>> token name only appears once
    public static HashMap<String, HashMap<String, ArrayList<String>>> mNestedFileTokensLookup; //HashMap<[nestedKey], HashMap<[filePath], ArrayList<[tokenItemText]>>>
    
    public static HashMap<String, ArrayList<String>> mUniqueTokenNameOptions; //HashMap<[tokenName], ArrayList<[possible-input-value-options]>> token name is NOT the nested name, it's just the name with no index
    public static HashMap<String, String> mTokenInputValues; //HashMap<[tokenName], [inputValue]> [tokenName] will be the nested key for nested token values
    public static HashMap<String, String> mChangedFileNames; //HashMap<[filePath], [changedFileName]>
    public static HashMap<String, String> mChangedFileDirs; //HashMap<[filePath], [changedFileDirectories]>
    
    private static ArrayList<File> mIncludeFiles; //Files objects to include into the build 
    private static String mUseTemplatePath; //the path to the current using template
    //objects
    private static StrMgr mStrMgr;
    private static FileMgr mFileMgr;
    
    //constructor
    public TemplateData(){
        mStrMgr=new StrMgr();
        mFileMgr=new FileMgr();
        mIncludeFiles=null;
    }
    //method to use a given list of files
    public void useFiles(String useTemplatePath, ArrayList<File> includeFiles){
        mIncludeFiles=includeFiles;
        mUseTemplatePath=useTemplatePath;
    }
    public void linkOptionsToUniqueTokenName(String tokenName, String tokenStr){
        //if there are defined input value options for this token
        String tOptionsStr = getTokenPart("options", tokenStr);
        if(tOptionsStr.length()>0){
            //if this token name doesn't already have any associated options
            if(!mUniqueTokenNameOptions.containsKey(tokenName)){
                //create the token name as a key in this HashMap
                ArrayList<String> options = new ArrayList<String>();
                mUniqueTokenNameOptions.put(tokenName, options);
            }
            //for each option
            String[] tOptionsArray = tOptionsStr.split("\\|");
            for(int a=0;a<tOptionsArray.length;a++){
                //if this option isn't already associated with this token name
                String opt=tOptionsArray[a];
                if(!mUniqueTokenNameOptions.get(tokenName).contains(opt)){
                    //add the association between this token name and value option
                    mUniqueTokenNameOptions.get(tokenName).add(opt);
                }
            }
        }
    }
    //load the template content for one nested section inside one file
    public boolean loadContentData(String filePath, String contents, File fNamesXmlFile, HashMap<String, String> filenamesFromXml, ArrayList<String> filenameTokens){
        boolean atLeastOneToken = false;
        //if this file has a <filename> in the _filenames.xml file
        String overwriteFilename="";
        if(filenamesFromXml.containsKey(filePath)){
            //get the filename token string from inside the <filename> xml element 
            //(this string is formatted just like a normal token, eg: <<filename:l:path:name>>)
            String filenameXmlStr=filenamesFromXml.get(filePath);
            //if the filename token string is NOT blank
            if(filenameXmlStr.length()>0){
                //any filename token in this file will be overwritten by the filename definition in _filenames.xml
                overwriteFilename=filenameXmlStr;
            }
        }
        //escape certain string contents 
        contents = contents.replace("\\"+mStrMgr.mStartToken, mStrMgr.mStartEscToken);
        contents = contents.replace("\\"+mStrMgr.mEndToken, mStrMgr.mEndEscToken);
        //GET A LIST OF TOKENS (THAT CAN CONTAIN NESTED TOKENS) THAT ARE INSIDE THIS FILE
        //===============================================================================
        //if this is NOT _filenames.xml
        if(!mFileMgr.isIgnoredFileOrFolder(filePath)){
            //get all of the tokens, eg: <<list>> which may contain nested content. tokenChunks will contain the complete token-chunks
            ArrayList<String> tokenChunks=getTokenChunksFromContent(contents);
            //if there were any token "chunks"
            if(tokenChunks.size()>0){ 
                //init the list of token definitions for this file
                ArrayList<String> chunkTokenStrs = new ArrayList<String>();
                //for each token chunk
                for(int c=0;c<tokenChunks.size();c++){
                    //get the chunk
                    String chunkContents=tokenChunks.get(c);
                    //load the template chunk data (including recursively getting nested chunks inside this chunk)
                    TemplateChunk chunkObj=new TemplateChunk(this, "", filePath, c + "", chunkContents);
                    //get the token values
                    String cName=chunkObj.getTokenName();
                    String cType=chunkObj.getTokenType();
                    //ADD TO mTemplateChunks
                    //if this chunk name isn't already in the hash map
                    if(!mTemplateChunks.containsKey(cName)){
                        //init this chunk's token name
                        HashMap<String, ArrayList<TemplateChunk>> fileChunks = new HashMap<String, ArrayList<TemplateChunk>>();
                        mTemplateChunks.put(cName, fileChunks);
                    }
                    //if this chunk's file path isn't already the hash map
                    if(!mTemplateChunks.get(cName).containsKey(filePath)){
                        //init this chunk's file path for this token name
                        ArrayList<TemplateChunk> fileChunks = new ArrayList<TemplateChunk>();
                        mTemplateChunks.get(cName).put(filePath, fileChunks);
                    }
                    //add this chunk object to mTemplateChunks
                    mTemplateChunks.get(cName).get(filePath).add(chunkObj);
                    //ADD TO UNIQUE TOKEN NAMES
                    //add the token definition, for this chunk, to the list of token definitions
                    chunkTokenStrs.add(chunkObj.getTokenStr());
                    //depending on the token type
                    switch(cType){
                        case "list": 
                            //if this token name isn't already in the list
                            if (!mUniqueListTokenNames.contains(cName)){
                                //add the unique token name, if not already in the list
                                mUniqueListTokenNames.add(cName); 
                                atLeastOneToken = true;
                            }
                            break;
                    }
                    //replace the nested chunk with a placeholder
                    contents=contents.replace(chunkObj.getContent(), chunkObj.getPlaceholder());
                }
                //add the list of token definitions to this file-path key
                mFileTokensLookup.put(filePath, chunkTokenStrs);
            }
        }
        //GET A LIST OF TOKENS THAT ARE INSIDE THIS FILE 
        //==============================================
        //store an array of tokens for this file
        ArrayList<String> tokens=getTokensFromContent(contents);
        //if there is a filename defined in _filenames.xml
        if(overwriteFilename.length()>0){
            //if _filenames.xml contains any tokens (eg, <<var:l:name>>)
            if(filenameTokens.size()>0){
                //ADD TO THE tokens LIST... ADD TOKENS DEFINED INSIDE _filenames.xml (IF ANY)
                //===========================================================================
                //for each token inside _filenames.xml
                for(int t=0;t<filenameTokens.size();t++){
                    String tStr=filenameTokens.get(t);
                    //add the tokens from _filenames.xml to the tokens list
                    tokens.add(tStr+" " + mStrMgr.mTokenSourceSeparator + " " + mStrMgr.mFilenamesXml);
                }
            }
            //ADD TO THE tokens LIST... ADD THE <filename> DEFINITION FROM _filenames.xml (IF EXISTS)
            //=======================================================================================
            //add the <filename> xml node token
            overwriteFilename+=" " + mStrMgr.mTokenSourceSeparator + " " + mStrMgr.mFilenamesXml;
            tokens.add(overwriteFilename);
            //add to the list of files that have a filename xml overwrite
            mFilenameXmlOverwriteLookup.put(filePath, overwriteFilename);
        }
        //STORE THE CONTENTS OF ONE FILE
        //==============================
        //store the file path and original content
        mFileContentLookup.put(filePath, contents);
        //if there is at least one token for this file 
        if(tokens.size()>0){
            atLeastOneToken = true;
            //for each token in this file
            for(int t=0;t<tokens.size();t++){
                //if this file already has a key for its token-list
                if(mFileTokensLookup.containsKey(filePath)){
                    //add the token-definition to the list
                    mFileTokensLookup.get(filePath).add(tokens.get(t));
                }
                //get the token name
                String tName = getTokenPart("name", tokens.get(t));
                //if the name is NOT blank
                if(tName.length()>0){
                    //STORE ALL OF THE TOKEN ALIASES (IF ANY) FOR THIS FILE
                    //=====================================================
                    //if this token has an alias
                    String tAlias = getTokenPart("alias", tokens.get(t));
                    if(tAlias.length()>0){
                        String tAliasPath=filePath;
                        //is this alias coming from _filenames.xml?
                        String tAliasSource = getTokenPart("source", tokens.get(t));
                        if(tAliasSource.equals(mStrMgr.mFilenamesXml)){
                            //set the path of _filenames.xml instead of the template files
                            tAliasPath=fNamesXmlFile.getPath();
                        }                                
                        //if this file path isn't already a key in mFileAliasesLookup
                        if(!mFileAliasesLookup.containsKey(tAliasPath)){
                            //create this path-key in HashMap<[tokenAlias], [tokenStr]>
                            HashMap<String, String> aliasTokenStr = new HashMap<String, String>();
                            mFileAliasesLookup.put(tAliasPath, aliasTokenStr);
                        }
                        //if this alias isn't already listed for this file
                        if (!mFileAliasesLookup.get(tAliasPath).containsKey(tAlias)){
                            //add the alias/name to the file's listing
                            mFileAliasesLookup.get(tAliasPath).put(tAlias, tokens.get(t));
                        }
                    }
                    //STORE THE TOKEN VALUE "OPTIONS" FOR THIS UNIQUE TOKEN NAME
                    //========================================================
                    linkOptionsToUniqueTokenName(tName, tokens.get(t));
                    //STORE ALL OF UNIQUE TOKEN NAMES FOR ALL FILES
                    //=============================================   
                    //if this is not a blank token
                    if (!tName.equals(".")){
                        //if this is NOT a literal token, eg: the file name is "hard-coded.txt"
                        if (tName.indexOf("\"") != 0 && tName.indexOf("'") != 0){
                            //if this token name is not already included in the list
                            if (!mUniqueTokenNames.contains(tName)){
                                //if this name key is also NOT being used by a <<list>> token type
                                if(!mUniqueListTokenNames.contains(tName)){
                                    //add the unique token name, if not already in the list
                                    mUniqueTokenNames.add(tName);
                                }
                            }
                        }
                    }
                }
            }
        }
        //if this file's token-definitions are NOT already listed (because there were no token chunks, eg: <<list>>)
        if(!mFileTokensLookup.containsKey(filePath)){
            //store the file path and tokens
            mFileTokensLookup.put(filePath, tokens);
        }
        return atLeastOneToken;
    }
    //look through all of the mIncludeFiles and load the content/token/alias hash lookups for each file
    public boolean loadFilesData(){
        /*LOOP EACH FILE THE FIRST TIME
        1) mFileContentLookup
            load a HashMap; HashMap<[filePath], [fileContent]>

            IF a file is NOT TEXT-BASED, eg: an image file...
            then the file content will be loaded with a placeholder string text, defined in the constant,
            mNonTextFileContent

        2) mFileTokensLookup
            load a HashMap; HashMap<[filePath], ArrayList<[tokenItemText]>> ... get tokens (if any)

            Token items are mixed together; both _filenames.xml AND template file tokens are included. 
            But tokens from _filenames.xml are only included for a file IF THE FILE'S NAME IS DEFINED IN _filenames.xml...
            AND the definition in _filenames.xml IS NOT BLANK TOKEN TEXT

                    ../config.xml
                            <<var:l:your hi>> --> _filenames.xml
                            <<var:u:your something => [something]>>

        3) mFileAliasesLookup
            load a HashMap; HashMap<[filePath], HashMap<[tokenAlias], [tokenStr]>> ... get token-aliases (if any)

            Contains alias lists, including aliases related to _filenames.xml.
            
            Note, there is a strict association between an alias and its file. The reason for this 1 to 1 relationship: 
            The scope of an alias is ONLY within the file in which it was defined. This means...
            There may be an alias with the SAME NAME in two different files, 
            but they would be able to hold DIFFERENT VALUES unique to their own file, having no knowledge/relation to their twin(s) in separate file(s). 
            
            Image files and other NON-text based files will NEVER have any items in mFileAliasesLookup

                    ../config.xml
                            [something]
                                    <<var:u:your something => [something]>>
                    ../_filenames.xml
                            [name]
                                    <<var:l:your name => [name]>> --> _filenames.xml
                            [test]
                                    <<var:l:your test => [test]>> --> _filenames.xml
            
        5) mUniqueTokenNames
            load an ArrayList; ArrayList<[tokenName]> where each token name only appears once.
            Token names come from any template file PLUS _filenames.xml.

                your name
                your test
                your something
                your hi

        6) mUniqueTokenNameOptions
            List all of the possible input values (if there are restrictions) for a unique token name

        7) mFilenameXmlOverwriteLookup
            HashMap<[filePath], [filenameTokenTxt from _filename.xml]>>

            Gets the filename defined in _filenames.xml... throughout the code, 
            you can check to see if a file's name is defined in _filenames.xml by seeing 
            if it's file path is a key inside mFilenameXmlOverwriteLookup.
            
            Each token string item is guaranteed to come from _filenames.xml AND
            it's guaranteed to be a filename token type

                    ../config.xml
                            <<filename:u:path:[test]>> --> _filenames.xml
                    ../Data.php
                            <<filename:l:.>> --> _filenames.xml
                    ../test.png
                            <<filename:n:test/path:"newname">> --> _filenames.xml
        */
        if(mFileContentLookup==null){
            //1) load a HashMap; HashMap<[filePath], [fileContent]>
            mFileContentLookup=new HashMap<String, String>();
        }else{
            mFileContentLookup.clear();
        }
        if(mFileTokensLookup==null){
            //2) load a HashMap; HashMap<[filePath], ArrayList<[tokenItemText]>> ... get tokens (if any)
            mFileTokensLookup=new HashMap<String, ArrayList<String>>();
        }else{
            mFileTokensLookup.clear();
        }
        if(mFileAliasesLookup==null){
            //3) load a HashMap; HashMap<[filePath], HashMap<[tokenAlias], [tokenStr]>> ... get token-aliases (if any)
            mFileAliasesLookup=new HashMap<String, HashMap<String, String>>();
        }else{
            mFileAliasesLookup.clear();
        }
        if(mUniqueTokenNames==null){
            //4) load an ArrayList; ArrayList<[tokenName]> where each token name only appears once
            mUniqueTokenNames=new ArrayList<String>();
        }else{
            mUniqueTokenNames.clear();
        }
        if(mUniqueListTokenNames==null){
            mUniqueListTokenNames=new ArrayList<String>();
        }else{
            mUniqueListTokenNames.clear();
        }
        if(mTemplateChunks==null){
            mTemplateChunks=new HashMap<String, HashMap<String, ArrayList<TemplateChunk>>>();
        }else{
            mTemplateChunks.clear();
        }
        if(mUniqueTokenNameOptions==null){
            //5) HashMap<[tokenName], ArrayList<[possible-input-value-options]>> where each token name only appears once
            mUniqueTokenNameOptions=new HashMap<String, ArrayList<String>>();
        }else{
            mUniqueTokenNameOptions.clear();
        }
        if(mFilenameXmlOverwriteLookup==null){
            mFilenameXmlOverwriteLookup=new HashMap<String, String>();
        }else{
            //6) HashMap<[filePath], [filenameTokenTxt from _filename.xml]>>
            mFilenameXmlOverwriteLookup.clear();
        }
        if(mTokenInputValues==null){
            mTokenInputValues=new HashMap<String, String>();
        }else{
            mTokenInputValues.clear();
        }
        if(mChangedFileNames==null){
            mChangedFileNames=new HashMap<String, String>();
        }else{
            mChangedFileNames.clear();
        }
        if(mChangedFileDirs==null){
            mChangedFileDirs=new HashMap<String, String>();
        }else{
            mChangedFileDirs.clear();
        }
        //GET TOKEN DATA FROM _filenames.xml SPECIAL FILE
        //===============================================
        //get the _filenames.xml file (if it exists)
        HashMap<String, String> filenamesFromXml = new HashMap<String, String>();
        File fNamesXmlFile=getXmlFilenamesFile();
        ArrayList<String> filenameTokens = new ArrayList<String>();
        if(fNamesXmlFile.exists()){
            //include the filenames xml file in the list of files to collect tokens from
            mIncludeFiles.add(fNamesXmlFile);
            //get all of the filenames from the xml (if any)
            filenamesFromXml=getXmlFilenameHashValues();
            //get the content from _filenames.xml
            String filenamesContent=mFileMgr.readFile(fNamesXmlFile.getPath());
            //if file read was successful
            if(filenamesContent!=null){
                //escape tokens, as needed
                filenamesContent = filenamesContent.replace("\\"+mStrMgr.mStartToken, mStrMgr.mStartEscToken);
                filenamesContent = filenamesContent.replace("\\"+mStrMgr.mEndToken, mStrMgr.mEndEscToken);
                //STORE <filename> TOKEN DEFINITIONS THAT CAME FROM _filenames.xml
                //================================================================
                //get the tokens used inside _filenames.xml
                filenameTokens=getTokensFromContent(filenamesContent);
            }
        }
        //LOOP THROUGH EACH FILE
        //======================
        //for each file
        boolean atLeastOneToken = false;
        for(int f=0;f<mIncludeFiles.size();f++){
            //READ THIS TEMPLATE FILE TO GET ITS CONTENTS
            //===========================================
            String contents="";
            //get the file content
            contents=mFileMgr.readFile(mIncludeFiles.get(f).getPath());
            //if file read was successful
            if(contents!=null){
                //load the data for this file
                boolean fileHasToken = loadContentData(mIncludeFiles.get(f).getPath(), contents, fNamesXmlFile, filenamesFromXml, filenameTokens);
                if(fileHasToken){atLeastOneToken=true;}
            }
        }
        return atLeastOneToken;
    }
    //get all of the tokens, eg: <<list>> which may contain nested content. Returns full token-chunks
    public ArrayList<String> getTokenChunksFromContent(String contents){
        ArrayList<String> chunks = new ArrayList<String>();
        //what are the different possible token type starting strings?
        ArrayList<String> tokenStartTags = new ArrayList<String>();
        tokenStartTags.add("list");
        //if the file content contains the start tag
        if(contents.contains(mStrMgr.mStartToken)){
            //for each token type, which may contain nested content
            for(int t=0;t<tokenStartTags.size();t++){
                //get the token type that's being searched out
                String type=tokenStartTags.get(t);
                //get a list of chunks for this type
                ArrayList<String> typeChunks = mStrMgr.getChunks(contents, mStrMgr.mStartToken+type+mStrMgr.mTokenSeparator, mStrMgr.mTokenSeparator+type+mStrMgr.mEndToken);
                //if there are any chunks for this type
                if(typeChunks.size()>0){
                    //for each chunk of this type
                    for(int c=0;c<typeChunks.size();c++){
                        //add the chunk to the total list of chunks (all chunks in this file)
                        chunks.add(typeChunks.get(c));
                    }
                }
            }
        }
        return chunks;
    }
    //return an array list of tokens found in string content (appended to the tokens)
    public ArrayList<String> getTokensFromContent(String contents){
        ArrayList<String> tokens = new ArrayList<String>();
        //what are the different possible token type starting strings?
        ArrayList<String> tokenStartTags = new ArrayList<String>();
        tokenStartTags.add("var");
        tokenStartTags.add("filename");
        //if the file content contains the start tag
        if(contents.contains(mStrMgr.mStartToken)){
            //split the contents up by the start token tag
            String[] splitByStartTag=contents.split(mStrMgr.mStartToken);
            //for each string that started with mStartToken
            for(int s=0;s<splitByStartTag.length;s++){
                //if the string is not empty
                String str=splitByStartTag[s];
                if(str.trim().length()>0){
                    //if the string contains the end tag
                    if(str.contains(mStrMgr.mEndToken)){
                        //get just the string before the next end token
                        str=str.substring(0,str.indexOf(mStrMgr.mEndToken)+mStrMgr.mEndToken.length());
                        //if the string contains the token-part-separator
                        if(str.contains(mStrMgr.mTokenSeparator)){
                            //get the token parts
                            String[] tokenParts=str.split(mStrMgr.mTokenSeparator);
                            //if the first token part is NOT blank
                            if(tokenParts[0].trim().length()>0){
                                //if the first token part is a token type listed in tokenStartTags
                                if(tokenStartTags.contains(tokenParts[0].trim())){
                                    //if there are at least three parts to the token
                                    if(tokenParts.length>2){
                                        //add the start tag back to the start of the token text
                                        str=mStrMgr.mStartToken+str;
                                        //if this token key is not already in the list
                                        if(!tokens.contains(str)){
                                            //add this token text to the list of of token text (for this one file)
                                            tokens.add(str);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return tokens;
    }
    public String getTokenHeadFromChunk(String tokenStr){
        //CLIP OFF THE LARGE TOKEN CHUNK, IF THIS IS A TOKEN CHUNK
        //========================================================
        //if the token string could be a chunk, eg: <<list>> ... :list>>
        if(tokenStr.contains(mStrMgr.mEndToken)){
            //remove the token part (up to and including the first >>)
            String chunk = tokenStr.substring(tokenStr.indexOf(mStrMgr.mEndToken)+mStrMgr.mEndToken.length());
            chunk=chunk.trim();
            //if the remaining chunk still ends with >>
            if(chunk.lastIndexOf(mStrMgr.mEndToken)==chunk.length()-mStrMgr.mEndToken.length()){
                //remove the chunk part of the token
                tokenStr=tokenStr.substring(0, tokenStr.indexOf(mStrMgr.mEndToken)+mStrMgr.mEndToken.length());
            }
        }
        return tokenStr;
    }
    public String getTokenPartsJson(String tokenStr){
        String[] tokenParts=tokenStr.split(mStrMgr.mTokenSeparator);
        return getTokenPartsJson(tokenParts);
    }
    public String getTokenPartsJson(String[] tokenParts){
        String json="{";
        //all token types have a type
        String tokenType=getTokenPart("type", tokenParts);
        json+="'type':'"+tokenType+"'";
        String casing=""; String options=""; String dir=""; String name=""; String alias=""; String source="";
        switch(tokenType){
            case "var":
                casing=getTokenPart("casing",tokenParts);if(casing.length()>0){json+=",'casing':'"+casing+"'";}
                options=getTokenPart("options",tokenParts);if(options.length()>0){json+=",'options':'"+options+"'";}
                name=getTokenPart("name",tokenParts);if(name.length()>0){json+=",'name':'"+name+"'";}
                alias=getTokenPart("alias",tokenParts);if(alias.length()>0){json+=",'alias':'"+alias+"'";}
                break;
            case "filename":
                casing=getTokenPart("casing",tokenParts);if(casing.length()>0){json+=",'casing':'"+casing+"'";}
                dir=getTokenPart("dir",tokenParts);if(dir.length()>0){json+=",'dir':'"+dir+"'";}
                name=getTokenPart("name",tokenParts);if(name.length()>0){json+=",'name':'"+name+"'";}
                source=getTokenPart("source",tokenParts);if(source.length()>0){json+=",'source':'"+source+"'";}
                break;
            case "list":
                name=getTokenPart("name",tokenParts);if(name.length()>0){json+=",'name':'"+name+"'";}
                break;
        }
        json+="}";
        return json;
    }
    public String getTokenPart(String partKey, String tokenStr){
        //CLIP OFF THE LARGE TOKEN CHUNK, IF THIS IS A TOKEN CHUNK
        //========================================================
        tokenStr=getTokenHeadFromChunk(tokenStr);
        //SPLIT THE TOKEN VALUES INTO A PARAMETER ARRAY
        //=============================================
        //get the token parts, eg: <<type:casing:name>>
        String[] tokenParts=tokenStr.split(mStrMgr.mTokenSeparator);
        return getTokenPart(partKey, tokenParts);
    }
    public String getTokenPart(String partKey, String[] tokenParts){
        String returnStr="";
        //return a different part depending on the given part key
        switch(partKey){
            case "type":
                //type is always first part
                String type = tokenParts[0];
                //if token name contains <<
                if (type.indexOf(mStrMgr.mStartToken)==0) {
                    //remove starting <<
                    type = type.substring(mStrMgr.mStartToken.length());
                }
                //always trim and lowercase token type
                type = type.trim(); type = type.toLowerCase();
                returnStr = type;
                break;
            case "casing":
                //token casing is always second part
                String casing = tokenParts[1];
                //always trim and lowercase token casing
                casing = casing.trim(); casing = casing.toLowerCase();
                returnStr = casing;
                break;
            case "options":
                returnStr = "";
                //recursively get type
                String tokType = getTokenPart("type", tokenParts);
                //if this type is a "var" (only var types can have options)
                if (tokType.equals("var")){
                    //if there are more than 3 parts
                    if(tokenParts.length > 3) {
                        //var options are always third part, eg: <<var:casing:options:name=>alias>>
                        String optsStr=tokenParts[2];
                        //recursively get the casing for this token
                        String tcase = getTokenPart("casing", tokenParts);
                        //split up the options into an array
                        String[] optsArray = optsStr.split("\\|");
                        //for each option
                        ArrayList<String> uniqueOpts = new ArrayList<String>();
                        for(int a=0;a<optsArray.length;a++){
                            //if this option is NOT blank
                            String opt=optsArray[a];
                            if(opt.length()>0){
                                //if trimming this option will not make it blank
                                if(opt.trim().length()>0){
                                    //trim the option
                                    opt=opt.trim();
                                }
                                //apply the casing to the options
                                opt = getAppliedCasing(tcase, opt);
                                //if this option is unique within the list
                                if(!uniqueOpts.contains(opt)){
                                    //list this option as no longer being unique
                                    uniqueOpts.add(opt);
                                    //if not the first item
                                    if(returnStr.length()>0){
                                        returnStr += "|"; //separator
                                    }
                                    //add this option to the return string
                                    returnStr+=opt;
                                }
                            }
                        }
                    }
                }
                break;
            case "dir":
                //if there are more than 3 token parts
                if (tokenParts.length>3){
                    //recursively get type
                    String tokenType=getTokenPart("type", tokenParts);
                    //if this type is a "filename"
                    if (tokenType.equals("filename")){
                        //token directory is always second-to-last part, eg: <<filename:lowercase:folder/path:filename>>
                        String dir=tokenParts[tokenParts.length-2];
                        //always trim token dir
                        dir = dir.trim();
                        returnStr = dir;
                        //normalize the directory separators
                        returnStr=returnStr.replace("\\", "/");
                        returnStr=returnStr.replace("///", "/");
                        returnStr=returnStr.replace("//", "/");
                        returnStr=returnStr.replace("/", File.separator);
                        //if this dir path contains a separtor
                        if (returnStr.contains(File.separator)){
                            //cannot end with \\
                            if (returnStr.lastIndexOf(File.separator) == returnStr.length() - File.separator.length()){
                                //trim off ending \\
                                returnStr = returnStr.substring(0, returnStr.length() - File.separator.length());
                            }
                            //cannot start with \\
                            if (returnStr.indexOf(File.separator) == 0){
                                //trim off starting \\
                                returnStr = returnStr.substring(File.separator.length());
                            }
                        }
                    }
                }
                break;
            case "name":
                //token name is always last part
                String uniqueTokenName = tokenParts[tokenParts.length - 1];
                //if the token name contains a name alias, eg: {name}=>{shorter-alias}
                if (uniqueTokenName.contains(mStrMgr.mAliasSetter)){
                    //get just the name part and remove the alias part
                    uniqueTokenName = uniqueTokenName.substring(0, uniqueTokenName.indexOf(mStrMgr.mAliasSetter));
                }
                else{
                    //no name alias...

                    //if token name contains >>
                    if (uniqueTokenName.contains(mStrMgr.mEndToken)){
                        //remove trailing >>
                        uniqueTokenName = uniqueTokenName.substring(0, uniqueTokenName.lastIndexOf(mStrMgr.mEndToken));
                    }
                }
                //always trim token name
                uniqueTokenName = uniqueTokenName.trim(); 
                //if the token name is NOT a string literal... surrounded by "quotes"
                if (uniqueTokenName.indexOf("\"") != 0 && uniqueTokenName.indexOf("'") != 0){
                    //unique (non-literal) token names are always lowercase
                    uniqueTokenName = uniqueTokenName.toLowerCase();
                }
                returnStr = uniqueTokenName;
                break;
            case "alias":
                returnStr = "";
                //recursively get type
                String tType = getTokenPart("type", tokenParts);
                //if this type is a "var" (only var types can have an alias)
                if (tType.equals("var")){
                    //token name => alias is always last part
                    String nameAndAlias = tokenParts[tokenParts.length - 1];
                    //if the token name contains a name alias, eg: {name}=>{shorter-alias}
                    if (nameAndAlias.contains(mStrMgr.mAliasSetter)){
                        //get just the alias part and remove the name part
                        String aliasStr = nameAndAlias.substring(nameAndAlias.indexOf(mStrMgr.mAliasSetter) + mStrMgr.mAliasSetter.length());
                        //if alias contains >>
                        if (aliasStr.contains(mStrMgr.mEndToken)){
                            //remove trailing >>
                            aliasStr = aliasStr.substring(0, aliasStr.lastIndexOf(mStrMgr.mEndToken));
                        }
                        //trim the alias (alias is case sensitive so it does NOT get toLowerCase)
                        aliasStr = aliasStr.trim();
                        returnStr = aliasStr;
                    }
                }
                break;
            case "source": //what file did the token come from? most times it will be from the file where the token is placed, but sometimes the token can come from _filenames.xml for example
                returnStr = "";
                //recursively get type
                String tfType = getTokenPart("type", tokenParts);
                //if this type is a "filename" (only filename types can have a source other than the file where the token is written)
                if (tfType.equals("filename")){
                    //if there are two or more token parts
                    if(tokenParts.length>1){
                        //get the last token part
                        String lastPart=tokenParts[tokenParts.length-1];
                        //if the last part contains -->
                        if(lastPart.contains(mStrMgr.mTokenSourceSeparator)){
                            //get just the string after the -->
                            lastPart=lastPart.substring(lastPart.lastIndexOf(mStrMgr.mTokenSourceSeparator)+mStrMgr.mTokenSourceSeparator.length());
                            returnStr=lastPart.trim();
                        }
                    }
                }
                break;
        }
        return returnStr;
    }
    //formats the value based on token parameters and input value, eg: decides the casing to apply to the user input
    public String getFormattedTokenValue(String[] tokenParts){
        return getFormattedTokenValue(tokenParts, new ArrayList<String>());
    }
    public String getFormattedTokenValue(String[] tokenParts, ArrayList<String> inFileNameTokens){
        String tokenValue = "";
        //get the token name
        String tokenName=getTokenPart("name", tokenParts);
        //if inFileNameTokens is NOT empty
        boolean doGetVal=true;
        if(inFileNameTokens!=null){
            if(inFileNameTokens.size()>0){
                //if this token name SHOULD be ignored because it is NOT listed in inFileNameTokens
                if(!inFileNameTokens.contains(tokenName)){
                    doGetVal=false;
                }
            }
        }
        //if this token name is NOT being ignored because it is NOT listed in inFileNameTokens
        if(doGetVal){
            //get the token value
            tokenValue=getFormattedTokenValue(tokenParts, tokenName);
        }
        return tokenValue;
    }
    //tokenNameInputKey could be a nested=>key:0, a normal token name, or a "static" value
    public String getFormattedTokenValue(String[] tokenParts, String tokenNameInputKey){
        String tokenValue = "";
        //get the token type
        String type=getTokenPart("type", tokenParts);
        //get the token casing
        String casing=getTokenPart("casing", tokenParts);
        //if not a blank tokenName, represented by a dot, . AND not a static value surrounded by "quotes"
        if (!tokenNameInputKey.equals(".") && tokenNameInputKey.indexOf("\"") != 0 && tokenNameInputKey.indexOf("'") != 0){
            //get the token value... the value is formatted based on the different token parts, eg: casing
            tokenValue = mTokenInputValues.get(tokenNameInputKey);
            //apply the casing formatting
            tokenValue = getAppliedCasing(casing, tokenValue);
        }
        return tokenValue;
    }
    public String getAppliedCasing(String casing, String strVal){
        //get the first letter of the casing 
        String firstCharCasing = casing.trim().toLowerCase();
        firstCharCasing = firstCharCasing.substring(0, 1);
        //default casing
        casing = "normal";
        //standardized what casing is assigned based on the first letter 
        //(for code-readability... no other reason)
        switch (firstCharCasing){
            case "u":
                casing = "uppercase";
                break;
            case "l":
                casing = "lowercase";
                break;
            case "c":
                casing = "capitalize";
                break;
            default:
                break;
        }
        //format depending on casing
        switch (casing){
            case "uppercase":
                strVal = strVal.toUpperCase();
                break;
            case "lowercase":
                strVal = strVal.toLowerCase();
                break;
            case "capitalize":
                String firstChar = strVal.substring(0, 1);
                String theRest = strVal.substring(1);
                firstChar = firstChar.toUpperCase();
                strVal = firstChar + theRest;
                break;
            case "normal":
                //yep... do nothing. Leave as is
                break;
            default:
                break;
        }
        return strVal;
    }
    //get a input value key of ONLY index numbers
    //eg: "name=>sub-name:1=>another-name:3" should change to
    //    "^1_3"
    public String getNestedIndexesNoKey(String listNestedKey, boolean includeLastNestedChildIndex){
        String listIndexesNoKey=""; //eg: "^1_3"
        if(listNestedKey.contains(mStrMgr.mTokenSeparator)){
            String[] nestedKeyParts=listNestedKey.split(mStrMgr.mTokenSeparator);
            //for each nestedKeyPart
            for(int p=0;p<nestedKeyParts.length;p++){
                //if this is an index part (NOT first token name)
                if(p!=0){
                    String indexPart=nestedKeyParts[p];
                    if(indexPart.contains(mStrMgr.mAliasSetter)){
                        //remove the NON index part
                        indexPart=indexPart.substring(0, indexPart.indexOf(mStrMgr.mAliasSetter));
                    }
                    //if NOT first index, then add "_" separator
                    if(listIndexesNoKey.length()>0){listIndexesNoKey+="_";}
                    //add the index part
                    listIndexesNoKey+=indexPart;
                }
            }
        }
        //if not supposed to include the last NESTED child index
        if(!includeLastNestedChildIndex){
            //if contains more than one level (if nested)
            if(listIndexesNoKey.contains("_")){
                //strip off the last NESTED child index
                listIndexesNoKey=listIndexesNoKey.substring(0, listIndexesNoKey.lastIndexOf("_"));
            }else{
                listIndexesNoKey="";
            }
        }
        //add the first level character
        listIndexesNoKey="^"+listIndexesNoKey;
        return listIndexesNoKey;
    }
    //get a input value key stripped of the index numbers
    //eg: "name=>sub-name:1=>another-name:3" should change to
    //    "name=>sub-name=>another-name"
    public String getNestedKeyNoIndexes(String listNestedKey){
        String listNestedKeyNoIndexes=listNestedKey; //eg: "name=>sub-name=>another-name"
        if(listNestedKeyNoIndexes.contains(mStrMgr.mTokenSeparator)){
            String[] nestedKeyParts=listNestedKeyNoIndexes.split(mStrMgr.mTokenSeparator);
            listNestedKeyNoIndexes="";
            //for each nestedKeyPart
            for(int p=0;p<nestedKeyParts.length;p++){
                //if this is an index part (NOT first token name)
                if(p!=0){
                    String indexPart=nestedKeyParts[p];
                    if(indexPart.contains(mStrMgr.mAliasSetter)){
                        //remove the index part
                        indexPart=indexPart.substring(indexPart.indexOf(mStrMgr.mAliasSetter));
                        //add the string that has the index part removed from it
                        listNestedKeyNoIndexes+=indexPart;
                    }
                }else{
                    //add the first token name
                    listNestedKeyNoIndexes+=nestedKeyParts[p];
                }
            }
        }
        return listNestedKeyNoIndexes;
    }
    //get the _filenames.xml File object, if the file exists
    private File getXmlFilenamesFile(){
        return new File(mUseTemplatePath+File.separator+mStrMgr.mFilenamesXml);
    }
    //get the _filenames.xml XML document object, if the file exists
    private Document getXmlFilenamesDoc(){return getXmlFilenamesDoc(getXmlFilenamesFile());}
    private Document getXmlFilenamesDoc(File fnXmlFile){
       Document xmlDoc=null;
        //if _filenames.xml exists
        if(fnXmlFile.exists()){
            //get the XML document object
            xmlDoc=mFileMgr.getXmlDoc(fnXmlFile);
        }
       return xmlDoc;
    }
    //get a list of file or folder names, that meet the criteria... they must start with, end with, or both
    private ArrayList<String> getFileOrDirNamesMatch(File[] fileList, String type,String nameStartsWith, String nameEndsWith){
        ArrayList<String> matchedNames = new ArrayList<String>();
        //for each potential match
        for(int f=0;f<fileList.length;f++){
            //if this is the correct file/folder type
            if((type=="dir"&&fileList[f].isDirectory())||(type=="file"&&fileList[f].isFile())){
                boolean isMatch=true;
                //get the file/folder name
                String name=fileList[f].getName();
                //if the name has to start with some string
                if(nameStartsWith!=null&&nameStartsWith.length()>0){
                    //if the name DOES NOT start with... 
                    if(name.indexOf(nameStartsWith)!=0){
                        isMatch=false;
                    }
                }
                //if still meets match criteria so far
                if(isMatch){
                    //if the name has to end with some string
                    if(nameEndsWith!=null&&nameEndsWith.length()>0){
                        //if the name DOES NOT end with... 
                        if(name.lastIndexOf(nameEndsWith)!=name.length()-nameEndsWith.length()){
                            isMatch=false;
                        }
                    }
                }
                //if still meets match criteria so far
                if(isMatch){
                    matchedNames.add(name);
                }
            }
        }
        return matchedNames;
    }
    //pass a matchesStr value to match files under the given rootPath 
    public ArrayList<String> getIncludeFileMatches(String rootPath, String matchesStr){
        ArrayList<String> matchedFiles = new ArrayList<String>();
        //normalize separators in paths
        String sep=File.separator;
        if(!sep.equals("/")){
            rootPath=rootPath.replace(sep,"/");
            matchesStr=matchesStr.replace(sep,"/");
        }
        //if the include rule begins with /
        if(matchesStr.indexOf("/")==0){
            //remove the starting "/"
            matchesStr=matchesStr.substring("/".length());
        }
        //if the include rule ends with /
        if(matchesStr.lastIndexOf("/")==matchesStr.length()-"/".length()){
            //remove the ending "/"
            matchesStr=matchesStr.substring(0, matchesStr.lastIndexOf("/"));
        }
        //if the root path ends with /
        if(rootPath.lastIndexOf("/")==rootPath.length()-"/".length()){
            //remove the ending "/"
            rootPath=rootPath.substring(0, rootPath.lastIndexOf("/"));
        }
        //if the matchesStr is NOT blank
        matchesStr=matchesStr.trim();
        if(matchesStr.length()>0){
            //if the rootPath is NOT blank
            rootPath=rootPath.trim();
            if(rootPath.length()>0){
                //get the full path
                String fullPath=rootPath+"/"+matchesStr;
                //if the full path contains * special wild-card character
                if(fullPath.indexOf("*")!=-1){
                    //*** and ** are the same as * (this tripple star does NOT denote a code-check)
                    fullPath=fullPath.replace("***", "*");
                    fullPath=fullPath.replace("**", "*");
                    //get the path BEFORE the first * wild-card
                    rootPath=fullPath.substring(0,fullPath.indexOf("*"));
                    //get the string AFTER the first * wild-card
                    matchesStr=fullPath.substring(fullPath.indexOf("*"));
                    //if the rootPath doesn't exist, eg: "path/to/file/filestartswith*"
                    File rootFileFold=new File(rootPath.replace("/", sep));
                    if(!rootFileFold.exists()){
                        //if the root path contains a "/"
                        if(rootPath.lastIndexOf("/")!=-1){
                            //add the string after the last "/" to the start of matchesStr
                            matchesStr=rootPath.substring(rootPath.lastIndexOf("/")+"/".length())+matchesStr;
                            //remove the string before the last "/" from rootPath
                            rootPath=rootPath.substring(0,rootPath.lastIndexOf("/")+"/".length());
                        }
                    }
                }else{
                    //no * special wild-card character...
                    
                    rootPath=fullPath;
                    matchesStr="";
                }
                //if the rootPath still exists
                File rootFileFold=new File(rootPath.replace("/", sep));
                if(rootFileFold.exists()){ 
                    //if there is still matchStr (must contain * in currentFileFold, before next "/")
                    if(matchesStr.length()>0){
                         //get the list of files/folders directly under rootFileFold (if any)... filter this list
                        File[] subFiles = rootFileFold.listFiles();
                        if(subFiles.length>0){
                            //if there is a folder separator
                            String currentFileFold=matchesStr;
                            if(currentFileFold.indexOf("/")!=-1){
                                //remove the sub folders from this path
                                currentFileFold=currentFileFold.substring(0,currentFileFold.indexOf("/"));
                                //remove the first folder from the path
                                matchesStr=matchesStr.substring(matchesStr.indexOf("/"));
                            }else{
                                //no more folder separators...

                                matchesStr="";
                            }
                            ArrayList<String> matchedCurrentFileFoldNames=null;
                            //currentFileFold = "*SOMETHING" OR
                            //currentFileFold = "SOMETHING*" OR
                            //currentFileFold = "SOMETHING*SOMETHING"
                            String startsWith=currentFileFold.substring(0,currentFileFold.indexOf("*"));
                            String endsWith=currentFileFold.substring(currentFileFold.lastIndexOf("*")+"*".length());
                            //if only matching FILE names
                            if(matchesStr.length()<1){
                                //get the FILE names that start with ___, end with ___, or both
                                matchedCurrentFileFoldNames=getFileOrDirNamesMatch(subFiles,"file",startsWith,endsWith);
                                //for each matched name
                                for(int m=0;m<matchedCurrentFileFoldNames.size();m++){
                                    //add this matched file name to the list
                                    String matchedName=matchedCurrentFileFoldNames.get(m);
                                    matchedFiles.add(rootFileFold.getPath()+sep+matchedName);
                                }
                            }else{
                                //only matching DIRECTORY names...

                                //get the DIRECTORY names that start with ___, end with ___, or both
                                matchedCurrentFileFoldNames=getFileOrDirNamesMatch(subFiles,"dir",startsWith,endsWith);
                                //for each matched name
                                for(int m=0;m<matchedCurrentFileFoldNames.size();m++){
                                    //get the matched folder name
                                    String matchedName=matchedCurrentFileFoldNames.get(m);
                                    rootPath=rootFileFold.getPath()+sep+matchedName;
                                    //recursively get the matches under this matched folder name
                                    ArrayList<String> subMatches=getIncludeFileMatches(rootPath, matchesStr);
                                    //for each recursive match
                                    for(int r=0;r<subMatches.size();r++){
                                        //if this file isn't already added to the match list
                                        if(!matchedFiles.contains(subMatches.get(r))){
                                            //add this file path to the matched list
                                            matchedFiles.add(subMatches.get(r));
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        //matchesStr is now empty...
                        
                        //if the root is a folder
                        if(rootFileFold.isDirectory()){
                            //get the list of files/folders directly under rootFileFold (if any)... filter this list
                           File[] subFiles = rootFileFold.listFiles();
                           if(subFiles.length>0){
                                //for each potential match
                                for(int f=0;f<subFiles.length;f++){
                                    //if this sub file is NOT a directory (if an entire directory is included, only include its sub-files but NOT sub folders)
                                    if(subFiles[f].isFile()){
                                        //add this file to the list of matched files
                                        matchedFiles.add(subFiles[f].getPath());
                                    }
                                }
                           }
                        }else if(rootFileFold.isFile()){
                            //if this file isn't already added to the match list
                            if(!matchedFiles.contains(rootFileFold.getPath())){
                                //add this file path to the matched list
                                matchedFiles.add(rootFileFold.getPath());
                            }
                        }
                    }
                }
            }
        }
        return matchedFiles;
    }
    //get a list of files to include (from _filenames.xml) in an export of a project
    public ArrayList<String> getXmlFilenamesIncludeValues(){
        ArrayList<String> includeValues = new ArrayList<String>();
        boolean xmlChangesMade=false;
        //get the _filenames.xml file (if it already exists)
        File fnXmlFile=getXmlFilenamesFile();
        if(fnXmlFile.exists()){
            //get the XML document object
            Document xmlDoc=getXmlFilenamesDoc(fnXmlFile);
            if(xmlDoc!=null){
                //get the document root
                Element root=xmlDoc.getDocumentElement();
                //loop through each <filename> node inside the root node
                NodeList renameNodes = root.getChildNodes();
                for (int r=0;r<renameNodes.getLength();r++){
                    //if this child node is an element (not a text node)
                    Node includeNode=renameNodes.item(r);
                    if (includeNode.getNodeType()==Node.ELEMENT_NODE) {
                        //if this child node is a "include" node
                        if(includeNode.getNodeName().toLowerCase().equals("include")){
                            //get the <include> node inner text
                            includeNode.normalize(); //remove comment text inside the node
                            String nodeText=includeNode.getTextContent();
                            if(nodeText==null){nodeText="";}
                            nodeText=nodeText.trim();
                            //if the node text is NOT blank
                            if(nodeText.length()>0){
                                //if the include rule begins with /
                                if(nodeText.indexOf("/")==0){
                                    //remove the starting "/"
                                    nodeText=nodeText.substring(1);
                                }
                                //if the include rule ends with /
                                if(nodeText.lastIndexOf("/")==nodeText.length()-1){
                                    //remove the ending "/"
                                    nodeText=nodeText.substring(0, nodeText.lastIndexOf("/"));
                                }
                                //if the node text is STILL NOT blank
                                if(nodeText.length()>0){
                                    //if this include value is NOT already listed
                                    if(!includeValues.contains(nodeText)){
                                        //add this include value to the list
                                        includeValues.add(nodeText);
                                    }else{
                                        //include value is ALREADY listed more than once...

                                        //remove this <include> node
                                        root.removeChild(includeNode);
                                        xmlChangesMade=true;
                                    }
                                }
                            }
                        }
                    }
                }
                //if any changes were made to the xml file
                if(xmlChangesMade){
                    //save the changes
                    mFileMgr.saveXmlDoc(xmlDoc, fnXmlFile);
                }
            }
        }
        return includeValues;
    }
    //get a list of rename values from _filenames.xml 
    //and remove any <filename> node that is pointing at nothing OR a file that doesn't exist 
    private HashMap<String, String> getXmlFilenameHashValues(){
        HashMap<String, String> renameList = new HashMap<String, String>();
        boolean xmlChangesMade=false;
        //get the _filenames.xml file (if it already exists)
        File fnXmlFile=getXmlFilenamesFile();
        if(fnXmlFile.exists()){
            //get the XML document object
            Document xmlDoc=getXmlFilenamesDoc(fnXmlFile);
            if(xmlDoc!=null){
                //get the document root
                Element root=xmlDoc.getDocumentElement();
                //loop through each <filename> node inside the root node
                NodeList renameNodes = root.getChildNodes();
                for (int r=0;r<renameNodes.getLength();r++){
                    //if this child node is an element (not a text node)
                    Node renameNode=renameNodes.item(r);
                    if (renameNode.getNodeType()==Node.ELEMENT_NODE) {
                        //if this child node is a "filename" node
                        if(renameNode.getNodeName().toLowerCase().equals("filename")){
                            boolean removeNode=true;
                            //if there is a name attribute
                            Node nameAttr=renameNode.getAttributes().getNamedItem("for");
                            if(nameAttr!=null){
                                //if the name attribute is NOT blank
                                String nameAttrVal=nameAttr.getNodeValue();
                                if(nameAttrVal.length()>0){
                                    //if the name attribute's filePath value is not already a key in the list
                                    String filePath=mUseTemplatePath+File.separator+nameAttrVal;
                                    if(!renameList.containsKey(filePath)){
                                        //if the file actual exists
                                        if(new File(filePath).exists()){
                                            removeNode=false;
                                            //get the <filename> node inner text
                                            renameNode.normalize(); //remove comment text inside the node
                                            String nodeText=renameNode.getTextContent();
                                            if(nodeText==null){nodeText="";}
                                            nodeText=nodeText.trim();
                                            //if the node text is NOT blank
                                            if(nodeText.length()>0){
                                                //if the token contains the token separator
                                                if(nodeText.contains(mStrMgr.mTokenSeparator)){
                                                    //create the full token text
                                                    nodeText=mStrMgr.mStartToken+"filename"+mStrMgr.mTokenSeparator+nodeText+mStrMgr.mEndToken;
                                                }else{
                                                    //invalid token string format
                                                    nodeText="";
                                                }
                                            }
                                            //add the token string to the map list
                                            renameList.put(filePath, nodeText);
                                        }
                                    }else{
                                        //filePath already a key in the list...
                                        removeNode=false;
                                    }
                                }
                            }
                            //if this node should be removed
                            if(removeNode){
                                //remove this <filename> node
                                root.removeChild(renameNode);
                                xmlChangesMade=true;
                            }
                        }
                    }
                }
                //if any changes were made to the xml file
                if(xmlChangesMade){
                    //save the changes
                    mFileMgr.saveXmlDoc(xmlDoc, fnXmlFile);
                }
            }
        }
        return renameList;
    }
    //create/update _filenames.xml for the files inside this template
    public void createUpdateFilenamesXml(String useTemplatePath){
        //get the template folder
        mUseTemplatePath=useTemplatePath;
        File templateFolder=new File(mUseTemplatePath);
        System.out.println(" filenames from the template --> " + mUseTemplatePath + File.separator + "...");
        //get the _filenames.xml file (if it already exists)
        File fnXmlFile=new File(mUseTemplatePath+File.separator+mStrMgr.mFilenamesXml);
        //HashMap<[filePathInTemplate], [filenameTokenTxt]>
        HashMap<String, String> renameNodeList = new HashMap<String, String>();
        //if _filenames.xml does NOT exist
        if(!fnXmlFile.exists()){
            System.out.println(" creating --> " + mStrMgr.mFilenamesXml + " ... ");
            //get boilerplate content for _filenames.xml
            String xmlStr=mFileMgr.getFilenamesXmlStr();
            //create _filenames.xml
            mFileMgr.writeFile(fnXmlFile.getPath(),xmlStr);
        }else{
            //_filenames.xml already exists...
            
            //get the HashMaps of rename values
            //HashMap<[filePathInTemplate], [filenameTokenTxt]>
            renameNodeList = getXmlFilenameHashValues();
        }
        System.out.println(" Tip: filename definitions in " + mStrMgr.mFilenamesXml + " will override filename definitions inside other template file tokens. ");
        System.out.println(" ... \n");
        //get the XML document object
        Document xmlDoc=mFileMgr.getXmlDoc(fnXmlFile);
        if(xmlDoc!=null){
            //LOAD ALL OF THE TOKENS INSIDE THE TEMPLATE SO THAT THE FILENAME TOKENS CAN BE FOUND
            if(mIncludeFiles==null){
                mIncludeFiles=new ArrayList<File>();
            }else{
                mIncludeFiles.clear();
            }
            File[] subFiles = templateFolder.listFiles();
            for(int f=0;f<subFiles.length;f++){
                mIncludeFiles.add(subFiles[f]);
            }
            boolean atLeastOneToken=loadFilesData();
            //LOOP THROUGH EACH FILE TO LIST ITS FILENAME DEFINITION(S) AND UPDATE IT'S _filename.xml FILE, IF NEEDED
            Element root=xmlDoc.getDocumentElement();
            //loop through each file inside templateFolder folder and add the <filename> node to xmlDoc, if it's not already there
            boolean xmlChangeMade=false;
            int fileIndex=0; 
            for(int f=0;f<subFiles.length;f++){
                //if file is NOT commented out
                if(!mFileMgr.isIgnoredFileOrFolder(subFiles[f])){
                    System.out.println(" "+subFiles[f].getName() + "\n");
                    //start the output messages (one for token filename and the other for xml filename)
                    String filenameTokenMsg=""; int numFilenameXmlTokens=0;int numFilenameTokens=0;
                    String tooManyFilenameTokensMsg="";
                    String nonTextMsg="";
                    //if this is NOT a text-based file
                    if(!mFileMgr.isTextBasedFile(subFiles[f])){
                        nonTextMsg="\tConfigure filename in " + mStrMgr.mFilenamesXml + "; this is NOT a text-based file type. \n";
                        nonTextMsg+="\t-------------------------------------------------------------------------- \n";
                    }
                    //if there is at least one token in any of the template files
                    if(atLeastOneToken){
                        //if this template file has ANY tokens
                        if(mFileTokensLookup.containsKey(subFiles[f].getPath())){
                            //for each token inside this file
                            ArrayList<String> tokens=mFileTokensLookup.get(subFiles[f].getPath());
                            for(int t=0;t<tokens.size();t++){
                                //if this token is a filename type
                                String tokenStr=tokens.get(t);
                                String type=getTokenPart("type", tokenStr);
                                if(type.equals("filename")){
                                    //if this filename came from _filenames.xml...
                                    String tokenSource=getTokenPart("source", tokenStr);
                                    if(tokenSource.equals(mStrMgr.mFilenamesXml)){numFilenameXmlTokens++;}
                                    else{numFilenameTokens++;}
                                    //write the token output
                                    filenameTokenMsg+="\t"+tokenStr+" \n";
                                    //if NOT the first token
                                    if(numFilenameTokens>1||numFilenameXmlTokens>1){
                                        tooManyFilenameTokensMsg="\tERROR: there should be ONLY 0 or 1 filename token per file!!! \n";
                                        tooManyFilenameTokensMsg+="\t------------------------------------------------------------- \n";
                                    }
                                }
                            }
                        }
                    }
                    //filename defined as token message
                    System.out.println(nonTextMsg+tooManyFilenameTokensMsg+filenameTokenMsg); 
                    //if this file is NOT already in the _filenames.xml file
                    if(!renameNodeList.containsKey(subFiles[f].getPath())){
                        //create tab before the filename node
                        root.appendChild(xmlDoc.createTextNode("\t"));
                        //create this <filename> node in the xmlDoc
                        Element renameNode=(Element)xmlDoc.createElement("filename");
                        renameNode.setAttribute("for", subFiles[f].getName());
                        Comment renameComment = xmlDoc.createComment("{1}"+mStrMgr.mTokenSeparator+"{2}"+mStrMgr.mTokenSeparator+"{3}");
                        renameNode.appendChild(renameComment);
                        root.appendChild(renameNode);
                        //create newline after the filename node
                        root.appendChild(xmlDoc.createTextNode("\n"));
                        //print message
                        xmlChangeMade=true;
                    }
                }
            }
            //if there were any xml changes
            if(xmlChangeMade){
                //save the changed xml document
                mFileMgr.saveXmlDoc(xmlDoc, fnXmlFile);
            }
        }
    }
}