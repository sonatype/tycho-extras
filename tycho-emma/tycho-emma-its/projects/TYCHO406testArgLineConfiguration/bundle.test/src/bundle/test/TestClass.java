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
package bundle.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestClass {

    @Test
    public void simpleTest() {
        ClassUnderTest cut = new ClassUnderTest();
        assertEquals(42, cut.add(39,3));
    }
}
