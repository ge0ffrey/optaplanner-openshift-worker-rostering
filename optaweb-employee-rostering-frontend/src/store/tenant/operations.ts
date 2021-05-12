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

import { Tenant } from 'domain/Tenant';
import moment from 'moment';
import { ThunkDispatch } from 'redux-thunk';
import { Action } from 'redux';
import { rosterOperations } from 'store/roster';
import { skillOperations } from 'store/skill';
import { spotOperations } from 'store/spot';
import { contractOperations } from 'store/contract';
import { employeeOperations } from 'store/employee';
import { timeBucketOperations } from 'store/rotation';
import * as rosterActions from 'store/roster/actions';
import * as skillActions from 'store/skill/actions';
import * as spotActions from 'store/spot/actions';
import * as contractActions from 'store/contract/actions';
import * as employeeActions from 'store/employee/actions';
import * as timeBucketActions from 'store/rotation/actions';
import { alert } from 'store/alert';
import { RosterState } from 'domain/RosterState';
import { AddAlertAction } from 'store/alert/types';
import { RouteComponentProps } from 'react-router';
import { setTenantIdInUrl } from 'util/BookmarkableUtils';
import {
  ChangeTenantAction, RefreshTenantListAction, RefreshSupportedTimezoneListAction,
  AddTenantAction, RemoveTenantAction,
} from './types';
import * as actions from './actions';
import { ThunkCommandFactory } from '../types';

function refreshData(dispatch: ThunkDispatch<any, any, Action<any>>): Promise<any> {
  dispatch(rosterActions.setShiftRosterIsLoading(true));
  dispatch(rosterActions.setAvailabilityRosterIsLoading(true));
  dispatch(rosterActions.setRosterStateIsLoading(true));
  dispatch(skillActions.setIsSkillListLoading(true));
  dispatch(spotActions.setIsSpotListLoading(true));
  dispatch(contractActions.setIsContractListLoading(true));
  dispatch(employeeActions.setIsEmployeeListLoading(true));
  dispatch(timeBucketActions.setIsTimeBucketListLoading(true));
  return Promise.all([
    dispatch(skillOperations.refreshSkillList()),
    dispatch(rosterOperations.getRosterState()),
    dispatch(rosterOperations.getSolverStatus()),
    dispatch(spotOperations.refreshSpotList()),
    dispatch(contractOperations.refreshContractList()),
    dispatch(employeeOperations.refreshEmployeeList()),
    dispatch(timeBucketOperations.refreshTimeBucketList()),
  ]);
}

export const changeTenant: ThunkCommandFactory<{ tenantId: number; routeProps: RouteComponentProps },
ChangeTenantAction> = params => (dispatch) => {
  rosterOperations.resetSolverStatus();
  dispatch(actions.changeTenant(params.tenantId));
  setTenantIdInUrl(params.routeProps, params.tenantId);
  return refreshData(dispatch);
};


let pollForTenantListTimeout: number | null = null;
export const refreshTenantList:
ThunkCommandFactory<void, RefreshTenantListAction | any> = () => (dispatch, state, client) => (
  client.get<Tenant[]>('/tenant/').then((tenantList) => {
    const { currentTenantId } = state().tenantData;
    if (tenantList.filter(tenant => tenant.id === currentTenantId).length !== 0) {
      dispatch(actions.refreshTenantList({ tenantList, currentTenantId }));
      refreshData(dispatch);
    } else if (tenantList.length > 0) {
      dispatch(actions.refreshTenantList({ tenantList, currentTenantId: tenantList[0].id as number }));
      refreshData(dispatch);
    } else {
      // TODO: this case occurs iff there are no tenants; need a special screen for that
      // Tenant Id cannot be negative, so use -1 as a special value to signal no tenant to abort
      // operations that happen automatically (namely roster fetches)
      dispatch(actions.refreshTenantList({ tenantList, currentTenantId: -1 }));
      dispatch(rosterActions.setShiftRosterIsLoading(true));
      dispatch(rosterActions.setAvailabilityRosterIsLoading(true));
      dispatch(rosterActions.setRosterStateIsLoading(true));
      dispatch(skillActions.setIsSkillListLoading(true));
      dispatch(spotActions.setIsSpotListLoading(true));
      dispatch(contractActions.setIsContractListLoading(true));
      dispatch(employeeActions.setIsEmployeeListLoading(true));
      dispatch(timeBucketActions.setIsTimeBucketListLoading(true));
      if (pollForTenantListTimeout === null) {
        pollForTenantListTimeout = window.setTimeout(() => {
          pollForTenantListTimeout = null;
          dispatch(refreshTenantList());
        }, 1000);
      }
    }
  }));

export const addTenant:
ThunkCommandFactory<RosterState, AddTenantAction | AddAlertAction> = rs => (dispatch, state, client) => (
  client.post<Tenant>('/tenant/add', {
    ...rs,
    firstDraftDate: moment(rs.firstDraftDate).local(false).format('YYYY-MM-DD'),
    lastHistoricDate: moment(rs.lastHistoricDate).local(false).format('YYYY-MM-DD'),
  }).then((tenant) => {
    dispatch(alert.showSuccessMessage('addTenant', { name: tenant.name }));
    dispatch(actions.addTenant(tenant));
  })
);

export const removeTenant:
ThunkCommandFactory<Tenant, RemoveTenantAction | AddAlertAction> = tenant => (dispatch, state, client) => (
  client.post<boolean>(`/tenant/remove/${tenant.id}`, {}).then((isSuccess) => {
    if (isSuccess) {
      dispatch(alert.showSuccessMessage('removeTenant', { name: tenant.name }));
      dispatch(actions.removeTenant(tenant));
    } else {
      dispatch(alert.showErrorMessage('removeTenantError', { name: tenant.name }));
    }
  })
);

export const refreshSupportedTimezones:
ThunkCommandFactory<void, RefreshSupportedTimezoneListAction> = () => (dispatch, state, client) => (
  client.get<string[]>('/tenant/supported/timezones').then((supportedTimezones) => {
    dispatch(actions.refreshSupportedTimezones(supportedTimezones));
  })
);
