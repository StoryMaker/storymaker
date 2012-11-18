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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *  The XmlRpcJsonSerializer class converts Java objects to their JSON counterparts.
 *  It inherently supports basic object types like String, Integer, Double, Float, Boolean,
 *  Date, and byte arrays. For other types of objects, custom serializers need to be
 *  registered. The Redstone XML-RPC library comes with a set of useful serializers for
 *  collections and other types of objects. @see the redstone.xmlrpc.serializers.json .
 *  
 *  Although this is not what you would expect to find in an XML-RPC library, implementing
 *  a JSON serializer required very little effort and gives great value for JavaScript
 *  clients that wants to use XML-RPC services in their AJAX implementations. It is easy
 *  to create XML-RPC compatible messages in JavaScript but less easy to parse the response
 *  which is not required using this format, just use eval( responseText ) to get a
 *  JavaScript object.
 *  
 *  TODO Change synchronization of global dateFormatter to prevent bottleneck.
 *
 *  @author  Greger Olsson
 */

public class XmlRpcJsonSerializer extends XmlRpcSerializer
{
    /**
     *  Constructor adding all core custom serializers.
     */

    public XmlRpcJsonSerializer()
    {
        this( true );
    }    
    

    /**
     *  Constructor that may add all the custom serializers in the library
     *  (which is almost always what you want).
     *  
     *  @param addCustomSerializers Indicates if the core custom serializers should be added.
     */

    public XmlRpcJsonSerializer( boolean addCustomSerializers )
    {
        super( false );

        if ( addCustomSerializers )
        {
            customSerializers.add( new redstone.xmlrpc.serializers.json.MapSerializer() );
            customSerializers.add( new redstone.xmlrpc.serializers.json.ListSerializer() );
            customSerializers.add( new redstone.xmlrpc.serializers.json.CollectionSerializer() );
            customSerializers.add( new redstone.xmlrpc.serializers.json.ObjectArraySerializer() );
            customSerializers.add( new redstone.xmlrpc.serializers.json.IntArraySerializer() );
            customSerializers.add( new redstone.xmlrpc.serializers.json.FloatArraySerializer() );
            customSerializers.add( new redstone.xmlrpc.serializers.json.DoubleArraySerializer() );
            customSerializers.add( new redstone.xmlrpc.serializers.json.BooleanArraySerializer() );
        }
    }
    

    /**
     *  Overrides the default serializing mechanism to use JSON format instead.
     */
    
    public void writeEnvelopeHeader( Object value, Writer writer ) throws IOException
    {
        writer.write( '(' );
    }
    
    
    /**
     *  Overrides the default serializing mechanism to use JSON format instead.
     */
    
    public void writeEnvelopeFooter( Object value, Writer writer ) throws IOException
    {
        writer.write( ')' );
    }

    
    /**
     *  Overrides the default serializing mechanism to use JSON format instead.
     */
    
    public void writeError( String message, Writer writer ) throws IOException
    {
        writer.write( '\'' );
        writer.write( message );
        writer.write( '\'' );
    }
    

    /**
     *  Overrides the default serializing mechanism to use JSON format instead.
     */

    public void serialize(
        Object value,
        Writer writer )
        throws XmlRpcException, IOException
    {
        if ( value instanceof String || value instanceof Character )
        {
            writer.write( '\'' );
            writer.write( value.toString() );
            writer.write( '\'' );
        }
        else if ( value instanceof Number || value instanceof Boolean )
        {
            writer.write( value.toString() );
        }
        else if ( value instanceof java.util.Calendar )
        {
            writer.write( "new Date('" );
            synchronized( dateFormatter )
            {
                writer.write( dateFormatter.format( ( ( Calendar ) value ).getTime() ) );
            }
            writer.write( "')" );
        }
        else if ( value instanceof java.util.Date )
        {
            writer.write( "new Date('" );
            synchronized( dateFormatter )
            {
                writer.write( dateFormatter.format( ( Date ) value ) );
            }
            writer.write( "')" );
        }
        else
        {
            // Value was not of basic type, see if there's a custom serializer
            // registered for it.

            for ( int i = 0; i < customSerializers.size(); ++i )
            {
                XmlRpcCustomSerializer serializer = ( XmlRpcCustomSerializer ) customSerializers.get( i );

                if ( serializer.getSupportedClass().isInstance( value ) )
                {
                    serializer.serialize( value, writer, this );
                    return;
                }
            }

            throw new XmlRpcException(
                XmlRpcMessages.getString( "XmlRpcSerializer.UnsupportedType" ) + value.getClass() );
        }
    }
    
    /** Shared date formatter shared. */
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat( "yyyy-dd-mm HH:mm:ss" );
}
