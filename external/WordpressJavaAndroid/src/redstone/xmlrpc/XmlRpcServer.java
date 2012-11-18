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
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import redstone.xmlrpc.handlers.ReflectiveInvocationHandler;

/**
 *  An XmlRpcServer is responsible for hosting a set of invocation handlers and a set of
 *  invocation interceptors. It is invoked by calling the execute() method, supplying a
 *  stream containing the XML-RPC message to be handled. The messages will be parsed and
 *  dispatched to the corresponding invocation handler, if any.
 *
 *  <p>The XmlRpcServer may also be started as a service accepting connections on a given port.
 *  This way, a servlet environment is not required to be able to expose XML-RPC services.
 *  The server acts as a minimal HTTP server accepting text/xml posts containing XML-RPC
 *  messages, only.</p>
 *
 *  <p>For further information on setting up an XML-RPC server, see the documentation.</p>
 *
 *  @author Greger Olsson
 */

public class XmlRpcServer
{
    /**
     *  Default constructor using default serializer supporting the basic types as well
     *  the custom serializers.
     */

    public XmlRpcServer()
    {
        this.serializer = new XmlRpcSerializer();
    }
    

    /**
     *  Accepts a serializer to be used during serialization.
     *  
     *  @param serializer The serializer to use for response messages.
     */

    public XmlRpcServer( XmlRpcSerializer serializer )
    {
        this.serializer = serializer;
    }

    
    /**
     *  Dispatches the call contained in the supplied input stream. The stream should contain
     *  a proper XML message conforming to the XML-RPC specification.
     *
     *  @param xmlInput The stream containing the XML-RPC message.
     *  
     *  @param output The stream to put the response in.
     *
     *  @throws XmlRpcException if the input stream contains unparseable XML or if some error
     *          occurs in the SAX driver.
     */

    public void execute( InputStream xmlInput, Writer output ) throws XmlRpcException
    {
        XmlRpcDispatcher dispatcher = new XmlRpcDispatcher( this, "(unknown)" );
        dispatcher.dispatch( xmlInput, output );
    }

    
    /**
     *  Binds an invocation handler object to the given name.
     *
     *  @param name The name to bind the handler to.
     *
     *  @param handler The invocation handler object.
     */

    public void addInvocationHandler( String name, Object handler )
    {
        addInvocationHandler( name, new ReflectiveInvocationHandler( handler ) );
    }

    
    /**
     *  Binds an invocation handler object to the given name.
     *
     *  @param name The name to bind the handler to.
     *
     *  @param handler The invocation handler object.
     */

    public void addInvocationHandler( String name, XmlRpcInvocationHandler handler )
    {
        if ( name == null )
        {
            name = XmlRpcDispatcher.DEFAULT_HANDLER_NAME;
        }

        handlers.put( name, handler );
    }

    
    /**
     *  Returns the invocation handler with the given name.
     * 
     *  @param name The name of the invocation handler to return.
     *  @return The invocation handler with the given name.
     */

    public XmlRpcInvocationHandler getInvocationHandler( String name )
    {
        return ( XmlRpcInvocationHandler ) handlers.get( name );
    }


    /**
     *  Unbinds a previously bound invocation handler.
     *
     *  @param name The name of the handler to unbind.
     */

    public void removeInvocationHandler( String name )
    {
        handlers.remove( name );
    }


    /**
     *  Invocation interceptors are invoked on every call, regardless of which handler or
     *  method the call is intended for. Interceptors are useful for supplying entry points
     *  for logging and other utilities like transaction handling. For instance, a fictive
     *  TransactionalInvocationInterceptor may examine the name of the method called upon, and
     *  if it starts with "tx_" it may attach an additional transaction object in the
     *  argument vector.
     *
     *  @param interceptor An invocation interceptor that will be invoked on every call
     *                     sent to this server.
     */

    public void addInvocationInterceptor( XmlRpcInvocationInterceptor interceptor )
    {
        interceptors.add( interceptor );
    }

    
    /**
     *  Returns the incovation interceptors installed in this server.
     * 
     *  @return The incovation interceptors installed in this server.
     */

    public List getInvocationInterceptors()
    {
        return interceptors;
    }


    /**
     *  Removes a previously registered invocation interceptor. The interceptor will no
     *  longer be invoked on inbound calls.
     *
     *  @param interceptor The invocation interceptor to remove
     */

    public void removeInvocationInterceptor( XmlRpcInvocationInterceptor interceptor )
    {
        interceptors.remove( interceptor );
    }

    
    
    /**
     *  Returns the serializer this server is using to encode responses.
     * 
     *  @return The serializer this server is using.
     */

    public XmlRpcSerializer getSerializer()
    {
        return serializer;
    }

    
    /**
     *  Sets the serializer this server is to use when encoding responses.
     * 
     *  @param serializer The serializer this server shall use.
     */

    public void setSerializer( XmlRpcSerializer serializer )
    {
        this.serializer = serializer;
    }
    

    /** Invocation handlers registered in the server */
    private Map/*<String,XmlRpcInvocationHandler>*/ handlers = new HashMap();

    /** List of interceptors invoked when processing XML-RPC messages */
    private List/*<XmlRpcInvocationInterceptor>*/ interceptors = new ArrayList();
    
    /** The serializer used by the server */
    private XmlRpcSerializer serializer;
}