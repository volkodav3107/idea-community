package com.intellij.psi.impl;

import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.openapi.project.Project;
import com.intellij.util.CachedValueBase;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public class PsiCachedValue<T> extends CachedValueBase<T> {

  private final PsiManager myManager;
  protected long myLastPsiTimeStamp = -1;

  public PsiCachedValue(PsiManager manager) {
    super();
    myManager = manager;
  }

  @Override
  protected Data<T> computeData(T value, Object[] dependencies) {
    Data<T> data = super.computeData(value, dependencies);

    myLastPsiTimeStamp = myManager.getModificationTracker().getModificationCount();

    return data;
  }

  @Override
  protected boolean isUpToDate(@NotNull Data data) {
    return !myManager.isDisposed() && super.isUpToDate(data);
  }

  @Override
  protected boolean isDependencyOutOfDate(Object dependency, long oldTimeStamp) {
    return !(dependency instanceof PsiElement && myLastPsiTimeStamp == myManager.getModificationTracker().getModificationCount()) &&
           super.isDependencyOutOfDate(dependency, oldTimeStamp);

  }

  @Override
  protected long getTimeStamp(Object dependency) {

    if (dependency instanceof PsiDirectory) {
      return myManager.getModificationTracker().getOutOfCodeBlockModificationCount();
    }

    if (dependency instanceof PsiElement) {
      PsiElement element = (PsiElement)dependency;
      if (!element.isValid()) return -1;
      PsiFile containingFile = element.getContainingFile();
      if (containingFile == null) return -1;
      return containingFile.getModificationStamp();
    }

    if (dependency == PsiModificationTracker.MODIFICATION_COUNT) {
      return myManager.getModificationTracker().getModificationCount();
    }
    else if (dependency == PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT) {
      return myManager.getModificationTracker().getOutOfCodeBlockModificationCount();
    }
    else if (dependency == PsiModificationTracker.JAVA_STRUCTURE_MODIFICATION_COUNT) {
      return myManager.getModificationTracker().getJavaStructureModificationCount();
    }

    return super.getTimeStamp(dependency);
  }

  @Override
  public boolean isFromMyProject(Project project) {
    return myManager.getProject() == project;
  }
}