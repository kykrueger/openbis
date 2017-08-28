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

var JupyterUtil = new function() {
	
	this.copyNotebook = function(datasetCode, notebookURL) {
		var jupyterNotebook = new JupyterCopyNotebookController(datasetCode, notebookURL);
		jupyterNotebook.init();
	}
	
	this.openJupyterNotebookFromTemplate = function(folder, fileName, template) {
		fileName = fileName + ".ipynb";
		var jupyterURL = profile.jupyterIntegrationServerEndpoint + "?token=" + mainController.serverFacade.openbisServer.getSession() + "&folder=" + folder + "&filename=" + fileName;
		var jupyterNotebookURL = profile.jupyterEndpoint + "user/" + mainController.serverFacade.getUserId() + "/notebooks/" + folder + "/";
		
		$.ajax({
            url : jupyterURL,
            type : 'POST',
            crossDomain: true,
            data : template,
            success : function(result) {
            	var win = window.open(jupyterNotebookURL + result.fileName, '_blank');
				win.focus(); 
            },
            error : function(result) {
            	alert("error: " + JSON.stringify(result));
            }
		});
	}
	
	this.createJupyterNotebookAndOpen = function(folder, fileName, dataSetIds) {
		fileName = fileName + ".ipynb";
		var jupyterURL = profile.jupyterIntegrationServerEndpoint + "?token=" + mainController.serverFacade.openbisServer.getSession() + "&folder=" + folder + "&filename=" + fileName;
		var newJupyterNotebook = this.createJupyterNotebookContent(dataSetIds);
		var jupyterNotebookURL = profile.jupyterEndpoint + "user/" + mainController.serverFacade.getUserId() + "/notebooks/" + folder + "/";
		
		$.ajax({
            url : jupyterURL,
            type : 'POST',
            crossDomain: true,
            data : JSON.stringify(newJupyterNotebook),
            success : function(result) {
            	var win = window.open(jupyterNotebookURL + result.fileName, '_blank');
				win.focus(); 
            },
            error : function(result) {
            	alert("error: " + JSON.stringify(result));
            }
		});
	}
	
	this.createJupyterNotebookContent = function(dataSetIds) {
		var content = [];
		var initializeOpenbisConnection = {
			      "cell_type": "code",
			      "execution_count": null,
			      "metadata": {
			        "collapsed": false
			      },
			      "outputs": [],
			      "source": [
			        "#Initialize Openbis API\n",
			        "from pybis import Openbis\n",
			        "o = Openbis(url='" + profile.jupyterOpenbisEndpoint + "', verify_certificates=False)"
			      ]
		};
		content.push(initializeOpenbisConnection);
		
		for(var cIdx = 0; cIdx < dataSetIds.length; cIdx++) {
			var loadDataset = {
				      "cell_type": "code",
				      "execution_count": null,
				      "metadata": {
				        "collapsed": true
				      },
				      "outputs": [],
				      "source": [
				        "ds = o.get_dataset('" + dataSetIds[cIdx]+ "')"
				      ]
			};
			content.push(loadDataset);
		}
		
		return {
			  "cells": content,
					  "metadata": {
					    "kernelspec": {
					      "display_name": "Python 3",
					      "language": "python",
					      "name": "python3"
					    },
					    "language_info": {
					      "codemirror_mode": {
					        "name": "ipython",
					        "version": 3
					      },
					      "file_extension": ".py",
					      "mimetype": "text/x-python",
					      "name": "python",
					      "nbconvert_exporter": "python",
					      "pygments_lexer": "ipython3",
					      "version": "3.5.2"
					    }
					  },
					  "nbformat": 4,
					  "nbformat_minor": 2
		};
	}

}