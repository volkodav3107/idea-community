package com.intellij.openapi.command.impl;

import com.intellij.history.core.LocalVcs;
import com.intellij.history.core.changes.Change;
import com.intellij.history.integration.IdeaGateway;
import com.intellij.history.integration.revertion.ChangeRevertionVisitor;

import java.io.IOException;

public class ChangeRange {
  private final IdeaGateway myGateway;
  private final LocalVcs myVcs;
  private final Change myFromChange;
  private final Change myToChange;

  public ChangeRange(IdeaGateway gw, LocalVcs vcs, Change change) {
    this(gw, vcs, change, change);
  }

  public ChangeRange(IdeaGateway gw, LocalVcs vcs, Change from, Change to) {
    myGateway = gw;
    myVcs = vcs;
    myFromChange = from;
    myToChange = to;
  }

  public ChangeRange revert() throws IOException {
    final Change[] first = {null};
    final Change[] last = {null};
    LocalVcs.Listener l = new LocalVcs.Listener() {
      public void onChange(Change c) {
        if (first[0] == null) first[0] = c;
        last[0] = c;
      }
    };
    myVcs.removeListener(l);
    try {
      myVcs.acceptWrite(new ChangeRangeRevertionVisitor(myGateway, myToChange, myFromChange));
    }
    finally {
      myVcs.removeListener(l);
    }
    assert first[0] != null && first[0] != null;
    return new ChangeRange(myGateway, myVcs, first[0], last[0]);
  }
}