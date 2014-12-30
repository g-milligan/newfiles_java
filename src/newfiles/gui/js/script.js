function getTestInBrowser(){return true;} //true = test outside of Java, in a browser ***
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
	//==FUNCTIONS THAT FLAG WHEN ANY CHANGES ARE MADE TO A TEMPLATE==
	var templateChangesMade=function(temName, whatChanged, howChanged, json){
		//if the #templateChangesMade element doesn't already exist
		var changesMadeWrap=bodyElem.children('#templateChangesMade:last');
		if(changesMadeWrap.length<1){
			//create the #templateChangesMade element
			bodyElem.append('<div style="display:none;" class="wrap" id="templateChangesMade"></div>');
			changesMadeWrap=bodyElem.children('#templateChangesMade:last');
		}
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
					//if changes for THIS temName don't already exist
					var thisTemChangesWrap=changesMadeWrap.children('div[name="'+temName+'"]:first');
					if(thisTemChangesWrap.length<1){
						//create the element for this template
						changesMadeWrap.append('<div undo="0" class="template" name="'+temName+'"></div>');
						thisTemChangesWrap=changesMadeWrap.children('div[name="'+temName+'"]:first');
					}
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
							//if HOW it was changed is specified
							if(howChanged!=undefined){
								//get the <how> element by its name
								var howWrap=whatWrap.children('how[name="'+howChanged+'"]:first');
								if(howWrap.length<1){
									//create <how> because it doesn't already exist
									whatWrap.append('<how name="'+howChanged+'"></how>');
									howWrap=whatWrap.children('how[name="'+howChanged+'"]:first');
								}
								//increment the most recent undo number
								var currentUndo=thisTemChangesWrap.attr('undo');currentUndo=parseInt(currentUndo);
								currentUndo++;
								//combine what/how to get a change key
								var whatHowChange=howChanged+'-'+whatChanged;
								var changeMade=false;
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
										alert('delete ' + incStr);
										//***
										break;
								}
								//if the change was made
								if(changeMade){
									//set the most recent, incremented, undo number
									thisTemChangesWrap.attr('undo',currentUndo+'');
								}
							}
						}
					}
				}
			}
		}
		return changesMadeWrap.html();
	};
	bodyElem[0]['templateChangesMade']=templateChangesMade;
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
					//*** highlight the selected files in tree view
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
	//convert <span class="str">...</span>, in the templates nav, to a JSON
	var tokenNavElemToJson=function(strElem,includePartSelector){
		if(includePartSelector==undefined){includePartSelector='.part';}
		var tokenJson;
		//if this element has a str class
		if(strElem.hasClass('str')){
			//if the parent of this element has a token class
			if(strElem.parent().hasClass('token')){
				tokenJson={};
				//for each token part
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
			//==SHOW WORKSPACE==
			//remove the no-selected-template indicator to show the workspace content
			workspaceWrap.removeClass('no-selected-template');
		}
	};
	bodyElem[0]['selectTemplate']=selectTemplate;
	//==SELECT FILE==
	var selectTemplateFile=function(temName,fName){
		//if this file from this template is not already selected
		var fileSelect=fileDropdownsWrap.children('nav.select[name="'+temName+'"]:first');
		var currentFile=fileSelect[0]['currentSelectedFile'];
		if(currentFile!=fName){
			//==LEFT NAV FILE==
			var temLi=getTemplateLi(temName);
			var fileLi=temLi.find('ul.ls.files > li[name="'+fName+'"]:first');
			//deselect other files in this template
			temLi.find('ul.ls.files > li.selected').removeClass('selected');
			//if this left nav item exists
			if(fileLi.length>0){
				//select the new file
				fileLi.addClass('selected');
			}
			//==FILES DROPDOWN==
			//if the select is not already correct
			if(fileSelect[0].val()!=fName){
				//set the correct select option
				fileSelect[0].val(fName);
			}
			fileSelect[0]['currentSelectedFile']=fName;
		}
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
			selectTemplateFile(fileSelect.attr('name'),nextOption.attr('val'));
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
			selectTemplateFile(fileSelect.attr('name'),prevOption.attr('val'));
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
	//evs +/- button for open/close
	var evsToggleOpenClose=function(){
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
	bodyElem[0]['evsToggleOpenClose']=evsToggleOpenClose;
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
			bodyElem[0].selectTemplateFile(temName,fileName);
			//select the token
			var tokenJson=tokenNavElemToJson(jQuery(this));
			bodyElem[0].selectTokenInstances(temName,
			{
				'files':[fileName], 'token':tokenJson
			});
		});
	};
	bodyElem[0]['evsTokens']=evsTokens;
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
										bodyElem[0].selectTemplateFile(temName,retVal);
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
			evsToggleOpenClose();
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
					var firstOpenBtn=firstDirElem.children('.opened-closed:first');
					firstOpenBtn.click(); //open (+) this first template on app load
				}
			}
		}
	};
	bodyElem[0]['updateTemplates']=updateTemplates;
	if(getTestInBrowser()){
		//for testing in a browser
		bodyElem[0].updateTemplates(sample_json_templates3());
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
	var treeViewWrap=btn.parents('.content[name="tree-view"]:first');
	if(btn.hasClass('on')){
		btn.removeClass('on');
		treeViewWrap.removeClass('hidden-system-files');
	}else{
		btn.addClass('on');
		treeViewWrap.addClass('hidden-system-files');
	}
}
