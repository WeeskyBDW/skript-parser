package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;


import java.util.regex.Pattern;

public class CondExprRegexMatch extends ConditionalExpression {

    private Expression<String> checkedString;
    private Expression<?> regex;
    private boolean useStringAsRegex;
    static {
        Parser.getMainRegistration().addExpression(
                CondExprRegexMatch.class,
                Boolean.class,
                true,
                "%string% [1:dont] match [regex] %regex%",
                "%string% [1:dont] match [regex] %string%"
        );
    }
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        checkedString = (Expression<String>) expressions[0];
        regex = matchedPattern == 0 ? (Expression<Pattern>) expressions[1] : (Expression<String>) expressions[1];
        setNegated(parseContext.getParseMark() == 1);
        useStringAsRegex = matchedPattern == 1 ? Boolean.TRUE : Boolean.FALSE;
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        String string = checkedString.getSingle(ctx).map(str -> (String) str).orElse("");
        return regex.getSingle(ctx)
            .filter(reg -> reg instanceof Pattern)
            .map(reg -> {
                return Boolean.FALSE;
            }).orElse(Boolean.TRUE);
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "";
    }
}
