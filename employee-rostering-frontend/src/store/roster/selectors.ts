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
import { AppState } from '../types';
import { employeeSelectors } from '../employee';
import Shift from 'domain/Shift';
import Spot from 'domain/Spot';
import ShiftRosterView from 'domain/ShiftRosterView';
import { objectWithout } from 'util/ImmutableCollectionOperations';
import Employee from 'domain/Employee';
import AvailabilityRosterView from 'domain/AvailabilityRosterView';
import { spotSelectors } from 'store/spot';
import EmployeeAvailability from 'domain/EmployeeAvailability';

export function isLoading(state: AppState) {
  return state.spotList.isLoading || state.employeeList.isLoading || state.skillList.isLoading ||
    state.contractList.isLoading || state.shiftRoster.isLoading || state.availabilityRoster.isLoading ||
    state.rosterState.isLoading;
}

export const getSpotListInShiftRoster = (state: AppState): Spot[] => {
  if (isLoading(state)) {
    return [];
  }
  return (state.shiftRoster.shiftRosterView as ShiftRosterView).spotList;
};

export const getShiftListForSpot = (state: AppState, spot: Spot): Shift[] => {
  if (isLoading(state)) {
    throw Error("Shift Roster is loading");
  }
  if ((spot.id as number) in (state.shiftRoster.shiftRosterView as ShiftRosterView).spotIdToShiftViewListMap) {
    return ((state.shiftRoster.shiftRosterView as ShiftRosterView)
      .spotIdToShiftViewListMap[spot.id as number]).map(sv => ({
      ...objectWithout(sv, "spotId", "rotationEmployeeId", "employeeId"),
      spot: spot,
      rotationEmployee: (sv.rotationEmployeeId !== null)?
        employeeSelectors.getEmployeeById(state, sv.rotationEmployeeId) : null,
      employee: (sv.employeeId !== null)? employeeSelectors.getEmployeeById(state, sv.employeeId) : null
    }));
  }
  else {
    return [];
  }
};

export const getEmployeeListInAvailabilityRoster = (state: AppState): Employee[] => {
  if (isLoading(state)) {
    return [];
  }
  return (state.availabilityRoster.availabilityRosterView as AvailabilityRosterView).employeeList;
};

export const getShiftListForEmployee = (state: AppState, employee: Employee): Shift[] => {
  if (isLoading(state)) {
    throw Error("Availability Roster is loading");
  }
  if ((employee.id as number) in
  (state.availabilityRoster.availabilityRosterView as AvailabilityRosterView).employeeIdToShiftViewListMap) {
    return ((state.availabilityRoster.availabilityRosterView as AvailabilityRosterView)
      .employeeIdToShiftViewListMap[employee.id as number]).map(sv => ({
      ...objectWithout(sv, "spotId", "rotationEmployeeId", "employeeId"),
      spot: spotSelectors.getSpotById(state, sv.spotId),
      employee: employee,
      rotationEmployee: (sv.rotationEmployeeId !== null)?
        employeeSelectors.getEmployeeById(state, sv.rotationEmployeeId) : null,
    }));
  }
  else {
    return [];
  }
};

export const getAvailabilityListForEmployee = (state: AppState, employee: Employee): EmployeeAvailability[] => {
  if (isLoading(state)) {
    throw Error("Availability Roster is loading");
  }
  if ((employee.id as number) in
  (state.availabilityRoster.availabilityRosterView as AvailabilityRosterView).employeeIdToAvailabilityViewListMap) {
    return ((state.availabilityRoster.availabilityRosterView as AvailabilityRosterView)
      .employeeIdToAvailabilityViewListMap[employee.id as number]).map(ea => ({
      ...objectWithout(ea, "employeeId"),
      employee: employeeSelectors.getEmployeeById(state, ea.employeeId)
    }));
  }
  else {
    return [];
  }
};