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

import { ThunkCommandFactory, AppState } from '../types';
import * as actions from './actions';
import * as operations from './operations'; // Hack used for mocking
import { SetRosterStateIsLoadingAction, SetRosterStateAction,
  SetShiftRosterIsLoadingAction, SetShiftRosterViewAction, SolveRosterAction, TerminateSolvingRosterEarlyAction, PublishRosterAction, PublishResult, SetAvailabilityRosterIsLoadingAction, SetAvailabilityRosterViewAction } from './types';
import RosterState from 'domain/RosterState';
import ShiftRosterView from 'domain/ShiftRosterView';
import { PaginationData, ObjectNumberMap, mapObjectNumberMap } from 'types';
import moment from 'moment';
import Spot from 'domain/Spot';
import { alert } from 'store/alert';
import { ThunkDispatch } from 'redux-thunk';
import { KindaShiftView, kindaShiftViewAdapter } from 'store/shift/operations';
import { KindaEmployeeAvailabilityView, kindaAvailabilityViewAdapter } from 'store/availability/operations';
import RestServiceClient from 'store/rest';
import { AddAlertAction } from 'store/alert/types';
import Employee from 'domain/Employee';
import AvailabilityRosterView from 'domain/AvailabilityRosterView';

export interface RosterSliceInfo {
  fromDate: Date;
  toDate: Date;
}

interface KindaShiftRosterView extends Omit<ShiftRosterView, "spotIdToShiftViewListMap"> {
  spotIdToShiftViewListMap: ObjectNumberMap<KindaShiftView[]>;
}

interface KindaAvailabilityRosterView extends Omit<AvailabilityRosterView, "employeeIdToShiftViewListMap" | "employeeIdToAvailabilityViewListMap" | "unassignedShiftViewList" > {
  employeeIdToShiftViewListMap: ObjectNumberMap<KindaShiftView[]>;
  employeeIdToAvailabilityViewListMap: ObjectNumberMap<KindaEmployeeAvailabilityView[]>;
  unassignedShiftViewList: KindaShiftView[];
}

let lastCalledShiftRosterArgs: any | null;
let lastCalledShiftRoster: ThunkCommandFactory<any, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> | null = null;

let lastCalledAvailabilityRosterArgs: any | null;
let lastCalledAvailabilityRoster: ThunkCommandFactory<any, SetAvailabilityRosterIsLoadingAction | SetAvailabilityRosterViewAction> | null = null;

let stopSolvingRosterTimeout: NodeJS.Timeout|null = null;
let autoRefreshShiftRosterDuringSolvingIntervalTimeout: NodeJS.Timeout|null = null;

function stopSolvingRoster(dispatch: ThunkDispatch<AppState, RestServiceClient, AddAlertAction | TerminateSolvingRosterEarlyAction>) {
  if (stopSolvingRosterTimeout !== null) {
    clearTimeout(stopSolvingRosterTimeout);
    stopSolvingRosterTimeout = null;
  }
  if (autoRefreshShiftRosterDuringSolvingIntervalTimeout !== null) {
    clearInterval(autoRefreshShiftRosterDuringSolvingIntervalTimeout);
    autoRefreshShiftRosterDuringSolvingIntervalTimeout = null;
  }
  dispatch(actions.terminateSolvingRosterEarly());
  Promise.all([
    dispatch(operations.refreshShiftRoster())
  ]).then(() => {
    dispatch(alert.showInfoMessage("finishSolvingRoster", { finishSolvingTime: moment(new Date()).format("LLL") }));
  });
}

const updateInterval = 1000;

function refresh(dispatch: ThunkDispatch<AppState, RestServiceClient, any>) {
  Promise.all([
    dispatch(operations.refreshShiftRoster()),
    dispatch(operations.refreshAvailabilityRoster())
  ]).then(() => {
    autoRefreshShiftRosterDuringSolvingIntervalTimeout = setTimeout(() => refresh(dispatch), updateInterval);
  });
}

export const solveRoster: ThunkCommandFactory<void,  AddAlertAction | SolveRosterAction> = () => 
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    return client.post(`/tenant/${tenantId}/roster/solve`, {}).then(() => {
      let solvingStartTime: number = new Date().getTime();
      const solvingLength = 30 * 1000;
      dispatch(actions.solveRoster());
      dispatch(alert.showInfoMessage("startSolvingRoster", { startSolvingTime: moment(solvingStartTime).format("LLL") }));
      autoRefreshShiftRosterDuringSolvingIntervalTimeout = setTimeout(() => refresh(dispatch), updateInterval);
      stopSolvingRosterTimeout = setTimeout(() => stopSolvingRoster(dispatch), solvingLength);
    });
  }


