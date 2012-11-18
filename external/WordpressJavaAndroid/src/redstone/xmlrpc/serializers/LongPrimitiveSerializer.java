/*
    Copyright (c) 2005 Redstone Handelsbolag

    This library is free software; you can redistribute it and/or modify it under the terms
    of the GNU Lesser General Public License as published by the Free Software Foundation;
    either version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License along with this
    library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
    Boston, MA  02111-1307  USA
*/

package redstone.xmlrpc.serializers;

import java.io.IOException;
import java.io.Writer;
import redstone.xmlrpc.XmlRpcCustomSerializer;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 *  Serializes primitiv longs. Note that unless setUseApacheExtension( true )
 *  has been invoked, the longs are demoted to integers before being serialized
 *  into regular XML-RPC &lt;i4>'s, possibly losing significant bits in the
 *  conversion.
 * 
 *  @author Greger Olsson
 */

public class LongPrimitiveSerializer implements XmlRpcCustomSerializer
{
    /*  (Documentation inherited)
     *  @see redstone.xmlrpc.XmlRpcCustomSerializer#getSupportedClass()
     */
    
    public Class getSupportedClass()
    {
        return long.class;
    }


    /**
     *  Sets whether or not to use the &lt;i8> Apache extensions when
     *  serializing longs.
     *
     *  @param useApacheExtension Flag for specifying the Apache extension to be used.
     */

    public void setUseApacheExtension( boolean useApacheExtension )
    {
        this.useApacheExtension = useApacheExtension;
    }

    
    /*  (Documentation inherited)
     *  @see redstone.xmlrpc.XmlRpcCustomSerializer#serialize(java.lang.Object, java.io.Writer, redstone.xmlrpc.XmlRpcSerializer)
     */
    
    public void serialize(
        Object value,
        Writer writer,
        XmlRpcSerializer builtInSerializer )
        throws XmlRpcException, IOException
    {
        Long longValue = ( Long ) value;

        if ( !useApacheExtension )
        {
            writer.write( "<i4>" );
            writer.write( Integer.toString( longValue.intValue() ) );
            writer.write( "</i4>" );
        }
        else
        {
            writer.write( "<value><i8 xmlns=\"http://ws.apache.org/xmlrpc/namespaces/extensions\">" );
            writer.write( Long.toString( longValue.longValue() ) );
            writer.write( "</i8></value>" );
        }
    }
    
    
    /** Flag indicating whether or not the Apache &lt;i8> extension should be used. */
    private boolean useApacheExtension;
}