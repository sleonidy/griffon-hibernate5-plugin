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
package org.codehaus.griffon.compile.hibernate5.ast.transform;

import griffon.plugins.hibernate5.Hibernate5Handler;
import griffon.transform.Hibernate5Aware;
import org.codehaus.griffon.compile.core.AnnotationHandler;
import org.codehaus.griffon.compile.core.AnnotationHandlerFor;
import org.codehaus.griffon.compile.core.ast.transform.AbstractASTTransformation;
import org.codehaus.griffon.compile.hibernate5.Hibernate5AwareConstants;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static org.codehaus.griffon.compile.core.ast.GriffonASTUtils.injectInterface;

/**
 * Handles generation of code for the {@code @Hibernate5Aware} annotation.
 *
 * @author Andres Almiray
 */
@AnnotationHandlerFor(Hibernate5Aware.class)
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class Hibernate5AwareASTTransformation extends AbstractASTTransformation implements Hibernate5AwareConstants, AnnotationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(Hibernate5AwareASTTransformation.class);
    private static final ClassNode Hibernate5_HANDLER_CNODE = makeClassSafe(Hibernate5Handler.class);
    private static final ClassNode Hibernate5_AWARE_CNODE = makeClassSafe(Hibernate5Aware.class);

    /**
     * Convenience method to see if an annotated node is {@code @Hibernate5Aware}.
     *
     * @param node the node to check
     * @return true if the node is an event publisher
     */
    public static boolean hasHibernate5AwareAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (Hibernate5_AWARE_CNODE.equals(annotation.getClassNode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param nodes  the ast nodes
     * @param source the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        checkNodesForAnnotationAndType(nodes[0], nodes[1]);
        addHibernate5HandlerIfNeeded(source, (AnnotationNode) nodes[0], (ClassNode) nodes[1]);
    }

    public static void addHibernate5HandlerIfNeeded(SourceUnit source, AnnotationNode annotationNode, ClassNode classNode) {
        if (needsDelegate(classNode, source, METHODS, Hibernate5Aware.class.getSimpleName(), Hibernate5_HANDLER_TYPE)) {
            LOG.debug("Injecting {} into {}", Hibernate5_HANDLER_TYPE, classNode.getName());
            apply(classNode);
        }
    }

    /**
     * Adds the necessary field and methods to support hibernate5 handling.
     *
     * @param declaringClass the class to which we add the support field and methods
     */
    public static void apply(@Nonnull ClassNode declaringClass) {
        injectInterface(declaringClass, Hibernate5_HANDLER_CNODE);
        Expression hibernate5Handler = injectedField(declaringClass, Hibernate5_HANDLER_CNODE, Hibernate5_HANDLER_FIELD_NAME);
        addDelegateMethods(declaringClass, Hibernate5_HANDLER_CNODE, hibernate5Handler);
    }
}