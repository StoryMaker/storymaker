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

import java.io.Writer;
import java.util.List;

/**
 *  Contains information about a particular invocation intercepted
 *  by an invocation processor. Invocation objects are only used
 *  on the server side, and only when calling on invocation processors.
 *
 *  @author Greger Olsson
 */

public class XmlRpcInvocation
{
    /**
     *  @param invocationId The unique identity of the invocation.
     *  @param handlerName The name of the handler that was invoked.
     *  @param methodName The name of the method within the handler that was invoked.
     *  @param handler The invocation handler that was invoked.
     *  @param arguments Arguments used in the invocation.
     *  @param writer The java.io.Writer that the response will be written over.
     */
    public XmlRpcInvocation(
        int invocationId,
        String handlerName,
        String methodName,
        XmlRpcInvocationHandler handler,
        List arguments,
        Writer writer )
    {
        this.invocationId = invocationId;
        this.handlerName = handlerName;
        this.methodName = methodName;
        this.handler = handler;
        this.arguments = arguments;
        this.writer = writer;
    }
    
    
    /**
     *  A sequence number for tracing invocations between preProcess() and
     *  postProcess() calls. This is unique within each session. That is,
     *  the sequence is restarted when the application restarts.
     * 
     *  @return The sequence number of the call.
     */
    
    public int getInvocationId()
    {
        return invocationId;
    }

    
    /**
     *  Returns the name of the invocation handler targeted by the invocation.
     * 
     *  @return The name of the invocation handler targeted by the invocation.
     */
    
    public String getHandlerName()
    {
        return handlerName;
    }

    
    /**
     *  Returns the name of the method in the invocation handler targeted by the invocation.
     *  Using naming conventions for method names various types of filters and processors
     *  may be created.
     *  
     *  @return The name of the method in the invocation handler targeted by the invocation.
     */
    
    public String getMethodName()
    {
        return methodName;
    }


    /**
     *  Sets a new method name to be invoked instead of the
     *  original method name. This can be handy when using a
     *  naming convention where the public XML-RPC interface
     *  uses names with prefixes, for instance, which are stripped
     *  away before reaching the invocation handler, and so forth.
     *
     *  @param methodName The name of the method to user.
     */
    
    public void setMethodName( String methodName )
    {
        this.methodName = methodName;
    }
    
    
    /**
     *  Returns a list of arguments supplied in the invocation. This list may be modified
     *  by the processor. Arguments may be analyzed, modified, added or removed before a
     *  call is dispatched to the handler method.
     * 
     *  @return A list of arguments supplied in the invocation.
     */
    
    public List getArguments()
    {
        return arguments;
    }
    
    
    /**
     *  Returns the handler that will be or has been invoked. This information
     *  may be used in conjunction with the handler name and method name to
     *  achieve some filtering scheme or some other type of processing procedure.
     *
     *  @return Returns the handler that will be or has been invoked.
     */
    
    public XmlRpcInvocationHandler getHandler()
    {
        return handler;
    }
    

    /**
     *  Returns the writer that the response of the invocation will be written
     *  to. The interceptor may write custom content to the writer as a custom
     *  header to the original response or as a complete replacement to the
     *  response.
     *  
     *  @see XmlRpcInvocationInterceptor#after(XmlRpcInvocation, Object)
     * 
     *  @return The writer that the response will be written to.
     */

    public Writer getWriter()
    {
        return writer;
    }
    
    
    /** The unique identity of the invocation. */
    private int invocationId;
    
    /** The name of the handler that was invoked. */
    private String handlerName;
    
    /** The name of the method within the handler that was invoked. */
    private String methodName;
    
    /** The invocation handler that was invoked. */
    private XmlRpcInvocationHandler handler;
    
    /** Arguments used in the invocation. */
    private List arguments;
    
    /** The java.io.Writer that the response will be written over. */
    private Writer writer;
}
