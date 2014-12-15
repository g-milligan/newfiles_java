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
	var mainTitleElem=mainViewWrap.find('header .title h1:first');
	var mainViewTabs=mainViewWrap.find('header .tabs:first');
	var mainViewFilesBar=mainViewWrap.find('header .template-files:first');
	var fileDropdownsWrap=mainViewFilesBar.children('.dropdown:first');
	var inputResizeHandle=inputViewWrap.children('.resize.height:last');
	var temResizeHandle=templatesWrap.children('.resize.width:last');
	//disable selection on certain elements
	preventSelect(temLsWrap); preventSelect(mainViewTabs); preventSelect(mainViewFilesBar);
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
					}, 280);
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
		}
		//get all the found elements for a search
		var getFoundElems=function(){
			//get the elements whose inner text should be searched, depending on searchType
			var searchElems=getSearchElems(searchType);
			//get the highlighted elements (should only be one)
			var foundElems=searchElems.has('found');
			foundElems=foundElems.children('found');
			return foundElems;
		};
		//move the highlighted found element into scroll view
		var scrollToHighlight=function(elem){
			var scrollElem=getScrollElem(searchType);
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
				//get the element's position and the view window bounds of the current sroll
				var elemLeft=elem.position().left;
				var scrollLeft=scrollElem.scrollLeft();
				var elemRight=elemLeft+elem.outerWidth();
				var scrollRight=scrollLeft+scrollElem.innerWidth();
				//if the scroll has passed the element's position
				if(elemLeft<scrollLeft){
					scrollElem.scrollLeft(elemLeft); //scroll left until the elem's left edge shows
				}else{
					//if the scroll is before the element's position
					if(elemRight>=scrollRight){
						scrollElem.scrollLeft(elemRight-scrollRight); //scroll left until the elem's right edge shows
					}
				}
			}
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
	var getSelectedTemplate=function(){
		//get the selected template from the main view title element
		var tem=mainTitleElem.text();
		return tem.trim();
	};
	bodyElem[0]['getSelectedTemplate']=getSelectedTemplate;
	//==UPDATE TEMPLATE/FILE/TOKEN LISTING==
	var updateTemplates=function(json){
		if(json!=undefined){
			//==SET THE TEMPLATES LISTING HTML==
			var htm=htm_template_dirs(json); //get html
			temLsWrap.html(''); //clear old html
			temLsWrap.append(htm.templates); //set html
			//==SET THE FILE DROPDOWNS HTML==
			fileDropdownsWrap.children('select[name]').remove(); //clear old html
			fileDropdownsWrap.append(htm.file_selects); //set html
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
			//select events for templates
			var dirPathElems=temLsWrap.find('li .dir > .path').not('.evs');
			dirPathElems.addClass('evs');
			dirPathElems.click(function(){
				//if the parent .dir isn't already selected
				var dirParent=jQuery(this).parent();
				var liParent=dirParent.parent();
				if(!liParent.hasClass('selected')){
					//deselect any templates that are currently selected
					temLsWrap.find('.ls.folders > li.selected').removeClass('selected');
					//select this dir
					liParent.addClass('selected');
					//set the selected template in the main title
					var dirp=jQuery(this).text(); dirp=dirp.trim();
					mainTitleElem.text(dirp);
					//show the correct file dropdown for the selected template
					var fileSelect=fileDropdownsWrap.children('select[name="'+dirp+'"]:first');
					fileDropdownsWrap.children('select').not(fileSelect).removeClass('active');
					fileSelect.addClass('active');
				}
			});
			//select events for files
			var fileNameElems=temLsWrap.find('ul.ls.files li .file > .name').not('.evs');
			fileNameElems.addClass('evs');
			fileNameElems.click(function(){
				//get some elements
				var fileParent=jQuery(this).parent();
				var liParent=fileParent.parent();
				//make sure this file's template is also selected, if not already
				var temLiParent=liParent.parents('li:first');
				if(!temLiParent.hasClass('selected')){
					//select the template for this file too
					var temPathBtn=temLiParent.find('.dir .path:first');
					temPathBtn.click();
				}
				//if this file isn't already selected
				if(!liParent.hasClass('selected')){
					//deselect any files that are currently selected (in this template)
					var fileListUl=fileParent.parents('ul.ls.files:first');
					fileListUl.children('li.selected').removeClass('selected');
					//select this file
					liParent.addClass('selected');
					//make sure this file is selected in the main-view dropdown
					selectFileFromTemplates();
				}
			});
			//select file events in the dropdowns
			var fileSelects=fileDropdownsWrap.children('select').not('.evs');
			fileSelects.addClass('evs');
			fileSelects.change(function(){selectFileFromDropdown(true);});
			//==SELECT / OPEN THE FIRST TEMPLATE ON PAGE LOAD==
			//if there is no selected template
			var selectedTemplate=temLsWrap.find('ul.ls.folders > li.selected');
			if(selectedTemplate.length<1){
				//select the first template by default
				var firstDirElem=temLsWrap.find('ul.ls.folders li .dir:first');
				var firstTemBtn=firstDirElem.children('.path:first');
				firstTemBtn.click(); //select the first template by default on app load
				var firstOpenBtn=firstDirElem.children('.opened-closed:first');
				firstOpenBtn.click(); //open (+) this first template on app load
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
	}
}
//make sure the file dropdown is aligned with the templates navigation selection
function selectFileFromTemplates(){
	//template elements
	var temsUl=jQuery('#templates nav.ls ul.ls.folders:first');
	var temLi=temsUl.children('li.selected:first');
	var temPathElem=temLi.find('.dir .path:first');
	//file elements
	var filesUl=temLi.children('ul.ls.files:first');
	var fileLi=filesUl.children('li.selected:first');
	//dropdown elements
	var fileDropdownsWrap=jQuery('#main-view header .template-files .dropdown:first');
	//get the directory
	var dirp=temPathElem.text(); dirp=dirp.trim();
	//get the targetted select dropdown element
	var fileSelect=fileDropdownsWrap.children('select[name="'+dirp+'"]:first');
	//if any file is selected
	var fname='';
	if(fileLi.length>0){
		var fileNameElem=fileLi.find('.file .name:first');
		fname=fileNameElem.text();fname=fname.trim();
		//if this file isn't already selected
		if(fname!=fileSelect.val()){
			//select this file 
			fileSelect.val(fname);
		}
	}else{
		//no file is selected so select the blank file option...
		fileSelect.val('...');
	}
}
//make sure the templates navigation selection is aligned with the file dropdown
function selectFileFromDropdown(openParent){
	if(openParent==undefined){openParent=false;}
	//get the selected template / file
	var fileDropdownsWrap=jQuery('#main-view header .template-files .dropdown:first');
	var fileSelect=fileDropdownsWrap.children('select.active:first');
	var temVal=fileSelect.attr('name');if(temVal==undefined){temVal='';}
	//get template nav elements
	var temsUl=jQuery('#templates nav.ls ul.ls.folders:first');
	//if any template is selected
	if(temVal.length>0){
		var temPathElem=temsUl.find('li .dir .path[name="'+temVal+'"]:first');
		var temLi=temPathElem.parents('li:first');
		//if not blank file
		var fileVal=fileSelect.val();
		if(fileVal!='...'){
			//if not already selected
			var fileNameElem=temLi.find('ul.ls.files li .file .name[name="'+fileVal+'"]:first');
			var fileLi=fileNameElem.parents('li:first');
			if(!fileLi.hasClass('selected')){
				//select the file
				fileNameElem.click();
				//if the template should be open to show the selected file
				if(openParent){
					//if NOT already open
					if(!temLi.hasClass('opened')){
						//then open
						temLi.find('.dir .opened-closed:first').click();
					}
				}
			}
		}else{
			//blank file selected... so deselect any files in this template
			temLi.find('ul.ls.files li.selected').removeClass('selected');
		}
	}else{
		//no template is selected... so deselect any templates
		temsUl.children('li.selected').removeClass('selected');
	}
}