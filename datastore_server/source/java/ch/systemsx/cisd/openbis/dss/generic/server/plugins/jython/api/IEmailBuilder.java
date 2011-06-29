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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api;

/**
 * Builder of emails with attachments.
 * 
 * @author Piotr Buczek
 */
// NOTE: All methods of this interface are part of the Reporting and Processing Plugin API.
public interface IEmailBuilder
{
    IEmailBuilder withSubject(String subject);

    IEmailBuilder withBody(String bodyText);

    IEmailBuilder withAttachedFile(String attachmentFilePath, String attachmentName);

    IEmailBuilder withAttachedText(String attachmentText, String attachmentName);
}
