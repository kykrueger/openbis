var EventUtil = new function() {

    var DEFAULT_TIMEOUT = 15000;
    var DEFAULT_TIMEOUT_STEP = 1000;

	this.click = function(elementId, ignoreError) {
	    return new Promise(function executor(resolve, reject) {
	        try {
                var element = $( "#" + elementId );
                if(!element) {
                    if(ignoreError) {
                        resolve();
                    } else {
                        throw "Element not found: #" + elementId;
                    }
                }
                element.focus();
                element.trigger('click');
                resolve();
            } catch(error) {
                reject();
            }
	    });
	};

	this.change = function(elementId, value, ignoreError) {
        return new Promise(function executor(resolve, reject) {
            try {
                var element = $( "#" + elementId );
                if(!element) {
                    if(ignoreError) {
                        resolve();
                    } else {
                        throw "Element not found: #" + elementId;
                    }
                }
                element.focus();
                element.val(value).change();
                resolve();
            } catch(error) {
                reject();
            }
        });
    };

    this.checked = function(elementId, value, ignoreError) {
        return new Promise(function executor(resolve, reject) {
            try {
                var element = $( "#" + elementId );
                if(!element) {
                    if(ignoreError) {
                        resolve();
                    } else {
                        throw "Element not found: #" + elementId;
                    }
                }
                element.focus();
                element.prop('checked', value);
                resolve();
            } catch(error) {
                reject();
            }
        });
    };

	this.write = function(elementId, text, ignoreError) {
	    return new Promise(function executor(resolve, reject) {
	        try {
                var element = $( "#" + elementId );
                if(!element) {
                    if(ignoreError) {
                        resolve();
                    } else {
                        throw "Element not found: #" + elementId;
                    }
                }
                element.focus();
                $(element).val(text);
                for (var i = 0; i < text.length; i++) {
                    $(element).trigger('keydown', {which: text.charCodeAt(i)});
                    $(element).trigger('keyup', {which: text.charCodeAt(i)});
                }
                resolve();
            } catch(error) {
                reject();
            }
	    });
	};

	this.verifyExistence = function(elementId, isExist, ignoreError) {
	    return new Promise(function executor(resolve, reject) {
	        var elementExistence = $("#" + elementId).length > 0;
	        if (elementExistence == isExist) {
                resolve();
	        } else {
	            if (ignoreError) {
	                resolve();
                } else {
                    throw "Element " + (isExist ? "not" : "") + " found: #" + elementId;
                }
	        }
	    });
	}

    this.waitForId = function(elementId, ignoreError, timeout) {
        return new Promise(function executor(resolve, reject) {
            if (!timeout) {
                timeout = DEFAULT_TIMEOUT;
            }
            timeout -= DEFAULT_TIMEOUT_STEP;

            if (timeout <= 0) {
                if(ignoreError) {
                    resolve();
                } else {
                    reject(new Error("Element '" + elementId + "' is not exist."));
                }
                return;
            }

            if($("#" + elementId).length <= 0) {
                setTimeout(executor.bind(null, resolve, reject), DEFAULT_TIMEOUT_STEP);
            } else {
                resolve();
            }
        });
    };

}