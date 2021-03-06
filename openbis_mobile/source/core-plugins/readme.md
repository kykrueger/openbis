The openBIS iPad App
====================

The openBIS iPad app is an native iOS application designed for displaying information from an openBIS instance. The app uses a navigation and interaction model that is natural to the iPad to support browsing openBIS data on and offline.

The UI
------

The UI of the iPad app is broken into two sections, the navigation section and the details section. (Will add screenshots from app to aid illustration). The navigation section is for browsing and navigating information in an openBIS instance. It shows summary information about entities to provide an overview. The details section is for looking at one single item and shows all information about that item.

Implementing an iPad UI for an Instance
---------------------------------------

The iPad app is generic -- it is designed to talk to any openBIS instance. The instance provides services that map from openBIS to the iPad to support the app. To provide an iPad UI for an openBIS instance, one needs to decide on a navigation model and an decide what information from openBIS should be shown where. This is the core design task. Once that has been done, implementation is the relatively straightforward task of providing the information necessary to support that model.

The sections on the the ipad-ui core plugin module and the ipad-read-service-v1 provide the detailed information necessary to customize the iPad UI to the data in an openBIS instance. The following sections discuss the decisions in the design process.

Mapping from openBIS to the iPad
--------------------------------

The iPad data model is more generic than the openBIS data model. Whereas openBIS tracks domain-specific information such as experiments, samples, etc., the iPad app only stores information necessary to support navigation and display. It does not know about the meaning of information within a domain context -- it only knows about how to navigate from some information to another. Because of this, the iPad app supports flexibly mapping information from openBIS to the iPad to support just about any kind of navigation. 

Both openBIS and the iPad app refer to groupings of information as entities. To disambiguate, we will refer to "openBIS entities" and "iPad entities" to make clear which we mean, because one openBIS entity may map to several iPad entities and vice-versa.

Structuring data in iPad entities comes down to determining how the user will navigate the information. In many cases, a one-to-one mapping from openBIS entities to iPad entities is an appropriate solution. There are, however some situations in which a more complex mapping is warranted. Sometimes it makes sense to show information from several openBIS entities in one place in the iPad. One example of this is a situation where the user wants to see the metadata for a sample in the context of the data set associated with the sample. This case can be easily implemented in the iPad app.

On the other hand, there are situations where one openBIS entity might best map to several iPad entities to support the desired navigation structure. For example, one openBIS data set may contain three images. By mapping the openBIS data set to three iPad entities, it is possible to show each image on its own page.

Once a navigational scheme has been decided upon, it is necessary to implement a service to perform the mapping. (We may implement a generic service in the future, but there currently is none.) The iPad app is currently read-only, so there is only one service used by the iPad app, the ipad-read-service-v1. In the future, the iPad app may gain the ability to modify entities, requiring the creation of an additional service.

The sections below explain how to implement the ipad-read-service-v1.


The ipad-read-service-v1 Service
================================


Summary
-------

The purpose of the read service is to provide information about entities to the iPad. To do this, it receives requests for data, retrieves the information necessary to fulfill this request, and translates between the openBIS entities, such as experiment and sample, and iPad entities. iPad entities are not the same as openBIS entities. The iPad has a much more generic understanding of entities and ties entities very closely to information displayed in the UI. Since the correspondence between openBIS entities and iPad entities is not simply one-to-one, one openBIS entity may be spread over several iPad entities and one iPad entity can contain information from multiple openBIS entities, the communication model is designed to make it possible for the service to map back and forth between iPad entities and openBIS entities.

iPad Data Model
---------------

The iPad data model tracks information for displaying and navigating between entities on the iPad. Each element in the model is used for a specific purpose in the UI. This table summarizes the pieces of the iPad entity data model and describes what the information is used for.

<table>
	<thead>
		<tr>
			<th>Column</th>
			<th>Description</th>
			<th>Use</th>
			<th>Examples</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>PERM_ID</td>
			<td>A stable identifier for the object.</td>
			<td>Used internally to map from server data to local data.</td>
			<td>openBIS permId</td>
		</tr>
		<tr>
			<td>REFCON</td>
			<td>Data that is passed unchanged back to the server when a row is modified. This can be used by the server to encode whatever it needs in order to modify the row. Transmitted as JSON.</td>
			<td>For server use only; transmitted to the server on every call that it is appropriate.</td>
			<td>E.g., {code : [code], entityKind : [entity kind], entityType : [entity type], permId : [permId]}</td>
		</tr>
		<tr>
			<td>CATEGORY</td>
			<td>A category identifier for grouping entities.</td>
			<td>Used to group entities together</td>
			<td>openBIS entity type</td>
		</tr>
		<tr>
			<td>SUMMARY_HEADER</td>
			<td>A short summary of the entity.</td>
			<td>Shown in listings and as the header for detail views.</td>
			<td>openBIS code</td>
		</tr>
		<tr>
			<td>SUMMARY</td>
			<td>A potentially longer summary of the entity.</td>
			<td>Shown in listings and in detail views.</td>
			<td>The content of a <code>description</code> property</td>
		</tr>
		<tr>
			<td>CHILDREN</td>
			<td>The permIds of the children of this entity. Transmitted as JSON.</td>
			<td>Used to navigate between entities. Entities with children allow drill down and the children are shown in the drill-down view.</td>
			<td>Sample / data set children. An experiments samples or data sets may also be shown this way.</td>
		</tr>
		<tr>
			<td>IDENTIFIER</td>
			<td>An identifier for the object.</td>
			<td>Shown in detail views.</td>
			<td>openBIS identifier</td>
		</tr>
