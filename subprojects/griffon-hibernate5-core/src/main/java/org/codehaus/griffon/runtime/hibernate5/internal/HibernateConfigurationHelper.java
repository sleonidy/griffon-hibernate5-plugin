/*
 * Copyright 2014-2015 the original author or authors.
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
package org.codehaus.griffon.runtime.hibernate5.internal;

import griffon.core.GriffonApplication;
import griffon.plugins.hibernate5.Hibernate5Mapping;
import griffon.plugins.hibernate5.exceptions.RuntimeHibernate5Exception;
import griffon.util.ServiceLoaderUtils;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static griffon.util.ConfigUtils.*;
import static griffon.util.GriffonNameUtils.isBlank;

/**
 * Sets up a shared Hibernate SessionFactory.
 * Based on Spring's {@code org.springframework.orm.hibernate5.LocalSessionFactoryBean}
 * Original author: Juergen Hoeller (Spring 1.2)
 *
 * @author Andres Almiray
 */
public class HibernateConfigurationHelper {
    public static final String ENTITY_INTERCEPTOR = "entityInterceptor";
    public static final String NAMING_STRATEGY = "namingStrategy";
    public static final String PHYSICAL_NAMING_STRATEGY = "physicalNamingStrategy";
    public static final String IMPLICIT_NAMING_STRATEGY = "implicitNamingStrategy";
    public static final String PROPS = "props";
    public static final String MAP_CLASSES_PATTERN = "mapClassesPattern";
    public static final String CURRENT_SESSION_CONTEXT = "currentSessionContext";
    public static final String PROVIDER_CLASS = "provider_class";
    private static final Logger LOG = LoggerFactory.getLogger(HibernateConfigurationHelper.class);
    private static final String HBM_XML_SUFFIX = ".hbm.xml";

    private final Map<String, Object> sessionConfig;
    private final String dataSourceName;
    private final DataSource dataSource;
    private final GriffonApplication application;
    private final Map<String, String> dataSourceConfiguration;

    private static Map<String, String> groovyToHibernateConfigurationName;

    static {
        groovyToHibernateConfigurationName = new HashMap<>();
        groovyToHibernateConfigurationName.put("driverClassName", "hibernate.connection.driver_class");
        groovyToHibernateConfigurationName.put("username", "hibernate.connection.username");
        groovyToHibernateConfigurationName.put("password", "hibernate.connection.password");
        groovyToHibernateConfigurationName.put("url", "hibernate.connection.url");

    }

