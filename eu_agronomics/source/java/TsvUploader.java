import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

/**
 * @author walshs
 */

class TsvUploader
{

    private final String incomingDir =
            new String("/localhome/openbis/sprint/datastore_server/data/incoming");

    private final int headRow;

    private final ArrayList<Integer> ignoreRow;

    private final ArrayList<Integer> keyCols = new ArrayList<Integer>();

    // SampleKeys are generated according to numeric specification supplied in keyCols
    private ArrayList<String> sampleKeys = new ArrayList<String>();

    private final String inFile;

    private final String tag;

    private final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

    private ArrayList<String> header;

    // directory for writing output files
    private String topLevel = new String();

    // CONSTRUCTER
    public TsvUploader(int h, ArrayList<Integer> i, String in, ArrayList<Integer> k, String t)
    {
        headRow = h;
        ignoreRow = i;
        inFile = in;
        tag = t;

        // change column specs from 1 based references to 0 based refs
        Iterator<Integer> iterator = k.iterator();
        while (iterator.hasNext())
        {
            Integer j = new Integer(iterator.next());
            j--;
            keyCols.add(j);
        }

        loadTsvFile();

        generateSampleKeys();

        topLevel =
                ("/tmp/Tsv2upload/" + tag + '/' + System.getProperty("user.name") + "/"
                        + System.currentTimeMillis() + "/");

        File top = new File(topLevel);
        top.mkdirs();

        System.out.println("Writing files to " + topLevel);

    }

