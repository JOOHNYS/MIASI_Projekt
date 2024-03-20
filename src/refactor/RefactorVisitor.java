package refactor;

import grammar.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;

public class RefactorVisitor extends JavaScriptParserBaseVisitor<String> {
    private final CharStream input;
    public RefactorVisitor(CharStream input) {
        super();
        this.input = input;
    }

    private String getText(ParserRuleContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        if(input==null) throw new RuntimeException("Input stream undefined");
        return input.getText(new Interval(a,b));
    }

    @Override
    public String visitLiteralExpression(JavaScriptParser.LiteralExpressionContext ctx) {
        System.out.println("LiteralExpression: " + getText(ctx));
        // check if the literal is a string
        return "StringLiteral";

    }
}