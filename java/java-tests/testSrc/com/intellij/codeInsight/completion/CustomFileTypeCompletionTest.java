package com.intellij.codeInsight.completion;

import com.intellij.JavaTestUtil;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: Oct 14, 2004
 * Time: 4:39:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomFileTypeCompletionTest extends LightCompletionTestCase {
  private static final String BASE_PATH = "/codeInsight/completion/customFileType/";

  @Override
  protected String getTestDataPath() {
    return JavaTestUtil.getJavaTestDataPath();
  }

  public void testKeyWordCompletion() throws Exception {
    configureByFile(BASE_PATH + "1.cs");
    checkResultByFile(BASE_PATH + "1_after.cs");

    configureByFile(BASE_PATH + "2.cs");
    checkResultByFile(BASE_PATH + "2_after.cs");
  }

  public void testWordCompletion() throws Throwable {
    configureByFile(BASE_PATH + "WordCompletion.cs");
    testByCount(2, "while", "whiwhiwhi");
  }

  public void testErlang() throws Throwable {
    configureByFile(BASE_PATH + "Erlang.erl");
    testByCount(2, "case", "catch");
  }
}