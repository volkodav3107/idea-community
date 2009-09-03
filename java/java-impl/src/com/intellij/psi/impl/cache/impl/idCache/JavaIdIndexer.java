package com.intellij.psi.impl.cache.impl.idCache;

import com.intellij.lexer.FilterLexer;
import com.intellij.lexer.JavaLexer;
import com.intellij.lexer.Lexer;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.impl.cache.impl.BaseFilterLexer;
import com.intellij.psi.impl.cache.impl.id.LexerBasedIdIndexer;
import com.intellij.psi.impl.source.tree.StdTokenSets;

public class JavaIdIndexer extends LexerBasedIdIndexer {
  protected Lexer createLexer(final BaseFilterLexer.OccurrenceConsumer consumer) {
    final JavaLexer javaLexer = new JavaLexer(LanguageLevel.JDK_1_3);
    final JavaFilterLexer filterLexer = new JavaFilterLexer(javaLexer, consumer);
    return new FilterLexer(filterLexer, new FilterLexer.SetFilter(StdTokenSets.WHITE_SPACE_OR_COMMENT_BIT_SET));
  }
}