/// How much horizontal space do the inspectors take
var inspectorsWidth = 100;

/// A helper function for drawing the lines between nodes
var diagonal = d3.svg.diagonal().projection(function(d) { return [d.y, d.x] }); 

/// The model for the visualization 
var model = null;

/// The visualization, referenced by functions that display content
var vis;

/// The Tree layout
var tree = null;

function getAppHeight(){
    return Math.max($(window).height() - 50, model.getVisibleLeafsCount() * 30);
}

function getAppWidth(){
    return $(window).width();
}


/**
 * A model for table visualizations.
 */
function DataTableModel(data) {
    this.data = data;
    this.headers = data.result.columns;
    this.rows = data.result.rows;
    this.getRows = function() { return this.rows; }
    this.getHeaders = function() { return this.headers; }
    this.getVisibleLeafsCount = function() { return this.rows.length; }
}

/**
 * An element in the tree. Consists of:
 *  - code Should be unique
 *  - label For displaying
 *  - type For differentiating between different types of tree elements
 *
 * A tree element keeps track of its children.
 */
function TreeElement(code, label, permId, type) {
    this.code = code;
    this.label = label;
    this.permId = permId;
    this.type = type;
    this.children = [];
    this.childMap = {};
}

TreeElement.prototype.addChild = function(element) {
    this.children.push(element);
    this.childMap[element.code] = element;
}

TreeElement.prototype.getChild = function(code) {
    return this.childMap[code];
}

TreeElement.prototype.getOrCreateChild = function(code, label, permId, type) {
    var child = this.childMap[code];
    if (null == child) {
        child = new TreeElement(code, label, permId, type);
        this.addChild(child);
    }

    return child;
}

/**
 * A model for tree visualizations.
 */
function DataTreeModel(data) {
    this.data = data;
    this.headers = data.result.columns;
    this.rows = data.result.rows;
    this.initializeTreeModel();
    this.getVisibleLeafsCount = function() { return this.rows.length; }
}

/**
 * Restructure the rows into a hieararchical form.
 */
DataTreeModel.prototype.initializeTreeModel = function() {
    // The rows are expected to be structured thusly:
    // BIO Experiment, BIO Sample, MS Inj Sample, Search Experiment, ACC#, Desc
    this.root = new TreeElement('PROT', '', 'ROOT');
    var root = this.root;

    // Construct the tree
    this.rows.forEach(function(each) {
        var bioExpCode = each[0].value;
        var bioSampCode = each[1].value;
        var msInjSampCode = each[2].value;
        var searchExpCode = each[3].value;
        var searchPermId = each[4].value;
        var acc = each[5].value;
        var desc = each[6].value;

        var bioExp = root.getOrCreateChild(bioExpCode, bioExpCode, null, 'BIO-EXP');
        var bioSamp = bioExp.getOrCreateChild(bioSampCode, bioSampCode, null, 'BIO-SAMP');
        var msInjSamp = bioSamp.getOrCreateChild(msInjSampCode, msInjSampCode, null, 'MS-INJ');
        var searchExp = msInjSamp.getOrCreateChild(searchExpCode, searchExpCode, searchPermId, 'MS-SEARCH');
        var proteinLabel = acc + " : " + desc;
        var protein = searchExp.getOrCreateChild(acc, proteinLabel, null, 'PROTEIN');
    });
}

var didCreateVis = false;

/**
 * Create the DOM elements to store the visualization (tree + inspectors)
 */
function createVis()
{ 
    if (didCreateVis) return;
    
    // Create a div to house the tree visualization and the inspectors
    vis = d3.select("#main").append("div").attr("id", "vis");

    didCreateVis = true;
}

function translateSrc(d)
{
    var translate;
    if (d.parent != undefined) {
        var y0 = (null != d.parent.y0) ? d.parent.y0 : d.parent.y;
        var x0 = (null != d.parent.x0) ? d.parent.x0 : d.parent.x;
        translate = "translate(" + y0 + "," + x0 + ")";
    } else {
        translate = "translate(" + 0 + "," + 0 + ")";
    }
    
    return translate;
}

function translateDst(d)
{
    d.x0 = d.x;
    d.y0 = d.y;
    var translate = "translate(" + d.y + "," + d.x + ")";
    
    return translate;
}

/**
 * Convert properties to pairs
 */
function props_to_pairs(d)
{
    var pairs = [];
    for (var prop in d) {
        var pair = [prop, d[prop]];
        pairs.push(pair);
    }
    pairs.sort(function(a, b) { 
        if (a[0] == b[0]) return 0;
        // Sort in reverse lexicographical
        return (a[0] < b[0]) ? -1 : 1;
    });
    return pairs;
}

