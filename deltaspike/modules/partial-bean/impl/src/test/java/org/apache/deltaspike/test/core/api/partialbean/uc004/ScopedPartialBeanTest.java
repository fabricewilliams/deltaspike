/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.test.core.api.partialbean.uc004;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.test.core.api.partialbean.shared.TestPartialBeanBinding;
import org.apache.deltaspike.test.core.api.partialbean.util.ArchiveUtils;
import org.apache.deltaspike.test.utils.CdiContainerUnderTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ScopedPartialBeanTest
{
    @Deployment
    public static WebArchive war()
    {
        String simpleName = ScopedPartialBeanTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
                .addPackage(ScopedPartialBeanTest.class.getPackage())
                .addPackage(TestPartialBeanBinding.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        WebArchive webArchive =  ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndPartialBeanArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        if (CdiContainerUnderTest.is("owb-.*")   ||
            CdiContainerUnderTest.is("tomee-.*") ||
            CdiContainerUnderTest.is("wildfly-.*") ||
            CdiContainerUnderTest.is("glassfish-.*"))
        {
            JavaArchive javassistJar = ShrinkWrap.create(JavaArchive.class, "dsjavassist.jar")
                    .addPackages(true, "javassist");
            webArchive.addAsLibrary(javassistJar);
        }

        return webArchive;
    }

    @Test
    public void testPartialBeanWithApplicationScope() throws Exception
    {
        String result = BeanProvider.getContextualReference(ApplicationScopedPartialBean.class).getResult();

        Assert.assertEquals("partial-test-false", result);

        int count = BeanProvider.getContextualReference(ApplicationScopedPartialBean.class).getManualResult();
        Assert.assertEquals(0, count);

        count = BeanProvider.getContextualReference(ApplicationScopedPartialBean.class).getManualResult();
        Assert.assertEquals(1, count);
    }

    @Test
    public void testPartialBeanWithDependentScope() throws Exception
    {
        String result = BeanProvider.getContextualReference(DependentScopedPartialBean.class).getResult();

        Assert.assertEquals("partial-test-false", result);

        int count = BeanProvider.getContextualReference(DependentScopedPartialBean.class).getManualResult();
        Assert.assertEquals(0, count);

        count = BeanProvider.getContextualReference(DependentScopedPartialBean.class).getManualResult();
        Assert.assertEquals(0, count);
    }
}
