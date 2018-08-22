function MoveEntityController(entityType, entityPermId, callbackOnSuccess) {
	var moveEntityModel = new MoveEntityModel(callbackOnSuccess);
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
		switch(entityType) {
			case "EXPERIMENT":
				require([ "as/dto/experiment/update/ExperimentUpdate", "as/dto/experiment/id/ExperimentIdentifier", "as/dto/project/id/ProjectIdentifier" ], 
			        function(ExperimentUpdate, ExperimentIdentifier, ProjectIdentifier) {
			            var experimentUpdate = new ExperimentUpdate();
			            experimentUpdate.setExperimentId(moveEntityModel.entity.getIdentifier());
			 			experimentUpdate.setProjectId(moveEntityModel.selected.getIdentifier());
			            mainController.openbisV3.updateExperiments([ experimentUpdate ]).done(function() {
			                callbackOnSuccess();
			            });
        			});
        
				break;
			case "SAMPLE":
				
				break;
			case "DATASET":
				
				break;
		}
		
	}
}