<!--
		<tr>
			<td>IMAGE_URL (deprecated: use IMAGES instead)</td>
			<td>A url for an image associated with this entity. If None or empty, no image is shown.</td>
			<td>Shown in detail views.</td>
			<td>An image from the DSS. An external image.</td>
		</tr>
-->
		<tr>
			<td>IMAGES</td>
			<td>A hash map containing image specifications (described below). Two keys are possible: MARQUEE, a single image specification shown in a prominant location; TILED, many image specification shown in a tiled display.</td>
			<td>Shown in detail views.</td>
			<td>An image from the DSS or an external image from the web.</td>
		</tr>
		<tr>
			<td>PROPERTIES</td>
			<td>Properties (metadata) that should be displayed for this entity. Transmitted as JSON.</td>
			<td>Shown in detail views.</td>
			<td>openBIS properties</td>
		</tr>
		<tr>
			<td>ROOT_LEVEL</td>
			<td>True if the entity should be shown on the root level.</td>
			<td>Used to construct the initial navigation view.</td>
			<td>Empty means do not show. Non-empty means show.</td>
		</tr>
	</tbody>
</table>


There are two fields in the data model that have a purpose beyond the UI. These are the `PERM_ID` field and the `REFCON` field. The `PERM_ID` field is assumed to be a stable identifier for the iPad entity and, thus, cannot change. The `PERM_ID` field is used by the iPad app to associate information from the server to the correct entity on the app.

The `REFCON` field is a field that is not touched by the iPad app at all. The server is free to place whatever information it wishes in this field. The content of the `REFCON` is sent back to the server when the iPad makes requests for more data for an existing entity. The server can thus use the `REFCON` to keep track for itself how several openBIS entities are merged into one iPad entity, for example. The `REFCON` is updated on every request, so the service can modify this value if it is appropriate.

Image Specification
-------------------

Image specifications describe images. They may contain either a URL for the image or the data for the image itself.

<table>
	<thead>
		<tr>
			<th>Key</th>
			<th>Value</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>URL</td>
			<td>A url to the image. The url may refer to the DSS or somewhere in the Internet.</td>
		</tr>
		<tr>
			<td>DATA</td>
			<td>A map containing the data for the image. The format has not yet been finalized, but we expect it to include the following keys: 'BASE64' (base-64 coded data for the image), 'FORMAT' (the image format, e.g., PNG, JPG, etc.).</td>
		</tr>
	</tbody>
</table>

Communication
-------------

The communication model between the iPad and the service has been designed to transmit only the necessary information on demand, when needed. The iPad specifies what kind of data it needs in the parameter key `request`. The response, as with all aggregation services, is in the form of a table model. The headers in the table model correspond to columns in the data model described above. The service then returns only the information requested back to the iPad. The `PERM_ID` and `REFCON` are always returned, as they are used by the communication model to match responses to iPad entities and iPad entities to openBIS entities.

### Requests


<table>
	<thead>
		<tr>
			<th>requestKey</th>
			<th>Arguments</th>
			<th>Description</th>
			<th>Use</th>
			<th>Columns</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>CLIENT_PREFS</td>
			<td>None</td>
			<td>Return values for the client preferences.</td>
			<td>Used to initialize client behavior</td>
			<td>KEY, VALUE</td>
		</tr>
		<tr>
			<td>NAVIGATION</td>
			<td>None</td>
			<td>Return the top-level categories used for navigation.</td>
			<td>Used to initialize the top level of the navigation view.</td>
			<td>PERM_ID, REFCON, CATEGORY, SUMMARY_HEADER, SUMMARY, ROOT_LEVEL</td>
		</tr>		
		<tr>
			<td>ROOT</td>
			<td>entities : List of {"PERM_ID" : String, REFCON : String}</td>
			<td>Return root entities for the specified navigational entities</td>
			<td>Used to track which entities should be constantly available on the iPad.</td>
			<td>PERM_ID, REFCON, CATEGORY, SUMMARY_HEADER, SUMMARY, CHILDREN, ROOT_LEVEL</td>
		</tr>
		<tr>
			<td>DRILL</td>
			<td>entities : List of {"PERM_ID" : String, REFCON : String}</td>
			<td>Return navigational information for the specified entities.</td>
			<td>Used to navigate. The result should at least include data for children of children to permit efficient navigation on the iPad.</td>
			<td>PERM_ID, REFCON, CATEGORY, SUMMARY_HEADER, SUMMARY, CHILDREN</td>
		</tr>
		<tr>
			<td>DETAIL</td>
			<td>entities : List of {"PERM_ID" : String, REFCON : String}</td>
			<td>Return detail information for the specified entities.</td>
			<td>Used to display the entity in the detail view. This should return all information necessary to show the entity.</td>
			<td>PERM_ID, REFCON, SUMMARY_HEADER, SUMMARY, IDENTIFIER, IMAGES, PROPERTIES</td>
		</tr>
		<tr>
			<td>SEARCH</td>
			<td>{searchtext : String, searchdomain : Map}</td>
			<td>Return a list of entities that match the specified search string. The search is restricted to the searchdomain, which is either null or one of the domains specified in the preferences.</td>
			<td>Used to search for matching entities on the server. This returns information necessary to navigate the results (same as DRILL).</td>
			<td>PERM_ID, REFCON, CATEGORY, SUMMARY_HEADER, SUMMARY, CHILDREN</td>
		</tr>		
	</tbody>
