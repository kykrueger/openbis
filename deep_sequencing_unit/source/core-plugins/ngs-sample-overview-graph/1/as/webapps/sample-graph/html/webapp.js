/**
 * Sample-Relationship Bottom Up Webapp
 *
 * This webapp draws a graph emanating from a flowcell and terminating at the biological sample.
 *
 * Because the structure is rigid, it draws each sample type in its own column.
 */

//
// BEGIN CONFIGURATION PARAMTERS
// 
// The following parameters must be configured for the webapp

var FLOWCELL_SAMPLE_TYPE = "FLOWCELL";
var FLOWLANE_SAMPLE_TYPE = "FLOWLANE";

// The view is organized in columns that correspond to a sample type. The columns are defined here.
// The order of the sample types here matches the bottom-up order of the sample types.
var COLUMNS = [
	{ type : FLOWLANE_SAMPLE_TYPE, label : "Flowlane", width : 200 },
	{ type : FLOWCELL_SAMPLE_TYPE, label : "Flowcell", width : 200 },
	{ type : "MULTIPLEX", label : "Multiplex", width : 300 },
	{ type : "LIBRARY", label : "Library", width : 240 },
	{ type : "ALIQUOT", label : "Aliquot", width : 400 },
	{ type : "SAMPLE", label : "Sample", width : 400 }
];

// The height of each line in the display
var LINE_HEIGHT = 20;

// The first N columns are shown expanded when the webapp is started. The next are initially collapsed.
// N probably should not be less than 2 and certainly should not be less than 1.
var FIRST_COLLAPSED_COLUMN = 2;

// The colors used for the different samples. The colors are used when one sample has multiple parents or children to disambiguate.
//var sampleColors = d3.scale.category10();
var sampleColors = d3.scale.ordinal().range(['#999']);

// The color used when the connection between two samples is unambiguous
var oneToOneColor = "#999"

// END CONFIGURATION PARAMTERS

/// The openbisServer we use for our data
var webappContext = new openbisWebAppContext();

// openbisServer is global -- index.html needs to refer to it.
openbisServer = new openbis('/openbis/openbis', '/datastore_server');
openbisServer.useSession(webappContext.getSessionId());

function parseJson(jsonString) { return eval('(' + jsonString + ')'); }

/**
 * The node stores the information necessary for presenting the sample data in the graph.
 */
function SampleGraphNode(sample) {
	this.identifier = sample.identifier;
	this.properties = sample.properties;
	this.permId = sample.permId;
	this.nodeId = sample["@id"];
	this.sampleType = sample.sampleTypeCode;
	this.children = [];
	this.parents = [];
	this.serverSample = sample;
	this.arrayIndex = -1;
}

/**
 * The link stores the information necessary for drawing connections between nodes
 */
function SampleGraphLink(sourceNode, targetNode) {
	this.sourceNode = sourceNode;
	this.targetNode = targetNode;
}

/**
 * The model that manages state and implements the operations for the sample graph. This model is specific to the Flowcell type.
 */
function FlowcellGraphModel() {
	this.initializeModel();
}

FlowcellGraphModel.prototype.initializeModel = function() {
	this.sampleIdentifier = webappContext.getEntityIdentifier();
	var identifierTokens = this.sampleIdentifier.split("/");
	if (identifierTokens.length > 2) {
		this.sampleSpace = identifierTokens[1];
		this.sampleCode = identifierTokens[2];
	} else {
		this.sampleSpace = null;
		this.sampleCode = identifierTokens[1];
	}
	this.samplePermId = webappContext.getEntityPermId();
	this.sampleType = webappContext.getEntityType();
	var samplesByType = {};
	COLUMNS.forEach(function(column) { samplesByType[column.type] = [] });
	this.samplesByType = samplesByType;
}

/**
 * Request the data necessary to display the graph.
 */
