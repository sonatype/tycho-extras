/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.tycho.test.TYCHO406testArgLineConfiguration;

import java.io.File;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.it.Verifier;
import org.eclipse.tycho.test.AbstractTychoIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

public class TestArgLineConfigurationTest extends AbstractTychoIntegrationTest {

    @Test
    public void testLocalMavenRepository() throws Exception {
        Verifier verifier = getVerifier("TYCHO406testArgLineConfiguration/eclemma", false);
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();

        verifier = getVerifier("TYCHO406testArgLineConfiguration", false);
        verifier.executeGoal("install");
        verifier.verifyErrorFreeLog();

        File emmaSessionFile = new File(verifier.getBasedir(), "bundle.test/target/coverage.es");
        Assert.assertTrue(emmaSessionFile.exists());

        File emmaXmlReport = new File(verifier.getBasedir(), "bundle.test/target/coverage.xml");
        Assert.assertTrue(emmaXmlReport.exists());

        Document coverageXml = XmlUtil.readFrom(emmaXmlReport);
        XPath xpath = XPathFactory.newInstance().newXPath();

        // the ClassUnderTest should be the only class in the report.
        Assert.assertEquals("1", xpath.evaluate("/report/stats/classes/@value", coverageXml));
        Assert.assertEquals("ClassUnderTest.java",
                xpath.evaluate("/report/data/all/package[@name='bundle.test']/srcfile/@name", coverageXml));
    }

}
