/**
 * Test searching and executing DSS services.
 */
define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common' ], function($, _, openbis, openbisExecuteOperations, common) {
	var executeModule = function(moduleName, openbis) {
		QUnit.module(moduleName);

		var testAction = function(c, fAction, fCheck) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				c.ok("Login");
				return fAction(facade).then(function(result) {
					c.ok("Got results");
					fCheck(facade, result);
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		var testActionWhichShouldFail = function(c, fAction, errorMessage) {
			c.start();
			
			c.createFacadeAndLogin().then(function(facade) {
				c.ok("Login");
				return fAction(facade).then(function(result) {
					c.fail("Action supposed to fail");
					c.finish();
				});
			}).fail(function(error) {
				c.ok("Action failed as expected");
				c.assertEqual(error.message, errorMessage, "Error message");
				c.finish();
			});
		}
		
		QUnit.test("searchSearchDomainService()", function(assert) {
			var c = new common(assert, openbis);

			var fAction = function(facade) {
				var criteria = new c.SearchDomainServiceSearchCriteria();
				var fetchOptions = new c.SearchDomainServiceFetchOptions();
				return facade.searchSearchDomainServices(criteria, fetchOptions);
			}

			var fCheck = function(facade, result) {
				c.assertEqual(result.getTotalCount(), 2, "Number of results");
				c.assertEqual(result.getObjects().length, 2, "Number of results");
				var objects = result.getObjects();
				objects.sort(function(o1, o2) { return o1.getPermId().toString().localeCompare(o2.getPermId().toString())});
				c.assertEqual(objects[0].getPermId().toString(), "DSS1:echo-database", "Perm id");
				c.assertEqual(objects[0].getName(), "echo-database", "Name");
				c.assertEqual(objects[0].getLabel(), "Echo database", "Label");
				c.assertEqual(objects[0].getPossibleSearchOptionsKey(), "optionsKey", "Possible searcg option keys");
				c.assertEqual(objects[0].getPossibleSearchOptions().toString(), "Alpha [alpha],beta [beta]", "Possible search options");
			}

			testAction(c, fAction, fCheck);
		});
		
		QUnit.test("executeSearchDomainService()", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				var options = new c.SearchDomainServiceExecutionOptions();
				options.withSearchString("key").withParameter("key", 
						JSON.stringify({
							"searchDomain" : "Echo database",
							"dataSetCode" : "20130415093804724-403",
							"pathInDataSet" : "PATH-2",
							"sequenceIdentifier" : "ID-2",
							"positionInSequence" : "2"
						}));
				return facade.executeSearchDomainService(options);
			}
			
			var fCheck = function(facade, result) {
				c.assertEqual(result.getTotalCount(), 2, "Number of results");
				c.assertEqual(result.getObjects().length, 2, "Number of results");
				var objects = result.getObjects();
				objects.sort(function(o1, o2) { return o1.getServicePermId().toString().localeCompare(o2.getServicePermId().toString())});
				c.assertEqual(objects[0].getServicePermId().toString(), "DSS1:echo-database", "Service perm id");
				c.assertEqual(objects[0].getSearchDomainLabel(), "Echo database", "Search domain label");
				c.assertEqual(objects[0].getEntityIdentifier(), "20130415093804724-403", "Entity identifier");
				c.assertEqual(objects[0].getEntityKind(), "DATA_SET", "Entity kind");
				c.assertEqual(objects[0].getEntityType(), "UNKNOWN", "Entity type");
				c.assertEqual(objects[0].getEntityPermId(), "20130415093804724-403", "Entity perm id");
				c.assertEqual(JSON.stringify(objects[0].getResultDetails()), JSON.stringify({"identifier": "ID-2",
					"path_in_data_set": "PATH-2", "position": "2"}), "Result details");
			}
			
			testAction(c, fAction, fCheck);
		});
		
		QUnit.test("searchAggregationService()", function(assert) {
			var c = new common(assert, openbis);

			var fAction = function(facade) {
				var criteria = new c.AggregationServiceSearchCriteria();
				var id = new c.DssServicePermId("js-test", new c.DataStorePermId("DSS1"));
				criteria.withId().thatEquals(id);
				var fetchOptions = new c.AggregationServiceFetchOptions();
				return facade.searchAggregationServices(criteria, fetchOptions);
			}

			var fCheck = function(facade, result) {
				c.assertEqual(result.getTotalCount(), 1, "Number of results");
				c.assertEqual(result.getObjects().length, 1, "Number of results");
				var objects = result.getObjects();
				c.assertEqual(objects[0].getPermId().toString(), "DSS1:js-test", "Perm id");
				c.assertEqual(objects[0].getLabel(), "js-test", "Label");
			}

			testAction(c, fAction, fCheck);
		});
		
		QUnit.test("executeAggregationService()", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				var id = new c.DssServicePermId("js-test", new c.DataStorePermId("DSS1"));
				var options = new c.AggregationServiceExecutionOptions();
				options.withParameter("method", "test");
				options.withParameter("answer", 42).withParameter("pi", 3.1415926);
				return facade.executeAggregationService(id, options);
			}
			
			var fCheck = function(facade, tableModel) {
				c.assertEqual(tableModel.getColumns().toString(), "key,value", "Table columns");
				c.assertEqual(tableModel.getRows().toString(), "method,test,answer,42,pi,3.1415926", "Table rows");
			}
			
			testAction(c, fAction, fCheck);
		});
		
		QUnit.test("executeAggregationService() with data store code is null", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				var id = new c.DssServicePermId("js-test", new c.DataStorePermId(null));
				var options = new c.AggregationServiceExecutionOptions();
				return facade.executeAggregationService(id, options);
			}
			
			testActionWhichShouldFail(c, fAction, "Data store code cannot be empty. (Context: [])");
		});
		
		QUnit.test("executeAggregationService() with data store id is null", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				var id = new c.DssServicePermId("js-test", null);
				var options = new c.AggregationServiceExecutionOptions();
				return facade.executeAggregationService(id, options);
			}
			
			testActionWhichShouldFail(c, fAction, "Data store id cannot be null. (Context: [])");
		});
		
		QUnit.test("executeAggregationService() with key is null", function(assert) {
			var c = new common(assert, openbis);
			
			var fAction = function(facade) {
				var id = new c.DssServicePermId(null, new c.DataStorePermId("DSS1"));
				var options = new c.AggregationServiceExecutionOptions();
				return facade.executeAggregationService(id, options);
			}
			
			testActionWhichShouldFail(c, fAction, "Service key cannot be empty. (Context: [])");
		});
	}

	return function() {
		executeModule("DSS service tests", openbis);
		executeModule("DSS service tests (executeOperations)", openbisExecuteOperations);
	}
})