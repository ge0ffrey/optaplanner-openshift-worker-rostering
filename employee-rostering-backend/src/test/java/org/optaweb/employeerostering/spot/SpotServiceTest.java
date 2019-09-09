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

package org.optaweb.employeerostering.spot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.service.skill.SkillService;
import org.optaweb.employeerostering.service.spot.SpotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.NestedServletException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SpotServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(SpotServiceTest.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SpotService spotService;

    @Autowired
    private SkillService skillService;

    private Skill createSkill(Integer tenantId, String name) {
        SkillView skillView = new SkillView(tenantId, name);
        return skillService.createSkill(tenantId, skillView);
    }

    @Before
    public void setup() {
        createTestTenant();
    }

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void getSpotListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/rest/tenant/{tenantId}/spot/", TENANT_ID)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getSpotTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        mvc.perform(MockMvcRequestBuilders
                .get("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, spot.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("spot"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requiredSkillSet").isNotEmpty());
    }

    @Test
    public void getNonExistentSpotTest() {
        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .get("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, 0)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                        "Exception: No Spot entity found with ID (0).");
    }

    @Test
    public void getNonMatchingSpotTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .get("/rest/tenant/{tenantId}/spot/{id}", 0, spot.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: The " +
                        "tenantId (0) does not match the persistable (spot)'s tenantId (" + TENANT_ID + ").");
    }

    @Test
    public void deleteSpotTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        mvc.perform(MockMvcRequestBuilders
                .delete("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, spot.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentSpotTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .delete("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, 0)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("false"));
    }

    @Test
    public void deleteNonMatchingSpotTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .delete("/rest/tenant/{tenantId}/spot/{id}", 0, spot.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                        "The tenantId (0) does not match the persistable (spot)'s tenantId (" + TENANT_ID + ").");
    }

    @Test
    public void createSpotTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(spotView);

        mvc.perform(MockMvcRequestBuilders
                .post("/rest/tenant/{tenantId}/spot/add", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("spot"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requiredSkillSet").isNotEmpty());
    }

    @Test
    public void createNonMatchingSpotTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(spotView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .post("/rest/tenant/{tenantId}/spot/add", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                        "The tenantId (0) does not match the persistable (spot)'s tenantId (" + TENANT_ID + ").");
    }

    @Test
    public void updateSpotTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        SpotView updatedSpot = new SpotView(TENANT_ID, "updatedSpot", testSkillSet);
        updatedSpot.setId(spot.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedSpot);

        mvc.perform(MockMvcRequestBuilders
                .put("/rest/tenant/{tenantId}/spot/update", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("updatedSpot"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requiredSkillSet").isNotEmpty());
    }

    @Test
    public void updateNonMatchingSpotTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        spotService.createSpot(TENANT_ID, spotView);

        SpotView updatedSpot = new SpotView(TENANT_ID, "updatedSpot", testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(updatedSpot);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .put("/rest/tenant/{tenantId}/spot/update", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                        "The tenantId (0) does not match the persistable (updatedSpot)'s tenantId (" + TENANT_ID + ")" +
                        ".");
    }

    @Test
    public void updateNonExistentSpotTest() throws Exception {
        SpotView spotView = new SpotView(TENANT_ID, "spot", Collections.emptySet());
        spotView.setId(0L);
        String body = (new ObjectMapper()).writeValueAsString(spotView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .put("/rest/tenant/{tenantId}/spot/update", TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                        "Exception: Spot entity with ID (0) not found.");
    }

    @Test
    public void updateChangeTenantIdSpotTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        SpotView updatedSpot = new SpotView(0, "updatedSpot", testSkillSet);
        updatedSpot.setId(spot.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedSpot);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .put("/rest/tenant/{tenantId}/spot/update", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalState" +
                        "Exception: Spot entity with tenantId (" + TENANT_ID + ") cannot change tenants.");
    }
}
