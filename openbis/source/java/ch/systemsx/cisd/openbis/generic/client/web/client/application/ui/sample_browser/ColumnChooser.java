/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;

class ColumnChooser extends TextToolItem
{
    private static final int START = 1;

    private static final int MENU_LENGTH = 15;

    private final CommonColumns commonColumns;

    private final ParentColumns parentColumns;

    private final PropertyColumns propertyColumns;

    public ColumnChooser(CommonColumns commonColumns, ParentColumns parentColumns,
            PropertyColumns propertyColumns)
    {
        super("Columns");
        this.commonColumns = commonColumns;
        this.parentColumns = parentColumns;
        this.propertyColumns = propertyColumns;
        setEnabled(false);
    }

    public void reload()
    {
        Menu menu = new Menu();
        addSubMenu(createCommonMenu(), menu, "Common", false);
        addSubMenu(createParentsMenu(), menu, "Parents", false);
        addSubMenu(createPropertiesMenu(), menu, "Properties", true);
        setMenu(menu);
    }

    private List<Item> createCommonMenu()
    {
        final ArrayList<Item> result = new ArrayList<Item>();
        for (ColumnConfig cc : commonColumns.getColumns())
        {
            result.add(createFromConfig(cc));
        }
        return result;
    }

    private List<Item> createParentsMenu()
    {
        final ArrayList<Item> result = new ArrayList<Item>();
        for (ColumnConfig cc : parentColumns.getColumns())
        {
            result.add(createFromConfig(cc));
        }
        return result;
    }

    private List<Item> createPropertiesMenu()
    {
        final ArrayList<Item> result = new ArrayList<Item>();
        for (ColumnConfig cc : propertyColumns.getColumns())
        {
            result.add(createFromConfig(cc));
        }
        return result;

    }

    private Item createFromConfig(final ColumnConfig cc)
    {
        final CheckMenuItem result = new CheckMenuItem(cc.getHeader());
        result.setChecked(cc.isHidden() == false);
        result.setHideOnClick(false);
        result.addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    cc.setHidden(result.isChecked() == false);
                }
            });
        return result;
    }

    private void addSubMenu(List<Item> columns, Menu menu, String title, boolean isLastSubmenu)
    {
        if (columns.size() > 0)
        {
            addItems(menu, menu.getItemCount() + columns.size() > MENU_LENGTH, title, columns);
            if (false == isLastSubmenu)
            {
                menu.add(new SeparatorMenuItem());
            }
        }
    }

    private void addItems(Menu columnsMenu, boolean folded, String title, List<Item> columns)
    {
        Menu subMenu = new Menu();
        int counter = START;
        for (Item column : columns)
        {
            if (folded == false)
            {
                columnsMenu.add(column);
            } else
            {
                if (counter % MENU_LENGTH - START == 0)
                {
                    MenuItem menuItem =
                            new MenuItem(title + " " + counter + " - "
                                    + Math.min(counter + MENU_LENGTH - 1, columns.size()));
                    subMenu = new Menu();
                    menuItem.setSubMenu(subMenu);
                    columnsMenu.add(menuItem);
                }
                subMenu.add(column);
                counter++;
            }
        }
    }

}