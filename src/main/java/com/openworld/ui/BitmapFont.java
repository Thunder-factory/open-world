package com.openworld.ui;

import com.openworld.render.Renderer;

public final class BitmapFont {

  /*
   * ============================================================
   * DIMENSIONES
   * ============================================================
   *
   * Cada carácter utiliza una matriz de:
   *
   * 5 columnas
   * 7 filas
   */

  private static final int CHARACTER_WIDTH = 5;

  private static final int CHARACTER_HEIGHT = 7;

  private static final int CHARACTER_SPACING = 1;

  private static final int LINE_SPACING = 2;

  private BitmapFont() {

  }

  /*
   * ============================================================
   * TEXTO
   * ============================================================
   */

  public static void drawText(
      Renderer renderer,
      String text,

      float x,
      float y,

      float pixelSize,

      float red,
      float green,
      float blue) {

    if (text == null
        || text.isEmpty()
        || pixelSize <= 0.0f) {

      return;
    }

    float cursorX = x;

    float cursorY = y;

    float characterAdvance = (CHARACTER_WIDTH
        + CHARACTER_SPACING)
        * pixelSize;

    float lineAdvance = (CHARACTER_HEIGHT
        + LINE_SPACING)
        * pixelSize;

    for (int i = 0; i < text.length(); i++) {

      char character = text.charAt(
          i);

      /*
       * Se permite utilizar varias líneas
       * con el mismo método.
       */
      if (character == '\n') {

        cursorX = x;

        cursorY += lineAdvance;

        continue;
      }

      drawCharacter(
          renderer,
          character,

          cursorX,
          cursorY,

          pixelSize,

          red,
          green,
          blue);

      cursorX += characterAdvance;
    }
  }

  public static float getTextWidth(
      String text,
      float pixelSize) {

    if (text == null
        || text.isEmpty()
        || pixelSize <= 0.0f) {

      return 0.0f;
    }

    float charactersWidth = text.length()
        * CHARACTER_WIDTH * pixelSize;
    float spacingWidth = Math.max(0, text.length() - 1)
        * CHARACTER_SPACING * pixelSize;
    return charactersWidth + spacingWidth;
  }

  public static float getTextHeight(
      float pixelSize) {

    if (pixelSize <= 0.0f) {
      return 0.0f;
    }

    return CHARACTER_HEIGHT * pixelSize;
  }

  /*
   * ============================================================
   * CARÁCTER
   * ============================================================
   */

  private static void drawCharacter(
      Renderer renderer,
      char character,

      float x,
      float y,

      float pixelSize,

      float red,
      float green,
      float blue) {

    /*
     * Los textos de la interfaz utilizan
     * únicamente mayúsculas.
     */
    char normalizedCharacter = Character.toUpperCase(
        character);

    int[] pattern = getPattern(
        normalizedCharacter);

    for (int row = 0; row < CHARACTER_HEIGHT; row++) {

      int rowBits = pattern[row];

      for (int column = 0; column < CHARACTER_WIDTH; column++) {

        /*
         * La columna izquierda corresponde
         * al bit más significativo.
         */
        int mask = 1 << (CHARACTER_WIDTH
            - 1
            - column);

        if ((rowBits & mask) == 0) {

          continue;
        }

        renderer.hudRect(

            x
                + column
                    * pixelSize,

            y
                + row
                    * pixelSize,

            pixelSize,
            pixelSize,

            red,
            green,
            blue);
      }
    }
  }

  /*
   * ============================================================
   * PATRONES
   * ============================================================
   *
   * Cada número representa una fila de cinco bits.
   *
   * Ejemplo:
   *
   * 01110
   * 10001
   * 10001
   * 11111
   * 10001
   * 10001
   * 10001
   *
   * representa la letra A.
   */

