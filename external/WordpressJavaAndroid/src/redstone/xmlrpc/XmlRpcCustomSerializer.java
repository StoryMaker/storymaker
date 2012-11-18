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

package redstone.xmlrpc;

import java.io.IOException;
import java.io.Writer;

/**
 *  Java objects are serialized into XML-RPC values using instances of classes
 *  implementing the XmlRpcCustomSerializer class. When processing an argument or a return
 *  value for an XML-RPC call, the XmlRpcSerializer will look through its list of
 *  XmlRpcCustomSerializer objects for a serializer that matches the object type of the
 *  value. The getValueClass() returns the Class supported by that serializer.
 *
 *  <p>A number of serializers for common types are already implemented, but you may
 *  wish to add custom serializers for special types of Java objects.</p>
 *
 *  @author Greger Olsson
 */

public interface XmlRpcCustomSerializer
{
    /**
     *  Returns the class of objects this serializer knows how to handle.
     *
     *  @return The class of objects interpretable to this serializer.
     */

    Class getSupportedClass();


    /**
     *  Asks the custom serializer to serialize the supplied value into the supplied
     *  writer. The supplied value will be of the type reported in getSupportedClass()
     *  or of a type extending therefrom.
     *
     *  @param value The object to serialize.
     *
     *  @param output The writer to place the serialized data.
     *  
     *  @param builtInSerializer The built-in serializer used by the client or the server.
     *
     *  @throws XmlRpcException if the value somehow could not be serialized. Many serializers
     *                          rely on the built in serializer which also may throw this
     *                          exception.
     * 
     *  @throws IOException if there was an error serializing the value through the
     *                       writer. The exception is the exception thrown by the
     *                       writer, which in most cases will be a StringWriter, in which
     *                       case this exception will never occurr. XmlRpcSerializer and
     *                       custom serializers may, however, be used outside of the
     *                       XML-RPC library to encode information in XML-RPC structs, in
     *                       which case the writer potentially could be writing the
     *                       information over a socket stream for instance.
     */

    void serialize(
        Object value,
        Writer output,
        XmlRpcSerializer builtInSerializer )
        throws XmlRpcException, IOException;
}