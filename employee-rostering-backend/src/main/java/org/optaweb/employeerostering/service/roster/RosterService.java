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

package org.optaweb.employeerostering.service.roster;

import javax.persistence.EntityNotFoundException;

import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RosterService extends AbstractRestService {

    private final RosterStateRepository rosterStateRepository;

    public RosterService(RosterStateRepository rosterStateRepository) {
        this.rosterStateRepository = rosterStateRepository;
    }

    // ************************************************************************
    // RosterState
    // ************************************************************************

    @Transactional
    public RosterState getRosterState(Integer tenantId) {
        RosterState rosterState = rosterStateRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("No RosterState entity found with tenantId (" +
                                                                       tenantId + ")."));
        validateTenantIdParameter(tenantId, rosterState);
        return rosterState;
    }

    // ************************************************************************
    // ShiftRosterView
    // ************************************************************************

    // TODO: Add getShiftRosterView() methods once SolverManager and IndictmentUtils are added

    // ************************************************************************
    // AvailabilityRosterView
    // ************************************************************************

    // TODO: Add getAvailabilityRosterView() methods once SolverManager and IndictmentUtils are added

}
