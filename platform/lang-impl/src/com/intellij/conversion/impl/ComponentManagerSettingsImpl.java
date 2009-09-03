package com.intellij.conversion.impl;

import com.intellij.conversion.CannotConvertException;
import com.intellij.conversion.ComponentManagerSettings;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author nik
 */
public abstract class ComponentManagerSettingsImpl implements ComponentManagerSettings {
  protected final SettingsXmlFile mySettingsFile;

  protected ComponentManagerSettingsImpl(File file, ConversionContextImpl context) throws CannotConvertException {
    mySettingsFile = context.getOrCreateFile(file);
  }

  @NotNull
  public Document getDocument() {
    return mySettingsFile.getDocument();
  }

  @NotNull
  public Element getRootElement() {
    return mySettingsFile.getRootElement();
  }

  public Element getComponentElement(@NotNull @NonNls String componentName) {
    return mySettingsFile.findComponent(componentName);
  }

  public void save() throws IOException {
    mySettingsFile.save();
  }
}