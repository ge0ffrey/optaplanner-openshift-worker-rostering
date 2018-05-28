package org.optaplanner.openshift.employeerostering.gwtui.client.viewport;

import elemental2.dom.CSSStyleDeclaration;
import elemental2.dom.DOMTokenList;
import elemental2.dom.HTMLElement;
import elemental2.promise.IThenable;
import elemental2.promise.IThenable.ThenOnFulfilledCallbackFn;
import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn.ResolveUnionType;
import jsinterop.annotations.JsOverlay;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.SingleGridObject;

import static org.mockito.Mockito.mock;

public abstract class AbstractViewportTest {

    PromiseExecutorCallbackFn rootPromise;

    protected SingleGridObject<Double, Object> getSingleGridObject(Double start, Double end) {
        return new MockGridObject(start, end);
    }

    protected DoubleScale getScale(Double end) {
        return new DoubleScale(end);
    }

    @SuppressWarnings("unchecked")
    public <T> Promise<T> promise(PromiseExecutorCallbackFn callback) {
        Promise<T> promise = Mockito.mock(Promise.class);
        if (rootPromise == null) {
            rootPromise = callback;
        }
        Mockito.doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ThenOnFulfilledCallbackFn onFulfilled = invocation.getArgument(0);
                Object[] out = new Object[1];
                rootPromise.onInvoke((res) -> {
                    out[0] = onFulfilled.onInvoke(res.asT());
                }, (rej) -> {
                });
                rootPromise = getCallbackFn(out[0]);
                return promise(rootPromise);
            }
        }).when(promise).then(Mockito.any());
        return promise;
    }

    private PromiseExecutorCallbackFn getCallbackFn(Object val) {
        return (resolve, reject) -> {
            resolve.onInvoke(resolveValue(val));
            rootPromise = null;
        };
    }

    protected <T> ResolveUnionType<T> resolveValue(T value) {
        return new MockResolveUnionType<>(value);
    }

    private static class MockResolveUnionType<T> implements ResolveUnionType<T> {

        private T value;

        public MockResolveUnionType(T value) {
            this.value = value;
        }

        @JsOverlay
        public IThenable<T> asIThenable() {
            return null;
        }

        @JsOverlay
        public T asT() {
            return value;
        }
    }

    private static class DoubleScale implements LinearScale<Double> {

        private Double end;

        public DoubleScale(Double end) {
            this.end = end;
        }

        @Override
        public double toGridUnits(Double valueInScaleUnits) {
            return valueInScaleUnits;
        }

        @Override
        public Double toScaleUnits(double valueInGridPixels) {
            return valueInGridPixels;
        }

        @Override
        public Double getEndInScaleUnits() {
            return end;
        }
    }

    private static class MockGridObject implements SingleGridObject<Double, Object> {

        private Double start;
        private Double end;
        private HTMLElement element;
        private Lane<Double, Object> lane;

        public MockGridObject(Double start, Double end) {
            this.start = start;
            this.end = end;
            element = mock(HTMLElement.class);
            element.style = mock(CSSStyleDeclaration.class);
            element.classList = mock(DOMTokenList.class);
        }

        @Override
        public Double getStartPositionInScaleUnits() {
            return start;
        }

        @Override
        public void setStartPositionInScaleUnits(Double newStartPosition) {
            this.start = newStartPosition;
        }

        @Override
        public Double getEndPositionInScaleUnits() {
            return end;
        }

        @Override
        public void setEndPositionInScaleUnits(Double newEndPosition) {
            this.end = newEndPosition;
        }

        @Override
        public void withLane(Lane<Double, Object> lane) {
            this.lane = lane;
        }

        @Override
        public Long getId() {
            return null;
        }

        @Override
        public Lane<Double, Object> getLane() {
            return lane;
        }

        @Override
        public void save() {}

        @Override
        public HTMLElement getElement() {
            return element;
        }

    }
}
