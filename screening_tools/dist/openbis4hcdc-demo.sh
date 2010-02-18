#!/bin/bash
LIB=lib
CP=.:$LIB/openbis4hcdc.jar:$LIB/commons-logging.jar:$LIB/commons-httpclient.jar:$LIB/commons-codec.jar:$LIB/cisd-base.jar:$LIB/hibernate-annotations.jar:$LIB/screening.jar:$LIB/commons-io.jar:$LIB/hibernate-search.jar:$LIB/restrictions.jar:$LIB/commons-lang.jar:$LIB/hibernate-validator.jar:$LIB/spring.jar:$LIB/ejb3-persistence.jar:$LIB/hibernate3.jar:$LIB/gwt-isserializable.jar:$LIB/lucene-core.jar
java -cp $CP ch.systemsx.cisd.openbis.hcdc.OpenbisConnectionDemo