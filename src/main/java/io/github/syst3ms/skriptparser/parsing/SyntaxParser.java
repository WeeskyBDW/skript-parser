package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.*;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.log.ErrorContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SkriptEventInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.RecentElementList;
import io.github.syst3ms.skriptparser.util.StringUtils;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains the logic for parsing and interpreting single statements, sections and expressions inside of a script.
 */
@SuppressWarnings("unchecked")
public class SyntaxParser {
    /**
     * Tells {@link #parseBooleanExpression(String, int, ParserState, SkriptLogger)} to only return expressions that are not conditional
     * @see #parseBooleanExpression(String, int, ParserState, SkriptLogger)
     */
    public static final int NOT_CONDITIONAL = 0;
    /**
     * Tells {@link #parseBooleanExpression(String, int, ParserState, SkriptLogger)} to return any expressions, conditional or not
     * @see #parseBooleanExpression(String, int, ParserState, SkriptLogger)
     */
    public static final int MAYBE_CONDITIONAL = 1;
    /**
     * Tells {@link #parseBooleanExpression(String, int, ParserState, SkriptLogger)} to only return conditional expressions
     * @see #parseBooleanExpression(String, int, ParserState, SkriptLogger)
     */
    public static final int CONDITIONAL = 2;
    public static final Pattern LIST_SPLIT_PATTERN = Pattern.compile("\\s*(,)\\s*|\\s+(and|or)\\s+", Pattern.CASE_INSENSITIVE);
    /**
     * The pattern type representing {@link Boolean}
     */
    @SuppressWarnings("ConstantConditions")
    public static final PatternType<Boolean> BOOLEAN_PATTERN_TYPE = new PatternType<>((Type<Boolean>) TypeManager.getByClass(Boolean.class), true);
    /**
     * The pattern type representing {@link Object}
     */
    @SuppressWarnings({"ConstantConditions", "RedundantCast"}) // Gradle requires the cast, but IntelliJ considers it redundant
    public static final PatternType<Object> OBJECT_PATTERN_TYPE = new PatternType<>((Type<Object>) TypeManager.getByClass(Object.class), true);

    @SuppressWarnings({"ConstantConditions", "RedundantCast"}) // Gradle requires the cast, but IntelliJ considers it redundant
    public static final PatternType<Object> OBJECTS_PATTERN_TYPE = new PatternType<>((Type<Object>) TypeManager.getByClass(Object.class), false);

    /**
     * All {@link Effect effects} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<SyntaxInfo<? extends Effect>> recentEffects = new RecentElementList<>();
    /**
     * All {@link CodeSection sections} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<SyntaxInfo<? extends CodeSection>> recentSections = new RecentElementList<>();
    /**
     * All {@link SkriptEvent events} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<SkriptEventInfo<?>> recentEvents = new RecentElementList<>();
    /**
     * All {@link Expression expressions} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<ExpressionInfo<?, ?>> recentExpressions = new RecentElementList<>();
    /**
     * All {@link ConditionalExpression conditions} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<ExpressionInfo<? extends ConditionalExpression, ? extends Boolean>> recentConditions = new RecentElementList<>();

    /**
     * Parses an {@link Expression} from the given {@linkplain String} and {@link PatternType expected return type}
     * @param <T> the type of the expression
     * @param s the string to be parsed as an expression
     * @param expectedType the expected return type
     * @param logger
     * @return an expression that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message.
     */
    public static <T> Expression<? extends T> parseExpression(String s, PatternType<T> expectedType, ParserState parserState, SkriptLogger logger) {
        if (s.isEmpty())
            return null;
        if (s.startsWith("(") && s.endsWith(")") && StringUtils.findClosingIndex(s, '(', ')', 0) == s.length() - 1) {
            s = s.substring(1, s.length() - 1);
        }
        Expression<? extends T> literal = parseLiteral(s, expectedType, parserState, logger);
        if (literal != null) {
            return literal;
        }
        Variable<? extends T> variable = (Variable<? extends T>) Variables.parseVariable(s, expectedType.getType().getTypeClass(), parserState, logger);
        if (variable != null) {
            if (!variable.isSingle() && expectedType.isSingle()) {
                logger.error("A single value was expected, but " + s + " represents multiple values.", ErrorType.SEMANTIC_ERROR);
                return null;
            }
            return variable;
        }
        if (!expectedType.isSingle()) {
            Expression<? extends T> listLiteral = parseListLiteral(s, expectedType, parserState, logger);
            if (listLiteral != null) {
                return listLiteral;
            }
        }
        for (ExpressionInfo<?, ?> info : recentExpressions) {
            Expression<? extends T> expr = matchExpressionInfo(s, info, expectedType, parserState, logger);
            if (expr != null) {
                recentExpressions.acknowledge(info);
                logger.clearLogs();
                return expr;
            }
            logger.forgetError();
        }
        // Let's not loop over the same elements again
        List<ExpressionInfo<?, ?>> remainingExpressions = SyntaxManager.getAllExpressions();
        recentExpressions.removeFrom(remainingExpressions);
        for (ExpressionInfo<?, ?> info : remainingExpressions) {
            Expression<? extends T> expr = matchExpressionInfo(s, info, expectedType, parserState, logger);
            if (expr != null) {
                recentExpressions.acknowledge(info);
                logger.clearLogs();
                return expr;
            }
            logger.forgetError();
        }
        logger.setContext(ErrorContext.NO_MATCH);
        logger.error("No expression matching ''" + s + "' was found", ErrorType.NO_MATCH);
        return null;
    }

