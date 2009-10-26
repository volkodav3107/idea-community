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
package com.intellij.openapi.vfs.impl.win32;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.local.LocalFileSystemBase;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class Win32LocalFileSystem extends LocalFileSystemBase {

  private final Win32Kernel myKernel = new Win32Kernel();

  @Override
  public String[] list(VirtualFile file) {
    try {
      String[] strings = myKernel.list(file.getPath());
      //assert Arrays.asList(strings).equals(Arrays.asList(super.list(file)));
      return strings;
    }
    catch (Exception e) {
//      assert false;
      return super.list(file);
    }
  }

  @Override
  public boolean exists(VirtualFile fileOrDirectory) {
    if (fileOrDirectory.getParent() == null) return true;
    boolean b = myKernel.exists(fileOrDirectory.getPath());
    //assert b == super.exists(fileOrDirectory);
    return b;
  }

  @Override
  public boolean isDirectory(VirtualFile file) {
    try {
      boolean b = myKernel.isDirectory(file.getPath());
      //assert b == super.isDirectory(file);
      return b;
    }
    catch (FileNotFoundException e) {
//      assert false;
      return super.isDirectory(file);
    }
  }

  @Override
  public boolean isWritable(VirtualFile file) {
    try {
      boolean b = myKernel.isWritable(file.getPath());
      //assert b == super.isWritable(file);
      return b;
    }
    catch (FileNotFoundException e) {
//      assert false: file;
      return super.isWritable(file);
    }
  }

  @Override
  public long getTimeStamp(VirtualFile file) {
    try {
      long timeStamp = myKernel.getTimeStamp(file.getPath());
      //assert timeStamp == super.getTimeStamp(file);
      return timeStamp;
    }
    catch (FileNotFoundException e) {
//      assert false;
      return super.getTimeStamp(file);
    }
  }

  @Override
  public WatchRequest addRootToWatch(@NotNull String rootPath, boolean toWatchRecursively) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Set<WatchRequest> addRootsToWatch(@NotNull Collection<String> rootPaths, boolean toWatchRecursively) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeWatchedRoots(@NotNull Collection<WatchRequest> rootsToWatch) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeWatchedRoot(@NotNull WatchRequest watchRequest) {
    throw new UnsupportedOperationException();
  }

}