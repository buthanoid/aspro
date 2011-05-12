/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: ColorPalette.java,v 1.2 2010-10-08 12:30:02 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2010/01/22 13:17:35  bourgesl
 * simple default color palette
 *
 ******************************************************************************/
package fr.jmmc.aspro.gui.util;

import java.awt.Color;
import org.jfree.chart.ChartColor;

/**
 * Very simple color palette
 * @author bourgesl
 */
public class ColorPalette {

  /** default colors */
  private final static Color[] DEFAULT_COLORS = {
    ChartColor.LIGHT_RED,
    ChartColor.LIGHT_BLUE,
    ChartColor.LIGHT_GREEN,
    ChartColor.LIGHT_YELLOW,
    ChartColor.LIGHT_MAGENTA,
    ChartColor.LIGHT_CYAN,
    ChartColor.VERY_LIGHT_RED,
    ChartColor.VERY_LIGHT_BLUE,
    ChartColor.VERY_LIGHT_GREEN,
    ChartColor.VERY_LIGHT_YELLOW,
    ChartColor.VERY_LIGHT_MAGENTA,
    ChartColor.VERY_LIGHT_CYAN,
    ChartColor.DARK_RED,
    ChartColor.DARK_BLUE,
    ChartColor.DARK_GREEN,
    ChartColor.DARK_YELLOW,
    ChartColor.DARK_MAGENTA,
    ChartColor.DARK_CYAN,
    ChartColor.VERY_DARK_RED,
    ChartColor.VERY_DARK_BLUE,
    ChartColor.VERY_DARK_GREEN,
    ChartColor.VERY_DARK_YELLOW,
    ChartColor.VERY_DARK_MAGENTA,
    ChartColor.VERY_DARK_CYAN
  };
  /** default color palette */
  private static ColorPalette DEFAULT_COLOR_PALETTE = new ColorPalette(DEFAULT_COLORS);

  /**
   * Return the default color palette
   * @return default color palette
   */
  public static ColorPalette getDefaultColorPalette() {
    return DEFAULT_COLOR_PALETTE;
  }

  /* members */
  /** palette colors */
  private Color[] colors;

  /**
   * Constructor
   * @param colors colors to use
   */
  public ColorPalette(final Color[] colors) {
    this.colors = colors;
  }

  /**
   * Return a Color for the given index
   * @param index
   * @return Color
   */
  public Color getColor(final int index) {
    return this.colors[index % this.colors.length];
  }
}
