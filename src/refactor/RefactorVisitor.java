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
    
    @Override
    public String visitImportStatement(JavaScriptParser.ImportStatementContext ctx) {
        this.refactorImport(ctx);
        return super.visitImportStatement(ctx);
    }

    @Override
    public Void visitDeclaration(JavaScriptParser.DeclarationContext ctx) {
        ParseTree variableNode = ctx.VARNAME(0);
        String originalName = variableNode.getText();
        String transformedName = transformVariableName(originalName);
        variableNode.getPayload().setText(transformedName);
        return super.visitDeclaration(ctx);
    }

     @Override
    public Void visitExpression(JavaScriptParser.ExpressionContext ctx) {
        ParseTree variableNode = ctx.VARNAME();
        String originalName = variableNode.getText();
        String transformedName = transformVariableName(originalName);
        variableNode.getPayload().setText(transformedName);
        return super.visitExpression(ctx);
    }

    private void refactorImport (JavaScriptParser.ImportStatementContext ctx) {
        // Detect import type (simple/from import)
        var importBody = ctx.importFromBlock();
        if (importBody.importFrom() == null) {
            handleSimpleImport(ctx);
        } else {
            handleFromImport(ctx);
        }
    }

    private void handleSimpleImport(JavaScriptParser.ImportStatementContext ctx) {
        System.out.println("Refactoring simple import");
        // Replace import 'module' with require('module');
        String text = ctx.getText();
        String modifiedText = text.replace("import", "require(") + ");";
        this.rewriter.replace(ctx.start, ctx.stop, modifiedText);
    }

    private void handleFromImport(JavaScriptParser.ImportStatementContext ctx) {
        System.out.println("Refactoring from import");
        // Get import body and extract module and identifiers
        var importBody = ctx.importFromBlock();
        var newImport = new StringBuilder();
        var importModule = importBody.importFrom().StringLiteral().getText();

        // Handle potential default import ( import defaultIdentifier, namedIdentifier from 'module'; )
        if (importBody.importDefault() != null) {
            var defaultIdentifier = importBody.importDefault().aliasName().identifierName().get(0).getText();
            newImport.append("const ")
                    .append(defaultIdentifier)
                    .append(" = require(")
                    .append(importModule)
                    .append(");\n");
        }
        // Handle named identifier
        var namedIdentifier = importBody.importNamespace().identifierName().get(0).getText();
        newImport.append("const { ")
                .append(namedIdentifier)
                .append(" } = require(")
                .append(importModule)
                .append(");");
        this.rewriter.replace(ctx.start, ctx.stop, newImport.toString());
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

    private String transformVariableName(String name) {
        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            result.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
        }
        return result.toString();
    }
}
