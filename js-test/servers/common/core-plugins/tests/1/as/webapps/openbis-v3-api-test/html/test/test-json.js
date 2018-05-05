define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common', 'util/Json' ], function($, _, openbis, openbisExecuteOperations, common, Json) {
	var executeModule = function(moduleName, openbis) {
		QUnit.module(moduleName);

		QUnit.test("fromJSON with object id before object definition", function(assert) {
			var c = new common(assert, openbis);
			c.start();

			var json = {
				"23" : {
					"@type" : "as.dto.query.Query",
					"@id" : 1,
					"fetchOptions" : {
						"@type" : "as.dto.query.fetchoptions.QueryFetchOptions",
						"@id" : 2,
						"count" : null,
						"from" : null,
						"cacheMode" : "NO_CACHE",
						"registrator" : {
							"@type" : "as.dto.person.fetchoptions.PersonFetchOptions",
							"@id" : 3,
							"count" : null,
							"from" : null,
							"cacheMode" : "NO_CACHE",
							"space" : null,
							"registrator" : null,
							"roleAssignments" : null,
							"webAppSettings" : null,
							"allWebAppSettings" : false,
							"sort" : null
						},
						"sort" : null,
						"sortBy" : null
					},
					"permId" : {
						"@type" : "as.dto.query.id.QueryTechId",
						"@id" : 4,
						"techId" : 23,
						"id" : "23"
					},
					"name" : "V3_QUERY_2018_5_4_21_26_476162",
					"description" : null,
					"databaseId" : {
						"@type" : "as.dto.query.id.QueryDatabaseName",
						"@id" : 5,
						"name" : "openbisDB"
					},
					"queryType" : "GENERIC",
					"entityTypeCodePattern" : null,
					"sql" : "select code from spaces",
					"publicFlag" : false,
					"registrationDate" : 1525462005857,
					"registrator" : {
						"@type" : "as.dto.person.Person",
						"@id" : 6,
						"fetchOptions" : {
							"@type" : "as.dto.person.fetchoptions.PersonFetchOptions",
							"@id" : 7,
							"count" : null,
							"from" : null,
							"cacheMode" : "NO_CACHE",
							"space" : null,
							"registrator" : null,
							"roleAssignments" : null,
							"webAppSettings" : null,
							"allWebAppSettings" : false,
							"sort" : null
						},
						"permId" : {
							"@type" : "as.dto.person.id.PersonPermId",
							"@id" : 8,
							"permId" : "openbis_test_js"
						},
						"userId" : "openbis_test_js",
						"firstName" : "Karel",
						"lastName" : "Mallarmé",
						"email" : "franz-josef.elmer@systemsx.ch",
						"registrationDate" : 1365770153704,
						"active" : true,
						"space" : null,
						"registrator" : null,
						"roleAssignments" : null,
						"webAppSettings" : null
					},
					"modificationDate" : 1525462005857
				},
				"22" : {
					"@type" : "as.dto.query.Query",
					"@id" : 9,
					"fetchOptions" : {
						"@type" : "as.dto.query.fetchoptions.QueryFetchOptions",
						"@id" : 10,
						"count" : null,
						"from" : null,
						"cacheMode" : "NO_CACHE",
						"registrator" : 3,
						"sort" : null,
						"sortBy" : null
					},
					"permId" : {
						"@type" : "as.dto.query.id.QueryTechId",
						"@id" : 11,
						"techId" : 22,
						"id" : "22"
					},
					"name" : "V3_QUERY_2018_5_4_21_26_374293",
					"description" : null,
					"databaseId" : {
						"@type" : "as.dto.query.id.QueryDatabaseName",
						"@id" : 12,
						"name" : "openbisDB"
					},
					"queryType" : "GENERIC",
					"entityTypeCodePattern" : null,
					"sql" : "select code from spaces",
					"publicFlag" : false,
					"registrationDate" : 1525462005857,
					"registrator" : 6,
					"modificationDate" : 1525462005857
				}
			};

			var returnType = {
				name : "Map",
				arguments : [ "IQueryId", "Query" ]
			};

			try {
				Json.fromJson(returnType, json).done(function(result) {
					c.assertEqual(result[22].getRegistrator().getUserId(), "openbis_test_js", "22.Registrator.userId");
					c.assertEqual(result[23].getRegistrator().getUserId(), "openbis_test_js", "23.Registrator.userId");
					c.finish();
				}).fail(function(error) {
					c.fail(error);
					c.finish();
				});
			} catch (error) {
				c.fail(error);
				c.finish();
			}
		});

	}

	return function() {
		executeModule("Json tests", openbis);
		executeModule("Json tests (executeOperations)", openbisExecuteOperations);
	}
});
