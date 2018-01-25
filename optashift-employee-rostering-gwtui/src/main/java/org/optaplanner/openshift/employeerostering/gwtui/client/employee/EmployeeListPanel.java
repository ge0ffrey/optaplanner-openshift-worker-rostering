package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;

import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import elemental2.promise.Promise;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.tagsinput.client.ui.base.SingleValueTagsInput;
import org.gwtbootstrap3.extras.typeahead.client.base.CollectionDataset;
import org.gwtbootstrap3.extras.typeahead.client.base.Dataset;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;

import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_actions;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_delete;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_edit;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_name;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_skills;

@Dependent
@Templated
public class EmployeeListPanel implements IsElement,
                                          Page {

    @Inject
    @DataField
    private Button refreshButton;

    @Inject
    @DataField
    private EmployeeSubform employeeSubform;

    //@Inject
    //@DataField
    //private EmployeeSubform employeeSubform1;

    //@Inject
    //@DataField
    //private Div employeeSubformDiv;

    @Inject
    @DataField
    private Button addButton;

    @Inject
    private Instance<EmployeeEditForm> editForm;

    // TODO use DataGrid instead
    @DataField
    private CellTable<Employee> table;
    @DataField
    private Pagination pagination;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Employee> dataProvider = new ListDataProvider<>();

    @Inject
    private TenantStore tenantStore;

    @Inject
    private SyncBeanManager beanManager;
    @Inject
    private TranslationService CONSTANTS;

    public EmployeeListPanel() {
        table = new CellTable<>(15);
        table.setBordered(true);
        table.setCondensed(true);
        table.setStriped(true);
        table.setHover(true);
        table.setHeight("100%");
        table.setWidth("100%");
        pagination = new Pagination();
    }

    @PostConstruct
    protected void initWidget() {
        initTable();
    }

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenant) {
        tenantId = tenant.getId();
        employeeSubform.setTenantId(tenantId);
        refresh();
    }

    @EventHandler("refreshButton")
    public void refresh(ClickEvent e) {
        refresh();
    }

    public Promise<Void> refresh() {
        return refreshSkillsTagsInput().then(i -> refreshTable());
    }

    private Promise<Void> refreshSkillsTagsInput() {

        if (tenantStore.getCurrentTenantId() == null) {
            return PromiseUtils.resolve();
        }
        SkillRestServiceBuilder.getSkillList(tenantId, new FailureShownRestCallback<List<Skill>>() {

        return new Promise<>((res, rej) -> {
            SkillRestServiceBuilder.getSkillList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(skillList -> {
                employeeSubform.setSkillList(skillList);
                res.onInvoke(PromiseUtils.resolve());
            }));
        });
    }

    private void initTable() {
        table.addColumn(new TextColumn<Employee>() {

            @Override
            public String getValue(Employee employee) {
                return employee.getName();
            }
        }, CONSTANTS.format(General_name));
        table.addColumn(new TextColumn<Employee>() {

            @Override
            public String getValue(Employee employee) {
                Set<Skill> skillProficiencySet = employee.getSkillProficiencySet();
                if (skillProficiencySet == null) {
                    return "";
                }
                return skillProficiencySet.stream().map(skillProficiency -> skillProficiency.getName())
                        .collect(Collectors.joining(", "));
            }
        }, CONSTANTS.format(General_skills));
        Column<Employee, String> deleteColumn = new Column<Employee, String>(new ButtonCell(IconType.REMOVE,
                                                                                            ButtonType.DANGER, ButtonSize.SMALL)) {

            @Override
            public String getValue(Employee employee) {
                return CONSTANTS.format(General_delete);
            }
        };
        deleteColumn.setFieldUpdater((index, employee, value) -> {
            EmployeeRestServiceBuilder.removeEmployee(tenantStore.getCurrentTenantId(), employee.getId(), FailureShownRestCallback.onSuccess(i -> {
                refreshTable();
            }));
        });
        Column<Employee, String> editColumn = new Column<Employee, String>(new ButtonCell(IconType.EDIT,
                                                                                          ButtonType.DEFAULT, ButtonSize.SMALL)) {

            @Override
            public String getValue(Employee employee) {
                return CONSTANTS.format(General_edit);
            }
        };
        editColumn.setFieldUpdater((index, employee, value) -> {
            EmployeeListPanel employeeListPanel = this;
            SkillRestServiceBuilder.getSkillList(tenantId, new FailureShownRestCallback<List<Skill>>() {

                @Override
                public void onSuccess(List<Skill> skillList) {
                    editForm
                            .get()
                            .setEmployee(employee)
                            .setSkillList(skillList)
                            .setCaller(employeeListPanel)
                            .show();
                }
            });
        });
        table.addColumn(deleteColumn, CONSTANTS.format(General_actions));
        table.addColumn(editColumn);

        table.addRangeChangeHandler(event -> pagination.rebuild(pager));

        pager.setDisplay(table);
        pagination.clear();
        dataProvider.addDataDisplay(table);
    }

    private Promise<Void> refreshTable() {
        if (tenantStore.getCurrentTenantId() == null) {
            return PromiseUtils.resolve();
        }
        return new Promise<>((res, rej) -> {
            EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(employeeList -> {
                dataProvider.setList(employeeList);
                dataProvider.flush();
                pagination.rebuild(pager);
                res.onInvoke(PromiseUtils.resolve());
            }));
        });
    }

    @EventHandler("addButton")
    public void add(ClickEvent e) {
        if (tenantStore.getCurrentTenantId() == null) {
            throw new IllegalStateException("The tenantStore.getTenantId() (" + tenantStore.getCurrentTenantId() + ") can not be null at this time.");
        }
        employeeSubform.submit(new Callback<EmployeeModel, Set<ConstraintViolation<EmployeeModel>>>() {

            @Override
            public void onFailure(Set<ConstraintViolation<EmployeeModel>> validationErrorSet) {
                ErrorPopup.show(CommonUtils.delimitCollection(validationErrorSet, (e) -> e.getMessage(), "\n"));
            }

            @Override
            public void onSuccess(EmployeeModel employee) {
                EmployeeRestServiceBuilder.addEmployee(tenantId, employee, new FailureShownRestCallback<Employee>() {

                    @Override
                    public void onSuccess(Employee employee) {
                        refreshTable();
                    }
                });
            }

        });
    }
}
