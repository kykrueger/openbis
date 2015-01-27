package ch.ethz.bsse.cisd.dsu.dss.systemtests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

public class QGFReadRtaTimestampTest extends DSUDropboxSystemTest
{

    @Override
    protected String getDropboxName()
    {
        return "read-rta-timestamp";
    }

    @Test
    public void testQGFCreateFlowcellNextseq() throws Exception
    {
        importDataWithMarker("141204_D00535_0035_BC5LPVANXX");
        waitUntilDataImported();
        waitUntilIndexUpdaterIsIdle();

        String sessionToken = getGeneralInformationService().tryToAuthenticateForAllServices("kohleman", "password");

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "141204_D00535_0035_BC5LPVANXX"));

        List<Sample> samples = getGeneralInformationService().searchForSamples(sessionToken, criteria);
        Assert.assertEquals(samples.size(), 1);
        Sample flowCell = samples.get(0);
        Assert.assertEquals(flowCell.getProperties().get("FLOW_CELL_SEQUENCED_ON"), "2014-12-04 15:31:37 +0100");
    }

}