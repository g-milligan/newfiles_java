//get svg xml by its unique function name
function getSvg(svgName){
	var svgXml=getFuncStr('svg_xml_'+svgName);
	return svgXml;
}
//BELOW ARE SVG XML IMAGES STORED INSIDE FUNCTIONS
function svg_xml_folder(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512"><path d="M378.393 420.135l74.158-197.754h-321.351l-74.158 197.754zM106.481 197.663l-49.438 222.474v-321.351h111.237l49.438 49.438h160.675v49.438z"></path></svg>*/}
function svg_xml_plus(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512">
<path d="M496 192h-176v-176c0-8.836-7.164-16-16-16h-96c-8.836 0-16 7.164-16 16v176h-176c-8.836 0-16 7.164-16 16v96c0 8.836 7.164 16 16 16h176v176c0 8.836 7.164 16 16 16h96c8.836 0 16-7.164 16-16v-176h176c8.836 0 16-7.164 16-16v-96c0-8.836-7.164-16-16-16z"></path>
</svg>*/}
function svg_xml_minus(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512">
<path d="M0 208v96c0 8.836 7.164 16 16 16h480c8.836 0 16-7.164 16-16v-96c0-8.836-7.164-16-16-16h-480c-8.836 0-16 7.164-16 16z"></path>
</svg>*/}
function svg_xml_cog(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512">
<path d="M429.696 256c0-26.829 16.512-48 41.344-62.541-4.505-14.951-10.394-29.312-17.69-42.803-27.904 7.296-50.457-3.635-69.401-22.579-18.995-18.969-24.806-41.549-17.51-69.401-13.491-7.296-27.801-13.261-42.803-17.715-14.541 24.858-40.832 41.37-67.635 41.37s-53.069-16.512-67.635-41.37c-15.001 4.455-29.312 10.419-42.803 17.689 7.27 27.879 1.485 50.457-17.485 69.427-18.969 18.944-41.549 29.875-69.401 22.605-7.296 13.465-13.235 27.827-17.715 42.778 24.858 14.541 41.37 35.712 41.37 62.541 0 26.803-16.512 53.094-41.37 67.661 4.48 14.976 10.419 29.312 17.715 42.803 27.879-7.296 50.432-1.485 69.401 17.485s24.755 41.523 17.485 69.401c13.491 7.296 27.827 13.261 42.803 17.715 14.566-24.883 40.832-41.37 67.635-41.37s53.12 16.512 67.661 41.37c15.002-4.48 29.312-10.419 42.803-17.715-7.296-27.878-1.51-50.432 17.51-69.401 18.944-18.944 41.498-29.875 69.401-22.605 7.296-13.491 13.209-27.827 17.69-42.803-24.858-14.567-41.37-35.738-41.37-62.541zM256 349.517c-51.635 0-93.491-41.882-93.491-93.517s41.856-93.517 93.491-93.517c51.661 0 93.517 41.882 93.517 93.517s-41.856 93.517-93.517 93.517z"></path>
</svg>*/}
function svg_xml_file(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512">
<path d="M423.044 113.669l-52.837-52.838c-16.408-16.407-48.816-29.831-72.019-29.831h-196.875c-23.203 0-42.188 18.985-42.188 42.188v365.625c0 23.203 18.984 42.188 42.188 42.188h309.375c23.203 0 42.188-18.985 42.188-42.188v-253.125c0-23.203-13.423-55.611-29.831-72.019zM312.25 90.573c0.965 0.353 1.957 0.739 2.97 1.159 7.937 3.288 13.447 7.109 15.212 8.874l52.838 52.839c1.766 1.766 5.586 7.274 8.875 15.212 0.42 1.013 0.805 2.005 1.159 2.969h-81.053v-81.052zM396.625 424.75h-281.25v-337.5h168.75v112.5h112.5v225z"></path>
</svg>*/}
function svg_xml_rcarrot(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 352 512">
<path d="M114.35 20.729c17.933 17.505 193.142 201.464 193.142 201.464 9.611 9.353 14.372 21.579 14.372 33.806s-4.762 24.455-14.372 33.806c0 0-175.208 183.96-193.142 201.422-17.933 17.505-50.151 18.704-69.329 0-19.134-18.662-20.635-44.704 0-67.568l160.793-167.658-160.751-167.657c-20.635-22.91-19.134-48.95 0-67.612s51.397-17.505 69.285 0z"></path>
</svg>*/}
function svg_xml_lcarrot(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 352 512">
<path d="M237.649 491.272c-17.934-17.505-193.142-201.464-193.142-201.464-9.611-9.353-14.371-21.579-14.371-33.807s4.761-24.454 14.371-33.806c0 0 175.208-183.96 193.142-201.422 17.934-17.505 50.151-18.704 69.329 0 19.134 18.662 20.635 44.703 0 67.569l-160.793 167.657 160.751 167.657c20.635 22.91 19.134 48.95 0 67.613s-51.397 17.505-69.286 0z"></path>
</svg>*/}
function svg_xml_paperclip(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512">
<path d="M142.623 481c-30.060 0-58.208-12.735-78.547-33.097-39.375-39.375-50.377-108.135 4.838-163.35 32.377-32.333 161.977-161.933 226.665-226.62 22.973-22.972 52.178-31.837 80.168-24.323 27.473 7.335 50.017 29.858 57.375 57.353 7.493 27.99-1.395 57.218-24.345 80.167l-216.72 216.743c-12.375 12.375-26.347 19.71-40.41 21.173-13.905 1.462-27.18-3.015-36.495-12.308-16.875-16.853-19.26-48.577 8.775-76.59l152.213-152.257c6.255-6.232 16.402-6.232 22.657 0 6.255 6.255 6.255 16.403 0 22.657l-152.235 152.235c-13.162 13.163-14.377 25.74-8.775 31.342 2.452 2.43 6.188 3.555 10.485 3.105 6.593-0.697 14.107-4.973 21.127-11.993l216.72-216.72c14.873-14.873 20.565-32.355 16.043-49.252-4.455-16.627-18.090-30.263-34.717-34.718-16.897-4.523-34.38 1.192-49.252 16.043-64.665 64.688-194.265 194.287-226.62 226.62-42.21 42.21-32.108 90.788-4.86 118.058 27.27 27.248 75.848 37.373 118.080-4.86l226.643-226.62c6.233-6.255 16.402-6.255 22.635 0 6.255 6.255 6.255 16.403 0 22.635l-226.643 226.643c-26.708 26.73-56.588 37.935-84.802 37.935z"></path>
</svg>*/}
function svg_xml_hidden(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512">
<path d="M382.424 172.733c29.989 21.255 54.603 49.861 71.329 83.267-36.74 73.369-111.502 123.596-197.754 123.596-24.166 0-47.427-3.945-69.206-11.233l30.109-30.11c12.768 2.82 25.867 4.264 39.096 4.264 34.59 0 68.308-9.844 97.504-28.466 23.237-14.822 42.928-34.675 57.718-58.050-14.311-22.62-33.221-41.927-55.483-56.582l26.685-26.685zM256 327.841c-8.777 0-17.285-1.149-25.387-3.296l120.964-120.964c2.149 8.1 3.3 16.606 3.3 25.383 0 54.609-44.269 98.876-98.877 98.876zM429.035 58.246h-20.755l-84.822 84.822c-21.273-6.923-43.936-10.665-67.457-10.665-86.254 0-161.016 50.227-197.754 123.596 16.476 32.904 40.605 61.144 69.984 82.295l-69.984 69.984v20.755h20.755l350.033-350.033v-20.755zM218.921 179.525c18.556 0 33.929 13.631 36.65 31.428l-42.303 42.303c-17.795-2.722-31.428-18.095-31.428-36.651 0-20.478 16.601-37.079 37.079-37.079zM100.777 256c14.79-23.376 34.481-43.23 57.717-58.050 1.513-0.965 3.042-1.901 4.58-2.82-3.846 10.555-5.952 21.947-5.952 33.833 0 22.604 7.59 43.431 20.353 60.084l-22.614 22.615c-21.657-14.538-40.074-33.514-54.086-55.661z"></path>
</svg>*/}
function svg_xml_search(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512">
<path d="M82.098 386.137l87.828-74.699c9.079-8.171 18.79-11.922 26.633-11.56-20.732-24.285-33.256-55.791-33.256-90.226 0-76.793 62.252-139.046 139.046-139.046 76.792 0 139.046 62.252 139.046 139.046s-62.252 139.046-139.046 139.046c-34.435 0-65.942-12.524-90.226-33.257 0.363 7.844-3.389 17.553-11.56 26.633l-74.698 87.828c-12.79 14.211-33.683 15.409-46.428 2.663s-11.548-33.639 2.662-46.428zM302.349 302.349c51.195 0 92.697-41.502 92.697-92.697s-41.502-92.697-92.697-92.697-92.697 41.501-92.697 92.697 41.501 92.697 92.697 92.697z"></path>
</svg>*/}
function svg_xml_x(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512">
<path d="M405.974 348.688c-0.002-0.002-0.003-0.003-0.004-0.003l-92.683-92.685 92.683-92.685c0.002-0.002 0.003-0.003 0.004-0.004 0.998-0.999 1.719-2.165 2.182-3.406 1.267-3.393 0.544-7.364-2.185-10.094l-43.783-43.783c-2.729-2.728-6.699-3.451-10.093-2.184-1.241 0.462-2.408 1.184-3.407 2.181 0 0.002-0.002 0.003-0.003 0.004l-92.685 92.686-92.685-92.685c-0.002-0.002-0.003-0.003-0.004-0.004-0.999-0.997-2.165-1.719-3.405-2.181-3.395-1.268-7.364-0.545-10.094 2.184l-43.784 43.783c-2.73 2.729-3.451 6.699-2.185 10.094 0.463 1.242 1.184 2.409 2.181 3.406 0.002 0.001 0.003 0.003 0.004 0.004l92.685 92.685-92.685 92.686c-0.001 0.002-0.003 0.003-0.004 0.003-0.997 0.998-1.719 2.164-2.182 3.406-1.268 3.394-0.544 7.364 2.185 10.094l43.783 43.783c2.73 2.729 6.699 3.452 10.094 2.185 1.242-0.463 2.408-1.185 3.406-2.182 0.001-0.002 0.003-0.003 0.004-0.003l92.685-92.685 92.685 92.685c0.002 0.001 0.003 0.003 0.003 0.004 0.999 0.998 2.164 1.719 3.406 2.182 3.394 1.267 7.365 0.545 10.093-2.185l43.783-43.784c2.729-2.729 3.452-6.7 2.185-10.094-0.463-1.241-1.185-2.407-2.182-3.406z"></path>
</svg>*/}
function svg_xml_colon(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512">
<path d="M324.351 147.258c0-37.749-30.603-68.352-68.351-68.352s-68.351 30.603-68.351 68.352 30.602 68.352 68.351 68.352 68.351-30.602 68.351-68.351zM324.351 364.741c0-37.749-30.603-68.351-68.351-68.351s-68.351 30.603-68.351 68.351 30.602 68.351 68.351 68.351 68.351-30.603 68.351-68.351z"></path>
</svg>*/}
function svg_xml_alias(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="-71.628 -87.405 640 640">
<path d="M302.145,455.702c16.883-16.439,181.831-189.627,181.831-189.627c9.007-8.804,13.53-20.314,13.53-31.826
	c0-11.55-4.523-23.062-13.53-31.826c0,0-164.948-173.189-181.831-189.667c-16.883-16.438-47.255-17.569-65.269,0
	c-17.972,17.609-19.427,42.125,0,63.653l151.338,157.84l-151.338,157.8c-19.427,21.568-17.972,46.124,0,63.653
	C254.931,473.312,285.303,472.222,302.145,455.702L302.145,455.702z"/>
<path d="M382.814,182.07c0,9.319-8.573,16.874-19.149,16.874H20.559c-10.576,0-19.15-7.555-19.15-16.874v-59.056
	c0-9.319,8.574-16.874,19.15-16.874h343.106c10.576,0,19.149,7.555,19.149,16.874V182.07z"/>
<path d="M381.404,344.317c0,9.318-8.572,16.875-19.146,16.875H19.15C8.574,361.192,0,353.636,0,344.317v-59.057
	c0-9.318,8.574-16.872,19.15-16.872h343.109c10.573,0,19.146,7.554,19.146,16.872V344.317z"/>
</svg>*/}
function svg_xml_up(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512">
<path d="M445.901 368.615c6.938 6.861 18.151 6.861 25.063 0 6.938-6.861 6.938-17.945 0-24.807l-202.445-200.448c-6.912-6.861-18.125-6.861-25.037 0l-202.445 200.448c-6.912 6.861-6.938 17.945 0 24.807s18.125 6.861 25.063 0l189.901-182.81 189.901 182.81z"></path>
</svg>*/}
function svg_xml_down(){/*
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512">
<path d="M66.099 143.386c-6.938-6.861-18.151-6.861-25.063 0-6.938 6.861-6.938 17.946 0 24.806l202.445 200.448c6.912 6.861 18.125 6.861 25.037 0l202.445-200.448c6.912-6.861 6.938-17.946 0-24.806s-18.125-6.861-25.063 0l-189.901 182.81-189.901-182.81z"></path>
</svg>*/}