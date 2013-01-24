/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import static ch.systemsx.cisd.openbis.dss.generic.shared.content.ContentCache.CACHE_FOLDER;
import static ch.systemsx.cisd.openbis.dss.generic.shared.content.ContentCache.DOWNLOADING_FOLDER;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.concurrent.MessageChannelBuilder;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.ConsoleLogger;
import ch.systemsx.cisd.common.test.ProxyAction;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.ContentCache.DataSetInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * @author Franz-Josef Elmer
 */
public class ContentCacheTest extends AbstractRemoteHierarchicalContentTestCase
{
    private static final String DATA_SET_CODE1 = "DS-1";
    private static final String DATA_SET_CODE2 = "DS-2";

    @Test
    public void testCreateCacheInstanceForEmptyCache()
    {
        createCache();

        assertEquals("Content cache created. Workspace: " + workSpace.getAbsolutePath() + "\n"
                + createSizeLogLine(0, 0).trim(), logRecorder.getLogContent());
        assertEquals("{}", dataSetInfos.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateCacheInstanceForOneDataSetInCache()
    {
        File dataSetFolder = new File(workSpace, CACHE_FOLDER + "/" + DATA_SET_CODE);
        File file = new File(dataSetFolder, "abc.txt");
        file.getParentFile().mkdirs();
        FileUtilities.writeToFile(file, FILE1_CONTENT);
        dataSetFolder.setLastModified(42000);
        prepareRequestPersistence(1);
        
        createCache();

        assertEquals(createFirstLogLine() + createRecreatedlogLine(DATA_SET_CODE)
                + createSizeLogLine(4, 1).trim(), logRecorder.getLogContent());
        assertDataSetInfos(DATA_SET_CODE, 1, 1, 42000);
        context.assertIsSatisfied();
    }

    @Test
    public void testDataSetLocking()
    {
        ContentCache cache = createCache();

        cache.lockDataSet("DS-1");

        assertEquals(true, cache.isDataSetLocked("DS-1"));
        assertEquals(false, cache.isDataSetLocked("DS-2"));

        cache.lockDataSet("DS-1");

        assertEquals(true, cache.isDataSetLocked("DS-1"));

        cache.unlockDataSet("DS-1");

        assertEquals(true, cache.isDataSetLocked("DS-1"));

        cache.unlockDataSet("DS-1");

        assertEquals(false, cache.isDataSetLocked("DS-1"));

        context.assertIsSatisfied();
    }

    @Test
    public void testGetFileWhichIsNotInCache()
    {
        final DataSetPathInfo pathInfo1 = prepareForDownloading(remoteFile1);
        prepareRequestPersistence(1);

        ContentCache cache = createCache();
        File file = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);

        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(file).trim());
        assertDataSetInfos(DATA_SET_CODE, FILE1_CONTENT.length(), 1000);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetFileWhichIsInCache()
    {
        DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setRelativePath(remoteFile1.getName());
        pathInfo.setDirectory(false);
        File fileInCache = new File(workSpace, CACHE_FOLDER + "/"
                + DATA_SET_CODE + "/" + pathInfo.getRelativePath());
        fileInCache.getParentFile().mkdirs();
        FileUtilities.writeToFile(fileInCache, FILE1_CONTENT);
        prepareRequestPersistence(2);
        
        ContentCache cache = createCache();
        File file = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
        
        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(file).trim());
        assertDataSetInfos(DATA_SET_CODE, 1, 1, 1000);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetInputStreamForFileNotInCache()
    {
        final DataSetPathInfo pathInfo1 = prepareForDownloading(remoteFile1);
        prepareRequestPersistence(1);
        
        ContentCache cache = createCache();
        InputStream inputStream = cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);
        
        assertEquals(FILE1_CONTENT, readContent(inputStream, true));
        assertDataSetInfos(DATA_SET_CODE, FILE1_CONTENT.length(), 1000);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetInputStreamForFileNotInCacheReadingBytePerByte() throws IOException
    {
        final DataSetPathInfo pathInfo1 = prepareForDownloading(remoteFile1);
        prepareRequestPersistence(1);
        
        ContentCache cache = createCache();
        InputStream inputStream = cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);
        
        StringBuilder builder = new StringBuilder();
        while (true)
        {
            int b = inputStream.read();
            if (b < 0)
            {
                break;
            }
            builder.append((char) b);
        }
        inputStream.close();
        
        assertEquals(FILE1_CONTENT, builder.toString());
        assertDataSetInfos(DATA_SET_CODE, FILE1_CONTENT.length(), 1000);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetInputStreamForFileNotInCacheAndInterruptReading() throws IOException
    {
        prepareForDownloading(remoteFile1);
        final DataSetPathInfo pathInfo1 = prepareForDownloading(remoteFile1);
        prepareRequestPersistence(1);
        
        ContentCache cache = createCache();
        InputStream inputStream = cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);
        
        byte[] bytes = new byte[100];
        assertEquals(11, inputStream.read(bytes, 0, 11));
        assertEquals(FILE1_CONTENT.substring(0, 11), new String(bytes, 0, 11));
        inputStream.close();
        
        inputStream = cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);
        assertEquals(FILE1_CONTENT, readContent(inputStream, true));
        assertDataSetInfos(DATA_SET_CODE, FILE1_CONTENT.length(), 1000);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetInputStreamForFileInCache()
    {
        DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setRelativePath(remoteFile1.getName());
        pathInfo.setDirectory(false);
        File fileInCache = new File(workSpace, CACHE_FOLDER + "/"
                + DATA_SET_CODE + "/" + pathInfo.getRelativePath());
        fileInCache.getParentFile().mkdirs();
        FileUtilities.writeToFile(fileInCache, FILE1_CONTENT);
        prepareRequestPersistence(2);
        
        ContentCache cache = createCache();
        InputStream inputStream = cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
        
        assertEquals(FILE1_CONTENT, readContent(inputStream, true));
        assertDataSetInfos(DATA_SET_CODE, 1, 1, 1000);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetInputStreamsForTwoFilesNotInCacheAndReadThemAtTheSameTime() throws IOException
    {
        final DataSetPathInfo pathInfo1 = prepareForDownloading(remoteFile1);
        final DataSetPathInfo pathInfo2 = prepareForDownloading(remoteFile2);
        prepareRequestPersistence(4);
        ContentCache cache = createCache();
        File downloadingFolder = new File(workSpace, DOWNLOADING_FOLDER);
        assertEquals(false, downloadingFolder.exists());
        
        InputStream inputStream1 =
                cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);
        assertEquals(1, new File(workSpace, DOWNLOADING_FOLDER).list().length);
        InputStream inputStream2 =
                cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo2);
        assertEquals(2, downloadingFolder.list().length);

