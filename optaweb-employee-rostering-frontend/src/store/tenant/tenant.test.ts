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

import { alert } from 'store/alert';
import * as timeBucketActions from 'store/rotation/actions';
import { onGet, onPost } from 'store/rest/RestTestUtils';
import { Tenant } from 'domain/Tenant';
import moment from 'moment';
import { timeBucketOperations } from 'store/rotation';
import { RosterState } from 'domain/RosterState';
import { flushPromises } from 'setupTests';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { doNothing, error } from 'types';
import { Map, List } from 'immutable';
import { createIdMapFromList } from 'util/ImmutableCollectionOperations';
import { mockStore } from '../mockStore';
import { AppState } from '../types';
import * as actions from './actions';
import * as skillActions from '../skill/actions';
import * as spotActions from '../spot/actions';
import * as contractActions from '../contract/actions';
import * as employeeActions from '../employee/actions';
import * as rosterActions from '../roster/actions';
import * as skillOperations from '../skill/operations';
import * as spotOperations from '../spot/operations';
import * as contractOperations from '../contract/operations';
import * as employeeOperations from '../employee/operations';
import * as rosterOperations from '../roster/operations';
import reducer, { tenantOperations } from './index';

const state: Partial<AppState> = {
  tenantData: {
    currentTenantId: 0,
    tenantList: List([
      {
        id: 0,
        version: 0,
        name: 'Tenant 0',
      },
      {
        id: 1,
        version: 0,
        name: 'Tenant 1',
      },
    ]),
    timezoneList: ['America/Toronto'],
  },
  isConnected: true,
};

