package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLTableRowElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.StateSync;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;

public abstract class TableRow<T> extends Composite implements TakesValue<T> {

    @Inject
    private Validator validator;

    @Inject
    protected DataBinder<T> dataBinder;

    @Inject
    @DataField("delete")
    private HTMLAnchorElement deleteCell;
    @Inject
    @DataField("edit")
    private HTMLAnchorElement editCell;

    @Inject
    @DataField("save")
    private HTMLButtonElement commitChanges;
    @Inject
    @DataField("cancel")
    private HTMLButtonElement cancelChanges;

    @Inject
    @DataField("editor")
    private HTMLTableRowElement editor;
    @Inject
    @DataField("presenter")
    private HTMLTableRowElement presenter;

    @Inject
    private CommonUtils commonUtils;

    // Not Null if and only if this row is a row for creating an instance
    private KiePager<T> pager;
    // Not Null if and only if this row is a row for creating an instance
    private ListComponent<T, ?> table;

    @PostConstruct
    private void init() {
        setEditing(false);
    }

    @EventHandler("edit")
    public void onEditClick(final @ForEvent("click") MouseEvent e) {
        setEditing(true);
    }

    @EventHandler("delete")
    public void onDeleteClick(final @ForEvent("click") MouseEvent e) {
        deleteRow(getValue());
    }

    @EventHandler("cancel")
    public void onCancelClick(final @ForEvent("click") MouseEvent e) {
        setEditing(false);
        if (isCreatingRow()) {
            table.getValue().remove(getOldValue());
            pager.refresh();
        }
    }

    @EventHandler("save")
    public void onSaveClick(final @ForEvent("click") MouseEvent e) {
        try {
            if (isCreatingRow()) {
                createRow(tryToGetNewValue());
            } else {
                updateRow(getValue(), tryToGetNewValue());
            }
            commitChanges();
            setEditing(false);
        } catch (ConstraintViolationException validationException) {
            Set<ConstraintViolation<?>> validationErrorSet = validationException.getConstraintViolations();
            ErrorPopup.show(commonUtils.delimitCollection(validationErrorSet, (violation) -> violation.getMessage(), "\n"));
        }
    }

    public void setEditing(boolean isEditing) {
        if (isEditing) {
            dataBinder.pause();
        } else {
            dataBinder.setModel(getOldValue(), StateSync.FROM_MODEL, true);
        }
        presenter.hidden = isEditing;
        editor.hidden = !isEditing;

        if (isEditing) {
            focusOnFirstInput();
        }
    }

    public void markAsCreator(ListComponent<T, ?> table, KiePager<T> pager) {
        this.table = table;
        this.pager = pager;
    }

    public T getOldValue() {
        return dataBinder.getModel();
    }

    public T getNewValue() {
        return dataBinder.getWorkingModel();
    }

    @Override
    public void setValue(T value) {
        dataBinder.setModel(value, StateSync.FROM_MODEL, true);
    }

    public void commitChanges() {
        setValue(getNewValue());
    }

    @Override
    public T getValue() {
        return dataBinder.getModel();
    }

    public Set<ConstraintViolation<?>> validate() {
        // Stupid workaround since Set<T> cannot be casted to Set<?>
        return new HashSet<>(validator.validate(getNewValue()));
    }

    public T tryToGetNewValue() {
        Set<ConstraintViolation<?>> validationErrorSet = validate();
        if (validationErrorSet.isEmpty()) {
            return getNewValue();
        } else {
            throw new ConstraintViolationException(validationErrorSet);
        }

    }

    public static <T> void createNewRow(T value, ListComponent<T, ? extends TableRow<T>> table, KiePager<T> pager) {
        if (table.getValue().isEmpty() || !table.getComponent(0).isCreatingRow()) {
            pager.getPager().firstPage();
            table.getValue().add(0, value);
            pager.refresh();
            table.getComponent(0).setEditing(true);
            table.getComponent(0).markAsCreator(table, pager);
        }
    }

    protected boolean isCreatingRow() {
        return null != table;
    }

    protected abstract void focusOnFirstInput();

    protected abstract void deleteRow(T value);

    protected abstract void updateRow(T oldValue, T newValue);

    protected abstract void createRow(T value);
}
