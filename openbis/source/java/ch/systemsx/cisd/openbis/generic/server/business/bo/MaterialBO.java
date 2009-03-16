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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The unique {@link IMaterialBO} implementation.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialBO extends AbstractBusinessObject implements IMaterialBO
{

    private final IEntityPropertiesConverter propertiesConverter;

    private MaterialPE material;

    private boolean dataChanged;

    public MaterialBO(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.MATERIAL, daoFactory));
    }

    @Private
    MaterialBO(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session);
        propertiesConverter = entityPropertiesConverter;
    }

    public final void loadByMaterialIdentifier(final MaterialIdentifier identifier)
    {
        material = getMaterialByIdentifier(identifier);
        if (material == null)
        {
            throw UserFailureException.fromTemplate(
                    "No material could be found with given identifier '%s'.", identifier);
        }
        dataChanged = false;
    }

    private MaterialPE getMaterialByIdentifier(final MaterialIdentifier identifier)
    {
        assert identifier != null : "Experiment identifier unspecified.";
        final MaterialPE mat = getMaterialDAO().tryFindMaterial(identifier);
        if (mat == null)
        {
            throw UserFailureException.fromTemplate(
                    "No material could be found for identifier '%s'.", identifier);
        }
        return mat;
    }

    public void save() throws UserFailureException
    {
        assert dataChanged : "Data not changed";
        {
            try
            {
                final ArrayList<MaterialPE> materials = new ArrayList<MaterialPE>();
                materials.add(material);
                getMaterialDAO().createMaterials(materials);
            } catch (final DataAccessException ex)
            {
                throwException(ex, String.format("Material '%s'", material.getCode()));
            }
            dataChanged = false;
        }
    }

    public void edit(MaterialIdentifier identifier, List<MaterialProperty> properties)
    {
        loadByMaterialIdentifier(identifier);
        updateProperties(properties);
        dataChanged = true;
    }

    private void updateProperties(List<MaterialProperty> properties)
    {
        final ArrayList<MaterialPropertyPE> existingProperties =
                new ArrayList<MaterialPropertyPE>(material.getProperties());
        final String type = material.getMaterialType().getCode();
        final MaterialProperty[] newProperties = properties.toArray(MaterialProperty.EMPTY_ARRAY);
        final PersonPE registrator = findRegistrator();
        material.setProperties(propertiesConverter.updateProperties(existingProperties, type,
                newProperties, registrator));
    }

    public MaterialPE getMaterial()
    {
        return material;
    }

}