FlowcellGraphModel.prototype.requestGraphData = function(callback)
{
	var containerCriteria = {
		matchClauses : 
			[ {"@type" : "AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "CODE",
				desiredValue : this.sampleCode
			}, {"@type" : "AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "SPACE",
				desiredValue : this.sampleSpace
			} ],
		operator : "MATCH_ALL_CLAUSES"		
	};

	var matchClauses = [ {"@type" : "AttributeMatchClause",
		fieldType : "ATTRIBUTE",
		attribute : "CODE",
		desiredValue : this.sampleCode + "*"
	}];

	if (this.sampleSpace) {
		matchClauses.push({"@type" : "AttributeMatchClause",
			fieldType : "ATTRIBUTE",
			attribute : "SPACE",
			desiredValue : this.sampleSpace
		})
	}


	var sampleCriteria = {
		matchClauses : matchClauses,
		operator : "MATCH_ALL_CLAUSES"
	};

	var lexicalParent = this;
	function coalesceResult(data) {
		lexicalParent.coalesceGraphData(data);
		callback();
	}

	openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS"], coalesceResult);
}

/**
 * Request the data necessary to display the graph.
 */
FlowcellGraphModel.prototype.coalesceGraphData = function(data, callback) {
	var samples = data.result;
	var nodesById = {};

	function isPureId(sample) { return null == sample["@id"]; }

	function nodeForSample(sample) { return isPureId(sample) ? nodesById[sample] : nodesById[sample["@id"]]; }

	var lexicalParent = this;
	function convertSampleToNode(sample) {
		// This is just a nodeId, it will be converted elsewhere
		if (isPureId(sample)) return;
		// This has already been converted
		if (nodesById[sample.nodeId]) return;

		var node = new SampleGraphNode(sample);
		nodesById[node.nodeId] = node;
		sampleTypeArray = lexicalParent.samplesByType[node.sampleType]
		if (sampleTypeArray) {
			node.arrayIndex = sampleTypeArray.length;
			sampleTypeArray.push(node);
		}

		if (sample.parents) sample.parents.forEach(convertSampleToNode);
		if (sample.children) sample.children.forEach(convertSampleToNode);
	}

	function resolveParents(sample) {
		// This is just a nodeId, it will be resolved elsewhere
		if (isPureId(sample)) return;

		sample.parents.forEach(resolveParents);

		var node = nodeForSample(sample);
		node.parents = sample.parents.map(nodeForSample);
	}

	function resolveChildren(sample) {
		// This is just a nodeId, it will be resolved elsewhere
		if (isPureId(sample)) return;

		sample.children.forEach(resolveChildren);

		var node = nodeForSample(sample);
		node.children = sample.children.map(nodeForSample);
	}

	samples.forEach(convertSampleToNode);
	samples.forEach(resolveParents);
	samples.forEach(resolveChildren);

	// The parents of the flowlanes should become the parents of the flow cell
	var flowcell = this.samplesByType[FLOWCELL_SAMPLE_TYPE][0];
	if (flowcell) {
		var flowlaneParents = []
		this.samplesByType[FLOWLANE_SAMPLE_TYPE].forEach(function(d) { flowlaneParents = flowlaneParents.concat(d.parents); });
		this.samplesByType[FLOWCELL_SAMPLE_TYPE][0].parents = flowlaneParents;
		this.samplesByType[FLOWLANE_SAMPLE_TYPE].forEach(function(d) { d.parents = [flowcell]});
	}
}


/**
 * The presenter that shows the model.
 */
function SampleGraphPresenter(model) {
	this.model = model;
	this.didCreateVis = false;
	this.useBottomUpMode();
	this.initializePresenter();
}

function textBBoxForNode(node) { 
	var bbox = presenter.columns.selectAll("text.sample")[node.col][node.visibleIndex].getBBox();
	// Correct for the column
	bbox.x += node.colOffset;
	return bbox;
}

var yLinkOffset = LINE_HEIGHT * 0.33;

/**
 * Create the DOM elements to store the visualization (tree + inspectors)
 */
