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
import java.util.ArrayList;
import java.util.HashMap;
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
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
public class BuildTemplate {
    //fields 
    private static HashMap<String, String> mFileContentLookup; //HashMap<[filePath], [fileContent]>
    private static HashMap<String, ArrayList<String>> mFileTokensLookup; //HashMap<[filePath], ArrayList<[tokenItemText]>>
    private static HashMap<String, String> mFilenameXmlOverwriteLookup; //HashMap<[filePath], [filenameTokenTxt from _filename.xml]>>
    private static HashMap<String, HashMap<String, String>> mFileAliasesLookup; //HashMap<[filePath], HashMap<[tokenAlias], [tokenStr]>>
    private static HashMap<String, HashMap<String, String>> mTokenChunkPlaceholders; //HashMap<[filePath], HashMap<[uniquePlaceholder], [tokenChunkStr]>>
    private static ArrayList<String> mUniqueTokenNames; //ArrayList<[tokenName]> each token name only appears once
    private static HashMap<String, ArrayList<String>> mUniqueTokenNameOptions; //HashMap<[tokenName], ArrayList<[possible-input-value-options]>>
    private static HashMap<String, String> mTokenInputValues; //HashMap<[tokenName], [inputValue]>
    private static HashMap<String, String> mChangedFileNames; //HashMap<[filePath], [changedFileName]>
    private static HashMap<String, String> mChangedFileDirs; //HashMap<[filePath], [changedFileDirectories]>
    
    private static ArrayList<File> mIncludeFiles; //Files objects to include into the build 
    private static String mTargetDir;
    private static String mBatchFileName;
    private static String mTemplatesRoot;
    private static String mUseTemplatePath; //the path to the current using template
    
    //constants
    private static final String mStartToken="<<";
    private static final String mEndToken=">>";
    private static final String mTokenSeparator=":";
    private static final String mTokenSourceSeparator="-->";
    private static final String mAliasSetter="=>";
    private static final String mStartEscToken="|_-+StrtToKen..=!_|";
    private static final String mEndEscToken="|_--eNdToKen..=!_|";
    private static final String mNonTextFileContent="|_--nOT@tXTfiLE..=!_|~~JUB123eZ55_-CoO__|"; //unique text content to use as a non-text file content placeholder
    private static final String mAppResDir="res"; //root folder name of resources packaged inside the .jar app
    private static final String mAppResXmlDir="xml";
    private static final String mAppLicensePath=mAppResDir+"/license/pandowerx.newfiles.LICENSE.txt";
    private final static String mFilenamesXml = "_filenames.xml"; //the filename where non-text files (eg: images) can have their output paths defined
    
    //objects
    private static StrMgr mStrMgr;
    
