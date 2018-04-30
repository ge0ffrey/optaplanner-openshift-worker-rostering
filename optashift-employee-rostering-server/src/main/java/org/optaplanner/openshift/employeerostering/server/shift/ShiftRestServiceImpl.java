/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.server.shift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.optaplanner.openshift.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestService;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.rotation.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.RotationView;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestService;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestService;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantRestService;

public class ShiftRestServiceImpl extends AbstractRestServiceImpl implements ShiftRestService {

    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private TenantRestService tenantRestService;
    @Inject
    private SpotRestService spotRestService;
    @Inject
    private EmployeeRestService employeeRestService;

    @Override
    @Transactional
    public ShiftView getShift(Integer tenantId, Long id) {
        Shift shift = entityManager.find(Shift.class, id);
        validateTenantIdParameter(tenantId, shift);
        return new ShiftView(shift);
    }

    @Override
    @Transactional
    public Long addShift(Integer tenantId, ShiftView shiftView) {
        Shift shift = convertFromView(tenantId, shiftView);
        entityManager.persist(shift);
        return shift.getId();
    }

    @Override
    @Transactional
    public Shift updateShift(Integer tenantId, ShiftView shiftView) {
        Shift shift = convertFromView(tenantId, shiftView);
        return entityManager.merge(shift);
    }

    private Shift convertFromView(Integer tenantId, ShiftView shiftView) {
        validateTenantIdParameter(tenantId, shiftView);
        Spot spot = entityManager.find(Spot.class, shiftView.getSpotId());
        validateTenantIdParameter(tenantId, spot);

        Long rotationEmployeeId = shiftView.getRotationEmployeeId();
        Employee rotationEmployee = null;
        if (rotationEmployeeId != null) {
            rotationEmployee = entityManager.find(Employee.class, rotationEmployeeId);
            if (rotationEmployee == null) {
                throw new IllegalArgumentException("ShiftView (" + shiftView + ") has an non-existing employeeId (" + rotationEmployeeId + ").");
            }
            validateTenantIdParameter(tenantId, rotationEmployee);
        }

        Shift shift = new Shift(tenantRestService.getTenantConfiguration(tenantId).getTimeZone(), shiftView, spot,
                rotationEmployee);
        shift.setPinnedByUser(shiftView.isPinnedByUser());
        Long employeeId = shiftView.getEmployeeId();
        if (employeeId != null) {
            Employee employee = entityManager.find(Employee.class, employeeId);
            if (employee == null) {
                throw new IllegalArgumentException("ShiftView (" + shiftView + ") has an non-existing employeeId (" + employeeId + ").");
            }
            validateTenantIdParameter(tenantId, employee);
            shift.setEmployee(employee);
        }
        return shift;
    }

    @Override
    @Transactional
    public Boolean removeShift(Integer tenantId, Long id) {
        Shift shift = entityManager.find(Shift.class, id);
        if (shift == null) {
            return false;
        }
        validateTenantIdParameter(tenantId, shift);
        entityManager.remove(shift);
        return true;
    }

    @Override
    public List<ShiftView> getShifts(Integer tenantId) {
        return getAllShifts(tenantId).stream().map(ShiftView::new).collect(Collectors.toList());
    }

    private List<Shift> getAllShifts(Integer tenantId) {
        TypedQuery<Shift> q = entityManager.createNamedQuery("Shift.findAll", Shift.class);
        q.setParameter("tenantId", tenantId);
        return q.getResultList();
    }

    @Override
    public RotationView getRotation(Integer tenantId) {
        List<ShiftTemplate> shiftTemplateList = entityManager.createNamedQuery("ShiftTemplate.findAll", ShiftTemplate.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        RotationView out = new RotationView();
        out.setTenantId(tenantId);
        out.setSpotList(spotRestService.getSpotList(tenantId));
        out.setEmployeeList(employeeRestService.getEmployeeList(tenantId));
        out.setRotationLength(entityManager.createNamedQuery("RosterState.find", RosterState.class)
                .setParameter("tenantId", tenantId)
                .getSingleResult().getRotationLength());
        Map<Long, List<ShiftTemplateView>> spotIdToShiftTemplateViewListMap = new HashMap<>();
        shiftTemplateList.forEach((shiftTemplate) -> {
            spotIdToShiftTemplateViewListMap.computeIfAbsent(shiftTemplate.getSpot().getId(),
                    (k) -> new ArrayList<>())
                    .add(new ShiftTemplateView(out.getRotationLength(), shiftTemplate));
        });
        out.setSpotIdToShiftTemplateViewListMap(spotIdToShiftTemplateViewListMap);
        return out;
    }

    @Override
    @Transactional
    public void updateRotation(Integer tenantId, RotationView rotationView) {
        if (!tenantId.equals(rotationView.getTenantId())) {
            throw new IllegalArgumentException("rotationView (" + rotationView + ") tenantId" +
                    " does not match tenantId (" + tenantId + ")");
        }
        List<ShiftTemplate> oldShiftTemplateList = entityManager.createNamedQuery("ShiftTemplate.findAll", ShiftTemplate.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        oldShiftTemplateList.forEach((s) -> entityManager.remove(s));
        // TODO: Update rotation length and unplanneOffset
        Integer rotationLength = entityManager.createNamedQuery("RosterState.find", RosterState.class)
                .setParameter("tenantId", tenantId)
                .getSingleResult().getRotationLength();
        Map<Long, Spot> spotIdToSpotMap = spotRestService
                .getSpotList(tenantId).stream().collect(Collectors
                        .toMap(spot -> spot.getId(), spot -> spot));
        Map<Long, Employee> employeeIdToEmployeeMap = employeeRestService
                .getEmployeeList(tenantId).stream().collect(Collectors
                        .toMap(employee -> employee.getId(), employee -> employee));
        rotationView.getSpotIdToShiftTemplateViewListMap()
                .forEach((spotId, shiftTemplateViewList) -> {
                    Spot spot = spotIdToSpotMap.get(spotId);
                    if (shiftTemplateViewList != null) {
                        shiftTemplateViewList.forEach(shiftTemplateView -> {
                            entityManager.merge(new ShiftTemplate(rotationLength,
                                    shiftTemplateView, spot,
                                    employeeIdToEmployeeMap.get(shiftTemplateView.getId())));
                        });
                    }
                });

    }

}
