/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.vcs.changes.patch;

import com.intellij.openapi.diff.impl.patch.TextFilePatch;
import com.intellij.openapi.diff.impl.patch.formove.PathMerger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.openapi.vcs.changes.SimpleContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FilePatchInProgress {
  private final TextFilePatch myPatch;
  private final FilePatchStatus myStatus;

  private VirtualFile myBase;
  private File myIoCurrentBase;
  private VirtualFile myCurrentBase;
  private boolean myBaseExists;
  private ContentRevision myNewContentRevision;
  private ContentRevision myCurrentRevision;
  private final List<VirtualFile> myAutoBases;

  private File myAfterFile;

  public FilePatchInProgress(final TextFilePatch patch, final Collection<VirtualFile> autoBases, final VirtualFile baseDir) {
    myPatch = patch;
    if (autoBases != null) {
      myAutoBases = new ArrayList<VirtualFile>();
      final String path = myPatch.getBeforeName() == null ? myPatch.getAfterName() : myPatch.getBeforeName();
      for (VirtualFile autoBase : autoBases) {
        final VirtualFile willBeBase = PathMerger.getBase(autoBase, path);
        if (willBeBase != null) {
          myAutoBases.add(willBeBase);
        }
      }
    } else {
      myAutoBases = Collections.emptyList();
    }
    myStatus = getStatus(myPatch);
    if (myAutoBases.isEmpty()) {
      setNewBase(baseDir);
    } else {
      setNewBase(myAutoBases.get(0));
    }
  }

  private static FilePatchStatus getStatus(final TextFilePatch patch) {
    final String beforeName = patch.getBeforeName().replace("\\", "/");
    final String afterName = patch.getAfterName().replace("\\", "/");
    
    if (patch.isNewFile() || (beforeName == null)) {
      return FilePatchStatus.ADDED;
    } else if (patch.isDeletedFile() || (afterName == null)) {
      return FilePatchStatus.DELETED;
    }

    if (beforeName.equals(afterName)) return FilePatchStatus.MODIFIED;
    return FilePatchStatus.MOVED_OR_RENAMED;
  }

  public PatchChange getChange() {
    return new PatchChange(getCurrentRevision(), getNewContentRevision(), this);
  }

  public void setNewBase(final VirtualFile base) {
    myBase = base;
    myNewContentRevision = null;
    myCurrentRevision = null;
    myAfterFile = null;

    final String beforeName = myPatch.getBeforeName();
    if (beforeName != null) {
      myIoCurrentBase = PathMerger.getFile(new File(myBase.getPath()), beforeName);
      myCurrentBase = VcsUtil.getVirtualFileWithRefresh(myIoCurrentBase);
      myBaseExists = (myCurrentBase != null) && myCurrentBase.exists();
    } else {
      // creation
      final String afterName = myPatch.getAfterName();
      myBaseExists = true;
      myIoCurrentBase = PathMerger.getFile(new File(myBase.getPath()), afterName);
      myCurrentBase = VcsUtil.getVirtualFileWithRefresh(myIoCurrentBase);
    }
  }

  public void setCreatedCurrentBase(final VirtualFile vf) {
    myCurrentBase = vf;
  }

  public FilePatchStatus getStatus() {
    return myStatus;
  }

  public File getIoCurrentBase() {
    return myIoCurrentBase;
  }

  public VirtualFile getCurrentBase() {
    return myCurrentBase;
  }

  public VirtualFile getBase() {
    return myBase;
  }

  public TextFilePatch getPatch() {
    return myPatch;
  }

  public boolean isBaseExists() {
    return myBaseExists;
  }

  public boolean baseExistsOrAdded() {
    return myBaseExists || FilePatchStatus.ADDED.equals(myStatus);
  }

  public ContentRevision getNewContentRevision() {
    if (FilePatchStatus.DELETED.equals(myStatus)) return null;

    if (myNewContentRevision == null) {
      if (FilePatchStatus.ADDED.equals(myStatus)) {
        final FilePath newFilePath = FilePathImpl.createNonLocal(myIoCurrentBase.getAbsolutePath(), false);
        final String content = myPatch.getNewFileText();
        myNewContentRevision = new SimpleContentRevision(content, newFilePath, myPatch.getAfterVersionId());
      } else {
        final FilePath newFilePath;
        if (FilePatchStatus.MOVED_OR_RENAMED.equals(myStatus)) {
          newFilePath = new FilePathImpl(PathMerger.getFile(new File(myBase.getPath()), myPatch.getAfterName()), false);
        } else {
          newFilePath = (myCurrentBase != null) ? new FilePathImpl(myCurrentBase) : new FilePathImpl(myIoCurrentBase, false);
        }
        myNewContentRevision = new LazyPatchContentRevision(myCurrentBase, newFilePath, myPatch.getAfterVersionId(), myPatch);
      }
    }
    return myNewContentRevision;
  }

  public ContentRevision getCurrentRevision() {
    if (FilePatchStatus.ADDED.equals(myStatus)) return null; 
    if (myCurrentRevision == null) {
      final FilePathImpl filePath = (myCurrentBase != null) ? new FilePathImpl(myCurrentBase) : new FilePathImpl(myIoCurrentBase, false);
      myCurrentRevision = new CurrentContentRevision(filePath);
    }
    return myCurrentRevision;
  }

  public static class PatchChange extends Change {
    private final FilePatchInProgress myPatchInProgress;

    public PatchChange(ContentRevision beforeRevision, ContentRevision afterRevision, FilePatchInProgress patchInProgress) {
      super(beforeRevision, afterRevision,
            patchInProgress.isBaseExists() || FilePatchStatus.ADDED.equals(patchInProgress.getStatus()) ? null : FileStatus.MERGED_WITH_CONFLICTS);
      myPatchInProgress = patchInProgress;
    }

    public FilePatchInProgress getPatchInProgress() {
      return myPatchInProgress;
    }
  }

  public List<VirtualFile> getAutoBasesCopy() {
    final ArrayList<VirtualFile> result = new ArrayList<VirtualFile>(myAutoBases.size() + 1);
    result.addAll(myAutoBases);
    return result;
  }

  public Pair<String, String> getKey() {
    return new Pair<String, String>(myPatch.getBeforeName(), myPatch.getAfterName());
  }
}