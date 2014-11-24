//get html from one of the functions depending on the htmName
function getHtm(htmName,json){
	var htm='';
	var funcName='htm_'+htmName;
	//if this function exists
	if(window.hasOwnProperty(funcName)){
		//get the html content from this function
		htm=window[funcName](json);
	}
	return htm;
}
//get template listing html 
function htm_template_dirs(json){
	var htm='';
	//get the template directories array
	var dirs=[];
	var hidden=[];
	if(json!=undefined){
		if(json.hasOwnProperty('dirs')){dirs=json.dirs;}
		if(json.hasOwnProperty('hidden')){hidden=json.hidden;}
	}
	//start dir listing
	htm+='<ul class="ls folders">';
	//for each template directory
	for(var d=0;d<dirs.length;d++){
		//get the html for one template dir
		htm+=htm_template_dir(dirs[d]);
	}
	//end dir listing
	htm+='</ul>';
	//==HIDDEN TEMPLATES==
	htm+='<ul class="hidden folders">';
	//if there are any hidden directories
	var hasDirsClass='no-dirs';numDirs=0;
	if(hidden.length>0){hasDirsClass='has-dirs';numDirs=hidden.length;}
	htm+='<li class="closed '+hasDirsClass+'">';
	//==HID TEMPLATES HEAD==
	htm+='<span class="hid-templates">';
    htm+='<span title="open/close" class="opened-closed"><span class="closed">'+getSvg('plus')+'</span><span class="opened">'+getSvg('minus')+'</span></span>';
	htm+='<span class="icon">'+getSvg('hidden')+'</span>';
	htm+='<span class="intro">(<span class="count">'+numDirs+'</span>) hidden template(s)</span>';
	htm+='</span>';
	htm+='<ul>';
	//for each hidden directory
	for(var h=0;h<hidden.length;h++){
		//get the html for one hidden template dir
		htm+=htm_template_hiddir(hidden[h]);
	}
	//end dir listing
	htm+='</ul></li></ul>';
	return htm;
}
//get hidden template name html 
function htm_template_hiddir(path){
	var htm='';
	//==HID TEMPLATE ITEM HTML==
	htm+='<li>'+path+'</li>';
	return htm;
}
//get the html for a template directory
function htm_template_dir(json){
	var htm='';
	if(json!=undefined){
		//if a dir path was provided (required)
		if(json.hasOwnProperty('path')){
			//==GET VALUES FROM THE JSON==
			//if json has ls property, then get the ls array
			var ls=[];if(json.hasOwnProperty('ls')){ls=json.ls;}
			//if json has includes property, then get the includes array
			var includes=[];if(json.hasOwnProperty('includes')){includes=json.includes;}
			//if json has hidden property, then get the hidden array
			var hidden=[];if(json.hasOwnProperty('hidden')){hidden=json.hidden;}
			//if there are any files, then set has-files class
			var hasFilesClass=' no-files';if(ls.length>0){hasFilesClass=' has-files';}
			//if there is an open property with an open value, then set the bool flag true
			var isOpen=false;if(json.hasOwnProperty('is_open')){isOpen=json.is_open;}
			//set the opened/closed class
			var openClass='closed';if(isOpen){openClass='opened';}
			//==DIRECTORY HEAD HTML==
			//start dir item
			htm+='<li class="'+openClass+hasFilesClass+'">';
			//the main directory path html
            htm+='<span class="dir">';
            htm+='<span title="open/close" class="opened-closed"><span class="closed">'+getSvg('plus')+'</span><span class="opened">'+getSvg('minus')+'</span></span>';
			htm+='<span class="icon">'+getSvg('folder')+'</span>';
            htm+='<span class="path">'+json.path+'</span>';
            htm+='<span title="options" class="menu-btn">'+getSvg('cog')+'</span>';
            htm+='</span>';
			//==FILE LIST HTML==
			htm+=htm_template_ls(ls);
			//==HIDDEN FILES HTML==
			htm+=htm_template_hidfiles(hidden);
			//==INCLUDES HTML==
			htm+=htm_template_includes(includes);
			//end dir item
			htm+='</li>';
		}
	}
	return htm;
}
//get file listing html 
function htm_template_ls(ls){
	var htm='';
	//start file listing for this directory
	htm+='<ul class="ls files">';
	//if the list was provided
	if(ls!=undefined){
		for(var f=0;f<ls.length;f++){
			//get the html for one file
			htm+=htm_template_file(ls[f]);
		}
	}
	//end file listing for this directory
	htm+='</ul>';
	return htm;
}
//get file html 
function htm_template_file(json){
	var htm='';
	if(json!=undefined){
		//if a file name was provided (required)
		if(json.hasOwnProperty('name')){
			//==GET VALUES FROM THE JSON==
			//if json has tokens property, then get the tokens array
			var tokens=[];if(json.hasOwnProperty('tokens')){tokens=json.tokens;}
			//if there are any tokens, then set has-tokens class
			var hasTokensClass=' no-tokens';if(tokens.length>0){hasTokensClass=' has-tokens';}
			//if there is an open property with an open value, then set the bool flag true
			var isOpen=false;if(json.hasOwnProperty('is_open')){isOpen=json.is_open;}
			//set the opened/closed class
			var openClass='closed';if(isOpen){openClass='opened';}
			//==FILE HEAD HTML==
			//start file item
			htm+='<li class="on '+openClass+hasTokensClass+'">';
			//the main directory path html
			htm+='<span class="file">';
            htm+='<span title="open/close" class="opened-closed"><span class="closed">'+getSvg('plus')+'</span><span class="opened">'+getSvg('minus')+'</span></span>';
			htm+='<span title="on/off" class="on-off on">'+getSvg('file')+'</span>';
			htm+='<span class="name">'+json.name+'</span>';
			htm+='<span title="options" class="menu-btn">'+getSvg('cog')+'</span>';
			htm+='</span>';
			//==TOKENS LIST HTML==
			htm+=htm_template_tokens(tokens);
			//end file item
			htm+='</li>';
		}
	}
	return htm;
}
//get tokens listing html 
function htm_template_tokens(tokens){
	var htm='';
	//start token listing for this file
	htm+='<ul class="tokens">';
	//if the list was provided
	if(tokens!=undefined){
		for(var t=0;t<tokens.length;t++){
			//get the html for one token
			htm+=htm_template_token(tokens[t]);
		}
	}
	//end token listing for this file
	htm+='</ul>';
	return htm;
}
//get token html 
function htm_template_token(tokenStr){
	var htm='';
	//==TOKEN ITEM HTML==
	htm+='<li>';
	htm+='<span class="token">';
	htm+='<span class="start-tag">'+getSvg('lcarrot')+getSvg('lcarrot')+'</span>';
	htm+='<span class="str">'+tokenStr+'</span>';
	htm+='<span class="end-tag">'+getSvg('rcarrot')+getSvg('rcarrot')+'</span>';
	htm+='</span>';
	htm+='</li>';
	return htm;
}
//get file listing html 
function htm_template_includes(includes){
	var htm='';
	//start includes listing
	htm+='<ul class="includes">';
	//if there are any includes
	var hasIncClass='no-includes';var numInc=0;
	if(includes!=undefined&&includes.length>0){hasIncClass='has-includes';numInc=includes.length;}
	htm+='<li class="closed '+hasIncClass+'">';
	//==INCLUDES HEAD==
	htm+='<span class="include">';
    htm+='<span title="open/close" class="opened-closed"><span class="closed">'+getSvg('plus')+'</span><span class="opened">'+getSvg('minus')+'</span></span>';
	htm+='<span class="icon">'+getSvg('paperclip')+'</span>';
	htm+='<span class="intro">(<span class="count">'+numInc+'</span>) include rule(s)</span>';
	htm+='</span>';
	htm+='<ul>';
	//INCLUDES LIST==
	if(includes!=undefined){
		//for each include
		for(var i=0;i<includes.length;i++){
			//get the html for one include
			htm+=htm_template_include(includes[i]);
		}
	}
	//end includes listing for this directory
	htm+='</ul></li></ul>';
	return htm;
}
//get include html 
function htm_template_include(includeStr){
	var htm='';
	//==INCLUDE ITEM HTML==
	htm+='<li>'+includeStr+'</li>';
	return htm;
}
//get file listing html 
function htm_template_hidfiles(files){
	var htm='';
	//start file listing
	htm+='<ul class="hidden files">';
	//if there are any files
	var hasFilesClass='no-files';numFiles=0;
	if(files!=undefined&&files.length>0){hasFilesClass='has-files';numFiles=files.length;}
	htm+='<li class="closed '+hasFilesClass+'">';
	//==HIDE FILES HEAD==
	htm+='<span class="hid">';
    htm+='<span title="open/close" class="opened-closed"><span class="closed">'+getSvg('plus')+'</span><span class="opened">'+getSvg('minus')+'</span></span>';
	htm+='<span class="icon">'+getSvg('hidden')+'</span>';
	htm+='<span class="intro">(<span class="count">'+numFiles+'</span>) hidden files(s)</span>';
	htm+='</span>';
	htm+='<ul>';
	//HIDDEN FILES LIST==
	if(files!=undefined){
		//for each hidden file
		for(var f=0;f<files.length;f++){
			//get the html for one hidden file
			htm+=htm_template_hidfile(files[f]);
		}
	}
	//end hidden files listing
	htm+='</ul></li></ul>';
	return htm;
}
//get include html 
function htm_template_hidfile(name){
	var htm='';
	//==HID FILE NAME HTML==
	htm+='<li>'+name+'</li>';
	return htm;
}