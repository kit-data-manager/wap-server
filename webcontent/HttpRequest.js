/**
 * HttpRequest is used to set up an entire JQuery Ajax request and support
 * functions to print to the HTTP Protocol page within the frontend.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.0
 */
class HttpRequest {
	/**
	 * Instance an object
	 */
	constructor() {
		this.data = null;
		this.xhr = null;
		this.textStatus = "";
		this.errorThrown = null;

		this.jsonString = null;
		this.jsonObject = null;

		this.visualizationId = "tabsLeft-1";
		this.protocolId = "tabsLeft-2";
		this.jsonPrettyId = "tabsRight-1";
		this.jsonTextId = "tabsRight-2";

		this.debugging = false;
		this.settings = {};
	}

	/**
	 * Perform the ajax XHR request.
	 * @param callback The HttpRequest object for callback in case of done (sucess) or fail (error). 
	 */
	static sendRequest(callback) {
		if (callback.debugging) {
			console.log("%c sendRequest to " + httpMethod.method.toUpperCase() + " " + $("#targetUrl").val(), "color: black; font-weight: bold;");
		}

		callback.settings = {
			url: $("#targetUrl").val(),
			method: httpMethod.method.toUpperCase(),
			cache: true,
			dataType: "text",
			headers: {},
			crossDomain: true,
			beforeSend: function (jqXHR, settings) {
				if (callback.debugging) {
					console.log("%c beforeSend is called", "color: blue; font-weight: bold;");
					console.log("print jqXHR");
					console.log(jqXHR);
					console.log("print settings");
					console.log(settings);
				}
			},
			dataFilter: function (data, type) {
				//return raw data
				return data;
			},
			xhrFields: {
				withCredentials: false
			}
		}

		//settings for sending JSON data to server
		if (callback.settings.method == "POST") {
			callback.settings.contentType = 'application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"';
			callback.settings.data = $("#subPostJson").val();
			if (Main.isTypeContainer(callback.settings.data)) {
				if (callback.debugging) {
					console.log("CONTAINER DETECTED");
				}
				callback.settings.headers["Link"] = '<http://www.w3.org/ns/ldp#BasicContainer>; rel="type"';
				callback.settings.headers["Slug"] = $("#subPostSlug").val();
			}
		}
		//settings for replace data on server, needing Etag
		if (callback.settings.method == "PUT") {
			callback.settings.contentType = 'application/ld+json; profile="http://www.w3.org/ns/anno.jsonld"';
			callback.settings.data = $("#subPutJson").val();
			callback.settings.headers["if-match"] = $("#subPutEtag").val();
		}
		//settings for delete data on server, needing Etag
		if (callback.settings.method == "DELETE") {
			callback.settings.headers["if-match"] = $("#subDeleteEtag").val();
		}

		//perform ajax and bind callback functions
		$.ajax(callback.settings)
			.done(function (data, textStatus, xhr) {
				callback.doneCallback(data, textStatus, xhr);
			})
			.fail(function (xhr, textStatus, errorThrown) {
				callback.failCallback(xhr, textStatus, errorThrown);
			});
	}

	/**
	 * Callback function used by sendRequest function, in case of success.
	 * @param data body content from server response
	 * @param textStatus status message from server response
	 * @param xhr The XMLHttpRequest object used by ajax
	 */
	doneCallback(data, textStatus, xhr) {
		this.data = data;
		this.errorThrown = null;
		this.xhr = xhr;
		this.textStatus = textStatus;

		if (this.debugging) {
			console.log("%c doneCallback is called", "color: green; font-weight: bold;");
			console.log("print jqXHR");
			console.log(xhr);
			console.log("print data");
			console.log(data);
		}

		//set jsonString and jsonObject for all other functions
		if (typeof data === typeof "") {
			if (data == "") {
				this.jsonString = new JsonString("");
				this.jsonObject = null;
			} else {
				this.jsonString = new JsonString(data);
				this.jsonObject = JSON.parse(data);
			}

		} else if (typeof data === typeof {}) {
			if (data.isEmptyObject) {
				this.jsonString = new JsonString("");
				this.jsonObject = null;
			} else {
				this.jsonString = new JsonString(JSON.stringify(data));
				this.jsonObject = data;
			}

		} else if (data === undefined || data === null) {
			this.jsonString = new JsonString("");
			this.jsonObject = null;

		} else {
			this.errorThrown = "Bad Response Data!";
		}

		//print
		this.printHttpProtocol();
		try {
			this.printTargetVisualization();
			this.printJsonText();
			this.printJsonPretty();
		} catch (e) {}
	}

