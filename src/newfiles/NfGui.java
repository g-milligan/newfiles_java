/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package newfiles;

import java.net.URL;
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
    //display elements
    private Newfiles mNewfiles;
    private WebView mWebView;
    private WebEngine mWebEngine;
    private Scene mScene;
    //objects
    private static StrMgr mStrMgr;
    private static FileMgr mFileMgr;
    //init objects and fields
    private void initObjects(){
        mNewfiles=new Newfiles();
        mWebView=new WebView();
        mWebEngine=mWebView.getEngine();
        mStrMgr=new StrMgr();
        mFileMgr=new FileMgr();
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
