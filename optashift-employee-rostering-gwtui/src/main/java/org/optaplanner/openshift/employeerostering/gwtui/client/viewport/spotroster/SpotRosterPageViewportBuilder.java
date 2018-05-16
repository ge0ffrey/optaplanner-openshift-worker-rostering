package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.spotroster;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import elemental2.promise.Promise;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_END;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_START;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SPOT_ROSTER_PAGINATION;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SPOT_ROSTER_UPDATE;

@Singleton
public class SpotRosterPageViewportBuilder {

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private CommonUtils commonUtils;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private ManagedInstance<ShiftGridObject> shiftGridObjectInstances;

    @Inject
    private EventManager eventManager;

    private SpotRosterPageViewport viewport;

    private boolean isUpdatingRoster;
    private boolean isSolving;

    private final int WORK_LIMIT_PER_CYCLE = 50;

    private Pagination pagination;
    private long currentWorkerStartTime;

    @PostConstruct
    public void init() {
        pagination = Pagination.of(0, 10);
        eventManager.subscribeToEvent(SOLVE_START, (m) -> this.onSolveStart());
        eventManager.subscribeToEvent(SOLVE_END, (m) -> this.onSolveEnd());
        eventManager.subscribeToEvent(SPOT_ROSTER_PAGINATION, (pagination) -> {
            this.pagination = pagination;
            buildSpotRosterViewport(viewport);
        });
    }

    public SpotRosterPageViewportBuilder withViewport(SpotRosterPageViewport viewport) {
        this.viewport = viewport;
        return this;
    }

    public RepeatingCommand getWorkerCommand(final SpotRosterView view, final Map<Long, Lane<LocalDateTime, SpotRosterMetadata>> laneMap, final long timeWhenInvoked) {
        if (view.getSpotList().isEmpty()) {
            eventManager.fireEvent(SPOT_ROSTER_PAGINATION, pagination.previousPage());
            return () -> false;
        }

        currentWorkerStartTime = timeWhenInvoked;

        final Iterator<ShiftView> shiftViewsToAdd = commonUtils.flatten(view.getSpotIdToShiftViewListMap().values()).iterator();
        eventManager.fireEvent(SPOT_ROSTER_UPDATE, view);
        setUpdatingRoster(true);

        return new RepeatingCommand() {

            final long timeWhenStarted = timeWhenInvoked;

            @Override
            public boolean execute() {
                if (timeWhenStarted != getCurrentWorkerStartTime()) {
                    return false;
                }
                int workDone = 0;
                while (shiftViewsToAdd.hasNext() && workDone < WORK_LIMIT_PER_CYCLE) {
                    ShiftView toAdd = shiftViewsToAdd.next();
                    laneMap.get(toAdd.getSpotId()).addOrUpdateGridObject(
                                                                         ShiftGridObject.class, toAdd.getId(), () -> {
                                                                             ShiftGridObject out = shiftGridObjectInstances.get();
                                                                             out.withShiftView(toAdd);
                                                                             return out;
                                                                         }, (s) -> {
                                                                             s.withShiftView(toAdd);
                                                                             return null;
                                                                         });
                    workDone++;
                }

                if (!shiftViewsToAdd.hasNext()) {
                    laneMap.forEach((l, lane) -> lane.endModifying());
                    setUpdatingRoster(false);
                }
                return shiftViewsToAdd.hasNext();
            }
        };
    }

    private void setUpdatingRoster(boolean isUpdatingRoster) {
        this.isUpdatingRoster = isUpdatingRoster;
    }

    private boolean isSolving() {
        return isSolving;
    }

    private long getCurrentWorkerStartTime() {
        return currentWorkerStartTime;
    }

    public void onSolveStart() {
        isSolving = true;
        Scheduler.get().scheduleFixedPeriod(() -> {
            if (!isUpdatingRoster) {
                setUpdatingRoster(true);
                getSpotRosterView().then(srv -> {
                    viewport.refresh(srv);
                    return promiseUtils.resolve();
                });
            }
            return isSolving();
        }, 2000);
    }

    public void onSolveEnd() {
        isSolving = false;
    }

    public Promise<Void> buildSpotRosterViewport(final SpotRosterPageViewport toBuild) {
        return getSpotRosterView().then((srv) -> {
            toBuild.refresh(srv);
            return promiseUtils.resolve();
        });
    }

    public Promise<SpotRosterView> getSpotRosterView() {
        return promiseUtils.promise((res, rej) -> {
            RosterRestServiceBuilder.getCurrentSpotRosterView(tenantStore.getCurrentTenantId(), pagination.getPageNumber(), pagination.getNumberOfItemsPerPage(),
                                                              FailureShownRestCallback.onSuccess((s) -> {
                                                                  res.onInvoke(s);
                                                              }));
        });
    }
}