    //constructor
    public BuildTemplate(String targetDir, String batchFileName, String templatesRoot){
        mStrMgr=new StrMgr();
        mIncludeFiles=null;
        mTargetDir=targetDir;
        mBatchFileName=batchFileName;
        mTemplatesRoot=templatesRoot;
    }
    //method to use a given list of files
    public void useFiles(String useTemplatePath, ArrayList<File> includeFiles){
        mIncludeFiles=includeFiles;
        mUseTemplatePath=useTemplatePath;
    }
    //build the template
    public void build(){
        if(mIncludeFiles!=null){
            if(mIncludeFiles.size()>0){
                //1) look through all of the mIncludeFiles and load the content/token/alias hash lookups
                boolean atLeastOneToken = loadFilesData();
                //2) accept user input for tokens
                userInputForTokens(atLeastOneToken);
                //3) get the template files' contents with replaced tokens 
                setTokenValues();
                //4) write the template files
                writeOutputFiles();
                //reset the include files
                mIncludeFiles=null;
            }
        }
    }
    //export a package based on a template
    public String export(){
        String exportDir="";
        if(mIncludeFiles!=null){
            if(mIncludeFiles.size()>0){
                //1) look through all of the mIncludeFiles and load the content/token/alias hash lookups
                boolean atLeastOneToken = loadFilesData();
                //2) accept user input for tokens (only if the token is used to build out the file path)
                ArrayList<String> inFileNameTokens=userInputForFilenameTokens(atLeastOneToken);
                //3) set the template files' contents with replaced filename-tokens
                //only replace token value in the inFileNameTokens list
                setTokenValues(inFileNameTokens);
                //4) write the export files
                exportDir=writeOutputFiles("export");
                //reset the include files
                mIncludeFiles=null;
            }
        }
        return exportDir;
    }
    //get the _filenames.xml File object, if the file exists
    private File getXmlFilenamesFile(){
        return new File(mUseTemplatePath+File.separator+mFilenamesXml);
    }
    //get the _filenames.xml XML document object, if the file exists
    private Document getXmlFilenamesDoc(){return getXmlFilenamesDoc(getXmlFilenamesFile());}
    private Document getXmlFilenamesDoc(File fnXmlFile){
       Document xmlDoc=null;
        //if _filenames.xml exists
        if(fnXmlFile.exists()){
            //get the XML document object
            xmlDoc=getXmlDoc(fnXmlFile);
        }
       return xmlDoc;
    }
    //get a list of rename values from _filenames.xml 
    //and remove any <filename> node that is pointing at nothing OR a file that doesn't exist 
    private HashMap<String, String> getXmlFilenameHashValues(){
        HashMap<String, String> renameList = new HashMap<String, String>();
        boolean xmlChangesMade=false;
        //get the _filenames.xml file (if it already exists)
        File fnXmlFile=getXmlFilenamesFile();
        if(fnXmlFile.exists()){
            //get the XML document object
            Document xmlDoc=getXmlFilenamesDoc(fnXmlFile);
            if(xmlDoc!=null){
                //get the document root
                Element root=xmlDoc.getDocumentElement();
                //loop through each <filename> node inside the root node
                NodeList renameNodes = root.getChildNodes();
                for (int r=0;r<renameNodes.getLength();r++){
                    //if this child node is an element (not a text node)
                    Node renameNode=renameNodes.item(r);
                    if (renameNode.getNodeType()==Node.ELEMENT_NODE) {
                        //if this child node is a "filename" node
                        if(renameNode.getNodeName().toLowerCase().equals("filename")){
                            boolean removeNode=true;
                            //if there is a name attribute
                            Node nameAttr=renameNode.getAttributes().getNamedItem("for");
                            if(nameAttr!=null){
                                //if the name attribute is NOT blank
                                String nameAttrVal=nameAttr.getNodeValue();
                                if(nameAttrVal.length()>0){
                                    //if the name attribute's filePath value is not already a key in the list
                                    String filePath=mUseTemplatePath+File.separator+nameAttrVal;
                                    if(!renameList.containsKey(filePath)){
                                        //if the file actual exists
                                        if(new File(filePath).exists()){
                                            removeNode=false;
                                            //get the <filename> node inner text
                                            renameNode.normalize(); //remove comment text inside the node
                                            String nodeText=renameNode.getTextContent();
                                            if(nodeText==null){nodeText="";}
                                            nodeText=nodeText.trim();
                                            //if the node text is NOT blank
                                            if(nodeText.length()>0){
                                                //if the token contains the token separator
                                                if(nodeText.contains(mTokenSeparator)){
                                                    //create the full token text
                                                    nodeText=mStartToken+"filename"+mTokenSeparator+nodeText+mEndToken;
                                                }else{
                                                    //invalid token string format
                                                    nodeText="";
                                                }
                                            }
                                            //add the token string to the map list
                                            renameList.put(filePath, nodeText);
                                        }
                                    }else{
                                        //filePath already a key in the list...
                                        removeNode=false;
                                    }
                                }
                            }
                            //if this node should be removed
                            if(removeNode){
                                //remove this <filename> node
                                root.removeChild(renameNode);
                                xmlChangesMade=true;
                            }
                        }
                    }
                }
                //if any changes were made to the xml file
                if(xmlChangesMade){
                    //save the changes
                    saveXmlDoc(xmlDoc, fnXmlFile);
                }
            }
        }
        return renameList;
    }
    //create/update _filenames.xml for the files inside this template
    public void createUpdateFilenamesXml(String useTemplatePath){
        //get the template folder
        mUseTemplatePath=useTemplatePath;
        File templateFolder=new File(mUseTemplatePath);
        System.out.println(" filenames from the template --> " + mUseTemplatePath + File.separator + "...");
        //get the _filenames.xml file (if it already exists)
        File fnXmlFile=new File(mUseTemplatePath+File.separator+mFilenamesXml);
        //HashMap<[filePathInTemplate], [filenameTokenTxt]>
        HashMap<String, String> renameNodeList = new HashMap<String, String>();
        //if _filenames.xml does NOT exist
        if(!fnXmlFile.exists()){
            System.out.println(" creating --> " + mFilenamesXml + " ... ");
            //get boilerplate content for _filenames.xml
            String xmlStr=getFilenamesXmlStr();
            //create _filenames.xml
            writeFile(fnXmlFile.getPath(),xmlStr);
        }else{
            //_filenames.xml already exists...
            
            //get the HashMaps of rename values
            //HashMap<[filePathInTemplate], [filenameTokenTxt]>
            renameNodeList = getXmlFilenameHashValues();
        }
        System.out.println(" Tip: filename definitions in " + mFilenamesXml + " will override filename definitions inside other template file tokens. ");
        System.out.println(" ... \n");
        //get the XML document object
        Document xmlDoc=getXmlDoc(fnXmlFile);
        if(xmlDoc!=null){
            //LOAD ALL OF THE TOKENS INSIDE THE TEMPLATE SO THAT THE FILENAME TOKENS CAN BE FOUND
            if(mIncludeFiles==null){
                mIncludeFiles=new ArrayList<File>();
            }else{
                mIncludeFiles.clear();
            }
            File[] subFiles = templateFolder.listFiles();
            for(int f=0;f<subFiles.length;f++){
                mIncludeFiles.add(subFiles[f]);
            }
            boolean atLeastOneToken=loadFilesData();
            //LOOP THROUGH EACH FILE TO LIST ITS FILENAME DEFINITION(S) AND UPDATE IT'S _filename.xml FILE, IF NEEDED
            Element root=xmlDoc.getDocumentElement();
            //loop through each file inside templateFolder folder and add the <filename> node to xmlDoc, if it's not already there
            boolean xmlChangeMade=false;
            int fileIndex=0; 
            for(int f=0;f<subFiles.length;f++){
                //if file is NOT commented out
                if(subFiles[f].getName().indexOf("_")!=0){
                    System.out.println(" "+subFiles[f].getName() + "\n");
                    //start the output messages (one for token filename and the other for xml filename)
                    String filenameTokenMsg=""; int numFilenameXmlTokens=0;int numFilenameTokens=0;
                    String tooManyFilenameTokensMsg="";
                    String nonTextMsg="";
                    //if this is NOT a text-based file
                    if(!isTextBasedFile(subFiles[f])){
                        nonTextMsg="\tConfigure filename in " + mFilenamesXml + "; this is NOT a text-based file type. \n";
                        nonTextMsg+="\t-------------------------------------------------------------------------- \n";
                    }
                    //if there is at least one token in any of the template files
                    if(atLeastOneToken){
                        //if this template file has ANY tokens
                        if(mFileTokensLookup.containsKey(subFiles[f].getPath())){
                            //for each token inside this file
                            ArrayList<String> tokens=mFileTokensLookup.get(subFiles[f].getPath());
                            for(int t=0;t<tokens.size();t++){
                                //if this token is a filename type
                                String tokenStr=tokens.get(t);
                                String type=getTokenPart("type", tokenStr);
                                if(type.equals("filename")){
                                    //if this filename came from _filenames.xml...
                                    String tokenSource=getTokenPart("source", tokenStr);
                                    if(tokenSource.equals(mFilenamesXml)){numFilenameXmlTokens++;}
                                    else{numFilenameTokens++;}
                                    //write the token output
                                    filenameTokenMsg+="\t"+tokenStr+" \n";
                                    //if NOT the first token
                                    if(numFilenameTokens>1||numFilenameXmlTokens>1){
                                        tooManyFilenameTokensMsg="\tERROR: there should be ONLY 0 or 1 filename token per file!!! \n";
                                        tooManyFilenameTokensMsg+="\t------------------------------------------------------------- \n";
                                    }
                                }
                            }
                        }
                    }
                    //filename defined as token message
                    System.out.println(nonTextMsg+tooManyFilenameTokensMsg+filenameTokenMsg); 
                    //if this file is NOT already in the _filenames.xml file
                    if(!renameNodeList.containsKey(subFiles[f].getPath())){
                        //create tab before the filename node
                        root.appendChild(xmlDoc.createTextNode("\t"));
                        //create this <filename> node in the xmlDoc
                        Element renameNode=(Element)xmlDoc.createElement("filename");
                        renameNode.setAttribute("for", subFiles[f].getName());
                        Comment renameComment = xmlDoc.createComment("{1}"+mTokenSeparator+"{2}"+mTokenSeparator+"{3}");
                        renameNode.appendChild(renameComment);
                        root.appendChild(renameNode);
                        //create newline after the filename node
                        root.appendChild(xmlDoc.createTextNode("\n"));
                        //print message
                        xmlChangeMade=true;
                    }
                }
            }
            //if there were any xml changes
            if(xmlChangeMade){
                //save the changed xml document
                saveXmlDoc(xmlDoc, fnXmlFile);
            }
        }
    }
    private boolean saveXmlDoc(Document xmlDoc, File output){
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
    private Document getXmlDoc(File file){return getXmlDoc(file, false);}
    private Document getXmlDoc(File file, boolean doNormalize){
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
    private String getFilenamesXmlStr() {
        //use / instead of File.separator because this is an internal resource path
        return getResStr(mAppResDir + "/" + mAppResXmlDir + "/" + mFilenamesXml);
    }
    //get the license document's string contents 
    public String getLicenseDocContents(){
        String licenseStr=getResStr(mAppLicensePath);
        licenseStr=licenseStr.trim();
        licenseStr="\n\n"+licenseStr+"\n\n";
        return licenseStr;
    }
    //gets a string from an internal resource stream
    private String getResStr(String resPath){
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
            str=mNonTextFileContent;
        }
        return str;
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
    //get all of the tokens, eg: <<list>> which may contain nested content. Returns full token-chunks
    private ArrayList<String> getTokenChunksFromContent(String contents){
        ArrayList<String> chunks = new ArrayList<String>();
        //what are the different possible token type starting strings?
        ArrayList<String> tokenStartTags = new ArrayList<String>();
        tokenStartTags.add("list");
        //if the file content contains the start tag
        if(contents.contains(mStartToken)){
            //for each token type, which may contain nested content
            for(int t=0;t<tokenStartTags.size();t++){
                //get the token type that's being searched out
                String type=tokenStartTags.get(t);
                //get a list of chunks for this type
                ArrayList<String> typeChunks = mStrMgr.getChunks(contents, mStartToken+type+mTokenSeparator, mTokenSeparator+type+mEndToken);
                //if there are any chunks for this type
                if(typeChunks.size()>0){
                    //for each chunk of this type
                    for(int c=0;c<typeChunks.size();c++){
                        //add the chunk to the total list of chunks (all chunks in this file)
                        chunks.add(typeChunks.get(c));
                    }
                }
            }
        }
        return chunks;
    }
    //return an array list of tokens found in string content (appended to the tokens)
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
            case "options":
                returnStr = "";
                //recursively get type
                String tokType = getTokenPart("type", tokenParts);
                //if this type is a "var" (only var types can have options)
                if (tokType.equals("var")){
                    //if there are more than 3 parts
                    if(tokenParts.length > 3) {
                        //var options are always third part, eg: <<var:casing:options:name=>alias>>
                        String optsStr=tokenParts[2];
                        //recursively get the casing for this token
                        String tcase = getTokenPart("casing", tokenParts);
                        //split up the options into an array
                        String[] optsArray = optsStr.split("\\|");
                        //for each option
                        ArrayList<String> uniqueOpts = new ArrayList<String>();
                        for(int a=0;a<optsArray.length;a++){
                            //if this option is NOT blank
                            String opt=optsArray[a];
                            if(opt.length()>0){
                                //if trimming this option will not make it blank
                                if(opt.trim().length()>0){
                                    //trim the option
                                    opt=opt.trim();
                                }
                                //apply the casing to the options
                                opt = getAppliedCasing(tcase, opt);
                                //if this option is unique within the list
                                if(!uniqueOpts.contains(opt)){
                                    //list this option as no longer being unique
                                    uniqueOpts.add(opt);
                                    //if not the first item
                                    if(returnStr.length()>0){
                                        returnStr += "|"; //separator
                                    }
                                    //add this option to the return string
                                    returnStr+=opt;
                                }
                            }
                        }
                    }
                }
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
            case "source": //what file did the token come from? most times it will be from the file where the token is placed, but sometimes the token can come from _filenames.xml for example
                returnStr = "";
                //recursively get type
                String tfType = getTokenPart("type", tokenParts);
                //if this type is a "filename" (only filename types can have a source other than the file where the token is written)
                if (tfType.equals("filename")){
                    //if there are two or more token parts
                    if(tokenParts.length>1){
                        //get the last token part
                        String lastPart=tokenParts[tokenParts.length-1];
                        //if the last part contains -->
                        if(lastPart.contains(mTokenSourceSeparator)){
                            //get just the string after the -->
                            lastPart=lastPart.substring(lastPart.lastIndexOf(mTokenSourceSeparator)+mTokenSourceSeparator.length());
                            returnStr=lastPart.trim();
                        }
                    }
                }
                break;
        }
        return returnStr;
    }
    //look through all of the mIncludeFiles and load the content/token/alias hash lookups for each file
    private boolean loadFilesData(){
        /*LOOP EACH FILE THE FIRST TIME
        1) mFileContentLookup
            load a HashMap; HashMap<[filePath], [fileContent]>

            IF a file is NOT TEXT-BASED, eg: an image file...
            then the file content will be loaded with a placeholder string text, defined in the constant,
            mNonTextFileContent

        2) mFileTokensLookup
            load a HashMap; HashMap<[filePath], ArrayList<[tokenItemText]>> ... get tokens (if any)

            Token items are mixed together; both _filenames.xml AND template file tokens are included. 
            But tokens from _filenames.xml are only included for a file IF THE FILE'S NAME IS DEFINED IN _filenames.xml...
            AND the definition in _filenames.xml IS NOT BLANK TOKEN TEXT

                    ../config.xml
                            <<var:l:your hi>> --> _filenames.xml
                            <<var:u:your something => [something]>>

        3) mFileAliasesLookup
            load a HashMap; HashMap<[filePath], HashMap<[tokenAlias], [tokenStr]>> ... get token-aliases (if any)

            Contains alias lists, including aliases related to _filenames.xml.
            
            Note, there is a strict association between an alias and its file. The reason for this 1 to 1 relationship: 
            The scope of an alias is ONLY within the file in which it was defined. This means...
            There may be an alias with the SAME NAME in two different files, 
            but they would be able to hold DIFFERENT VALUES unique to their own file, having no knowledge/relation to their twin(s) in separate file(s). 
            
            Image files and other NON-text based files will NEVER have any items in mFileAliasesLookup

                    ../config.xml
                            [something]
                                    <<var:u:your something => [something]>>
                    ../_filenames.xml
                            [name]
                                    <<var:l:your name => [name]>> --> _filenames.xml
                            [test]
                                    <<var:l:your test => [test]>> --> _filenames.xml
                                    
        4) mTokenChunkPlaceholders
            HashMap<[filePath], HashMap<[uniquePlaceholder], [tokenChunkStr]>>
        
            Some token types can contain nested content (eg: "list" tokens, "opt" tokens) which may or may not be output to the project
            mTokenChunkPlaceholders will contain the placeholder key for this content and the original chunk string that will be 
            used IF the content is printed in the project file
            
        5) mUniqueTokenNames
            load an ArrayList; ArrayList<[tokenName]> where each token name only appears once.
            Token names come from any template file PLUS _filenames.xml.

                your name
                your test
                your something
                your hi

        6) mUniqueTokenNameOptions
            List all of the possible input values (if there are restrictions) for a unique token name

        7) mFilenameXmlOverwriteLookup
            HashMap<[filePath], [filenameTokenTxt from _filename.xml]>>

            Gets the filename defined in _filenames.xml... throughout the code, 
            you can check to see if a file's name is defined in _filenames.xml by seeing 
            if it's file path is a key inside mFilenameXmlOverwriteLookup.
            
            Each token string item is guaranteed to come from _filenames.xml AND
            it's guaranteed to be a filename token type

                    ../config.xml
                            <<filename:u:path:[test]>> --> _filenames.xml
                    ../Data.php
                            <<filename:l:.>> --> _filenames.xml
                    ../test.png
                            <<filename:n:test/path:"newname">> --> _filenames.xml
        */
        if(mFileContentLookup==null){
            //1) load a HashMap; HashMap<[filePath], [fileContent]>
            mFileContentLookup=new HashMap<String, String>();
        }else{
            mFileContentLookup.clear();
        }
        if(mFileTokensLookup==null){
            //2) load a HashMap; HashMap<[filePath], ArrayList<[tokenItemText]>> ... get tokens (if any)
            mFileTokensLookup=new HashMap<String, ArrayList<String>>();
        }else{
            mFileTokensLookup.clear();
        }
        if(mFileAliasesLookup==null){
            //3) load a HashMap; HashMap<[filePath], HashMap<[tokenAlias], [tokenStr]>> ... get token-aliases (if any)
            mFileAliasesLookup=new HashMap<String, HashMap<String, String>>();
        }else{
            mFileAliasesLookup.clear();
        }
        if(mTokenChunkPlaceholders==null){
            //4) HashMap<[filePath], HashMap<[uniquePlaceholder], [tokenChunkStr]>>
            mTokenChunkPlaceholders=new HashMap<String, HashMap<String, String>>();
        }else{
            mTokenChunkPlaceholders.clear();
        }
        if(mUniqueTokenNames==null){
            //5) load an ArrayList; ArrayList<[tokenName]> where each token name only appears once
            mUniqueTokenNames=new ArrayList<String>();
        }else{
            mUniqueTokenNames.clear();
        }
        if(mUniqueTokenNameOptions==null){
            //6) HashMap<[tokenName], ArrayList<[possible-input-value-options]>> where each token name only appears once
            mUniqueTokenNameOptions=new HashMap<String, ArrayList<String>>();
        }else{
            mUniqueTokenNameOptions.clear();
        }
        if(mFilenameXmlOverwriteLookup==null){
            mFilenameXmlOverwriteLookup=new HashMap<String, String>();
        }else{
            //7) HashMap<[filePath], [filenameTokenTxt from _filename.xml]>>
            mFilenameXmlOverwriteLookup.clear();
        }
        if(mTokenInputValues==null){
            mTokenInputValues=new HashMap<String, String>();
        }else{
            mTokenInputValues.clear();
        }
        if(mChangedFileNames==null){
            mChangedFileNames=new HashMap<String, String>();
        }else{
            mChangedFileNames.clear();
        }
        if(mChangedFileDirs==null){
            mChangedFileDirs=new HashMap<String, String>();
        }else{
            mChangedFileDirs.clear();
        }
        //get the _filenames.xml file (if it exists)
        HashMap<String, String> filenamesFromXml = new HashMap<String, String>();
        File fNamesXmlFile=getXmlFilenamesFile();
        ArrayList<String> filenameTokens = new ArrayList<String>();
        if(fNamesXmlFile.exists()){
            //include the filenames xml file in the list of files to collect tokens from
            mIncludeFiles.add(fNamesXmlFile);
            //get all of the filenames from the xml (if any)
            filenamesFromXml=getXmlFilenameHashValues();
            //get the content from _filenames.xml
            String filenamesContent=readFile(fNamesXmlFile.getPath());
            //if file read was successful
            if(filenamesContent!=null){
                //escape tokens, as needed
                filenamesContent = filenamesContent.replace("\\"+mStartToken, mStartEscToken);
                filenamesContent = filenamesContent.replace("\\"+mEndToken, mEndEscToken);
                //STORE <filename> TOKEN DEFINITIONS THAT CAME FROM _filenames.xml
                //================================================================
                //get the tokens used inside _filenames.xml
                filenameTokens=getTokensFromContent(filenamesContent);
            }
        }
        //for each file
        boolean atLeastOneToken = false;
        for(int f=0;f<mIncludeFiles.size();f++){
            //READ THIS TEMPLATE FILE TO GET ITS CONTENTS
            //===========================================
            String contents="";
            //get the file content
            contents=readFile(mIncludeFiles.get(f).getPath());
            //if file read was successful
            if(contents!=null){
                //if this file has a <filename> in the _filenames.xml file
                String overwriteFilename="";
                if(filenamesFromXml.containsKey(mIncludeFiles.get(f).getPath())){
                    //get the filename token string from inside the <filename> xml element 
                    //(this string is formatted just like a normal token, eg: <<filename:l:path:name>>)
                    String filenameXmlStr=filenamesFromXml.get(mIncludeFiles.get(f).getPath());
                    //if the filename token string is NOT blank
                    if(filenameXmlStr.length()>0){
                        //any filename token in this file will be overwritten by the filename definition in _filenames.xml
                        overwriteFilename=filenameXmlStr;
                    }
                }
                //escape certain string contents 
                contents = contents.replace("\\"+mStartToken, mStartEscToken);
                contents = contents.replace("\\"+mEndToken, mEndEscToken);
                //GET A LIST OF TOKENS (THAT CAN CONTAIN NESTED TOKENS) THAT ARE INSIDE THIS FILE
                //===============================================================================
                //get all of the tokens, eg: <<list>> which may contain nested content. tokenChunks will contain the complete token-chunks
                ArrayList<String> tokenChunks=getTokenChunksFromContent(contents);
                //if there were any token "chunks"
                if(tokenChunks.size()>0){
                    //init the chunk list for this file --> HashMap<[filePath], HashMap<[uniquePlaceholder], [tokenChunkStr]>>
                    HashMap<String, String> placeholderChunks = new HashMap<String, String>();
                    mTokenChunkPlaceholders.put(mIncludeFiles.get(f).getPath(), placeholderChunks);
                    //for each token chunk
                    for(int c=0;c<tokenChunks.size();c++){
                        //get the chunk
                        String chunk=tokenChunks.get(c);
                        //create a placeholder
                        String placeholder=mStartToken+"chunk"+mTokenSeparator+c+mEndToken;
                        //add the chunk/placeholder to the list
                        mTokenChunkPlaceholders.get(mIncludeFiles.get(f).getPath()).put(placeholder, chunk);
                        //remove this chunk from the file content
                        contents=contents.replace(chunk, placeholder);
                    }
                }
                //GET A LIST OF TOKENS THAT ARE INSIDE THIS FILE *** add token chunks to tokens? 
                //==============================================
                //store an array of tokens for this file
                ArrayList<String> tokens=getTokensFromContent(contents);
                //if there is a filename defined in _filenames.xml
                if(overwriteFilename.length()>0){
                    //if _filenames.xml contains any tokens (eg, <<var:l:name>>)
                    if(filenameTokens.size()>0){
                        //ADD TO THE tokens LIST... ADD TOKENS DEFINED INSIDE _filenames.xml (IF ANY)
                        //===========================================================================
                        //for each token inside _filenames.xml
                        for(int t=0;t<filenameTokens.size();t++){
                            String tStr=filenameTokens.get(t);
                            //add the tokens from _filenames.xml to the tokens list
                            tokens.add(tStr+" " + mTokenSourceSeparator + " " + mFilenamesXml);
                        }
                    }
                    //ADD TO THE tokens LIST... ADD THE <filename> DEFINITION FROM _filenames.xml (IF EXISTS)
                    //=======================================================================================
                    //add the <filename> xml node token
                    overwriteFilename+=" " + mTokenSourceSeparator + " " + mFilenamesXml;
                    tokens.add(overwriteFilename);
                    //add to the list of files that have a filename xml overwrite
                    mFilenameXmlOverwriteLookup.put(mIncludeFiles.get(f).getPath(), overwriteFilename);
                }
                //STORE THE CONTENTS OF ONE FILE
                //==============================
                //store the file path and original content
                mFileContentLookup.put(mIncludeFiles.get(f).getPath(), contents);
                //store the file path and tokens
                mFileTokensLookup.put(mIncludeFiles.get(f).getPath(), tokens);
                //if there is at least one token for this file *** 
                if(tokens.size()>0){
                    atLeastOneToken = true;
                    //for each token in this file
                    for(int t=0;t<tokens.size();t++){
                        //get the token name
                        String tName = getTokenPart("name", tokens.get(t));
                        //if the name is NOT blank
                        if(tName.length()>0){
                            //STORE ALL OF THE TOKEN ALIASES (IF ANY) FOR THIS FILE
                            //=====================================================
                            //if this token has an alias
                            String tAlias = getTokenPart("alias", tokens.get(t));
                            if(tAlias.length()>0){
                                String tAliasPath=mIncludeFiles.get(f).getPath();
                                //is this alias coming from _filenames.xml?
                                String tAliasSource = getTokenPart("source", tokens.get(t));
                                if(tAliasSource.equals(mFilenamesXml)){
                                    //set the path of _filenames.xml instead of the template files
                                    tAliasPath=fNamesXmlFile.getPath();
                                }                                
                                //if this file path isn't already a key in mFileAliasesLookup
                                if(!mFileAliasesLookup.containsKey(tAliasPath)){
                                    //create this path-key in HashMap<[tokenAlias], [tokenStr]>
                                    HashMap<String, String> aliasTokenStr = new HashMap<String, String>();
                                    mFileAliasesLookup.put(tAliasPath, aliasTokenStr);
                                }
                                //if this alias isn't already listed for this file
                                if (!mFileAliasesLookup.get(tAliasPath).containsKey(tAlias)){
                                    //add the alias/name to the file's listing
                                    mFileAliasesLookup.get(tAliasPath).put(tAlias, tokens.get(t));
                                }
                            }
                            //STORE THE TOKEN VALUE "OPTIONS" FOR THIS UNIQUE TOKEN NAME
                            //========================================================
                            //if there are defined input value options for this token
                            String tOptionsStr = getTokenPart("options", tokens.get(t));
                            if(tOptionsStr.length()>0){
                                //if this token name doesn't already have any associated options
                                if(!mUniqueTokenNameOptions.containsKey(tName)){
                                    //create the token name as a key in this HashMap
                                    ArrayList<String> options = new ArrayList<String>();
                                    mUniqueTokenNameOptions.put(tName, options);
                                }
                                //for each option
                                String[] tOptionsArray = tOptionsStr.split("\\|");
                                for(int a=0;a<tOptionsArray.length;a++){
                                    //if this option isn't already associated with this token name
                                    String opt=tOptionsArray[a];
                                    if(!mUniqueTokenNameOptions.get(tName).contains(opt)){
                                        //add the association between this token name and value option
                                        mUniqueTokenNameOptions.get(tName).add(opt);
                                    }
                                }
                            }
                            //STORE ALL OF UNIQUE TOKEN NAMES FOR ALL FILES
                            //=============================================   
                            //if this is not a blank token
                            if (!tName.equals(".")){
                                //if this is NOT a literal token, eg: the file name is "hard-coded.txt"
                                if (tName.indexOf("\"") != 0 && tName.indexOf("'") != 0){
                                    //if this token name is not already included in the list
                                    if (!mUniqueTokenNames.contains(tName)){
                                        //add the unique token name, if not already in the list
                                        mUniqueTokenNames.add(tName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return atLeastOneToken;
    }
    //prompt the user for the next input
    public String getInput(String inputLabel){return getInput(inputLabel, null);}
    public String getInput(String inputLabel, ArrayList<String> inputOptions){
         //if the options aren't null
        boolean hasOptions = false;
        if(inputOptions!=null){
            //if there are any option items
            if(inputOptions.size()>0){
                hasOptions=true;
                System.out.print("\n \tChoose option # ");
                System.out.println("\n");
                //for each option
                for(int i=0;i<inputOptions.size();i++){
                    System.out.println(" \t" + i + "  \"" + inputOptions.get(i) + "\"");
                }
                System.out.print("\n " + inputLabel + " >> ");
            }
        }
        //no options (any value)
        if(!hasOptions){
            //prompt for next command
            System.out.print(" " + inputLabel + " >> ");
        }
        String line = "";
        try{
            //accept next input from user
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            line = bufferRead.readLine();
            line=line.trim();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        //if input is based on options...
        if(hasOptions){
            //if no input was given
            if(line.length()<1){line="0";}
            int lineInt=-1;
            String errMsg = "";
            try{
                lineInt=lineInt=Integer.parseInt(line);
            }catch(NumberFormatException e){
                errMsg="\""+line+"\" is not a number. Try again...";
            }
            //if valid number
            if(errMsg.length()<1){
                //if number greater than -1
                if(lineInt>-1){
                    //if number less than the number of items
                    if(lineInt<inputOptions.size()){
                        //set the chosen option
                        line=inputOptions.get(lineInt);
                    }else{
                        //number too high
                        errMsg="\""+line+"\" is too high. Try again...";
                    }
                }else{
                    //number less than 0
                    errMsg="\""+line+"\" is too low. Try again...";
                }
            }
            //if number too high or low
            if(errMsg.length()>0){
                //print error message
                System.out.print("\n " + errMsg + " \n");
                //recursive try again
                line = getInput(inputLabel, inputOptions);
            }else{
                //print the valid selected value
                System.out.println(" \t--> \"" + line + "\" \n");
            }
        }
        return line;
    }
    //get the input from the user for all of the template tokens
    private void getAllTokenInput(ArrayList<String> uniqueTokenNames, int startIndex, boolean isBack){
        /*ASSIGN REAL USER VALUES TO EACH UNIQUE TOKEN NAME
            1) mTokenInputValues
            load a HashMap: HashMap<[tokenName], [userInput]>

            HashMap values come from any file inside the template, including the special _filenames.xml file
                
                your name
                    gmilligan
                your test
                    I am testing
                your something
                    in the way she moves...
                your hi
                    howdy
         */
        String backTxt="<<";
        //if NOT moved back
        if(!isBack){
            //display direction on how to move back
            System.out.println("\n --------------------");
            System.out.println(" DEFINE VARIABLE VALUES");
            System.out.println(" Type \""+backTxt+"\" to back-track... \n");
        }
        //for each input value to enter
        String lastTokenName=null;
        for(int i=startIndex;i<=uniqueTokenNames.size();i++){
            String input="";
            //if NOT entered all of the values
            if(i<uniqueTokenNames.size()){
                //if this token requires specific value options
                ArrayList<String> inputOptions = new ArrayList<String>();
                if(mUniqueTokenNameOptions.containsKey(uniqueTokenNames.get(i))){
                    //get the allowed options
                    inputOptions = mUniqueTokenNameOptions.get(uniqueTokenNames.get(i));
                }
                //if the user is allowed to enter any value
                if(inputOptions.size()<1){
                    //get user input
                    input=getInput("" + (i+1) + "/" + uniqueTokenNames.size() + ") Enter --> \"" +uniqueTokenNames.get(i) + "\"");
                }else{
                    //there are specific option values that are required
                    
                    //get user input
                    input=getInput("" + (i+1) + "/" + uniqueTokenNames.size() + ") Select --> \"" +uniqueTokenNames.get(i) + "\"", inputOptions);
                }
            }else{
                //entered all of the values
                System.out.println("");
                System.out.println(" Target root path --> " + mTargetDir+File.separator+"...");
                input=getInput("Ok, got it. Hit [enter] to build");
            }
            //if the input is the back text
            if(input.equals(backTxt)){
                //print the back message
                System.out.println("\n \tback... \n");
                //make sure the index won't go below zero when backtracking
                if(i==0){i++;}
                //if there was a previous item
                if(lastTokenName!=null){
                    //if the previous value was saved
                    if(mTokenInputValues.containsKey(lastTokenName)){
                       //remove the saved value for the previous item 
                       mTokenInputValues.remove(lastTokenName);
                    }
                }
                //recursive move back
                getAllTokenInput(uniqueTokenNames,i-1,true);
                break;
            }else{
                //if NO already saved all input values
                if(i<uniqueTokenNames.size()){
                    //add this input value to the list
                    mTokenInputValues.put(uniqueTokenNames.get(i), input);
                    //record the last token name to be assigned a value
                    lastTokenName=uniqueTokenNames.get(i);
                }
            }
        }
    }
    //get a list of tokens that have an influence on filename/paths
    //note: the only way for a token to influence a file name or file path is for it to have its alias inside either the name or path
    private ArrayList<String> getTokensInFilenames(){
        /*RETURN A LIST OF ALL OF THE TOKENS (WITHIN THE TEMPLATE) THAT HAVE INFLUENCE OVER ANY FILENAME
        Load an ArrayList<[uniqe token name]>
        
        This is an import list of tokens to have if you want to ask the user 
        ONLY for tokens that distinguish an individual project's files from those belonging to a different project
        
        If a file's name is defined inside _filenames.xml, then ONLY token aliases within _filenames.xml (if any) can have influence for THAT file...
        But a token can control a template file's name if it's NOT defined in _filenames.xml; _filenames.xml definitions overwrite token definitions.
            
            your name
            your test
         */
        ArrayList<String> inFileNameTokens = new ArrayList<String>();
        //for each file that contains tokens
        for (String filePath : mFileTokensLookup.keySet()) {
            String fileAliasPath=filePath;
            String name="";String dir="";
            //GET THE FILENAME TOKEN FROM EITHER _filenames.xml OR WITHIN A TOKEN INSIDE THE FILE
            //if this file's name gets defined in _filenames.xml
            if(mFilenameXmlOverwriteLookup.containsKey(filePath)){
                //the file aliases (used inside the filename) are assigned to _filenames.xml instead of the actual template file
                fileAliasPath=mUseTemplatePath+File.separator+mFilenamesXml;
                //get the token string
                String tokenStr=mFilenameXmlOverwriteLookup.get(filePath);
                String[] tokenParts = tokenStr.split(mTokenSeparator);
                //get the file name
                name=getTokenPart("name", tokenParts);
                //if the name is NOT a string literal (non-string literals CANNOT contain aliases)
                if (name.indexOf("\"") != 0 && name.indexOf("'") != 0){
                    name=""; //no need to check a NON-string literal. It will NOT contain any aliases
                }
                //get the file folder
                dir=getTokenPart("dir", tokenParts);
            }else{
                //TRY TO FIND A FILENAME TOKEN WITHIN THE TEMPLATE FILE... _filenames.xml DOESN'T INFLUENCE THIS TEMPLATE FILE'S NAME...
                //for each token belonging to this file
                ArrayList<String> tokens = mFileTokensLookup.get(filePath);
                for(int t=0;t<tokens.size();t++){
                    //get the token text, eg: <<var:l:something>>
                    String tokenStr=tokens.get(t);
                    String[] tokenParts = tokenStr.split(mTokenSeparator);
                    //get the token alias
                    String tokenType=getTokenPart("type", tokenParts);
                    //if the token type is filename
                    if(tokenType.equals("filename")){
                        //get the file name
                        name=getTokenPart("name", tokenParts);
                        //if the name is NOT a string literal (non-string literals CANNOT contain aliases)
                        if (name.indexOf("\"") != 0 && name.indexOf("'") != 0){
                            name=""; //no need to check a NON-string literal. It will NOT contain any aliases
                        }
                        //get the file folder
                        dir=getTokenPart("dir", tokenParts);
                        //end the looped search for the filename token inside this file
                        break;
                    }
                }
            }
            //CHECK ALL ALIASES TO SEE IF THEY ARE BEING USED IN EITHER THE FILE dir OR FILE name
            //if EITHER the filename directory OR the name is a candidate for possibly containing token aliases
            if(name.length()>0||dir.length()>0){
                //if the file (that defines the filename token) contains any aliases
                if(mFileAliasesLookup.containsKey(fileAliasPath)){
                    //for each alias inside this file (one or more aliases MAY appear inside either the filename "name" or "dir")
                    for (String aliasStr : mFileAliasesLookup.get(fileAliasPath).keySet()) {
                        //get the token name, associated with this alias
                        String nameForAlias=getTokenPart("name", mFileAliasesLookup.get(fileAliasPath).get(aliasStr));
                        //if this token name is NOT ALREADY listed as influencing one or more filename/paths
                        if(!inFileNameTokens.contains(nameForAlias)){
                            boolean aliasInflencesFilename=false;
                            //if the "name" might contain one or more aliases
                            if(name.length()>0){
                                //if this alias string is inside the file "name"
                                if(name.contains(aliasStr)){
                                    aliasInflencesFilename=true;
                                }
                            }
                            //if this alias was NOT found as part of the file "name"
                            if(!aliasInflencesFilename){
                                //if the "dir" might contain one or more aliases
                                if(dir.length()>0){
                                    //if this alias string is inside the file "directory"
                                    if(dir.contains(aliasStr)){
                                        aliasInflencesFilename=true;
                                    }
                                }
                            }
                            //if this alias influences this filename
                            if(aliasInflencesFilename){
                                //add the token name (associated with this alias) to the list
                                //the user will have to input a value for this token in order to determine the file path
                                inFileNameTokens.add(nameForAlias);
                            }
                        }
                    }
                }
            }
        }
        return inFileNameTokens;
    }
    //accept user input ONLY for tokens that effect the file-name/paths
    private ArrayList<String> userInputForFilenameTokens(boolean atLeastOneToken){ 
        //get ONLY the token names that have influence over one or more filenames or paths
        ArrayList<String> inFileNameTokens=getTokensInFilenames();
        //if there is at least one token
        if(inFileNameTokens.size()>0){
            System.out.println(" Which project would you like to export? \n");
            //if more than one token value to input
            if(inFileNameTokens.size()>1){
                System.out.println(" "+inFileNameTokens.size()+" unique token values needed to identify your project: ");
            }else{
               //only one token value to input 
               System.out.println(" only ONE token value needed to identify your project: ");
            }
            //for each token value to input
            System.out.print(" ");
            for(int v=0;v<inFileNameTokens.size();v++){
                //if NOT the first unique token name
                if(v!=0){
                    System.out.print(", ");
                }
                //print the token name
                System.out.print("\""+inFileNameTokens.get(v)+"\"");
            }
            System.out.println("");
            //get all of the token input values from the user
            getAllTokenInput(inFileNameTokens,0,false);System.out.println("");
        }
        return inFileNameTokens;
    }
    //accept user input for each of the unique tokens
    private void userInputForTokens(boolean atLeastOneToken){
        //if there is at least one token
        if(mUniqueTokenNames.size()>0){
            //if more than one token value to input
            if(mUniqueTokenNames.size()>1){
                System.out.println(" "+mUniqueTokenNames.size()+" unique token values to input: ");
            }else{
               //only one token value to input 
               System.out.println(" only ONE token value to input: ");
            }
            //for each token value to input
            System.out.print(" ");
            for(int v=0;v<mUniqueTokenNames.size();v++){
                //if NOT the first unique token name
                if(v!=0){
                    System.out.print(", ");
                }
                //print the token name
                System.out.print("\""+mUniqueTokenNames.get(v)+"\"");
            }
            System.out.println("\n");
            //get the list tokens/continue choice 
            String lsOrContinue=getInput("\"ls\"=list files/tokens, [enter]=continue");
            //if the user chose to view the file / token listing
            if(lsOrContinue.toLowerCase().equals("ls")){
                //for each file (that contains at least one token)
                for (String path : mFileTokensLookup.keySet()) {
                    File file=new File(path);
                    //print the file name and number of tokens
                    ArrayList<String> fileTokens = mFileTokensLookup.get(path);
                    System.out.println("\n "+file.getName()+" >> "+fileTokens.size()+" token(s) \n");
                    //for each token in this file
                    for(int t=0;t<fileTokens.size();t++){
                        //print the token text
                        System.out.println(" \t"+fileTokens.get(t).trim());
                    }
                }
            }
            //get all of the token input values from the user
            getAllTokenInput(mUniqueTokenNames,0,false);System.out.println("");
        }else{
            //no token values to input
            System.out.println(" ZERO unique token values to input: ");
        }
    }
    //formats the value based on token parameters and input value, eg: decides the casing to apply to the user input
    private String getFormattedTokenValue(String[] tokenParts){return getFormattedTokenValue(tokenParts, null);}
    private String getFormattedTokenValue(String[] tokenParts, ArrayList<String> inFileNameTokens){
        String tokenValue = "";
        //get the token name
        String tokenName=getTokenPart("name", tokenParts);
        //if inFileNameTokens is NOT null
        boolean doGetVal=true;
        if(inFileNameTokens!=null){
            //if this token name SHOULD be ignored because it is NOT listed in inFileNameTokens
            if(!inFileNameTokens.contains(tokenName)){
                doGetVal=false;
            }
        }
        //if this token name is NOT being ignored because it is NOT listed in inFileNameTokens
        if(doGetVal){
            //get the token type
            String type=getTokenPart("type", tokenParts);
            //get the token casing
            String casing=getTokenPart("casing", tokenParts);
            //if not a blank tokenName, represented by a dot, . AND not a static value surrounded by "quotes"
            if (!tokenName.equals(".") && tokenName.indexOf("\"") != 0 && tokenName.indexOf("'") != 0){
                //get the token value... the value is formatted based on the different token parts, eg: casing
                tokenValue = mTokenInputValues.get(tokenName);
                //apply the casing formatting
                tokenValue = getAppliedCasing(casing, tokenValue);
            }
        }
        return tokenValue;
    }
    private String getAppliedCasing(String casing, String strVal){
        //get the first letter of the casing 
        String firstCharCasing = casing.trim().toLowerCase();
        firstCharCasing = firstCharCasing.substring(0, 1);
        //default casing
        casing = "normal";
        //standardized what casing is assigned based on the first letter 
        //(for code-readability... no other reason)
        switch (firstCharCasing){
            case "u":
                casing = "uppercase";
                break;
            case "l":
                casing = "lowercase";
                break;
            case "c":
                casing = "capitalize";
                break;
            default:
                break;
        }
        //format depending on casing
        switch (casing){
            case "uppercase":
                strVal = strVal.toUpperCase();
                break;
            case "lowercase":
                strVal = strVal.toLowerCase();
                break;
            case "capitalize":
                String firstChar = strVal.substring(0, 1);
                String theRest = strVal.substring(1);
                firstChar = firstChar.toUpperCase();
                strVal = firstChar + theRest;
                break;
            case "normal":
                //yep... do nothing. Leave as is
                break;
            default:
                break;
        }
        return strVal;
    }
    //replace the aliases inside fileContent with their associated value (if the alias is inside fileContent)
    private String getReplacedAliases(String fileContent, HashMap<String, String> aliasValueLookup)
    {
        //if there are any aliases
        if (aliasValueLookup.size()>0){
            //if there is any file content
            if (fileContent.trim().length() > 0){
                //for each alias
                for (String aliasKey : aliasValueLookup.keySet()) {
                    //if the file content contains this alias
                    if (fileContent.contains(aliasKey)){
                        //replace this alias with the value inside file content
                        fileContent = fileContent.replace(aliasKey, aliasValueLookup.get(aliasKey));
                    }
                }
            }
        }
        return fileContent;
    }
    //determine if the default file name / path should be changed based on the given token
    private void setChangedFilenameForFile(String filePath, String[] tokenParts, String tokenValue, HashMap<String, String> aliasValueLookup){
        //if this file doesn't already have a designated changed name
        if (!mChangedFileNames.containsKey(filePath)){
            //if there is a specified file name (other than the existing template file's name)
            if (!tokenValue.equals("") && !tokenValue.equals(".")){
                //replace any aliases with real values that may be inside the filename
                tokenValue = getReplacedAliases(tokenValue, aliasValueLookup);
                //set the new name of the file
                mChangedFileNames.put(filePath, tokenValue);
            }else{
                //no specified file name...

                String tokenName = getTokenPart("name", tokenParts);
                //if the file name was literally hard-coded (surrounded by "quotes")
                if (tokenName.indexOf("\"") == 0 || tokenName.indexOf("'") == 0){
                    //set the static filename value surrounded by "quotes"
                    tokenValue = tokenName;
                    //strip off starting quote
                    tokenValue = tokenValue.substring(1);
                    //strip off ending quote
                    tokenValue = tokenValue.substring(0, tokenValue.length() - 1);
                    //trim
                    tokenValue = tokenValue.trim();
                    //if the literal file name value is not blank
                    if (tokenValue.length() > 0){
                        //replace any aliases with real values that may be inside the filename
                        tokenValue = getReplacedAliases(tokenValue, aliasValueLookup);
                        //set the static name of the file
                        mChangedFileNames.put(filePath, tokenValue);
                    }
                }
            }
        }
        //since this token specifies a filename, it may also specify the root directory for the file (if not, changedFileDir = "")...
        //if this file doesn't already have a designated changed sub directory
        if (!mChangedFileDirs.containsKey(filePath)){
            //if there is a specified directory in the tokenParts
            String changedDir = getTokenPart("dir", tokenParts);
            if (!changedDir.equals("") && !changedDir.equals(".")){
                //replace any aliases with real values that may be inside the directory
                changedDir = getReplacedAliases(changedDir, aliasValueLookup);
                //set the new sub directory of the file
                mChangedFileDirs.put(filePath, changedDir);
            }
        }
    }
    //build out the template files
    private void setTokenValues(){setTokenValues(null);} 
    private void setTokenValues(ArrayList<String> inFileNameTokens){
        /*LOOP EACH FILE A SECOND TIME
        1) remove alias definitions from file content and determine what value each alias should be replaced with
            Replace substring tokens in mFileContentLookup
            with the actual values in mTokenInputValues
        2) replace non-aliased tokens with their actual values
        3) determine the file-path and file name that each file will have
        ============================
        IF inFileNameTokens is NOT null, then only replace the tokens that are named in this inFileNameTokens list
        */        
        
        //for each template file
        for (String filePath : mFileTokensLookup.keySet()) {
            //get key values related to this file
            String fileContent=mFileContentLookup.get(filePath);
            //if this file is NOT a non-text file, eg: an image
            ArrayList<String> tokens=mFileTokensLookup.get(filePath);
            /*tokens:
                ../config.xml
                    <<var:l:your hi>> --> _filenames.xml
                    <<var:u:your something => [something]>>
                ../otherfile.txt
                    <<filename:n:my/path:"newname">>
                    <<filename:n:my/path:"overwrite">> --> _filenames.xml
                    <<var:u:another thing => [yep]>>
             */
            //GET FORMATTED VALUES FOR ALIASES AND REMOVE ALIAS DECLARATIONS FROM FILE CONTENTS
            //=================================================================================
            //if there are any aliased tokens in this file,
            //side-note: ONLY text-based files might have associated aliases. Non-text files, like images, will never have any items in mFileAliasesLookup ...
            HashMap<String, String> aliasValueLookup=new HashMap<String, String>();
            if(mFileAliasesLookup.containsKey(filePath)){
                //for each alias declaration inside the file
                for (String aliasKey : mFileAliasesLookup.get(filePath).keySet()) {
                    //if the alias doesn't already have an associated formatted value
                    if(!aliasValueLookup.containsKey(aliasKey)){
                        //REMOVE ALIAS DEFINITION FROM THE CONTENT
                        //get the token string for this alias
                        String tokenStr = mFileAliasesLookup.get(filePath).get(aliasKey);
                        //remove the tokenStr (alias variable declaration) from fileContent
                        fileContent = fileContent.replace(tokenStr, "");
                        //STORE THE VALUE ASSIGNED TO THE ALIAS (THAT WILL REPLACE INSTANCES OF THE ALIAS THROUGHOUT THE FILE)
                        //split the token key parts up
                        String[] tokenParts = tokenStr.split(mTokenSeparator);
                        //get the formatted token value
                        String tokenValue = getFormattedTokenValue(tokenParts,inFileNameTokens);
                        //associate this value with this alias (only for this file)
                        aliasValueLookup.put(aliasKey, tokenValue);
                    }
                }
            }
            //SET THE OVERRIDING FILENAME FROM _filenames.xml, IF THIS FILE HAS SUCH AN OVERRIDE
            //==================================================================================
            //if this file has a filename defined in _filenames.xml
            boolean filenameInXml=false;
            if(mFilenameXmlOverwriteLookup.containsKey(filePath)){
                String tokenStr=mFilenameXmlOverwriteLookup.get(filePath);
                //split the token key parts up
                String[] tokenParts=tokenStr.split(mTokenSeparator);
                //get the formatted token value
                String tokenValue=getFormattedTokenValue(tokenParts,inFileNameTokens);
                //save the changed file name or directory, if either dir or name should be changed
                setChangedFilenameForFile(filePath, tokenParts, tokenValue, aliasValueLookup);
                filenameInXml=true;
            }
            //LOOP THROUGH ALL OF THE NON-ALIASED TOKENS IN THIS FILE
            //=======================================================
            //for each token in the file
            for(int t=0;t<tokens.size();t++){
                //FORMAT THE TOKEN VALUE DEPENDING ON IT'S PARAMETERS
                //get the token key, eg: <<type:casing:name>>
                String tokenStr=tokens.get(t);
                //split the token key parts up
                String[] tokenParts=tokenStr.split(mTokenSeparator);
                //get the formatted token value
                String tokenValue=getFormattedTokenValue(tokenParts,inFileNameTokens);
                //INSERT THE FORMATTED TOKEN VALUE INTO THE CONTENT (OR SET IT AS A SPECIAL VALUE, LIKE FILENAME, ETC...)
                //=======================================================================================================
                //get the token name
                String tokenName=getTokenPart("name", tokenParts);
                //get the token type
                String type=getTokenPart("type", tokenParts);
                switch (type){
                    case "var":
                        //replace the tokens with the actual values
                        fileContent=fileContent.replace(tokenStr, tokenValue);
                        break;
                    case "filename": 
                        //remove these tokens from the file content
                        fileContent=fileContent.replace(tokenStr, "");
                        //if the filename is NOT already defined in _filenames.xml
                        if(!filenameInXml){
                            //save the changed file name or directory, if either dir or name should be changed
                            setChangedFilenameForFile(filePath, tokenParts, tokenValue, aliasValueLookup);
                        }
                        break;
                    default:
                        break;
                }
                //replace any aliases with real values that may be inside the fileContent
                fileContent=getReplacedAliases(fileContent, aliasValueLookup);
                //set the modified content into the lookup
                mFileContentLookup.remove(filePath);
                mFileContentLookup.put(filePath, fileContent);
            }
        }
    }
    //write the output files
    private String writeOutputFiles(){return writeOutputFiles("build");} 
    private String writeOutputFiles(String writeType){
        //if exporting a completed project file-set
        String exportDir=""; String failedExportDir="";
        if(writeType.equals("export")){
            //CREATE A UNIQUE FAILED EXPORT FOLDER NAME, JUST IN CASE
            //get a root directory path to export to
            failedExportDir=mTargetDir + File.separator + "DELETE_THIS_FAILED_EXPORT";
            //if this export directory already exists
            if(new File(failedExportDir).exists()){
                int dirIndex = 2;
                //while this directory PLUS the index exists
                while(new File(failedExportDir + dirIndex).exists()){
                    //increase the index
                    dirIndex++;
                }
                //modify the export directory name so that it's unique
                failedExportDir+=dirIndex;
            }
            //REAL EXPORT DIRECTORY
            //get a root directory path to export to
            exportDir=mTargetDir + File.separator + "export";
            //if this export directory already exists
            if(new File(exportDir).exists()){
                int dirIndex = 2;
                //while this directory PLUS the index exists
                while(new File(exportDir + dirIndex).exists()){
                    //increase the index
                    dirIndex++;
                }
                //modify the export directory name so that it's unique
                exportDir+=dirIndex;
            }
            //create the export directory
            File exportDirFile=new File(exportDir);
            try{
                //try to create this directory
                exportDirFile.mkdir();
            }catch(SecurityException se){
                System.out.println("\n Uh oh... failed to create directory --> "+exportDir);
                se.printStackTrace();
            }
        }
        //LOOP EACH FILE A FINAL THIRD TIME
        //1) create a new file based on the corresponding template file (the new file will have real input values instead of tokens)
        //=================================
        //for each file to create
        int fileCount = 0; int skippedFileCount = 0; int errFileCount = 0;
        for (String filePath : mFileContentLookup.keySet()) {
            String fileName = "";File templateFile=new File(filePath);
            //if this file doesn't start with _, eg: _filenames.xml
            if(templateFile.getName().indexOf("_")!=0){
                //if changing the file name
                if (mChangedFileNames.containsKey(filePath)){
                    //get just the file extension
                    String fileExt = templateFile.getName();
                    if (fileExt.indexOf(".") != -1){
                        //remove file name from the extension
                        fileExt = fileExt.substring(fileExt.lastIndexOf("."));
                        //add file extension to file name
                        fileName = mChangedFileNames.get(filePath) + fileExt;
                    }else{fileExt="";}
                }
                else //use same filename as the original template file
                {
                    //get just the filename with no path
                    fileName = templateFile.getName();
                }
                //if changing the file directory (under the current project directory)
                String changedFileDir = "";
                if (mChangedFileDirs.containsKey(filePath)){
                    changedFileDir = mChangedFileDirs.get(filePath);
                    //make sure each directory exists... create them if they don't
                    changedFileDir=changedFileDir.replace(File.separator, "/");
                    String[] dirs = changedFileDir.split("/");
                    String currentDir = "";
                    //depending on the write type...
                    switch(writeType){
                        case "build": //if building a boiler-plate template files...
                            currentDir = mTargetDir + File.separator;
                        break;
                        case "export": //if exporting a completed project files...
                            currentDir = exportDir + File.separator;
                        break;
                    }
                    //MAKE SURE EACH OUTPUT DIRECTORY IS CREATED IF IT DOESN'T ALREADY EXIST
                    //for each sub directory (that may need to be created)
                    for (int d=0;d<dirs.length;d++){
                        //if this directory doesn't exist
                        currentDir += dirs[d].trim() + File.separator;
                        File dirFile=new File(currentDir);
                        if (!dirFile.exists()){
                            try{
                                //try to create this directory
                                dirFile.mkdir();
                            }catch(SecurityException se){
                                System.out.println("\n Uh oh... failed to create directory --> "+currentDir);
                                se.printStackTrace();
                                break;
                            }
                        }
                    }
                    //append the final \\ at the end of the directory path
                    changedFileDir += File.separator;
                }
                //set the ouput file path (relative to the current directory)
                String outputFilePath = File.separator + changedFileDir + fileName;
                //depending on the write type...
                switch(writeType){
                    case "build": //if building a boiler-plate template file...
                        //set the target directory as the root of the output file path
                        outputFilePath = mTargetDir + outputFilePath;
                        //get the new file content
                        String fileContent = mFileContentLookup.get(filePath);
                        File newFile=new File(outputFilePath);
                        //if the new file doesn't already exist
                        if (!newFile.exists()){
                            //if the file content is NOT blank
                            fileContent = fileContent.trim();
                            if (fileContent.length() > 0){
                                //create the file with its content (maybe changed or maybe not changed and just copied over)
                                boolean success=false;
                                //if this is a NON-text file, eg and image
                                if(fileContent.equals(mNonTextFileContent)){
                                    //copy the NON text file to the build location
                                    success=copyFileTo(new File(mUseTemplatePath + File.separator + new File(filePath).getName()), newFile);
                                }else{
                                    //this IS a text-based file...
                                    //restore certain string contents
                                    fileContent = fileContent.replace(mStartEscToken, mStartToken);
                                    fileContent = fileContent.replace(mEndEscToken, mEndToken);
                                    //write the token-replaced file content
                                    success=writeFile(newFile.getPath(), fileContent);
                                }
                                if(success){
                                    System.out.println(" FILE CREATED: \t" + "..."+newFile.getPath().substring(mTargetDir.length()+1));
                                    fileCount++;
                                }else{
                                    errFileCount++;
                                }
                            }
                            else
                            {
                                System.out.println(" FILE SKIP (BLANK): \t" + "..."+newFile.getPath().substring(mTargetDir.length()+1));
                                skippedFileCount++;
                            }
                        }
                        else
                        {
                            System.out.println(" FILE SKIP (ALREADY EXISTS): \t" + "..."+newFile.getPath().substring(mTargetDir.length()+1));
                            skippedFileCount++;
                        }
                    break;
                    case "export": //if exporting a completed project file...
                        //normalize file path separators
                        outputFilePath=outputFilePath.replace("\\", "/");
                        outputFilePath=outputFilePath.replace("///", "/");
                        outputFilePath=outputFilePath.replace("//", "/");
                        outputFilePath=outputFilePath.replace("/", File.separator);
                        //get the file to export
                        File exportFile=new File(mTargetDir + outputFilePath);
                        //if the new file already exists
                        if (exportFile.exists()){
                            //try to get the file content
                            String exportContent = ""; boolean errorReading=false;
                            exportContent = readFile(exportFile.getPath());
                            if(exportContent==null){
                                errorReading=true;
                                errFileCount++;
                            }
                            //if the file could be read
                            if(!errorReading){
                                //if the file content is NOT blank
                                if (exportContent.length() > 0){
                                    //make a copy of the export file under the exportDir root
                                    boolean success=copyFileTo(exportFile, new File(exportDir + outputFilePath));
                                    if(success){
                                        System.out.println(" FILE EXPORTED: \t" + "..."+(exportDir + outputFilePath).substring(mTargetDir.length()+1));
                                        fileCount++;
                                    }else{
                                        errFileCount++;
                                    }
                                }
                                else
                                {
                                    System.out.println(" FILE SKIP (BLANK): \t" + "..."+exportFile.getPath().substring(mTargetDir.length()+1));
                                    skippedFileCount++;
                                }
                            }
                        }
                        else
                        {
                            System.out.println(" FILE SKIP (DOES NOT EXIST): \t" + "..."+exportFile.getPath().substring(mTargetDir.length()+1));
                            skippedFileCount++;
                        }
                    break;
                }
            }
        }
        //FILE WRITING COMPLETE... PRINT STATUS MESSAGE
        System.out.println("\n -------------------------------------------------------");
        System.out.println(" Done.\n Created files: (" + fileCount + ") \n Skipped files: (" + skippedFileCount + ") \n Error files: (" + errFileCount + ") \n ");
        //ADDITIONAL INPUT FOR EXPORT OPERATIONS
        //if exported project files
        if(writeType.equals("export")){
            //if any files were successfully exported
            if(fileCount > 0){
                //get just the export folder name (without the full path)
                String exportFolder=exportDir;
                if(exportFolder.contains(File.separator)){
                    exportFolder=exportFolder.substring(exportFolder.lastIndexOf(File.separator)+File.separator.length());
                }
                //get the new export directory file name
                String newName = getInput("Rename the \"" + exportFolder + "\" root folder by typing a new name... \n OR hit [enter] to keep this name");
                //if a new folder name was given
                newName=newName.trim();
                if(newName.length()>0){
                    System.out.println(" Renaming: \"" + exportFolder + "\" --> \"" + newName + "\" \n");
                    //rename the export directory
                    File existingDir = new File(exportDir);
                    File renamedDir = new File(existingDir.getParent() + File.separator + newName);
                    //if this folder doesn't already exist
                    if(!renamedDir.exists()){
                        //rename
                        existingDir.renameTo(renamedDir);
                        exportDir=renamedDir.getPath();
                    }else{
                        //already exists message
                        System.out.println(" No. \"" + newName + "\" already exists. \n");
                    }
                }
            }else{
                //no files were successfully exported... 
                
                //remove the exportDir
                File emptyDir = new File(exportDir);
                //make sure the failed export folder path is still unique
                String indexStr="";int index=2;
                while(new File(failedExportDir+indexStr).exists()){
                    indexStr=index+"";
                    index++;
                }
                failedExportDir+=indexStr;
                //rename the export directory so that it's obvious that it's a failed export
                emptyDir.renameTo(new File(failedExportDir));
                //display failer message
                System.out.println(" No files were successfully exported.");
                System.out.println(" Note: This app renamed the failed export folder. You can manually delete this folder:");
                System.out.println(" " + failedExportDir + "\n\n");
                //clear the export directory path since the export failed
                exportDir="";
            }
        }
        return exportDir;
    }
}