function displayReturnedResults(data)
{
	  $("#waiting").hide();

    if (data.error) {
        console.log(data.error);
        vis.append("p").text("Could not retrieve data.");
        return;
    }

    var showResultsInTable = false;
    if (showResultsInTable) {
        model = new DataTableModel(data);
        displayResultsInTable();
    } else {
        displayResultsAsGraph(createDataModel(data));
//        model = new DataTreeModel(data);
//        displayResultsInTree();
    }
}

/**
 * Display the samples returned by the server in a table.
 */
function displayResultsInTable()
{   
    // This will show the object in the log -- helpful for debugging
    // console.log(data);
    clearVis();
    
    var headers = model.getHeaders();
    var rows = model.getRows();
    
    var table = vis.append("table").data([rows]);
    table.enter().append("table").attr("class", "result");
    
    var headerRow = table.selectAll("tr.headers").data([headers])
        .enter().append("tr").attr("class", "headers");
        
    headerRow.selectAll("th").data(function(d) { return d })
        .enter().append("th").text(function(d) { return d.title });
        
    var row = table.selectAll("tr.rows").data(rows);
    row.enter().append("tr").attr("class", function(d,i) { if (i % 2 == 0) return "evenRow"; else return "oddRow"; });
    
    row.selectAll("td").data(function(d) { return d })
        .enter()
            .append("td")
            .style("padding", "0px 5px")
            .text(function(d) { return d.value });
}

function classForNode(d) { 
    // Use whether the node has open children or not to compute the class
    var cssClass = "node " + d.type;
    if (d.inspected) cssClass = cssClass + " inspected";
    cssClass = cssClass + " sequenced";
    return cssClass;
}

function getTextAnchorType(d){
    return d.type == 'PROTEIN' ? 'start' : 'end';    
}

// Toggle children on click.
function clickedNode(treeNode) { 
    var url;
    switch(treeNode.type) {
        case 'MS-SEARCH':
            url = openbisUrl + "/?#entity=EXPERIMENT&permId=" + treeNode.permId;
            break;
        default:
            url = null;
    }
    if (null != url) window.open(url, '_blank');
}

function hasChildren(d)
{
    return d.children.length > 0;
}

function displayResultsAsGraph(data) 
{
    clearVis();
    if (data.length == 0)
    {
    	vis.append("p").text("Nothing found.");
    	return;
    }
    
    var xOffset = 30,
        yOffset = 10,
        xStep = 200,
        yStep = 30;
        
    var graph = vis.selectAll("svg").data([data]).enter().append("svg:svg")
        .attr("width", $(window).width() - 50)
        .attr("height", $(window).height() - 50);

    var g = graph.selectAll("g").data(function(d) { return d; })
        .enter().append("svg:g")
        .attr("transform", function(d,i) { return "translate(" + (xOffset + i * xStep) + ", "
                                                               + yOffset + ")"; });
    g.selectAll("path").data(function(d) { return d[1]; })
        .enter().append("svg:path")
        .attr("class", "line")
        .attr("d", function(d,i) { 
            var y0 = d[0] * yStep;
            var y1 = d[1] * yStep;
            return "M0," + y0 
                 + "Q" + xStep/4 + "," + y0 + "," + xStep/2 + "," + (y0+y1)/2 
                 + "L" + xStep/2 + "," + (y0+y1)/2 
                 + "Q" + 3*xStep/4 + "," + y1 + "," + xStep + "," + y1
                 + "L" + xStep + "," + y1; 
            })
        
    g.selectAll("circle").data(function(d) { return d[0]; })
        .enter().append("svg:circle")
        .attr("class", "circle")
        .attr("cx", 0)
        .attr("cy", function(d, i) { return i * yStep; })
        .attr("r", 5);
        
    g.selectAll("text").data(function(d) { return d[0]; })
        .enter().append("svg:text")
        .text(function(d) { return d; })
        .attr("text-anchor", "left")
        .attr("class", "nt")
        .attr("x", -10)
        .attr("y", function(d, i) { return 18 + i * yStep; });
}

