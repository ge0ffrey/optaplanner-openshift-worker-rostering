package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.shiftroster;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import elemental2.promise.Promise;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.LocalDateRange;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.Lockable;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.ShiftRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_DATE_RANGE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_INVALIDATE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_PAGINATION;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_UPDATE;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_END;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_START;

@Singleton
public class ShiftRosterPageViewportBuilder {

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

    @Inject
    private LoadingSpinner loadingSpinner;

    private ShiftRosterPageViewport viewport;

    private boolean isUpdatingRoster;
    private boolean isSolving;

    private final int WORK_LIMIT_PER_CYCLE = 50;

    private Pagination pagination;
    private LocalDateRange localDateRange;
    private long currentWorkerStartTime;

    @PostConstruct
    public void init() {
        pagination = Pagination.of(0, 10);
        eventManager.subscribeToEvent(SOLVE_START, (m) -> this.onSolveStart());
        eventManager.subscribeToEvent(SOLVE_END, (m) -> this.onSolveEnd());
        eventManager.subscribeToEvent(SHIFT_ROSTER_PAGINATION, (pagination) -> {
            this.pagination = pagination;
            buildShiftRosterViewport(viewport);
        });

        eventManager.subscribeToEvent(SHIFT_ROSTER_INVALIDATE, (nil) -> {
            buildShiftRosterViewport(viewport);
        });

        eventManager.subscribeToEvent(SHIFT_ROSTER_DATE_RANGE, dr -> {
            localDateRange = dr;
            buildShiftRosterViewport(viewport);
        });

        RosterRestServiceBuilder.getRosterState(tenantStore.getCurrentTenantId(),
                                                FailureShownRestCallback.onSuccess((rs) -> {
                                                    eventManager.fireEvent(SHIFT_ROSTER_DATE_RANGE, new LocalDateRange(rs.getFirstDraftDate(), rs.getFirstDraftDate().plusDays(7)));
                                                }));
    }

    public ShiftRosterPageViewportBuilder withViewport(ShiftRosterPageViewport viewport) {
        this.viewport = viewport;
        return this;
    }

    public RepeatingCommand getWorkerCommand(final ShiftRosterView view, final Lockable<Map<Long, Lane<LocalDateTime, ShiftRosterMetadata>>> lockableLaneMap, final long timeWhenInvoked) {
        currentWorkerStartTime = timeWhenInvoked;

        if (view.getSpotList().isEmpty() && !pagination.isOnFirstPage()) {
            eventManager.fireEvent(SHIFT_ROSTER_PAGINATION, pagination.previousPage());
            return () -> false;
        }

        final Iterator<ShiftView> shiftViewsToAdd = commonUtils.flatten(view.getSpotIdToShiftViewListMap().values()).iterator();
        eventManager.fireEvent(SHIFT_ROSTER_UPDATE, view);
        setUpdatingRoster(true);

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
                    while (shiftViewsToAdd.hasNext() && workDone < WORK_LIMIT_PER_CYCLE) {
                        ShiftView toAdd = shiftViewsToAdd.next();
                        if (!laneIdFilteredSet.contains(toAdd.getSpotId())) {
                            Set<Long> shiftViewsId = view.getSpotIdToShiftViewListMap().get(toAdd.getSpotId()).stream().map(sv -> sv.getId()).collect(Collectors.toSet());
                            laneMap.get(toAdd.getSpotId()).filterGridObjects(ShiftGridObject.class,
                                                                             (sv) -> shiftViewsId.contains(sv.getId()));
                            laneIdFilteredSet.add(toAdd.getSpotId());
                        }
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
                        loadingSpinner.hideFor(viewport.getLoadingTaskId());
                        setUpdatingRoster(false);
                    }
                });
                return shiftViewsToAdd.hasNext();
            }
        };
    }

    private void setUpdatingRoster(boolean isUpdatingRoster) {
        this.isUpdatingRoster = isUpdatingRoster;
    }

    public boolean isSolving() {
        return isSolving;
    }

    private long getCurrentWorkerStartTime() {
        return currentWorkerStartTime;
    }

    public void onSolveStart() {
        viewport.lock();
        isSolving = true;
        Scheduler.get().scheduleFixedPeriod(() -> {
            if (!isUpdatingRoster) {
                setUpdatingRoster(true);
                getShiftRosterView().then(srv -> {
                    viewport.refresh(srv);
                    return promiseUtils.resolve();
                });
            }
            return isSolving();
        }, 2000);
    }

    public void onSolveEnd() {
        viewport.unlock();
        isSolving = false;
        eventManager.fireEvent(SHIFT_ROSTER_INVALIDATE);
    }

    public Promise<Void> buildShiftRosterViewport(final ShiftRosterPageViewport toBuild) {
        return getShiftRosterView().then((srv) -> {
            toBuild.refresh(srv);
            return promiseUtils.resolve();
        });
    }

    public Promise<ShiftRosterView> getShiftRosterView() {
        return promiseUtils
                           .promise(
                                    (res, rej) -> {
                                        RosterRestServiceBuilder
                                                                .getShiftRosterView(tenantStore.getCurrentTenantId(),
                                                                                    pagination.getPageNumber(), pagination.getNumberOfItemsPerPage(),
                                                                                    localDateRange
                                                                                                  .getStartDate()
                                                                                                  .toString(),
                                                                                    localDateRange.getEndDate().toString(),
                                                                                    FailureShownRestCallback.onSuccess((s) -> {
                                                                                        res.onInvoke(s);
                                                                                    }));
                                    });
    }
}