SampleGraphPresenter.prototype.initializePresenter = function()
{ 
	if (this.didCreateVis) return;
	
	// Create a div to house the tree visualization and the inspectors
	this.root = d3.select("#root");
	this.rootLabel = d3.select("#root-label");
	this.rootLabel.text(this.model.sampleIdentifier);

	// Function used to draw paths between elements
	function source(d) {
		// Find the associated text node in the DOM and use that as a basis for creating the links
		var bbox = textBBoxForNode(d.sourceNode);
		return { x : bbox.x + bbox.width + 7, y  : bbox.y + yLinkOffset };
	}
	function target(d) {
		var bbox = textBBoxForNode(d.targetNode);
		return { x : bbox.x, y  : bbox.y + yLinkOffset }
	}

	this.useLineLinkPath(source, target);
	this.didCreateVis = true;
}

SampleGraphPresenter.prototype.calcuateVisibleColumnOffsets = function() {
	// Calculate the offsets for the columns -- only need to do this once
	var xOffset = 0;

	// Depending on whether or not we are viewing top down or bottom up, the visible columns are those
	// before and including the selected sample type, or those after and including the sample type, respectively.
	var querySampleType = model.sampleType;
	var seenQuerySampleType = false;
	var bottomUpMode = this.bottomUpMode;
	var visibleColumns = [];
	COLUMNS.forEach(function(col, i) {
		var atQuerySampleType = col.type == querySampleType;
		if (atQuerySampleType) {
			visibleColumns.push(col);
		} else {
			var includeCol = seenQuerySampleType && bottomUpMode;
			includeCol = includeCol || !seenQuerySampleType && !bottomUpMode;
			if (includeCol) visibleColumns.push(col)
		}
		seenQuerySampleType = seenQuerySampleType || atQuerySampleType;
	})

	if (!bottomUpMode) visibleColumns = visibleColumns.reverse();
	this.visibleColumns = visibleColumns;
	this.visibleColumns.forEach(function(column) { column.xOffset = xOffset; xOffset += column.width });
	this.vizWidth = xOffset;
};

/**
 * Put the presenter in the bottom-up display mode (compared to top-down)
 */
SampleGraphPresenter.prototype.useBottomUpMode = function() {
	this.bottomUpMode = true;
	this.calcuateVisibleColumnOffsets();
};

/**
 * Put the presenter in the top-down display mode (compared to bottom-up)
 */
SampleGraphPresenter.prototype.useTopDownMode = function() {
	this.bottomUpMode = false;
	this.calcuateVisibleColumnOffsets();
};

/**
 * Draw links using the diagonal function
 */
SampleGraphPresenter.prototype.useDiagonalLinkPath = function(source, target) {
	var diagonal = d3.svg.diagonal();
	diagonal.source(source);
	diagonal.target(target);	
	this.path = diagonal;
}

/**
 * Draw links using the line function
 */
SampleGraphPresenter.prototype.useLineLinkPath = function(source, target) {
	var line = d3.svg.line();
	this.path = function(d) {
		var src = source(d);
		var dst = target(d);
		return line([[src.x, src.y], [dst.x, dst.y]]); }
}

/**
 * Return a function that gives the outgoing edges for a sample.
 *
 * The outgoing edges depends on whether the view is top-down or bottom-up
 */
SampleGraphPresenter.prototype.outEdgesFunction = function() {
	var bottomUpMode = this.bottomUpMode;
	return function(samp) { return bottomUpMode ? samp.parents : samp.children }
}

/**
 * Initialize the sample nodes
 */
