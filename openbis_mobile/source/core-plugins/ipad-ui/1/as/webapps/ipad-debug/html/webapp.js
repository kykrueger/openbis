/// The openbisServer we use for our data
openbisServer = new openbis('/openbis/openbis', '/datastore_server');

function getAppHeight(){
	return Math.max($(window).height() - 50, getVisibleLeafsCountForNode(root) * 30);
}

function getAppWidth(){
	return $(window).width();
}


var didCreateVis = false;

function parseJson(jsonString) { return eval('(' + jsonString + ')'); }


/**
 * The model that manages state and implements the operations.
 */
function IpadModel() {
	this.initializeModel();
}

IpadModel.prototype.initializeModel = function() {
	this.selectedEntity = null;
}

IpadModel.prototype.selectEntity = function(d) {
	this.selectedEntity = d;
	var permId = d[0].value;
	var refcon = parseJson(d[1].value);
	var rootLevel = d[d.length - 1].value;
	if ("1" == rootLevel || "true" == rootLevel.toLowerCase()) {
		this.selectNavigationEntity(permId, refcon);
	} else {
		this.selectNormalEntity(permId, refcon, d);
	}
}

IpadModel.prototype.selectNavigationEntity = function(permId, refcon) {
	listRootLevelEntities(permId, refcon);
}

IpadModel.prototype.selectNormalEntity = function(permId, refcon, d) {
	var children = parseJson(d[5].value);
	if (children.length > 0) {
		drillOnEntity(permId, refcon);
		detailsForEntity(permId, refcon);
	} else {
		detailsForEntity(permId, refcon);
	}	
}

IpadModel.prototype.setSearchText = function(searchText, searchDomain) {
	searchForText(searchText, searchDomain);
}

/// The model that manages state and implements the operations
var model;
model = new IpadModel();


/// The visualization, referenced by functions that display content
var clientPrefs, navigation, root, drill, detail, searchresults;

/**
 * Create the DOM elements to store the visualization (tree + inspectors)
 */
function createVis()
{ 
	if (didCreateVis) return;
	
	// Create a div to house the tree visualization and the inspectors
	clientPrefs = d3.select("#clientprefs");
	navigation = d3.select("#navigation");
	root = d3.select("#root");
	drill = d3.select("#drill");	
	detail = d3.select("#detail");
	searchresults = d3.select("#searchresults");

	didCreateVis = true;
}

/**
 * Construct the table header.
 */
function showTableHeader(table)
{
	var header = table.selectAll("thead").data(function(d) { return [d.columns] });
	header.enter().append("thead");
	var headerRows = header.selectAll("tr").data(function(d) { return [d] });
	headerRows.enter().append("tr");
	var headerData = headerRows.selectAll("th").data(function(d) { return d; });
	headerData.enter().append("th");
	headerData.text(function (d) { return d.title})
}

/**
 * Construct the table data.
 */
function showTableData(table)
{
	var tableBody = table.selectAll("tbody").data(function(d) { return [d.rows] });
	tableBody.enter().append("tbody");
	var dataRows = tableBody.selectAll("tr").data(function(d) { return d });
	dataRows.enter().append("tr").on("click", function (d) { model.selectEntity(d); });
	dataRows.exit().remove();

	var dataData = dataRows.selectAll("td").data(function(d) { return d });
	dataData.enter().append("td");
	dataData.text(function (d) { return d.value});
}


/**
 * Display the data returned by the server
 */
function displayResults(node, data)
{
	if (data.error) {
		console.log(data.error);
		node.append("p").text("Could not retrieve data.");
		return;
	}

	node.selectAll("p").data([]).exit().remove();
	
	// This will show the object in the log -- helpful for debugging
	// console.log(data.result);
	var tableData = data.result;
	
	// Display the rows in a table
	var table = node.selectAll("table").data([tableData]);
	// Code under enter is run if there is no HTML element for a data element	
	table.enter().append("table").attr("class", "table");
	showTableHeader(table);
	showTableData(table);
}

/**
 * Display that the call is in progress
 */
function displayCallInProgress(node)
{
	// Display the rows in a table
	var table = node.selectAll("table").data([]);
	// Code under enter is run if there is no HTML element for a data element
	table.enter().append("table").attr("class", "table");
	table.exit().remove();
	node.append("p").text("Getting data...");
}

