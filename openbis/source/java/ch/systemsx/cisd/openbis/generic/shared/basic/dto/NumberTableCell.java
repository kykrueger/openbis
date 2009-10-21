/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * Table cell wrapping a number of type double.
 *
 * @author Franz-Josef Elmer
 */
public class NumberTableCell implements ISerializableComparable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;
    
    private double number;
    
    public NumberTableCell(double doubleValue)
    {
        this.number = doubleValue;
    }
    
    public double getNumber()
    {
        return number;
    }
    
    public int compareTo(ISerializableComparable o)
    {
        if (o instanceof NumberTableCell)
        {
            double v1 = number;
            NumberTableCell numberTableCell = (NumberTableCell) o;
            double v2 = numberTableCell.number;
            return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
        }
        return toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj
                || (obj instanceof NumberTableCell && number == ((NumberTableCell) obj).number);
    }

    @Override
    public int hashCode()
    {
        return (int) number;
    }

    @Override
    public String toString()
    {
        return Double.toString(number);
    }
    
    // ---------------------------

    // GWT only
    @SuppressWarnings("unused")
    private NumberTableCell()
    {
    }

    // GWT only
    @SuppressWarnings("unused")
    private void setNumber(double number)
    {
        this.number = number;
    }

    
}
