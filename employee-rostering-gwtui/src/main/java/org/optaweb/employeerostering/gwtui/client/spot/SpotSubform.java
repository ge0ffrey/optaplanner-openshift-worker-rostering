/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.spot;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.HTMLTableCellElement;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.AutoTrimWhitespaceTextBox;
import org.optaweb.employeerostering.gwtui.client.common.DataInvalidation;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.StringListToSkillSetConverter;
import org.optaweb.employeerostering.gwtui.client.common.TableRow;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.OptaWebUIConstants;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.util.CommonUtils;
import org.optaweb.employeerostering.shared.skill.Skill;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestServiceBuilder;

@Templated("#row")
public class SpotSubform extends TableRow<Spot> implements TakesValue<Spot> {

    @Inject
    private TenantStore tenantStore;

    @Inject
    private StringListToSkillSetConverter skillConvertor;

    @Inject
    @DataField("spot-name-text-box")
    private AutoTrimWhitespaceTextBox spotName;

    @Inject
    @DataField("spot-required-skill-set-select")
    private MultipleSelect spotRequiredSkillSet;

    @Inject
    @DataField("spot-name-display")
    @Named("td")
    private HTMLTableCellElement spotNameDisplay;

    @Inject
    @DataField("spot-required-skill-set-display")
    @Named("td")
    private HTMLTableCellElement spotRequiredSkillSetDisplay;

    @Inject
    private Event<DataInvalidation<Spot>> dataInvalidationEvent;

    @Inject
    private TranslationService translationService;

    private Subscription subscription;

    @Inject
    private CommonUtils commonUtils;

    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void initWidget() {
        spotName.getElement().setAttribute("placeholder", translationService.format(
                                                                                    OptaWebUIConstants.SpotListPanel_spotName));
        dataBinder.getModel().setTenantId(tenantStore.getCurrentTenantId());
        updateSkillMap(skillConvertor.getSkillMap());
        dataBinder.bind(spotName, "name");
        dataBinder.bind(spotRequiredSkillSet, "requiredSkillSet", skillConvertor);

        dataBinder.<String> addPropertyChangeHandler("name", (e) -> {
            spotNameDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(e.getNewValue()).toSafeHtml().asString();
        });
        dataBinder.<Set<Skill>> addPropertyChangeHandler("requiredSkillSet", (e) -> {
            spotRequiredSkillSetDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(commonUtils.delimitCollection(e
                                                                                                                       .getNewValue(),
                                                                                                                      (s) -> s.getName(), ", ")).toSafeHtml().asString();
        });
        subscription = ErraiBus.get().subscribe("SkillMapListener", (m) -> updateSkillMap(m.getValue(Map.class)));
    }

    public void reset() {
        spotName.setValue("");
    }

    private void updateSkillMap(Map<String, Skill> skillMap) {
        spotRequiredSkillSet.clear();
        skillMap.forEach((name, skill) -> {
            Option option = new Option();
            option.setName(name);
            option.setValue(name);
            option.setText(name);
            spotRequiredSkillSet.add(option);
        });
        spotRequiredSkillSet.refresh();
    }

    @Override
    protected void deleteRow(Spot spot) {
        SpotRestServiceBuilder.removeSpot(tenantStore.getCurrentTenantId(), spot.getId(),
                                          FailureShownRestCallback.onSuccess(success -> {
                                              dataInvalidationEvent.fire(new DataInvalidation<>());
                                          }));
    }

    @Override
    protected void updateRow(Spot oldValue, Spot newValue) {
        SpotRestServiceBuilder.updateSpot(tenantStore.getCurrentTenantId(), newValue,
                                          FailureShownRestCallback.onSuccess(v -> {
                                              dataInvalidationEvent.fire(new DataInvalidation<>());
                                          }));
    }

    @Override
    protected void createRow(Spot spot) {
        SpotRestServiceBuilder.addSpot(tenantStore.getCurrentTenantId(), spot,
                                       FailureShownRestCallback.onSuccess(v -> {
                                           dataInvalidationEvent.fire(new DataInvalidation<>());
                                       }));
    }

    @EventHandler("row")
    public void onUnload(@ForEvent("unload") elemental2.dom.Event e) {
        subscription.remove();
    }

    @Override
    protected void focusOnFirstInput() {
        spotName.setFocus(true);
    }
}
