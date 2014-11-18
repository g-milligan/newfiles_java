jQuery(document).ready(function(){
	//GET KEY ELEMENTS
	//================
	var menuBarWrap=jQuery('#menu-bar:first');
	var contentWrap=jQuery('#content:first');	
	var templatesWrap=contentWrap.find('#templates:first');
	var workspaceWrap=contentWrap.find('#workspace:first');
	var temResizeHandle=templatesWrap.find('.resize.width:last');
	//UPDATE TEMPLATE/FILE/TOKEN LISTING
	//==================================
	var updateTemplates=function(){
		//*** getSvg('folder')
	};
	templatesWrap[0]['updateTemplates']=updateTemplates;
	templatesWrap[0].updateTemplates();
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
			//make sure the menu-bar and content share the space exactly
			var menuBarOffset=menuBarWrap.outerHeight();
			var windowHeight=jQuery(window).outerHeight();
			contentWrap.css('top', menuBarOffset+'px');
			contentWrap.css('height', (windowHeight-menuBarOffset)+'px');
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