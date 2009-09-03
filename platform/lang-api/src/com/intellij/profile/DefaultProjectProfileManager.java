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
package com.intellij.profile;

import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.components.StateSplitter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.packageDependencies.DependencyValidationManager;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.util.ArrayUtil;
import com.intellij.util.text.UniqueNameGenerator;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * User: anna
 * Date: 30-Nov-2005
 */
public abstract class DefaultProjectProfileManager extends ProjectProfileManager {
  protected static final Logger LOG = Logger.getInstance("#com.intellij.profile.DefaultProjectProfileManager");

  @NonNls protected static final String PROFILES = "profiles";
  @NonNls public static final String SCOPES = "scopes";
  @NonNls protected static final String SCOPE = "scope";
  @NonNls public static final String PROFILE = "profile";
  @NonNls protected static final String NAME = "name";


  private static final String VERSION = "1.0";

  protected final Project myProject;

  private final String myProfileType;

  public String PROJECT_PROFILE;
  public boolean USE_PROJECT_PROFILE = !isPlatform();

  protected ApplicationProfileManager myApplicationProfileManager;

  protected Map<String, Profile> myProfiles = new HashMap<String, Profile>();
  protected final DependencyValidationManager myHolder;
  protected List<ProfileChangeAdapter> myProfilesListener = new ArrayList<ProfileChangeAdapter>();

  public DefaultProjectProfileManager(final Project project, final String profileType, final DependencyValidationManager holder) {
    myProject = project;
    myProfileType = profileType;
    myHolder = holder;
    myApplicationProfileManager = ApplicationProfileManager.getProfileManager(profileType);
    LOG.assertTrue(myApplicationProfileManager != null);
  }

  public Profile getProfile(@NotNull String name, boolean returnRootProfileIfNamedIsAbsent) {
    return myProfiles.containsKey(name) ? myProfiles.get(name) : myApplicationProfileManager.getProfile(name, returnRootProfileIfNamedIsAbsent);
  }

  public void updateProfile(Profile profile) {
    myProfiles.put(profile.getName(), profile);
    for (ProfileChangeAdapter profileChangeAdapter : myProfilesListener) {
      profileChangeAdapter.profileChanged(profile);
    }
  }



  public void readExternal(Element element) throws InvalidDataException {
    myProfiles.clear();
    DefaultJDOMExternalizer.readExternal(this, element);
    final Element profilesElement = element.getChild(PROFILES);
    if (profilesElement != null) {
      for (Object o : profilesElement.getChildren(PROFILE)) {
        final Profile profile = myApplicationProfileManager.createProfile();
        profile.setProfileManager(this);
        profile.readExternal((Element)o);
        final String name = profile.getName();
        if (myApplicationProfileManager.getProfile(name) != null) { //override ide profile
          // myApplicationProfileManager.deleteProfile(name);
        }
        myProfiles.put(name, profile);
      }
    }
    if (element.getChild("version") == null || !Comparing.strEqual(element.getChild("version").getAttributeValue("value"), VERSION)) {
      boolean toConvert = true;
      for (Object o : element.getChildren("option")) {
        if (Comparing.strEqual(((Element)o).getAttributeValue("name"), "USE_PROJECT_LEVEL_SETTINGS")) {
          toConvert = Boolean.parseBoolean(((Element)o).getAttributeValue("value"));
          break;
        }
      }
      if (toConvert) {
        convert(element);
      }
    }
  }

  protected void convert(Element element) throws InvalidDataException {
  }

  public void writeExternal(Element element) throws WriteExternalException {
    DefaultJDOMExternalizer.writeExternal(this, element);
    Element version = new Element("version");
    version.setAttribute("value", VERSION);
    element.addContent(version);
    final List<String> sortedProfiles = new ArrayList<String>(myProfiles.keySet());
    Collections.sort(sortedProfiles);
    final Element profiles = new Element(PROFILES);
    for (String profile : sortedProfiles) {
      final Profile projectProfile = myProfiles.get(profile);
      if (projectProfile != null) {
        final Element profileElement = new Element(PROFILE);
        projectProfile.writeExternal(profileElement);
        profiles.addContent(profileElement);
      }
    }
    element.addContent(profiles);
  }