SampleGraphPresenter.prototype.initializeGraphSamples = function()
{
	var colors = sampleColors;
	var nodes = this.visibleColumns.map(function(c) { return model.samplesByType[c.type] });
	var outEdgesGetter = this.outEdgesFunction();
	// Compute the x/y coordinates for each sample
	for (var col = 0; col < nodes.length; ++col) {
		var colData = nodes[col];
		var xOffset = this.visibleColumns[col].xOffset;
		var width = this.visibleColumns[col].width;
		for (row = 0; row < colData.length; ++row) {
			var sampleData = colData[row];
			sampleData.col = col;
			sampleData.row = row;
			sampleData.colOffset = xOffset;
			var outEdges = outEdgesGetter(sampleData);
			var oneEdgeOrLess = outEdges.length < 2;
			var connectedNodesWithMultipleEdges = outEdges.filter(function(c) { return outEdgesGetter(c).length > 1 });
			var oneToOne = oneEdgeOrLess && connectedNodesWithMultipleEdges.length == 0;
			sampleData.color = (!oneToOne) ? colors(row) : oneToOneColor;
			sampleData.userEdgesVisible = null;
			sampleData.edgesVisible = col + 1 < FIRST_COLLAPSED_COLUMN;
		}
	}
	this.allNodes = nodes;

	this.vizHeight = (d3.max(nodes, function(d) { return d.length}) + 1) * LINE_HEIGHT + 5;
}

SampleGraphPresenter.prototype.updateVisibility = function() {
	// Turn off visibility on all nodes
	this.allNodes.forEach(function(samps, i) { 
		samps.forEach(function(s) { s.visible = (i > 0) ? false : true })
	});
	var outEdgesGetter = this.outEdgesFunction();
	// Figure out if the nodes should be visible
	this.allNodes.forEach(function(samps) {
		samps.forEach(function(sample) {
			if (null != sample.userEdgesVisible) sample.edgesVisible = sample.userEdgesVisible;
			var showChildren = sample.visible && sample.edgesVisible;
			if (!showChildren) return;
			outEdgesGetter(sample).forEach(function(c) {
				c.visible = true;
				// Nodes with only one outgoing edge should show their connected node as well, unless the user requests otherwise
				if (outEdgesGetter(c).length == 1) {
					c.edgesVisible = (null == c.userEdgesVisible) ? true : c.userEdgesVisible;
				}
			});
		})
	});
}

SampleGraphPresenter.prototype.updateLinks = function() {
	var links = [];
	var outEdgesGetter = this.outEdgesFunction();
	this.allNodes.forEach(function(samps) {
		samps.forEach(function(d) { 
			if (!d.visible) return;
			outEdgesGetter(d).forEach(function(c) { if (c.visible) links.push(new SampleGraphLink(d, c))});
		})
	});

	this.links = links;
}

SampleGraphPresenter.prototype.updateNodes = function() {
	var nodes = this.allNodes.map(function(d) { return d.filter(function(n) { return n.visible })});
	nodes.forEach(function(samps) { samps.forEach(function(s, i) { s.visibleIndex = i })});
	this.nodes = nodes;
}

SampleGraphPresenter.prototype.updateState = function()
{
	// These need to be done in this order
	this.updateVisibility();
	this.updateNodes();
	this.updateLinks();
}

/**
 * Display the sample nodes.
 */
SampleGraphPresenter.prototype.draw = function()
{
	this.updateState();
	var nodes = this.nodes;
	var vizWidth = this.vizWidth;
	var vizHeight = this.vizHeight;

	// Display the graph in an SVG element
	this.viz = this.root.selectAll("svg").data([nodes]);
	// Code under enter is run if there is no HTML element for a data element	
	this.viz.enter().append("svg:svg").attr("class", "viz");
	this.viz.attr("width", vizWidth);
	this.viz.attr("height", vizHeight);
	// Columns
	this.columns = this.viz.selectAll("g").data(function(d) { return d });
	this.columns.enter().append("svg:g").attr("class", "column");
	this.columns.exit().remove();
	var lexicalParent = this;
	this.columns.attr("transform", function(d, i) { return "translate(" + lexicalParent.visibleColumns[i].xOffset + ", 0)"});
	this.drawHeaders();
	this.drawNodes();
	this.drawLinks();
}

/**
 * Draw the headers
 */
SampleGraphPresenter.prototype.drawHeaders = function()
{
	var lexicalParent = this;
	var header = this.columns.selectAll("text.header").data(function(d, i) { return [lexicalParent.visibleColumns[i]] });
	header.enter().append("svg:text")
		.attr("class", "header")
		.attr("x", "0")
		.attr("y", LINE_HEIGHT)
		.attr("text-anchor", "begin")
		.style("font-weight", "bold");
	header
		.text(function(d) { return d.label });
}

