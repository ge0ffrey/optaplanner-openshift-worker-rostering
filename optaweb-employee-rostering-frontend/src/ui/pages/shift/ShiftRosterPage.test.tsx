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
import { shallow, mount } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Spot } from 'domain/Spot';
import { Employee } from 'domain/Employee';
import { Shift } from 'domain/Shift';
import { RosterState } from 'domain/RosterState';
import moment from 'moment-timezone';
import 'moment/locale/en-ca';
import color from 'color';
import { useTranslation, Trans } from 'react-i18next';
import { BlueprintIcon, EditIcon, TrashIcon } from '@patternfly/react-icons';
import Actions from 'ui/components/Actions';
import Schedule from 'ui/components/calendar/Schedule';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { getShiftColor } from './ShiftEvent';
import { ShiftRosterPage, Props, ShiftRosterUrlProps } from './ShiftRosterPage';
import ExportScheduleModal from './ExportScheduleModal';

describe('Shift Roster Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render correctly when loaded', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    expect(toJson(shiftRosterPage)).toMatchSnapshot();
  });

  it('should render correctly when loading', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
      isLoading
      allSpotList={[]}
      shownSpotList={[]}
      spotIdToShiftListMap={new Map()}
    />);
    expect(toJson(shiftRosterPage)).toMatchSnapshot();
  });

  it('should render correctly when solving', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
      isSolving
    />);
    expect(toJson(shiftRosterPage)).toMatchSnapshot();
  });

  it('should render correctly when creating a new shift via button', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.find(Actions).prop('actions')
      .filter(a => a.name === 'Trans(i18nKey=createShift)')
      .forEach(a => a.action());
    expect(toJson(shiftRosterPage)).toMatchSnapshot();
  });

  it('should call addShift on addShift', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);
    const newShift: Shift = {
      ...shift,
      id: undefined,
      version: undefined,
      startDateTime: startDate,
      endDateTime: endDate,
    };
    shiftRosterPage.addShift(newShift);
    expect(baseProps.addShift).toBeCalled();
    expect(baseProps.addShift).toBeCalledWith(newShift);
  });

  it('should call updateShift on updateShift', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);
    const updatedShift: Shift = {
      ...shift,
      startDateTime: startDate,
      endDateTime: endDate,
    };
    shiftRosterPage.updateShift(updatedShift);
    expect(baseProps.updateShift).toBeCalled();
    expect(baseProps.updateShift).toBeCalledWith(updatedShift);
  });

  it('should call removeShift on deleteShift', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);
    shiftRosterPage.deleteShift(shift);
    expect(baseProps.removeShift).toBeCalled();
    expect(baseProps.removeShift).toBeCalledWith(shift);
  });

  it('should go to the Spots page if the user click on the link', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
      allSpotList={[]}
      shownSpotList={[]}
      spotIdToShiftListMap={new Map()}
    />);
    mount((shiftRosterPage.find(Trans).prop('components') as any)[2]).simulate('click');
    expect(baseProps.history.push).toBeCalled();
    expect(baseProps.history.push).toBeCalledWith('/0/wards');
  });

  it('should change the week when the user change the week', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    const newDateStart = moment(startDate).add(7, 'days').toDate();
    const newDateEnd = moment(endDate).add(7, 'days').toDate();
    shiftRosterPage.find('[aria-label="Select Week to View"]').simulate('change', newDateStart, newDateEnd);
    expect(baseProps.getShiftRosterFor).toBeCalled();
    expect(baseProps.getShiftRosterFor).toBeCalledWith({
      fromDate: newDateStart,
      toDate: newDateEnd,
      spotList: baseProps.shownSpotList,
    });
  });

  it('should change the spot when the user change the spot', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.find('[aria-label="Select Spot"]').simulate('change', newSpot);
    expect(baseProps.getShiftRosterFor).toBeCalled();
    expect(baseProps.getShiftRosterFor).toBeCalledWith({
      fromDate: startDate,
      toDate: endDate,
      spotList: [newSpot],
    });
  });

  it('should call publishRoster when the publish button is clicked', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.find(Actions).prop('actions')
      .filter(a => a.name === 'Trans(i18nKey=publish)')
      .forEach(a => a.action());
    expect(baseProps.publishRoster).toBeCalled();
  });

  it('should open the export schedule modal when export is clicked', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.find(Actions).prop('actions')
      .filter(a => a.name === 'Trans(i18nKey=exportToExcel)')
      .forEach(a => a.action());
    expect(shiftRosterPage.find(ExportScheduleModal).prop('isOpen')).toEqual(true);
  });

  it('should close the export schedule modal when export is closed', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.find(Actions).prop('actions')
      .filter(a => a.name === 'Trans(i18nKey=exportToExcel)')
      .forEach(a => a.action());
    expect(shiftRosterPage.find(ExportScheduleModal).simulate('close'));
    expect(shiftRosterPage.find(ExportScheduleModal).prop('isOpen')).toEqual(false);
  });

  it('should call solveRoster when the schedule button is clicked', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.find(Actions).prop('actions')
      .filter(a => a.name === 'Trans(i18nKey=schedule)')
      .forEach(a => a.action());
    expect(baseProps.solveRoster).toBeCalled();
  });

  it('should call terminateSolvingRosterEarly when the Terminate Early button is clicked', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
      isSolving
    />);
    shiftRosterPage.find(Actions).prop('actions')
      .filter(a => a.name === 'Trans(i18nKey=terminateEarly)')
      .forEach(a => a.action());
    expect(baseProps.terminateSolvingRosterEarly).toBeCalled();
  });

  it('should call refreshShiftRoster and show an info message when the refresh button is clicked', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.find(Actions).prop('actions')
      .filter(a => a.name === 'Trans(i18nKey=refresh)')
      .forEach(a => a.action());
    expect(baseProps.refreshShiftRoster).toBeCalled();
    expect(baseProps.showInfoMessage).toBeCalled();
    expect(baseProps.showInfoMessage).toBeCalledWith('shiftRosterRefresh');
  });

  it('call deleteShift when the EditShiftModal delete a shift', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.setState({
      selectedShift: shift,
      isCreatingOrEditingShift: true,
    });
    shiftRosterPage.find('[aria-label="Edit Shift"]').simulate('delete', shift);
    expect(baseProps.removeShift).toBeCalled();
    expect(baseProps.removeShift).toBeCalledWith(shift);
    expect(shiftRosterPage.state('isCreatingOrEditingShift')).toEqual(false);
  });

  it('call addShift when the EditShiftModal add a new shift', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.setState({
      isCreatingOrEditingShift: true,
    });
    const newShift: Shift = {
      ...shift,
      id: undefined,
      version: undefined,
      startDateTime: startDate,
      endDateTime: endDate,
    };
    shiftRosterPage.find('[aria-label="Edit Shift"]').simulate('save', newShift);
    expect(baseProps.addShift).toBeCalled();
    expect(baseProps.addShift).toBeCalledWith(newShift);
    expect(shiftRosterPage.state('isCreatingOrEditingShift')).toEqual(false);
  });

  it('call updateShift when the EditShiftModal updates a shift', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.setState({
      selectedShift: shift,
      isCreatingOrEditingShift: true,
    });
    const newShift: Shift = {
      ...shift,
      startDateTime: startDate,
      endDateTime: endDate,
    };
    shiftRosterPage.find('[aria-label="Edit Shift"]').simulate('save', newShift);
    expect(baseProps.updateShift).toBeCalled();
    expect(baseProps.updateShift).toBeCalledWith(newShift);
    expect(shiftRosterPage.state('isCreatingOrEditingShift')).toEqual(false);
  });

  it('should set isEditingOrCreatingShift to false when closed', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.setState({
      isCreatingOrEditingShift: true,
    });
    shiftRosterPage.find('[aria-label="Edit Shift"]').simulate('close');
    expect(shiftRosterPage.state('isCreatingOrEditingShift')).toEqual(false);
  });

  it('should call addShift when a timeslot is selected', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    const newDateStart = moment(startDate).add(7, 'days').toDate();
    const newDateEnd = moment(endDate).add(7, 'days').toDate();
    shiftRosterPage.find(Schedule).simulate('addEvent', newDateStart,
      newDateEnd);

    expect(baseProps.addShift).toBeCalled();
    expect(baseProps.addShift).toBeCalledWith({
      tenantId: spot.tenantId,
      startDateTime: newDateStart,
      endDateTime: newDateEnd,
      spot,
      requiredSkillSet: [],
      originalEmployee: null,
      employee: null,
      rotationEmployee: null,
      pinnedByUser: false,
    });
  });

  it('should call updateShift when an event is updated', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    const newDateStart = moment(startDate).add(7, 'days').toDate();
    const newDateEnd = moment(endDate).add(7, 'days').toDate();
    shiftRosterPage.find(Schedule).simulate('updateEvent', shift, newDateStart,
      newDateEnd);

    expect(baseProps.updateShift).toBeCalled();
    expect(baseProps.updateShift).toBeCalledWith({
      ...shift,
      startDateTime: newDateStart,
      endDateTime: newDateEnd,
    });
  });

  it('should saturate the color and have a solid border if the shift is published', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);
    const publishedShift: Shift = {
      ...shift,
      startDateTime: moment(startDate).subtract('1', 'day').toDate(),
    };

    const style = shiftRosterPage.getShiftStyle(publishedShift);
    expect(style).toEqual({
      style: {
        border: '1px solid',
        backgroundColor: color(getShiftColor(publishedShift)).hex(),
        opacity: 0.3,
      },
    });
  });

  it('should keep the color and have a dashed border if the shift is published', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);

    const style = shiftRosterPage.getShiftStyle(shift);
    expect(style).toEqual({
      style: {
        border: '1px dashed',
        backgroundColor: getShiftColor(shift),
      },
    });
  });

  it('day should be white if it is draft', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);

    const style = shiftRosterPage.getDayStyle(endDate);
    expect(style).toEqual({
      className: 'draft-day',
      style: {
        backgroundColor: 'var(--pf-global--BackgroundColor--100)',
      },
    });
  });

  it('day should be gray if it is published', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);

    const style = shiftRosterPage.getDayStyle(moment(startDate).subtract(1, 'day').toDate());
    expect(style).toEqual({
      className: 'published-day',
      style: {
        backgroundColor: 'var(--pf-global--BackgroundColor--300)',
      },
    });
  });

  it('should bring up the Edit Shift modal when the edit button is clicked on a shift', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage {...baseProps} />);
    const PopoverHeaderComponent: React.FC<Shift> = shiftRosterPage.find(Schedule).prop('popoverHeader');
    const popoverHeader = shallow(<PopoverHeaderComponent {...shift} />);
    popoverHeader.find(EditIcon).parent().simulate('click');
    expect(shiftRosterPage.state('isCreatingOrEditingShift')).toEqual(true);
    expect(shiftRosterPage.state('selectedShift')).toEqual(shift);
  });

  it('should duplicate a shift when the copy button is clicked on a shift', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage {...baseProps} />);
    const PopoverHeaderComponent: React.FC<Shift> = shiftRosterPage.find(Schedule).prop('popoverHeader');
    const popoverHeader = shallow(<PopoverHeaderComponent {...shift} />);
    popoverHeader.find(BlueprintIcon).parent().simulate('click');
    expect(baseProps.addShift).toBeCalledWith({
      ...shift,
      employee: null,
      id: undefined,
      version: undefined,
    });
  });

  it('should delete the shift when the delete button is clicked on a shift', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage {...baseProps} />);
    const PopoverHeaderComponent: React.FC<Shift> = shiftRosterPage.find(Schedule).prop('popoverHeader');
    const popoverHeader = shallow(<PopoverHeaderComponent {...shift} />);
    popoverHeader.find(TrashIcon).parent().simulate('click');
    expect(baseProps.removeShift).toBeCalledWith(shift);
  });
});