export const terminateSolvingRosterEarly: ThunkCommandFactory<void, TerminateSolvingRosterEarlyAction> = () => 
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    return client.post(`/tenant/${tenantId}/roster/terminate`, {}).then(() => stopSolvingRoster(dispatch));
  }


export const refreshShiftRoster: ThunkCommandFactory<void, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = () =>
  (dispatch, state, client) => {
    if (lastCalledShiftRosterArgs !== null && lastCalledShiftRoster !== null) {
      dispatch(lastCalledShiftRoster(lastCalledShiftRosterArgs));
    }
  }

export const refreshAvailabilityRoster: ThunkCommandFactory<void, SetAvailabilityRosterIsLoadingAction | SetAvailabilityRosterViewAction> = () =>
  (dispatch, state, client) => {
    if (lastCalledAvailabilityRosterArgs !== null && lastCalledAvailabilityRoster !== null) {
      dispatch(lastCalledAvailabilityRoster(lastCalledAvailabilityRosterArgs));
    }
  }

export const getRosterState: ThunkCommandFactory<void, SetRosterStateIsLoadingAction | SetRosterStateAction> = () =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setRosterStateIsLoading(true));
    return client.get<RosterState>(`/tenant/${tenantId}/roster/state`).then(newRosterState => {
      dispatch(actions.setRosterState(newRosterState));
      dispatch(actions.setRosterStateIsLoading(false));
    });
  };

export const publish: ThunkCommandFactory<void,  AddAlertAction | PublishRosterAction> = () =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    return client.post<PublishResult>(`/tenant/${tenantId}/roster/publishAndProvision`, {}).then(pr => {
      dispatch(actions.publishRoster({
        publishedFromDate: moment(pr.publishedFromDate).toDate(),
        publishedToDate: moment(pr.publishedToDate).toDate()
      }));
      dispatch(operations.refreshShiftRoster());
      dispatch(alert.showSuccessMessage("publish", { from: moment(pr.publishedFromDate).format("LL"), to: moment(pr.publishedToDate).format("LL") }));
    });
  }

function convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView: KindaShiftRosterView): ShiftRosterView {
  return {
    ...newShiftRosterView,
    spotIdToShiftViewListMap: mapObjectNumberMap(newShiftRosterView.spotIdToShiftViewListMap, shiftViewList =>
      shiftViewList.map(kindaShiftViewAdapter))
  };
}

function convertKindaAvailabilityRosterViewToAvailabilityRosterView(newAvailabilityRosterView: KindaAvailabilityRosterView): AvailabilityRosterView {
  return {
    ...newAvailabilityRosterView,
    employeeIdToAvailabilityViewListMap: mapObjectNumberMap(newAvailabilityRosterView.employeeIdToAvailabilityViewListMap, availabilityViewList =>
      availabilityViewList.map(kindaAvailabilityViewAdapter)),
    employeeIdToShiftViewListMap: mapObjectNumberMap(newAvailabilityRosterView.employeeIdToShiftViewListMap, shiftViewList =>
      shiftViewList.map(kindaShiftViewAdapter)),
    unassignedShiftViewList: newAvailabilityRosterView.unassignedShiftViewList.map(kindaShiftViewAdapter),
  };
}

export const getCurrentShiftRoster: ThunkCommandFactory<PaginationData, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = pagination =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setShiftRosterIsLoading(true));
    return client.get<KindaShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView/current?p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`).then(newShiftRosterView => {
      const shiftRosterView = convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView);
      dispatch(actions.setShiftRosterView(shiftRosterView));
      lastCalledShiftRoster = getCurrentShiftRoster;
      lastCalledShiftRosterArgs = pagination;
      dispatch(actions.setShiftRosterIsLoading(false));
    });
  };

export const getShiftRoster: ThunkCommandFactory<RosterSliceInfo & { pagination: PaginationData }, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = params =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    const fromDateAsString = moment(params.fromDate).format("YYYY-MM-DD");
    const toDateAsString = moment(params.toDate).add(1, "day").format("YYYY-MM-DD");
    dispatch(actions.setShiftRosterIsLoading(true));
    return client.get<KindaShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView?` +
    `p=${params.pagination.pageNumber}&n=${params.pagination.itemsPerPage}` +
    `&startDate=${fromDateAsString}&endDate=${toDateAsString}`).then(newShiftRosterView => {
      const shiftRosterView = convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView);
      dispatch(actions.setShiftRosterView(shiftRosterView));
      lastCalledShiftRoster = getShiftRoster;
      lastCalledShiftRosterArgs = params;
      dispatch(actions.setShiftRosterIsLoading(false));
    });
  };

