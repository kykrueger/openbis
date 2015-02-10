define([ 'jquery', 'openbis-v3-api' ], function($, openbis) {
	/*
	 * These tests should be run against openBIS instance with screening sprint
	 * server database version
	 */

	var testProtocol = window.location.protocol;
	var testHost = window.location.hostname;
	var testPort = window.location.port;
	var testUrl = testProtocol + "//" + testHost + ":" + testPort;
	var testApiUrl = testUrl + "/openbis/openbis/rmi-application-server-v3.json";

	var testUserId = "openbis_test_js";
	var testUserPassword = "password";

	var createFacade = function(action) {
		stop();

		var facade = new openbis(testApiUrl);

		action(facade);
	}

	var createFacadeAndLogin = function() {
		var dfd = $.Deferred();

		createFacade(function(facade) {
			facade.login(testUserId, testUserPassword).done(function() {
				dfd.resolve(facade);
				start();
			}).fail(function() {
				dfd.reject(arguments);
				start();
			});
		});

		return dfd.promise();
	}

	var createExperimentPermId = function(permId) {
		var dfd = $.Deferred();

		require([ 'dto/id/experiment/ExperimentPermId' ], function(ExperimentPermId) {
			var id = new ExperimentPermId(permId);
			dfd.resolve(id);
		});

		return dfd.promise();
	}

	var createSamplePermId = function(permId) {
		var dfd = $.Deferred();

		require([ 'dto/id/sample/SamplePermId' ], function(SamplePermId) {
			var id = new SamplePermId(permId);
			dfd.resolve(id);
		});

		return dfd.promise();
	}

	var createExperimentSearchCriterion = function() {
		var dfd = $.Deferred();

		require([ 'dto/search/ExperimentSearchCriterion' ], function(ExperimentSearchCriterion) {
			var criterion = new ExperimentSearchCriterion();
			dfd.resolve(criterion);
		});

		return dfd.promise();
	}

	var createExperimentFetchOptions = function() {
		var dfd = $.Deferred();

		require([ 'dto/fetchoptions/experiment/ExperimentFetchOptions' ], function(efo) {
			var fo = new efo;
			fo.withType();
			fo.withProject().withSpace();
			fo.withProperties();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			fo.withAttachments();

			dfd.resolve(fo);
		});

		return dfd.promise();
	}
	
	var createSampleSearchCriterion = function() {
		var dfd = $.Deferred();

		require([ 'dto/search/SampleSearchCriterion' ], function(SampleSearchCriterion) {
			var criterion = new SampleSearchCriterion();
			dfd.resolve(criterion);
		});

		return dfd.promise();
	}	

	var createSampleFetchOptions = function() {
		var dfd = $.Deferred();

		require([ 'dto/fetchoptions/sample/SampleFetchOptions' ], function(sfo) {
			var fo = new sfo;
			fo.withType();
			fo.withExperiment().withProject().withSpace();
			fo.withSpace();
			fo.withProperties();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			fo.withAttachments();
			fo.withChildrenUsing(fo);
			dfd.resolve(fo);
		});
		return dfd.promise();
	}

	return function() {
		asyncTest("mapExperiments()", function() {
			$.when(createFacadeAndLogin(), createExperimentPermId("20130412105232616-2"), createExperimentFetchOptions()).then(function(facade, permId, fetchOptions) {
				return facade.mapExperiments([ permId ], fetchOptions).done(function() {
					facade.logout()
				});
			}).done(function(experiments) {
				assertObjectsCount(Object.keys(experiments), 1);

				var experiment = experiments["20130412105232616-2"];
				equal(experiment.getCode(), "EXP-1", "Experiment code");
				equal(experiment.getType().getCode(), "HCS_PLATONIC", "Type code");
				equal(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
				equal(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});

		asyncTest("mapSamples()", function() {
			$.when(createFacadeAndLogin(), createSamplePermId("20130415095748527-404"), createSampleFetchOptions()).then(function(facade, permId, fetchOptions) {
				return facade.mapSamples([ permId ], fetchOptions).done(function() {
					facade.logout()
				})
			}).done(function(samples) {
				assertObjectsCount(Object.keys(samples), 1);
				var sample = samples["20130415095748527-404"];
				equal(sample.code, "TEST-SAMPLE-2-PARENT", "Sample code");
				equal(sample.type.code, "UNKNOWN", "Type code");
				equal(sample.experiment.code, "TEST-EXPERIMENT-2", "Experiment code");
				equal(sample.experiment.project.code, "TEST-PROJECT", "Project code");
				equal(sample.space.code, "TEST", "Space code");
				notEqual(sample.children, null, "Children expected");
				if (sample.children !== null) {
					console.log("Children %s", sample.children);
					var child = sample.children[0];
					equal(sample.children.length, 1, "Number of children");
					equal(child.code, "TEST-SAMPLE-2", "Child sample code");
//					equal(child.type.code, "UNKNOWN", "Child type code");
//					equal(child.experiment.code, "TEST-EXPERIMENT-2", "Child experiment code");
					notEqual(child.children, null, "Grand children expected");
					if (child.children !== null) {
						equal(child.children.length, 2, "Number of grand children");
					}
				}
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});

		asyncTest("searchExperiments()", function() {
			$.when(createFacadeAndLogin(), createExperimentSearchCriterion(), createExperimentFetchOptions()).then(function(facade, criterion, fetchOptions) {

				criterion.withCode().thatEquals("TEST-EXPERIMENT-2");

				return facade.searchExperiments(criterion, fetchOptions).done(function() {
					facade.logout();
				})
			}).done(function(experiments) {
				assertObjectsCount(experiments, 1);

				var experiment = experiments[0];
				equal(experiment.getCode(), "TEST-EXPERIMENT-2", "Experiment code");
				equal(experiment.getType().getCode(), "UNKNOWN", "Type code");
				equal(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
				equal(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});

		asyncTest("searchSamples()", function() {
			$.when(createFacadeAndLogin(), createSampleSearchCriterion(), createSampleFetchOptions()).then(function(facade, criterion, fetchOptions) {
				
				criterion.withCode().thatEquals("PLATE-1");

				return facade.searchSamples(criterion, fetchOptions).done(function() {
					facade.logout();
				})
			}).done(function(samples) {
				assertObjectsCount(samples, 1);

				var sample = samples[0];
				equal(sample.getCode(), "PLATE-1", "Sample code");
				equal(sample.getType().getCode(), "PLATE", "Type code");
				equal(sample.getExperiment().getCode(), "EXP-1", "Experiment code");
				equal(sample.getExperiment().getProject().getCode(), "SCREENING-EXAMPLES", "Project  code");
				equal(sample.getSpace().getCode(), "PLATONIC", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});
		/*
		asyncTest("createExperiments()", function() {
			var code = "CREATE_JSON_EXPERIMENT_" + (new Date().getTime());
			// var creations = [ {
			// "@type" : "ExperimentCreation",
			//
			// "typeId" : {
			// "@type" : "EntityTypePermId",
			// "permId" : "UNKNOWN"
			// },
			//
			// "code" : code,
			//
			// "projectId" : {
			// "@type" : "ProjectIdentifier",
			// "identifier" : "/TEST/TEST-PROJECT"
			// },
			//
			// "tagIds" : [ {
			// "@type" : "TagCode",
			// "code" : "CREATE_JSON_TAG"
			// } ]
			// } ];
			require([ 'dto/entity/experiment/ExperimentCreation', 'dto/id/entitytype/EntityTypePermId', 'dto/id/project/ProjectIdentifier', 'dto/id/tag/TagCode' ], function(ExperimentCreation,
					EntityTypePermId, ProjectIdentifier, TagCode) {

				var experimentCreation = new ExperimentCreation();
				experimentCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
				experimentCreation.setCode(code);
				experimentCreation.setProjectId(new ProjectIdentifier("/TEST/TEST-PROJECT"));
				experimentCreation.setTagIds([ new TagCode("CREATE_JSON_TAG") ]);

				$.when(createFacadeAndLogin(), createExperimentFetchOptions()).then(function(facade, fetchOptions) {
					return facade.createExperiments([ experimentCreation ]).then(function(permIds) {
						return facade.mapExperiments(permIds, fetchOptions).done(function() {
							facade.logout();
						})
					})
				}).done(function(experiments) {
					var keys = Object.keys(experiments);
					assertObjectsCount(keys, 1);

					var experiment = experiments[keys[0]];
					equal(experiment.getCode(), code, "Experiment code");
					equal(experiment.getType().getCode(), "UNKNOWN", "Type code");
					equal(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
					equal(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
					equal(experiment.getTags()[0].code, "CREATE_JSON_TAG", "Tag code");
					start();
				}).fail(function(error) {
					ok(false, error);
					start();
				});
			})
		});

		asyncTest("createSamples()", function() {
			var code = "CREATE_JSON_SAMPLE_" + (new Date().getTime());

			$.when(createFacadeAndLogin(), createSampleFetchOptions()).then(function(facade, fetchOptions) {
				var creations = [ {
					"@type" : "SampleCreation",

					"typeId" : {
						"@type" : "EntityTypePermId",
						"permId" : "UNKNOWN"
					},

					"code" : code,

					"spaceId" : {
						"@type" : "SpacePermId",
						"permId" : "TEST"
					},

					"tagIds" : [ {
						"@type" : "TagCode",
						"code" : "CREATE_JSON_TAG"
					} ]
				} ];

				return facade.createSamples(creations).then(function(permIds) {
					return facade.mapSamples(permIds, fetchOptions).done(function() {
						facade.logout();
					})
				})
			}).done(function(samples) {
				var keys = Object.keys(samples);
				assertObjectsCount(keys, 1);

				var sample = samples[keys[0]];
				equal(sample.code, code, "Sample code");
				equal(sample.type.code, "UNKNOWN", "Type code");
				equal(sample.space.code, "TEST", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});

		asyncTest("updateExperiments()", function() {
			var code = "UPDATE_JSON_EXPERIMENT_" + (new Date().getTime());

			var ids = createFacadeAndLogin().then(function(facade) {
				var creations = [ {
					"@type" : "ExperimentCreation",

					"typeId" : {
						"@type" : "EntityTypePermId",
						"permId" : "UNKNOWN"
					},

					"code" : code,

					"projectId" : {
						"@type" : "ProjectIdentifier",
						"identifier" : "/TEST/TEST-PROJECT"
					}
				} ];

				var ids = facade.createExperiments(creations).then(function(permIds) {
					var updates = [ {
						"@type" : "ExperimentUpdate",

						"experimentId" : permIds[0],

						"projectId" : {
							"@type" : "ProjectIdentifier",
							"identifier" : "/PLATONIC/SCREENING-EXAMPLES"
						}
					} ];

					return facade.updateExperiments(updates).then(function() {
						return permIds;
					});
				});

				return $.when(ids, createExperimentFetchOptions()).then(function(permIds, fetchOptions) {
					return facade.mapExperiments(permIds, fetchOptions).done(function() {
						facade.logout();
					})
				})
			}).done(function(experiments) {
				var keys = Object.keys(experiments);
				assertObjectsCount(keys, 1);

				var experiment = experiments[keys[0]];
				equal(experiment.getCode(), code, "Experiment code");
				equal(experiment.getType().getCode(), "UNKNOWN", "Type code");
				equal(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
				equal(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});

		asyncTest("updateSamples()", function() {
			var code = "UPDATE_JSON_SAMPLE_" + (new Date().getTime());

			createFacadeAndLogin().then(function(facade) {
				var creations = [ {
					"@type" : "SampleCreation",

					"typeId" : {
						"@type" : "EntityTypePermId",
						"permId" : "UNKNOWN"
					},

					"code" : code,

					"spaceId" : {
						"@type" : "SpacePermId",
						"permId" : "PLATONIC"
					}
				} ];

				var ids = facade.createSamples(creations).then(function(permIds) {
					var updates = [ {
						"@type" : "SampleUpdate",

						"sampleId" : permIds[0],

						"spaceId" : {
							"@type" : "SpacePermId",
							"permId" : "TEST"
						}
					} ];

					return facade.updateSamples(updates).then(function() {
						return permIds;
					});
				});

				return $.when(ids, createSampleFetchOptions()).then(function(permIds, fetchOptions) {
					return facade.mapSamples(permIds, fetchOptions).done(function() {
						facade.logout();
					})
				})
			}).done(function(samples) {
				var keys = Object.keys(samples);
				assertObjectsCount(keys, 1);

				var sample = samples[keys[0]];
				equal(sample.code, code, "Sample code");
				equal(sample.type.code, "UNKNOWN", "Type code");
				equal(sample.space.code, "TEST", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});
		*/
	}
});