var EventUtil = new function() {

    var DEFAULT_TIMEOUT = 15000;
    var DEFAULT_TIMEOUT_STEP = 1000;

	this.click = function(elementId) {
	    return new Promise(function executor(resolve, reject) {
	        try {
                var element = $( "#" + elementId );
                if(!element) {
                    throw "Element not found: #" + elementId;
                }
                element.focus();
                element.trigger('click');
                resolve();
            } catch(error) {
                reject();
            }
	    });
	};

	this.change = function(elementId, value) {
        var element = $( "#" + elementId );
        element.focus();
        element.val(value).change();
    };

    this.checked = function(elementId, value) {
        var element = $( "#" + elementId );
        element.focus();
        element.prop('checked', value);
    };

	this.write = function(elementId, text) {
	    return new Promise(function executor(resolve, reject) {
	        try {
                var element = $( "#" + elementId );
                if(!element) {
                    throw "Element not found: #" + elementId;
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

    this.waitForId = function(elementId, timeout) {
        return new Promise(function executor(resolve, reject) {
            if (!timeout) {
                timeout = DEFAULT_TIMEOUT;
            }
            timeout -= DEFAULT_TIMEOUT_STEP;

            if (timeout <= 0) {
                reject(new Error("Element '" + elementId + "' is not exist."));
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