    /**
     * Parses a {@link Expression boolean expression} from the given {@linkplain String}
     * @param s the string to be parsed as an expression
     * @param conditional a constant describing whether the result can be a {@link ConditionalExpression condition}
     * @param parserState
     * @param logger
     * @see SyntaxParser#NOT_CONDITIONAL
     * @see SyntaxParser#MAYBE_CONDITIONAL
     * @see SyntaxParser#CONDITIONAL
     * @return a boolean expression that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message.
     */
    public static Expression<Boolean> parseBooleanExpression(String s, @MagicConstant(intValues = {NOT_CONDITIONAL, MAYBE_CONDITIONAL, CONDITIONAL}) int conditional, ParserState parserState, SkriptLogger logger) {
        // I swear this is the cleanest way to do it
        if (s.startsWith("(") && s.endsWith(")") && StringUtils.findClosingIndex(s, '(', ')', 0) == s.length() - 1) {
            s = s.substring(1, s.length() - 1);
        }
        if (s.equalsIgnoreCase("true")) {
            return new SimpleLiteral<>(Boolean.class, true);
        } else if (s.equalsIgnoreCase("false")) {
            return new SimpleLiteral<>(Boolean.class, false);
        }
        Variable<Boolean> variable = (Variable<Boolean>) Variables.parseVariable(s, Boolean.class, parserState, logger);
        if (variable != null) {
            if (!variable.isSingle()) {
                logger.error("A single value was expected, but " + s + " represents multiple values.", ErrorType.SEMANTIC_ERROR);
                return null;
            }
            return variable;
        }
        for (ExpressionInfo<?, ?> info : recentExpressions) {
            if (info.getReturnType().getType().getTypeClass() != Boolean.class)
                continue;
            Expression<Boolean> expr = (Expression<Boolean>) matchExpressionInfo(s, info, BOOLEAN_PATTERN_TYPE, parserState, logger);
            if (expr != null) {
                switch (conditional) {
                    case 0: // Can't be conditional
                        if (ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            logger.error("The boolean expression must not be conditional", ErrorType.SEMANTIC_ERROR);
                            return null;
                        }
                        break;
                    case 2: // Has to be conditional
                        if (!ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            logger.error("The boolean expression must be conditional", ErrorType.SEMANTIC_ERROR);
                            return null;
                        }
                    case 1: // Can be conditional
                        if (ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            recentConditions.acknowledge((ExpressionInfo<? extends ConditionalExpression, ? extends Boolean>) info);
                        }
                    default: // You just want me dead, don't you ?
                        break;
                }
                recentExpressions.acknowledge(info);
                logger.clearLogs();
                return expr;
            }
            logger.forgetError();
        }
        // Let's not loop over the same elements again
        List<ExpressionInfo<?, ?>> remainingExpressions = SyntaxManager.getAllExpressions();
        recentExpressions.removeFrom(remainingExpressions);
        for (ExpressionInfo<?, ?> info : remainingExpressions) {
            if (info.getReturnType().getType().getTypeClass() != Boolean.class)
                continue;
            Expression<Boolean> expr = (Expression<Boolean>) matchExpressionInfo(s, info, BOOLEAN_PATTERN_TYPE, parserState, logger);
            if (expr != null) {
                switch (conditional) {
                    case 0: // Can't be conditional
                        if (ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            logger.error("The boolean expression must not be conditional", ErrorType.SEMANTIC_ERROR);
                            return null;
                        }
                        break;
                    case 2: // Has to be conditional
                        if (!ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            logger.error("The boolean expression must be conditional", ErrorType.SEMANTIC_ERROR);
                            return null;
                        }
                    case 1: // Can be conditional
                        if (ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            recentConditions.acknowledge((ExpressionInfo<? extends ConditionalExpression, ? extends Boolean>) info);
                        }
                    default: // You just want me dead, don't you ?
                        break;
                }
                recentExpressions.acknowledge(info);
                logger.clearLogs();
                return expr;
            }
            logger.forgetError();
        }
        logger.setContext(ErrorContext.NO_MATCH);
        logger.error("No expression matching '" + s + "' was found", ErrorType.NO_MATCH);
        return null;
    }

