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

import griffon.plugins.hibernate5.Hibernate5Callback;
import griffon.plugins.hibernate5.Hibernate5Factory;
import griffon.plugins.hibernate5.Hibernate5Handler;
import griffon.plugins.hibernate5.Hibernate5Storage;
import griffon.plugins.hibernate5.exceptions.RuntimeHibernate5Exception;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultHibernate5Handler implements Hibernate5Handler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHibernate5Handler.class);
    private static final String ERROR_SESSION_FACTORY_NAME_BLANK = "Argument 'sessionFactoryName' must not be blank";
    private static final String ERROR_CALLBACK_NULL = "Argument 'callback' must not be null";

    private final Hibernate5Factory hibernate5Factory;
    private final Hibernate5Storage hibernate5Storage;

    @Inject
    public DefaultHibernate5Handler(@Nonnull Hibernate5Factory hibernate5Factory, @Nonnull Hibernate5Storage hibernate5Storage) {
        this.hibernate5Factory = requireNonNull(hibernate5Factory, "Argument 'hibernate5Factory' must not be null");
        this.hibernate5Storage = requireNonNull(hibernate5Storage, "Argument 'hibernate5Storage' must not be null");
    }

    @Nullable
    @Override
    public <R> R withHbm5Session(@Nonnull Hibernate5Callback<R> callback) throws RuntimeHibernate5Exception {
        return withHbm5Session(DefaultHibernate5Factory.KEY_DEFAULT, callback);
    }

    @Nullable
    @Override
    @SuppressWarnings("ThrowFromFinallyBlock")
    public <R> R withHbm5Session(@Nonnull String sessionFactoryName, @Nonnull Hibernate5Callback<R> callback) throws RuntimeHibernate5Exception {
        requireNonBlank(sessionFactoryName, ERROR_SESSION_FACTORY_NAME_BLANK);
        requireNonNull(callback, ERROR_CALLBACK_NULL);
        SessionFactory sf = getSessionFactory(sessionFactoryName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing statements on session '{}'", sessionFactoryName);
        }
        Session session = null;
        Transaction transaction = null;
        try {
            session = sf.openSession();
            transaction = session.beginTransaction();
            R result = callback.handle(sessionFactoryName, session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            try {
                if (transaction != null)
                    transaction.rollback();
            } catch (RuntimeException runtimeException) {
                LOG.error("Failed to rollback", runtimeException);
            }
            throw new RuntimeHibernate5Exception(sessionFactoryName, e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
                throw new RuntimeHibernate5Exception(sessionFactoryName, e);
            }
        }
    }

    @Override
    public void closeHbm5Session() {
        closeHbm5Session(DefaultHibernate5Factory.KEY_DEFAULT);
    }

    @Override
    public void closeHbm5Session(@Nonnull String sessionFactoryName) {
        requireNonBlank(sessionFactoryName, ERROR_SESSION_FACTORY_NAME_BLANK);
        SessionFactory hibernate5 = hibernate5Storage.get(sessionFactoryName);
        if (hibernate5 != null) {
            hibernate5Factory.destroy(sessionFactoryName, hibernate5);
            hibernate5Storage.remove(sessionFactoryName);
        }
    }

    @Nonnull
    private SessionFactory getSessionFactory(@Nonnull String sessionFactoryName) {
        SessionFactory sessionFactory = hibernate5Storage.get(sessionFactoryName);
        if (sessionFactory == null) {
            sessionFactory = hibernate5Factory.create(sessionFactoryName);
            hibernate5Storage.set(sessionFactoryName, sessionFactory);
        }
        return sessionFactory;
    }
}
