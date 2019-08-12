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
import * as React from "react";
import { TextInput, Button, ButtonVariant } from "@patternfly/react-core";
import './FilterComponent.css';
import { Predicate } from "types";

export interface FilterProps<T> {
  filter: (filter: string) => Predicate<T>;
  onChange: (filter: Predicate<T>) => void;
}

export interface FilterState {
  filterText: string;
}

class FilterComponent<T> extends React.Component<FilterProps<T>, FilterState> {

  constructor(props: FilterProps<T>) {
    super(props);
    this.state = {filterText: ""};
    this.updateFilter = this.updateFilter.bind(this);
  }

  updateFilter(filterText: string) {
    this.props.onChange(this.props.filter(filterText));
    this.setState({filterText});
  }

  render() {
    return (
      <div className="search-icons">
        <TextInput
          aria-label="Search"
          placeholder="Search..."
          value={this.state.filterText}
          onChange={this.updateFilter} 
        />
        <Button
          variant={ButtonVariant.plain}
          isDisabled={this.state.filterText.length === 0} 
          onClick={() => this.updateFilter("")}
        >
          <svg style={{verticalAlign: "-0.125em"}} fill="currentColor" height="1em" width="1em" viewBox="0 0 512 512" aria-hidden="true" role="img">
            <path d="M256 8C119 8 8 119 8 256s111 248 248 248 248-111 248-248S393 8 256 8zm121.6 313.1c4.7 4.7 4.7 12.3 0 17L338 377.6c-4.7 4.7-12.3 4.7-17 0L256 312l-65.1 65.6c-4.7 4.7-12.3 4.7-17 0L134.4 338c-4.7-4.7-4.7-12.3 0-17l65.6-65-65.6-65.1c-4.7-4.7-4.7-12.3 0-17l39.6-39.6c4.7-4.7 12.3-4.7 17 0l65 65.7 65.1-65.6c4.7-4.7 12.3-4.7 17 0l39.6 39.6c4.7 4.7 4.7 12.3 0 17L312 256l65.6 65.1z" transform="" />
          </svg>
        </Button>
      </div>
    );
  }
}

export default FilterComponent;