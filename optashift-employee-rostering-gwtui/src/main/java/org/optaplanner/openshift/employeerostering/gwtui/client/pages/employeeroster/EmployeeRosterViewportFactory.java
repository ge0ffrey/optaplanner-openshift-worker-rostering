/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.employeeroster;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Positive2HoursScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.grid.CssGridLinesFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.grid.TicksFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListElementViewPool;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.TimingUtils;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Dependent
public class EmployeeRosterViewportFactory {

    @Inject
    private ListElementViewPool<EmployeeBlobView> shiftBlobViewPool;

    @Inject
    private ManagedInstance<EmployeeBlobView> shiftBlobViewInstances;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private CssGridLinesFactory cssGridLinesFactory;

    @Inject
    private TicksFactory<LocalDateTime> ticksFactory;

    @Inject
    private TimingUtils timingUtils;

    @Inject
    private CommonUtils commonUtils;

    private Map<Employee, List<ShiftView>> employeeShiftRosterModel;

    private Map<Employee, List<EmployeeAvailabilityView>> employeeAvailabilityRosterModel;

    private LinearScale<LocalDateTime> scale;

    public EmployeeRosterViewport getViewport(final EmployeeRosterView employeeRosterView) {

        return timingUtils.time("Employee Roster viewport instantiation", () -> {

            shiftBlobViewPool.init(1500L, shiftBlobViewInstances::get); //FIXME: Make maxSize variable

            employeeAvailabilityRosterModel = buildEmployeeAvailabilityRosterModel(employeeRosterView);

            employeeShiftRosterModel = buildEmployeeShiftRosterModel(employeeRosterView);

            scale = new Positive2HoursScale(employeeRosterView.getStartDate().atTime(0, 0),
                    employeeRosterView.getEndDate().atTime(0, 0));

            final Map<Long, Spot> spotsById = indexById(employeeRosterView.getSpotList());
            final Map<Long, Employee> employeesById = indexById(employeeRosterView.getEmployeeList());
            final List<Lane<LocalDateTime>> lanes = buildLanes(employeeRosterView, spotsById, employeesById);

            return new EmployeeRosterViewport(tenantStore.getCurrentTenantId(),
                    shiftBlobViewPool::get,
                    scale,
                    cssGridLinesFactory.newWithSteps(2L, 12L),
                    ticksFactory.newTicks(scale, "date-tick", 12L),
                    ticksFactory.newTicks(scale, "time-tick", 2L),
                    lanes,
                    spotsById,
                    employeesById);
        });
    }

    private Map<Employee, List<EmployeeAvailabilityView>> buildEmployeeAvailabilityRosterModel(final EmployeeRosterView employeeRosterView) {
        return employeeRosterView.getEmployeeList().stream()
                .collect(Collectors.toMap((employee) -> employee,
                        (employee) -> employeeRosterView.getEmployeeIdToAvailabilityViewListMap().get(employee.getId())));
    }

    private Map<Employee, List<ShiftView>> buildEmployeeShiftRosterModel(final EmployeeRosterView employeeRosterView) {
        return employeeRosterView.getEmployeeList().stream()
                .collect(Collectors.toMap((employee) -> employee,
                        (employee) -> employeeRosterView.getEmployeeIdToShiftViewListMap().get(employee.getId())));
    }

    private List<Lane<LocalDateTime>> buildLanes(final EmployeeRosterView employeeRosterView, Map<Long, Spot> spotsById, Map<Long, Employee> employeesById) {
        return employeeRosterView.getEmployeeList()
                .stream()
                .map(e -> new EmployeeLane(e, buildSubLanes(e, employeeRosterView.getEmployeeIdToAvailabilityViewListMap(), employeeRosterView.getEmployeeIdToShiftViewListMap(), employeesById, spotsById)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<SubLane<LocalDateTime>> buildSubLanes(final Employee employee,
                                                       final Map<Long, List<EmployeeAvailabilityView>> employeeIdToAvailabilityViewList,
                                                       final Map<Long, List<ShiftView>> employeeIdToShiftViewList,
                                                       final Map<Long, Employee> employeeIdToEmployee,
                                                       final Map<Long, Spot> spotIdToSpot) {

        if (employeeIdToAvailabilityViewList.isEmpty()) {
            return new ArrayList<>(singletonList(new SubLane<>()));
        }

        List<EmployeeAvailabilityView> employeeAvailabilities = new ArrayList<>();
        List<ShiftView> employeeShifts = new ArrayList<>();
        for (EmployeeAvailabilityView eav : commonUtils.flatten(employeeIdToAvailabilityViewList.values())) {
            employeeAvailabilities.add(eav);
        }
        for (ShiftView sv : commonUtils.flatten(employeeIdToShiftViewList.values())) {
            employeeShifts.add(sv);
        }

        final List<Blob<LocalDateTime>> employeeAvailabilitiesBlobs = employeeAvailabilities
                .stream()
                .filter(a -> a.getEmployeeId().equals(employee.getId()))
                .map(a -> {
                    return buildEmployeeAvailabilityBlob(employee, spotIdToSpot, employeeIdToEmployee, a);
                }).collect(Collectors.toList());

        final List<Blob<LocalDateTime>> employeeShiftsBlobs = employeeShifts
                .stream()
                .filter(s -> s.getEmployeeId().equals(employee.getId()))
                .map(s -> {
                    return buildEmployeeShiftBlob(employee, spotIdToSpot.get(s.getSpotId()), spotIdToSpot, employeeIdToEmployee, s);
                }).collect(Collectors.toList());
        // Impossible for an employee to have two employee availabilities at the same time
        return new ArrayList<>(Arrays.asList(new SubLane<>(employeeAvailabilitiesBlobs), new SubLane<>(employeeShiftsBlobs)));//conflictFreeSubLanesFactory.createSubLanes(blobs);
    }

    private EmployeeBlob buildEmployeeAvailabilityBlob(final Employee employee,
                                                       Map<Long, Spot> spotsById, Map<Long, Employee> employeesById,
                                                       final EmployeeAvailabilityView availabilityView) {
        return new EmployeeBlob(scale, spotsById, employeesById, availabilityView);
    }

    private EmployeeBlob buildEmployeeShiftBlob(final Employee employee,
                                                final Spot spot,
                                                Map<Long, Spot> spotsById, Map<Long, Employee> employeesById,
                                                final ShiftView shiftView) {
        return new EmployeeBlob(scale, spotsById, employeesById, shiftView);
    }

    private <T extends AbstractPersistable> Map<Long, T> indexById(final List<T> abstractPersistables) {
        return abstractPersistables.stream().collect(toMap(AbstractPersistable::getId, identity()));
    }
}
