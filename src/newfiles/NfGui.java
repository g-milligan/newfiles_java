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
    
    //display elements
    private Newfiles mNewfiles;
    private WebView mWebView;
    private WebEngine mWebEngine;
    private Scene mScene;
    //objects
    private static StrMgr mStrMgr;
    //init objects and fields
    private void initObjects(){
        mNewfiles=new Newfiles();
        mWebView=new WebView();
        mWebEngine=mWebView.getEngine();
        mStrMgr=new StrMgr();
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
                    System.out.println(" GUI window ready...");
                }
            }
        });
        //FINISH AND LOAD WEBVIEW
        //=======================
        mWebEngine.load(indexPath.toExternalForm());
    }
    //get the URL path of a GUI resource
    private URL getGuiUrl(String fileName){return getClass().getResource(mStrMgr.mGuiRootDir + fileName);}
    @Override
    public void start(Stage stage) {
        //normal init
        initObjects();
        //start web view, include attach events, configuration, setting load URL
        startWebView();
        // create the scene
        stage.setTitle(mStrMgr.mGuiTitle+" \""+mNewfiles.VERSION_ALIAS+"\" ("+mNewfiles.VERSION_NUMBER+"_"+mNewfiles.PATCH_NUMBER+") | "+mStrMgr.mPandowerxUrl);
        mScene = new Scene(mWebView,750,500, Color.web("#666970"));
        stage.setScene(mScene);      
        stage.show();
    }
    //close Newfiles (terminal, GUI window, all of it)
    public void close(){
        System.out.println(" Closing Newfiles from the GUI window...");
        System.exit(0);
    }
    //initial entry point called from Newfiles.java "gui" command
    public static void main(String[] args) {
        launch(args);
    }
}
