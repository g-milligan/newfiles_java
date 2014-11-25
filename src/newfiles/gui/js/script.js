jQuery(document).ready(function(){
	//GET KEY ELEMENTS
	//================
	var bodyElem=jQuery('body:first');
	var menuBarWrap=bodyElem.children('#menu-bar:first');
	var contentWrap=bodyElem.children('#content:first');	
	var templatesWrap=contentWrap.children('#templates:first');
	var temHeaderWrap=templatesWrap.children('header:first');
	var temContentWrap=templatesWrap.children('.content:last');
	var temLsWrap=temContentWrap.children('nav.ls:first');
	var workspaceWrap=contentWrap.children('#workspace:first');
	var temResizeHandle=templatesWrap.children('.resize.width:last');
	//UPDATE TEMPLATE/FILE/TOKEN LISTING
	//==================================
	var updateTemplates=function(){
		//******
		//*** getSvg('folder')
		json={
			'dirs':
			[
				{
					'path':'Demos/ListTokenDemo',
					'ls':
					[
						{'name':'fileA.css'},
						{'name':'fileB.phtml','tokens':
							[
								'filename:l:path/to/file:.',
								'var:u:some var',
								'list:some list'
							]
						},
						{'name':'fileC.php'},
						{'name':'fileD.js','tokens':
							[
								'filename:l:path/to/file:.',
								'var:u:some var',
								'list:some list'
							]
						},
						{'name':'fileE.html'},
						{'name':'fileF.txt'}
					],
					'includes':
					[
						'test/*/allFiles',
						'[css]/stuff*yep/sub.css',
						'[css]/includethis.*'
					],
					'hidden':
					[
						'_file1.css','_file2.phtml','_file3.php'
					]
				},
				{'path':'Demos/TestInclude','ls':
					[
						{'name':'fileA.css'},
						{'name':'fileB.phtml'}
					]
				},
				{'path':'Demos/TestVarOptions','ls':
					[
						{'name':'fileA.css'},
						{'name':'fileF.txt'}
					]
				},
				{'path':'Magento/MageStarterKit','ls':
					[
						{'name':'fileA.css'},
						{'name':'fileB.phtml'},
						{'name':'fileC.php','tokens':
							[
								'filename:l:path/to/file:.',
								'var:u:some var',
								'list:some list'
							]
						}
					]
				}
			],
			'hidden':
			[
			 	'Demos/_HiddenFolder',
				'Demos/_AnotherTemplate'
			]
		};
		var htm=getHtm('template_dirs',json);
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
	//WINDOW READY
	//=============
	jQuery(window).ready(function(){
		//TEMPLATES RESIZE 
		//================
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
		//WINDOW RESIZE
		//=============
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