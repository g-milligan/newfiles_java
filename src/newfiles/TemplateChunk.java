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
    public ArrayList<TemplateChunk> mTemplateChunks; //the nested template chunks (inside this template chunk)
    public ArrayList<String> mUniqueListTokenNames;
    public ArrayList<String> mUniqueTokenNames;
    public HashMap<String, String> mAliasesLookup; //HashMap<[tokenAlias], [tokenStr]>
    public ArrayList<String> mTokens; //ArrayList<[tokenStr]>
    //fields 
    private String mIndex;
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
    public String getContent(){return mContent;}
    public String getFilePath(){return mFilePath;}
    public String getChunk(){return mChunk;}
    public String getTokenStr(){return mTokenStr;}
    public String getTokenName(){return mTokenName;}
    public String getTokenType(){return mTokenType;}
    public String getPlaceholder(){return mStrMgr.mStartToken+"placeholder"+mStrMgr.mTokenSeparator+mTokenType+mStrMgr.mTokenSeparator+mIndex+mStrMgr.mEndToken;}
    //constructor
    public TemplateChunk(TemplateData tdata, String filePath, String index, String contents){
        //init objects
        mTemplateData=tdata; //init object with useful methods
        mStrMgr=new StrMgr();
        //init lists
        mUniqueListTokenNames=new ArrayList<String>();
        mUniqueTokenNames=new ArrayList<String>();
        mAliasesLookup=new HashMap<String, String>();
        mTemplateChunks=new ArrayList<TemplateChunk>();
        mTokens=new ArrayList<String>();
        //start processing values
        mFilePath=filePath;
        mIndex=index;
        mContent=contents;
        mTokenStr=mTemplateData.getTokenHeadFromChunk(mContent);
        //set token parts
        mTokenParts=mTokenStr.split(mStrMgr.mTokenSeparator);
        mTokenName=mTemplateData.getTokenPart("name", mTokenParts);
        mTokenType=mTemplateData.getTokenPart("type", mTokenParts);
        //get the mChunk
        mChunk=mContent;
        mChunk=mChunk.substring(mTokenStr.length()); //remove the starting mTokenStr
        mChunk=mChunk.substring(0, mChunk.lastIndexOf(mStrMgr.mTokenSeparator+mTokenType+mStrMgr.mEndToken)); //remove the ending mEndToken
        //GET NESTED TEMPLATE CHUNKS
        //==========================
        //if there are any nested token "chunks"
        ArrayList<String> tokenChunks=mTemplateData.getTokenChunksFromContent(mChunk);
        if(tokenChunks.size()>0){ 
            //for each token chunk
            for(int c=0;c<tokenChunks.size();c++){
                //get the chunk
                String chunkContents=tokenChunks.get(c);
                //recursively add to the list of nested template chunks
                TemplateChunk chunkObj=new TemplateChunk(mTemplateData, mFilePath, mIndex + "_" + c, chunkContents);
                mTemplateChunks.add(chunkObj);
                //replace the nested chunk with a placeholder
                mChunk=mChunk.replace(chunkObj.getContent(), chunkObj.getPlaceholder());
                //add this token definition to the list
                mTokens.add(chunkObj.getTokenStr());
                //depending on the token type
                String cType=chunkObj.getTokenType();
                String cName=chunkObj.getTokenName();
                switch(cType){
                    case "list": 
                        //if this token name isn't already in the list
                        if (!mUniqueListTokenNames.contains(cName)){
                            //add the unique token name, if not already in the list
                            mUniqueListTokenNames.add(cName); 
                        }
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
}
