/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.openapi.vcs.changes.shelf;

import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vcs.FileStatus;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class ShelvedBinaryFile implements JDOMExternalizable {
  public String BEFORE_PATH;
  public String AFTER_PATH;
  @Nullable public String SHELVED_PATH;         // null if binary file was deleted

  public ShelvedBinaryFile() {
  }

  public ShelvedBinaryFile(final String beforePath, final String afterPath, @Nullable final String shelvedPath) {
    assert beforePath != null || afterPath != null;
    BEFORE_PATH = beforePath;
    AFTER_PATH = afterPath;
    SHELVED_PATH = shelvedPath;
  }

  public void readExternal(Element element) throws InvalidDataException {
    DefaultJDOMExternalizer.readExternal(this, element);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    DefaultJDOMExternalizer.writeExternal(this, element);
  }

  public FileStatus getFileStatus() {
    if (BEFORE_PATH == null) {
      return FileStatus.ADDED;
    }
    if (SHELVED_PATH == null) {
      return FileStatus.DELETED;
    }
    return FileStatus.MODIFIED;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ShelvedBinaryFile that = (ShelvedBinaryFile)o;

    if (AFTER_PATH != null ? !AFTER_PATH.equals(that.AFTER_PATH) : that.AFTER_PATH != null) return false;
    if (BEFORE_PATH != null ? !BEFORE_PATH.equals(that.BEFORE_PATH) : that.BEFORE_PATH != null) return false;
    if (SHELVED_PATH != null ? !SHELVED_PATH.equals(that.SHELVED_PATH) : that.SHELVED_PATH != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = BEFORE_PATH != null ? BEFORE_PATH.hashCode() : 0;
    result = 31 * result + (AFTER_PATH != null ? AFTER_PATH.hashCode() : 0);
    result = 31 * result + (SHELVED_PATH != null ? SHELVED_PATH.hashCode() : 0);
    return result;
  }
}