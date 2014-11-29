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
	var temResizeHandle=templatesWrap.children('.resize.width:last');
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
	//==DEFINE EVENTS THAT DON'T HAVE TO BE RE-DEFINED AFTER DYNAMIC CONTENT CHANGES==
	//SIZE CONTROLS (EG: PIN/UNPIN)
	var sizeCtlsWraps=bodyElem.find('.size-controls');
	//pin/un-pin column
	var pinElems=sizeCtlsWraps.children('.pin');
	pinElems.click(function(){
		//get the parent element that is affected by pinned/unpinned state
		var parentPinElem=jQuery(this).parents('.pinned-unpinned:first');
		//get the elements that share the space with the pinned-unpinned element
		var rowWrap=parentPinElem.parent();
		var spaceShareElems=rowWrap.children().not(parentPinElem);
		//if the parent element doesn't already have the hover event
		if(!parentPinElem.hasClass('evs')){
			parentPinElem.addClass('evs');
			//add the hover event
			var slider_timeout;
			parentPinElem.hover(function(){
				//if unpinned
				if(parentPinElem.hasClass('unpinned')){
					//after a delay
					slider_timeout=setTimeout(function(){
						//remove slide-in class on hover
						rowWrap.removeClass('slide-in');
						rowWrap.addClass('slide-out');
					}, 400);
				}
			},function(){
				//add slide-in class on hover out
				clearTimeout(slider_timeout);
				//if unpinned
				if(parentPinElem.hasClass('unpinned')){
					rowWrap.addClass('slide-in');
				}else{rowWrap.removeClass('slide-in');}
				rowWrap.removeClass('slide-out');
			});
		}
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
	//SEARCH BOXES
	//internal function to sanitize the search string
	var sanitizeSearchStr=function(str){
		//case insensitive
		str=str.trim();
		str=str.toLowerCase();
		//remove certain strings
		str=replaceAll(str,'   ',' ');
		str=replaceAll(str,'  ',' ');
		
		return str;
	};
	//get the search wraps
	var searchWraps=contentWrap.find('.search');
	searchWraps.each(function(){
		var searchWrap=jQuery(this);
		var searchInput=searchWrap.children('input:first');
		var searchBtn=searchWrap.children('.search-btn:last');
		var clearBtn=searchWrap.children('.clear-btn:last');
		//get the default text for this search field
		var defaultTxt=searchInput.attr('value');
		var origTxt=defaultTxt;
		defaultTxt=sanitizeSearchStr(defaultTxt);
		//add the button images
		searchBtn.html(getSvg('search'));
		clearBtn.html(getSvg('x'));
		//get the elements whose inner text should be searched
		var getSearchElems=function(type){
			var elems;
			switch(type){
				case 'templates':
					elems=temLsWrap.find('.dir > .path,.file > .name,.token .str > .part.path,.token .str > .part.name');
				break;
				default:
					
				break;
			}
			return elems;
		};
		//search button click event
		var doSearch=function(){
			searchWrap.addClass('do-search');
			setTimeout(function(){searchWrap.removeClass('do-search');},200);
			//if the current text is the default text OR blank
			var currentTxt=searchInput.val();
			currentTxt=sanitizeSearchStr(currentTxt);
			if(currentTxt==defaultTxt||currentTxt.length<1){
				//clear the default text and set focus
				searchInput.val('');
				searchInput.focus();
			}else{
				//current text is NOT the default NOR blank...
				
				//get the searchType indicator for the search that needs to be conducted
				var searchType=searchWrap.attr('name');
				//get the elements whose inner text should be searched, depending on searchType
				var searchElems=getSearchElems(searchType);
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
						var newTxt=thisTxt;
						//*** figure out what text to wrap in <found></found> tags
					}
				});
			}
		};
		searchBtn.click(function(){doSearch();});
		var clearTxt=function(){
			//clear the text and set focus
			searchInput.val('');
			searchInput.focus();
			searchWrap.removeClass('text-entered');
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
		searchInput.keyup(function(e){
			switch(e.keyCode){
				case 27: //escape key pressed
					e.preventDefault();
					clearTxt();
				break;
				case 13: //enter key pressed
					e.preventDefault();
					doSearch();
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
					}
				break;
			}
		});
	});
	//==UPDATE TEMPLATE/FILE/TOKEN LISTING==
	var updateTemplates=function(){
		//******
		var htm=getHtm('template_dirs',sample_json_templates());
		temLsWrap.append(htm);
		//******
		//==ADD JS EVENTS TO NEW ELEMENTS==
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
		//add on-off toggle events (to elements that don't already have events added)
		var onOffElems=temLsWrap.find('.on-off').not('.evs');
		//mark these elements as having the events attached
		onOffElems.addClass('evs');
		onOffElems.click(function(){
			//get the parent li wrapper
			var parentLi=jQuery(this).parents('li:first');
			//if currently on
			if(parentLi.hasClass('on')){
				//turn it off
				parentLi.removeClass('on');
				parentLi.addClass('off');
			}else{
				//currently off, so turn it on
				parentLi.addClass('on');
				parentLi.removeClass('off');
			}
		});
	};
	bodyElem[0]['updateTemplates']=updateTemplates;
	bodyElem[0].updateTemplates();
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
};
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