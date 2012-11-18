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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 *  <p>An Array represents an XML-RPC array in its Java form. Essentially,
 *  it's just a java.util.ArrayList with utility methods for extracting
 *  members of any of the data types supported by the XML-RPC library.
 *  The class does not introduce any new field members which makes it
 *  no more expensive than a regular ArrayList.</p>
 *
 *  <p>To extract nested values from the list, use the new simplified
 *  accessors which perform the casting for you:<p>
 *
 *  <pre>
 *  boolean boolean = myList.getStruct( 0 ).getBoolean( "someBoolean" );</pre>
 *  </pre>
 *  
 *  @author Greger Olsson
 */

public class XmlRpcArray extends ArrayList
{
    /**
     *  Returns the String at the given index in the array.
     *
     *  @param index The index into the array to extract the value from.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not a String.
     */

    public String getString( int index )
    {
        return ( String ) get( index );
    }


    /**
     *  Returns the boolean at the given index in the array.
     *
     *  @param index The index into the array to extract the value from.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not a Boolean.
     */

    public boolean getBoolean( int index )
    {
        return ( ( Boolean ) get( index ) ).booleanValue();
    }


    /**
     *  Returns the Boolean wrapper at the given index in the array.
     *
     *  @param index The index into the array to extract the value from.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not a Boolean.
     */

    public Boolean getBooleanWrapper( int index )
    {
        return ( Boolean ) get( index );
    }


    /**
     *  Returns the integer at the given index in the array.
     *
     *  @param index The index into the array to extract the value from.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not an Integer.
     */

    public int getInteger( int index )
    {
        return ( ( Integer ) get( index ) ).intValue();
    }


    /**
     *  Returns the Integer wrapper at the given index in the array.
     *
     *  @param index The index into the array to extract the value from.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not a Integer.
     */

    public Integer getIntegerWrapper( int index )
    {
        return ( Integer ) get( index );
    }


    /**
     *  Returns the Double at the given index in the array.
     *
     *  @param index The index into the array to extract the value from.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not a Double.
     */

    public double getDouble( int index )
    {
        return ( ( Double ) get( index ) ).doubleValue();
    }


    /**
     *  Returns the Double wrapper at the given index in the array.
     *
     *  @param index The index into the array to extract the value from.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not a Double.
     */

    public Double getDoubleWrapper( int index )
    {
        return ( Double ) get( index );
    }


    /**
     *  Returns the Array at the given index in the array.
     *
     *  @param index The index into the array to extract the value from.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not an Array.
     */

    public XmlRpcArray getArray( int index )
    {
        return ( XmlRpcArray ) get( index );
    }


    /**
     *  Returns the Struct at the given index in the array.
     *
     *  @param index The index into the array to extract the value from.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not a Struct.
     */

    public XmlRpcStruct getStruct( int index )
    {
        return ( XmlRpcStruct ) get( index );
    }


    /**
     *  Returns the Date at the given index in the array.
     *
     *  @param index The index into the array to extract the value from.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not a Date.
     */

    public Date getDate( int index )
    {
        return ( Date ) get( index );
    }


    /**
     *  Returns the byte[] at the given index in the array.
     *
     *  @param index The index into the array to extract the value from.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not a byte[].
     */

    public byte[] getBinary( int index )
    {
        return ( byte[] ) get( index );
    }


    /**
     *  Returns the byte[] with at the given index in the Array, as an
     *  input stream (currently, a java.io.ByteArrayInputStream).
     *
     *  @param index The index of the value to extract.
     *
     *  @throws ArrayOutOfBoundsException if index > size() - 1.
     *  @throws ClassCastException if the value at the given index is not a byte[].
     */

    public InputStream getBinaryAsStream( int index )
    {
        return new ByteArrayInputStream( ( byte[] ) get( index ) );
    }

    
    /** Serial version UID. */
    private static final long serialVersionUID = 3256446889107863860L;
}
