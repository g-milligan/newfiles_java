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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
    
    //display elements
    private Newfiles mNewfiles;
    private WebView mWebView;
    private WebEngine mWebEngine;
    private Scene mScene;
    //objects
    private static StrMgr mStrMgr;
    private static FileMgr mFileMgr;
    private static TemplateData mTemplateData;
    //init objects and fields
    private void initObjects(){
        mNewfiles=new Newfiles();
        mWebView=new WebView();
        mWebEngine=mWebView.getEngine();
        mStrMgr=new StrMgr();
        mFileMgr=new FileMgr();
        mTemplateData=new TemplateData();
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
                                default:
                                    break;
                            }
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
    //get a path with / instead of \
    private String getForwardSeparator(String unformattedPath){
        String path=unformattedPath;
        //if the file separator is NOT /
        if(!File.separator.equals("/")){
            //user / instead of the standard file separator
            path=path.replace(File.separator, "/");
        }
        return path;
    }
    //get the formatted path for a template directory
    private String getFormattedDir(String unformattedPath){
        String path=unformattedPath;
        //if the path starts with the templates root
        if(path.indexOf(mTemplatesRoot)==0){
            //remove the templates root from the start
            path=path.substring(mTemplatesRoot.length()+1);
        }
        path=getForwardSeparator(path);
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
    //get the templates listing JSON
    private String getTemplatesJson(){
        String json="{"; String hiddenDirsJson="";
        //mTemplateHierarchy = HashMap<[templatePath], HashMap<[fileName], ArrayList<[tokenStr]>>>
        loadTemplateHierarchy();
        //if the templates folder exists (mTemplateHierarchy is null if the templates folder doesn't exist)
        if(mTemplateHierarchy!=null){
            //for each templatePath
            for (String templatePath : mTemplateHierarchy.keySet()) {
                //get the files for this template
                HashMap<String, ArrayList<String>> fileTokens=mTemplateHierarchy.get(templatePath);
                //if NOT a hidden template folder
                if(fileTokens!=null){
                    //***
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
        }else{
            //the templates folder doesn't exist...
            
            json+="'error':'The templates folder does not exist. "+getForwardSeparator(mTemplatesRoot)+"'";
        }
        //if any hidden directories
         if(hiddenDirsJson.length()>0){
            //if anything templates were added to the json
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
                        ArrayList<String> tokens=mTemplateData.getTokensFromContent(fileContent);
                        filesTokens.put(subFiles[f].getName(), tokens);
                    }else{
                        //this is an ignored file...
                        
                        //if the file was intentionally hidden from newfiles
                        if(subFiles[f].getName().indexOf("_")==0){
                            //since dir contains at least one file, it should be counted as a template folder
                            dirHasFile=true;
                            //add this hidden file to the list without any listed tokens
                            filesTokens.put(subFiles[f].getName(), null);
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
        //normal init
        initObjects();
        //start web view, include attach events, configuration, setting load URL
        startWebView();
        // create the scene
        int windowWidth=getScreenWorkingWidth();windowWidth=(int)(windowWidth*.88); //% of width
        int windowHeight=getScreenWorkingHeight();windowHeight=(int)(windowHeight*.88); //% of height
        stage.setTitle(mStrMgr.mGuiTitle+" \""+mNewfiles.VERSION_ALIAS+"\" ("+mNewfiles.VERSION_NUMBER+"_"+mNewfiles.PATCH_NUMBER+") | "+mStrMgr.mPandowerxUrl);
        mScene = new Scene(mWebView,windowWidth,windowHeight, Color.web("#666970"));
        //set the scene to the stage
        stage.setScene(mScene); 
        //get icon image(s)
        Image icon32 = getGuiImage("logo_newfiles_32.png"); 
        //set icon image(s)
        stage.getIcons().add(icon32);
        //show the window
        stage.show();
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