const spot: Spot = {
  tenantId: 0,
  id: 2,
  version: 0,
  name: 'Spot',
  requiredSkillSet: [
    {
      tenantId: 0,
      id: 3,
      version: 0,
      name: 'Skill',
    },
  ],
};

const newSpot: Spot = {
  ...spot,
  id: 111,
  name: 'New Spot',
};

const employee: Employee = {
  tenantId: 0,
  id: 4,
  version: 0,
  name: 'Employee 1',
  contract: {
    tenantId: 0,
    id: 5,
    version: 0,
    name: 'Basic Contract',
    maximumMinutesPerDay: 10,
    maximumMinutesPerWeek: 70,
    maximumMinutesPerMonth: 500,
    maximumMinutesPerYear: 6000,
  },
  skillProficiencySet: [{
    tenantId: 0,
    id: 6,
    version: 0,
    name: 'Not Required Skill',
  }],
};

const shift: Shift = {
  tenantId: 0,
  id: 1,
  version: 0,
  startDateTime: moment('2018-07-01T09:00').toDate(),
  endDateTime: moment('2018-07-01T17:00').toDate(),
  spot,
  requiredSkillSet: [],
  originalEmployee: null,
  employee,
  rotationEmployee: {
    ...employee,
    id: 7,
    name: 'Rotation Employee',
  },
  pinnedByUser: false,
  indictmentScore: { hardScore: 0, mediumScore: 0, softScore: 0 },
  requiredSkillViolationList: [],
  unavailableEmployeeViolationList: [],
  shiftEmployeeConflictList: [],
  desiredTimeslotForEmployeeRewardList: [],
  undesiredTimeslotForEmployeePenaltyList: [],
  rotationViolationPenaltyList: [],
  unassignedShiftPenaltyList: [],
  contractMinutesViolationPenaltyList: [],
};

