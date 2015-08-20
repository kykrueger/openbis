define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Create tests");

		var testCreate = function(c, fCreate, fFind, fCheck) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(facade).then(function(permIds) {
					c.assertTrue(permIds != null && permIds.length == 1, "Entity was created");
					return fFind(facade, permIds[0]).then(function(entity) {
						c.assertNotNull(entity, "Entity can be found");
						fCheck(entity);
						c.finish();
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		QUnit.test("createSpaces()", function(assert) {
			var c = new common(assert);
			var code = c.generateId("SPACE");

			var fCreate = function(facade) {
				var creation = new c.SpaceCreation();
				creation.setCode(code);
				creation.setDescription("test description");
				return facade.createSpaces([ creation ]);
			}

			var fCheck = function(space) {
				c.assertEqual(space.getCode(), code, "Code");
				c.assertEqual(space.getDescription(), "test description", "Description");
			}

			testCreate(c, fCreate, c.findSpace, fCheck);
		});

		QUnit.test("createProjects()", function(assert) {
			var c = new common(assert);
			var code = c.generateId("PROJECT");

			var fCreate = function(facade) {
				var projectCreation = new c.ProjectCreation();
				projectCreation.setSpaceId(new c.SpacePermId("TEST"));
				projectCreation.setCode(code);
				projectCreation.setDescription("JS test project");
				attachmentCreation = new c.AttachmentCreation();
				attachmentCreation.setFileName("test_file");
				attachmentCreation.setTitle("test_title");
				attachmentCreation.setDescription("test_description");
				attachmentCreation.setContent(btoa("hello world!"));
				projectCreation.setAttachments([ attachmentCreation ]);
				return facade.createProjects([ projectCreation ]);
			}

			var fCheck = function(project) {
				c.assertEqual(project.getCode(), code, "Project code");
				c.assertEqual(project.getSpace().getCode(), "TEST", "Space code");
				c.assertEqual(project.getDescription(), "JS test project", "Description");
				var attachments = project.getAttachments();
				c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
				c.assertEqual(attachments[0].title, "test_title", "Attachment title");
				c.assertEqual(attachments[0].description, "test_description", "Attachment description");
				c.assertEqual(atob(attachments[0].content), "hello world!", "Attachment content");
				c.assertEqual(attachments.length, 1, "Number of attachments");
			}

			testCreate(c, fCreate, c.findProject, fCheck);
		});

		QUnit.test("createExperiments()", function(assert) {
			var c = new common(assert);
			var code = c.generateId("EXPERIMENT");

			var fCreate = function(facade) {
				var experimentCreation = new c.ExperimentCreation();
				experimentCreation.setTypeId(new c.EntityTypePermId("HT_SEQUENCING"));
				experimentCreation.setCode(code);
				experimentCreation.setProjectId(new c.ProjectIdentifier("/TEST/TEST-PROJECT"));
				experimentCreation.setTagIds([ new c.TagCode("CREATE_JSON_TAG") ]);
				attachmentCreation = new c.AttachmentCreation();
				attachmentCreation.setFileName("test_file");
				attachmentCreation.setTitle("test_title");
				attachmentCreation.setDescription("test_description");
				attachmentCreation.setContent(btoa("hello world!"));
				experimentCreation.setAttachments([ attachmentCreation ]);
				experimentCreation.setProperty("EXPERIMENT_DESIGN", "SEQUENCE_ENRICHMENT");
				return facade.createExperiments([ experimentCreation ]);
			}

			var fCheck = function(experiment) {
				c.assertEqual(experiment.getCode(), code, "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
				c.assertEqual(experiment.getTags()[0].code, "CREATE_JSON_TAG", "Tag code");
				var tags = experiment.getTags();
				c.assertEqual(tags[0].code, 'CREATE_JSON_TAG', "tags");
				c.assertEqual(tags.length, 1, "Number of tags");
				var attachments = experiment.getAttachments();
				c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
				c.assertEqual(attachments[0].title, "test_title", "Attachment title");
				c.assertEqual(attachments[0].description, "test_description", "Attachment description");
				c.assertEqual(atob(attachments[0].content), "hello world!", "Attachment content");
				c.assertEqual(attachments.length, 1, "Number of attachments");
				var properties = experiment.getProperties();
				c.assertEqual(properties["EXPERIMENT_DESIGN"], "SEQUENCE_ENRICHMENT", "Property EXPERIMENT_DESIGN");
				c.assertEqual(Object.keys(properties), "EXPERIMENT_DESIGN", "Properties");
			}

			testCreate(c, fCreate, c.findExperiment, fCheck);
		});

		QUnit.test("createSamples()", function(assert) {
			var c = new common(assert);
			var code = c.generateId("SAMPLE");

			var fCreate = function(facade) {
				var creation = new c.SampleCreation();
				creation.setTypeId(new c.EntityTypePermId("UNKNOWN"));
				creation.setCode(code);
				creation.setSpaceId(new c.SpacePermId("TEST"));
				creation.setTagIds([ new c.TagCode("CREATE_JSON_TAG") ]);
				return facade.createSamples([ creation ]);
			}

			var fCheck = function(sample) {
				c.assertEqual(sample.getCode(), code, "Sample code");
				c.assertEqual(sample.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
				c.assertEqual(sample.getTags()[0].getCode(), "CREATE_JSON_TAG", "Tag code");
			}

			testCreate(c, fCreate, c.findSample, fCheck);
		});

		QUnit.test("createMaterials()", function(assert) {
			var c = new common(assert);
			var code = c.generateId("MATERIAL");

			var fCreate = function(facade) {
				var materialCreation = new c.MaterialCreation();
				materialCreation.setTypeId(new c.EntityTypePermId("COMPOUND"));
				materialCreation.setCode(code);
				materialCreation.setProperty("DESCRIPTION", "Metal");
				return facade.createMaterials([ materialCreation ]);
			}

			var fCheck = function(material) {
				c.assertEqual(material.getCode(), code, "Material code");
				c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
				var properties = material.getProperties();
				c.assertEqual(properties["DESCRIPTION"], "Metal", "Property DESCRIPTION");
			}

			testCreate(c, fCreate, c.findMaterial, fCheck);
		});

	}
});