    private void loadTsvFile()
    {
        // loads the tsv to memory
        // splits the data and header into seperate structures
        try
        {
            BufferedReader input = new BufferedReader(new FileReader(inFile));
            try
            {
                String line = null;

                Integer lineNum = 0;

                while ((line = input.readLine()) != null)
                {

                    lineNum++;
                    if (line.matches("^\\s+$"))
                    {
                        System.err.println("Skipping line " + lineNum + " as it contains no data");
                        // Weird bug with TSV generated by excel
                        // This line.match skips some empty lines because the println runs
                        // However, it doesn't match all empty lines as some get put into the "data"
                        // ArrayList
                        // Can see this when using the debugger
                        // It's difficult to inspect because the Mac command line tools such as tail
                        // and less
                        // don't recognise /r as a newline break. They display as ^M.
                        // However, the java readLine doesn't have a problem.
                        // Easy quick work around was to go into Excel and delete the blank row of
                        // cells
                        // so that they are truly empty

                    } else
                    {

                        ArrayList<String> dataLine = new ArrayList<String>();

                        Scanner tokenize = new Scanner(line).useDelimiter("\t");
                        while (tokenize.hasNext())
                        {
                            dataLine.add(tokenize.next());
                        }
                        if (lineNum == headRow)
                        {
                            header = dataLine;

                        } else if (ignoreRow.contains(lineNum))
                        {
                            // do nothing
                        } else
                        {
                            data.add(dataLine);
                        }
                    }

                }
            } finally
            {
                input.close();
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public String listSampleKeyColumns()
    {
        Iterator<Integer> iterator = keyCols.iterator();
        StringBuffer skcols = new StringBuffer();
        while (iterator.hasNext())
        {
            skcols.append("Key Col :" + header.get(iterator.next()) + "\n");
        }
        return skcols.toString();
    }

    // private void writeTopLevelDirs()
    // {
    //
    // Iterator<String> hid = header.iterator();
    // while (hid.hasNext())
    // {
    // String topDir = topLevel.concat(hid.next());
    // // System.out.println(topDir);
    // try
    // {
    // File d = new File(topDir);
    // boolean success = d.mkdirs();
    // if (success)
    // {
    // // System.out.println("Directory: " + topDir + " created");
    // } else
    // {
    // System.out.println("failed to make " + topDir);
    // }
    // } catch (SecurityException se)
    // {
    // System.err.println("Error: " + se.getMessage());
    // }
    // }
    // }

    private void generateSampleKeys()
    {

        // generate openBIS SAMPLE IDs
        Iterator<ArrayList<String>> di = data.iterator();
        String key2;
        ArrayList<String> sks = new ArrayList<String>();
        while (di.hasNext())
        {
            ArrayList<String> row = di.next();

            Iterator<Integer> kci = keyCols.iterator();
            StringBuffer key = new StringBuffer();
            while (kci.hasNext())
            {
                String value = row.get(kci.next());
                key.append(value + '_');
            }
            key2 = new String(StringUtils.chop(key.toString()));
            key2 = key2.toUpperCase();
            sks.add(key2);
        }

        sampleKeys = sks;
    }

    public String sampleKeysAsString()
    {
        return sampleKeys.toString();
    }

    public void writeSamplesFile()
    {

        // Batch upload of samples requires a knowledge of Sample type
        // property types
        //
        // It would be possible to supply arguments so that the file for
        // batch upload of Samples could be written
        // This would need a sample prefix
        //
        // i.e. sample_prefix = CISD:/AGRON-OMICS/
        //
        // and a structure to specify which columns of the data are sample
        // meta-data
        // 
        // e.g. sample_meta=[user.ecotype@1,user.color@4]
        //
        // for now I avoid this

        Iterator<String> si = sampleKeys.iterator();
        StringBuffer s = new StringBuffer("identifier\n");
        StringBuffer sl = new StringBuffer("");
        while (si.hasNext())
        {
            String sample = si.next();
            s.append("CISD:/AGRON-OMICS/" + sample + "\n");
            sl.append(sample + "\n");
        }

        File f = new File(topLevel + "SAMPLES_register_from_a_file_and_attach.txt");
        File f2 = new File(topLevel + "SAMPLES_specify_the_list_of_existing_samples.txt");

        try
        {
            BufferedWriter fOut = new BufferedWriter(new FileWriter(f));
            fOut.write(s.toString());
            fOut.close();

            BufferedWriter fOut2 = new BufferedWriter(new FileWriter(f2));
            fOut2.write(sl.toString());
            fOut2.close();

        } catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }

    }

    private String getConfigFromTemplate(String t)
    {

        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb
                .append("# ---------------------------------------------------------------------------\n");
        sb.append("# " + t + " thread configuration\n");
        sb
                .append("# ---------------------------------------------------------------------------\n");
        sb.append("# The directory to watch for incoming data.\n");
        sb.append(t + ".incoming-dir = data/incoming/" + t + "\n");
        sb.append(t + ".incoming-data-completeness-condition = auto-detection\n");
        sb.append(t + ".space-code = 'AGRON-OMICS'\n");
        sb.append("# ---------------- Plugin properties\n");
        sb
                .append(t
                        + ".data-set-info-extractor = ch.systemsx.cisd.etlserver.DefaultDataSetInfoExtractor\n");
        sb.append("# following should be set to $ further up the file \n");
        sb
                .append(t
                        + ".data-set-info-extractor.entity-separator = ${data-set-file-name-entity-separator}\n");
        sb.append(t + ".data-set-info-extractor.space-code = AGRON-OMICS\n");
        sb
                .append(t
                        + ".data-set-info-extractor.data-set-properties-file-name = data-set-properties.tsv\n");
        sb.append(t + ".type-extractor = ch.systemsx.cisd.etlserver.SimpleTypeExtractor\n");
        sb.append(t + ".type-extractor.file-format-type = TSV\n");
        sb.append(t + ".type-extractor.locator-type = RELATIVE_LOCATION\n");
        sb.append(t + ".type-extractor.data-set-type = " + t + "\n");
        sb.append(t + ".storage-processor = ch.systemsx.cisd.etlserver.DefaultStorageProcessor\n");
        return sb.toString();
    }

    public void writeDssMkdir()
    {

        StringBuffer mkdir = new StringBuffer("#!/bin/bash\n" + "mkdir -v ");

        Iterator<String> hid = header.iterator();
        while (hid.hasNext())
        {
            String head = hid.next();
            mkdir.append(incomingDir + "/" + head + " ");
        }
        mkdir.append("\n");
        File f = new File(topLevel + "make_incoming_dirs.sh");
        try
        {
            BufferedWriter fOut = new BufferedWriter(new FileWriter(f));
            fOut.write(mkdir.toString());
            fOut.close();

        } catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }

    }

    public void writeServiceDotProperties()
    {

        StringBuffer inputsLine = new StringBuffer("inputs=");
        StringBuffer config = new StringBuffer();
        Iterator<String> hid = header.iterator();
        while (hid.hasNext())
        {
            String head = hid.next();
            String t = getConfigFromTemplate(head);
            config.append(t);
            inputsLine.append(head + ",");
        }
        String sptext =
                new String(StringUtils.chop(inputsLine.toString()) + "\n\n" + config.toString()
                        + "\n\n");

        File f = new File(topLevel + "thread_config_service_properties.txt");
        try
        {
            BufferedWriter fOut = new BufferedWriter(new FileWriter(f));
            fOut.write(sptext);
            fOut.close();

        } catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void writeDataForDssUpload()
    {

        // writeTopLevelDirs();

        Iterator<ArrayList<String>> di = data.iterator();
        Iterator<String> sKey = sampleKeys.iterator();
        // each row of data
        System.err.println("Writing Files");
        while (di.hasNext())
        {
            System.err.print(".");
            ArrayList<String> row = di.next();
            String sample = sKey.next();
            // a cell of data
            Iterator<String> ci = row.iterator();
            Iterator<String> hi = header.iterator();
            while (ci.hasNext())
            {
                String cell = ci.next().toUpperCase();
                String head = hi.next();

                File path = new File(topLevel + "/DataSet/" + head + "/" + sample + "/");
                File ftsv = new File(path + "/data-set-properties.tsv");
                File fres = new File(path + "/result.txt");
                try
                {
                    path.mkdirs();
                    BufferedWriter ftsvOut = new BufferedWriter(new FileWriter(ftsv));
                    ftsvOut.write("property\tvalue\n");
                    if (cell.matches("\\S+"))
                    {
                        ftsvOut.write("USER." + head + "\t" + cell + "\n");
                    }
                    ftsvOut.close();

                    BufferedWriter fresOut = new BufferedWriter(new FileWriter(fres));
                    fresOut.write(cell + "\n");
                    fresOut.close();

                } catch (Exception e)
                {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
        System.err.println("\nDone Writing Files.");

    }
}