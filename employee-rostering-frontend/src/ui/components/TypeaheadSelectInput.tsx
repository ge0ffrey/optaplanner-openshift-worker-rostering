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
import { Select, SelectOption, SelectVariant } from "@patternfly/react-core";
import "./TypeaheadSelectInput.css";

export interface TypeaheadSelectProps<T> {
  emptyText: string;
  options: T[];
  defaultValue: T | undefined;
  optionToStringMap: (option: T) => string;
  onChange: (selected: T | undefined) => void;
  optional?: boolean; 
}

export interface TypeaheadSelectState<T> {
  isExpanded: boolean;
  selected: T | undefined;
}

export default class TypeaheadSelectInput<T> extends React.Component<
TypeaheadSelectProps<T>,
TypeaheadSelectState<T>
> { 
  constructor(props: TypeaheadSelectProps<T>) {
    super(props);

    this.onToggle = this.onToggle.bind(this);
    this.onSelect = this.onSelect.bind(this);
    this.clearSelection = this.clearSelection.bind(this);

    this.state = {
      isExpanded: false,
      selected: props.defaultValue
    };
  }

  onToggle(isExpanded: boolean) {
    this.setState({
      isExpanded
    });
  }

  clearSelection(event: any) {
    if (event.eventPhase === 2) {
      this.props.onChange(undefined);
      this.setState({
        selected: undefined,
        isExpanded: false
      });
    } // HACK: For some reason, when there are two or more Select, the
    // clear button of the Select above is clicked on Keyboard enter via event bubbling.
  }

  onSelect(event: any,
    selection: string,
    isPlaceholder: boolean) {
    const selectedOption = this.props.options.find(
      option => this.props.optionToStringMap(option) === selection
    ) as T;
    setTimeout(() => {
      this.props.onChange(selectedOption);
      this.setState(() => ({
        isExpanded: false,
        selected: selectedOption
      }))
    }, 0); // HACK: For some reason, when there are two or more Select, the
    // clear button is clicked on Keyboard enter. 
  }

  render() {
    const { isExpanded, selected } = this.state;
    const emptyText = this.props.emptyText;
    const selection =
      selected !== undefined ? this.props.optionToStringMap(selected) : null;

    return (
      <div>
        <Select
          ref={(select) => {
            // Hack to get select to display selection without needing to toggle
            if (select !== null && selection !== null) {
              select.setState({
                typeaheadInputValue: selection
              });
            }
          }}
          variant={SelectVariant.typeahead}
          aria-label={emptyText}
          onToggle={this.onToggle}
          onSelect={this.onSelect as any}
          onClear={this.clearSelection}
          selections={selection as any}
          isExpanded={isExpanded}
          placeholderText={emptyText}
          required={!this.props.optional}
        >
          {this.props.options.map((option) => (
            <SelectOption
              isDisabled={false}
              key={this.props.optionToStringMap(option)}
              value={this.props.optionToStringMap(option)}
            />
          ))}
        </Select>
      </div>
    );
  }
}
