/**
 * Main.js is the entry point to all javascript used in the web based mini-frontend.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.0
 */

/**
 * Global variables
 */
var httpMethod;
var httpRequest;

/**
 * Init javascript after DOM is loaded, because then all variables in the layout are accessable.
 */
$(document).ready(function () {
	//instance all global variables/objects

	//instance httpMethod
	httpMethod = new HttpMethod(Main.makeHttpMethodList(), $("input[name=httpRadio]:checked").val(), "sub");

	//instance httpRequest
	httpRequest = undefined;

	//call main after all global variables/objects have been instanced
	Main.main();
});

/**
 * main: init javascript goes here. Usually just once called function for layout, design, event Handler ...
 */
class Main {
	constructor() {
		//Main should not be instanced
		//declare as "static class" does not work
	}

	/**
	 * Run javascript for the dynamic web fontend 
	 */
	static main() {
		this.init();
	}

	/**
	 * Call all init functions at once
	 */
	static init() {
		this.initJQueryOverrides();
		this.initEventHandler();
		//init value for targetUrl
		if (window.location.origin !== "null") {
			let contextpath = !window.location.pathname.startsWith("/webapp") ? window.location.pathname.split("/webapp").at(0) : ""
			$("#targetUrl").val(window.location.origin + contextpath + "/wap/");
		}
	}

	/**
	 * JQueryUI Overrides
	 */
	static initJQueryOverrides() {
		//hide radiobutton icons 
		$(function () {
			$("input[name=httpRadio]").checkboxradio({
				icon: false
			});
		});

		//unset fieldset width 
		$(function () {
			$("fieldset").controlgroup();
		});

		//activate JQueryUi tabs to override html div-tabs
		$(function () {
			$("#tabsLeft").tabs();
			$("#tabsRight").tabs();
		});

	}

	/**
	 * bind event handler
	 */
	static initEventHandler() {
		//event handler for selecting http method
		$("input[name=httpRadio]").click(function () {
			httpMethod.method = this.value;
		});

		//event handler for submit button
		$("#submitButton").click(function () {
			httpRequest = new HttpRequest();
			HttpRequest.sendRequest(httpRequest);
		});
		
		//event handler for dropped link on target URL
		$("#targetUrl").on("dragover", false);
		$("#targetUrl").on("drop", function(event){
			$("#targetUrl").val("");
		});

		//event handler to synchronise subGet content
		//"page"
		$("#subGetPage").change(function () {
			Main.synchroniseTargetUrl("page", $("#subGetPage").val());
		});
		//"iris"
		$("#subGetIris").change(function () {
			Main.synchroniseTargetUrl("iris", $("#subGetIris").val());
		});
		//targetUrl
		$("#targetUrl").change(function () {
			Main.synchroniseSubGet($("#targetUrl").val());
		});

		//event handler for copy last response by subPost JSON textarea
		$("#subPostJsonCopyButton").click(function () {
			try {
				$("#subPostJson").val(httpRequest.jsonString.jsonString);
			} catch (e) {
				$("#subPostJson").val("");
			}
		});

		//event handler for clear by subPost JSON textarea
		$("#subPostJsonClearButton").click(function () {
			$("#subPostJson").val("");
		});

		//event handler for copy last response by subPut JSON textarea
		$("#subPutJsonCopyButton").click(function () {
			try {
				$("#subPutJson").val(httpRequest.jsonString.jsonString);
			} catch (e) {
				$("#subPutJson").val("");
			}
		});

		//event handler for clear by subPut JSON textarea
		$("#subPutJsonClearButton").click(function () {
			$("#subPutJson").val("");
		});

		//event handler for copy by subPut Etag input
		$("#subPutEtagCopyButton").click(function () {
			try {
				$("#subPutEtag").val(httpRequest.xhr.getResponseHeader("Etag"));
			} catch (e) {
				$("#subPutEtag").val("not found");
			}

		});

		//event handler for copy by subDelete Etag input
		$("#subDeleteEtagCopyButton").click(function () {
			try {
				$("#subDeleteEtag").val(httpRequest.xhr.getResponseHeader("Etag"));
			} catch (e) {
				$("#subDeleteEtag").val("not found");
			}

		});
		
		

		//event handler for exampleContainer by subPost
		var exampleDataContainer = ExampleData.container();
		$("#subPostJsonExampleContainerButton").click(function () {
			$("#subPostJson").val(Main.trimLinebreaks(exampleDataContainer.text));
		});
		//event handler for example1 by subPost
		var exampleDataSerialized = ExampleData.serialized();
		$("#subPostJsonExample1Button").click(function () {
			$("#subPostJson").val(Main.trimLinebreaks(exampleDataSerialized.text));
		});
		//event handler for example2 by subPost
		var exampleDataSimplified = ExampleData.simplified();
		$("#subPostJsonExample2Button").click(function () {
			$("#subPostJson").val(Main.trimLinebreaks(exampleDataSimplified.text));
		});
		//event handler for example3 by subPost
		var exampleDataSvg = ExampleData.svg();
		$("#subPostJsonExample3Button").click(function () {
			$("#subPostJson").val(Main.trimLinebreaks(exampleDataSvg.text));
		});
		//event handler for example4 by subPost
		var exampleDataPerson = ExampleData.person();
		$("#subPostJsonExample4Button").click(function () {
			$("#subPostJson").val(Main.trimLinebreaks(exampleDataPerson.text));
		});

		//event handler for example1 by subPut
		$("#subPutJsonExample1Button").click(function () {
			$("#subPutJson").val(Main.trimLinebreaks(exampleDataSerialized.text));
		});
		//event handler for example2 by subPut
		$("#subPutJsonExample2Button").click(function () {
			$("#subPutJson").val(Main.trimLinebreaks(exampleDataSimplified.text));
		});
		//event handler for example3 by subPut
		$("#subPutJsonExample3Button").click(function () {
			$("#subPutJson").val(Main.trimLinebreaks(exampleDataSvg.text));
		});
		//event handler for example4 by subPut
		$("#subPutJsonExample4Button").click(function () {
			$("#subPutJson").val(Main.trimLinebreaks(exampleDataPerson.text));
		});
	}

