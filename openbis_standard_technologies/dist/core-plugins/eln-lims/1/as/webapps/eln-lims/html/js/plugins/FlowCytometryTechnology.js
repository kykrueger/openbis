function FlowCytometryTechnology() {
	this.init();
}

$.extend(FlowCytometryTechnology.prototype, ELNLIMSPlugin.prototype, {
	init: function () {

	},
	forcedDisableRTF: [],
	forceMonospaceFont: [],
	sampleTypeDefinitionsExtension: {
		"FACS_ARIA_EXPERIMENT": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SAMPLE_PARENTS_HINT": [{
				"LABEL": "Organization Units",
				"TYPE": "ORGANIZATION_UNIT",
				"ANNOTATION_PROPERTIES": []
			}]
		},
		"INFLUX_EXPERIMENT": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SAMPLE_PARENTS_HINT": [{
				"LABEL": "Organization Units",
				"TYPE": "ORGANIZATION_UNIT",
				"ANNOTATION_PROPERTIES": []
			}]
		},
		"LSR_FORTESSA_EXPERIMENT": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SAMPLE_PARENTS_HINT": [{
				"LABEL": "Organization Units",
				"TYPE": "ORGANIZATION_UNIT",
				"ANNOTATION_PROPERTIES": []
			}]
		},
		"MOFLO_XDP_EXPERIMENT": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SAMPLE_PARENTS_HINT": [{
				"LABEL": "Organization Units",
				"TYPE": "ORGANIZATION_UNIT",
				"ANNOTATION_PROPERTIES": []
			}]
		},
		"S3E_EXPERIMENT": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SAMPLE_PARENTS_HINT": [{
				"LABEL": "Organization Units",
				"TYPE": "ORGANIZATION_UNIT",
				"ANNOTATION_PROPERTIES": []
			}]
		},
		"FACS_ARIA_SPECIMEN": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"FACS_ARIA_TUBE": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"FACS_ARIA_TUBESET": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"INFLUX_SPECIMEN": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"INFLUX_TUBE": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"INFLUX_TUBESET": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"LSR_FORTESSA_PLATE": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"LSR_FORTESSA_SPECIMEN": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"LSR_FORTESSA_TUBE": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"LSR_FORTESSA_TUBESET": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"LSR_FORTESSA_WELL": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"MOFLO_XDP_SPECIMEN": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"MOFLO_XDP_TUBE": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"MOFLO_XDP_TUBESET": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"S3E_TUBE": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"S3E_TUBESET": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		},
		"S3E_SPECIMEN": {
			"TOOLBAR": { CREATE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SHOW_ON_NAV": true
		}
	},
	dataSetTypeDefinitionsExtension: {
		"FACS_ARIA_FCSFILE": {
			"DATASET_PARENTS_DISABLED": true,
			"TOOLBAR": { EDIT: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
		},
		"INFLUX_FCSFILE": {
			"DATASET_PARENTS_DISABLED": true,
			"TOOLBAR": { EDIT: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
		},
		"LSR_FORTESSA_FCSFILE": {
			"DATASET_PARENTS_DISABLED": true,
			"TOOLBAR": { EDIT: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
		},
		"MOFLO_XDP_FCSFILE": {
			"DATASET_PARENTS_DISABLED": true,
			"TOOLBAR": { EDIT: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
		},
		"S3E_ARIA_FCSFILE": {
			"DATASET_PARENTS_DISABLED": true,
			"TOOLBAR": { EDIT: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
		}
	},
	sampleFormTop: function ($container, model) {

	},
	sampleFormBottom: function ($container, model) {

	},
	dataSetFormTop: function ($container, model) {

	},
	dataSetFormBottom: function ($container, model) {

	}
});
