package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.math.BigInteger;

/**
 * Change String or number into another base
 * @name ChangeBase
 * @type EXPRESSION
 * @pattern %number% convert[ed] in hexadecimal
 * @pattern %number% convert[ed] in octal
 * @pattern %number% convert[ed] in binary
 * @pattern %number% convert[ed] in base 64
 * @pattern %string% convert[ed] in hexadecimal
 * @pattern %string% convert[ed] in binary
 * @pattern %string% convert[ed] in octal
 * @pattern %string% convert[ed] in base 64
 * @pattern %string% convert[ed] in decimal
 * @pattern %number% convert[ed] in base %number%
 * @since 0.1
 * @author WeeskyBDW
 */
public class ExprChangeBase implements Expression<Object> {

    //Get value of the expression as Object to make only 1 syntax for string to string and number to string base convertion
    private Expression<?> expression;
    private Expression<Number> customBase;
    private int conversion;
    static {
        Parser.getMainRegistration().addExpression(
                ExprChangeBase.class,
                Object.class,
                true,

                //Number to string
                "%number% convert[ed] in hexadecimal",
                "%number% convert[ed] in octal",
                "%number% convert[ed] in binary",
                "%number% convert[ed] in base64",

                //String to string
                "%string% convert[ed] in hexadecimal",
                "%string% convert[ed] in binary",
                "%string% convert[ed] in octal",
                "%string% convert[ed] in base64",

                //String to number
                "%string% convert[ed] in decimal",

                //Base N to base c
                "%number% convert[ed] in base %number%"
        );
    }
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        expression = expressions[0];
        if (matchedPattern == 9) customBase = (Expression<Number>) expressions[1];
        conversion = matchedPattern;
        return true;
    }
    // TODO: continue implementation and test already implements things
    @Override
    public Object[] getValues(TriggerContext ctx) {
        var value = expression.getSingle(ctx).get();
        Number base = customBase.getSingle(ctx).isPresent() ? customBase.getSingle(ctx).get() : 10;
        Object returnValue = new Object();
        //Set the good type for theses syntax
        if (value instanceof Number) {
            switch (conversion) {
                case 0:
                    returnValue = Integer.toString(((Number) value).intValue(), 16);
                case 1:
                    returnValue = Integer.toString(((Number) value).intValue(), 8);
                case 2:
                    returnValue = Integer.toString(((Number) value).intValue(), 2);
                case 3:
                    returnValue = Integer.toString(((Number) value).intValue(), 64);
                case 9:
                    returnValue = Integer.toString(((Number) value).intValue(), base.intValue());
            }
        } else if (value instanceof String) {
            switch (conversion) {
                // TODO: implement
            }
        }

        return new Object[] {returnValue};
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return null;
    }
}
