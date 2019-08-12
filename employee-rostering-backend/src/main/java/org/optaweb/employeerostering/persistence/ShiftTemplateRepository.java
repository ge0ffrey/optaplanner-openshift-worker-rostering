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

package org.optaweb.employeerostering.persistence;

import java.util.List;

import org.optaweb.employeerostering.domain.rotation.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {

    @Query("select distinct sa from ShiftTemplate sa " +
            "left join fetch sa.spot s " +
            "left join fetch sa.rotationEmployee re " +
            "where sa.tenantId = :tenantId " +
            "order by sa.startDayOffset, sa.startTime, s.name, re.name")
    List<ShiftTemplate> findAllByTenantId(Integer tenantId);
}