    private static <T> Expression<? extends T> matchExpressionInfo(String s, ExpressionInfo<?, ?> info, PatternType<T> expectedType, ParserState parserState, SkriptLogger logger) {
        List<PatternElement> patterns = info.getPatterns();
        PatternType<?> infoType = info.getReturnType();
        Class<?> infoTypeClass = infoType.getType().getTypeClass();
        Class<T> expectedTypeClass = expectedType.getType().getTypeClass();
        if (!expectedTypeClass.isAssignableFrom(infoTypeClass) && !Converters.converterExists(infoTypeClass, expectedTypeClass))
            return null;
        for (int i = 0; i < patterns.size(); i++) {
            PatternElement element = patterns.get(i);
            logger.setContext(ErrorContext.MATCHING);
            MatchContext parser = new MatchContext(element, parserState, logger);
            if (element.match(s, 0, parser) != -1) {
                try {
                    Expression<? extends T> expression = (Expression<? extends T>) info.getSyntaxClass().newInstance();
                    logger.setContext(ErrorContext.INITIALIZATION);
                    if (!expression.init(
                            parser.getParsedExpressions().toArray(new Expression[0]),
                            i,
                            parser.toParseResult()
                    )) {
                        continue;
                    }
                    logger.setContext(ErrorContext.CONSTRAINT_CHECKING);
                    Class<?> expressionReturnType = expression.getReturnType();
                    if (!expectedTypeClass.isAssignableFrom(expressionReturnType)) { // Would only screw up in case of bad dynamic type usage
                        Expression<?> converted = expression.convertExpression(expectedTypeClass);
                        if (converted != null) {
                            return (Expression<? extends T>) converted;
                        } else {
                            Type<?> type = TypeManager.getByClass(expressionReturnType);
                            assert type != null;
                            logger.error(StringUtils.withIndefiniteArticle(expectedType.toString(), false) +
                                    " was expected, but " +
                                    StringUtils.withIndefiniteArticle(type.toString(), false) +
                                    " was found", ErrorType.SEMANTIC_ERROR);
                            return null;
                        }
                    }
                    if (!expression.isSingle() &&
                            expectedType.isSingle()) {
                        logger.error("A single value was expected, but '" + s + "' represents multiple values.", ErrorType.SEMANTIC_ERROR);
                        continue;
                    }
                    if (parserState.isRestrictingExpressions() && parserState.forbidsSyntax(expression.getClass())) {
                        logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                        logger.error("The enclosing section does not allow the use of this expression : " + expression.toString(null, logger.isDebug()), ErrorType.SEMANTIC_ERROR);
                        continue;
                    }
                    return expression;
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Couldn't instantiate class '" + info.getSyntaxClass().getName() + "'", ErrorType.EXCEPTION);
                }
            }
        }
        return null;
    }

    /**
     * Parses a line of code as an {@link InlineCondition}
     * @param s the line to be parsed
     * @param parserState
     * @param logger
     * @return an inline condition that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message
     */
    @Nullable
    public static InlineCondition parseInlineCondition(String s, ParserState parserState, SkriptLogger logger) {
        if (s.isEmpty())
            return null;
        Expression<Boolean> cond = parseBooleanExpression(s, CONDITIONAL, parserState, logger);
        return cond != null ? new InlineCondition(cond) : null;
    }

