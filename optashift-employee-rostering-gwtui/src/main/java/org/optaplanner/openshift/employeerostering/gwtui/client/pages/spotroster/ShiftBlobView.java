/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.spotroster;

import java.time.OffsetDateTime;

import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListElementView;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListView;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.BlobPopover;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;

@Templated
public class ShiftBlobView implements BlobView<OffsetDateTime, ShiftBlob> {

    private static final Long BLOB_POSITION_DISPLACEMENT_IN_SCREEN_PIXELS = 3L;
    private static final Long BLOB_SIZE_DISPLACEMENT_IN_SCREEN_PIXELS = -5L;

    @Inject
    @DataField("blob")
    private HTMLDivElement root;

    @Inject
    @Named("span")
    @DataField("label")
    private HTMLElement label;

    @Inject
    private BlobPopover popover;

    private Viewport<OffsetDateTime> viewport;
    private ListView<ShiftBlob> blobViews;
    private Runnable onDestroy;

    private ShiftBlob blob;

    @Override
    public ListElementView<ShiftBlob> setup(final ShiftBlob blob,
                                            final ListView<ShiftBlob> blobViews) {

        this.blobViews = blobViews;
        this.blob = blob;

        refresh();

        // FIXME: Enable draggability and resizability after backend supports it.

        return this;
    }

    public void refresh() {

        if (blob.getShift().isPinnedByUser()) {
            getElement().classList.add("pinned");
        } else {
            getElement().classList.remove("pinned");
        }

        if (blob.getShift().getEmployee() == null) {
            getElement().classList.add("unassigned");
        } else {
            getElement().classList.remove("unassigned");
        }

        viewport.setPositionInScreenPixels(this, blob.getPositionInGridPixels(), BLOB_POSITION_DISPLACEMENT_IN_SCREEN_PIXELS);
        viewport.setSizeInScreenPixels(this, blob.getSizeInGridPixels(), BLOB_SIZE_DISPLACEMENT_IN_SCREEN_PIXELS);

        updateLabel();
    }

    private boolean onResize(final Long newSizeInGridPixels) {
        blob.setSizeInGridPixels(newSizeInGridPixels);
        //TODO: Update Shift's time slot
        //ShiftRestServiceBuilder.updateShift(blob.getShift().getTenantId(), new ShiftView(blob.getShift()));

        updateLabel();
        return true;
    }

    private boolean onDrag(final Long newPositionInGridPixels) {
        blob.setPositionInScaleUnits(viewport.getScale().toScaleUnits(newPositionInGridPixels));
        //TODO: Update Shift's time slot
        //ShiftRestServiceBuilder.updateShift(blob.getShift().getTenantId(), new ShiftView(blob.getShift()));

        updateLabel();
        return true;
    }

    @EventHandler("blob")
    public void onBlobClicked(final @ForEvent("click") MouseEvent e) {
        popover.showFor(this);
    }

    private void updateLabel() {
        label.textContent = blob.getLabel();
    }

    @Override
    public BlobView<OffsetDateTime, ShiftBlob> withViewport(final Viewport<OffsetDateTime> viewport) {
        this.viewport = viewport;
        return this;
    }

    @Override
    public BlobView<OffsetDateTime, ShiftBlob> withSubLane(final SubLane<OffsetDateTime> subLaneView) {
        return this;
    }

    @Override
    public void onDestroy(final Runnable onDestroy) {
        this.onDestroy = onDestroy;
    }

    @Override
    public void destroy() {
        onDestroy.run();
    }

    public void remove() {
        blobViews.remove(blob);
    }

    @Override
    public Blob<OffsetDateTime> getBlob() {
        return blob;
    }
}
