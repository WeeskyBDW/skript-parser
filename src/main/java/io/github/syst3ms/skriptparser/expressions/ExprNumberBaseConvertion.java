package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.math.BigInteger;
import java.util.Optional;

public class ExprNumberBaseConvertion implements Expression<Number> {

    private Expression<Number> convertedNumber;
    private Expression<Number> base;
    private boolean useAlias;
    static {
        Parser.getMainRegistration().addExpression(
                ExprNumberBaseConvertion.class,
                Number.class,
                true,
                "%number% converted in base %number%",
                "%number% converted in [0:decimal|1:hexadecimal|2:octal]"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        convertedNumber = (Expression<Number>) expressions[0];
        if (matchedPattern == 0) base = (Expression<Number>) expressions[1];
        else useAlias = true;
        return true;
    }

    @Override
    public Number[] getValues(TriggerContext ctx) {
        if (base.getSingle(ctx).)
        convertedNumber.getSingle(ctx).map()

        switch (parseMark) {
            case 1:
                baseNumber = 16;
                break;
            case 2:
                baseNumber = 8;
                break;
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return null;
    }

    private Number toBaseNumber(int parseMark) {
        Number baseNumber = 10; //By default, it will convert to decimal

        return baseNumber;
    }
}