    /**
     * Parses a list literal expression (of the form {@code ..., ... and ...}) from the given {@linkplain String}  and {@link PatternType expected return type}
     * @param <T> the type of the list literal
     * @param s the string to be parsed as a list literal
     * @param expectedType the expected return type (must be plural)
     * @param logger
     * @return a list literal that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message.
     */
    public static <T> Expression<? extends T> parseListLiteral(String s, PatternType<T> expectedType, ParserState parserState, SkriptLogger logger) {
        assert !expectedType.isSingle();
        if (!s.contains(",") && !s.contains("and") && !s.contains("nor") && !s.contains("or"))
            return null;
        List<String> parts = new ArrayList<>();
        Matcher m = LIST_SPLIT_PATTERN.matcher(s);
        int lastIndex = 0;
        for (int i = 0; i < s.length(); i = StringUtils.nextSimpleCharacterIndex(s, i+1)) {
            if (i == -1) {
                return null;
            } else if (StringUtils.nextSimpleCharacterIndex(s, i) > i) { // We are currently at the start of something we need to skip over
                i = StringUtils.nextSimpleCharacterIndex(s, i) - 1;
                continue;
            }
            char c = s.charAt(i);
            if (c == ' ' || c == ',') {
                m.region(i, s.length());
                if (m.lookingAt()) {
                    if (i == lastIndex)
                        return null;
                    parts.add(s.substring(lastIndex, i));
                    parts.add(m.group());
                    i = m.end() - 1;
                    lastIndex = i;
                }
            } else if (c == '(') {
                String closing = StringUtils.getEnclosedText(s, '(', ')', i);
                if (closing != null) {
                    i = i + closing.length() + 1;
                }
            }
        }
        if (lastIndex < s.length() - 1)
            parts.add(s.substring(lastIndex));
        if (parts.size() == 1)
            return null;
        Boolean isAndList = null; // Hello nullable booleans, it had been a pleasure NOT using you
        for (int i = 0; i < parts.size(); i++) {
            if ((i & 1) == 1) { // Odd index == separator
                String separator = parts.get(i).trim();
                if (separator.equalsIgnoreCase("and") || separator.equalsIgnoreCase("nor")) {
                    isAndList = true;
                } else if (separator.equalsIgnoreCase("or")) {
                    isAndList = isAndList != null && isAndList;
                }
            }
        }
        isAndList = isAndList == null || isAndList; // Defaults to true
        List<Expression<? extends T>> expressions = new ArrayList<>();
        boolean isLiteralList = true;
        for (int i = 0; i < parts.size(); i++) {
            if ((i & 1) == 0) { // Even index == element
                String part = parts.get(i).trim();
                logger.recurse();
                Expression<? extends T> expression = parseExpression(part, expectedType, parserState, logger);
                logger.callback();
                if (expression == null) {
                    return null;
                }
                isLiteralList &= Literal.isLiteral(expression);
                expressions.add(expression);
            }
        }
        if (expressions.size() == 1)
            return expressions.get(0);
        if (isLiteralList) {
            Literal[] literals = new Literal[expressions.size()];
            for (int i = 0; i < expressions.size(); i++) {
                Expression<? extends T> exp = expressions.get(i);
                if (exp instanceof Literal) {
                    literals[i] = (Literal) exp;
                } else {
                    assert exp instanceof VariableString;
                    literals[i] = new SimpleLiteral(String.class, exp.getSingle(TriggerContext.DUMMY));
                }
            }
            Class<?> returnType = ClassUtils.getCommonSuperclass(Arrays.stream(literals).map(Literal::getReturnType).toArray(Class[]::new));
            return new LiteralList<>(
                literals,
                returnType,
                isAndList
            );
        } else {
            Expression[] exprs = expressions.toArray(new Expression[0]);
            Class<?> returnType = ClassUtils.getCommonSuperclass(Arrays.stream(exprs).map(Expression::getReturnType).toArray(Class[]::new));
            return new ExpressionList<>(
                exprs,
                returnType,
                isAndList
            );
        }
    }

