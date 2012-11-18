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
 *  Callback interface to implement when using asynchronous invocations with
 *  XmlRpcClient. The XmlRpcClient will call either onResult(), onFault(), or
 *  onException() based on the outcome of the invocation.
 *
 *  @author Greger Olsson
 */

public interface XmlRpcCallback
{
    /**
     *  Called by the XmlRpcClient when a response was received from the server.
     *
     *  @param result The object containing the result value.
     */

    public void onResult( Object result );


    /**
     *  Called by the XmlRpcClient when a fault response was received from the server.
     *
     *  @param faultCode The error code reported by the XML-RPC service.
     *
     *  @param faultMessage The error message reported by the XML-RPC service.
     */

    public void onFault( int faultCode, String faultMessage );


    /**
     *  Called by the XmlRpcClient when an exception was raised during the call. This
     *  only includes exceptions occurring locally. Remote exceptions are transported as
     *  XML-RPC faults and are reported through the onFault() callback.
     *
     *  @param exception The local exception which can be the result of network problems,
     *                   or problems with the XML payload and serialization.
     */

    public void onException( XmlRpcException exception );
}