package nl.bluetainer.jbeancount.language;

public sealed interface CompoundExpression
    permits BinaryCompoundExpression, UnaryCompoundExpression {}
