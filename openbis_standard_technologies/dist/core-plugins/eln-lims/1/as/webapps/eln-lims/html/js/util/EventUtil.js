var EventUtil = new function() {

    var DEFAULT_TIMEOUT = 15000;
    var DEFAULT_TIMEOUT_STEP = 1000;

	this.click = function(elementId) {
		var element = $( "#" + elementId );
		element.focus();
		element.trigger('click');
	};

	this.write = function(elementId, text) {
		var element = $("#" + elementId);
		element.focus();
		$(element).val(text);
		for (var i = 0; i < text.length; i++) {
			$(element).trigger('keydown', {which: text.charCodeAt(i)});
			$(element).trigger('keyup', {which: text.charCodeAt(i)});
		}
	};

    this.waitForId = function(elementId, timeout) {
        return new Promise(function(resolve, reject) {
            if (!timeout) {
                timeout = DEFAULT_TIMEOUT;
            }
            timeout -= DEFAULT_TIMEOUT_STEP;

            if (timeout <= 0) {
                reject(new Error("Element '" + elementId + "' is not exist."));
                return;
            }

            if($("#" + elementId).length <= 0) {
                setTimeout(function() { EventUtil.waitForId(elementId, timeout).then(resolve())
                                                 .catch(reject(new Error("Element '" + elementId + "' is not exist.")));
                                      }, DEFAULT_TIMEOUT_STEP);
            } else {
                resolve();
            }
        });
    };

}