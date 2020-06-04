package com.example.doruked.applier.resumable;

/**
 * This class packages the information that an observer watching the application
 * of {@link TEffect}s will be notified of.
 *
 * @param <TInput>   the input used by effects
 * @param <TEffect>  the effect applied to input or it's members
 * @param <TMessage> the type of message used to impart further info
 */
//Invariant: Keep pattern TInput, TEffect, TMessage
public interface EffectContext<TInput, TEffect, TMessage>{


    TInput getInput();

    TEffect getEffect();

    TMessage getMessage();

    /**
     * Creates a basic immutable instance of {@link EffectContext}
     *
     * @param <TInput>   the input used by effects
     * @param <TEffect>  the effect applied to input or it's members
     * @param <TMessage> the type of message used to impart further info
     * @see #create(Object, Object, Object)
     */
    class Basic<TInput, TEffect, TMessage> implements EffectContext<TInput, TEffect, TMessage> {

        private TEffect effect;
        private TInput input;
        private TMessage message;

        private Basic(TInput input, TEffect effect, TMessage message) {
            this.effect = effect;
            this.input = input;
            this.message = message;
        }

        @Override
        public TInput getInput() {
            return input;
        }

        @Override
        public TEffect getEffect() {
            return effect;
        }

        @Override
        public TMessage getMessage() {
            return message;
        }

        /**
         * A simple factory method that creates a new instance of {@link Basic}.
         *
         * @param input     the the cards to be contained
         * @param effect    the effect to be contained
         * @param message   the status message to be contained
         * @return  a new instance of {@link Basic}
         */
        public static <TInput, TEffect, TMessage> Basic<TInput, TEffect, TMessage> create(TInput input, TEffect effect,  TMessage message) {
            return new Basic<>(input, effect, message);
        }

    }
}
