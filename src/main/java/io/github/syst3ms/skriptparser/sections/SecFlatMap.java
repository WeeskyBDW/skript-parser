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

public class SecFlatMap extends ReturnSection<Object> {
    static {
        Parser.getMainRegistration().addSection(
                SecFlatMap.class,
                "flat map %~objects%|map %~objects% flat"
        );
    }

    private Expression<?> flatMapped;
    private SkriptFunction<?, ?> flatMapper;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        flatMapped = expressions[0];
        var logger = parseContext.getLogger();
        if (flatMapped.acceptsChange(ChangeMode.SET).isEmpty()) {
            logger.error(
                    "The expression '" +
                            flatMapped.toString(TriggerContext.DUMMY, logger.isDebug()) +
                            "' cannot be changed",
                    ErrorType.SEMANTIC_ERROR
            );
            return false;
        }
        flatMapper = SkriptFunction.create(this);
        return true;
    }

    @Override
    public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        var currentLine = logger.getLine();
        return super.loadSection(section, parserState, logger) && checkReturns(logger, currentLine, true);
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        Object[] mappedValues = Arrays.stream(flatMapped.getValues(ctx))
                .flatMap(e -> flatMapper.apply(ctx, e)
                        .map(Arrays::stream)
                        .orElse(null)
                )
                .toArray();
        flatMapped.change(ctx, mappedValues, ChangeMode.SET);
        return getNext();
    }

    @Override
    public Class<?> getReturnType() {
        return flatMapped.getReturnType();
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "flat map " + flatMapped.toString(ctx, debug);
    }
}