	/**
	 * Callback function used by sendRequest function, in case of error.
	 * @param xhr The XMLHttpRequest object used by ajax
	 * @param textStatus status message from server response
	 * @param errorThrown error message from ajax/XMLHttpRequest object
	 */
	failCallback(xhr, textStatus, errorThrown) {
		this.data = null;
		this.errorThrown = errorThrown;
		this.xhr = xhr;
		this.textStatus = textStatus;

		//set alternative data for printings in case of error
		try {
			this.jsonString = new JsonString(xhr.responseText);
			this.jsonObject = JSON.parse(xhr.responseText);
		} catch (e) {
			this.jsonString = new JsonString("");
			this.jsonObject = null;
		}

		if (this.debugging) {
			console.log("%c failCallback is called", "color: red; font-weight: bold;");
			console.log("print jqXHR");
			console.log(xhr);
			console.log("print errorThrown");
			console.log(errorThrown);
		}

		//print
		this.printHttpProtocol();
		try {
			this.printTargetVisualization();
			this.printJsonText();
			this.printJsonPretty();
		} catch (e) {

		}

		//set specific tabs active
		$("#tabsLeft").tabs({
			active: 1
		});
	}

	/**
	 * Print function to output on "HTTP Protocol" tab
	 * @param protocolId DOM's element ID to append HTML print
	 */
	printHttpProtocol(protocolId = this.protocolId) {
		//do not delete old printings
		//$( "#" + protocolId ).html("");

		//Get the raw header string
		var headers = this.xhr.getAllResponseHeaders();

		//Convert the header string into an array of individual headers
		var headerList = headers.trim().split(/[\r\n]+/);

		//Create a map of header names to values
		var headerMap = {};
		headerList.forEach(function (line) {
			var parts = line.split(': ');
			var header = parts.shift();
			var value = parts.join(': ');
			headerMap[header] = value;
		});

		var html = '';
		var spaceTab = '    '; //works only in <pre><code> enviroment
		var twistTitle;
		var twistColor;
		var twistErrorThrown;
		if (this.errorThrown === null) {
			//sucess twist
			twistTitle = 'Sucess on';
			twistColor = 'color: darkGreen;';
			twistErrorThrown = '';
		} else {
			//error twist
			twistTitle = 'Error on';
			twistColor = 'color: darkRed;';
			twistErrorThrown = '<br>' + spaceTab + 'errorThrown: <span style="font-style: oblique;">' + this.errorThrown + '</span>';
		}

		//make html markup

		//outer frame init
		html += '<pre><code style="color:grey;">';

		//title line
		html += '<span class="bold" style="' + twistColor + '">' + twistTitle + '</span> ';
		html += '<span class="bold" style="color:purple;">' + this.settings.method + '</span> '
		html += this.settings.url;

		//status line
		html += '<br>' + spaceTab + '<span style="color:darkCyan;">status: </span>' + this.xhr.status + ' <span style="font-style: oblique;">' + this.xhr.statusText + '</span>';

		//headers line
		var twistHeader = '<br>' + spaceTab + '<span style="color:darkCyan;">headers: </span>';
		if (headerList.length == 0 || (headerList.length == 1 && headerList[0] == "")) {
			html += twistHeader + "null"
		} else {
			var i;
			for (i = 0; i < headerList.length; i++) {
				var parts = headerList[i].split(': ');
				var header = parts.shift();
				var value = parts.join(': ');
				html += twistHeader;
				twistHeader = '<br>' + spaceTab + spaceTab + spaceTab + ' ';
				html += '<span style="color:blue;">' + header + '</span>: ' + value;
			}
		}

		//print anything for only sucess or only error
		if (this.errorThrown === null) {
			//sucess print
			
		} else {
			//error print
		}

		//outer frame close
		html += '</code></pre><br>';

		//autolink hyperrefs
		html = (new JsonString(html)).autolink();

		//print protocol
		$("#" + protocolId).prepend(html);
	}

	/**
	 * Print function to output on "Target Visualization" tab
	 * @param visualizationId DOM's element ID to append HTML print
	 */
	printTargetVisualization(visualizationId = this.visualizationId) {
		$("#" + visualizationId).html("");

		//print anything for only sucess or only error
		if (this.errorThrown === null) {
			//sucess print
			var webAnnotation = new WebAnnotation(this.jsonObject);
			webAnnotation.visualizeTarget($("#" + visualizationId));

		} else {
			//error print

		}
	}

	/**
	 * Print function to output on "JSON text" tab
	 * @param jsonTextId DOM's element ID to append HTML print
	 */
	printJsonText(jsonTextId = this.jsonTextId) {
		$("#" + jsonTextId).html("");

		//print anything for only sucess or only error
		if (this.errorThrown === null) {
			//sucess print
		} else {
			//error print
		}

		$("#" + jsonTextId).append(this.jsonString.jsonText());
	}

	/**
	 * Print function to output on "JSON pretty" tab
	 * @param jsonPrettyId DOM's element ID to append HTML print
	 */
	printJsonPretty(jsonPrettyId = this.jsonPrettyId) {
		$("#" + jsonPrettyId).html("");

		//print anything for only sucess or only error
		if (this.errorThrown === null) {
			//sucess print
		} else {
			//error print
		}

		$("#" + jsonPrettyId).append(this.jsonString.jsonPretty());
	}
}
