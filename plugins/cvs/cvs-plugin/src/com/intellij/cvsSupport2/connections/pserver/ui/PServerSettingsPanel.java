package com.intellij.cvsSupport2.connections.pserver.ui;

import com.intellij.cvsSupport2.config.CvsApplicationLevelConfiguration;
import com.intellij.cvsSupport2.config.ui.CvsConfigurationPanel;
import com.intellij.openapi.ui.InputException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;

/**
 * author: lesya
 */
public class PServerSettingsPanel {
  private TextFieldWithBrowseButton myPathToPasswordFile;
  private JTextField myTimeout;
  private JPanel myPanel;
  private JLabel myConnectionTimeoutLabel;
  private JLabel myPasswordFileLabel;

  public PServerSettingsPanel() {
    CvsConfigurationPanel.addBrowseHandler(myPathToPasswordFile, com.intellij.CvsBundle.message("dialog.title.select.path.to.cvs.password.file"));
    myConnectionTimeoutLabel.setLabelFor(myTimeout);
    myPasswordFileLabel.setLabelFor(myPathToPasswordFile.getTextField());
  }

  public JPanel getPanel() {
    return myPanel;
  }

  public void updateFrom(CvsApplicationLevelConfiguration config) {
    myPathToPasswordFile.setText(config.getPathToPassFilePresentation());
    myPathToPasswordFile.getTextField().selectAll();
    myTimeout.setText(String.valueOf(config.TIMEOUT));
    myTimeout.selectAll();
  }

  public void saveTo(CvsApplicationLevelConfiguration config) {
    config.setPathToPasswordFile(myPathToPasswordFile.getText());
    try {
      int timeout = Integer.parseInt(myTimeout.getText());
      if (timeout < 0) throwInvalidTimeoutException();
      config.TIMEOUT = timeout;
    }
    catch (NumberFormatException ex) {
      throwInvalidTimeoutException();
    }
  }

  private void throwInvalidTimeoutException() {
    throw new InputException(com.intellij.CvsBundle.message("exception.message.invalid.timeout.value", myTimeout.getText()), myTimeout);
  }


}