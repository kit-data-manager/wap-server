/**
 * Manages the access and loading of example data. 
 * 
 * exampleDataContainer
 * exampleDataPerson
 * exampleDataSerialized
 * exampleDataSvg
 * exampleDataSimple
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.0
 */
class ExampleData {
	/**
	 * Instance an ExampleData object.
	 * @param filename String used to request a file content with ajax.
	 */
	constructor(filename) {
		this.filename = filename;
		this.responseText = "empty, no response";
		
		this.request();
	}
	
	/**
	 * Perform a ajax XHR request to load the content of file.
	 * @param filename The Name of the File beeing loaded.
	 */
	request(filename = this.filename){
		var callback = this;
		
		callback.settings = {
				url: "",
				method: "GET",
				cache: true,
				dataType: "text",
				headers: {},
				crossDomain: true,
				dataFilter: function (data, type) {
					//return raw data
					return data;
				},
				xhrFields: {
					withCredentials: false
				}
			}
		
		if (window.location.origin !== "null") {
			let contextpath = !window.location.pathname.startsWith("/webapp") ? window.location.pathname.split("/webapp").at(0) : ""
			callback.settings["url"] = window.location.origin + contextpath + "/webapp/" + "resources/examples/" + filename;
		} else {
			return false;
		}
		
		$.ajax(callback.settings)
			.done(function (data, textStatus, xhr) {
				callback.responseText = xhr.responseText;
			})
			.fail(function (xhr, textStatus, errorThrown) {
				callback.responseText = xhr.responseText;
			});
	}
	
	/**
	 * Get the content of the file loaded by constructor.
	 * This might be also the default value, when the request has not responded yet
	 * or the fail response text from server. 
	 * @return file content loaded by constructor
	 */
	get text (){
		return this.responseText;
	}
	
	/**
	 * Less an example, more a shematic to POST a container easily.
	 * @return exampleDataContainer 
	 */
	static container (){
		return new ExampleData("exampleDataContainer.jsonld");
	}
	
	/**
	 * An serialized example for a Web Annotation.
	 * @return exampleDataSerialized 
	 */
	static serialized (){
		return new ExampleData("exampleDataSerialized.jsonld");
	}
	
	/**
	 * An simplified example for a Web Annotation.
	 * @return exampleDataSimplified 
	 */
	static simplified (){
		return new ExampleData("exampleDataSimplified.jsonld");
	}
	
	/**
	 * An simplified example for a Web Annotation with diffrent SVG selectors.
	 * @return exampleDataSvg 
	 */
	static svg (){
		return new ExampleData("exampleDataSvg.jsonld");
	}
	
	/**
	 * An example for a JSON file but not really a Web Annotation.
	 * @return exampleDataPerson 
	 */
	static person (){
		return new ExampleData("exampleDataPerson.jsonld");
	}
}