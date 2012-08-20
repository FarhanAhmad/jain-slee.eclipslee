/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.eclipslee.servicecreation.popup.actions;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.mobicents.eclipslee.util.maven.MavenProjectUtils;

/**
 * Handler for "Add Module" menu
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class AddModuleAction implements IObjectActionDelegate {

  private String moduleType;

  public AddModuleAction() {
    super();
  }

  public AddModuleAction(String moduleType) {
    super();
    this.moduleType = moduleType;
  }

  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

  public void run(IAction action) {
    initialize();
    if (!initialized) {
      MessageDialog.openError(new Shell(), "Error Deleting Module", getLastError());
      return;
    }

    CaptureModuleNameWizard wizard = new CaptureModuleNameWizard();
    WizardDialog dialog = new WizardDialog(new Shell(), wizard);
    dialog.create();
    dialog.open();
  }

  /**
   * Get the EventXML data object for the current selection.
   * 
   */

  private void initialize() {

    if (selection == null && selection.isEmpty()) {
      setLastError("Please select a JAIN SLEE project first.");
      return;
    }

    if (!(selection instanceof IStructuredSelection)) {
      setLastError("Please select a JAIN SLEE project first.");
      return;
    }

    IStructuredSelection ssel = (IStructuredSelection) selection;
    if (ssel.size() > 1) {
      setLastError("This plugin only supports editing of one project at a time.");
      return;
    }

    // Get the first (and only) item in the selection.
    Object obj = ssel.getFirstElement();
    if (obj instanceof IProject) {

      // Get the maven identifier to look for later
      project = (IProject) obj;
      parentPom = project.getFile("pom.xml");
    }
    else {
      setLastError("Unsupported object type: " + obj.getClass().toString());
      return;
    }

    // Initialization complete
    initialized = true;
    return;
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    this.selection = selection;
  }

  private void setLastError(String error) {
    if (error == null) {
      lastError = "Success";
    }
    else {
      lastError = error;
    }
  }

  private String getLastError() {
    String error = lastError;
    setLastError(null);
    return error;
  }

  private ISelection selection;

  private String lastError;

  private IProject project;
  private IFile parentPom;

  private boolean initialized = false;

  MavenExecutionResult mavenResult = null;

  private class CaptureModuleNameWizard extends Wizard {
    ModuleNamePage moduleNamePage;

    public void addPages() {
      moduleNamePage = new ModuleNamePage("Module Name Page");
      addPage(moduleNamePage);
    }

    public boolean performFinish() {
      boolean moduleExists = true;
      try {
        String moduleName = moduleNamePage.getModuleName();
        boolean isDefaultModule = moduleName.equals("");
        String fullModuleName = moduleName + (isDefaultModule ? "" : "-") + moduleType;
        IFolder moduleFolder = project.getFolder(fullModuleName);
        moduleExists = moduleFolder.exists();
        if(!moduleExists) {
          // Create maven folder structure
          moduleFolder.create(true, true, null);
          moduleFolder.getFolder("src").create(true, true, null);
          moduleFolder.getFolder("src/main").create(true, true, null);
          moduleFolder.getFolder("src/main/java").create(true, true, null);
          moduleFolder.getFolder("src/main/resources").create(true, true, null);

          // Generate pom.xml
          if(moduleType.equals("events")) {
            MavenProjectUtils.generateEventsModulePomFile(project, null, isDefaultModule ? null : moduleName);
          }
          else if (moduleType.equals("sbb")) {
            MavenProjectUtils.generateSBBModulePomFile(project, null, isDefaultModule ? null : moduleName);
          }
          else if (moduleType.equals("profile-spec")) {
            MavenProjectUtils.generateProfileSpecModulePomFile(project, null, isDefaultModule ? null : moduleName);
          }
          else if (moduleType.equals("ratype")) {
            MavenProjectUtils.generateRATypeModulePomFile(project, null, isDefaultModule ? null : moduleName);
          }
          else if (moduleType.equals("ra")) {
            MavenProjectUtils.generateRAModulePomFile(project, null, isDefaultModule ? null : moduleName);
          }
          else if (moduleType.equals("library")) {
            MavenProjectUtils.generateLibraryModulePomFile(project, null, isDefaultModule ? null : moduleName);
          }

          project.refreshLocal(IResource.DEPTH_INFINITE, null);

          // Update parent pom
          MavenXpp3Reader reader = new MavenXpp3Reader();
          Model parentModel = reader.read(new InputStreamReader(parentPom.getContents()));

          // if (library) add at first position; else add before last (which should be du)
          List<String> modules = parentModel.getModules();
          modules.add(moduleType.equals("library") ? 0 : modules.size()-1, fullModuleName);
          parentModel.setModules(modules);

          MavenProjectUtils.writePomFile(parentModel, parentPom.getLocation().toOSString());

          // Update deps poms
          for(Button button : moduleNamePage.getDependants()) {
            if(button.getSelection()) {
              IFile dependantPom = project.getFolder((String) button.getData()).getFile("pom.xml");
              Model dependantModel = reader.read(new InputStreamReader(dependantPom.getContents()));

              // Create dependency
              Dependency dep = new Dependency();
              dep.setGroupId(parentModel.getGroupId());
              dep.setArtifactId(parentModel.getArtifactId() + "-" + fullModuleName);
              dep.setVersion(parentModel.getVersion());

              // Adds dependency with checks for duplicates
              MavenProjectUtils.addDependency(dependantModel, dep);
              MavenProjectUtils.writePomFile(dependantModel, dependantPom.getLocation().toOSString());
            }
          }

          project.refreshLocal(IResource.DEPTH_INFINITE, null);

          for(Button button : moduleNamePage.getDependencies()) {
            if(button.getSelection()) {
              IFile ownPom = project.getFolder(fullModuleName).getFile("pom.xml");
              Model ownModel = reader.read(new InputStreamReader(ownPom.getContents()));
              IFile dependencyPom = project.getFolder((String) button.getData()).getFile("pom.xml");
              Model dependencyModel = reader.read(new InputStreamReader(dependencyPom.getContents()));

              // Create dependency -- use utils to get self or parent group/version
              Dependency dep = new Dependency();
              dep.setGroupId(MavenProjectUtils.getGroupId(dependencyModel));
              dep.setArtifactId(MavenProjectUtils.getArtifactId(dependencyModel));
              dep.setVersion(MavenProjectUtils.getVersion(dependencyModel));

              // Adds dependency with checks for duplicates
              MavenProjectUtils.addDependency(ownModel, dep);
              MavenProjectUtils.writePomFile(ownModel, ownPom.getLocation().toOSString());
            }
          }

          project.refreshLocal(IResource.DEPTH_INFINITE, null);

          // Add to classpath.. let's do a backup before
          IJavaProject javaProject = JavaCore.create(parentPom.getProject());
          IClasspathEntry[] classpath = javaProject.getRawClasspath();
          IClasspathEntry[] extendedCP = new IClasspathEntry[classpath.length+2];

          // so we can place sources on top..
          boolean isAdded = false;
          for(int i = 0; i < classpath.length; i++) {
            IClasspathEntry cpEntry = classpath[i];
            if(!isAdded && cpEntry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
              extendedCP[i] = JavaCore.newSourceEntry(project.getFullPath().append(fullModuleName + "/src/main/java"));
              extendedCP[i+1] = JavaCore.newSourceEntry(project.getFullPath().append(fullModuleName + "/src/main/resources"));
              isAdded = true;
            }
            extendedCP[isAdded ? i+2 : i] = cpEntry;
          }

          // Let's try updating with mobicents:eclipse
          try {
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell()); 
            dialog.run(true, false, new IRunnableWithProgress(){ 
              public void run(IProgressMonitor monitor) { 
                monitor.beginTask("Updating classpath. This may take a few seconds ...", 100);
                mavenResult = null;
                mavenResult = MavenProjectUtils.runMavenTask(project.getFile("pom.xml"), new String[]{"mobicents:eclipse"}, monitor);
                monitor.done(); 
              } 
            });
          }
          catch (Exception e) {
            // ignore
          }
          // Fallback to manually created since mobicents:eclipse failed
          if(mavenResult == null || mavenResult.hasExceptions()) {
            javaProject.setRawClasspath(extendedCP, null);
          }

          project.refreshLocal(IResource.DEPTH_INFINITE, null);
        }
        else {
          MessageDialog.openError(new Shell(), "Error Adding Module", "A module with the chosen name already exists. Please change the name.");
        }
      }
      catch (Exception e) {
        MessageDialog.openError(new Shell(), "Error Adding Module", "Failure trying to add the new module, please refresh the project and try again.");
      }
      return !moduleExists;
    }

  }

  private class ModuleNamePage extends WizardPage {
    Text moduleName;
    ArrayList<Button> dependenciesButtons = new ArrayList<Button>();
    ArrayList<Button> dependantsButtons = new ArrayList<Button>();

    protected ModuleNamePage(String pageName) {
      super(pageName);
      setTitle("Add New " + moduleType.toUpperCase() + " Module");
      setDescription("Please enter the module name and select on which modules it will depend on (dependency)\n" +
      		"and which ones will depend on this new module (dependants)");
      initialize();
    }

    public String getModuleName() {
      return moduleName.getText();
    }

    public ArrayList<Button> getDependencies() {
      return dependenciesButtons;
    }

    public ArrayList<Button> getDependants() {
      return dependantsButtons;
    }

    public void createControl(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      composite.setLayout(layout);
      setControl(composite);
      new Label(composite, SWT.NONE).setText("New Module Name");
      moduleName = new Text(composite, SWT.BORDER | SWT.SINGLE);
      moduleName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.FILL_HORIZONTAL));
      moduleName.addVerifyListener(new VerifyListener() {
        public void verifyText(VerifyEvent event) {
          // Assume we don't allow it
          event.doit = true;

          // Get the character typed
          char myChar = event.character;
          String text = ((Text) event.widget).getText();

          // Do not allow ' ' if first character
          if (myChar == ' ' && text.length() == 0)
            event.doit = false;
        }});
      
      // Set the default name warning
      new Label(composite, SWT.NONE); // just a dumb one, to fill the blank
      Composite defNameWarnComp = new Composite(composite, SWT.NONE);
      GridLayout defNameWarnGrid = new GridLayout();
      defNameWarnGrid.numColumns = 2;
      defNameWarnComp.setLayout(defNameWarnGrid);
      new Label(defNameWarnComp, SWT.NONE).setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK));
      new Label(defNameWarnComp, SWT.NONE).setText("If a name is not provided for the module, \rthe default '" + moduleType +"' will be used.");

      try {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model parentModel = reader.read(new InputStreamReader(parentPom.getContents()));
  
        Composite otherComposite = new Composite(composite, SWT.NONE);
        GridLayout otherLayout = new GridLayout();
        otherLayout.numColumns = 3;
        otherComposite.setLayout(otherLayout);

        new Label(otherComposite, SWT.NONE).setText("Dependency");
        new Label(otherComposite, SWT.NONE).setText("Dependant");
        new Label(otherComposite, SWT.NONE).setText("");
  
        for(String module : parentModel.getModules()) {
          Button depButton = new Button(otherComposite, SWT.CHECK | SWT.BORDER | SWT.CENTER);
          depButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
          depButton.setText("");
          depButton.setData(module);
          if(module.equals("du")) {
            depButton.setEnabled(false);
          }
          dependenciesButtons.add(depButton);
          depButton = new Button(otherComposite, SWT.CHECK | SWT.BORDER | SWT.CENTER);
          depButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
          depButton.setText("");
          depButton.setData(module);
          if(module.equals("du")) {
            depButton.setSelection(true);
          }
          new Label(otherComposite, SWT.NONE).setText(module);
          dependantsButtons.add(depButton);
        }
      }
      catch (Exception e) {
        // Just don't show...
      }
    }
  }

}
