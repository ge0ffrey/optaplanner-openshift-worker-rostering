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
import React from "react";
import Shift from "domain/Shift";
import Spot from "domain/Spot";
import { AppState } from "store/types";
import { spotSelectors } from "store/spot";
import { shiftSelectors } from "store/shift";
import { connect } from 'react-redux';
import Schedule from 'ui/components/Schedule';
import moment from 'moment';

interface StateProps {
  spotList: Spot[];
  shiftList: Shift[];
}
  
const mapStateToProps = (state: AppState): StateProps => ({
  spotList: spotSelectors.getSpotList(state),
  shiftList: shiftSelectors.getShiftList(state)
}); 
  
export interface DispatchProps {
}
  
const mapDispatchToProps: DispatchProps = {
};
  
export type Props = StateProps & DispatchProps;

export class ShiftRosterPage extends React.Component<Props> {
  render() {
    if (this.props.shiftList.length === 0 || this.props.spotList.length === 0) {
        return (<div />);
    }

    const startDate = moment.min(this.props.shiftList.map(s => moment(s.startDateTime))).toDate();
    const endDate = moment(startDate).add(7, "days").toDate();
    return (
      <Schedule
        startDate={startDate}
        endDate={endDate}
        rowTitles={this.props.spotList.map(spot => spot.name)}
        rowData={this.props.spotList.map(spot => this.props.shiftList.filter(shift => shift.spotId.id === spot.id && moment(shift.startDateTime).isBefore(endDate)))}
        dataToNameMap={s => (s.employeeId !== null)? s.employeeId.name : "Unassigned"}
        dataGetStartDate={s => s.startDateTime}
        dataGetEndDate={s => s.endDateTime}
        minDurationInMinutes={60 * 4}
      />
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(ShiftRosterPage);