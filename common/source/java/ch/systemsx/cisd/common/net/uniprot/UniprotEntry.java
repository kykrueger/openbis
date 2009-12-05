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

package ch.systemsx.cisd.common.net.uniprot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.utilities.DateFormatThreadLocal;

/**
 * A data transfer object for a Uniprot database entry.
 *
 * @author Bernd Rinn
 */
public class UniprotEntry
{
    /** The Uniprot date format pattern. */
    static final String UNIPROT_DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            new DateFormatThreadLocal(UNIPROT_DATE_FORMAT_PATTERN);

    private String citation;
    
    private String comments;
    
    private String database;

    private String domains;
    
    private String domain;
    
    private String ec;
    
    private String id;
    
    private String entryName;
    
    private String existence;
    
    private String families;
    
    private String features;
    
    private String genes;
    
    private String go;
    
    private String goId;
    
    private String interpro;
    
    private String interactor;
    
    private String keywords;
    
    private Date lastModified;
    
    private Integer length;
    
    private String organism;
    
    private String organismId;
    
    private String pathway;
    
    private String proteinNames;
    
    private String status;
    
    private String score;
    
    private String sequence;
    
    private String threeD;
    
    private String subcellularLocations;
    
    private String taxon;
    
    private Integer version;
    
    private String virusHosts;

    public String getCitation()
    {
        return citation;
    }

    void setCitation(String citation)
    {
        this.citation = citation;
    }

    public String getComments()
    {
        return comments;
    }

    void setComments(String comments)
    {
        this.comments = comments;
    }

    public String getDatabase()
    {
        return database;
    }

    void setDatabase(String database)
    {
        this.database = database;
    }

    public String getDomains()
    {
        return domains;
    }

    void setDomains(String domains)
    {
        this.domains = domains;
    }

    public String getDomain()
    {
        return domain;
    }

    void setDomain(String domain)
    {
        this.domain = domain;
    }

    public String getEc()
    {
        return ec;
    }

    void setEc(String ec)
    {
        this.ec = ec;
    }

    public String getId()
    {
        return id;
    }

    void setId(String id)
    {
        this.id = id;
    }

    public String getEntryName()
    {
        return entryName;
    }

    void setEntryName(String entryName)
    {
        this.entryName = entryName;
    }

    public String getExistence()
    {
        return existence;
    }

    void setExistence(String existence)
    {
        this.existence = existence;
    }

    public String getFamilies()
    {
        return families;
    }

    void setFamilies(String families)
    {
        this.families = families;
    }

    public String getFeatures()
    {
        return features;
    }

    void setFeatures(String features)
    {
        this.features = features;
    }

    public String getGenes()
    {
        return genes;
    }

    void setGenes(String genes)
    {
        this.genes = genes;
    }

    public String getGo()
    {
        return go;
    }

    void setGo(String go)
    {
        this.go = go;
    }

    public String getGoId()
    {
        return goId;
    }

    void setGoId(String goId)
    {
        this.goId = goId;
    }

    public String getInterpro()
    {
        return interpro;
    }

    void setInterpro(String interpro)
    {
        this.interpro = interpro;
    }

    public String getInteractor()
    {
        return interactor;
    }

    void setInteractor(String interactor)
    {
        this.interactor = interactor;
    }

    public String getKeywords()
    {
        return keywords;
    }

    void setKeywords(String keywords)
    {
        this.keywords = keywords;
    }

    public Date getLastModified()
    {
        return lastModified;
    }

    public String getLastModifiedStr()
    {
        return DATE_FORMAT.get().format(lastModified);
    }

