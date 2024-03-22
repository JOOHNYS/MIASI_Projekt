package refactor;

import grammar.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.Collections;

public class RefactorVisitor extends JavaScriptParserBaseVisitor<String> {
    private final CharStream input;
    private final RefactorConfig config;
    public RefactorVisitor(CharStream input, RefactorConfig config) {
        super();
        this.input = input;
        this.config = config;
    }

    private String getText(ParserRuleContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        if(input==null) throw new RuntimeException("Input stream undefined");
        return input.getText(new Interval(a,b));
    }

    @Override
    public String visitLiteralExpression(JavaScriptParser.LiteralExpressionContext ctx) {
        this.refactorQuotes(ctx);
        return super.visitLiteralExpression(ctx);
    }

    private void refactorQuotes(JavaScriptParser.LiteralExpressionContext ctx) {
        if(ctx.literal().StringLiteral()!=null) {
            String text = ctx.literal().StringLiteral().getText();
            String modifiedText = this.config.getQuote() + text.substring(1, text.length() - 1) + this.config.getQuote();
            TerminalNode newNode = new TerminalNodeImpl(new CommonToken(JavaScriptParser.StringLiteral, modifiedText));
            ctx.children = new ArrayList<>(Collections.singletonList(newNode));
        }
    }
}