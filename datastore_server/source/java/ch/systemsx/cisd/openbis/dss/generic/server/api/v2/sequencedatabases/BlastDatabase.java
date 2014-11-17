/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v2.sequencedatabases;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.fasta.FastaUtilities;
import ch.systemsx.cisd.common.fasta.SequenceType;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.etlserver.plugins.BlastDatabaseCreationMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchDomainService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.BlastUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityPropertySearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;

/**
 * Implementation of {@link ISearchDomainService} based on <a href="http://blast.ncbi.nlm.nih.gov/Blast.cgi">BLAST</a>. 
 * The following configuration parameters are understood:
 * <ul>
 * <li><tt>blast-tools-directory</tt>: Absolute path to the directory with blastn and blastp command line tools.<br/>
 * <li><tt>blast-databases-folder</tt>: Folder with the BLAST databases as generated by {@link BlastDatabaseCreationMaintenanceTask}.
 * </ul>
 * By analyzing the specified sequence snippet either command line tool 'blastn' (nucleoid sequence) 
 * or 'blastp' (amoniacid sequence) is invoked. 
 * <p>
 * The following optional parameters are understood:
 * <p>
 * blastn: task, evalue, word_size, ungapped
 * <p>
 * blastp: task, evalue, word_size
 * <p>
 * Note:
 * <ul>
 * <li>For flag-like options the option value has to be an empty string.
 * <li>The default value for 'task' is 'blastn'/'blastp'. 
 * <li>Name space of optional parameters: In order to differenciate between blastn and blastp parameters the prefix
 * 'blastn.' and 'blasp.' can be used. In addition the name of the sequence database (as sepcified by the
 * configuration parameter 'label') can be used to distinguish properties for this instance from other instances.
 * <p>
 * Examples of understood optional properties assuming the name of the database reads 'My BLAST db':
 * <p>
 * blastn example:
 * <pre>
 * task
 * ungapped
 * blastn.word_size
 * My Blast db.evalue
 * My Blast db.blastn.task
 * </pre>
 * <p>
 * blastp example:
 * <pre>
 * task
 * blastp.task
 * My Blast db.evalue
 * My Blast db.blastp.word_size
 * </pre>
 * </ul>
 * 
 * @author Franz-Josef Elmer
 */
