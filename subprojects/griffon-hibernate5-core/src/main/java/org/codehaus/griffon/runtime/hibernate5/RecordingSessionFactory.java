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


import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Andres Almiray
 */
public class RecordingSessionFactory extends SessionFactoryDecorator {
    private AtomicInteger sessionCount = new AtomicInteger(0);

    public RecordingSessionFactory(@Nonnull SessionFactory delegate) {
        super(delegate);
    }

    public int increaseSessionCount() {
        return sessionCount.incrementAndGet();
    }

    public int decreaseSessionCount() {
        return sessionCount.decrementAndGet();
    }

    public int getSessionCount() {
        return sessionCount.get();
    }

    @Override
    public Session openSession() throws HibernateException {
        Session session = super.openSession();
        increaseSessionCount();
        return wrap(session);
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        Session session = super.getCurrentSession();
        increaseSessionCount();
        return wrap(session);
    }

    @Nonnull
    private Session wrap(@Nonnull Session session) {
        return session instanceof LinkedSession ? session : new LinkedSession(session, this);
    }
}
