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
package org.codehaus.griffon.runtime.jmx;

import griffon.core.env.Metadata;
import griffon.plugins.hibernate5.Hibernate5Storage;
import org.codehaus.griffon.runtime.monitor.AbstractObjectStorageMonitor;
import org.hibernate.SessionFactory;

import javax.annotation.Nonnull;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class Hibernate5StorageMonitor extends AbstractObjectStorageMonitor<SessionFactory> implements Hibernate5StorageMonitorMXBean {
    public Hibernate5StorageMonitor(@Nonnull Metadata metadata, @Nonnull Hibernate5Storage delegate) {
        super(metadata, delegate);
    }

    @Override
    protected String getStorageName() {
        return "hibernate5";
    }
}