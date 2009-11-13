package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;

public class InternalLinkCellRenderer implements GridCellRenderer<BaseEntityModel<?>>
{

    public Object render(BaseEntityModel<?> model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
            Grid<BaseEntityModel<?>> grid)
    {
        if (model.get(property) == null)
        {
            return "";
        } else
        {
            String originalValue = String.valueOf(model.get(property));
            return LinkRenderer.renderAsLinkWithAnchor(originalValue);
        }
    }
}
