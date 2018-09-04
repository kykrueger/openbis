/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function SpaceFormController(mainController, space) {
	this._mainController = mainController;
	this._spaceFormModel = new SpaceFormModel(space);
	this._spaceFormView = new SpaceFormView(this, this._spaceFormModel);

	this.init = function(views) {
		var _this = this;
		mainController.serverFacade.searchRoleAssignments({
			user: mainController.serverFacade.getUserId(),
			space: _this._spaceFormModel.space.code
		}, function(roleAssignments) {
			_this._spaceFormModel.roles = [];
			for (var i=0; i<roleAssignments.length; i++) {
				if (roleAssignments[i].roleLevel == "SPACE") {
					_this._spaceFormModel.roles.push(roleAssignments[i].role);
				}
			}
			_this._spaceFormView.repaint(views);
		});
	}
	
	this.createProject = function() {
		this._mainController.changeView('showCreateProjectPage', this._spaceFormModel.space.code);
	}

	this.authorizeUserOrGroup = function(role, shareWith, groupOrUser) {
		var _this = this;
		Util.blockUI();
		this._mainController.serverFacade.createRoleAssignment({
			user: shareWith == "User" ? groupOrUser : null,
			group: shareWith == "Group" ? groupOrUser : null,
			role: role,
			space: _this._spaceFormModel.space.code
		}, function(result) {
			Util.unblockUI();
			if (result) {
				Util.showSuccess("Access granted.");
			}
		});
	}

}