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
//get the hidden template label
function htm_hidden_dirs_label(numDirs){
	var label='';
	if(numDirs!=undefined){
		switch(numDirs){
			case 0:
				label=label='(<span class="count">'+numDirs+'</span>) hidden templates';
			break;
			case 1:
				label='(<span class="count">'+numDirs+'</span>) hidden template';
			break;
			default: //more than one hidden template
				label='(<span class="count">'+numDirs+'</span>) hidden templates';
			break;
		}
	}
	return label;
}
//get template listing html 
function htm_template_dirs(json){
	var temHtm='';var selFileHtm='';var projectIdsHtm='';
	//get the template directories array
	var dirs=[];
	var hidden=[];
	var hasError=false;
	if(json!=undefined){
		if(json.hasOwnProperty('dirs')){dirs=json.dirs;}
		if(json.hasOwnProperty('hidden')){hidden=json.hidden;}
		if(json.hasOwnProperty('error')){
			if(json.error.length>0){
				temHtm+='<div class="error">';
				temHtm+=json.error;
				temHtm+='</div/>';
				hasError=true;
			}
		}
	}
	//if there is NOT an error
	if(!hasError){
		//start dir listing
		temHtm+='<ul class="ls folders">';
		//if there are any template dirs
		if(dirs.length>0){
			//for each template directory
			for(var d=0;d<dirs.length;d++){
				//get the html for one template dir
				var htm=htm_template_dir(dirs[d]);
				temHtm+=htm.template;
				selFileHtm+=htm.file_select;
				projectIdsHtm+=htm.project_ids;
			}
		}else{
			//no template dirs...
			
			temHtm+='<li class="error no-templates">';
			temHtm+='No templates. <span onclick="javascript:alert(\'do it\');">Create a new one?</span>';
			temHtm+='</li>';
		}
		//end dir listing
		temHtm+='</ul>';
		//==HIDDEN TEMPLATES==
		temHtm+='<ul class="hidden folders">';
		//if there are any hidden directories
		var hasDirsClass='no-dirs';numDirs=0;
		if(hidden.length>0){hasDirsClass='has-dirs';numDirs=hidden.length;}
		temHtm+='<li class="closed '+hasDirsClass+'">';
		//==HID TEMPLATES HEAD==
		temHtm+='<span class="hid-templates">';
		temHtm+='<span title="open/close" class="opened-closed"><span class="closed">'+getSvg('plus')+'</span><span class="opened">'+getSvg('minus')+'</span></span>';
		temHtm+='<span class="icon">'+getSvg('hidden')+'</span>';
		temHtm+='<span class="intro">'+htm_hidden_dirs_label(numDirs)+'</span>';
		temHtm+='</span>';
		temHtm+='<ul>';
		//for each hidden directory
		for(var h=0;h<hidden.length;h++){
			//get the html for one hidden template dir
			temHtm+=htm_template_hiddir(hidden[h]);
		}
		//end dir listing
		temHtm+='</ul></li></ul>';
	}
	//put all of the different html parts together into a json
	var retJson={'templates':temHtm,'file_selects':selFileHtm,'project_ids':projectIdsHtm};
	return retJson;
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
	var temHtml='';var selFileHtm='';var projectIdsHtm='';
	if(json!=undefined){
		//if a dir path was provided (required)
		if(json.hasOwnProperty('path')){
			//==GET VALUES FROM THE JSON==
			//if json has ls property, then get the ls array
			var ls=[];if(json.hasOwnProperty('ls')){ls=json.ls;}
			//if json has project_ids property, then get the project ids array
			var project_ids=[];if(json.hasOwnProperty('project_ids')){project_ids=json.project_ids;}
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
			temHtml+='<li class="'+openClass+hasFilesClass+'" name="'+json.path+'">';
			//the main directory path html
            temHtml+='<span class="dir">';
            temHtml+='<span title="open/close" class="opened-closed"><span class="closed">'+getSvg('plus')+'</span><span class="opened">'+getSvg('minus')+'</span></span>';
			temHtml+='<span class="icon">'+getSvg('folder')+'</span>';
            temHtml+='<span class="path">'+json.path+'</span>';
            temHtml+='<span title="options" class="menu-btn">'+getSvg('cog')+'</span>';
            temHtml+='</span>';
			//==FILE LIST HTML==
			var htm=htm_template_ls(ls); 
			temHtml+=htm.template;
			selFileHtm+='<nav class="select" name="'+json.path+'">';
			selFileHtm+='<span class="lbl">[select a file]</span><ul><li class="active" val="..."><span class="txt">[select a file]</span></li>';
			selFileHtm+=htm.file_select;
			selFileHtm+='</ul></nav>';
			//==HIDDEN FILES HTML==
			temHtml+=htm_template_hidfiles(hidden);
			//==INCLUDES HTML==
			temHtml+=htm_template_includes(includes);
			//end dir item
			temHtml+='</li>';
			//==PROJECT IDS HTML==
			projectIdsHtm+=htm_project_ids(json.path,project_ids);
		}
	}
	//put the return json together
	var retJson={'template':temHtml,'file_select':selFileHtm,'project_ids':projectIdsHtm};
	return retJson;
}
//get file listing html 
function htm_template_ls(ls){
	var temHtml='';selFileHtm='';
	//start file listing for this directory
	temHtml+='<ul class="ls files">';
	//if the list was provided
	if(ls!=undefined){
		for(var f=0;f<ls.length;f++){
			//get the html for one file
			var htm=htm_template_file(ls[f]);
			temHtml+=htm.template;
			selFileHtm+=htm.file_select;
		}
	}
	//end file listing for this directory
	temHtml+='</ul>';
	//put the return json together
	var retJson={'template':temHtml,'file_select':selFileHtm};
	return retJson;
}
//get file html 
function htm_template_file(json){
	var temHtml='';var selFileHtm='';
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
			var isSpecial=false; var specialClass='';
			if(json.name.indexOf('_')==0){ //if special _filenames.xml hidden file
				isSpecial=true;
				specialClass=' special';
			}
			//start file item
			temHtml+='<li class="on '+openClass+hasTokensClass+specialClass+'" name="'+json.name+'">';
			//the main directory path html
			temHtml+='<span class="file">';
            temHtml+='<span title="open/close" class="opened-closed"><span class="closed">'+getSvg('plus')+'</span><span class="opened">'+getSvg('minus')+'</span></span>';
			if(!isSpecial){ //if not the special _filenames.xml hidden file
				temHtml+='<span title="on/off" class="on-off">'+getSvg('file')+'</span>';
			}
			temHtml+='<span class="name">'+json.name+'</span>';
			temHtml+='<span title="options" class="menu-btn">'+getSvg('cog')+'</span>';
			temHtml+='</span>';
			selFileHtm+='<li class="on'+specialClass+'" val="'+json.name+'">';
			if(!isSpecial){ //if not the special _filenames.xml hidden file
				selFileHtm+='<span class="on-off">'+getSvg('file')+'</span>';
			}
			selFileHtm+='<span class="txt">'+json.name+'</span></li>';
			//==TOKENS LIST HTML==
			temHtml+=htm_template_tokens(tokens);
			//end file item
			temHtml+='</li>';
		}
	}
	//put the return json together
	var retJson={'template':temHtml,'file_select':selFileHtm};
	return retJson;
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
function htm_template_token(json){
	var htm='';
	//==TOKEN ITEM HTML==
	htm+='<li name="'+json.type+'">';
	htm+='<span class="token">';
	htm+='<span class="start-tag">'+getSvg('lcarrot')+getSvg('lcarrot')+'</span>';
	htm+='<span class="str">';
	var aliasSep='';var colonSep='';
	var partIndex=0;var lastKey='';
	//for each token part
	for (var partKey in json){
		//if key is an actual property of an object, (not from the prototype)
		if (json.hasOwnProperty(partKey)){
			//if NOT the first part
			if(partIndex>0){
				//separator class
				var sepClass='after_'+lastKey+' before_'+partKey;
				//figure out which separator image to use based on the class
				var imgSvg='';
				switch(sepClass){
					case 'after_name before_alias':
						if(aliasSep==''){aliasSep=getSvg('alias');}
						imgSvg=aliasSep;
					break;
					default:
						if(colonSep==''){colonSep=getSvg('colon');}
						imgSvg=colonSep;
					break;
				}
				//write the separator html
				htm+='<span class="sep '+sepClass+'">'+imgSvg+'</span>';
			}
			//display value
			var val=json[partKey]; var manyOptionsClass='';
			if(typeof val!='string'){
				if(isNaN(val)){
					//the val is an array
					var array=val;
					val='';
					//for each array item
					for(var a=0;a<array.length;a++){
						//add the array item html
						val+='<span class="i">'+array[a]+'</span>';
					}
					//if more than two options, then add the many options class
					if(array.length>4){manyOptionsClass=' many';}
				}
			}
			//write the token part html
			htm+='<span class="part'+manyOptionsClass+' '+partKey+'">';
			htm+=val;
			htm+='</span>';
			//next part
			partIndex++;lastKey=partKey;
		}
	}
	htm+='</span>';
	htm+='<span class="end-tag">'+getSvg('rcarrot')+getSvg('rcarrot')+'</span>';
	htm+='</span>';
	htm+='</li>';
	return htm;
}
//get the include rules label
function htm_includes_label(numInc){
	var label='';
	if(numInc!=undefined){
		switch(numInc){
			case 0:
				label=label='(<span class="count">'+numInc+'</span>) include rules';
			break;
			case 1:
				label='(<span class="count">'+numInc+'</span>) include rule';
			break;
			default: //more than one
				label='(<span class="count">'+numInc+'</span>) include rules';
			break;
		}
	}
	return label;
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
	htm+='<span class="intro">'+htm_includes_label(numInc)+'</span>';
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
	htm+='<li><span class="inc">'+includeStr+'</span><span class="del"></span></li>';
	return htm;
}
//get the hidden files label
function htm_hidden_files_label(numFiles){
	var label='';
	if(numFiles!=undefined){
		switch(numFiles){
			case 0:
				label=label='(<span class="count">'+numFiles+'</span>) hidden files';
			break;
			case 1:
				label='(<span class="count">'+numFiles+'</span>) hidden file';
			break;
			default: //more than one
				label='(<span class="count">'+numFiles+'</span>) hidden files';
			break;
		}
	}
	return label;
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
	htm+='<span class="intro">'+htm_hidden_files_label(numFiles)+'</span>';
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
//get the project ids html for a single template
function htm_project_ids(temPath,project_ids){
	var htm='';
	if(project_ids!=undefined){
		if(project_ids.length>0){
			htm+='<div name="'+temPath+'" class="block project-ids">';
			//for each project id
			for(var i=0;i<project_ids.length;i++){
				//get the html for one project id
				htm+=htm_project_id(project_ids[i]);
			}
			htm+='</div>';
		}else{
			htm+='<div name="'+temPath+'" class="block project-ids no-project-ids">';
			htm+='(0) project id\'s';
			htm+='</div>';
		}
	}
	return htm;
}
//get one project id html
function htm_project_id(json){
	var htm='';
	if(json.hasOwnProperty('name')){
		htm+='<div name="'+json.name+'" class="id">';
		htm+='<span class="label">'+json.name+'</span>';
		var optionArray=[];if(json.hasOwnProperty('options')){optionArray=json.options;}
		//if there are no options
		if(optionArray.length<1){
			htm+='<input class="id-val" type="text" />';
		}else{
			//there are options for this project_id...
			
			//start option select 
			htm+='<nav class="select">';
			htm+='<span class="lbl">[select an option]</span>';
			htm+='<ul>';
			htm+='<li class="active" val="..."><span class="txt">[select an option]</span></li>';
			//for each option
			for(var i=0;i<optionArray.length;i++){
				var option=optionArray[i];
				//add item
				htm+='<li val="'+option+'"><span class="txt">'+option+'</span></li>';
			}
			//end option select 
			htm+='</ul></nav>';
		}
		htm+='</div>';
	}
	return htm;
}