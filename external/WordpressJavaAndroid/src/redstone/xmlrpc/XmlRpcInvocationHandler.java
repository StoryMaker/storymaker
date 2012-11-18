/*
    Copyright (c) 2006 by Redstone Handelsbolag
    All Rights Reserved.

    The copyright to the source code herein is the property of
    Redstone Handelsbolag. The source code may be used and/or copied only
    with written permission from Redstone or in accordance with
    the terms and conditions stipulated in the agreement/contract
    under which the source code has been supplied.
*/

package redstone.xmlrpc;

import java.util.List;

/**
 *  When receiving XML-RPC messages, XmlRpcServers parse the XML payload and dispatch
 *  control to XmlRpcInvocationHandlers that perform the actual processing.
 *
 *  <p>The XML-RPC library includes a reflective handler that other handlers may extend
 *  or use to wrap them up. @see serializers.ReflectiveInvocationHandler</p>
 *
 *  @author Greger Olsson
 */

public interface XmlRpcInvocationHandler
{
    /**
     *  Called by a dipatcher when an XML-RPC invocation has been received
     *  (and processed by any XmlRpcInvocationProcessors).
     * 
     *  @param method The name of the method that is to be invoked.
     *  @param arguments The arguments to supply to the method.
     *  @return The return value from the method.
     *  @throws Throwable Any kind of exception may occurr in the method.
     */
    
    Object invoke(
        String method,
        List arguments )
        throws Throwable;
}