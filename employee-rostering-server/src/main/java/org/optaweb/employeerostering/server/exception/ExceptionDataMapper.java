/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.optaweb.employeerostering.server.exception;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.ejb.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.transaction.RollbackException;
import javax.ws.rs.core.Response.Status;

import org.optaweb.employeerostering.server.util.HierarchyTree;
import org.optaweb.employeerostering.shared.exception.ServerSideExceptionInfo;

@Singleton
public class ExceptionDataMapper {

    private HierarchyTree<Class<? extends Throwable>, ExceptionData> exceptionHierarchyTree;

    public ExceptionDataMapper() {
        exceptionHierarchyTree = new HierarchyTree<>((a, b) -> {
            if (a.equals(b)) {
                return HierarchyTree.HierarchyRelationship.IS_THE_SAME_AS;
            } else if (a.isAssignableFrom(b)) {
                return HierarchyTree.HierarchyRelationship.IS_ABOVE;
            } else if (b.isAssignableFrom(a)) {
                return HierarchyTree.HierarchyRelationship.IS_BELOW;
            } else {
                return HierarchyTree.HierarchyRelationship.HAS_NO_DIRECT_RELATION;
            }
        });

        for (ExceptionData exceptionData : ExceptionData.values()) {
            exceptionHierarchyTree.putInHierarchy(exceptionData.getExceptionClass(), exceptionData);
        }
    }

    public ExceptionData getExceptionDataForExceptionClass(Class<? extends Throwable> clazz) {
        Optional<ExceptionData> exceptionData = exceptionHierarchyTree.getHierarchyClassValue(clazz);
        if (exceptionData.isPresent()) {
            return exceptionData.get();
        } else {
            throw new IllegalStateException("No ExceptionData for exception class (" + clazz + ").");
        }
    }

    public enum ExceptionData {
        GENERIC_EXCEPTION("ServerSideException.generic", Status.INTERNAL_SERVER_ERROR, Throwable.class,
                          t -> Collections.emptyList()),
        ILLEGAL_ARGUMENT("ServerSideException.illegalArgument", Status.INTERNAL_SERVER_ERROR, IllegalArgumentException.class,
                         t -> Collections.singletonList(t.getMessage())),
        NULL_POINTER("ServerSideException.nullPointer", Status.INTERNAL_SERVER_ERROR, NullPointerException.class, t -> Collections.emptyList()),
        ENTITY_NOT_FOUND("ServerSideException.entityNotFound", Status.NOT_FOUND, EntityNotFoundException.class,
                         t -> Collections.singletonList(t.getMessage())),
        TRANSACTION_ROLLBACK("ServerSideException.rollback", Status.CONFLICT, RollbackException.class, t -> Collections.emptyList());

        private String i18nKey;
        private Status statusCode;
        private Class<? extends Throwable> exceptionClass;
        private Function<Throwable, List<String>> parameterMapping;

        private ExceptionData(String i18nKey, Status statusCode, Class<? extends Throwable> exceptionClass, Function<Throwable, List<String>> parameterMapping) {
            this.i18nKey = i18nKey;
            this.statusCode = statusCode;
            this.exceptionClass = exceptionClass;
            this.parameterMapping = parameterMapping;
        }

        public Class<? extends Throwable> getExceptionClass() {
            return exceptionClass;
        }

        public Status getStatusCode() {
            return statusCode;
        }

        public ServerSideExceptionInfo getServerSideExceptionInfoFromException(Throwable exception) {
            return new ServerSideExceptionInfo(exception, i18nKey,
                                               parameterMapping.apply(exception).toArray(new String[0]));
        }
    }
}