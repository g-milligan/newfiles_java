jQuery(document).ready(function(){
	//GET KEY ELEMENTS
	//================
	var menuBarWrap=jQuery('#menu-bar:first');
	var contentWrap=jQuery('#content:first');	
	var templatesWrap=contentWrap.find('#templates:first');
	var workspaceWrap=contentWrap.find('#workspace:first');
	var temResizeHandle=templatesWrap.find('.resize.width:last');
	//WINDOW READY
	//=============
	jQuery(window).ready(function(){
		//TEMPLATES RESIZE 
		//================
		temResizeHandle.draggable({
			'addClasses':false,
			'axis':'x',
			'zIndex':999,
			'stop': function(e,ui) {
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