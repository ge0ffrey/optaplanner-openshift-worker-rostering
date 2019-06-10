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
const getAnswers: Map<string, any> = new Map();
const postAnswers: Map<string, any> = new Map();
const putAnswers: Map<string, any> = new Map();
const deleteAnswers: Map<string, any> = new Map();

export function onGet(url: string, answer: any) {getAnswers.set(url, answer)}
export function onPost(url: string, params: any, answer: any) {postAnswers.set(url + JSON.stringify(params), answer)}
export function onPut(url: string, params: any, answer: any) {putAnswers.set(url + JSON.stringify(params), answer)}
export function onDelete(url: string, answer: any) {deleteAnswers.set(url, answer)}

export const mockGet = jest.fn().mockImplementation((url) =>
  Promise.resolve(getAnswers.get(url)));
export const mockPost = jest.fn().mockImplementation((url, params) =>
  Promise.resolve(postAnswers.get(url + JSON.stringify(params))));
export const mockPut = jest.fn().mockImplementation((url, params) =>
  Promise.resolve(putAnswers.get(url + JSON.stringify(params))));
export const mockDelete = jest.fn().mockImplementation((url) =>
  Promise.resolve(deleteAnswers.get(url)));

const mock = jest.fn().mockImplementation(() => {
  return {
    get: mockGet,
    post: mockPost,
    put: mockPut,
    delete: mockDelete
  };
});

export default mock;