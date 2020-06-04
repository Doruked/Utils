package com.example.doruked.applier.resumable;

/**
 * This class packages two types of information used to help resume execution with a given {@link ContextualApplier}
 * The guidelines given are suggestions, as this class cannot strictly force an implementation of ContextualApplier to
 * behave a certain way.
 *
 *(1){@link TProcess} is to tell a {@code ContextualApplier} from where to resume execution. This requires coordination
 * with the specific applier to know it's resumable points.
 *(2){@link TExecutionContext} contains the data an applier needs to resume from the point specified from {@link TProcess}
 * To be used, this requires that the applier is coordinated to parse {@link TExecutionContext}
 *
 * @param <TProcess> a type denoting what failed from a previous execution
 * @param <TExecutionContext> the type containing relevant data existent at the point of failure
 */
public interface FailureContext<TProcess, TExecutionContext> {


     TProcess getFailedProcess();

     TExecutionContext getReExecute();

    /**
     * A basic immutable implementation of {@link FailureContext}
     *
     * @param <TFailed> a type denoting what failed from a previous execution
     * @param <TExecutionContext> the type containing the relevant data at the point of failure
     */
    class Basic<TFailed, TExecutionContext> implements FailureContext<TFailed, TExecutionContext> {


        private final TFailed failed;
        private final TExecutionContext executionContext;

        private Basic(TFailed failed, TExecutionContext executionContext) {
            this.failed = failed;
            this.executionContext = executionContext;
        }

        @Override
        public TFailed getFailedProcess() {
          return  failed;
        }

        @Override
        public TExecutionContext getReExecute() {
            return executionContext;
        }

        /**
         * Creates an instances of {@link Basic}
         *
         * @param context the object containing the relevant data at the point of failure
         * @param process the object denoting what failed from a previous execution
         * @return an instance {@link Basic}
         */
        public static <TFailed, TExecutionContext> Basic<TFailed, TExecutionContext> create(TExecutionContext context, TFailed process) {
            return new Basic<>(process, context);
        }

    }
}
