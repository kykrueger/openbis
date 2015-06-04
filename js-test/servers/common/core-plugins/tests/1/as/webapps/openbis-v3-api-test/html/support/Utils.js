/**
 * Library
 */
define([ "support/underscore-min" ], function(_) {
	var STJSUtil = function() {
	}

	STJSUtil.prototype.executeFunctionByName = function(functionName, context) {
		var args = [].slice.call(arguments).splice(2);
		var namespaces = functionName.split(".");
		var func = namespaces.pop();
		for (var i = 0; i < namespaces.length; i++) {
			context = context[namespaces[i]];
		}
		return context[func].apply(this, args);
	}

	STJSUtil.prototype.fromJson = function(jsonObject) {
		var dfd = $.Deferred();

		var types = {}
		collectTypes(jsonObject, types);

		var moduleNames = Object.keys(types).map(function(type) {
			return typeToModuleName(type);
		});
		require(moduleNames, function() {
			var moduleMap = {};

			for (var i = 0; i < arguments.length; i++) {
				var moduleName = moduleNames[i];
				var module = arguments[i];
				moduleMap[moduleName] = module;
			}

			var dto = fromJsonObjectWithTypeOrArrayOrMap(null, jsonObject, {}, moduleMap);
			dfd.resolve(dto);
		});

		return dfd.promise();
	}

	STJSUtil.prototype.decycle = function(object) {
		return decycleLocal(object, []);
	}

	var collectTypes = function(jsonObject, types) {
		if (jsonObject instanceof Array) {
			jsonObject.forEach(function(item) {
				collectTypes(item, types);
			});
		} else if (jsonObject instanceof Object) {
			Object.keys(jsonObject).forEach(function(key) {
				var value = jsonObject[key];
				if (key == "@type") {
					types[value] = value;
				} else {
					collectTypes(value, types);
				}
			});
		}
	}

	var typeToModuleName = function(type) {
		return type.replace(/\./g, '/');
	}

	var fromJsonObjectWithTypeOrArrayOrMap = function(jsonName, jsonObject, hashedObjects, modulesMap) {
		if (jsonObject instanceof Array) {
			var array = [];
			jsonObject.forEach(function(item, index) {
				var dto = fromJsonObjectWithTypeOrArrayOrMap(index, item, hashedObjects, modulesMap);
				array.push(dto);
			});
			return array;
		} else if (jsonObject instanceof Object) {
			if (jsonObject["@type"]) {
				return fromJsonObjectWithType(jsonName, jsonObject, hashedObjects, modulesMap)
			} else {
				var map = {};
				Object.keys(jsonObject).forEach(function(key) {
					var dto = fromJsonObjectWithTypeOrArrayOrMap(key, jsonObject[key], hashedObjects, modulesMap);
					map[key] = dto;
				});
				return map;
			}
		} else {
			if (_.isNumber(jsonObject) && (false == _.isString(jsonName) || (jsonName.indexOf("Date") == -1 && jsonName != "@id"))) {
				// TODO we have to come up with ids that are easier to recognize
				if (jsonObject in hashedObjects) {
					return hashedObjects[jsonObject];
				} else {
					throw "Expected that integer fields are id's of objects, and they should be present in cache"
				}
			} else {
				return jsonObject;
			}
		}
	}

	var fromJsonObjectWithType = function(jsonName, jsonObject, hashedObjects, modulesMap) {
		var jsonId = jsonObject["@id"]
		var jsonType = jsonObject["@type"]

		var moduleName = typeToModuleName(jsonType);
		var module = modulesMap[moduleName];
		var object = new module;

		if (jsonId) {
			if (jsonId in hashedObjects) {
				throw "This object has already been cached!"
			}
			hashedObjects[jsonId] = object
		}

		for ( var key in jsonObject) {
			object[key] = fromJsonObjectWithTypeOrArrayOrMap(key, jsonObject[key], hashedObjects, modulesMap)
		}
		return object;
	};

	var decycleLocal = function(object, references) {
		if (object === null) {
			return object;
		}
		index = _.indexOf(references, object);
		if (index >= 0) {
			return index;
		}
		if (_.isArray(object)) {
			return _.map(object, function(el, key) {
				return decycleLocal(el, references)
			});
		} else if (_.isObject(object)) {
			var result = {};
			if (object["@type"] != null) {
				id = references.length;
				result["@id"] = id;
				references.push(object);
			}
			for ( var i in object) {
				if (_.isFunction(object[i]) === false && i !== "@id") {
					result[i] = decycleLocal(object[i], references);
				}
			}
			return result;
		}
		return object;
	};

	var stjsUtil = new STJSUtil();

	var Serializable = function() {
	};

	function RuntimeException(message) {
		this.name = "RuntimeException";
		this.message = (message || "");
	}
	RuntimeException.prototype = Error.prototype;

	function IllegalArgumentException(message) {
		this.name = "IllegalArgumentException";
		this.message = (message || "");
	}
	IllegalArgumentException.prototype = Error.prototype;

	function NotFetchedException(message) {
		this.name = "NotFetchedException";
		this.message = (message || "");
	}
	NotFetchedException.prototype = Error.prototype;

	return stjsUtil;
});