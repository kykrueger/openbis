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
	
	this.openJupyterNotebookFromTemplate = function(folder, fileName, template, dataSetId, keepHistory) {
		fileName = fileName + ".ipynb";
		var jupyterURL = profile.jupyterIntegrationServerEndpoint + "?token=" + mainController.serverFacade.openbisServer.getSession() + "&folder=" + folder + "&filename=" + fileName;
		var jupyterNotebookURL = profile.jupyterEndpoint + "user/" + mainController.serverFacade.getUserId() + "/notebooks/" + folder + "/";
		
		$.ajax({
            url : jupyterURL + "&test=True",
            type : 'POST',
            crossDomain: true,
            data : "TEST",
            success : function(result) {
            	fileName =  result.fileName;
            	var jupyterNotebookJson = JSON.parse(template);
        		
        		var autogeneratedWithOpenBIS = jupyterNotebookJson.metadata["autogenerated_by_openbis"];
        		
        		var setNotebookCellVariable = function(jupyterNotebookJson, key, value) {
        			for(var cIdx = 0; cIdx < jupyterNotebookJson.cells.length; cIdx++) {
        				if(jupyterNotebookJson.cells[cIdx].metadata["code_cell_id"] === key) {
        					jupyterNotebookJson.cells[cIdx].source[0] = key + "=" + value;
        				}
        			}
        		}
        		
        		if(autogeneratedWithOpenBIS) {
        			if(!keepHistory) {
        				setNotebookCellVariable(jupyterNotebookJson, "resultDatasetHistoryId", "'" + Util.guid() + "'");
        			}
        			setNotebookCellVariable(jupyterNotebookJson, "fileName", "'" + fileName + "'");
        			setNotebookCellVariable(jupyterNotebookJson, "resultDatasetParents", JSON.stringify([dataSetId]));
        		}
        		
            	$.ajax({
                    url : jupyterURL + "&test=False",
                    type : 'POST',
                    crossDomain: true,
                    data : JSON.stringify(jupyterNotebookJson),
                    success : function(result) {
                    	var win = window.open(jupyterNotebookURL + result.fileName, '_blank');
        				win.focus(); 
                    },
                    error : function(result) {
                    	alert("error: " + JSON.stringify(result));
                    }
        		});
            },
            error : function(result) {
            	alert("error: " + JSON.stringify(result));
            }
		});
	}
	
	this.createJupyterNotebookAndOpen = function(folder, fileName, dataSets, ownerEntity) {
		var _this = this;
		fileName = fileName + ".ipynb";
		var jupyterURL = profile.jupyterIntegrationServerEndpoint + "?token=" + mainController.serverFacade.openbisServer.getSession() + "&folder=" + folder + "&filename=" + fileName;
		
		$.ajax({
            url : jupyterURL + "&test=True",
            type : 'POST',
            crossDomain: true,
            data : "TEST",
            success : function(result) {
            	var fileName = result.fileName
            	var newJupyterNotebook = _this.createJupyterNotebookContent(dataSets, ownerEntity, fileName);
        		var jupyterNotebookURL = profile.jupyterEndpoint + "user/" + mainController.serverFacade.getUserId() + "/notebooks/" + folder + "/";
        		
        		$.ajax({
                    url : jupyterURL + "&test=False",
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
            },
            error : function(result) {
            	alert("error: " + JSON.stringify(result));
            }
		});
	}
	
	this.getMarkdownCell = function(text) {
		return { "cell_type": "markdown", "metadata": {}, "source": [ text ] };
	}
	
	this.getCodeCell = function(source, code_cell_id) {
		if(!code_cell_id) {
			code_cell_id = null;
		}
		return { "cell_type": "code", "execution_count": null, "metadata": { "collapsed": false, "code_cell_id" : code_cell_id }, "outputs": [], "source": source };
	}
	
	this.createJupyterNotebookContent = function(dataSets, ownerEntity, fileName) {
		var content = [];
		var dataSetIds = [];
		for(var dIdx = 0; dIdx < dataSets.length; dIdx++) {
			dataSetIds.push(dataSets[dIdx].permId.permId);
		}
		content.push(this.getMarkdownCell("# Jupyter notebook title, modify me!\n"));
		content.push(this.getMarkdownCell("Variables used by other parts of this autogenerated notebook - Don't modify if you don't know what you are doing!)"));
		content.push(this.getMarkdownCell("Description : fileName variable, indicates the name of the document to save"));
		content.push(this.getCodeCell(["fileName='" + fileName + "'"], "fileName"));
		content.push(this.getMarkdownCell("Description : resultDatasetParents variable, indicates the permIds of the parents of the result dataset"));
		content.push(this.getCodeCell(["resultDatasetParents=" + JSON.stringify(dataSetIds)], "resultDatasetParents"));
		content.push(this.getMarkdownCell("Description : history identifier, different versions of the same notebook should share the same identifier to keep the history"));
		content.push(this.getCodeCell(["resultDatasetHistoryId='" + Util.guid() + "'"], "resultDatasetHistoryId"));
		content.push(this.getMarkdownCell("Description : resultDatasetName variable, indicates the name of the result dataset, **to be set by the user**"));
		content.push(this.getCodeCell(["resultDatasetName='Name your dataset!'"], "resultDatasetName"));
		content.push(this.getMarkdownCell("Description : resultDatasetNotes variable, indicate some notes of the result dataset, **to be set by the user**"));
		content.push(this.getCodeCell(["resultDatasetNotes='Write some notes or leave empty this property!'"], "resultDatasetNotes"));
		content.push(this.getMarkdownCell("## Connect to openBIS"));
		content.push(this.getCodeCell([ "from pybis import Openbis\n", "o = Openbis()" ]));
		content.push(this.getMarkdownCell("## Datasets Information"));
		
		for(var cIdx = 0; cIdx < dataSetIds.length; cIdx++) {
			content.push(this.getMarkdownCell("Dataset " + dataSetIds[cIdx] + " Owner:"));
			if(dataSets[cIdx].sample) {
				content.push(this.getCodeCell(["s" + cIdx + " = o.get_object('" + dataSets[cIdx].sample.permId.permId + "')\n", "s" + cIdx ]));
			} else if(dataSets[cIdx].experiment) {
				content.push(this.getCodeCell(["s" + cIdx + " = o.get_experiment('" + dataSets[cIdx].sample.permId.permId + "')\n", "s" + cIdx + ".attrs" ]));
			}
			content.push(this.getMarkdownCell("Dataset " + dataSetIds[cIdx] + ":"));
			content.push(this.getCodeCell(["ds" + cIdx + " = o.get_dataset('" + dataSetIds[cIdx] + "')\n", "ds" + cIdx + ".attrs" ]));
			content.push(this.getCodeCell(["ds" + cIdx + ".get_files(start_folder=\"/\")"]));
		}
		
		content.push(this.getMarkdownCell("## Datasets Download"));
		
		for(var cIdx = 0; cIdx < dataSetIds.length; cIdx++) {
			//"ds" + cIdx + ".data[\"dataStore\"][\"downloadUrl\"]='http://10.0.2.2:8889'\n"
			content.push(this.getCodeCell(["ds" + cIdx + ".download(files=ds" + cIdx + ".file_list, destination='./', wait_until_finished=True)"]));
		}
		
		content.push(this.getMarkdownCell("## Process your data here"));
		content.push(this.getCodeCell([]));
		
		content.push(this.getMarkdownCell("## Saving the results"));
		content.push(this.getMarkdownCell("The next cell stores a copy of this notebook as an html file so it can be open easily in browsers without the need of Jupyter, **save before executing this to get the lattest version as html**"));
		content.push(this.getCodeCell([
					        "from nbconvert import HTMLExporter\n",
					        "import codecs\n",
					        "import nbformat\n",
					        "exporter = HTMLExporter()\n",
					        "output_notebook = nbformat.read(fileName, as_version=4)\n",
					        "output, resources = exporter.from_notebook_node(output_notebook)\n",
					        "codecs.open(fileName + '.html', 'w', encoding='utf-8').write(output)\n"
		]));
		
		
		content.push(this.getMarkdownCell("Sets the owner of the result dataset"));
		var owner = "";
		var ownerSettings = "";
		switch(ownerEntity["@type"]) {
				case "as.dto.experiment.Experiment":
					owner = "owner= o.get_experiment('"+ ownerEntity.identifier.identifier +"'),\n";
					ownerSettings = "experiment= o.get_experiment('"+ ownerEntity.identifier.identifier +"'),\n";
					break;
				case "as.dto.sample.Sample":
					owner = "owner= o.get_object('"+ ownerEntity.identifier.identifier +"'),\n";
					ownerSettings = "sample= o.get_object('"+ ownerEntity.identifier.identifier +"'),\n";
					break;
		}
		content.push(this.getCodeCell([ owner, "owner" ]));
		
		content.push(this.getMarkdownCell("Creates the result dataset"));
		content.push(this.getCodeCell([
		                     "ds_new = o.new_dataset(\n",
		                     "type='ANALYZED_DATA',\n",
		                     ownerSettings,
		                     "parents=resultDatasetParents,\n",
		                     "files = [fileName, fileName + '.html'],\n",
		                     "props={'name': resultDatasetName, 'notes': resultDatasetNotes, 'history_id' : resultDatasetHistoryId })\n",
		                     "ds_new.save()"
		]));
		
		return {
			  "cells": content,
					  "metadata": {
					  	"autogenerated_by_openbis" : true,
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