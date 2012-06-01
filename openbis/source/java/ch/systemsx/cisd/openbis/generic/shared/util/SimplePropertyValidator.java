/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.PropertyUtils.Boolean;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.ValidationUtilities.HyperlinkValidationHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * This is a refactoring of
 * {@link ch.systemsx.cisd.openbis.generic.server.dataaccess.PropertyValidator} that takes some
 * simple validations that do not require access to the PEs to a more accessible place.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SimplePropertyValidator
{

    public enum SupportedDatePattern
    {
        DAYS_DATE_PATTERN("yyyy-MM-dd"),

        MINUTES_DATE_PATTERN("yyyy-MM-dd HH:mm"),

        SECONDS_DATE_PATTERN("yyyy-MM-dd HH:mm:ss"),

        US_DATE_PATTERN("M/d/yy"),

        US_DATE_TIME_PATTERN("M/d/yy h:mm a"),

        US_DATE_TIME_24_PATTERN("M/d/yy HH:mm"),

        CANONICAL_DATE_PATTERN(BasicConstant.CANONICAL_DATE_FORMAT_PATTERN),

        RENDERED_CANONICAL_DATE_PATTERN(BasicConstant.RENDERED_CANONICAL_DATE_FORMAT_PATTERN);

        private final String pattern;

        SupportedDatePattern(String pattern)
        {
            this.pattern = pattern;
        }

        public String getPattern()
        {
            return pattern;
        }
    }

    private final static String[] DATE_PATTERNS = createDatePatterns();

    private final static Map<DataTypeCode, IDataTypeValidator> dataTypeValidators =
            createDataTypeValidators();

    private final static Map<DataTypeCode, IDataTypeValidator> createDataTypeValidators()
    {
        final Map<DataTypeCode, IDataTypeValidator> map =
                new EnumMap<DataTypeCode, IDataTypeValidator>(DataTypeCode.class);
        map.put(DataTypeCode.BOOLEAN, new BooleanValidator());
        map.put(DataTypeCode.VARCHAR, new VarcharValidator());
        map.put(DataTypeCode.TIMESTAMP, new TimestampValidator());
        map.put(DataTypeCode.INTEGER, new IntegerValidator());
        map.put(DataTypeCode.REAL, new RealValidator());
        map.put(DataTypeCode.HYPERLINK, new HyperlinkValidator());
        map.put(DataTypeCode.MULTILINE_VARCHAR, new VarcharValidator());
        return map;
    }

    private final static String[] createDatePatterns()
    {
        final List<String> datePatterns = new ArrayList<String>();
        // Order does not matter due to DateUtils implementation used.
        for (SupportedDatePattern supportedPattern : SupportedDatePattern.values())
        {
            datePatterns.add(supportedPattern.getPattern());
        }
        return datePatterns.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public boolean canValidate(DataTypeCode entityDataType)
    {
        return null != dataTypeValidators.get(entityDataType);
    }

    //
    // IPropertyValidator
    //

    public final String validatePropertyValue(final DataTypeCode entityDataType, final String value)
            throws UserFailureException
    {
        assert value != null : "Unspecified value.";

        // don't validate error messages and placeholders
        if (value.startsWith(BasicConstant.ERROR_PROPERTY_PREFIX))
        {
            return value;
        }
        final IDataTypeValidator dataTypeValidator = dataTypeValidators.get(entityDataType);
        assert dataTypeValidator != null : String.format("No IDataTypeValidator implementation "
                + "specified for '%s'.", entityDataType);
        return dataTypeValidator.validate(value);
    }

    //
    // Helper classes
    //

    public static interface IDataTypeValidator
    {
        /**
         * Validates given <var>value</var> according to this data type.
         * 
         * @return the validated value. Note that it can differ from the given one.
         * @throws UserFailureException if given <var>value</var> is not valid.
         */
        public String validate(final String value) throws UserFailureException;
    }

    public final static class BooleanValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            final Boolean bool = PropertyUtils.Boolean.getBoolean(value);
            if (bool == null)
            {
                throw UserFailureException.fromTemplate("Boolean value '%s' has improper format. "
                        + "It should be either 'true' or 'false'.", value);
            }
            return java.lang.Boolean.toString(bool.toBoolean());
        }
    }

    private final static class VarcharValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            return value;
        }
    }

    public final static class TimestampValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            try
            {
                Date date = DateUtils.parseDate(value, DATE_PATTERNS);
                // we store date in CANONICAL_DATE_PATTERN
                return DateFormatUtils.format(date,
                        SupportedDatePattern.CANONICAL_DATE_PATTERN.getPattern());
            } catch (final ParseException ex)
            {
                throw UserFailureException.fromTemplate(
                        "Date value '%s' has improper format. It must be one of '%s'.", value,
                        Arrays.toString(DATE_PATTERNS));
            }
        }
    }

    public final static class IntegerValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            try
            {
                Long.parseLong(value);
                return value;
            } catch (final NumberFormatException ex)
            {
                throw UserFailureException.fromTemplate("Integer value '%s' has improper format.",
                        value);
            }
        }
    }

    public final static class RealValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            try
            {
                Double.parseDouble(value);
                return value;
            } catch (final NumberFormatException ex)
            {
                throw UserFailureException.fromTemplate("Double value '%s' has improper format.",
                        value);
            }
        }
    }

    private final static class HyperlinkValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";

            // validate protocols and format
            if (HyperlinkValidationHelper.isProtocolValid(value) == false)
            {
                throw UserFailureException.fromTemplate(
                        "Hyperlink '%s' should start with one of the following protocols: '%s'",
                        value, HyperlinkValidationHelper.getValidProtocolsAsString());
            }
            if (HyperlinkValidationHelper.isFormatValid(value) == false)
            {
                throw UserFailureException.fromTemplate(
                        "Hyperlink value '%s' has improper format.", value);
            }

            // validated value is valid
            return value;
        }
    }
}
