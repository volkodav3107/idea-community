/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.codeInspection.actions;

import com.intellij.codeInsight.intention.EmptyIntentionAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.*;
import com.intellij.codeInspection.reference.RefManagerImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: anna
 * Date: 21-Feb-2006
 */
public class CleanupInspectionIntention implements IntentionAction {
  private final LocalInspectionTool myTool;
  private final Class myQuickfixClass;

  public CleanupInspectionIntention(final LocalInspectionTool tool, Class quickFixClass) {
    myTool = tool;
    myQuickfixClass = quickFixClass;
  }

  @NotNull
  public String getText() {
    return InspectionsBundle.message("fix.all.inspection.problems.in.file", myTool.getDisplayName());
  }

  @NotNull
  public String getFamilyName() {
    return getText();
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(project)
      .ensureFilesWritable(file.getVirtualFile());
    if (status.hasReadonlyFiles()) return;
    final InspectionManagerEx managerEx = ((InspectionManagerEx)InspectionManagerEx.getInstance(project));
    final GlobalInspectionContextImpl context = managerEx.createNewGlobalContext(false);
    final LocalInspectionToolWrapper tool = new LocalInspectionToolWrapper(myTool);
    tool.initialize(context);
    ((RefManagerImpl)context.getRefManager()).inspectionReadActionStarted();
    tool.processFile(file, true, managerEx, true);
    final List<CommonProblemDescriptor> descriptions = new ArrayList<CommonProblemDescriptor>(tool.getProblemDescriptors());
    Collections.sort(descriptions, new Comparator<CommonProblemDescriptor>() {
      public int compare(final CommonProblemDescriptor o1, final CommonProblemDescriptor o2) {
        final ProblemDescriptorImpl d1 = (ProblemDescriptorImpl)o1;
        final ProblemDescriptorImpl d2 = (ProblemDescriptorImpl)o2;
        return d2.getTextRange().getStartOffset() - d1.getTextRange().getStartOffset();
      }
    });
    for (CommonProblemDescriptor descriptor : descriptions) {
      final QuickFix[] fixes = descriptor.getFixes();
      if (fixes != null && fixes.length > 0) {
        for (QuickFix fix : fixes) {
          if (fix != null && fix.getClass().isAssignableFrom(myQuickfixClass)) {
            final PsiElement element = ((ProblemDescriptor)descriptor).getPsiElement();
            if (element != null && element.isValid()) {
              fix.applyFix(project, descriptor);
            }
            break;
          }
        }
      }
    }
    ((RefManagerImpl)context.getRefManager()).inspectionReadActionFinished();
    context.cleanup(managerEx);
  }

  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return myQuickfixClass != null && myQuickfixClass != EmptyIntentionAction.class && !(myTool instanceof UnfairLocalInspectionTool);
  }

  public boolean startInWriteAction() {
    return true;
  }
}