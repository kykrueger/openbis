import ch.systemsx.cisd.openbis.generic.server.ComponentNames as ComponentNames
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider as CommonServiceProvider

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions as SpaceFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId as SpacePermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space as Space

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions as ProjectFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId as ProjectPermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project as Project

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions as ExperimentFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId as ExperimentPermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment as Experiment

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions as SampleFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId as SamplePermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample as Sample

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions as DataSetFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId as DataSetPermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet as DataSet

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions as PersonFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId as PersonPermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role as Role
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel as RoleLevel


def process(context, parameters):
	method = parameters.get("method");
	result = None;
	
#	try:
	if method == "freeze":
		# 1. Get entity by type to verify existence and obtain its space code
		sessionToken = parameters.get("sessionToken");
		type = parameters.get("entityType");
		permId = parameters.get("permId");
		entity = getEntity(context.applicationService, sessionToken, type, permId);
		spaceCode = getSpace(entity);
		
		# 2. Verify that the user is an admin in such space
		userId = sessionToken.split("-")[0];
		isAdminOfSpace = isUserAdminOnSpace(context.applicationService, sessionToken, userId, spaceCode)
		
		# 3. Create Freeze List
		result = "OK"
		
		# Debug Info
		print "sessionToken: " + sessionToken
		print "type: " + type
		print "permId: " + permId
		print "entity: " + str(entity)
		print "spaceCode: " + spaceCode
		print "userId: " + userId
		print "isAdminOfSpace: " + str(isAdminOfSpace)
		
		
		
#	except Exception as e:
#		result = str(e)
	return result;

def isUserAdminOnSpace(service, sessionToken, userId, spaceCode):
	id = PersonPermId(userId);
	personfetchOptions = PersonFetchOptions();
	personfetchOptions.withRoleAssignments().withSpace();
	persons = service.getPersons(sessionToken, [id], personfetchOptions);
	person = persons[id];
	for roleAssignment in person.getRoleAssignments():
		if roleAssignment.getRole() == Role.ADMIN and roleAssignment.getRoleLevel() == RoleLevel.INSTANCE:
			return True
		if roleAssignment.getRole() == Role.ADMIN and roleAssignment.getRoleLevel() == RoleLevel.SPACE and roleAssignment.getSpace().getCode() == spaceCode:
			return True
	return False

def getEntity(service, sessionToken, type, permId):
	entity = None;
	if type == "SPACE":
		spaceFetchOptions = SpaceFetchOptions();
		id = SpacePermId(permId);
		entities = service.getSpaces(sessionToken, [id], spaceFetchOptions);
		entity = entities[id];
	if type == "PROJECT":
		projectFetchOptions = ProjectFetchOptions();
		projectFetchOptions.withSpace();
		id = ProjectPermId(permId)
		entities = service.getProjects(sessionToken, [id], projectFetchOptions);
		entity = entities[id];
	if type == "EXPERIMENT":
		experimentFetchOptions = ExperimentFetchOptions();
		experimentFetchOptions.withProject().withSpace();
		id = ExperimentPermId(permId);
		entities = service.getExperiments(sessionToken, [id], experimentFetchOptions);
		entity = entities[id];
	if type == "SAMPLE":
		sampleFetchOptions = SampleFetchOptions();
		sampleFetchOptions.withSpace();
		id = SamplePermId(permId);
		entities = service.getSamples(sessionToken, [id], sampleFetchOptions);
		entity = entities[id];
	if type == "DATASET":
		dataSetFetchOptions = DataSetFetchOptions();
		dataSetFetchOptions.withExperiment().withProject().withSpace();
		dataSetFetchOptions.withSample().withSpace();
		id = DataSetPermId(permId);
		entities = service.getDataSets(sessionToken, [id], dataSetFetchOptions);
		entity = entities[id];
	return entity

def getSpace(entity):
	spaceCode = None;
	if isinstance(entity, Space):
		spaceCode = entity.getCode();
	if isinstance(entity, Project):
		spaceCode = entity.getSpace().getCode();
	if isinstance(entity, Experiment):
		spaceCode = entity.getProject().getSpace().getCode();
	if isinstance(entity, Sample):
		spaceCode = entity.getSpace().getCode();
	if isinstance(entity, DataSet):
		if entity.getSample() is not None:
			spaceCode = entity.getSample().getSpace().getCode();
		if entity.getExperiment() is not None:
			spaceCode = entity.getExperiment().getProject().getSpace().getCode();
	return spaceCode