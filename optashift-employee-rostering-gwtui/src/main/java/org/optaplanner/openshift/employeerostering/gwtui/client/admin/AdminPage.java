package org.optaplanner.openshift.employeerostering.gwtui.client.admin;

import javax.inject.Inject;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.MouseEvent;
import elemental2.promise.Promise;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.admin.AdminRestServiceBuilder;

@Templated
public class AdminPage implements Page {

    @Inject
    @DataField("tenant-list")
    TenantListPanel tenantListPanel;

    @Inject
    @DataField("reset-application-button")
    HTMLButtonElement resetApplicationButton;

    @Override
    public Promise<Void> beforeOpen() {
        return tenantListPanel.refresh();
    }

    @EventHandler("reset-application-button")
    private void resetApplication(@ForEvent("click") MouseEvent e) {
        AdminRestServiceBuilder.resetApplication(FailureShownRestCallback.onSuccess((success) -> {
            ErrorPopup.show("Application was successfully reset");
        }));
    }
}