  public NamedScopesHolder getScopesManager() {
    return DependencyValidationManager.getInstance(myProject);
  }

  public String getProfileType() {
    return myProfileType;
  }

  public Collection<Profile> getProfiles() {
    getProjectProfileImpl();
    return myProfiles.values();
  }

  public String[] getAvailableProfileNames() {
    return ArrayUtil.toStringArray(myProfiles.keySet());
  }

  public void deleteProfile(String name) {
    myProfiles.remove(name);
  }

  public String getProjectProfile() {
    return PROJECT_PROFILE;
  }

  public void setProjectProfile(final String projectProfile) {
    final String profileName = PROJECT_PROFILE;
    PROJECT_PROFILE = projectProfile;
    USE_PROJECT_PROFILE = projectProfile != null;
    for (ProfileChangeAdapter adapter : myProfilesListener) {
      adapter.profileActivated(profileName != null ? getProfile(profileName) : null, projectProfile != null ?  getProfile(projectProfile) : null);
    }
  }

  @NotNull
  public Profile getProjectProfileImpl(){
    if (isPlatform() || !USE_PROJECT_PROFILE) return myApplicationProfileManager.getRootProfile();
    if (PROJECT_PROFILE == null || myProfiles.isEmpty()){
      @NonNls final String projectProfileName = "Project Default";
      setProjectProfile(projectProfileName);
      final Profile projectProfile = myApplicationProfileManager.createProfile();
      projectProfile.copyFrom(myApplicationProfileManager.getRootProfile());
      projectProfile.setLocal(false);
      projectProfile.setName(projectProfileName);
      myProfiles.put(projectProfileName, projectProfile);
    } else if (!myProfiles.containsKey(PROJECT_PROFILE)){
      final String projectProfileAttempt = myProfiles.keySet().iterator().next();
      setProjectProfile(projectProfileAttempt);
    }
    final Profile profile = myProfiles.get(PROJECT_PROFILE);
    profile.setProfileManager(this);
    return profile;
  }

  private static boolean isPlatform() {
    return !ApplicationNamesInfo.getInstance().getLowercaseProductName().equals("Idea");
  }

  public void addProfilesListener(ProfileChangeAdapter profilesListener) {
    myProfilesListener.add(profilesListener);
  }

  public void removeProfilesListener(ProfileChangeAdapter profilesListener) {
    myProfilesListener.remove(profilesListener);
  }

  public static class ProfileStateSplitter implements StateSplitter {

    public List<Pair<Element, String>> splitState(final Element e) {
      final UniqueNameGenerator generator = new UniqueNameGenerator();
      List<Pair<Element, String>> result = new ArrayList<Pair<Element, String>>();

      final Element[] elements = JDOMUtil.getElements(e);
      for (Element element : elements) {
        if (element.getName().equals("profiles")) {
          element.detach();

          final Element[] profiles = JDOMUtil.getElements(element);
          for (Element profile : profiles) {
            String profileName = null;

            final Element[] options = JDOMUtil.getElements(profile);
            for (Element option : options) {
              if (option.getName().equals("option") && option.getAttributeValue("name").equals("myName")) {
                profileName = option.getAttributeValue("value");
              }
            }

            assert profileName != null;

            final String name = generator.generateUniqueName(FileUtil.sanitizeFileName(profileName)) + ".xml";
            result.add(new Pair<Element, String>(profile,  name));
          }
        }
      }


      result.add(new Pair<Element, String>(e, generator.generateUniqueName("profiles_settings") + ".xml"));

      return result;
    }

    public void mergeStatesInto(final Element target, final Element[] elements) {
      Element profiles = new Element("profiles");
      target.addContent(profiles);

        for (Element element : elements) {
        if (element.getName().equals("profile")) {
          element.detach();
          profiles.addContent(element);
        }
        else {
          final Element[] states = JDOMUtil.getElements(element);
          for (Element state : states) {
            state.detach();
            target.addContent(state);
          }
        }
      }
    }
  }

}