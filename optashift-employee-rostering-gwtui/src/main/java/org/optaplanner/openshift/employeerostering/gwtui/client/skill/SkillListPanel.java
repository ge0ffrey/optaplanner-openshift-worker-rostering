package org.optaplanner.openshift.employeerostering.gwtui.client.skill;

import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.MouseEvent;
import elemental2.promise.Promise;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.DataInvalidation;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.KiePager;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.KieSearchBar;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;

@Templated
public class SkillListPanel implements IsElement,
                            Page {

    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;
    @Inject
    @DataField("add-button")
    private HTMLButtonElement addButton;

    @Inject
    @DataField("pager")
    private KiePager<Skill> pager;

    @Inject
    @DataField("search-bar")
    private KieSearchBar searchBar;

    @Inject
    private TenantStore tenantStore;

    @Inject
    @DataField("table")
    @ListContainer("table")
    private ListComponent<Skill, SkillSubform> table;

    @Inject
    @DataField("name-header")
    @Named("th")
    private HTMLTableCellElement skillNameHeader;

    public SkillListPanel() {}

    @PostConstruct
    protected void initWidget() {
        initTable();
    }

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    public void onAnyInvalidationEvent(@Observes DataInvalidation<Skill> skill) {
        refresh();
    }

    @EventHandler("refresh-button")
    public void refresh(final @ForEvent("click") MouseEvent e) {
        refresh();
    }

    public Promise<Void> refresh() {
        if (tenantStore.getCurrentTenantId() == null) {
            return PromiseUtils.resolve();
        }
        return new Promise<>((res, rej) -> {
            SkillRestServiceBuilder.getSkillList(tenantStore.getCurrentTenantId(), FailureShownRestCallback
                                                                                                           .onSuccess(newSkillList -> {
                                                                                                               pager.setData(newSkillList);
                                                                                                               res.onInvoke(PromiseUtils.resolve());
                                                                                                           }));
        });
    }

    private void initTable() {
        pager.setData(Collections.emptyList());
        pager.setPresenter(table);
    }

    @EventHandler("add-button")
    public void add(final @ForEvent("click") MouseEvent e) {
        SkillSubform.createNewRow(new Skill(tenantStore.getCurrentTenantId(), ""), table, pager);
    }

    @EventHandler("name-header")
    public void spotNameHeaderClick(final @ForEvent("click") MouseEvent e) {
        pager.sortBy((a, b) -> CommonUtils.stringWithIntCompareTo(a.getName(), b.getName()));
    }
}
