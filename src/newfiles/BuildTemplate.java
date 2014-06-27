/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package newfiles;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author gmilligan
 */
public class BuildTemplate {
    //fields 
    private static HashMap<String, String> mFileContentLookup; //HashMap<[filePath], [fileContent]>
    private static HashMap<String, ArrayList<String>> mFileTokensLookup; //HashMap<[filePath], ArrayList<[tokenItemText]>>
    private static HashMap<String, HashMap<String, String>> mFileAliasesLookup; //HashMap<[filePath], HashMap<[tokenAlias], [tokenStr]>>
    
    private static ArrayList<File> mIncludeFiles; //Files objects to include into the build 
    private static String mTargetDir;
    private static String mBatchFileName;
    
    //constants
    private static final String mStartToken="<<";
    private static final String mEndToken=">>";
    private static final String mTokenSeparator=":";
    private static final String mAliasSetter="=>";
    private static final String mStartEscToken="|_-+StrtToKen..=!_|";
    private static final String mEndEscToken="|_--eNdToKen..=!_|";
    //constructor
    public BuildTemplate(String targetDir, String batchFileName){
        mIncludeFiles=null;
        mTargetDir=targetDir;
        mBatchFileName=batchFileName;
    }
    //method to use a given list of files
    public void useFiles(ArrayList<File> includeFiles){
        mIncludeFiles=includeFiles;
    }
    //build the template
    public void build(){
        if(mIncludeFiles!=null){
            if(mIncludeFiles.size()>0){
                //look through all of the mIncludeFiles and load the content/token/alias hash lookups
                boolean atLeastOneToken = loadFilesData();
                //*** continue logic from here
                //reset the include files
                mIncludeFiles=null;
            }
        }
    }
    //read the contents of a file into a string (default UTF 8 encoding) 
    static String readFile(String path) throws IOException {
        return readFile(path, StandardCharsets.UTF_8);
    }
    //read the contents of a file into a string
    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    private ArrayList<String> getTokensFromContent(String contents){
        ArrayList<String> tokens = new ArrayList<String>();
        //what are the different possible token type starting strings?
        ArrayList<String> tokenStartTags = new ArrayList<String>();
        tokenStartTags.add("var");
        tokenStartTags.add("filename");
        //if the file content contains the start tag
        if(contents.contains(mStartToken)){
            //split the contents up by the start token tag
            String[] splitByStartTag=contents.split(mStartToken);
            //for each string that started with mStartToken
            for(int s=0;s<splitByStartTag.length;s++){
                //if the string is not empty
                String str=splitByStartTag[s];
                if(str.trim().length()>0){
                    //if the string contains the end tag
                    if(str.contains(mEndToken)){
                        //get just the string before the next end token
                        str=str.substring(0,str.indexOf(mEndToken)+mEndToken.length());
                        //if the string contains the token-part-separator
                        if(str.contains(mTokenSeparator)){
                            //get the token parts
                            String[] tokenParts=str.split(mTokenSeparator);
                            //if the first token part is NOT blank
                            if(tokenParts[0].trim().length()>0){
                                //if the first token part is a token type listed in tokenStartTags
                                if(tokenStartTags.contains(tokenParts[0].trim())){
                                    //if there are at least three parts to the token
                                    if(tokenParts.length>2){
                                        //add the start tag back to the start of the token text
                                        str=mStartToken+str;
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
    private String getTokenPart(String partKey, String tokenStr){
        //get the token parts, eg: <<type:casing:name>>
        String[] tokenParts=tokenStr.split(mTokenSeparator);
        return getTokenPart(partKey, tokenParts);
    }
    private String getTokenPart(String partKey, String[] tokenParts){
        String returnStr="";
        //return a different part depending on the given part key
        switch(partKey){
            case "type":
                //type is always first part
                String type = tokenParts[0];
                //if token name contains >>
                if (type.contains(mStartToken)) {
                    //remove starting <<
                    type = type.substring(mEndToken.length());
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
                if (uniqueTokenName.contains(mAliasSetter)){
                    //get just the name part and remove the alias part
                    uniqueTokenName = uniqueTokenName.substring(0, uniqueTokenName.indexOf(mAliasSetter));
                }
                else{
                    //no name alias...

                    //if token name contains >>
                    if (uniqueTokenName.contains(mEndToken)){
                        //remove trailing >>
                        uniqueTokenName = uniqueTokenName.substring(0, uniqueTokenName.lastIndexOf(mEndToken));
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
                    if (nameAndAlias.contains(mAliasSetter)){
                        //get just the alias part and remove the name part
                        String aliasStr = nameAndAlias.substring(nameAndAlias.indexOf(mAliasSetter) + mAliasSetter.length());
                        //if alias contains >>
                        if (aliasStr.contains(mEndToken)){
                            //remove trailing >>
                            aliasStr = aliasStr.substring(0, aliasStr.lastIndexOf(mEndToken));
                        }
                        //trim the alias (alias is case sensitive so it does NOT get toLowerCase)
                        aliasStr = aliasStr.trim();
                        returnStr = aliasStr;
                    }
                }
                break;
        }
        return returnStr;
    }
    //look through all of the mIncludeFiles and load the content/token/alias hash lookups for each file
    private boolean loadFilesData(){
        //LOOP EACH FILE THE FIRST TIME
        //1) load a HashMap; HashMap<[filePath], ArrayList<[tokenItemText]>> ... get tokens (if any)
        //2) load a HashMap; HashMap<[filePath], HashMap<[tokenAlias], [tokenStr]>> ... get token-aliases (if any)
        //=============================
        //clear/init for new data
        if(mFileTokensLookup==null){
            mFileTokensLookup=new HashMap<String, ArrayList<String>>();
        }else{
            mFileTokensLookup.clear();
        }
        if(mFileAliasesLookup==null){
            mFileAliasesLookup=new HashMap<String, HashMap<String, String>>();
        }else{
            mFileAliasesLookup.clear();
        }
        boolean atLeastOneToken = false;
        //for each file
        for(int f=0;f<mIncludeFiles.size();f++){
            //STORE ORIGINAL FILE CONTENTS AND GET AN ARRAY OF TOKENS FOR THIS FILE
            //=====================================================================
            String contents=""; boolean fileReadErr=false;
            try{
                //get the file content
                contents=readFile(mIncludeFiles.get(f).getPath());
            }catch (IOException errread){
                fileReadErr=true;
                System.out.println("\nERROR READING FILE: \n"+mIncludeFiles.get(f).getPath()+" \n...\n"+errread.getMessage()+"\n");
            }
            //if file read was successful
            if(!fileReadErr){
                //escape certain string contents
                contents = contents.replace("\\"+mStartToken, mStartEscToken);
                contents = contents.replace("\\"+mEndToken, mEndEscToken);
                //store the file path and original content
                mFileContentLookup.put(mIncludeFiles.get(f).getPath(), contents);
                //store an array of tokens for this file
                ArrayList<String> tokens=getTokensFromContent(contents);
                mFileTokensLookup.put(mIncludeFiles.get(f).getPath(), tokens);
                //if there is at least one token for this file
                if(tokens.size()>0){
                    atLeastOneToken = true;
                    //STORE ALL OF THE TOKEN ALIASES (IF ANY) FOR THIS FILE
                    //=====================================================
                    //for each token in this file
                    for(int t=0;t<tokens.size();t++){
                        //if this token has an alias
                        String tAlias = getTokenPart("alias", tokens.get(t));
                        if(tAlias.length()>0){
                            //if the name is NOT blank
                            String tName = getTokenPart("name", tokens.get(t));
                            if(tName.length()>0){
                                //if this file path isn't already a key in mFileAliasesLookup
                                if(!mFileAliasesLookup.containsKey(mIncludeFiles.get(f).getPath())){
                                    //create this path-key in HashMap<[tokenAlias], [tokenStr]>
                                    HashMap<String, String> aliasTokenStr = new HashMap<String, String>();
                                    mFileAliasesLookup.put(mIncludeFiles.get(f).getPath(), aliasTokenStr);
                                }
                                //if this alias isn't already listed for this file
                                if (!mFileAliasesLookup.get(mIncludeFiles.get(f).getPath()).containsKey(tAlias)){
                                    //add the alias/name to the file's listing
                                    mFileAliasesLookup.get(mIncludeFiles.get(f).getPath()).put(tAlias, tokens.get(t));
                                }
                            }
                        }
                    }
                }
            }
        }
        return atLeastOneToken;
    }
}
