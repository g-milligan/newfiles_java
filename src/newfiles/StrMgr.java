/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package newfiles;

import java.util.ArrayList;

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
public class StrMgr {
    //accepts a string blob and returns a sub-string that begins and ends with a beginning and ending sub-string
    //even if there are NESTED start and end tags within the return string, this function will determine the correct return sub-string
    public String getChunk(String str, String startTag, String endTag){ return getChunk(str, startTag, endTag, true); }
    public String getChunk(String str, String startTag, String endTag, boolean returnWrapTags){
        String retStr = "";
        //VALIDATE START / END TAGS
        //=========================
        boolean proceed = false;
        //if the string blob is NOT blank
        if (str != null && str.length() > 0){
            //if the start tag isn't blank
            if(startTag != null && startTag.length() > 0){
                //if the start tag is inside the string
                if(str.contains(startTag)){
                    //if the endTag is inside the blob or the endTag is blank
                    if(endTag == null || str.contains(endTag) || endTag.length() < 1){
                        if(endTag == null){endTag="";}
                        //the start and end tags are valid
                        proceed = true;
                    }
                }
            }
        }
        //WORK ON GETTING THE STRING CHUNK OUT OF THE BLOB
        //================================================
        //if confirmed both start and end tags are valid
        if(proceed){
            int startTagCount = 0; int endTagCount = 0; boolean onward = true;
            //remove startTag AND left of startTag
            str=str.substring(str.indexOf(startTag) + startTag.length());
            startTagCount++;
            if(returnWrapTags){
                //add startTag
                retStr+=startTag;
            }
            //while...
            while(onward){
                //if startTag exists
                if(str.contains(startTag)){
                    //if endTag exists
                    if(str.contains(endTag)){
                        //if startTag BEFORE endTag
                        if(str.indexOf(startTag)<str.indexOf(endTag)){
                            //add startTag AND left of startTag
                            retStr+=str.substring(0, str.indexOf(startTag) + startTag.length());
                            //remove startTag AND left of startTag
                            str=str.substring(str.indexOf(startTag) + startTag.length());
                            startTagCount++;
                        }else{
                            //endTag BEFORE startTag...
                            
                            //add endTag AND left of endTag
                            retStr+=str.substring(0, str.indexOf(endTag) + endTag.length());
                            //remove endTag AND left of endTag
                            str=str.substring(str.indexOf(endTag) + endTag.length());
                            endTagCount++;
                        }
                    }else{
                        //startTag, but no endTag...

                        //if too few endTags
                        if(endTagCount<startTagCount){
                            onward = false;
                        }else{
                            //add startTag AND left of startTag
                            retStr+=str.substring(0, str.indexOf(startTag) + startTag.length());
                            //remove startTag AND left of startTag
                            str=str.substring(str.indexOf(startTag) + startTag.length());
                            startTagCount++;
                        }
                    }
                }else{
                    //no startTag...
                    
                    //if endTag exists
                    if(str.contains(endTag)){
                        //if too few startTags
                        if(startTagCount<endTagCount){
                            onward = false;
                        }else{
                            //add endTag AND left of endTag
                            retStr+=str.substring(0, str.indexOf(endTag) + endTag.length());
                            //remove endTag AND left of endTag
                            str=str.substring(str.indexOf(endTag) + endTag.length());
                            endTagCount++;
                        }
                    }else{
                        //no more startTag nor endTag
                        onward=false;
                    }
                }
                //DO THE NUMBER OF START/END TAGS MATCH?
                //if number startTags = endTags (parsed count)
                if(startTagCount==endTagCount){
                    //got the string chunk!
                    onward = false;
                    //if NOT returning the string WITH the wrap tags
                    if(!returnWrapTags){
                        //strip off the last endTag
                        retStr = retStr.substring(0, retStr.length() - endTag.length());
                    }
                }
            }
            //if the number of startTag vs endTag don't match
            if(startTagCount!=endTagCount){
                //maybe an endTag wasn't properly closing the startTag in nested tags. Return nothing.
                retStr = "";
            }
        }
        return retStr;
    }
    //get an array of "chunks" inside the string
    public ArrayList<String> getChunks(String str, String startTag, String endTag){
        ArrayList<String> retStrs = new ArrayList<String>();
        //if the blob even contains the startTag
        if(str.contains(startTag)){
            //while there is even one chunk inside the string
            String oneChunk = ""; boolean firstTry = true;
            while(firstTry || oneChunk.length() > 0){
                firstTry = false;
                //if there is a chunk left in the array
                oneChunk = getChunk(str, startTag, endTag);
                if(oneChunk.length() > 0){
                    //add the chunk to the array
                    retStrs.add(oneChunk);
                    //remove this chunk from the string
                    str=str.substring(str.indexOf(oneChunk) + oneChunk.length());
                }
            }
        }
        return retStrs;
    }
}
