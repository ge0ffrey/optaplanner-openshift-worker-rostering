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
import Spot from "domain/Spot";
import { AppState } from "store/types";
import { spotSelectors } from "store/spot";
import { connect } from 'react-redux';
import moment from 'moment';
import { Level, LevelItem, Button, Text, Pagination, ButtonVariant } from "@patternfly/react-core";
import { EventProps } from 'react-big-calendar'
import { modulo } from 'util/MathUtils';
import TypeaheadSelectInput from "ui/components/TypeaheadSelectInput";
import { alert } from "store/alert";
import RosterState from "domain/RosterState";

import ShiftTemplate from "domain/ShiftTemplate";
import { shiftTemplateSelectors, shiftTemplateOperations } from "store/rotation";
import { WithTranslation, withTranslation, useTranslation } from "react-i18next";
import EditShiftTemplateModal from "./EditShiftTemplateModal";
import { EditIcon, TrashIcon } from "@patternfly/react-icons";
import Schedule from "ui/components/calendar/Schedule";

interface StateProps {
  isLoading: boolean;
  spotList: Spot[];
  spotIdToShiftTemplateListMap: Map<number, ShiftTemplate[]>;
  rosterState: RosterState | null;
}
  
const mapStateToProps = (state: AppState): StateProps => ({
  isLoading: state.shiftTemplateList.isLoading,
  spotList: spotSelectors.getSpotList(state),
  spotIdToShiftTemplateListMap: shiftTemplateSelectors.getShiftTemplateList(state)
    .reduce((prev, curr) => {
      const old = prev.get(curr.spot.id as number)? prev.get(curr.spot.id as number) as ShiftTemplate[] : [];
      const templatesToAdd: ShiftTemplate[] =
      (state.rosterState.rosterState !== null &&
        moment.duration(curr.durationBetweenRotationStartAndTemplateStart)
          .add(curr.shiftTemplateDuration).asDays() >= state.rosterState.rosterState.rotationLength)?
        [curr, {
          ...curr,
          durationBetweenRotationStartAndTemplateStart:
           moment.duration(-state.rosterState.rosterState.rotationLength, "days")
             .add(curr.durationBetweenRotationStartAndTemplateStart)
        }] : [curr];
      return prev.set(curr.spot.id as number, old.concat(templatesToAdd));
    },
    new Map<number, ShiftTemplate[]>()),
  rosterState: state.rosterState.rosterState
}); 
  
export interface DispatchProps {
  addShiftTemplate: typeof shiftTemplateOperations.addShiftTemplate;
  removeShiftTemplate: typeof shiftTemplateOperations.removeShiftTemplate;
  updateShiftTemplate: typeof shiftTemplateOperations.updateShiftTemplate;
  showInfoMessage: typeof alert.showInfoMessage;
}
  
const mapDispatchToProps: DispatchProps = {
  addShiftTemplate: shiftTemplateOperations.addShiftTemplate,
  removeShiftTemplate: shiftTemplateOperations.removeShiftTemplate,
  updateShiftTemplate: shiftTemplateOperations.updateShiftTemplate,
  showInfoMessage: alert.showInfoMessage
}
  
export type Props = StateProps & DispatchProps;
interface State {
  shownSpot: Spot|null;
  isCreatingOrEditingShiftTemplate: boolean;
  selectedShiftTemplate?: ShiftTemplate;
  weekNumber: number;
}

export const baseDate = moment("2018-01-01T00:00").startOf('week').toDate();

const ShiftTemplatePopoverHeader: React.FC<{
  shiftTemplate: ShiftTemplate;
  rotationLength: number;
  onEdit: (shift: ShiftTemplate) => void;
  onDelete: (shift: ShiftTemplate) => void;
}> = props =>{
  const { t } = useTranslation();
  const durationBetweenRotationStartAndEnd = moment
    .duration(props.shiftTemplate.durationBetweenRotationStartAndTemplateStart)
    .add(props.shiftTemplate.shiftTemplateDuration);
  return (
    <span> 
      <Text> 
        {t("shiftTemplate", {
          spot: props.shiftTemplate.spot.name,
          rotationEmployee: props.shiftTemplate.rotationEmployee? props.shiftTemplate.rotationEmployee.name
            : "Unassigned",
          dayStart: Math.floor(modulo(
            props.shiftTemplate.durationBetweenRotationStartAndTemplateStart.asDays(),
            props.rotationLength)) + 1,
          startTime: moment("2018-01-01")
            .add(props.shiftTemplate.durationBetweenRotationStartAndTemplateStart).format("LT"),
          dayEnd: Math.floor(modulo(durationBetweenRotationStartAndEnd.asDays(),
            props.rotationLength)) + 1,
          endTime: moment("2018-01-01")
            .add(durationBetweenRotationStartAndEnd).format("LT")
        })}

      </Text>
      <Button
        onClick={() => props.onEdit(props.shiftTemplate)}
        variant={ButtonVariant.link}
      >
        <EditIcon />
      </Button>
      <Button
        onClick={() => props.onDelete(props.shiftTemplate)}
        variant={ButtonVariant.link}
      >
        <TrashIcon />
      </Button>
    </span>
  )}

