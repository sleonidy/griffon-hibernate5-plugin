/*
 * Copyright 2014-2017 the original author or authors.
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
package griffon.plugins.hibernate5;

import griffon.core.storage.ObjectFactory;
import org.hibernate.SessionFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 */
public interface Hibernate5Factory extends ObjectFactory<SessionFactory> {
    @Nonnull
    Set<String> getSessionFactoryNames();

    @Nonnull
    Map<String, Object> getConfigurationFor(@Nonnull String sessionFactoryName);
}
