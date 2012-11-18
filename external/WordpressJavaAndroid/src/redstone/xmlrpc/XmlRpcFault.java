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

/**
 *  Exception thrown by the XML-RPC library in case of a fault response. The
 *  exception is thrown only if the call was successfully made but the response
 *  contained a fault message. If a call could not be made due to a local
 *  problem (if an argument could not be serialized or if there was a network
 *  problem) an XmlRpcException is thrown instead.
 *
 *  @author  Greger Olsson
 */

public class XmlRpcFault extends Exception
{
    /**
     *  Creates a new exception with the supplied message and error code.
     *  The message and error code values are those returned from the remote
     *  XML-RPC service.
     *
     *  @param message The exception message.
     */

    public XmlRpcFault( int errorCode, String message )
    {
        super( message );
        this.errorCode = errorCode;
    }


    /**
     *  Returns the error code reported by the remote XML-RPC service.
     * 
     *  @return the error code reported by the XML-RPC service.
     */
    
    public int getErrorCode()
    {
        return errorCode;
    }

    
    /** The exception error code. See XML-RPC specification. */
    public final int errorCode;
    
    /** Serial version UID. */
    private static final long serialVersionUID = 3257566200450856503L;
}
