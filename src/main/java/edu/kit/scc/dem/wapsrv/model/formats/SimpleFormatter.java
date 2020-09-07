package edu.kit.scc.dem.wapsrv.model.formats;

import edu.kit.scc.dem.wapsrv.model.FormattableObject;
import edu.kit.scc.dem.wapsrv.model.FormattableObject.Type;

/**
 * This class implements a simple formatter that needs no parsing of additional
 * information in Accept or Content-Type headers and is completely clear with
 * just format and format string info
 *
 * @author Matthias Dressel
 * @author Michael Hitzker
 * @author Markus Hoefler
 * @author Andreas Loeffler
 * @author Timo Schmidt
 * @version 1.1
 */
public class SimpleFormatter extends AbstractFormatter{

  /**
   * The string representing the format
   */
  private final String formatString;

  /**
   * Creates a new simple formatter for the given format and using the given
   * formatString
   *
   * @param format The format implemented
   * @param formatString The format string representing the format
   */
  public SimpleFormatter(Format format, String formatString){
    super(format);
    this.formatString = formatString;
  }

  @Override
  public String getFormatString(){
    return formatString;
  }

  @Override
  public String format(FormattableObject obj){
    return obj.toString(getFormat());
  }

  @Override
  public void setAcceptPart(String acceptPart, Type type){
    // we do not use acceptPart in SimpleFormatters, no matter of type
    setValid(true);
  }

  @Override
  public String getContentType(){
    // Simple formatters do not have additional info to display
    return formatString + ";charset=utf-8";
  }
}
