/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.rest.api.cycle;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleDefaultMimeType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.impl.db.CycleServiceDbXStreamImpl;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiStreamingWebScript;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * @author Nils Preusker
 */
public class ContentGet extends ActivitiStreamingWebScript {

  // private CycleService cycleService;
  private RepositoryConnector repositoryConnector;

  private void init(ActivitiRequest req) {
    String cuid = req.getCurrentUserId();

    HttpSession session = req.getHttpServletRequest().getSession(true);
    // this.cycleService = SessionUtil.getCycleService();
    this.repositoryConnector = CycleServiceDbXStreamImpl.getRepositoryConnector(cuid, session);
  }

  @Override
  protected void executeStreamingWebScript(ActivitiRequest req, WebScriptResponse res) throws IOException {
    init(req);

    // Retrieve the artifactId from the request
    String artifactId = req.getMandatoryString("artifactId");
    String contentRepresentationId = req.getMandatoryString("contentRepresentationId");

    // Retrieve the artifact from the repository
    RepositoryArtifact artifact = repositoryConnector.getRepositoryArtifact(artifactId);

    ContentRepresentation contentRepresentation = artifact.getArtifactType().getContentRepresentation(contentRepresentationId);

    String contentType = contentRepresentation.getMimeType().getContentType();
    // assuming we want to create an attachment for binary data...
    boolean attach = contentType.startsWith("application/") ? true : false;

    // TODO: This code should become obsolete when the connectors store the file
    // names properly with suffix.
    String attachmentFileName = null;
    if (attach) {
      attachmentFileName = artifact.getMetadata().getName();

      if (contentType.equals(CycleDefaultMimeType.XML) && !attachmentFileName.endsWith(".xml")) {
        attachmentFileName += ".xml";
      } else if (contentType.equals(CycleDefaultMimeType.JSON) && !attachmentFileName.endsWith(".json")) {
        attachmentFileName += ".json";
      } else if (contentType.equals(CycleDefaultMimeType.TEXT) && !attachmentFileName.endsWith(".txt")) {
        attachmentFileName += ".txt";
      } else if (contentType.equals(CycleDefaultMimeType.PDF) && !attachmentFileName.endsWith(".pdf")) {
        attachmentFileName += ".pdf";
      } else if (contentType.equals(CycleDefaultMimeType.MS_EXCEL) && !attachmentFileName.endsWith(".xls")) {
        attachmentFileName += ".xls";
      } else if (contentType.equals(CycleDefaultMimeType.MS_POWERPOINT) && !attachmentFileName.endsWith(".ppt")) {
        attachmentFileName += ".ppt";
      } else if (contentType.equals(CycleDefaultMimeType.MS_WORD) && !attachmentFileName.endsWith(".doc")) {
        attachmentFileName += ".doc";
      }
    }

    // TODO: what is a good way to determine the etag? Using a fake one...
    streamResponse(res, repositoryConnector.getContent(artifact.getCurrentPath(), contentRepresentation.getId()).asInputStream(), new Date(0),
            "W/\"647-1281077702000\"", attach, attachmentFileName, contentType);

  }

}