    void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }

    public Integer getLength()
    {
        return length;
    }

    void setLength(Integer length)
    {
        this.length = length;
    }

    public String getOrganism()
    {
        return organism;
    }

    void setOrganism(String organism)
    {
        this.organism = organism;
    }

    public String getOrganismId()
    {
        return organismId;
    }

    void setOrganismId(String organismId)
    {
        this.organismId = organismId;
    }

    public String getPathway()
    {
        return pathway;
    }

    void setPathway(String pathway)
    {
        this.pathway = pathway;
    }

    public String getProteinNames()
    {
        return proteinNames;
    }

    void setProteinNames(String proteinNames)
    {
        this.proteinNames = proteinNames;
    }

    public String getStatus()
    {
        return status;
    }

    void setStatus(String status)
    {
        this.status = status;
    }

    public String getScore()
    {
        return score;
    }

    void setScore(String score)
    {
        this.score = score;
    }

    public String getSequence()
    {
        return sequence;
    }

    void setSequence(String sequence)
    {
        this.sequence = sequence;
    }

    public String getThreeD()
    {
        return threeD;
    }

    void setThreeD(String threeD)
    {
        this.threeD = threeD;
    }

    public String getSubcellularLocations()
    {
        return subcellularLocations;
    }

    void setSubcellularLocations(String subcellularLocations)
    {
        this.subcellularLocations = subcellularLocations;
    }

    public String getTaxon()
    {
        return taxon;
    }

    void setTaxon(String taxon)
    {
        this.taxon = taxon;
    }

    public Integer getVersion()
    {
        return version;
    }

    void setVersion(Integer version)
    {
        this.version = version;
    }

    public String getVirusHosts()
    {
        return virusHosts;
    }

    void setVirusHosts(String virusHosts)
    {
        this.virusHosts = virusHosts;
    }
    
    void set(UniprotColumn column, String value)
    {
        switch (column)
        {
            case CITATION:
                setCitation(value);
                break;
            case COMMENTS:
                setComments(value);
                break;
            case DATABASE:
                setDatabase(value);
                break;
            case DOMAIN:
                setDomain(value);
                break;
            case DOMAINS:
                setDomains(value);
                break;
            case EC:
                setEc(value);
                break;
            case ENTRY_NAME:
                setEntryName(value);
                break;
            case EXISTENCE:
                setExistence(value);
                break;
            case FAMILIES:
                setFamilies(value);
                break;
            case FEATURES:
                setFeatures(value);
                break;
            case GENES:
                setGenes(value);
                break;
            case GO:
                setGo(value);
                break;
            case GO_ID:
                setGoId(value);
                break;
            case ID:
                setId(value);
                break;
            case INTERACTOR:
                setInteractor(value);
                break;
            case INTERPRO:
                setInterpro(value);
                break;
            case KEYWORDS:
                setKeywords(value);
                break;
            case LAST_MODIFIED:
                if (StringUtils.isNotBlank(value))
                {
                    try
                    {
                        setLastModified(DATE_FORMAT.get().parse(value));
                    } catch (ParseException ex)
                    {
                        throw new ParserException("Error parsing date: " + value, ex);
                    }
                }
                break;
            case LENGTH:
                if (StringUtils.isNotBlank(value))
                {
                    setLength(Integer.parseInt(value));
                }
                break;
            case ORGANISM:
                setOrganism(value);
                break;
            case ORGANISM_ID:
                setOrganismId(value);
                break;
            case PATHWAY:
                setPathway(value);
                break;
            case PROTEIN_NAMES:
                setProteinNames(value);
                break;
            case SCORE:
                setScore(value);
                break;
            case SEQUENCE:
                setSequence(value);
                break;
            case STATUS:
                setStatus(value);
                break;
            case SUBCELLULAR_LOCATIONS:
                setSubcellularLocations(value);
                break;
            case TAXON:
                setTaxon(value);
                break;
            case THREED:
                setThreeD(value);
                break;
            case VERSION:
                if (StringUtils.isNotBlank(value))
                {
                    setVersion(Integer.parseInt(value));
                }
                break;
            case VIRUS_HOSTS:
                setVirusHosts(value);
                break;
            
        }
    }
    
}
