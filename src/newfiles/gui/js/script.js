function getTestInBrowser(){return false;} //true = test outside of Java, in a browser ***
jQuery(document).ready(function(){
    //==GET KEY ELEMENTS==
    var bodyElem=jQuery('body:first');
    var menuBarWrap=bodyElem.children('#menu-bar:first');
    var contentWrap=bodyElem.children('#content:first');
    var templatesWrap=contentWrap.children('#templates:first');
    var temHeaderWrap=templatesWrap.children('header:first');
    var temContentWrap=templatesWrap.children('.content:last');
    var temLsWrap=temContentWrap.children('nav.ls:first');
    var workspaceWrap=contentWrap.children('#workspace:first');
    var inputViewWrap=workspaceWrap.children('#input-view:last');
    var mainViewWrap=workspaceWrap.children('#main-view:first');
    var treeViewContent=mainViewWrap.children('.content[name="tree-view"]:first');
    var treeViewSearchInput=treeViewContent.find('.search[name="search-tree-view"] input:first');
    var treeViewWrap=treeViewContent.find('#tree-view:first');
    var projectIdsWrap=treeViewContent.find('.rcol .col-content .block-wrap.project-ids:first');
    var fileViewContent=mainViewWrap.children('.content[name="file-view"]:first');
    var mainTitleElem=mainViewWrap.find('header .title h1:first');
    var mainViewTabs=mainViewWrap.find('header .tabs:first');
    var mainViewFilesBar=mainViewWrap.find('header .template-files:first');
    var prevNextFileBtns=mainViewFilesBar.children('.prev-next:first');
    var fileDropdownsWrap=mainViewFilesBar.children('.dropdown:first');
    var inputResizeHandle=inputViewWrap.children('.resize.height:last');
    var temResizeHandle=templatesWrap.children('.resize.width:last');
    //disable selection on certain elements
    preventSelect(temLsWrap); preventSelect(mainViewTabs); preventSelect(prevNextFileBtns);
    //==INTERNAL FUNCTIONS==
    //function to save inline style rules, remove them, but allow them to be restored later
    var saveRemoveInlineStyles=function(elem,targetRulesArray){
        //if currently has a style attribute
        var styleAttr=elem.attr('style');
        if(styleAttr!=undefined){
            if(typeof styleAttr=='string'&&styleAttr.length>0){
                //for each style rule
                var hasWidth=false;
                var styleAttrArray=styleAttr.split(';');
                for(var r=0;r<styleAttrArray.length;r++){
                    //if there is a key/val dual style part
                    var keyVal=styleAttrArray[r].split(':');
                    if(keyVal.length>1){
                        var key=keyVal[0].trim();key=key.toLowerCase();
                        var val=keyVal[1].trim();val=val.toLowerCase();
                        //for each target rule
                        for(var t=0;t<targetRulesArray.length;t++){
                            //if this is a target value
                            if(key.indexOf(targetRulesArray[t])!=-1){
                                //preserve this target value
                                //so that when the element is re-pinned, the rule can be restored
                                if(!elem[0].hasOwnProperty('restoreStyles')){
                                    //set the property
                                    elem[0]['restoreStyles']=[];
                                }
                                //push the style value into the array of styles to preserve
                                elem[0].restoreStyles.push({'key':key,'val':val});
                                //remove this style value for now
                                elem.css(key,'');
                            }
                        }
                    }
                }
            }
        }
    };
    //restores the inline styles that were removed by saveRemoveInlineStyles()
    var restoreInlineStyles=function(elem){
            //if any styles should be restored
            if(elem[0].hasOwnProperty('restoreStyles')){
                    //for each rule to restore
                    for(var r=0;r<elem[0].restoreStyles.length;r++){
                            //get the rule
                            var rule=elem[0].restoreStyles[r];
                            //restore the rule
                            elem.css(rule.key,rule.val);
                    }
            }
    };
    //==FUNCTION TO CHECK/SET ALL-PROJECT-IDS FLAG
    var hasAllProjectIds=function(temName){
        var hasAllIds=false;
        //if this template exists
        var temLi=bodyElem[0].getTemplateLi(temName);
        if(temLi.length>0){
            //if this template has ANY changes
            if(temLi.hasClass('project-changes')){
                //get the project id wrapper
                var projBlockWrap=projectIdsWrap.children('.block[name="'+temName+'"]:first');
                //for each project id field wrap
                projBlockWrap.children().each(function(c){
                    //innocent until proven... missing any id
                    if(c==0){hasAllIds=true;}
                    //if this is an input project id
                    var inputElem=jQuery(this).children('input:first');
                    if(inputElem.length>0){
                        //if this value is blank
                        var inputVal=inputElem.val(); inputVal=inputVal.trim();
                        if(inputVal.length<1){
                            //this project does NOT have all id's defined... end loop through fields
                            hasAllIds=false; return false;
                        }
                    }else{
                        //this is a dropdown project id
                        var selectElem=jQuery(this).children('nav.select:first');
                        var selOpt=selectElem.find('ul li.active');
                        var selVal=selOpt.attr('val');
                        //if this dropdown doesn't have a selected item (default item is selected)
                        if(selVal=='...'||selVal==''){
                            //this project does NOT have all id's defined... end loop through fields
                            hasAllIds=false; return false;
                        }
                    }
                });
            }
        }
        return hasAllIds;
    };
    //==FUNCTIONS TO FLAG ANY PROJECT CHANGES==
    var setProjUnsavedChangesFlag=function(temName){
        //if this template is currently selected
        if(bodyElem[0].getSelectedTemplate()==temName){
            var temLi=bodyElem[0].getTemplateLi(temName);
            if(temLi.length>0){
                //==SET OR REMOVE project-changes CLASSES==
                var menuProjBtn=menuBarWrap.find('li[name="project"]:first');
                //if this template has any unsaved changes
                if(temLi.hasClass('project-changes')){
                    //add the project-changes class
                    mainTitleElem.addClass('project-changes');
                    menuProjBtn.addClass('project-changes');
                }else{
                    //no unsaved changes...

                    //remove the project-changes class
                    mainTitleElem.removeClass('project-changes');
                    menuProjBtn.removeClass('project-changes');
                }
            }
        }
    };
    //==FUNCTIONS TO FLAG ANY TEMPLATE CHANGES==
    var setTemUnsavedChangesFlag=function(temName){
        //if this template is currently selected
        if(bodyElem[0].getSelectedTemplate()==temName){
            var temLi=bodyElem[0].getTemplateLi(temName);
            if(temLi.length>0){
                var menuTemBtn=menuBarWrap.find('li[name="template"]:first');
                //if this template has any unsaved changes
                if(temLi.hasClass('template-changes')){
                    //add the template-changes class
                    mainTitleElem.addClass('template-changes');
                    menuTemBtn.addClass('template-changes');
                }else{
                    //no unsaved changes...

                    //remove the template-changes class
                    mainTitleElem.removeClass('template-changes');
                    menuTemBtn.removeClass('template-changes');
                }
            }
        }
    };
    //create the basic changes data XML structure
    var createGetChangeDataWraps=function(temName, whatChanged, howChanged, wrapId){
        var wrapElems={'complete':false};
        //if the #wrapId element doesn't already exist
        var changesMadeWrap=bodyElem.children('#'+wrapId+':last');
        if(changesMadeWrap.length<1){
            //create the #wrapId element
            bodyElem.append('<div style="display:none;" class="wrap" id="'+wrapId+'"></div>');
            changesMadeWrap=bodyElem.children('#'+wrapId+':last');
        }
        wrapElems['changesMadeWrap']=changesMadeWrap;
        if(temName!=undefined){
            //if a boolean value was passed INSTEAD of a template name
            if(typeof temName=='boolean'){
                //if false was passed
                if(!temName){
                    //then clear the changes made
                    changesMadeWrap.html('');
                }
            }else{
                //if this template exists in the left nav
                var temLi=bodyElem[0].getTemplateLi(temName);
                if(temLi.length>0){
                    wrapElems['temLi']=temLi;
                    //if changes for THIS temName don't already exist
                    var thisTemChangesWrap=changesMadeWrap.children('div[name="'+temName+'"]:first');
                    if(thisTemChangesWrap.length<1){
                        //create the element for this template
                        changesMadeWrap.append('<div undo="0" class="template" name="'+temName+'"></div>');
                        thisTemChangesWrap=changesMadeWrap.children('div[name="'+temName+'"]:first');
                    }
                    wrapElems['thisTemChangesWrap']=thisTemChangesWrap;
                    //if WHAT was changed is specified
                    if(whatChanged!=undefined){
                        //if a boolean value was passed INSTEAD of whatChanged
                        if(typeof whatChanged=='boolean'){
                            //if false was passed
                            if(!whatChanged){
                                //then clear the changes made FOR THIS TEMPLATE
                                thisTemChangesWrap.html('');
                            }
                        }else{
                            //get the <what> element by its name
                            var whatWrap=thisTemChangesWrap.children('what[name="'+whatChanged+'"]:first');
                            if(whatWrap.length<1){
                                //create <what> because it doesn't already exist
                                thisTemChangesWrap.append('<what name="'+whatChanged+'"></what>');
                                whatWrap=thisTemChangesWrap.children('what[name="'+whatChanged+'"]:first');
                            }
                            wrapElems['whatWrap']=whatWrap;
                            //if HOW it was changed is specified
                            if(howChanged!=undefined){
                                //get the <how> element by its name
                                var howWrap=whatWrap.children('how[name="'+howChanged+'"]:first');
                                if(howWrap.length<1){
                                    //create <how> because it doesn't already exist
                                    whatWrap.append('<how name="'+howChanged+'"></how>');
                                    howWrap=whatWrap.children('how[name="'+howChanged+'"]:first');
                                }
                                wrapElems['howWrap']=howWrap;
                                wrapElems['complete']=true;
                            }
                        }
                    }
                }
            }
        }
        return wrapElems;
    };
    var templateChangesMade=function(temName, whatChanged, howChanged, json){
        //create the data wraps if they don't already exist... if ALL of the wraps could be retrieved with the given data
        var wrapElems=createGetChangeDataWraps(temName, whatChanged, howChanged, 'templateChangesMade');
        var changesMadeWrap=wrapElems.changesMadeWrap; //changesMadeWrap should be available no matter what
        if(wrapElems.complete){
            //get the remaining wrap elems (all complete)
            var thisTemChangesWrap=wrapElems.thisTemChangesWrap; var temLi=wrapElems.temLi;
            var whatWrap=wrapElems.whatWrap; var howWrap=wrapElems.howWrap;
            //increment the most recent undo number
            var currentUndo=thisTemChangesWrap.attr('undo');currentUndo=parseInt(currentUndo); currentUndo++;
            //combine what/how to get a change key
            var whatHowChange=howChanged+'-'+whatChanged; var changeMade=false;
            //handle different types of changes
            switch(whatHowChange){
                case 'add-include_rule': //add new include rule
                    var incStr=json;
                    //append to the xml to send back to Java
                    howWrap.append('<add undo="'+currentUndo+'">'+incStr+'</add>');
                    changeMade=true;
                    //==LEFT NAV==
                    //get the new include item's html
                    var incHtm=htm_template_include(incStr);
                    //append the new html to the list
                    var includesLi=temLi.find('ul.includes > li:first');
                    var includesUl=includesLi.children('ul:first');
                    includesUl.append(incHtm);
                    //update the include rules count
                    var countElem=includesLi.find('.include .intro .count:first');
                    var count=countElem.text();count=parseInt(count);
                    count++;countElem.text(count+'');
                    //add the has-includes class
                    includesLi.removeClass('no-includes');
                    includesLi.addClass('has-includes');
                    //attach events to the new include rule
                    bodyElem[0].evsIncludeRules();
                    //select the new include rule
                    var newLi=includesUl.children('li:last');
                    var incElem=newLi.children('.inc:first');
                    incElem.click();
                    //make sure the new include rule is within scroll view
                    bodyElem[0].scrollToHighlight(incElem);
                    break;
                case 'mod-include_rule': //modify existing include rule
                    var newStr=json.new;var oldStr=json.old;
                    if(newStr!=oldStr){
                        //append to the xml to send back to Java
                        howWrap.append('<mod undo="'+currentUndo+'"><old>'+oldStr+'</old><new>'+newStr+'</new></mod>');
                        changeMade=true;
                        //==LEFT NAV==
                        //get the .inc elements to modify
                        var incElems=temLi.find('ul.includes li ul li .inc:contains("'+oldStr+'")');
                        var lastIncElem;
                        incElems.each(function(){
                            //if this is one of the matching strings to modify
                            var incStr=jQuery(this).text();
                            if(incStr==oldStr){
                                //set the new string
                                jQuery(this).html(newStr);
                                lastIncElem=jQuery(this);
                            }
                        });
                        //if at least one .inc string was modified
                        if(lastIncElem!=undefined){
                            //make sure the modified rule(s) are selected
                            lastIncElem.click();
                            //make sure the modified rule is within scroll view
                            bodyElem[0].scrollToHighlight(lastIncElem);
                        }
                    }
                    break;
                case 'del-include_rule': //delete include rule
                    var incStr=json;
                    //append to the xml to send back to Java
                    howWrap.append('<del undo="'+currentUndo+'">'+incStr+'</del>');
                    changeMade=true;
                    //==LEFT NAV==
                    //get the .inc elements to delete
                    var incElems=temLi.find('ul.includes li ul li .inc:contains("'+incStr+'")');
                    incElems.each(function(){
                        //if this is one of the matching strings to delete
                        var incStr=jQuery(this).text();
                        if(incStr==incStr){
                            //mark for deletion
                            jQuery(this).parent().addClass('del');
                        }
                    });
                    //get the items that need deletion
                    var delLis=temLi.find('ul.includes li ul li.del');
                    //how many to delete?
                    var delCount=delLis.length;
                    //delete the elements
                    delLis.remove();
                    //update the number of include rules
                    var countElem=temLi.find('ul.includes .include .intro .count:first');
                    var count=countElem.text();count=parseInt(count);
                    countElem.text((count-delCount)+'');
                    //make sure the top of the include rule list is scrolled into view
                    bodyElem[0].scrollToHighlight(temLi.find('ul.includes > li:first'));
                    break;
            }
            //==UNDO INDEX TRACKING==
            //if the change was made
            if(changeMade){
                //set the most recent, incremented, undo number
                thisTemChangesWrap.attr('undo',currentUndo+'');
            }
            //==TEMPLATE CHANGES MADE FLAGGING==
            //if this template has ANY changes AT ALL
            if(thisTemChangesWrap.html().length>0){
                //flag that this template has unsaved changes, if not flagged already
                temLi.addClass('template-changes');
            }else{
                //no unsaved changes...
                temLi.removeClass('template-changes');
            }
            //update other elements that may need to change the template-changes class (based on if temLi has this class)
            setTemUnsavedChangesFlag(temName);
        }else{
            //not all of the wraps were retrieved based on the given input...

            //if a real value was given for temName
            if(temName!=undefined){
                //if a boolean value was given for temName
                if(typeof temName=='boolean'){
                    //if false was passed
                    if(!temName){
                        //for ALL template li's in the left nav
                        var temLis=temLsWrap.find('ul.ls.folders > li');
                        temLis.each(function(){
                            //clear the unsaved changes class for this template
                            jQuery(this).removeClass('template-changes');
                            //update other elements that may need to change the template-changes class
                            setTemUnsavedChangesFlag(jQuery(this).attr('name'));
                        });
                    }
                }
            }
        }
        return changesMadeWrap[0].outerHTML;
    };
    bodyElem[0]['templateChangesMade']=templateChangesMade;
    //==PROJECT VALUE CHANGE MADE==
    //VAR EXAMPLE:
    //temName = 'MyTemplate'
    //whatChanged = 'token_value'
    //howChanged = 'set'
    //json = {'name':'token name', 'value':'set token value'}
    //LIST VAR EXAMPLE:
    //temName = 'MyTemplate'
    //whatChanged = 'token_value'
    //howChanged = 'add'
    //json = {'name':'list name=>token name', 'value':'set token value'}
    var projectChangesMade=function(temName, whatChanged, howChanged, json){
        //combine what/how to get a change key
        var whatHowChange=howChanged+'-'+whatChanged; var howWrapName=howChanged;
        switch(whatHowChange){
            case 'del-token_value': howWrapName='set'; break; //no delete wrap, just edit the existing set wrap
        }
        //create the data wraps if they don't already exist... if ALL of the wraps could be retrieved with the given data
        var wrapElems=createGetChangeDataWraps(temName, whatChanged, howWrapName, 'projectChangesMade');
        var changesMadeWrap=wrapElems.changesMadeWrap; //changesMadeWrap should be available no matter what
        if(wrapElems.complete){
            //get the remaining wrap elems (all complete)
            var thisTemChangesWrap=wrapElems.thisTemChangesWrap; var temLi=wrapElems.temLi;
            var whatWrap=wrapElems.whatWrap; var howWrap=wrapElems.howWrap;
            //can't undo project changes
            thisTemChangesWrap.removeAttr('undo');
            //internal functions
            var getCreateElem=function(parentElem, tagName, attrName){
                //if this child element doesn't already exist
                var childElem=parentElem.children(tagName+'[name="'+attrName+'"]:first');
                if(childElem.length<1){
                    //then create it
                    parentElem.append('<'+tagName+' name="'+attrName+'"></'+tagName+'>');
                    childElem=parentElem.children(tagName+'[name="'+attrName+'"]:first');
                }
                return childElem;
            };
            //get/create the appropriate <token> element hierarchy for the given tokenName (which can describe nested=>names)
            var getItemElem=function(tokenName){
                var elem;
                //if describes a nested name
                if(tokenName.indexOf('=>')!=-1){
                    elem=howWrap;
                    while(tokenName.indexOf('=>')!=-1){
                        //if tokenName starts with =>
                        if(tokenName.indexOf('=>')==0){
                            //remove => from the front of tokenName
                            tokenName=tokenName.substring('=>'.length); tokenName=tokenName.trim();
                        }
                        //get the next token name
                        var nextName=tokenName;
                        //if => is still in nextName
                        if(nextName.indexOf('=>')!=-1){
                            //remove the string at and after =>
                            nextName=nextName.substring(0,nextName.indexOf('=>'));
                        }
                        //remove the nextName from the start of tokenName
                        tokenName=tokenName.substring(nextName.length);
                        tokenName=tokenName.trim(); nextName=nextName.trim(); //trim
                        //get the next sub elem
                        elem=getCreateElem(elem,'item',nextName);
                    }
                }else{
                    //token name doesn't describe a nested name
                    tokenName=tokenName.trim();
                    elem=getCreateElem(howWrap,'item',tokenName);
                }
                return elem;
            };
            var changeMade=false;
            //handle different types of changes
            switch(whatHowChange){
                case 'set-token_value': //set the value of a token
                    //get or create this token element in xml
                    var tokenElem=getItemElem(json.name);
                    //if there is more than one child value
                    var childValElems=tokenElem.children('value');
                    if(childValElems.length>1){
                        //remove all of these child values because "set" will overwrite, not modify
                        childValElems.remove(); changeMade=true;
                    }else if(childValElems.length<1){
                        //no values yet... set this new value element
                        tokenElem.append('<value></value>');changeMade=true;
                    }
                    //determine if the value has changed
                    var valElem=tokenElem.children('value:first');
                    var prevVal=valElem.html();
                    if(prevVal!=json.value){changeMade=true;}
                    //set the new value
                    valElem.html(json.value);
                break;
                case 'add-token_value': //add the value of a token (ie: inside of a list)
                    //*** unlike set-token_value, add will NOT overwrite the previous value
                break;
                case 'del-token_value': //delete all or some values for a token
                    //get or create this token element in xml
                    var tokenElem=getItemElem(json.name);
                    //for each token value inside the token element
                    tokenElem.children('value').each(function(i){
                        //if delete value by index
                        if(json.hasOwnProperty('index')){
                            if(i==json.index){
                                jQuery(this).addClass('del'); //delete at this index
                            }
                        }else{
                            //if delete value by value
                            if(json.hasOwnProperty('value')){
                                if(jQuery(this).html()==json.value){
                                    jQuery(this).addClass('del'); //delete this value
                                }
                            }else{
                                //delete all values under this token
                                jQuery(this).addClass('del');
                            }
                        }
                    });
                    //if any of this token's values were selected for deletion
                    var delValElems=tokenElem.children('value.del');
                    if(delValElems.length>0){
                        //do the value-delete
                        delValElems.remove();
                        changeMade=true;
                    }
                    //if this token element doesn't contain anything anymore
                    if(tokenElem.children().length<1){
                        //also remove this <token>
                        tokenElem.remove();
                    }
                break;
                case 'set-project_file_path':
                    var fileElem=getItemElem(json.templateFile);
                    var valElem=fileElem.children('value:first');
                    if(valElem.length<1){
                        fileElem.append('<value></value>');
                        valElem=fileElem.children('value:first');
                    }
                    if(valElem.html()!=json.resolvedPath){
                        valElem.html(json.resolvedPath);
                        changeMade=true;
                    }
                break;
                case 'del-project_file_path':
                    //if there was data
                    var oldXml=howWrap.html();
                    if(oldXml.length>0){
                        //delete the previous data for ALL paths in this template
                        howWrap.html('');
                        changeMade=true;
                    }
                break;
            }
            //==PROJECT CHANGES MADE FLAGGING==
            //if this template has ANY changes AT ALL
            if(thisTemChangesWrap.html().length>0){
                //flag that this template's project has unsaved changes, if not flagged already
                temLi.addClass('project-changes');
            }else{
                //no unsaved changes...
                temLi.removeClass('project-changes');
            }
            //update other elements that may need to change the project-changes class (based on if temLi has this class)
            setProjUnsavedChangesFlag(temName);
        }else{
            //not all of the wraps were retrieved based on the given input...

            //if a real value was given for temName
            if(temName!=undefined){
                //if a boolean value was given for temName
                if(typeof temName=='boolean'){
                    //if false was passed
                    if(!temName){
                        //for ALL template li's in the left nav
                        var temLis=temLsWrap.find('ul.ls.folders > li');
                        temLis.each(function(){
                            //clear the unsaved changes class for this template
                            jQuery(this).removeClass('project-changes');
                            //update other elements that may need to change the project-changes class
                            setProjUnsavedChangesFlag(jQuery(this).attr('name'));
                        });
                    }
                }
            }
        }
        return changesMadeWrap[0].outerHTML;
    };
    bodyElem[0]['projectChangesMade']=projectChangesMade;
    //==GET FORMATTED TOKEN VALUE BASED ON TOKEN PART RULES, IE: CASING==
    var getFormattedTokenVal=function(tokenVal,json){
        //casing
        if(json.hasOwnProperty('casing')){
            var casing=json.casing;
            casing=casing.trim();
            if(casing.length>0){
                //only get first lowercase letter
                casing=casing.substring(0,1); casing=casing.toLowerCase();
                switch(casing){
                    case 'u': //uppercase
                        tokenVal=tokenVal.toUpperCase();
                        break;
                    case 'l': //lowercase
                        tokenVal=tokenVal.toLowerCase();
                        break;
                    case 'c': //capitalize
                        var firstChar=tokenVal.substring(0, 1);
                        var theRest = tokenVal.substring(1);
                        firstChar=firstChar.toUpperCase();
                        tokenVal=firstChar+theRest;
                        break;
                    case 'n': //normal
                        //yep... do nothing. Leave as is
                        break;
                }
            }
        }
        return tokenVal;
    };
    //==GET A TOKEN VALUE==
    var getTokenValue=function(temName,itemName,valueType){
        var val='';
        //if a template name and token name are BOTH give
        if(temName!=undefined&&itemName!=undefined){
            //if there are any project changes
            var changesMadeWrap=bodyElem.children('#projectChangesMade:last');
            if(changesMadeWrap.length>0){
                //if this template has any changes
                var thisTemChangesWrap=changesMadeWrap.children('.template[name="'+temName+'"]:first');
                if(thisTemChangesWrap.length>0){
                    if(valueType==undefined){valueType='token_value';}
                    //if there are any token values
                    var whatWrap=thisTemChangesWrap.children('what[name="'+valueType+'"]:first');
                    if(whatWrap.length>0){
                        //if there are any set values
                        var howWrap=whatWrap.children('how[name="set"]:first');
                        if(howWrap.length>0){
                            //if this item name could be found (if this is a project_file_paths type, then the item refers to the template file's name... otherwise the item refers to a token name)
                            var itemElem=howWrap.children('item[name="'+itemName+'"]:first');
                            if(itemElem.length>0){
                                //ONLY one value
                                var valueElems=itemElem.children('value');
                                if(valueElems.length==1){
                                    val=valueElems.html(); //get the one value for this token name
                                }else{
                                    //if more than one value
                                    if(valueElems.length>0){
                                        val=[];
                                        valueElems.each(function(){
                                            var oneVal=jQuery(this).html();
                                            val.push(oneVal); //add one of the token name's values to the array
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        switch(valueType){
            case 'project_file_path':
                //if all project ids are available
                if(bodyElem.hasClass('all-project-ids')){
                    //if there is no defined path for this file
                    if(val.length<1){
                        //set the path as the template file's name by default
                        val=itemName;
                    }
                }
                break;
        }
        return val;
    };
    bodyElem[0]['getTokenValue']=getTokenValue;
    //==DEFINE EVENTS THAT DON'T HAVE TO BE RE-DEFINED AFTER DYNAMIC CONTENT CHANGES==
    //PLUS/MINUS SECTIONS
    var plusMinusClick=function(btn){
            var btnParent=btn.parent();
            if(btnParent.hasClass('plus')){
                    var blockElem=btnParent.next('.plus:first');
                    btnParent.removeClass('plus');
                    blockElem.removeClass('plus');
                    btnParent.addClass('minus');
                    blockElem.addClass('minus');
            }else{
                    var blockElem=btnParent.next('.minus:first');
                    btnParent.removeClass('minus');
                    blockElem.removeClass('minus');
                    btnParent.addClass('plus');
                    blockElem.addClass('plus');
            }
    };
    var plusMinusBtns=bodyElem.find('.plus-minus').not('.evs');
    preventSelect(plusMinusBtns);
    plusMinusBtns.addClass('evs');
    plusMinusBtns.html('<span class="minus">'+getSvg('plus')+'</span><span class="plus">'+getSvg('minus')+'</span>');
    plusMinusBtns.click(function(){plusMinusClick(jQuery(this));});
    //SIZE CONTROLS (EG: PIN/UNPIN)
    var pinUnpinElems=bodyElem.find('.pinned-unpinned').not('.evs');
    pinUnpinElems.addClass('evs');
    var slider_timeout;
    pinUnpinElems.hover(function(){
            //if unpinned
            if(jQuery(this).hasClass('unpinned')){
                    var rowWrap=jQuery(this).parent();
                    //after a delay
                    slider_timeout=setTimeout(function(){
                            //remove slide-in class on hover
                            rowWrap.removeClass('slide-in');
                            rowWrap.addClass('slide-out');
                    }, 280);
            }
    },function(){
            //add slide-in class on hover out
            clearTimeout(slider_timeout);
            var rowWrap=jQuery(this).parent();
            //if unpinned
            if(jQuery(this).hasClass('unpinned')){
                    rowWrap.addClass('slide-in');
            }else{rowWrap.removeClass('slide-in');}
            rowWrap.removeClass('slide-out');
    });
    var sizeCtlsWraps=bodyElem.find('.size-controls');
    //pin/un-pin column
    var pinElems=sizeCtlsWraps.children('.pin');
    pinElems.click(function(){
            //get the parent element that is affected by pinned/unpinned state
            var parentPinElem=jQuery(this).parents('.pinned-unpinned:first');
            //get the elements that share the space with the pinned-unpinned element
            var rowWrap=parentPinElem.parent();
            var spaceShareElems=rowWrap.children().not(parentPinElem);
            //if currently pinned
            if(parentPinElem.hasClass('pinned')){
                    //then unpin
                    rowWrap.addClass('slide-view');
                    parentPinElem.removeClass('pinned');
                    parentPinElem.addClass('unpinned');
                    //remove width style rules so that they can be restored later
                    saveRemoveInlineStyles(parentPinElem, ['width']);
                    //do the same for the other elements that share the space with parentPinElem
                    spaceShareElems.each(function(){
                            //remove width style rules so that they can be restored later
                            saveRemoveInlineStyles(jQuery(this), ['width']);
                    });
            }else{
                    //NOT pinned...

                    //then pin
                    rowWrap.removeClass('slide-view');
                    rowWrap.removeClass('slide-in');
                    rowWrap.removeClass('slide-out');
                    parentPinElem.removeClass('unpinned');
                    parentPinElem.addClass('pinned');
                    //restore inline styles
                    restoreInlineStyles(parentPinElem);
                    //do the same for the other elements that share the space with parentPinElem
                    spaceShareElems.each(function(){
                            //restore width style rules
                            restoreInlineStyles(jQuery(this));
                    });
            }
    });
    //pin/un-pin column
    var toggleExpandBtns=sizeCtlsWraps.children('.toggle-expand');
    toggleExpandBtns.click(function(){
            //get the parent element that is affected by expanded/collapsed state
            var expandParentElem=jQuery(this).parents('.expanded-collapsed:first');
            //get the wrapper above this expanded/collapsed element
            var colWrap=expandParentElem.parent();
            //get the other elements that share the space with the expandParentElem
            var spaceShareElems=colWrap.children().not(expandParentElem);
            //figure out what key name to use for this expanded/collapsed system
            var ecName=expandParentElem.attr('id');if(ecName==undefined){ecName='';}
            if(ecName.length<1){
                    ecName=expandParentElem.attr('name');if(ecName==undefined){ecName='';}
            }
            //if there is a unique id name for this expand/collapse system
            var uniqueExpandClass='';var uniqueCollapseClass='';
            if(ecName.length>0){
                    uniqueExpandClass='expand-'+ecName;
                    uniqueCollapseClass='collapse-'+ecName;
            }
            //if currently expanded
            if(expandParentElem.hasClass('expanded')){
                    //so collapse
                    expandParentElem.removeClass('expanded');
                    expandParentElem.addClass('collapsed');
                    //remove height style rules so that they can be restored later
                    saveRemoveInlineStyles(expandParentElem, ['height']);
                    //do the same for the other elements that share the space with expandParentElem
                    spaceShareElems.each(function(){
                            //remove height style rules so that they can be restored later
                            saveRemoveInlineStyles(jQuery(this), ['height']);
                    });
                    //if there is a unique name for this expand/collapse system
                    if(ecName.length>0){
                            colWrap.removeClass(uniqueExpandClass);
                            colWrap.addClass(uniqueCollapseClass);
                    }
            }else{
                    //currently collapsed...

                    //so expand
                    expandParentElem.addClass('expanded');
                    expandParentElem.removeClass('collapsed');
                    //if there is a unique name for this expand/collapse system
                    if(ecName.length>0){
                            colWrap.removeClass(uniqueCollapseClass);
                            colWrap.addClass(uniqueExpandClass);
                    }
                    //restore inline styles
                    restoreInlineStyles(expandParentElem);
                    //do the same for the other elements that share the space with expandParentElem
                    spaceShareElems.each(function(){
                            //restore width style rules
                            restoreInlineStyles(jQuery(this));
                    });
            }
    });
    //get the element that is scrolled to move found searches into view
    var getScrollElem=function(type){
            var elem;
            switch(type){
                    case 'templates':
                            elem=temLsWrap;
                    break;
                    default:

                    break;
            }
            return elem;
    };
    bodyElem[0]['getScrollElem']=getScrollElem;
    //move the highlighted found element into scroll view
    var scrollToHighlight=function(elem,searchType){
        if(elem!=undefined&&elem.length>0){
            if(searchType==undefined){searchType='templates';}
            var scrollElem=bodyElem[0].getScrollElem(searchType);
            if(scrollElem!=undefined){
                //==VERTICAL==
                //get the element's position and the view window bounds of the current sroll
                var elemTop=elem.position().top;
                var scrollTop=scrollElem.scrollTop();
                elemTop+=scrollTop;
                var elemBottom=elemTop+elem.outerHeight();
                var scrollBottom=scrollTop+scrollElem.innerHeight();
                //if the scroll has passed the element's position
                if(elemTop<scrollTop){
                    scrollElem.scrollTop(elemTop); //scroll up to the element
                }else{
                    //if the scroll is before the element's position
                    if(elemBottom>=scrollBottom){
                        scrollElem.scrollTop(elemTop); //scroll down to the element
                    }
                }
                //==HORIZONTAL==
                //current scroll amount
                var scrollLeft=scrollElem.scrollLeft();
                //compare the left edge with where the elem is located
                var offsetLeftEdge=scrollElem.offset().left;
                var elemLeft=elem.offset().left;
                //if the element is hanging off the left edge
                if(elemLeft<=offsetLeftEdge){
                    //how far is the element element hanging off the left edge?
                    var difference=offsetLeftEdge-elemLeft;
                    //move the scroll to the left to correct the difference
                    var newScrollLeft=scrollLeft-difference;
                    if(newScrollLeft<0){newScrollLeft=0;}
                    scrollElem.scrollLeft(newScrollLeft);
                }else{
                    //compare the right edge with where the elem is located
                    var elemRight=elemLeft+elem.outerWidth();
                    var offsetRightEdge=offsetLeftEdge+scrollElem.innerWidth();
                    //if the element is hanging off the right edge
                    if(elemRight>=offsetRightEdge){
                        //how far is the element element hanging off the right edge?
                        var difference=elemRight-offsetRightEdge;
                        //move the scroll to the right to correct the difference
                        var newScrollRight=scrollLeft+difference;
                        scrollElem.scrollLeft(newScrollRight);
                    }
                }
            }
        }
    };
    bodyElem[0]['scrollToHighlight']=scrollToHighlight;
    //INCLUDE FILE RULE BOX
    //internal function to sanitize the search string
    var sanitizeIncludeStr=function(str){
            //case insensitive
            str=str.trim();
            //remove certain strings
            str=replaceAll(str,'   ',' ');
            str=replaceAll(str,'  ',' ');
            str=replaceAll(str,'<','');
            str=replaceAll(str,'>','');
            str=replaceAll(str,'"','');
            str=replaceAll(str,"'",'');
            //make sure the rule string doesn't begin with, or end with /
            if(str.indexOf('/')==0){str.substring('/'.length);}
            if(str.lastIndexOf('/')==str.length-'/'.length){str=str.substring(0,str.length-'/'.length);}

            return str;
    };
    //get the include rule wraps
    var includeRuleWraps=contentWrap.find('.edit-include-rule');
    includeRuleWraps.each(function(){
            var includeRuleWrap=jQuery(this);
            var includeInput=includeRuleWrap.children('input:first');
            var addBtn=includeRuleWrap.children('.add-btn:last');
            var clearBtn=includeRuleWrap.children('.clear-btn:last');
            //disable selection on certain elements
            preventSelect(addBtn); preventSelect(clearBtn);
            //add the button images
            addBtn.html(getSvg('paperclip'));
            clearBtn.html(getSvg('x'));
            //get the default text for this field
            var defaultTxt=includeInput.attr('value');
            var origTxt=defaultTxt;
            includeInput[0]['origTxt']=origTxt;
            defaultTxt=sanitizeIncludeStr(defaultTxt);
            //internal functions
            var doAdd=function(){
                    includeRuleWrap.addClass('do-add');
                    setTimeout(function(){includeRuleWrap.removeClass('do-add');},200);
                    //get the text to search
                    var currentTxt=includeInput.val();
                    currentTxt=sanitizeIncludeStr(currentTxt);
                    //if the current text is the default text OR blank
                    if(currentTxt==defaultTxt||currentTxt.length<1){
                            //if the text is blank
                            if(currentTxt.length<1){
                                    //if there is a selected include rule to modify
                                    var temLi=getTemplateLi();
                                    var temName=temLi.attr('name');
                                    var incElem=temLi.find('ul.includes > li ul li.selected > .inc:first');
                                    if(incElem.length>0){
                                            var incStr=incElem.text();
                                            //delete this include rule since a BLANK string was entered to modify its value
                                            bodyElem[0].templateChangesMade(temName,'include_rule','del',incStr);
                                    }
                            }
                            //clear the default text and set focus
                            bodyElem[0].deselectIncludeRules();
                            includeInput.val('');
                            includeInput.focus();
                    }else{
                            //the text is NOT blank NOR default text...

                            //get the template name
                            var temLi=getTemplateLi();
                            var temName=temLi.attr('name');
                            //==SELECT THIS INCLUDE RULE TEXT IF IT'S IN THE LIST==
                            //get the existing inc elements (with this text)
                            var includesUl=temLi.find('ul.includes > li ul:first');
                            var existingIncElems=includesUl.find('li .inc:contains("'+currentTxt+'")');
                            //for each element that contains this include rule text
                            existingIncElems.each(function(){
                                    var incStr=jQuery(this).text();
                                    //if this text matches exactly
                                    if(incStr==currentTxt){
                                            //make sure no other rule is selected
                                            bodyElem[0].deselectIncludeRules();
                                            //select this match
                                            jQuery(this).click();
                                            //stop search
                                            return false;
                                    }
                            });
                            //==MODIFY EXISTING... OR ADD NEW==
                            //if any include rule is already selected for modification
                            var selectedLi=includesUl.children('li.selected');
                            if(selectedLi.length>0){
                                    var incElem=selectedLi.children('.inc:first'); var incStr=incElem.text();
                                    //if the value is different from the old value
                                    if(incStr!=currentTxt){
                                            //modify an existing include rule
                                            bodyElem[0].templateChangesMade(temName,'include_rule','mod',{'new':currentTxt,'old':incStr});
                                    }
                            }else{
                                    //new include rule to add...
                                    bodyElem[0].templateChangesMade(temName,'include_rule','add',currentTxt);
                            }
                    }
            };
            var gotFocus=function(){
                    //if the current text is the default text
                    var currentTxt=includeInput.val();
                    currentTxt=sanitizeIncludeStr(currentTxt);
                    if(currentTxt==defaultTxt){
                            //clear text
                            bodyElem[0].deselectIncludeRules();
                            includeInput.val('');
                    }
                    //expand the include rules in the selected templates nav
                    var temLi=getTemplateLi();
                    if(temLi.hasClass('closed')){
                            //open the templates in the menu nav
                            var openBtn=temLi.find('.dir .opened-closed:first');
                            openBtn.click();
                    }
                    var includesLi=temLi.find('ul.includes > li:first');
                    if(includesLi.hasClass('closed')){
                            //open the includes in the menu nav
                            var openBtn=includesLi.find('.include .opened-closed:first');
                            openBtn.click();
                    }
                    //make sure the include rules are NOT scrolled out of view
                    scrollToHighlight(includesLi);
            };
            //EVENTS
            addBtn.click(function(){doAdd();includeInput.focus();});
            //clear button click event
            clearBtn.click(function(){
                    bodyElem[0].deselectIncludeRules();
            });
            includeRuleWrap.hover(function(){
                    jQuery(this).addClass('hover');
            },function(){
                    jQuery(this).removeClass('hover');
            });
            includeInput.blur(function(){
                    //if NOT over the addBtn NOR clearBtn
                    if(!includeRuleWrap.hasClass('hover')){
                            //clear out selection, <found> highlights, AND include rule text in the input box
                            bodyElem[0].deselectIncludeRules();
                    }
            });
            if(includeInput.focusin){
                    includeInput.focusin(function(){gotFocus();});
            }else{
                    includeInput.click(function(){gotFocus();});
            }
            includeInput.keydown(function(e){
                    switch(e.keyCode){
                            case 9: //tab key pressed
                                    e.preventDefault();
                                    var currentTxt=includeInput.val();
                                    currentTxt=sanitizeIncludeStr(currentTxt);
                                    //if the string is NOT blank NOR default text
                                    if(currentTxt!=defaultTxt&&currentTxt.length>0){
                                            var temLi=getTemplateLi();
                                            var includesLi=temLi.find('ul.includes > li.has-includes:first');
                                            //if has-includes
                                            if(includesLi.length>0){
                                                    //if there is more than one found element left
                                                    var foundElems=includesLi.find('ul li .inc found');
                                                    if(foundElems.length>0){
                                                            //for each partial match
                                                            var commonTxt;
                                                            foundElems.each(function(){
                                                                    //get the include string
                                                                    var foundTxt=jQuery(this).text();
                                                                    var incElem=jQuery(this).parent();
                                                                    var incStr=incElem.text();
                                                                    //if this is ENTIRELY matched already
                                                                    if(incStr==foundTxt){
                                                                            //select this incElem then
                                                                            incElem.click();
                                                                    }else{
                                                                            //get the remaining unmatched text
                                                                            var remainingTxt=incStr.substring(foundTxt.length);
                                                                            //if this is the first include string to compare
                                                                            if(commonTxt==undefined){commonTxt=remainingTxt;}
                                                                            else{
                                                                                    //not the first include string to compare...

                                                                                    //trim the commonTxt so it's NOT longer than remainingTxt
                                                                                    if(commonTxt.length>remainingTxt.length){
                                                                                            commonTxt=commonTxt.substring(0,remainingTxt.length);
                                                                                    }
                                                                                    //for each letter in the remainingTxt
                                                                                    for(var c=0;c<remainingTxt.length;c++){
                                                                                            //if this letter DOESN'T match the commonTxt
                                                                                            if(remainingTxt[c]!=commonTxt[c]){
                                                                                                    //trim the commonTxt to exclude the characters that are different
                                                                                                    commonTxt=commonTxt.substring(0,c);
                                                                                                    //stop comparing
                                                                                                    break;
                                                                                            }
                                                                                    }
                                                                                    //if there is no more commonTxt
                                                                                    if(commonTxt.length<1){
                                                                                            //stop whittling down the commonTxt
                                                                                            return false;
                                                                                    }
                                                                            }
                                                                    }
                                                            });
                                                            //if there is commonTxt that got partially matched
                                                            if(commonTxt!=undefined&&commonTxt.length>0){
                                                                    //add the commonTxt to the end of the currentTxt
                                                                    var extendedTxt=currentTxt+commonTxt
                                                                    //set the extended text
                                                                    includeInput.val(extendedTxt);
                                                            }
                                                    }
                                            }
                                    }
                            break;
                    }
            });
            includeInput.keyup(function(e){
                    switch(e.keyCode){
                            case 27: //escape key pressed
                                    e.preventDefault();
                                    bodyElem[0].deselectIncludeRules();
                                    includeInput.val('');
                            break;
                            case 13: //enter key pressed
                                    e.preventDefault();
                                    doAdd();
                            break;
                            default: //another key pressed, eg: a, b, c
                                    //if the current text is NOT the default text OR blank
                                    var currentTxt=includeInput.val();
                                    currentTxt=sanitizeIncludeStr(currentTxt);
                                    if(currentTxt!=defaultTxt&&currentTxt.length>0){
                                            //add the text entered class
                                            includeRuleWrap.addClass('text-entered');
                                            //highlight the corresponding include rule text in the templates nav
                                            var temLi=getTemplateLi();
                                            var includesLi=temLi.find('ul.includes > li.has-includes:first');
                                            //if has-includes
                                            if(includesLi.length>0){
                                                    //if NO include rules are already selected
                                                    if(includesLi.find('ul li.selected').length<1){
                                                            //get the include elements
                                                            var incElems=includesLi.find('ul li .inc');
                                                            //for each include string element
                                                            incElems.each(function(){
                                                                    var incStr=jQuery(this).text();
                                                                    incStr=sanitizeIncludeStr(incStr);
                                                                    //if this include rule begins with the entered text
                                                                    if(incStr.indexOf(currentTxt)==0){
                                                                            //entered text matches first part of the include rule...
                                                                            incStr=incStr.replace(currentTxt,'<found class="glow"><<<>>></found>');
                                                                            incStr=incStr.replace('<<<>>>',currentTxt);
                                                                    }
                                                                    //set the updated include string
                                                                    jQuery(this).html(incStr);
                                                            });
                                                    }
                                            }
                                    }else{
                                            //default text OR blank
                                            includeRuleWrap.removeClass('text-entered');
                                    }
                            break;
                    }
            });
    });
    //==SELECT INCLUDE RULE==
    var selectIncludeRule=function(temName,ruleStr){
        ruleStr=sanitizeIncludeStr(ruleStr);
        if(ruleStr.length>0){
            //==LEFT NAV FILE==
            //if this template name exists
            var temLi=getTemplateLi(temName);
            if(temLi.length>0){
                var includesUl=temLi.children('ul.includes:last');
                //deselect current rule strings
                //---bodyElem[0].deselectIncludeRules(); //blur should take care of this
                //if any include rules match this ruleStr
                var includesBtn=includesUl.find('li.has-includes ul li .inc:contains("'+ruleStr+'")');
                if(includesBtn.length>0){
                    //for each matching include rule
                    includesBtn.each(function(){
                        var thisRuleStr=jQuery(this).text();
                        thisRuleStr=sanitizeIncludeStr(thisRuleStr);
                        //if this rule matches
                        if(thisRuleStr==ruleStr){
                            //add select class
                            jQuery(this).parent().addClass('selected');
                        }
                        //remove <found> markup
                        jQuery(this).html(thisRuleStr);
                    });
                    //==TREE VIEW INCLUDE RULE TEXT BOX==
                    //open tree view tab
                    goToTabView(mainViewTabs.children('.tree-view:first'));
                    //for each text box that should contain the selected include rule string
                    includeRuleWraps.each(function(i){
                        //set this include rule as the text
                        var includeInput=jQuery(this).children('input:last');
                        includeInput.val(ruleStr);
                        //the first input box receives focus
                        if(i==0){includeInput.focus();}
                        //add the text entered class
                        jQuery(this).addClass('text-entered');
                    });
                    //*** highlight the selected INCLUDE files in tree view
                }
            }
        }
    };
    bodyElem[0]['selectIncludeRule']=selectIncludeRule;
    //==DESELECT INCLUDE RULES==
    var deselectIncludeRules=function(focusInput){
            //==LEFT NAV==
            //get the include rule <li> elements
            var includeLis=temLsWrap.find('ul.ls.folders ul.includes li ul li');
            //remove selected classes
            includeLis.filter('.selected').removeClass('selected');
            //for each highlighted <found> element
            includeLis.find('.inc found').each(function(){
                    //remove <found> element from include rule text
                    var incElem=jQuery(this).parent();
                    var incStr=incElem.text();
                    incElem.html(incStr);
            });
            //==TREE VIEW==
            var includeInput=jQuery('.edit-include-rule input');
            includeInput.each(function(){
                    //if has text entered
                    var includeWrap=includeInput.parent();
                    //reset text
                    var origTxt=jQuery(this)[0].origTxt;
                    jQuery(this).val(origTxt);
                    includeWrap.removeClass('text-entered');
            });
    };
    bodyElem[0]['deselectIncludeRules']=deselectIncludeRules;
    //SEARCH BOXES
    //internal function to sanitize the search string
    var sanitizeSearchStr=function(str){
            //case insensitive
            str=str.trim();
            str=str.toLowerCase();
            //remove certain strings
            str=replaceAll(str,'   ',' ');
            str=replaceAll(str,'  ',' ');
            str=replaceAll(str,'<','');
            str=replaceAll(str,'>','');

            return str;
    };
    //get the search wraps
    var searchWraps=contentWrap.find('.search');
    searchWraps.each(function(){
            var searchWrap=jQuery(this);
            var searchInput=searchWrap.children('input:first');
            var searchBtn=searchWrap.children('.search-btn:last');
            var clearBtn=searchWrap.children('.clear-btn:last');
            var foundCountWrap=searchWrap.children('.found-count:last');
            var foundCurrentElem=foundCountWrap.children('.current:first');
            var foundTotalElem=foundCountWrap.children('.total:last');
            var foundPrevBtn=foundCountWrap.children('.prev:last');
            var foundNextBtn=foundCountWrap.children('.next:last');
            //disable selection on certain elements
            preventSelect(foundCountWrap); preventSelect(searchBtn); preventSelect(clearBtn);
            //get the default text for this search field
            var defaultTxt=searchInput.attr('value');
            var origTxt=defaultTxt;
            defaultTxt=sanitizeSearchStr(defaultTxt);
            //add the button images
            searchBtn.html(getSvg('search'));
            clearBtn.html(getSvg('x'));
            foundPrevBtn.html(getSvg('up'));
            foundNextBtn.html(getSvg('down'));
            //get the searchType indicator for the search that needs to be conducted
            var searchType=searchWrap.attr('name');
            //get the elements whose inner text should be searched
            var getSearchElems=function(type){
                    var elems;
                    switch(type){
                            case 'templates':
                                    elems=temLsWrap.find('.dir > .path,.file > .name,.token .str > .part.dir,.token .str > .part.name,.token .str > .part.alias,.token .str .part > .i');
                            break;
                            default:

                            break;
                    }
                    return elems;
            };
            //get all the found elements for a search
            var getFoundElems=function(){
                    //get the elements whose inner text should be searched, depending on searchType
                    var searchElems=getSearchElems(searchType);
                    //get the highlighted elements (should only be one)
                    var foundElems=searchElems.has('found');
                    foundElems=foundElems.children('found');
                    return foundElems;
            };
            //hightlight the next found search match
            var highlightNext=function(){
                    var didIt=false;
                    //if there is a previous search saved
                    if(searchInput[0].hasOwnProperty('currentSearch')){
                            //get the found elements
                            var foundElems=getFoundElems();
                            //if there is more than one found element
                            if(foundElems.length>1){
                                    //get the highlighted found
                                    var highlightedElems=foundElems.filter('.highlight');
                                    //get the index of the highlighted element
                                    var highlightIndex=highlightedElems.eq(0).attr('name');
                                    highlightIndex=parseInt(highlightIndex);
                                    //if this is NOT the last found element
                                    var nextElem;
                                    if(highlightIndex+1<foundElems.length){
                                            //move to the next index
                                            nextElem=foundElems.filter('found[name="'+(highlightIndex+1)+'"]:first');
                                    }else{
                                            //this is the last found element...

                                            //cycle back to the first element
                                            nextElem=foundElems.eq(0);
                                    }
                                    //add the highlight to the next found
                                    highlightedElems.removeClass('highlight');
                                    nextElem.addClass('highlight');
                                    //update the current count number
                                    var currentCount=nextElem.attr('name');
                                    currentCount=parseInt(currentCount);currentCount++;
                                    foundCurrentElem.text(currentCount+'');
                                    //if at the last count
                                    if(foundCurrentElem.text()==foundTotalElem.text()){
                                            searchWrap.addClass('atLast');
                                    }else{
                                            searchWrap.removeClass('atLast');
                                    }
                                    //indicate the move happened
                                    didIt=true;
                                    //delay so that css transitions will finish changing the highlighted size
                                    setTimeout(function(){
                                            //scroll to the highlighted element, if outside of curent scroll
                                            scrollToHighlight(nextElem);
                                    },20);
                            }
                    }
                    return didIt;
            };
            //hightlight the previous found search match
            var highlightPrev=function(){
                    var didIt=false;
                    //if there is a previous search saved
                    if(searchInput[0].hasOwnProperty('currentSearch')){
                            //get the found elements
                            var foundElems=getFoundElems();
                            //if there is more than one found element
                            if(foundElems.length>1){
                                    //get the highlighted found
                                    var highlightedElems=foundElems.filter('.highlight');
                                    //get the index of the highlighted element
                                    var highlightIndex=highlightedElems.eq(0).attr('name');
                                    highlightIndex=parseInt(highlightIndex);
                                    //if this is NOT the first found element
                                    var nextElem;
                                    if(highlightIndex>0){
                                            //move to the prev index
                                            nextElem=foundElems.filter('found[name="'+(highlightIndex-1)+'"]:first');
                                    }else{
                                            //this is the first found element...

                                            //cycle back to the last element
                                            nextElem=foundElems.filter(':last');
                                    }
                                    //add the highlight to the next found
                                    highlightedElems.removeClass('highlight');
                                    nextElem.addClass('highlight');
                                    //update the current count number
                                    var currentCount=nextElem.attr('name');
                                    currentCount=parseInt(currentCount);currentCount++;
                                    foundCurrentElem.text(currentCount+'');
                                    //if at the last count
                                    if(foundCurrentElem.text()==foundTotalElem.text()){
                                            searchWrap.addClass('atLast');
                                    }else{
                                            searchWrap.removeClass('atLast');
                                    }
                                    //indicate the move happened
                                    didIt=true;
                                    //delay so that css transitions will finish changing the highlighted size
                                    setTimeout(function(){
                                            //scroll to the highlighted element, if outside of curent scroll
                                            scrollToHighlight(nextElem);
                                    },20);
                            }
                    }
                    return didIt;
            };
            //search button click event
            var doSearch=function(){
                    searchWrap.addClass('do-search');
                    setTimeout(function(){searchWrap.removeClass('do-search');},200);
                    //get the text to search
                    var currentTxt=searchInput.val();
                    currentTxt=sanitizeSearchStr(currentTxt);
                    //if the current text is the default text OR blank
                    if(currentTxt==defaultTxt||currentTxt.length<1){
                            //clear the default text and set focus
                            searchInput.val('');
                            searchInput.focus();
                    }else{
                            //current text is NOT the default NOR blank...

                            //if this text is different than the previous search
                            if(!searchInput[0].hasOwnProperty('currentSearch')
                                            ||searchInput[0].currentSearch!=currentTxt){
                                    //save this currentTxt as the current search
                                    searchInput[0]['currentSearch']=currentTxt;
                                    //get the elements whose inner text should be searched, depending on searchType
                                    var searchElems=getSearchElems(searchType);
                                    var foundIndex=0;
                                    //for each element to search
                                    searchElems.each(function(){
                                            //get the inner text of this element
                                            var thisTxt=jQuery(this).text();
                                            var origTxt=thisTxt;
                                            //remove inner html (if a previous search had been conducted that left highlight html in this text)
                                            jQuery(this).html(origTxt);
                                            //normalize the inner text (casing, etc...)
                                            thisTxt=sanitizeSearchStr(thisTxt);
                                            //if any part of this inner text matches the text being searched
                                            if(thisTxt.indexOf(currentTxt)!=-1){
                                                    //get the matched sub-string
                                                    var startIndex=thisTxt.indexOf(currentTxt);
                                                    var matchedTxt=origTxt.substring(startIndex,startIndex+currentTxt.length);
                                                    //insert the <found></found> elements around the matchedTxt
                                                    var crazyTxt="<<<>>>";
                                                    origTxt=replaceAll(origTxt,matchedTxt,crazyTxt);
                                                    origTxt=replaceAll(origTxt,crazyTxt,'<found>'+matchedTxt+'</found>');
                                                    jQuery(this).html(origTxt);
                                                    //make sure this found text is NOT hidden inside a closed parent
                                                    var closedParents=jQuery(this).parents('li:first').parents('li.closed');
                                                    closedParents.each(function(){
                                                            //open up this parent element
                                                            var openBtn=jQuery(this).find('.opened-closed:first');
                                                            openBtn.click();
                                                    });
                                                    //number the found elements
                                                    var foundElems=jQuery(this).children('found');
                                                    foundElems.each(function(){
                                                            //highlight the first found
                                                            if(foundIndex==0){
                                                                    //highlight
                                                                    jQuery(this).addClass('glow');
                                                                    jQuery(this).addClass('highlight');
                                                                    //make sure this first found element is scrolled into view
                                                                    scrollToHighlight(jQuery(this));
                                                            }else{jQuery(this).addClass('glow');}
                                                            //add index number to found element
                                                            jQuery(this).attr('name',foundIndex);
                                                            foundIndex++;
                                                    });
                                            }
                                    });
                                    //show the found count
                                    if(foundIndex>0){
                                            //at least one found
                                            foundCurrentElem.text('1');
                                            foundTotalElem.text(foundIndex+'');
                                            //if only one found
                                            if(foundIndex==1){
                                                    searchWrap.addClass('atLast');
                                            }else{
                                                    searchWrap.removeClass('atLast');
                                            }
                                    }else{
                                            //no matches found
                                            searchWrap.addClass('atLast');
                                            foundCurrentElem.text('0');
                                            foundTotalElem.text('0');
                                    }
                                    searchWrap.addClass('results');
                            }else{
                                    //this search is not different than the previous search...

                                    //highlight the next search match
                                    highlightNext();
                            }
                    }
            };
            searchBtn.click(function(){doSearch();searchInput.focus();});
            var removeFoundHighlights=function(){
                    //if there is a previous search
                    if(searchInput[0].hasOwnProperty('currentSearch')){
                            //clear the previous search
                            searchInput[0].currentSearch=undefined;
                            //hide the found count
                            foundCurrentElem.text('-');
                            foundTotalElem.text('-');
                            searchWrap.removeClass('results');
                            searchWrap.removeClass('atLast');
                            //get the elements whose inner text should be searched, depending on searchType
                            var searchElems=getSearchElems(searchType);
                            //for each search element that contains a <found> element
                            searchElems.has('found').each(function(){
                                    //make sure there are no <found> elements hanging around
                                    var origTxt=jQuery(this).text();
                                    jQuery(this).html(origTxt);
                            });
                    }
            };
            var clearTxt=function(){
                    //clear the text and set focus
                    searchWrap.removeClass('text-entered');
                    searchInput.val(origTxt);
                    removeFoundHighlights();
            };
            //clear button click event
            clearBtn.click(function(){clearTxt();});
            //search input events
            searchInput.blur(function(){
                    //if the current text is the default text OR blank
                    var currentTxt=searchInput.val();
                    currentTxt=sanitizeSearchStr(currentTxt);
                    if(currentTxt==defaultTxt||currentTxt.length<1){
                            //restore the default text
                            searchInput.val(origTxt);
                            searchWrap.removeClass('text-entered');
                            removeFoundHighlights();
                    }
            });
            var gotFocus=function(){
                    //if the current text is the default text
                    var currentTxt=searchInput.val();
                    currentTxt=sanitizeSearchStr(currentTxt);
                    if(currentTxt==defaultTxt){
                            //clear text
                            searchInput.val('');
                    }
            };
            if(searchInput.focusin){
                    searchInput.focusin(function(){gotFocus();});
            }else{
                    searchInput.click(function(){gotFocus();});
            }
            searchInput.keydown(function(e){
                    switch(e.keyCode){
                            case 38: //up arrow
                                    if(highlightPrev()){
                                            e.preventDefault();}
                            break;
                            case 40: //down arrow
                                    if(highlightNext()){
                                            e.preventDefault();}
                            break;
                    }
            });
            searchInput.keyup(function(e){
                    switch(e.keyCode){
                            case 27: //escape key pressed
                                    e.preventDefault();
                                    clearTxt();
                                    searchInput.val('');
                            break;
                            case 13: //enter key pressed
                                    e.preventDefault();
                                    doSearch();
                            break;
                            default: //another key pressed, eg: a, b, c
                                    //if the current text is NOT the default text OR blank
                                    var currentTxt=searchInput.val();
                                    currentTxt=sanitizeSearchStr(currentTxt);
                                    if(currentTxt!=defaultTxt&&currentTxt.length>0){
                                            //add the text entered class
                                            searchWrap.addClass('text-entered');
                                    }else{
                                            //default text OR blank
                                            searchWrap.removeClass('text-entered');
                                            removeFoundHighlights();
                                    }
                            break;
                    }
            });
            foundNextBtn.click(function(){
                    highlightNext();
                    searchInput.focus();
            });
            foundPrevBtn.click(function(){
                    highlightPrev();
                    searchInput.focus();
            });
    });
    //==FUNCTIONS ATTACHED TO THE BODY ELEMENT==
    //detect if the given string is literal (starts and ends with " or ');
    var isLiteralString=function(str){
        var isLit=false;str=str.trim();
        if(str.length>0){
            //if starts with quote...
            if(str.indexOf('"')==0||str.indexOf("'")==0){
                var lastChar=str.substring(str.length-1);
                //... and ends with quote
                if(lastChar=='"'||lastChar=="'"){
                    isLit=true;
                }
            }
        }
        return isLit;
    };
    bodyElem[0]['isLiteralString']=isLiteralString;
    //convert <span class="str">...</span>, in the templates nav, to a JSON
    var tokenNavElemToJson=function(strElem,includePartSelector){
        if(includePartSelector==undefined){includePartSelector='.part';}
        var tokenJson;
        //if this element has a str class
        if(strElem.hasClass('str')){
            //if the parent of this element has a token class
            if(strElem.parent().hasClass('token')){
                tokenJson={};
                //if this token has-source
                var parentLi=strElem.parents('li:first');
                if(parentLi.hasClass('has-source')){
                    //add the source value to the json
                    var srcSpan=parentLi.find('span.part.source:last');
                    var srcVal=srcSpan.text();srcVal=srcVal.trim();
                    tokenJson['source']=srcVal;
                }else{
                    //this token doesn't have a source... is it overwritten?
                    if(parentLi.hasClass('overwritten')){
                        //set overwritten
                        tokenJson['overwritten']=true;
                    }
                }
                //for each token part under the strElem
                strElem.children(includePartSelector).not('.sep').each(function(){
                    //get the last class as the partName
                    var partName=jQuery(this).attr('class');
                    partName=partName.split(' ');
                    partName=partName[partName.length-1];
                    //if this part name isn't already in the json
                    if(!tokenJson.hasOwnProperty(partName)){
                        //if the part contains options
                        var partVal='';
                        var iElems=jQuery(this).children('.i');
                        if(iElems.length>0){
                            partVal=[];
                            //for each option
                            iElems.each(function(){
                                //add the option value to the array
                                var val=jQuery(this).text();val=val.trim();
                                partVal.push(val);
                            });
                        }else{
                            //set the option value
                            partVal=jQuery(this).text();partVal=partVal.trim();
                        }
                        //add the part name/value to the json
                        tokenJson[partName]=partVal;
                    }
                });
            }
        }
        return tokenJson;
    };
    bodyElem[0]['tokenNavElemToJson']=tokenNavElemToJson;
    //convert <span class="str">...</span>, in the templates nav, to template code token string
    var tokenNavElemToString=function(strElem){
        var tokenStr='';
        //start/end tags
        var startTag='<<';var endTag='>>';
        //token separators
        var sep=':';
        var aliasSep='=>';
        var sourceSep=' --> ';
        var optionSep='|';
        //get token parts
        var getPart=function(partName,includeSep){
            var partVal='';
            //if NOT source part
            if(partName!='source'){
                //if part element exists under .str
                var partElem=strElem.children('.part.'+partName+':first');
                if(partElem.length>0){
                    //if NOT options part
                    if(partName!='options'){
                        //get the value
                        partVal=partElem.html();
                    }else{
                        //for each option
                        partElem.children('.i').each(function(i){
                            //if NOT first option... add option separator
                            if(partVal.length>0){partVal+=optionSep;}
                            //add option
                            partVal+=jQuery(this).html();
                        });
                    }
                }
            }else{
                var sourceElem=strElem.parent().children('.part.source:last');
                if(sourceElem.length>0){
                    partVal=sourceElem.html();
                }
            }
            if(includeSep==undefined){includeSep=true;}
            if(includeSep){
                if(partVal.length>0){
                    switch(partName){
                        case 'type':break;
                        case 'alias':
                            partVal=aliasSep+partVal;
                            break;
                         case 'source':
                             partVal=sourceSep+partVal;
                            break;
                        default:
                            partVal=sep+partVal;
                            break;
                    }
                }
            }
            return partVal;
        };
        var tokenType=getPart('type');
        if(tokenType.length>0){
            //start token string with type
            tokenStr+=startTag+tokenType;
            //depending on the token type...
            switch(tokenType){
                case 'var':
                    tokenStr+=getPart('casing')+getPart('options')+getPart('name')+getPart('alias')+endTag;
                    break;
                case 'filename':
                    tokenStr+=getPart('casing')+getPart('dir')+getPart('name')+endTag+getPart('source');
                    break;
                case 'list':
                    tokenStr+=getPart('name')+endTag;
                    break;
            }
        }
        return tokenStr;
    };
    bodyElem[0]['tokenNavElemToString']=tokenNavElemToString;
    //==GET SELECTED TEMPLATE==
    var getSelectedTemplate=function(){
            //get the selected template from the main view title element
            var tem=mainTitleElem.text();
            return tem.trim();
    };
    bodyElem[0]['getSelectedTemplate']=getSelectedTemplate;
    //gets the <li> element of the selected template (from the templates nav)
    var getTemplateLi=function(temName){
            if(temName==undefined){temName=getSelectedTemplate();}
            var temLi=temLsWrap.find('ul.ls.folders > li[name="'+temName+'"]:first');
            return temLi;
    };
    bodyElem[0]['getTemplateLi']=getTemplateLi;
    //==GET TREE VIEW ROOT==
    //gets the <li> element of the root tree view folder
    var getTreeRootLi=function(){
            var rootUl=treeViewWrap.children('ul:first');
            var rootLi=rootUl.children('li:first');
            return rootLi;
    };
    bodyElem[0]['getTreeRootLi']=getTreeRootLi;
    //get the selected li element in the tree
    var getSelectedTreeLi=function(){
            var selLi=getTreeRootLi();
            //if the root is NOT selected
            if(!selLi.hasClass('selected')){
                    //find the selected <li> somewhere under the root
                    selLi=selLi.find('.selected:first');
            }
            return selLi;
    };
    bodyElem[0]['getSelectedTreeLi']=getSelectedTreeLi;
    //get the path of the selected file or folder in the tree view
    var getSelectedTreePath=function(){
            var path='';
            var selLi=getSelectedTreeLi();
            //if the root is NOT selected
            if(!selLi.hasClass('root')){
                    //get the path for this sub li
                    path=bodyElem[0].getTreePathForElem(selLi);
            }else{
                    //the root is selected so just get the path from the name
                    path=selLi.attr('name');
            }
            return path;
    };
    bodyElem[0]['getSelectedTreePath']=getSelectedTreePath;
    //==GET ALL TOKEN LI ELEMENTS FOR fileLi
    var getTokenLiElems=function(fileLi, tokenTypes){
        var tokenLis;
        //if this fileLi is defined
        if(fileLi!=undefined&&fileLi.length==1){
            //if this fileLi has ANY tokens
            if(fileLi.hasClass('has-tokens')){
                //get the tokens ul for this fileLi
                var tokensUl=fileLi.children('ul.tokens:last');
                //select the token types (or select all types if no types are specified)
                var tokensSelector='li';
                if(tokenTypes!=undefined){
                    if(tokenTypes.length>0){
                        tokensSelector='';
                        //for each token type
                        for(var t=0;t<tokenTypes.length;t++){
                            //if not first type, then add separator
                            if(t!=0){tokensSelector+=',';}
                            //add this token type to the selector
                            tokensSelector+='li[name="'+tokenTypes[t]+'"]';
                        }
                    }
                }
                //if there are any selected tokens
                tokenLis=tokensUl.children(tokensSelector);
                //if there are no selected tokens, then return undefined
                if(tokenLis.length<1){tokenLis=undefined;}
            }
        }
        return tokenLis;
    };
    bodyElem[0]['getTokenLiElems']=getTokenLiElems;
    //==GET RESOLVED FILE PATH FOR FILE LI ELEMENT
    var updateProjectFilePath=function(fileLi){
        var newData=false;
        //if ALL project ID values are known (set by the user)
        if(bodyElem.hasClass('all-project-ids')){
            //if this fileLi is defined
            if(fileLi!=undefined&&fileLi.length==1){
                //if this is NOT a special file (like _filenames.xml)
                if(!fileLi.hasClass('special')){
                    //get the template name
                    var temName=fileLi.parents('li:first').attr('name');
                    //get the template file's name
                    var temFileName=fileLi.attr('name');
                    //get token li elements
                    var fnameLis=getTokenLiElems(fileLi,['filename']);
                    //if there are any filename tokens for this file
                    if(fnameLis!=undefined){
                        var useXml=false; var fnameLi;
                        //if there is more than one filename token
                        if(fnameLis.length>0){
                            //try: only use the filename that came from the _filenames.xml source
                            fnameLi=fnameLis.filter('.has-source').eq(0);
                            //if there is no such sourced filename (should be) ... just use the first filename otherwise
                            if(fnameLi.length<1){fnameLi=fnameLis.eq(0);}
                            else{useXml=true;} //else the filename came from _filenames source (good)
                        }else{
                            //only one filename token
                            fnameLi=fnameLis.eq(0);
                        }
                        //get the str element for this filename token
                        var tokenStrElem=fnameLi.find('.token > .str:first');
                        //get the token string
                        var fnameTokenJson=tokenNavElemToJson(tokenStrElem);
                        //if there is a filenames token string (should be)
                        if(fnameTokenJson!=undefined){
                            //get the <<filename>> token's name and dir values
                            var fnameTokenName='';if(fnameTokenJson.hasOwnProperty('name')){fnameTokenName=fnameTokenJson.name;}
                            var fnameTokenDir='';if(fnameTokenJson.hasOwnProperty('dir')){fnameTokenDir=fnameTokenJson.dir;}
                            //can the filename token dir/name contain any aliases?
                            var pathCanHaveAlias=false;
                            if(isLiteralString(fnameTokenName)){pathCanHaveAlias=true;} //if the filename token name is a string literal
                            else{
                                if(fnameTokenDir.length>0){pathCanHaveAlias=true;} //if the filename has a directory part
                            }
                            //==REPLACE ALIASES IN THE FILENAME TOKEN DIR/NAME==
                            if(pathCanHaveAlias){
                                //if the filename is defined in _filenames.xml
                                var varLis;
                                if(useXml){
                                    //try to get the var tokens from _filenames.xml
                                    var filesUl=fileLi.parent();
                                    var fnamesLi=filesUl.children('li.special:first');
                                    if(fnamesLi.length>0){
                                        //get the var token li elements (they could contain aliases used in the filename path)
                                        varLis=getTokenLiElems(fnamesLi,['var']);
                                    }
                                }else{
                                    //filename NOT defined in _filenames.xml...

                                    //get the var token li elements (they could contain aliases used in the filename path)
                                    varLis=getTokenLiElems(fileLi,['var']);
                                }
                                //if there are any var tokens
                                if(varLis!=undefined&&varLis.length>0){
                                    //get the name of the file where the aliases are written
                                    var aliasFileName=varLis.eq(0).parents('li:first').attr('name');
                                    //for each var token
                                    varLis.each(function(){ 
                                        var strElem=jQuery(this).find('.token > .str:first');
                                        //if this var token has an alias
                                        var aliasElem=strElem.children('.part.alias:last');
                                        if(aliasElem.length>0){
                                            //alias
                                            var aliasStr=aliasElem.html(); 
                                            //get the name part, used to retrieve the alias value
                                            var nameElem=strElem.children('.part.name:last');
                                            var nameStr=nameElem.html();
                                            //alias value
                                            var aliasVal=getTokenValue(temName,nameStr); 
                                            //get the casing part, used to format alias value
                                            var casingElem=strElem.children('.part.casing:last');
                                            var casingStr=casingElem.html();
                                            //format the alias value based on casing rule
                                            aliasVal=getFormattedTokenVal(aliasVal,{'casing':casingStr});
                                            //if the alias placeholder is different from the value to replace it with
                                            if(aliasStr!=aliasVal){
                                                //if the filename token could have aliases in its name
                                                if(isLiteralString(fnameTokenName)){
                                                    //replace the alias with the value for the filename's name literal
                                                    fnameTokenName=replaceAll(fnameTokenName,aliasStr,aliasVal);
                                                }
                                                //if the dir path could have aliases in its name
                                                if(fnameTokenDir.length>0){
                                                    //replace the alias with the value for the filename's dir
                                                    fnameTokenDir=replaceAll(fnameTokenDir,aliasStr,aliasVal);
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                            //==RESOLVE THE EXTENSION==
                            var extension=''; var origTemFileName=temFileName;
                            if(temFileName.indexOf('.')!=-1){
                                extension=temFileName.substring(temFileName.lastIndexOf("."));
                                temFileName=temFileName.substring(0,temFileName.lastIndexOf("."));
                            }
                            //==RESOLVE THE FILE NAME==
                            //if the fnameTokenName of filename token is a dot
                            if(fnameTokenName=='.'){
                                //change this value to the template file name
                                fnameTokenName=temFileName;
                            }else{
                                //if the token name is a literal value
                                if(isLiteralString(fnameTokenName)){
                                    //strip off the starting quote
                                    fnameTokenName=fnameTokenName.substring(1);
                                    //strip off the ending quote
                                    fnameTokenName=fnameTokenName.substring(0, fnameTokenName.length-1);
                                }else{
                                    //token name asks for an input value from the user...

                                    //get this input value for this filename token's name
                                    fnameTokenName=getTokenValue(temName,fnameTokenJson.name);
                                }
                            }
                            //format the input value based in the filename token's casing rule
                            fnameTokenName=getFormattedTokenVal(fnameTokenName,fnameTokenJson);
                            //==RESOLVE THE FILE DIR==
                            if(fnameTokenDir.length>0){fnameTokenDir+='/';}
                            var resolvedPath=fnameTokenDir+fnameTokenName+extension;
                            //==SET THE RESOLVED PATH==
                            //if the old path is different from the new path
                            var oldPath=getTokenValue(temName, origTemFileName, 'project_file_path');
                            if(oldPath!=resolvedPath){
                                //save the resolved path
                                projectChangesMade(temName, 'project_file_path', 'set', {'templateFile':origTemFileName,'resolvedPath':resolvedPath});
                                //indicate the change was made to the data
                                newData=true;
                            }
                        }
                    }
                }
            }
        }
        return newData;
    };
    bodyElem[0]['updateProjectFilePath']=updateProjectFilePath;
    //request all file paths for a project
    var updateProjectFilePaths=function(temName){
        var newData=false;
        if(temName!=undefined){
            //if ALL project ID values are known (set by the user)
            if(bodyElem.hasClass('all-project-ids')){
                //if this template's li element exists
                var temLi=getTemplateLi(temName);
                if(temLi.length>0){
                    //if there are any template files (should be)
                    var fileLis=temLi.find('ul.ls.files > li').not('.special');
                    if(fileLis.length>0){
                        //for each template file
                        fileLis.each(function(){
                            //build the request data for this file
                            if(updateProjectFilePath(jQuery(this))){
                                newData=true;
                            }
                        });
                    }
                }
            }
        }
        return newData;
    };
    bodyElem[0]['updateProjectFilePaths']=updateProjectFilePaths;
    //==SELECT TEMPLATE==
    var selectTemplate=function(temName){
        //if NOT already selected
        var currentTem=mainTitleElem.text();
        if(currentTem!=temName){
            //==MAIN TITLE==
            mainTitleElem.text(temName);
            //==LEFT NAV TEMPLATES==
            //deselect existing selection
            temLsWrap.find('ul.ls.folders > li.selected').removeClass('selected');
            //select the new template
            var temLi=getTemplateLi(temName);
            temLi.addClass('selected');
            //==FILES DROPDOWN==
            fileDropdownsWrap.children('nav.select').removeClass('active');
            var fileSelect=fileDropdownsWrap.children('nav.select[name="'+temName+'"]:first');
            fileSelect.addClass('active');
            //==PROJECT IDS==
            projectIdsWrap.children('.block').removeClass('active');
            projectIdsWrap.children('.block[name="'+temName+'"]:first').addClass('active');
            if(hasAllProjectIds(temName)){bodyElem.addClass('all-project-ids');}
            else{bodyElem.removeClass('all-project-ids');}
            //==SHOW WORKSPACE==
            //remove the no-selected-template indicator to show the workspace content
            workspaceWrap.removeClass('no-selected-template');
            //==INDICATE IF THIS TEMPLATE HAS UNSAVED CHANGES==
            setTemUnsavedChangesFlag(temName);
            //==INDICATE IF THIS TEMPLATE'S PROJECT HAS UNSAVED CHANGES==
            setProjUnsavedChangesFlag(temName);
        }
    };
    bodyElem[0]['selectTemplate']=selectTemplate;
    //==SELECT FILE==
    var selectTemplateFile=function(temName,fName){
        var fileLi;
        //if this file from this template is not already selected (in the main dropdown)
        var fileSelect=fileDropdownsWrap.children('nav.select[name="'+temName+'"]:first');
        var currentFile=fileSelect[0]['currentSelectedFile'];
        if(currentFile!=fName){
            fileSelect[0]['currentSelectedFile']=fName;
            //==LEFT NAV FILE==
            var temLi=getTemplateLi(temName);
            var filesUl=temLi.children('ul.ls.files:first');
            fileLi=filesUl.children('li[name="'+fName+'"]:first');
            //if NOT already selected (in the left nav)
            if(!fileLi.hasClass('selected')){
                //deselect other files in this template
                filesUl.removeClass('special-selected');
                filesUl.children('li.selected').removeClass('selected');
                //if this left nav item exists
                if(fileLi.length>0){
                    //select the new file
                    fileLi.addClass('selected');
                    //if this is a special file, ie: _filenames.xml
                    if(fileLi.hasClass('special')){
                        filesUl.addClass('special-selected');
                    }else{
                        //NOT a special file like _filenames.xml...

                        //==TREE-VIEW FILE==
                        //if all project id values are filled out
                        if(bodyElem.hasClass('all-project-ids')){
                            //if this file path is available
                            var projPath=getTokenValue(temName, fName, 'project_file_path');
                            //*** try to get the resolved file path for this template file
                            treeViewSearchInput.val('/' + projPath); //***
                        }else{
                            //NOT all project id's are filled out...

                            //*** reset treeViewSearchInput.val('')
                        }
                    }
                }
            }
            //==FILES DROPDOWN==
            //if the select is not already correct
            if(fileSelect[0].val()!=fName){
                //set the correct select option
                fileSelect[0].val(fName);
            }
        }
        return fileLi; //returns the fileLi from the left nav
    };
    bodyElem[0]['selectTemplateFile']=selectTemplateFile;
    //==SELECT NEXT FILE==
    var selectNextFile=function(){
        //if there is more than one option
        var fileSelect=fileDropdownsWrap.children('nav.select.active:first');
        if(fileSelect.find('ul li').length>1){
            //get the selected option
            var selFileOption=fileSelect.find('ul li.active:first');
            if(selFileOption.length<1){
                //just get the first option if the selected one isn't found
                selFileOption=fileSelect.find('ul li:first');
            }
            //try to get the next option
            var nextOption=selFileOption.next('li:first');
            if(nextOption.length<1){
                //or just cycle back to the first option
                nextOption=fileSelect.find('ul li:first');
            }
            //select the new option
            var fileLi=selectTemplateFile(fileSelect.attr('name'),nextOption.attr('val'));
            //scroll to selected file
            scrollToHighlight(fileLi);
        }
    };
    bodyElem[0]['selectNextFile']=selectNextFile;
    //==SELECT PREVIOUS FILE==
    var selectPrevFile=function(){
        //if there is more than one option
        var fileSelect=fileDropdownsWrap.children('nav.select.active:first');
        if(fileSelect.find('ul li').length>1){
            //get the selected option
            var selFileOption=fileSelect.find('ul li.active:first');
            if(selFileOption.length<1){
                //just get the first option if the selected one isn't found
                selFileOption=fileSelect.find('ul li:first');
            }
            //try to get the prev option
            var prevOption=selFileOption.prev('li:first');
            if(prevOption.length<1){
                //or just cycle back to the last option
                prevOption=fileSelect.find('ul li:last');
            }
            //select the new option
            var fileLi=selectTemplateFile(fileSelect.attr('name'),prevOption.attr('val'));
            //scroll to selected file
            scrollToHighlight(fileLi);
        }
    };
    bodyElem[0]['selectPrevFile']=selectPrevFile;
    //==SELECT TOKEN==
    var selectTokenInstances=function(temName,json){
        //json can specify what to select. EG: an array of file-names. And a json of token parts to match
        //json = {'files':['file1.txt','file2.txt','file3.txt'], 'token':{'type':'var','name':'my token'}}
        //if the json was provided
        if(json!=undefined){
            //if the json has the minimum amount of required token properties
            if(json.hasOwnProperty('token')&&json.token.hasOwnProperty('type')&&json.token.hasOwnProperty('name')){
                //==WHICH FILES? (LOOP ONCE FOR EACH SECTION)==
                //figure out which files to include in the token selection
                var navFileSelFormat='ul.ls.files li{file-names} ul.tokens li[name="'+json.token.type+'"] .token .str .part.name';
                var navFileSel=navFileSelFormat.replace('{file-names}','');
                //if selecting from any specific file(s) (select from any template file by default)
                if(json.hasOwnProperty('files')){
                    if(json.files.length>0){
                        navFileSel='';
                        for(var f=0;f<json.files.length;f++){
                            //if not the first file, then add the selector separator
                            if(f!=0){navFileSel+=',';}
                            //add to the selector
                            navFileSel+=navFileSelFormat.replace('{file-names}','[name="'+json.files[f]+'"]');
                        }
                    }
                }
                //==LEFT NAV TOKENS==
                //target the template, by name
                var temLi=getTemplateLi(temName);
                if(temLi.length>0){
                    //deselect any other selected tokens in this template
                    temLi.find('ul.tokens > li[name]').removeClass('selected');
                    //get all of the name part elements (under the selected files) in the template
                    var nameElems=temLi.find(navFileSel);
                    //for each name part element in this template
                    nameElems.each(function(){
                        //name must match exactly
                        var tokenName=jQuery(this).text();tokenName=tokenName.trim();
                        if(tokenName==json.token.name){
                            var strElem=jQuery(this).parent();
                            //this is a selected token until proven not to be a match
                            var isMatched=true;
                            var tokenJson=tokenNavElemToJson(strElem);
                            //for each token part
                            for (var partKey in json.token){
                                //no need to compare an options token part
                                if(partKey!='options'){
                                    //if key is an actual property of an object, (not from the prototype)
                                    if (json.token.hasOwnProperty(partKey)){
                                        //if tokenJson also has partKey (needed to match)
                                        if(tokenJson.hasOwnProperty(partKey)){
                                            switch(partKey){
                                                case 'type':break; //already checked
                                                case 'name':break; //already checked
                                                default:
                                                    //if has the NOT the same value
                                                    if(json.token[partKey]!=tokenJson[partKey]){
                                                        isMatched=false;
                                                    }
                                                break;
                                            }
                                        }else{isMatched=false;} //tokenJson doesn't have this token part
                                    }
                                }
                                //if not a match, then stop comparing the token parts
                                if(!isMatched){break;}
                            }
                            //if the token parts match up
                            if(isMatched){
                                //then this token should be selected
                                strElem.parents('li:first').addClass('selected');
                            }
                        }
                    });
                }
                //==SOME OTHER TOKEN SELECTIONS==
                //... +++
            }
        }
    };
    bodyElem[0]['selectTokenInstances']=selectTokenInstances;
    //==SELECT TREE NODE (FILE OR FOLDER)==
    var selectTreeNode=function(path){
        //if the element for this path exists
        var pathLi=bodyElem[0].getTreeElem(path);
        if(pathLi!=undefined&&pathLi.length>0){
            //if NOT already selected
            if(!pathLi.hasClass('selected')){
                //==TREE VIEW==
                //deselect any current selected node
                treeViewWrap.find('ul.tree-root li.selected').removeClass('selected');
                //select this clicked node
                pathLi.addClass('selected');
            }
        }
    };
    bodyElem[0]['selectTreeNode']=selectTreeNode;
    //==GET FOLDER PATH FOR CURRENT TREE ELEMENT==
    var getTreePathForElem=function(elem){
            var path='';
            //if this element is <li> with a name
            if(elem[0].tagName.toLowerCase()=='li'){
                    var liName=elem.attr('name');
                    if(liName!=undefined&&liName.length>0){
                            //the path ends with the current li's name value
                            path=liName;
                    }
            }
            //build the path to open
            var dirLis=elem.parents('li.dir');
            dirLis.each(function(i){
                    //get this dir name
                    var dirName=jQuery(this).attr('name');
                    //if NOT the first dirName... then add separator
                    if(path.length>0){dirName+='/';}
                    //prepend the dir name to the path
                    path=dirName+path;
            });
            return path;
    };
    bodyElem[0]['getTreePathForElem']=getTreePathForElem;
    //==GET TREE ELEMENT FOR PATH==
    //get an <li> element, from the tree-view, at the given path
    var getTreeElem=function(path){
            var elem;
            var rootLi=getTreeRootLi();
            var rootPath=rootLi.attr('name');
            //if path starts with /
            if(path.indexOf('/')==0){
                    //if the path is ONLY /
                    if(path=='/'){path=rootPath;} //then replace / with the full root path
                    else{
                            //the path starts with / but also has additional stuff after /...

                            //if the root path ALSO starts with /
                            if(rootPath.indexOf('/')==0){
                                    //if the path (that starts with /) does NOT start with rootPath (rootPath also starts with /)
                                    if(path.indexOf(rootPath)!=0){
                                            path=rootPath+path; //make sure the rootPath is tacked on to the front
                                    }
                            }else{
                                    //the root path does NOT also start with /
                                    path=rootPath+path; //make sure the rootPath is tacked on to the front
                            }
                    }
            }
            //if the given path starts the rootPath
            if(path.indexOf(rootPath)==0){
                    //if the path extends beyond the root
                    if(path.length>rootPath.length){
                            //remove the root from the path
                            path=path.substring(rootPath.length+'/'.length);
                            //get each folder split
                            var dirs=path.split('/');
                            elem=rootLi;
                            //for each dir
                            for(var d=0;d<dirs.length;d++){
                                    var dir=dirs[d];
                                    //get next folder down
                                    elem=elem.find('li[name="'+dir+'"]:first');
                                    //if this folder/file doesn't exist
                                    if(elem.length<1){
                                            //return nothing and stop the search
                                            elem=undefined;
                                            break;
                                    }
                            }
                    }else{
                            //the path selects the root <li>
                            elem=rootLi;
                    }
            }
            return elem;
    };
    bodyElem[0]['getTreeElem']=getTreeElem;
    //==TURN FILE ON/OFF==
    var toggleOnOffFile=function(temName,fName){
            //==GET FILES DROPDOWN==
            var fileSelect=fileDropdownsWrap.children('nav.select[name="'+temName+'"]:first');
            var selectedLi=fileSelect.find('ul li[val="'+fName+'"]:first');
            //if this file exists
            if(selectedLi.length>0){
                    //==GET LEFT NAV FILE==
                    var temLi=getTemplateLi(temName);
                    var fileLi=temLi.find('ul.ls.files > li[name="'+fName+'"]:first');
                    //if already on
                    if(selectedLi.hasClass('on')){
                            //==TOGGLE FILES DROPDOWN OFF==
                            selectedLi.removeClass('on');
                            selectedLi.addClass('off');
                            //==TOGGLE LEFT NAV FILE OFF==
                            fileLi.removeClass('on');
                            fileLi.addClass('off');
                    }else{
                            //file no already on...

                            //==TOGGLE FILES DROPDOWN ON==
                            selectedLi.removeClass('off');
                            selectedLi.addClass('on');
                            //==TOGGLE LEFT NAV FILE ON==
                            fileLi.removeClass('off');
                            fileLi.addClass('on');
                    }
            }
    };
    bodyElem[0]['toggleOnOffFile']=toggleOnOffFile;
    //==ATTACH EVENTS, INTERNAL FUNCTIONS==
    //evs +/- button for open/close under temLsWrap
    var evsTemToggleOpenClose=function(){
            //add opened-closed toggle events (to elements that don't already have events added)
            var openCloseElems=temLsWrap.find('.opened-closed').not('.evs');
            //mark these elements as having the events attached
            openCloseElems.addClass('evs');
            openCloseElems.click(function(){
                    //get the parent li wrapper
                    var parentLi=jQuery(this).parents('li:first');
                    //if currently closed
                    if(parentLi.hasClass('closed')){
                            //open it
                            parentLi.removeClass('closed');
                            parentLi.addClass('opened');
                    }else{
                            //currently open, so close it
                            parentLi.addClass('closed');
                            parentLi.removeClass('opened');
                    }
            });
    };
    bodyElem[0]['evsTemToggleOpenClose']=evsTemToggleOpenClose;
    //evs +/- button for open/close under treeViewWrap
    var evsTreeToggleOpenClose=function(){
            //add opened-closed toggle events (to elements that don't already have events added)
            var openCloseElems=treeViewWrap.find('.opened-closed').not('.evs');
            //prevent select on these open/close buttons
            preventSelect(openCloseElems);
            //mark these elements as having the events attached
            openCloseElems.addClass('evs');
            openCloseElems.click(function(){
                    //get the parent li wrapper
                    var parentLi=jQuery(this).parents('li:first');
                    //if NOT already processing
                    if(!parentLi.hasClass('processing')){
                            //if currently closed
                            if(parentLi.hasClass('closed')){
                                    //open it
                                    parentLi.removeClass('closed');
                                    parentLi.addClass('opened');
                                    //if the folder content data isn't loaded yet
                                    var childUl=parentLi.children('ul:first');
                                    if(childUl.hasClass('empty')){
                                            //if NOT confirmed as empty... yet
                                            if(!childUl.hasClass('confirmed')){
                                                    //get the path that needs data
                                                    var path=getTreePathForElem(childUl);
                                                    if(path.length>0){
                                                            //add getting class
                                                            parentLi.addClass('processing');
                                                            //request that Java pulls in the folder contents (if any)
                                                            appendToTree(path);
                                                    }
                                            }
                                    }
                            }else{
                                    //currently open, so close it
                                    parentLi.addClass('closed');
                                    parentLi.removeClass('opened');
                            }
                    }
            });
    };
    bodyElem[0]['evsTreeToggleOpenClose']=evsTreeToggleOpenClose;
    //tree root options button
    var evsTreeRootOptionsBtn=function(){
            //get the browse button
            var optionsBtn=treeViewWrap.find('ul.tree-root > li.dir.root > .dir-lbl > .options:first').not('.evs');
            optionsBtn.addClass('evs');
            optionsBtn.click(function(){
                    //get the current root path
                    var rootLi=getTreeRootLi();
                    var currentPath=rootLi.attr('name');
                    //browse for new root path
                    browseTreeRoot(currentPath);
            });
    };
    bodyElem[0]['evsTreeRootOptionsBtn']=evsTreeRootOptionsBtn;
    //add the events to select tree view nodes
    var evsTreeSelectNode=function(){
            var rootUl=treeViewWrap.children('ul.tree-root:first');
            var selBtns=rootUl.find('.dir-lbl > .path, .dir-lbl > .name, .file-lbl > .name').not('evs');
            selBtns.addClass('evs');
            selBtns.click(function(){
                    //if NOT already selected
                    var parentLi=jQuery(this).parents('li:first');
                    if(!parentLi.hasClass('selected')){
                            //select the tree node
                            var path=getTreePathForElem(parentLi);
                            selectTreeNode(path);
                    }
            });
    };
    bodyElem[0]['evsTreeSelectNode']=evsTreeSelectNode;
    //evs +/- open a folder from treeViewWrap
    var evsTreeOpenFolder=function(){
            //add opened-closed toggle events (to elements that don't already have events added)
            var openDirElems=treeViewWrap.find('.dir-lbl > .icon').not('.evs');
            //prevent select on these folder buttons
            preventSelect(openDirElems);
            //mark these elements as having the events attached
            openDirElems.addClass('evs');
            openDirElems.click(function(){
                    //get the tree path for this open/close element
                    var path=getTreePathForElem(jQuery(this));
                    //request Java open this path
                    if(path.length>0){openDir(path);}
            });
    };
    bodyElem[0]['evsTreeOpenFolder']=evsTreeOpenFolder;
    //evs on off button for files
    var evsToggleFileOnOff=function(){
            var onOffElems=temLsWrap.find('.on-off').not('.evs');
            //mark these elements as having the events attached
            onOffElems.addClass('evs');
            onOffElems.click(function(){
                    var fileLi=jQuery(this).parents('li:first');
                    var temLi=fileLi.parents('li:first');
                    bodyElem[0].toggleOnOffFile(temLi.attr('name'),fileLi.attr('name'));
            });
    };
    bodyElem[0]['evsToggleFileOnOff']=evsToggleFileOnOff;
    //evs for templates
    var evsTemplates=function(){
            //select events for templates
            var dirPathElems=temLsWrap.find('ul.ls.folders li .dir > .path').not('.evs');
            dirPathElems.addClass('evs');
            dirPathElems.click(function(){
                    //select the template
                    bodyElem[0].selectTemplate(jQuery(this).text());
            });
    };
    bodyElem[0]['evsTemplates']=evsTemplates;
    //evs for files
    var evsFiles=function(){
            //select events for files
            var fileNameElems=temLsWrap.find('ul.ls.files li .file > .name').not('.evs');
            fileNameElems.addClass('evs');
            fileNameElems.click(function(){
                    var fileParent=jQuery(this).parent();
                    var liParent=fileParent.parent();
                    var temLiParent=liParent.parents('li:first');
                    var temPathBtn=temLiParent.find('.dir .path:first');
                    //select the template
                    var temName=temPathBtn.text();
                    bodyElem[0].selectTemplate(temName);
                    //select the template file
                    bodyElem[0].selectTemplateFile(temName,jQuery(this).text());
            });
    };
    bodyElem[0]['evsFiles']=evsFiles;
    //evs for tokens
    var evsTokens=function(){
            //select events for tokens
            var tokenElems=temLsWrap.find('ul.ls.folders ul.ls.files ul.tokens li .token > .str').not('.evs');
            tokenElems.addClass('evs');
            tokenElems.click(function(){
                    var tokensUl=jQuery(this).parents('ul.tokens:first');
                    var fileLi=tokensUl.parent();
                    var fileNameBtn=fileLi.find('.file .name:first');
                    var temLi=fileLi.parents('li:first');
                    var temPathBtn=temLi.find('.dir .path:first');
                    //select the template
                    var temName=temPathBtn.text();
                    bodyElem[0].selectTemplate(temName);
                    //select the template file
                    var fileName=fileNameBtn.text();
                    var sourceFile=fileName;
                    //select the true source of the token, if the source is NOT the current file, ie: _filenames.xml
                    var tokenJson=tokenNavElemToJson(jQuery(this));
                    if(tokenJson.hasOwnProperty('source')){sourceFile=tokenJson.source;}
                    bodyElem[0].selectTemplateFile(temName,sourceFile);
                    //select the token
                    bodyElem[0].selectTokenInstances(temName,
                    {
                            'files':[fileName], 'token':tokenJson
                    });
            });
    };
    bodyElem[0]['evsTokens']=evsTokens;
    //evs for project id change (blur input)
    var evsEditProjectId=function(){
        var doInputEdit=function(inputElem){
            //check to see if all project ids were entered BEFORE this user action
            var hasAllIds=false;if(bodyElem.hasClass('all-project-ids')){hasAllIds=true;}
            //get active template name
            var temName=getSelectedTemplate();
            //get token data
            var inputParent=inputElem.parent(); var inputLabel=inputParent.children('.label:first');
            var idName=inputLabel.text(); idName=idName.trim();
            //if value isn't blank
            var idVal=inputElem.val(); idVal=idVal.trim();
            if(idVal.length>0){
                //handle project id edit
                projectChangesMade(temName, 'token_value', 'set', {'name':idName,'value':idVal});
            }else{
                //the field is blank... make sure this token value is blank
                projectChangesMade(temName, 'token_value', 'del', {'name':idName,'value':idVal});
            }
            //if previously did NOT have all project ids
            if(!hasAllIds){
                //if all ids are filled out now
                if(hasAllProjectIds(temName)){
                    //indicate all project ids are there
                    bodyElem.addClass('all-project-ids');
                    //resolve all of the file paths for this template
                    updateProjectFilePaths(temName);
                }
            }else{
                //previously had ALL project ids...
                
                //if it doesn't have ALL project ids now
                if(!hasAllProjectIds(temName)){
                    //indicate not all project ids are there
                    bodyElem.removeClass('all-project-ids');
                    //delete resolved file paths that were once there
                    projectChangesMade(temName, 'project_file_path', 'del');
                }
            }
        };
        //input elements for project ids
        var projIdInputs=projectIdsWrap.find('input.id-val').not('.evs');
        projIdInputs.addClass('evs');
        //input blur
        projIdInputs.blur(function(){doInputEdit(jQuery(this));});
        //input key pressed
        projIdInputs.keyup(function(e){
            switch(e.keyCode){
                case 13: //enter key pressed
                    e.preventDefault();
                    doInputEdit(jQuery(this));
                break;
            }
        });
    };
    bodyElem[0]['evsEditProjectId']=evsEditProjectId;
    //evs for include rules
    var evsIncludeRules=function(){
            //select events for include rules
            var includeRuleElems=temLsWrap.find('ul.ls.folders li ul.includes li.has-includes ul li').not('.evs');
            includeRuleElems.addClass('evs');
            var delBtns=includeRuleElems.children('.del');
            delBtns.html(getSvg('x'));
            delBtns.click(function(){
                    //get include rule text
                    var parentLi=jQuery(this).parent();
                    var incElem=parentLi.children('.inc:first');
                    var incStr=incElem.text();
                    //get template name
                    var temLi=parentLi.parent().parent().parent().parent();
                    var temName=temLi.attr('name');
                    //make the change
                    bodyElem[0].templateChangesMade(temName,'include_rule','del',incStr);
            });
            includeRuleElems.children('.inc').click(function(){
                    //get include rule string
                    var includeRuleStr=jQuery(this).text();
                    //get template name
                    var includesUl=jQuery(this).parents('ul.includes:first');
                    var temLi=includesUl.parent();
                    var temName=temLi.attr('name');
                    //select template
                    bodyElem[0].selectTemplate(temName);
                    //select include rule string
                    bodyElem[0].selectIncludeRule(temName,includeRuleStr);
            });
    };
    bodyElem[0]['evsIncludeRules']=evsIncludeRules;
    //==UPDATE TEMPLATE/FILE/TOKEN LISTING==
    var updateTemplates=function(json){
        if(json!=undefined){
            //==SET THE TEMPLATES LISTING HTML==
            var htm=htm_template_dirs(json); //get html
            temLsWrap.html(''); //clear old html
            temLsWrap.append(htm.templates); //set html
            //make sure the special files, ie: _filenames.xml are first in the listings
            var specialLi=temLsWrap.find('ul.ls.files > li.special');
            specialLi.each(function(){
                //make the special file first in the file listing
                var parentUl=jQuery(this).parent();
                parentUl.prepend(jQuery(this));
            });
            //==SET THE FILE DROPDOWNS HTML==
            fileDropdownsWrap.children('nav.select[name]').remove(); //clear old html
            fileDropdownsWrap.append(htm.file_selects); //set html
            //make sure the special files, ie: _filenames.xml are first in the listings
            var specialOption=fileDropdownsWrap.find('nav.select > ul li.special');
            specialOption.each(function(){
                //make the special file second in the file listing
                var parentUl=jQuery(this).parent();
                var firstOption=parentUl.children('li:first');
                firstOption.after(jQuery(this));
            });
            //==PROJECT IDS LISTINGS FOR EACH TEMPLATE==
            projectIdsWrap.html(''); //clear old html
            projectIdsWrap.append(htm.project_ids); //set html
            //==HOME-MADE SELECT DROPDOWNS VAL FUNCTION==
            var selectDrops=contentWrap.find('nav.select').not('.evs');
            selectDrops.each(function(){
                var thisDropDown=jQuery(this);
                thisDropDown.addClass('evs');
                //prevent selection inside the select dropdown
                preventSelect(thisDropDown.children('ul:first'));
                //hover event
                thisDropDown.hover(function(){
                    //hover
                    jQuery(this).addClass('over');
                },function(){
                    //end hover and close, if open
                    jQuery(this).removeClass('over');
                    jQuery(this).removeClass('open');
                });
                //dropdown label click
                thisDropDown.children('.lbl:first').click(function(){
                    //if dropdown is open
                    var thisDropDown=jQuery(this).parent();
                    if(thisDropDown.hasClass('open')){
                        //then close it
                        thisDropDown.removeClass('open');
                    }else{
                        //otherwise, open it
                        thisDropDown.addClass('open');
                        //scroll to the selected item
                        var activeItem=thisDropDown.find('ul li.active:first');
                        if(activeItem.length>0){
                            var ulParent=activeItem.parent();
                            //the index at which the item appears in the list
                            var activeItemIndex=activeItem.index();
                            //the height of the item
                            var activeItemHeight=activeItem.outerHeight();
                            //calculate the height of item stack BEFORE the selected item
                            var scrollPos=activeItemHeight*activeItemIndex;
                            //scroll to the item so it appears at the top of the scroll
                            ulParent.scrollTop(scrollPos);
                        }
                    }
                });
                //val function for home-made select dropdown
                var val=function(valToSelect){
                    var retVal='';
                    //if there are any list items
                    var listItems=thisDropDown.find('ul li');
                    if(listItems.length>0){
                        //get the active list item txt element(s)
                        var selectedTxt=listItems.filter('.active').children('.txt');
                        //if there is no active selected item
                        if(selectedTxt.length<1){
                            //select the first item by default
                            thisDropDown.find('ul li:first').addClass('active');
                        }
                        //get ONLY ONE selected text item
                        var firstSelectedTxt=selectedTxt.eq(0);
                        //if there is more than one selected
                        if(selectedTxt.length>1){
                            //make sure only one item is selected
                            selectedTxt.not(firstSelectedTxt).parent().removeClass('active');
                        }
                        //if setting a new selected value
                        var setNewTxt=false;
                        if(valToSelect!=undefined){
                            //if this new item value exists
                            var newSelectLi=listItems.filter('[val="'+valToSelect+'"]:first');
                            if(newSelectLi.length>0){
                                //deselect previous item
                                listItems.not(newSelectLi).removeClass('active');
                                //select the new item
                                newSelectLi.addClass('active');
                                //set the new text
                                selectedTxt=newSelectLi.children('.txt:first');
                                setNewTxt=true;
                            }
                        }
                        //get the value of this first item
                        retVal=selectedTxt.text();
                        retVal=retVal.trim();
                        //if new text was set
                        if(setNewTxt){
                            //make sure the label text is correct
                            var selectLbl=thisDropDown.children('.lbl:first');
                            //if the text has changed
                            if(selectLbl!=retVal){
                                //set the new text
                                selectLbl.text(retVal);
                                //fire change event
                                var parentId=thisDropDown.parents('[id]:first').attr('id');
                                switch(parentId){
                                    case 'main-view': //main view file dropdown
                                        //select the template
                                        var temName=thisDropDown.attr('name');
                                        bodyElem[0].selectTemplate(temName);
                                        //select the template file
                                        var fileLi=bodyElem[0].selectTemplateFile(temName,retVal);
                                        //make sure the selected file is scrolled into view in the left nav
                                        scrollToHighlight(fileLi);
                                        break;
                                    case 'project-ids':
                                        //get the token name for this dropdown
                                        var fieldWrap=thisDropDown.parent();
                                        var idName=fieldWrap.attr('name');
                                        //get the template name for this dropdown
                                        var temName=fieldWrap.parent().attr('name');
                                        //if NOT selected the default value
                                        var valAttr=listItems.filter('.active:first').attr('val');
                                        retVal=retVal.trim();
                                        if (retVal.length>0&&valAttr!='...'){
                                            //handle project id edit
                                            projectChangesMade(temName, 'token_value', 'set', {'name':idName,'value':retVal});
                                        }else{
                                            //selected the default value...

                                            //handle project id delete
                                            projectChangesMade(temName, 'token_value', 'del', {'name':idName});
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    return retVal;
                };
                thisDropDown[0]['val']=val;
                //click event for select items
                var txtElems=thisDropDown.find('ul li .txt').not('.evs');
                txtElems.addClass('evs');
                txtElems.click(function(){
                        //select item on click
                        var thisDropDown=jQuery(this).parents('nav.select:first');
                        var txtVal=jQuery(this).parent().attr('val'); txtVal=txtVal.trim();
                        thisDropDown[0].val(txtVal);
                        //close the dropdown
                        thisDropDown.removeClass('open');
                });
                //hover event for click items
                txtElems.hover(function(){
                        jQuery(this).parent().addClass('over');
                },function(){
                        jQuery(this).parent().removeClass('over');
                });
                //click events for on-off buttons
                var onOffBtns=thisDropDown.find('ul li .on-off').not('.evs');
                onOffBtns.addClass('evs');
                onOffBtns.click(function(){
                        var parentLi=jQuery(this).parent();
                        bodyElem[0].toggleOnOffFile(thisDropDown.attr('name'),parentLi.attr('val'));
                });
            });
            //==ADD JS EVENTS TO NEW ELEMENTS==
            //add opened-closed toggle events (to elements that don't already have events added)
            evsTemToggleOpenClose();
            //add on-off toggle events (to elements that don't already have events added)
            evsToggleFileOnOff();
            //select events for templates
            evsTemplates();
            //select events for files
            evsFiles();
            //select events for tokens
            evsTokens();
            //select events for include rules
            evsIncludeRules();
            //detect when a project id has been modified
            evsEditProjectId();
            //==SELECT / OPEN THE FIRST TEMPLATE ON PAGE LOAD==
            //if there is no selected template
            var selectedTemplate=temLsWrap.find('ul.ls.folders > li.selected');
            if(selectedTemplate.length<1){
                    //select the first template by default
                    var firstDirElem=temLsWrap.find('ul.ls.folders li .dir:first');
                    //if there is a template
                    if(firstDirElem.length>0){
                            //find the first template select button
                            var firstTemBtn=firstDirElem.children('.path:first');
                            firstTemBtn.click(); //select the first template by default on app load
                    }
            }
        }
    };
    bodyElem[0]['updateTemplates']=updateTemplates;
    if(getTestInBrowser()){
            //for testing in a browser
            bodyElem[0].updateTemplates(sample_json_templates3());
    }
    //==UPDATE TREE VIEW LISTING==
    var updateTreeView=function(json,appendToPath){
            if(json!=undefined){
                    //get tree view html
                    var htm=htm_tree_view_dir(json);
                    //if clean slate
                    if(appendToPath==undefined){
                            //complete fresh start
                            treeViewWrap.html(''); //clear old html
                            treeViewWrap.append(htm); //set html
                    }else{
                            //appending new tree structure to the existing structure...

                            //if this is a FOLDER not a FILE
                            var doAppend=true;
                            if(json.hasOwnProperty('dir')){
                                    //if there were NO sub files/folders to append
                                    if(!json.hasOwnProperty('ls')){
                                            doAppend=false;
                                    }
                            }else{
                                    //if doesn't have file data either
                                    if(!json.hasOwnProperty('file')){doAppend=false;}
                            }
                            //did the json data have the required data?
                            if(doAppend){
                                    //if there is an <li> element, with this appendToPath
                                    var appendToLi=getTreeElem(appendToPath);
                                    if(appendToLi!=undefined&&appendToLi.length>0){
                                            //get the parent and the name of this li
                                            var liName=appendToLi.attr('name');
                                            var ulParent=appendToLi.parent();
                                            //save li state, to be restored on the replacement li
                                            var isSelected=false;if(appendToLi.hasClass('selected')){isSelected=true;}
                                            //append the new tree branches to the existing tree and remove the old, less complete, branch
                                            appendToLi.after(htm);appendToLi.remove();
                                            //make sure the new branch is open
                                            var newLi=ulParent.children('li[name="'+liName+'"]:first');
                                            if(newLi.hasClass('closed')){
                                                    //make sure it's open
                                                    newLi.addClass('opened');
                                                    newLi.removeClass('closed');
                                                    //restore state for this replacement li
                                                    if(isSelected){newLi.addClass('selected');}
                                            }
                                    }
                            }else{
                                    //json value didn't have the required data
                                    failedAppendToTree(appendToPath);
                            }
                    }
                    //==SET TREE VIEW EVENTS==
                    //allow open and closing of folders +/-
                    evsTreeToggleOpenClose();
                    //open folder buttons
                    evsTreeOpenFolder();
                    //root folder options button
                    evsTreeRootOptionsBtn();
                    //add ability to select tree nodes
                    evsTreeSelectNode();
            }
    };
    bodyElem[0]['updateTreeView']=updateTreeView;
    if(getTestInBrowser()){
            //for testing in a browser
            bodyElem[0].updateTreeView(sample_json_treeview2());
    }
    //==WINDOW READY==
    jQuery(window).ready(function(){
            //==TEMPLATES RESIZE==
            temResizeHandle.draggable({
                    'addClasses':false,
                    'axis':'x',
                    'zIndex':999,
                    'stop':function(e,ui){
                            //when the drag stops...

                            //calculate the percentage position where the drag stopped
                            var newLeftOffset=ui.helper.offset().left;
                            var helperWidth=ui.helper.outerWidth();
                            var newRightOffset=newLeftOffset+helperWidth;
                            var windowWidth=jQuery(window).outerWidth();
                            var templatesPercent=(newRightOffset/windowWidth)*100;
                            var workspacePercent=100-templatesPercent;
                            //set the new percentage widths
                            templatesWrap.css('width',templatesPercent+'%');
                            workspaceWrap.css('width',workspacePercent+'%');
                            //remove the extra style junk from the drag handle
                            temResizeHandle.removeAttr('style');
                    }
            });
            //==INPUT VIEW RESIZE==
            inputResizeHandle.draggable({
                    'addClasses':false,
                    'axis':'y',
                    'zIndex':999,
                    'stop':function(e,ui){
                            //when the drag stops...

                            //calculate the percentage position where the drag stopped
                            var newTopOffset=ui.helper.offset().top;
                            var workspaceTop=workspaceWrap.offset().top;
                            newTopOffset-=workspaceTop; //remove the space where the content does not reach
                            var workspaceHeight=workspaceWrap.innerHeight();
                            var newInputHeight=workspaceHeight-newTopOffset;
                            var inputHeightPercent=(newInputHeight/workspaceHeight)*100;
                            var mainHeightPercent=100-inputHeightPercent;
                            //set the new percentage heights
                            inputViewWrap.css('height',inputHeightPercent+'%');
                            mainViewWrap.css('height',mainHeightPercent+'%');
                            //remove the extra style junk from the drag handle
                            inputResizeHandle.removeAttr('style');
                    }
            });
            //==WINDOW RESIZE==
            //do stuff on window resize or window ready
            var onResize=function(){
                    var windowHeight=jQuery(window).outerHeight();
                    //make sure the menu-bar and content share the space exactly
                    var menuBarOffset=menuBarWrap.outerHeight();
                    contentWrap.css('top', menuBarOffset+'px');
                    contentWrap.css('height', (windowHeight-menuBarOffset)+'px');
                    //make sure the templates header and the templates content share the space exactly
                    var temHeaderHeight=temHeaderWrap.outerHeight();
                    var contentWrapHeight=contentWrap.innerHeight();
                    temContentWrap.css('height',(contentWrapHeight-temHeaderHeight)+'px');
            };
            var resize_timeout;
            jQuery(window).resize(function(){
                    clearTimeout(resize_timeout);
                    var resize_timeout=setTimeout(function(){
                            //do stuff on window resize
                            onResize();
                    },100);
            });
            //do stuff on window ready
            onResize();
    });
});
//generic replace all function
function replaceAll(theStr, charToReplace, replaceWith) {
    if(theStr != undefined){
        if(charToReplace==undefined){charToReplace='';}
        if(replaceWith==undefined){replaceWith='';}
        if(charToReplace!=replaceWith){
            while(theStr.indexOf(charToReplace)!=-1){
                theStr=theStr.replace(charToReplace,replaceWith);
            }
        }
    }else{theStr='';}
    return theStr;
}
//make a request to java to open a folder
function openDir(type){
	//if a folder type was given
	if(type!=undefined){
		//if the type data element does NOT already exist
		var bodyElem=jQuery('body:first');
		var typeElem=bodyElem.children('#nf_open_folder:last');
		if(typeElem.length<1){
			//create the type data element
			bodyElem.append('<div id="nf_open_folder" style="display:none;"></div>');
			typeElem=bodyElem.children('#nf_open_folder:last');
		}
		//set the type data
		typeElem.text(type);
		//trigger the event
		document.dispatchEvent(new Event('nf_open_folder'));
	}
}
//failed to append to the tree, perhapse because the folder that was opened is actually empty
function failedAppendToTree(path){
	//if the element that was trying to open is found
	var elemLi=document.body.getTreeElem(path);
	if(elemLi!=undefined&&elemLi.length>0){
		//element is no longer processing
		elemLi.removeClass('processing');
		//indicate that this element was confirmed as empty
		elemLi.children('.empty:first').addClass('confirmed');
	}
}
//browse tree root folder
function browseTreeRoot(currentRootPath){
	if(currentRootPath!=undefined&&typeof currentRootPath=='string'){
		//if the type data element does NOT already exist
		var bodyElem=jQuery('body:first');
		var dataElem=bodyElem.children('#nf_browse_tree_root:last');
		if(dataElem.length<1){
			//create the type data element
			bodyElem.append('<div id="nf_browse_tree_root" style="display:none;"></div>');
			dataElem=bodyElem.children('#nf_browse_tree_root:last');
		}
		//set the type data
		dataElem.html(currentRootPath);
		//trigger the event
		document.dispatchEvent(new Event('nf_browse_tree_root'));
	}
}
//make a request to java to load a tree view folder
function appendToTree(path,maxLevels){
	if(path!=undefined&&typeof path=='string'){
		if(maxLevels==undefined){maxLevels=2;}
		//if the type data element does NOT already exist
		var bodyElem=jQuery('body:first');
		var dataElem=bodyElem.children('#nf_append_to_tree:last');
		if(dataElem.length<1){
			//create the type data element
			bodyElem.append('<div id="nf_append_to_tree" style="display:none;"></div>');
			dataElem=bodyElem.children('#nf_append_to_tree:last');
		}
		//set the type data
		dataElem.html('<path>'+path+'</path><max_levels>'+maxLevels+'</max_levels>');
		//trigger the event
		document.dispatchEvent(new Event('nf_append_to_tree'));
	}
}
//refresh the tree to reflect changes outside of the newfiles gui
function refreshTreePath(path){
	if(path==undefined){path=document.body.getSelectedTreePath();}
	if(path.length>0){
		//if the type data element does NOT already exist
		var bodyElem=jQuery('body:first');
		var dataElem=bodyElem.children('#nf_refresh_tree_path:last');
		if(dataElem.length<1){
			//create the type data element
			bodyElem.append('<div id="nf_refresh_tree_path" style="display:none;"></div>');
			dataElem=bodyElem.children('#nf_refresh_tree_path:last');
		}
		//set the type data
		dataElem.html(path);
		//trigger the event
		document.dispatchEvent(new Event('nf_refresh_tree_path'));
	}
}
//prevent the element or element children from being selected
function preventSelect(elem){
	elem.bind('selectstart',function(e){e.preventDefault();return false;});
}
//gets the contents of a function, eg: function functionName(){/* ...contents... */}
function getFuncStr(functionName){
	var functionContent = '';
	//if this function exists
	if(window.hasOwnProperty(functionName)){
		//get code inside the function object
		functionContent = window[functionName];
		functionContent = functionContent.toString();
		//strip off the function string
		var startCode = '{/*';var endCode = '*/}';
		//safari tries to be helpful by inserting a ';' at the end of the function code if there is not already a ';'
		if (functionContent.lastIndexOf(endCode) == -1) {endCode='*/;}';}
		//strip off everything left of, and including startCode
		functionContent = functionContent.substring(functionContent.indexOf(startCode) + startCode.length);
		//strip off everything right of, and including endCode
		functionContent = functionContent.substring(0, functionContent.lastIndexOf(endCode));
		functionContent = functionContent.trim();
	}
	return functionContent;
}
//go to a specific tab view by passing the corresponding tab button to the function
function goToTabView(tabBtn){
	tabBtn=jQuery(tabBtn);
	var tabName=tabBtn.attr('name');
	//get the parent wrap for the tabs
	var tabsNav=tabBtn.parent();
	var mainViewWrap=tabsNav.parents('#main-view:first');
	//if this isn't the currently selected tab
	if(!mainViewWrap.hasClass(tabName)){
		//find which tab is selected, and deselect
		var otherTabs=tabsNav.children().not(tabBtn);
		otherTabs.each(function(){
			//if this tab is selected
			var otherTabName=jQuery(this).attr('name');
			if(mainViewWrap.hasClass(otherTabName)){
				//remove this previous tab from the class list of mainViewWrap
				mainViewWrap.removeClass(otherTabName);
			}
		});
		//select the new tab
		mainViewWrap.addClass(tabName);
		//remove active class from current content
		var contentWraps=mainViewWrap.children('.content');
		contentWraps.removeClass('active');
		//add active class to new selected content
		contentWraps.filter('[name="'+tabName+'"]:first').addClass('active');
	}
}
function toggleHiddenSystemFiles(btn){
	btn=jQuery(btn);
	var treeViewContent=btn.parents('.content[name="tree-view"]:first');
	if(btn.hasClass('on')){
		btn.removeClass('on');
		treeViewContent.removeClass('hidden-system-files');
	}else{
		btn.addClass('on');
		treeViewContent.addClass('hidden-system-files');
	}
}
