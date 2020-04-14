function BBBHubTechnology() {
    this.init();
}

$.extend(BBBHubTechnology.prototype, ELNLIMSPlugin.prototype, {
    // This code is copy of openBis plugin. It was taken from here:
    // https://sissource.ethz.ch/sis/bbb-hub/tree/master/openbis/core-plugins/bbb-hub/1/as/webapps/snakemake/html/js
    // Now it is a ELN plugin.

	init: function() {
        loadJSResorce("./plugins/bbb-hub/UiComponents.js");
        loadJSResorce("./plugins/bbb-hub/BBBServerFacade.js");
        loadJSResorce("./plugins/bbb-hub/snakemake-table.js");
        loadJSResorce("./plugins/bbb-hub/snakemake-trigger.js");
        this.configureFancyTree("LAB_NOTEBOOK", "Datasets");
	},

	experimentFormTop : function($container, model) {
	    BBBServerFacade.getExperiment($container, model);
    },

    experimentFormBottom : function($container, model) {
    },

    getExtraUtilities : function() {
        return [{
            icon : "fa fa-table",
            uniqueViewName : "BBB_VIEW_NAME_TEST",
            label : "Index Page",
            paintView : function($header, $content) {
                    $header.append($("<h1>").append("Public Index Page"));
                    BBBServerFacade.getExperiments($content);
                }
            }];
    },

    configureFancyTree : function(elementId, newName, timeout) {
        var DEFAULT_TIMEOUT_STEP = 300;

        return new Promise(function executor(resolve, reject) {
            if($("#" + elementId).length <= 0) {
                setTimeout(executor.bind(null, resolve, reject), DEFAULT_TIMEOUT_STEP);
            } else {
                $("#" + elementId).html(newName);
                resolve();
            }
        });
    }
});

profile.plugins.push(new BBBHubTechnology());