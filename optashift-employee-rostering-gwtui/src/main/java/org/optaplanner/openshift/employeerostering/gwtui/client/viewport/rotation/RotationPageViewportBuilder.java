package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.rotation;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import elemental2.promise.Promise;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.Lockable;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.RotationView;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.ROTATION_INVALIDATE;

@Singleton
public class RotationPageViewportBuilder {

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private CommonUtils commonUtils;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private ManagedInstance<ShiftTemplateModel> shiftTemplateModelInstances;
    
    @Inject
    private EventManager eventManager;

    @Inject
    private LoadingSpinner loadingSpinner;

    private RotationPageViewport viewport;

    private final int WORK_LIMIT_PER_CYCLE = 50;

    private long currentWorkerStartTime;

    @PostConstruct
    private void init() {
        eventManager.subscribeToEvent(ROTATION_INVALIDATE, e -> buildRotationViewport(viewport));
    }

    public RotationPageViewportBuilder withViewport(RotationPageViewport viewport) {
        this.viewport = viewport;
        return this;
    }

    public RepeatingCommand getWorkerCommand(final RotationView view, final Lockable<Map<Long, Lane<LocalDateTime, RotationMetadata>>> lockableLaneMap, final long timeWhenInvoked) {
        currentWorkerStartTime = timeWhenInvoked;
        final Iterator<ShiftTemplateView> shiftTemplateViewsToAdd = commonUtils.flatten(view.getSpotIdToShiftTemplateViewListMap().values()).iterator();

        return new RepeatingCommand() {

            final long timeWhenStarted = timeWhenInvoked;
            final Set<Long> laneIdFilteredSet = new HashSet<>();

            @Override
            public boolean execute() {
                if (timeWhenStarted != getCurrentWorkerStartTime()) {
                    return false;
                }
                lockableLaneMap.acquireIfPossible(laneMap -> {
                    int workDone = 0;
                    while (shiftTemplateViewsToAdd.hasNext() && workDone < WORK_LIMIT_PER_CYCLE) {
                        ShiftTemplateView toAdd = shiftTemplateViewsToAdd.next();
                        if (!laneIdFilteredSet.contains(toAdd.getSpotId())) {
                            Set<Long> shiftTemplateModelId = view.getSpotIdToShiftTemplateViewListMap().get(toAdd.getSpotId()).stream().map(sv -> sv.getId()).collect(Collectors.toSet());
                            laneMap.get(toAdd.getSpotId()).filterGridObjects(ShiftTemplateModel.class,
                                                                             (sv) -> shiftTemplateModelId.contains(sv.getId()));
                            laneIdFilteredSet.add(toAdd.getSpotId());
                        }
                        laneMap.get(toAdd.getSpotId()).addOrUpdateGridObject(
                                                                             ShiftTemplateModel.class, toAdd.getId(), () -> {
                                                                                 ShiftTemplateModel out = shiftTemplateModelInstances.get();
                                                                                 out.withShiftTemplateView(toAdd);
                                                                                 return out;
                                                                             }, (s) -> {
                                                                                 s.withShiftTemplateView(toAdd);
                                                                                 return null;
                                                                             });
                        workDone++;
                    }

                    if (!shiftTemplateViewsToAdd.hasNext()) {
                        laneMap.forEach((l, lane) -> lane.endModifying());
                        loadingSpinner.hideFor(viewport.getLoadingTaskId());
                    }
                });
                return shiftTemplateViewsToAdd.hasNext();
            }
        };
    }

    private long getCurrentWorkerStartTime() {
        return currentWorkerStartTime;
    }

    public Promise<Void> buildRotationViewport(final RotationPageViewport toBuild) {
        return getRotationView().then((srv) -> {
            toBuild.refresh(srv);
            return promiseUtils.resolve();
        });
    }

    public Promise<RotationView> getRotationView() {
        return promiseUtils.promise((res, rej) -> {
            ShiftRestServiceBuilder.getRotation(tenantStore.getCurrentTenantId(),
                                                FailureShownRestCallback.onSuccess((rv) -> {
                                                    res.onInvoke(rv);
                                                }));
        });
    }
}
