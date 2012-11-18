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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import redstone.xmlrpc.XmlRpcCustomSerializer;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 *  Serializes java.util.Maps into XML-RPC structs. For each value in the map
 *  it recursively calls the XmlRpcSerializer, which potentially ends up back
 *  in this class if a value in the Map is another Map. The key should be a String
 *  or something that properly implements toString().
 *
 *  @author Greger Olsson
 */

public class MapSerializer implements XmlRpcCustomSerializer
{
    /*  (Documentation inherited)
     *  @see redstone.xmlrpc.XmlRpcCustomSerializer#getSupportedClass()
     */
    
    public Class getSupportedClass()
    {
        return Map.class;
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
        writer.write( "<struct>" );

        Map map     = ( Map ) value;
        Set keys    = map.keySet();
        Iterator it = keys.iterator();

        while ( it.hasNext() )
        {
            Object key = it.next();

            writer.write( "<member><name>" );
            writer.write( key.toString() );
            writer.write( "</name>");

            // Reuse default serializing mechanism for each member.
            // If the member is another HashMap, this will result in
            // a recursive call to this method. If no serializer
            // supports the member type, an XmlRpcException will be thrown.

            builtInSerializer.serialize( map.get( key ), writer );

            writer.write( "</member>" );
        }

        writer.write( "</struct>" );
    }
}