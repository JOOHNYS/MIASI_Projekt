package refactor;

import grammar.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.List;
import java.util.stream.Collectors;

public class RefactorVisitor extends JavaScriptParserBaseVisitor<String> {
    private final CharStream input;
    private final RefactorConfig config;
    public TokenStreamRewriter rewriter;

    public RefactorVisitor(CharStream input, RefactorConfig config, CommonTokenStream tokens) {
        super();
        this.input = input;
        this.config = config;
        this.rewriter = new TokenStreamRewriter(tokens);
    }

    private String getText(ParserRuleContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        if (input == null) throw new RuntimeException("Input stream undefined");
        return input.getText(new Interval(a, b));
    }

    @Override
    public String visitFunctionDeclaration(JavaScriptParser.FunctionDeclarationContext ctx) {
        this.refactorArguments(ctx);
        return super.visitFunctionDeclaration(ctx);
    }

    @Override
    public String visitLiteralExpression(JavaScriptParser.LiteralExpressionContext ctx) {
        this.refactorQuotes(ctx);
        this.refactorTemplateString(ctx);
        return super.visitLiteralExpression(ctx);
    }

    private void refactorQuotes(JavaScriptParser.LiteralExpressionContext ctx) {
        if (ctx.literal().StringLiteral() != null) {
            String text = ctx.literal().StringLiteral().getText();
            String modifiedText = this.config.getQuote() + text.substring(1, text.length() - 1) + this.config.getQuote();
            TerminalNode newNode = new TerminalNodeImpl(new CommonToken(JavaScriptParser.StringLiteral, modifiedText));
            this.rewriter.replace(ctx.start, ctx.stop, modifiedText);
        }
    }

    private void refactorArguments(JavaScriptParser.FunctionDeclarationContext ctx) {
        System.out.println("Refactoring arguments");
        if (ctx.formalParameterList() != null) {
            List<String> arguments = ctx.formalParameterList().getRuleContexts(ParserRuleContext.class)
                    .stream()
                    .map(RuleContext::getText)
                    .sorted()
                    .collect(Collectors.toList());

            String modifiedText = String.join(", ", arguments);
            rewriter.replace(ctx.formalParameterList().start, ctx.formalParameterList().stop, modifiedText);
        }
    }
    private void refactorTemplateString(JavaScriptParser.LiteralExpressionContext ctx) {
    if (ctx.literal().StringLiteral() != null) {
        String text = ctx.literal().StringLiteral().getText();
        if (text.startsWith("`") && text.endsWith("`")) {
            String content = text.substring(1, text.length() - 1);
            content = content.replaceAll("\\$\\{([^}]*)\\}", "\" + $1 + \"");
            String modifiedText = "\"" + content + "\"";
            this.rewriter.replace(ctx.start, ctx.stop, modifiedText);
        }
    }
}
