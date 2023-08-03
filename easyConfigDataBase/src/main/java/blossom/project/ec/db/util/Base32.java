package blossom.project.ec.db.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Base32 {
    private static byte BitsInBlock = 5;
    private static byte BitsInByte = 8;
    private static char[] Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private static char Padding = '=';

    public Base32() {
    }

    public static String Encode(byte[] input) {
        if (input.length == 0) {
            return "";
        } else {
            BigDecimal tmp = BigDecimal.valueOf((long)input.length).divide(BigDecimal.valueOf((long)BitsInBlock));
            int tmp2 = tmp.setScale(0, RoundingMode.CEILING).intValue() * BitsInByte;
            char[] output = new char[tmp2 * BitsInByte];
            int position = 0;
            byte workingByte = 0;
            byte remainingBits = BitsInBlock;
            byte[] var7 = input;
            int var8 = input.length;

            for(int var9 = 0; var9 < var8; ++var9) {
                byte currentByte = var7[var9];
                workingByte = (byte)(workingByte | currentByte >> BitsInByte - remainingBits);
                output[position++] = Alphabet[workingByte];
                if (remainingBits < BitsInByte - BitsInBlock) {
                    workingByte = (byte)(currentByte >> BitsInByte - BitsInBlock - remainingBits & 31);
                    output[position++] = Alphabet[workingByte];
                    remainingBits += BitsInBlock;
                }

                remainingBits = (byte)(remainingBits - (BitsInByte - BitsInBlock));
                workingByte = (byte)(currentByte << remainingBits & 31);
            }

            if (position != output.length) {
                output[position++] = Alphabet[workingByte];
            }

            while(position < output.length) {
                output[position++] = Padding;
            }

            return trimEnd(new String(output), '=');
        }
    }

    public static byte[] Decode(String input) {
        if (input != null && !input.equals("")) {
            input = trimEnd(input, Padding).toUpperCase();
            byte[] output = new byte[input.length() * BitsInBlock / BitsInByte];
            int position = 0;
            byte workingByte = 0;
            byte bitsRemaining = BitsInByte;
            char[] var5 = input.toCharArray();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                char currentChar = var5[var7];
                int currentCharPosition = -1;

                for(int i = 0; i < Alphabet.length; ++i) {
                    if (Alphabet[i] == currentChar) {
                        currentCharPosition = i;
                        break;
                    }
                }

                int mask;
                if (bitsRemaining > BitsInBlock) {
                    mask = currentCharPosition << bitsRemaining - BitsInBlock;
                    workingByte = (byte)(workingByte | mask);
                    bitsRemaining -= BitsInBlock;
                } else {
                    mask = currentCharPosition >> BitsInBlock - bitsRemaining;
                    workingByte = (byte)(workingByte | mask);
                    output[position++] = workingByte;
                    workingByte = (byte)(currentCharPosition << BitsInByte - BitsInBlock + bitsRemaining);
                    bitsRemaining = (byte)(bitsRemaining + (BitsInByte - BitsInBlock));
                }
            }

            return output;
        } else {
            return new byte[0];
        }
    }

    public static String trimEnd(String src, char sep) {
        if (src != null && !src.trim().isEmpty()) {
            int index;
            for(index = src.length() - 1; index >= 0 && src.charAt(index) == sep; --index) {
            }

            return index < 0 ? "" : src.substring(0, index + 1);
        } else {
            return src;
        }
    }
}
