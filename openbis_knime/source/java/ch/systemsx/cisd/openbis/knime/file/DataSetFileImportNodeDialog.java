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

package ch.systemsx.cisd.openbis.knime.file;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.knime.common.AbstractDescriptionBasedNodeDialog;
import ch.systemsx.cisd.openbis.knime.common.Util;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetFileImportNodeDialog extends AbstractDescriptionBasedNodeDialog<ReportDescription>
{
    private static boolean isBlank(String text)
    {
        return text == null || text.trim().length() == 0;
    }

    private JTextComponent dataSetCodeField;
    private JTextComponent filePathField;
    private JTextComponent downloadsPathField;

    DataSetFileImportNodeDialog()
    {
        super("Data Set File Importer Settings");
    }

    @Override
    protected void defineQueryForm(JPanel queryPanel, JComboBox reportComboBox)
    {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        dataSetCodeField = createTextFieldWithButton("Data Set Code", new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooseDataSet(createFacade());
                }
            }, panel);
        filePathField = createTextFieldWithButton("File", new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooseDataSetFile();
                }
            }, panel);
        downloadsPathField =
                createTextFieldWithButton("Location of Downloads", new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            chooseTempFolder();
                        }
                    }, panel);
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(panel, BorderLayout.NORTH);
        queryPanel.add(northPanel, BorderLayout.CENTER);
    }
    
    private JTextComponent createTextFieldWithButton(String label, ActionListener actionListener,
            JPanel panel)
    {
        JPanel textFieldWithButton = new JPanel(new BorderLayout());
        JTextField field = new JTextField();
        textFieldWithButton.add(field, BorderLayout.CENTER);
        JButton button = new JButton("...");
        button.addActionListener(actionListener);
        textFieldWithButton.add(button, BorderLayout.EAST);
        addField(panel, label, textFieldWithButton);
        return field;
    }

    @Override
    protected List<ReportDescription> getSortedDescriptions(IQueryApiFacade facade)
    {
        List<ReportDescription> descriptions = facade.listTableReportDescriptions();
        Collections.sort(descriptions, new Comparator<ReportDescription>()
            {
                public int compare(ReportDescription o1, ReportDescription o2)
                {
                    return o1.getLabel().compareTo(o2.getLabel());
                }
            });
        return descriptions;
    }
    
    @Override
    protected String getDescriptionKey()
    {
        return "";
    }

    @Override
    protected void loadMoreSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
        dataSetCodeField.setText(settings.getString(DataSetFileImportNodeModel.DATA_SET_CODE_KEY,
                ""));
        filePathField.setText(settings.getString(DataSetFileImportNodeModel.FILE_PATH_KEY, ""));
        downloadsPathField.setText(settings.getString(
                DataSetFileImportNodeModel.DOWNLOADS_PATH_KEY, ""));
    }

    @Override
    protected void saveMoreSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException
    {
        String dataSetCode = dataSetCodeField.getText();
        if (isBlank(dataSetCode))
        {
            throw new InvalidSettingsException("Data set code hasn't been specified.");
        }
        settings.addString(DataSetFileImportNodeModel.DATA_SET_CODE_KEY, dataSetCode);
        String filePath = filePathField.getText();
        if (isBlank(filePath))
        {
            throw new InvalidSettingsException("Data set file path hasn't been specified.");
        }
        settings.addString(DataSetFileImportNodeModel.FILE_PATH_KEY, filePath);
        String downloadsPath = downloadsPathField.getText();
        if (isBlank(downloadsPath))
        {
            throw new InvalidSettingsException("Location of downloads hasn't been specified.");
        }
        settings.addString(DataSetFileImportNodeModel.DOWNLOADS_PATH_KEY, downloadsPath);
    }

    private void chooseDataSet(IQueryApiFacade facade)
    {
        try
        {
            List<DataSet> dataSets = loadDataSets(facade);
            List<DataSet> selectedDataSets = Util.getSelectedDataSets(getPanel(), dataSets, true);
            if (selectedDataSets.isEmpty() == false)
            {
                String code = selectedDataSets.get(0).getCode();
                if (code.equals(dataSetCodeField.getText()) == false)
                {
                    dataSetCodeField.setText(code);
                    filePathField.setText("");
                }
            }
        } catch (Exception ex)
        {
            showException(ex);
        }
    }
    
    private void chooseDataSetFile()
    {
        String dataSetCode = dataSetCodeField.getText();
        if (isBlank(dataSetCode))
        {
            return;
        }
        String url = urlField.getText();
        String userID = userField.getText();
        String password = new String(passwordField.getPassword());

        try
        {
            ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet dataSet =
                    DataSetUtil.getDataSetProxy(url, userID, password, dataSetCode);
            if (dataSet == null)
            {
                JOptionPane.showMessageDialog(getPanel(), "Unknown data set: " + dataSetCode);
                return;
            }
            FileInfoDssDTO[] files = dataSet.listFiles("", true);
            FileChooser fileChooser = new FileChooser(dataSetCode, files);
            JOptionPane.showMessageDialog(getPanel(), fileChooser);
            FileInfoDssDTO fileInfo = fileChooser.getSelectedFileInfoOrNull();
            if (fileInfo != null && fileInfo.getPathInListing() != null
                    && fileInfo.isDirectory() == false)
            {
                filePathField.setText(fileInfo.getPathInDataSet());
            } else
            {
                JOptionPane.showMessageDialog(getPanel(),
                        "Either no file or a directory has been chosen");
            }
        } catch (RuntimeException e)
        {
            showException(e);
            throw e;
        } catch (Throwable t)
        {
            showException(t);
        }
    }

    private void chooseTempFolder()
    {
        JFileChooser chooser = new JFileChooser();
        String path = downloadsPathField.getText();
        if (isBlank(path) == false)
        {
            chooser.setSelectedFile(new File(path));
        }
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.showDialog(getPanel(), "Select");
        File selectedFile = chooser.getSelectedFile();
        if (selectedFile != null)
        {
            downloadsPathField.setText(selectedFile.getPath());
        }
    }
    
    private List<DataSet> loadDataSets(IQueryApiFacade facade)
    {
        IGeneralInformationService service = facade.getGeneralInformationService();
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);
        List<DataSet> dataSets = service.searchForDataSets(facade.getSessionToken(), searchCriteria);
        return dataSets;
    }

}