export const getShiftRosterFor: ThunkCommandFactory<RosterSliceInfo & { spotList: Spot[] }, SetShiftRosterIsLoadingAction | SetShiftRosterViewAction> = params =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    const fromDateAsString = moment(params.fromDate).format("YYYY-MM-DD");
    const toDateAsString = moment(params.toDate).add(1, "day").format("YYYY-MM-DD");
    dispatch(actions.setShiftRosterIsLoading(true));
    return client.post<KindaShiftRosterView>(`/tenant/${tenantId}/roster/shiftRosterView/for?` +
    `&startDate=${fromDateAsString}&endDate=${toDateAsString}`, params.spotList).then(newShiftRosterView => {
      const shiftRosterView = convertKindaShiftRosterViewToShiftRosterView(newShiftRosterView);
      dispatch(actions.setShiftRosterView(shiftRosterView));
      lastCalledShiftRoster = getShiftRosterFor;
      lastCalledShiftRosterArgs = params;
      dispatch(actions.setShiftRosterIsLoading(false));
    });
  };

export const getCurrentAvailabilityRoster: ThunkCommandFactory<PaginationData, SetAvailabilityRosterIsLoadingAction | SetAvailabilityRosterViewAction> = pagination =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    dispatch(actions.setAvailabilityRosterIsLoading(true));
    return client.get<KindaAvailabilityRosterView>(`/tenant/${tenantId}/roster/availabilityRosterView/current?p=${pagination.pageNumber}&n=${pagination.itemsPerPage}`).then(newAvailabilityRosterView => {
      const availabilityRosterView = convertKindaAvailabilityRosterViewToAvailabilityRosterView(newAvailabilityRosterView);
      dispatch(actions.setAvailabilityRosterView(availabilityRosterView));
      lastCalledAvailabilityRoster = getCurrentAvailabilityRoster;
      lastCalledAvailabilityRosterArgs = pagination;
      dispatch(actions.setAvailabilityRosterIsLoading(false));
    });
  };

export const getAvailabilityRoster: ThunkCommandFactory<RosterSliceInfo & { pagination: PaginationData }, SetAvailabilityRosterIsLoadingAction | SetAvailabilityRosterViewAction> = params =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    const fromDateAsString = moment(params.fromDate).format("YYYY-MM-DD");
    const toDateAsString = moment(params.toDate).add(1, "day").format("YYYY-MM-DD");
    dispatch(actions.setAvailabilityRosterIsLoading(true));
    return client.get<KindaAvailabilityRosterView>(`/tenant/${tenantId}/roster/availabilityRosterView?` +
    `p=${params.pagination.pageNumber}&n=${params.pagination.itemsPerPage}` +
    `&startDate=${fromDateAsString}&endDate=${toDateAsString}`).then(newAvailabilityRosterView => {
      const availabilityRosterView = convertKindaAvailabilityRosterViewToAvailabilityRosterView(newAvailabilityRosterView);
      dispatch(actions.setAvailabilityRosterView(availabilityRosterView));
      lastCalledAvailabilityRoster = getAvailabilityRoster;
      lastCalledAvailabilityRosterArgs = params;
      dispatch(actions.setAvailabilityRosterIsLoading(false));
    });
  };

export const getAvailabilityRosterFor: ThunkCommandFactory<RosterSliceInfo & { employeeList: Employee[] }, SetAvailabilityRosterIsLoadingAction | SetAvailabilityRosterViewAction> = params =>
  (dispatch, state, client) => {
    const tenantId = state().tenantData.currentTenantId;
    const fromDateAsString = moment(params.fromDate).format("YYYY-MM-DD");
    const toDateAsString = moment(params.toDate).add(1, "day").format("YYYY-MM-DD");
    dispatch(actions.setAvailabilityRosterIsLoading(true));
    return client.post<KindaAvailabilityRosterView>(`/tenant/${tenantId}/roster/availabilityRosterView/for?` +
    `&startDate=${fromDateAsString}&endDate=${toDateAsString}`, params.employeeList).then(newAvailabilityRosterView => {
      const availabilityRosterView = convertKindaAvailabilityRosterViewToAvailabilityRosterView(newAvailabilityRosterView);
      dispatch(actions.setAvailabilityRosterView(availabilityRosterView));
      lastCalledAvailabilityRoster = getAvailabilityRosterFor;
      lastCalledAvailabilityRosterArgs = params;
      dispatch(actions.setAvailabilityRosterIsLoading(false));
    });
  };
