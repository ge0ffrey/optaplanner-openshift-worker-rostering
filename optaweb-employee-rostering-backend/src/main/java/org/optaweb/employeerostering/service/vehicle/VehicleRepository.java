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

package org.optaweb.employeerostering.service.vehicle;

import java.util.List;

import org.optaweb.employeerostering.domain.vehicle.Vehicle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("select e from Vehicle e " +
            "where e.tenantId = :tenantId " +
            "order by LOWER(e.name)")
    List<Vehicle> findAllByTenantId(@Param("tenantId") Integer tenantId, Pageable pageable);

    @Query("select e from Vehicle e " +
            "where e.tenantId = :tenantId and e.name = :name")
    Vehicle findVehicleByName(@Param("tenantId") Integer tenantId, @Param("name") String name);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Vehicle e where e.tenantId = :tenantId")
    void deleteForTenant(@Param("tenantId") Integer tenantId);
}