public class BlastDatabase extends AbstractSearchDomainService
{
    public static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BlastDatabase.class);
    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, BlastDatabase.class);
    private static final String QUERY_FILE_NAME_TEMPLATE = "query-{0,date,yyyyMMDDHHmmssSSS}-{1}.fasta";
    private static final Pattern STITLE_PATTERN = Pattern.compile("(.*) \\[Data set: (.*), File: (.*)\\]$"); 
    private static final Pattern ENTITY_PROPERTY_TITLE_PATTERN 
            = Pattern.compile("^(MATERIAL|EXPERIMENT|SAMPLE|DATA_SET)\\+(.+)\\+([A-Z0-9_\\-.]+)\\+(\\d+)$");
    
    private static final String[] BLASTN_OPTIONS = {"task", "evalue", "word_size", "ungapped"};
    private static final String[] BLASTP_OPTIONS = {"task", "evalue", "word_size"};

    private final File databaseFolder;
    private final String blastn;
    private final String blastp;
    private final boolean available;
    private final File queriesFolder;
    
    private AtomicInteger counter = new AtomicInteger();

    public BlastDatabase(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        String blastToolDirectory = BlastUtils.getBLASTToolDirectory(properties);
        blastn = blastToolDirectory + "blastn";
        blastp = blastToolDirectory + "blastp";
        available = process(blastn, "-version");
        if (available == false)
        {
            BlastUtils.logMissingTools(operationLog);
        }
        databaseFolder = BlastUtils.getBlastDatabaseFolder(properties, storeRoot);
        queriesFolder = new File(databaseFolder, "queries-folder");
        queriesFolder.mkdirs();
    }

    @Override
    public boolean isAvailable()
    {
        return available;
    }
    
    @Override
    public List<SearchDomainSearchResult> search(String sequenceSnippet, Map<String, String> optionalParametersOrNull)
    {
        Map<String, String> parameters = new HashMap<String, String>();
        if (optionalParametersOrNull != null)
        {
            parameters.putAll(optionalParametersOrNull);
        }
        List<SearchDomainSearchResult> result = new ArrayList<SearchDomainSearchResult>();
        SequenceType sequenceType = FastaUtilities.determineSequenceType(sequenceSnippet);
        String queryFileName = new MessageFormat(QUERY_FILE_NAME_TEMPLATE).format(
                new Object[] {new Date(), counter.getAndIncrement()});
        File queryFile = new File(queriesFolder, queryFileName);
        try
        {
            FileUtilities.writeToFile(queryFile, ">query\n" + sequenceSnippet + "\n");
            List<String> command = createCommand(sequenceType, queryFile, parameters);
            List<String> output = processAndDeliverOutput(command);
            for (String line : output)
            {
                String[] row = line.split("\t");
                Matcher matcher = STITLE_PATTERN.matcher(row[0]);
                if (matcher.matches())
                {
                    SearchDomainSearchResult sequenceSearchResult = new SearchDomainSearchResult();
                    DataSetFileSearchResultLocation resultLocation = new DataSetFileSearchResultLocation();
                    resultLocation.setIdentifier(matcher.group(1));
                    resultLocation.setDataSetCode(matcher.group(2));
                    resultLocation.setPathInDataSet(matcher.group(3));
                    resultLocation.setPosition(parse(row[1]));
                    sequenceSearchResult.setResultLocation(resultLocation);
                    result.add(sequenceSearchResult);
                } else
                {
                    matcher = ENTITY_PROPERTY_TITLE_PATTERN.matcher(row[0]);
                    if (matcher.matches())
                    {
                        SearchDomainSearchResult sequenceSearchResult = new SearchDomainSearchResult();
                        EntityPropertySearchResultLocation resultLocation = new EntityPropertySearchResultLocation();
                        resultLocation.setEntityKind(EntityKind.valueOf(matcher.group(1)));
                        resultLocation.setPermId(matcher.group(2));
                        resultLocation.setPropertyType(matcher.group(3));
                        resultLocation.setPosition(parse(row[1]));
                        sequenceSearchResult.setResultLocation(resultLocation);
                        result.add(sequenceSearchResult);
                    }
                }
            }
        } finally
        {
            FileUtilities.delete(queryFile);
        }
        return result;
    }
    

    private int parse(String number)
    {
        try
        {
            return Integer.parseInt(number);
        } catch (NumberFormatException ex)
        {
            return -1;
        }
    }

    private List<String> createCommand(SequenceType sequenceType, File queryFile, Map<String, String> parameters)
    {
        List<String> command = new ArrayList<String>();
        String[] options;
        String defaultTask;
        if (sequenceType == SequenceType.NUCL)
        {
            command.add(blastn);
            defaultTask = "blastn";
            options = BLASTN_OPTIONS;
        } else
        {
            command.add(blastp);
            defaultTask = "blastp";
            options = BLASTP_OPTIONS;
        }
        command.add("-db");
        command.add(databaseFolder.getAbsolutePath() + "/all-" + sequenceType.toString().toLowerCase());
        command.add("-query");
        command.add(queryFile.getAbsolutePath());
        command.add("-outfmt");
        command.add("6 stitle sstart");
        if (parameters.containsKey("task") == false)
        {
            parameters.put("task", defaultTask);
        }
        for (String option : options)
        {
            String value = tryGetOption(option, sequenceType, parameters);
            if (value != null)
            {
                command.add("-" + option);
                if (StringUtils.isNotBlank(value))
                {
                    command.add(value);
                }
            }
        }
        return command;
    }
    
    private String tryGetOption(String option, SequenceType sequenceType, Map<String, String> parameters)
    {
        String value = parameters.get(option);
        if (value != null)
        {
            return value;
        }
        String prefixedOption = (sequenceType == SequenceType.NUCL ? "blastn." : "blastp.") + option;
        value = parameters.get(prefixedOption); 
        if (value != null)
        {
            return value;
        }
        value = parameters.get(name + "." + option);
        if (value != null)
        {
            return value;
        }
        return parameters.get(name + "." + prefixedOption);
    }
    
    private boolean process(String... command)
    {
        ProcessResult processResult = run(Arrays.asList(command));
        if (processResult.isOK())
        {
            processResult.logAsInfo();
        } else
        {
            processResult.log();
        }
        return processResult.isOK();
    }

    private List<String> processAndDeliverOutput(List<String> command)
    {
        ProcessResult processResult = run(command);
        List<String> output = processResult.getOutput();
        if (processResult.isOK())
        {
            return output;
        }
        StringBuilder builder = new StringBuilder("Execution failed: ").append(command);
        List<String> lines = processResult.isBinaryOutput() ? processResult.getErrorOutput() : output;
        for (String line : lines)
        {
            builder.append("\n").append(line);
        }
        processResult.log();
        throw new EnvironmentFailureException(builder.toString()); 
    }
    
    @Private ProcessResult run(List<String> command)
    {
        return ProcessExecutionHelper.run(command, operationLog, machineLog);
    }
}
