jQuery(document).ready(function(){
	//CREATE ALL OF THE CODE SECTIONS
	//===============================
	//for each <code> element
	var codeElems=jQuery('code');
	codeElems.each(function(c){
		var codeElem=jQuery(this);
		//build the code structure
		codeElem.after('<div class="code-wrap"><div class="header"></div><div class="code-content"></div></div><div style="clear:both;"></div>');
		var codeWrap=codeElem.next('.code-wrap:first');
		var codeHeader=codeWrap.children('.header:first');
		var codeContent=codeWrap.children('.code-content:first');
		codeContent.append('<div class="code-txt"><div class="line-numbers"></div><div class="source"></div></div>');
		var codeTxtWrap=codeContent.children('.code-txt:first');
		var lineNumsWrap=codeTxtWrap.children('.line-numbers:first');
		var sourceWrap=codeTxtWrap.children('.source:first');
		//set the code header, if there is a file attribute
		var fileVal=codeElem.attr('file');
		if(fileVal==undefined){fileVal='';}
		codeHeader.html(fileVal);
		//get the inner html of the code element
		var codeHtml=codeElem.html();
		if(codeHtml==undefined){codeHtml='';}
		//split into an array of code lines
		var codeLines=codeHtml.split("\n");
		//internal function to add a code line to the view
		var addCodeLine=function(lineTxt, index){
			//if the line is blank
			var blankClass='';
			if(lineTxt.trim().length<1){
				blankClass+=' blank';
			}
			//add the source code
			sourceWrap.append('<div name="'+(index+1)+'" class="line'+blankClass+'">'+lineTxt+'</div>');
			//add the line number
			lineNumsWrap.append('<div name="'+(index+1)+'" class="line'+blankClass+'">'+(index+1)+'</div>');
		};
		//if there are any code lines
		if(codeLines.length>0){
			//for each code line
			for(var c=0;c<codeLines.length;c++){
				//add this code line to the view
				addCodeLine(codeLines[c], c);
			}
			//remove the original code element
			codeElem.remove();
			//add the outer-code wrap
			codeWrap.before('<div class="outer-code"></div>');
			var outerCodeElem=codeWrap.prev('.outer-code:first');
			outerCodeElem.append(codeWrap);
		}else{
			//no code lines...
			//remove the generated code html
			codeWrap.remove();
		}
	});
	//CREATE ALL OF THE IMAGE SLIDERS
	//===============================
	//for each image that has slides
	var slideImgs=jQuery('img[slide="true"]');
	slideImgs.each(function(i){
		//create the slides wrap
		var img=jQuery(this);
		img.after('<div class="slides-wrap"><div class="header"></div><div class="imgs"></div><div class="btns"></div></div><div style="clear:both;"></div>');
		//get the slide wrap's section elements
		var slidesWrap=img.next('.slides-wrap:first');
		var headerWrap=slidesWrap.children('.header:first');
		var imgsWrap=slidesWrap.children('.imgs:first');
		var btnsWrap=slidesWrap.children('.btns:first');
		//if the image has an alt value
		var altVal=img.attr('alt');
		if(altVal==undefined){altVal='';}
		if(altVal.length<1){
			//default alt value
			altVal='Slides';
			img.attr('alt',altVal);
		}
		if(altVal.length>0){
			//add the alt value as the header title text
			headerWrap.html('<div class="title">' + altVal + '</div>');
		}
		//function to call when all of the slides have been added
		var finishAddSlides=function(){
			//add "1/total" to header
			var btns=btnsWrap.children('.btn');
			headerWrap.append('<div class="count"><div class="current">1</div><div class="total">'+btns.length+'</div></div>');
			//create left and right slide buttons
			if(btns.length>1){
				slidesWrap.append('<div class="slideLeft disabled"></div><div class="slideRight"></div>');
				var leftBtn=slidesWrap.children('.slideLeft:last');
				var rightBtn=slidesWrap.children('.slideRight:last');
				leftBtn.click(function(){
					jQuery(this).addClass('active');
					var btn=jQuery(this);
					setTimeout(function(){
						btn.removeClass('active');
					},300);
					//get the current active button
					var currentActiveBtn=slidesWrap.find('.btns .btn.active:first');
					//try to get the previous button
					var prevBtn=currentActiveBtn.prev('.btn:first');
					if(prevBtn.length>0){
						prevBtn.click();
					}
				});
				rightBtn.click(function(){
					jQuery(this).addClass('active');
					var btn=jQuery(this);
					setTimeout(function(){
						btn.removeClass('active');
					},300);
					//get the current active button
					var currentActiveBtn=slidesWrap.find('.btns .btn.active:first');
					//try to get the next button
					var nextBtn=currentActiveBtn.next('.btn:first');
					if(nextBtn.length>0){
						nextBtn.click();
					}
				});
				leftBtn.hover(function(){
					jQuery(this).addClass('hover');
				},function(){
					jQuery(this).removeClass('hover');
				});
				rightBtn.hover(function(){
					jQuery(this).addClass('hover');
				},function(){
					jQuery(this).removeClass('hover');
				});
			}else{
				//only 1 slide... no need for slide buttons
				btnsWrap.remove();
			}
			//create an additional outer-wrap layer
			slidesWrap.before('<div class="outer-slides"></div>');
			var outerWrap=slidesWrap.prev('.outer-slides:first');
			outerWrap.append(slidesWrap);
		};
		//function to add an image to the slides
		var numSlides=-1;
		var addSlide=function(imgElem){
			imgElem.removeAttr('slide');
			//get the index number for this image
			var indexNum=imgsWrap.children('img').length+1;
			//add the index number attribute to the image
			imgElem.attr('index',indexNum+'');
			//add the image to the slides
			imgsWrap.append(imgElem);
			//create a button for this image
			btnsWrap.append('<span class="btn" name="'+indexNum+'">'+indexNum+'</span>');
			//if this is the first image in the slides
			var btn=btnsWrap.children('.btn:last');
			if(indexNum==1){
				//on page load, this slide is active
				imgElem.addClass('active');
				btn.addClass('active');
				//get the total number of slides attribute value
				var slidesAttr=imgElem.attr('slides');
				if(slidesAttr==undefined){slidesAttr='-1';}
				slidesAttr=slidesAttr.trim();
				if(slidesAttr.length<1){slidesAttr='-1';}
				numSlides=parseInt(slidesAttr);
				imgElem.removeAttr('slides');
			}
			//add hover event to button
			btn.hover(function(){
				jQuery(this).addClass('hover');
			},function(){
				jQuery(this).removeClass('hover');
			});
			//add click event to button
			btn.click(function(){
				//remove active classes
				imgsWrap.children('img').removeClass('active');
				btnsWrap.children('.btn').removeClass('active');
				//add the active class
				jQuery(this).addClass('active');
				var activeImg=imgsWrap.children('img[index="' + indexNum + '"]:first');
				activeImg.addClass('active');
				//refresh the slide count in the upper right
				var currentCountElem=slidesWrap.find('.header .count .current:first');
				currentCountElem.text(indexNum+'');
				//get left/right slide buttons
				var slideLeft=slidesWrap.find('.slideLeft:last');
				var slideRight=slidesWrap.find('.slideRight:last');
				//if at the first slide
				if(indexNum==1){
					//disable slide left button
					slideLeft.addClass('disabled');
				}else{
					//not at first slide
					
					//enable slide left button
					slideLeft.removeClass('disabled');
				}
				//if at the last slide
				if(indexNum==slidesWrap.find('.btns .btn').length){
					//disable slide right button
					slideRight.addClass('disabled');
				}else{
					//not at last slide
					
					//enable slide right button
					slideRight.removeClass('disabled');
				}
			});
			//if not at the end of the slides
			if(numSlides==-1||numSlides>indexNum){
				//get the next image name in the slide series
				var imgSrc=imgElem.attr('src');
				if(imgSrc.indexOf('_')!=-1){
					//get just the image prefix
					var imgSrcPrefix=imgSrc.substring(0, imgSrc.indexOf('_')+1);
					//if the image source contains a dot
					if(imgSrc.lastIndexOf('.')!=-1){
						//get just the image extension
						var ext=imgSrc.substring(imgSrc.lastIndexOf('.'));
						//set the next image name in the slides sequence
						var nextImgSrc=imgSrcPrefix+(indexNum+1)+ext;
						//try to add this next image
						var nextImgElem=document.createElement('img');
						nextImgElem.setAttribute('src', nextImgSrc);
						nextImgElem.setAttribute('alt', imgElem.attr('alt'));
						document.body.appendChild(nextImgElem);
						nextImgElem.onerror=function(){
							//image source doesn't exist... remove this image element
							this.parentNode.removeChild(this);
							finishAddSlides();
						};
						nextImgElem.onload=function(){
							//recurively add slide
							addSlide(jQuery(this));
						};
					}
				}
			}else{
				finishAddSlides();
			}
		};
		//start the slide image adding
		addSlide(img);
	});
	//resize function
	var windowResize=function(){
		//resize slides
		var imgs=jQuery('.slides-wrap > .imgs');
		imgs.each(function(i){
			//get the active image
			var img=jQuery(this).find('img.active:first');
			var imgWrap=jQuery(this);
			//get the height of the active image
			var imgHeight=img.outerHeight();
			//if height indicates that the image is not loaded yet
			if(imgHeight<10){
				//wait until the image loades
				img.load(function(){
					//set the minimum height of the wrapper around the image
					var imgHeight=img.outerHeight();
					imgWrap.css('min-height',imgHeight+'px');
					var imgWidth=img.outerWidth();
					imgWrap.css('min-width',imgWidth+'px');
				});
			}else{
				//image is already loaded...
				//set the minimum height of the wrapper around the image
				imgWrap.css('min-height',imgHeight+'px');
				var imgWidth=img.outerWidth();
				imgWrap.css('min-width',imgWidth+'px');
			}
		});
	};
	//CREATE ALL OF THE TOKENS
	//========================
	//for each <token> element
	var tokenElems=jQuery('token');
	tokenElems.each(function(t){
		//get the token text
		var tokenElem=jQuery(this);
		var tokenTxt=tokenElem.text();
		if(tokenTxt==undefined){tokenTxt='';}
		tokenTxt=tokenTxt.trim();
		var source='';
		//if there is any token text
		if(tokenTxt.length>0){
			//if the text contains the : separator
			if(tokenTxt.indexOf(':')!=-1){
				var tokenParts=tokenTxt.split(':');
				//if there are at least three token parts
				if(tokenParts.length>2){
					//get the token type
					var type=tokenParts[0];
					type=type.trim();
					type=type.toLowerCase();
					//get the token casing
					var casing=tokenParts[1];
					casing=casing.trim();casing=casing.toLowerCase();
					//start re-assembling the token text
					var sep='<span class="sep">:</span>';
					tokenTxt='<span class="part type">'+type+'</span>'
						+sep+'<span class="part casing">'+casing+'</span>'+sep;
					switch(type){
						case 'var':
							var name=tokenParts[2];
							//if the name contains a source
							var source='';
							if(name.indexOf('-->')!=-1){
								var nameParts=name.split('-->');
								name=nameParts[0];
								//separate out the source
								source=nameParts[1];source=source.trim();
								source='<span class="src sep">--&gt;</span><span class="part source">'+source+'</span>';
							}
							//if the name contains an alias
							var alias='';
							if(name.indexOf('=>')!=-1){
								var nameParts=name.split('=>');
								name=nameParts[0];
								//separate out the alias
								alias=nameParts[1];alias=alias.trim();
								alias='<span class="al sep">=&gt;</span><span class="part alias">'+alias+'</span>';
							}
							name=name.trim();
							tokenTxt+='<span class="part name">'+name+'</span>'+alias;
							break;
						case 'filename':
							//***
							break;
					}
				}
			}
		}
		//set the token start and end tags
		tokenTxt='<span class="startTag">&lt;&lt;</span>'+tokenTxt+'<span class="endTag">&gt;&gt;</span>'+source;
		tokenElem.html(tokenTxt);
	});
	//window ready
	jQuery(window).ready(function(){
		//window resize
		var resizeTimeout;
		jQuery(window).resize(function(){	
			//don't resize until the resizing ends
			clearTimeout(resizeTimeout);
			resizeTimeout=setTimeout(function(){
				windowResize();
			},100);
		});
		//resize on load
		windowResize();
	});
});