	/**
	 * Static help function for global use.
	 * @param text input String
	 * @return Remove linebreaks at beginn and end of text
	 */
	static trimLinebreaks(text) {
		text = text.replace(/^(\n\r|\r\n|\n|\r)/ig, "");
		text = text.replace(/(\n\r|\r\n|\n|\r)$/ig, "");
		return text;
	}

	/**
	 * Static help function for global use.
	 * @param text input String
	 * @return Replace Linebreaks: rn, nr, r, n to br 
	 */
	static replaceLinebreaksToBr(text) {
		return text.replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/ig, '$1' + "<br>" + '$2');
	}

	/**
	 * Static help function for global use.
	 * @param text input String
	 * @return Replace space chracters with html ententity: &nbsp;
	 */
	static replaceSpaceToNbsp(text) {
		return text.replace(/\s/ig, '&nbsp;');
	}

	/**
	 * Static help function for global use.
	 * @param text input String
	 * @return true if text matches regex for ContainerType
	 * 			false else
	 */
	static isTypeContainer(text) {
		var match = text.match(/("@type":)\s*\[?\s*"(ldp:Container|ldp:BasicContainer|AnnotationCollection|http:\/\/www.w3.org\/ns\/ldp#BasicContainer)"/ig);
		return match !== null;
	}

	/**
	 * Make an array of all http method values which are set in html by input with name=httpRadio
	 * @reutrn Array of http method values.
	 */
	static makeHttpMethodList() {
		var methods = [];
		$("input[name=httpRadio]").each(function (index, element) {
			methods[methods.length] = $(element).val();
		});
		return methods;
	}

	/**
	 * Update a specific TargetUrl query param
	 * @param queryName The query param name to update
	 * @param queryValue The query param value 
	 */
	static synchroniseTargetUrl(queryName, queryValue) {
		//set queryParam
		var queryParam = "";
		if (queryValue != "") {
			queryParam = queryName + "=" + queryValue;
		}

		//set url
		var url = $("#targetUrl").val();

		//set query
		var query = url.match(/\?.+/g);
		if (query === null) {
			if (queryParam == "") {
				query = "";
			} else {
				query = "?" + queryParam;
			}

		} else {
			query = query[0];
			if (query.match(new RegExp(queryName + "=[^&]*", "ig")) === null) {
				//add queryParam
				if (queryParam != "") {
					query = query + "&" + queryParam;
				}
			} else {
				//replace queryParam
				query = query.replace(new RegExp(queryName + "=[^&]*", "ig"), queryParam);
				if (queryParam == "") {
					//clean query string bugs
					query = query.replace(/(&$|\?$)/ig, "");
					query = query.replace(/^\?&/ig, "?");
					query = query.replace(/^&&/ig, "&");
				}
			}
		}

		//update targetUrl
		if (url.match(/\?.*/g) === null) {
			$("#targetUrl").val(url + query);
		} else {
			$("#targetUrl").val(url.replace(/\?.*/ig, query));
		}
	}

	/**
	 * Update the subGet content params "page" and "iris"
	 * @param url The url string to update from.
	 */
	static synchroniseSubGet(url) {
		//set page
		var page = url.match(/page=[^&]*/ig);
		if (page === null) {
			page = "";
		} else {
			page = page[0].replace(/page=/ig, "");
		}
		$("#subGetPage").val(page);

		//set iris
		var iris = url.match(/iris=[^&]*/ig);
		if (iris === null) {
			iris = "";
		} else {
			iris = iris[0].replace(/iris=/ig, "");
		}
		$("#subGetIris").val(iris);
	}
}
