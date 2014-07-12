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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author gmilligan
 */
public class BuildTemplate {
    //fields 
    private static HashMap<String, String> mFileContentLookup; //HashMap<[filePath], [fileContent]>
    private static HashMap<String, ArrayList<String>> mFileTokensLookup; //HashMap<[filePath], ArrayList<[tokenItemText]>>
    private static HashMap<String, HashMap<String, String>> mFileAliasesLookup; //HashMap<[filePath], HashMap<[tokenAlias], [tokenStr]>>
    private static ArrayList<String> mUniqueTokenNames; //ArrayList<[tokenName]> each token name only appears once
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
    private static final String mAliasSetter="=>";
    private static final String mStartEscToken="|_-+StrtToKen..=!_|";
    private static final String mEndEscToken="|_--eNdToKen..=!_|";
    private static final String mNonTextFileContent="|_--nOT@tXTfiLE..=!_|~~JUB123eZ55_-CoO__|"; //unique text content to use as a non-text file content placeholder
    private static final String mAppResDir="res"; //root folder name of resources packaged inside the .jar app
    private static final String mAppResXmlDir="xml";
    private final static String mFilenamesXml = "_filenames.xml"; //the filename where non-text files (eg: images) can have their output paths defined
    //constructor
    public BuildTemplate(String targetDir, String batchFileName, String templatesRoot){
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
    //get a list of rename values from _filenames.xml 
    //and remove any <rename> node that is pointing at nothing OR a file that doesn't exist 
    private HashMap<String, String> getXmlRenameHashValues(){
        HashMap<String, String> renameList = new HashMap<String, String>();
        boolean xmlChangesMade=false;
        //get the _filenames.xml file (if it already exists)
        File fnXmlFile=new File(mUseTemplatePath+File.separator+mFilenamesXml);
        if(fnXmlFile.exists()){
            //get the XML document object
            Document xmlDoc=getXmlDoc(fnXmlFile);
            if(xmlDoc!=null){
                //get the document root
                Element root=xmlDoc.getDocumentElement();
                //loop through each <rename> node inside the root node
                NodeList renameNodes = root.getChildNodes();
                for (int r=0;r<renameNodes.getLength();r++){
                    //if this child node is an element (not a text node)
                    Node renameNode=renameNodes.item(r);
                    if (renameNode.getNodeType()==Node.ELEMENT_NODE) {
                        //if this child node is a "rename" node
                        if(renameNode.getNodeName().toLowerCase().equals("rename")){
                            boolean removeNode=true;
                            //if there is a name attribute
                            Node nameAttr=renameNode.getAttributes().getNamedItem("name");
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
                                            //get the <rename> node inner text
                                            renameNode.normalize();
                                            String nodeText=renameNode.getNodeValue();
                                            if(nodeText!=null){
                                                //if the node text is NOT blank
                                                nodeText=nodeText.trim();
                                                if(nodeText.length()>0){
                                                    //create the full token text
                                                    nodeText=mStartToken+"filename"+mTokenSeparator+nodeText+mEndToken;
                                                    //add the token string to the map list
                                                    renameList.put(filePath, nodeText);
                                                }
                                            }
                                        }
                                    }else{
                                        //filePath already a key in the list...
                                        removeNode=false;
                                    }
                                }
                            }
                            //if this node should be removed
                            if(removeNode){
                                //remove this <rename> node
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
        //get the _filenames.xml file (if it already exists)
        File fnXmlFile=new File(mUseTemplatePath+File.separator+mFilenamesXml);
        //HashMap<[filePathInTemplate], [filenameTokenTxt]>
        HashMap<String, String> renameNodeList = new HashMap<String, String>();
        //if _filenames.xml does NOT exist
        if(!fnXmlFile.exists()){
            //get boilerplate content for _filenames.xml
            String xmlStr=getFilenamesXmlStr();
            //create _filenames.xml
            writeFile(fnXmlFile.getPath(),xmlStr);
        }else{
            //_filenames.xml already exists...
            
            //get the HashMaps of rename values
            //HashMap<[filePathInTemplate], [filenameTokenTxt]>
            renameNodeList = getXmlRenameHashValues();
        }
        //get the XML document object
        Document xmlDoc=getXmlDoc(fnXmlFile);
        if(xmlDoc!=null){
            //loop through each file inside useTemplatePath folder and add the <rename> node to xmlDoc, if it's not already there
            //***
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
    public static String readFile(String path) throws IOException {
        return readFile(path, StandardCharsets.UTF_8);
    }
    //read the contents of a file into a string
    public static String readFile(String path, Charset encoding) throws IOException {
        String str="";
        //if this is a text based file, eg: not an image file
        if(isTextBasedFile(new File(path))){
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            str=new String(encoded, encoding);
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
        }
        return returnStr;
    }
    //look through all of the mIncludeFiles and load the content/token/alias hash lookups for each file
    private boolean loadFilesData(){
        //LOOP EACH FILE THE FIRST TIME
        //1) load a HashMap; HashMap<[filePath], [fileContent]>
        //2) load a HashMap; HashMap<[filePath], ArrayList<[tokenItemText]>> ... get tokens (if any)
        //3) load a HashMap; HashMap<[filePath], HashMap<[tokenAlias], [tokenStr]>> ... get token-aliases (if any)
        //4) load an ArrayList; ArrayList<[tokenName]> where each token name only appears once
        //=============================
        //clear/init for new data
        if(mFileContentLookup==null){
            mFileContentLookup=new HashMap<String, String>();
        }else{
            mFileContentLookup.clear();
        }
        if(mFileTokensLookup==null){
            mFileTokensLookup=new HashMap<String, ArrayList<String>>();
        }else{
            mFileTokensLookup.clear();
        }
        if(mFileAliasesLookup==null){
            mFileAliasesLookup=new HashMap<String, HashMap<String, String>>();
        }else{
            mFileAliasesLookup.clear();
        }
        if(mUniqueTokenNames==null){
            mUniqueTokenNames=new ArrayList<String>();
        }else{
            mUniqueTokenNames.clear();
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
        boolean atLeastOneToken = false;
        //for each file
        for(int f=0;f<mIncludeFiles.size();f++){
            //STORE ORIGINAL FILE CONTENTS AND GET AN ARRAY OF TOKENS FOR THIS FILE
            //=====================================================================
            String contents=""; boolean fileReadErr=false;
            try{
                //get the file content
                contents=readFile(mIncludeFiles.get(f).getPath());
            }catch (IOException errread){
                fileReadErr=true;
                System.out.println("\nERROR READING FILE: \n"+mIncludeFiles.get(f).getPath()+" \n...\n"+errread.getMessage()+"\n");
            }
            //if file read was successful
            if(!fileReadErr){
                //escape certain string contents
                contents = contents.replace("\\"+mStartToken, mStartEscToken);
                contents = contents.replace("\\"+mEndToken, mEndEscToken);
                //store the file path and original content
                mFileContentLookup.put(mIncludeFiles.get(f).getPath(), contents);
                //store an array of tokens for this file
                ArrayList<String> tokens=getTokensFromContent(contents);
                mFileTokensLookup.put(mIncludeFiles.get(f).getPath(), tokens);
                //if there is at least one token for this file
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
                                //if this file path isn't already a key in mFileAliasesLookup
                                if(!mFileAliasesLookup.containsKey(mIncludeFiles.get(f).getPath())){
                                    //create this path-key in HashMap<[tokenAlias], [tokenStr]>
                                    HashMap<String, String> aliasTokenStr = new HashMap<String, String>();
                                    mFileAliasesLookup.put(mIncludeFiles.get(f).getPath(), aliasTokenStr);
                                }
                                //if this alias isn't already listed for this file
                                if (!mFileAliasesLookup.get(mIncludeFiles.get(f).getPath()).containsKey(tAlias)){
                                    //add the alias/name to the file's listing
                                    mFileAliasesLookup.get(mIncludeFiles.get(f).getPath()).put(tAlias, tokens.get(t));
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
    public String getInput(String inputLabel){
        //prompt for next command
        System.out.print(" " + inputLabel + " >> ");
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
        return line;
    }
    //get the input from the user for all of the template tokens
    private void getAllTokenInput(ArrayList<String> uniqueTokenNames, int startIndex, boolean isBack){
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
                //get user input
                input=getInput("" + (i+1) + "/" + uniqueTokenNames.size() + ") Enter --> \"" +uniqueTokenNames.get(i) + "\"");
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
        ArrayList<String> inFileNameTokens = new ArrayList<String>();
        //for each file that contains tokens
        for (String filePath : mFileTokensLookup.keySet()) {
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
                    String name=getTokenPart("name", tokenParts);
                    //if the name is NOT a string literal (non-string literals CANNOT contain aliases)
                    if (name.indexOf("\"") != 0 && name.indexOf("'") != 0){
                        name=""; //no need to check a NON-string literal. It will NOT contain any aliases
                    }
                    //get the file folder
                    String dir=getTokenPart("dir", tokenParts);
                    //if EITHER the filename directory OR the name is a candidate for possibly containing token aliases
                    if(name.length()>0||dir.length()>0){
                        //for each alias inside this file (one or more aliases MAY appear inside either the filename "name" or "dir")
                        for (String aliasStr : mFileAliasesLookup.get(filePath).keySet()) {
                            //get the token name, associated with this alias
                            String nameForAlias=getTokenPart("name", mFileAliasesLookup.get(filePath).get(aliasStr));
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
                        tokenValue = tokenValue.toUpperCase();
                        break;
                    case "lowercase":
                        tokenValue = tokenValue.toLowerCase();
                        break;
                    case "capitalize":
                        String firstChar = tokenValue.substring(0, 1);
                        String theRest = tokenValue.substring(1);
                        firstChar = firstChar.toUpperCase();
                        tokenValue = firstChar + theRest;
                        break;
                    case "normal":
                        //yep... do nothing. Leave as is
                        break;
                    default:
                        break;
                }
            }
        }
        return tokenValue;
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
    //build out the template files
    private void setTokenValues(){setTokenValues(null);}
    private void setTokenValues(ArrayList<String> inFileNameTokens){
        //LOOP EACH FILE A SECOND TIME
        //1) remove alias definitions from file content and determine what value each alias should be replaced with
        //2) replace non-aliased tokens with their actual values
        //3) determine the file-path and file name that each file will have
        //============================
        //IF inFileNameTokens is NOT null, then only replace the tokens that are named in this inFileNameTokens list
        //for each template file
        for (String filePath : mFileTokensLookup.keySet()) {
            //get key values related to this file
            String fileContent=mFileContentLookup.get(filePath);
            ArrayList<String> tokens=mFileTokensLookup.get(filePath);
            //GET FORMATTED VALUES FOR ALIASES AND REMOVE ALIAS DECLARATIONS FROM FILE CONTENTS
            //=================================================================================
            //if there are any aliased tokens in this file
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
            //LOOP THROUGH ALL OF THE NON-ALIASED TOKENS IN THIS FILE
            //=======================================================
            //for each token in the file
            for(int t=0;t<tokens.size();t++){
                //FORMAT THE TOKEN VALUE DEPENDING ON IT'S PARAMETERS
                //get the token key, eg: <<type:casing:name>>
                String tokenKey=tokens.get(t);
                //split the token key parts up
                String[] tokenParts=tokenKey.split(mTokenSeparator);
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
                        fileContent=fileContent.replace(tokenKey, tokenValue);
                        break;
                    case "filename": 
                        //remove these tokens from the file content
                        fileContent=fileContent.replace(tokenKey, "");
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
                                success=copyFileTo(new File(mUseTemplatePath + File.separator + newFile.getName()), newFile);
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
                        try{
                            exportContent = readFile(exportFile.getPath());
                        }catch (IOException errread){
                            errorReading=true;
                            System.out.println("\nERROR READING FILE: \n"+exportFile.getPath()+" \n...\n"+errread.getMessage()+"\n");
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
        System.out.println("\n -------------------------------------------------------");
        System.out.println(" Done.\n Created files: (" + fileCount + ") \n Skipped files: (" + skippedFileCount + ") \n Error files: (" + errFileCount + ") \n ");
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
