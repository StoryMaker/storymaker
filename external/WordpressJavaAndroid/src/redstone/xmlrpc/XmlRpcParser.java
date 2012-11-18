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

import java.io.InputStream;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *  An XmlRpcParser converts inbound XML-RPC messages to their Java counterparts through
 *  the use of a SAX compliant parser. This is an abstract class that is only concerned
 *  with the XML-RPC values contained in a message. Deriving classes supply a
 *  handleParsedValue() method that is called whenever an XML-RPC value has been parsed.
 *
 *  <p>If a class needs to be notified of additional parts of an XML-RPC message, the
 *  startElement() or endElement() methods are overridden and extended with checks for
 *  the appropriate element. This is the case with XmlRpcClient that wants to know if
 *  a fault element is present. Also, the XmlRpcServer wants to know the name of the
 *  method for which values are supplied.</p>
 *
 *  <p>Internally, the implementation uses pre-calculated hash values of the element names
 *  to allow for switch() constructs when comparing elements supplied by the SAX parser.</p>
 *
 *  @author Greger Olsson
 */

public abstract class XmlRpcParser extends DefaultHandler
{
    /** The hash value of value elements */
    public final static int VALUE = 111972721;

    /** The hash value of string elements */
    public final static int STRING = -891985903;

    /** The hash value of i4 elements */
    public final static int I4 = 3307;

    /** The hash value of i8, Apache elements */
    public final static int I8 = 3311;
    
    /** The hash value of int elements */
    public final static int INT = 104431;

    /** The hash value of boolean elements */
    public final static int BOOLEAN = 64711720;

    /** The hash value of double elements */
    public final static int DOUBLE = -1325958191;

    /** The hash value of double elements */
    public final static int DATE = -586971087;

    /** The hash value of double elements */
    public final static int BASE64 = -1396204209;

    /** The hash value of struct elements */
    public final static int STRUCT = -891974699;

    /** The hash value of array elements */
    public final static int ARRAY = 93090393;

    /** The hash value of member elements */
    public final static int MEMBER = -1077769574;

    /** The hash value of name elements */
    public final static int NAME = 3373707;


    /**
     *  Abstract method implemented by specialized message parsers like XmlRpcServer
     *  and XmlRpcClient. The method is called for every parsed top-level value.
     *  That is, it is called once for arrays and structs, and not once for every
     *  element.
     *
     *  @param obj The parsed value object. Can be String, Integer, Double, Boolean,
     *             Date, Vector, byte[], or Map objects.
     */

    protected abstract void handleParsedValue( Object obj );


    /**
     *  Parses the XML-RPC message contained in the supplied input stream. It does so
     *  by using the current SAX driver, and will call handleParsedValue() for every
     *  top-level value contained in the message. This method can be overridden to
     *  supply additional processing, like identifying method names and such. This
     *  implementation is only concerned with the values of the message.
     *
     *  @param is The input stream containing the XML-RPC message
     *
     *  @throws XmlRpcException If anything went wrong during the whole parsing phase
     */

    public void parse( InputStream is ) throws XmlRpcException
    {
        XMLReader reader = null;

        synchronized ( readers )
        {
            if ( readers.empty() )
            {
                try
                {
                    reader = XMLReaderFactory.createXMLReader();
                }
                catch ( SAXException e )
                {
                    throw new XmlRpcException(
                        XmlRpcMessages.getString( "XmlRpcParser.ReaderInstantiationError" ), e );
                }
            }
            else
            {
                reader = ( XMLReader ) readers.pop();
            }
        }

        reader.setContentHandler( this );

        try
        {
            try
            {
                reader.parse( new InputSource( is ) );
            }
            catch ( Exception e )
            {
                throw new XmlRpcException(
                    XmlRpcMessages.getString( "XmlRpcParser.ParsingError" ), e );                        
            }
        }
        finally
        {
            readers.push( reader );
        }
    }


    /**
     *  Called by SAX driver when a new element has been found in the message.
     *
     *  <p>This implementation uses a switch construct on the hash value of the element
     *  name. This increases readability, in my opinion, and perhaps performance
     *  as well (only one loop -- in hashCode() -- instead of in every equals() call).</p>
     *
     *  @param uri {@inheritDoc}
     *  @param name {@inheritDoc}
     *  @param qualifiedName {@inheritDoc}
     */

