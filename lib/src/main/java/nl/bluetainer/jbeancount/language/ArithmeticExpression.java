package nl.bluetainer.jbeancount.language;

public sealed interface ArithmeticExpression extends ScalarValue
    permits ConstantExpression,
        AbstractBinaryArithmeticExpression,
        AbstractUnaryArithmeticExpression {}
