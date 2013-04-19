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

package ch.systemsx.cisd.openbis.jstest;

/**
 * @author pkupczyk
 */
public class JsTestDataStoreServer extends TestDataStoreServer
{

    private String name;

    private String rootPath;

    private int debugPort;

    public JsTestDataStoreServer(String name, String rootPath, int debugPort)
    {
        this.name = name;
        this.rootPath = rootPath;
        this.debugPort = debugPort;
    }

    @Override
    protected String getName()
    {
        return name;
    }

    @Override
    protected String getRootPath()
    {
        return rootPath;
    }

    @Override
    protected String getCommand()
    {
        return "java -ea -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address="
                + debugPort
                + " -Dpython.path=../../../libraries/jython/jython-lib -Dfile.encoding=UTF-8 -classpath ./etc:../../../screening/targets/www/WEB-INF/classes:../../../rtd_phosphonetx/targets/www/WEB-INF/classes:../../../datastore_server/targets/classes:../../../common/targets/classes:../../../libraries/log4j/log4j.jar:../../../libraries/testng/testng-jdk15.jar:../../../libraries/commons-lang/commons-lang.jar:../../../libraries/commons-io/commons-io.jar:../../../libraries/mail/mail.jar:../../../libraries/jmock/jmock.jar:../../../libraries/activation/activation.jar:../../../libraries/commons-codec/commons-codec.jar:../../../libraries/restrictionchecker/restrictions.jar:../../../libraries/cglib/cglib-nodep.jar:../../../libraries/jmock/hamcrest/hamcrest-core.jar:../../../libraries/jmock/objenesis/objenesis-1.0.jar:../../../libraries/jmock/hamcrest/hamcrest-library.jar:../../../libraries/unix:../../../libraries/cisd-base/cisd-base.jar:../../../libraries/fast-md5/fast-md5.jar:../../../libraries/jython/jython.jar:../../../libraries/commons-httpclient/commons-httpclient.jar:../../../libraries/commons-logging/commons-logging.jar:../../../libraries/jaxb/jaxb-api.jar:../../../libraries/jaxb/jsr173_1.0_api.jar:../../../libraries/jaxb/jaxb-impl.jar:../../../libraries/classycle/classycle.jar:../../../libraries/commons-collections/commons-collections.jar:../../../libraries/spring/spring-core.jar:../../../libraries/spring/spring-context.jar:../../../libraries/spring/spring-aop.jar:../../../libraries/spring/third-party/aopalliance.jar:../../../libraries/cisd-args4j/cisd-args4j.jar:../../../libraries/jetty8/lib/server/servlet-api-3.0.jar:../../../libraries/spring/spring.jar:../../../libraries/spring/webmvc/spring-webmvc.jar:../../../libraries/gwt2.4/gwt-servlet.jar:../../../libraries/spring/third-party/stream-supporting-httpinvoker.jar:../../../libraries/cisd-jhdf5/hdf5-linux-intel.jar:../../../libraries/cisd-jhdf5/hdf5-macosx-intel.jar:../../../libraries/cisd-jhdf5/hdf5-windows-intel.jar:../../../libraries/poi/poi-ooxml-schemas.jar:../../../libraries/poi/poi-ooxml.jar:../../../libraries/poi/poi.jar:../../../openbis/targets/www/WEB-INF/classes:../../../authentication/targets/classes:../../../libraries/jmock/jmock-legacy.jar:../../../libraries/jline/jline.jar:../../../openbis-common/targets/classes:../../../libraries/hibernate-search/jms.jar:../../../libraries/eodsql/eodsql.jar:../../../libraries/hibernate-search/jsr250-api.jar:../../../libraries/spring/spring-beans.jar:../../../libraries/spring/spring-web.jar:../../../libraries/spring/spring-jdbc.jar:../../../libraries/hibernate-core/hibernate-core.jar:../../../libraries/jetty8/lib/common/jetty-http.jar:../../../libraries/jetty8/lib/common/jetty-io.jar:../../../libraries/jetty8/lib/common/jetty-util.jar:../../../libraries/jetty8/lib/server/jetty-continuation.jar:../../../libraries/jetty8/lib/server/jetty-security.jar:../../../libraries/jetty8/lib/server/jetty-server.jar:../../../libraries/jetty8/lib/server/jetty-servlet.jar:../../../libraries/jmock/hamcrest/hamcrest-integration.jar:../../../libraries/dom4j/dom4j.jar:../../../libraries/javassist/javassist.jar:../../../libraries/reflections/lib/gson-1.4.jar:../../../libraries/reflections/lib/guava-r08.jar:../../../libraries/reflections/lib/jboss-vfs-3.0.0.CR5.jar:../../../libraries/reflections/lib/xml-apis-1.0.b2.jar:../../../libraries/reflections/reflections.jar:../../../libraries/slf4j/slf4j.jar:../../../libraries/jackson/jackson-annotations.jar:../../../libraries/jackson/jackson-core.jar:../../../libraries/jackson/jackson-databind.jar:../../../libraries/jsonrpc4j/jsonrpc4j.jar:../../../libraries/cisd-jhdf5/cisd-jhdf5-core.jar:../../../libraries/cisd-jhdf5/cisd-jhdf5-tools.jar:../../../openbis_api/targets/classes:../../../dbmigration/targets/classes:../../../libraries/commons-dbcp/commons-dbcp.jar:../../../libraries/postgresql/postgresql.jar:../../../libraries/h2/h2.jar:../../../libraries/commons-pool/commons-pool.jar:../../../libraries/apgdiff/apgdiff.jar:../../../libraries/hibernate-validator/hibernate-validator.jar:../../../libraries/antlr/antlr.jar:../../../libraries/slf4j/log4j12/slf4j-log4j12.jar:../../../libraries/spring/test/spring-test.jar:../../../libraries/junit/junit.jar:../../../libraries/hibernate-search/hibernate-search.jar:../../../libraries/ehcache/ehcache.jar:../../../libraries/lucene/lucene-core.jar:../../../libraries/commons-fileupload/commons-fileupload.jar:../../../libraries/lucene-highlighter/lucene-highlighter.jar:../../../libraries/fastutil/fastutil.jar:../../../libraries/gwt-debug-panel/gwt-debug-panel.jar:../../../libraries/gwt-image-loader/gwt-image-loader.jar:../../../libraries/gwt2.4/gwt-user.jar:../../../libraries/gxt2.2.5/gxt.jar:../../../libraries/gwt2.4/validation-api-1.0.0.GA.jar:../../../libraries/gwt2.4/validation-api-1.0.0.GA-sources.jar:../../../libraries/hibernate-commons-annotations/hibernate-commons-annotations.jar:../../../libraries/jta/jta.jar:../../../libraries/hibernate-jpa-2.0-api/hibernate-jpa-2.0-api.jar:../../../libraries/validation-api/validation-api.jar:../../../libraries/jetty8/lib/server/jetty-deploy.jar:../../../libraries/jetty8/lib/server/jetty-webapp.jar:../../../libraries/csv/csv.jar:../../../libraries/imagej/ij.jar:../../../libraries/jfreechart/jcommon-1.0.16.jar:../../../libraries/jfreechart/jfreechart-1.0.13.jar:../../../libraries/jai/jai_codec.jar:../../../libraries/jai/jai_core.jar:../../../libraries/cifex/cifex.jar:../../../libraries/truezip/truezip.jar:../../../libraries/cisd-image_readers/cisd-image_readers-bioformats.jar:../../../libraries/cisd-image_readers/cisd-image_readers-jai.jar:../../../libraries/cisd-image_readers/cisd-image_readers.jar:../../../libraries/cisd-image_readers/cisd-image_readers-imagej.jar:../../../libraries/poi/ooxml-lib/dom4j-1.6.1.jar:../../../libraries/poi/ooxml-lib/geronimo-stax-api_1.0_spec-1.0.jar:../../../libraries/poi/ooxml-lib/xmlbeans-2.3.0.jar:../../../libraries/ftpserver/ftpserver-core.jar:../../../libraries/mina/mina-core.jar:../../../libraries/pngj/pngj.jar:../../../libraries/gwt2.4/gwt-isserializable.jar:../../../libraries/sshd/sshd-core.jar:etc/log.xml:../../../libraries/cisd-hotdeploy/cisd-hotdeploy.jar ch.systemsx.cisd.openbis.dss.generic.DataStoreServer";
    }

