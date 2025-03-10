package nl.bluetainer.jbeancount.language;

public sealed interface JournalDeclaration<T extends Node<T, B>, B extends Node.Builder<T, B>>
    extends Node<T, B>
    permits AbstractDirectiveNode, AbstractPragmaNode, Comment, DirectiveNode, Eol, PragmaNode {}