        byte[] bytes1 = new byte[100];
        byte[] bytes2 = new byte[100];
        assertEquals(11, inputStream1.read(bytes1, 0, 11));
        assertEquals(11, inputStream2.read(bytes2, 0, 11));
        assertEquals(3, inputStream1.read(bytes1, 11, 100 - 11));
        assertEquals(3, inputStream2.read(bytes2, 11, 100 - 11));
        inputStream1.close();
        assertEquals(1, new File(workSpace, DOWNLOADING_FOLDER).list().length);
        inputStream2.close();
        assertEquals(0, new File(workSpace, DOWNLOADING_FOLDER).list().length);
        
        assertEquals(FILE1_CONTENT, new String(bytes1, 0, FILE1_CONTENT.length()));
        assertEquals(FILE2_CONTENT, new String(bytes2, 0, FILE2_CONTENT.length()));
        assertEquals(
                FILE1_CONTENT,
                FileUtilities.loadToString(
                        cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1)).trim());
        assertEquals(
                FILE2_CONTENT,
                FileUtilities.loadToString(
                        cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo2)).trim());

        context.assertIsSatisfied();
    }

    @Test(invocationCount = 1, invocationTimeOut = 10000)
    public void testGetInputStreamForSameContentInTwoThreads()
    {
        final DataSetPathInfo pathInfo1 = prepareForDownloading(remoteFile1);
        ConsoleLogger logger = new ConsoleLogger();
        final MessageChannel channel1 =
                new MessageChannelBuilder(10000).name("1").logger(logger).getChannel();
        final MessageChannel channel2 =
                new MessageChannelBuilder(10000).name("2").logger(logger).getChannel();
        final MessageChannel channel3 =
                new MessageChannelBuilder(10000).name("3").logger(logger).getChannel();
        final ContentCache cache = createCache();
        File fileInCache =
                new File(workSpace, CACHE_FOLDER + "/" + DATA_SET_CODE + "/"
                        + remoteFile1.getName());
        assertEquals(false, fileInCache.exists());
        prepareRequestPersistence(3);

        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    InputStream inputStream =
                            cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);
                    channel1.send(STARTED_MESSAGE);
                    channel2.assertNextMessage(STARTED_MESSAGE);
                    channel1.send(readContent(inputStream, true));
                }
            }, "thread1").start();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    channel1.assertNextMessage(STARTED_MESSAGE);
                    channel2.send(STARTED_MESSAGE);
                    InputStream inputStream =
                            cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);
                    channel1.assertNextMessage(FILE1_CONTENT);
                    channel2.send(readContent(inputStream, true));
                    channel3.send(FINISHED_MESSAGE);
                }
            }, "thread2").start();

        channel3.assertNextMessage(FINISHED_MESSAGE);
        channel2.assertNextMessage(FILE1_CONTENT);

        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(fileInCache).trim());
        File fileFromCache = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);
        assertEquals(fileInCache.getAbsolutePath(), fileFromCache.getAbsolutePath());
        context.assertIsSatisfied();
    }

    @Test(invocationCount = 1, invocationTimeOut = 10000)
    public void testGetFileAndGetInputStreamForSameContentInTwoThreads() throws Exception
    {
        final DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setRelativePath(remoteFile1.getName());
        pathInfo.setDirectory(false);
        ConsoleLogger logger = new ConsoleLogger();
        final MessageChannel channel1 =
                new MessageChannelBuilder(10000).name("11").logger(logger).getChannel();
        final MessageChannel channel2 =
                new MessageChannelBuilder(10000).name("12").logger(logger).getChannel();
        final MessageChannel channel3 =
                new MessageChannelBuilder(10000).name("13").logger(logger).getChannel();
        final ContentCache cache = createCache();
        context.checking(new Expectations()
            {
                {
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo.getRelativePath());
                    will(new ProxyAction(returnValue(remoteFile1.toURI().toURL().toString()))
                        {
                            @Override
                            protected void doBeforeReturn()
                            {
                                channel1.send(STARTED_MESSAGE);
                                channel2.assertNextMessage(STARTED_MESSAGE);
                            }
                        });
                }
            });
        prepareRequestPersistence(3);
        
        File fileInCache =
                new File(workSpace, CACHE_FOLDER + "/" + DATA_SET_CODE + "/"
                        + remoteFile1.getName());
        assertEquals(false, fileInCache.exists());

        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    File file = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
                    channel1.send(FileUtilities.loadToString(file).trim());
                }
            }, "thread1").start();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    channel1.assertNextMessage(STARTED_MESSAGE);
                    channel2.send(STARTED_MESSAGE);
                    InputStream inputStream =
                            cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
                    channel1.assertNextMessage(FILE1_CONTENT);
                    channel2.send(readContent(inputStream, true));
                    channel3.send(FINISHED_MESSAGE);
                }
            }, "thread2").start();

        channel3.assertNextMessage(FINISHED_MESSAGE);
        channel2.assertNextMessage(FILE1_CONTENT);

        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(fileInCache).trim());
        File fileFromCache = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
        assertEquals(fileInCache.getAbsolutePath(), fileFromCache.getAbsolutePath());
        context.assertIsSatisfied();
    }

    @Test(invocationCount = 1, invocationTimeOut = 10000)
    public void testGetInputStreamGetFileForSameContentInTwoThreads() throws Exception
    {
        final DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setRelativePath(remoteFile1.getName());
        pathInfo.setDirectory(false);
        ConsoleLogger logger = new ConsoleLogger();
        final MessageChannel channel1 =
                new MessageChannelBuilder(10000).name("21").logger(logger).getChannel();
        final MessageChannel channel2 =
                new MessageChannelBuilder(10000).name("22").logger(logger).getChannel();
        final MessageChannel channel3 =
                new MessageChannelBuilder(10000).name("3").logger(logger).getChannel();
        final ContentCache cache = createCache();
        context.checking(new Expectations()
            {
                {
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo.getRelativePath());
                    will(new ProxyAction(returnValue(remoteFile1.toURI().toURL().toString()))
                        {
                            @Override
                            protected void doBeforeReturn()
                            {
                                channel1.send(STARTED_MESSAGE);
                                channel2.assertNextMessage(STARTED_MESSAGE);
                            }
                        });
                }
            });
        prepareRequestPersistence(3);
        File fileInCache =
                new File(workSpace, CACHE_FOLDER + "/" + DATA_SET_CODE + "/"
                        + remoteFile1.getName());
        assertEquals(false, fileInCache.exists());

        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    InputStream inputStream =
                            cache.getInputStream(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
                    channel1.send(readContent(inputStream, true));
                }
            }, "thread1").start();
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    channel1.assertNextMessage(STARTED_MESSAGE);
                    channel2.send(STARTED_MESSAGE);
                    File file = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
                    channel1.assertNextMessage(FILE1_CONTENT);
                    channel2.send(FileUtilities.loadToString(file).trim());
                    channel3.send(FINISHED_MESSAGE);
                }
            }, "thread2").start();

        channel3.assertNextMessage(FINISHED_MESSAGE);
        channel2.assertNextMessage(FILE1_CONTENT);

        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(fileInCache).trim());
        File fileFromCache = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo);
        assertEquals(fileInCache.getAbsolutePath(), fileFromCache.getAbsolutePath());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testCacheMaintenance()
    {
        File dataSetFolder1 = new File(workSpace, CACHE_FOLDER + "/" + DATA_SET_CODE1);
        File file1 = new File(dataSetFolder1, "abc.txt");
        file1.getParentFile().mkdirs();
        FileUtilities.writeToFile(file1, createStringOfSize(8 * FileUtils.ONE_KB));
        dataSetFolder1.setLastModified(1000);
        File dataSetFolder2 = new File(workSpace, CACHE_FOLDER + "/" + DATA_SET_CODE2);
        File file2 = new File(dataSetFolder2, "abc2.txt");
        file2.getParentFile().mkdirs();
        FileUtilities.writeToFile(file2, createStringOfSize(12 * FileUtils.ONE_KB));
        dataSetFolder2.setLastModified(2000);
        DataSetPathInfo pathInfo1 = prepareForDownloading(remoteFile1);
        prepareRequestPersistence(2);
        prepareForRemoving(dataSetFolder1, true);
        ContentCache cache = createCache(19 * FileUtils.ONE_KB, 1000);
        timeProvider.getTimeInMilliseconds(); // next timestamp for the new file will be 61000

        File file = cache.getFile(SESSION_TOKEN, DATA_SET_LOCATION, pathInfo1);

        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(file).trim());
        assertEquals(createFirstLogLine() + createRecreatedlogLine(DATA_SET_CODE1)
                + createRecreatedlogLine(DATA_SET_CODE2) + createSizeLogLine(20, 2)
                + "Cached files for data set " + DATA_SET_CODE1 + " have been removed.",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    private String createFirstLogLine()
    {
        return "Content cache created. Workspace: " + workSpace.getAbsolutePath() + "\n";
    }
    
    private String createRecreatedlogLine(String dataSetCode)
    {
        return "Data set info recreated for data set " + dataSetCode + ".\n";
    }

    private String createSizeLogLine(int totalFileSize, int numberOfDataSets)
    {
        int size = totalFileSize;
        if (OSUtilities.isMacOS() == false)
        {
            size += numberOfDataSets * 4;
        }
        return "Content cache initialized. It contains "
                + (size == 0 ? "0 bytes" : size + ".00 KB") + " from " + numberOfDataSets
                + " data sets.\n";
    }
    
    private String createStringOfSize(long size)
    {
        StringBuilder builder = new StringBuilder();
        for (long i = 0; i < size; i++)
        {
            builder.append(Long.toHexString(i & 0xf));
        }
        return builder.toString();
    }
    
    private void assertDataSetInfos(String dataSetCode, int expectedNumberOfSmallFiles, int expectedNumberOfFolders,
            long expectedLastModified)
    {
        long expectedSize = expectedNumberOfSmallFiles;
        if (OSUtilities.isMacOS() == false)
        {
            expectedSize += expectedNumberOfFolders;
        }
        assertDataSetInfos(dataSetCode, expectedSize * 4096, expectedLastModified);
    }
    
    private void assertDataSetInfos(String dataSetCode, long expectedSize,
            long expectedLastModified)
    {
        DataSetInfo dataSetInfo = dataSetInfos.get(dataSetCode);
        assertEquals(expectedLastModified, dataSetInfo.lastModified);
        assertEquals(expectedSize, dataSetInfo.size);
    }

    private String readContent(InputStream inputStream, boolean closeStream)
    {
        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, outputStream);
            return outputStream.toString();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (closeStream)
            {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }
    
    private void prepareForRemoving(final File dataSetFolder, final boolean success)
    {
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).removeRecursivelyQueueing(dataSetFolder);
                    will(returnValue(success));
                }
            });
    }

    private DataSetPathInfo prepareForDownloading(final File remoteFile)
    {
        final DataSetPathInfo pathInfo = new DataSetPathInfo();
        pathInfo.setRelativePath(remoteFile.getName());
        pathInfo.setDirectory(false);
        context.checking(new Expectations()
            {
                {
                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo.getRelativePath());
                    try
                    {
                        will(returnValue(remoteFile.toURI().toURL().toString()));
                    } catch (MalformedURLException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            });
        return pathInfo;
    }

}
