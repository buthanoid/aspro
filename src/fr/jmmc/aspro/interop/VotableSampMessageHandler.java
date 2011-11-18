/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.aspro.interop;

import fr.jmmc.jmcs.gui.MessagePane;
import fr.jmmc.jmcs.network.Http;
import fr.jmmc.jmcs.network.interop.SampCapability;
import fr.jmmc.jmcs.network.interop.SampManager;
import fr.jmmc.jmcs.network.interop.SampMessageHandler;
import fr.jmmc.jmcs.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.astrogrid.samp.Message;
import org.astrogrid.samp.Metadata;
import org.astrogrid.samp.client.SampException;

/**
 * This class handles the SAMP Message load votable:
 * - coming from SearchCal to extract calibrators
 * - coming from PIVOT to create one new observation with given targets
 * @author bourgesl
 */
public final class VotableSampMessageHandler extends SampMessageHandler {

  /** Class logger */
  private static final Logger logger = Logger.getLogger(VotableSampMessageHandler.class.getName());
  /** flag to dump votable into logs */
  private static final boolean DUMP_VOTABLE = true;

  /**
   * Public constructor
   */
  public VotableSampMessageHandler() {
    super(SampCapability.LOAD_VO_TABLE);
  }

  /**
   * Implements message processing
   *
   * @param senderId public ID of sender client
   * @param message message with MType this handler is subscribed to
   * @throws SampException if any error occured while message processing
   */
  @Override
  protected void processMessage(final String senderId, final Message message) throws SampException {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("\tReceived '" + this.handledMType() + "' message from '" + senderId + "' : '" + message + "'.");
    }

    // get url of votable (locally stored) :
    final String voTableURL = (String) message.getRequiredParam("url");

    logger.info("processMessage: VOTable URL = " + voTableURL);

    if (voTableURL == null) {
      throw new SampException("Can not get the url of the votable");
    }

    final URI voTableURI;
    try {
      voTableURI = new URI(voTableURL);
    } catch (URISyntaxException use) {
      logger.log(Level.SEVERE, "invalid URI", use);

      throw new SampException("Can not read the votable : " + voTableURL, use);
    }
    
    File voTableFile = null;

    try {
      final String scheme = voTableURI.getScheme();

      if (scheme.equalsIgnoreCase("file")) {
        voTableFile = new File(voTableURI);

      } else if (scheme.equalsIgnoreCase("http")) {
        final File file = FileUtils.getTempFile("votable-", ".vot");

        if (Http.download(voTableURI, file, true)) {
          voTableFile = file;
        }
      }

      if (voTableFile == null) {
        throw new SampException("Not supported URI scheme : " + voTableURL);
      }

      final String votable = FileUtils.readFile(voTableFile);

      if (DUMP_VOTABLE) {
        logger.info("votable :\n" + votable);
      }

      final Metadata senderMetadata = SampManager.getMetaData(senderId);
      final String searchCalVersion = senderMetadata.getString("searchcal.version");

      if (searchCalVersion == null) {
        AnyVOTableHandler.processMessage(votable);

      } else {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("SearchCal version = " + searchCalVersion);
        }
        SearchCalVOTableHandler.processMessage(votable, searchCalVersion);
      }

    } catch (IOException ioe) {
      MessagePane.showErrorMessage("Can not read the votable :\n\n" + voTableURL);

      throw new SampException("Can not read the votable : " + voTableURL, ioe);
    }
  }
}
