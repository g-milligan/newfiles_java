/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package newfiles;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

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
public class NfGui extends Application {
    
    //http://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
    
    //fields
    private static String mTargetDir;
    private static String mBatchFileName;
    private static String mTemplatesRoot;
    private static HashMap<String, HashMap<String, ArrayList<String>>> mTemplateHierarchy; //HashMap<[templatePath], HashMap<[fileName], ArrayList<[tokenStr]>>>
    private static HashMap<String, ArrayList<String>> mIncludeRules; //HashMap<[templatePath], ArrayList<[includeRul]>>
    private static HashMap<String, HashMap<String, String>> mFilenameXmlOverwriteLookup; //HashMap<[templatePath], HashMap<[filePath], [filenameTokenTxt from _filename.xml]>>>
    private static HashMap<String, HashMap<String, HashMap<String, String>>> mFileAliasesLookup; //HashMap<[templatePath], HashMap<[filePath], HashMap<[tokenAlias], [tokenStr]>>>
    public static HashMap<String, HashMap<String, ArrayList<String>>> mUniqueTokenNameOptions; //HashMap<[templatePath], HashMap<[tokenName], ArrayList<[possible-input-value-options]>>> token name is NOT the nested name, it's just the name with no index
    
    //display elements
    private Newfiles mNewfiles;
    private WebView mWebView;
    private WebEngine mWebEngine;
    private Scene mScene;
    private Stage mStage;
    //objects
    private static StrMgr mStrMgr;
    private static FileMgr mFileMgr;
    private static TemplateData mTemplateData;
    private static BuildTemplate mBuildTemplate;
    //init objects and fields
    private void initObjects(){
        mNewfiles=new Newfiles();
        mWebView=new WebView();
        mWebEngine=mWebView.getEngine();
        mStrMgr=new StrMgr();
        mFileMgr=new FileMgr();
        mTemplateData=new TemplateData();
        mBuildTemplate=new BuildTemplate(mTargetDir, mBatchFileName, mTemplatesRoot);
    }
    private void updateTreeRoot(String targetDir){
        //build the tree view listing
        int maxLevels=2; //only show two levels on page load: 1) root folder AND 2) direct child files/folders
        String treeViewJson=getTreeViewJson(true,targetDir,maxLevels); //max level (from the target root), to start off
        mWebEngine.executeScript("document.body.updateTreeView("+treeViewJson+")");
    }
    //configure and attach events wo the web view
    private void startWebView(){
        //get the web view home page URL
        URL indexPath=getGuiUrl("index.html");

        //WEB VIEW EVENTS
        //===============
         //onload... when the dom loads... 
        mWebEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                //if load successful
                if (newState == Worker.State.SUCCEEDED) {
                    //get the DOM document
                    final Document doc = mWebEngine.getDocument();
                    //build the templates listing
                    String templatesJson=getTemplatesJson();
                    mWebEngine.executeScript("document.body.updateTemplates("+templatesJson+")");
                    //build the tree view listing
                    updateTreeRoot(mTargetDir);
                    //event listener to detect when javascript makes a request to java
                    ((EventTarget)doc).addEventListener("nf_open_folder", new EventListener(){
                        public void handleEvent(Event ev){
                            //get the type of folder to open
                            Element el = doc.getElementById("nf_open_folder");
                            String type=el.getTextContent();
                            //depending on what folder type should be opened
                            switch(type){
                                case "templates":
                                    mFileMgr.openDirWindow(mTemplatesRoot);
                                    break;
                                case "selected-template":
                                    //if there is a selected template
                                    String selectedTemplate=(String)mWebEngine.executeScript("document.body.getSelectedTemplate()");
                                    if(selectedTemplate.length()>0){
                                        //format the path to get it ready to open the actual directory
                                        selectedTemplate=getFormattedDir(selectedTemplate);
                                        selectedTemplate=mFileMgr.getSystemSeparator(selectedTemplate);
                                        //open the actual template directory
                                        mFileMgr.openDirWindow(mTemplatesRoot+File.separator+selectedTemplate);
                                    }else{
                                        //no selected template... just open the template root by default
                                        mFileMgr.openDirWindow(mTemplatesRoot);
                                    }
                                    break;
                                default:
                                    //if type is NOT an empty string
                                    type=type.trim();
                                    if(type.length()>0){
                                        //the "type" could be the actual path requested to open
                                        String path=mFileMgr.getSystemSeparator(type);
                                        File pathFile=new File(path);
                                        //if this path exists
                                        if(pathFile.exists()){
                                            //if this is a file
                                            if(pathFile.isFile()){
                                                //get the parent directory path of this file
                                                path=pathFile.getParent();
                                            }
                                            //open this directory
                                            mFileMgr.openDirWindow(path);
                                        }
                                    }
                                    break;
                            }
                        }
                    }, false);
                    ((EventTarget)doc).addEventListener("nf_append_to_tree", new EventListener(){
                        public void handleEvent(Event ev){
                            String json="";String appendPath="";
                            //get the requested folder data
                            Element el = doc.getElementById("nf_append_to_tree");
                            //get the requested path
                            String path="";
                            NodeList pathNode=el.getElementsByTagName("path");
                            if(pathNode!=null&&pathNode.getLength()>0){
                                Node pathN=pathNode.item(0);
                                path=pathN.getTextContent();path=path.trim();
                            }
                            //if a requested path was provided
                            if(path.length()>0){
                                //if this path actually exists
                                appendPath=path;
                                path=mFileMgr.getSystemSeparator(path);
                                File pathFile = new File(path);
                                if(pathFile.exists()){
                                    //get the requested max levels
                                    String maxLevelsStr=""; int maxLevels=2;
                                    NodeList maxLevelsNode=el.getElementsByTagName("max_levels");
                                    if(maxLevelsNode!=null&&maxLevelsNode.getLength()>0){
                                        Node maxLevelsN=maxLevelsNode.item(0);
                                        maxLevelsStr=maxLevelsN.getTextContent();maxLevelsStr=maxLevelsStr.trim();
                                        try{maxLevels=Integer.parseInt(maxLevelsStr);}
                                        catch(NumberFormatException e) {}
                                    }
                                    json=getTreeViewJson(path,maxLevels);
                                }
                            }
                            //if any json was returned for the folder data
                            if(json.length()>0){
                                //return the json to javascript for processing
                                mWebEngine.executeScript("document.body.updateTreeView("+json+",'"+appendPath+"')");
                            }else{
                                //failed to get json data (this folder could be empty)
                                mWebEngine.executeScript("document.body.failedAppendToTree('"+appendPath+"')");
                            }
                        }
                    }, false);
                    ((EventTarget)doc).addEventListener("nf_browse_tree_root", new EventListener(){
                        public void handleEvent(Event ev){
                            //get the current tree root path
                            Element el = doc.getElementById("nf_browse_tree_root");
                            String currentPath=el.getTextContent();
                            String path=mFileMgr.getSystemSeparator(currentPath);
                            //open the browse for directory window
                            DirectoryChooser dirChooser=new DirectoryChooser();
                            dirChooser.setTitle("Browse Project Root");
                            //if the current directory still exists
                            File initDir=new File(path);
                            if(initDir.exists()){
                                //set the current directory as the initial directory
                                dirChooser.setInitialDirectory(initDir);
                            }
                            //get the chosen directory
                            File selectedDir=dirChooser.showDialog(mStage);
                            //if a directory was chosen
                            if(selectedDir!=null){
                                //if the chosen directory is different from the previous directory
                                if(!selectedDir.getPath().equals(initDir.getPath())){
                                    //change the target root
                                    mTargetDir=selectedDir.getPath();
                                    //update the tree root
                                    updateTreeRoot(mTargetDir);
                                }
                            }
                        }
                    }, false);
                    ((EventTarget)doc).addEventListener("nf_refresh_tree_path", new EventListener(){
                        public void handleEvent(Event ev){
                            //get the current tree root path
                            Element el = doc.getElementById("nf_refresh_tree_path");
                            String currentPath=el.getTextContent();
                            String path=mFileMgr.getSystemSeparator(currentPath);
                            //*** refresh this path
                        }
                    }, false);
                    //DONE SETTING WEB EVENTS, INDICATE DONE
                    System.out.println(" GUI window ready...");
                }
            }
        });
        //FINISH AND LOAD WEBVIEW
        //=======================
        mWebEngine.load(indexPath.toExternalForm());
    }
    //get the formatted path for a template directory
    private String getFormattedDir(String unformattedPath){
        String path=mStrMgr.sanitizeJsonValue(unformattedPath);
        //if the path starts with the templates root
        if(path.indexOf(mTemplatesRoot)==0){
            //remove the templates root from the start
            path=path.substring(mTemplatesRoot.length()+1);
        }
        path=mFileMgr.getForwardSeparator(path);
        //if the path starts with /
        if(path.indexOf("/")==0){
            //remove starting /
            path=path.substring(1);
        }
        //if the path ends with /
        if(path.lastIndexOf("/")==path.length()-1){
            //remove ending /
            path=path.substring(0,path.length()-1);
        }
        return path;
    }
    //get the tree view listing JSON (limit maxLevels from the given targetDir root)
    private String getTreeViewJson(String targetDir, int maxLevels){return getTreeViewJson(false, targetDir, maxLevels);}
    private String getTreeViewJson(boolean isRoot, String targetDir, int maxLevels){
        String json="{";
        //if more than zero maxLevels
        if(maxLevels>0){
            //use real file separator instead of /
            targetDir=mFileMgr.getSystemSeparator(targetDir);
            //if this targetDir exists
            File target=new File(targetDir);
            if(target.exists()){
                //if has read permissions
                if(target.canRead()){
                    //if is directory
                    if(target.isDirectory()){
                        //if NOT a hidden directory
                        if(!target.isHidden()){
                            //if this is the root directory
                            if(isRoot){
                                //add the root path
                                json+="'root':'"+mFileMgr.getForwardSeparator(target.getParent())+"',";
                                //root should be open on page load
                                json+="'is_open':true,";
                            }
                            //directory name
                            json+="'dir':'"+mStrMgr.sanitizeJsonValue(target.getName())+"'";
                            //if still below max number of levels
                            if(maxLevels-1>0){
                                //get the sub files and folders under this folder path
                                File[] subFiles = target.listFiles();
                                //if there are sub files
                                if(subFiles!=null){
                                    //for each sub file
                                    String subJson="";
                                    for(int f=0;f<subFiles.length;f++){
                                        //if this sub node is readable
                                        if(subFiles[f].canRead()){
                                            //if this is a sub file
                                            if(subFiles[f].isFile()){
                                                //if this file is NOT ignored in the tree view
                                                if(!mFileMgr.isIgnoredFileInTreeView(subFiles[f])){
                                                    //if first subJson... then start off the sub level json
                                                    if(subJson.length()<1){subJson+=",'ls':[";}
                                                    else{subJson+=",";} //separate from previous file/folder json item
                                                    //start file json
                                                    subJson+="{";
                                                    //if name
                                                    subJson+="'file':'"+mStrMgr.sanitizeJsonValue(subFiles[f].getName())+"'";
                                                    //if is hidden file
                                                    if(subFiles[f].isHidden()){
                                                        subJson+=",'is_hidden':true";
                                                    }
                                                    //end file json
                                                    subJson+="}";
                                                }
                                            }else{
                                                //if is sub folder
                                                if(subFiles[f].isDirectory()){
                                                    //if NOT a hidden directory
                                                    if(!subFiles[f].isHidden()){
                                                        //if first subJson... then start off the sub level json
                                                        if(subJson.length()<1){subJson+=",'ls':[";}
                                                        else{subJson+=",";} //separate from previous file/folder json item
                                                        //recursive get sub level directory
                                                        subJson+=getTreeViewJson(subFiles[f].getPath(), maxLevels-1);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    //if there were any sub levels
                                    if(subJson.length()>0){
                                        //end the sub 'ls' array
                                        subJson+="]";
                                    }
                                    //add the sub levels to the main json
                                    json+=subJson;
                                }
                            }
                        }
                    }
                }
            }
        }
        json+="}";
        return json;
    }
    //get the templates listing JSON
    private String getTemplatesJson(){
        String json="{"; String hiddenDirsJson=""; 
        //mTemplateHierarchy = HashMap<[templatePath], HashMap<[fileName], ArrayList<[tokenStr]>>>
        loadTemplateHierarchy();
        //if the templates folder exists (mTemplateHierarchy is null if the templates folder doesn't exist)
        if(mTemplateHierarchy!=null){
            //for each templatePath
            for(String templatePath : mTemplateHierarchy.keySet()){
                String includeRulesJson="";
                //get the files for this template
                HashMap<String, ArrayList<String>> fileTokens=mTemplateHierarchy.get(templatePath);
                //if NOT a hidden template folder
                if(fileTokens!=null){
                    //if first dir
                    if(json.equals("{")){
                        //start dirs array
                        json+="'dirs':[";
                    }else{
                        //NOT first dir...
                        json+=",";
                    }
                    //start this dir json
                    json+="{'path':'"+getFormattedDir(templatePath)+"'";
                    //for each file in the template
                    String ignoredFilesJson=""; boolean hasFiles=false;
                    for(String filePath : fileTokens.keySet()){
                        String fileName=new File(filePath).getName();
                        //if this is an ignored file
                        if(fileName.indexOf("_")==0){
                            //if this is the first ignored file
                            if(ignoredFilesJson.length()<1){
                                //start it off
                                ignoredFilesJson+=",'hidden':[";
                            }else{
                                //not the first ignored file
                                ignoredFilesJson+=",";
                            }
                            //add the name to the list of ignored files
                            ignoredFilesJson+="'"+mStrMgr.sanitizeJsonValue(fileName)+"'";
                        }
                        //if NOT an ignored file OR is the _filenames.xml file
                        if(fileName.indexOf("_")!=0||fileName.equals(mStrMgr.mFilenamesXml)){
                            //if this is the first template file
                            if(!hasFiles){json+=",'ls':[";}
                            else{json+=",";}
                            hasFiles=true;
                            //start file json
                            json+="{'name':'"+mStrMgr.sanitizeJsonValue(fileName)+"'";
                            //if this template has _filenames.xml
                            String tokensJson="";boolean hasFilenameOverwrite=false;
                            if(mFilenameXmlOverwriteLookup.containsKey(templatePath)){
                                //if this file has a <filename> in _filenames.xml
                                if(mFilenameXmlOverwriteLookup.get(templatePath).containsKey(filePath)){
                                    //if this filenames token isn't blank inside _filenames.xml
                                    String tokenStr=mFilenameXmlOverwriteLookup.get(templatePath).get(filePath);
                                    if(tokenStr.length()>0){
                                        //if this is the first token
                                        if(tokensJson.length()<1){                                  
                                            tokensJson+=",'tokens':["; //start tokens array
                                        }else{
                                            //not the first token
                                            tokensJson+=",";
                                        }
                                        //if the tokenStr doesn't have an attached source part, ie: --> _filenames.xml
                                        String sourcePart=" " + mStrMgr.mTokenSourceSeparator + " " + mStrMgr.mFilenamesXml;
                                        if(!tokenStr.contains(sourcePart)){
                                            tokenStr+=sourcePart;
                                        }
                                        //get the json for this token string
                                        tokensJson+=mTemplateData.getTokenPartsJson(tokenStr);
                                        hasFilenameOverwrite=true;
                                    }
                                }
                            }
                            //get the tokens in this file
                            ArrayList<String> tokenStrs=fileTokens.get(filePath);
                            if(tokenStrs!=null){
                                //for each token in this file
                                for(int t=0;t<tokenStrs.size();t++){
                                    //if this is the first token
                                    if(tokensJson.length()<1){                                  
                                        tokensJson+=",'tokens':["; //start tokens array
                                    }else{
                                        //not the first token
                                        tokensJson+=",";
                                    }
                                    //get the token string
                                    String tokenStr=tokenStrs.get(t);
                                    //get the json for this token string
                                    tokensJson+=mTemplateData.getTokenPartsJson(tokenStr,hasFilenameOverwrite);
                                }
                            }
                            //if there were any tokens
                            if(tokensJson.length()>0){
                                tokensJson+="]"; //end tokens array
                            }
                            //end file json
                            json+=tokensJson+"}";
                        }                        
                    }
                    //if has any template files, then end 'ls' file array
                    if(hasFiles){json+="]";}
                    //if there were any hidden files
                    if(ignoredFilesJson.length()>0){
                       //tack on the ignored files json
                       json+=ignoredFilesJson+"]";
                    }
                    //if this template has any include rules
                    if(mIncludeRules.containsKey(templatePath)){
                        //start the include rules json
                        includeRulesJson+=",'includes':[";
                        //get the include rules
                        ArrayList<String> includeRules=mIncludeRules.get(templatePath);
                        //for each include rule
                        for(int r=0;r<includeRules.size();r++){
                            //if NOT the first rule, then add the separator
                            if(r!=0){includeRulesJson+=",";}
                            //create the rule's json item
                            String rule=mFileMgr.getForwardSeparator(includeRules.get(r));
                            includeRulesJson+="'"+mStrMgr.sanitizeJsonValue(rule)+"'";
                        }
                        //end the include rules json
                        includeRulesJson+="]";
                        //add the include rules to the json
                        json+=includeRulesJson;
                    }
                    //get the project ids (the token names that help define one or more file path)
                    HashMap<String, String> filenameXmlOverwriteLookup=null;
                    if(mFilenameXmlOverwriteLookup.containsKey(templatePath)){
                        filenameXmlOverwriteLookup=mFilenameXmlOverwriteLookup.get(templatePath);
                    }
                    HashMap<String, HashMap<String, String>> fileAliasesLookup=null;
                    if(mFileAliasesLookup.containsKey(templatePath)){
                        fileAliasesLookup=mFileAliasesLookup.get(templatePath);
                    }
                    //array of unique project id's (names of tokens that define one or more project file paths)
                    ArrayList<String> inFileNameTokens=mBuildTemplate.getTokensInFilenames(templatePath,filenameXmlOverwriteLookup,fileTokens,fileAliasesLookup);
                    //create the json if there are any project id's for this template
                    if(inFileNameTokens.size()>0){
                        //if this template has any tokens WITH OPTIONS
                        HashMap<String, ArrayList<String>> tokenNamesWithOptions=null;
                        if(mUniqueTokenNameOptions.containsKey(templatePath)){
                            tokenNamesWithOptions=mUniqueTokenNameOptions.get(templatePath);
                        }
                        //start project id list json
                        json+=",'project_ids':[";
                        //for each project id (token name)
                        for(int p=0;p<inFileNameTokens.size();p++){ 
                            //if not the first token name... then add comma separator
                            if(p!=0){json+=",";}
                            //start token json
                            json+="{";
                            //add to the token name to the json
                            String tokenName=inFileNameTokens.get(p);
                            json+="'name':'"+mStrMgr.sanitizeJsonValue(tokenName)+"'";
                            //if this template has any tokens WITH OPTIONS
                            if(tokenNamesWithOptions!=null){
                                //if this project id has fixed options
                                if(tokenNamesWithOptions.containsKey(tokenName)){
                                    //if there are any option items in the list
                                    ArrayList<String> options=tokenNamesWithOptions.get(tokenName);
                                    if(options.size()>0){
                                        //start options list
                                        json+=",'options':[";
                                        //for each option
                                        for(int o=0;o<options.size();o++){
                                            //if not the first option, then add separator
                                            if(o!=0){json+=",";}
                                            //add option value
                                            json+="'"+mStrMgr.sanitizeJsonValue(options.get(o))+"'";
                                        }
                                        //end options list
                                        json+="]";
                                    }
                                }
                            }
                            //end token json
                            json+="}";
                        }
                        //end project id list json
                        json+="]";
                    }
                    //end this template dir json
                    json+="}";
                }else{
                    //this is a hidden template folder...
                    
                    //if this is the first hidden directory
                    if(hiddenDirsJson.length()<1){
                        //start the hidden template json
                        hiddenDirsJson+="'hidden':[";
                    }else{
                        //continue json
                        hiddenDirsJson+=",";
                    }
                    //add the hidden template path to the json
                    hiddenDirsJson+="'"+getFormattedDir(templatePath)+"'";
                }
            }
            //if there were any directories
            if(!json.equals("{")){
                //end dirs array
                json+="]";
            }
        }else{
            //the templates folder doesn't exist...
            
            json+="'error':'The templates folder does not exist. "+mFileMgr.getForwardSeparator(mTemplatesRoot)+"'";
        }
        //if any hidden directories
        if(hiddenDirsJson.length()>0){
           //if anything templates or include rules were added to the json
           if(json.length()>1){
               //separate directories from hidden
               json+=",";
           }
           //tack on the hidden directory json
           json+=hiddenDirsJson+"]";
        }
        //close the outer json
        json+="}";
        return json;
    }
    //load the file system's template hierarchy
    private void loadTemplateHierarchy(){
        if(mTemplateHierarchy==null){
            mTemplateHierarchy=new HashMap<String, HashMap<String, ArrayList<String>>>();
        }else{
            mTemplateHierarchy.clear();
        }
        if(mIncludeRules==null){
            mIncludeRules=new HashMap<String, ArrayList<String>>();
        }else{
            mIncludeRules.clear();
        }
        if(mFilenameXmlOverwriteLookup==null){
            mFilenameXmlOverwriteLookup=new HashMap<String, HashMap<String, String>>();
        }else{
            mFilenameXmlOverwriteLookup.clear();
        }
        if(mFileAliasesLookup==null){
            mFileAliasesLookup=new HashMap<String, HashMap<String, HashMap<String, String>>>();
        }else{
            mFileAliasesLookup.clear();
        }
        if(mUniqueTokenNameOptions==null){
            mUniqueTokenNameOptions=new HashMap<String, HashMap<String, ArrayList<String>>>();
        }else{
            mUniqueTokenNameOptions.clear();
        }
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
        }else{
            //template root folder doesn't exist...
            
            mTemplateHierarchy=null;
        }
    }
    //store the associated options for a token name inside of a template
    private static void linkOptionsToUniqueTokenName(String templatePath, String tokenName, String tokenStr){
        //if there are defined input value options for this token
        String tOptionsStr = mTemplateData.getTokenPart("options", tokenStr);
        if(tOptionsStr.length()>0){
            //if this template doesn't already have any associated options
            if(!mUniqueTokenNameOptions.containsKey(templatePath)){
                HashMap<String, ArrayList<String>> options = new HashMap<String, ArrayList<String>>();
                mUniqueTokenNameOptions.put(templatePath, options);
            }
            //if this token name doesn't already have any associated options
            if(!mUniqueTokenNameOptions.get(templatePath).containsKey(tokenName)){
                //create the token name as a key in this HashMap
                ArrayList<String> options = new ArrayList<String>();
                mUniqueTokenNameOptions.get(templatePath).put(tokenName, options);
            }
            //for each option
            String[] tOptionsArray = tOptionsStr.split("\\|");
            for(int a=0;a<tOptionsArray.length;a++){
                //if this option isn't already associated with this token name
                String opt=tOptionsArray[a];
                if(!mUniqueTokenNameOptions.get(templatePath).get(tokenName).contains(opt)){
                    //add the association between this token name and value option
                    mUniqueTokenNameOptions.get(templatePath).get(tokenName).add(opt);
                }
            }
        }
    }
    private static void loadAliasLookupForOneToken(String temPath, String filePath, String tokenStr){
        //if this token contains an alias
        String tAlias=mTemplateData.getTokenPart("alias", tokenStr);
        if(tAlias.length()>0){
            if(!mFileAliasesLookup.containsKey(temPath)){
                HashMap<String, HashMap<String, String>> fileAliasTokenStr = new HashMap<String, HashMap<String, String>>();
                mFileAliasesLookup.put(temPath, fileAliasTokenStr);
            }
            //if this file path isn't already a key in fileAliasesLookup
            if(!mFileAliasesLookup.get(temPath).containsKey(filePath)){
                //create this path-key in HashMap<[tokenAlias], [tokenStr]>
                HashMap<String, String> aliasTokenStr = new HashMap<String, String>();
                mFileAliasesLookup.get(temPath).put(filePath, aliasTokenStr);
            }
            //if this alias isn't already listed for this file
            if (!mFileAliasesLookup.get(temPath).get(filePath).containsKey(tAlias)){
                //add the alias/name to the file's listing
                mFileAliasesLookup.get(temPath).get(filePath).put(tAlias, tokenStr);
            }
        }
    }
    //if a the given directory contains at least one file, hidden from Newfiles or otherwise. But excluding hidden system files
    private static boolean dirContainsFile(File dir){
        boolean hasFile=false;
        //if this is a directory
        if(dir.isDirectory()){
            //for each sub file/folder
            File[] subFiles=dir.listFiles();
            for(int f=0;f<subFiles.length;f++){
                //if this direct child is NOT a directory, but a file
                if(subFiles[f].isFile()){
                    //if this file is NOT hidden OR is purposefully hidden from Newfiles (this excludes other system hidden files)
                    if(!mFileMgr.isIgnoredFileOrFolder(subFiles[f])||subFiles[f].getName().indexOf("_")==0){
                        hasFile=true;
                        break;
                    }
                }
            }
        }
        return hasFile;
    }
    //add a directory to the template list IF it contains at least one file
    private static void maybeAddDirToTemplateList(File dir){
        //if this is an ignored directory
        if(!mFileMgr.isIgnoredFileOrFolder(dir)){
            boolean dirHasFile=false;
            //HashMap<[fileName], ArrayList<[tokenStr]>>
            HashMap<String, ArrayList<String>> filesTokens = new HashMap<String, ArrayList<String>>();
            //for each direct child under dir
            File[] subFiles=dir.listFiles();
            for(int f=0;f<subFiles.length;f++){
                //if this direct child under dir is a directory
                if(subFiles[f].isDirectory()){
                    //add this directory to the template list IF it contains at least one file
                    maybeAddDirToTemplateList(subFiles[f]);
                }else{
                    //this is NOT a directory, it's a file...

                    //if the file is NOT ignored
                    if(!mFileMgr.isIgnoredFileOrFolder(subFiles[f])){
                        //since dir contains at least one file, it should be counted as a template folder
                        dirHasFile=true;
                        //add this file to the list of files under this directory
                        String fileContent=mFileMgr.readFile(subFiles[f].getPath()); //*** save to display... fileContent in file view?
                        //escape tokens, as needed
                        fileContent = fileContent.replace("\\"+mStrMgr.mStartToken, mStrMgr.mStartEscToken);
                        fileContent = fileContent.replace("\\"+mStrMgr.mEndToken, mStrMgr.mEndEscToken);
                        //get the tokens from the string
                        ArrayList<String> tokens=mTemplateData.getTokensFromContent(fileContent, true); //true = include list chunk tokens
                        //if this template file contains any tokens
                        if(tokens.size()>0){
                            //for each token inside the template file
                            for(int t=0;t<tokens.size();t++){
                                //get the token name
                                String tokenName=mTemplateData.getTokenPart("name", tokens.get(t));
                                //load the alias info for this token
                                loadAliasLookupForOneToken(dir.getPath(),subFiles[f].getPath(),tokens.get(t));
                                //load the options info for this token
                                linkOptionsToUniqueTokenName(dir.getPath(),tokenName,tokens.get(t));
                            }
                        }
                        filesTokens.put(subFiles[f].getPath(), tokens);
                    }else{
                        //this is an ignored file...
                        
                        //if the file was intentionally hidden from newfiles
                        if(subFiles[f].getName().indexOf("_")==0){
                            //since dir contains at least one file, it should be counted as a template folder
                            dirHasFile=true;
                            //if this is _filenames.xml
                            if(subFiles[f].getName().equals(mStrMgr.mFilenamesXml)){
                                //get the include rules for this template's _filenames.xml file
                                ArrayList<String> includeRules=mTemplateData.getXmlFilenamesIncludeValues(dir.getPath());
                                //if this _filenames.xml file has any include rules
                                if(includeRules.size()>0){
                                    //add the include rules to the list
                                    mIncludeRules.put(dir.getPath(), includeRules);
                                }
                                //get the <filename> overwrites in this file
                                HashMap<String, String> filenameXmlOverwriteLookup=mTemplateData.getXmlFilenameHashValues(dir.getPath());
                                mFilenameXmlOverwriteLookup.put(dir.getPath(), filenameXmlOverwriteLookup);
                                //get the tokens inside the _filenames.xml file
                                String fileContent=mFileMgr.readFile(subFiles[f].getPath());
                                //escape tokens, as needed
                                fileContent = fileContent.replace("\\"+mStrMgr.mStartToken, mStrMgr.mStartEscToken);
                                fileContent = fileContent.replace("\\"+mStrMgr.mEndToken, mStrMgr.mEndEscToken);
                                //get the tokens from the string
                                ArrayList<String> tokens=mTemplateData.getTokensFromContent(fileContent, false); //true = include list chunk tokens, but _filenames.xml can't contain token chunks
                                //if _filenames.xml contains any tokens
                                if(tokens.size()>0){
                                    ArrayList<String> tokenStrs=new ArrayList<String>();
                                    //for each token inside _filenames.xml
                                    for(int t=0;t<tokens.size();t++){
                                        String tStr=tokens.get(t);
                                        //get the token name
                                        String tokenName=mTemplateData.getTokenPart("name", tStr);
                                        //append the source to the token AND add the modified value to a new list
                                        tokenStrs.add(tStr+" " + mStrMgr.mTokenSourceSeparator + " " + mStrMgr.mFilenamesXml);
                                        //load the alias info for this token
                                        loadAliasLookupForOneToken(dir.getPath(),subFiles[f].getPath(),tStr);
                                        //load the options info for this token
                                        linkOptionsToUniqueTokenName(dir.getPath(),tokenName,tStr);
                                    }
                                    //add the list of tokens inside _filenames.xml
                                    filesTokens.put(subFiles[f].getPath(), tokenStrs);
                                }else{
                                    //_filenames.xml contains ZERO tokens
                                    filesTokens.put(subFiles[f].getPath(), null);
                                }
                            }else{
                                //this is a hidden file AND NOT _filenames.xml
                                //add this hidden file to the list without any listed tokens
                                filesTokens.put(subFiles[f].getPath(), null);
                            }
                        }
                    }
                }
            }
            //if dir contained at least one file
            if(dirHasFile){
                //get the directory path
                String dirPath=dir.getPath();
                //if this directory path is NOT already listed in mTemplateList
                if(!mTemplateHierarchy.containsKey(dirPath)){
                    //add the directory path to mTemplateList
                    mTemplateHierarchy.put(dirPath, filesTokens);
                }
            }
        }else{
            //this is an ignored directory...
            
            //if the directory was intentionally hidden from newfiles
            if(dir.getName().indexOf("_")==0){
                //if this hidden folder contains at least one file
                if(dirContainsFile(dir)){
                    //if this directory path is NOT already listed in mTemplateList
                    String dirPath=dir.getPath();
                    if(!mTemplateHierarchy.containsKey(dirPath)){
                        //add the directory path to mTemplateList
                        mTemplateHierarchy.put(dirPath, null);
                    }
                }
            }
        }
    }
    //get the width and height of the current computer screen
    private int getScreenWorkingWidth() {
        return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
    }
    private int getScreenWorkingHeight() {
        return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
    }
    //get the URL path of a GUI resource
    private URL getGuiUrl(String fileName){return getClass().getResource(mStrMgr.mGuiRootDir + fileName);}
    //get the image object for a GUI image resource
    private Image getGuiImage(String fileName){return new Image(getClass().getResourceAsStream(mStrMgr.mGuiRootDir+"img/"+fileName)); }
    @Override
    public void start(Stage stage) {
        mStage=stage;
        //normal init
        initObjects();
        //start web view, include attach events, configuration, setting load URL
        startWebView();
        // create the scene
        int windowWidth=getScreenWorkingWidth();windowWidth=(int)(windowWidth*.88); //% of width
        int windowHeight=getScreenWorkingHeight();windowHeight=(int)(windowHeight*.88); //% of height
        mStage.setTitle(mStrMgr.mGuiTitle+" \""+mNewfiles.VERSION_ALIAS+"\" ("+mNewfiles.VERSION_NUMBER+"_"+mNewfiles.PATCH_NUMBER+") | "+mStrMgr.mPandowerxUrl);
        mScene = new Scene(mWebView,windowWidth,windowHeight, Color.web("#666970"));
        //set the scene to the mStage
        mStage.setScene(mScene); 
        //get icon image(s)
        Image icon32 = getGuiImage("logo_newfiles_32.png"); 
        //set icon image(s)
        mStage.getIcons().add(icon32);
        //show the window
        mStage.show();
    }
    //close Newfiles (terminal, GUI window, all of it)
    public void close(){
        System.out.println(" Closing Newfiles from the GUI window...");
        System.exit(0);
    }
    //initial entry point called from Newfiles.java "gui" command
    public static void main(String[] args) {
        mTargetDir=args[0];
        mBatchFileName=args[1]; 
        mTemplatesRoot=args[2];
        launch(args);
    }
}
