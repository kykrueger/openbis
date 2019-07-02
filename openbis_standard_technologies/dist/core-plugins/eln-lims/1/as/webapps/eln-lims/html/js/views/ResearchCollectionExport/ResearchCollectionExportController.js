/*
 * Copyright 2011 ETH Zuerich, CISD
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

function ResearchCollectionExportController(parentController) {
    var parentController = parentController;
    var researchCollectionExportModel = new ResearchCollectionExportModel();
    var researchCollectionExportView = new ResearchCollectionExportView(this, researchCollectionExportModel);

    this.init = function(views) {
        researchCollectionExportView.repaint(views);
    };

    this.exportSelected = function() {
        var selectedNodes = $(researchCollectionExportModel.tree).fancytree('getTree').getSelectedNodes();

        var toExport = [];
        for (var eIdx = 0; eIdx < selectedNodes.length; eIdx++) {
            var node = selectedNodes[eIdx];
            toExport.push({ type: node.data.entityType, permId : node.key, expand : !node.expanded });
        }

        if (toExport.length === 0) {
            Util.showInfo("First select something to export.");
        }

        Util.blockUI();
        mainController.serverFacade.exportRc(toExport, true, false, function(error, result) {
            if (error) {
                Util.showError(error);
            } else {
                Util.showSuccess("Export is being processed. If you logout the process will stop.", function() { Util.unblockUI(); });
                mainController.refreshView();
            }
        });
    }
}