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
import { RotationPage, Props, baseDate } from './RotationPage';
import RosterState from 'domain/RosterState';
import moment from 'moment-timezone';
import ShiftTemplate from 'domain/ShiftTemplate';
import { useTranslation, WithTranslation } from 'react-i18next';

describe('Rotation Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render correctly when loaded', () => {
    const rotationPage = shallow(<RotationPage
      {...baseProps}
    />);
    expect(toJson(rotationPage)).toMatchSnapshot();
  });

  it('should render correctly when loading', () => {
    const rotationPage = shallow(<RotationPage
      {...baseProps}
      isLoading
      spotList={[]}
      spotIdToShiftTemplateListMap={new Map()}
    />);
    expect(toJson(rotationPage)).toMatchSnapshot();
  });

  it('should render correctly when creating a new shift template via button', () => {
    const rotationPage = shallow(<RotationPage
      {...baseProps}
    />);
    rotationPage.find('Button[aria-label="Create Shift Template"]').simulate("click");
    expect(toJson(rotationPage)).toMatchSnapshot();
  });

  it('should update weekNumber state when page change', () => {
    const rotationPage = shallow(<RotationPage
      {...baseProps}
    />);
    rotationPage.find('Pagination').simulate("setPage", null, 2);
    expect(rotationPage.state("weekNumber")).toEqual(1);
  });

  it('should update shownSpot on spot change', () => {
    const rotationPage = shallow(<RotationPage
      {...baseProps}
    />);
    const newSpot: Spot = {
      ...spot,
      id: 111,
      name: "New Spot"
    };

    const setStateSpy = jest.spyOn(rotationPage.instance(), "setState");
    rotationPage.find('TypeaheadSelectInput[aria-label="Select Spot"]').simulate("change", newSpot);
    // Bug in enzyme; wrapper.state is not updated after setState sometimes
    expect(setStateSpy).toBeCalledWith({ shownSpot: newSpot });
  });

  it('should not update shownSpot on spot change if invalid', () => {
    const rotationPage = shallow(<RotationPage
      {...baseProps}
    />);
    const setStateSpy = jest.spyOn(rotationPage.instance(), "setState");
    rotationPage.find('TypeaheadSelectInput[aria-label="Select Spot"]').simulate("change");
    expect(setStateSpy).not.toBeCalled();
  });

  it('should call addShift on addShift', () => {
    const rotationPage = new RotationPage(baseProps);
    const newShiftTemplate: ShiftTemplate = {
      ...shiftTemplate,
      id: undefined,
      version: undefined
    };
    rotationPage.addShiftTemplate(newShiftTemplate);
    expect(baseProps.addShiftTemplate).toBeCalled();
    expect(baseProps.addShiftTemplate).toBeCalledWith(newShiftTemplate);
  });

  it('should call updateShift on updateShift', () => {
    const rotationPage = new RotationPage(baseProps);
    const updatedShiftTemplate: ShiftTemplate = {
      ...shiftTemplate,
    };
    rotationPage.updateShiftTemplate(updatedShiftTemplate);
    expect(baseProps.updateShiftTemplate).toBeCalled();
    expect(baseProps.updateShiftTemplate).toBeCalledWith(updatedShiftTemplate);
  });

  it('should call removeShiftTemplate on deleteShiftTemplate', () => {
    const rotationPage = new RotationPage(baseProps);
    rotationPage.deleteShiftTemplate(shiftTemplate);
    expect(baseProps.removeShiftTemplate).toBeCalled();
    expect(baseProps.removeShiftTemplate).toBeCalledWith(shiftTemplate);
  });

  it('call deleteShift when the EditShiftModal delete a shift', () => {
    const rotationPage = shallow(<RotationPage
      {...baseProps}
    />);
    rotationPage.setState({
      selectedShift: shiftTemplate,
      isCreatingOrEditingShiftTemplate: true
    });
    rotationPage.find('Connect(Component)[aria-label="Edit Shift Template"]')
      .simulate("delete", shiftTemplate);
    expect(baseProps.removeShiftTemplate).toBeCalled();
    expect(baseProps.removeShiftTemplate).toBeCalledWith(shiftTemplate);
    expect(rotationPage.state("isCreatingOrEditingShiftTemplate")).toEqual(false);
  });

  it('call addShiftTemplate when the EditShiftTemplateModal add a new shift', () => {
    const rotationPage = shallow(<RotationPage
      {...baseProps}
    />);
    rotationPage.setState({
      selectedShift: null,
      isCreatingOrEditingShiftTemplate: true
    });
    rotationPage.find('Connect(Component)[aria-label="Edit Shift Template"]')
      .simulate("save", shiftTemplate);
    expect(baseProps.addShiftTemplate).toBeCalled();
    expect(baseProps.addShiftTemplate).toBeCalledWith(shiftTemplate);
    expect(rotationPage.state("isCreatingOrEditingShiftTemplate")).toEqual(false);
  });

  it('call updateShift when the EditShiftModal updates a shift', () => {
    const rotationPage = shallow(<RotationPage
      {...baseProps}
    />);
    rotationPage.setState({
      selectedShift: shiftTemplate,
      isCreatingOrEditingShiftTemplate: true
    });
    rotationPage.find('Connect(Component)[aria-label="Edit Shift Template"]')
      .simulate("save", shiftTemplate);
    expect(baseProps.addShiftTemplate).toBeCalled();
    expect(baseProps.addShiftTemplate).toBeCalledWith(shiftTemplate);
    expect(rotationPage.state("isCreatingOrEditingShiftTemplate")).toEqual(false);
  });

  it('should set isCreatingOrEditingShiftTemplate to false when closed', () => {
    const rotationPage = shallow(<RotationPage
      {...baseProps}
    />);
    rotationPage.setState({
      isCreatingOrEditingShiftTemplate: true
    });
    rotationPage.find('Connect(Component)[aria-label="Edit Shift Template"]').simulate("close");
    expect(rotationPage.state("isCreatingOrEditingShiftTemplate")).toEqual(false);
  });

  it('should call addShiftTemplate when a timeslot is selected', () => {
    const rotationPage = shallow(<RotationPage
      {...baseProps}
    />);
    const newDateStart = moment(baseDate).add(7, "days").toDate();
    const newDateEnd = moment(baseDate).add(7, "days").add(8, "hours").toDate();
    ((rotationPage.find('Schedule').props()) as { addEvent: Function}).addEvent(newDateStart,
      newDateEnd);

    expect(baseProps.addShiftTemplate).toBeCalled();
    expect(baseProps.addShiftTemplate).toBeCalledWith({
      tenantId: spot.tenantId,
      durationBetweenRotationStartAndTemplateStart: moment.duration(168, "hours"),
      shiftTemplateDuration: moment.duration(8, "hours"),
      spot: spot,
      rotationEmployee: null
    });
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

const shiftTemplate: ShiftTemplate = {
  tenantId: 0,
  id: 1,
  version: 0, 
  durationBetweenRotationStartAndTemplateStart: moment.duration(1, "day").add(9, "hours"),
  shiftTemplateDuration: moment.duration(8, "hours"),
  spot: spot,
  rotationEmployee: employee
};

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

const baseProps: Props & WithTranslation = {
  ...useTranslation(),
  tReady: true,
  isLoading: false,
  spotList: [spot],
  spotIdToShiftTemplateListMap: new Map<number, ShiftTemplate[]>([
    [2, [shiftTemplate]],
    [111, []]
  ]),
  rosterState: rosterState,
  addShiftTemplate: jest.fn(),
  removeShiftTemplate: jest.fn(),
  updateShiftTemplate: jest.fn(),
  showInfoMessage: jest.fn()
}