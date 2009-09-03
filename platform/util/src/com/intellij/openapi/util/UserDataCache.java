/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package com.intellij.openapi.util;

import org.jetbrains.annotations.NonNls;

public abstract class UserDataCache<T, Owner extends UserDataHolder, S> extends FieldCache<T, Owner, Key<T>, S> {
  private final Key<T> myKey;

  protected UserDataCache() {
    myKey = null;
  }

  public UserDataCache(@NonNls String keyName) {
    myKey = new Key<T>(keyName);
  }

  public T get(final Owner owner, final S parameter) {
    return get(myKey, owner, parameter);
  }

  protected final T getValue(final Owner owner, final Key<T> key) {
    return owner.getUserData(key);
  }

  protected final void putValue(final T t, final Owner owner, final Key<T> key) {
    owner.putUserData(key, t);
  }
}