    public void startElement(
        String uri,
        String name,
        String qualifiedName,
        Attributes attributes )
        throws SAXException
    {
        int element = hashCode( name );

        switch( element )
        {
            case VALUE:

                if ( currentValue != null )
                {
                    values.push( currentValue );
                }

                currentValue = new XmlRpcValue();
                shallProcessCharData = true;

                break;

            case STRING:
            case I4:
            case I8:
            case INT:
            case BOOLEAN:
            case DOUBLE:
            case DATE:
            case BASE64:
            case ARRAY:
            case STRUCT:

                currentValue.setType( element );

            case NAME:

                shallProcessCharData = true;
        }
    }


    /**
     *  Called by SAX driver when a new end-element has been found in the message.<p>
     *
     *  This implementation determines if our current state is that we have a current
     *  value that needs to be processed, and if that value has some character data
     *  in the buffer required to finalize the value. The handleParsedValue() method
     *  is only called if a top-level value element has ended. If not, the value
     *  is added to the enclosing value, which may be an array or a struct.<p>
     *
     *  Note that struct values are processed when the member element ends and not
     *  when the value element ends. This is because we need to use the included
     *  member name.
     *
     *  @param uri {@inheritDoc}
     *  @param name {@inheritDoc}
     *  @param qualifiedName {@inheritDoc}
     */

    public void endElement(
        String uri,
        String name,
        String qualifiedName )
        throws SAXException
    {
        if ( currentValue != null && shallProcessCharData )
        {
            currentValue.processCharacterData( consumeCharData() );
        }
        else
        {
            charData.setLength( 0 );
        }

        switch( hashCode( name ) )
        {
            case VALUE:

                int depth = values.size();

                if ( depth == 0 )
                {
                    handleParsedValue( currentValue.value );
                    currentValue = null;
                }
                else if ( values.elementAt( depth - 1 ).hashCode() != STRUCT )
                {
                    XmlRpcValue v = currentValue;

                    currentValue = ( XmlRpcValue ) values.pop();
                    currentValue.addChildValue( v );
                }

                break;


            case MEMBER:

                XmlRpcValue v = currentValue;

                currentValue = ( XmlRpcValue ) values.pop();
                currentValue.addChildValue( v );
                break;
        }
    }


    /**
     *  Called by the SAX driver when character data is available.
     *
     *  <p>This implementation appends the data to an internal string buffer.
     *  The method is called for every element, wether characters are included
     *  int the element or not. This leads to the buffer being prepended with whitespace
     *  until actual character data is aquired. This is removed using the trim()
     *  method when the character data is consumed.</p>
     *
     *  @param data {@inheritDoc}
     *  @param start {@inheritDoc}
     *  @param length {@inheritDoc}
     */

    public void characters( char[] data, int start, int length )
    {
        charData.append( data, start, length );
    }


    /**
     *  Consumes the data in the internal string buffer. Whitespace is trimmed and the
     *  buffer is emptied.
     *
     *  @return The string representing the trimmed string value of the buffer.
     */

    protected String consumeCharData()
    {
        String data = charData.toString().trim();
        charData.setLength( 0 );
        shallProcessCharData = false;

        return data;
    }


    /**
     *  Internal hashcode algorithm. This algorithm is used instead
     *  of the build-in hashCode() method of java.lang.String to ensure
     *  that that the same hash value is calculated in all JVMs. The code
     *  in this class switches execution based on has values rather than
     *  using the String.equals() call for each element. Hash values for
     *  the XML-RPC elements have been pre-calculated and are represented
     *  by the hash constants at the top of this file.
     *  
     *  @param string The string to calculate a hash for.
     */

    private int hashCode( String string )
    {
        int hash = 0;
        int length = string.length();

        for ( int i = 0; i < length; ++i )
        {
            hash = 31 * hash + string.charAt( i );
        }

        return hash;
    }


    /** Our stack of values. May contain several levels depending on message complexity */
    private Stack values = new Stack();

    /** The current value (the currently enclosing <value> element) */
    private XmlRpcValue currentValue;

    /** Determines if we shall process the character data fed by the SAX driver */
    private boolean shallProcessCharData;

    /** The accumulated character data from the SAX driver. Is emptied when consumed */
    private StringBuffer charData = new StringBuffer( 128 );

    /** A cache of parsers so that we don't have to recreate them at every call. TODO Determine if necessary. */
    private static Stack/*<XMLReader>*/ readers = new Stack();
}
