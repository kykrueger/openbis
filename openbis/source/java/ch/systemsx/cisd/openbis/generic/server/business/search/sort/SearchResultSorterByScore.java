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
package ch.systemsx.cisd.openbis.generic.server.business.search.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;

/**
 * This Class sorts the given entity list trying to match the code, properties values and typeCode in a generic manner assigning different scores.
 * 
 * @author pkupczyk
 * @author juanf
 */
public class SearchResultSorterByScore implements ISearchResultSorter
{

    @Override
    public void sort(List<? extends IEntitySearchResult> entitiesToSort, DetailedSearchCriteria criteria)
    {
        // 0. Pre check that the results can be sorted
        if (criteria == null || criteria.getCriteria() == null || criteria.getCriteria().isEmpty())
        {
            return;
        }

        // 1. Get terms to use as input for the main algorithm
        List<Pattern> partialMatchTerms = new ArrayList<Pattern>();
        List<String> exactMatchTerms = new ArrayList<String>();
        List<Boost> boosts = new ArrayList<Boost>();
        for (DetailedSearchCriterion criterion : criteria.getCriteria())
        {
            partialMatchTerms.add(getPartialMatchTerm(criterion.getValue()));
            exactMatchTerms.add(getExactMatchTerm(criterion.getValue()));

            switch (criterion.getField().getKind())
            {
                case ANY_FIELD:
                    boosts.add(new Boost(1, 1, 1, 1, null));
                    break;
                case ANY_PROPERTY:
                    boosts.add(new Boost(0, 0, 1, 1, null));
                    break;
                case PROPERTY:
                    boosts.add(new Boost(0, 0, 0, 1, criterion.getField().getPropertyCode()));
                    break;
                case ATTRIBUTE:
                    if (criterion.getField().getAttributeCode().equalsIgnoreCase("code"))
                    {
                        boosts.add(new Boost(1, 0, 0, 0, null)); // Attribute code
                    } else if ( // TODO FIX hard coded types, will be clever to not naming the same thing differently internally to avoid this.
                    criterion.getField().getAttributeCode().equalsIgnoreCase("sample_type")
                            || criterion.getField().getAttributeCode().equalsIgnoreCase("data_set_type")
                            || criterion.getField().getAttributeCode().equalsIgnoreCase("material_type")
                            || criterion.getField().getAttributeCode().equalsIgnoreCase("experiment_type"))
                    {
                        boosts.add(new Boost(0, 1, 0, 0, null)); // Attribute type code
                    } else
                    {
                        boosts.add(new Boost(1, 1, 1, 1, null)); // Other attributes not supported, default to general case
                    }
                    break;
                case REGISTRATOR:
                    boosts.add(new Boost(1, 1, 1, 1, null)); // Registrator not supported, default to general case
                    break;
            }
        }

        // 2. Main algorithm
        final Map<IEntitySearchResult, Integer> scores = new HashMap<IEntitySearchResult, Integer>();

        for (IEntitySearchResult entity : entitiesToSort)
        {
            int score = getScore(entity, partialMatchTerms, exactMatchTerms, boosts);
            scores.put(entity, score);
        }

        // 3. Sort
        Collections.sort(entitiesToSort, new Comparator<IEntitySearchResult>()
            {
                @Override
                public int compare(IEntitySearchResult o1, IEntitySearchResult o2)
                {
                    Integer score1 = scores.get(o1);
                    Integer score2 = scores.get(o2);
                    int result = -score1.compareTo(score2);
                    if (result == 0)
                    {
                        return o1.getCode().compareTo(o2.getCode());
                    } else
                    {
                        return result;
                    }
                }
            });
    }

    private int getScore(IEntitySearchResult entity, List<Pattern> partialMatchTerms, List<String> exactMatchTerms, List<Boost> boosts)
    {
        int score = 0;
        for (int i = 0; i < exactMatchTerms.size(); i++)
        {
            Pattern partialTerm = partialMatchTerms.get(i);
            String exactTerm = exactMatchTerms.get(i);
            Boost boost = boosts.get(i);

            // 1. Code
            if (isPartialMatch(entity.getCode(), partialTerm))
            { // If code matches partially
                score += 100000 * boost.getCodeBoost();
                if (isExactMatch(entity.getCode(), exactTerm))
                { // If code matches exactly
                    score += 1000000 * boost.getCodeBoost();
                }
            }

            // 2. Entity type code
            if (isExactMatch(entity.getTypeCode(), exactTerm))
            { // If type matches exactly
                score += 1000 * boost.getTypeCodeBoost();
            }

            // 3. Properties
            if (entity.getProperties() != null && entity.getProperties().keySet() != null)
            {
                for (String propertykey : entity.getProperties().keySet())
                {
                    String propertyValue = entity.getProperties().get(propertykey);
                    if (isPartialMatch(propertyValue, partialTerm))
                    { // If property matches partially
                        score += 100 * boost.getPropertyBoost(propertykey);
                        if (isExactMatch(propertyValue, exactTerm))
                        { // If property matches exactly
                            score += 10000 * boost.getPropertyBoost(propertykey);
                        }
                    }
                }
            }
        }
        // For development System.out.println(entity.getCode() + " " + score);
        return score;
    }

    //
    // Helper Methods
    //
    private static class Boost
    {
        private int codeBoost;

        private int typeCodeBoost;

        private int propertyBoost;

        private int propertyDefaultBoost;

        private String propertyName;

        public Boost(int codeBoost, int typeCodeBoost, int propertyDefaultBoost, int propertyBoost, String propertyName)
        {
            super();
            this.codeBoost = codeBoost;
            this.typeCodeBoost = typeCodeBoost;
            this.propertyDefaultBoost = propertyDefaultBoost;
            this.propertyBoost = propertyBoost;
            this.propertyName = propertyName;
        }

        public int getCodeBoost()
        {
            return codeBoost;
        }

        public int getTypeCodeBoost()
        {
            return typeCodeBoost;
        }

        public int getPropertyBoost(String propertyNameToBoost)
        {
            if (this.propertyName != null && this.propertyName.equals(propertyNameToBoost))
            {
                return propertyBoost;
            } else
            {
                return propertyDefaultBoost;
            }
        }

    }

    public Pattern getPartialMatchTerm(String term)
    {
        return Pattern.compile(("*" + term + "*").replace("*", ".*").replace("?", ".?"), Pattern.CASE_INSENSITIVE);
    }

    public String getExactMatchTerm(String term)
    {
        return term.replace("*", "").replace("?", "");
    }

    public boolean isExactMatch(String value, String term)
    {
        if (value != null && term != null)
        {
            return value.equalsIgnoreCase(term);
        } else
        {
            return false;
        }
    }

    public boolean isPartialMatch(String value, Pattern pattern)
    {
        if (value != null && pattern != null)
        {
            return pattern.matcher(value).matches();
        } else
        {
            return false;
        }
    }
}