/**
 * Draw the nodes
 */
SampleGraphPresenter.prototype.drawNodes = function()
{
	var outEdgesGetter = this.outEdgesFunction();
	var sample = this.columns.selectAll("text.sample").data(function(d) { return d });
	sample.enter().append("svg:text")
		.attr("class", "sample")
		.attr("x", "0")
		.attr("y", LINE_HEIGHT)
		.attr("text-anchor", "begin")
		.style("cursor", "pointer")
		.on("click", function(d) { presenter.openSample(this, d) })
		.transition()
			.style("opacity", 1);
	sample.exit()
		.transition()
			.style("opacity", 0).remove();
	sample
		.attr("x", "0")
		.attr("y", function(d, i) { return LINE_HEIGHT * (i+2)})

		.text(function(d) { return d.identifier });

	var ring = this.columns.selectAll("circle.ring").data(function(d) { return d });
	ring.enter().append("svg:circle")
		.attr("class", "ring")
		.attr("pointer-events", "all")
		.attr("r", 0)
		.style("cursor", "pointer")
		.style("stroke-width", "2px")
	ring.exit()
		.transition()
			.style("opacity", 0).remove();
	ring
		.attr("cx", function(d) { return textBBoxForNode(d).width + 7 })
		.attr("cy", function(d, i) { return LINE_HEIGHT * (i+2) - yLinkOffset})
		.style("fill", function(d) { return d.edgesVisible ? "none" : d.color})
		.style("stroke", function(d) { return d.color})
		.on("click", function(d) { presenter.toggleExpand(this, d) });
	ring
		.transition()
			.style("opacity", function(d) { return outEdgesGetter(d).length > 0 ? 1 : 0 })
			.attr("r", 5);
}

/**
 * Draw the links
 */
SampleGraphPresenter.prototype.drawLinks = function()
{
	var link = this.viz.selectAll("path.link").data(this.links);
	link.enter().append("svg:path")
		.attr("class", "link")
		.attr("pointer-events", "none")
		.style("fill", "none")
		.style("stroke-width", "1.5px")
		.transition()
			.style("opacity", 1);
	link.exit()
		.transition()
			.style("opacity", 0).remove();
	link
		.style("stroke", function(d) { return d.sourceNode.color})
		.attr("d", this.path);
}

SampleGraphPresenter.prototype.toggleExpand = function(svgNode, d) {
	// toggle visiblity
	d.userEdgesVisible = (null == d.userEdgesVisible) ? !d.edgesVisible :!d.userEdgesVisible;
	this.draw();
}

SampleGraphPresenter.prototype.openSample = function(svgNode, d) {
	var url = "/openbis/index.html?viewMode=SIMPLE#entity=SAMPLE&permId=" + d.permId;
	window.open(url, '_blank');
}

function displayActiveMode(active, inactive) {
	active.parent().addClass("active");
	inactive.parent().removeClass("active");
}

function clickedBottomUp() {
	if (presenter.bottomUpMode) return;

	displayActiveMode($('#bottom-up'), $('#top-down'));
	presenter.useBottomUpMode();
	presenter.initializeGraphSamples();
	presenter.draw();
}

function clickedTopDown() {
	if (!presenter.bottomUpMode) return;

	displayActiveMode($('#top-down'), $('#bottom-up'));
	presenter.useTopDownMode();
	presenter.initializeGraphSamples();
	presenter.draw();
}



/// The model that manages state and implements the operations
var model;
model = new FlowcellGraphModel();

// The presenter tranlsates the model into visual elements
var presenter;


function enterApp(data)
{
	$('#bottom-up').click(clickedBottomUp);
	$('#top-down').click(clickedTopDown);
	presenter = new SampleGraphPresenter(model);
	presenter.useBottomUpMode()
    model.requestGraphData(function() { presenter.initializeGraphSamples(); presenter.draw() });
}