function displayClientPreferences(data) { displayResults(clientPrefs, data) }
function displayNavigation(data) { displayResults(navigation, data) }
function displayRoot(data) { displayResults(root, data) }
function displayDrill(data) { displayResults(drill, data) }
function displayDetail(data) { displayResults(detail, data) }
function displaySearchResults(data) { displayResults(searchresults, data) }

/**
 * Request the client perferences and show them in the page.
 */
function clientPreferencesCallback(data) {
	displayClientPreferences(data);
	updateSearchDomains(data);
}

function listClientPreferences()
{
	var parameters = {requestKey : 'CLIENT_PREFS'};
	displayCallInProgress(clientPrefs);
	openbisServer.createReportFromAggregationService("DSS1", "ipad-read-service-v1", parameters, clientPreferencesCallback);
}

/**
 * Request the top-level navigational entities and show them in the page
 */
function listNavigationEntities()
{
	var parameters = {requestKey : 'NAVIGATION'};
	displayCallInProgress(navigation);
	openbisServer.createReportFromAggregationService("DSS1", "ipad-read-service-v1", parameters, displayNavigation);
}

/**
 * Request samples matching some criteria from the server and show them in the page.
 */
function listRootLevelEntities(permId, refcon)
{
	var entities = [{"PERM_ID" : permId, "REFCON" : refcon}];
	var parameters = {requestKey : 'ROOT', entities: entities};

	displayCallInProgress(root);
	openbisServer.createReportFromAggregationService("DSS1", "ipad-read-service-v1", parameters, displayRoot);
}

function drillOnEntity(permId, refcon)
{
	var entities = [{"PERM_ID" : permId, "REFCON" : refcon}];
	var parameters = {requestKey : 'DRILL', entities: entities};

	displayCallInProgress(drill);
	openbisServer.createReportFromAggregationService("DSS1", "ipad-read-service-v1", parameters, displayDrill);
}

function detailsForEntity(permId, refcon)
{
	var entities = [{"PERM_ID" : permId, "REFCON" : refcon}];
	var parameters = {requestKey : 'DETAIL', entities: entities};

	displayCallInProgress(detail);
	openbisServer.createReportFromAggregationService("DSS1", "ipad-read-service-v1", parameters, displayDetail);
}

function searchForText(searchText, searchDomain)
{
	var parameters = {requestKey : 'SEARCH', searchtext: searchText, searchdomain: searchDomain};

	displayCallInProgress(searchresults);
	openbisServer.createReportFromAggregationService("DSS1", "ipad-read-service-v1", parameters, displaySearchResults);
}

function homeTab()
{
    $('#home').show();
    $('#search').hide();
	$('#activate-home').parent().addClass("active");
    $('#activate-search').parent().removeClass("active");    
}

function searchTab()
{
    $('#home').hide();
    $('#search').show();
    $('#activate-home').parent().removeClass("active");
    $('#activate-search').parent().addClass("active");
}


function getClientPreferencesAsMap(data) {
	var clientPrefsMap = {};
	for(var rowIndex = 0; rowIndex < data.result.rows.length; rowIndex++) {
		var row = data.result.rows[rowIndex];
		clientPrefsMap[row[0].value] = [row[1].value];
	}
	return clientPrefsMap;
}

function updateSearchDomains(data) {
	var clientPrefs = getClientPreferencesAsMap(data);
	var domains = $.parseJSON(clientPrefs['SEARCH_DOMAINS'][0]);
	for(var domainIndex = 0; domainIndex < domains.length; domainIndex++) {
			$('#searchdomains').append(new Option(domains[domainIndex].label, domains[domainIndex].key, true, true));
	}
}

function configureTabs()
{
	$('#activate-search').click(searchTab);
	$('#activate-home').click(homeTab);	
	$('#search-form').submit(function() {
		model.setSearchText($.trim($('#searchtext').val()), $('#search-domains-select').val());
	});
	
}

function enterApp(data)
{
	if(data.result == null){
		alert("Login or password incorrect");
		$("#username").focus();
		return;
	}

	$('#login').hide();
    $('#main').show();
    $('#search').hide();

    createVis();
    listClientPreferences();
    listNavigationEntities();
}