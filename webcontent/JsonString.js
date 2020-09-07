/**
 * Supports all functions needed to handle with a json string for this mini-frontend.
 * 
 * use jsonPretty() to recieve a manipulated json string with html tags for pretty look.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.0
 */
class JsonString {
	/**
	 * Instance an JsonString object.
	 * @param jsonString A String with syntax like from JSON.stringify().
	 */
	constructor(jsonString) {
		if(typeof jsonString === typeof {}){
			jsonString = JSON.stringify(jsonString);
		}
		this.jsonString = jsonString;
	}

	/**
	 * Return a jsonString as a json object.
	 * @param jsonString can be used to turn any other json string into a json object.
	 * @return json object from JSON.parse(jsonString).
	 */
	jsonObject(jsonString = this.jsonString) {
		return JSON.parse(jsonString);
	}
	
	/**
	 * Manipulate jsonString to remove printing problems with html entieties like <, >, &
	 * @param jsonString can be used to print any other json string.
	 * @return manipulated jsonString with removed or replaced problem characters.
	 */
	jsonText(jsonString = this.jsonString) {
		jsonString = jsonString.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
		return "<pre><code>" + jsonString + "</code></pre>";
	}

	/**
	 * Manipulates jsonString into a pretty html string.
	 * @param jsonString can be used to make any other json string pretty.
	 * @return manipulated jsonString with html tags.
	 */
	jsonPretty(jsonString = this.jsonString) {
		jsonString = this.cascadeJsonString(this.autolink(this.syntaxHighlight(jsonString)));
		return "<pre><code>" + jsonString + "</code></pre>";
	}

	/**
	 * Run Autolinker to generate hyperref links to click on.
	 * Options are optimized for json string syntax.
	 * class "autolink" attached to "a" tag. 
	 * @param jsonString can be used to syntaxHighLight any other json string.
	 * @return manipulated jsonString with html tags by Autolinker.
	 */
	autolink(jsonString = this.jsonString) {
		return Autolinker.link(jsonString, {
			urls: {
				schemeMatches: true,
				wwwMatches: true,
				tldMatches: false
			},
			newWindow: true,
			email: true,
			phone: false,
			mention: false,
			hashtag: false,
			stripPrefix: false,
			stripTrailingSlash: false,
			truncate: 0,
			className: "autolink",
			decodePercentEncoding: false,
			replaceFn: null
		});
	}

	/**
	 * Generates for each key:value pair, in given jsonString, a "span" html tag with css class properties:
	 * .key
	 * .number
	 * .key
	 * .string
	 * .boolean
	 * .null
	 * @param jsonString can be used to syntaxHighLight any other json string.
	 * @return manipulated jsonString with html tags.
	 */
	syntaxHighlight(jsonString = this.jsonString) {
		jsonString = jsonString.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
		return jsonString.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
			var cls = 'number';
			if (/^"/.test(match)) {
				//starts with " -> String detected :: JSON key:values are always strings
				if (/:$/.test(match)) {
					cls = 'key';
				} else {
					cls = 'string';
					//json string values could be also a type like boolean, null or a number
					var matchLowerCase = match.toLowerCase();
					if (matchLowerCase === '"true"' || matchLowerCase === '"false"') {
						cls = 'boolean';
					} else if (matchLowerCase === '"null"') {
						cls = 'null';
					} else if (Number(matchLowerCase.replace(/"/g, '')) == matchLowerCase.replace(/"/g, '')) {
						cls = 'number';
					} else {
						//cls allready set to string.
						//cls = 'string';
					}
				}
			} else if (/true|false/.test(match)) {
				cls = 'boolean';
			} else if (/null/.test(match)) {
				cls = 'null';
			}
			return '<span class="' + cls + '">' + match + '</span>';
		});
	}

	/**
	 * Generates a clickable view for a json string with cascading elements.
	 * adding "span" tags with javascript onclick event handler to toggle view.
	 * css class "cascadeJsonString" attached to parent "span" tag.
	 * css class "cascadeJsonStringContent" attached to child "span" tag.
	 * @param jsonString can be used to cascade any other json string.
	 * @return manipulated jsonString with html tags.
	 */
	cascadeJsonString(jsonString = this.jsonString) {
		var insertCascadeString = '<span class="cascadeJsonString" onclick="JsonString.cascadeJsonStringEventHandler(this);">-</span><span class="cascadeJsonStringContent">';
		// insert cascading for {
		jsonString = jsonString.replace(/\{/g, ('{' + insertCascadeString));
		jsonString = jsonString.replace(/\}/g, ('</span>' + '}'));
		// insert cascading for [
		jsonString = jsonString.replace(/\[/g, ('[' + insertCascadeString));
		jsonString = jsonString.replace(/\]/g, ('</span>' + ']'));
		return jsonString;
	}

	/**
	 * Static help function for global use.
	 * Toggles view for the next element and changes text according to the state.
	 * @param element The event element which did call.
	 * @param event The event string that did occur.
	 */
	static cascadeJsonStringEventHandler(element, event = "click") {
		if (event.toLowerCase() === "click") {
			$(element).next().toggle();
			if ($(element).next().is(":hidden")) {
				$(element).text("+");
			} else {
				$(element).text("-");
			}
		}
	}
}