describe('Tenant operations', () => {
  const mockRefreshSkillList = jest.spyOn(skillOperations, 'refreshSkillList');
  const mockRefreshSpotList = jest.spyOn(spotOperations, 'refreshSpotList');
  const mockRefreshContractList = jest.spyOn(contractOperations, 'refreshContractList');
  const mockRefreshEmployeeList = jest.spyOn(employeeOperations, 'refreshEmployeeList');
  const mockRefreshTimeBucketList = jest.spyOn(timeBucketOperations, 'refreshTimeBucketList');
  const mockRefreshRosterState = jest.spyOn(rosterOperations, 'getRosterState');

  const loadingActions = [
    skillActions.setIsSkillListLoading(true),
    spotActions.setIsSpotListLoading(true),
    contractActions.setIsContractListLoading(true),
    employeeActions.setIsEmployeeListLoading(true),
    rosterActions.setAvailabilityRosterIsLoading(true),
    rosterActions.setRosterStateIsLoading(true),
    rosterActions.setShiftRosterIsLoading(true),
    timeBucketActions.setIsTimeBucketListLoading(true),
  ];

  beforeAll(() => {
    mockRefreshSkillList.mockImplementation(() => doNothing);
    mockRefreshSpotList.mockImplementation(() => doNothing);
    mockRefreshContractList.mockImplementation(() => doNothing);
    mockRefreshEmployeeList.mockImplementation(() => doNothing);
    mockRefreshTimeBucketList.mockImplementation(() => doNothing);
    mockRefreshRosterState.mockImplementation(() => doNothing);
  });

  afterAll(() => {
    mockRefreshSkillList.mockRestore();
    mockRefreshSpotList.mockRestore();
    mockRefreshContractList.mockRestore();
    mockRefreshEmployeeList.mockRestore();
    mockRefreshTimeBucketList.mockRestore();
    mockRefreshRosterState.mockRestore();
  });

  it('should dispatch actions and change tenant when current tenant not in refreshed the tenant list ',
    async () => {
      const { store, client } = mockStore(state);
      const mockTenantList: Tenant[] = [{
        id: 1,
        version: 0,
        name: 'Tenant 1',
      },
      {
        id: 2,
        version: 0,
        name: 'Tenant 2',
      },
      {
        id: 3,
        version: 0,
        name: 'Tenant 3',
      }];
      onGet('/tenant/', mockTenantList);

      await store.dispatch(tenantOperations.refreshTenantList());
      await flushPromises();

      expect(store.getActions()).toEqual(expect.arrayContaining([
        actions.refreshTenantList({ currentTenantId: 1, tenantList: mockTenantList }),
        ...loadingActions,
      ]));

      expect(client.get).toHaveBeenCalledTimes(2);
      expect(client.get).toHaveBeenCalledWith('/tenant/');
      expect(client.get).toHaveBeenCalledWith('/tenant/0/roster/status');

      expect(mockRefreshSkillList).toBeCalled();
      expect(mockRefreshSpotList).toBeCalled();
      expect(mockRefreshContractList).toBeCalled();
      expect(mockRefreshEmployeeList).toBeCalled();
      expect(mockRefreshRosterState).toBeCalled();
      expect(mockRefreshTimeBucketList).toBeCalled();
    });

  it('should dispatch actions and change tenant to -1 when tenant list is empty',
    async () => {
      const { store, client } = mockStore(state);
      const mockTenantList: Tenant[] = [];
      onGet('/tenant/', mockTenantList);

      await store.dispatch(tenantOperations.refreshTenantList());
      await flushPromises();

      expect(store.getActions()).toEqual(expect.arrayContaining([
        actions.refreshTenantList({ currentTenantId: -1, tenantList: [] }),
        ...loadingActions,
      ]));

      expect(client.get).toHaveBeenCalledTimes(1);
      expect(client.get).toHaveBeenCalledWith('/tenant/');

      expect(mockRefreshSkillList).not.toBeCalled();
      expect(mockRefreshSpotList).not.toBeCalled();
      expect(mockRefreshContractList).not.toBeCalled();
      expect(mockRefreshEmployeeList).not.toBeCalled();
      expect(mockRefreshRosterState).not.toBeCalled();
      expect(mockRefreshTimeBucketList).not.toBeCalled();
    });

  it('should dispatch actions and not change tenant when current tenant is in refreshed tenant list',
    async () => {
      const { store, client } = mockStore(state);
      const mockTenantList: Tenant[] = [{
        id: 1,
        version: 0,
        name: 'Tenant 1',
      },
      {
        id: 0,
        version: 0,
        name: 'Tenant 2',
      },
      {
        id: 3,
        version: 0,
        name: 'Tenant 3',
      }];
      onGet('/tenant/', mockTenantList);

      mockTenantList[1].id = 0;

      await store.dispatch(tenantOperations.refreshTenantList());
      await flushPromises();

      expect(store.getActions()).toEqual(expect.arrayContaining([
        actions.refreshTenantList({ currentTenantId: 0, tenantList: mockTenantList }),
        ...loadingActions,
      ]));

      expect(client.get).toHaveBeenCalledTimes(2);
      expect(client.get).toHaveBeenCalledWith('/tenant/');
      expect(client.get).toHaveBeenCalledWith('/tenant/0/roster/status');

      expect(mockRefreshSkillList).toBeCalled();
      expect(mockRefreshSpotList).toBeCalled();
      expect(mockRefreshContractList).toBeCalled();
      expect(mockRefreshEmployeeList).toBeCalled();
      expect(mockRefreshRosterState).toBeCalled();
      expect(mockRefreshTimeBucketList).toBeCalled();
    });

  it('should change the tenant and refresh all lists on change tenant', async () => {
    const { store, client } = mockStore(state);
    await store.dispatch(tenantOperations.changeTenant({
      routeProps: getRouterProps('/test', {}),
      tenantId: 0,
    }));

    expect(store.getActions()).toEqual(expect.arrayContaining([
      actions.changeTenant(0),
      ...loadingActions,
    ]));

    expect(client.get).toBeCalledTimes(1);
    expect(client.get).toBeCalledWith('/tenant/0/roster/status');

    expect(mockRefreshSkillList).toBeCalled();
    expect(mockRefreshSpotList).toBeCalled();
    expect(mockRefreshContractList).toBeCalled();
    expect(mockRefreshEmployeeList).toBeCalled();
    expect(mockRefreshRosterState).toBeCalled();
    expect(mockRefreshTimeBucketList).toBeCalled();
  });

  it('should get the Shift Roster for a spot in the spot list and get the Availability Roster for an '
  + 'employee in the employee list if roster state is not null', async () => {
    const mockTenantList: Tenant[] = [{
      id: 1,
      version: 0,
      name: 'Tenant 1',
    },
    {
      id: 0,
      version: 0,
      name: 'Tenant 2',
    },
    {
      id: 3,
      version: 0,
      name: 'Tenant 3',
    }];

    mockTenantList[1].id = 0;
    const date = moment('2018-01-01', 'YYYY-MM-DD').toDate();
    const newState: AppState = {
      tenantData: {
        currentTenantId: 0,
        tenantList: List([
          {
            id: 0,
            version: 0,
            name: 'Tenant 0',
          },
          {
            id: 1,
            version: 0,
            name: 'Tenant 1',
          },
        ]),
        timezoneList: ['America/Toronto'],
      },
      employeeList: {
        isLoading: false,
        employeeMapById: createIdMapFromList([
          {
            tenantId: 0,
            id: 10,
            version: 0,
            name: 'Amy',
            contract: 0,
            skillProficiencySet: [],
            shortId: 'A',
            color: '#FFFFFF',
          },
        ]),
      },
      contractList: {
        isLoading: false,
        contractMapById: Map(),
      },
      spotList: {
        isLoading: false,
        spotMapById: Map(),
      },
      skillList: {
        isLoading: false,
        skillMapById: Map(),
      },
      timeBucketList: {
        isLoading: false,
        timeBucketMapById: Map(),
      },
      rosterState: {
        isLoading: false,
        rosterState: {
          publishNotice: 7,
          publishLength: 7,
          draftLength: 2,
          unplannedRotationOffset: 0,
          rotationLength: 1,
          lastHistoricDate: date,
          firstDraftDate: date,
          timeZone: 'NA',
          tenant: mockTenantList[1],
        },
      },
      shiftRoster: {
        isLoading: true,
        shiftRosterView: null,
      },
      availabilityRoster: {
        isLoading: true,
        availabilityRosterView: null,
      },
      solverState: {
        solverStatus: 'NOT_SOLVING',
      },
      alerts: {
        alertList: List(),
        idGeneratorIndex: 0,
      },
      isConnected: true,
    };

    const { store, client } = mockStore(newState);
    onGet('/tenant/', mockTenantList);

    await store.dispatch(tenantOperations.refreshTenantList());
    await flushPromises();

    expect(store.getActions()).toEqual(expect.arrayContaining([
      actions.refreshTenantList({ currentTenantId: 0, tenantList: mockTenantList }),
      ...loadingActions,
    ]));

    expect(client.get).toHaveBeenCalledTimes(2);
    expect(client.get).toHaveBeenCalledWith('/tenant/');
    expect(client.get).toHaveBeenCalledWith('/tenant/0/roster/status');

    expect(mockRefreshSkillList).toBeCalled();
    expect(mockRefreshSpotList).toBeCalled();
    expect(mockRefreshContractList).toBeCalled();
    expect(mockRefreshEmployeeList).toBeCalled();
    expect(mockRefreshRosterState).toBeCalled();
    expect(mockRefreshTimeBucketList).toBeCalled();
  });

  it('should call client and dispatch actions on addTenant', async () => {
    const { store, client } = mockStore(state);
    const initialRosterState: RosterState = {
      publishNotice: 7,
      firstDraftDate: '2018-01-01' as unknown as Date,
      publishLength: 7,
      draftLength: 7,
      unplannedRotationOffset: 0,
      rotationLength: 7,
      lastHistoricDate: '2018-01-01' as unknown as Date,
      timeZone: 'America/Toronto',
      tenant: {
        name: 'New Tenant',
      },
    };

    onPost('/tenant/add', initialRosterState, {
      ...initialRosterState.tenant,
      id: 2,
      version: 0,
    });

    await store.dispatch(tenantOperations.addTenant(initialRosterState));

    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('addTenant', { name: 'New Tenant' }),
      actions.addTenant({ ...initialRosterState.tenant, id: 2, version: 0 }),
    ]);

    expect(client.post).toBeCalled();
    expect(client.post).toBeCalledWith('/tenant/add', initialRosterState);
  });


  it('should call client and dispatch actions on removeTenant', async () => {
    const { store, client } = mockStore(state);
    const tenantToDelete: Tenant = {
      id: 2,
      version: 0,
      name: 'Deleted Tenant',
    };

    onPost(`/tenant/remove/${tenantToDelete.id}`, {}, true);

    await store.dispatch(tenantOperations.removeTenant(tenantToDelete));

    expect(store.getActions()).toEqual([
      alert.showSuccessMessage('removeTenant', { name: 'Deleted Tenant' }),
      actions.removeTenant(tenantToDelete),
    ]);

    expect(client.post).toBeCalled();
    expect(client.post).toBeCalledWith(`/tenant/remove/${tenantToDelete.id}`, {});
  });

  it('should call client and show an alert on failed removeTenant', async () => {
    const { store, client } = mockStore(state);
    const tenantToDelete: Tenant = {
      id: 2,
      version: 0,
      name: 'Deleted Tenant',
    };

    onPost(`/tenant/remove/${tenantToDelete.id}`, {}, false);

    await store.dispatch(tenantOperations.removeTenant(tenantToDelete));

    expect(store.getActions()).toEqual([
      alert.showErrorMessage('removeTenantError', { name: tenantToDelete.name }),
    ]);

    expect(client.post).toBeCalled();
    expect(client.post).toBeCalledWith(`/tenant/remove/${tenantToDelete.id}`, {});
  });

  it('should call client and dispatch actions on refreshSupportedTimezones', async () => {
    const { store, client } = mockStore(state);

    onGet('/tenant/supported/timezones', ['My/Timezone']);

    await store.dispatch(tenantOperations.refreshSupportedTimezones());

    expect(store.getActions()).toEqual([
      actions.refreshSupportedTimezones(['My/Timezone']),
    ]);

    expect(client.get).toBeCalled();
    expect(client.get).toBeCalledWith('/tenant/supported/timezones');
  });
});

