package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.math.NumberMath;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.ThreadLocalRandom;
import java.lang.Math;
/**
 * Generate a random number (double) or integer.
 *
 * @name random number
 * @pattern [a] random (integer|number) (from|between) %number% (to|and) %number%
 * @since ALPHA
 * @author WeeskyBDW
 */
public class ExprRandomNumber implements Expression<Number> {

    private Expression<Number> lowerNumber, maxNumber;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final SkriptLogger log = new SkriptLogger();
    private boolean isInteger;
    private boolean isStrictly;
    static {
        Main.getMainRegistration().addExpression(
                ExprRandomNumber.class,
                Number.class,
                true,
                "[a] [(1:strictly)] random integer (from|between) %integer% (to|and) %integer%",
                "[a] [(1:strictly)] random number (from|between) %number% (to|and) %number%"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext context) {
        lowerNumber = (Expression<Number>) expressions[0];
        maxNumber = (Expression<Number>) expressions[1];
        isInteger = matchedPattern == 0;
        isStrictly = context.getParseMark() == 1;
        return true;
    }

    @Override
    public Number[] getValues(TriggerContext ctx) {
        Number low = lowerNumber.getSingle(ctx);
        Number max = maxNumber.getSingle(ctx);
        if (low == null || max == null)
            return new Number[0];

        //Send an error if low == max
        /*if(low.doubleValue() == max.doubleValue())
            return log.error("Cannot ");*/

        //Check to find out which number is the greater of the 2, while keeping the type
        Number realLow = low.doubleValue() >= max.doubleValue() ? low : max;
        Number realMax = low.doubleValue() <= max.doubleValue() ? max : low;

        return new Number[]{NumberMath.random(low, max, isStrictly, random)};
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "a " + (isStrictly ? "strictly" : "") + "random " + (isInteger ? "integer" : "number") + " between " + lowerNumber.toString(ctx, debug) + " and " + maxNumber.toString(ctx, debug);
    }
}
