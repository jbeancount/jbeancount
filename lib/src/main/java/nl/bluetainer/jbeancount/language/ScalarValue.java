package nl.bluetainer.jbeancount.language;

public sealed interface ScalarValue extends MetadataValue
    permits Account,
        ArithmeticExpression,
        BooleanValue,
        Commodity,
        DateValue,
        LinkValue,
        NilValue,
        StringValue,
        TagValue {}