    /**
     * Parses a literal of a given {@link PatternType type} from the given {@linkplain String}
     * @param <T> the type of the literal
     * @param s the string to be parsed as a literal
     * @param expectedType the expected return type
     * @param logger
     * @return a literal that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message.
     */
    public static <T> Expression<? extends T> parseLiteral(String s, PatternType<T> expectedType, ParserState parserState, SkriptLogger logger) {
        Map<Class<?>, Type<?>> classToTypeMap = TypeManager.getClassToTypeMap();
        for (Class<?> c : classToTypeMap.keySet()) {
            Class<? extends T> expectedClass = expectedType.getType().getTypeClass();
            if (expectedClass.isAssignableFrom(c) || Converters.converterExists(c, expectedClass)) {
                Function<String, ?> literalParser = classToTypeMap.get(c).getLiteralParser();
                if (literalParser != null) {
                    T literal = (T) literalParser.apply(s);
                    if (literal != null && expectedClass.isAssignableFrom(c)) {
                        T[] one = (T[]) Array.newInstance(literal.getClass(), 1);
                        one[0] = literal;
                        return new SimpleLiteral<>(one);
                    } else if (literal != null) {
                        return new SimpleLiteral<>((Class<T>) c, literal).convertExpression(expectedType.getType().getTypeClass());
                    }
                } else if (expectedClass == String.class || c == String.class) {
                    VariableString vs = VariableString.newInstanceWithQuotes(s, parserState, logger);
                    if (vs != null) {
                        return (Expression<? extends T>) vs;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Parses a line of code as an {@link Effect}
     * @param s the line to be parsed
     * @param parserState
     * @param logger the logger
     * @return an effect that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message
     */
    public static Effect parseEffect(String s, ParserState parserState, SkriptLogger logger) {
        if (s.isEmpty())
            return null;
        for (SyntaxInfo<? extends Effect> recentEffect : recentEffects) {
            Effect eff = matchEffectInfo(s, recentEffect, parserState, logger);
            if (eff != null) {
                recentEffects.acknowledge(recentEffect);
                logger.clearLogs();
                return eff;
            }
            logger.forgetError();
        }
        // Let's not loop over the same elements again
        List<SyntaxInfo<? extends Effect>> remainingEffects = SyntaxManager.getEffects();
        recentEffects.removeFrom(remainingEffects);
        for (SyntaxInfo<? extends Effect> remainingEffect : remainingEffects) {
            Effect eff = matchEffectInfo(s, remainingEffect, parserState, logger);
            if (eff != null) {
                recentEffects.acknowledge(remainingEffect);
                logger.clearLogs();
                return eff;
            }
            logger.forgetError();
        }
        logger.setContext(ErrorContext.NO_MATCH);
        logger.error("No effect matching '" + s + "' was found", ErrorType.NO_MATCH);
        return null;
    }

    private static Effect matchEffectInfo(String s, SyntaxInfo<? extends Effect> info, ParserState parserState, SkriptLogger logger) {
        List<PatternElement> patterns = info.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            PatternElement element = patterns.get(i);
            logger.setContext(ErrorContext.MATCHING);
            MatchContext parser = new MatchContext(element, parserState, logger);
            if (element.match(s, 0, parser) != -1) {
                try {
                    Effect eff = info.getSyntaxClass().newInstance();
                    logger.setContext(ErrorContext.INITIALIZATION);
                    if (!eff.init(
                        parser.getParsedExpressions().toArray(new Expression[0]),
                        i,
                        parser.toParseResult()
                    )) {
                        continue;
                    }
                    return eff;
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Couldn't instantiate class " + info.getSyntaxClass(), ErrorType.EXCEPTION);
                }
            }
        }
        return null;
    }

    /**
     * Parses a line of code as a {@link Statement}, either an {@link Effect} or an {@link InlineCondition}
     * @param s the line to be parsed
     * @param parserState
     * @param logger the logger
     * @return a statement that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message
     */
    @Nullable
    public static Statement parseStatement(String s, ParserState parserState, SkriptLogger logger) {
        if (s.isEmpty())
            return null;
        if (s.regionMatches(true, 0, "continue if ", 0, "continue if ".length())) { // startsWithIgnoreCase
            InlineCondition cond = parseInlineCondition(s.substring("continue if ".length()), parserState, logger);
            if (parserState.forbidsSyntax(InlineCondition.class)) {
                logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                logger.error("Inline conditions are not allowed in this section", ErrorType.SEMANTIC_ERROR);
                return null;
            } else if (cond != null) {
                return cond;
            }
        }
        Effect eff = parseEffect(s, parserState, logger);
        if (eff == null) {
            return null;
        } else if (parserState.forbidsSyntax(eff.getClass())) {
            logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
            logger.error("The enclosing section does not allow the use of this effect : " + eff.toString(null, logger.isDebug()), ErrorType.SEMANTIC_ERROR);
            return null;
        } else {
            return eff;
        }
    }

    /**
     * Parses a section of a file as a {@link CodeSection}
     * @param section the section to be parsed
     * @param parserState
     * @param logger the logger
     * @return a section that was successfully parsed, or {@literal null} if the section is empty,
     * no match was found
     * or for another reason detailed in an error message
     */
    public static CodeSection parseSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        if (section.getLineContent().isEmpty())
            return null;
        for (SyntaxInfo<? extends CodeSection> recentSection : recentSections) {
            CodeSection sec = matchSectionInfo(section, recentSection, parserState, logger);
            if (sec != null) {
                recentSections.acknowledge(recentSection);
                logger.clearLogs();
                return sec;
            }
            logger.forgetError();
        }
        List<SyntaxInfo<? extends CodeSection>> remainingSections = SyntaxManager.getSections();
        recentSections.removeFrom(remainingSections);
        for (SyntaxInfo<? extends CodeSection> remainingSection : remainingSections) {
            CodeSection sec = matchSectionInfo(section, remainingSection, parserState, logger);
            if (sec != null) {
                recentSections.acknowledge(remainingSection);
                logger.clearLogs();
                return sec;
            }
            logger.forgetError();
        }
        logger.setContext(ErrorContext.NO_MATCH);
        logger.error("No section matching '" + section.getLineContent() + "' was found", ErrorType.NO_MATCH);
        return null;
    }

    private static CodeSection matchSectionInfo(FileSection section, SyntaxInfo<? extends CodeSection> info, ParserState parserState, SkriptLogger logger) {
        List<PatternElement> patterns = info.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            PatternElement element = patterns.get(i);
            logger.setContext(ErrorContext.MATCHING);
            MatchContext parser = new MatchContext(element, parserState, logger);
            if (element.match(section.getLineContent(), 0, parser) != -1) {
                try {
                    CodeSection sec = info.getSyntaxClass().newInstance();
                    logger.setContext(ErrorContext.INITIALIZATION);
                    if (!sec.init(
                            parser.getParsedExpressions().toArray(new Expression[0]),
                            i,
                            parser.toParseResult()
                    )) {
                        continue;
                    }
                    sec.loadSection(section, parserState, logger);
                    return sec;
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Couldn't instantiate class " + info.getSyntaxClass(), ErrorType.EXCEPTION);
                }
            }
        }
        return null;
    }

    /**
     * Parses a section of a file as a {@link Trigger}
     * @param section the section to be parsed
     * @param logger the logger
     * @return a trigger that was successfully parsed, or {@literal null} if the section is empty,
     * no match was found
     * or for another reason detailed in an error message
     */
    @Nullable
    public static UnloadedTrigger parseTrigger(FileSection section, SkriptLogger logger) {
        if (section.getLineContent().isEmpty())
            return null;
        for (SkriptEventInfo<?> recentEvent : recentEvents) {
            UnloadedTrigger trigger = matchEventInfo(section, recentEvent, logger);
            if (trigger != null) {
                recentEvents.acknowledge(recentEvent);
                logger.clearLogs();
                return trigger;
            }
            logger.forgetError();
        }
        // Let's not loop over the same elements again
        List<SkriptEventInfo<?>> remainingEvents = SyntaxManager.getEvents();
        recentEvents.removeFrom(remainingEvents);
        for (SkriptEventInfo<?> remainingEvent : remainingEvents) {
            UnloadedTrigger trigger = matchEventInfo(section, remainingEvent, logger);
            if (trigger != null) {
                recentEvents.acknowledge(remainingEvent);
                logger.clearLogs();
                return trigger;
            }
            logger.forgetError();
        }
        logger.setContext(ErrorContext.NO_MATCH);
        logger.error("No trigger matching '" + section.getLineContent() + "' was found", ErrorType.NO_MATCH);
        return null;
    }

    private static UnloadedTrigger matchEventInfo(FileSection section, SkriptEventInfo<?> info, SkriptLogger logger) {
        List<PatternElement> patterns = info.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            PatternElement element = patterns.get(i);
            ParserState parserState = new ParserState();
            logger.setContext(ErrorContext.MATCHING);
            MatchContext parser = new MatchContext(element, parserState, logger);
            if (element.match(section.getLineContent(), 0, parser) != -1) {
                try {
                    SkriptEvent event = info.getSyntaxClass().newInstance();
                    logger.setContext(ErrorContext.INITIALIZATION);
                    if (!event.init(
                            parser.getParsedExpressions().toArray(new Expression[0]),
                            i,
                            parser.toParseResult()
                    )) {
                        continue;
                    }
                    Trigger trig = new Trigger(event);
                    parserState.setCurrentContexts(info.getContexts());
                    // trig.loadSection(section, parserState, logger);
                    /*
                     * We don't actually load the trigger, that will be left to the loading priority system
                     */
                    return new UnloadedTrigger(trig, section, logger.getLine(), info, parserState);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Couldn't instantiate class " + info.getSyntaxClass(), ErrorType.EXCEPTION);
                }
            }
        }
        return null;
    }
}
