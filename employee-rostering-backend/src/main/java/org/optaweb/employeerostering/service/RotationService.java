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

package org.optaweb.employeerostering.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.rotation.ShiftTemplate;
import org.optaweb.employeerostering.domain.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.persistence.ShiftTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class RotationService extends AbstractRestService {

    private final ShiftTemplateRepository shiftTemplateRepository;
    private final RosterService rosterService;
    private final SpotService spotService;
    private final EmployeeService employeeService;

    public RotationService(ShiftTemplateRepository shiftTemplateRepository, RosterService rosterService,
                           SpotService spotService, EmployeeService employeeService) {
        this.shiftTemplateRepository = shiftTemplateRepository;

        this.rosterService = rosterService;
        Assert.notNull(rosterService, "rosterService must not be null.");

        this.spotService = spotService;
        Assert.notNull(spotService, "spotService must not be null.");

        this.employeeService = employeeService;
        Assert.notNull(employeeService, "employeeService must not be null.");
    }

    @Transactional
    public List<ShiftTemplateView> getShiftTemplateList(Integer tenantId) {
        RosterState rosterState = rosterService.getRosterState(tenantId);
        return shiftTemplateRepository.findAllByTenantId(tenantId)
                .stream()
                .map(st -> new ShiftTemplateView(rosterState.getRotationLength(), st))
                .collect(Collectors.toList());
    }

    @Transactional
    public ShiftTemplateView getShiftTemplate(Integer tenantId, Long id) {
        RosterState rosterState = rosterService.getRosterState(tenantId);
        Optional<ShiftTemplate> shiftTemplateOptional = shiftTemplateRepository.findById(id);

        if (!shiftTemplateOptional.isPresent()) {
            throw new EntityNotFoundException("No ShiftTemplate entity found with ID (" + id + ").");
        }

        validateTenantIdParameter(tenantId, shiftTemplateOptional.get());
        return new ShiftTemplateView(rosterState.getRotationLength(), shiftTemplateOptional.get());
    }

    @Transactional
    public ShiftTemplateView createShiftTemplate(Integer tenantId, ShiftTemplateView shiftTemplateView) {
        RosterState rosterState = rosterService.getRosterState(tenantId);
        Spot spot = spotService.getSpot(tenantId, shiftTemplateView.getSpotId());
        Employee employee;

        if (shiftTemplateView.getRotationEmployeeId() != null) {
            employee = employeeService.getEmployee(tenantId, shiftTemplateView.getRotationEmployeeId());
        } else {
            employee = null;
        }

        ShiftTemplate shiftTemplate = new ShiftTemplate(rosterState.getRotationLength(), shiftTemplateView, spot,
                                                        employee);
        validateTenantIdParameter(tenantId, shiftTemplate);
        shiftTemplateRepository.save(shiftTemplate);
        return new ShiftTemplateView(rosterState.getRotationLength(), shiftTemplate);
    }

    @Transactional
    public ShiftTemplateView updateShiftTemplate(Integer tenantId, ShiftTemplateView shiftTemplateView) {
        RosterState rosterState = rosterService.getRosterState(tenantId);
        Spot spot = spotService.getSpot(tenantId, shiftTemplateView.getSpotId());
        Employee employee;

        if (shiftTemplateView.getRotationEmployeeId() != null) {
            employee = employeeService.getEmployee(tenantId, shiftTemplateView.getRotationEmployeeId());
        } else {
            employee = null;
        }

        ShiftTemplate shiftTemplate = new ShiftTemplate(rosterState.getRotationLength(), shiftTemplateView, spot,
                                                        employee);
        validateTenantIdParameter(tenantId, shiftTemplate);

        Optional<ShiftTemplate> shiftTemplateOptional = shiftTemplateRepository.findById(shiftTemplate.getId());

        if (!shiftTemplateOptional.isPresent()) {
            throw new EntityNotFoundException("ShiftTemplate entity with ID (" + shiftTemplate.getId()
                                                      + ") not found.");
        } else if (!shiftTemplateOptional.get().getTenantId().equals(shiftTemplate.getTenantId())) {
            throw new IllegalStateException("ShiftTemplate entity with tenantId ("
                                                    + shiftTemplateOptional.get().getTenantId()
                                                    + ") cannot change tenants.");
        }

        ShiftTemplate databaseShiftTemplate = shiftTemplateOptional.get();
        databaseShiftTemplate.setRotationEmployee(employee);
        databaseShiftTemplate.setSpot(spot);
        databaseShiftTemplate.setStartDayOffset(shiftTemplate.getStartDayOffset());
        databaseShiftTemplate.setEndDayOffset(shiftTemplate.getEndDayOffset());
        databaseShiftTemplate.setStartTime(shiftTemplate.getStartTime());
        databaseShiftTemplate.setEndTime(shiftTemplate.getEndTime());
        // Flush to increase version number before we duplicate it to ShiftTemplateView
        shiftTemplateRepository.saveAndFlush(databaseShiftTemplate);

        return new ShiftTemplateView(rosterState.getRotationLength(), shiftTemplate);
    }

    @Transactional
    public Boolean deleteShiftTemplate(Integer tenantId, Long id) {

        Optional<ShiftTemplate> shiftTemplateOptional = shiftTemplateRepository.findById(id);

        if (!shiftTemplateOptional.isPresent()) {
            throw new EntityNotFoundException("No ShiftTemplate entity found with ID (" + id + ").");
        }

        validateTenantIdParameter(tenantId, shiftTemplateOptional.get());
        shiftTemplateRepository.deleteById(id);
        return true;
    }
}
