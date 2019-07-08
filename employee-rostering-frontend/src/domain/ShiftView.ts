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
import DomainObject from './DomainObject';
import DomainObjectView from './DomainObjectView';
import Shift from './Shift';
import HardMediumSoftScore from './HardMediumSoftScore';

export const shiftToShiftView = (shift: Shift): ShiftView => ({
  id: shift.id,
  version: shift.version,
  tenantId: shift.tenantId,
  pinnedByUser: shift.pinnedByUser,
  startDateTime: shift.startDateTime,
  endDateTime: shift.endDateTime,
  spotId: shift.spot.id as number,
  employeeId: shift.employee? shift.employee.id as number : null,
  rotationEmployeeId: shift.rotationEmployee? shift.rotationEmployee.id as number : null,
  indictmentScore: shift.indictmentScore
}); 

export const shiftViewToDomainObjectView = (view: ShiftView): DomainObjectView<Shift> => ({
        ...view,
        employee: view.employeeId,
        spot: view.spotId,
        rotationEmployee: view.rotationEmployeeId
  }); 

export default interface ShiftView extends DomainObject {
    startDateTime: Date;
    endDateTime: Date;
    spotId: number;
    rotationEmployeeId: number | null;
    employeeId: number | null;
    pinnedByUser: boolean;
    indictmentScore?: HardMediumSoftScore;
  }