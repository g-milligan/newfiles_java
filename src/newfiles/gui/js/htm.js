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
function htm_template_dirs(dirs){
	var htm='';
	//start dir listing
	htm+='<ul class="ls folders">';
	//if the list was provided
	if(dirs!=undefined){
		for(var d=0;d<dirs.length;d++){
			//get the html for one template dir
			htm+=htm_template_dir(dirs[d]);
		}
	}
	//end dir listing
	htm+='</ul>';
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
            htm+='<span class="opened-closed"></span>';
            htm+='<span class="path">'+json.path+'</span>';
            htm+='<span class="menu-btn"></span>';
            htm+='</span>';
			//==FILE LIST HTML==
			htm+=htm_template_ls(ls);
			//==INCLUDES HTML==
			//start includes for this directory
			htm+='<ul class="includes">';
			//***
			//end includes for this directory
			htm+='</ul>';
			//==HIDDEN FILES HTML==
			//start hidden files for this directory
			htm+='<ul class="includes">';
			//***
			//end hidden files for this directory
			htm+='</ul>';
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
			htm+='<li class="'+openClass+hasTokensClass+'">';
			//the main directory path html
			htm+='<span class="file">';
			htm+='<span class="opened-closed"></span>';
			htm+='<span class="on-off on"></span>';
			htm+='<span class="name">'+json.name+'</span>';
			htm+='<span class="menu-btn"></span>';
			htm+='</span>';
			//==TOKENS LIST HTML==
			htm+=htm_template_tokens(tokens);
			//end file item
			htm+='<li class="'+openClass+hasTokensClass+'">';
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
	htm='<li>'+tokenStr+'</li>';
	return htm;
}