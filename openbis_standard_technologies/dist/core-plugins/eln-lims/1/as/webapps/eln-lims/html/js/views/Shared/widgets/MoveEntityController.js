function MoveEntityController(entityType, entityPermId) {
	var moveEntityModel = new MoveEntityModel();
	var moveEntityView = new MoveEntityView(this, moveEntityModel);
	
	this.init = function() {
		var criteria = { entityKind : entityType, logicalOperator : "AND", rules : { "UUIDv4" : { type : "Attribute", name : "PERM_ID", value : entityPermId } } };
		
		var render = function(result) {
			moveEntityModel.entity = result.objects[0];
			moveEntityView.repaint();
		}
		
		switch(entityType) {
			case "EXPERIMENT":
				mainController.serverFacade.searchForExperimentsAdvanced(criteria, null, render);
				break;
			case "SAMPLE":
				mainController.serverFacade.searchForSamplesAdvanced(criteria, null, render);
				break;
			case "DATASET":
				mainController.serverFacade.searchForDataSetsAdvanced(criteria, null, render);
				break;
		}
	}
	
	this.move = function() {
		var done = function() {
			Util.showSuccess("Move successfull", function() { Util.unblockUI(); });
		};
		var fail = function(error) {
			Util.showError("Move failed: " + JSON.stringify(error));
		};
		
		switch(entityType) {
			case "EXPERIMENT":
				require([ "as/dto/experiment/update/ExperimentUpdate"], 
			        function(ExperimentUpdate) {
			            var experimentUpdate = new ExperimentUpdate();
			            experimentUpdate.setExperimentId(moveEntityModel.entity.getIdentifier());
			 			experimentUpdate.setProjectId(moveEntityModel.selected.getIdentifier());
			            mainController.openbisV3.updateExperiments([ experimentUpdate ]).done(done).fail(fail);
        			});
				break;
			case "SAMPLE":
				require([ "as/dto/sample/update/SampleUpdate"], 
			        function(SampleUpdate) {
			            var sampleUpdate = new SampleUpdate();
			            sampleUpdate.setSampleId(moveEntityModel.entity.getIdentifier());
			 			sampleUpdate.setExperimentId(moveEntityModel.selected.getIdentifier());
			            mainController.openbisV3.updateSamples([ sampleUpdate ]).done(done).fail(fail);
        			});
				break;
			case "DATASET":
				require([ "as/dto/dataset/update/DataSetUpdate"], 
			        function(DataSetUpdate) {
			            var datasetUpdate = new DataSetUpdate();
			            datasetUpdate.setDataSetId(moveEntityModel.entity.getPermId());
			            
			            switch(moveEntityModel.selected["@type"]) {
							case "as.dto.experiment.Experiment":
								datasetUpdate.setExperimentId(moveEntityModel.selected.getIdentifier());
							break;
							case "as.dto.sample.Sample":
								datasetUpdate.setSampleId(moveEntityModel.selected.getIdentifier());
							break;
						}
						
			            mainController.openbisV3.updateDataSets([ datasetUpdate ]).done(done).fail(fail);
        			});
				break;
		}
		
	}
}