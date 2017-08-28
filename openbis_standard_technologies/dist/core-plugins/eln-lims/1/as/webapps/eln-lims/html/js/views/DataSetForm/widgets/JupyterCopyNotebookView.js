/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function JupyterCopyNotebookView(jupyterNotebookController, jupyterNotebookModel) {
	this._jupyterNotebookController = jupyterNotebookController;
	this._jupyterNotebookModel = jupyterNotebookModel;
	
	this.repaint = function() {
		var _this = this;
		var $window = $('<form>', { 'action' : 'javascript:void(0);' });
		
		$window.append($('<legend>').append("Use Jupyter Notebook as Template"));
		$window.append(FormUtil.getFieldForLabelWithText("Notebook", this._jupyterNotebookModel.existingNotebookURL));
		var $workspace = FormUtil._getInputField('text', null, 'workspace Name', null, true);
		var $notebookName = FormUtil._getInputField('text', null, 'notebook Name', null, true);
		$window.append(FormUtil.getFieldForComponentWithLabel($workspace, "Workspace"));
		$window.append(FormUtil.getFieldForComponentWithLabel($notebookName, "Notebook Name"));
		
		var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' });
		$window.submit(function() {
			$.get(_this._jupyterNotebookModel.existingNotebookURL, function( data ) {
				_this._jupyterNotebookController.create($workspace.val(), $notebookName.val(), data);
			});
		});
		var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Cancel');
		$btnCancel.click(function() {
			Util.unblockUI();
		});
		
		$window.append($btnAccept).append('&nbsp;').append($btnCancel);
		
		var css = {
				'text-align' : 'left',
				'top' : '15%',
				'width' : '70%',
				'left' : '15%',
				'right' : '20%',
				'overflow' : 'hidden'
		};
		
		Util.blockUI($window, css);
	}
}