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
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import Spot from 'domain/Spot';
import Employee from 'domain/Employee';
import Shift from 'domain/Shift';
import { EventWrapper, ShiftRosterPage, Props } from './ShiftRosterPage';
import RosterState from 'domain/RosterState';
import moment from 'moment-timezone';
import "moment/locale/en-ca";
import { getShiftColor } from './ShiftEvent';
import color from 'color';

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
    shiftRosterPage.find('Button[aria-label="Create Shift"]').simulate("click");
    expect(toJson(shiftRosterPage)).toMatchSnapshot();
  });

  it('should call getShiftRosterFor on onDateChange', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);
    const newDateStart = moment(startDate).add(7, "days").toDate();
    const newDateEnd = moment(endDate).add(7, "days").toDate();
    shiftRosterPage.onDateChange(newDateStart, newDateEnd);
    expect(baseProps.getShiftRosterFor).toBeCalled();
    expect(baseProps.getShiftRosterFor).toBeCalledWith({
      fromDate: newDateStart,
      toDate: newDateEnd,
      spotList: baseProps.shownSpotList
    });
  });

  it('should call getShiftRosterFor on spot change', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);
    const newSpot: Spot = {
      ...spot,
      id: 111,
      name: "New Spot"
    };

    shiftRosterPage.onUpdateSpotList(newSpot);
    expect(baseProps.getShiftRosterFor).toBeCalled();
    expect(baseProps.getShiftRosterFor).toBeCalledWith({
      fromDate: baseProps.startDate,
      toDate: baseProps.endDate,
      spotList: [newSpot]
    });
  });

  it('should not call getShiftRosterFor on spot change if invalid', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);
    shiftRosterPage.onUpdateSpotList(undefined);
    expect(baseProps.getShiftRosterFor).not.toBeCalled();
  });

  it('should call addShift on addShift', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);
    const newShift: Shift = {
      ...shift,
      id: undefined,
      version: undefined,
      startDateTime: startDate,
      endDateTime: endDate
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
      endDateTime: endDate
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

  it('should change the week when the user change the week', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    const newDateStart = moment(startDate).add(7, "days").toDate();
    const newDateEnd = moment(endDate).add(7, "days").toDate();
    shiftRosterPage.find('WeekPicker[aria-label="Select Week to View"]').simulate("change", newDateStart, newDateEnd);
    expect(baseProps.getShiftRosterFor).toBeCalled();
    expect(baseProps.getShiftRosterFor).toBeCalledWith({
      fromDate: newDateStart,
      toDate: newDateEnd,
      spotList: baseProps.shownSpotList
    });
  });

  it('should change the spot when the user change the spot', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    const newSpot: Spot = {
      ...spot,
      id: 111,
      name: "New Spot"
    };
    shiftRosterPage.find('TypeaheadSelectInput[aria-label="Select Spot"]').simulate("change", newSpot);
    expect(baseProps.getShiftRosterFor).toBeCalled();
    expect(baseProps.getShiftRosterFor).toBeCalledWith({
      fromDate: baseProps.startDate,
      toDate: baseProps.endDate,
      spotList: [newSpot]
    });
  });

  it('should call publishRoster when the publish button is clicked', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.find('Button[aria-label="Publish"]').simulate("click");
    expect(baseProps.publishRoster).toBeCalled();
  });

  it('should call solveRoster when the solve button is clicked', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.find('Button[aria-label="Solve"]').simulate("click");
    expect(baseProps.solveRoster).toBeCalled();
  });

  it('should call terminateSolvingRosterEarly when the Terminate Early button is clicked', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
      isSolving
    />);
    shiftRosterPage.find('Button[aria-label="Terminate Early"]').simulate("click");
    expect(baseProps.terminateSolvingRosterEarly).toBeCalled();
  });

  it('should call refreshShiftRoster and show an info message when the refresh button is clicked', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.find('Button[aria-label="Refresh"]').simulate("click");
    expect(baseProps.refreshShiftRoster).toBeCalled();
    expect(baseProps.showInfoMessage).toBeCalled();
    expect(baseProps.showInfoMessage).toBeCalledWith("shiftRosterRefresh");
  });

  it('call deleteShift when the EditShiftModal delete a shift', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.setState({
      selectedShift: shift,
      isCreatingOrEditingShift: true
    });
    shiftRosterPage.find('Connect(EditShiftModal)[aria-label="Edit Shift"]').simulate("delete", shift);
    expect(baseProps.removeShift).toBeCalled();
    expect(baseProps.removeShift).toBeCalledWith(shift);
    expect(shiftRosterPage.state("isCreatingOrEditingShift")).toEqual(false);
  });

  it('call addShift when the EditShiftModal add a new shift', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.setState({
      isCreatingOrEditingShift: true
    });
    const newShift: Shift = {
      ...shift,
      id: undefined,
      version: undefined,
      startDateTime: startDate,
      endDateTime: endDate
    };
    shiftRosterPage.find('Connect(EditShiftModal)[aria-label="Edit Shift"]').simulate("save", newShift);
    expect(baseProps.addShift).toBeCalled();
    expect(baseProps.addShift).toBeCalledWith(newShift);
    expect(shiftRosterPage.state("isCreatingOrEditingShift")).toEqual(false);
  });

  it('call updateShift when the EditShiftModal updates a shift', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.setState({
      selectedShift: shift,
      isCreatingOrEditingShift: true
    });
    const newShift: Shift = {
      ...shift,
      startDateTime: startDate,
      endDateTime: endDate
    };
    shiftRosterPage.find('Connect(EditShiftModal)[aria-label="Edit Shift"]').simulate("save", newShift);
    expect(baseProps.updateShift).toBeCalled();
    expect(baseProps.updateShift).toBeCalledWith(newShift);
    expect(shiftRosterPage.state("isCreatingOrEditingShift")).toEqual(false);
  });

  it('should set isEditingOrCreatingShift to false when closed', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    shiftRosterPage.setState({
      isCreatingOrEditingShift: true
    });
    shiftRosterPage.find('Connect(EditShiftModal)[aria-label="Edit Shift"]').simulate("close");
    expect(shiftRosterPage.state("isCreatingOrEditingShift")).toEqual(false);
  });

  it('should call addShift when a timeslot is selected', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    const newDateStart = moment(startDate).add(7, "days").toDate();
    const newDateEnd = moment(endDate).add(7, "days").toDate();
    // Note: Calendar is called "ForwardRef" in SNAPSHOT
    shiftRosterPage.find('ForwardRef').simulate("selectSlot", {
      start: newDateStart,
      end: newDateEnd,
      action: "select"
    });

    expect(baseProps.addShift).toBeCalled();
    expect(baseProps.addShift).toBeCalledWith({
      tenantId: spot.tenantId,
      startDateTime: newDateStart,
      endDateTime: newDateEnd,
      spot: spot,
      employee: null,
      rotationEmployee: null,
      pinnedByUser: false
    });
  });

  it('should not call addShift when a timeslot is clicked', () => {
    const shiftRosterPage = shallow(<ShiftRosterPage
      {...baseProps}
    />);
    const newDateStart = moment(startDate).add(7, "days").toDate();
    const newDateEnd = moment(endDate).add(7, "days").toDate();
    // Note: Calendar is called "ForwardRef" in SNAPSHOT
    shiftRosterPage.find('ForwardRef').simulate("selectSlot", {
      start: newDateStart,
      end: newDateEnd,
      action: "click"
    });

    expect(baseProps.addShift).not.toBeCalled();
  });

  it('should saturate the color and have a solid border if the shift is published', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);
    const publishedShift: Shift = {
      ...shift,
      startDateTime: moment(startDate).subtract("1", "day").toDate()
    };

    const style = shiftRosterPage.getShiftStyle(publishedShift);
    expect(style).toEqual({
      style: {
        border: "1px solid",
        backgroundColor: color(getShiftColor(publishedShift)).saturate(-0.5).hex()
      }
    });
  });

  it('should keep the color and have a dashed border if the shift is published', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);

    const style = shiftRosterPage.getShiftStyle(shift);
    expect(style).toEqual({
      style: {
        border: "1px dashed",
        backgroundColor: getShiftColor(shift)
      }
    });
  });

  it('day should be white if it is draft', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);

    const style = shiftRosterPage.getDayStyle(endDate);
    expect(style).toEqual({
      className: "draft-day",
      style: {
        backgroundColor: "var(--pf-global--BackgroundColor--100)"
      }
    });
  });

  it('day should be gray if it is published', () => {
    const shiftRosterPage = new ShiftRosterPage(baseProps);

    const style = shiftRosterPage.getDayStyle(moment(startDate).subtract(1, "day").toDate());
    expect(style).toEqual({
      className: "published-day",
      style: {
        backgroundColor: "var(--pf-global--BackgroundColor--300)"
      }
    });
  });

  it('should render EventWrapper correctly', () => {
    const eventWrapper = shallow(
      <EventWrapper
        event={shift}
        style={
          {
            top: "50%",
            height: "30%"
          }
        }
      >
        <span>Shift</span>
      </EventWrapper>
    );

    expect(toJson(eventWrapper)).toMatchSnapshot();
  });
});

