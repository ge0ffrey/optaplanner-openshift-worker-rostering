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

import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.service.SkillService;
import org.optaweb.employeerostering.service.SpotService;
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
import org.springframework.web.util.NestedServletException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SpotServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(SpotServiceTest.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SpotService spotService;

    @Autowired
    private SkillService skillService;

    private Skill createSkill(Integer tenantId, String name) {
        Skill skill = new Skill(tenantId, name);
        return skillService.createSkill(tenantId, skill);
    }

    @Test
    public void getSpotListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/spot", 1)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getSpotTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);
        spotService.createSpot(tenantId, spot);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/spot/{id}", tenantId, spot.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requiredSkillSet").isNotEmpty());
    }

    @Test
    public void getNonExistentSpotTest() {
        Integer tenantId = 1;

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/spot/{id}", tenantId, -1L)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: No Spot entity found with ID (-1).");
    }

    @Test
    public void getNonMatchingSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);
        spotService.createSpot(tenantId, spot);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/spot/{id}", 2,
                                                           spot.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: The " +
                                     "tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void deleteSpotTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);
        spotService.createSpot(tenantId, spot);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/spot/{id}", tenantId, spot.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentSpotTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/spot/{id}", 1, -1L)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("false"));
    }

    @Test
    public void deleteNonMatchingSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);
        spotService.createSpot(tenantId, spot);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .delete("/rest/tenant/{tenantId}/spot/{id}", 2, spot.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void createSpotTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(spot);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/spot/add", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requiredSkillSet").isNotEmpty());
    }

    @Test
    public void createNonMatchingSpotTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(spot);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .post("/rest/tenant/{tenantId}/spot/add", 2)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void updateSpotTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, Collections.emptySet());
        spotService.createSpot(tenantId, spot);

        Spot spot2 = new Spot(tenantId, "name2", testSkillSet);
        spot2.setId(spot.getId());
        String body = (new ObjectMapper()).writeValueAsString(spot2);

        mvc.perform(MockMvcRequestBuilders
                            .put("/rest/tenant/{tenantId}/spot/update", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("name2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requiredSkillSet").isNotEmpty());
    }

    @Test
    public void updateNonMatchingSpotTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, Collections.emptySet());
        spotService.createSpot(tenantId, spot);

        Spot spot2 = new Spot(tenantId, "name2", testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(spot2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/spot/update", 2)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (2) does not match the persistable (name2)'s tenantId (1).");
    }

    @Test
    public void updateNonExistentSpotTest() throws Exception {
        Spot spot = new Spot(1, "name", Collections.emptySet());
        spot.setId(-1L);
        String body = (new ObjectMapper()).writeValueAsString(spot);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/spot/update", 1)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: Spot entity with ID (-1) not found.");
    }

    @Test
    public void updateChangeTenantIdSpotTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, Collections.emptySet());
        spotService.createSpot(tenantId, spot);

        Spot spot2 = new Spot(2, name, testSkillSet);
        spot2.setId(spot.getId());
        String body = (new ObjectMapper()).writeValueAsString(spot2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/spot/update", 2)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalState" +
                                     "Exception: Spot entity with tenantId (1) cannot change tenants.");
    }
}
