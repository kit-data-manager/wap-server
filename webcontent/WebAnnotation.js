/**
 * Json objects are just key:value pairs and values can be an other json object or array lists.
 * Visualisation is very limited not knowing anything about the keys.
 * Web Annotation object are json object but with rules and defined vocabulars.
 * For Target Visualisation, the "target" key with his "source" key is needed,
 * also his optional "selector" key is used to visualize.
 * 
 * Use visualizeTarget() to get html string to visualize target's source.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.0
 */
class WebAnnotation {
	/**
	 * Instance an WebAnnotation object.
	 * @param jsonObject A Object like from JSON.parse().
	 */
	constructor(jsonObject) {
		if(typeof jsonObject === typeof ""){
			jsonObject = JSON.parse(jsonObject);
		}
		this.jsonObject = jsonObject;
	}

	/**
	 * Assume that jsonObject semantic is a web annotation, having body and target.
	 * Searching for target.source and target.selector.value and resize it to window width.
	 * Alternative information of image with and height might be stored in the
	 * values from body...imageWidth and body...imageHeight.
	 * @param parentToAppend wich will contain the created html code.
	 * @param jsonObject can be used to visualize the target of any other json object.
	 */
	visualizeTarget(parentToAppend, jsonObject = this.jsonObject) {
		var html = "";

		var target = $(jsonObject).attr("target");
		if (target === undefined) {
			html += '"<span class="bold">target</span>" not found! <br>';
			html += 'A Web Annotation must have 1 or more targets.';

		} else {
			var source = $(target).attr("source");
			if (source === undefined) {
				html += '"<span class="bold">source</span>" in "target" not found! <br>';
				html += 'Target Visualization needs a "source" attribute to visualize.';

			} else {
				var selector = $(target).attr("selector");
				if (selector === undefined) {
					//just print source
					html += '<img src="' + source + '" class="fadeIn" style="max-width:100%;">';

				} else {
					var svg = $(selector).attr("value");
					if (svg === undefined) {
						//just print source
						html += '<img src="' + source + '" class="fadeIn" style="max-width:100%;">';

					} else {
						//Load image asynchronously to be able to get naturalWidth and naturalHeight for the SVG viewBox.
						var callback = this;
						var targetImage = new Image();
						targetImage.onload = function(){
							callback.visualizeTargetEventHandlerOnload(parentToAppend, targetImage, svg);
						};
						targetImage.src = source;
						html += '<img src="" class="loading" style="max-width:100%;">';
					}
				}
			}
		}
		if (parentToAppend !== undefined){
			parentToAppend.append(html);
		}
	}
	
	/**
	 * After the target image loading is done, we can get naturalWidth and naturalHeight to set the SVG viewBox.
	 * 
	 * @param parentToAppend wich will contain the created html code.
	 * @param targetImage which did call the event onload.
	 * @param svg The found SVG selector in the Web Annotation.
	 */
	visualizeTargetEventHandlerOnload(parentToAppend, targetImage, svg){
		var html = "";
		var source = targetImage.src;
		var naturalWidth = targetImage.naturalWidth;
		var naturalHeight = targetImage.naturalHeight;
		
		var sourceStyle = 'max-width:100%;';
		if(naturalWidth > 0 && naturalHeight > 0){
			svg = svg.replace(/svg /, 'svg viewBox="0 0 ' + naturalWidth + ' ' + naturalHeight + '" ');
		} else {
			//try to get alternative information about image witdh and height from web annotation body key
			var sourceWidth = $(this.searchBodyObjectByValue("imageWidth")).attr("value");
			var sourceHeight = $(this.searchBodyObjectByValue("imageHeight")).attr("value");
			if (sourceWidth !== undefined && sourceHeight !== undefined) {
				svg = svg.replace(/svg /, 'svg viewBox="0 0 ' + sourceWidth + ' ' + sourceHeight + '" ');				
			} else {
				//no information about image witdh and height avaiable.
				//just print image and SVG wihtout rezising it.
				sourceStyle = '';
			}
		}
		
		//visualize svg selector as overlay on the source
		html += '<div class="fadeIn" style="border:0px solid purple; position:relative; overflow:visible;">'; //relative outer frame container
		html += '<img src="' + source + '" alt="' + source + '" style="' + sourceStyle + '">' //this resizes the outer frame container. allows the parent container to use scrollbars. 
		html += '<svg class="blockframe" style="">';
		html += svg;
		html += '</svg>';
		html += '</div>';
		
		if (parentToAppend !== undefined){
			parentToAppend.html("");
			parentToAppend.append(html);
		}
	}

