function FlowCytometryTechnology() {
	this.init();
}

$.extend(FlowCytometryTechnology.prototype, ELNLIMSPlugin.prototype, {
	init: function() {
		
	},
	forcedDisableRTF : [],
	forceMonospaceFont : [],
	sampleTypeDefinitionsExtension : {
				"FACS_ARIA_EXPERIMENT" : {
					"SAMPLE_PARENTS_HINT" : [{
						"LABEL" : "Organization Units",
						"TYPE": "ORGANIZATION_UNIT",
						"ANNOTATION_PROPERTIES" : []
					}]
				},
				"INFLUX_EXPERIMENT" : {
					"SAMPLE_PARENTS_HINT" : [{
						"LABEL" : "Organization Units",
						"TYPE": "ORGANIZATION_UNIT",
						"ANNOTATION_PROPERTIES" : []
					}]
				},
				"LSR_FORTESSA_EXPERIMENT" : {
					"SAMPLE_PARENTS_HINT" : [{
						"LABEL" : "Organization Units",
						"TYPE": "ORGANIZATION_UNIT",
						"ANNOTATION_PROPERTIES" : []
					}]
				},
				"MOFLO_XDP_EXPERIMENT" : {
					"SAMPLE_PARENTS_HINT" : [{
						"LABEL" : "Organization Units",
						"TYPE": "ORGANIZATION_UNIT",
						"ANNOTATION_PROPERTIES" : []
					}]
				},
				"S3E_EXPERIMENT" : {
					"SAMPLE_PARENTS_HINT" : [{
						"LABEL" : "Organization Units",
						"TYPE": "ORGANIZATION_UNIT",
						"ANNOTATION_PROPERTIES" : []
					}]
				},
				"FACS_ARIA_PLATE" : { "SHOW_ON_NAV" : true },
				"FACS_ARIA_SPECIMEN" : { "SHOW_ON_NAV" : true },
				"FACS_ARIA_TUBE" : { "SHOW_ON_NAV" : true },
				"FACS_ARIA_TUBESET" : { "SHOW_ON_NAV" : true },
				"FACS_ARIA_WELL" : { "SHOW_ON_NAV" : true },
				"INFLUX_SPECIMEN" : { "SHOW_ON_NAV" : true },
				"INFLUX_TUBE" : { "SHOW_ON_NAV" : true },
				"INFLUX_TUBESET" : { "SHOW_ON_NAV" : true },
				"LSR_FORTESSA_PLATE" : { "SHOW_ON_NAV" : true },
				"LSR_FORTESSA_SPECIMEN" : { "SHOW_ON_NAV" : true },
				"LSR_FORTESSA_TUBE" : { "SHOW_ON_NAV" : true },
				"LSR_FORTESSA_TUBESET" : { "SHOW_ON_NAV" : true },
				"LSR_FORTESSA_WELL" : { "SHOW_ON_NAV" : true },
				"MOFLO_XDP_SPECIMEN" : { "SHOW_ON_NAV" : true },
				"MOFLO_XDP_TUBE" : { "SHOW_ON_NAV" : true },
				"MOFLO_XDP_TUBESET" : { "SHOW_ON_NAV" : true },
				"S3E_TUBE" : { "SHOW_ON_NAV" : true },
				"S3E_TUBESET" : { "SHOW_ON_NAV" : true },
				"SE3_SPECIMEN" : { "SHOW_ON_NAV" : true }
	},
	dataSetTypeDefinitionsExtension : {

	},
	sampleFormTop : function($container, model) {

    },
	sampleFormBottom : function($container, model) {
	
	},
	dataSetFormTop : function($container, model) {

	},
	dataSetFormBottom : function($container, model) {
		
	}
});
