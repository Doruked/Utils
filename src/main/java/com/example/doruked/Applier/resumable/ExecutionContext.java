package com.example.doruked.Applier.resumable;

import com.example.doruked.Applier.Applier;

import java.util.concurrent.Future;

/**
 * Note: It is likely recommended that you create your own unrelated "execution context",
 * than look to this one for behavior. This interface is here to represent that something
 * like an execution context may be needed in creating a resumable effect. This is not to
 * say that the behavior provided here is what it means to be one generically. I quite
 * frankly don't have an answer to that. I recommend also that you use a different name
 * as "ExecutionContext" and "EffectContext" can be confusing.
 *
 * My Use:
 * This interface couples optional and non-optional information needed to resume an effect.
 * {@link TNode} contains non-optional data. This should at minimum require the effect
 * and input used by a given call to {@link Applier#apply(Object, Object).
 * In addition {@code TNode} contains, as it's node relation, all previously all previously
 * exeucuted effects + input, and any known future executions from the point of it's creation.
 * This is more relevant if the applier potientially generates more work in the application of any effect.
 *
 * All other values are optional, which include {@link TPosition}, and {@link TFuture}
 * {@link TPosition} is used if at the point of stopping, execution was mid some iteration. This value allows
 * resuming at the exact point of exiting. Of note, this may require multiple values if there is nested iteration.
 * {@link TFuture} for my use, applying effects entails notifying observers. These observers relay {@link Future}s
 * if their results are not ready. {@link TFuture} stores the {@code future} in the event execution stopped,
 * but the result was not retrieved yet.
 *
 * @param <TNode> a node containing the non-optional data needed to re-execute an effect
 * @param <TPosition> index information needed to resume execution if it stopped mid iteration
 * @param <TFuture> the unused future generated within a stopped execution
 */
public interface ExecutionContext<TNode, TPosition, TFuture> {


    TNode getExecutionTree();

    TPosition getPosition();

    TFuture getFuture();


    /**
     * Creates a basic immutable instance of {@link ExecutionContext}
     *
     * @param <TNode> a node containing the non-optional data needed to re-execute an effect
     * @param <TPosition> index information needed to resume execution if it stopped mid iteration
     * @param <TFuture> the unused future generated within a stopped execution
     */
    class Basic<TNode, TPosition, TFuture> implements ExecutionContext<TNode, TPosition, TFuture> {

        private final TNode node;
        private final TPosition position;
        private final TFuture future;

        private Basic(TNode node, TPosition position, TFuture future) {
            this.node = node;
            this.position = position;
            this.future = future;
        }

        @Override
        public TNode getExecutionTree() {
            return node;
        }

        @Override
        public TPosition getPosition() {
            return position;
        }

        @Override
        public TFuture getFuture() {
            return future;
        }

//Factories

        /**
         * Designed for {@link ObservableApplier} and named to indicate it should be used when only
         * a {@code node} and {@code index} are applicable
         *
         * @param executionTree a node containing the non-optional data needed to re-execute an effect
         * @param index         index information needed to resume execution if it stopped mid iteration
         * @return an effect context for notification
         */
        public static <TNode, TFuture, TPosition> Basic<TNode, TPosition, TFuture> createAtIndex(TNode executionTree, TPosition index) {
            return new Basic<>(executionTree, index, null);
        }

        /**
         * Designed for {@link ObservableApplier} and named to indicate it should be used when {@link Future}s
         * are applicable to effect execution.
         *
         * @param executionTree a node containing the non-optional data needed to re-execute an effect
         * @param future        the unused future generated within a stopped execution
         * @return an effect context for notification
         */
        public static <TNode, TFuture, TPosition> Basic<TNode, TPosition, TFuture> createWithFuture(TNode executionTree, TFuture future) {
            return new Basic<>(executionTree, null, future);
        }

        /**
         * Designed for {@link ObservableApplier} and named to indicate it should be used when {@link Future}s
         * are applicable to effect execution.
         *
         * @param executionTree a node containing the non-optional data needed to re-execute an effect
         * @param index         index information needed to resume execution if it stopped mid iteration
         * @param future        the unused future generated within a stopped execution
         * @return an effect context for indexed futures
         */
        public static <TNode, TFuture, TPosition> Basic<TNode, TPosition, TFuture> createWithFuture(TNode executionTree, TFuture future, TPosition index) {
            return new Basic<>(executionTree, index, future);
        }

        /**
         * Designed for {@link ObservableApplier} and named according to when this method should be called in said class.
         *
         * @param executionTree a node containing the non-optional data needed to re-execute an effect
         * @return an effect context for notification
         */
        public static <TNode, TFuture, TPosition> Basic<TNode, TPosition, TFuture> createForNotification(TNode executionTree) {
            return new Basic<>(executionTree, null, null);
        }

    }
}
