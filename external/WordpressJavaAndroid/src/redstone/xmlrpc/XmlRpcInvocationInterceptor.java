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
 *  Invocation processors are called by XmlRpcServers before and after a call is
 *  dispathed to its invocation handler. Processors are called for all inbound messages
 *  regardless of destination. They serve as appropriate entry points where logging
 *  and other type of processing may be inserted. For instance, a transaction
 *  processor may examine the name of the method being called, and if it starts
 *  with "tx_" it may add an additional transaction argument to the list of arguments
 *  received by the method. After the call is dispatched, the transaction may be
 *  commited or rolled back, in case of an error.
 *
 *  @author Greger Olsson
 */

public interface XmlRpcInvocationInterceptor
{
    /**
     *  Called by an XmlRpcServer before the method with the supplied name is called.
     *  The arguments list may be altered if necessary. The method must have matching
     *  parameters, however.
     *
     *  <p>If, for some reason, the processor wishes to cancel the invocation altogether, it
     *  returns false from this method. This may be the case for filtering processors where
     *  certain IP-addresses are restricted from particular methods, or if the arguments
     *  contain some encrypted password and username that does not authorize.</p>
     *
     *  @param invocation The invocation intercepted by the processor. 
     *
     *  @return true if the invocation should proceed, or false if not.
     */

    boolean before( XmlRpcInvocation invocation );


    /**
     *  Called by an XmlRpcServer after the supplied method has been called. The
     *  processor may alter the return value, if it wants, before the response is
     *  created. This may be useful if the information therein should be encrypted
     *  or compressed, for instance.
     *  
     *  <p>If the interceptor returns null it means that the call was intercepted
     *  completely and that the interceptor itself is responsible of generating
     *  a response to the caller through the java.io.Writer in the XmlRpcInvocation.
     *  This makes it possible to write interceptors that override the whole
     *  serialization mechanism to return customized content for invocations.</p>
     *
     *  @param invocation The invocation intercepted by the processor. 
     *
     *  @param returnValue The object returned by the method. If the method
     *                     returned a primitive, it is wrapped in its object counterpart.
     *
     *  @return The (possibly modified) returnValue argument, or null if the interceptor
     *          has intercepted the call completely and no value is to be returned.
     */

    Object after( XmlRpcInvocation invocation, Object returnValue );


    /**
     *  Called by an XmlRpcServer when the supplied method throws an exception.
     *
     *  @param invocation The invocation intercepted by the processor. 
     *  
     *  @param exception The exception thrown by the method.
     */

    void onException( XmlRpcInvocation invocation, Throwable exception );
}