  private static int[] getPattern(
      char character) {

    return switch (character) {

      /*
       * ====================================================
       * LETRAS
       * ====================================================
       */

      case 'A' -> new int[] {
          0b01110,
          0b10001,
          0b10001,
          0b11111,
          0b10001,
          0b10001,
          0b10001
      };

      case 'B' -> new int[] {
          0b11110,
          0b10001,
          0b10001,
          0b11110,
          0b10001,
          0b10001,
          0b11110
      };

      case 'C' -> new int[] {
          0b01111,
          0b10000,
          0b10000,
          0b10000,
          0b10000,
          0b10000,
          0b01111
      };

      case 'D' -> new int[] {
          0b11110,
          0b10001,
          0b10001,
          0b10001,
          0b10001,
          0b10001,
          0b11110
      };

      case 'E' -> new int[] {
          0b11111,
          0b10000,
          0b10000,
          0b11110,
          0b10000,
          0b10000,
          0b11111
      };

      case 'F' -> new int[] {
          0b11111,
          0b10000,
          0b10000,
          0b11110,
          0b10000,
          0b10000,
          0b10000
      };

      case 'G' -> new int[] {
          0b01111,
          0b10000,
          0b10000,
          0b10111,
          0b10001,
          0b10001,
          0b01110
      };

      case 'H' -> new int[] {
          0b10001,
          0b10001,
          0b10001,
          0b11111,
          0b10001,
          0b10001,
          0b10001
      };

      case 'I' -> new int[] {
          0b11111,
          0b00100,
          0b00100,
          0b00100,
          0b00100,
          0b00100,
          0b11111
      };

      case 'J' -> new int[] {
          0b00111,
          0b00010,
          0b00010,
          0b00010,
          0b00010,
          0b10010,
          0b01100
      };

      case 'K' -> new int[] {
          0b10001,
          0b10010,
          0b10100,
          0b11000,
          0b10100,
          0b10010,
          0b10001
      };

      case 'L' -> new int[] {
          0b10000,
          0b10000,
          0b10000,
          0b10000,
          0b10000,
          0b10000,
          0b11111
      };

      case 'M' -> new int[] {
          0b10001,
          0b11011,
          0b10101,
          0b10101,
          0b10001,
          0b10001,
          0b10001
      };

      case 'N' -> new int[] {
          0b10001,
          0b11001,
          0b10101,
          0b10011,
          0b10001,
          0b10001,
          0b10001
      };

      case 'O' -> new int[] {
          0b01110,
          0b10001,
          0b10001,
          0b10001,
          0b10001,
          0b10001,
          0b01110
      };

      case 'P' -> new int[] {
          0b11110,
          0b10001,
          0b10001,
          0b11110,
          0b10000,
          0b10000,
          0b10000
      };

      case 'Q' -> new int[] {
          0b01110,
          0b10001,
          0b10001,
          0b10001,
          0b10101,
          0b10010,
          0b01101
      };

      case 'R' -> new int[] {
          0b11110,
          0b10001,
          0b10001,
          0b11110,
          0b10100,
          0b10010,
          0b10001
      };

      case 'S' -> new int[] {
          0b01111,
          0b10000,
          0b10000,
          0b01110,
          0b00001,
          0b00001,
          0b11110
      };

      case 'T' -> new int[] {
          0b11111,
          0b00100,
          0b00100,
          0b00100,
          0b00100,
          0b00100,
          0b00100
      };

      case 'U' -> new int[] {
          0b10001,
          0b10001,
          0b10001,
          0b10001,
          0b10001,
          0b10001,
          0b01110
      };

      case 'V' -> new int[] {
          0b10001,
          0b10001,
          0b10001,
          0b10001,
          0b10001,
          0b01010,
          0b00100
      };

      case 'W' -> new int[] {
          0b10001,
          0b10001,
          0b10001,
          0b10101,
          0b10101,
          0b10101,
          0b01010
      };

      case 'X' -> new int[] {
          0b10001,
          0b10001,
          0b01010,
          0b00100,
          0b01010,
          0b10001,
          0b10001
      };

      case 'Y' -> new int[] {
          0b10001,
          0b10001,
          0b01010,
          0b00100,
          0b00100,
          0b00100,
          0b00100
      };

      case 'Z' -> new int[] {
          0b11111,
          0b00001,
          0b00010,
          0b00100,
          0b01000,
          0b10000,
          0b11111
      };

      /*
       * ====================================================
       * NÚMEROS
       * ====================================================
       */

      case '0' -> new int[] {
          0b01110,
          0b10001,
          0b10011,
          0b10101,
          0b11001,
          0b10001,
          0b01110
      };

      case '1' -> new int[] {
          0b00100,
          0b01100,
          0b00100,
          0b00100,
          0b00100,
          0b00100,
          0b01110
      };

      case '2' -> new int[] {
          0b01110,
          0b10001,
          0b00001,
          0b00010,
          0b00100,
          0b01000,
          0b11111
      };

      case '3' -> new int[] {
          0b11110,
          0b00001,
          0b00001,
          0b01110,
          0b00001,
          0b00001,
          0b11110
      };

      case '4' -> new int[] {
          0b00010,
          0b00110,
          0b01010,
          0b10010,
          0b11111,
          0b00010,
          0b00010
      };

      case '5' -> new int[] {
          0b11111,
          0b10000,
          0b10000,
          0b11110,
          0b00001,
          0b00001,
          0b11110
      };

      case '6' -> new int[] {
          0b01110,
          0b10000,
          0b10000,
          0b11110,
          0b10001,
          0b10001,
          0b01110
      };

      case '7' -> new int[] {
          0b11111,
          0b00001,
          0b00010,
          0b00100,
          0b01000,
          0b01000,
          0b01000
      };

      case '8' -> new int[] {
          0b01110,
          0b10001,
          0b10001,
          0b01110,
          0b10001,
          0b10001,
          0b01110
      };

      case '9' -> new int[] {
          0b01110,
          0b10001,
          0b10001,
          0b01111,
          0b00001,
          0b00001,
          0b01110
      };

      /*
       * ====================================================
       * SÍMBOLOS
       * ====================================================
       */

      case ':' -> new int[] {
          0b00000,
          0b00100,
          0b00100,
          0b00000,
          0b00100,
          0b00100,
          0b00000
      };

      case '.' -> new int[] {
          0b00000,
          0b00000,
          0b00000,
          0b00000,
          0b00000,
          0b00100,
          0b00100
      };

      case '-' -> new int[] {
          0b00000,
          0b00000,
          0b00000,
          0b11111,
          0b00000,
          0b00000,
          0b00000
      };

      case '/' -> new int[] {
          0b00001,
          0b00010,
          0b00010,
          0b00100,
          0b01000,
          0b01000,
          0b10000
      };

      case ' ' -> emptyPattern();

      /*
       * Un carácter no soportado se representa
       * mediante un pequeño cuadro.
       */
      default -> new int[] {
          0b11111,
          0b10001,
          0b10001,
          0b10001,
          0b10001,
          0b10001,
          0b11111
      };
    };
  }

  private static int[] emptyPattern() {

    return new int[] {
        0,
        0,
        0,
        0,
        0,
        0,
        0
    };
  }
}
