var EventUtil = new function() {

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

    this.waitForId = function(elementId, action) {
        if(!document.querySelector("#" + elementId)) {
            setTimeout(this.waitForElement,500);
        } else {
            action();
        }
    };
}