const startDate = moment('2018-07-01T09:00').startOf('week').toDate();
const endDate = moment('2018-07-01T09:00').endOf('week').toDate();

const rosterState: RosterState = {
  tenant: {
    id: 0,
    version: 0,
    name: 'Tenant',
  },
  publishNotice: 14,
  publishLength: 7,
  firstDraftDate: new Date('2018-07-01'),
  draftLength: 7,
  unplannedRotationOffset: 0,
  rotationLength: 7,
  lastHistoricDate: new Date('2018-07-01'),
  timeZone: 'EST',
};

const baseProps: Props = {
  ...useTranslation('ShiftRosterPage'),
  tenantId: 0,
  score: { hardScore: 0, mediumScore: 0, softScore: 0 },
  indictmentSummary: {
    constraintToCountMap: {},
    constraintToScoreImpactMap: {},
  },
  tReady: true,
  isSolving: false,
  isLoading: false,
  allSpotList: [spot, newSpot],
  shownSpotList: [spot],
  spotIdToShiftListMap: new Map<number, Shift[]>([
    [2, [shift]],
  ]),
  totalNumOfSpots: 1,
  rosterState,
  addShift: jest.fn(),
  removeShift: jest.fn(),
  updateShift: jest.fn(),
  getShiftRosterFor: jest.fn(),
  refreshShiftRoster: jest.fn(),
  solveRoster: jest.fn(),
  publishRoster: jest.fn(),
  terminateSolvingRosterEarly: jest.fn(),
  showInfoMessage: jest.fn(),
  ...getRouterProps<ShiftRosterUrlProps>('/shift', { spot: 'Spot', week: '2018-07-01' }),
};
