/*
 * Copyright 2015 ETH Zuerich, Scientific IT Services
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
function HierarchyFilterModel(sample) {
	this._sample = sample;
	
	this.getTypes = function() {
		var types = {};
		var visited = {};
		var getTypesWithQueueRecursion = function(entity, types, visited) {
			var permId = FormUtil.getPermId(entity);
			var type = FormUtil.getType(entity);
			
			if(!visited[permId]) {
				visited[permId] = true;
			} else {
				return;
			}
			if (!types[type]) {
				types[type] = true;
			}
			if(entity.parents) {
				for (var i = 0; i < entity.parents.length; i++) {
					getTypesWithQueueRecursion(entity.parents[i], types, visited);
				}
			}
			if (entity.children) {
				for (var i = 0; i < entity.children.length; i++) {
					getTypesWithQueueRecursion(entity.children[i], types, visited);
				}
			}
		}
		getTypesWithQueueRecursion(this._sample, types, visited);
		return types;
	}
	
	this.getMaxChildrenDepth = function() {
		var getMaxChildrenDepthWithQueueRecurion = function(sample, max) {
			if (sample.children) {
				var posibleNextMax = [];
				for (var i = 0; i < sample.children.length; i++) {
					var nextMax = getMaxChildrenDepthWithQueueRecurion(sample.children[i], (max + 1));
					posibleNextMax.push(nextMax);
				}
				for (var i = 0; i < posibleNextMax.length; i++) {
					if (posibleNextMax[i] > max) {
						max = posibleNextMax[i];
					}
				}
			}
			return max;
		}
		return getMaxChildrenDepthWithQueueRecurion(this._sample, 0);
	}
	
	this.getMaxParentsDepth = function(sample) {
		var getMaxParentsDepthWithQueueRecurion = function(sample, max) {
			if (sample.parents) {
				var posibleNextMax = [];
				for (var i = 0; i < sample.parents.length; i++) {
					var nextMax = getMaxParentsDepthWithQueueRecurion(sample.parents[i], (max + 1));
					posibleNextMax.push(nextMax);
				}
				for (var i = 0; i < posibleNextMax.length; i++) {
					if (posibleNextMax[i] > max) {
						max = posibleNextMax[i];
					}
				}
			}
			return max;
		}
		return getMaxParentsDepthWithQueueRecurion(this._sample, 0);
	}
	

}
