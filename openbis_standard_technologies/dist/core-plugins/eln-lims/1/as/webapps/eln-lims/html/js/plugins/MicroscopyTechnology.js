function MicroscopyTechnology() {
	this.init();
}

$.extend(MicroscopyTechnology.prototype, ELNLIMSPlugin.prototype, {
	init: function () {

	},
	forcedDisableRTF: [],
	forceMonospaceFont: [],
	sampleTypeDefinitionsExtension: {
		"MICROSCOPY_SAMPLE_TYPE": {
			"SHOW_ON_NAV": true,
			"SHOW": false,
			"SAMPLE_CHILDREN_DISABLED": true,
			"SAMPLE_PARENTS_DISABLED": true,
			"TOOLBAR": { CREATE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true }
		},
		"MICROSCOPY_EXPERIMENT": {
			"SHOW": false,
			"SAMPLE_CHILDREN_DISABLED": false,
			"SAMPLE_PARENTS_DISABLED": false,
			"SAMPLE_PARENTS_ANY_TYPE_DISABLED": true,
			"TOOLBAR": { CREATE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
			"SAMPLE_PARENTS_HINT": [{
				"LABEL": "Organization Units",
				"TYPE": "ORGANIZATION_UNIT",
				"ANNOTATION_PROPERTIES": []
			}]
		}
	},
	dataSetTypeDefinitionsExtension: {
		"MICROSCOPY_ACCESSORY_FILE": {
			"DATASET_PARENTS_DISABLED": true,
			"TOOLBAR": { EDIT: true, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
		},
		"MICROSCOPY_IMG": {
			"DATASET_PARENTS_DISABLED": true,
			"TOOLBAR": { EDIT: true, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
		},
		"MICROSCOPY_IMG_OVERVIEW": {
			"DATASET_PARENTS_DISABLED": true,
			"TOOLBAR": { EDIT: true, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
		},
		"MICROSCOPY_IMG_THUMBNAIL": {
			"DATASET_PARENTS_DISABLED": true,
			"TOOLBAR": { EDIT: true, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
		},
		"MICROSCOPY_IMG_CONTAINER": {
			"DATASET_PARENTS_DISABLED": true,
			"TOOLBAR": { EDIT: true, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
		}
	},
	sampleFormTop: function ($container, model) {
		if (model.sample && model.sample.sampleTypeCode == "MICROSCOPY_EXPERIMENT") {
			this.displayExperimentThumbnails($container, model, model.sample);
		}
		if (model.datasets && model.datasets.length > 0) {
			var imageViewerDataSets = [];
			for (var dsIdx = 0; dsIdx < model.datasets.length; dsIdx++) {
				if (profile.isImageViewerDataSetCode(model.datasets[dsIdx].dataSetTypeCode)) {
					imageViewerDataSets.push(model.datasets[dsIdx].code);
				}
			}

			if (imageViewerDataSets.length > 0) {
				require(["openbis-screening", "components/imageviewer/ImageViewerWidget"], function (openbis, ImageViewerWidget) {
					var screningFacade = new openbis(null);
					screningFacade._internal.sessionToken = mainController.openbisV1._internal.sessionToken;

					// Create the image viewer component for the specific data sets
					var widget = new ImageViewerWidget(screningFacade, imageViewerDataSets);

					// Render the component and add it to the page
					$container.append($('<legend>').text('Microscopy Viewer'));
					var $imageWidgetContainer = new $('<div>');
					$imageWidgetContainer.css("margin", "20px");
					$container.append($imageWidgetContainer);
					$imageWidgetContainer.append(widget.render());
				});
			}
		}
	},
	sampleFormBottom: function ($container, model) {

	},
	dataSetFormTop: function ($container, model) {
		if (model.dataSetV3 && profile.isImageViewerDataSetCode(model.dataSetV3.type.code)) {
			require(["openbis-screening", "components/imageviewer/ImageViewerWidget"], function (openbis, ImageViewerWidget) {
				var screningFacade = new openbis(null);
				screningFacade._internal.sessionToken = mainController.openbisV1._internal.sessionToken;

				// Create the image viewer component for the specific data sets
				var widget = new ImageViewerWidget(screningFacade, [model.dataSetV3.permId.permId]);

				// Render the component and add it to the page
				$container.append($('<legend>').text('Microscopy Viewer'));
				var $imageWidgetContainer = new $('<div>');
				$imageWidgetContainer.css("margin", "20px");
				$container.append($imageWidgetContainer);
				$imageWidgetContainer.append(widget.render());
			});
		}
	},
	dataSetFormBottom: function ($container, model) {

	},

	displayExperimentThumbnails: function ($container, model, microscopyExperimentSample) {
		if (microscopyExperimentSample.sampleTypeCode !== "MICROSCOPY_EXPERIMENT") {
			return;
		}

		// Clear the container
		$container.empty();

		if (microscopyExperimentSample.children.length === 0) {
			return;
		}

		// Add legend
		var legend = $("<legend>")
			.text("File previews");
		$container.append(legend);

		// Prepare a div to host the thumbnails
		var sampleView_div = $("<div>");
		$container.append(sampleView_div);

		var newThumbRow = null;
		var numSample = 0;

		var thisObj = this;

		// Prepare the display
		microscopyExperimentSample.children.forEach(function (sample) {

			// Keep track of the number of the sample
			numSample++;

			// Add a new row for the next three thumbnails
			if (numSample % 3 === 1) {
				newThumbRow = $("<div />", { class: "row" });
				sampleView_div.append(newThumbRow);
			}

			// Prepare the name to be shown
			var name;
			if (sample.properties.MICROSCOPY_SAMPLE_NAME) {
				name = sample.properties.MICROSCOPY_SAMPLE_NAME;
			} else {
				name = sample.code;
			}

			// Make sure it is not too long
			var displayName;
			var l = name.length;
			if (l > 40) {
				displayName = name.substring(0, 18) + "..." + name.substring(l - 18);
			} else {
				displayName = name;
			}

			// If the size property exists (this was added later), retrieve it and display it as well
			var datasetSize;
			if (sample.properties.MICROSCOPY_SAMPLE_SIZE_IN_BYTES) {
				datasetSize = thisObj.formatSizeForDisplay(sample.properties.MICROSCOPY_SAMPLE_SIZE_IN_BYTES);
			} else {
				datasetSize = "";
			}

			// A column to be added to current row that will store all
			// elements related to current sample
			var newThumbCol = $("<div />",
				{
					class: "col-md-4",
					"text-align": "center",
					id: sample.code
				});

			// A div element to contain the thumbnail and its info
			var thumbnailView = $("<div />")
				.css("min-height", "320px")
				.css("text-align", "center")
				.css("padding-top", "10px")
				.css("padding-bottom", "10px")

			// Link to the dataset (sample) viewer.
			var link = $("<a>").text(displayName).attr("href", "#").attr("title", name).click(
				function () {
					window.top.location.hash = "#" + (new Date().getTime());
					return false;
				});

			// Actual thumbnail. Initially we display a place holder. Later,
			// we will replace it asynchronously.
			var thumbnailImage = $("<img />",
				{
					src: "./img/wait.png",
					class: "img-responsive",
					display: "inline",
					"text-align": "center",
					id: "image_" + sample.code,
					title: name
				});

			// Build the thumbnail viewer
			thumbnailView.append(thumbnailImage);
			thumbnailView.append($("<br />"));
			thumbnailView.append(link);
			if (datasetSize !== "") {
				thumbnailView.append($("<br />"));
				var spanSz = $("<span>").text(datasetSize);
				thumbnailView.append(spanSz);
			}

			// Add the thumbnail to the column and the row
			newThumbCol.append(thumbnailView);
			newThumbRow.append(newThumbCol);

			// Now retrieve the link to the thumbnail image asynchronously and update the <img>
			thisObj.displayThumbnailForSample(model, sample, "image_" + sample.code);

		});
	},

	formatSizeForDisplay: function (datasetSize) {

		// Output
		var formattedDatasetSize = "";

		// Cast datasetSize to float
		var datasetSizeF = parseFloat(datasetSize);

		var sMB = datasetSizeF / 1024.0 / 1024.0;
		if (sMB < 1024.0) {
			formattedDatasetSize = sMB.toFixed(2) + " MiB";
		} else {
			var sGB = sMB / 1024.0;
			formattedDatasetSize = sGB.toFixed(2) + " GiB";
		}

		return formattedDatasetSize;
	},

	displayThumbnailForSample: function (model, sample, img_id) {

		// Get the datasets with type "MICROSCOPY_IMG_THUMBNAIL" for current sample
		this.getMicroscopyImgThumbnailDataSetsForMicroscopySample(
			model,
			sample.experimentIdentifierOrNull,
			sample.code, function (dataset) {

				// Get the containers
				if (dataset == null) {
					return;
				}

				// Retrieve the file for the dataset and the associated URL
				mainController.openbisV1.listFilesForDataSet(dataset.code, '/', true,
					function (response) {

						// Make sure that we got some results from the DSS to process
						if (response.error) {

							// Thumbnail not found!
							var imD = $("#" + img_id);
							imD.attr("src", "./img/error.png");
							imD.attr("title", "Could not find any files associated to this dataset!");

							return;

						}

						// Find the thumbnail.png file
						response.result.forEach(function (f) {

							if (!f.isDirectory && f.pathInDataSet.toLowerCase() === "thumbnail.png") {

								// Retrieve the file URL
								mainController.openbisV1.getDownloadUrlForFileForDataSetInSession(
									dataset.code, f.pathInDataSet, function (url) {

										// Replace the image
										var eUrl = encodeURI(url);
										eUrl = eUrl.replace('+', '%2B');
										$("#" + img_id).attr("src", eUrl);

									});
							} else {

								// Thumbnail not found!
								var imD = $("#" + img_id);
								imD.attr("src", "./img/error.png");
								imD.attr("title", "Could not find a thumbnail for this dataset!");

							}
						});

					});

			});

	},

	getMicroscopyImgThumbnailDataSetsForMicroscopySample: function (model, expCode, sampleCode, action) {

		// Experiment criteria (experiment of type "COLLECTION" and code expCode)
		var experimentCriteria = new SearchCriteria();
		experimentCriteria.addMatchClause(
			SearchCriteriaMatchClause.createAttributeMatch(
				"CODE", expCode)
		);
		experimentCriteria.addMatchClause(
			SearchCriteriaMatchClause.createAttributeMatch(
				"TYPE", "COLLECTION")
		);

		// Sample criteria (sample of type "MICROSCOPY_SAMPLE_TYPE" and code sampleCode)
		var sampleCriteria = new SearchCriteria();
		sampleCriteria.addMatchClause(
			SearchCriteriaMatchClause.createAttributeMatch(
				"CODE", sampleCode)
		);
		sampleCriteria.addMatchClause(
			SearchCriteriaMatchClause.createAttributeMatch(
				"TYPE", "MICROSCOPY_SAMPLE_TYPE")
		);

		// Dataset criteria
		var datasetCriteria = new SearchCriteria();
		datasetCriteria.addMatchClause(
			SearchCriteriaMatchClause.createAttributeMatch(
				"TYPE", "MICROSCOPY_IMG_CONTAINER")
		);

		// Add sample and experiment search criteria as subcriteria
		datasetCriteria.addSubCriteria(
			SearchSubCriteria.createSampleCriteria(sampleCriteria)
		);
		datasetCriteria.addSubCriteria(
			SearchSubCriteria.createExperimentCriteria(experimentCriteria)
		);

		// Search
		mainController.openbisV1.searchForDataSets(datasetCriteria, function (response) {

			// Get the containers
			if (response.error || response.result.length === 0) {
				return null;
			}

			// All MICROSCOPY_IMG_CONTAINER datasets (i.e. a file series) contain a MICROSCOPY_IMG_OVERVIEW
			// and a MICROSCOPY_IMG dataset; one of the series will also contain a MICROSCOPY_IMG_THUMBNAIL,
			// which is what we are looking for here.
			// Even though the MICROSCOPY_IMG_THUMBNAIL is always created for series 0, we cannot guarantee
			// here that series zero will be returned as the first. We quickly scan through the returned
			// results for the MICROSCOPY_IMG_CONTAINER that has three contained datasets.
			// From there we can then quickly retrieve the MICROSCOPY_IMG_THUMBNAIL.
			for (var i = 0; i < response.result.length; i++) {
				var series = response.result[i];
				for (var j = 0; j < series.containedDataSets.length; j++) {
					if (series.containedDataSets[j].dataSetTypeCode === "MICROSCOPY_IMG_THUMBNAIL") {
						action(series.containedDataSets[j]);
						return;
					}
				}
			}
		});

	}
});
