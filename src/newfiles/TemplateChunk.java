/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package newfiles;

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
public class TemplateChunk {
    //list fields 
    public HashMap<String, ArrayList<TemplateChunk>> mNestedTemplateChunks; //HashMap<[nestedListTokenName], ArrayList<TemplateChunk>>... the nested template chunks (inside this template chunk)
    public ArrayList<String> mUniqueListTokenNames;
    public ArrayList<String> mUniqueTokenNames;
    public HashMap<String, String> mAliasesLookup; //HashMap<[tokenAlias], [tokenStr]>
    public ArrayList<String> mTokens; //ArrayList<[tokenStr]>
    //fields 
    private String mIndex;
    private int mNestLevel; //how many chunk tokens parent this token?
    private String mNestKey;
    private String mFilePath;
    private String mContent; //the full content, eg: "<<list:my token>> ... inner chunk ... :list>>"
    private String mChunk; //the full content MINUS the start and close tags, eg: "... inner chunk ..."
    private String mTokenStr; //the full content MINUS the inner chunk and close tag, eg: "<<list:my token>>"
    private String[] mTokenParts;
    private String mTokenName; //the name of the token, eg: "my token"
    private String mTokenType; //the type of the token, eg: "list"
    //objects
    private TemplateData mTemplateData;
    private StrMgr mStrMgr;
    //get property methods
    public String getIndex(){return mIndex;}
    public String getNestKey(){return mNestKey;}
    public int getNestLevel(){return mNestLevel;}
    public String getContent(){return mContent;}
    public String getFilePath(){return mFilePath;}
    public String getChunk(){return mChunk;}
    public String getTokenStr(){return mTokenStr;}
    public String getTokenName(){return mTokenName;}
    public String getTokenType(){return mTokenType;}
    public String getPlaceholder(){return mStrMgr.mStartToken+mStrMgr.mPlaceholderChunkName+mStrMgr.mTokenSeparator+mTokenType+mStrMgr.mTokenSeparator+mIndex+mStrMgr.mEndToken;}
    //constructor
    public TemplateChunk(TemplateData tdata, String parentNestKey, String filePath, String index, String contents){
        //init objects
        mTemplateData=tdata; //init object with useful methods
        mStrMgr=new StrMgr();
        //init lists
        mUniqueListTokenNames=new ArrayList<String>();
        mUniqueTokenNames=new ArrayList<String>();
        mAliasesLookup=new HashMap<String, String>();
        mNestedTemplateChunks=new HashMap<String, ArrayList<TemplateChunk>>();
        mTokens=new ArrayList<String>();
        //start processing values
        mFilePath=filePath;
        mIndex=index;
        mNestLevel=mIndex.split("_").length;
        mContent=contents;
        mTokenStr=mTemplateData.getTokenHeadFromChunk(mContent);
        //set token parts
        mTokenParts=mTokenStr.split(mStrMgr.mTokenSeparator);
        mTokenName=mTemplateData.getTokenPart("name", mTokenParts);
        mTokenType=mTemplateData.getTokenPart("type", mTokenParts);
        //nested key
        mNestKey=parentNestKey;
        if(mNestKey.length()>0){mNestKey+=mStrMgr.mAliasSetter;}
        mNestKey+=mTokenName;
        //get the mChunk
        mChunk=mContent;
        mChunk=mChunk.substring(mTokenStr.length()); //remove the starting mTokenStr
        mChunk=mChunk.substring(0, mChunk.lastIndexOf(mStrMgr.mTokenSeparator+mTokenType+mStrMgr.mEndToken)); //remove the ending mEndToken
        //INIT THE NESTED LISTS OF THE PARENT TemplateData OBJECT
        //========================================================
        if(tdata.mNestedFileTokensLookup==null){tdata.mNestedFileTokensLookup=new HashMap<String, HashMap<String, ArrayList<String>>>();}
        if(!tdata.mNestedFileTokensLookup.containsKey(mNestKey)){
            HashMap<String, ArrayList<String>> nestedFileTokens = new HashMap<String, ArrayList<String>>();
            tdata.mNestedFileTokensLookup.put(mNestKey, nestedFileTokens);
        }
        if(!tdata.mNestedFileTokensLookup.get(mNestKey).containsKey(mFilePath)){
            ArrayList<String> nestedTokens = new ArrayList<String>();
            tdata.mNestedFileTokensLookup.get(mNestKey).put(mFilePath, nestedTokens);
        }
        //GET NESTED TEMPLATE CHUNKS
        //==========================
        //if there are any nested token "chunks"
        ArrayList<String> tokenChunks=mTemplateData.getTokenChunksFromContent(mChunk);
        if(tokenChunks.size()>0){ 
            //for each token chunk
            for(int c=0;c<tokenChunks.size();c++){
                //get the chunk
                String chunkContents=tokenChunks.get(c);
                //load the nested template chunk object
                TemplateChunk chunkObj=new TemplateChunk(mTemplateData, mNestKey, mFilePath, mIndex + "_" + c, chunkContents);
                //get some key values from the chunk
                String cType=chunkObj.getTokenType();
                String cName=chunkObj.getTokenName();
                //if this nested list name is not already listed
                if(!mNestedTemplateChunks.containsKey(cName)){
                    //init the chunk at this listed key
                    ArrayList<TemplateChunk> nestedChunksList = new ArrayList<TemplateChunk>();
                    mNestedTemplateChunks.put(cName, nestedChunksList);
                }
                //add this chunk to the key position
                mNestedTemplateChunks.get(cName).add(chunkObj);
                //replace the nested chunk with a placeholder
                mChunk=mChunk.replace(chunkObj.getContent(), chunkObj.getPlaceholder());
                //add this token definition to the list
                mTokens.add(chunkObj.getTokenStr());
                //depending on the token type
                switch(cType){
                    case "list": 
                        //if this token name isn't already in the list
                        if (!mUniqueListTokenNames.contains(cName)){
                            //add the unique token name, if not already in the list
                            mUniqueListTokenNames.add(cName); 
                        }
                        //INIT THE NESTED LISTS OF THE PARENT TemplateData OBJECT
                        //========================================================
                        if(tdata.mNestedUniqueListTokenNames==null){tdata.mNestedUniqueListTokenNames=new HashMap<String, ArrayList<String>>();}
                        if(!tdata.mNestedUniqueListTokenNames.containsKey(mNestKey)){
                            ArrayList<String> nestedUniqueTokens = new ArrayList<String>();
                            tdata.mNestedUniqueListTokenNames.put(mNestKey, nestedUniqueTokens);
                        }
                        //if this token name isn't already added to this nested key
                        if(!tdata.mNestedUniqueListTokenNames.get(mNestKey).contains(cName)){
                           tdata.mNestedUniqueListTokenNames.get(mNestKey).add(cName);
                        }
                        //now add this tokenStr to the file token lookup 
                        tdata.mNestedFileTokensLookup.get(mNestKey).get(mFilePath).add(chunkObj.getTokenStr());
                        break;
                }
            }
        }
        //GET NON-CHUNK TOKENS (TOKENS THAT DON'T HAVE NESTED CONTENT)
        //============================================================
        //store an array of tokens for this chunk
        ArrayList<String> tokens=mTemplateData.getTokensFromContent(mChunk);
        //if there are any tokens
        if(tokens.size()>0){
            //for each token in this chunk
            for(int t=0;t<tokens.size();t++){
                //if the name is NOT blank
                String tokenStr=tokens.get(t);
                String[] tokenParts=tokenStr.split(mStrMgr.mTokenSeparator);
                String tName = mTemplateData.getTokenPart("name", tokenParts);
                if(tName.length()>0){
                    //if this token name is not already used as the name of a list token
                    if(!mUniqueListTokenNames.contains(tName)){
                        //if NOT a filename token (filename tokens are NOT allowed nested in chunks, like in list chunks)
                        String tType = mTemplateData.getTokenPart("type", tokenParts);
                        if(!tType.equals("filename")){
                            //if this token name is NOT already listed in unique names
                            if(!mUniqueTokenNames.contains(tName)){
                                mUniqueTokenNames.add(tName);
                            }
                            //if this token has an alias
                            String tAlias = mTemplateData.getTokenPart("alias", tokenParts);
                            if(tAlias.length()>0){
                                //if this alias isn't already listed
                                if(!mAliasesLookup.containsKey(tAlias)){
                                    mAliasesLookup.put(tAlias, tokenStr);
                                }
                            }
                            //add this token definition to the list
                            mTokens.add(tokenStr);
                            //INIT THE NESTED LISTS OF THE PARENT TemplateData OBJECT
                            //========================================================
                            if(tdata.mNestedUniqueTokenNames==null){tdata.mNestedUniqueTokenNames=new HashMap<String, ArrayList<String>>();}
                            if(!tdata.mNestedUniqueTokenNames.containsKey(mNestKey)){
                                ArrayList<String> nestedUniqueTokens = new ArrayList<String>();
                                tdata.mNestedUniqueTokenNames.put(mNestKey, nestedUniqueTokens);
                            }
                            //if this token name isn't already added to this nested key
                            if(!tdata.mNestedUniqueTokenNames.get(mNestKey).contains(tName)){
                               tdata.mNestedUniqueTokenNames.get(mNestKey).add(tName);
                            }
                            //now add this tokenStr to the file token lookup 
                            tdata.mNestedFileTokensLookup.get(mNestKey).get(mFilePath).add(tokenStr);
                        }else{
                            //this is a filename token...

                            //the filename token should be removed from this nested content
                            mChunk=mChunk.replace(tokenStr, "");
                        }
                    }else{
                        //this token name already used as the name of a list token...
                        
                        //the token names must be unique... remove the non-unique token
                        mChunk=mChunk.replace(tokenStr, "");
                    }
                }
            }
        }
    }
    //replace TemplateChunk tokens with real values and return file content (td is the mData object)
    public String setTokenValues(String nestedKey, String fileContent, TemplateData td){
        //get the placeholder for this chunk (inside the fileContent)... from THIS object
        String chunkPlaceholderStr=getPlaceholder();
        //get the token chunk to insert back into the file content... from THIS object
        String oneItemChunkStr=getChunk();
        //if there are any tokens in this chunk
        if(mTokens.size()>0){
            //HashMap<[itemIndex], HashMap<[tokenStrOrAlias], [tokenValue]>>
            HashMap<String, HashMap<String, String>> oneLevelListValues = new HashMap<String, HashMap<String, String>>();
            ArrayList<String> parentListIndexKeys = new ArrayList<String>();
            //GET ALL OF THE LIST ITEM VALUES AND THEIR CORRESPONDING SUB-STRINGS TO REPLACE
            //==============================================================================
            //for each token inside this nested level... from THIS object
            int lastItemIndex=-1; boolean hasNestedChunk=false;
            for (int n=0;n<mTokens.size();n++){
                //get the name of this nested token
                String tokenStr=mTokens.get(n);
                String[] tokenParts=tokenStr.split(mStrMgr.mTokenSeparator);
                String tokenType=td.getTokenPart("type", tokenParts);
                String tokenName=td.getTokenPart("name", tokenParts);
                //depending on token type
                switch(tokenType){
                    case "var":
                        //HANDLE ALL var VALUES (INSIDE EVERY LIST ITEM)
                        //==============================================
                        //based on the token name, get the keys to retrieve the stored input values
                        String tokenInputKey=nestedKey+mStrMgr.mAliasSetter+tokenName;
                        //list item index
                        int itemIndex=0;
                        while(td.mTokenInputValues.containsKey(tokenInputKey+mStrMgr.mTokenSeparator+itemIndex)){
                            //FORMAT THE VAR VALUE
                            //====================
                            String inputValue=td.mTokenInputValues.get(tokenInputKey+mStrMgr.mTokenSeparator+itemIndex);
                            //WHAT STRING GETS REPLACED BY THE VALUE?
                            //=======================================
                            String replaceThisStr=tokenStr;
                            //if this token has an alias
                            String tokenAlias=td.getTokenPart("alias", tokenParts);
                            if(tokenAlias.length()>0){
                                //remove the token definition from the oneItemChunkStr
                                oneItemChunkStr=oneItemChunkStr.replace(tokenStr, "");
                                //target the alias for value replacement
                                replaceThisStr=tokenAlias;
                            }
                            //MAKE SURE THE ITEM INDEX IS IN THE SET OF KEYS
                            //==============================================
                            //if this list item index is NOT already in the list
                            if(!oneLevelListValues.containsKey(itemIndex+"")){
                                HashMap<String, String> nameValList = new HashMap<String, String>();
                                oneLevelListValues.put(itemIndex+"", nameValList);
                            }
                            //STORE THIS ITEM'S INPUT VALUE
                            //=============================
                            //add the key/value for this list item index
                            oneLevelListValues.get(itemIndex+"").put(replaceThisStr, inputValue);
                            //next item index
                            itemIndex++;
                        }
                        //set the highest item index for this level (under this nestedKey path)
                        lastItemIndex=itemIndex-1;
                    break;
                    case "list":
                        //handle this list chunk once PER LIST ITEM
                        hasNestedChunk=true;
                    break;
                }
            }
            //FOR EACH LIST ITEM (WRITE THE ACTUAL VALUES)
            //============================================
            //for each list item
            String allItemChunks="";
            for(int i=0;i<=lastItemIndex;i++){
                //one repeated (for every item) template chunk 
                String thisItemChunk=oneItemChunkStr;
                //if this list item has nested chunk(s)
                if(hasNestedChunk){
                    //HANDLE NESTED CHUNKS (RECURSIVELY) INSIDE THIS LIST ITEM
                    //========================================================
                    //if there is another nested sub list inside this list's item chunks
                    if(thisItemChunk.contains(mStrMgr.mStartToken+mStrMgr.mPlaceholderChunkName+mStrMgr.mTokenSeparator+"list")){
                        //for each nested list's token name
                        for(String listTokenName : mNestedTemplateChunks.keySet()){
                            //for each nested chunk (with this listTokenName)
                            ArrayList<TemplateChunk> nestedChunkObjs=mNestedTemplateChunks.get(listTokenName);
                            for(int nn=0;nn<nestedChunkObjs.size();nn++){
                                //get the list item index for this chunk
                                String subNestedKey=nestedKey+mStrMgr.mAliasSetter+listTokenName+mStrMgr.mTokenSeparator+i;
                                //get the chunk object
                                TemplateChunk nestedChunkObj=nestedChunkObjs.get(nn);
                                //fill in one chunk list item
                                thisItemChunk=nestedChunkObj.setTokenValues(subNestedKey,thisItemChunk,td); 
                            }
                        }
                    }
                }
                //REPLACE ALL TOKEN PLACEHOLDERS WITH REAL VALUES IN THIS LIST ITEM
                //=================================================================
                //HashMap<[tokenStrOrAlias], [tokenValue]>
                HashMap<String, String> itemValues=oneLevelListValues.get(i+"");
                //for each input value in THIS list item
                for(String replaceTxt : itemValues.keySet()){
                    //get the input value
                    String inputValue = itemValues.get(replaceTxt);
                    //replace the alias or token definition with the value
                    thisItemChunk=thisItemChunk.replace(replaceTxt, inputValue);
                }
                //APPEND THIS LIST ITEM TEXT TO THE GLOB OF ALL LIST ITEMS
                //========================================================
                //add another item chunk
                allItemChunks+=thisItemChunk;
            }
            //INSERT THE LIST ITEMS GLOB BACK INTO THE FILE CONTENT
            //=====================================================
            //the template chunk is complete... replace the placeholder with the actual template text
            fileContent=fileContent.replace(chunkPlaceholderStr, allItemChunks);
        }else{
            //no tokens inside this chunk...
            
            //*** just repeat this token chunk depending on user input item count
        }
        return fileContent;
    }
}
