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
import { Employee } from 'domain/Employee';
import { EmployeeAvailability } from 'domain/EmployeeAvailability';
import { Shift } from 'domain/Shift';
import { Spot } from 'domain/Spot';
import { mount, shallow } from 'enzyme';
import { BlueprintIcon, EditIcon, TrashIcon } from '@patternfly/react-icons';
import toJson from 'enzyme-to-json';
import moment from 'moment-timezone';
import 'moment/locale/en-ca';
import * as React from 'react';
import ShiftEvent, * as Indictments from './ShiftEvent';

describe('ShiftEvent', () => {
  it('getRequiredSkillViolations should render correctly', () => {
    const shift: Shift = {
      ...baseShift,
      requiredSkillViolationList: [{
        score: { hardScore: -10, mediumScore: 0, softScore: 0 },
        shift: baseShift,
      }],
    };
    const requiredSkillViolations = mount(<Indictments.RequiredSkillViolations {...shift} />);
    expect(toJson(requiredSkillViolations)).toMatchSnapshot();
  });

  it('getContractMinutesViolations should render correctly', () => {
    const shift: Shift = {
      ...baseShift,
      contractMinutesViolationPenaltyList: [
        {
          employee,
          type: 'DAY',
          minutesWorked: 20,
          score: { hardScore: -1, mediumScore: 0, softScore: 0 },
        },
        {
          employee,
          type: 'WEEK',
          minutesWorked: 80,
          score: { hardScore: -1, mediumScore: 0, softScore: 0 },
        },
        {
          employee,
          type: 'MONTH',
          minutesWorked: 600,
          score: { hardScore: -1, mediumScore: 0, softScore: 0 },
        },
        {
          employee,
          type: 'YEAR',
          minutesWorked: 7000,
          score: { hardScore: -1, mediumScore: 0, softScore: 0 },
        },
      ],
    };

    const contractMinutesViolations = mount(<Indictments.ContractMinutesViolations {...shift} />);
    expect(toJson(contractMinutesViolations)).toMatchSnapshot();
  });

  it('getUnavaliableEmployeeViolations should render correctly', () => {
    const unavaliableAvailability: EmployeeAvailability = {
      ...baseEmployeeAvailability,
      state: 'UNAVAILABLE',
    };
    const shift: Shift = {
      ...baseShift,
      unavailableEmployeeViolationList: [{
        score: { hardScore: -1, mediumScore: 0, softScore: 0 },
        employeeAvailability: unavaliableAvailability,
        shift: baseShift,
      }],
    };
    const unavailableEmployeeViolations = mount(<Indictments.UnavailableEmployeeViolations {...shift} />);
    expect(toJson(unavailableEmployeeViolations)).toMatchSnapshot();
  });

  it('getShiftEmployeeConflictViolations should render correctly', () => {
    const shift: Shift = {
      ...baseShift,
      shiftEmployeeConflictList: [
        {
          score: { hardScore: -1, mediumScore: 0, softScore: 0 },
          leftShift: baseShift,
          rightShift: {
            ...baseShift,
            id: 100,
            startDateTime:
            moment(baseShift.startDateTime).add(1, 'hour').toDate(),
          },
        },
        {
          score: { hardScore: -1, mediumScore: 0, softScore: 0 },
          rightShift: baseShift,
          leftShift: {
            ...baseShift,
            id: 101,
            startDateTime: moment(baseShift.startDateTime).subtract(1, 'hour').toDate(),
          },
        },
      ],
    };
    const shiftEmployeeConflictViolations = mount(<Indictments.ShiftEmployeeConflictViolations {...shift} />);
    expect(toJson(shiftEmployeeConflictViolations)).toMatchSnapshot();
  });

  it('getRotationViolationPenalties should render correctly', () => {
    const shift: Shift = {
      ...baseShift,
      rotationViolationPenaltyList: [
        {
          score: { hardScore: 0, mediumScore: 0, softScore: -100 },
          shift: baseShift,
        },
      ],
    };
    const rotationViolationPenalties = mount(<Indictments.RotationViolationPenalties {...shift} />);
    expect(toJson(rotationViolationPenalties)).toMatchSnapshot();
  });

  it('getUnassignedShiftPenalties should render correctly', () => {
    const shift: Shift = {
      ...baseShift,
      unassignedShiftPenaltyList: [
        {
          score: { hardScore: 0, mediumScore: -1, softScore: 0 },
          shift: baseShift,
        },
      ],
    };
    const unassignedShiftPenalties = mount(<Indictments.UnassignedShiftPenalties {...shift} />);
    expect(toJson(unassignedShiftPenalties)).toMatchSnapshot();
  });

  it('getUndesiredTimeslotForEmployeePenalties should render correctly', () => {
    const undesiredAvailability: EmployeeAvailability = {
      ...baseEmployeeAvailability,
      state: 'UNDESIRED',
    };
    const shift: Shift = {
      ...baseShift,
      undesiredTimeslotForEmployeePenaltyList: [{
        score: { hardScore: 0, mediumScore: 0, softScore: -1 },
        employeeAvailability: undesiredAvailability,
        shift: baseShift,
      }],
    };
    const undesiredTimeslotForEmployeePenalties = mount(
      <Indictments.UndesiredTimeslotForEmployeePenalties {...shift} />,
    );
    expect(toJson(undesiredTimeslotForEmployeePenalties)).toMatchSnapshot();
  });

  it('getDesiredTimeslotForEmployeeReward should render correctly', () => {
    const desiredAvailability: EmployeeAvailability = {
      ...baseEmployeeAvailability,
      state: 'DESIRED',
    };
    const shift: Shift = {
      ...baseShift,
      desiredTimeslotForEmployeeRewardList: [{
        score: { hardScore: 0, mediumScore: 0, softScore: -1 },
        employeeAvailability: desiredAvailability,
        shift: baseShift,
      }],
    };
    const desiredTimeslotForEmployeeRewards = mount(<Indictments.DesiredTimeslotForEmployeeRewards {...shift} />);
    expect(toJson(desiredTimeslotForEmployeeRewards)).toMatchSnapshot();
  });

  it('getIndictments should render correctly with no indictments', () => {
    const indictments = mount(<Indictments.Indictments {...baseShift} />);
    expect(toJson(indictments)).toMatchSnapshot();
  });

  it('getIndictments should render correctly with indictments', () => {
    const shift: Shift = {
      ...baseShift,
      requiredSkillViolationList: [{
        score: { hardScore: -10, mediumScore: 0, softScore: 0 },
        shift: baseShift,
      }],
      rotationViolationPenaltyList: [
        {
          score: { hardScore: 0, mediumScore: 0, softScore: -100 },
          shift: baseShift,
        },
      ],
    };
    const indictments = mount(<Indictments.Indictments {...shift} />);
    expect(toJson(indictments)).toMatchSnapshot();
  });

  it('getShiftColor should return a color depending on score', () => {
    const getShiftWithScore: (hard: number, medium: number, soft: number) => Shift = (hard, medium, soft) => ({
      ...baseShift,
      indictmentScore: { hardScore: hard, mediumScore: medium, softScore: soft },
    });

    expect({
      negativeHardColor: Indictments.getShiftColor(getShiftWithScore(-5, 0, 0)),
      negativeMediumColor: Indictments.getShiftColor(getShiftWithScore(0, -1, 0)),
      negativeSoftColor: Indictments.getShiftColor(getShiftWithScore(0, 0, -10)),
      zeroColor: Indictments.getShiftColor(getShiftWithScore(0, 0, 0)),
      positiveSoftColor: Indictments.getShiftColor(getShiftWithScore(0, 0, 5)),
    }).toMatchSnapshot();
  });

  it('should render ShiftPopupHeader correctly', () => {
    const shiftEventObj = shallow(
      <Indictments.ShiftPopupHeader shift={baseShift} onEdit={jest.fn()} onCopy={jest.fn()} onDelete={jest.fn()} />,
    );
    expect(toJson(shiftEventObj)).toMatchSnapshot();
  });

  it('should render ShiftPopupHeader correctly without an onCopy function', () => {
    const shiftEventObj = shallow(
      <Indictments.ShiftPopupHeader shift={baseShift} onEdit={jest.fn()} onDelete={jest.fn()} />,
    );
    expect(toJson(shiftEventObj)).toMatchSnapshot();
  });

  it('ShiftPopupHeader should call onEdit if onEdit button is clicked', () => {
    const onEdit = jest.fn();
    const shiftPopupHeaderObj = shallow(
      <Indictments.ShiftPopupHeader shift={baseShift} onEdit={onEdit} onCopy={jest.fn()} onDelete={jest.fn()} />,
    );
    shiftPopupHeaderObj.find(EditIcon).parent().simulate('click');
    expect(onEdit).toBeCalledWith(baseShift);
  });

  it('ShiftPopupHeader should call onCopy if onCopy button is clicked', () => {
    const onCopy = jest.fn();
    const shiftPopupHeaderObj = shallow(
      <Indictments.ShiftPopupHeader shift={baseShift} onEdit={jest.fn()} onCopy={onCopy} onDelete={jest.fn()} />,
    );
    shiftPopupHeaderObj.find(BlueprintIcon).parent().simulate('click');
    expect(onCopy).toBeCalledWith(baseShift);
  });

  it('ShiftPopupHeader should call onDelete if onDelete button is clicked', () => {
    const onDelete = jest.fn();
    const shiftPopupHeaderObj = shallow(
      <Indictments.ShiftPopupHeader shift={baseShift} onEdit={jest.fn()} onCopy={jest.fn()} onDelete={onDelete} />,
    );
    shiftPopupHeaderObj.find(TrashIcon).parent().simulate('click');
    expect(onDelete).toBeCalledWith(baseShift);
  });

  it('should render ShiftPopupBody correctly', () => {
    const shiftEventObj = shallow(
      <Indictments.ShiftPopupBody {...baseShift} />,
    );
    expect(toJson(shiftEventObj)).toMatchSnapshot();
  });

  it('should render ShiftEvent correctly', () => {
    const shiftEventObj = shallow(
      <ShiftEvent event={baseShift} title="Employee" />,
    );
    expect(toJson(shiftEventObj)).toMatchSnapshot();
  });

  it('should render ShiftEvent correctly when pinned', () => {
    const shiftEventObj = shallow(
      <ShiftEvent event={{ ...baseShift, pinnedByUser: true }} title="Employee" />,
    );
    expect(toJson(shiftEventObj)).toMatchSnapshot();
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

const baseEmployeeAvailability: Omit<EmployeeAvailability, 'state'> = {
  tenantId: 0,
  id: 8,
  version: 0,
  startDateTime: moment('2018-07-01T09:00').toDate(),
  endDateTime: moment('2018-07-01T17:00').toDate(),
  employee,
};

const baseShift: Shift = {
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