describe('Tenant reducers', () => {
  const newTenantList: Tenant[] = [{
    id: 1,
    version: 0,
    name: 'Tenant 1',
  },
  {
    id: 2,
    version: 0,
    name: 'Tenant 2',
  },
  {
    id: 3,
    version: 0,
    name: 'Tenant 3',
  }];
  const { store } = mockStore(state);
  const storeState = store.getState();

  it('refresh timezone list', () => {
    expect(
      reducer(storeState.tenantData, actions.refreshSupportedTimezones(['New/Timezone'])),
    ).toEqual({ ...storeState.tenantData, timezoneList: ['New/Timezone'] });
  });
  it('refresh tenant list', () => {
    expect(
      reducer(storeState.tenantData, actions.refreshTenantList({ currentTenantId: 1, tenantList: newTenantList })),
    ).toEqual({ ...storeState.tenantData, currentTenantId: 1, tenantList: List(newTenantList) });
  });
  it('change tenant', () => {
    expect(
      reducer(storeState.tenantData, actions.changeTenant(1)),
    ).toEqual({ ...storeState.tenantData, currentTenantId: 1 });
  });
  it('addTenant', () => {
    expect(
      reducer(storeState.tenantData, actions.addTenant(newTenantList[2])),
    ).toEqual({ ...storeState.tenantData,
      tenantList: storeState.tenantData.tenantList.set(2, newTenantList[2]) });
  });
  it('removeTenant', () => {
    expect(
      reducer(storeState.tenantData, actions.removeTenant(storeState.tenantData.tenantList.get(0) ?? error())),
    ).toEqual({
      ...storeState.tenantData,
      tenantList: storeState.tenantData.tenantList.delete(0),
    });
  });
});