</table>


Perferences
-----------

The CLIENT_PREFS request returns information used to configure the client. This information includes the following:

<table>
	<thead>
		<tr>
			<th>Key</th>
			<th>Value Type</th>
			<th>Description</th>
			<th>Use</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>ROOT_SET_REFRESH_INTERVAL</td>
			<td>Number</td>
			<td>The number of seconds the client should wait between calls for the root set</td>
			<td>Used to throttle client/server request frequency</td>
		</tr>
		<tr>
			<td>SEARCH_DOMAINS</td>
			<td>Array of Dictionary</td>
			<td>The domains that can used in searches. The dictionary for a domain should include the keys "key" and "label". The label is displayed in the UI. The domains are displayed in the order sent by the server. The first value in the array is the default search domain on the client. If there is a global domain, that should be the first one.</td>
			<td>Used to support searching in specific domains. Example include "GLOBAL", that is search everywhere, "BARCODE", which searches just for values that can be barcodes, etc.</td>
		</tr>		
		<tr>
			<td>ROOT</td>
			<td>entities : List of {"PERM_ID" : String, REFCON : String}</td>
			<td>Return root entities for the specified navigational entities</td>
			<td>Used to track which entities should be constantly available on the iPad.</td>
			<td>PERM_ID, REFCON, CATEGORY, SUMMARY_HEADER, SUMMARY, CHILDREN, ROOT_LEVEL</td>
		</tr>
		<tr>
			<td>DRILL</td>
			<td>entities : List of {"PERM_ID" : String, REFCON : String}</td>
			<td>Return navigational information for the specified entities.</td>
			<td>Used to navigate. The result should at least include data for children of children to permit efficient navigation on the iPad.</td>
			<td>PERM_ID, REFCON, CATEGORY, SUMMARY_HEADER, SUMMARY, CHILDREN</td>
		</tr>
		<tr>
			<td>DETAIL</td>
			<td>entities : List of {"PERM_ID" : String, REFCON : String}</td>
			<td>Return detail information for the specified entities.</td>
			<td>Used to display the entity in the detail view. This should return all information necessary to show the entity.</td>
			<td>PERM_ID, REFCON, SUMMARY_HEADER, SUMMARY, IDENTIFIER, IMAGES, PROPERTIES</td>
		</tr>
		<tr>
			<td>SEARCH</td>
			<td>{searchtext : String, searchdomain : Map}</td>
			<td>Return a list of entities that match the specified search string. The search is restricted to the searchdomain, which is either null or one of the domains specified in the preferences.</td>
			<td>Used to search for matching entities on the server. This returns information necessary to navigate the results (same as DRILL).</td>
			<td>PERM_ID, REFCON, CATEGORY, SUMMARY_HEADER, SUMMARY, CHILDREN</td>
		</tr>		
	</tbody>
</table>



Communication Model
-------------------

On startup, the iPad app requests the client preferences. These preferences configure how the client should behave. For example, the server can inform the client how often it should refresh its root set. This allows the server to control the load caused by ipad clients. Once the preferences have been retrieved, the client starts initializing its root data, if necessary. The root data contains all the entities that should be stored locally on the iPad. To initialize the root data, it first retrieves the categories from the server. The categories determine the groups shown at the very top level of navigation. These categories are typically not real openBIS entities; they are just there to aid navigation and group entities suitably. Once the categories have been retrieved, the client retrieves the root data for each category by specifying `{ requestKey : ROOT, category : [category ref con]}` as the request parameters.


### Communication diagram

See the diagram in ...


The ipad-ui core plugin module
==============================

The ipad-ui core plugin module defines core-plugins that can be used by any openBIS iPad app. At the moment, this is only a webapp called ipad-debug. This webapp can be used in the development of ipad uis.


The ipad-ui-demo core plugin module
===================================

The ipad-ui-demo is an example module that includes a dropbox for registering data to display with the ipad-ui and a an implementation of the ipad-read-service-v1 specific to this instance.

To use the ipad-ui-demo, install the ipad-ui-demo core-plugin module. 

The tests require images to be jpg.