const ShiftTemplatePopoverBody: React.FC = () => <></>;

const ShiftTemplateEvent: React.FC<EventProps<ShiftTemplate>> = (props) => {

  return (
    <span
      data-tip
      data-for={String(props.event.id)}
      style={{
        display: "flex",
        height: "100%",
        width: "100%"
      }}
    >
      {props.title}
    </span>
  )
};

export class RotationPage extends React.Component<Props & WithTranslation, State> {
  constructor(props: Props & WithTranslation) {
    super(props);
    this.addShiftTemplate = this.addShiftTemplate.bind(this);
    this.updateShiftTemplate = this.updateShiftTemplate.bind(this);
    this.deleteShiftTemplate = this.deleteShiftTemplate.bind(this);

    const shownSpot = (props.spotList.length > 0)? props.spotList[0] : null;
    this.state = {
      isCreatingOrEditingShiftTemplate: false,
      weekNumber: 0,
      shownSpot
    };
  }

  componentDidUpdate() {
    const shownSpot = this.state.shownSpot;
    if (this.props.spotList.length > 0 && ((shownSpot !== null && 
      this.props.spotList.find((spot) => spot.id === shownSpot.id) === undefined) ||
      (shownSpot === null))
    ) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({
        shownSpot: this.props.spotList[0]
      });
    }
  }

  addShiftTemplate(addedShiftTemplate: ShiftTemplate) {
    this.props.addShiftTemplate(addedShiftTemplate);
  }

  updateShiftTemplate(updatedShiftTemplate: ShiftTemplate) {
    this.props.updateShiftTemplate(updatedShiftTemplate);
  }


  deleteShiftTemplate(deletedShiftTemplate: ShiftTemplate) {
    this.props.removeShiftTemplate(deletedShiftTemplate);
  }

  render() {
    if (this.props.rosterState === null || this.props.isLoading || this.props.spotList.length <= 0 || 
      this.state.shownSpot === null || !this.props.tReady) {
      return <div />;
    }

    const startDate = moment(baseDate).add(this.state.weekNumber, "weeks").toDate();
    const endDate = moment(startDate).add(1, "week").toDate();
    const spot = this.state.shownSpot;
    const events: { shiftTemplate: ShiftTemplate; start: Date; end: Date }[] = [];

    (this.props.spotIdToShiftTemplateListMap.get(this.state.shownSpot.id as number) as ShiftTemplate[])
      .forEach(st => {
        const startHours = st.shiftTemplateDuration.hours();
        const startMinutes = st.shiftTemplateDuration.minutes();
        const durationBetweenRotationStartAndTemplateStart = 
          moment.duration(st.durationBetweenRotationStartAndTemplateStart);

        if (startHours === 0 && startMinutes === 0) { 
          durationBetweenRotationStartAndTemplateStart.add(1, "ms");
        }


        const durationBetweenRotationStartAndEnd = moment
          .duration(st.durationBetweenRotationStartAndTemplateStart).add(st.shiftTemplateDuration);
        const endHours = durationBetweenRotationStartAndEnd.hours();
        const endMinutes = durationBetweenRotationStartAndEnd.minutes();
        const shiftTemplateDuration = moment.duration(st.shiftTemplateDuration);
        if (endHours === 0 && endMinutes=== 0) {
          shiftTemplateDuration.subtract(1, "ms");
        }
        events.push({
          shiftTemplate: st,
          start:  moment(baseDate).add(durationBetweenRotationStartAndTemplateStart).toDate(),
          end: moment(baseDate)
            .add(durationBetweenRotationStartAndTemplateStart)
            .add(shiftTemplateDuration).toDate()
        });
      });

    return (
      <>
        <Level
          gutter="sm"
          style={{
            height: "60px",
            padding: "5px 5px 5px 5px",
            backgroundColor: "var(--pf-global--BackgroundColor--100)"
          }}
        >
          <LevelItem style={{display: "flex"}}>
            <TypeaheadSelectInput
              aria-label="Select Spot"
              emptyText="Select Spot"
              optionToStringMap={spot => spot.name}
              options={this.props.spotList}
              defaultValue={this.state.shownSpot}
              onChange={(newSpot) => {
                if (newSpot !== undefined) {
                  this.setState({ shownSpot: newSpot });
                }
              }}
            />
            <Pagination
              itemCount={Math.ceil(this.props.rosterState.rotationLength)}
              page={this.state.weekNumber + 1}
              onSetPage={(e, page) => {
                this.setState({ weekNumber: page - 1 });
              }}
              perPage={7}
              perPageOptions={[]}
              titles={{
                items: 'days',
                page: 'week',
                itemsPerPage: 'Week #',
                perPageSuffix: 'Week #',
                toFirstPage: 'Go to the first week',
                toPreviousPage: 'Go to the previous week',
                toLastPage: 'Go to the last week',
                toNextPage: 'Go to the next week',
                optionsToggle: 'Select',
                currPage: 'Current week',
                paginationTitle: 'Week Select'
              }}
            />
          </LevelItem>
          <LevelItem style={{display: "flex"}}>
            <Button
              style={{margin: "5px"}}
              aria-label="Create Shift Template"
              onClick={() => {
                this.setState({
                  selectedShiftTemplate: undefined,
                  isCreatingOrEditingShiftTemplate: true
                })
              }}
            >
              Create Shift Template
            </Button>
          </LevelItem>
        </Level>
        <EditShiftTemplateModal
          aria-label="Edit Shift Template"
          shiftTemplate={this.state.selectedShiftTemplate}
          isOpen={this.state.isCreatingOrEditingShiftTemplate}
          onSave={shiftTemplate => {
            if (this.state.selectedShiftTemplate !== undefined) {
              this.props.updateShiftTemplate(shiftTemplate);
            }
            else {
              this.props.addShiftTemplate(shiftTemplate);
            }
            this.setState({ selectedShiftTemplate: undefined, isCreatingOrEditingShiftTemplate: false });
          }}
          onDelete={shiftTemplate => {
            this.props.removeShiftTemplate(shiftTemplate);
            this.setState({ isCreatingOrEditingShiftTemplate: false });
          }}
          onClose={() => {
            this.setState({ 
              selectedShiftTemplate: undefined, 
              isCreatingOrEditingShiftTemplate: false
            });
          }}
        />
        <Schedule<{ shiftTemplate: ShiftTemplate; start: Date; end: Date }>
          key={spot.id}
          startDate={startDate}
          endDate={endDate}
          dateFormat={(date) =>
            this.props.t("rotationDay",
              {
                day: moment.duration(moment(date).diff(baseDate)).asDays() + 1
              })
          }
          events={events}
          titleAccessor={e => e.shiftTemplate.rotationEmployee?
            e.shiftTemplate.rotationEmployee.name : "Unassigned"}
          startAccessor={e => e.start}
          endAccessor={e => e.end}
          addEvent={
            (start,end) => {this.addShiftTemplate({
              tenantId: spot.tenantId,
              durationBetweenRotationStartAndTemplateStart: moment.duration(moment(start)
                .diff(baseDate)),
              shiftTemplateDuration: moment.duration(moment(end).diff(baseDate))
                .subtract(moment(start).diff(baseDate)),
              spot: spot,
              rotationEmployee: null,
            });}
          }
          eventStyle={() => ({})}
          dayStyle={() => ({})}
          wrapperStyle={() => ({})}
          popoverHeader={st => ShiftTemplatePopoverHeader({
            shiftTemplate: st.shiftTemplate,
            rotationLength: (this.props.rosterState as RosterState).rotationLength,
            onEdit: (shiftTemplate) => this.setState({
              selectedShiftTemplate: shiftTemplate,
              isCreatingOrEditingShiftTemplate: true
            }),
            onDelete: (shiftTemplate) => this.props.removeShiftTemplate(shiftTemplate)
          })
          }
          popoverBody={() => ShiftTemplatePopoverBody}
          eventComponent={(params) => ShiftTemplateEvent({
            ...params,
            event: params.event.shiftTemplate,
          })}
        />
      </>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation()(RotationPage));