package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.lambda.ReturnSection;
import io.github.syst3ms.skriptparser.lang.lambda.SkriptFunction;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;

import java.util.Arrays;
import java.util.Optional;

public class SecFilter extends ReturnSection<Boolean> {
    static {
        Parser.getMainRegistration().addSection(
                SecFilter.class,
                "filter %~objects%"
        );
    }

    private Expression<?> filtered;
    private SkriptFunction<SecFilter, Boolean> lambda;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        filtered = expressions[0];
        var logger = parseContext.getLogger();
        if (filtered.acceptsChange(ChangeMode.SET).isEmpty()
                || filtered.acceptsChange(ChangeMode.DELETE).isEmpty()) {
            logger.error(
                    "The expression '" +
                            filtered.toString(TriggerContext.DUMMY, logger.isDebug()) +
                            "' cannot be changed",
                    ErrorType.SEMANTIC_ERROR
            );
            return false;
        }
        lambda = SkriptFunction.create(this);
        return true;
    }

    @Override
    public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        var currentLine = logger.getLine();
        return super.loadSection(section, parserState, logger) && checkReturns(logger, currentLine, true);
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        var filteredValues = Arrays.stream(filtered.getValues(ctx))
                .filter(v -> lambda.apply(ctx, v)
                        .filter(a -> a.length == 1)
                        .map(a -> a[0])
                        .orElse(false)
                )
                .toArray();
        if (filteredValues.length == 0) {
            filtered.change(ctx, new Object[0], ChangeMode.DELETE);
        } else {
            filtered.change(ctx, filteredValues, ChangeMode.SET);
        }
        return getNext();
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "filter " + filtered.toString(ctx, debug);
    }
}