function createDataModel(tableModel)
{
    var rows = tableModel.result.rows
    var maps = [];
    
    for (n = 0; n < rows.length; n++) {
        row = rows[n];
        row[5].value = row[5].value + ": " + row[6].value; // concatenate accession number with description
        row.splice(6, 1);
        row.splice(4, 1); // remove search experiment perm id
        for (i = 0; i < row.length; i++) {
            map = maps[i];
            if (map == null) {
                map = {};
                maps[i] = map;
            }
            cell = row[i].value;
            links = map[cell];
            if (links == null) {
                links = {};
                map[cell] = links;
            }
            if (i < row.length - 1) {
                links[row[i + 1].value] = 0;
            }
        }
    }
    var data = [];
    var maxNumber = 0;
    for (i = 0; i < maps.length; i++) {
        var map = maps[i];
        var elements = [];
        for (element in map) {
            elements.push(element);
        }
        maxNumber = Math.max(maxNumber, elements.length);
        elements.sort();
        var indexMap = {};
        for (j = 0; j < elements.length; j++) {
            indexMap[elements[j]] = j;
        }
        data.push([elements, [], indexMap]);
    }
    
    for (i = 0; i < maps.length - 1; i++) {
        var map = maps[i];
        var from = data[i][0];
        var links = data[i][1];
        var indexMap = data[i + 1][2];
        for (j = 0; j < from.length; j++) {
            element = from[j];
            linkedElements = map[element];
            for (linkedElement in linkedElements) {
                links.push([j, indexMap[linkedElement]]);
            }
        }
    }
    
    return data;
}


/**
 * Display the samples returned by the server in a tree.
 */
function displayResultsInTree()
{
    var duration = 500;
    var treeVis = vis.selectAll("svg").data([model.root]);
    treeVis.enter()
        .append("svg:svg");

    // Adjust a size of the vis 
    treeVis
        .attr("width", getAppWidth() - 50)
        .attr("height", getAppHeight());
    
    // Adjust a size of the tree 
    tree = d3.layout.tree().size([getAppHeight(), getAppWidth() - 300]) 
    
    // Update the root and compute the new layout 
    var nodes = tree.nodes(model.root);

    // Draw / update the links  
    var link = treeVis.selectAll("path.link").data(tree.links(nodes), function(d) { return d.code });
    
    link.enter().append("svg:path")
            .attr("class", "link")
            .attr("d", function(d) {
                var y0 = (null != d.source.y0) ? d.source.y0 : d.source.y; 
                var x0 = (null != d.source.x0) ? d.source.x0 : d.source.x;
                var o = {x: x0, y: y0};
                return diagonal({source: o, target: o});
            })
        .transition()
            .duration(duration)
            .attr("d", diagonal);
        
    link.transition()
        .duration(duration)
        .attr("d", diagonal);
        
    link.exit().transition()
        .duration(duration)
        .attr("d", function(d) {
                var y0 = (null != d.source.y0) ? d.source.y0 : d.source.y; 
                var x0 = (null != d.source.x0) ? d.source.x0 : d.source.x;
                var o = {x: x0, y: y0};
                return diagonal({source: o, target: o});
            })
        .remove();

    // Draw / update the nodes
    var node = treeVis.selectAll("g.node").data(nodes, function(d) { return d.code });
    
    var nodeEnter = 
        node.enter().append("svg:g")
            .attr("class", classForNode)
            .attr("transform", translateSrc)
            .on("click", clickedNode);

    nodeEnter.append("svg:circle")
        .attr("r", 5.5);

    nodeEnter.append("svg:text")
        .text(function(d) { return d.label });

    nodeEnter
        .transition()
            .duration(duration)
            .attr("transform", translateDst);
        
    
    // Transition nodes to their new position.
    node.transition()
        .duration(duration)
        .attr("class", classForNode)
        .attr("transform", translateDst);

    // Move the text elements to the appropriate position
    node.selectAll("text").transition()
        .duration(duration)
        .attr("dx", function(d) { return hasChildren(d) ? 0 : 8 })
        .attr("dy", function(d) { return hasChildren(d) ? -10 : 3 })
        .attr("text-anchor", function(d) { return getTextAnchorType(d) });
        
    node.exit().transition()
        .duration(duration)
        .attr("transform", translateSrc)
        .remove();
}

function clearVis()
{
    $('#vis > *').remove();
}

/**
 * Request samples matching some criteria from the server and show them in the Page.
 */
function queryForResults(parameters)
{
	  $("#waiting").show();
    openbisServer.createReportFromAggregationService("DSS1", "demo-proteomics-aggregation", parameters, displayReturnedResults);
//    openbisServer.createReportFromAggregationService("STANDARD", "demo-proteomics-aggregation", parameters, displayReturnedResults);
}


function enterApp(data)
{
    if(data.result == null){
        alert("Login or password incorrect");
        $("#username").focus();
        return;
    }
    
    $("#login-form-div").hide();
    $("#main").show();
    
    $('#openbis-logo').height(30);
    
    createVis()
}