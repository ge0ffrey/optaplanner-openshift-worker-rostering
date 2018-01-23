package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
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
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayView;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.FormPopup;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeSkillProficiency;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;

@Templated
public class EmployeeEditForm implements IsElement {

    @Inject
    private EmployeeSubform employeeSubform;

    @Inject
    @DataField
    private Div employeeSubformDiv;

    @Inject
    @DataField
    private Button saveButton;

    @Inject
    @DataField
    private Button cancelButton;

    @Inject
    @DataField
    private Button closeButton;

    @Inject
    @DataField
    private @Named(value = "h3") HeadingElement title;

    @Inject
    private TranslationService CONSTANTS;

    private static Employee employee;
    private static EmployeeListPanel panel;
    private static List<Skill> skills;

    private FormPopup popup;

    public static EmployeeEditForm create(SyncBeanManager beanManager, EmployeeListPanel employeePanel,
            Employee employeeData, List<
                    Skill> skillData) {
        panel = employeePanel;
        employee = employeeData;
        skills = skillData;
        return beanManager.lookupBean(EmployeeEditForm.class).newInstance();
    }

    @PostConstruct
    protected void initWidget() {
        employeeSubformDiv.getElement().appendChild(Element.as((Node) (employeeSubform.getElement()
                .getParentNode().getFirstChild())));
        employeeSubform.setEmployeeModel(new EmployeeModel(employee));

        title.setInnerSafeHtml(new SafeHtmlBuilder().appendEscaped(employee.getName())
                .toSafeHtml());
        popup = FormPopup.getFormPopup(this);
        popup.center();
    }

    @EventHandler("cancelButton")
    public void cancel(ClickEvent e) {
        popup.hide();
    }

    @EventHandler("closeButton")
    public void close(ClickEvent e) {
        popup.hide();
    }

    @EventHandler("saveButton")
    public void save(ClickEvent click) {
        employeeSubform.submit(new Callback<EmployeeModel, Set<ConstraintViolation<EmployeeModel>>>() {

            @Override
            public void onFailure(Set<ConstraintViolation<EmployeeModel>> validationErrorSet) {
                popup.hide();
                ErrorPopup.show(CommonUtils.delimitCollection(validationErrorSet, (e) -> e.getMessage(), "\n"));
            }

            @Override
            public void onSuccess(EmployeeModel employee) {
                popup.hide();
                EmployeeRestServiceBuilder.updateEmployee(employee.getTenantId(), employee,
                        new FailureShownRestCallback<Employee>() {

                            @Override
                            public void onSuccess(Employee employee) {
                                panel.refresh();
                            }
                        });
            }
        });

    }

}
