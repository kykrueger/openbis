function ELNLIMSPlugin() {
	this.init();
}

$.extend(ELNLIMSPlugin.prototype, {
	init: function() {
	
	},
	forcedDisableRTF : ["$NAME"],
	forceMonospaceFont : [],
	sampleTypeDefinitionsExtension : {
	
	},
	dataSetTypeDefinitionsExtension : {
	
	},
	experimentFormTop : function($container, model) {

    },
    experimentFormBottom : function($container, model) {

    },
	sampleFormTop : function($container, model) {
	
	},
	sampleFormBottom : function($container, model) {
	
	},
	dataSetFormTop : function($container, model) {
	
	},
	dataSetFormBottom : function($container, model) {

	},
	onSampleSave : function(sample, changesToDo, success, failed) {
        success();
	},
	extraUtilities : function() {
	    return [];
	}
});