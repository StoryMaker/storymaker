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
 *  The exception thrown by the XML-RPC library in case of serialization or
 *  network problems. If a call is successfully made but the remote XML-RPC service
 *  returns a fault message, an XmlRpcFault exception will be thrown.
 *
 *  @author Greger Olsson
 */

public class XmlRpcException extends RuntimeException
{
    /**
     *  Creates a new exception with the supplied message.
     *
     *  @param message The exception message.
     */

    public XmlRpcException( String message )
    {
        super( message );
    }

    
    /**
     *  Creates a new exception with the supplied message.
     *  The supplied cause will be attached to the exception.
     *  
     *  @param message The error message.
     *  @param cause The original cause leading to the exception.
     */

    public XmlRpcException( String message, Throwable cause )
    {
        super( message, cause );
    }

    
    /** <describe> */
    private static final long serialVersionUID = 3257844394139596598L;
}