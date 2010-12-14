package org.activiti.cycle;

import java.io.Serializable;

import org.activiti.cycle.impl.mimetype.Mimetypes;

/**
 * TODO: javadoc
 * 
 * @author bernd.ruecker@camunda.com
 * @author daniel.meyer@camunda.com
 */
public interface ContentRepresentation extends Serializable {

  /**
   * Name of this representation, serves as a <b>unique key</b> to query the
   * correct representation and may be used by the client to show a list of
   * possible {@link ContentRepresentationDefinition}s
   */
  public String getId();

  /**
   * The renderInfo property is used by the user interface to determine how to
   * render a content-representation, the supported formats are defined in
   * {@link RenderInfo}.
   */
  public RenderInfo getRenderInfo();

  /**
   * Returns the content provided by this {@link ContentRepresentation}
   */
  public Content getContent(RepositoryArtifact artifact);

  /**
   * The {@link MimeType} of the returned content. Note: this is not necesarily
   * the same mimetype as {@link #getRepositoryArtifactType().getMimeType()}.
   */
  public MimeType getRepresentationMimeType();

  /**
   * The {@link RepositoryArtifactType} of the artifacts this
   * {@link ContentRepresentation} can provide content for. Note: this is not
   * the type of the returned representation.
   */
  public RepositoryArtifactType getRepositoryArtifactType();

  // TODO: Think about that, maybe as annotation in the Plugin-Config
  // public boolean isDownloadable();
}