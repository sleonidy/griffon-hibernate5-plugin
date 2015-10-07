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
package org.codehaus.griffon.runtime.hibernate5;

import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.TypeHelper;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

import javax.annotation.Nonnull;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class SessionFactoryDecorator implements SessionFactory {
    private final SessionFactory delegate;

    public SessionFactoryDecorator(@Nonnull SessionFactory delegate) {
        this.delegate = requireNonNull(delegate, "Argument 'delegate' must not be null");
    }

    @Nonnull
    protected SessionFactory getDelegate() {
        return delegate;
    }

    @Override
    public SessionFactoryOptions getSessionFactoryOptions() {
        return delegate.getSessionFactoryOptions();
    }

    @Override
    public SessionBuilder withOptions() {
        return delegate.withOptions();
    }

    @Override
    public Session openSession() throws HibernateException {
        return delegate.openSession();
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        return delegate.getCurrentSession();
    }

    @Override
    public StatelessSessionBuilder withStatelessOptions() {
        return delegate.withStatelessOptions();
    }

    @Override
    public StatelessSession openStatelessSession() {
        return delegate.openStatelessSession();
    }

    @Override
    public StatelessSession openStatelessSession(Connection connection) {
        return delegate.openStatelessSession(connection);
    }

    @Override
    public ClassMetadata getClassMetadata(Class entityClass) {
        return delegate.getClassMetadata(entityClass);
    }

    @Override
    public ClassMetadata getClassMetadata(String entityName) {
        return delegate.getClassMetadata(entityName);
    }

    @Override
    public CollectionMetadata getCollectionMetadata(String roleName) {
        return delegate.getCollectionMetadata(roleName);
    }

    @Override
    public Map<String, ClassMetadata> getAllClassMetadata() {
        return delegate.getAllClassMetadata();
    }

    @Override
    public Map getAllCollectionMetadata() {
        return delegate.getAllCollectionMetadata();
    }

    @Override
    public Statistics getStatistics() {
        return delegate.getStatistics();
    }

    @Override
    public void close() throws HibernateException {
        delegate.close();
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public Cache getCache() {
        return delegate.getCache();
    }


    @Override
    public Set getDefinedFilterNames() {
        return delegate.getDefinedFilterNames();
    }

    @Override
    public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
        return delegate.getFilterDefinition(filterName);
    }

    @Override
    public boolean containsFetchProfileDefinition(String name) {
        return delegate.containsFetchProfileDefinition(name);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return delegate.getTypeHelper();
    }

    @Override
    public Reference getReference() throws NamingException {
        return delegate.getReference();
    }
}
