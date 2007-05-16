/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.crowd;

import java.text.MessageFormat;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * 
 *
 * @author felmer
 */
public class CrowdAuthenticationService implements IAuthenticationService
{
    private static final MessageFormat AUTHENTICATE_APPL =
        new MessageFormat("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + " <soap:Body>\n"
                + "   <authenticateApplication xmlns=\"urn:SecurityServer\">\n" + "     <in0>\n"
                + "       <credential xmlns=\"http://authentication.integration.crowd.atlassian.com\">\n"
                + "         <credential>{1}</credential>\n" + "       </credential>\n"
                + "       <name xmlns=\"http://authentication.integration.crowd.atlassian.com\">{0}</name>\n"
                + "       <validationFactors xmlns=\"http://authentication.integration.crowd.atlassian.com\" "
                + "                          xsi:nil=\"true\" />\n" + "     </in0>\n"
                + "   </authenticateApplication>\n" + " </soap:Body>\n" + "</soap:Envelope>\n");

    private static final MessageFormat AUTHENTICATE_USER =
        new MessageFormat(
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                        + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + " <soap:Body>\n"
                        + "   <authenticatePrincipal xmlns=\"urn:SecurityServer\">\n"
                        + "     <in0>\n"
                        + "       <name xmlns=\"http://authentication.integration.crowd.atlassian.com\">{0}</name>\n"
                        + "       <token xmlns=\"http://authentication.integration.crowd.atlassian.com\">{1}</token>\n"
                        + "     </in0>\n"
                        + "     <in1>\n"
                        + "       <application xmlns=\"http://authentication.integration.crowd.atlassian.com\">{0}</application>\n"
                        + "       <credential xmlns=\"http://authentication.integration.crowd.atlassian.com\">\n"
                        + "         <credential>{3}</credential>\n"
                        + "       </credential>\n"
                        + "       <name xmlns=\"http://authentication.integration.crowd.atlassian.com\">{2}</name>\n"
                        + "       <validationFactors xmlns=\"http://authentication.integration.crowd.atlassian.com\" />\n"
                        + "     </in1>\n" + "   </authenticatePrincipal>\n" + " </soap:Body>\n"
                        + "</soap:Envelope>\n");

    private final String url;
    private final String application;
    private final String applicationPassword;


    public CrowdAuthenticationService(String host, int port, String application, String applicationPassword)
    {
        this("https://" + host + ":" + port + "/crowd/services/SecurityServer", application, applicationPassword);
    }
    
    public CrowdAuthenticationService(String url, String application, String applicationPassword)
    {
        this.url = url;
        this.application = application;
        this.applicationPassword = applicationPassword;
    }
    
    public boolean authenticate(String user, String password)
    {
        String applicationToken = xmlEncode(execute("token", AUTHENTICATE_APPL, application, applicationPassword));
        String userToken = xmlEncode(execute("out", AUTHENTICATE_USER, application, applicationToken, user, password));
        return userToken != null;
    }

    private String execute(String responseElement, MessageFormat template, String... args)
    {
        Object[] decodedArguments = new Object[args.length];
        for (int i = 0; i < args.length; i++)
        {
            decodedArguments[i] = xmlDecode(args[i]);
        }
        String response = execute(template.format(decodedArguments));
        return pickElementContent(response, responseElement);
    }

    private String pickElementContent(String xmlString, String element)
    {
        int index = xmlString.indexOf("<" + element);
        if (index < 0)
        {
            return null;
        }
        index = xmlString.indexOf(">", index);
        if (index < 0)
        {
            return null;
        }
        int endIndex = xmlString.indexOf("</" + element, index);
        if (endIndex < 0)
        {
            return null;
        }
        return xmlString.substring(index + 1, endIndex);
    }

    private String execute(String xmlMessage)
    {
        try
        {
            HttpClient client = new HttpClient();
            PostMethod post = new PostMethod(url);
            StringRequestEntity entity = new StringRequestEntity(xmlMessage, "application/soap+xml", "utf-8");
            post.setRequestEntity(entity);
            String response = null;
            try
            {
                client.executeMethod(post);
                response = post.getResponseBodyAsString();
            } finally
            {
                post.releaseConnection();
            }
            return response;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private String xmlDecode(String text)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, n = text.length(); i < n; i++)
        {
            char c = text.charAt(i);
            switch (c)
            {
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '&':
                    builder.append("&amp;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                default:
                    builder.append(c);
                    break;
            }
        }
        return new String(builder);
    }

    private String xmlEncode(String xml)
    {
        // TODO implementation
        return xml;
    }

}
