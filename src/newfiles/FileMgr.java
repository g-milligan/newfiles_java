/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package newfiles;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
public class FileMgr {
    //objects
    private static StrMgr mStrMgr;
    //constructor
    public FileMgr(){
        mStrMgr = new StrMgr();
    }
    //determine if the file is a text-based file or other... eg: an image...
    //this info is needed to decide if a file should be written or copied to a location
    public static boolean isTextBasedFile(File f){
        boolean isTxt=true;
        //if the file exists
        if(f.exists()){
            //get the file name
            String fileName=f.getName();
            //if the file name contains a dot, '.'
            int lastIndexOfDot=fileName.lastIndexOf(".");
            if(lastIndexOfDot>-1){
                //get just the extension from the file name (trim off name)
                String ext=fileName.substring(lastIndexOfDot+1);
                ext=ext.trim();
                //if the filename didn't just end with a dot
                if(ext.length()>0){
                   ext=ext.toLowerCase();
                   //detect for NON-text based file extensions
                   switch(ext){
                       //COMMON IMAGE FILE EXTENSIONS
                       case "jpeg": isTxt=false;break; case "jpg": isTxt=false;break;
                       case "jfif": isTxt=false;break; case "exif": isTxt=false;break;
                       case "tiff": isTxt=false;break; case "tif": isTxt=false;break;
                       case "raw": isTxt=false;break; case "gif": isTxt=false;break;
                       case "bmp": isTxt=false;break; case "png": isTxt=false;break;
                       case "ppm": isTxt=false;break; case "pgm": isTxt=false;break;
                       case "pbm": isTxt=false;break; case "pnm": isTxt=false;break;
                       case "webp": isTxt=false;break; case "hdr": isTxt=false;break;
                       //COMMON COMPRESSED FILE EXTENSIONS
                       case "zip": isTxt=false;break; case "tgz": isTxt=false;break;
                       case "gz": isTxt=false;break; case "tar": isTxt=false;break; 
                       case "lbr": isTxt=false;break; case "iso": isTxt=false;break;
                       case "7z": isTxt=false;break; case "ar": isTxt=false;break;
                       case "rar": isTxt=false;break;
                       //COMMON EXECUTABLE EXTENSIONS
                       case "jar": isTxt=false;break; case "exe": isTxt=false;break;
                       //COMMON COMPILED CODE EXTENSIONS
                       case "dll": isTxt=false;break;
                       //DEFAULT HANDLING
                       default:
                       break;
                   }
                }
            }
        }else{
            //file doesn't exist
            isTxt=false;
        }
        return isTxt;
    }
    //determine if the file or folder should be ignored:
    //1) if it's hidden in the file-system
    //2) if it begins with "_"
    public static boolean isIgnoredFileOrFolder(String forfPath){
        File forf = new File(forfPath);
        return isIgnoredFileOrFolder(forf);
    }
    public static boolean isIgnoredFileOrFolder(File forf){
        boolean isIgnored = false;
        //if the name begins with "_"
        if(forf.getName().indexOf("_")==0){
            //should be ignored
            isIgnored = true;
    }else{
            //if the name is not blank
            if(forf.getName().length() > 0){
                //if this file actually exists
                if(forf.exists()){
                    //if the file should be hidden 
                    if(forf.isHidden()){
                        //should be ignored
                        isIgnored = true;
                    }
                }
            }
        }
        return isIgnored;
    }
    //copy a non-text OR normal text file to some location
    public static boolean copyFileTo(File source, File dest) {
        boolean success=false;
        try {
            //try to copy the file to a destination
            Files.copy(source.toPath(), dest.toPath());
            success=true;
        } catch (IOException ex) {
            //show a message if the copy failed
            System.out.println("\n Uh oh... failed to copy file --> "+source.toPath() + " ");
            System.out.println("to destination --> " + dest.toPath() + " \n" + ex.getMessage() + "\n");
        }
        return success;
    }
    //create / overwrite file (default UTF 8 encoding) 
    public boolean writeFile(String filePath, String fileContent){
        return writeFile(filePath, fileContent, StandardCharsets.UTF_8);
    }
    //create / overwrite file
    public boolean writeFile(String filePath, String fileContent, Charset encoding){
        Writer writer = null; boolean success=false;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), encoding));
            writer.write(fileContent);
            success=true;
        } catch (IOException ex) {
            System.out.println("\n Uh oh... failed to write file --> "+filePath);
            ex.printStackTrace();
        } finally {
           try {writer.close();} catch (Exception ex) {}
        }
        return success;
    }
    //read the contents of a file into a string (default UTF 8 encoding) 
    public static String readFile(String path){
        return readFile(path, StandardCharsets.UTF_8);
    }
    //read the contents of a file into a string
    public static String readFile(String path, Charset encoding){
        String str="";
        //if this is a text based file, eg: not an image file
        if(isTextBasedFile(new File(path))){
            try{
                //read the file content
                byte[] encoded = Files.readAllBytes(Paths.get(path));
                str=new String(encoded, encoding);
            }catch (IOException errread){
                str=null;
                System.out.println("\n Uh oh... failed to read file --> " + path + " ");
                System.out.println(errread.getStackTrace() + "\n");
            }
        }else{
            //this is a non-text based file...
            str=mStrMgr.mNonTextFileContent;
        }
        return str;
    }
    //get the license document's string contents 
    public String getLicenseDocContents(){
        String licenseStr=getResStr(mStrMgr.mAppLicensePath);
        licenseStr=licenseStr.trim();
        licenseStr="\n\n"+licenseStr+"\n\n";
        return licenseStr;
    }
    //gets a string from an internal resource stream
    public String getResStr(String resPath){
        String str=null;
        //get the stream
        InputStream is = this.getClass().getResourceAsStream(resPath);
        BufferedReader reader = null;
        //start reading
        try {
            //create a buffered reader
            reader = new BufferedReader(new InputStreamReader(is));
            //create a string builder
            StringBuilder out = new StringBuilder();
            String line;
            //try to read the file
            while ((line = reader.readLine()) != null) {
                out.append(line+"\n");
            }
            //set the file contents
            str=out.toString();
        } catch (IOException ex) {
            System.out.println("\n Uh oh... failed to read resource file --> "+resPath);
            ex.printStackTrace();
        } finally {
           try {reader.close();} catch (Exception ex) {}
        }
        return str;
    }
    //open up a direcctory window
    public static void openDirWindow(String dirPath){
        Desktop desktop = Desktop.getDesktop();
        try {
            File dirToOpen = new File(dirPath);
            System.out.print(" opening... ");
            desktop.open(dirToOpen);
            System.out.println("");
        } catch (IOException e) {
            System.out.println("\nUh oh... failed to open directory --> " + dirPath);
            System.out.println(e.getMessage()+"\n");
        }
    }
    public boolean saveXmlDoc(Document xmlDoc, File output){
        boolean success=false;
        try{
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result result = new StreamResult(output);
            Source input = new DOMSource(xmlDoc);
            transformer.transform(input, result);
            success=true;
        }catch(TransformerException te){
            System.out.println("\n Uh oh... failed to save XML file --> "+output.getPath());
            System.out.println(te.getStackTrace()+"\n");
        }
        return success;
    }
    //get an xml document object from the given file object
    public Document getXmlDoc(File file){return getXmlDoc(file, false);}
    public Document getXmlDoc(File file, boolean doNormalize){
        Document document = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(file);
            if(doNormalize){
                document.getDocumentElement().normalize();
            }
        }catch(ParserConfigurationException pex){
            System.out.println("\n Uh oh... failed to parse XML file --> "+file.getPath());
            System.out.println(pex.getStackTrace()+"\n");
        }catch(SAXException sax){
            System.out.println("\n Uh oh... failed to sax XML file --> "+file.getPath());
            System.out.println(sax.getStackTrace()+"\n");
        }catch(IOException iox){
            System.out.println("\n Uh oh... failed to io XML file --> "+file.getPath());
            System.out.println(iox.getStackTrace()+"\n");
        }
        return document;
    }
    //get the url to a resource file packaged inside the .jar app
    public String getFilenamesXmlStr() {
        //use / instead of File.separator because this is an internal resource path
        return getResStr(mStrMgr.mAppResDir + "/" + mStrMgr.mAppResXmlDir + "/" + mStrMgr.mFilenamesXml);
    }
}
