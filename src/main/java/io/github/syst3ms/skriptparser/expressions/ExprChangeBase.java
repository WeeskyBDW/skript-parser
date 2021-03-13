package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.DoubleOptional;

import java.math.BigInteger;
import java.util.Optional;

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

                "%integer/string% convert in [0:hexadecimal|1:octal|2:binary|3:base64|4:decimal]",

                /*
                //Number to string
                "%number% convert in hexadecimal",
                "%number% convert in octal",
                "%number% convert in binary",
                "%number% convert in base64",

                //String to string
                "%string% convert in hexadecimal",
                "%string% convert in binary",
                "%string% convert in octal",
                "%string% convert in base64",

                 //String to number
                "%string% convert in decimal",
                */


                //Base N to base c
                "%string% convert in base %integer%"
        );
    }
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        expression = expressions[0];
        customBase = (Expression<Number>) expressions[1];
        conversion = parseContext.getParseMark();
        return true;
    }

    private Object convertBase(String str, Integer base, boolean returnNumber) {
        if (returnNumber)
            return Integer.parseInt(str, base);
        else return Integer.toString(Integer.parseInt(str), base);
    }

    private void convertBase(Number nbr, Integer base, boolean returnNumber) {
        convertBase(nbr.toString(), base, returnNumber);
    }

    // TODO: continue implementation and test already implements things
    @Override
    public Object[] getValues(TriggerContext ctx) {
        return DoubleOptional.ofOptional(expression.getSingle(ctx), customBase.getSingle(ctx))
                .flatMap((n, b) -> DoubleOptional.of(
                        n instanceof BigInteger ? n.toString() : (String) n,
                        b instanceof BigInteger ? b.intValue() : 10
                ))
                //TODO finir gestion première syntaxe
                .flatMapToOptional((n,b) -> {
                    Object result = -1;
                    if (customBase.getSingle(ctx).isPresent())
                        result = convertBase(n, b, false);
                    else {
                        switch (conversion) {
                            case 0:
                                result = convertBase(n, 16, false);
                            case 1:
                                result = convertBase(n, 8, true);
                        }
                    }
                    return new Object[] {result};
                });
        //return new Object[]{};
        }

    ;
        /*
        Optional<Object> value = expression.getSingle(ctx)
                .filter(v -> v instanceof String)
                    .map(v -> (Number) Integer.parseInt(v))
                .orElse()
        Optional<Object> base = Optional.of(customBase.getSingle(ctx)
                .map(v -> (Number) v)
                .orElse(10));
        Object returnValue = new Object();
        //Set the good type for theses syntax
        if (value instanceof Number) {
            switch (conversion) {
                case 0:
                    returnValue = Integer.toHexString()
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
                case 4:
                    returnValue = Integer.toHexString()
            }
        }
        return new Object[] {returnValue};
        */

    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return null;
    }
}
