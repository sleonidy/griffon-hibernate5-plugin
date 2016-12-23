/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.hibernate5;

import griffon.core.Configuration;
import griffon.core.addon.GriffonAddon;
import griffon.core.injection.Module;
import griffon.inject.DependsOn;
import griffon.plugins.hibernate5.Hibernate5Factory;
import griffon.plugins.hibernate5.Hibernate5Handler;
import griffon.plugins.hibernate5.Hibernate5Storage;
import org.codehaus.griffon.runtime.core.injection.AbstractModule;
import org.codehaus.griffon.runtime.util.ResourceBundleProvider;
import org.kordamp.jipsy.ServiceProviderFor;

import javax.inject.Named;
import java.util.ResourceBundle;

import static griffon.util.AnnotationUtils.named;

/**
 * @author Andres Almiray
 */
@DependsOn("datasource")
@Named("hibernate5")
@ServiceProviderFor(Module.class)
public class Hibernate5Module extends AbstractModule {
    @Override
    protected void doConfigure() {
        // tag::bindings[]
        bind(ResourceBundle.class)
            .withClassifier(named("hibernate5"))
            .toProvider(new ResourceBundleProvider("Hibernate5"))
            .asSingleton();

        bind(Configuration.class)
            .withClassifier(named("hibernate5"))
            .to(DefaultHibernate5Configuration.class)
            .asSingleton();

        bind(Hibernate5Storage.class)
            .to(DefaultHibernate5Storage.class)
            .asSingleton();

        bind(Hibernate5Factory.class)
            .to(DefaultHibernate5Factory.class)
            .asSingleton();

        bind(Hibernate5Handler.class)
            .to(DefaultHibernate5Handler.class)
            .asSingleton();

        bind(GriffonAddon.class)
            .to(Hibernate5Addon.class)
            .asSingleton();
        // end::bindings[]
    }
}
