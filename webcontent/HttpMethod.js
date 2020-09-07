/**
 * HttpMethod handle the selected http method by user input to show the sub content to that method.
 * It will make the sub element of the selected http method visible and hide all others.
 * Also can be used to get the String of selected http method.
 * 
 * @author  Matthias Dressel
 * @author  Michael Hitzker
 * @author  Markus Hoefler
 * @author  Andreas Loeffler
 * @author  Timo Schmidt
 * @version 1.0
 */
class HttpMethod {
	/**
	 * Instance an object
	 * @param methods array to list all avaiable http methods
	 * @param checked determines the current selected http methods by user
	 * @param subPrefix DOM id, needed to access the sub contents from the current method.
	 */
	constructor(methods, checked, subPrefix) {
		$.each(methods, function (index, value) {
			value = value.toUpperCase();
		});
		this.methods = methods;
		this.subPrefix = subPrefix;
		this.methodString = checked.toUpperCase();
	}

	/**
	 * httpMethod.method = "text"
	 * Trying to set the method property (methodString) of httpMethod with String "text".
	 * If "text" does not match with the kown methods (set by constructor) the set attempt will be ignored.
	 * If "text" is the same as last time, the sub content for that methods is toogled
	 * else the new set methods subcontent will be visible and the replaced one will be hidden.
	 * @param input should be a value from listed methods
	 */
	set method(input) {
		input = input.toUpperCase();
		var i;
		for (i = 0; i < this.methods.length; i++) {
			if (input == this.methods[i]) {
				if (input == this.methodString) {
					this.toggleMethod();
				} else {
					this.hideMethod();
					this.methodString = input;
					this.showMethod();
				}
				break;
			}
		}
	}

	/**
	 *  xyz = httpMethod.method
	 *  @return methodString value will be returned as the method property
	 */
	get method() {
		return this.methodString;
	}

	/**
	 * Hides the outer div container from the subcontent for the current method property. 
	 */
	hideMethod() {
		if (this.methodString != "") {
			$("#" + this.subPrefix + this.methodString.substring(0, 1).toUpperCase() + this.methodString.substring(1).toLowerCase()).hide();
		}
	}

	/**
	 * Shows the outer div container from the subcontent for the current method property.
	 */
	showMethod() {
		if (this.methodString != "") {
			$("#" + this.subPrefix + this.methodString.substring(0, 1).toUpperCase() + this.methodString.substring(1).toLowerCase()).show();
		}
	}

	/**
	 * Toggles the outer div container from the subcontent for the current method property.
	 */
	toggleMethod() {
		if (this.methodString != "") {
			$("#" + this.subPrefix + this.methodString.substring(0, 1).toUpperCase() + this.methodString.substring(1).toLowerCase()).toggle();
		}
	}
}
