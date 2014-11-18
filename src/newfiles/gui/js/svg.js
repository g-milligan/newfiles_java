//get svg xml by its unique function name
function getSvg(svgName){
	var svgXml=getFuncStr('svg_xml_'+svgName);
	return svgXml;
}
//BELOW ARE SVG XML IMAGES STORED INSIDE FUNCTIONS
function svg_xml_folder(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512"><path d="M378.393 420.135l74.158-197.754h-321.351l-74.158 197.754zM106.481 197.663l-49.438 222.474v-321.351h111.237l49.438 49.438h160.675v49.438z"></path></svg>*/}