from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria

def process(context, parameters):
    spaces = context.applicationService.searchSpaces(context.sessionToken, SpaceSearchCriteria(), SpaceFetchOptions())
    return "Number of spaces: %s" % spaces.getObjects().size()