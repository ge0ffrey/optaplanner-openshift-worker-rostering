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

import Spot from 'domain/Spot';
import { ActionFactory } from '../types';
import { ActionType, SetSpotListLoadingAction, AddSpotAction, UpdateSpotAction, RemoveSpotAction, RefreshSpotListAction } from './types';

export const setIsSpotListLoading: ActionFactory<Boolean, SetSpotListLoadingAction> = (isLoading: Boolean) => ({
  type: ActionType.SET_SPOT_LIST_LOADING,
  isLoading: isLoading.valueOf()
});

export const addSpot: ActionFactory<Spot, AddSpotAction> = newSpot => ({
  type: ActionType.ADD_SPOT,
  spot: newSpot
});

export const removeSpot: ActionFactory<Spot, RemoveSpotAction> = deletedSpot => ({
  type: ActionType.REMOVE_SPOT,
  spot: deletedSpot
});

export const updateSpot: ActionFactory<Spot, UpdateSpotAction> = updatedSpot => ({
  type: ActionType.UPDATE_SPOT,
  spot: updatedSpot
});

export const refreshSpotList: ActionFactory<Spot[], RefreshSpotListAction> = newSpotList => ({
  type: ActionType.REFRESH_SPOT_LIST,
  spotList: newSpotList
});