    @Override
    protected String getLinuxCommand()
    {
        return "java -ea -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address="
                + debugPort
                + " -Dpython.path=../../../libraries/jython/jython-lib -Dfile.encoding=UTF-8 -classpath ./etc:../../../screening/targets/ant/www/WEB-INF/classes:../../../rtd_phosphonetx/targets/ant/www/WEB-INF/classes:../../../datastore_server/targets/ant/classes:../../../common/targets/ant/classes:../../../libraries/log4j/log4j.jar:../../../libraries/testng/testng-jdk15.jar:../../../libraries/commons-lang/commons-lang.jar:../../../libraries/commons-io/commons-io.jar:../../../libraries/mail/mail.jar:../../../libraries/jmock/jmock.jar:../../../libraries/activation/activation.jar:../../../libraries/commons-codec/commons-codec.jar:../../../libraries/restrictionchecker/restrictions.jar:../../../libraries/cglib/cglib-nodep.jar:../../../libraries/jmock/hamcrest/hamcrest-core.jar:../../../libraries/jmock/objenesis/objenesis-1.0.jar:../../../libraries/jmock/hamcrest/hamcrest-library.jar:../../../libraries/unix:../../../libraries/cisd-base/cisd-base.jar:../../../libraries/fast-md5/fast-md5.jar:../../../libraries/jython/jython.jar:../../../libraries/commons-httpclient/commons-httpclient.jar:../../../libraries/commons-logging/commons-logging.jar:../../../libraries/jaxb/jaxb-api.jar:../../../libraries/jaxb/jsr173_1.0_api.jar:../../../libraries/jaxb/jaxb-impl.jar:../../../libraries/classycle/classycle.jar:../../../libraries/commons-collections/commons-collections.jar:../../../libraries/spring/spring-core.jar:../../../libraries/spring/spring-context.jar:../../../libraries/spring/spring-aop.jar:../../../libraries/spring/third-party/aopalliance.jar:../../../libraries/cisd-args4j/cisd-args4j.jar:../../../libraries/jetty8/lib/server/servlet-api-3.0.jar:../../../libraries/spring/spring.jar:../../../libraries/spring/webmvc/spring-webmvc.jar:../../../libraries/gwt2.4/gwt-servlet.jar:../../../libraries/spring/third-party/stream-supporting-httpinvoker.jar:../../../libraries/cisd-jhdf5/hdf5-linux-intel.jar:../../../libraries/cisd-jhdf5/hdf5-macosx-intel.jar:../../../libraries/cisd-jhdf5/hdf5-windows-intel.jar:../../../libraries/poi/poi-ooxml-schemas.jar:../../../libraries/poi/poi-ooxml.jar:../../../libraries/poi/poi.jar:../../../openbis/targets/www/WEB-INF/classes:../../../authentication/targets/ant/classes:../../../libraries/jmock/jmock-legacy.jar:../../../libraries/jline/jline.jar:../../../openbis-common/targets/ant/classes:../../../libraries/hibernate-search/jms.jar:../../../libraries/eodsql/eodsql.jar:../../../libraries/hibernate-search/jsr250-api.jar:../../../libraries/spring/spring-beans.jar:../../../libraries/spring/spring-web.jar:../../../libraries/spring/spring-jdbc.jar:../../../libraries/hibernate-core/hibernate-core.jar:../../../libraries/jetty8/lib/common/jetty-http.jar:../../../libraries/jetty8/lib/common/jetty-io.jar:../../../libraries/jetty8/lib/common/jetty-util.jar:../../../libraries/jetty8/lib/server/jetty-continuation.jar:../../../libraries/jetty8/lib/server/jetty-security.jar:../../../libraries/jetty8/lib/server/jetty-server.jar:../../../libraries/jetty8/lib/server/jetty-servlet.jar:../../../libraries/jmock/hamcrest/hamcrest-integration.jar:../../../libraries/dom4j/dom4j.jar:../../../libraries/javassist/javassist.jar:../../../libraries/reflections/lib/gson-1.4.jar:../../../libraries/reflections/lib/guava-r08.jar:../../../libraries/reflections/lib/jboss-vfs-3.0.0.CR5.jar:../../../libraries/reflections/lib/xml-apis-1.0.b2.jar:../../../libraries/reflections/reflections.jar:../../../libraries/slf4j/slf4j.jar:../../../libraries/jackson/jackson-annotations.jar:../../../libraries/jackson/jackson-core.jar:../../../libraries/jackson/jackson-databind.jar:../../../libraries/jsonrpc4j/jsonrpc4j.jar:../../../libraries/cisd-jhdf5/cisd-jhdf5-core.jar:../../../libraries/cisd-jhdf5/cisd-jhdf5-tools.jar:../../../openbis_api/targets/ant/classes:../../../dbmigration/targets/ant/classes:../../../libraries/commons-dbcp/commons-dbcp.jar:../../../libraries/postgresql/postgresql.jar:../../../libraries/h2/h2.jar:../../../libraries/commons-pool/commons-pool.jar:../../../libraries/apgdiff/apgdiff.jar:../../../libraries/hibernate-validator/hibernate-validator.jar:../../../libraries/antlr/antlr.jar:../../../libraries/slf4j/log4j12/slf4j-log4j12.jar:../../../libraries/spring/test/spring-test.jar:../../../libraries/junit/junit.jar:../../../libraries/hibernate-search/hibernate-search.jar:../../../libraries/ehcache/ehcache.jar:../../../libraries/lucene/lucene-core.jar:../../../libraries/commons-fileupload/commons-fileupload.jar:../../../libraries/lucene-highlighter/lucene-highlighter.jar:../../../libraries/fastutil/fastutil.jar:../../../libraries/gwt-debug-panel/gwt-debug-panel.jar:../../../libraries/gwt-image-loader/gwt-image-loader.jar:../../../libraries/gwt2.4/gwt-user.jar:../../../libraries/gxt2.2.5/gxt.jar:../../../libraries/gwt2.4/validation-api-1.0.0.GA.jar:../../../libraries/gwt2.4/validation-api-1.0.0.GA-sources.jar:../../../libraries/hibernate-commons-annotations/hibernate-commons-annotations.jar:../../../libraries/jta/jta.jar:../../../libraries/hibernate-jpa-2.0-api/hibernate-jpa-2.0-api.jar:../../../libraries/validation-api/validation-api.jar:../../../libraries/jetty8/lib/server/jetty-deploy.jar:../../../libraries/jetty8/lib/server/jetty-webapp.jar:../../../libraries/csv/csv.jar:../../../libraries/imagej/ij.jar:../../../libraries/jfreechart/jcommon-1.0.16.jar:../../../libraries/jfreechart/jfreechart-1.0.13.jar:../../../libraries/jai/jai_codec.jar:../../../libraries/jai/jai_core.jar:../../../libraries/cifex/cifex.jar:../../../libraries/truezip/truezip.jar:../../../libraries/cisd-image_readers/cisd-image_readers-bioformats.jar:../../../libraries/cisd-image_readers/cisd-image_readers-jai.jar:../../../libraries/cisd-image_readers/cisd-image_readers.jar:../../../libraries/cisd-image_readers/cisd-image_readers-imagej.jar:../../../libraries/poi/ooxml-lib/dom4j-1.6.1.jar:../../../libraries/poi/ooxml-lib/geronimo-stax-api_1.0_spec-1.0.jar:../../../libraries/poi/ooxml-lib/xmlbeans-2.3.0.jar:../../../libraries/ftpserver/ftpserver-core.jar:../../../libraries/mina/mina-core.jar:../../../libraries/pngj/pngj.jar:../../../libraries/gwt2.4/gwt-isserializable.jar:../../../libraries/sshd/sshd-core.jar:etc/log.xml:../../../libraries/cisd-hotdeploy/cisd-hotdeploy.jar ch.systemsx.cisd.openbis.dss.generic.DataStoreServer";
    }

    @Override
    protected String getDatabaseDumpFolderPathOrNull()
    {
        return rootPath + "/db";
    }

}
