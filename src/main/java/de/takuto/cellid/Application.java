package de.takuto.cellid;

import java.nio.ByteBuffer;
import java.util.HexFormat;

public class Application {

  private static final int EXIT_FAILURE = 1;

  /**
   * Decodes the bytes into something human-readable. Following format applies:
   * Source: <a href="https://www.3gpp.org/ftp/tsg_cn/TSG_CN/TSGN_07/Tdocs/PDF/NP-000138.pdf">3GPP</a>
   * <p>
   *  GlobalCellId ::= OCTET STRING (SIZE (5..7))
   *   -- Refers to Cell Global Identification defined in TS 3GPP TS 23.003 [17].
   *   -- The internal structure is defined as follows:
   *   -- octet 1 bits 4321 Mobile Country Code 1st digit
   *   -- bits 8765 Mobile Country Code 2nd digit
   *   -- octet 2 bits 4321 Mobile Country Code 3rd digit
   *   -- bits 8765 Mobile Network Code 3rd digit
   *   -- or filler (1111) for 2 digit MNCs
   *   -- octet 3 bits 4321 Mobile Network Code 1st digit
   *   -- bits 8765 Mobile Network Code 2nd digit
   *   -- octets 4 and 5 Location Area Code according to TS 3GPP TS 24.008
   *   -- octets 6 and 7 Cell Identity (CI) according to TS 3GPP TS 24.008
   * </p>
   */
  public static void main(final String[] args) {

    // Check if we have something to decode
    if (args.length != 1) {
      printUsageAndExit();
    }

    try {
      final var encoded = HexFormat.of().parseHex(
          args[0].replace(" ", "")
      );

      if (encoded.length < 7) {
        throw new NumberFormatException(
            String.format(
                "Hexadecimal value must contain 7 bytes! Got only %d from value: %s",
                encoded.length, HexFormat.of().formatHex(encoded)
            )
        );
      }

      final var mcc = decodeMcc(encoded[0], encoded[1]);
      final var mnc = decodeMnc(encoded[1], encoded[2]);
      final var lac = int32BytesToInt64(encoded[3], encoded[4]);
      final var cellId = int32BytesToInt64(encoded[5], encoded[6]);

      System.out.printf("MCC: %03d, MNC: %03d, LAC: %d, Cell-ID: %d%n", mcc, mnc, lac, cellId);
    } catch (final NumberFormatException e) { // Decoding of hex failed
      System.err.printf("Invalid argument: %s%n", e.getMessage());
      printUsageAndExit();
    }

  }

  private static void printUsageAndExit() {
    System.err.println("Usage: cellid-decode [cell-id]");
    System.exit(EXIT_FAILURE);
  }



  private static int int32BytesToInt64(final byte fourthOctet, final byte fifthOctet) {
    // Will probably break on 32 bit systems 🤔
    return ByteBuffer.wrap(new byte[]{0x0, 0x0, fourthOctet, fifthOctet}).getInt();
  }

  private static int decodeMcc(final byte firstOctet, final byte secondOctet) {

    final int mccFirstDigit = firstOctet & 0x0F;
    final int mccSecondDigit = (firstOctet & 0xF0) >> 4;
    final int mccThirdDigit =  secondOctet & 0x0F;

    return (mccFirstDigit * 100) + (mccSecondDigit * 10) + mccThirdDigit ;
  }

  private static int decodeMnc(final byte secondOctet, final byte thirdOctet) {
    final int mncFirstDigit = (thirdOctet & 0xF0) >> 4;
    final int mncSecondDigit = thirdOctet & 0x0F;
    final int mncThirdDigit = (secondOctet & 0xF0) >> 4;
    final boolean hasFiller = mncThirdDigit == 0xF;
    final boolean isSingleDigitMnc = (mncSecondDigit == 0x00 && hasFiller);
    final boolean isTwoDigitMnc = hasFiller && !isSingleDigitMnc;

    if (isSingleDigitMnc) {
      return mncFirstDigit;
    }

    if (isTwoDigitMnc) {
      return (mncFirstDigit * 10) + mncSecondDigit;
    }

    return (mncFirstDigit * 100) + (mncSecondDigit * 10) + mncThirdDigit;
  }

}