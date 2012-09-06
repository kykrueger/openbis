package ch.systemsx.cisd.openbis.uitest;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.infra.Space;
import ch.systemsx.cisd.openbis.uitest.page.SpaceBrowser;

@Test(groups =
    { "login-admin" })
public class SpaceTest extends SeleniumTest
{

    @Test
    public void newSpaceIsListedInSpaceBrowser() throws Exception
    {
        Space space = new Space();

        openbis.create(space);

        assertThat(SpaceBrowser.class, listsSpace(space));
    }

}
