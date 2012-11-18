/*
    Copyright (c) 2006 Redstone Handelsbolag

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

import java.text.SimpleDateFormat;
import java.text.ParseException;
import redstone.xmlrpc.util.Base64;

/**
 *  An XmlRpcValue wraps a value object that is initialized with data supplied from the
 *  XmlRpcParser according to a specific XML-RPC type. When parsing a message, the parser
 *  first determines the type of the value, and sets it accordingly. It then sets the actual
 *  value of the XmlRpcValue object. The XmlRpcValue object is responsible for handling the
 *  raw character data from the parser, according to the type of the value.
 *
 *  @author Greger Olsson
 */

public class XmlRpcValue
{
    /**
     *  Constructs an XmlRpcValue initially assumed to be a string.
     */

    XmlRpcValue()
    {
        type = XmlRpcParser.STRING;
    }


    /**
     *  Sets the type of the value. If the type is a composite type, a corresponding
     *  XmlRpcArray or XmlRpcStruct will be created to hold the nested values.
     */

    void setType( int type )
    {
        this.type = type;

        if ( type == XmlRpcParser.ARRAY )
        {
            value = new XmlRpcArray();
        }
        else if ( type == XmlRpcParser.STRUCT )
        {
            value = new XmlRpcStruct();
        }
    }


    /**
     *  Processes the character data supplied by the parser. Depending on the current type
     *  of the value, the data will be treated accordingly.
     *  
     *  @param charData the character data from the XML-RPC message.
     */

    void processCharacterData( String charData ) throws XmlRpcException
    {
        switch ( type )
        {
            case XmlRpcParser.STRING:

                value = charData;
                break;

            case XmlRpcParser.I4:
            case XmlRpcParser.INT:

                value = new Integer( charData );
                break;

            case XmlRpcParser.I8:
                
                value = new Long( charData );
                break;
                
            case XmlRpcParser.BOOLEAN:

                value = new Boolean( Integer.parseInt( charData ) == 1 );
                break;

            case XmlRpcParser.DOUBLE:

                value = new Double( charData );
                break;

            case XmlRpcParser.DATE:

                try
                {
                    // TODO Optimize.
                    synchronized( dateFormatter )
                    {
                        value = dateFormatter.parse( charData );
                    }
                }
                catch( ParseException e )
                {
                    throw new XmlRpcException(
                        XmlRpcMessages.getString( "XmlRpcValue.IllegalDate" ) + charData, e );
                }
                break;

            case XmlRpcParser.BASE64:

                value = Base64.decode( charData.getBytes() );
                break;

            case XmlRpcParser.STRUCT:

                memberName = charData;
                break;
        }
    }


    /**
     *  Adds a child value to this value, if it is of composite type (array or struct).
     *  
     *  @param value The nested valued of this value.
     */

    void addChildValue( XmlRpcValue childValue )
    {
        if ( type == XmlRpcParser.ARRAY )
        {
            ( ( XmlRpcArray ) value ).add( childValue.value );
        }
        else if ( type == XmlRpcParser.STRUCT )
        {
            ( ( XmlRpcStruct ) value ).put( memberName, childValue.value );
        }
        else
        {
            throw new XmlRpcException(
                XmlRpcMessages.getString( "XmlRpcValue.UnexpectedNestedValue" ) );
        }
    }


    /**
     *  @todo describe.
     */

    public int hashCode()
    {
        return type;
    }


    /** The encapsulated, interpreted value */
    Object value;

    /** The type of the value (see XmlRpcParser) */
    int type;

    /** If this is a struct value, this holds the member name */
    String memberName;

    /** Date formatter shared by all XmlRpcValues */
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat( "yyyyMMdd'T'HH:mm:ss" );
}