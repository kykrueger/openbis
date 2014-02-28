/*
 * Copyright 2013 ETH Zuerich, CISD
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

/**
 * Creates an instance of MainMenu.
 *
 * @constructor
 * @this {MainMenu}
 * @param {string} containerId The Container where the Inspector DOM will be atached.
 * @param {List<GroupOfMenuItems>} menuStructure The menu structure.
 */
function MainMenu(mainController, containerId, menuStructure, mainMenuContentExtra) {
	this.mainController = mainController;
	this.containerId = containerId;
	this.menuStructure = menuStructure;
	this.mainMenuContentExtra = mainMenuContentExtra;

	this.init = function() {
		this.repaint();
	}
	
	this.repaint = function() {
		var localReference = this;
		
		$("#"+this.containerId).empty();
		
		var $mainMenuContainerWrapper = $("<div>", { style: "float:left; margin-right:10px;" });
		var $mainMenuContainer = $("<ul>", { class: "nav nav-tabs nav-stacked span5" });
		$mainMenuContainerWrapper.append($mainMenuContainer);
		
		var $mainMenuContainerWrapperSub = $("<div>", { style: "float:left" });
		
		for (var k = 0; k < this.menuStructure.length; k++) {
			var groupOfMenuItems = this.menuStructure[k];
			
			var onHoverEvent = function(groupOfMenuItems) {
				return function() {
					var $mainMenuContainerSub = $("<ul>", { class: "nav nav-tabs nav-stacked span5" });
					for(var i = 0; i < groupOfMenuItems.menuItems.length; i++) {
						var menuItem = groupOfMenuItems.menuItems[i];
						
						//this is necessary to avoid using the same menuItem reference in all clicks
						var onClick = function(menuItem) {
							return function() {
								localReference.mainController.changeView(menuItem.href, menuItem.hrefArgs);
							}
						}
						
						var $subMenuOption = $("<li>", {click: onClick(menuItem)})
						.append($("<a>").append(menuItem.displayName));
						
						$mainMenuContainerSub.append($subMenuOption);
					}
					$mainMenuContainerWrapperSub.empty();
					$mainMenuContainerWrapperSub.append($mainMenuContainerSub);
				}
			}
			
			var $mainMenuIconContainer = $("<li>", { mouseenter: onHoverEvent(groupOfMenuItems)})
				.append($("<a>").append(groupOfMenuItems.displayName + "<i class='icon-chevron-right'></i>"));
			
			$mainMenuContainer.append($mainMenuIconContainer);
		}
			
		$("#"+this.containerId).append($mainMenuContainerWrapper);
		$("#"+this.containerId).append($mainMenuContainerWrapperSub);
		var $mainMenuExtra = $("<div>", { id: "mainMenuExtra", style: "clear:both;" });
		$mainMenuExtra.append(this.mainMenuContentExtra);
		$("#"+this.containerId).append($mainMenuExtra);
	}
}

function GroupOfMenuItems(key, displayName, menuItems) {
	this.key = key;
	this.displayName = displayName;
	this.menuItems = menuItems;
}

function MenuItem(image, href, hrefArgs, displayName) {
	this.image = image;
	this.href = href;
	this.hrefArgs = hrefArgs;
	this.displayName = displayName;
}