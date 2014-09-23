/*
 * TextEditingTargetCppHelper.java
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.workbench.views.source.editors.text;

import org.rstudio.studio.client.common.SimpleRequestCallback;
import org.rstudio.studio.client.common.filetypes.TextFileType;
import org.rstudio.studio.client.server.ServerError;
import org.rstudio.studio.client.server.ServerRequestCallback;
import org.rstudio.studio.client.server.VoidServerRequestCallback;
import org.rstudio.studio.client.workbench.views.buildtools.model.BuildServerOperations;
import org.rstudio.studio.client.workbench.views.source.editors.EditingTarget;
import org.rstudio.studio.client.workbench.views.source.model.CppCapabilities;
import org.rstudio.studio.client.workbench.views.source.model.CppServerOperations;

public class TextEditingTargetCppHelper
{
   public TextEditingTargetCppHelper(BuildServerOperations buildServer,
                                     CppServerOperations cppServer)
   {
      buildServer_ = buildServer;
      cppServer_ = cppServer;
   }
   
   public void checkBuildCppDependencies(
                     final EditingTarget editingTarget,
                     final WarningBarDisplay warningBar, 
                     TextFileType fileType)
   {
      // bail if this isn't a C file or we've already verified we can build
      if (!fileType.isC() || capabilities_.hasAllCapabiliites())
         return;
      
      buildServer_.getCppCapabilities(
                     new ServerRequestCallback<CppCapabilities>() {
         
         @Override
         public void onResponseReceived(CppCapabilities capabilities)
         {
            if (capabilities_.hasAllCapabiliites())
            {
               capabilities_ = capabilities;
            }
            else 
            {
               if (!capabilities.getCanBuild())
               {
                  warningBar.showWarningBar(
                     "The tools required to build C/C++ code for R " +
                     "are not currently installed");
                  
                  // do a prompted install of the build tools
                  buildServer_.installBuildTools(
                           "Compiling C/C++ code for R",
                           new SimpleRequestCallback<Boolean>() {
                              @Override
                              public void onResponseReceived(Boolean confirmed)
                              {
                                 if (confirmed)
                                    warningBar.hideWarningBar();
                              }
                           });
                  
               }
               else if (!capabilities.getCanSourceCpp())
               {
                  if (editingTarget.search("Rcpp\\:\\:export") != null)
                  {
                     warningBar.showWarningBar(
                        "The Rcpp package (version 0.10.1 or higher) is not " +
                        "currently installed");
                  }
               }
            }
         }
         
         @Override
         public void onError(ServerError error)
         {
            // ignore error since this is merely advisory
         }
      });
   }
      
   
   public void printCppCompletions(String docId,
                                   String docPath,
                                   String docContents,
                                   boolean docDirty,
                                   int line,
                                   int column)
   {
      cppServer_.printCppCompletions(docId, 
                                     docPath, 
                                     docContents, 
                                     docDirty,
                                     line, 
                                     column, 
                                     new VoidServerRequestCallback());
   }
  
   private final BuildServerOperations buildServer_;
   private final CppServerOperations cppServer_;
  
   
   // cache the value statically -- once we get an affirmative response
   // we never check again
   private static CppCapabilities capabilities_ 
                                    = CppCapabilities.createDefault();
}
