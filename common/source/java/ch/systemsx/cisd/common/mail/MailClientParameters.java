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

package ch.systemsx.cisd.common.mail;

import java.io.Serializable;

/**
 * @author Franz-Josef Elmer
 */
public class MailClientParameters implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String from;

    private String smtpUser;

    private String smtpPassword;

    private String smtpHost;

    private String testAddress;

    public final String getFrom()
    {
        return from;
    }

    public final void setFrom(String from)
    {
        this.from = from;
    }

    public final String getSmtpUser()
    {
        return smtpUser;
    }

    public final void setSmtpUser(String smtpUser)
    {
        this.smtpUser = smtpUser;
    }

    public final String getSmtpPassword()
    {
        return smtpPassword;
    }

    public final void setSmtpPassword(String smtpPassword)
    {
        this.smtpPassword = smtpPassword;
    }

    public final String getSmtpHost()
    {
        return smtpHost;
    }

    public final void setSmtpHost(String smtpHost)
    {
        this.smtpHost = smtpHost;
    }

    public String getTestAddress()
    {
        return testAddress;
    }

    public void setTestAddress(String testAddress)
    {
        this.testAddress = testAddress;
    }
}
