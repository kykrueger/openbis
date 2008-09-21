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

package ch.systemsx.cisd.common.array;

/**
 * Base class of a multi-dimensional array. The <var>shape</var> of an array is provided separately
 * to the data as a <code>int[]</code>.
 * 
 * @author Bernd Rinn
 */
public abstract class MDArray<T>
{

    protected final int[] shape;

    protected MDArray(int[] shape)
    {
        assert shape != null;

        this.shape = shape;
    }

    /**
     * Returns the rank of the array.
     */
    public int rank()
    {
        return shape.length;
    }

    /**
     * Returns the extent of the array along its <var>dim</var>-th axis.
     */
    public int size(int dim)
    {
        assert dim < shape.length;

        return shape[dim];
    }

    /**
     * Returns a copy of the shape (dimensions) of the multi-dimensional array.
     */
    public int[] shape()
    {
        return shape.clone();
    }

    /**
     * Returns a copy of the shape (dimensions) of the multi-dimensional array as
     * <code>long[]</code>.
     */
    public long[] longShape()
    {
        final long[] shapeCopy = new long[shape.length];
        for (int i = 0; i < shapeCopy.length; ++i)
        {
            shapeCopy[i] = shape[i];
        }
        return shapeCopy;
    }

    /**
     * Returns the number of elements in the array.
     */
    public abstract int size();

    /**
     * Return an object which has the same value as the element of the array specified by
     * <var>indices</var>.
     */
    public abstract T getAsObject(int[] indices);

    /**
     * Sets the element of the array specified by <var>indices</var> to the particular
     * <var>value</var>.
     */
    public abstract void setToObject(int[] indices, T value);

    /**
     * Computes the linear index for the multi-dimensional <var>indices</var> provided.
     */
    protected int computeIndex(int[] indices)
    {
        assert indices != null;
        assert indices.length == shape.length;

        int index = indices[0];
        for (int i = 1; i < indices.length; ++i)
        {
            index = index * shape[i] + indices[i];
        }
        return index;
    }

    /**
     * Computes the linear index for the two-dimensional (<var>indexX, indexY</var>) provided.
     */
    protected int computeIndex(int indexX, int indexY)
    {
        assert 2 == shape.length;

        return shape[1] * indexX + indexY;
    }

    /**
     * Computes the linear index for the three-dimensional (<var>indexX, indexY, indexZ</var>)
     * provided.
     */
    protected int computeIndex(int indexX, int indexY, int indexZ)
    {
        assert 3 == shape.length;

        return shape[2] * (shape[1] * indexX + indexY) + indexZ;
    }

    /**
     * Converts the <var>shape</var> from <code>long[]</code> to <code>int[]</code>.
     */
    public static int[] toInt(final long[] shape)
    {
        assert shape != null;

        final int[] result = new int[shape.length];
        for (int i = 0; i < result.length; ++i)
        {
            result[i] = (int) shape[i];
            if (result[i] != shape[i])
            {
                throw new IllegalArgumentException("Dimension " + i + "  is too large (" + shape[i]
                        + ")");
            }
        }
        return result;
    }

    /**
     * Converts the <var>shape</var> from <code>int[]</code> to <code>long[]</code>.
     */
    public static long[] toLong(final int[] shape)
    {
        assert shape != null;

        final long[] result = new long[shape.length];
        for (int i = 0; i < result.length; ++i)
        {
            result[i] = shape[i];
        }
        return result;
    }

    /**
     * Returns the one-dimensional length of the multi-dimensional array defined by
     * <var>shape</var>.
     * 
     * @throws IllegalArgumentException If <var>shape</var> overflow the <code>int</code> type.
     */
    public static int getLength(final int[] shape)
    {
        assert shape != null;

        if (shape.length == 0)
        {
            return 0;
        }
        long length = shape[0];
        for (int i = 1; i < shape.length; ++i)
        {
            length *= shape[i];
        }
        int intLength = (int) length;
        if (length != intLength)
        {
            throw new IllegalArgumentException("Length is too large (" + length + ")");
        }
        return intLength;
    }

    /**
     * Returns the one-dimensional length of the multi-dimensional array defined by
     * <var>shape</var>.
     * 
     * @throws IllegalArgumentException If <var>shape</var> overflow the <code>int</code> type.
     */
    public static int getLength(final long[] shape)
    {
        assert shape != null;

        if (shape.length == 0) // NULL data space needs to be treated differently
        {
            return 0;
        }
        long length = shape[0];
        for (int i = 1; i < shape.length; ++i)
        {
            length *= shape[i];
        }
        int intLength = (int) length;
        if (length != intLength)
        {
            throw new IllegalArgumentException("Length is too large (" + length + ")");
        }
        return intLength;
    }

}
