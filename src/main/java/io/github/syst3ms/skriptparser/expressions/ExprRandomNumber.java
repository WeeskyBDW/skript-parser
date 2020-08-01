package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
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
    private boolean isInteger;
    private boolean isStrictly;
    private final ThreadLocalRandom thread = ThreadLocalRandom.current();

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
        if (low == null || max == null) {
            return new Number[0];
        }
        long lowLong = low.longValue();
        long maxLong = max.intValue();
        if (lowLong > maxLong) {
            long oldMax = maxLong;
            maxLong = lowLong;
            lowLong = oldMax;
        }
        if (isStrictly) {
            lowLong++;
            maxLong--;
        }
        if (isInteger) {
            return new Long[]{thread.nextLong(lowLong, maxLong)};
        }
		return new Double[]{thread.nextDouble(lowLong, maxLong)};
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "a " + (isStrictly ? "strictly" : "") + "random " + (isInteger ? "integer" : "number") + " between " + lowerNumber.toString(ctx, debug) + " and " + maxNumber.toString(ctx, debug);
    }
}