	/**
	 * Because the value which is searched for is not a attribute name of an body element,
	 * it needs to search all attributes values to match the search.
	 * After the body element is identified, which contains the search value as one of its own attribute values,
	 * the whole element is returned.
	 * @param search string used to identify the body element, if one attribtue value equals "search".
	 * @param jsonObject can be used to search in any other json object.
	 */
	searchBodyObjectByValue(search, jsonObject = this.jsonObject) {
		var body = $(jsonObject).attr("body");
		if (body === undefined) {
			return undefined;

		} else {
			var found = undefined;
			$.each(body, function (bodyIndex, bodyItem) {
				$.each(bodyItem, function (key, value) {
					if (found === undefined && value == search) {
						found = bodyItem;
					}
				});
			});
			return found;
		}
	}
	
	/**
	 * Recalculate SVG fixed sizes into percentages to be able to fit into window width.
	 * 
	 * Note: image need to be loaded to access naturalWidth and naturalHeight!
	 * 
	 * @param source A URL to an image file.
	 * @param svg SVG string to manipulate.
	 * @return manipulated SVG
	 */
	calculateSvgPercentagesBySource(svg, source){
		var sourceImage = new Image();
		sourceImage.src = source;
		svg = svg.replace(/x="\d+"|y="\d+"|width="\d+"|height="\d+"|cx="\d+"|cy="\d+"|r="\d+"|rx="\d+"|ry="\d+"|x1="\d+"|x2="\d+"|y1="\d+"|y2="\d+"|points="(\d|,| )+"/g, function(match){
			//rect, text
			if(/^x=/.test(match)){
			    var x = parseInt(match.match(/\d+/));
				return 'x="' + parseInt(100 / sourceImage.naturalWidth * x) + '%"';
			}
			if(/^y=/.test(match)){
			    var y = parseInt(match.match(/\d+/));
				return 'y="' + parseInt(100 / sourceImage.naturalHeight * y) + '%"';
			}
			//rect
			if(/^width=/.test(match)){
			    var width = parseInt(match.match(/\d+/));
				return 'width="' + parseInt(100 / sourceImage.naturalWidth * width) + '%"';
			}
			if(/^height=/.test(match)){
			    var height = parseInt(match.match(/\d+/));
				return 'height="' + parseInt(100 / sourceImage.naturalHeight * height) + '%"';
			}
			//circle
			if(/^cx=/.test(match)){
			    var cx = parseInt(match.match(/\d+/));
				return 'cx="' + parseInt(100 / sourceImage.naturalWidth * cx) + '%"';
			}
			if(/^cy=/.test(match)){
			    var cy = parseInt(match.match(/\d+/));
				return 'cy="' + parseInt(100 / sourceImage.naturalHeight * cy) + '%"';
			}
			if(/^r=/.test(match)){
			    var r = parseInt(match.match(/\d+/));
				return 'r="' + parseInt(100 / sourceImage.naturalWidth * r) + '%"';
			}
			//ellipse
			if(/^rx=/.test(match)){
			    var rx = parseInt(match.match(/\d+/));
				return 'rx="' + parseInt(100 / sourceImage.naturalWidth * rx) + '%"';
			}
			if(/^ry=/.test(match)){
			    var ry = parseInt(match.match(/\d+/));
				return 'ry="' + parseInt(100 / sourceImage.naturalHeight * ry) + '%"';
			}
			//line
			if(/^x1=/.test(match)){
			    var x1 = parseInt((match.replace(/x1=/, 'x=')).match(/\d+/));
				return 'x1="' + parseInt(100 / sourceImage.naturalWidth * x1) + '%"';
			}
			if(/^x2=/.test(match)){
			    var x2 = parseInt((match.replace(/x2=/, 'x=')).match(/\d+/));
				return 'x2="' + parseInt(100 / sourceImage.naturalWidth * x2) + '%"';
			}
			if(/^y1=/.test(match)){
			    var y1 = parseInt((match.replace(/y1=/, 'y=')).match(/\d+/));
				return 'y1="' + parseInt(100 / sourceImage.naturalHeight * y1) + '%"';
			}
			if(/^y2=/.test(match)){
			    var y2 = parseInt((match.replace(/y2=/, 'y=')).match(/\d+/));
				return 'y2="' + parseInt(100 / sourceImage.naturalHeight * y2) + '%"';
			}
			//points
			//ex: points="200,10 250,190 160,210"
			if(/^points=/.test(match)){
				match = match.replace(/\d+(,| |")/g, function(match){
					if(/,$/.test(match)){
						var x = parseInt(match.match(/\d+/));
						return '' + parseInt(100 / sourceImage.naturalWidth * x) + '%,';
					}
					if(/( )$/.test(match)){
						var y = parseInt(match.match(/\d+/));
						return '' + parseInt(100 / sourceImage.naturalHeight * y) + '% ';
					}
					if(/(")$/.test(match)){
						var y = parseInt(match.match(/\d+/));
						return '' + parseInt(100 / sourceImage.naturalHeight * y) + '%"';
					}
					return match;
				});
				return match;
			}
			
			//polygon, polyline
			//(does not work with percentage values).
			//... not supported ...
			
			//path
			//ex: <path d="M150 0 L75 200 L225 200 Z" />
			//... not supported ...
			
			//else return origin match
			return match;
		});
	}
}
