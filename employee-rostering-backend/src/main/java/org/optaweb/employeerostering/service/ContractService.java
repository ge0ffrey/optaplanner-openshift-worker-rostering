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

package org.optaweb.employeerostering.service;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.persistence.ContractRepository;
import org.springframework.stereotype.Service;

@Service
public class ContractService extends AbstractRestService {

    private final ContractRepository contractRepository;

    public ContractService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Transactional
    public List<Contract> getContractList(Integer tenantId) {
        return contractRepository.findAllByTenantId(tenantId);
    }

    @Transactional
    public Contract getContract(Integer tenantId, Long id) {
        Optional<Contract> contractOptional = contractRepository.findById(id);

        if (!contractOptional.isPresent()) {
            throw new EntityNotFoundException("No Contract entity found with ID (" + id + ").");
        }

        validateTenantIdParameter(tenantId, contractOptional.get());
        return contractOptional.get();
    }

    @Transactional
    public Boolean deleteContract(Integer tenantId, Long id) {
        Optional<Contract> contractOptional = contractRepository.findById(id);

        if (!contractOptional.isPresent()) {
            return false;
        }

        validateTenantIdParameter(tenantId, contractOptional.get());
        contractRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Contract createContract(Integer tenantId, Contract contract) {
        validateTenantIdParameter(tenantId, contract);

        return contractRepository.save(contract);
    }

    @Transactional
    public Contract updateContract(Integer tenantId, Contract contract) {
        validateTenantIdParameter(tenantId, contract);

        Optional<Contract> contractOptional = contractRepository.findById(contract.getId());

        if (!contractOptional.isPresent()) {
            throw new EntityNotFoundException("Contract entity with ID (" + contract.getId() + ") not found.");
        } else if (!contractOptional.get().getTenantId().equals(contract.getTenantId())) {
            throw new IllegalStateException("Contract entity with tenantId (" + contractOptional.get().getTenantId()
                                                    + ") cannot change tenants.");
        }

        Contract databaseContract = contractOptional.get();
        databaseContract.setName(contract.getName());
        databaseContract.setMaximumMinutesPerDay(contract.getMaximumMinutesPerDay());
        databaseContract.setMaximumMinutesPerWeek(contract.getMaximumMinutesPerWeek());
        databaseContract.setMaximumMinutesPerMonth(contract.getMaximumMinutesPerMonth());
        databaseContract.setMaximumMinutesPerYear(contract.getMaximumMinutesPerYear());
        return contractRepository.save(databaseContract);
    }
}
