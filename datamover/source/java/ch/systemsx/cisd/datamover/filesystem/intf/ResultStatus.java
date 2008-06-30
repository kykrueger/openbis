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

package ch.systemsx.cisd.datamover.filesystem.intf;

/**
 * A class that holds the information about the status and the result of an operation. To be used
 * whenever a failure of an operation is signaled back via a return value rather than an exception.
 * 
 * @author Tomasz Pylak
 */
public class ResultStatus<T>
{
    private final T result;

    // if true the result is unavailable
    private final boolean errorOccurred;

    // can be used not only in case of errors
    private final String messageOrNull;

    public static <T> ResultStatus<T> createError()
    {
        return new ResultStatus<T>(null, true, null);
    }

    public static <T> ResultStatus<T> createError(String message)
    {
        return new ResultStatus<T>(null, true, message);
    }

    public static <T> ResultStatus<T> createResult(T result)
    {
        return new ResultStatus<T>(result, false, null);
    }

    public static <T> ResultStatus<T> createResult(T result, String message)
    {
        return new ResultStatus<T>(result, false, message);
    }

    protected ResultStatus(T result, boolean errorOccurred, String messageOrNull)
    {
        this.result = result;
        this.errorOccurred = errorOccurred;
        this.messageOrNull = messageOrNull;
    }

    /**
     * can be called only if no error occurred
     * 
     * @return result of an operation
     */
    public T getResult()
    {
        assert isError() == false : "Operation failed, there is no result";
        return result;
    }

    /** @return message associated with the result or an error if there is any */
    public String tryGetMessage()
    {
        return messageOrNull;
    }

    /** has operation finished with an error? */
    public boolean isError()
    {
        return errorOccurred;
    }
}
