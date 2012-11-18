
package redstone.xmlrpc.util;

import redstone.xmlrpc.XmlRpcMessages;

/**
 *  Provides encoding of raw bytes to base64-encoded characters, and
 *  decoding of base64 characters to raw bytes. The original version
 *  was written by Kevin Kelly which has been updated to support
 *  newline characters based on from Object Refinery Limited and
 *  contributors.
 *
 *  @author Kevin Kelley (kelley@iguana.ruralnet.net)
 *  @author Object Refinery Limited and Contributors
 */

public class Base64
{
    /**
     *  Returns an array of base64-encoded characters to represent the
     *  passed data array.
     *
     *  @param data the array of bytes to encode
     *  @return base64-coded character array.
     */

    static public char[] encode( byte[] data )
    {
        char[] out = new char[ ( ( data.length + 2 ) / 3 ) * 4 ];

        // 3 bytes encode to 4 chars.  Output is always an even
        // multiple of 4 characters.

        for ( int i = 0, index = 0; i < data.length; i += 3, index += 4 )
        {
            boolean quad = false;
            boolean trip = false;
            int val = (0xFF & data[ i ] );

            val <<= 8;

            if ( ( i + 1 ) < data.length )
            {
                val |= ( 0xFF & data[ i + 1 ] );
                trip = true;
            }

            val <<= 8;

            if ( ( i + 2 ) < data.length )
            {
                val |= ( 0xFF & data[ i + 2 ] );
                quad = true;
            }

            out[ index + 3 ] = alphabet[ ( quad ? ( val & 0x3F ) : 64 ) ];
            val >>= 6;
            out[ index + 2 ] = alphabet[ ( trip ? ( val & 0x3F ) : 64 ) ];
            val >>= 6;
            out[ index + 1 ] = alphabet[ val & 0x3F ];
            val >>= 6;
            out[ index ] = alphabet[ val & 0x3F ];
        }

        return out;
    }


    /**
     *  Returns an array of bytes which were encoded in the passed
     *  character array.
     *
     *  @param data the array of base64-encoded characters which can
     *              contain whitespace, padding, and invalid characters
     *              which are stripped from the input.
     *              
     *  @return decoded data array
     */

    static public byte[] decode( byte[] data )
    {
        // Calculate actual length of the data, filtering away any
        // non-BASE64 characters.
        
        int tempLen = data.length;

        for ( int i = 0; i < data.length; ++i )
        {
            if ( ( data[ i ] > 255) || codes[ data[ i ] ] < 0 )
            {
                --tempLen;
            }
        }
        
        // Calculate required length of byte array:
        //  -- 3 bytes for every 4 valid base64 chars
        //  -- plus 2 bytes if there are 3 extra base64 chars,
        //     or plus 1 byte if there are 2 extra.

        int len = ( tempLen / 4 ) * 3;
        
        if ( ( tempLen % 4 ) == 3 )
        {
            len += 2;
        }
        
        if ( ( tempLen % 4 ) == 2 )
        {
            len += 1;
        }

        final byte[] out = new byte[ len ];
        int shift = 0;
        int accum = 0;
        int index = 0;

        for ( int i = 0; i < data.length; ++i )
        {
            final int value = ( data[ i ] > 255 ) ? -1 : codes[ data[ i ] ];

            // Skip over non-code
            
            if ( value >= 0 )
            {
                accum <<= 6;    // bits shift up by 6 each time thru
                shift += 6;     // loop, with new bits being put in
                accum |= value; // at the bottom.
                
                // Whenever there are 8 or more shifted in, write them out from
                // the top, leaving any excess at the bottom for next iteration.
                
                if ( shift >= 8 )
                {
                    shift -= 8;
                    out[index++] = ( byte ) ( ( accum >> shift ) & 0xff );
                }
            }
        }

        if ( index != out.length )
        {
            throw new RuntimeException( XmlRpcMessages.getString( "Base64.InvalidDataLength" ) );
        }

        return out;
    }


    /** Code characters for values 0..63 */
    static private char[] alphabet =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();

    /** Lookup table for converting base64 characters to value in range 0..63 */
    static private byte[] codes = new byte[256];

    /** Initialize look-up table */
    static
    {
        for ( int i = 0; i < 256; i++ )
        {
            codes[ i ] = -1;
        }

        for ( int i = 'A'; i <= 'Z'; i++ )
        {
            codes[ i ] = ( byte )( i - 'A' );
        }

        for ( int i = 'a'; i <= 'z'; i++ )
        {
            codes[ i ] = ( byte )( 26 + i - 'a' );
        }

        for ( int i = '0'; i <= '9'; i++ )
        {
            codes[ i ] = ( byte )( 52 + i - '0' );
        }

        codes[ '+' ] = 62;
        codes[ '/' ] = 63;
    }
}