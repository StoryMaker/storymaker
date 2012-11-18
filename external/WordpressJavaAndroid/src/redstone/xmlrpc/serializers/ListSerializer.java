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
import java.util.List;
import redstone.xmlrpc.XmlRpcCustomSerializer;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 *  A custom serializer that support serialization of objects implementing
 *  the java.util.List interface. For ArrayLists, for instance, this may
 *  be more effective than the CollectionSerializer since this serializer
 *  uses random access rather that instantiating an iterator.
 *
 *  @author Greger Olsson
 */

public class ListSerializer implements XmlRpcCustomSerializer
{
    /*  (Documentation inherited)
     *  @see redstone.xmlrpc.XmlRpcCustomSerializer#getSupportedClass()
     */

    public Class getSupportedClass()
    {
        return List.class;
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
        writer.write( "<array><data>" );

        List list = ( List ) value;

        for ( int i = 0; i < list.size(); ++i )
        {
            builtInSerializer.serialize( list.get( i ), writer );
        }

        writer.write( "</data></array>" );
    }
}