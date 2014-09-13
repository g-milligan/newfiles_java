/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package newfiles;

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
            /*
             * What's going in in the following code? Consider the following example:
             * 
             * > str = "blah blah stuff... { this is a {sentence that} I shall parse} some more blahs"
             * > startTag = "{"
             * > endTag = "}"
             * > returnWrapTags = true
             * 
             * The intended result will be:
             * 
             * > retStr = "{ this is a {sentence that} I shall parse}"
             * 
             * follow along...
             */
            //while the startTag is still inside the blob
            int startTagCount = 0; int endTagCount = 0; boolean onward = true;
            while(onward){
                //if startTag still exists
                if(str.contains(startTag)){
                    //remove any string BEFORE AND INCLUDING the startTag
                    //1 > str = " this is a {sentence that} I shall parse} some more blahs"
                    str=str.substring(str.indexOf(startTag) + startTag.length());
                    startTagCount++;
                    //add the start tag to the retStr IF the wrap tags should be returned OR this is NOT the start of the return string
                    if(returnWrapTags || retStr.length() > 0){
                        //1 > retStr = "{"
                        retStr += startTag;
                    }
                }
                //if there is an end tag
                if(endTag.length() > 0 && str.contains(endTag)){
                    //if no more startTag in blob OR startTag AFTER the next endTag
                    if(!str.contains(startTag) || str.indexOf(startTag) > str.indexOf(endTag)){
                        //2 > retStr = "{ this is a {" + "sentence that}"
                        //3 > retStr = "{ this is a {sentence that}" + " I shall parse}"
                        retStr += str.substring(0, str.indexOf(endTag) + endTag.length());
                        //2 > str = " I shall parse} some more blahs"
                        //3 > str = " some more blahs"
                        str=str.substring(str.indexOf(endTag) + endTag.length());
                        endTagCount++;
                    }else{
                        //startTag BEFORE endTag... 
                        
                        //1 > retStr = "{" + " this is a {"
                        retStr += str.substring(0, str.indexOf(startTag) + startTag.length());
                        //1 > str = "sentence that} I shall parse} some more blahs"
                        str=str.substring(str.indexOf(startTag) + startTag.length());
                        startTagCount++;
                    }
                }else{
                    //no more endTag...
                    
                    //just call it a day
                    onward = false;
                }
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
}
