

package com.example.doruked.applier.resumable;

import com.example.doruked.applier.Applier;
import com.example.doruked.Effect;
import com.example.doruked.exceptions.ExecutionExceptionStamped;
import com.example.doruked.exceptions.InterruptedExceptionStamped;
import com.example.doruked.node.CompatibleNode;
import com.example.doruked.node.iterators.DiveIterator;
import com.example.doruked.NullChecker;
import com.example.doruked.responder.rolledcontext.SleepContext;
import com.example.doruked.responder.SlimMediator;
import com.example.doruked.statuswatcher.Observable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * This class is a resumable applier that allows it's behavior to be observed and modified mid execution.
 * The specifics of observability and modification are maintained in {@link #apply(List, Effect)}
 * <p>
 * The basic strategy for resuming execution is as follows:
 * When some execution fails (throws a checked exception), a {@link FailureCTX} is generated and stored.
 * In addition, the thrown exception contains a message that may be used to retrieve said context
 * To resumed the execution, use the thrown exception with {@link #applyFromEx(ObservableApplier, Exception)}
 *
 * @param <E> the type of elements that will have effects to it
 * @implSpec the internal contents of used "contexts" ({@link EffectCTX}, {@link ProcessCTX} and {@link FailureCTX}
 * are modifiable. However, any contexts created by this applier are not published until they're removed.
 */
//atm this class requires manually making sure apply and ApplyFrom are consistent and free of errors.
//This mostly entails making changes to their associated helpers (helperApply & helperApplyFrom)
public class ObservableApplier<E> implements ContextualApplier<List<E>, Effect<E>, Object, ObservableApplier.FailureCTX<E>> {

    private final SlimMediator.Futured<EffectCTX<E>> notifier;
    private final com.example.doruked.statuswatcher.Observable<SleepContext> sleeper;
    private final Map<Integer, FailureCTX<E>> failed = new HashMap<>();
    private volatile int count = 0;

    public ObservableApplier(SlimMediator.Futured<EffectCTX<E>> notifier, com.example.doruked.statuswatcher.Observable<SleepContext> sleeper) {
        this.notifier = notifier;
        this.sleeper = sleeper;
    }

    /**
     * This method accepts an input and execute it's contained {@code effect} on that input.
     * During execution an observer will be notified and it's response may change the {@code effect}
     * and the {@code input} used by this method. Observers are notified at certain points of execution,
     * so changes made will conform to a particular pattern. The pattern is as followed:
     * (1) the observer is notified before execution
     * (2) the observer is notified at the conclusion of execution (which may lead to to further
     * execution if the observer sends such a response)
     * <p>
     * The important detail to note is that once execution begins, a notifications is not sent until the end.
     * When an effect is being executed on a group of objects, that group will be processed in unison and
     * any relevant responses will be against that unified action.
     * <p>
     * Sleep and Interruption:
     * This method is responsive to interruption and sleep requests by contained {@code observables}.
     * If interrupted during sleep, the interrupted status does not proliferate. The method will attempt to
     * resume previous execution or continue sleeping if the observable has not instructed sleep to discontinue.
     * If it is desired to simultaneously interrupt sleep and interrupt the method, it is recommend
     * to simply not use the observable to request sleep. Or do so judiciously.
     * <p>
     * Note: Checking sleep requests from observables will occur before checking for thread interruptions.
     * The {@code observable} that requests sleep is provided in construction, see:
     * {@link ObservableApplier#ObservableApplier(SlimMediator.Futured, Observable)}
     *
     * @param input  the input to execute an effect on
     * @param effect the effect to be applied to the input
     * @return an ignorable value that's not used
     * @throws ExecutionExceptionStamped if applying the effect to the input results in an exception
     * @throws InterruptedExceptionStamped if the current thread was interrupted during execution
     * @throws NullPointerException if input or effect is null
     * @implSpec the mechanics of sleep and interruption may be subject to change
     */
    public Object apply(List<E> input, Effect<E> effect) throws ExecutionExceptionStamped, InterruptedExceptionStamped {
        CompatibleNode<EffectCTX<E>> node = null; // no concrete impl yet and could probably use a lower level
        helperApply(input, effect, node);
        return null;
    }

    /**
     * This method works like {@link #apply(List, Effect)} with the exception that execution
     * starts from the {@link Phase} retrieved from the specified {@code info}.
     * <p>
     * This method is intended to be used as a response to a failed execution by this applier.
     * In which case, an exception has been thrown containing a message that is used to retrieve
     * the {@code info} needed by this method. When supplied, this method will attempt to resume
     * execution from the point where the previous execution failed.
     * Note: a failure is used to say it threw a checked exception. In other cases info is not generated.
     *
     * @param info the context that specifies the resume point and the data needed for execution
     * @return an ignorable value that's not used
     * @throws ExecutionExceptionStamped if applying the effect to the input results in an exception
     * @throws InterruptedExceptionStamped if the current thread was interrupted during execution
     * @throws NullPointerException if input or effect is null and null is not permitted
     * @implSpec It is possible to supply this method with an improper info. This is not a concern, when the method is
     * used as expected. Here are guidelines to creating {@code info}(Though not suggested):
     * "Correctness" is based on what values were being used at the time execution stops.
     * Figuring this out is simple in practice. You attempt to make the most
     * informed {@link FailureContext} at the time of stopping. Indices are not needed
     * if execution did not stop mid iteration, nor are Futures needed if their values
     * have already been extracted etc. Unneeded values should be unspecified or {@code null}
     * @see #apply(List, Effect)
     */
    public Object applyFrom(FailureCTX<E> info) throws ExecutionExceptionStamped, InterruptedExceptionStamped {
        //parse info
        Phase failed = info.getFailedProcess();
        ProcessCTX<E> execution = info.getReExecute();

        //parse execution
        CompatibleNode<EffectCTX<E>> current = execution.getExecutionTree();
        Future<List<EffectCTX<E>>> future = execution.getFuture();
        int[] indices = execution.getPosition();

        //apply values
        helperApplyFrom(current, failed, future, indices);
        return null;
    }


//helpers

    /**
     * Useful documentation in {@link #helperApplyFrom(CompatibleNode, Phase, Future, int[])}.
     * The only real difference is that this method always starts at phase 1
     *
     * @param input   the input to execute an effect on
     * @param effect  the effect to be applied to the input
     * @param current the node that this execution will replace and build upon
     * @throws ExecutionExceptionStamped if applying the effect to the input results in an exception
     * @throws InterruptedExceptionStamped if the current thread was interrupted during execution
     */
    private void helperApply(final List<E> input, final Effect<E> effect, CompatibleNode<EffectCTX<E>> current) throws InterruptedExceptionStamped, ExecutionExceptionStamped {
        List<E> targetInput = input;
        Effect<E> targetEffect = effect;

        EffectCTX<E> context = createEffectContext(input, effect, Message.START); //make private helepr
        Future<List<EffectCTX<E>>> future;
        List<EffectCTX<E>> response;

        current.setData(context);

        //Notify Intentions - Step 1
        future = helperHandleNotify(current, Phase.FORWARD_NOTIFICATION);  //can resumbmitt from hrer, as nothing has happened

        //Get New Instructions - Step 2
        response = handleGetResponseFromFuture(future, current);
        response.forEach(current::addChild);

        //Process Instructions - Step 3
        List<EffectCTX<E>> delayed = new ArrayList<>();//if response.size() == 0, which should indicate an error and maybe throw an exception instead

        for (int i = 0; i < response.size(); i++) {
            EffectCTX<E> e = response.get(i);
            targetInput = e.getInput();
            targetEffect = e.getEffect();
            current = current.getChildNodes().get(i);

            boolean success;
            for (int j = 0; j < targetInput.size(); j++) {
                E target = targetInput.get(j);

                if (isSleeping()) sleep();
                success = false;
                if (!Thread.currentThread().isInterrupted()) {
                    try {
                        targetEffect.apply(target);
                        success = true;
                    } catch (InterruptedException ex) {
                        //empty; interrupted status handled below in if(!success){}
                    }
                }
                if (!success) {
                    int id = helperGenerateFailureContext(current, Phase.EFFECT_EXECUTION, i, j);
                    throw interruptedException(id);
                }
            }
            context = createEffectContext(targetInput, targetEffect, Message.END);
            delayed.add(context);
            current.addChild(context);
        }
     //Process Responses to Applied Effects

        //Notify past Executions - Step 4
        for (int i = 0; i < delayed.size(); i++) { // can lead to infinite or very long chains. depends on content matter

            EffectCTX<E> e = delayed.get(i);
            current = current.getSiblingNodes().get(i); //data should now be equal to "e" / delayed.get(i);

            future = helperHandleNotify(current, i);

            //get Responses - Step 5
            handleGetResponseFromFuture(future, i, current);
            response.forEach(current::addChild);


            //Apply Each Response as New Intention
            for (EffectCTX<E> j : response) {
                targetInput = j.getInput();
                targetEffect = j.getEffect();

                helperApply(targetInput, targetEffect, current);//throws exceptions rather than catching
                //The plan is to rely on the exception that bubbles up
                //And provide a provide that nows how to resume execution from a given node
            }
        }
    }

    //has latency on resuming to the 6th step. once there. execution relies on an iterator ot
    //there is currently duplication between other methods

    /**
     * This method is required to work like {@link #apply(List, Effect)}, but starts at a different point in execution.
     * The "resume point" is determined by the specified {@code phase}
     * <p>
     * -----------------Phase Explanation-----------
     * This section explains how phases are used.
     * <p>
     * Used Terms:
     * Instruction = effect + input
     * Process = apply effect to input
     * <p>
     * Phase Order:
     * see {@link Phase#order}
     * <p>
     * Forward vs Retro:
     * Defines when a notification or retrieval is occurring.
     * Forward entails it is occurring before {@link Phase#EFFECT_EXECUTION}. Retro means it is occurring after.
     * This is relevant to track because it changes what phases should follow after. It is also important knowledge
     * to observers, who are notified whether execution has occurred or not.
     * <p>
     * Notification:
     * Notification is the act of notifying this appliers observers.
     * This will occur at three points.
     * <p>
     * (1) Before Effect Execution (Forward)- informs observers what effects and input are going to be processed.
     * Observers are notified so they may form a response that provides new instructions.
     * Note: This normally is the first phase. Here, the method always starts with a single {@code instruction} (effect + input)
     * that it wishes to process(apply the effects to input). Once notified, this applier may receive multiple responses
     * from an observer and each are collected as new instructions to be processed. So, from one instruction,
     * you are able to end up with many before reaching effect execution. Any consolidation must be done before hand by observers.
     * <p>
     * (2) After Effect Execution (Retro) - This phase notifies observers of what instruction(s) have been processed.
     * This occurs to allow observers to return a response that may trigger further instructions and processing.
     * Note: Multiple instructions may have been processed in Effect Execution. This applier will not attempt
     * to notify until they have all completed. A separate context is generated for each completed instruction, that explains
     * is has done. This applier notifies observers of each generated context in the order it's instruction was completed.
     * If further execution is requested while notifying, it is handled immediately in a nested function.
     * <p>
     * (3) In Nested Functions - When more execution is requested during Retro Notification, this entails
     * processing that request equivalent to calling {@link #apply(List, Effect)} on the instruction. In short, this
     * works much like a recursive call. Within executing these requests, they will undergo all the normal phases and in order.
     * Note: This does mean this applier is liable to executing infinitely, if an infinite amount of work is requested.
     * Preventing this can be achieved through managing what responses observers send.
     * <p>
     * Retrieval:
     * Retrieval is the act of getting the response from the notified observes.
     * This phase always occurs after notification. Itself doesn't perform differently regardless of Retro or Forward,
     * but what phases follow after do change.
     * <p>
     * Nested Execution / Step "6":
     * This is an unofficial phase that occurs after Retro Retrieval. It does not have it's own explicit
     * phase because it's a nested function. Each function that executes here has it's own full account of {@link Phase}s
     * Once this pseudo phase ends, no further instructions are processed by this applier.
     * Note: Notification(3) contains other details about this phase
     * <p>
     * Effect Execution:
     * The phase where effects are actively being applied. The applier takes each instruction received
     * from Forward Retrieval, and iteratively applies the effect to each element within input.
     * <p>
     * Apply vs ApplyFrom
     * The two methods should produce the same results. Performance is fairly similar,
     * and ApplyFrom is only notably different for the initial 1-5 steps.
     * <p>
     * Sleep & Interruptions:
     * Each phase is responsive to interruption and sleep request from {@link #sleeper}
     * The mechanics of sleep may be changed later
     * <p>
     * Another Way of Control:
     * It's possible to subvert the behavior of this method. At any point of notification, an observer may then choose
     * to interrupt the executing thread. This causes a FailureContext to be generated, and the method exists.
     * Interrupting at any other time work as well, however, there's no guarantee of which phase will receive it.
     * <p>
     * Essential Attributes
     * Notification: Notified before and after processing. Not notified during processing
     * Retrieval: always after notification. Simply retrieves result.
     * Observers: May produce multiple responses to a notification. This applier does not
     * reduce them. Observers must manage which they send.
     * Sleep: On request by SleepContext. Checked every phase
     * Interruption: checked every phase
     *
     * @param current  the node to execute from
     * @param error    the phase to resume from
     * @param optional the future to resume from if the stopped execution generated one
     * @param indeces  the indices used to resume iteration or nested iteration
     * @throws ExecutionExceptionStamped   if applying the effect to the input results in an exception
     * @throws InterruptedExceptionStamped if the current thread was interrupted during execution
     * @implNote Not every parameter is needed, depending on which {@code error} is specified..
     * This method ignores specified parameters if they're are not expected.
     * Parameters that can be ignored are {@code future} and {@code indeces}
     * @see {@link #apply(List, Effect)}
     */
    private void helperApplyFrom(@NotNull CompatibleNode<EffectCTX<E>> current, @NotNull final Phase error, Future<List<EffectCTX<E>>> optional, int[] indeces) throws InterruptedExceptionStamped, ExecutionExceptionStamped {
        Phase phase = error;
        EffectCTX<E> context;

        List<E> targetInput;
        Effect<E> targetEffect;

        Future<List<EffectCTX<E>>> future = optional;
        List<EffectCTX<E>> response = null;

        int fromIndex = 0;
        int subIndex = 0;


        //Step 1
        if(phase == Phase.FORWARD_NOTIFICATION){
            future = helperHandleNotify(current, Phase.FORWARD_NOTIFICATION);  //can resumbmitt from hrer, as nothing has happened
            phase = nextPhase(phase);
        }

        //Step 2
        if(phase == Phase.FORWARD_RETRIEVAL){
            response = handleGetResponseFromFuture(future, current);
            response.forEach(current::addChild);

            phase = nextPhase(phase);
        }

        List<EffectCTX<E>> delayed = new ArrayList<>();//if response.size() == 0, which should indicate an error and maybe throw an exception instead

        //Step 3
        if(phase == Phase.EFFECT_EXECUTION){
            //setup
            if(response == null) response = current.getSiblingData();
            if(error == Phase.EFFECT_EXECUTION) { //means it started at this phase
                fromIndex =  indeces[0];
                subIndex =  indeces[1];
            }

            //Process responses
            for (int i = fromIndex; i < response.size(); i++) {

                EffectCTX<E> e = response.get(i);
                targetInput = e.getInput();
                targetEffect = e.getEffect();


                boolean success;
                for (int j = subIndex; j < targetInput.size(); j++) {
                    E target = targetInput.get(j);
                    success = false;

                    if (isSleeping()) sleep();
                    if(!Thread.currentThread().isInterrupted()){
                        try {
                            targetEffect.apply(target);
                            success = true;
                        } catch (InterruptedException ex) {
                            //interrupted status handled below in if(!success){}
                        }
                    }
                    if(!success){
                        int id = helperGenerateFailureContext(current, Phase.EFFECT_EXECUTION, i, j);
                        throw interruptedException(id);
                    }
                }
                context = createEffectContext(targetInput, targetEffect, Message.END);
                delayed.add(context);

                current = current.getChildNodes().get(i);
                current.addChild(context);
                //if interupted
            }

            phase = nextPhase(phase);
        }

        //Step 4 - 5

        //setup
        if (error == Phase.RETRO_NOTIFICATION || error == Phase.FORWARD_RETRIEVAL) fromIndex = indeces[0];//means it started at given phase
        boolean oneTimeSkip = (phase == Phase.RETRO_RETRIEVAL); //used to distinguish starting iteration from Step 4 or 5

        //execution
        for (int i = fromIndex; i < delayed.size(); i++) {

            if(!oneTimeSkip) future = helperHandleNotify(current, i);
            else {
                oneTimeSkip = false;
                phase = nextPhase(phase);
            }

            response = handleGetResponseFromFuture(future, i, current);
            response.forEach(current::addChild);

            //Apply Each Response as New Intention
            for (EffectCTX<E> j : response) {
                targetInput = j.getInput();
                targetEffect = j.getEffect();

                helperApply(targetInput, targetEffect, current);
            }
        }

        //Step 6  - execute rest of tree

        Iterator<CompatibleNode<EffectCTX<E>>> it = new DiveIterator<>(current);
        it.next(); //skipping first because it was what we processed in steps 1-5
        while (it.hasNext()) {
            helperApplyFrom(it.next());
        }
    }

    /**
     * Unboxes values needed from the specified {@code node} then defers to {@link #helperApply(List, Effect, CompatibleNode)}
     *
     * @see #helperApply(List, Effect, CompatibleNode)
     */
    private void helperApplyFrom(CompatibleNode<EffectCTX<E>> node) throws ExecutionExceptionStamped, InterruptedExceptionStamped {
        EffectCTX<E> context = node.getData();
        helperApply(getInput(context), getEffect(context), node);
    }

    //Handle Failure Points

    /**
     * Retrieves the response for the specified {@code future}, waiting if necessary,
     * and handles any thrown exceptions.
     *
     * @param future the future to get the response from
     * @param index the index of {@code current}
     * @param current the node to generate a failure context of
     * @return the response from the future
     * @throws InterruptedExceptionStamped  if the current thread was interrupted
     * @throws ExecutionExceptionStamped if the future threw an exception
     * @throws NullPointerException if future or current is null
     */
    private  List<EffectCTX<E>> handleGetResponseFromFuture(Future<List<EffectCTX<E>>> future, int index, CompatibleNode<EffectCTX<E>> current) throws InterruptedExceptionStamped, ExecutionExceptionStamped {
        sleepIfRequired();
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ex) {
            int id = helperGenerateFailureContext(current, Phase.EFFECT_EXECUTION, future, index); //incorrect, want to create with future

            if(ex instanceof  InterruptedException) throw stamped((InterruptedException) ex, id);
            else throw stamped((ExecutionException) ex, id);
        }
    }

    /**
     * Retrieves the response for the specified {@code future}, waiting if necessary,
     * and handles any thrown exceptions.
     *
     * @param future the future to get the response from
     * @param current the node to generate a failure context of
     * @return the response from the future
     * @throws InterruptedExceptionStamped  if the current thread was interrupted
     * @throws ExecutionExceptionStamped if the future threw an exception
     * @throws NullPointerException if future or current is null
     */
    private List<EffectCTX<E>> handleGetResponseFromFuture(Future<List<EffectCTX<E>>> future, CompatibleNode<EffectCTX<E>> current) throws InterruptedExceptionStamped, ExecutionExceptionStamped {
       sleepIfRequired();
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ex) {
            ProcessCTX<E> process = new ProcessCTX<>(ExecutionContext.Basic.createWithFuture(current, future));
            int id = helperGenerateFailureContext(process, Phase.FORWARD_RETRIEVAL);
            if(ex instanceof  InterruptedException) throw stamped((InterruptedException) ex, id);
            else throw stamped((ExecutionException) ex, id);
        }
    }

    /**
     * Works like {@link #helperHandleNotify(EffectCTX, Supplier, Phase)}
     *
     * @param index   the index of {@code current}
     * @param current node containing the context to notify observers of
     * @see #helperHandleNotify(EffectCTX, Supplier, Phase)
     */
    private Future<List<EffectCTX<E>>> helperHandleNotify(CompatibleNode<EffectCTX<E>> current, int index) throws InterruptedExceptionStamped {
        int[] ar = {index};
        Supplier<ProcessCTX<E>> process = () -> new ProcessCTX<>(ExecutionContext.Basic.createAtIndex(current, ar));

        return helperHandleNotify(current.getData(), process, Phase.RETRO_NOTIFICATION);
    }

    /**
     * Works like {@link #helperHandleNotify(EffectCTX, Supplier, Phase)}
     *
     * @param current node containing the context to notify observers of
     * @see #helperHandleNotify(EffectCTX, Supplier, Phase)
     */
    private Future<List<EffectCTX<E>>> helperHandleNotify(CompatibleNode<EffectCTX<E>> current, Phase failed) throws InterruptedExceptionStamped {
        Supplier<ProcessCTX<E>> process = ()->  new ProcessCTX<>(ExecutionContext.Basic.createForNotification(current));
        return helperHandleNotify(current.getData(), process, failed);
    }

    /**
     * Notifies the observer and handles any thrown exception
     *
     * @param context the context to notify observers of
     * @param process the context of the current execution (only used on failure)
     * @param failed  the current phase (only used on failure)
     * @return the result of notifying this objects observer of the specified {@code context}
     * @throws InterruptedExceptionStamped if the executing thread was interrupted
     */
    private Future<List<EffectCTX<E>>> helperHandleNotify(EffectCTX<E> context, Supplier<ProcessCTX<E>> process, Phase failed) throws InterruptedExceptionStamped {
        sleepIfRequired();
        try {
            return notifier.notify(context);
        } catch (InterruptedException ex) {
           int id = helperGenerateFailureContext(process.get(), failed);
            throw stamped(ex, id);
        }
    }

    //Help Generate Failure

    private int helperGenerateFailureContext(CompatibleNode<EffectCTX<E>> current, Phase phase, int... index) {
        ProcessCTX<E> process = new ProcessCTX<>(ExecutionContext.Basic.createAtIndex(current, index));

        return helperGenerateFailureContext(process, phase);
    }

    /**
     * Creates and stores a FailureContext
     *
     * @param current the node to generate a failure context of
     * @param phase  the current phase
     * @param future the future to get the response from
     * @param index the index of {@code current} within it's sibling nodes
     * @return the key belonging to the generated failure context
     */
    private int helperGenerateFailureContext(CompatibleNode<EffectCTX<E>> current, Phase phase, Future<List<EffectCTX<E>>> future, int index){
        int[] ar = {index}; //created because int[] is expected by ProcessCTX target typing
        ProcessCTX<E> process = new ProcessCTX<>(ExecutionContext.Basic.createWithFuture(current, future, ar));

        return helperGenerateFailureContext(process, phase);
    }

    /**
     * Creates and stores a FailureContext
     *
     * @param process the process to contain in a failure context
     * @param phase   the current phase
     * @return the key belonging to the generated failure context
     */
    private int helperGenerateFailureContext(ProcessCTX<E> process, Phase phase){
        FailureCTX<E> fail =  FailureCTX.create(process, phase);
        failed.put(count, fail);
        return count++;
    }

    //Create Exception

    /** creates stamped exception */
    private InterruptedExceptionStamped interruptedException(int id) {
        InterruptedException ignored = null; //stamped(InterruptedException) doesn't use it's param
        return stamped(ignored, id);
    }

    /** creates stamped exception */
    private InterruptedExceptionStamped stamped(InterruptedException ignored, int id) { //param is accepted to add clarity
        return new InterruptedExceptionStamped(String.valueOf(id));          //where the method is invoked
    }

    /** creates stamped exception */
    private ExecutionExceptionStamped stamped(ExecutionException e, int id){
        return new ExecutionExceptionStamped(String.valueOf(id), e);
    }

    //Manage Sleep

    /** checks whether this observer is currently requested to sleep */
    private boolean isSleeping(){
        return sleeper.getStatus().getShouldSleep();
    }

    /**
     * Attempts to sleep if there is a request to from {@link #sleeper}
     *
     * @implSpec to change the {@code interval} or end sleep, the {@link #sleeper}
     * much update it's {@code context} and interrupt the applier's {@code thread}.
     * If not interrupted, the updated {@code context} wil not be checked until
     * the current {@code interval} resets;
     * @implNote continues to sleep for  intervals of the {@code interval} until
     * the {@link #sleeper} updates it's context to reflect that sleeping is over.
     * Whether it was updated is checked in {@link #isSleeping()}
     * <p>
     * The interval may be updated by the {@link #sleeper}, however, this object must
     * be interrupted from sleep inorder to check for interval changes. Once interrupted,
     * this method examines the interval of the {@link #sleeper}, and re-sleeps for the new interval duration.
     * If the interval is not different, sleep attempts to continue as if it was not interrupted.
     */
    private void sleep() {
        long interval = getSleepInterval();
        long duration = interval;
        long start = System.currentTimeMillis();

        while(isSleeping()){
            try {
                Thread.sleep(duration);
                if(!isSleep())break;
                duration = interval;
                start = System.currentTimeMillis();

            } catch (InterruptedException e) {
                long current =   getSleepInterval();
                if(interval != current) duration = interval = current;
                else duration =  duration - (System.currentTimeMillis() - start);
            }
        }
    }
    /** @see #sleep()  */
    private void sleepIfRequired(){
       if(isSleeping()) sleep();
    }

    /**
     * checks whether this applier is currently "requested" to sleep.
     *
     * @implSpec This applier should attempt to sleep whenever requested.
     * The method that coordinates this behavior {@link #sleep()}
     */
    private boolean isSleep(){
        return sleeper.getStatus().getShouldSleep();
    }

    private long getSleepInterval() {
        return sleeper.getStatus().getSleepInterval();
    }

    //General Helpers

    private static <E> Effect<E> getEffect(EffectContext<List<E>, Effect<E>, Message> context){
        return context.getEffect();
    }

    private static <E> List<E> getInput(EffectContext<List<E>, Effect<E>, ObservableApplier.Message> context){
        return context.getInput();
    }

    /**
     * parses the id from the message of a "stamped" exception. The id is the {@code key}
     * is used to retrieve the {@link FailureContext} from {@link #failed}
     *
     * @see #stamped(ExecutionException, int)
     * @see #stamped(InterruptedException, int)
     * @see #failed
     */
    private static int helperGetID(Exception ex) {
        String id = ex.getMessage().substring(0, 1);
        return Integer.valueOf(id);
    }

    private EffectCTX<E> createEffectContext(List<E> input, Effect<E> effect, Message message){
        return new EffectCTX<>(EffectContext.Basic.create(input, effect, message));
    }

    private Phase nextPhase(Phase phase) {
        return phase.next();
    }

// Utilities

    /**
     * The specified {@code exception} contains a message that is used to generate a {@link FailureCTX}.
     * The generated context is then used to call {@link #applyFrom(FailureCTX)} with the specified {@code applier}
     *
     * @param <E> the type that will have effects applied to them
     * @param applier the applier to apply with
     * @param ex the exception containing a message that the specified applier can parse and resume from
     * @return the context that was applied
     * @throws InterruptedExceptionStamped  if the current thread was interrupted
     * @throws ExecutionExceptionStamped if the future threw an exception
     */
    public static <E> FailureCTX<E> applyFrom(ObservableApplier<E> applier, InterruptedExceptionStamped ex) throws ExecutionExceptionStamped, InterruptedExceptionStamped {
        return helperApplyFrom(applier, ex);
    }

    /**
     * The specified {@code exception} contains a message that is used to generate a {@link FailureCTX}.
     * The generated context is then used to call {@link #applyFrom(FailureCTX)} with the specified {@code applier}
     *
     * @param <E> the type that will have effects applied to them
     * @param applier the applier to apply with
     * @param ex the exception containing a message that the specified applier can parse and resume from
     * @return the context that was applied
     * @throws InterruptedExceptionStamped  if the current thread was interrupted
     * @throws ExecutionExceptionStamped if the future threw an exception
     */
    public static <E> FailureCTX<E> applyFrom(ObservableApplier<E> applier, ExecutionExceptionStamped ex) throws InterruptedExceptionStamped, ExecutionExceptionStamped {
        return helperApplyFrom(applier, ex);
    }

    /**
     * The specified {@code exception} contains a message that is used to generate a {@link FailureCTX}.
     * The generated context is then used to call {@link #applyFrom(FailureCTX)} with the specified {@code applier}
     *
     * @param <E> the type that will have effects applied to them
     * @param applier the applier to apply with
     * @param ex the exception containing a message that the specified applier can parse and resume from
     * @throws InterruptedExceptionStamped  if the current thread was interrupted
     * @return the context that was applied
     * @throws ExecutionExceptionStamped if the future threw an exception
     * @throws IllegalArgumentException if the specified exception is not declared throwable by this method
     */
    public static <E> FailureCTX<E> applyFromEx(ObservableApplier<E> applier, Exception ex) throws ExecutionExceptionStamped, InterruptedExceptionStamped {
        if (ex instanceof InterruptedExceptionStamped || ex instanceof ExecutionExceptionStamped) {
            return helperApplyFrom(applier, ex);
        }
        else throw new IllegalArgumentException("Exception( " + ex + ") is not declared throwable by this method");
    }

    /**
     * The specified {@code exception} contains a message that is used to generate a {@link FailureCTX}.
     * The generated context is then used to call {@link #applyFrom(FailureCTX)} with the specified {@code applier}
     *
     * @param applier the applier to apply with
     * @param ex the exception containing a message that the specified applier can parse and resume from
     * @param <E> the type that will have effects applied to them
     * @return the context that was applied
     * @throws InterruptedExceptionStamped  if the current thread was interrupted
     * @throws ExecutionExceptionStamped if the future threw an exception
     */
    private static <E> FailureCTX<E> helperApplyFrom(ObservableApplier<E> applier, Exception ex) throws InterruptedExceptionStamped, ExecutionExceptionStamped {
        int id = helperGetID(ex);
        FailureCTX<E> context = applier.failed.remove(id);
        applier.applyFrom(context);
        return context;
    }

//Inner classes

    /**
     * Used to indicates whether effects are "starting" to be applied
     * or have already finished
     */
    public enum Message{START, END}

    /**
     * Demarks the points at which execution can stop for an {@link ObservableApplier}
     * Each point represents a sub-process within an execution
     */
    public enum Phase {
        FORWARD_NOTIFICATION(1), FORWARD_RETRIEVAL(2), EFFECT_EXECUTION(3),
        RETRO_NOTIFICATION(4), RETRO_RETRIEVAL(5);

        private final int order;

        Phase(int order) {
            this.order = order;
        }

        public int getOrder() {
            return order;
        }

        public Phase next() {
          switch (this){
              case FORWARD_NOTIFICATION: return Phase.FORWARD_RETRIEVAL;
              case FORWARD_RETRIEVAL: return Phase.EFFECT_EXECUTION;
              case EFFECT_EXECUTION: return Phase.RETRO_NOTIFICATION;
              case RETRO_NOTIFICATION: return Phase.RETRO_RETRIEVAL;
              case RETRO_RETRIEVAL: //no next phase
              default: return null;
          }
        }
    }

    //Used Contexts

    /**
     * A wrapper class used to reduce needed generics declarations
     * @param <V> the type of elements that an {@link Applier} will apply effects to
     * @see EffectContext
     */
    public static class EffectCTX<V> implements EffectContext<List<V>, Effect<V>, Message> {

        private final EffectContext<List<V>, Effect<V>, Message> delegate;


        public EffectCTX(EffectContext<List<V>, Effect<V>, Message> delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<V> getInput() {
            return delegate.getInput();
        }

        @Override
        public Effect<V> getEffect() {
            return delegate.getEffect();
        }

        @Override
        public Message getMessage() {
            return delegate.getMessage();
        }

    }

    /**
     * A wrapper class  to reduce needed generics declarations
     * @param <V> the type of elements that an {@link Applier} will apply effects to
     * @see ExecutionContext
     */
    public static class ProcessCTX<V> implements ExecutionContext<CompatibleNode<EffectCTX<V>>, int[], Future<List<EffectCTX<V>>>> {

        private final ExecutionContext<CompatibleNode<EffectCTX<V>>, int[], Future<List<EffectCTX<V>>>> delegate;

        public ProcessCTX(ExecutionContext<CompatibleNode<EffectCTX<V>>, int[], Future<List<EffectCTX<V>>>> delegate) {
            this.delegate = delegate;
        }

        @Override
        public CompatibleNode<EffectCTX<V>> getExecutionTree() {
            return delegate.getExecutionTree();
        }

        @Override
        public int[] getPosition() {
            return delegate.getPosition();
        }

        @Override
        public Future<List<EffectCTX<V>>> getFuture() {
            return delegate.getFuture();
        }

    }

    /**
     * A wrapper class to reduce needed generics declarations
     * @param <V> the type of elements that an {@link Applier} will apply effects to
     * @see FailureContext
     */
    public static class FailureCTX<V> implements FailureContext<Phase, ProcessCTX<V>> {


        private final FailureContext<Phase, ProcessCTX<V>> delegate;

        private FailureCTX(FailureContext<Phase, ProcessCTX<V>> delegate) {
            this.delegate = delegate;
        }

        public Phase getFailedProcess() {
            return delegate.getFailedProcess();
        }

        public ProcessCTX<V> getReExecute() {
            return delegate.getReExecute();
        }

        /**
         * A factory method that does not accept {@code null} values
         *
         * @param context the object containing the relevant data at the point of failure
         * @param failed the object denoting what failed from a previous execution
         * @return a new {@link FailureCTX} with state that is not null
         * @throws NullPointerException is context or phase is null
         * @implSpec it's still possible for data within this objects state to be {@code null}
         * This is revelent for {@code context} which contains further values
         */
        public static <E> FailureCTX<E> create(ProcessCTX<E> context, Phase failed) {
            NullChecker.notNull(context, failed);

            FailureContext<Phase, ProcessCTX<E>> delegate = Basic.create(context, failed);
            return new FailureCTX<>(delegate);
        }

    }
}
