package com.example.doruked;

import java.util.Objects;

/**
 * This class is used to represent a range. It accepts values and uses them to represent a min and max value.
 * 
 * @implNote the class forces that {@code min >= 0}. This class is not suitable for cases whether a lower
 * range is needed. Constraining the min is allows clients to use this class without concern
 * of the range ever being negative
 */
public class Range {
   private final int min; // inclusive
   private final int max; //inclusive

    /** a constructor that accepts two values as the min and max. If {@code min < 0}, 0 is used*/
    public Range(int min, int max) {
        int validMin = validateMin(min);
        this.min = validMin;
        this.max = validateMax(validMin, max);
    }

    /** a constructor that accepts a max value and sets min to 0*/
    public Range(int max) {
        this.min = 0;
        this.max = validateMax(0, max);
    }

    //change minimum

    /**
     * Adds the specified {@code value} to this objects current {@code min}. To create an increases or reductions,
     * it is expected that you appropriately pass a positive or negative value.
     *
     * @param value the value to add to current minimum
     * @return a Range who's {@code min} is the result of adding the specified value
     * @implNote the method forces that {@code min >= 0}
     * @see #Range(int, int)
     */
    // adds specified value to min. min sum = 0
    public Range alterMinimum(int value) {
        int min = this.min + value; //implNote - relies on the constructor logic to auto-correct values
        return new Range(min, this.max);
    }

    /**
     * Replaces the the current {@code min} with the specified {@code value}
     *
     * @param value the value to use when replacing
     * @return a Range object that has the new value as it's min
     * @implNote the method forces that {@code min >= 0}
     */
    //min value accepted = 0
    public Range replaceMinimum(int value) {
        return replaceMinimum(value, true);
    }

    /**
     * Replaces the the current {@code min} with the specified {@code value}
     *
     * @param value the value to use when replacing
     * @param doNotExceedMax whether the specified value is allowed create an increase to the max range
     *                       This is relevant if the specified value is greater than the current max.
     * @return a Range object that has the new value as it's min
     * @implNote the method forces that {@code min >= 0}
     * @see #Range(int, int) 
     */
    //if false and new value exceeds max, max == min
    public Range replaceMinimum(int value, boolean doNotExceedMax) {
        int min = value;
        int max = this.max;

        if(min > max){
            if(doNotExceedMax) min = max;
            else max = min;
        }
        return new Range(min,max);
    }

    //change maximum

    /**
     * Adds the specified value to the this object's {@code max}. To create an increases or reductions,
     * it is expected that you appropriately pass a positive or negative value.
     *
     * This method prevents changes to the {@code max} that result in a decrease of the current {@code min}.
     * This means the {@code max} can, at most, be lowered to the current {@code min}
     *
     * @param value to add the current maximum
     * @return a Range who's {@code min} is the result of adding the specified value
     */
    public Range alterMaximum(int value) {
        int max = this.max + value;
        int min = Math.min(this.min, max);
        return new Range(min, max); //implNote - relies on the constructor logic to auto-correct values
    }

    /**
     * Replaces the the current {@code max} with the specified {@code value}.
     *
     * @param value the value to be used as the new maximum
     * @return a Range that uses {@code value > min ? value : min} as it's new maximum
     * @implNote This method prevents changes  to the {@code max} that result in a decrease of
     * the current {@code min}. As a result, any change is equivalent to {@code value > min ? value : min} as it's new maximum
     */
    public Range replaceMaximum(int value){
        return replaceMaximum(value, true);
    }

    /**
     * Replaces the the current {@code max} with the specified {@code value. This method prevents changes
     * to the {@code max} that result in a decrease of the current {@code min}. In a Range that uses
     * {@code value > min ? value : min} as it's new maximum
     *
     * @param value the value to be used as the new maximum
     * @param doNotLowerMin whether the specified value is allowed create an decrease to the min range
     * @return a Range with a replaced maximum value //reword
     */
    //if false and new value exceeds min, min == max
    public Range replaceMaximum(int value, boolean doNotLowerMin) {
        int min = this.min;
        int max = value;

        if (max < min) {
            if (doNotLowerMin) max = min;
            else min = max;
        }
        return new Range(min, max);
    }

    //change range

    /**
     * Adds the specified {@code value} to this object's {@code min} and {@code max}.
     *
     * @param value the value to add the range
     * @return a Range that is the result of adding the specified value.
     * @implNote The result of this operation cannot lower {@code min || max} to be lower than zero.
     *           In such cases, zero is used instead of a negitive value
     * @see #Range(int, int)
     */
    //affects the min and max values by the specified amount. The minimum value is 0
    //adds the specified value to the range
    public Range alterRange(int value) {
        int min = validateMin(this.min + value);
        int max = validateMin(this.max + value);
        return new Range(min, max);
    }


//utility

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    /**
     * Checks whether the specified value is within the range of this objects
     * {@code min} and {@code max} values. Min and max are inclusive when determining if in range.
     *
     * @param value the value to check if in range
     * @return true if value is in range, false if not.
     */
    public boolean validate(int value) {
        return min <= value &&
                max >= value;
    }

    /**
     * The form of the string is {@code "min - max"}. This may change but for now this seems sufficient.
     *
     * @return a string representing this object's range
     */
    @Override
    public String toString() {
        return min + "-" + max;
    }

    /**
     * Checks whether the specified {@code o} shares the same {@link #min} and {@link #max} with this object.
     *
     * @param o the object to compare to
     * @return true if the objects share min and max
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range)) return false;
        Range range = (Range) o;
        return min == range.min &&
                max == range.max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }


//static

    /**
     * Creates a {@link Range} which has a {@code min} and {@code max} of the specified {@code value}
     *
     * @param value the value to use for min and max
     * @return a Range object that has the specified value as it's min and max
     */
    public static Range singleRange(int value) {
        return new Range(value, value);
    }


//helpers

    private static int validateMin(int value) {
        return Math.max(0, value);
    }

    private static int validateMax(int min, int value){
        return Math.max(min, value);
    }

}