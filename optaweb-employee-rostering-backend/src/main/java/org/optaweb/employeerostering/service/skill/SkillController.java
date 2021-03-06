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

package org.optaweb.employeerostering.service.skill;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;

@Path("/rest/tenant/{tenantId}/skill")
@ApplicationScoped
@Tag(name = "Skill")
public class SkillController {

    private final SkillService skillService;

    @Inject
    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GET
    @Path("/")
    @Operation(summary = "List Skills", description = "Get a list of all skills")
    public List<Skill> getSkillList(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return skillService.getSkillList(tenantId);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get Skill", description = "Gets a skill by id")
    public Skill getSkill(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return skillService.getSkill(tenantId, id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete Skill", description = "Deletes a skill by id")
    public Boolean deleteSkill(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return skillService.deleteSkill(tenantId, id);
    }

    @POST
    @Path("/add")
    @Operation(summary = "Add Skill", description = "Adds a new skill")
    public Skill createSkill(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid SkillView skillView) {
        return skillService.createSkill(tenantId, skillView);
    }

    @POST
    @Path("/update")
    @Operation(summary = "Update Skill", description = "Updates a skill")
    public Skill updateSkill(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid SkillView skillView) {
        return skillService.updateSkill(tenantId, skillView);
    }
}