const spot: Spot = {
  tenantId: 0,
  id: 2,
  version: 0,
  name: "Spot",
  requiredSkillSet: [
    {
      tenantId: 0,
      id: 3,
      version: 0,
      name: "Skill"
    }
  ]
}

const employee: Employee = {
  tenantId: 0,
  id: 4,
  version: 0,
  name: "Employee 1",
  contract: {
    tenantId: 0,
    id: 5,
    version: 0,
    name: "Basic Contract",
    maximumMinutesPerDay: 10,
    maximumMinutesPerWeek: 70,
    maximumMinutesPerMonth: 500,
    maximumMinutesPerYear: 6000
  },
  skillProficiencySet: [{
    tenantId: 0,
    id: 6,
    version: 0,
    name: "Not Required Skill"
  }]
}

const shift: Shift = {
  tenantId: 0,
  id: 1,
  version: 0, 
  startDateTime: moment("2018-07-01T09:00").toDate(),
  endDateTime: moment("2018-07-01T17:00").toDate(),
  spot: spot,
  employee: employee,
  rotationEmployee: {
    ...employee,
    id: 7,
    name: "Rotation Employee"
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
  contractMinutesViolationPenaltyList: []
};

const startDate = moment("2018-07-01T09:00").startOf('week').toDate();
const endDate = moment("2018-07-01T09:00").endOf('week').toDate()

const rosterState: RosterState = {
  tenant: {
    id: 0,
    version: 0,
    name: "Tenant"
  },
  publishNotice: 14,
  publishLength: 7,
  firstDraftDate: new Date("2018-07-01"),
  draftLength: 7,
  unplannedRotationOffset: 0,
  rotationLength: 7,
  lastHistoricDate: new Date("2018-07-01"),
  timeZone: "EST"
};

const baseProps: Props = {
  isSolving: false,
  isLoading: false,
  allSpotList: [spot],
  shownSpotList: [spot],
  spotIdToShiftListMap: new Map<number, Shift[]>([
    [2, [shift]]
  ]),
  startDate: startDate,
  endDate: endDate,
  totalNumOfSpots: 1,
  rosterState: rosterState,
  addShift: jest.fn(),
  removeShift: jest.fn(),
  updateShift: jest.fn(),
  getShiftRosterFor: jest.fn(),
  refreshShiftRoster: jest.fn(),
  solveRoster: jest.fn(),
  publishRoster:jest.fn(),
  terminateSolvingRosterEarly: jest.fn(),
  showInfoMessage: jest.fn()
}