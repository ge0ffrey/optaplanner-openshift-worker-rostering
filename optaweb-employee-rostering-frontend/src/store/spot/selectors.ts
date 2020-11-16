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
import { skillSelectors } from 'store/skill';
import { Spot } from 'domain/Spot';
import DomainObjectView from 'domain/DomainObjectView';
import { AppState } from '../types';
import { Map, List } from 'immutable';

export const getSpotById = (state: AppState, id: number): Spot => {
  if (state.spotList.isLoading || state.skillList.isLoading) {
    throw Error('Spot list is loading');
  }
  const spotView = state.spotList.spotMapById.get(id) as DomainObjectView<Spot>;
  return {
    ...spotView,
    requiredSkillSet: spotView.requiredSkillSet.map(skillId => skillSelectors.getSkillById(state, skillId)),
  };
};

let oldSpotMapById: Map<number, DomainObjectView<Spot>> | null = null;
let spotListForOldSpotMapById: List<Spot> | null = null;

export const getSpotList = (state: AppState): List<Spot> => {
  if (state.spotList.isLoading || state.skillList.isLoading) {
    return List();
  }
  if (oldSpotMapById === state.spotList.spotMapById && spotListForOldSpotMapById !== null) {
    return spotListForOldSpotMapById;
  }

  const out = state.spotList.spotMapById.keySeq().map((key) => getSpotById(state, key)).toList();

  oldSpotMapById = state.spotList.spotMapById;
  spotListForOldSpotMapById = out;
  return out;
};
