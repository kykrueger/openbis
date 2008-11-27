package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IGenericImageBundle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IPageController;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.CompositeMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DictonaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * The <i>screening</i> plugin specific {@link IViewContext} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class ScreeningViewContext extends AbstractViewContext<IScreeningClientServiceAsync>
{
    private static final String TECHNOLOGY_NAME = "screening";

    private final IViewContext<ICommonClientServiceAsync> commonViewContext;

    private final IMessageProvider messageProvider;

    private final IScreeningClientServiceAsync service;

    public ScreeningViewContext(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        this.commonViewContext = originalViewContext;
        this.messageProvider =
                new CompositeMessageProvider(new DictonaryBasedMessageProvider(TECHNOLOGY_NAME),
                        originalViewContext.getMessageProvider());
        this.service = createScreeningClientService();
    }

    private final static IScreeningClientServiceAsync createScreeningClientService()
    {
        final IScreeningClientServiceAsync service = GWT.create(IScreeningClientService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) service;
        endpoint.setServiceEntryPoint(GenericConstants.createServicePath(TECHNOLOGY_NAME));
        return service;
    }

    //
    // IViewContext
    //

    public final IMessageProvider getMessageProvider()
    {
        return messageProvider;
    }

    public final IGenericImageBundle getImageBundle()
    {
        return commonViewContext.getImageBundle();
    }

    public final GenericViewModel getModel()
    {
        return commonViewContext.getModel();
    }

    public final IPageController getPageController()
    {
        return commonViewContext.getPageController();
    }

    public final IScreeningClientServiceAsync getService()
    {
        return service;
    }

    public final IClientPluginFactoryProvider getClientPluginFactoryProvider()
    {
        return commonViewContext.getClientPluginFactoryProvider();
    }

    public final IViewContext<ICommonClientServiceAsync> getCommonViewContext()
    {
        return commonViewContext;
    }
}