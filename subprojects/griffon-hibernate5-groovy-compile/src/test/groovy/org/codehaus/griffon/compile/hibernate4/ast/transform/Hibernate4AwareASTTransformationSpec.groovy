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
package org.codehaus.griffon.compile.hibernate5.ast.transform

import griffon.plugins.hibernate5.Hibernate5Handler
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * @author Andres Almiray
 */
class Hibernate5AwareASTTransformationSpec extends Specification {
    def 'Hibernate5AwareASTTransformation is applied to a bean via @Hibernate5Aware'() {
        given:
        GroovyShell shell = new GroovyShell()

        when:
        def bean = shell.evaluate('''
        @griffon.transform.Hibernate5Aware
        class Bean { }
        new Bean()
        ''')

        then:
        bean instanceof Hibernate5Handler
        Hibernate5Handler.methods.every { Method target ->
            bean.class.declaredMethods.find { Method candidate ->
                candidate.name == target.name &&
                candidate.returnType == target.returnType &&
                candidate.parameterTypes == target.parameterTypes &&
                candidate.exceptionTypes == target.exceptionTypes
            }
        }
    }

    def 'Hibernate5AwareASTTransformation is not applied to a Hibernate5Handler subclass via @Hibernate5Aware'() {
        given:
        GroovyShell shell = new GroovyShell()

        when:
        def bean = shell.evaluate('''
        import griffon.plugins.hibernate5.Hibernate5Callback
        import griffon.plugins.hibernate5.exceptions.RuntimeHibernate5Exception
        import griffon.plugins.hibernate5.Hibernate5Handler

        import javax.annotation.Nonnull
        @griffon.transform.Hibernate5Aware
        class Hibernate5HandlerBean implements Hibernate5Handler {
            @Override
            public <R> R withHbm5Session(@Nonnull Hibernate5Callback<R> callback) throws RuntimeHibernate5Exception {
                return null
            }
            @Override
            public <R> R withHbm5Session(@Nonnull String sessionFactoryName, @Nonnull Hibernate5Callback<R> callback) throws RuntimeHibernate5Exception {
                return null
            }
            @Override
            void closeHbm5Session(){}
            @Override
            void closeHbm5Session(@Nonnull String sessionFactoryName){}
        }
        new Hibernate5HandlerBean()
        ''')

        then:
        bean instanceof Hibernate5Handler
        Hibernate5Handler.methods.every { Method target ->
            bean.class.declaredMethods.find { Method candidate ->
                candidate.name == target.name &&
                    candidate.returnType == target.returnType &&
                    candidate.parameterTypes == target.parameterTypes &&
                    candidate.exceptionTypes == target.exceptionTypes
            }
        }
    }
}
