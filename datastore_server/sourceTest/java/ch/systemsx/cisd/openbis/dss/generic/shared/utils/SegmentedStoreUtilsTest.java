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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SegmentedStoreUtilsTest extends AbstractFileSystemTestCase
{
    private static final class MockLogger implements ISimpleLogger
    {
        private final StringBuilder builder = new StringBuilder();
        
        public void log(LogLevel level, String message)
        {
            builder.append(level).append(": ").append(message).append('\n');
        }

        @Override
        public String toString()
        {
            return builder.toString();
        }
    }
    
    private static final String DATA_STORE_CODE = "ds-code";
    
    private Mockery context;
    private IEncapsulatedOpenBISService service;
    private IShareIdManager shareIdManager;
    private ISimpleLogger log;
    private IFreeSpaceProvider freeSpaceProvider;
    private ITimeProvider timeProvider;

    private File store;
    
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        timeProvider = context.mock(ITimeProvider.class);
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));
                }
            });
        log = new MockLogger();
        store = new File(workingDirectory, "store");
        store.mkdirs();
        new File(store, "blabla").mkdirs();
        new File(store, "error").mkdirs();
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetDataSetsPerShare()
    {
        final File ds1File = new File(store, "1/uuid/01/02/03/ds-1");
        ds1File.mkdirs();
        FileUtilities.writeToFile(new File(ds1File, "read.me"), "nice work!");
        final SimpleDataSetInformationDTO ds1 = dataSet(ds1File, DATA_STORE_CODE, null);
        File ds2File = new File(store, "1/uuid/01/02/04/ds-2");
        ds2File.mkdirs();
        FileUtilities.writeToFile(new File(ds2File, "hello.txt"), "hello world");
        final SimpleDataSetInformationDTO ds2 = dataSet(ds2File, "blabla", null);
        File ds3File = new File(store, "2/uuid/01/05/04/ds-3");
        ds3File.mkdirs();
        FileUtilities.writeToFile(new File(ds3File, "hi.txt"), "hi everybody");
        final SimpleDataSetInformationDTO ds3 = dataSet(ds3File, DATA_STORE_CODE, 123456789L);
        File ds4File = new File(store, "1/uuid/0a/02/03/ds-4");
        ds4File.mkdirs();
        FileUtilities.writeToFile(new File(ds4File, "hello.data"), "hello data");
        final SimpleDataSetInformationDTO ds4 = dataSet(ds4File, DATA_STORE_CODE, 42L);
        final SimpleDataSetInformationDTO ds5 = new SimpleDataSetInformationDTO();
        ds5.setDataSetCode("ds5");
        ds5.setDataSetShareId("2");
        ds5.setDataSetLocation("blabla");
        ds5.setDataStoreCode(DATA_STORE_CODE);
        final RecordingMatcher<HostAwareFile> fileMatcher = new RecordingMatcher<HostAwareFile>();
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSets();
                    will(returnValue(Arrays.asList(ds1, ds2, ds3, ds4, ds5)));
                    
                    one(service).updateShareIdAndSize("ds-1", "1", 10L);
                    
                    try
                    {
                        one(freeSpaceProvider).freeSpaceKb(with(fileMatcher));
                        will(returnValue(12345L));
                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            });
        
        List<Share> shares =
                SegmentedStoreUtils.getDataSetsPerShare(store, DATA_STORE_CODE, freeSpaceProvider,
                        service, log, timeProvider);
        Share share1 = shares.get(0);
        long freeSpace = share1.calculateFreeSpace();
        
        assertEquals("INFO: Calculating size of " + ds1File + "\n" + "INFO: " + ds1File
                + " contains 10 bytes (calculated in 0 msec)\n"
                + "WARN: Data set ds5 no longer exists in share 2.\n", log.toString());
        assertEquals(new File(store, "1"), fileMatcher.recordedObject().getFile());
        assertEquals(12345L * 1024, freeSpace);
        assertEquals(new File(store, "1").toString(), share1.getShare().toString());
        assertEquals("1", share1.getShareId());
        assertSame(ds4, share1.getDataSetsOrderedBySize().get(0));
        assertEquals(42L, share1.getDataSetsOrderedBySize().get(0).getDataSetSize().longValue());
        assertSame(ds1, share1.getDataSetsOrderedBySize().get(1));
        assertEquals(10L, share1.getDataSetsOrderedBySize().get(1).getDataSetSize().longValue());
        assertEquals(2, share1.getDataSetsOrderedBySize().size());
        assertEquals(52L, share1.getTotalSizeOfDataSets());
        assertEquals(new File(store, "2").toString(), shares.get(1).getShare().toString());
        assertEquals("2", shares.get(1).getShareId());
        assertSame(ds3, shares.get(1).getDataSetsOrderedBySize().get(0));
        assertEquals(123456789L, shares.get(1).getDataSetsOrderedBySize().get(0).getDataSetSize().longValue());
        assertEquals(1, shares.get(1).getDataSetsOrderedBySize().size());
        assertEquals(123456789L, shares.get(1).getTotalSizeOfDataSets());
        assertEquals(2, shares.size());
        
        context.assertIsSatisfied();
    }

    @Test
    public void testMoveDataSetToAnotherShare()
    {
        File share1 = new File(workingDirectory, "store/1");
        File share1uuid01 = new File(share1, "uuid/01");
        File ds2 = new File(share1uuid01, "0b/0c/ds-2/original");
        ds2.mkdirs();
        FileUtilities.writeToFile(new File(ds2, "read.me"), "do nothing");
        File dataSetDirInStore = new File(share1uuid01, "02/03/ds-1");
        File original = new File(dataSetDirInStore, "original");
        original.mkdirs();
        FileUtilities.writeToFile(new File(original, "hello.txt"), "hello world");
        File share2 = new File(workingDirectory, "store/2");
        share2.mkdirs();
        File share2uuid01 = new File(share2, "uuid/01");
        File file = new File(share2uuid01, "22/33/orig");
        file.mkdirs();
        FileUtilities.writeToFile(new File(file, "hi.txt"), "hi");
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet("ds-1");
                    will(returnValue(new ExternalData()));
                    
                    one(service).updateShareIdAndSize("ds-1", "2", 11L);
                    one(shareIdManager).setShareId("ds-1", "2");
                }
            });
        assertEquals(true, dataSetDirInStore.exists());
        assertFileNames(share2uuid01, "22");
        
        SegmentedStoreUtils.moveDataSetToAnotherShare(dataSetDirInStore, share2, service, shareIdManager);

        assertEquals(false, dataSetDirInStore.exists());
        assertFileNames(share2uuid01, "02", "22");
        assertEquals("hello world\n",
                FileUtilities.loadToString(new File(share2uuid01, "02/03/ds-1/original/hello.txt")));
        context.assertIsSatisfied();
    }
    
    private void assertFileNames(File file, String... names)
    {
        File[] files = file.listFiles();
        Arrays.sort(files);
        List<String> actualNames = new ArrayList<String>();
        for (File child : files)
        {
            actualNames.add(child.getName());
        }
        assertEquals(Arrays.asList(names).toString(), actualNames.toString());
    }
    
    private SimpleDataSetInformationDTO dataSet(File dataSetFile, String dataStoreCode, Long size)
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(dataSetFile.getName());
        dataSet.setDataStoreCode(dataStoreCode);
        String path = FileUtilities.getRelativeFile(store, dataSetFile);
        int indexOfFirstSeparator = path.indexOf(File.separatorChar);
        dataSet.setDataSetShareId(path.substring(0, indexOfFirstSeparator));
        dataSet.setDataSetLocation(path.substring(indexOfFirstSeparator + 1));
        dataSet.setDataSetSize(size);
        return dataSet;
    }
}