    public HibernateConfigurationHelper(GriffonApplication application, Map<String, Object> sessionConfig, String dataSourceName, DataSource dataSource, Map dataSources) {
        this.application = application;
        this.sessionConfig = sessionConfig;
        this.dataSourceName = dataSourceName;
        this.dataSource = dataSource;
        this.dataSourceConfiguration = (Map<String, String>) dataSources.get(dataSourceName);
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public Map<String, Object> getSessionConfig() {
        return sessionConfig;
    }

    public Configuration buildConfiguration() {
        // Create Configuration instance.
        Configuration config = newConfiguration();
        application.getInjector().injectMembers(this);
        applyEntityInterceptor(config);
        applyNamingStrategy(config);
        applyProperties(config);
        applyDialect(config);
        applyMappings(config);
        applySessionContext(config);
        applyProviderClassToHibernate(config);
        return config;
    }

    private void applyProviderClassToHibernate(Configuration config) {
        String providerClass = getConfigValueAsString(sessionConfig, PROVIDER_CLASS, null);
        if (providerClass!=null) {
            config.setProperty("hibernate.connection.provider_class", providerClass);
            // Copying all connection configuration from data source to hibernate
            for (Map.Entry<String, String> keyValueConfiguration : dataSourceConfiguration.entrySet()) {
                if (groovyToHibernateConfigurationName.containsKey(keyValueConfiguration.getKey()))
                    config.setProperty(groovyToHibernateConfigurationName.get(keyValueConfiguration.getKey()), keyValueConfiguration.getValue());
            }
        }


    }

    private void applySessionContext(Configuration config) {
        String sessionContext = getConfigValueAsString(sessionConfig, CURRENT_SESSION_CONTEXT, null);
        if (sessionContext != null)
            config.setProperty("hibernate.current_session_context_class", sessionContext);
    }


    private void applyEntityInterceptor(Configuration config) {
        Object entityInterceptor = getConfigValue(sessionConfig, ENTITY_INTERCEPTOR, null);
        if (entityInterceptor instanceof Class) {
            config.setInterceptor((Interceptor) newInstanceOf((Class) entityInterceptor));
        } else if (entityInterceptor instanceof String) {
            config.setInterceptor((Interceptor) newInstanceOf((String) entityInterceptor));
        }
    }

    private void applyNamingStrategy(Configuration config) {
        Object namingStrategy = getConfigValue(sessionConfig, NAMING_STRATEGY, null);
        if (namingStrategy instanceof Class) {
            config.setPhysicalNamingStrategy((PhysicalNamingStrategy) newInstanceOf((Class) namingStrategy));
        } else if (namingStrategy instanceof String) {
            config.setPhysicalNamingStrategy((PhysicalNamingStrategy) newInstanceOf((String) namingStrategy));
        }
        applyPhysicalNamingStrategy(config);
        applyImplicitNamingStrategy(config);
    }

    private void applyImplicitNamingStrategy(Configuration config) {
        Object namingStrategy = getConfigValue(sessionConfig, IMPLICIT_NAMING_STRATEGY, null);
        if (namingStrategy instanceof Class) {
            config.setImplicitNamingStrategy((ImplicitNamingStrategy) newInstanceOf((Class) namingStrategy));
        } else if (namingStrategy instanceof String) {
            config.setImplicitNamingStrategy((ImplicitNamingStrategy) newInstanceOf((String) namingStrategy));
        }
    }

    private void applyPhysicalNamingStrategy(Configuration config) {
        Object namingStrategy = getConfigValue(sessionConfig, PHYSICAL_NAMING_STRATEGY, null);
        if (namingStrategy instanceof Class) {
            config.setImplicitNamingStrategy((ImplicitNamingStrategy) newInstanceOf((Class) namingStrategy));
        } else if (namingStrategy instanceof String) {
            config.setImplicitNamingStrategy((ImplicitNamingStrategy) newInstanceOf((String) namingStrategy));
        }
    }

    private void applyProperties(Configuration config) {
        Object props = getConfigValue(sessionConfig, PROPS, null);
        if (props instanceof Properties) {
            config.setProperties((Properties) props);
        } else if (props instanceof Map) {
            for (Map.Entry<String, String> entry : ((Map<String, String>) props).entrySet()) {
                config.setProperty(entry.getKey(), entry.getValue());
            }
        }


        if (getConfigValueAsBoolean(sessionConfig, "logSql", false)) {
            config.setProperty("hibernate.show_sql", "true");
        }
        if (getConfigValueAsBoolean(sessionConfig, "formatSql", false)) {
            config.setProperty("hibernate.format_sql", "true");
        }

    }

    private void applyDialect(Configuration config) {
        Object dialect = getConfigValue(sessionConfig, "dialect", null);
        if (dialect instanceof Class) {
            config.setProperty("hibernate.dialect", ((Class) dialect).getName());
        } else if (dialect != null) {
            config.setProperty("hibernate.dialect", dialect.toString());
        } else {
            DialectDetector dialectDetector = new DialectDetector(dataSource);
            config.setProperty("hibernate.dialect", dialectDetector.getDialect());
        }
    }

    private boolean matchMapClassPattern(Object pattern, String line) {
        if (pattern instanceof Pattern)
            return ((Pattern) pattern).matcher(line).matches();
        else if (pattern instanceof String)
            return Pattern.compile((String) pattern).matcher(line).matches();
        return false;
    }

    private void addAnnotatedClass(final Configuration configuration, ClassLoader classLoader, String className) {
        LOG.debug("Registering as annotated class");
        try {
            Class<?> clazz = classLoader.loadClass(className);
            configuration.addAnnotatedClass(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeHibernate5Exception(dataSourceName, e);
        }
    }

    private void applyMappings(final Configuration config) {
        final Object mapClasses = getConfigValue(sessionConfig, MAP_CLASSES_PATTERN, Pattern.compile(".*"));
        ServiceLoaderUtils.load(application.getApplicationClassLoader().get(), "META-INF/types", Hibernate5Mapping.class, new ServiceLoaderUtils.LineProcessor() {
            @Override
            public void process(ClassLoader classLoader, Class<?> type, String line) {
                String originalName = line.trim();

                if (isBlank(originalName) || !matchMapClassPattern(mapClasses, originalName)) return;
                line = originalName.replace('.', '/');
                LOG.debug("Registering {} as hibernate resource", line + HBM_XML_SUFFIX);
                if (classLoader.getResource(line + HBM_XML_SUFFIX) != null)
                    config.addResource(line + HBM_XML_SUFFIX);
                else {
                    addAnnotatedClass(config, classLoader, originalName);
                }

            }
        });

        for (String mapping : getConfigValue(sessionConfig, "mappings", Collections.<String>emptyList())) {
            mapping = mapping.replace('.', '/');
            if (!mapping.endsWith(HBM_XML_SUFFIX)) {
                mapping = mapping + HBM_XML_SUFFIX;
            }
            LOG.debug("Registering {} as hibernate resource", mapping);
            config.addResource(mapping);
        }
    }

    private Object newInstanceOf(String className) {
        try {
            return newInstanceOf(Thread.currentThread().getContextClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot instantiate class " + className, e);
        }
    }

    private Object newInstanceOf(Class klass) {
        try {
            return klass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot instantiate " + klass, e);
        }
    }

    // -------------------------------------------------

    private Configuration newConfiguration() throws HibernateException {
        Configuration configuration = new Configuration();
        configuration.getProperties().put(Environment.DATASOURCE, dataSource);
        return configuration;
    }
}
